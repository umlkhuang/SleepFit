package edu.uml.swin.sleepfit.sensing;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import edu.uml.swin.sleepfit.util.Constants;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

public class AppUsageProber {
	
	private Context mContext;
	private String mLastUsedApp;
	private Map<String, Integer> mAppUsedTimes;
	private Map<String, Integer> mAppUsageTime;
	private ActivityManager mActivityManager;
	private TimerTask mTimerTask;
	private Timer mTimer;
	private boolean mTimerIsRunning;
	private IntentFilter mMsgFilter;
	private BroadcastReceiver mReceiver;
	
	class MyTimerTask extends TimerTask {
		@Override
		public void run() {
			String appName = mActivityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
			
			if (mAppUsedTimes.containsKey(appName)) {
				int oldValue = mAppUsedTimes.get(appName);
				if (!appName.equals(mLastUsedApp)) {
					mLastUsedApp = appName;
					mAppUsedTimes.put(appName, oldValue + 1);
				} 
			} else {
				mAppUsedTimes.put(appName, 1);
				mLastUsedApp = appName;
			}
			
			if (mAppUsageTime.containsKey(appName)) {
				int seconds = mAppUsageTime.get(appName);
				mAppUsageTime.put(appName, seconds + Constants.APP_USAGE_PROBE_PERIOD_SECOND);
			} else {
				mAppUsageTime.put(appName, Constants.APP_USAGE_PROBE_PERIOD_SECOND);
			}
		}
	}
	
	public AppUsageProber(Context context) {
		mContext = context;
		mLastUsedApp = "";
		mAppUsedTimes = new HashMap<String, Integer>();
		mAppUsageTime = new HashMap<String, Integer>();
		mActivityManager = (ActivityManager) mContext.getSystemService(Activity.ACTIVITY_SERVICE);
		mMsgFilter = new IntentFilter();
		mMsgFilter.addAction(Intent.ACTION_SCREEN_ON);
		mMsgFilter.addAction(Intent.ACTION_SCREEN_OFF);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
					// Stop the timer task if the screen is off
					if (mTimerIsRunning) {
						mTimerTask.cancel();
						mTimerTask = null;
						mTimer.cancel();
						mTimerIsRunning = false;
					}
				} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
					// Start the timer task once the screen is on 
					if (!mTimerIsRunning) {
						mTimerTask = new MyTimerTask();
						mTimer = new Timer();
						mTimer.schedule(mTimerTask, 0, Constants.APP_USAGE_PROBE_PERIOD_SECOND * 1000);
						mTimerIsRunning = true;
					}
				}
			}
		};
		
		mTimerTask = new MyTimerTask();
		mTimer = new Timer();
	}
	
	public void startService() {
		mContext.registerReceiver(mReceiver, mMsgFilter);
		mTimer.schedule(mTimerTask, 0, Constants.APP_USAGE_PROBE_PERIOD_SECOND * 1000);
		mTimerIsRunning = true;
	}
	
	public void stopService() {
		mContext.unregisterReceiver(mReceiver);
		if (mTimerIsRunning) {
			mTimerTask.cancel();
			mTimerTask = null;
			mTimer.cancel();
			mTimerIsRunning = false;
		}
	}
	
	public String getAppUsage() {
		int usageSize = mAppUsageTime.size();
		int idx = 1;
		StringBuilder strBuf = new StringBuilder();
		
		if (usageSize == 0) {
			mAppUsedTimes.clear();
			mAppUsageTime.clear();
			return "";
		} else {
			for (String key : mAppUsedTimes.keySet()) {
				strBuf.append(key).append(":").append(mAppUsedTimes.get(key));
				if (mAppUsageTime.containsKey(key))
					strBuf.append(":").append(mAppUsageTime.get(key));
				else 
					strBuf.append(":").append(Constants.APP_USAGE_PROBE_PERIOD_SECOND);
				
				if (idx < usageSize) strBuf.append(","); 
				idx ++;
			}
			mAppUsedTimes.clear();
			mAppUsageTime.clear();
			//Log.d(Constants.TAG, strBuf.toString());
			return strBuf.toString();
		}
	} 
}
