package com.anglab.smstelegram.service;

import com.anglab.smstelegram.BuildConfig;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.util.Log;

public class SmsMonitorService extends Service {
    private static Uri uriSMS = Uri.parse("content://mms-sms/conversations/");

    private ContentResolver crSMS;
    private ContentResolver crMissCall;
    private SmsContentObserver observerSMS = null;
    private MissCallContentObserver observerMissCall = null;
    private Context context;
    private static boolean sRunning = false; // 서비스 중복실행방지
    private static boolean mRunning; // 이벤트 중복실행방지
    private static boolean doAgain = false; // 추가로 펑션이 호출되면, 지금 서비스가 완료되고 한번더 실행하라. 여러번 호출되도 한번만 더 호출.
    
    public int gflags = 0, gstartId = 0;

    @Override
    public void onCreate() {
        doAgain = true;
    	if ( sRunning ) return;
        sRunning = true;
        doAgain = false;
        super.onCreate();
        context = this.getApplicationContext();
        if (BuildConfig.DEBUG) Log.d("D : " + sRunning, "SmsMonitorService created");
        registerSMSObserver();
        registerMissCallObserver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        gflags = flags;
        gstartId = startId;
        if (BuildConfig.DEBUG) Log.d("D : " + sRunning, "SmsMonitorService onStartCommand " + flags + " / " + startId);
        registerSMSObserver();
        registerMissCallObserver();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //unregisterSMSObserver();
        //unregisterMissCallObserver();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
     * Registers the observer for SMS changes
     */
    private void registerSMSObserver() {
    	Log.d("registerSMSObserver", "등록");
        if ( observerSMS == null ) {
        	Log.d("observerSMS", "등록");
            observerSMS = new SmsContentObserver(new Handler());
            crSMS = getContentResolver();
            crSMS.registerContentObserver(uriSMS, true, observerSMS);
            if (BuildConfig.DEBUG) Log.d("D", "SMS Observer registered.");
        }
    }

    /*
     * Registers the observer for SMS changes
     */
    private void registerMissCallObserver() {
    	Log.d("registerMissCallObserver", "등록");
        if ( observerMissCall == null ) {
        	Log.d("observerMissCall", "등록");
            observerMissCall = new MissCallContentObserver(new Handler());
            crMissCall = getContentResolver();
            crMissCall.registerContentObserver(CallLog.Calls.CONTENT_URI, true, observerMissCall);
            if (BuildConfig.DEBUG) Log.d("D", "MissCall Observer registered.");
        }
    }

    /**
     * Unregisters the observer for call log changes
     */
    private void unregisterSMSObserver() {
        if ( crSMS != null ) crSMS.unregisterContentObserver(observerSMS);
        if ( observerSMS != null ) observerSMS = null;
        if ( BuildConfig.DEBUG ) Log.d("D", "Unregistered SMS Observer");
        sRunning = false;
    }

    /**
     * Unregisters the observer for call log changes
     */
    private void unregisterMissCallObserver() {
        if ( crMissCall != null ) crMissCall.unregisterContentObserver(observerMissCall);
        if ( observerMissCall != null ) observerMissCall = null;
        if ( BuildConfig.DEBUG ) Log.d("D", "Unregistered MissCall Observer");
        sRunning = false;
    }

    private class SmsContentObserver extends ContentObserver {
        public SmsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
    		Log.d("리졸버", "리턴? " + gflags + " / " + gstartId);
    		try {
        		if ( mRunning ) return;
        		Log.d("리졸버", "시작" + gflags + " / " + gstartId);
        		mRunning = true;
        		//serviceDb.fn_run();

        	    Intent serviceIntent = new Intent(context, serviceDb.class);
        	    context.startService(serviceIntent);
    		    mRunning = false;
        		Log.d("리졸버", "끝" + gflags + " / " + gstartId);

        		if ( doAgain ) {
        			doAgain = false;
            		//beginStartingService(context);
        		}
    		} catch (Exception e) {
    		} finally {
			}
        }
    }

    
    
    private class MissCallContentObserver extends ContentObserver {
        public MissCallContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
    		Log.d("MissCall리졸버", "리턴? " + gflags + " / " + gstartId);
    		try {
        		if ( mRunning ) return;
        		Log.d("MissCall리졸버", "시작" + gflags + " / " + gstartId);
        		mRunning = true;
        		//serviceDb.fn_missCallrun();
        	    Intent serviceIntent = new Intent(context, serviceDb.class);
        	    context.startService(serviceIntent);
    		    mRunning = false;
        		Log.d("MissCall리졸버", "끝" + gflags + " / " + gstartId);

        		if ( doAgain ) {
        			doAgain = false;
            		//beginStartingService(context);
        		}
    		} catch (Exception e) {
    		} finally {
			}
        }
    }

    /**
     * Start the service to process that will run the content observer
     */
    public static void beginStartingService(Context context) {
        if (BuildConfig.DEBUG) Log.d("D : " + sRunning, "SmsMonitorService: beginStartingService()");
        if ( sRunning ) return;
        sRunning = true;
        context.startService(new Intent(context, SmsMonitorService.class));
    }

    /**
     * Called back by the service when it has finished processing notifications,
     * releasing the wake lock if the service is now stopping.
     */
    public static void finishStartingService(Service service) {
        if (BuildConfig.DEBUG) Log.d("D : " + sRunning, "SmsMonitorService: finishStartingService()");
        sRunning = false;
        service.stopSelf();
    }
}