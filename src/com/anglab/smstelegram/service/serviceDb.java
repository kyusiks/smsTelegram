package com.anglab.smstelegram.service;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.anglab.smstelegram.NotesDbAdapter;
import com.anglab.smstelegram.aUtil;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

@SuppressLint("NewApi")
public class serviceDb extends Service {
	public static final String BASE_URL = "http://anglab.dothome.co.kr/smsTelegram/";
	public static final String URL_SYS = "a.php";
	public static final String URL_MSG = "sendMessage.php";
	public static final String URL_PHOTO = "sendPhoto.php";

	static Context context;
	private static NotesDbAdapter dbAdapter;
	public static boolean isServerRunning = false;
	public static boolean doRunningAgain  = false;

	@Override
	public IBinder onBind(Intent intent) { return null; }

	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
		dbAdapter = aUtil.getDB(context);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String vMethod = aUtil.nvl(intent.getStringExtra("met"));
		Log.d("onStartCommand", "vMethod : " + vMethod);

		if ( "mmsimg".equals(vMethod) ) {
			Log.d("onStartCommand", "mmsimg");
			List<HashMap<String, String>> vList = new ArrayList<>();
			HashMap<String, String> vData = new HashMap<>();

	        Bundle extras = intent.getExtras();
			for (String key : extras.keySet()) {
				Log.d("onStartCommand", key + " : " + extras.get(key));
				Object value = extras.get(key);
				vData.put(key, (value + "").toString());
			}

			vList = dbAdapter.inqSql("SEL_MMSIMG", vData); // �߼� ��� ��� ��ȸ
			/** 3 �� */
	
			Log.d("�߼۴��Ǽ�", "6/" + vList.size());

			/** 4 �߼� */
			if ( vList.size() > 0 && aUtil.isOnline(context) ) {
				(new BackgroundTask()).execute(vList);
			} else {
				if ( vList.size() == 0 ) {
					ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("mod"  , "sys"));
					nameValuePairs.add(new BasicNameValuePair("conId", extras.getString("conId")));
					nameValuePairs.add(new BasicNameValuePair("met"  , extras.getString("met"  )));
					nameValuePairs.add(new BasicNameValuePair("dpl"  , extras.getString("dpl"  )));
					nameValuePairs.add(new BasicNameValuePair("msgId", extras.getString("msgId")));
			    	String dd = aUtil.sendMessage(URL_MSG, nameValuePairs);
			    	Log.d("�̰�4", dd);
			    	
					//TODO �̰�������
				} else {
					if ( aUtil.isOnline(context) ) {
						//TODO �̰�������
					}
				}
			}

		} else if ( "test".equals(vMethod) ) {
			Log.d("onStartCommand", "test");
			List<HashMap<String, String>> vList = new ArrayList<>();
			HashMap<String, String> vData = new HashMap<>();
			vList = dbAdapter.inqSql("SELtext", vData); // �߼� ��� ��� ��ȸ
			/** 3 �� */
	
			Log.d("�߼۴��Ǽ�", "6/" + vList.size());

			/** 4 �߼� */
			if ( vList.size() > 0 && aUtil.isOnline(context) ) {
				(new BackgroundTask()).execute(vList);
			} else {
			}

		} else if ( "save".equals(vMethod) ) { // �ƴϸ� ���� �� ����
			Log.d("onStartCommand", "fn_save");
			HashMap<String, String> vData = new HashMap<String, String>();
			vData.put("MODE"   , intent.getStringExtra("MODE"   ));
			vData.put("DPL", intent.getStringExtra("DPL"));
			vData.put("MSG_TXT" , intent.getStringExtra("MSG_TXT" ));
			vData.put("RCV_DH"  , intent.getStringExtra("RCV_DH"  ));
			vData.put("SND_NUM" , intent.getStringExtra("SND_NUM" ));
			vData.put("SND_NM"  , intent.getStringExtra("SND_NM"  ));
			vData.put("RCV_NUM" , intent.getStringExtra("RCV_NUM" ));
			Log.d("onStartCommand :vData",vData.toString());
			fn_saveEach(vData); // phone, network
			
		} else {
			Log.d("onStartCommand", "fn_run");
			// ����Ʈ�� ������ �ణ�� ���� �ΰ� �����Ѵ�.
			// ���������� ���������� ���忡 �ð��� �ʿ��ϱ⶧����. ���� Ÿ�̹��� �������� �����϶�
			// �ϴ� 6���� ���� 30���� ���� 2ȸ.
	
			new Handler().postDelayed(new Runnable() {
			    @Override
			    public void run() {
					fn_run();
			    }
			}, 6 * 1000);
			new Handler().postDelayed(new Runnable() {
			    @Override
			    public void run() {
					fn_run();
			    }
			}, 30 * 1000);
		}

		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() { super.onDestroy(); }

	public static void fn_run() {
		try {
			Log.d("doRunningAgain / isServerRunning ", doRunningAgain+"/"+isServerRunning);
			//aUtil.isServiceRunningCheck(context);
			fn_runThread();

			/* fn_runThread() ó����, ���ο� ����Ʈ�� �Ǵٽ� fn_runThread()�� ȣ��ȴ�.
			 * ���� fn_runThread()�� ������ ���̶��, �ߺ� ������ ��������, ���� �Լ� ���Ḧ ��ٸ� �� ȣ���Ѵ�.
			 * ������ fn_runThread()�� ������ ���� ���� �ִµ� ����� ���͵� ������� 1������ ���Ѵ�.
			 * ���� �Լ� ���� �� ��ٷ� ����� �Ǵ°��� �ƴ϶� n�ʰ� ���� �ΰ� ������Ų��. 
			 * 
			 */
			if ( !isServerRunning && doRunningAgain ) {
				fn_run();
			}
			
		} catch (Exception e) {}
	}

	private static void fn_runThread() { // ������Ʈ���� �ִٸ� return true
		try {
			doRunningAgain = true; // �Ʒ����� false����, �� �Լ� ������ �ٽ� ����.
			if ( isServerRunning ) return;
			doRunningAgain = false; // �ٽý������� ����. ���Ŀ� �� �Լ� ������ �ٽ� ���Լ��� ȣ��ȴٸ� �ٽ� ���ؿ�����...
			isServerRunning = true; // ���� ������ ���۵ɶ� �ߺ� ���� ����

			/**
			 * 1 SMS, MMS ����� ������ DB�� ���̱׷��̼� 
			 */
			List<HashMap<String, String>> vList = new ArrayList<>();
			vList = aUtil.getSmsList(context);
			vList.addAll(aUtil.getMissCallList(context)); // ������ ��ȭ ���

			if ( vList.size() > 0 ) {
				for (int i = 0; i < vList.size(); i++) {
					fn_saveEach(vList.get(i));
				}
				/** 1 ��� ���̱׷��̼� �� */

				/** 
				 * 2 ���� DB�� ���� �߼۴���� �ۼ�(����)
				 */
				vList = dbAdapter.inqSql("SEL02", null);

				Log.d("ddD","vList :" + vList.size());
				if ( vList.size() > 0 ) {
					List<HashMap<String, String>> vParamList = new ArrayList<>();
					HashMap<String, String> vData = new HashMap<String, String>();
					for (int i = 0; i < vList.size(); i++) {
						vData = vList.get(i);
						// TODO ���͸� �̿��� TB_SMS02 �� �ִ� ������ �ִ´�. ������ ������ �ִ´�.
						if ("1".equals("1")) { // �����۵�. �߼۴���̸�
							String vRoomId = "159284966";
							vData.put("CID", vRoomId);
							vData.put("CON_ID", "2");
							vParamList.add(new HashMap<String, String>(vData));
						}
					}
					dbAdapter.updList("UPD01", vList); // ���ڸ�� �� ���� üũ �Ϸ�
					dbAdapter.updList("INS02", vParamList); // ���۸�� ����
				}
			}
			/** 2 �� */

			/**
			 * 3 �߼۴���� Ȯ��
			 */
			vList.clear();
			vList = dbAdapter.inqSql("SEL03", null); // �߼� ��� ��� ��ȸ
			/** 3 �� */

			Log.d("�߼۴��Ǽ�", "6/" + vList.size());
			Log.d("�߼۴��Ǽ�", "6/" + vList.toString());

			/** 4 �߼� */
			if ( vList.size() > 0 && aUtil.isOnline(context) ) {
				(new BackgroundTask()).execute(vList);
			}

		} catch (Exception e) { // ������ �������Ǹ� ����൵ ��ȿ
			doRunningAgain  = false;
		}

		new Handler().postDelayed(new Runnable() {
		    @Override
		    public void run() { isServerRunning = false;}
		}, 10 * 1000); // ���� Ʈ������ ���� 10 �� �� �Ϸ� ����.
	}

	/*****************************
	 * ��Ƽ������ ����
	 *****************************/
	static class BackgroundTask extends AsyncTask<List<HashMap<String, String>>, String, HashMap<String, String>> {
		protected void onPreExecute() {}

		protected HashMap<String, String> doInBackground(List<HashMap<String, String>>... values) {
			List<HashMap<String, String>> vList = values[0];
			HashMap<String, String> vData = null;

			String vVer = aUtil.getVersionName(context);
			
			for (int i = 0; i < vList.size(); i++) {
				try {
					vData = vList.get(i); // �Ϸ�� ù row�� ��� ���������Ƿ� �ε����� �׻� 0
	
					String vGubun = vData.get("MODE");
					if ( TextUtils.isEmpty(vGubun) ) continue;

					String vSndNum  = vData.get("SND_NUM" );
					String vSndNm   = URLEncoder.encode(vData.get("SND_NM" )+"", "utf-8");
					String vMsgTxt  = URLEncoder.encode(vData.get("MSG_TXT")+"", "utf-8");
					String vRcvDh   = vData.get("RCV_DH"  );
					String vRcvNum  = vData.get("RCV_NUM" );
					//String vRoomId  = vData.get("CID");
					String vDuplChk = vData.get("DPL");
					String vImg     = vData.get("IMG");
					String vConId   = vData.get("CON_ID");

					vData.put("LOC_OUT_DH", aUtil.getNow());
					dbAdapter.updList("UPD04", vData); // �߼� �õ� �ð� ����

					// --------------------------
					// URL �����ϰ� �����ϱ�
					// --------------------------

					ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("lid", "1"));
					//nameValuePairs.add(new BasicNameValuePair("cid", vRoomId));
					nameValuePairs.add(new BasicNameValuePair("msg", vMsgTxt));
					nameValuePairs.add(new BasicNameValuePair("num", vSndNum));
					nameValuePairs.add(new BasicNameValuePair("nam", vSndNm ));
					nameValuePairs.add(new BasicNameValuePair("myn", vRcvNum));
					nameValuePairs.add(new BasicNameValuePair("tim", vRcvDh ));
					nameValuePairs.add(new BasicNameValuePair("mod", vGubun ));
					nameValuePairs.add(new BasicNameValuePair("ver", vVer   ));
					nameValuePairs.add(new BasicNameValuePair("img", vImg   ));
					nameValuePairs.add(new BasicNameValuePair("dpl", vDuplChk));
					nameValuePairs.add(new BasicNameValuePair("conId", vConId));

					String method = "";
					if ( "img".equals(vGubun) ) {
						method = URL_PHOTO;
						if ( !aUtil.isWifi(context) ) continue;
						String[] vKey = vDuplChk.split("-"); // [0]�޽���id, [1]part_id
						Bitmap bitmap = aUtil.getMmsImage(context, vKey[1]);
						if ( bitmap == null ) continue;
	
						ByteArrayOutputStream bao = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
						byte [] ba = bao.toByteArray();
						String ba1 = Base64.encodeToString(ba, Base64.DEFAULT);
						nameValuePairs.add(new BasicNameValuePair("photo", ba1));
						nameValuePairs.add(new BasicNameValuePair("orgMsgId", vData.get("ORG_MSG_ID" )));
						nameValuePairs.add(new BasicNameValuePair("msgId", vData.get("MSG_ID")));
						
					} else {
						method = URL_MSG;
					}

					String content = aUtil.callUrl(method, nameValuePairs);

					Log.d("content", content);
					XmlPullParserFactory fatorry = XmlPullParserFactory.newInstance();
					fatorry.setNamespaceAware(true);
					XmlPullParser xpp = fatorry.newPullParser();
					xpp.setInput(new StringReader(content));
	
					int eventType = xpp.getEventType();
					String tagname = "";
					while ( eventType != XmlPullParser.END_DOCUMENT ) {
						if ( eventType == XmlPullParser.START_TAG ) {
							tagname = xpp.getName();
						} else if ( eventType == XmlPullParser.TEXT ) { // �±׺��� ����
							vData.put(aUtil.sectionFind(tagname), xpp.getText());
							Log.d("���ε�", aUtil.sectionFind(tagname) + " : " + xpp.getText());
						} else if ( eventType == XmlPullParser.END_TAG ) {
						}
						eventType = xpp.next();
					}
					Log.d("onPostExecute pData", vData.toString());
					
					
					//TODO �Ϸ�ð�������Ʈ Ȯ���� ����
					dbAdapter.updList("UPD02", vData); // �߼ۿϷ�
					chkUserUpdate(vData); // ����� �������� ���� Ȯ���� ���� ����

					if ( "mms".equals(vGubun) && !"".equals(vImg) ) {
						if ( "Y".equals(vData.get("AUTO_IMG_SND")) ) {
							HashMap<String, String> vImgData = new HashMap<>();
							vImgData.put("msgId", "");
							vImgData.put("orgMsgId", vData.get("MSG_ID"));

							String[] vImgArr = vImg.split("\\|");
							Log.d("imgs", vImg + "///");
							for ( String v1 : vImgArr ) {
								if ( "".equals(v1) ) continue;
								vImgData.put("dpl", vDuplChk + "-" + v1);
								vList.addAll(dbAdapter.inqSql("SEL_MMSIMG", vImgData)); // �߼� ��� ��� ��ȸ
							}
						}
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return vData;
		}

		protected void onPostExecute(HashMap<String, String> pData) {
			try {
				if ( pData != null ) {
					//Log.d("onPostExecute pData", pData.toString());
					//dbAdapter.updList("UPD02", pData); // �߼ۿϷ�
					//chkUserUpdate(pData); // ����� �������� ���� Ȯ���� ���� ����
				}
			} catch (Exception e) {
			} finally {
			}
		}

		protected void onProgressUpdate(String... values) {}
		protected void onCancelled() {}
	}

	// ����� �������� ���� Ȯ��
	public static boolean chkUserUpdate(HashMap<String, String> pData) {
		try {
			List<HashMap<String, String>> vList = new ArrayList<>();
			String vTrnUserSeq = aUtil.nvl(pData.get("CID_SEQ"));
			String vFstNm = aUtil.nvl(pData.get("FST_NM")); // DB����
			String vLstNm = aUtil.nvl(pData.get("LST_NM"));
			String vType = aUtil.nvl(pData.get("TYPE"));
	
			String vFstNmN = aUtil.nvl(pData.get("RT_FST_NM")); // WEB ���ϳ���
			String vLstNmN = aUtil.nvl(pData.get("RT_LST_NM"));
			String vTypeN = aUtil.nvl(pData.get("RT_TYPE"));
	
			if (vFstNm.equals(vFstNmN) && vLstNm.equals(vLstNmN) && vType.equals(vTypeN)) {
				return false; // user �������� �����. pass
			} else { // user ���������־� ���� ����
				BigDecimal vBigTrnUserSeq = new BigDecimal(vTrnUserSeq);
				vTrnUserSeq = vBigTrnUserSeq.add(BigDecimal.ONE).toString();
				pData.put("CID_SEQ", vTrnUserSeq);
				pData.put("FST_NM", vFstNmN);
				pData.put("LST_NM", vLstNmN);
				pData.put("TYPE", vTypeN);
				vList.add(pData);
				dbAdapter.updList("UPD03", vList); // ��������� ������Ʈ(�����ǻ���)
				dbAdapter.updList("INS03", vList); // ��������� ������Ʈ(�ű԰��μ�Ʈ)
				return true;
			}
		} catch (Exception e) { e.printStackTrace(); return false; }
	}

	public static void fn_saveEach(HashMap<String, String> pData) {
		String vGubun   = aUtil.nvl(pData.get("MODE"    ));
		String vDuplChk = aUtil.nvl(pData.get("DPL"     ));
		String vMsgTxt  = aUtil.nvl(pData.get("MSG_TXT" ));
		String vRcvDh   = aUtil.nvl(pData.get("RCV_DH"  ));
		String vSndNum  = aUtil.nvl(pData.get("SND_NUM" )).replaceAll("\\D", "");
		String vSndNm   = aUtil.nvl(pData.get("SND_NM"  ));
		String vRcvNum  = aUtil.nvl(pData.get("RCV_NUM" )).replaceAll("\\D", "");

		Log.d("fn_saveEach : vGubun", vGubun + "/" + vDuplChk + "/" + vMsgTxt + "/" + vSndNum + "/" + vSndNm + "/" + vRcvNum + "/" + vRcvDh);

		if ( "".equals(vGubun ) ) return;
		if ( "".equals(vSndNm ) ) vSndNm  = aUtil.getNameByNumber(vSndNum, context);
		if ( "".equals(vRcvNum) ) vRcvNum = aUtil.getMyNumber(context);
		if ( "".equals(vRcvDh ) ) vRcvDh  = aUtil.getNow();
		if ( vRcvDh.length() == 10) vRcvDh += "000";

		HashMap<String, String> vData = new HashMap<String, String>();
		vData.put("MODE", vGubun); // sms/mms/bat(���͸�)/cha(����)/mic(��������)
		vData.put("DPL", vDuplChk); // �ߺ�üũŰ

		List<HashMap<String, String>> vList = new ArrayList<>();
		//TODO ���ο� �ߺ�üũ���� vList = dbAdapter.inqSql("SEL01", vData); // ���� ����� �޽����ΰ�.
		//if ( vList.size() == 0 || !"0".equals(vList.get(0).get("CNT")) ) return; // �ߺ��̸� ����.
		Log.d("SEL01!!!!!!!!!", vList.size() + "��");
		vData.put("SND_NUM", vSndNum);
		vData.put("SND_NM" , vSndNm );
		vData.put("RCV_NUM", vRcvNum);
		vData.put("RCV_DH" , vRcvDh );
		vData.put("MSG_TXT", vMsgTxt);

		//vList.clear();
		//vList.add(vData);
		dbAdapter.updList("INS01", vData); // ����DB�� ������Ʈ�Ұ������� ����
	}

	public static void fn_missCallrun() {
		Log.d("fn_missCallrun", "����");
		aUtil.getMissCallList(context);
		Log.d("fn_missCallrun", "��");
	}
}