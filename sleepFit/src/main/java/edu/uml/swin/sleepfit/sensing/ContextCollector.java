package edu.uml.swin.sleepfit.sensing;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;

import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.DB.SensingData;
import edu.uml.swin.sleepfit.util.Constants;
import android.content.Context;
import android.location.Location;
import android.util.Log;

public class ContextCollector implements WifiProberCallbackIf,
										//LocationProberCallbackIf,
										PowerStatusProberCallbackIf {
	
	private Context mContext;
	private ContextCollector mSelf;
	//private SystemLogger mSysLogger;
	private long mCurrentTime;
	private int mSensingDataId;
	private DatabaseHelper mDatabaseHelper;
	private Dao<SensingData, Integer> mDao;
	private SensingData mNewSensing;
	
	//private DecibelProber mDecibelProber;
	private WifiProber mWifiProber;
	//private LocationProber mLocationProber;
	//private IlluminanceProber mIlluminanceProber;
	private PowerStatusProber mPowerStatusProber;
	//private MovementProber mMovementProber;
	
	public ContextCollector(Context context) {
		mContext = context;
		mSelf = this;
		//mSysLogger = new SystemLogger(mContext);
		
		//mDecibelProber = new DecibelProber(mContext);
		mWifiProber = new WifiProber(mContext);
		//mLocationProber = new LocationProber(mContext);
		//mIlluminanceProber = new IlluminanceProber(mContext);
		mPowerStatusProber = new PowerStatusProber(mContext);
		//mMovementProber = new MovementProber(mContext);
		
		mDatabaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class); 

		mCurrentTime = System.currentTimeMillis();
		//SensingData newSensing = new SensingData(mCurrentTime);
		mNewSensing = new SensingData(mCurrentTime);
		
		/*
		try {
			mDao = mDatabaseHelper.getSensingDataDao();
		} catch (SQLException e) {
			//mSysLogger.addLog("Get SensingData DAO fail: " + e.toString());
			Log.e(Constants.TAG, "Cannot get the SensingData DAO: " + e.toString());
			e.printStackTrace();
		}
		try {
			mDao.create(newSensing);
			mSensingDataId = newSensing.getId();
		} catch (SQLException e) {
			//mSysLogger.addLog("Add new SensingData record fail: " + e.toString());
			Log.d(Constants.TAG, "Add new sensing data record failed: " + e.toString());
			e.printStackTrace();
		}
		*/
	}
	
	public void saveNewSensingToDatabase() {
		try {
			mDao = mDatabaseHelper.getSensingDataDao();
			mDao.create(mNewSensing);
			mSensingDataId = mNewSensing.getId();
		} catch (SQLException e) {
			Log.d(Constants.TAG, "Add new sensing data record failed: " + e.toString());
			e.printStackTrace();
		}
	}
	
	public void getDecibelValue(ArrayList<Integer> decibelValues) {
		float maxValue = Integer.MIN_VALUE;
		float minValue = Integer.MAX_VALUE;
		float avgValue = 0.0f; 
		float stdValue = 0.0f;
		
		
		if (decibelValues == null || decibelValues.size() == 0) {
			maxValue = 0.0f;
			minValue = 0.0f;
			avgValue = 0.0f;
			stdValue = 0.0f;
		} else {
			for (Integer value : decibelValues) {
				if (value > maxValue) maxValue = value;
				if (value < minValue) minValue = value;
				avgValue += value;
			}
			avgValue /= decibelValues.size(); 
			
			float variance = 0.0f;
			for (Integer value : decibelValues) {
				variance += Math.pow(value - avgValue, 2);
			}
			stdValue = (float) Math.sqrt(variance / decibelValues.size());
		}
		
		mNewSensing.setDecibelMax(maxValue);
		mNewSensing.setDecibelMin(minValue);
		mNewSensing.setDecibelAvg(avgValue);
		mNewSensing.setDecibelStd(stdValue);
		
	    Log.d(Constants.TAG, "Max Decibel Value = " + maxValue + ", Min value = " + minValue + ", Average value = " + avgValue + ", STD = " + stdValue);
        
	    /*
        try {
        	UpdateBuilder<SensingData, Integer> updateBuilder = mDao.updateBuilder();
        	updateBuilder.where().eq("id", mSensingDataId);
        	updateBuilder.updateColumnValue("decibelMax", maxValue);
        	updateBuilder.updateColumnValue("decibelMin", minValue);
        	updateBuilder.updateColumnValue("decibelAvg", avgValue);
        	updateBuilder.updateColumnValue("decibelStd", stdValue);
        	updateBuilder.update();
        } catch (SQLException e) {
        	//mSysLogger.addLog("Update decibel context fail: " + e.toString());
			Log.d(Constants.TAG, "Update decibel context failed: " + e.toString());
			e.printStackTrace();
		} 
		*/
	}
	
	public void getWifiSSIDs() {
		mWifiProber.getWifiContext(mSelf);
	}
	
	/*
	public void getLocationInfo() {
		mLocationProber.getLocationInfo(mSelf);
	}
	*/
	
	public void getIlluminanceValue(ArrayList<Float> values) {
		float maxValue = Float.MIN_VALUE;
		float minValue = Float.MAX_VALUE;
		float avgValue = 0.0f; 
		float stdValue = 0.0f;
		
		if (values == null || values.size() == 0) {
			maxValue = 0.0f;
			minValue = 0.0f;
			avgValue = 0.0f;
			stdValue = 0.0f;
		} else {
			for (Float value : values) {
				if (value > maxValue) maxValue = value;
				if (value < minValue) minValue = value;
				avgValue += value;
			}
			avgValue /= values.size(); 
			
			float variance = 0.0f;
			for (Float value : values) {
				variance += Math.pow(value - avgValue, 2);
			}
			stdValue = (float) Math.sqrt(variance / values.size());
		}
		
		mNewSensing.setIlluminanceMax(maxValue);
		mNewSensing.setIlluminanceMin(minValue);
		mNewSensing.setIlluminanceAvg(avgValue);
		mNewSensing.setIlluminanceStd(stdValue);
		
		Log.d(Constants.TAG, "Max Illuminance Value = " + maxValue + ", Min value = " + minValue + ", Average value = " + avgValue + ", STD = " + stdValue);
	    
		/*
	    try {
        	UpdateBuilder<SensingData, Integer> updateBuilder = mDao.updateBuilder();
        	updateBuilder.where().eq("id", mSensingDataId);
        	updateBuilder.updateColumnValue("illuminanceMax", maxValue);
        	updateBuilder.updateColumnValue("illuminanceMin", minValue);
        	updateBuilder.updateColumnValue("illuminanceAvg", avgValue);
        	updateBuilder.updateColumnValue("illuminanceStd", stdValue);
        	updateBuilder.update();
        } catch (SQLException e) {
        	//mSysLogger.addLog("Update illuminance context fail: " + e.toString());
			Log.d(Constants.TAG, "Update Illuminance context failed: " + e.toString());
			e.printStackTrace();
		}
		*/
	}
	
	public void getPowerStatus() {
		mPowerStatusProber.getPowerStatus(mSelf);
	} 
	
	public void getMovementInfo(int counts) {
		Log.d(Constants.TAG, "Movement counts = " +  counts);
		
		mNewSensing.setMovement(counts);
		
		/*
		try {
        	UpdateBuilder<SensingData, Integer> updateBuilder = mDao.updateBuilder();
        	updateBuilder.where().eq("id", mSensingDataId);
        	updateBuilder.updateColumnValue("movement", counts);
        	updateBuilder.update();
        } catch (SQLException e) {
        	//mSysLogger.addLog("Update movement context fail: " + e.toString());
			Log.d(Constants.TAG, "Update movement context failed: " + e.toString());
			e.printStackTrace();
		} 
		*/
	}
	
	public void getProximityInfo(ArrayList<Float> values) {
		float avgValue = 0.0f;
		if (values == null || values.size() == 0) {
			avgValue = 0.0f;
		} else {
			for (Float value : values) {
				avgValue += value;
			}
			avgValue /= values.size();
		}
		
		Log.d(Constants.TAG, "Average Proximity value = " + avgValue);
		
		mNewSensing.setProximity(avgValue);
		
		/*
	    try {
        	UpdateBuilder<SensingData, Integer> updateBuilder = mDao.updateBuilder();
        	updateBuilder.where().eq("id", mSensingDataId);
        	updateBuilder.updateColumnValue("proximity", avgValue);
        	updateBuilder.update();
        } catch (SQLException e) {
        	//mSysLogger.addLog("Update proximity context fail: " + e.toString());
			Log.d(Constants.TAG, "Update proximity context failed: " + e.toString());
			e.printStackTrace();
		} 
		*/
	}
	
	public void getAppUsage(String usage) {
		Log.d(Constants.TAG, "App Usage Statistic = " + usage);
		
		mNewSensing.setAppUsage(usage);
		
		/*
		try {
			UpdateBuilder<SensingData, Integer> updateBuilder = mDao.updateBuilder();
        	updateBuilder.where().eq("id", mSensingDataId);
        	updateBuilder.updateColumnValue("appUsage", usage);
        	updateBuilder.update();
		} catch (SQLException e) {
			//mSysLogger.addLog("Update App usage context fail: " + e.toString());
			Log.d(Constants.TAG, "App Usage context failed: " + e.toString());
			e.printStackTrace();
		} 
		*/
	}

	/*
	@Override
    public void onDecibelProbeFinished(ArrayList<Integer> decibelValues) { 
		int maxValue = Integer.MIN_VALUE;
		int minValue = Integer.MAX_VALUE;
		int avgValue = 0; 
		
		if (decibelValues == null || decibelValues.size() == 0) {
			maxValue = 0;
			minValue = 0;
			avgValue = 0;
		} else {
			for (Integer value : decibelValues) {
				if (value > maxValue) maxValue = value;
				if (value < minValue) minValue = value;
				avgValue += value;
			}
			avgValue /= decibelValues.size(); 
		}
		
	    Log.d(Constants.TAG, "Max Decibel Value = " + maxValue + ", Min value = " + minValue + ", Average value = " + avgValue);
        
        try {
        	UpdateBuilder<SensingData, Integer> updateBuilder = mDao.updateBuilder();
        	updateBuilder.where().eq("id", mSensingDataId);
        	updateBuilder.updateColumnValue("decibelMax", maxValue);
        	updateBuilder.updateColumnValue("decibelMin", minValue);
        	updateBuilder.updateColumnValue("decibelAvg", avgValue);
        	updateBuilder.update();
        } catch (SQLException e) {
        	mSysLogger.addLog("Update decibel context fail: " + e.toString());
			Log.d(Constants.TAG, "Update decibel context failed: " + e.toString());
			e.printStackTrace();
		} 
    }
    */

	@Override
    public void onWifiProbeFinished(HashSet<String> setSSIDs) {
        String strSSID = setSSIDs.toString();
		Log.d(Constants.TAG, "WiFi SSIDs = " + strSSID);
        //mSysLogger.addLog("WiFi prober: finished WiFi probing");
		
        // delete the quotation marks 
        String finalSSID = strSSID.substring(1, strSSID.length() - 1);
        
        mNewSensing.setSsid(finalSSID);
        
        /*
         * It takes the longest time to finish the sensing, after that
         * we need to save the instance to database here, otherwise no
         * wifi will be saved to database. 
         * 
         */
        saveNewSensingToDatabase();
        
        /*
        try {
        	UpdateBuilder<SensingData, Integer> updateBuilder = mDao.updateBuilder();
        	updateBuilder.where().eq("id", mSensingDataId);
        	updateBuilder.updateColumnValue("ssid", finalSSID);
        	updateBuilder.update();
        } catch (SQLException e) {
        	//mSysLogger.addLog("Update WiFi context fail: " + e.toString());
			Log.d(Constants.TAG, "Update WiFi SSIDs context failed: " + e.toString());
			e.printStackTrace();
		} 
		*/
    }

	/*
	@Override
    public void onLocationProbeFinished(Location locInfo) {
	    Log.d(Constants.TAG, "Location context: longitude = " + locInfo.getLongitude() + ", latitude = " + locInfo.getLatitude());
	    //mSysLogger.addLog("Location prober: finished location probing");
	    
	    try {
        	UpdateBuilder<SensingData, Integer> updateBuilder = mDao.updateBuilder();
        	updateBuilder.where().eq("id", mSensingDataId);
        	updateBuilder.updateColumnValue("longitude", locInfo.getLongitude());
        	updateBuilder.updateColumnValue("latitude", locInfo.getLatitude());
        	updateBuilder.update();
        } catch (SQLException e) {
        	mSysLogger.addLog("Update location context fail: " + e.toString());
			Log.d(Constants.TAG, "Update Location context failed: " + e.toString());
			e.printStackTrace();
		} 
    }
	
	@Override
    public void onNoLocationAvailable() {
		Log.d(Constants.TAG, "Location context: no location context available");
	    //mSysLogger.addLog("Location prober: no location context available");
    }
    */

	/*
	@Override
    public void onIlluminanceProberFinished(ArrayList<Float> values) {
		float maxValue = Float.MIN_VALUE;
		float minValue = Float.MAX_VALUE;
		float avgValue = 0.0f; 
		
		if (values == null || values.size() == 0) {
			maxValue = 0;
			minValue = 0;
			avgValue = 0;
		} else {
			for (Float value : values) {
				if (value > maxValue) maxValue = value;
				if (value < minValue) minValue = value;
				avgValue += value;
			}
			avgValue /= values.size(); 
		}
		
		Log.d(Constants.TAG, "Max Illuminance Value = " + maxValue + ", Min value = " + minValue + ", Average value = " + avgValue);
	    
	    try {
        	UpdateBuilder<SensingData, Integer> updateBuilder = mDao.updateBuilder();
        	updateBuilder.where().eq("id", mSensingDataId);
        	updateBuilder.updateColumnValue("illuminanceMax", maxValue);
        	updateBuilder.updateColumnValue("illuminanceMin", minValue);
        	updateBuilder.updateColumnValue("illuminanceAvg", avgValue);
        	updateBuilder.update();
        } catch (SQLException e) {
        	mSysLogger.addLog("Update illuminance context fail: " + e.toString());
			Log.d(Constants.TAG, "Update Illuminance context failed: " + e.toString());
			e.printStackTrace();
		} 
    }
    */

	@Override
	public void onPowerStatusProbeFinished(boolean isCharging, float powerLevel) {
		Log.d(Constants.TAG, "Power is charging = " + isCharging + ", power level = " + powerLevel);
	    
		mNewSensing.setIsCharging(isCharging);
		mNewSensing.setPowerlevel(powerLevel);
		
	    /*
	    try {
        	UpdateBuilder<SensingData, Integer> updateBuilder = mDao.updateBuilder();
        	updateBuilder.where().eq("id", mSensingDataId);
        	updateBuilder.updateColumnValue("isCharging", isCharging);
        	updateBuilder.updateColumnValue("powerLevel", powerLevel);
        	updateBuilder.update();
        } catch (SQLException e) {
        	//mSysLogger.addLog("Update power status context fail: " + e.toString());
			Log.d(Constants.TAG, "Update power status context failed: " + e.toString());
			e.printStackTrace();
		} 
		*/
	}

	/*
	@Override
	public void onMovementProberFinished(int counts) {
		Log.d(Constants.TAG, "Movement counts = " +  counts);
		
		try {
        	UpdateBuilder<SensingData, Integer> updateBuilder = mDao.updateBuilder();
        	updateBuilder.where().eq("id", mSensingDataId);
        	updateBuilder.updateColumnValue("movement", counts);
        	updateBuilder.update();
        } catch (SQLException e) {
        	mSysLogger.addLog("Update movement context fail: " + e.toString());
			Log.d(Constants.TAG, "Update movement context failed: " + e.toString());
			e.printStackTrace();
		} 
	}
	*/
	
}
