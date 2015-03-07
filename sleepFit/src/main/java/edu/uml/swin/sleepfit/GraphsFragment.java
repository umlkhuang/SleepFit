package edu.uml.swin.sleepfit;

import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.DB.SleepLogger;
import edu.uml.swin.sleepfit.graphplot.MultitouchPlot;
import edu.uml.swin.sleepfit.graphplot.MyBarRenderer;
import edu.uml.swin.sleepfit.util.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GraphsFragment extends Fragment {
	
	private static final String ARG_SECTION_NUMBER = "section_number";

    private TextView mChooseGraphTitle;
    private RadioGroup mOptionGroup;
    private RadioButton mSleepDebtOption;
    private RadioButton mSleepDurationOption;
    private MultitouchPlot mGraph;
    private boolean mGraphExisting;

    private int mGraphOption;
    private Context mContext;
    private DatabaseHelper mDatabaseHelper;
    private Dao<SleepLogger, Integer> mSleepLogDao;
    private List<SleepLogger> mSleepLogList = null;
    private float mWakeSleepRatio;
    private XYSeries mSleepDebt;
    private XYSeries mDuration;
    private XYSeries mZero;
    private XYSeries mNeedSleep;
    private List<Float> mSleepDebtData;
    private List<Float> mDurationData;
    private List<Float> mZeroData;
    private List<Float> mNeedSleepData;
    private List<String> mTimeLabels;
    private LineAndPointFormatter mDataFormatter;
    private LineAndPointFormatter mZeroFormatter;
    private LineAndPointFormatter mNeededSleepHourFormatter;
    private float mMaxDebt;
    private float mMinDebt;
    private float mMaxDuration;
	
	public static GraphsFragment newInstance(int sectionNumber) {
		GraphsFragment fragment = new GraphsFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public GraphsFragment() {
	}

	
	// Sequence of Fragment life cycle 
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));

        mContext = activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.d(Constants.TAG, "In onCreate");
		
		mGraphExisting = false;
        mGraphOption = 0;

        mDatabaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        try {
            mSleepLogDao = mDatabaseHelper.getSleepLoggerDao();
        } catch (SQLException e) {
            Log.e(Constants.TAG, "Cannot get the SleepLogger DAO: " + e.toString());
            e.printStackTrace();
        }

        SharedPreferences preferences = mContext.getSharedPreferences(Constants.SURVEY_FILE_NAME, Context.MODE_PRIVATE);
        float sleepHours = Float.valueOf(preferences.getString("sleepHours", "8"));
        mWakeSleepRatio = (float) ((24.0 - sleepHours) / sleepHours);

        mSleepDebtData = new ArrayList<Float>();
        mDurationData = new ArrayList<Float>();
        mZeroData = new ArrayList<Float>();
        mNeedSleepData = new ArrayList<Float>();
        mTimeLabels = new ArrayList<String>();
        getAllSleepHistory();
        processData();
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(Constants.TAG, "In onCreateView");
		
		View rootView = inflater.inflate(R.layout.fragment_graphs, container, false);

        mChooseGraphTitle = (TextView) rootView.findViewById(R.id.statGraphTitle);
        mOptionGroup = (RadioGroup) rootView.findViewById(R.id.graphRadioGroup);
        mSleepDebtOption = (RadioButton) rootView.findViewById(R.id.sleepDebtOption);
        mSleepDurationOption = (RadioButton) rootView.findViewById(R.id.sleepDurationOption);
        mGraph = (MultitouchPlot) rootView.findViewById(R.id.statsGraph);
        //mGraph = (XYPlot) rootView.findViewById(R.id.statsGraph);

        mOptionGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String optionStr;
                RadioButton radioButton = (RadioButton) group.findViewById(checkedId);
                if (radioButton.isChecked()) {
                    optionStr = radioButton.getText().toString();
                    if (optionStr.equals("Sleep Debt")) {
                        mGraphOption = 0;
                    } else if (optionStr.equals("Sleep Duration")) {
                        mGraphOption = 1;
                    } else {
                        mGraphOption = 0;
                    }
                    onResume();
                }
            }
        });

        return rootView;
	}

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(Constants.TAG, "In graphsfragment onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		
		if (savedInstanceState != null) {
			mGraphExisting = savedInstanceState.getBoolean("graphExists", false);

		} 
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Log.d(Constants.TAG, "In Graphs fragment onStart");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(Constants.TAG, "In Graphs fragment onResume");

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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		//inflater.inflate(R.menu.home, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
		Log.d(Constants.TAG, "In onSaveinstanceState");
		
		super.onSaveInstanceState(outState);
		outState.putBoolean("graphExists", false);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			((ActionBarActivity) getActivity()).getSupportActionBar().hide();
			mChooseGraphTitle.setVisibility(View.GONE);
			mOptionGroup.setVisibility(View.GONE);

            mGraph.clear();
            mGraph.setDomainBoundaries(0, mDurationData.size()-1, BoundaryMode.AUTO);
            if (mGraphOption == 0) {
                mGraph.setRangeBoundaries(mMinDebt, mMaxDebt, BoundaryMode.FIXED);
                mGraph.addSeries(mSleepDebt, mDataFormatter);
                mGraph.addSeries(mZero, mZeroFormatter);

                mGraph.setTitle("Accumulate Sleep Debt (7 days)");
                mGraph.setRangeLabel("Sleep Debt (hours)");
            } else if (mGraphOption == 1) {
                mGraph.setRangeBoundaries(0f, mMaxDuration, BoundaryMode.FIXED);
                mGraph.addSeries(mDuration, mDataFormatter);
                mGraph.addSeries(mNeedSleep, mNeededSleepHourFormatter);

                mGraph.setTitle("Sleep Duration Statistic");
                mGraph.setRangeLabel("Sleep Duration (hours)");
            }
            mGraph.redraw();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			((ActionBarActivity) getActivity()).getSupportActionBar().show();
			mChooseGraphTitle.setVisibility(View.VISIBLE);
			mOptionGroup.setVisibility(View.VISIBLE);

            mGraph.clear();
            mGraph.setDomainBoundaries(0, mDurationData.size() > 10 ? 10 : mDurationData.size()-1, BoundaryMode.FIXED);
            if (mGraphOption == 0) {
                mGraph.setRangeBoundaries(mMinDebt, mMaxDebt, BoundaryMode.FIXED);
                mGraph.addSeries(mSleepDebt, mDataFormatter);
                mGraph.addSeries(mZero, mZeroFormatter);

                mGraph.setTitle("Accumulate Sleep Debt (7 days)");
                mGraph.setRangeLabel("Sleep Debt (hours)");
            } else if (mGraphOption == 1) {
                mGraph.setRangeBoundaries(0f, mMaxDuration, BoundaryMode.FIXED);
                mGraph.addSeries(mDuration, mDataFormatter);
                mGraph.addSeries(mNeedSleep, mNeededSleepHourFormatter);

                mGraph.setTitle("Sleep Duration Statistic");
                mGraph.setRangeLabel("Sleep Duration (hours)");
            }
            mGraph.redraw();
		}
	}

    private void getAllSleepHistory() {
        mSleepLogList = null;
        if (mSleepLogDao != null) {
            QueryBuilder<SleepLogger, Integer> qb = mSleepLogDao.queryBuilder();
            try {
                qb.orderBy("wakeupTime", true);
                mSleepLogList = mSleepLogDao.query(qb.prepare());
            } catch (SQLException e) {
                e.printStackTrace();
                Log.e(Constants.TAG, e.toString());
            }
        }
    }

    private void processData() {
        if (mSleepLogList != null && mSleepLogList.size() > 0) {
            int len = mSleepLogList.size();
            for (int i = len-1; i >= 0; i--) {
                SleepLogger sleep = mSleepLogList.get(i);
                if (sleep.getSleepTime() == null || sleep.getWakeupTime() == null) {
                    mSleepLogList.remove(i);
                }
            }

            List<Float> debtList = new ArrayList<Float>();
            float cumDebt = 0f;
            len = mSleepLogList.size();
            for (int i = 0; i < len; i++) {
                SleepLogger sleep = mSleepLogList.get(i);
                float duration = (sleep.getWakeupTime().getTime() - sleep.getSleepTime().getTime()) / 1000f / 60f / 60f;
                mDurationData.add(duration);
                //Log.d(Constants.TAG, "From " + sleep.getSleepTime().toString() + " to " + sleep.getSleepTime().toString() + ", duration = " + duration);
                duration = duration + (float) sleep.getNaptime() / 60f;
                float needSleepHours = (24 - duration) / mWakeSleepRatio;
                float sleepDebt = duration - needSleepHours;
                debtList.add(sleepDebt);

                if (i < 7) {
                    cumDebt += sleepDebt;
                } else {
                    cumDebt += sleepDebt;
                    cumDebt -= debtList.get(i - 7);
                }
                mSleepDebtData.add(cumDebt);
                mTimeLabels.add(new SimpleDateFormat("MM/dd", Locale.US).format(sleep.getWakeupTime()));
                mZeroData.add(0f);
                mNeedSleepData.add((float) (24.0 / (mWakeSleepRatio + 1.0)));
            }
            Log.d(Constants.TAG, "Duration: " + mDurationData.toString());
            Log.d(Constants.TAG, "Sleep debt: " + mSleepDebtData.toString());

            if (mSleepDebtData.size() == 0) {
                mSleepDebtData.add(0f);
                mSleepDebtData.add(0f);
                mDurationData.add(0f);
                mDurationData.add(0f);
                mZeroData.add(0f);
                mZeroData.add(0f);
                mNeedSleepData.add((float) (24.0 / (mWakeSleepRatio + 1.0)));
                mNeedSleepData.add((float) (24.0 / (mWakeSleepRatio + 1.0)));
                mMaxDebt = 1f;
                mMinDebt = 0f;
                mMaxDuration = 1f;
                mTimeLabels.add("");
                mTimeLabels.add("");
            } else if (mSleepDebtData.size() == 1) {
                mSleepDebtData.add(mSleepDebtData.get(0));
                mDurationData.add(mDurationData.get(0));
                mZeroData.add(0f);
                mTimeLabels.add(mTimeLabels.get(0));
            }

            mSleepDebt = new SimpleXYSeries(mSleepDebtData, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");
            mDuration = new SimpleXYSeries(mDurationData, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");
            mZero = new SimpleXYSeries(mZeroData, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");
            mNeedSleep = new SimpleXYSeries(mNeedSleepData, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");

            if (mSleepDebtData != null && mSleepDebtData.size() > 0) {
                mMaxDebt = Collections.max(mSleepDebtData) + 1.5f;
                mMinDebt = Collections.min(mSleepDebtData) - 1.5f;
            } else {
                mMaxDebt = 1f;
                mMinDebt = 0f;
            }
            if (mDurationData != null && mDurationData.size() > 0) {
                mMaxDuration = Collections.max(mDurationData) + 1.5f;
            } else {
                mMaxDuration = 1f;
            }
        } else {
            mSleepDebt = new SimpleXYSeries(Arrays.asList(new Float[] {0f, 0f}), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");
            mDuration = new SimpleXYSeries(Arrays.asList(new Float[] {0f, 0f}), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");
            mZero = new SimpleXYSeries(Arrays.asList(new Float[] {0f, 0f}), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");
            mNeedSleepData.add((float) (24.0 / (mWakeSleepRatio + 1.0)));
            mNeedSleepData.add((float) (24.0 / (mWakeSleepRatio + 1.0)));
            mNeedSleep = new SimpleXYSeries(mNeedSleepData, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");
            mMaxDebt = 1f;
            mMinDebt = 0f;
            mMaxDuration = 1f;
        }
    }

	private void drawGraph() {
        if (!mGraphExisting) {
            PointLabelFormatter plf = new PointLabelFormatter();
            plf.getTextPaint().setTextSize(22);
            plf.getTextPaint().setColor(Color.WHITE);
            plf.getTextPaint().setTextAlign(Paint.Align.RIGHT);
            plf.getTextPaint().setTypeface(Typeface.create("Arial", Typeface.BOLD));
            mDataFormatter = new LineAndPointFormatter(Color.WHITE, Color.WHITE, Color.argb(255, 0, 128, 0), plf);
            mDataFormatter.getVertexPaint().setStrokeWidth(14);

            Paint dataPaint = mDataFormatter.getLinePaint();
            dataPaint.setStrokeWidth(6);
            mDataFormatter.setLinePaint(dataPaint);
            mGraph.getGraphWidget().getGridBackgroundPaint().setColor(Color.BLACK);
            mGraph.getGraphWidget().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
            mGraph.getGraphWidget().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
            mDataFormatter.setPointLabeler(new PointLabeler() {
                @Override
                public String getLabel(XYSeries xySeries, int i) {
                    return String.format("%.1f", (float) xySeries.getY(i));
                }
            });

            mZeroFormatter = new LineAndPointFormatter(Color.RED, null, null, null);
            Paint zeroPaint = mZeroFormatter.getLinePaint();
            zeroPaint.setStyle(Paint.Style.STROKE);
            zeroPaint.setStrokeWidth(4);
            zeroPaint.setPathEffect(new DashPathEffect(new float[] {10, 10}, 0));
            mZeroFormatter.setLinePaint(zeroPaint);

            mNeededSleepHourFormatter = new LineAndPointFormatter(Color.RED, null, null, null);
            Paint needSleepPaint = mNeededSleepHourFormatter.getLinePaint();
            needSleepPaint.setStyle(Paint.Style.STROKE);
            needSleepPaint.setStrokeWidth(4);
            needSleepPaint.setPathEffect(new DashPathEffect(new float[] {10, 10}, 0));
            mNeededSleepHourFormatter.setLinePaint(needSleepPaint);

            mGraph.setTicksPerRangeLabel(1);
            mGraph.getLayoutManager().remove(mGraph.getLegendWidget());
            mGraph.getLayoutManager().remove(mGraph.getDomainLabelWidget());
            mGraph.setDomainBoundaries(0, mDurationData.size() > 10 ? 10 : mDurationData.size()-1, BoundaryMode.FIXED);
            mGraph.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
            mGraph.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 1);
            mGraph.getGraphWidget().setDomainLabelOrientation(-25);
            mGraph.getGraphWidget().setDomainLabelVerticalOffset(5);
            mGraph.getGraphWidget().setDomainLabelHorizontalOffset(-13);
            mGraph.setDomainValueFormat(new Format() {
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

            if (mGraphOption == 0) {
                mGraph.setRangeBoundaries(mMinDebt, mMaxDebt, BoundaryMode.FIXED);
                mGraph.addSeries(mSleepDebt, mDataFormatter);
                mGraph.addSeries(mZero, mZeroFormatter);

                mGraph.setTitle("Accumulate Sleep Debt (7 days)");
                mGraph.setRangeLabel("Sleep Debt (hours)");
            } else if (mGraphOption == 1) {
                mGraph.setRangeBoundaries(0f, mMaxDuration, BoundaryMode.FIXED);
                mGraph.addSeries(mDuration, mDataFormatter);
                mGraph.addSeries(mNeedSleep, mNeededSleepHourFormatter);

                mGraph.setTitle("Sleep Duration Statistic");
                mGraph.setRangeLabel("Sleep Duration (hours)");
            }
            mGraphExisting = true;
        } else {
            mGraph.clear();
            if (mGraphOption == 0) {
                mGraph.setRangeBoundaries(mMinDebt, mMaxDebt, BoundaryMode.FIXED);
                mGraph.addSeries(mSleepDebt, mDataFormatter);
                mGraph.addSeries(mZero, mZeroFormatter);

                mGraph.setTitle("Accumulate Sleep Debt (7 days)");
                mGraph.setRangeLabel("Sleep Debt (hours)");
            } else if (mGraphOption == 1) {
                mGraph.setRangeBoundaries(0f, mMaxDuration, BoundaryMode.FIXED);
                mGraph.addSeries(mDuration, mDataFormatter);
                mGraph.addSeries(mNeedSleep, mNeededSleepHourFormatter);

                mGraph.setTitle("Sleep Duration Statistic");
                mGraph.setRangeLabel("Sleep Duration (hours)");
            }
            mGraph.redraw();
        }
	}

}
