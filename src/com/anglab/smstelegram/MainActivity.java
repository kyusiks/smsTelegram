package com.anglab.smstelegram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.anglab.smstelegram.ListViewer.ListAdapterWithButton;
import com.anglab.smstelegram.receiver.receiverSms;
import com.anglab.smstelegram.service.SmsMonitorService;
import com.anglab.smstelegram.service.serviceDb;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	public static Context context;
    public static NotesDbAdapter dbAdapter;
	List<HashMap<String, String>> list = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		context = getApplicationContext();
		dbAdapter = aUtil.getDB(this);

        if ( checkPlayServices() ) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            if ( regid.isEmpty() ) registerInBackground();
        } else {
            Log.i("TAG", "No valid Google Play Services APK found.");
        }
        
        Log.d("으흠?", regid);
        Log.i("BOARD", Build.BOARD); 
        Log.i("BRAND", Build.BRAND);
        Log.i("CPU_ABI", Build.CPU_ABI); 
        Log.i("DEVICE", Build.DEVICE);
        Log.i("DISPLAY", Build.DISPLAY); 
        Log.i("FINGERPRINT", Build.FINGERPRINT); 
        Log.i("HOST", Build.HOST);
        Log.i("ID", Build.ID);
        Log.i("MANUFACTURER", Build.MANUFACTURER); 
        Log.i("MODEL", Build.MODEL);
        Log.i("PRODUCT", Build.PRODUCT); 
        Log.i("TAGS", Build.TAGS);
        Log.i("TYPE", Build.TYPE);
        Log.i("USER", Build.USER);
        // device 정보 가져오기
        TelephonyManager telephony=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String tag="ddd";
        Log.d(tag, "getCallState : "+telephony.getCallState());
        Log.d(tag, "getDataActivity : "+telephony.getDataActivity());
        Log.d(tag, "getDataState : "+telephony.getDataState());
        Log.d(tag, "getDeviceId : "+telephony.getDeviceId());
        Log.d(tag, "getDeviceSoftwareVersion : "+telephony.getDeviceSoftwareVersion());
        Log.d(tag, "getLine1Number : "+telephony.getLine1Number());
        Log.d(tag, "getNetworkCountryIso : "+telephony.getNetworkCountryIso());
        Log.d(tag, "getNetworkOperator : "+telephony.getNetworkOperator());
        Log.d(tag, "getNetworkOperatorName : "+telephony.getNetworkOperatorName());
        Log.d(tag, "getNetworkType : "+telephony.getNetworkType());
        Log.d(tag, "getPhoneType : "+telephony.getPhoneType());
        Log.d(tag, "getSimCountryIso : "+telephony.getSimCountryIso());
        Log.d(tag, "getSubscriberId : "+telephony.getSubscriberId());
        Log.d(tag, "getVoiceMailAlphaTag : "+telephony.getVoiceMailAlphaTag());
        Log.d(tag, "getVoiceMailNumber : "+telephony.getVoiceMailNumber());
        Log.d(tag, "isNetworkRoaming : "+telephony.isNetworkRoaming());
        Log.d(tag, "hasIccCard : "+telephony.hasIccCard());
        Log.d(tag, "hashCode : "+telephony.hashCode());
        Log.d(tag, "toString : "+telephony.toString());
           
    // device 계정 정보 가져오기
        Account[] accounts =  AccountManager.get(getApplicationContext()).getAccounts();
        Account account = null;
                     
        for(int i=0;i<accounts.length;i++) {
              account = accounts[i];
              Log.d(tag, "Account - name: " + account.name + ", type :" + account.type);
     
              if(account.type.equals("com.google")){     //이러면 구글 계정 구분 가능
                           
              }
        }

        SmsMonitorService.beginStartingService(context);

		setContentView(R.layout.activity_main);
		

		Button btn_addMain = (Button) findViewById(R.id.btn_addMain);
		btn_addMain.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Log.d("시작", "2");
					Intent intent = new Intent(getApplicationContext(), regTelegramUser.class);
					startActivity(intent);

					overridePendingTransition(R.anim.push_left_in, R.anim.hold);
	    			
					
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		

		Button ddd1 = (Button) findViewById(R.id.btn_myList);
		ddd1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					Log.d("시작", "1");
					//setContentView(R.layout.view_mode);
					list = dbAdapter.inqSql("SEL03", null); 
		    	    Intent serviceIntent = new Intent(context, serviceDb.class);

		    	    serviceIntent.putExtra("met", "test");
		    	    context.startService(serviceIntent);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		
		

		Button ddd = (Button) findViewById(R.id.btn_favo);
		ddd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					Log.d("시작", "1");

					//setContentView(R.layout.view_mode);

					list = dbAdapter.inqSql("SELtext", null); 
					//list = aUtil.getSmsList(context);
					//Log.d("f", list.toString());
					//testSMS();
	    			fn_listAdapter("!");
	    			
					if ("1".equals("!")) return;
					
					//dbAdapter.dbClear();
					receiverSms d = new receiverSms();
					d.onReceive(context, null);


					if ("!".equals("!")) return;
					aUtil.getMissCallList(context);
					//serviceDb.fn_missCallrun();

					if ("!".equals("!")) return;
					
					
					getHistory();
					if ("!".equals("!")) return;
					
					
					
					Intent serviceIntent = new Intent(getBaseContext(), serviceDb.class);
					getBaseContext().startService(serviceIntent);
					if ("!2".equals("!")) return;
					
					Log.d("시작", "2");
					

	    			

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		
		ddd.callOnClick();
	}

	private void getHistory() {
		String[] projection = { CallLog.Calls.CONTENT_TYPE, CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.DATE };
		           
		Cursor cur = managedQuery(CallLog.Calls.CONTENT_URI, null, CallLog.Calls.TYPE + "= ?",
		                new String[]{ String.valueOf(CallLog.Calls.MISSED_TYPE) }, CallLog.Calls.DEFAULT_SORT_ORDER);
		       
		Log.d("db count=", String.valueOf(cur.getCount()));
		Log.d("db count=", CallLog.Calls.CONTENT_ITEM_TYPE);
		Log.d("db count=", CallLog.Calls.CONTENT_TYPE);

		aUtil.cur2list(cur);
		/*
		if(cur.moveToFirst() && cur.getCount() > 0) {
		   while(cur.isAfterLast() == false) {
		      StringBuffer sb = new StringBuffer();

		      sb.append("call type=").append(cur.getString(cur.getColumnIndex(CallLog.Calls.TYPE)));
		      sb.append(", cashed name=").append(cur.getString(cur.getColumnIndex(CallLog.Calls.CACHED_NAME)));
		      sb.append(", content number=").append(cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER)));
		      sb.append(", duration=").append(cur.getString(cur.getColumnIndex(CallLog.Calls.DURATION)));
		      sb.append(", new=").append(cur.getString(cur.getColumnIndex(CallLog.Calls.NEW)));
		      sb.append(", date=").append(aUtil.convDate(cur.getLong(cur.getColumnIndex(CallLog.Calls.DATE))+"")).append("]");
		      cur.moveToNext();
		      Log.d("call history[", sb.toString());
		     
		      }
		   }*/
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// pArrList 배열로 넘어온 값을 적당히 가공하여 리스트업 한다.
	// 내비게이션바에 pNav 으로 세팅한다. pTF 가 true면 새로쓰기 아니면 이어쓰기
	public void fn_listAdapter(String pMode) {
		//fn_chgContentView(R.layout.activity_main);
		//TextView txt_nav = (TextView)findViewById(R.id.txt_nav);

		ListAdapterWithButton<String> adapter = new ListAdapterWithButton<String>(this, list);
		ListView listView = (ListView) findViewById(R.id.list);
		listView.setAdapter(adapter);
	}

	public void onBatteryChanged(Intent intent) {

/*
		BroadcastReceiver mBRBattery = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				String ggg = "";
				if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
					// onBatteryChanged(intent);
				} else if (action.equals(Intent.ACTION_BATTERY_LOW)) {
					// To do
					ggg = "ACTION_BATTERY_LOW";
				} else if (action.equals(Intent.ACTION_BATTERY_OKAY)) {
					// To do
					ggg = "ACTION_BATTERY_OKAY";
				} else if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
					// To do
					ggg = "ACTION_POWER_CONNECTED";
				} else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
					// To do
					ggg = "ACTION_POWER_DISCONNECTED";
				}
				if ( !"".equals(ggg) ) {
					try {
						Intent serviceIntent = new Intent(mContext, serviceDb.class);
						serviceIntent.putExtra("vGubun" , "cha");
						serviceIntent.putExtra("vMsgTxt", ggg);
						serviceIntent.putExtra("vRcvDh" , "");
						serviceIntent.putExtra("vSndNum", "");
						serviceIntent.putExtra("vSndNm" , "");
						serviceIntent.putExtra("vRcvNum", "");
						mContext.startService(serviceIntent);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_BATTERY_LOW);
		filter.addAction(Intent.ACTION_BATTERY_OKAY);
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		registerReceiver(mBRBattery, filter);
*/
		// StrictMode.setThreadPolicy(new
		// StrictMode.ThreadPolicy.Builder().permitNetwork().build());

		
		int plug, status, scale, level, ratio;
		String sPlug = "";
		String sStatus = "";

		if (intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false) == false) {
			// mStatus.setText("no battery");
			return;
		}

		plug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
		status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
		scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
		level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		ratio = level * 100 / scale;

		switch (plug) {
		case BatteryManager.BATTERY_PLUGGED_AC:
			sPlug = "AC";
			break;
		case BatteryManager.BATTERY_PLUGGED_USB:
			sPlug = "USB";
			break;
		default:
			sPlug = "BATTERY";
			break;
		}

		switch (status) {
		case BatteryManager.BATTERY_STATUS_CHARGING:
			sStatus = "Charging";
			break;
		case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
			sStatus = "not charging";
			break;
		case BatteryManager.BATTERY_STATUS_DISCHARGING:
			sStatus = "discharging";
			break;
		case BatteryManager.BATTERY_STATUS_FULL:
			sStatus = "fully charged";
			break;
		default:
		case BatteryManager.BATTERY_STATUS_UNKNOWN:
			sStatus = "Unknwon status";
			break;
		}

		String ggg = plug + "/" + status + "/" + scale + "/" + level + "/" + ratio + "/" + sPlug + "/" + sStatus;
		if (sPlug.length() > 1 && sStatus.length() > 1) {

			try {
				Intent serviceIntent = new Intent(context, serviceDb.class);
				serviceIntent.putExtra("vGubun" , "bat");
				serviceIntent.putExtra("vMsgTxt", ggg);
				serviceIntent.putExtra("vRcvDh" ,  "");
				serviceIntent.putExtra("vSndNum", "");
				serviceIntent.putExtra("vSndNm" , "");
				serviceIntent.putExtra("vRcvNum", "");
				context.startService(serviceIntent);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	
	
	
	
	

	//////////////////////////
	// GCM setting part 시작
	//////////////////////////
    String SENDER_ID = "174732147215";
    GoogleCloudMessaging gcm;
    SharedPreferences prefs;
    
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    String regid;
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if ( gcm == null ) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    sendRegistrationIdToBackend();
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //mDisplay.append(msg + "\n");
            	Log.d("gcm테스트중", msg);
            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regid) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i("TAG", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regid);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void sendRegistrationIdToBackend() {

    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i("TAG", "Registration not found.");
            return "";
        }
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("TAG", "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if ( resultCode != ConnectionResult.SUCCESS ) {
            if ( GooglePlayServicesUtil.isUserRecoverableError(resultCode) ) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("ICELANCER", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
	//////////////////////////
	// GCM setting part 끝
	//////////////////////////

}
