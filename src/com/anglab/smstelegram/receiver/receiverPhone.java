package com.anglab.smstelegram.receiver;

import com.anglab.smstelegram.service.SmsMonitorService;
import com.anglab.smstelegram.service.serviceDb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class receiverPhone extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent)  {
		Log.d("왔다", "receiverPhone");

		String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		String vNum = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		
		if ( state.equals(TelephonyManager.EXTRA_STATE_IDLE) ) {
			//통화 종료 후 구현 ...
			Log.d("리스너", "EXTRA_STATE_IDLE : " +vNum);
			
	        SmsMonitorService.beginStartingService(context);
		    Intent serviceIntent = new Intent(context, serviceDb.class);
		    context.startService(serviceIntent);
		    
		} else if ( state.equals(TelephonyManager.EXTRA_STATE_RINGING) ) {
	        //통화 벨 울릴 시 구현 ...
			//Log.d("리스너", "EXTRA_STATE_RINGING : "+vNum);
			//aUtil.gvTelNum = vNum;
		} else if ( state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) ) {
			//통화 중 상태일 때 구현 ...
			//Log.d("리스너", "EXTRA_STATE_OFFHOOK : "+vNum);
		} else if ( intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL) ) {
			//전화를 걸때 상태 구현 ...
			//Log.d("리스너", "ACTION_NEW_OUTGOING_CALL : "+vNum);
		} else if ( intent.getAction().equals(TelephonyManager.DATA_CONNECTED) ) { // 네트워크에 연결됐다면, 전송 불가 건들 전송
			Log.d("리스너", "DATA_CONNECTED : "+TelephonyManager.DATA_CONNECTED);
	        SmsMonitorService.beginStartingService(context);
		    Intent serviceIntent = new Intent(context, serviceDb.class);
		    context.startService(serviceIntent);
		} else {
		}
    }
    
}