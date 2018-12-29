package com.anglab.smstelegram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.anglab.smstelegram.service.serviceDb;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public class aUtil extends Activity {

    private static NotesDbAdapter dbAdapter;
    public static String gvTelNum;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        dbAdapter = new NotesDbAdapter(this);
        dbAdapter.open();
	}

	public static NotesDbAdapter getDB(Context context) {
		if ( dbAdapter == null ) {
	        dbAdapter = new NotesDbAdapter(context);
	        dbAdapter.open();
		}
		return dbAdapter;
	}

    public static String getVersionName(Context context) {
        try {
            PackageInfo pi= context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException e) {
            return "";
        }
    }

    public static String[] vValueBefore = { "A" , "B"        , "C"        , "D"      , "E"         , "F"     , "G"      , "H"           , "I"       
			                              , "J"       , "K"     , "L"        , "M"   , "N"   , "O"         , "P"         , "Q"     , "R"
			                              , "S"     , "T"      , "U"};
    public static String[] vValueAfter  = { "OK", "RT_FST_NM", "RT_LST_NM", "RT_TYPE", "TL_COMP_DH", "ERR_CD", "ERR_DSC", "AUTO_IMG_SND", "MSG_ID"
			                              , "SET_CONT", "SET_ID", "SET_VALUE", "SITE", "SORT", "THUMB_COMN", "THUMB_NAIL", "USE_YN", "CNT"
			                              , "ARTIST", "SELL_YN", "ORG_UPD_DH"};

    // 트레픽을 줄이기위해 변수를 약어로 바꿨다. 치환해주는 역활
    public static String sectionFind(String pTagname) {
		for ( int i = 0; i < vValueBefore.length; i++ ) {
			if ( pTagname.equals(vValueBefore[i]) ) return vValueAfter[i];
		}
		return pTagname;
	}
    
    public static void alertD(Context context, String pStr) { // 다이얼로그 얼럿
	    AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);
	    alt_bld.setMessage(pStr)
				.setCancelable(false)
				.setPositiveButton("YES",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = alt_bld.create();
		alert.setTitle("R.string.str_alert");
		alert.show();
    }

	public static String getMyNumber(Context context) {
		String vMyNumber = "";
        try { // 이름은 에러나도 그냥 진행.
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        	vMyNumber = tm.getLine1Number();   //폰번호
    	} catch (Exception e) { e.printStackTrace(); }
        return vMyNumber;
    }

	public static String getNameByNumber(String pSndNum, Context context) {
        String vName = "";
		try {
	        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	        String[] projection = new String[] {
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID, // 연락처 ID -> 사진 정보 가져오는데 사용
				ContactsContract.CommonDataKinds.Phone.NUMBER, // 연락처
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME }; // 연락처 // 이름.
	        String where = "TRIM(REPLACE(" + ContactsContract.CommonDataKinds.Phone.NUMBER + ", '-', '')) = TRIM('" + pSndNum + "')";
//Log.d("ffffff", where);
	        Cursor contactCursor = context.getContentResolver().query(uri, projection, where, null, null);

	        if ( contactCursor.getCount() > 0 ) {
	            while (contactCursor.moveToNext()) {
	                vName = contactCursor.getString(2);
	                //if ( pSndNum.equals(contactCursor.getString(1).replaceAll("\\D", "")) ) break; // 완전 일치하면 이거다!
	            }
	        } // 완전 일치 안하면 그냥 리스트중에서 적당한게 리턴.
	        contactCursor.close();
		} catch (Exception e) { }
        return vName;
    }

	// 현재시간을 timestamp 형식으로
	public static String getNow() {
		return (System.currentTimeMillis()+"").substring(0, 10);
	}

	public static String nvl(String pStr) {
		return nvl(pStr, "");
	}
	public static String nvl(String pStr, String pReplace) {
		if ( TextUtils.isEmpty(pReplace) ) pReplace = ""; 
		if ( TextUtils.isEmpty(pStr) ) return pReplace;
		if ( "null".equals(pStr) ) return pReplace;
		return pStr;
	}
	
	public static boolean isWifi(Context context) {
		try {
			ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if ( wifi.isConnected() ) { // WIFI
				Log.d("net", "wifi connect success");
				return true;
			} else {	
			    Log.d("net", "wifi connect fail");
				return false;
			}
		} catch (Exception e) { return false; }
	}

	public static boolean isOnline(Context context) {
		try {

		    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo netInfo = cm.getActiveNetworkInfo();
		    //should check null because in air plan mode it will be null
		    return (netInfo != null && netInfo.isConnected());

		    
		    /*
			ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if ( mobile.isConnected() || wifi.isConnected() ) {
				// WIFI, 3G 어느곳에도 연결되지 않았을때 
				Log.d("net", "Network connect success");
				return true;
			} else {	
			    Log.d("net", "Network connect fail");
				return false;
			}*/
		} catch (Exception e) { return true; }
	}
	
	// Cursor를 List로 바꿔준다.
	public static List<HashMap<String, String>> cur2list (Cursor result) {
		List<HashMap<String, String>> vList = new ArrayList<>();
		if ( result.getCount() > 0 ) {
			result.moveToFirst();
			HashMap<String, String> data = new HashMap<String, String>();
			while ( !result.isAfterLast() ) {
				data.clear();
				for (int i = 0; i < result.getColumnCount(); i++) {
					data.put(result.getColumnName(i), result.getString(i));
				}
				//Log.d("cur2list", data.toString());
				vList.add(new HashMap<String, String>(data));
				result.moveToNext();
			}
		}
		return vList;
	}

	public static String convDate(String pTimestamp) {
		try {
			//Log.d("convDate", (pTimestamp + ""));
			if ( TextUtils.isEmpty(pTimestamp) ) return "nope"; 
			Long vTimestamp = Long.parseLong(pTimestamp);
			if ( vTimestamp < 10000000000.0 ) vTimestamp = vTimestamp * 1000;

		    Timestamp currentTime = new Timestamp(vTimestamp);
		    Date date = new Date(currentTime.getTime());
		    
		    SimpleDateFormat sdfCurrent = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
			return sdfCurrent.format(date);
		} catch(Exception e) {return pTimestamp;} 
	}


	public static boolean isServiceRunningCheck(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
		for ( RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE) ) {
			Log.d("isServiceRunningCheck", service.service.getClassName());
			if ( "ServiceName".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}



	public static List<HashMap<String, String>> getSmsList(Context context) {
		List<HashMap<String, String>> vList = new ArrayList<>();
		vList = dbAdapter.inqSql("SEL05", null);

		String vMaxDh = nvl(vList.get(0).get("RCV_DH"));
		if ( "".equals(vMaxDh) ) vMaxDh = (System.currentTimeMillis() - (long)( 60*60*24*3 * 1000)) + ""; //TODO 3일분
		if ( vMaxDh.length() == 10 ) vMaxDh += "000";

		//Log.d("getSmsList vMaxDh", vMaxDh);
		Uri uri = Uri.parse("content://mms-sms/conversations/");
		final String[] projection = new String[] { "_id", "normalized_date", "m_id", "body", "address", "sub", "type"};

		Cursor result = context.getContentResolver().query(uri, projection, "normalized_date > " + vMaxDh + " and ( type is null or type = 1 ) and ( msg_box is null or msg_box = 1 ) ", null,
				"normalized_date desc limit 0,1000"); // 미발송은 아무리 많아도 10개까지만

		//Log.d("getSmsList 이거", "" + result.getCount());
		//Log.d("ddd", cur2list(result).toString() );

		vList.clear();
		int aa = 0;
		if ( result.getCount() > 0 ) {
			result.moveToFirst(); // 사이트를 키로 이미지뷰어, 썸네일 주소가 저장된다.
			HashMap<String, String> data = new HashMap<String, String>();
			String vId = "";
			while (!result.isAfterLast()) {
				data.clear();
				vId = result.getString(result.getColumnIndex("_id"));

				if ( !TextUtils.isEmpty(result.getString(result.getColumnIndex("m_id"))) ) { // MMS인경우
					String vNum = parseNumber(context, vId);
					if ( TextUtils.isEmpty(vNum) ) continue; // 경험상 전화번호 없는 건은 전송중인 MMS
					data.put("MODE", "mms");
					data.put("SND_NUM", vNum);
					String vSub = aUtil.nvl(result.getString(result.getColumnIndex("sub")));
					if ( !TextUtils.isEmpty(vSub) ) {
						try {
							vSub = (new String(vSub.getBytes("iso-8859-1"), "UTF-8"));
							data.put("vSub", vSub); // MMS에 이미지가 있다면 넣기위해서.
							vSub = "<" + vSub + ">\n\n";
						} catch (Exception e) {} // 한글 인코딩 잡아준다.
					}
					data.put("MSG_TXT", vSub + parseMessage(context, vId)); // 제목+내용

				} else { // SMS인경우
					data.put("MODE", "sms");
					data.put("SND_NUM", result.getString(result.getColumnIndex("address")));
					data.put("MSG_TXT", result.getString(result.getColumnIndex("body")));
				}
				data.put("DPL", vId);
				data.put("RCV_DH", result.getString(result.getColumnIndex("normalized_date")));

				Log.d("getSmsList 이2거", "" + data.toString());

				if ( !TextUtils.isEmpty(data.get("SND_NUM")) ) vList.add(new HashMap<String, String>(data));
				if ( "mms".equals(data.get("MODE")) ) {
					List<HashMap<String, String>> vImgList = parseImage(context, data); // mms이미지가 있다면 저장
					if ( vImgList.size() > 0 ) vList.addAll(vImgList);
				}
				result.moveToNext();
				//if ( aa++ > 10 ) break;
			}
		}
		result.close();
		
		return vList;
	}


	public static List<HashMap<String, String>> getMissCallList(Context context) {
		List<HashMap<String, String>> vList = new ArrayList<>();
		vList = dbAdapter.inqSql("SEL05", null);

		String vMaxDh = nvl(vList.get(0).get("RCV_DH"));
		if ( "".equals(vMaxDh) ) vMaxDh = (System.currentTimeMillis() - (long)( 60*60*24*3 * 1000)) + ""; //TODO 3일분
		if ( vMaxDh.length() == 10 ) vMaxDh += "000";

		String[] projection = { "_id", "date", "number", "name" };
		Cursor result = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, projection, CallLog.Calls.TYPE + "= ? and date > ? ",
		                new String[]{ String.valueOf(CallLog.Calls.MISSED_TYPE), vMaxDh }, CallLog.Calls.DEFAULT_SORT_ORDER);

		vList.clear();
		int aa = 0;
		if ( result.getCount() > 0 ) {
			result.moveToFirst(); // 사이트를 키로 이미지뷰어, 썸네일 주소가 저장된다.
			HashMap<String, String> data = new HashMap<String, String>();
			while (!result.isAfterLast()) {
				data.clear();
				data.put("MODE", "mis");
				data.put("SND_NUM", result.getString(result.getColumnIndex("number")));
				data.put("SND_NM" , nvl(result.getString(result.getColumnIndex("name"))));
				data.put("MSG_TXT", "");
				data.put("DPL", result.getString(result.getColumnIndex("_id")));
				data.put("RCV_DH", result.getString(result.getColumnIndex("date")));

				if ( !TextUtils.isEmpty(data.get("SND_NUM")) ) vList.add(new HashMap<String, String>(data));
				result.moveToNext();
				//if ( aa++ > 10 ) break;
			}
		}
		result.close();
		return vList;
	}

	public static String sendMessage(String pMethod, ArrayList<NameValuePair> pNameValuePairs) {
		try {
			/*
			ArrayList<NameValuePair> nameValuePa1irs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("mod"   , "gcm"));
			nameValuePairs.add(new BasicNameValuePair("conId" , extras.getString("conId" )));
			nameValuePairs.add(new BasicNameValuePair("method", extras.getString("method")));
			nameValuePairs.add(new BasicNameValuePair("dpl"   , extras.getString("dpl"   )));
			nameValuePairs.add(new BasicNameValuePair("msgId" , extras.getString("msgId" )));
			nameValuePairs.add(new BasicNameValuePair("orgMsgId", extras.getString("orgMsgId")));
			nameValuePairs.add(new BasicNameValuePair("lid", "1"));
			nameValuePairs.add(new BasicNameValuePair("cid", vRoomId));
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
			*/

			pNameValuePairs.add(new BasicNameValuePair("sendMessageForJavaUrl", pMethod));
			Log.d("ffF", pNameValuePairs.toString()); //TODO 여기할차례

			String ddd = (new sendMessage()).execute(pNameValuePairs).get();
			Log.d("이거3,", ddd+"ㄹㄹㄹㄹㄹㄹ"+ddd);
			
			return ddd;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	// AsyncTask<Params,Progress,Result>
	static class sendMessage extends AsyncTask<ArrayList<NameValuePair>, Void, String> {

		protected String doInBackground(ArrayList<NameValuePair>... pNameValuePairs) {
			ArrayList<NameValuePair> nvPairs = pNameValuePairs[0];
			String content = "";
	        try {
	        	String vUrl = "";
				for ( NameValuePair ss : nvPairs ) {
					if ( "sendMessageForJavaUrl".equals(ss.getName()) ) vUrl = ss.getValue();
				}
	        	callUrl(vUrl, nvPairs);
	        } catch(Exception e) {
	        	e.printStackTrace();
        	}
			return content;
		}

		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}
	}

	public static String callUrl(String pUrl, ArrayList<NameValuePair> nvPairs) {
		String content = "";
        try {
        	String vBaseUrl = serviceDb.BASE_URL;
        	//TODO if pUrl 이 http로 시작하면 vBaseUrl = ""
			if ( "".equals(pUrl) ) return null;//TODO 에러처리로직
			
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(vBaseUrl + pUrl);
			post.setEntity(new UrlEncodedFormEntity(nvPairs));
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			content = EntityUtils.toString(entity);
        } catch(Exception e) {
        	e.printStackTrace();
    	}
		return content;
	}
	
	
	
	
	
	
	private static String parseNumber(Context context, String $id) {
		String result = null;
		Uri uri = Uri.parse(MessageFormat.format("content://mms/{0}/addr", $id));
		String[] projection = new String[] { "address" };
		String selection = "msg_id = ? and type = 137";// type=137은 발신자
		String[] selectionArgs = new String[] { $id };
		Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
				"_id asc limit 1");

		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			result = cursor.getString(cursor.getColumnIndex("address"));
		}
		cursor.close();
		return result;
	}

	private static String parseMessage(Context context, String $id) {
		String result = null;
		// 조회에 조건을 넣게되면 가장 마지막 한두개의 mms를 가져오지 않는다.
		Cursor cursor = context.getContentResolver().query(Uri.parse("content://mms/part"),
				new String[] { "mid", "_id", "ct", "_data", "text" }, "mid=?", new String[] { $id }, null);

		if ( cursor.getCount() > 0 ) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				String partId = cursor.getString(cursor.getColumnIndex("_id"));
				String type = cursor.getString(cursor.getColumnIndex("ct"));
				if ( "text/plain".equals(type) ) {
					String data = cursor.getString(cursor.getColumnIndex("_data"));
					if ( TextUtils.isEmpty(data) ) {
						result = cursor.getString(cursor.getColumnIndex("text"));
					} else {
						result = parseMessageWithPartId(context, partId);
					}
				}
				cursor.moveToNext();
			}
		}
		cursor.close();
		return result;
	}

	private static String parseMessageWithPartId(Context context, String pPartId) {
		Uri partURI = Uri.parse("content://mms/part/" + pPartId);
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		try {
			is = context.getContentResolver().openInputStream(partURI);
			if ( is != null ) {
				InputStreamReader isr = new InputStreamReader(is, "UTF-8");
				BufferedReader reader = new BufferedReader(isr);
				String temp = reader.readLine();
				while (!TextUtils.isEmpty(temp)) {
					sb.append(temp);
					temp = reader.readLine();
				}
			}
		} catch (IOException e) {
		} finally {
			if (is != null) { try { is.close(); } catch (IOException e) {} }
		}
		return sb.toString();
	}

	// MMS 이미지
	private static List<HashMap<String, String>> parseImage(Context context, HashMap<String, String> pData) {
		String vId = pData.get("DPL");
		List<HashMap<String, String>> vList = new ArrayList<>();

		Cursor cursor = context.getContentResolver().query(Uri.parse("content://mms/part"),
				new String[] { "mid", "_id", "ct", "_data", "text" }, "mid=?", new String[] { vId }, null);

		if ( cursor.getCount() > 0 ) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				String type = cursor.getString(cursor.getColumnIndex("ct"));
				if ( "image/gif".equals(type) || "image/bmp".equals(type) 
		          || "image/jpg".equals(type) || "image/jpeg".equals(type)
		          || "image/png".equals(type)) {
					HashMap<String, String> vData = new HashMap<String, String>();
					vData.put("MODE"   , "img");
					vData.put("DPL", vId + "-" + cursor.getString(cursor.getColumnIndex("_id")));
					vData.put("MSG_TXT" , pData.get("vSub" ));
					vData.put("RCV_DH"  , pData.get("RCV_DH" ));
					vData.put("SND_NUM" , pData.get("SND_NUM"));
					
					vList.add(new HashMap<String, String>(vData));
				}
				cursor.moveToNext();
			}
		}
		cursor.close();
		return vList;
	}

	// MMS 이미지
	public static Bitmap getMmsImage(Context context, String pPartId) {
	    Uri partURI = Uri.parse("content://mms/part/" + pPartId);
	    InputStream is = null;
	    Bitmap bitmap = null;
	    try {
	        is = context.getContentResolver().openInputStream(partURI);
	        bitmap = BitmapFactory.decodeStream(is);
	    } catch (IOException e) {
	    } finally {
	        if (is != null) { try { is.close(); } catch (IOException e) {} }
	    }
	    return bitmap;
	}
	
}
