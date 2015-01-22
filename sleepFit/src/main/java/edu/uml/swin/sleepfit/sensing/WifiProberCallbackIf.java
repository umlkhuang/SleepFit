package edu.uml.swin.sleepfit.sensing;

import java.util.HashSet;

public interface WifiProberCallbackIf {
	public void onWifiProbeFinished(HashSet<String> setSSIDs);
}
