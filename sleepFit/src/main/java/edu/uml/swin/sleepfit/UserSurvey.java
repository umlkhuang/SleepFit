package edu.uml.swin.sleepfit;

import edu.uml.swin.sleepfit.util.Constants;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class UserSurvey extends ActionBarActivity {
	
	private EditText mAgeEditText;
	private RadioGroup mGenderRadioGroup;
	private CheckBox mRacialA;
	private CheckBox mRacialB;
	private CheckBox mRacialC;
	private CheckBox mRacialD;
	private CheckBox mRacialE;
	private EditText mSleepHoursEditText;
	private Button mSubmitButton;
	
	private String mAge;
	private String mGender;
	private String mRacial;
	private String mSleepHours;

	class NotFinishedDialogFragment extends DialogFragment {
		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("You have not finished the survey yet! Please double check and try it again.")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dismiss();
						}
					});
			return builder.create();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_survey);
		
		mAge = "";
		mGender = "";
		mRacial = "";
		mSleepHours = "";
		mAgeEditText = (EditText) findViewById(R.id.ageEditText);
		mGenderRadioGroup = (RadioGroup) findViewById(R.id.genderRadioGroup);
		mRacialA = (CheckBox) findViewById(R.id.racialCheckBoxA);
		mRacialB = (CheckBox) findViewById(R.id.racialCheckBoxB);
		mRacialC = (CheckBox) findViewById(R.id.racialCheckBoxC);
		mRacialD = (CheckBox) findViewById(R.id.racialCheckBoxD);
		mRacialE = (CheckBox) findViewById(R.id.racialCheckBoxE);
		mSleepHoursEditText = (EditText) findViewById(R.id.sleepHourEditText);
		mSubmitButton = (Button) findViewById(R.id.surveyButton);
		
		mGenderRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton radioButton = (RadioButton) findViewById(checkedId);
				if (radioButton.isChecked())
					mGender = radioButton.getText().toString();
			}
		});
		
		mSubmitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAge = mAgeEditText.getText().toString();
				StringBuilder strBuilder = new StringBuilder();
				if (mRacialA.isChecked()) strBuilder.append("A");
				if (mRacialB.isChecked()) strBuilder.append("B");
				if (mRacialC.isChecked()) strBuilder.append("C");
				if (mRacialD.isChecked()) strBuilder.append("D");
				if (mRacialE.isChecked()) strBuilder.append("E");
				mRacial = strBuilder.toString();
				mSleepHours = mSleepHoursEditText.getText().toString();
				
				if (mAge.equals("") || mGender.equals("") || mRacial.equals("") || mSleepHours.equals("")) {
					FragmentTransaction ft = getFragmentManager().beginTransaction();
					DialogFragment notFinishDialog = new NotFinishedDialogFragment();
					notFinishDialog.show(ft, "ErrorDialog");
					return;
				}
				
				saveUserSurvey();
				
				Intent intent = new Intent(UserSurvey.this, MainActivity.class);
			    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Reuse the existing instance
			    startActivity(intent);
			}
		});
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	public void saveUserSurvey() {
		SharedPreferences preferences = getSharedPreferences(Constants.SURVEY_FILE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("UUID", Constants.getUUID(this));
		editor.putString("CID", Constants.getUUID(this));
		editor.putString("age", mAge);
		editor.putString("gender", mGender);
		editor.putString("racial", mRacial);
		editor.putString("sleepHours", mSleepHours);
		editor.putBoolean("doneSurvey", true);
		editor.putBoolean("uploaded", false);
		editor.commit();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.user_survey, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
	    	Intent intent = new Intent(this, MainActivity.class);
	    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Reuse the existing instance
	        startActivity(intent);
	        return true;
	    default:
	    	return super.onOptionsItemSelected(item);
	}
	}
}
