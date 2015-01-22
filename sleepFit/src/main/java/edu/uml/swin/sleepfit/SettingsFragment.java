package edu.uml.swin.sleepfit;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

import edu.uml.swin.sleepfit.util.Constants;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Constants.TAG, "In Settings Fragment onCreate.");

        addPreferencesFromResource(R.xml.preferences);
    }
}
