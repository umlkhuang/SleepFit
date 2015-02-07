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
import com.j256.ormlite.stmt.UpdateBuilder;

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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private boolean mEnableSummaryCard;
    private boolean mEnableDailyCard;
    private boolean mEnableLifestyleCards;

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
    private boolean mHasGotDataFromServer;

    private IntentFilter mMessageFilter;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.UPDATED_SLEEP_INFO)) {
                getSleepInfo();
                onResume();
            }
        }
    };
	
	public static HomeFragment newInstance(int sectionNumber) {
		HomeFragment fragment = new HomeFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		args.putBoolean("ForHome", true);
		fragment.setArguments(args);
		return fragment;
	}
	
	public static HomeFragment newInstance(String trackDate, boolean forHome, boolean enableSummaryCard,
                                      boolean enableDailyCard, boolean enableLifestyleCards) {
		HomeFragment fragment = new HomeFragment();
		Bundle args = new Bundle();
		args.putString("trackDate", trackDate);
		args.putBoolean("ForHome", forHome);
        args.putBoolean("enableSummaryCard", enableSummaryCard);
        args.putBoolean("enableDailyCard", enableDailyCard);
        args.putBoolean("enableLifestyleCards", enableLifestyleCards);
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

        /*
		if (getArguments().getBoolean("ForHome")) {
			((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER)); 
		}
		*/

		String tmpTrackDate = getArguments().getString("trackDate", "");
		if (tmpTrackDate.equals("")) {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
			this.trackDate = dateFormatter.format(new Date(System.currentTimeMillis()));
		} else {
			this.trackDate = tmpTrackDate;
		}
        Log.d(Constants.TAG, "=== In HomeFragment onAttache, trackDate = " + this.trackDate);

        mEnableSummaryCard = getArguments().getBoolean("enableSummaryCard", true);
        mEnableDailyCard = getArguments().getBoolean("enableDailyCard", true);
        mEnableLifestyleCards = getArguments().getBoolean("enableLifestyleCards", true);
		
		mContext = activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(Constants.TAG, "In onCreate of HomeFragment");

        mMessageFilter = new IntentFilter();
        mMessageFilter.addAction(Constants.UPDATED_SLEEP_INFO);
        mContext.registerReceiver(mMessageReceiver, mMessageFilter);
		
		mDatabaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
		try {
			mLifestyleDao = mDatabaseHelper.getLifestyleRawDao();
			mDailyLogDao = mDatabaseHelper.getDailyLogDao();
			mSleepLogDao = mDatabaseHelper.getSleepLoggerDao();
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Cannot get DAO: " + e.toString());
			e.printStackTrace();
		}
		
		mNeedPausing = true;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(Constants.TAG, "In home fragment onCreateOptionsMenu");

		//inflater.inflate(R.menu.home, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //case R.id.home_action_setting:
            //    Log.d(Constants.TAG, "Home fragment clicked setting action icon.");

                //Intent intent = new Intent(getActivity(), SettingActivity.class);
                //startActivity(intent);
                /*
                try {
                    mSleepLogDao.executeRaw("delete from `sleeplogger` where id = 9;");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                */
                //return true;
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
        Log.d(Constants.TAG, "Summary Card = " + mEnableSummaryCard + ", Dailylog Card = " + mEnableDailyCard + ", Lifestyle Card = " + mEnableLifestyleCards);
		
		if (mNeedPausing) {
			if (getArguments().getBoolean("ForHome", false)) {
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
                        Bundle bundle = new Bundle();
                        bundle.putString("trackDate", trackDate);
                        intent.putExtras(bundle);
						startActivity(intent);
					}
				});
			}

			mNeedPausing = false;
		}

        // This should be in order! Since we will going to use sleep.createTime in dailylog.
        getSleepInfo();
        getDailyLogInfo();
        getAllCards();
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        mContext.unregisterReceiver(mMessageReceiver);
    }
	
	private void getAllCards() {
		mCards = new ArrayList<HomeCard>();

        if (mEnableSummaryCard) {
            mCards.add(new HomeCard(getActivity(), mSleepLog, mTodayDailyLog));
        } else {
            mCards.add(new HomeCard());
        }

        if (mEnableDailyCard) {
            mCards.add(new HomeCard(getActivity(), mTodayDailyLog, mSleepLog));
        } else {
            mCards.add(new HomeCard());
        }

        if (mEnableLifestyleCards) {
            mLifestyleLog = new ArrayList<LifestyleRaw>();
            mLifestyleLog.addAll(getLocalLifestyleLog());

            for (LifestyleRaw record : mLifestyleLog) {
                mCards.add(new HomeCard(getActivity(), record));
            }
        }
		
		Log.d(Constants.TAG, "Get all cards, cards number is " + mCards.size());
	}
	
	private void getSleepInfo() {
        List<SleepLogger> mSleepLogList = null;
        if (mSleepLogDao != null) {
            QueryBuilder<SleepLogger, Integer> qb = mSleepLogDao.queryBuilder();
            try {
                qb.where().eq("trackDate", trackDate);
                qb.orderBy("id", false);
                mSleepLogList = mSleepLogDao.query(qb.prepare());
            } catch (SQLException e) {
                e.printStackTrace();
                Log.e(Constants.TAG, e.toString());
            }
        }

        if (mSleepLogList != null && mSleepLogList.size() > 0) {
            mSleepLog = mSleepLogList.get(0);
        } else {
            // Need to upload data in case that user clicked notification on the morning
            new SyncWorker(mContext, System.currentTimeMillis()).execute();

            mPausingDialog = ProgressDialog.show(getActivity(), "", "Getting data from server ...", true);
            String getSleepUrl = Constants.GET_SLEEP_URL + "?accessCode=" + Constants.getAccessCode(mContext) + "&trackDate=" + trackDate;

            if (!mHasGotDataFromServer) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis());
                if (cal.get(Calendar.HOUR_OF_DAY) >= 6 && cal.get(Calendar.HOUR_OF_DAY) <= 10) {
                    new HttpRequestTask(mContext, this).execute(getSleepUrl);
                }
            } else {
                mPausingDialog.dismiss();
            }
        }
	}

    private void getDailyLogInfo() {
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
            mTodayDailyLog = new DailyLog(mSleepLog.getCreateTime().getTime());
            try {
                mDailyLogDao.create(mTodayDailyLog);
            } catch (SQLException e) {
                Log.d(Constants.TAG, "Add new daily log record failed: " + e.toString());
                e.printStackTrace();
            }
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
        Log.d(Constants.TAG, "In HomeFragment onRequestTaskCompleted, json = " + json);

        mHasGotDataFromServer = true;
        List<SleepLogger> sleeps = null;
        if (mSleepLogDao != null) {
            QueryBuilder<SleepLogger, Integer> qb = mSleepLogDao.queryBuilder();
            try {
                qb.where().eq("trackDate", trackDate);
                qb.orderBy("id", false);
                sleeps = mSleepLogDao.query(qb.prepare());
            } catch (SQLException e) {
                e.printStackTrace();
                Log.e(Constants.TAG, e.toString());
            }
        }

        if (json != null && !json.equals("")) {
            Log.d(Constants.TAG, "Sleep JSON = " + json);

            try {
                JSONObject jsonObj = new JSONObject(json);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
                String sleepTimeStr = jsonObj.getString("sleepTime");
                String wakeupTimeStr = jsonObj.getString("wakeupTime");
                Date sleepTime = dateFormat.parse(sleepTimeStr);
                Date wakeupTime = dateFormat.parse(wakeupTimeStr);

                SleepLogger latestSleep = sleeps.get(0);
                if (latestSleep.getTrackDate().equals(trackDate)) {
                    UpdateBuilder<SleepLogger,Integer> ub = mSleepLogDao.updateBuilder();
                    try {
                        ub.where().eq("trackDate", trackDate);
                        ub.updateColumnValue("sleepTime", sleepTime);
                        ub.updateColumnValue("wakeupTime", wakeupTime);
                        mSleepLogDao.update(ub.prepare());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    mSleepLog = latestSleep;
                    mSleepLog.setSleepTime(sleepTime);
                    mSleepLog.setWakeupTime(wakeupTime);
                } else {
                    // Note that we should set "finished" to be true since the user is awake now,
                    // and should set "uploaded" to false to make sure the sleep engine will not
                    // calculate new sleep log
                    mSleepLog = new SleepLogger(System.currentTimeMillis(), trackDate, sleepTime, wakeupTime, 0, 0, true, false);
                    try {
                        mSleepLogDao.create(mSleepLog);
                    } catch (SQLException e) {
                        Log.d(Constants.TAG, "Add new sleeplogger data record failed: " + e.toString());
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e1) {
                e1.printStackTrace();
            }

            Intent msg = new Intent(Constants.UPDATED_SLEEP_INFO);
            mContext.sendBroadcast(msg);
        } else {
            Log.d(Constants.TAG, "No sleep data available, need to create a new empty sleeplog.");

            mSleepLog = new SleepLogger(System.currentTimeMillis(), trackDate, null, null, 0, 0, false, false);
            try {
                mSleepLogDao.create(mSleepLog);
            } catch (SQLException e) {
                Log.d(Constants.TAG, "Add new sleeplogger data record failed: " + e.toString());
                e.printStackTrace();
            }
        }

        mPausingDialog.dismiss();
        onResume();
    }
	
}
