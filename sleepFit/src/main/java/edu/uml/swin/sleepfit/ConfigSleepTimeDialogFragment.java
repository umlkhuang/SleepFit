package edu.uml.swin.sleepfit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import edu.uml.swin.sleepfit.util.Constants;

public class ConfigSleepTimeDialogFragment extends DialogFragment {

    public interface ConfigSleepTimeListener {
        public void onConfigSleepTimeFinished(float hours);
    }

    private Context mContext;

    private ConfigSleepTimeListener listener;

    public ConfigSleepTimeDialogFragment(Fragment fragment) {
        try {
            listener = (ConfigSleepTimeListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement ConfigSleepTimeListener");
        }

        mContext = fragment.getActivity();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View theView = inflater.inflate(R.layout.config_sleep_time, null);
        final EditText hourText = (EditText) theView.findViewById(R.id.editSleepTime);

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.SURVEY_FILE_NAME, Context.MODE_MULTI_PROCESS);
        String sleepHours = preferences.getString("sleepHours", "8");
        hourText.setText(sleepHours);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(theView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputStr = hourText.getText().toString().trim();
                        if (inputStr.equals("")) {
                            Toast.makeText(mContext, "Sleep duration cannot be empty!", Toast.LENGTH_SHORT).show();
                        } else {
                            float hours = Float.valueOf(inputStr);
                            listener.onConfigSleepTimeFinished(hours);

                            dismiss();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }
}
