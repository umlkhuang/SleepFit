package edu.uml.swin.sleepfit.sensing;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.DB.SysEvents;
import edu.uml.swin.sleepfit.EveningCardActivity;
import edu.uml.swin.sleepfit.MainActivity;
import edu.uml.swin.sleepfit.MorningCardActivity;
import edu.uml.swin.sleepfit.R;
import edu.uml.swin.sleepfit.util.Constants;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class SystemEventsProber extends BroadcastReceiver {
	
	private DatabaseHelper mDatabaseHelper;
	private Dao<SysEvents, Integer> mDao;
    private Context mContext;
	
	public SystemEventsProber() {
		// No code here 
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
        mContext = context;

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

            String trackDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date());
            SharedPreferences preferences = mContext.getSharedPreferences(Constants.TMP_PREF_FILE, Context.MODE_MULTI_PROCESS);
            Calendar cal = Calendar.getInstance();
            if (cal.get(Calendar.HOUR_OF_DAY) >= 8 && cal.get(Calendar.HOUR_OF_DAY) <= 10) {
                String morningTrackDate = preferences.getString("morningTrackDate", "");
                //Log.d(Constants.TAG, "MorningTrackDate = " + morningTrackDate + ", trackDate = " + trackDate);
                if (!morningTrackDate.equals(trackDate)) {
                    startMorningNotification();
                }
            } else if (cal.get(Calendar.HOUR_OF_DAY) >= 20) {
                String eveningTrackDate = preferences.getString("eveningTrackDate", "");
                //Log.d(Constants.TAG, "EveningTrackDate = " + eveningTrackDate + ", trackDate = " + trackDate);
                if (!eveningTrackDate.equals(trackDate)) {
                    startEveningNotification();
                }
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

    private void startMorningNotification() {
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        String trackDate;
        trackDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date());
        Bundle bundle = new Bundle();
        bundle.putString("trackDate", trackDate);
        Intent resultIntent = new Intent(mContext, MorningCardActivity.class);
        resultIntent.putExtras(bundle);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        String text = "Please update your sleep/wake up time and rate your sleep.";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentTitle("How is your sleep last night?")
                .setSmallIcon(R.drawable.ic_launcher_trans)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(resultPendingIntent);

        mNotificationManager.notify(2, builder.build());
    }

    private void startEveningNotification() {
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        String trackDate;
        trackDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date());
        Bundle bundle = new Bundle();
        bundle.putString("trackDate", trackDate);
        Intent resultIntent = new Intent(mContext, EveningCardActivity.class);
        resultIntent.putExtras(bundle);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        String text = "Please log your nap time and rate your day.";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentTitle("How do you feel today?")
                .setSmallIcon(R.drawable.ic_launcher_trans)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(resultPendingIntent);

        mNotificationManager.notify(3, builder.build());
    }

}
