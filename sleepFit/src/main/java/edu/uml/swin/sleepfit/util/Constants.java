package edu.uml.swin.sleepfit.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;

public class Constants {
	// Constants definition for the global application 
	public static final String TAG = "SleepFit";
	public static final String APPNAME = "SleepFit";
	public static final String VERSION = "0.1.0";
	public static final String DB_NAME = "sleepfit.db";
	
	// The system log file name 
	public static final String LOG_FILE_NAME = "SleepFitSysLog.txt"; 
	
	// Default invalid location longitude/latitude value 
	public static final double INVALID_GEO_VALUE = -9999.0;
	
	// Constants for accel sensing and movement detection
	public static final int REPEAT_INTERVAL_SECOND = 10; 		// 10 seconds
	public static final int SENSING_DURATION_SECOND = 10; 		// 10 seconds
	public static final int STILL_COUNT_DOWN_SECOND = 5 * 60;	// 5 minutes
	public static final int SENSING_DUTY_INTERVAL_SECOND = 5 * 60;	// 5 minutes 
	public static final int SUB_SENSING_DUTY_INTERVAL_SECOND = 30;  // 30 seconds 
	public static final int MOVEMENT_DUTY_INTERVAL_SECOND = 20; 	// 20 seconds 
	public static final int MOVEMENT_COUNT_THRESHOLD = 20; 
	public static final float ACTIVE_MOVEMENT_THRESHOLD = 8.5f;
	public static final String COLLECT_CONTEXT_DUTY_MSG = "edu.uml.swin.sleepfit.sensing"; 
	public static final int MOVEMENT_TIMEOUT_MSG = 1;
	public static final int MOVEMENT_IDLE_FINISH_MSG = 2;
	
	
	// Constants for decibel prober 
	public static final int DECIBEL_RECORD_TIMEOUT_MSG = 10;
	public static final int DECIBEL_RECORD_IDLE_FINISH_MSG = 11;
	public static final int DECIBEL_TIMEOUT_SECOND = 3; 	// record 3 seconds audio 
	
	// Constants for WiFi prober
	public static final int WIFI_SCAN_TRIGGER_MSG = 20;
	public static final int WIFI_SCAN_TIMEOUT_MSG = 21;
	public static final int WIFI_SCAN_DURATION_SECOND = 10;	// scan 10 seconds 
	
	// Constants for location prober 
	public static final int LOCATION_GPS_MESSAGE_TIMEOUT_MSG = 30;
	public static final int LOCATION_NETWORK_MESSAGE_TIMEOUT_MSG = 31;
	public static final int LOCATIOIN_GPS_SCAN_DURATION_SECOND = 120;	// 120 seconds 
	public static final int LOCATION_NETWORK_SCAN_DURATION_SECOND = 20;	// 20 seconds 
	public static final int LOCATIOIN_UPDATE_INTERVAL_SECOND = 10;		// at least 10 seconds 
	public static final float LOCATIOIN_UPDATE_MIN_DISTANCE = 25.0f;	// at least 25m 
	
	// Constants for illuminance prober
	public static final int LIGHT_SENSE_TIMEOUT_MSG = 40;
	public static final int LIGHT_SENSE_IDLE_FINISH_MSG = 41;
	public static final int LIGHT_SENSE_DURATION_SECOND = 3;	// 5 seconds
	
	// System events definitions 
	public static final int SYSTEM_POWER_ON = 1;
	public static final int SYSTEM_POWER_OFF = 2;
	public static final int SCREEN_ON = 3;
	public static final int SCREEN_OFF = 4;
	public static final int POWER_CONNECTED = 5;
	public static final int POWER_DISCONNECTED = 6;
	public static final int USER_LOGGED_SLEEP_EVENT = 7;
	public static final int USER_LOGGED_WAKE_EVENT = 8;
	
	// App usage constants
	public static final int APP_USAGE_PROBE_PERIOD_SECOND = 1;	// every 1 second
	
	// Proximity constants
	public static final int PROXIMITY_SENSE_TIMEOUT_MSG = 50;
	public static final int PROXIMITY_SENSE_IDLE_FINISHED_MSG = 51;
	public static final int PROXIMITY_SENSE_DURATION_SECOND = 3; 	// 2 seconds 
	
	public static final int LATEST_SLEEP_LOG_NUMBER = 5;
	
	// Constants for uploading files 
	public static final String POST_FILE_URL = "http://swin06.cs.uml.edu/sleepfit/processdata/upload";
	public static final String GET_SLEEP_URL = "http://swin06.cs.uml.edu/sleepfit/processdata/getSleep";
	public static final String UPDATE_TIME_FILE = "upload_time_preference";
	public static final String SURVEY_FILE_NAME = "survey";
	public static final String USER_CONFIG_FILE = "configuration";
	public static final String SETTING_FILE = "setting";
	public static final String POST_SURVEY_URL = "http://swin06.cs.uml.edu/sleepfit/processdata/uploadSurvey";
	public static final String TMP_PREF_FILE = "temp_pref";
	
	// Data collection expiration date 
	public static final int EXPIRE_YEAR = 2214;
	public static final int EXPIRE_MONTH = 12; 
	public static final int EXPIRE_DAY = 31; 
	
	// Messages and constants for the notification to reminder user to log sleep time 
	public static final String USER_LOGGED_SLEEP_TIME = "sleepfit.user_logged_sleep_time";
	public static final String USER_LOGGED_WAKEUP_TIME = "sleepfit.user_logged_wakeup_time";
	public static final int REMIND_TO_LOG_SLEEP_TIME_HOUR = 22; 	// Remind user to log sleep time after 22:00 
	public static final int REMIND_TO_LOG_MAX_DURATION_SECONDS = 16 * 60 * 60; 	// Remind user to log sleep time after 16 hours
	
	public static final String BASE_SERVER_URL = "http://swin06.cs.uml.edu/sleepfit/processdata/";
	
	public static final int DELETE_OLD_DATA_DAYS = 31; // 31 day 
	public static final long UPLOAD_DATA_INTERVAL = 5 * 60 * 60 * 1000;			// 5 hours
	public static final long UPLOAD_DATA_INTERVAL_SMALL = 1 * 60 * 60 * 1000;	// 1 hours

    public static final String UPDATED_SLEEP_INFO = "need_to_update_sleep_info";
	
	public static String getUUID(Context context) {
		String uuid, serialNo, androidId;
		
		serialNo = android.os.Build.SERIAL; 
		androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		uuid = serialNo + "-" + androidId;
		// MD5 hash the device ID 
		StringBuffer hexString = new StringBuffer();
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(uuid.getBytes());
			byte msgBytes[] = messageDigest.digest();
			for (int i = 0; i < msgBytes.length; i++)
				hexString.append(Integer.toHexString(0xFF & msgBytes[i]));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return hexString.toString();
	}
	
	public static String getAccessCode(Context context) {
		/*
		SharedPreferences preferences = context.getSharedPreferences(Constants.USER_CONFIG_FILE, Context.MODE_PRIVATE);
		String code = preferences.getString("code", "");
		return code;
		*/
		return Constants.getUUID(context);
	}
}
