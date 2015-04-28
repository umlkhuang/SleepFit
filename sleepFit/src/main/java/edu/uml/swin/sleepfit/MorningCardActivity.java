package edu.uml.swin.sleepfit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.uml.swin.sleepfit.DB.UserEvents;
import edu.uml.swin.sleepfit.util.Constants;


public class MorningCardActivity extends ActionBarActivity {

    private HomeFragment mHomeFragment;
    private String mTrackDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morning_card);

        Intent intent = getIntent();
        mTrackDate = intent.getStringExtra("trackDate");
        if (mTrackDate == null || mTrackDate.equals("")) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            mTrackDate = dateFormatter.format(new Date(System.currentTimeMillis()));
        }

        if (savedInstanceState == null) {
            Log.d(Constants.TAG, "In onCreate of MorningCardActivity");

            if (mHomeFragment == null)
                mHomeFragment = HomeFragment.newInstance(mTrackDate, false, true, false, false);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mHomeFragment).commit();
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(mTrackDate);
    }


    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences preferences = getSharedPreferences(Constants.TMP_PREF_FILE, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("morningTrackDate", mTrackDate);
        editor.commit();

        UserEvents event = new UserEvents(System.currentTimeMillis(), "name|status|trackDate", "ClickMorningNotification|Enter|"+mTrackDate);
        Constants.addNewUserEvent(this, event);
    }

    @Override
    public void onPause() {
        super.onPause();

        UserEvents event = new UserEvents(System.currentTimeMillis(), "name|status|trackDate", "ClickMorningNotification|Exit|"+mTrackDate);
        Constants.addNewUserEvent(this, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_morning_card, menu);
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
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
