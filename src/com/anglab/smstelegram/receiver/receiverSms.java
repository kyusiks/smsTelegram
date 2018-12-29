package com.anglab.smstelegram.receiver;

import com.anglab.smstelegram.service.SmsMonitorService;
import com.anglab.smstelegram.service.serviceDb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class receiverSms extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("¿Ô´Ù", "sms");
        SmsMonitorService.beginStartingService(context);
	    Intent serviceIntent = new Intent(context, serviceDb.class);
	    context.startService(serviceIntent);
	}
}