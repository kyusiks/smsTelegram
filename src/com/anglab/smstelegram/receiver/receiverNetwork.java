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
    	Log.d("왔다", "net");
        if ( aUtil.isOnline(context) ) {
        	Log.d("isOnline", gvIsOnline + " / " + gvIsWifi);

        	if ( gvIsOnline ) { // 이전 상태가 온라인이었는데
        		if ( aUtil.isWifi(context) ) { // 와이파이상태를 체크해서
    	        	if ( gvIsWifi ) return; // 이전 상태가 와이파이였으면 리턴.(상태변화가 없는걸로 간주)
    	        	gvIsWifi = true; // 와이파이 상태가 가능으로 변경되었다.
    	        	// 와이파이로 상태변화가 있을경우 코딩하라.
    	        	
    	        	// 와이파이로 상태변화가 있을경우 코딩하라.
    	        } else {
    	        	gvIsWifi = false;
    	        	return; // 이전상태의 gvIsOnline이 동일하고 와이파이가 꺼진 상태면 그냥 리턴.
    	        }
        	} else {
        		gvIsOnline = true;
        	}
        	

        	Log.d("왔다", "net 테스트 통과 " + gvIsOnline + " / " + gvIsWifi);

            SmsMonitorService.beginStartingService(context);
    	    Intent serviceIntent = new Intent(context, serviceDb.class);
    	    context.startService(serviceIntent);
        } else {
        	gvIsOnline = false;
        }
    }
}