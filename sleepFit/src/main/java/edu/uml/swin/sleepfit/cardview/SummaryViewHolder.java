package edu.uml.swin.sleepfit.cardview;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mirko.android.datetimepicker.time.TimePickerDialog;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import edu.uml.swin.sleepfit.R;
import edu.uml.swin.sleepfit.DB.DailyLog;
import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.DB.SleepLogger;
import edu.uml.swin.sleepfit.util.Constants;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SummaryViewHolder extends ViewHolder {
	
	private DatabaseHelper mDatabaseHelper;
	private Dao<DailyLog, Integer> mDailyLogDao;
	private Dao<SleepLogger, Integer> mSleepLogDao;
	private Context mContext;
	
	public String mOldBedtimeStr;
	public String mOldWaketimeStr;
	public TimePickerDialog mBedtimePicker;
	public TimePickerDialog mWaketimePicker;
	public Date mNewBedtime;
	public Date mNewWaketime;
	public Date mOldBedtime;
	public Date mOldWaketime;
	public int mBedtimeSelectedHour;
	public int mBedtimeSleectedMinute;
	public int mWaketimeSelectedHour;
	public int mWaketimeSelectedMinute;
	public String mSleepDebtStr;
	public float mSleepDebt;
	
	public ProgressBar mProgressBar;
	public TextView mSleepdebtText;
	public EditText mBedtimeText;
	public EditText mWaketimeText;
	public TextView mDurationText;
	public RatingBar mQualityRating;
	public RatingBar mRestoredRating;
	public ToggleButton mSaveButton;
    public Button mSaveNormalButton;
    public CardView mCardView;

	private static float MAX_ABS_SLEEP_DEBT = 8;
	private static int DAYS_NEEDED_FOR_SLEEP_DEBT = 7;

	public SummaryViewHolder(View itemView) {
		super(itemView);
		mContext = itemView.getContext();

        mCardView = (CardView) itemView.findViewById(R.id.card_layout);
		mProgressBar = (ProgressBar) itemView.findViewById(R.id.sleep_debt_progressbar);
		mSleepdebtText = (TextView) itemView.findViewById(R.id.sleepdebt_info);
		mBedtimeText = (EditText) itemView.findViewById(R.id.bedtimeText);
		mWaketimeText = (EditText) itemView.findViewById(R.id.waketimeText);
		mDurationText = (TextView) itemView.findViewById(R.id.durationText);
		mQualityRating = (RatingBar) itemView.findViewById(R.id.qualityRatingBar);
		mRestoredRating = (RatingBar) itemView.findViewById(R.id.restoredRatingBar); 
		mSaveButton = (ToggleButton) itemView.findViewById(R.id.saveMorningDiaryToggleButton);
        mSaveNormalButton = (Button) itemView.findViewById(R.id.saveMorningDiaryButton);
		
		mDatabaseHelper = OpenHelperManager.getHelper(itemView.getContext(), DatabaseHelper.class);
		try {
			mDailyLogDao = mDatabaseHelper.getDailyLogDao();
			mSleepLogDao = mDatabaseHelper.getSleepLoggerDao();
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Cannot get dailylog or sleeplogger DAO in SummaryViewHolder(): " + e.toString());
			e.printStackTrace();
		}
		
		mSaveButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mBedtimeText.setEnabled(true);
					mWaketimeText.setEnabled(true);
					mQualityRating.setIsIndicator(false);
					mRestoredRating.setIsIndicator(false); 
					
					mOldBedtimeStr = mBedtimeText.getText().toString();
					mOldWaketimeStr = mWaketimeText.getText().toString();
				} else {
					SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
					String trackDate = dateFormatter.format(mNewWaketime);
					
					SleepLogger newSleep = new SleepLogger(System.currentTimeMillis(), trackDate, mNewBedtime, mNewWaketime, 0, 0, true, false);
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
						try {
							mSleepLogDao.create(newSleep);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					} else {
						try {
							UpdateBuilder<SleepLogger, Integer> ub = mSleepLogDao.updateBuilder();
							ub.where().eq("trackDate", trackDate);
							ub.updateColumnValue("sleepTime", mNewBedtime);
							ub.updateColumnValue("wakeupTime", mNewWaketime);
							ub.updateColumnValue("finished", true);
							ub.updateColumnValue("uploaded", false);
							ub.update();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					
					try {
						UpdateBuilder<DailyLog, Integer> updateBuilder = mDailyLogDao.updateBuilder();
						updateBuilder.where().eq("trackDate", trackDate);
						updateBuilder.updateColumnValue("quality", (int) mQualityRating.getRating());
						updateBuilder.updateColumnValue("restored", (int) mRestoredRating.getRating());
						updateBuilder.updateColumnValue("uploaded", false);
						updateBuilder.update();
					} catch (SQLException e) {
						Log.d(Constants.TAG, "Update dailylog failed: " + e.toString());
						e.printStackTrace();
					}
					
					mBedtimeText.setEnabled(false);
					mWaketimeText.setEnabled(false);
					mQualityRating.setIsIndicator(true);
					mRestoredRating.setIsIndicator(true); 
					
					mSleepDebt = getSleepDebt();
					updateSleepDebt();

                    Intent msg = new Intent(Constants.UPDATED_SLEEP_INFO);
                    mContext.sendBroadcast(msg);
                }
			}
		});

        mSaveNormalButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you sure to save the change?");
                builder.setCancelable(false);
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                        String trackDate = dateFormatter.format(mNewWaketime);

                        SleepLogger newSleep = new SleepLogger(System.currentTimeMillis(), trackDate, mNewBedtime, mNewWaketime, 0, 0, true, false);
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
                            try {
                                mSleepLogDao.create(newSleep);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                UpdateBuilder<SleepLogger, Integer> ub = mSleepLogDao.updateBuilder();
                                ub.where().eq("trackDate", trackDate);
                                ub.updateColumnValue("sleepTime", mNewBedtime);
                                ub.updateColumnValue("wakeupTime", mNewWaketime);
                                ub.updateColumnValue("finished", true);
                                ub.updateColumnValue("uploaded", false);
                                ub.update();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }

                        try {
                            UpdateBuilder<DailyLog, Integer> updateBuilder = mDailyLogDao.updateBuilder();
                            updateBuilder.where().eq("trackDate", trackDate);
                            updateBuilder.updateColumnValue("quality", (int) mQualityRating.getRating());
                            updateBuilder.updateColumnValue("restored", (int) mRestoredRating.getRating());
                            updateBuilder.updateColumnValue("uploaded", false);
                            updateBuilder.update();
                        } catch (SQLException e) {
                            Log.d(Constants.TAG, "Update dailylog failed: " + e.toString());
                            e.printStackTrace();
                        }

                        mBedtimeText.setEnabled(false);
                        mWaketimeText.setEnabled(false);
                        mQualityRating.setIsIndicator(true);
                        mRestoredRating.setIsIndicator(true);

                        mSleepDebt = getSleepDebt();
                        updateSleepDebt();

                        Intent msg = new Intent(Constants.UPDATED_SLEEP_INFO);
                        mContext.sendBroadcast(msg);
                    }
                });

                builder.create().show();
            }
        });
		
		mBedtimeText.setOnClickListener(new OnClickListener() {
			private String tag;
			
			@Override
			public void onClick(View v) {
				mBedtimePicker.show(((Activity)mContext).getFragmentManager(), tag);
			}
		});
		
		mWaketimeText.setOnClickListener(new OnClickListener() {
			private String tag;
			
			@Override
			public void onClick(View v) {
				mWaketimePicker.show(((Activity)mContext).getFragmentManager(), tag);
			}
		});
	}

	public float getSleepDebt() {
		float sleepDebt = 0;
		float sleepHours; 
		float wakeSleepRatio;
		
		SharedPreferences preferences = mContext.getSharedPreferences(Constants.SURVEY_FILE_NAME, Context.MODE_PRIVATE);
		sleepHours = Float.valueOf(preferences.getString("sleepHours", "8")); 
		wakeSleepRatio = (float) ((24.0 - sleepHours) / sleepHours);
		
		Calendar calEnd = Calendar.getInstance();
        if (mNewWaketime != null) {
            calEnd.setTime(mNewWaketime);
        }
		calEnd.set(Calendar.HOUR_OF_DAY, 15);
		
		Calendar calStart = Calendar.getInstance();
        if (mNewWaketime != null) {
            calStart.setTime(mNewWaketime);
        }
		calStart.add(Calendar.DAY_OF_YEAR, -1 * (DAYS_NEEDED_FOR_SLEEP_DEBT - 1));
		calStart.set(Calendar.HOUR_OF_DAY, 1);
		
		List<SleepLogger> sleeps = null;
		QueryBuilder<SleepLogger, Integer> qb = mSleepLogDao.queryBuilder();
		try {
			qb.where().ge("wakeupTime", calStart.getTime())
					.and().le("wakeupTime", calEnd.getTime());
			sleeps = mSleepLogDao.query(qb.prepare());
		} catch (SQLException e) {
			Log.e(Constants.TAG, e.toString());
			return sleepDebt;
		} 
		
		if (sleeps != null && sleeps.size() > 0) {
			for (int i = 0; i < sleeps.size(); i++) {
				float duration = (float) (sleeps.get(i).getWakeupTime().getTime() - sleeps.get(i).getSleepTime().getTime()) / 1000 / 60;
                duration = (duration + (float) sleeps.get(i).getNaptime()) / 60;
				float needSleepHours = (24 - duration) / wakeSleepRatio;
				float tmpDebt = duration - needSleepHours; 
				//Log.d(Constants.TAG, "Ratio: " + wakeSleepRatio + ", Needed sleep: " + needSleepHours + ", duration: " + duration);
				sleepDebt += tmpDebt;
				//Log.d(Constants.TAG, "Night: " + sleeps.get(i).getSleepTime() + ", sleep debt = " + tmpDebt);
			} 
		}
		
		return sleepDebt;
	}
	
	public void updateSleepDebt() {
		float absSleepDebt = Math.abs(mSleepDebt);
		float portion;
		if (absSleepDebt >= MAX_ABS_SLEEP_DEBT) {
			portion = 1;
		} else {
			portion = Math.abs(mSleepDebt) / MAX_ABS_SLEEP_DEBT;
		}
		//String sleepDebtStr = String.format("%.2f", mSleepDebt) + " Hours";
		//mSleepDebtStr = sleepDebtStr;
		int durationMinutes = (int) (absSleepDebt * 60);
		if (mSleepDebt < 0) {
			mProgressBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.red_sleepdebt_progressbar));
			mSleepdebtText.setTextColor(Color.parseColor("#CC0000"));
			mSleepDebtStr = "-" + String.valueOf(durationMinutes / 60) + " Hours " + String.valueOf(durationMinutes % 60) + " Minutes";
		} else {
			mProgressBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.green_sleepdebt_progressbar));
			mSleepdebtText.setTextColor(Color.parseColor("#339933"));
			mSleepDebtStr = String.valueOf(durationMinutes / 60) + " Hours " + String.valueOf(durationMinutes % 60) + " Minutes";
		}
		mSleepdebtText.setText(mSleepDebtStr);
		mProgressBar.setProgress((int) (portion * 100));
	}
}
