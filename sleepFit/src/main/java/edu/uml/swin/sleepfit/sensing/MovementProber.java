package edu.uml.swin.sleepfit.sensing;

import java.sql.SQLException;
import java.util.ArrayList;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.DB.MovementRaw;
import edu.uml.swin.sleepfit.util.Constants;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MovementProber implements SensorEventListener {

	private Context mContext;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private int mMovementCount;
	private Handler mHandler;
	private float mAccelLast;
	private float mAccelCurrent;
	private float mAccel;
	private ArrayList<Float> mMovementRaw;
	private DatabaseHelper mDatabaseHelper;
	private Dao<MovementRaw, Integer> mDao;
	private MovementProber mSelf;
	
	public MovementProber(Context context) {
		mContext = context;
		mSelf = this;
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mAccelLast = SensorManager.GRAVITY_EARTH;
		mAccelCurrent = SensorManager.GRAVITY_EARTH;
		mAccel = 0.00f;
		mMovementCount = 0;
		mMovementRaw = new ArrayList<Float>();
		
		mDatabaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
		try {
			mDao = mDatabaseHelper.getMovementRawDao();
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Cannot get the MovementRaw DAO: " + e.toString());
			e.printStackTrace();
		}
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case Constants.MOVEMENT_TIMEOUT_MSG:
					mSensorManager.unregisterListener(mSelf);
					/*
					if (mMovementRaw.size() > 0) {
						MovementRaw newRaw = new MovementRaw(System.currentTimeMillis(), mMovementRaw);
						try {
							mDao.create(newRaw);
						} catch (SQLException e) {
							Log.d(Constants.TAG, "Add new MovementRaw data record failed: " + e.toString());
							e.printStackTrace();
						}
					}
					*/
					sendEmptyMessageDelayed(Constants.MOVEMENT_IDLE_FINISH_MSG, (Constants.MOVEMENT_DUTY_INTERVAL_SECOND - Constants.SENSING_DURATION_SECOND) * 1000);
					break;
				case Constants.MOVEMENT_IDLE_FINISH_MSG:
					//mMovementRaw = new ArrayList<Float>();
					mSensorManager.registerListener(mSelf, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
					sendEmptyMessageDelayed(Constants.MOVEMENT_TIMEOUT_MSG, Constants.SENSING_DURATION_SECOND * 1000);
					break;
				default:
					super.handleMessage(msg);
					break;
				}
			}
		};
	}
	
	public void startService() {
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		mHandler.sendEmptyMessageDelayed(Constants.MOVEMENT_TIMEOUT_MSG, Constants.SENSING_DURATION_SECOND * 1000);
	}
	
	public void stopService() {
		mHandler = null;
		mSensorManager.unregisterListener(this);
	}
	
	public int getMovementInof() {
		int ret = mMovementCount;
		mMovementCount = 0;
		
		MovementRaw newRaw = new MovementRaw(System.currentTimeMillis(), mMovementRaw);
		try {
			mDao.create(newRaw);
		} catch (SQLException e) {
			Log.d(Constants.TAG, "Add new MovementRaw data record failed: " + e.toString());
			e.printStackTrace();
		}
		mMovementRaw = new ArrayList<Float>();
		
		return ret;
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
			mMovementCount ++;
		}
		if (mAccelCurrent > SensorManager.GRAVITY_EARTH + 1.0) {
			mMovementRaw.add(mAccelCurrent);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// don't need to modify this right now 
	}

}
