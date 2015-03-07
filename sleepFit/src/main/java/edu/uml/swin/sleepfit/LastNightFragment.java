package edu.uml.swin.sleepfit;

import java.sql.SQLException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.DynamicTableModel;
import com.androidplot.ui.PositionMetrics;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.DB.SensingData;
import edu.uml.swin.sleepfit.graphplot.MultitouchPlot;
import edu.uml.swin.sleepfit.util.Constants;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

public class LastNightFragment extends Fragment {

	private static final String ARG_SECTION_NUMBER = "section_number";
	private static final float EVENT_THRESHOLD = 0.35f;
	
	private Context mContext;
	private DatabaseHelper mDatabaseHelper;
	private Dao<SensingData, Integer> mSensingDataDao;
	private List<SensingData> mSensingDataList = null;
	private Calendar mStartTS;
	private Calendar mEndTS;
	private List<Float> mLightData;
	private List<Float> mSoundData;
	private List<Float> mScreenOn;
	private List<Float> mMovement;
	private List<Float> mEvents;
	private List<String> mTimeLabels;
	private MultitouchPlot mGraphPlot;
	private boolean mGraphExisting;

    private XYSeries lightSeries;
    private XYSeries soundSeries;
    private XYSeries screenOnSeries;
    private XYSeries movementSeries;
    private XYSeries eventsSeries;

    private LineAndPointFormatter lightFormat;
    private LineAndPointFormatter soundFormat;
    private LineAndPointFormatter screenOnFormat;
    private LineAndPointFormatter movementFormat;
    private BarFormatter eventFormat;
	
	public static LastNightFragment newInstance(int sectionNumber) {
		LastNightFragment fragment = new LastNightFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}
	
	public LastNightFragment() {
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_lastnight, container, false);
		mGraphPlot = (MultitouchPlot) rootView.findViewById(R.id.sleepNightGraph);
		
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
		Log.d(Constants.TAG, "In onCreate of LastNightFragment");
		
		mStartTS = Calendar.getInstance();
		mStartTS.add(Calendar.DAY_OF_YEAR, -1);
		mStartTS.set(Calendar.HOUR_OF_DAY, 21);
		mStartTS.set(Calendar.MINUTE, 0);
		mStartTS.set(Calendar.SECOND, 0);
		
		mEndTS = Calendar.getInstance();
		mEndTS.set(Calendar.HOUR_OF_DAY, 9);
		mEndTS.set(Calendar.MINUTE, 59);
		mEndTS.set(Calendar.SECOND, 59);
		
		mLightData = new ArrayList<Float>();
		mSoundData = new ArrayList<Float>();
		mScreenOn = new ArrayList<Float>();
		mMovement = new ArrayList<Float>();
		mEvents = new ArrayList<Float>();
		mTimeLabels = new ArrayList<String>();
		mGraphExisting = false;
		
		mDatabaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
		try {
			mSensingDataDao = mDatabaseHelper.getSensingDataDao();
			QueryBuilder<SensingData, Integer> qb = mSensingDataDao.queryBuilder();
			qb.where().ge("createTime", mStartTS.getTime())
					.and().le("createTime", mEndTS.getTime());
			mSensingDataList = mSensingDataDao.query(qb.prepare());
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Cannot get the SensingData DAO: " + e.toString());
			e.printStackTrace();
		}
		
		if (mSensingDataList != null && mSensingDataList.size() > 0) {
			int len = mSensingDataList.size();
			for (int i = 0; i < len; i++) {
				SensingData data = mSensingDataList.get(i);
				SimpleDateFormat formater = new SimpleDateFormat("hh:mma", Locale.US);
				mLightData.add(data.getIlluminanceMax());
				mSoundData.add(data.getDecibelMax());
				mScreenOn.add(getScreenOnSeconds(data.getAppUsage()));
				mMovement.add((float) data.getMovement());
				mTimeLabels.add(formater.format(data.getCreateTime()));
				
				if (i == 0) {
					mEvents.add(0f);
				} else {
					int count = 0;
					float lightDelta = Math.abs(mLightData.get(i) - mLightData.get(i - 1));
					if ((lightDelta / Math.max(mLightData.get(i), mLightData.get(i - 1))) > EVENT_THRESHOLD) count++;
					
					float soundDelta = Math.abs(mSoundData.get(i) - mSoundData.get(i - 1));
					if ((soundDelta / Math.max(mSoundData.get(i), mSoundData.get(i - 1))) > EVENT_THRESHOLD) count++;
					
					float screenOnDelta = Math.abs(mScreenOn.get(i) - mScreenOn.get(i - 1));
					if ((screenOnDelta / Math.max(mScreenOn.get(i), mScreenOn.get(i - 1))) > EVENT_THRESHOLD) count++;
					
					float movementDelta = Math.abs(mMovement.get(i) - mMovement.get(i - 1));
					if ((movementDelta / Math.max(mMovement.get(i), mMovement.get(i - 1))) > EVENT_THRESHOLD) count++;
					
					if (count >= 3) {
						mEvents.add(1.06f);
						//Log.d(Constants.TAG, "lightDelta = " + lightDelta + ", soundDelta = " + soundDelta + ", screenOnDelta = " + screenOnDelta + ", movementDelta = " + movementDelta);
					} else {
						mEvents.add(0f);
					}
				}
			} 
		}

