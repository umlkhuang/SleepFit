package edu.uml.swin.sleepfit.sensing;

import edu.uml.swin.sleepfit.util.Constants;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public class PowerStatusProber {

	private boolean mIsCharging;
	private float mPowerLevel;
	private Context mContext;
	
	public PowerStatusProber(Context context) {
		mContext = context;
		mIsCharging = false;
		mPowerLevel = 1.0f;
	}
	
	public void getPowerStatus(PowerStatusProberCallbackIf callback) {
		Log.d(Constants.TAG, "Power status prober: start to probe power status");
		
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = mContext.registerReceiver(null, ifilter);
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		mIsCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
					  status == BatteryManager.BATTERY_STATUS_FULL;
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		mPowerLevel = level / (float)scale;
		
		callback.onPowerStatusProbeFinished(mIsCharging, mPowerLevel);
	}
	
}
