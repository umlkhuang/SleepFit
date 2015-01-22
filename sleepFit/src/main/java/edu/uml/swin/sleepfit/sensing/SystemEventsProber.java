package edu.uml.swin.sleepfit.sensing;

import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.DB.SysEvents;
import edu.uml.swin.sleepfit.util.Constants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SystemEventsProber extends BroadcastReceiver {
	
	private DatabaseHelper mDatabaseHelper;
	private Dao<SysEvents, Integer> mDao; 
	
	public SystemEventsProber() {
		// No code here 
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		mDatabaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
		try {
			mDao = mDatabaseHelper.getSysEventsDao();
		} catch (SQLException e) {
			Log.e(Constants.TAG, "In SystemEventsProber, Cannot get the SysEvents DAO: " + e.toString());
			e.printStackTrace();
		}
		
		long mCurrentTime = System.currentTimeMillis();
		
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			Log.d(Constants.TAG, "In SystemEventsProber, received screen off event");
			
			SysEvents sysEvent = new SysEvents(mCurrentTime, Constants.SCREEN_OFF);
			try {
				mDao.create(sysEvent);
			} catch (SQLException e) {
				Log.e(Constants.TAG, "In SystemEventsProber, Add new sysevent data record failed: " + e.toString());
				e.printStackTrace();
			}
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			Log.d(Constants.TAG, "In SystemEventsProber, received screen on event");
			
			SysEvents sysEvent = new SysEvents(mCurrentTime, Constants.SCREEN_ON);
			try {
				mDao.create(sysEvent);
			} catch (SQLException e) {
				Log.e(Constants.TAG, "In SystemEventsProber, Add new sysevent data record failed: " + e.toString());
				e.printStackTrace();
			}
		} else if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
			Log.d(Constants.TAG, "In SystemEventsProber, received power connected event");
			
			SysEvents sysEvent = new SysEvents(mCurrentTime, Constants.POWER_CONNECTED);
			try {
				mDao.create(sysEvent);
			} catch (SQLException e) {
				Log.e(Constants.TAG, "In SystemEventsProber, Add new sysevent data record failed: " + e.toString());
				e.printStackTrace();
			}
		} else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
			Log.d(Constants.TAG, "In SystemEventsProber, received power disconnected event");
			
			SysEvents sysEvent = new SysEvents(mCurrentTime, Constants.POWER_DISCONNECTED);
			try {
				mDao.create(sysEvent);
			} catch (SQLException e) {
				Log.e(Constants.TAG, "In SystemEventsProber, Add new sysevent data record failed: " + e.toString());
				e.printStackTrace();
			}
		} else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
			Log.d(Constants.TAG, "In SystemEventsProber, received shutdown event");
			
			SysEvents sysEvent = new SysEvents(mCurrentTime, Constants.SYSTEM_POWER_OFF);
			try {
				mDao.create(sysEvent);
			} catch (SQLException e) {
				Log.e(Constants.TAG, "In SystemEventsProber, Add new sysevent data record failed: " + e.toString());
				e.printStackTrace();
			}
		}  else if (intent.getAction().equals(Constants.USER_LOGGED_SLEEP_TIME)) {
			Log.d(Constants.TAG, "In SystemEventsProber, received user logged sleep time event");
			
			SysEvents sysEvent = new SysEvents(mCurrentTime, Constants.USER_LOGGED_SLEEP_EVENT);
			try {
				mDao.create(sysEvent);
			} catch (SQLException e) {
				Log.e(Constants.TAG, "In SystemEventsProber, Add new sysevent data record failed: " + e.toString());
				e.printStackTrace();
			}
		} else if (intent.getAction().equals(Constants.USER_LOGGED_WAKEUP_TIME)) {
			Log.d(Constants.TAG, "In SystemEventsProber, received user logged wakeup time event");
			
			SysEvents sysEvent = new SysEvents(mCurrentTime, Constants.USER_LOGGED_WAKE_EVENT);
			try {
				mDao.create(sysEvent);
			} catch (SQLException e) {
				Log.e(Constants.TAG, "In SystemEventsProber, Add new sysevent data record failed: " + e.toString());
				e.printStackTrace();
			}
		}
	}

}
