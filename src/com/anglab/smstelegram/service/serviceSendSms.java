package com.anglab.smstelegram.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.anglab.smstelegram.NotesDbAdapter;
import com.anglab.smstelegram.aUtil;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

public class serviceSendSms extends Service {

	static Context context;
	private static NotesDbAdapter dbAdapter;

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
		Log.d("onStartCommand", "sndsms");
		String vMsg = aUtil.nvl(intent.getStringExtra("msg"));
		String vOrgMsgId = aUtil.nvl(intent.getStringExtra("orgMsgId"));
		String vDpl = aUtil.nvl(intent.getStringExtra("dpl"));
		String vMsgId = aUtil.nvl(intent.getStringExtra("msgId"));
		String vMod = aUtil.nvl(intent.getStringExtra("mod"));
		
		String vSndNum = aUtil.nvl(intent.getStringExtra("num"));
		String vConId = aUtil.nvl(intent.getStringExtra("conId"));
		String vNm = "";


        Bundle extras = intent.getExtras();
		HashMap<String, String> vData = new HashMap<>();
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for ( String key : extras.keySet() ) {
			vData.put(key, extras.get(key).toString());
			nameValuePairs.add(new BasicNameValuePair(key, extras.get(key).toString()));
		}

		List<HashMap<String, String>> list = new ArrayList<>();

		if ( "".equals(vSndNum) ) { // 답장으로 보내는 sms
			list = dbAdapter.inqSql("SEL_SNDSMS", vData);

			Log.d("list3", list.toString());
			Log.d("vData3", vData.toString());
			
			if ( list.size() > 0 ) {
				vSndNum = list.get(0).get("SND_NUM");
				vNm = list.get(0).get("SND_NM");
			}
		} else { // 직접 명령어로 보내는 sms
			vNm = aUtil.getNameByNumber(vSndNum, context);
		}
		
    	Log.d("11", "22/"+vSndNum+"/"+vNm);
    	
		if ( "".equals(vSndNum) ) { // 번호가 유효치 않을때.
			nameValuePairs.add(new BasicNameValuePair("mod", "sys"));
			nameValuePairs.add(new BasicNameValuePair("tim", intent.getStringExtra("msgInDh")));
			nameValuePairs.add(new BasicNameValuePair("met", "sndsms-04"));
	    	aUtil.sendMessage(serviceDb.URL_MSG, nameValuePairs);
			return START_REDELIVER_INTENT;
		}

		String vRcvNum = aUtil.getMyNumber(context);
		String vRcvDh  = aUtil.getNow();
		vData.put("rcvNum", vRcvNum);
		vData.put("rcvDh", vRcvDh);

		list = dbAdapter.inqSql("SEL06", vData);
		String vCnt = list.get(0).get("CNT");
		Log.d("list", list.toString());
		Log.d("vData", vData.toString());

		if ( !"0".equals(vCnt) ) return START_REDELIVER_INTENT; // 중복 발송이므로 무시
		dbAdapter.updList("INS04", vData); // 발송 시도 저장

		list = dbAdapter.inqSql("SEL06", vData);
		Log.d("list2", list.toString());
		Log.d("vData2", vData.toString());
		
		sendSMS(vSndNum, vMsg);

		nameValuePairs.add(new BasicNameValuePair("mod", "sys"));
		nameValuePairs.add(new BasicNameValuePair("tim", intent.getStringExtra("msgInDh")));
		nameValuePairs.add(new BasicNameValuePair("met", "sndsms-01"));
    	aUtil.sendMessage(serviceDb.URL_MSG, nameValuePairs);

		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() { super.onDestroy(); }

	private void sendSMS(String phoneNumber, String message) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

		// ---when the SMS has been sent---
		registerReceiver(new BroadcastReceiver() {
			public void onReceive(Context arg0, Intent arg1) {

				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("mod"  , "sys"));
				nameValuePairs.add(new BasicNameValuePair("met"  , "sndsms-02"));
		    	
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					nameValuePairs.add(new BasicNameValuePair("msg"  , "SMS sent"));
			    	aUtil.sendMessage(serviceDb.URL_MSG, nameValuePairs);
					//Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					nameValuePairs.add(new BasicNameValuePair("msg"  , "Generic failure"));
			    	aUtil.sendMessage(serviceDb.URL_MSG, nameValuePairs);
					//Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					nameValuePairs.add(new BasicNameValuePair("msg"  , "No service"));
			    	aUtil.sendMessage(serviceDb.URL_MSG, nameValuePairs);
					//Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					nameValuePairs.add(new BasicNameValuePair("msg"  , "Null PDU"));
			    	aUtil.sendMessage(serviceDb.URL_MSG, nameValuePairs);
					//Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					nameValuePairs.add(new BasicNameValuePair("msg"  , "Radio off"));
			    	aUtil.sendMessage(serviceDb.URL_MSG, nameValuePairs);
					//Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {

				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("mod"  , "sys"));
				nameValuePairs.add(new BasicNameValuePair("met"  , "sndsms-03"));
				
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					nameValuePairs.add(new BasicNameValuePair("msg"  , "SMS delivered"));
			    	aUtil.sendMessage(serviceDb.URL_MSG, nameValuePairs);
					//Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
					break;
				case Activity.RESULT_CANCELED:
					nameValuePairs.add(new BasicNameValuePair("msg"  , "SMS not delivered"));
			    	aUtil.sendMessage(serviceDb.URL_MSG, nameValuePairs);
					//Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(DELIVERED));

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}

	

}
