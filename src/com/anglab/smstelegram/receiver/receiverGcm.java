package com.anglab.smstelegram.receiver;

import com.anglab.smstelegram.service.serviceGcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
	
public class receiverGcm extends WakefulBroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.d("GCM incoming", "real");
        Bundle extras = intent.getExtras();

        //Set<String> keys = intent.getExtras().keySet();
        /*for ( String key : extras.keySet() ) {
            Object value = extras.get(key);
            Log.d("TAG", String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
        }
         */
        if ( extras.containsKey("met") ) { // 이건 필수
            ComponentName comp = new ComponentName(context.getPackageName(), serviceGcm.class.getName());
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);
        } else {
            setResultCode(Activity.RESULT_CANCELED);
        }
        
    }
}
