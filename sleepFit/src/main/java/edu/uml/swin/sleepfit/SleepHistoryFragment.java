package edu.uml.swin.sleepfit;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.DB.SleepLogger;
import edu.uml.swin.sleepfit.cardview.HistoryCard;
import edu.uml.swin.sleepfit.cardview.HistoryListAdapter;
import edu.uml.swin.sleepfit.cardview.HistoryListAdapter.OnItemClickListener;
import edu.uml.swin.sleepfit.util.Constants;
import edu.uml.swin.sleepfit.util.HttpRequestTask;
import edu.uml.swin.sleepfit.util.SyncWorker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class SleepHistoryFragment extends Fragment implements HttpRequestTask.HttpRequestCallback {

	private static final String ARG_SECTION_NUMBER = "section_number";
	
	private TextView mEmptyText;
	private RecyclerView mRecyclerView;
	private HistoryListAdapter mAdapter;
	private ArrayList<HistoryCard> mCards;
	
	private Context mContext;
	private DatabaseHelper mDatabaseHelper;
	private Dao<SleepLogger, Integer> mSleepLogDao;
	private List<SleepLogger> mSleepLogList = null;

    private ProgressDialog mPausingDialog;
    private boolean mHasGotDataFromServer;
	
	public static SleepHistoryFragment newInstance(int sectionNumber) {
		SleepHistoryFragment fragment = new SleepHistoryFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}
	
	public SleepHistoryFragment() {
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_sleephistory, container, false);

		mEmptyText = (TextView) rootView.findViewById(R.id.empty_history_text);
		
		mRecyclerView = (RecyclerView) rootView.findViewById(R.id.sleephistory_card_list);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		
		return rootView;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER)); 
		
		mContext = activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(Constants.TAG, "In onCreate of SleepHistoryFragment");

        mHasGotDataFromServer = false;
		mDatabaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
		try {
			mSleepLogDao = mDatabaseHelper.getSleepLoggerDao();
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Cannot get the SleepLogger DAO: " + e.toString());
			e.printStackTrace();
		}

	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.home, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of
        // actions in the action bar.
        setHasOptionsMenu(true);
    }
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
		Log.d(Constants.TAG, "In onSaveinstanceState, SleepHistoryFragment");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(Constants.TAG, "In onResume, SleepHistoryFragment");

        getAllSleepHistory();
		
		if (mCards.size() == 0) {
			mEmptyText.setVisibility(View.VISIBLE);
		} else {
			mEmptyText.setVisibility(View.GONE);
		}
		mAdapter = new HistoryListAdapter(mCards, getActivity()); 
		mAdapter.SetOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				Log.d(Constants.TAG, "=== In onItemClick of history list adapter.");

                String trackDate;
                if (mCards.get(position).mWakeTime != null) {
                    trackDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(mCards.get(position).mWakeTime);
                } else {
                    trackDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date());
                }
				Bundle bundle = new Bundle();
				bundle.putString("trackDate", trackDate);
				Intent intent = new Intent(mContext, ViewSleepHistoryActivity.class);
				intent.putExtras(bundle);
				mContext.startActivity(intent);
			}
		});
		mRecyclerView.setAdapter(mAdapter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.d(Constants.TAG, "In onPause, SleepHistoryFragment");
	}


    private void getAllSleepHistory() {
        mSleepLogList = null;
        if (mSleepLogDao != null) {
            QueryBuilder<SleepLogger, Integer> qb = mSleepLogDao.queryBuilder();
            try {
                qb.orderBy("id", false);
                qb.orderBy("wakeupTime", false);
                mSleepLogList = mSleepLogDao.query(qb.prepare());
            } catch (SQLException e) {
                e.printStackTrace();
                Log.e(Constants.TAG, e.toString());
            }
        }

        float wakeSleepRatio;
        SharedPreferences preferences = mContext.getSharedPreferences(Constants.SURVEY_FILE_NAME, Context.MODE_PRIVATE);
        float sleepHours = Float.valueOf(preferences.getString("sleepHours", "8"));
        wakeSleepRatio = (float) ((24.0 - sleepHours) / sleepHours);

        mCards = new ArrayList<HistoryCard>();

        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        String trackDate = dateFormatter.format(new Date(System.currentTimeMillis()));
        if (mSleepLogList != null && mSleepLogList.size() > 0) {
            // Check today's sleep log, if there is not then upload data and query the sleep time
            // First upload the data, then query sleep time if there is no today's data
            SleepLogger latestSleep = mSleepLogList.get(0);
            if (!latestSleep.getTrackDate().equals(trackDate) || latestSleep.getSleepTime() == null ||
                    latestSleep.getWakeupTime() == null || !latestSleep.getFinished()) {
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
                    for (SleepLogger log : mSleepLogList) {
                        mCards.add(new HistoryCard(log, wakeSleepRatio));
                    }
                    mPausingDialog.dismiss();
                }

            } else {
                for (SleepLogger log : mSleepLogList) {
                    mCards.add(new HistoryCard(log, wakeSleepRatio));
                }
            }
        } else {
            SleepLogger sleepLog = new SleepLogger(System.currentTimeMillis(), trackDate, null, null, 0, 0, false, false);
            mCards.add(0, new HistoryCard(sleepLog, wakeSleepRatio));
            // Save this fake sleep into the database
            try {
                mSleepLogDao.create(sleepLog);
            } catch (SQLException e) {
                Log.e(Constants.TAG, e.toString());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestTaskCompleted(String json) {
        Log.d(Constants.TAG, "In SleepHistoryFragment onRequestTaskCompleted, json = " + json);

        mHasGotDataFromServer = true;
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        String trackDate = formatter.format(new Date(System.currentTimeMillis()));
        SleepLogger sleepLog = null;

        List<SleepLogger> sleeps = null;
        if (mSleepLogDao != null) {
            QueryBuilder<SleepLogger, Integer> qb = mSleepLogDao.queryBuilder();
            try {
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
                } else {
                    // Note that we should set "finished" to be true since the user is awake now,
                    // and should set "uploaded" to false to make sure the sleep engine will not
                    // calculate new sleep log
                    sleepLog = new SleepLogger(System.currentTimeMillis(), trackDate, sleepTime, wakeupTime, 0, 0, true, false);
                    try {
                        mSleepLogDao.create(sleepLog);
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
        } else if (!sleeps.get(0).getTrackDate().equals(trackDate)) {
            Log.d(Constants.TAG, "Latest TrackDate = " + sleeps.get(0).getTrackDate() + ", need to create new empty sleeplog.");

            sleepLog = new SleepLogger(System.currentTimeMillis(), trackDate, null, null, 0, 0, false, false);
            try {
                mSleepLogDao.create(sleepLog);
            } catch (SQLException e) {
                Log.d(Constants.TAG, "Add new sleeplogger data record failed: " + e.toString());
                e.printStackTrace();
            }
        }

        mPausingDialog.dismiss();
        onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String trackDate;
        trackDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date());
        Bundle bundle = new Bundle();

        switch (item.getItemId()) {
            case R.id.home_action_morning_card:
                bundle.putString("trackDate", trackDate);
                Intent intent1 = new Intent(mContext, MorningCardActivity.class);
                intent1.putExtras(bundle);
                mContext.startActivity(intent1);
                return true;
            case R.id.home_action_evening_card:
                bundle.putString("trackDate", trackDate);
                Intent intent2 = new Intent(mContext, EveningCardActivity.class);
                intent2.putExtras(bundle);
                mContext.startActivity(intent2);
                return true;
            case R.id.home_action_fix_bug:
                DeleteBuilder<SleepLogger, Integer> db = mSleepLogDao.deleteBuilder();
                try {
                    db.where().ge("id", 21)
                            .and()
                            .le("id", 738);
                    mSleepLogDao.delete(db.prepare());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
