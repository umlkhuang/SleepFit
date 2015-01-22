package edu.uml.swin.sleepfit;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import edu.uml.swin.sleepfit.DB.DailyLog;
import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.DB.LifestyleRaw;
import edu.uml.swin.sleepfit.DB.SleepLogger;
import edu.uml.swin.sleepfit.cardview.HomeCard;
import edu.uml.swin.sleepfit.cardview.HomeListAdapter;
import edu.uml.swin.sleepfit.fab.FloatingActionButton;
import edu.uml.swin.sleepfit.util.Constants;
import edu.uml.swin.sleepfit.util.HttpRequestTask;
import edu.uml.swin.sleepfit.util.HttpRequestTask.HttpRequestCallback;
import edu.uml.swin.sleepfit.util.SyncWorker;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

public class HomeFragment extends Fragment implements HttpRequestCallback {
	
	private static final String ARG_SECTION_NUMBER = "section_number";
	private FloatingActionButton mFabButton;
	
	private RecyclerView mRecyclerView;
	private HomeListAdapter mAdapter;
	private ArrayList<HomeCard> mCards;
	private Context mContext;
	private String trackDate;

	private DatabaseHelper mDatabaseHelper;
	private Dao<LifestyleRaw, Integer> mLifestyleDao;
	private Dao<DailyLog, Integer> mDailyLogDao;
	private Dao<SleepLogger, Integer> mSleepLogDao;
	
	private ProgressDialog mPausingDialog;
	private boolean mNeedPausing;
	private ArrayList<LifestyleRaw> mLifestyleLog;
	private List<DailyLog> mDailyLogList;
	private DailyLog mTodayDailyLog;
	private SleepLogger mSleepLog;
	
	public static HomeFragment newInstance(int sectionNumber) {
		HomeFragment fragment = new HomeFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		args.putBoolean("ForHome", true);
		fragment.setArguments(args);
		return fragment;
	}
	
	public static HomeFragment newInstance(String trackDate) {
		HomeFragment fragment = new HomeFragment();
		Bundle args = new Bundle();
		args.putString("trackDate", trackDate);
		args.putBoolean("ForHome", false);
		fragment.setArguments(args);
		return fragment;
	}

	public HomeFragment() {
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        
		mRecyclerView = (RecyclerView) rootView.findViewById(R.id.lifestyle_card_list);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		
        return rootView;
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of
        // actions in the action bar.
        setHasOptionsMenu(true);
    }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if (getArguments().getBoolean("ForHome")) {
			((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER)); 
		} 
		String tmpTrackDate = getArguments().getString("trackDate", "");
		if (tmpTrackDate.equals("")) {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
			this.trackDate = dateFormatter.format(new Date(System.currentTimeMillis()));
		} else {
			this.trackDate = tmpTrackDate;
		}
		
		mContext = (Context) activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(Constants.TAG, "In onCreate of HomeFragment");
		
		mDatabaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
		try {
			mLifestyleDao = mDatabaseHelper.getLifestyleRawDao();
			mDailyLogDao = mDatabaseHelper.getDailyLogDao();
			mSleepLogDao = mDatabaseHelper.getSleepLoggerDao();
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Cannot get DAO: " + e.toString());
			e.printStackTrace();
		}
		
		//SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
		//String trackDate = dateFormatter.format(new Date(System.currentTimeMillis()));
		try {
			QueryBuilder<DailyLog, Integer> queryBuilder = mDailyLogDao.queryBuilder();
			queryBuilder.where().eq("trackDate", trackDate); 
			PreparedQuery<DailyLog> preparedQuery = queryBuilder.prepare(); 
			mDailyLogList = mDailyLogDao.query(preparedQuery); 
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Get today daily log data failed: " + e.toString());
			e.printStackTrace();
		}
		
		if (mDailyLogList != null && mDailyLogList.size() > 0) {
			mTodayDailyLog = mDailyLogList.get(mDailyLogList.size() - 1);
		} else {
			mTodayDailyLog = new DailyLog(System.currentTimeMillis()); 
			try {
				mDailyLogDao.create(mTodayDailyLog);
			} catch (SQLException e) {
				Log.d(Constants.TAG, "Add new daily log record failed: " + e.toString());
				e.printStackTrace();
			} 
		} 
		
		mNeedPausing = true;
		
		// Get last night's sleep information 
		getSleepInfo();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(Constants.TAG, "In home fragment onCreateOptionsMenu");

