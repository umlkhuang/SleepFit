package edu.uml.swin.sleepfit.sensing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;

import edu.uml.swin.sleepfit.util.Constants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class WifiProber {

	private Context mContext;
	private Handler mHandler;
	private WifiProberCallbackIf mWifiProberCallback;
	private WifiManager mWifiMgr;
	private WifiLock mWifiLock;
    private WakeLock mWakeLock;
	private HashSet<String> mSetSSIDs;
	private HashSet<String> mSetMACs;
	private BroadcastReceiver mScanResultReceiver;
	private boolean mWifiReciverRegistered;
	private boolean mShouldDisableWifiAfterFinish;
	
	public WifiProber(Context context) {
		mContext = context;
		mWifiReciverRegistered = false;
		mWifiMgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		mWifiLock = null; 
		
		// Create a message handler to deal with timeout and Wifi scanning
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case Constants.WIFI_SCAN_TRIGGER_MSG:
					// Enable the WIFI if it is disabled and mark it
					if (mWifiMgr.isWifiEnabled() == false) {
						mWifiMgr.setWifiEnabled(true);
						mShouldDisableWifiAfterFinish = true;
					} else {
						mShouldDisableWifiAfterFinish = false;
					}
					
					mWifiLock = mWifiMgr.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "WifiLock");
					mWifiLock.setReferenceCounted(true);
					if ((mWifiLock != null) && (!mWifiLock.isHeld())) mWifiLock.acquire();
					
					/*
					mWakeLock = ((PowerManager) mContext.getSystemService(Context.POWER_SERVICE))
	                        .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeLock");
					mWakeLock.setReferenceCounted(true);
					if ((mWakeLock != null) && (!mWakeLock.isHeld())) mWakeLock.acquire();
					*/
					
					mWifiMgr.startScan();
					break;
				case Constants.WIFI_SCAN_TIMEOUT_MSG:
			        Log.d(Constants.TAG, "Wifi prober: finished WiFi probing");
			        
					if (mWifiReciverRegistered) {
						mContext.unregisterReceiver(mScanResultReceiver);
						mWifiReciverRegistered = false;
						
						// Disable WIFI if it's disabled before this call
						if (mShouldDisableWifiAfterFinish) {
							mWifiMgr.setWifiEnabled(false);
							mShouldDisableWifiAfterFinish = false;
						}
					}

					if (mWifiLock != null && mWifiLock.isHeld()) {
						mWifiLock.release(); 
						mWifiLock = null;
					}
					/*
					if (mWakeLock != null && mWakeLock.isHeld()) {
						mWakeLock.release();
						mWakeLock = null;
					}
					*/
					
					mWifiProberCallback.onWifiProbeFinished(mSetSSIDs);
					
					break;
				default:
					super.handleMessage(msg);
					break;
				}
			}
		};
	}
	
	public void getWifiContext(WifiProberCallbackIf callback) {
		Log.d(Constants.TAG, "WiFi prober: start to probe Wifi SSIDs");
		
		// Clear the set with each call
	    mSetSSIDs = new HashSet<String>();
	    mSetMACs = new HashSet<String>();
	    mWifiProberCallback = callback;
	    
	    mScanResultReceiver = new BroadcastReceiver() {
			@Override
            public void onReceive(Context context, Intent intent) {
				List<ScanResult> results = mWifiMgr.getScanResults();
				for (ScanResult result:results) {
					// Filter hidden SSID
			        if (result.SSID == "") continue;
			        if (mSetMACs.contains(result.BSSID)) continue;
			        else mSetMACs.add(result.BSSID); 
			        
			        // Hash the BSSID 
			        StringBuffer hexString = new StringBuffer();
					try {
						MessageDigest messageDigest = MessageDigest.getInstance("MD5");
						messageDigest.update(result.BSSID.getBytes());
						byte msgBytes[] = messageDigest.digest();
						for (int i = 0; i < msgBytes.length; i++)
							hexString.append(Integer.toHexString(0xFF & msgBytes[i]));
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
			        String combinedInfo = hexString.toString() + "#" + result.level; 
			        // String combinedInfo = result.SSID + "#" + result.BSSID + "#" + result.level;
			        // Replace the single quote with a space, otherwise it will be an error when write DB  
			        // combinedInfo = combinedInfo.replace("'", " "); 
			        mSetSSIDs.add(combinedInfo);
				}
				
				// Keep scanning WIFI until timeout
		        mWifiMgr.startScan();
            }
	    };
	    
	    // Register the WIFI scan result receiver
	    mContext.registerReceiver(mScanResultReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	    mWifiReciverRegistered = true;
	    
	    // Send message to trigger a scanning task and then send a timeout notification message
	    mHandler.sendEmptyMessage(Constants.WIFI_SCAN_TRIGGER_MSG);
	    mHandler.sendEmptyMessageDelayed(Constants.WIFI_SCAN_TIMEOUT_MSG, Constants.WIFI_SCAN_DURATION_SECOND * 1000); 
	}
	
}
