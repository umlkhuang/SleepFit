package edu.uml.swin.sleepfit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import edu.uml.swin.sleepfit.util.Constants;

public class GraphViewTipDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View theView = inflater.inflate(R.layout.view_graphs_tip, null);
        final CheckBox tipCheckBox = (CheckBox) theView.findViewById(R.id.neverShowTipCheckbox);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(theView)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (tipCheckBox.isChecked()) {
                        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.TMP_PREF_FILE, Context.MODE_MULTI_PROCESS);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("noGraphTip", true);
                        editor.commit();
                    }

                    dismiss();
                }
            });

        return builder.create();
    }
}
