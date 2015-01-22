package edu.uml.swin.sleepfit;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import mirko.android.datetimepicker.date.DatePickerDialog;
import mirko.android.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import mirko.android.datetimepicker.time.RadialPickerLayout;
import mirko.android.datetimepicker.time.TimePickerDialog;
import mirko.android.datetimepicker.time.TimePickerDialog.OnTimeSetListener;
import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.DB.LifestyleRaw;
import edu.uml.swin.sleepfit.util.Constants;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class LifestyleFragment extends Fragment implements AdapterView.OnItemSelectedListener {
	private static final String ARG_SECTION_NUMBER = "section_number";
	
	private TextView mLifestyleTipText;
	private Spinner mLifestyleSpinner;
	private EditText mDatePicker;
	private EditText mTimePicker;
	private EditText mNoteText;
	private DatePickerDialog mDatePickerDialog;
	private TimePickerDialog mTimePickerDialog;
	private Spinner mSelectionSpinner;
	private Activity mActivity;
	
	private Calendar mCalendar;
	private Calendar mYesterday;
	private SimpleDateFormat month_date;
	private int mSelectedYear;
	private int mSelectedMonth;
	private int mSelectedDay;
	private int mSelectedHour;
	private int mSelectedMinute;
	private int mLifestylePosition;
	private int mSelectionPosition;
	private String[] mTips;
	private String[] mSelectionOptions;
	
	private DatabaseHelper mDatabaseHelper;
	private Dao<LifestyleRaw, Integer> mDao;
	
	public static LifestyleFragment newInstance(int sectionNumber) {
		LifestyleFragment fragment = new LifestyleFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}
	
	public LifestyleFragment() {
		mCalendar = Calendar.getInstance();
		mYesterday = Calendar.getInstance();
		mYesterday.add(Calendar.DAY_OF_YEAR, -1);
		month_date = new SimpleDateFormat("MMM", Locale.US);
		mSelectedHour = mCalendar.get(Calendar.HOUR_OF_DAY);
		mSelectedMinute = mCalendar.get(Calendar.MINUTE);
		mSelectedDay = mCalendar.get(Calendar.DAY_OF_MONTH);
		mSelectedMonth = mCalendar.get(Calendar.MONTH);
		mSelectedYear = mCalendar.get(Calendar.YEAR);
		mLifestylePosition = 0;
	}
	
	
	// Sequence of Fragment life cycle 
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mActivity = activity;
		if (activity instanceof MainActivity) {
			((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(Constants.TAG, "In onCreate of LifestyleFragment");
		
		mDatabaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
		try {
			mDao = mDatabaseHelper.getLifestyleRawDao();
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Cannot get the LightRaw DAO: " + e.toString());
			e.printStackTrace();
		}
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(Constants.TAG, "In onCreateView");
		
		View rootView = inflater.inflate(R.layout.fragment_lifestyle, container, false);

		mLifestyleTipText = (TextView) rootView.findViewById(R.id.lifestyle_tips_content);
		mLifestyleSpinner = (Spinner) rootView.findViewById(R.id.lifestyle_type_spinner);
		mLifestyleSpinner.setOnItemSelectedListener(this); 
		mDatePicker = (EditText) rootView.findViewById(R.id.date_picker);
		mTimePicker = (EditText) rootView.findViewById(R.id.time_picker);
		mSelectionSpinner = (Spinner) rootView.findViewById(R.id.lifestyle_selection_spinner);
		mSelectionSpinner.setOnItemSelectedListener(this);
		mNoteText = (EditText) rootView.findViewById(R.id.lifestyle_note); 

		mDatePicker.setText("Today");
		mTimePicker.setText(new StringBuilder().append(String.format("%02d", mSelectedHour))
				.append(" : ").append(String.format("%02d", mSelectedMinute)));
		
		mDatePickerDialog = DatePickerDialog.newInstance(new OnDateSetListener() {
			public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, month);
				cal.set(Calendar.DAY_OF_MONTH, day);
				
				if (mSelectedYear == year && mSelectedMonth == month && mSelectedDay == day) {
					mDatePicker.setText("Today");
				} else if (mYesterday.get(Calendar.YEAR) == year && mYesterday.get(Calendar.MONTH) == month && mYesterday.get(Calendar.DAY_OF_MONTH) == day) {
					mDatePicker.setText("Yesterday");
				} else {
					mDatePicker.setText(
						new StringBuilder().append(month_date.format(cal.getTime()))
						.append(" ").append(day).append(", ").append(year));
				}
				
				mSelectedYear = year;
				mSelectedMonth = month;
				mSelectedDay = day;
			}
		}, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
		
		
		mTimePickerDialog = TimePickerDialog.newInstance(new OnTimeSetListener() {
			@Override
			public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
				mTimePicker.setText(
						new StringBuilder().append(String.format("%02d", hourOfDay))
						.append(" : ").append(String.format("%02d", minute)));
				
				mSelectedHour = hourOfDay;
				mSelectedMinute = minute;
			}
		}, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true);
		
		mDatePicker.setOnClickListener(new OnClickListener() {
			private String tag;
			
			@Override
			public void onClick(View v) {
				mDatePickerDialog.show(getActivity().getFragmentManager(), tag);
			}
		});
		
		mTimePicker.setOnClickListener(new OnClickListener() {
			private String tag;
			
			@Override
			public void onClick(View v) {
				mTimePickerDialog.show(getActivity().getFragmentManager(), tag);
			}
		});

		mTips = getResources().getStringArray(R.array.lifestyle_tip_items);
		mLifestyleTipText.setText(mTips[0]);
		
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
	public void onPause() {
		super.onPause();
	}
	

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (parent.getId() == mLifestyleSpinner.getId() && mLifestylePosition != position) {
			switch (position) {
			case 0:
				mSelectionOptions = getResources().getStringArray(R.array.lifestyle_caffeine_selections);
				break;
			case 1:
				mSelectionOptions = getResources().getStringArray(R.array.lifestyle_food_selections);
				break;
			case 2:
				mSelectionOptions = getResources().getStringArray(R.array.lifestyle_cigarette_selections);
				break;
			case 3:
				mSelectionOptions = getResources().getStringArray(R.array.lifestyle_alcohol_selections);
				break;
			case 4:
				mSelectionOptions = getResources().getStringArray(R.array.lifestyle_exercise_selection);
				break;
			case 5:
				mSelectionOptions = getResources().getStringArray(R.array.lifestyle_napping_selection);
				break;
			case 6:
				mSelectionOptions = getResources().getStringArray(R.array.lifestyle_emotion_selection);
				break;
			case 7:
				mSelectionOptions = getResources().getStringArray(R.array.lifestyle_medication_selection);
				break;
			case 8:
				mSelectionOptions = getResources().getStringArray(R.array.lifestyle_fatigue_selection);
				break;
			default:
				break;
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mSelectionOptions);
			mSelectionSpinner.setAdapter(adapter);
		}
		
		mLifestylePosition = mLifestyleSpinner.getSelectedItemPosition();
		mSelectionPosition = mSelectionSpinner.getSelectedItemPosition();
		mLifestyleTipText.setText(mTips[mLifestylePosition]);
		Log.d(Constants.TAG, "Selected lifestyle position: " + mLifestylePosition + ", selection position: " + mSelectionPosition);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.d(Constants.TAG, "In lifestyle fragment onCreateOptionsMenu");
		
		inflater.inflate(R.menu.lifestyle, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.lifestyle_action_save:
				long createTime = System.currentTimeMillis();
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, mSelectedYear);
				cal.set(Calendar.MONTH, mSelectedMonth);
				cal.set(Calendar.DAY_OF_MONTH, mSelectedDay);
				cal.set(Calendar.HOUR_OF_DAY, mSelectedHour);
				cal.set(Calendar.MINUTE, mSelectedMinute);
				String[] lifestyles = getActivity().getResources().getStringArray(R.array.lifestyle_type_items);
				String note = mNoteText.getText().toString().trim();
				int ret = saveLifestyleLog(createTime, cal.getTimeInMillis(), lifestyles[mLifestylePosition], mLifestylePosition, mSelectionPosition, note);
				
				CharSequence text;
				if (ret >= 0) {
					text = "Save lifestyle log succeeded!";
				} else {
					text = "Save lifestyle log failed!";
				}
				Context context = getActivity();
				Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();
				
				if (mActivity instanceof NewLifestyleActivity) {
					mActivity.onBackPressed();
				} else {
					mLifestyleSpinner.setSelection(0);
					mSelectionSpinner.setSelection(0);
					mNoteText.setText("");
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private int saveLifestyleLog(long createTime, long when, String type, int typeId, int selection, String note) {
		LifestyleRaw newRaw = new LifestyleRaw(createTime, when, type, typeId, selection, note);
		
		try {
			mDao.create(newRaw);
		} catch (SQLException e) {
			Log.d(Constants.TAG, "Add new LifestyleRaw data record failed: " + e.toString());
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
}
