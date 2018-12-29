package com.anglab.smstelegram.service;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.anglab.smstelegram.aUtil;
import com.anglab.smstelegram.receiver.receiverGcm;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class serviceGcm extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;
	static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
	}
	
    public serviceGcm() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		for ( String key : extras.keySet() ) {
			nameValuePairs.add(new BasicNameValuePair(key, extras.get(key).toString()));
		}
		nameValuePairs.add(new BasicNameValuePair("mod"  , "gcm"));
		nameValuePairs.add(new BasicNameValuePair("tim"  , extras.getString("msgInDh")));
		//nameValuePairs.add(new BasicNameValuePair("conId", extras.getString("conId")));
		//nameValuePairs.add(new BasicNameValuePair("met"  , extras.getString("met"  )));
		//nameValuePairs.add(new BasicNameValuePair("dpl"  , extras.getString("dpl"  )));
		//nameValuePairs.add(new BasicNameValuePair("msgId", extras.getString("msgId")));
    	aUtil.sendMessage(serviceDb.URL_SYS, nameValuePairs);

    	try {
    		long a = Long.valueOf(extras.getString("msgInDh"));
        	long b = Long.valueOf(aUtil.getNow());
        	long c = 60 * 3; // 3분 기다림.
        	if ( b - a > c ) { // 3분이 지난 gcm이라면 실패로 간주하고 pass
        		nameValuePairs.add(new BasicNameValuePair("mod"  , "gcf"));
        		nameValuePairs.add(new BasicNameValuePair("dpl"  , extras.getString("mod") + "_" + extras.getString("dpl") + "_" + extras.getString("msgId") ));
            	aUtil.sendMessage(serviceDb.URL_SYS, nameValuePairs);
        		return;
        	} else {
        		// 계속진행하라
        		nameValuePairs.add(new BasicNameValuePair("mod"  , "gc2"));
            	aUtil.sendMessage(serviceDb.URL_SYS, nameValuePairs);
        	}
    	} catch (Exception e) {
    	}

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

		if ( !extras.isEmpty() ) {
			if ( GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType) ) {
				// Set<String> keys = intent.getExtras().keySet();
				for (String key : extras.keySet()) {
					Object value = extras.get(key);
					Log.d("service", String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
				}

				String vMethod = aUtil.nvl(extras.getString("met"));
				if ( vMethod.equals("mmsimg") ) {
					Intent serviceIntent = new Intent(context, serviceDb.class);
					for ( String key : extras.keySet() ) {
						serviceIntent.putExtra(key, extras.get(key).toString());
					}
					context.startService(serviceIntent);
				} else if ( vMethod.equals("wakeup") ) {
					// 별다른 로직 없음. 위의 GCM응답 보내는걸로 종료.
				} else if ( vMethod.equals("sndsms") ) {
					// 문자발송
					Intent serviceIntent = new Intent(context, serviceSendSms.class);
					for ( String key : extras.keySet() ) {
						serviceIntent.putExtra(key, extras.get(key).toString());
					}
					context.startService(serviceIntent);
				} else {

				}
            }
        }
        receiverGcm.completeWakefulIntent(intent);
    }
}