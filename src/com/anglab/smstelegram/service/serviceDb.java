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

			vList = dbAdapter.inqSql("SEL_MMSIMG", vData); // 발송 대상 목록 조회
			/** 3 끝 */
	
			Log.d("발송대상건수", "6/" + vList.size());

			/** 4 발송 */
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
			    	Log.d("이거4", dd);
			    	
					//TODO 이거할차례
				} else {
					if ( aUtil.isOnline(context) ) {
						//TODO 이거할차례
					}
				}
			}

		} else if ( "test".equals(vMethod) ) {
			Log.d("onStartCommand", "test");
			List<HashMap<String, String>> vList = new ArrayList<>();
			HashMap<String, String> vData = new HashMap<>();
			vList = dbAdapter.inqSql("SELtext", vData); // 발송 대상 목록 조회
			/** 3 끝 */
	
			Log.d("발송대상건수", "6/" + vList.size());

			/** 4 발송 */
			if ( vList.size() > 0 && aUtil.isOnline(context) ) {
				(new BackgroundTask()).execute(vList);
			} else {
			}

		} else if ( "save".equals(vMethod) ) { // 아니면 저장 후 실행
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
			// 인텐트가 들어오면 약간의 텀을 두고 실행한다.
			// 리졸버에서 가져오려면 저장에 시간이 필요하기때문에. 실행 타이밍은 경험으로 세팅하라
			// 일단 6초후 실행 30초후 실행 2회.
	
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

			/* fn_runThread() 처리중, 새로운 인텐트로 또다시 fn_runThread()가 호출된다.
			 * 이전 fn_runThread()가 끝나기 전이라면, 중복 실행을 막기위해, 이전 함수 종료를 기다린 후 호출한다.
			 * 이후의 fn_runThread()가 여러번 들어올 수도 있는데 몇번을 들어와도 재실행은 1번으로 한한다.
			 * 이전 함수 종료 후 곧바로 재실행 되는것이 아니라 n초간 텀을 두고 재실행시킨다. 
			 * 
			 */
			if ( !isServerRunning && doRunningAgain ) {
				fn_run();
			}
			
		} catch (Exception e) {}
	}

	private static void fn_runThread() { // 업데이트값이 있다면 return true
		try {
			doRunningAgain = true; // 아래에서 false나면, 이 함수 종료후 다시 실행.
			if ( isServerRunning ) return;
			doRunningAgain = false; // 다시실행하지 말것. 추후에 본 함수 실행중 다시 이함수가 호출된다면 다시 실해오디기로...
			isServerRunning = true; // 이하 로직이 시작될때 중복 실행 배제

			/**
			 * 1 SMS, MMS 목록을 어플의 DB로 마이그레이션 
			 */
			List<HashMap<String, String>> vList = new ArrayList<>();
			vList = aUtil.getSmsList(context);
			vList.addAll(aUtil.getMissCallList(context)); // 부재중 전화 목록

			if ( vList.size() > 0 ) {
				for (int i = 0; i < vList.size(); i++) {
					fn_saveEach(vList.get(i));
				}
				/** 1 목록 마이그레이션 끝 */

				/** 
				 * 2 위의 DB를 토대로 발송대상목록 작성(필터)
				 */
				vList = dbAdapter.inqSql("SEL02", null);

				Log.d("ddD","vList :" + vList.size());
				if ( vList.size() > 0 ) {
					List<HashMap<String, String>> vParamList = new ArrayList<>();
					HashMap<String, String> vData = new HashMap<String, String>();
					for (int i = 0; i < vList.size(); i++) {
						vData = vList.get(i);
						// TODO 필터를 이용해 TB_SMS02 에 넣는 로직을 넣는다. 지금은 무조건 넣는다.
						if ("1".equals("1")) { // 필터작동. 발송대상이면
							String vRoomId = "159284966";
							vData.put("CID", vRoomId);
							vData.put("CON_ID", "2");
							vParamList.add(new HashMap<String, String>(vData));
						}
					}
					dbAdapter.updList("UPD01", vList); // 문자목록 중 전송 체크 완료
					dbAdapter.updList("INS02", vParamList); // 전송목록 제작
				}
			}
			/** 2 끝 */

			/**
			 * 3 발송대상목록 확보
			 */
			vList.clear();
			vList = dbAdapter.inqSql("SEL03", null); // 발송 대상 목록 조회
			/** 3 끝 */

			Log.d("발송대상건수", "6/" + vList.size());
			Log.d("발송대상건수", "6/" + vList.toString());

			/** 4 발송 */
			if ( vList.size() > 0 && aUtil.isOnline(context) ) {
				(new BackgroundTask()).execute(vList);
			}

		} catch (Exception e) { // 에러로 마무리되면 재실행도 무효
			doRunningAgain  = false;
		}

		new Handler().postDelayed(new Runnable() {
		    @Override
		    public void run() { isServerRunning = false;}
		}, 10 * 1000); // 이전 트랜젝션 종료 10 초 후 완료 보고.
	}

	/*****************************
	 * 멀티스레드 시작
	 *****************************/
	static class BackgroundTask extends AsyncTask<List<HashMap<String, String>>, String, HashMap<String, String>> {
		protected void onPreExecute() {}

		protected HashMap<String, String> doInBackground(List<HashMap<String, String>>... values) {
			List<HashMap<String, String>> vList = values[0];
			HashMap<String, String> vData = null;

			String vVer = aUtil.getVersionName(context);
			
			for (int i = 0; i < vList.size(); i++) {
				try {
					vData = vList.get(i); // 완료된 첫 row를 계속 지워나가므로 인덱스가 항상 0
	
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
					dbAdapter.updList("UPD04", vData); // 발송 시도 시간 저장

					// --------------------------
					// URL 설정하고 접속하기
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
						String[] vKey = vDuplChk.split("-"); // [0]메시지id, [1]part_id
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
						} else if ( eventType == XmlPullParser.TEXT ) { // 태그별로 저장
							vData.put(aUtil.sectionFind(tagname), xpp.getText());
							Log.d("바인딩", aUtil.sectionFind(tagname) + " : " + xpp.getText());
						} else if ( eventType == XmlPullParser.END_TAG ) {
						}
						eventType = xpp.next();
					}
					Log.d("onPostExecute pData", vData.toString());
					
					
					//TODO 완료시간업데이트 확인할 차례
					dbAdapter.updList("UPD02", vData); // 발송완료
					chkUserUpdate(vData); // 사용자 프로파일 변경 확인을 위한 로직

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
								vList.addAll(dbAdapter.inqSql("SEL_MMSIMG", vImgData)); // 발송 대상 목록 조회
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
					//dbAdapter.updList("UPD02", pData); // 발송완료
					//chkUserUpdate(pData); // 사용자 프로파일 변경 확인을 위한 로직
				}
			} catch (Exception e) {
			} finally {
			}
		}

		protected void onProgressUpdate(String... values) {}
		protected void onCancelled() {}
	}

	// 사용자 프로파일 변경 확인
	public static boolean chkUserUpdate(HashMap<String, String> pData) {
		try {
			List<HashMap<String, String>> vList = new ArrayList<>();
			String vTrnUserSeq = aUtil.nvl(pData.get("CID_SEQ"));
			String vFstNm = aUtil.nvl(pData.get("FST_NM")); // DB내용
			String vLstNm = aUtil.nvl(pData.get("LST_NM"));
			String vType = aUtil.nvl(pData.get("TYPE"));
	
			String vFstNmN = aUtil.nvl(pData.get("RT_FST_NM")); // WEB 리턴내용
			String vLstNmN = aUtil.nvl(pData.get("RT_LST_NM"));
			String vTypeN = aUtil.nvl(pData.get("RT_TYPE"));
	
			if (vFstNm.equals(vFstNmN) && vLstNm.equals(vLstNmN) && vType.equals(vTypeN)) {
				return false; // user 변동내역 없어요. pass
			} else { // user 변동내역있어 새로 저장
				BigDecimal vBigTrnUserSeq = new BigDecimal(vTrnUserSeq);
				vTrnUserSeq = vBigTrnUserSeq.add(BigDecimal.ONE).toString();
				pData.put("CID_SEQ", vTrnUserSeq);
				pData.put("FST_NM", vFstNmN);
				pData.put("LST_NM", vLstNmN);
				pData.put("TYPE", vTypeN);
				vList.add(pData);
				dbAdapter.updList("UPD03", vList); // 사용자정보 업데이트(기존건삭제)
				dbAdapter.updList("INS03", vList); // 사용자정보 업데이트(신규건인서트)
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
		vData.put("MODE", vGubun); // sms/mms/bat(배터리)/cha(충전)/mic(부재중콜)
		vData.put("DPL", vDuplChk); // 중복체크키

		List<HashMap<String, String>> vList = new ArrayList<>();
		//TODO 새로운 중복체크로직 vList = dbAdapter.inqSql("SEL01", vData); // 기존 저장된 메시지인가.
		//if ( vList.size() == 0 || !"0".equals(vList.get(0).get("CNT")) ) return; // 중복이면 리턴.
		Log.d("SEL01!!!!!!!!!", vList.size() + "건");
		vData.put("SND_NUM", vSndNum);
		vData.put("SND_NM" , vSndNm );
		vData.put("RCV_NUM", vRcvNum);
		vData.put("RCV_DH" , vRcvDh );
		vData.put("MSG_TXT", vMsgTxt);

		//vList.clear();
		//vList.add(vData);
		dbAdapter.updList("INS01", vData); // 로컬DB에 업데이트할게있으면 업뎃
	}

	public static void fn_missCallrun() {
		Log.d("fn_missCallrun", "시작");
		aUtil.getMissCallList(context);
		Log.d("fn_missCallrun", "끝");
	}
}