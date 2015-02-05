package edu.uml.swin.sleepfit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.uml.swin.sleepfit.util.Constants;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class ViewSleepHistoryActivity extends ActionBarActivity {
	
	private HomeFragment mHomeFragment;
	private String mTrackDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_sleep_history);
		
		Intent intent = getIntent();
		mTrackDate = intent.getStringExtra("trackDate");
		if (mTrackDate == null || mTrackDate.equals("")) {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
			mTrackDate = dateFormatter.format(new Date(System.currentTimeMillis()));
		}
		
		if (savedInstanceState == null) {
			Log.d(Constants.TAG, "In onCreate of ViewSleepHistoryActivity");
			
			if (mHomeFragment == null) mHomeFragment = HomeFragment.newInstance(mTrackDate, true, true, true, true);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, mHomeFragment).commit();
		} 
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(mTrackDate);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.view_sleep_history, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
		}
	}

}
