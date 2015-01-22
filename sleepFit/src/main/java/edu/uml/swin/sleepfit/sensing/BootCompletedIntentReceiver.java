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
import android.content.SharedPreferences;
import android.util.Log;

public class BootCompletedIntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.d(Constants.TAG, "In BootCompletedIntentReceiver, received bootup event");
			
			// Start the sensing service 
			Intent sensingIntent = new Intent(context, SensingService.class);
			context.startService(sensingIntent);
			SharedPreferences config = context.getSharedPreferences(Constants.USER_CONFIG_FILE, Context.MODE_PRIVATE);
			
			// Next, write the system event into the database 
			long mCurrentTime = System.currentTimeMillis();
			DatabaseHelper mDatabaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
			Dao<SysEvents, Integer> mDao = null;
			try {
				mDao = mDatabaseHelper.getSysEventsDao();
			} catch (SQLException e) {
				Log.e(Constants.TAG, "In BootCompletedIntentReceiver, Cannot get the SysEvents DAO: " + e.toString());
				e.printStackTrace();
			}
			
			if (mDao != null) {
				SysEvents sysEvent = new SysEvents(mCurrentTime, Constants.SYSTEM_POWER_ON);
				try {
					mDao.create(sysEvent);
				} catch (SQLException e) {
					Log.e(Constants.TAG, "In BootCompletedIntentReceiver, Add new sysevent data record failed: " + e.toString());
					e.printStackTrace();
				}
			}
		}
	}

}
