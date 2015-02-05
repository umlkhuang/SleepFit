package edu.uml.swin.sleepfit;

import edu.uml.swin.sleepfit.sensing.SensingService;
import edu.uml.swin.sleepfit.util.Constants;
import edu.uml.swin.sleepfit.util.SurveyUploader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class MainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;
	
	private String[] navMenuTitles;
	
	private HomeFragment mHomeFragment;
	private LastNightFragment mLastNightFragment;
	private LifestyleDetailViewFragment mLifestyleDetailViewFragment;
    private LifestyleListFragment mLifestyleListFragment;
	private GraphsFragment mGraphsFragment;
	private SleepHistoryFragment mSleepHistoryFragment;
	private PlaceholderFragment mPlaceholderFragment;
	
	class PopupSurveyDialogFragment extends DialogFragment {
		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("We need your help to finish a very simple user survey, do you want to do it now?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intentUserSurvey = new Intent(getActivity(), UserSurvey.class);
							intentUserSurvey.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				    		startActivity(intentUserSurvey);
				    		dismiss();
						}
					})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
		
		startService(new Intent(this, SensingService.class));
		
		SharedPreferences preferences = getSharedPreferences(Constants.SURVEY_FILE_NAME, Context.MODE_PRIVATE);
		boolean hasDoneSurvey = preferences.getBoolean("doneSurvey", false);
		if (!hasDoneSurvey) { 
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			DialogFragment dialog = new PopupSurveyDialogFragment();
			dialog.show(ft, "UserSurvey"); 
		} else {
			boolean hasUploaded = preferences.getBoolean("uploaded", false);
			if (!hasUploaded) {
				SurveyUploader uploader = new SurveyUploader(this); 
				uploader.execute(); 
			}
		}
		
		// Once user open the App, the local data will be uploaded to backend server
		//new SyncWorker(getBaseContext(), System.currentTimeMillis()).execute();
	}

	@Override
	public void onNavigationDrawerItemSelected(int position, boolean fromSavedInstanceState) {
		// If rotate the device, the Activity will be re-created and the Fragments too.
		// Do NOT need to re-create fragment if it is caused by device rotation 
		if (fromSavedInstanceState) return;
		
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager(); 
		switch (position) {
		case 0:
            /*
			if (mHomeFragment == null) mHomeFragment = HomeFragment.newInstance(position + 1);
			fragmentManager.beginTransaction()
					.replace(R.id.container, mHomeFragment).commit();
			break;
			*/
            if (mSleepHistoryFragment == null) mSleepHistoryFragment = SleepHistoryFragment.newInstance(position + 1);
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mSleepHistoryFragment).commit();
            break;
		case 1:
			if (mLastNightFragment == null) mLastNightFragment = LastNightFragment.newInstance(position + 1);
			fragmentManager.beginTransaction()
					.replace(R.id.container, mLastNightFragment).commit();
			break;
		case 2:
            /*
			if (mLifestyleDetailViewFragment == null) mLifestyleDetailViewFragment = LifestyleDetailViewFragment.newInstance(position + 1);
			fragmentManager.beginTransaction()
					.replace(R.id.container, mLifestyleDetailViewFragment).commit();
			break;
			*/

            if (mLifestyleListFragment == null) mLifestyleListFragment = LifestyleListFragment.newInstance(position + 1);
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mLifestyleListFragment).commit();
            break;
		case 3:
			if (mGraphsFragment == null) mGraphsFragment = GraphsFragment.newInstance(position + 1);
			fragmentManager.beginTransaction()
					.replace(R.id.container, mGraphsFragment).commit();
			break;
        /*
		case 4:
			if (mSleepHistoryFragment == null) mSleepHistoryFragment = SleepHistoryFragment.newInstance(position + 1);
			fragmentManager.beginTransaction()
					.replace(R.id.container, mSleepHistoryFragment).commit();
			break;
		*/
		default:
			if (mPlaceholderFragment == null) mPlaceholderFragment = PlaceholderFragment.newInstance(position + 1);
			fragmentManager.beginTransaction()
					.replace(R.id.container, mPlaceholderFragment).commit();
			break;
		}
	}

	public void onSectionAttached(int number) {
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items); 
		mTitle = navMenuTitles[number - 1];
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		//actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		/*
		if (id == R.id.action_settings) {
			return true;
		}
		*/
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
	}

}