		float maxLight = 0f;
		if (mLightData != null && mLightData.size() > 0) {
			maxLight = Collections.max(mLightData);
		} 
		
		float maxSound = 0f;
		if (mSoundData != null && mSoundData.size() > 0) {
			maxSound = Collections.max(mSoundData);
		} 
		
		float maxScreenOn = 0f;
		if (mScreenOn != null && mScreenOn.size() > 0) {
			maxScreenOn = Collections.max(mScreenOn);
		}
		
		float maxMovement = 0f;
		if (mMovement != null && mMovement.size() > 0) {
			maxMovement = Collections.max(mMovement);
		}
		
		if (maxLight > 0) {
			for (int i = 0; i < mLightData.size(); i++) {
				mLightData.set(i, mLightData.get(i) / maxLight);
			}
		}
		if (maxSound > 0) {
			for (int i = 0; i < mSoundData.size(); i++) {
				mSoundData.set(i, mSoundData.get(i) / maxSound);
			}
		}
		if (maxScreenOn > 0) {
			for (int i = 0; i < mScreenOn.size(); i++) {
				mScreenOn.set(i, mScreenOn.get(i) / maxScreenOn);
			}
		}
		if (maxMovement > 0) {
			for (int i = 0; i < mMovement.size(); i++) {
				mMovement.set(i, mMovement.get(i) / maxMovement);
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
		Log.d(Constants.TAG, "In onSaveinstanceState, LastNightFragment");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(Constants.TAG, "In onResume, LastNightFragment");
		
		drawGraph();

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.TMP_PREF_FILE, Context.MODE_MULTI_PROCESS);
        boolean noGraphTip = preferences.getBoolean("noGraphTip", false);
        if (!noGraphTip) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DialogFragment dialog = new GraphViewTipDialogFragment();
            dialog.show(ft, "graphTip");
        }
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.d(Constants.TAG, "In onPause, LastNightFragment");
	}
	
	private float getScreenOnSeconds(String appUsage) {
		float ret = 0;
		if (appUsage.equals("")) return ret;
		String[] usage = appUsage.split(",");
		for (int i = 0; i < usage.length; i++) {
			String tmp = usage[i];
			String[] segs = tmp.split(":");
			if (segs.length == 3) {
				ret += Float.valueOf(segs[2]);
			}
		}
		
		return ret;
	}
	
	private void drawGraph() {
		if (mGraphExisting) return;

        /*
		if (mLightData.size() == 0) {
			lightSeries = new SimpleXYSeries(Arrays.asList(new Float[] {0f, 0f}), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Light");
			soundSeries = new SimpleXYSeries(Arrays.asList(new Float[] {0f, 0f}), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Sound");
			screenOnSeries = new SimpleXYSeries(Arrays.asList(new Float[] {0f, 0f}), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Screen-On");
			movementSeries = new SimpleXYSeries(Arrays.asList(new Float[] {0f, 0f}), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Movement");
			eventsSeries = new SimpleXYSeries(Arrays.asList(new Float[] {0f, 0f}), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Event");
		} else {
			lightSeries = new SimpleXYSeries(mLightData, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Light");
			soundSeries = new SimpleXYSeries(mSoundData, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Sound");
			screenOnSeries = new SimpleXYSeries(mScreenOn, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Screen-On");
			movementSeries = new SimpleXYSeries(mMovement, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Movement");
			eventsSeries = new SimpleXYSeries(mEvents, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Event");
		}
		*/

        if (mLightData.size() == 0) {
            mLightData.add(0f);
            mLightData.add(0f);
            mSoundData.add(0f);
            mSoundData.add(0f);
            mScreenOn.add(0f);
            mScreenOn.add(0f);
            mMovement.add(0f);
            mMovement.add(0f);
            mEvents.add(0f);
            mEvents.add(0f);
        }
        lightSeries = new SimpleXYSeries(mLightData, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Light");
        soundSeries = new SimpleXYSeries(mSoundData, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Sound");
        screenOnSeries = new SimpleXYSeries(mScreenOn, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Screen-On");
        movementSeries = new SimpleXYSeries(mMovement, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Movement");
        eventsSeries = new SimpleXYSeries(mEvents, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Event");

		
		lightFormat = new LineAndPointFormatter(Color.rgb(204, 0, 0), null, Color.argb(60, 204, 0, 0), null);
		mGraphPlot.addSeries(lightSeries, lightFormat);

        soundFormat = new LineAndPointFormatter(Color.rgb(0, 128, 0), null, Color.argb(60, 0, 128, 0), null);
		mGraphPlot.addSeries(soundSeries, soundFormat);
		
		screenOnFormat = new LineAndPointFormatter(Color.rgb(0, 0, 204), null, Color.argb(60, 0, 0, 204), null);
		mGraphPlot.addSeries(screenOnSeries, screenOnFormat);
		
		movementFormat = new LineAndPointFormatter(Color.rgb(255, 215, 0), null, Color.argb(60, 255, 215, 0), null);
		mGraphPlot.addSeries(movementSeries, movementFormat);

        eventFormat = new BarFormatter(Color.DKGRAY, Color.BLACK);
        PointLabelFormatter plf = new PointLabelFormatter();
        plf.getTextPaint().setTextSize(16);
        plf.getTextPaint().setColor(Color.BLACK);
        plf.getTextPaint().setAlpha(180);
        eventFormat.setPointLabelFormatter(plf);
        eventFormat.setPointLabeler(new PointLabeler() {
            @Override
            public String getLabel(XYSeries series, int index) {
                if ((Float) series.getY(index) == 0f) return "";
                else return mTimeLabels.get(index);
            }
        });
        mGraphPlot.addSeries(eventsSeries, eventFormat);
		
		mGraphPlot.setTicksPerRangeLabel(1);
		mGraphPlot.setTitle("Contextual Sensing Data");
		mGraphPlot.getGraphWidget().setDomainLabelOrientation(-20);
		mGraphPlot.getGraphWidget().setDomainLabelVerticalOffset(5);
		mGraphPlot.getGraphWidget().setDomainLabelHorizontalOffset(-10);
		mGraphPlot.getGraphWidget().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
		mGraphPlot.setRangeBoundaries(0, 1.125, BoundaryMode.FIXED);
        mGraphPlot.setDomainBoundaries(0, Math.min(11, mLightData.size()-1), BoundaryMode.FIXED);
        mGraphPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
		mGraphPlot.setDomainValueFormat(new Format() {
			private static final long serialVersionUID = 1L;
			@Override
			public StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
				if (mTimeLabels.size() == 0) return new StringBuffer(" ");
				else return new StringBuffer(mTimeLabels.get(((Number) object).intValue()));
			}

			@Override
			public Object parseObject(String string, ParsePosition position) {
				return null;
			}
		});

        mGraphPlot.getLegendWidget().setTableModel(new DynamicTableModel(4, 1));
        mGraphPlot.getLegendWidget().setSize(new SizeMetrics(40, SizeLayoutType.ABSOLUTE, 0.95f, SizeLayoutType.RELATIVE));
        mGraphPlot.getLegendWidget().setPositionMetrics(new PositionMetrics(35, XLayoutStyle.ABSOLUTE_FROM_LEFT, -2, YLayoutStyle.ABSOLUTE_FROM_BOTTOM, AnchorPosition.LEFT_BOTTOM));
		mGraphExisting = true;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			((ActionBarActivity) getActivity()).getSupportActionBar().hide();

            mGraphPlot.clear();
            mGraphPlot.setDomainBoundaries(0, Math.min(23, mLightData.size()-1), BoundaryMode.FIXED);
            mGraphPlot.setRangeBoundaries(0, 1.125, BoundaryMode.FIXED);
            mGraphPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
            mGraphPlot.addSeries(lightSeries, lightFormat);
            mGraphPlot.addSeries(soundSeries, soundFormat);
            mGraphPlot.addSeries(screenOnSeries, screenOnFormat);
            mGraphPlot.addSeries(movementSeries, movementFormat);
            mGraphPlot.addSeries(eventsSeries, eventFormat);
            mGraphPlot.redraw();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			((ActionBarActivity) getActivity()).getSupportActionBar().show();

            mGraphPlot.clear();
            mGraphPlot.setDomainBoundaries(0, Math.min(11, mLightData.size()-1), BoundaryMode.FIXED);
            mGraphPlot.setRangeBoundaries(0, 1.125, BoundaryMode.FIXED);
            mGraphPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
            mGraphPlot.addSeries(lightSeries, lightFormat);
            mGraphPlot.addSeries(soundSeries, soundFormat);
            mGraphPlot.addSeries(screenOnSeries, screenOnFormat);
            mGraphPlot.addSeries(movementSeries, movementFormat);
            mGraphPlot.addSeries(eventsSeries, eventFormat);
            mGraphPlot.redraw();
		}
	}
	
}
