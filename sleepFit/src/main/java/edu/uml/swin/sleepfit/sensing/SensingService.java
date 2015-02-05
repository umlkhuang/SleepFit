package edu.uml.swin.sleepfit.sensing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.DB.SleepLogger;
import edu.uml.swin.sleepfit.MainActivity;
import edu.uml.swin.sleepfit.R;
import edu.uml.swin.sleepfit.util.Constants;
import edu.uml.swin.sleepfit.util.FileUploader;
import edu.uml.swin.sleepfit.util.HttpRequestTask;
import edu.uml.swin.sleepfit.util.HttpRequestTask.HttpRequestCallback;
import edu.uml.swin.sleepfit.util.SyncWorker;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Gravity;
import android.widget.RemoteViews;
import android.widget.Toast;

public class SensingService extends Service implements HttpRequestCallback{

	private static SensingService mSensingService = null;
	//private static SystemLogger mSysLogger = null;
	//private static MoveDetector mMoveDetector = null;
	private static AppUsageProber mAppUsageProber = null;
	private static DecibelProber mDecibelProber = null;
	private static IlluminanceProber mIlluminanceProber = null;
	private static MovementProber mMovementProber = null;
	private static ProximityProber mProximityProber = null;
	private IntentFilter mMessageFilter;
	private IntentFilter mSysMsgFilter;
	private SystemEventsProber mSystemEventsProber;
	//private WifiLock mWifiLock;
	private boolean mSleepTimeLogged;
	//private CountDownTimer mNotificationTimer;
	private boolean mNotificationTimerRunning;
	private boolean mDailyNotified; 
	private AlarmManager mSensingDutyTimer;
	private Intent mAlarmIntent;
	private PendingIntent mAlarmPendingIntent;
	
	private DatabaseHelper mDatabaseHelper;
	private Dao<SleepLogger, Integer> mSleepLogDao;
    private String mSleepTimeStr;
    private String mSleepDebtStr;

