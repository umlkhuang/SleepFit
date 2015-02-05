package edu.uml.swin.sleepfit;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import edu.uml.swin.sleepfit.graphplot.MultitouchPlot;
import edu.uml.swin.sleepfit.graphplot.MyBarFormatter;
import edu.uml.swin.sleepfit.graphplot.MyBarRenderer;
import edu.uml.swin.sleepfit.graphplot.MyIndexFormat;
import edu.uml.swin.sleepfit.util.Constants;
import edu.uml.swin.sleepfit.util.HttpRequestTask;
import edu.uml.swin.sleepfit.util.HttpRequestTask.HttpRequestCallback;

public class GraphsFragment_bak extends Fragment implements AdapterView.OnItemSelectedListener, HttpRequestCallback {

	private static final String ARG_SECTION_NUMBER = "section_number";

	private Spinner mGraphTypeSpinner;
	private Spinner mGraphViewTypeSpinner;
	private LinearLayout mSelectGraphLayout;
	private LinearLayout mSelectDateLayout;
	private ImageView mLeftArrow;
	private ImageView mRightArrow;
	private TextView mDateText;
	private ProgressBar mProgressBar;
	private int mGraphTypePosition;
	private int mGraphViewTypePosition;
	private String mDateTitle;
	private String mJsonStr;
	private boolean mGraphExisting;

	private MultitouchPlot mTimePlot;
	private MultitouchPlot mIndexPlot;

