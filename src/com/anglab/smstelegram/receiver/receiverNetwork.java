package com.anglab.smstelegram.receiver;

import com.anglab.smstelegram.aUtil;
import com.anglab.smstelegram.service.SmsMonitorService;
import com.anglab.smstelegram.service.serviceDb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class receiverNetwork extends BroadcastReceiver {
	private static boolean gvIsOnline = false;;
	private static boolean gvIsWifi = false;;

    @Override
    public void onReceive(final Context context, Intent intent)  {
    	Log.d("�Դ�", "net");
        if ( aUtil.isOnline(context) ) {
        	Log.d("isOnline", gvIsOnline + " / " + gvIsWifi);

        	if ( gvIsOnline ) { // ���� ���°� �¶����̾��µ�
        		if ( aUtil.isWifi(context) ) { // �������̻��¸� üũ�ؼ�
    	        	if ( gvIsWifi ) return; // ���� ���°� �������̿����� ����.(���º�ȭ�� ���°ɷ� ����)
    	        	gvIsWifi = true; // �������� ���°� �������� ����Ǿ���.
    	        	// �������̷� ���º�ȭ�� ������� �ڵ��϶�.
    	        	
    	        	// �������̷� ���º�ȭ�� ������� �ڵ��϶�.
    	        } else {
    	        	gvIsWifi = false;
    	        	return; // ���������� gvIsOnline�� �����ϰ� �������̰� ���� ���¸� �׳� ����.
    	        }
        	} else {
        		gvIsOnline = true;
        	}
        	

        	Log.d("�Դ�", "net �׽�Ʈ ��� " + gvIsOnline + " / " + gvIsWifi);

            SmsMonitorService.beginStartingService(context);
    	    Intent serviceIntent = new Intent(context, serviceDb.class);
    	    context.startService(serviceIntent);
        } else {
        	gvIsOnline = false;
        }
    }
}