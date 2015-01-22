package edu.uml.swin.sleepfit;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.DB.SleepLogger;
import edu.uml.swin.sleepfit.cardview.HistoryCard;
import edu.uml.swin.sleepfit.cardview.HistoryListAdapter;
import edu.uml.swin.sleepfit.cardview.HistoryListAdapter.OnItemClickListener;
import edu.uml.swin.sleepfit.util.Constants;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class SleepHistoryFragment extends Fragment {

	private static final String ARG_SECTION_NUMBER = "section_number";
	
	private TextView mEmptyText;
	private RecyclerView mRecyclerView;
	private HistoryListAdapter mAdapter;
	private ArrayList<HistoryCard> mCards;
	
	private Context mContext;
	private DatabaseHelper mDatabaseHelper;
	private Dao<SleepLogger, Integer> mSleepLogDao;
	private List<SleepLogger> mSleepLogList = null;
	
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
		
		mContext = (Context) activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(Constants.TAG, "In onCreate of SleepHistoryFragment");
		
		mCards = new ArrayList<HistoryCard>();
		
		mDatabaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
		try {
			mSleepLogDao = mDatabaseHelper.getSleepLoggerDao();
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Cannot get the SleepLogger DAO: " + e.toString());
			e.printStackTrace();
		}
		
		if (mSleepLogDao != null) {
			QueryBuilder<SleepLogger, Integer> qb = mSleepLogDao.queryBuilder();
			try {
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
		
		if (mSleepLogList != null && mSleepLogList.size() > 0) {
			for (SleepLogger log : mSleepLogList) {
				mCards.add(new HistoryCard(log, wakeSleepRatio));
			}
		} 
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		//inflater.inflate(R.menu.home, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
		Log.d(Constants.TAG, "In onSaveinstanceState, SleepHistoryFragment");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(Constants.TAG, "In onResume, SleepHistoryFragment");
		
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
				
				String trackDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(mCards.get(position).mWakeTime);
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
	
	
}
