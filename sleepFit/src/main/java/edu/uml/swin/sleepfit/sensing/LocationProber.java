package edu.uml.swin.sleepfit.sensing;

import edu.uml.swin.sleepfit.util.Constants;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class LocationProber {

	private Context mContext;
	private Handler mHandler;
	private LocationProberCallbackIf mLocationProberCallback;
	private LocationManager mLocationMgr;
	private LocationListener mLocationListener;
	private Location mCurrentLocation;
	private boolean mIsGPSEnabled;
	private boolean mIsNetworkEnabled;
	
	public LocationProber(Context context) {
		mContext = context;
		mCurrentLocation = null;
		mLocationMgr = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case Constants.LOCATION_GPS_MESSAGE_TIMEOUT_MSG:
					// GPS is enabled but no signal, try network
					if (mIsNetworkEnabled) {
				        Log.d(Constants.TAG, "Locatioin prober: finished locatioin probing by using GPS");
				        
						this.sendEmptyMessageDelayed(Constants.LOCATION_NETWORK_MESSAGE_TIMEOUT_MSG, 
								Constants.LOCATION_NETWORK_SCAN_DURATION_SECOND * 1000);
						mLocationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
								Constants.LOCATIOIN_UPDATE_INTERVAL_SECOND, 
								Constants.LOCATIOIN_UPDATE_MIN_DISTANCE, 
								mLocationListener);
					} else {
						//mLocationMgr.removeUpdates(mLocationListener);
						if (mCurrentLocation == null) {
							mLocationProberCallback.onNoLocationAvailable();
						} else {
							mLocationProberCallback.onLocationProbeFinished(mCurrentLocation);
						}
					}
					break;
				case Constants.LOCATION_NETWORK_MESSAGE_TIMEOUT_MSG:
			        Log.d(Constants.TAG, "Locatioin prober: finished locatioin probing by using network location");
			        
					mLocationMgr.removeUpdates(mLocationListener);
					if (mCurrentLocation == null) mLocationProberCallback.onNoLocationAvailable();
					else mLocationProberCallback.onLocationProbeFinished(mCurrentLocation);
					break;
				default:
					super.handleMessage(msg);
					break;
				}
			}
		};
		
		mLocationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				//Log.d(Constants.TAG, "onLocationListener, location changed, location = " + location.toString());
				
				mCurrentLocation = location;
				mHandler.removeMessages(Constants.LOCATION_GPS_MESSAGE_TIMEOUT_MSG);
				mHandler.removeMessages(Constants.LOCATION_NETWORK_MESSAGE_TIMEOUT_MSG);
				mLocationMgr.removeUpdates(this);
				if (mCurrentLocation != null) {
					mLocationProberCallback.onLocationProbeFinished(mCurrentLocation);
				} else {
					Log.d(Constants.TAG, "Location prober: location listener got NULL location context");
					mLocationProberCallback.onNoLocationAvailable();
				}
			}
		      
			@Override
			public void onProviderDisabled(String provider) {
				Log.d(Constants.TAG, "Location provider " + provider + "has been disabled");
			}
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) { }
			
			@Override
			public void onProviderEnabled(String provider) {
				Log.d(Constants.TAG, "Location provider " + provider + "has been enabled");
			}
			
		};
	}
	
	public void getLocationInfo(LocationProberCallbackIf callback) {
		Log.d(Constants.TAG, "Location prober: start to probe location context");
		
		mLocationProberCallback = callback;
		mIsGPSEnabled = mLocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
		mIsNetworkEnabled = mLocationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		
		if (mIsGPSEnabled) {
			Log.d(Constants.TAG, "GPS is enabled, use GPS to get location context");
			
			mHandler.sendEmptyMessageDelayed(Constants.LOCATION_GPS_MESSAGE_TIMEOUT_MSG, 
					Constants.LOCATIOIN_GPS_SCAN_DURATION_SECOND * 1000);
			mLocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
					Constants.LOCATIOIN_UPDATE_INTERVAL_SECOND,
					Constants.LOCATIOIN_UPDATE_MIN_DISTANCE,
					mLocationListener);
		} else if (mIsNetworkEnabled) {
			Log.d(Constants.TAG, "GPS is disabled, use Network to get location context");
			
			mHandler.sendEmptyMessageDelayed(Constants.LOCATION_NETWORK_MESSAGE_TIMEOUT_MSG,
					Constants.LOCATION_NETWORK_SCAN_DURATION_SECOND * 1000);
			mLocationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
					Constants.LOCATIOIN_UPDATE_INTERVAL_SECOND,
					Constants.LOCATIOIN_UPDATE_MIN_DISTANCE,
					mLocationListener);
		} else {
			// Directly return null location information if all location services are not available
			Log.d(Constants.TAG, "No location service is available");
			mCurrentLocation = null;
			mLocationProberCallback.onLocationProbeFinished(mCurrentLocation);
		}
	}
	
}
