package edu.uml.swin.sleepfit.sensing;

public interface PowerStatusProberCallbackIf {
	public void onPowerStatusProbeFinished(boolean isCharging, float powerLevel);
}
