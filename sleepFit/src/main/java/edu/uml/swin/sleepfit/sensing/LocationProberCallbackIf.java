package edu.uml.swin.sleepfit.sensing;

import android.location.Location;

public interface LocationProberCallbackIf {
	public void onLocationProbeFinished(Location locInfo);
	public void onNoLocationAvailable();
}
