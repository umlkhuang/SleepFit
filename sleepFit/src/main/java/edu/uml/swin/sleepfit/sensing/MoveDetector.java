package edu.uml.swin.sleepfit.sensing;

import edu.uml.swin.sleepfit.util.Constants;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.util.Log;

public class MoveDetector implements SensorEventListener {

	private Context mContext;
	private MoveDetector mSelf;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	//private boolean mSensingRegistered;
	//private boolean mIsSensing;
	private CountDownTimer mSensingTimer;
	private CountDownTimer mStillCountDown;
	private CountDownTimer mIdleTimer; 
	private AlarmManager mSensingDutyTimer;
	private Intent mAlarmIntent;
	private PendingIntent mAlarmPendingIntent;
	private boolean mSensingDutyRunning;
	private int mEventsCounter;
	private float mAccelLast;
	private float mAccelCurrent;
	private float mAccel;
	//private SystemLogger mSysLogger;
	
	public MoveDetector(Context context) {
		mContext = context;
		mSelf = this;
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		//mSensingRegistered = false;
		//mIsSensing = false;
		mEventsCounter = 0;
		mAccelLast = SensorManager.GRAVITY_EARTH;
		mAccelCurrent = SensorManager.GRAVITY_EARTH;
		mAccel = 0.00f;
		//mSysLogger = new SystemLogger(context);
		mSensingDutyRunning = false;
		
		mSensingDutyTimer = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE); 
		
		mSensingTimer = new CountDownTimer(Constants.SENSING_DURATION_SECOND * 1000, 1 * 1000) {
			@Override
            public void onFinish() {
	            if (mEventsCounter >= Constants.MOVEMENT_COUNT_THRESHOLD) {
	            	Log.d(Constants.TAG, "MoveDetector Sensing timer: Detected enough movement!"); 
	            	
	            	// Check if the sensing duty is running, cancel it if it's working 
	            	if (mSensingDutyRunning) {
	            		Log.d(Constants.TAG, "MoveDetector Sensing timer: cancel the sensing duty timer");
	            		
	            		mSensingDutyTimer.cancel(mAlarmPendingIntent); 
	            		mSensingDutyRunning = false;
	            	}
	            } else if (!mSensingDutyRunning) { 
	            	Log.d(Constants.TAG, "Started the sensing duty timer!"); 
	            	
	            	mAlarmIntent = new Intent(mContext, AlarmReceiver.class);
					mAlarmPendingIntent = PendingIntent.getBroadcast(mContext, 989898, mAlarmIntent, 0); 
	            	mSensingDutyTimer.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Constants.SENSING_DUTY_INTERVAL_SECOND * 1000, 
	            			Constants.SENSING_DUTY_INTERVAL_SECOND * 1000, mAlarmPendingIntent); 
	            	
	            	mSensingDutyRunning = true; 
	            } 
	            
	            Log.d(Constants.TAG, "MoveDetector Sensing timer: start the idle period");
	            mEventsCounter = 0;
	            // Unregister sensing for REPEAT_INTERVAL_SECOND seconds
	            mSensorManager.unregisterListener(mSelf);
	            // Start the idle timer 
	            mIdleTimer.start();
            }

			@Override
            public void onTick(long millisUntilFinished) {
	            // No code here 
            }
		};
		
		mIdleTimer = new CountDownTimer(Constants.REPEAT_INTERVAL_SECOND * 1000, 1 * 1000) {
			@Override
            public void onFinish() {
	            Log.d(Constants.TAG, "MoveDetector idle timer: idle timer finished, register the sensor again");
	            
	            // Register the accelerometer sensor and start the sensing timer again 
	            mSensorManager.registerListener(mSelf, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	            mSensingTimer.cancel();
	            mSensingTimer.start();
            }

			@Override
            public void onTick(long millisUntilFinished) {
	            // No code here 
            }
		};
	}
	
	@Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// don't need to modify this right now 
    }

	@Override
    public void onSensorChanged(SensorEvent event) {
		float[] accValues = event.values.clone();
		// Shake detection
		float x = accValues[0];
		float y = accValues[1];
		float z = accValues[2];
		mAccelLast = mAccelCurrent;
		mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);
		float delta = Math.abs(mAccelCurrent - mAccelLast); 
		mAccel = mAccel * 0.9f + delta; 
		
		if (mAccel >= Constants.ACTIVE_MOVEMENT_THRESHOLD) {
			//Log.d(Constants.TAG, "Acc new value, mAccelCurrent = " + mAccelCurrent + ", delta = " + delta + ", mAccel = " + mAccel);
			
			mEventsCounter++;
		}
    }

	public void startService() {
		Log.d(Constants.TAG, "Starting the Accel sensing service, start to detect movement");
		
		mSensingTimer.start();
	}
	
	public void stopService() {
		Log.d(Constants.TAG, "Stopping the Accel sensing service, stop detecting movement");
		
		mSensorManager.unregisterListener(mSelf);
		mSensingTimer.cancel();
		mStillCountDown.cancel();
		mIdleTimer.cancel();
		//mSensingDutyTimer.cancel();
		if (mSensingDutyTimer != null && mSensingDutyRunning) 
			mSensingDutyTimer.cancel(mAlarmPendingIntent);
	}
}