		inflater.inflate(R.menu.home, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home_action_setting:
                Log.d(Constants.TAG, "Home fragment clicked setting action icon.");

                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
		Log.d(Constants.TAG, "In onSaveinstanceState, HomeFragment");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(Constants.TAG, "In onResume of HomeFragment");
		
		if (mNeedPausing) {
			if (getArguments().getBoolean("ForHome")) {
				mFabButton = new FloatingActionButton.Builder(getActivity())
					        .withDrawable(getResources().getDrawable(R.drawable.ic_lifestyle))
					        .withButtonColor(Color.parseColor("#163004"))
					        .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
					        .withMargins(0, 0, 16, 16)
					        .create();
				
				mFabButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getActivity(), NewLifestyleActivity.class);
						startActivity(intent);
					}
				});
			}
			
			getAllCards();
			mNeedPausing = false;
		}
		
		mAdapter = new HomeListAdapter(mCards, getActivity());
		if (mAdapter != null)
			mRecyclerView.setAdapter(mAdapter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.d(Constants.TAG, "In onPause, HomeFragment");
		
		if (getArguments().getBoolean("ForHome")) {
			ViewGroup root = (ViewGroup) getActivity().findViewById(android.R.id.content);
			mFabButton.hideFloatingActionButton();
			root.removeView(mFabButton);
		}
		
		mNeedPausing = true;
	}
	
	private void getAllCards() {
		mCards = new ArrayList<HomeCard>();
		mCards.add(new HomeCard(getActivity(), mSleepLog, mTodayDailyLog));
		mCards.add(new HomeCard(getActivity(), mTodayDailyLog)); 
		
		mLifestyleLog = new ArrayList<LifestyleRaw>();
		mLifestyleLog.addAll(getLocalLifestyleLog()); 
		
		for (LifestyleRaw record : mLifestyleLog) {
			mCards.add(new HomeCard(getActivity(), record));
		}
		
		Log.d(Constants.TAG, "Get all cards, cards number is " + mCards.size());
	}
	
	private void getSleepInfo() {
		//SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
		//String trackDate = dateFormatter.format(new Date(System.currentTimeMillis()));
		
		List<SleepLogger> sleeps = null;
		QueryBuilder<SleepLogger, Integer> qb = mSleepLogDao.queryBuilder();
		try {
			qb.where().eq("trackDate", trackDate);
			sleeps = mSleepLogDao.query(qb.prepare());
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(Constants.TAG, e.toString());
		}
		if (sleeps == null || sleeps.size() <= 0) {
			mSleepLog = null;
			new SyncWorker(mContext, System.currentTimeMillis()).execute();
		} else {
			mSleepLog = sleeps.get(0);
			if (mSleepLog.getSleepTime() == null || mSleepLog.getWakeupTime() == null) {
				// Need to delete the uncompleted sleep log 
				DeleteBuilder<SleepLogger, Integer> db = mSleepLogDao.deleteBuilder();
				try {
					db.where().eq("id", mSleepLog.getId());
					mSleepLogDao.delete(db.prepare());
				} catch (SQLException e) {
					Log.e(Constants.TAG, "Delete uncompleted sleep log error: " + e.toString());
				}
				
				mSleepLog = null;
			}
		}
		
		// Only when there is no local sleep log, then pull data from server 
		if (mSleepLog == null) {
            //SharedPreferences preferences = mContext.getSharedPreferences(Constants.UPDATE_TIME_FILE, Context.MODE_PRIVATE);
            //long lastQuerySleepTS = preferences.getLong("lastQuerySleepTS", 0);
            //if ((System.currentTimeMillis() - lastQuerySleepTS) >= Constants.UPLOAD_DATA_INTERVAL_SMALL) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            if (cal.get(Calendar.HOUR_OF_DAY) >= 6) {
                mPausingDialog = ProgressDialog.show(getActivity(), "", "Getting data from server ...", true);
                String getSleepUrl = Constants.GET_SLEEP_URL + "?accessCode=" + Constants.getAccessCode(mContext) + "&trackDate=" + trackDate;
                //Log.d(Constants.TAG, getSleepUrl);
                new HttpRequestTask(mContext, this).execute(getSleepUrl);
            }

            //SharedPreferences.Editor editor = preferences.edit();
            //editor.putLong("lastQuerySleepTS", System.currentTimeMillis());
            //editor.commit();
            //}
		}
	}
	
	private List<LifestyleRaw> getLocalLifestyleLog() {
		List<LifestyleRaw> ret = new ArrayList<LifestyleRaw>();
		
		//String trackDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date());
		QueryBuilder<LifestyleRaw, Integer> qb = mLifestyleDao.queryBuilder();
		try {
			qb.where().eq("trackDate", trackDate);
			qb.orderBy("logTime", false);
			ret = mLifestyleDao.query(qb.prepare());
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(Constants.TAG, e.toString());
		}
		
		return ret;
	}

	@Override
	public void onRequestTaskCompleted(String json) {
		// This function will only be called once when there is no local sleep log 
		if (json == null) {
			mPausingDialog.dismiss();
			CharSequence text = "failed to get data from server! Either the phone is not connected to internet or the server is down.";
			Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
		} else if (!json.equals("")) {
			Log.d(Constants.TAG, "Sleep JSON = " + json);
			//SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
			//String trackDate = formatter.format(new Date(System.currentTimeMillis()));
			
			try {
				JSONObject jsonObj = new JSONObject(json);
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
				String sleepTimeStr = jsonObj.getString("sleepTime");
				String wakeupTimeStr = jsonObj.getString("wakeupTime");
				Date sleepTime = dateFormat.parse(sleepTimeStr);
				Date wakeupTime = dateFormat.parse(wakeupTimeStr);

				// Note that we should set "finished" to be true since the user is awake now, and should set "uploaded" to false
				// to make sure the sleep engine will not calculate new sleep log
				mSleepLog = new SleepLogger(System.currentTimeMillis(), trackDate, sleepTime, wakeupTime, 0, true, false);
				try {
					mSleepLogDao.create(mSleepLog);
				} catch (SQLException e) {
					Log.d(Constants.TAG, "Add new sleeplogger data record failed: " + e.toString());
					e.printStackTrace();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			mPausingDialog.dismiss();

            Intent msg = new Intent(Constants.UPDATED_SLEEP_INFO);
            mContext.sendBroadcast(msg);
		} else {
			mPausingDialog.dismiss();
		}
	}
	
}