    private static int NOTIFICATION_ID = 99999;
	
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
        public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constants.COLLECT_CONTEXT_DUTY_MSG)) {
				Log.d(Constants.TAG, "Context collector: received the message to collect contextual information");

				// Upload the database file to the remote server if needed 
				long currentTS = System.currentTimeMillis();
				Calendar rightNow = Calendar.getInstance(); 
				SharedPreferences preferences = getSharedPreferences(Constants.UPDATE_TIME_FILE, Context.MODE_PRIVATE);
				long lastUploadTS = preferences.getLong("lastUploadTS", 0);
				long morningUploadTS = preferences.getLong("morningTS", 0);
				if (lastUploadTS == 0) {
					lastUploadTS = currentTS;
					SharedPreferences.Editor editor = preferences.edit();
					editor.putLong("lastUploadTS", currentTS); 
					editor.putLong("morningTS", currentTS);
					editor.commit();
				}
				
				if ((currentTS - lastUploadTS) >= Constants.UPLOAD_DATA_INTERVAL) {
                //if ((currentTS - lastUploadTS) >= 4 * 60 * 1000) {
					new SyncWorker(getBaseContext(), currentTS).execute();
					
					SharedPreferences.Editor editor = preferences.edit();
					editor.putLong("lastUploadTS", currentTS); 
					editor.putLong("morningTS", currentTS); 
					editor.commit();
				} else if (rightNow.get(Calendar.HOUR_OF_DAY) >= 6 && rightNow.get(Calendar.HOUR_OF_DAY) <= 8
							&& (currentTS - morningUploadTS) >= Constants.UPLOAD_DATA_INTERVAL_SMALL) {
					new SyncWorker(getBaseContext(), currentTS).execute();
					
					SharedPreferences.Editor editor = preferences.edit();
					editor.putLong("morningTS", currentTS); 
					editor.commit();
				} else if(rightNow.get(Calendar.HOUR_OF_DAY) >= 6 && rightNow.get(Calendar.HOUR_OF_DAY) <= 8
                            && (currentTS - morningUploadTS) <= 8 * 60 * 1000) {
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    String trackDate = dateFormatter.format(rightNow.getTime());
                    String getSleepUrl = Constants.GET_SLEEP_URL + "?accessCode=" + Constants.getAccessCode(mSensingService) + "&trackDate=" + trackDate;
                    new HttpRequestTask(mSensingService.getBaseContext(), mSensingService).execute(getSleepUrl);
                }
				
				// make sure the calculated sleep log will be synced to local database in 9AM
				if (rightNow.get(Calendar.HOUR_OF_DAY) >= 9 && rightNow.get(Calendar.MINUTE) >= 55) {
					SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
					String trackDate = dateFormatter.format(rightNow.getTime());
					List<SleepLogger> sleeps = null;
					QueryBuilder<SleepLogger, Integer> qb = mSleepLogDao.queryBuilder();
					try {
						qb.where().eq("trackDate", trackDate);
						sleeps = mSleepLogDao.query(qb.prepare());
					} catch (SQLException e) {
						e.printStackTrace();
						Log.e(Constants.TAG, e.toString());
					}
					// Only try to get sleep log if no local record has been found 
					if (sleeps == null || sleeps.size() <= 0) {
						String getSleepUrl = Constants.GET_SLEEP_URL + "?accessCode=" + Constants.getAccessCode(mSensingService) + "&trackDate=" + trackDate;
						new HttpRequestTask(mSensingService.getBaseContext(), mSensingService).execute(getSleepUrl); 
					}
				}
				
				// Start to collect context 
				Runnable task = new Runnable() {
					@Override
                    public void run() {
						Looper.prepare();
						
						ContextCollector mContextCollector = new ContextCollector(mSensingService);
						//mContextCollector.getLocationInfo();
						mContextCollector.getDecibelValue(mDecibelProber.getDecibelValue());
						mContextCollector.getWifiSSIDs();
						mContextCollector.getIlluminanceValue(mIlluminanceProber.getIlluminanceContext());
						mContextCollector.getPowerStatus();
						mContextCollector.getAppUsage(mAppUsageProber.getAppUsage());
						mContextCollector.getMovementInfo(mMovementProber.getMovementInof());
						mContextCollector.getProximityInfo(mProximityProber.getProximityContext()); 
						
						Looper.loop();
                    } 
				};
				Thread taskThread = new Thread(task);
				taskThread.start();
			} else if (intent.getAction().equals(Constants.UPDATED_SLEEP_INFO)) {
                startServiceNotification();
            }
        }
	};
	
	@Override
    public IBinder onBind(Intent intent) {
	    return null;
    }
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(Constants.TAG, "Sensing service in onStartCommand()");

		startServiceNotification();
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(Constants.TAG, "Sensing service in onCreate()"); 
		
		mSensingService = this;
		//if (mSysLogger == null) mSysLogger = new SystemLogger(this);
		
		//mWakeLock = null;
		//mWifiLock = null;
		mSleepTimeLogged = false;
		mNotificationTimerRunning = false;
		mDailyNotified = false;

        mSleepTimeStr = "Not available";
        mSleepDebtStr = "Not available";
		
		/*
		mNotificationTimer = new CountDownTimer(Constants.REMIND_TO_LOG_MAX_DURATION_SECONDS * 1000, 1 * 1000) {
			@Override
			public void onFinish() {
				startRemindMissLogSleepNotification();
				mNotificationTimerRunning = false;
			}

			@Override
			public void onTick(long millisUntilFinished) {
				// No code here 
			}
		};
		*/
		
		mDatabaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
		try {
			mSleepLogDao = mDatabaseHelper.getSleepLoggerDao();
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Cannot get the LightRaw DAO: " + e.toString());
			e.printStackTrace();
		}
		
		mMessageFilter = new IntentFilter();
		mMessageFilter.addAction(Constants.COLLECT_CONTEXT_DUTY_MSG);
		mMessageFilter.addAction(Constants.USER_LOGGED_SLEEP_TIME);
		mMessageFilter.addAction(Constants.USER_LOGGED_WAKEUP_TIME);
		mMessageFilter.addAction("show_notification");
        mMessageFilter.addAction(Constants.UPDATED_SLEEP_INFO);
		registerReceiver(mMessageReceiver, mMessageFilter);
		
		mSystemEventsProber = new SystemEventsProber();
		mSysMsgFilter = new IntentFilter();
		mSysMsgFilter.addAction(Intent.ACTION_SCREEN_OFF);
		mSysMsgFilter.addAction(Intent.ACTION_SCREEN_ON);
		mSysMsgFilter.addAction(Intent.ACTION_POWER_CONNECTED);
		mSysMsgFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		mSysMsgFilter.addAction(Intent.ACTION_SHUTDOWN);
		mSysMsgFilter.addAction(Constants.USER_LOGGED_SLEEP_TIME);
		mSysMsgFilter.addAction(Constants.USER_LOGGED_WAKEUP_TIME);
		registerReceiver(mSystemEventsProber, mSysMsgFilter);
		
		Log.d(Constants.TAG, "Creating the Sensing service"); 
		//mSysLogger.addLog("Creating the Sensing service");
		
		startAllSensings();
		
	}
	
	@Override
	public void onDestroy() {
		Log.d(Constants.TAG, "Destroying the Sensing service");
		//mSysLogger.addLog("Destroying the Sensing service");
		
		unregisterReceiver(mMessageReceiver);
		unregisterReceiver(mSystemEventsProber);
		destroyAllSensings();
		stopForeground(true);
		
		super.onDestroy();
	}

	public static SensingService getSensingServiceInstance() {
		return mSensingService;
	}
	
	private void startAllSensings() {
		/*
		WifiManager wm = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "WifiLock");
		mWifiLock.setReferenceCounted(true);
		if ((mWifiLock != null) && (!mWifiLock.isHeld())) mWifiLock.acquire();
		*/
		
		mSensingDutyTimer = (AlarmManager) getSystemService(Context.ALARM_SERVICE); 
		mAlarmIntent = new Intent(this, AlarmReceiver.class);
		mAlarmPendingIntent = PendingIntent.getBroadcast(this, 989898, mAlarmIntent, 0); 
    	mSensingDutyTimer.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Constants.SENSING_DUTY_INTERVAL_SECOND * 1000, 
    			Constants.SENSING_DUTY_INTERVAL_SECOND * 1000, mAlarmPendingIntent); 
		
	    if (mAppUsageProber == null) 
	    	mAppUsageProber = new AppUsageProber(mSensingService);
	    mAppUsageProber.startService();
	    
	    if (mDecibelProber == null) 
	    	mDecibelProber = new DecibelProber(mSensingService);
	    mDecibelProber.startService();
	    
	    if (mIlluminanceProber == null) 
	    	mIlluminanceProber = new IlluminanceProber(mSensingService);
	    mIlluminanceProber.startService();
	    
	    if (mMovementProber == null) 
	    	mMovementProber = new MovementProber(mSensingService);
	    mMovementProber.startService();
	    
	    if (mProximityProber == null)
	    	mProximityProber = new ProximityProber(mSensingService);
	    mProximityProber.startService();
    }

	private void destroyAllSensings() {
		//if (mWifiLock != null && mWifiLock.isHeld()) mWifiLock.release();
		
		if (mSensingDutyTimer != null) 
			mSensingDutyTimer.cancel(mAlarmPendingIntent); 
		
	    mAppUsageProber.stopService();
	    mAppUsageProber = null;
	    
	    mDecibelProber.stopService();
	    mDecibelProber = null;
	    
	    mIlluminanceProber.stopService();
	    mIlluminanceProber = null;
	    
	    mMovementProber.stopService();
	    mMovementProber = null;
	    
	    mProximityProber.stopService();
	    mProximityProber = null;
	}
	
	private void startServiceNotification() {
        /*
		Intent resultIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent to the top of the stack
		stackBuilder.addNextIntent(resultIntent); 
		// Gets a PendingIntent containing the entire back stack
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
			.setContentTitle("SleepFit is running")
			.setContentText("SleepFit sensing service is running.")
			.setSmallIcon(R.drawable.ic_launcher)
			.setAutoCancel(false)
			.setOngoing(true)
			.setContentIntent(resultPendingIntent);
		Notification notification = builder.build();
		notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
		
		startForeground(1, notification);
		*/

        // Get the sleep time string and sleep debt string
        getSleepInfo();
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.sleep_notification_layout);
        remoteViews.setTextViewText(R.id.noti_sleeptime, mSleepTimeStr);
        remoteViews.setTextViewText(R.id.noti_sleepdebt, mSleepDebtStr);
        Log.d(Constants.TAG, "SleepTime Str = " + mSleepTimeStr + ", SleepDebt Str = " + mSleepDebtStr);

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_launcher_trans)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContent(remoteViews)
            .setColor(Color.parseColor("#ffffffff"))
            .setContentIntent(resultPendingIntent);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;

        startForeground(NOTIFICATION_ID, notification);
	}
	
	
	private void startRemindLogDailySleepNotification() {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Intent resultIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent to the top of the stack
		stackBuilder.addNextIntent(resultIntent); 
		// Gets a PendingIntent containing the entire back stack
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		
		String text = "Please do not forget to log your sleep time tonight and your wake up time tomorrow morning.";
		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
			.setContentTitle("Please log your sleep")
			.setSmallIcon(R.drawable.notification)
			.setAutoCancel(true)
			.setOnlyAlertOnce(true)
			.setStyle(new NotificationCompat.BigTextStyle().bigText(text))
			.setContentIntent(resultPendingIntent);
		mNotificationManager.notify(2, builder.build()); 
	}
	
	private void startRemindMissLogSleepNotification() {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Intent resultIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent to the top of the stack
		stackBuilder.addNextIntent(resultIntent); 
		// Gets a PendingIntent containing the entire back stack
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		
		String text = "It seems that you have not logged your sleep time for 16 hours, please remember to log it.";
		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
			.setContentTitle("Forgot to log your sleep?")
			.setSmallIcon(R.drawable.notification)
			.setAutoCancel(true)
			.setOnlyAlertOnce(true)
			.setStyle(new NotificationCompat.BigTextStyle().bigText(text))
			.setContentIntent(resultPendingIntent);
		mNotificationManager.notify(3, builder.build()); 
	}
	
	private void copyFile(File src, File dst) throws IOException {
	    FileInputStream inStream = new FileInputStream(src);
	    FileOutputStream outStream = new FileOutputStream(dst);
	    FileChannel inChannel = inStream.getChannel();
	    FileChannel outChannel = outStream.getChannel();
	    inChannel.transferTo(0, inChannel.size(), outChannel);
	    inStream.close();
	    outStream.close();
	}

	@Override
	public void onRequestTaskCompleted(String json) {
		if (json != null && !json.equals("")) {
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
			String trackDate = formatter.format(new Date(System.currentTimeMillis()));
			
			try {
				JSONObject jsonObj = new JSONObject(json);
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
				String sleepTimeStr = jsonObj.getString("sleepTime");
				String wakeupTimeStr = jsonObj.getString("wakeupTime");
				Date sleepTime = dateFormat.parse(sleepTimeStr);
				Date wakeupTime = dateFormat.parse(wakeupTimeStr);

                List<SleepLogger> sleeps = null;
                if (mSleepLogDao != null) {
                    QueryBuilder<SleepLogger, Integer> qb = mSleepLogDao.queryBuilder();
                    try {
                        qb.orderBy("id", false);
                        sleeps = mSleepLogDao.query(qb.prepare());
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Log.e(Constants.TAG, e.toString());
                    }
                }

                if (sleeps != null && sleeps.size() > 0) {
                    SleepLogger latestSleep = sleeps.get(0);

                    if (latestSleep.getTrackDate().equals(trackDate) && !latestSleep.getFinished()) {
                        UpdateBuilder<SleepLogger,Integer> ub = mSleepLogDao.updateBuilder();
                        try {
                            ub.where().eq("trackDate", trackDate);
                            ub.updateColumnValue("sleepTime", sleepTime);
                            ub.updateColumnValue("wakeupTime", wakeupTime);
                            mSleepLogDao.update(ub.prepare());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else if (!latestSleep.getTrackDate().equals(trackDate)) {
                        SleepLogger mSleepLog = new SleepLogger(System.currentTimeMillis(), trackDate, sleepTime, wakeupTime, 0, 0, false, true);
                        try {
                            mSleepLogDao.create(mSleepLog);
                        } catch (SQLException e) {
                            Log.d(Constants.TAG, "Add new sleeplogger data record failed: " + e.toString());
                            e.printStackTrace();
                        }
                    }

                    startServiceNotification();
                } else {
                    SleepLogger mSleepLog = new SleepLogger(System.currentTimeMillis(), trackDate, sleepTime, wakeupTime, 0, 0, false, true);
                    try {
                        mSleepLogDao.create(mSleepLog);
                    } catch (SQLException e) {
                        Log.d(Constants.TAG, "Add new sleeplogger data record failed: " + e.toString());
                        e.printStackTrace();
                    }
                }
			} catch (JSONException e) {
				Log.e(Constants.TAG, e.toString());
			} catch (ParseException e1) {
				Log.e(Constants.TAG, e1.toString());
			}
		}
	}

    private void getSleepInfo() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        String trackDate = dateFormatter.format(new Date(System.currentTimeMillis()));

        List<SleepLogger> sleeps = null;
        QueryBuilder<SleepLogger, Integer> qb = mSleepLogDao.queryBuilder();
        try {
            qb.where().eq("trackDate", trackDate);
            sleeps = mSleepLogDao.query(qb.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, e.toString());
        }
        if (sleeps != null && sleeps.size() > 0) {
            SleepLogger sleepLog = sleeps.get(0);
            if (sleepLog.getSleepTime() != null && sleepLog.getWakeupTime() != null) {
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm", Locale.US);
                String sleepTimeStr = formatter.format(sleepLog.getSleepTime());
                String wakeTimeStr  = formatter.format(sleepLog.getWakeupTime());
                mSleepTimeStr = sleepTimeStr + " - " + wakeTimeStr;

                SharedPreferences preferences = getSharedPreferences(Constants.SURVEY_FILE_NAME, Context.MODE_PRIVATE);
                float sleepHours = Float.valueOf(preferences.getString("sleepHours", "8"));
                float wakeSleepRatio = (float) ((24.0f - sleepHours) / sleepHours);
                long duration = sleepLog.getWakeupTime().getTime() - sleepLog.getSleepTime().getTime();
                duration += sleepLog.getNaptime() * 60 * 1000;
                float sleepTimeHours = (float) duration / 1000 / 60 / 60;
                float neededSleep = (24 - sleepTimeHours) / wakeSleepRatio;
                float sleepDebt = sleepTimeHours - neededSleep;
                int sleepDebtMinutes = (int) (Math.abs(sleepDebt) * 60);
                if (sleepDebt < 0) {
                    mSleepDebtStr = "-" + String.valueOf(sleepDebtMinutes / 60) + " Hours " + String.valueOf(sleepDebtMinutes % 60) + " Minutes";
                } else {
                    mSleepDebtStr = "+" + String.valueOf(sleepDebtMinutes / 60) + " Hours " + String.valueOf(sleepDebtMinutes % 60) + " Minutes";
                }
            }
        }
    }

}
