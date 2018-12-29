package com.anglab.smstelegram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.anglab.smstelegram.service.serviceDb;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

public class sendSms extends Activity {

	public static Context context;
    public static NotesDbAdapter dbAdapter;
	List<HashMap<String, String>> list = new ArrayList<>();


	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getApplicationContext();
		dbAdapter = aUtil.getDB(this);
		// setContentView(R.layout.main);
		
    	Intent intent = getIntent();
		String vMethod = aUtil.nvl(intent.getStringExtra("met"));
		Log.d("옴", "sendSms.java");
    	if ( !vMethod.equals("sndsms") ) return;

		String vMod = aUtil.nvl(intent.getStringExtra("mod"));
		String vDpl = aUtil.nvl(intent.getStringExtra("dpl"));
		String vOrgMsgId = aUtil.nvl(intent.getStringExtra("orgMsgId"));
		String vSndNum = aUtil.nvl(intent.getStringExtra("num"));
		String vMsg = aUtil.nvl(intent.getStringExtra("msg"));
		String vConId = aUtil.nvl(intent.getStringExtra("conId"));
		String vNm = "";
		
		if ( "".equals(vSndNum) ) { // 답장으로 보내는 sms
			HashMap<String, String> vData = new HashMap<>();
			vData.put("mod", vMod);
			vData.put("dpl", vDpl);
			vData.put("msgId", vOrgMsgId);
			list = dbAdapter.inqSql("SEL_SNDSMS", vData);
			vSndNum = list.get(0).get("SND_NUM");
			vNm = list.get(0).get("SND_NM");
		} else { // 직접 명령어로 보내는 sms
			vNm = aUtil.getNameByNumber(vSndNum, context);
		}
    	Log.d("11", "22/"+vSndNum+"/"+vNm);
		if ( "".equals(vSndNum) ) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("mod"  , "sys"));
			nameValuePairs.add(new BasicNameValuePair("conId", vConId)); //TODO 여기할차례
			nameValuePairs.add(new BasicNameValuePair("tim"     , intent.getStringExtra("msgInDh")));
			nameValuePairs.add(new BasicNameValuePair("met"     , "sndsms-04"));
			nameValuePairs.add(new BasicNameValuePair("msg"     , intent.getStringExtra("msg"  )));
			nameValuePairs.add(new BasicNameValuePair("orgMsgId", intent.getStringExtra("orgMsgId")));
			nameValuePairs.add(new BasicNameValuePair("dpl"     , intent.getStringExtra("dpl"  )));
			nameValuePairs.add(new BasicNameValuePair("msgId"   , intent.getStringExtra("msgId")));
			nameValuePairs.add(new BasicNameValuePair("mod"     , intent.getStringExtra("mod"  )));
	    	aUtil.sendMessage(serviceDb.URL_MSG, nameValuePairs);
			return;
		}
		
		
		sendSMS(vSndNum, vMsg);

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("mod"  , "sys"));
		nameValuePairs.add(new BasicNameValuePair("conId", vConId)); //TODO 여기할차례
		nameValuePairs.add(new BasicNameValuePair("tim"     , intent.getStringExtra("msgInDh")));
		nameValuePairs.add(new BasicNameValuePair("met"     , "sndsms-01"));
		nameValuePairs.add(new BasicNameValuePair("msg"     , intent.getStringExtra("msg"  )));
		nameValuePairs.add(new BasicNameValuePair("orgMsgId", intent.getStringExtra("orgMsgId")));
		nameValuePairs.add(new BasicNameValuePair("dpl"     , intent.getStringExtra("dpl"  )));
		nameValuePairs.add(new BasicNameValuePair("msgId"   , intent.getStringExtra("msgId")));
		nameValuePairs.add(new BasicNameValuePair("mod"     , intent.getStringExtra("mod"  )));
		
    	aUtil.sendMessage(serviceDb.URL_MSG, nameValuePairs);
	}


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

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
