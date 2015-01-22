package edu.uml.swin.sleepfit.sensing;

import edu.uml.swin.sleepfit.util.Constants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(Constants.TAG, "Sensing duty timer finished. Broadcast sensing message and start itself again");
		
		// Send a message to notify the sensing service to start collect context 
		Intent msg = new Intent(Constants.COLLECT_CONTEXT_DUTY_MSG);
		context.sendBroadcast(msg); 
	}

}
