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
		Log.d("�Դ�", "receiverPhone");

		String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		String vNum = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		
		if ( state.equals(TelephonyManager.EXTRA_STATE_IDLE) ) {
			//��ȭ ���� �� ���� ...
			Log.d("������", "EXTRA_STATE_IDLE : " +vNum);
			
	        SmsMonitorService.beginStartingService(context);
		    Intent serviceIntent = new Intent(context, serviceDb.class);
		    context.startService(serviceIntent);
		    
		} else if ( state.equals(TelephonyManager.EXTRA_STATE_RINGING) ) {
	        //��ȭ �� �︱ �� ���� ...
			//Log.d("������", "EXTRA_STATE_RINGING : "+vNum);
			//aUtil.gvTelNum = vNum;
		} else if ( state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) ) {
			//��ȭ �� ������ �� ���� ...
			//Log.d("������", "EXTRA_STATE_OFFHOOK : "+vNum);
		} else if ( intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL) ) {
			//��ȭ�� �ɶ� ���� ���� ...
			//Log.d("������", "ACTION_NEW_OUTGOING_CALL : "+vNum);
		} else if ( intent.getAction().equals(TelephonyManager.DATA_CONNECTED) ) { // ��Ʈ��ũ�� ����ƴٸ�, ���� �Ұ� �ǵ� ����
			Log.d("������", "DATA_CONNECTED : "+TelephonyManager.DATA_CONNECTED);
	        SmsMonitorService.beginStartingService(context);
		    Intent serviceIntent = new Intent(context, serviceDb.class);
		    context.startService(serviceIntent);
		} else {
		}
    }
    
}