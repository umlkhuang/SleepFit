package edu.uml.swin.sleepfit.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector {
	private Context mContext;
	
	public ConnectionDetector(Context context) {
		this.mContext = context;
	}
	
	// Detects whether the phone's WiFi is connected to Internet 
	public boolean isConnectingToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) { 
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getType() == ConnectivityManager.TYPE_WIFI) {
						if (info[i].getState() == NetworkInfo.State.CONNECTED)
							return true;
					} 
				}
			}
		}
		return false;
	}
	
	public boolean isInternetAvailable() {
		ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
						return true;
				}
			}
		}
		return false;
	}
	
}