	public static GraphsFragment_bak newInstance(int sectionNumber) {
		GraphsFragment_bak fragment = new GraphsFragment_bak();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public GraphsFragment_bak() {
	}

	
	// Sequence of Fragment life cycle 
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER)); 
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mGraphExisting = false;
		Log.d(Constants.TAG, "In onCreate");
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(Constants.TAG, "In onCreateView");
		
		View rootView = inflater.inflate(R.layout.fragment_graphs_bak, container, false);

		mSelectGraphLayout = (LinearLayout) rootView.findViewById(R.id.selectGraphLinearLayout);
		mSelectDateLayout = (LinearLayout) rootView.findViewById(R.id.selectDateLinearLayout);
		mGraphTypeSpinner = (Spinner) rootView.findViewById(R.id.graph_type);
		mGraphViewTypeSpinner = (Spinner) rootView.findViewById(R.id.show_type);
		mGraphTypeSpinner.setOnItemSelectedListener(this);
		mGraphViewTypeSpinner.setOnItemSelectedListener(this); 
		
		mLeftArrow = (ImageView) rootView.findViewById(R.id.leftArrowButton);
		mRightArrow = (ImageView) rootView.findViewById(R.id.rightArrowButton);
		mDateText = (TextView) rootView.findViewById(R.id.pick_date);
		mProgressBar = (ProgressBar) rootView.findViewById(R.id.waitingResponse);
		
		mTimePlot = (MultitouchPlot) rootView.findViewById(R.id.graph1);
		mIndexPlot = (MultitouchPlot) rootView.findViewById(R.id.graph2);
        
        return rootView;
	}

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(Constants.TAG, "In graphsfragment onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		
		if (savedInstanceState != null) {
			mGraphTypePosition = savedInstanceState.getInt("graphTypeSelected", 0);
			mGraphViewTypePosition = savedInstanceState.getInt("graphViewSelected", 0);
			mJsonStr = savedInstanceState.getString("json", null);
			mDateTitle = savedInstanceState.getString("title", "");
			mGraphExisting = savedInstanceState.getBoolean("graphExists", false);
			
			Log.d(Constants.TAG, "Title = " + mDateTitle + ", JSONstr = " + mJsonStr);
		} 
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		mLeftArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SimpleDateFormat dayFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
				SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy", Locale.US);
				switch (mGraphViewTypePosition) { 
				case 0:
					try {
						Date showDate = dayFormat.parse(mDateTitle);
						Calendar cal = Calendar.getInstance();
						cal.setTime(showDate);
						cal.add(Calendar.DAY_OF_YEAR, -1);
						mDateTitle = dayFormat.format(cal.getTime());
					} catch (ParseException e) {
						e.printStackTrace();
					}
					break;
				case 1:
					String[] dateArray = mDateTitle.split(" - ");
					try {
						Date startDate = dayFormat.parse(dateArray[0]);
						Calendar cal = Calendar.getInstance();
						cal.setTime(startDate);
						cal.add(Calendar.WEEK_OF_YEAR, -1);
						startDate = cal.getTime();
						cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
						Date endDate = cal.getTime();
						mDateTitle = dayFormat.format(startDate) + " - " + dayFormat.format(endDate);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					break;
				case 2:
					try {
						Date monthDate = monthFormat.parse(mDateTitle);
						Calendar cal = Calendar.getInstance();
						cal.setTime(monthDate);
						cal.add(Calendar.MONTH, -1);
						mDateTitle = monthFormat.format(cal.getTime());
					} catch (ParseException e) {
						e.printStackTrace();
					}
					break;
				default:
					break;
				}
				mDateText.setText(mDateTitle);
				mJsonStr = null;
				onResume();
			}
		});
		
		mRightArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SimpleDateFormat dayFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
				SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy", Locale.US);
				switch (mGraphViewTypePosition) { 
				case 0:
					try {
						Date showDate = dayFormat.parse(mDateTitle);
						Calendar cal = Calendar.getInstance();
						cal.setTime(showDate);
						cal.add(Calendar.DAY_OF_YEAR, 1);
						mDateTitle = dayFormat.format(cal.getTime());
					} catch (ParseException e) {
						e.printStackTrace();
					}
					break;
				case 1:
					String[] dateArray = mDateTitle.split(" - ");
					try {
						Date startDate = dayFormat.parse(dateArray[0]);
						Calendar cal = Calendar.getInstance();
						cal.setTime(startDate);
						cal.add(Calendar.WEEK_OF_YEAR, 1);
						startDate = cal.getTime();
						cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
						Date endDate = cal.getTime();
						mDateTitle = dayFormat.format(startDate) + " - " + dayFormat.format(endDate);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					break;
				case 2:
					try {
						Date monthDate = monthFormat.parse(mDateTitle);
						Calendar cal = Calendar.getInstance();
						cal.setTime(monthDate);
						cal.add(Calendar.MONTH, 1);
						mDateTitle = monthFormat.format(cal.getTime());
					} catch (ParseException e) {
						e.printStackTrace();
					}
					break;
				default:
					break; 
				}
				mDateText.setText(mDateTitle);
				mJsonStr = null;
				onResume();
			}
		});
		
		Log.d(Constants.TAG, "In Graphs fragment onStart");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(Constants.TAG, "In Graphs fragment onResume");
		
		if (mDateTitle == null) {
			switch (mGraphViewTypePosition) { 
			// View in daily
			case 0:
				SimpleDateFormat dayFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
				mDateTitle = dayFormat.format(new Date());
				break;
			// View in weekly
			case 1:
				SimpleDateFormat weekFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(System.currentTimeMillis());
				cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
				Date sunday = cal.getTime();
				cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
				Date saturday = cal.getTime();
				mDateTitle = weekFormat.format(sunday) + " - " + weekFormat.format(saturday);
				break;
			// View in monthly 
			case 2:
				SimpleDateFormat  monthFormat = new SimpleDateFormat("MM/yyyy", Locale.US);
				mDateTitle = monthFormat.format(new Date());
				break;
			default:
				break;
			}
			mJsonStr = null;
		}
		mDateText.setText(mDateTitle);
		
		if (mJsonStr == null) {
			mProgressBar.setVisibility(View.VISIBLE);
			mTimePlot.setVisibility(View.INVISIBLE);
			mIndexPlot.setVisibility(View.INVISIBLE);

			String requestURL = Constants.BASE_SERVER_URL;
			if (mGraphViewTypePosition == 0) {
				if (mGraphTypePosition == 0) {
					requestURL += "getMobilityByHour?cid=" + Constants.getUUID(getActivity()) + "&";
				} else if (mGraphTypePosition == 1) {
					requestURL += "getUsageByHour?cid=" + Constants.getUUID(getActivity()) + "&";
				}
				requestURL += "date=";
				requestURL += mDateTitle;
			} else if (mGraphViewTypePosition == 1) {
				if (mGraphTypePosition == 0) {
					requestURL += "getMobilityByDay?cid=" + Constants.getUUID(getActivity()) + "&";
				} else if (mGraphTypePosition == 1) {
					requestURL += "getUsageByDay?cid=" + Constants.getUUID(getActivity()) + "&";
				}
				
				String[] dates = mDateTitle.split(" - ");
				requestURL += "endDate=";
				requestURL += dates[1];
				requestURL += "&days=7";
			} else if (mGraphViewTypePosition == 2) {
				if (mGraphTypePosition == 0) {
					requestURL += "getMobilityByDay?cid=" + Constants.getUUID(getActivity()) + "&";
				} else if (mGraphTypePosition == 1) {
					requestURL += "getUsageByDay?cid=" + Constants.getUUID(getActivity()) + "&";
				}
				
				SimpleDateFormat dayFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
				String[] segs = mDateTitle.split("/");
				int month = Integer.valueOf(segs[0]);
				int year = Integer.valueOf(segs[1]);
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, month - 1);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)); 
				requestURL += "endDate=";
				requestURL += dayFormat.format(cal.getTime());
				requestURL += "&days=";
				requestURL += cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			}
			Log.d(Constants.TAG, requestURL);
			
			new HttpRequestTask(getActivity(), this).execute(requestURL);
		} else if (mJsonStr.equals("")) {
			mJsonStr = null;
			
			Context context = getActivity();
			CharSequence text = "failed to get data from server! Either the phone is not connected to internet or the server is down.";
			Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
			
			XYSeries series1 = new SimpleXYSeries(Arrays.asList(new Number[] {0, 1}), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");
			XYSeries series2 = new SimpleXYSeries(Arrays.asList(new Number[] {0, 1}), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");
			MyBarFormatter formatter1 = new MyBarFormatter(Color.parseColor("#4571DA"), Color.LTGRAY);
			MyBarFormatter formatter2 = new MyBarFormatter(Color.parseColor("#4571DA"), Color.LTGRAY);
			mTimePlot.addSeries(series1, formatter1);
			mIndexPlot.addSeries(series2, formatter2);
		} else {
			drawGraph();
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.home, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Log.d(Constants.TAG, "In onItemSelected! Parent Id = " + parent.getId() + ", position = " + position);
		
		if (parent.getId() == mGraphViewTypeSpinner.getId() && position != mGraphViewTypePosition) {
			mDateTitle = null;
		}
		
		mGraphTypePosition = mGraphTypeSpinner.getSelectedItemPosition();
		mGraphViewTypePosition = mGraphViewTypeSpinner.getSelectedItemPosition();
		mJsonStr = null;
		onResume();
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
		Log.d(Constants.TAG, "In onSaveinstanceState");
		
		super.onSaveInstanceState(outState);
		outState.putInt("graphTypeSelected", mGraphTypePosition);
		outState.putInt("graphViewSelected", mGraphViewTypePosition);
		outState.putString("json", mJsonStr);
		outState.putString("title", mDateTitle);
		outState.putBoolean("graphExists", false);
	}
	

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public void onRequestTaskCompleted(String json) {
		if (json == null) {
			Log.d(Constants.TAG, "Get JSON null!");
			mJsonStr = "";
		} else {
			mJsonStr = json;
			//Log.d(Constants.TAG, mJsonStr);
		}
		mProgressBar.setVisibility(View.GONE);
		mTimePlot.setVisibility(View.VISIBLE);
		mIndexPlot.setVisibility(View.VISIBLE);
		onResume();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			((ActionBarActivity) getActivity()).getSupportActionBar().hide();
			mSelectGraphLayout.setVisibility(View.GONE);
			mSelectDateLayout.setVisibility(View.GONE);
			
			if (mGraphViewTypePosition == 0) {
				mTimePlot.setDomainBoundaries(0, 20, BoundaryMode.FIXED);
				mIndexPlot.setDomainBoundaries(0, 20, BoundaryMode.FIXED);
			} else if (mGraphViewTypePosition == 1) {
				mTimePlot.setDomainBoundaries(0, 6, BoundaryMode.FIXED);
				mIndexPlot.setDomainBoundaries(0, 6, BoundaryMode.FIXED);
			} else if (mGraphViewTypePosition == 2) {
				mTimePlot.setDomainBoundaries(0, 20, BoundaryMode.FIXED);
				mIndexPlot.setDomainBoundaries(0, 20, BoundaryMode.FIXED);
			}
			
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			((ActionBarActivity) getActivity()).getSupportActionBar().show();
			mSelectGraphLayout.setVisibility(View.VISIBLE);
			mSelectDateLayout.setVisibility(View.VISIBLE);
			
			if (mGraphViewTypePosition == 0) {
				mTimePlot.setDomainBoundaries(8, 18, BoundaryMode.FIXED);
				mIndexPlot.setDomainBoundaries(8, 18, BoundaryMode.FIXED);
			} else if (mGraphViewTypePosition == 1) {
				mTimePlot.setDomainBoundaries(0, 6, BoundaryMode.FIXED);
				mIndexPlot.setDomainBoundaries(0, 6, BoundaryMode.FIXED);
			} else if (mGraphViewTypePosition == 2) {
				mTimePlot.setDomainBoundaries(10, 20, BoundaryMode.FIXED);
				mIndexPlot.setDomainBoundaries(10, 20, BoundaryMode.FIXED);
			}
		}
	}
	
	private void drawGraph() {
		//Log.d(Constants.TAG, mJsonStr);
		try {
			JSONObject jsonObj = new JSONObject(this.mJsonStr);
			JSONObject timeObj = jsonObj.getJSONObject("time");
			JSONObject indexObj = jsonObj.getJSONObject("index");
			String timeTitle = timeObj.getString("title");
			String indexTitle = indexObj.getString("title");
			JSONArray timeData = timeObj.getJSONArray("data");
			JSONArray indexData = indexObj.getJSONArray("data");
			double maxTime = timeObj.getDouble("y_max"), minTime = timeObj.getDouble("y_min");
			if (maxTime == minTime) maxTime = minTime + 1.0;
			double maxIndex = indexObj.getDouble("y_max"), minIndex = timeObj.getDouble("y_min");
			if (maxIndex == minIndex) maxIndex = minIndex + 1.0;
			
			int timeSize = timeData.length();
			int indexSize = indexData.length();
			final String[] timeLabel = new String[timeSize];
			final String[] indexLabel = new String[indexSize];
			Number[] timeGraphData = new Number[timeSize];
			Number[] indexGraphData = new Number[indexSize];
			
			MyBarFormatter formatter1 = new MyBarFormatter(Color.parseColor("#4571DA"), Color.LTGRAY);
			MyBarFormatter formatter2 = new MyBarFormatter(Color.parseColor("#4571DA"), Color.LTGRAY);

			for (int i = 0; i < timeSize; i++) {
				JSONObject tmp = timeData.getJSONObject(i).getJSONObject("data");
				timeLabel[i] = tmp.getString("name");
				timeGraphData[i] = tmp.getDouble("value");
			}
			XYSeries timeSeries = new SimpleXYSeries(Arrays.asList(timeGraphData), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");
			formatter1.setPointLabelFormatter(new PointLabelFormatter());

			for (int i = 0; i < indexSize; i++) {
				JSONObject tmp = indexData.getJSONObject(i).getJSONObject("data");
				indexLabel[i] = tmp.getString("name");
				indexGraphData[i] = tmp.getDouble("value");
			}
			XYSeries indexSeries = new SimpleXYSeries(Arrays.asList(indexGraphData), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");
			formatter2.setPointLabelFormatter(new PointLabelFormatter());
			
			if (!mGraphExisting) {
				mTimePlot.addSeries(timeSeries, formatter1);
				mIndexPlot.addSeries(indexSeries, formatter2);
				
				setBoundaries(minTime, maxTime, minIndex, maxIndex);
				setDomainLabels();
				
				mTimePlot.setTicksPerRangeLabel(1);
				mTimePlot.setMarkupEnabled(false);
				mTimePlot.setTitle(timeTitle);
				mTimePlot.getGraphWidget().setDomainLabelOrientation(-45);
				mTimePlot.getGraphWidget().setRangeLabelOrientation(-45);
				mTimePlot.getGraphWidget().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
				MyBarRenderer timeRenderer = ((MyBarRenderer) mTimePlot.getRenderer(MyBarRenderer.class));
				timeRenderer.setBarWidthStyle(BarRenderer.BarWidthStyle.FIXED_WIDTH);
				timeRenderer.setBarWidth(50); 
				timeRenderer.setBarGap(20);
				
				mIndexPlot.setTicksPerRangeLabel(1);
				mIndexPlot.setMarkupEnabled(false);
				mIndexPlot.setTitle(indexTitle);
				mIndexPlot.getGraphWidget().setDomainLabelOrientation(-45);
				mIndexPlot.getGraphWidget().setRangeLabelOrientation(-45);
				mIndexPlot.getGraphWidget().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
				MyBarRenderer indexRenderer = ((MyBarRenderer) mIndexPlot.getRenderer(MyBarRenderer.class));
				indexRenderer.setBarWidthStyle(BarRenderer.BarWidthStyle.FIXED_WIDTH);
				indexRenderer.setBarWidth(50); 
				indexRenderer.setBarGap(20);
				
				mGraphExisting = true;
			} else {
				mTimePlot.clear();
				mIndexPlot.clear();
				
				mTimePlot.addSeries(timeSeries, formatter1);
				mTimePlot.setTitle(timeTitle);
				mIndexPlot.addSeries(indexSeries, formatter2);
				mIndexPlot.setTitle(indexTitle);
				
				setBoundaries(minTime, maxTime, minIndex, maxIndex);
				setDomainLabels();
				
				mTimePlot.redraw();
				mIndexPlot.redraw();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void setBoundaries(double min1, double max1, double min2, double max2) {
		mTimePlot.setRangeBoundaries(min1, max1, BoundaryMode.FIXED);
		mIndexPlot.setRangeBoundaries(min2, max2, BoundaryMode.FIXED); 
		if (mGraphViewTypePosition == 0) {
			mTimePlot.setDomainBoundaries(8, 18, BoundaryMode.FIXED);
			mIndexPlot.setDomainBoundaries(8, 18, BoundaryMode.FIXED);
		} else if (mGraphViewTypePosition == 1) {
			mTimePlot.setDomainBoundaries(0, 6, BoundaryMode.FIXED);
			mIndexPlot.setDomainBoundaries(0, 6, BoundaryMode.FIXED);
		} else if (mGraphViewTypePosition == 2) {
			mTimePlot.setDomainBoundaries(10, 20, BoundaryMode.FIXED);
			mIndexPlot.setDomainBoundaries(10, 20, BoundaryMode.FIXED);
		}
	}
	
	private void setDomainLabels() {
		String[] hourLabels = new String[]{"0h", "1h", "2h", "3h", "4h", "5h", "6h", "7h", "8h", "9h", "10h", "11h", "12h", 
				"13h", "14h", "15h", "16h", "17h", "18h", "19h", "20h", "21h", "22h", "23h"};
		String[] weekDayLabels = new String[]{"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
		String[] dayLabels = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", 
				"17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
		
		MyIndexFormat format = new MyIndexFormat();
		if (mGraphViewTypePosition == 0) {
			format.labels = hourLabels;
		} else if (mGraphViewTypePosition == 1) {
			format.labels = weekDayLabels;
		} else if (mGraphViewTypePosition == 2) {
			format.labels = dayLabels;
		}
		
		//mTimePlot.setRangeValueFormat(new DecimalFormat("0")); 
		mTimePlot.setDomainValueFormat(format);
		mTimePlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
		//mIndexPlot.setRangeValueFormat(new DecimalFormat("0"));
		mIndexPlot.setDomainValueFormat(format);
		mIndexPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
	}
	
}
