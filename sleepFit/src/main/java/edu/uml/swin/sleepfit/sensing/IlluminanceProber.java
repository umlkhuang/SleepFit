package edu.uml.swin.sleepfit.sensing;

import java.sql.SQLException;
import java.util.ArrayList;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.DB.LightRaw;
import edu.uml.swin.sleepfit.util.Constants;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class IlluminanceProber implements SensorEventListener {

	private Context mContext;
	private SensorManager mSensorManager;
	private IlluminanceProber mSelf;
	private Sensor mLightSensor;
	//private float mLightValue;
	//private int mValueCounter;
	private Handler mHandler;
	//private ArrayList<Float> mValues; 
	private ArrayList<Float> mLightRaw;
	private DatabaseHelper mDatabaseHelper;
	private Dao<LightRaw, Integer> mDao;
	
	public IlluminanceProber(Context context) {
		mContext = context;
		mSelf = this;
		//mLightValue = 0.0f;
		//mValueCounter = 0;
		
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		//mValues = new ArrayList<Float>();
		mLightRaw = new ArrayList<Float>();
		
		mDatabaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
		try {
			mDao = mDatabaseHelper.getLightRawDao();
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Cannot get the LightRaw DAO: " + e.toString());
			e.printStackTrace();
		}
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case Constants.LIGHT_SENSE_TIMEOUT_MSG:
					mSensorManager.unregisterListener(mSelf);
					Log.d(Constants.TAG, "Illuminance Value probing finished!");
					/*
					float retValue;
					if (mValueCounter == 0) retValue = 0.0f;
					else retValue = mLightValue / mValueCounter;
					mValues.add(retValue);
					sendEmptyMessageDelayed(Constants.LIGHT_SENSE_IDLE_FINISH_MSG, (Constants.SUB_SENSING_DUTY_INTERVAL_SECOND - Constants.LIGHT_SENSE_DURATION_SECOND) * 1000);
					
					if (mLightRaw.size() > 0) {
						LightRaw newRaw = new LightRaw(System.currentTimeMillis(), mLightRaw);
						try {
							mDao.create(newRaw);
						} catch (SQLException e) {
							Log.d(Constants.TAG, "Add new LightRaw data record failed: " + e.toString());
							e.printStackTrace();
						}
					}
					*/
					sendEmptyMessageDelayed(Constants.LIGHT_SENSE_IDLE_FINISH_MSG, (Constants.SUB_SENSING_DUTY_INTERVAL_SECOND - Constants.LIGHT_SENSE_DURATION_SECOND) * 1000);
					break;
				case Constants.LIGHT_SENSE_IDLE_FINISH_MSG:
					/*
					mLightValue = 0.0f;
					mValueCounter = 0;
					*/
					if (mLightSensor != null) {
						mSensorManager.registerListener(mSelf, mLightSensor, SensorManager.SENSOR_DELAY_GAME);
					}
					mHandler.sendEmptyMessageDelayed(Constants.LIGHT_SENSE_TIMEOUT_MSG, Constants.LIGHT_SENSE_DURATION_SECOND * 1000);
					//mLightRaw = new ArrayList<Float>();
					break;
				default:
					super.handleMessage(msg);
					break;
				}
			}
		}; 
	}
	
	public void startService() {
		if (mLightSensor == null) {
			Log.d(Constants.TAG, "Illuminance prober: no illuminance senser available");
			mHandler.sendEmptyMessageDelayed(Constants.LIGHT_SENSE_TIMEOUT_MSG, Constants.LIGHT_SENSE_DURATION_SECOND * 1000);
		} else {
			mSensorManager.registerListener(mSelf, mLightSensor, SensorManager.SENSOR_DELAY_GAME);
			mHandler.sendEmptyMessageDelayed(Constants.LIGHT_SENSE_TIMEOUT_MSG, Constants.LIGHT_SENSE_DURATION_SECOND * 1000);
		}
	}
	
	public void stopService() {
		mHandler = null;
		mSensorManager.unregisterListener(this);
	}
	
	public ArrayList<Float> getIlluminanceContext() {
		ArrayList<Float> ret = mLightRaw;
		mLightRaw = new ArrayList<Float>();
		
		LightRaw newRaw = new LightRaw(System.currentTimeMillis(), ret);
		try {
			mDao.create(newRaw);
		} catch (SQLException e) {
			Log.d(Constants.TAG, "Add new LightRaw data record failed: " + e.toString());
			e.printStackTrace();
		}
		
		return ret;
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// don't need to modify this right now 
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//Log.d(Constants.TAG, "Illuminance sensing value = " + event.values[0]);
		
		//mLightValue +=  event.values[0];
		//mValueCounter ++; 
		mLightRaw.add(event.values[0]);
	}

}
