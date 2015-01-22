package edu.uml.swin.sleepfit.sensing;

import java.sql.SQLException;
import java.util.ArrayList;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.DB.ProximityRaw;
import edu.uml.swin.sleepfit.util.Constants;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ProximityProber implements SensorEventListener {

	private Context mContext;
	private SensorManager mSensorManager;
	private Sensor mProximity;
	//private float mDistanceValue;
	//private int mDistanceCount;
	private Handler mHandler;
	//private ArrayList<Float> mValues; 
	private ArrayList<Float> mProximityRaw;
	private ProximityProber mSelf;
	private DatabaseHelper mDatabaseHelper;
	private Dao<ProximityRaw, Integer> mDao;
	
	public ProximityProber(Context context) {
		mContext = context;
		mSelf = this;
		//mDistanceValue = 0.0f;
		//mDistanceCount = 0;
		
		//mValues = new ArrayList<Float>();
		mProximityRaw = new ArrayList<Float>();
		
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY); 
		
		mDatabaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
		try {
			mDao = mDatabaseHelper.getProximityRawDao();
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Cannot get the ProximityRaw DAO: " + e.toString());
			e.printStackTrace();
		}
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case Constants.PROXIMITY_SENSE_TIMEOUT_MSG: 
					mSensorManager.unregisterListener(mSelf);
					Log.d(Constants.TAG, "Proximity probing finished!");
					/*
					float retValue;
					if (mDistanceCount == 0) retValue = 99999.0f;
					else retValue = mDistanceValue / mDistanceCount;
					mValues.add(retValue);
					sendEmptyMessageDelayed(Constants.PROXIMITY_SENSE_IDLE_FINISHED_MSG, (Constants.SUB_SENSING_DUTY_INTERVAL_SECOND - Constants.PROXIMITY_SENSE_DURATION_SECOND) * 1000);
					
					if (mProximityRaw.size() > 0) {
						ProximityRaw newRaw = new ProximityRaw(System.currentTimeMillis(), mProximityRaw);
						try {
							mDao.create(newRaw);
						} catch (SQLException e) {
							Log.d(Constants.TAG, "Add new ProximityRaw data record failed: " + e.toString());
							e.printStackTrace();
						}
					}
					*/
					sendEmptyMessageDelayed(Constants.PROXIMITY_SENSE_IDLE_FINISHED_MSG, (Constants.SUB_SENSING_DUTY_INTERVAL_SECOND - Constants.PROXIMITY_SENSE_DURATION_SECOND) * 1000);
					break;
				case Constants.PROXIMITY_SENSE_IDLE_FINISHED_MSG:
					//mDistanceValue = 0.0f;
					//mDistanceCount = 0;
					
					if (mProximity != null) {
						mSensorManager.registerListener(mSelf, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
					}
					mHandler.sendEmptyMessageDelayed(Constants.PROXIMITY_SENSE_TIMEOUT_MSG, Constants.PROXIMITY_SENSE_DURATION_SECOND * 1000);
					//mProximityRaw = new ArrayList<Float>();
					
					break;
				default:
					super.handleMessage(msg);
					break;
				}
			}
		};
	}
	
	public void startService() {
		if (mProximity == null) {
			Log.d(Constants.TAG, "Proximity prober: no proximity sensor available");
		} else {
			mSensorManager.registerListener(mSelf, mProximity, SensorManager.SENSOR_DELAY_GAME);
		}
		mHandler.sendEmptyMessageDelayed(Constants.PROXIMITY_SENSE_TIMEOUT_MSG, Constants.PROXIMITY_SENSE_DURATION_SECOND * 1000);
	}
	
	public void stopService() {
		mHandler = null;
		mSensorManager.unregisterListener(this);
	}
	
	public ArrayList<Float> getProximityContext() { 
		ArrayList<Float> ret = mProximityRaw;
		mProximityRaw = new ArrayList<Float>();
		
		ProximityRaw newRaw = new ProximityRaw(System.currentTimeMillis(), ret);
		try {
			mDao.create(newRaw);
		} catch (SQLException e) {
			Log.d(Constants.TAG, "Add new ProximityRaw data record failed: " + e.toString());
			e.printStackTrace();
		}
		
		return ret;
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		//Log.d(Constants.TAG, "Proximity sensing value = " + event.values[0]);
		
		//mDistanceValue += event.values[0];
		//mDistanceCount ++;
		mProximityRaw.add(event.values[0]);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// don't need to modify this right now 
	}

}
