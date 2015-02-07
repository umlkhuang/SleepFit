package edu.uml.swin.sleepfit.cardview;

import java.sql.SQLException;
import java.util.Date;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;

import edu.uml.swin.sleepfit.DB.SleepLogger;
import edu.uml.swin.sleepfit.R;
import edu.uml.swin.sleepfit.DB.DailyLog;
import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.util.Constants;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.ToggleButton;

public class DiaryViewHolder extends ViewHolder {
	
	private DatabaseHelper mDatabaseHelper;
	private Dao<DailyLog, Integer> mDailyLogDao;
    private Dao<SleepLogger, Integer> mSleepLogDao;
    private Context mContext;

    public EditText mNapTimeText;
	public RatingBar mStressRating;
	public RatingBar mDepressionRating;
	public RatingBar mFatigueRating;
	public RatingBar mSleepinessRating;
	public ToggleButton mToggleButton;
    public Button mSaveButton;
    public String trackDate;
    public CardView mCardView;
    public Date mCreateTime;

	public DiaryViewHolder(View itemView) {
		super(itemView);
        mContext = itemView.getContext();

        mCardView = (CardView) itemView.findViewById(R.id.card_layout);
        mNapTimeText = (EditText) itemView.findViewById(R.id.nap_time);
		mStressRating = (RatingBar) itemView.findViewById(R.id.stressRatingBar);
		mDepressionRating = (RatingBar) itemView.findViewById(R.id.depressionRatingBar);
		mFatigueRating = (RatingBar) itemView.findViewById(R.id.fatigueRatingBar);
		mSleepinessRating = (RatingBar) itemView.findViewById(R.id.sleepinessRatingBar);
		mToggleButton = (ToggleButton) itemView.findViewById(R.id.saveDiaryToggleButton);
        mSaveButton = (Button) itemView.findViewById(R.id.saveDiaryButton);

		mDatabaseHelper = OpenHelperManager.getHelper(itemView.getContext(), DatabaseHelper.class);
		try {
			mDailyLogDao = mDatabaseHelper.getDailyLogDao();
            mSleepLogDao = mDatabaseHelper.getSleepLoggerDao();
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Cannot get the LightRaw DAO: " + e.toString());
			e.printStackTrace();
		}
		
		mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mStressRating.setIsIndicator(false);
					mDepressionRating.setIsIndicator(false);
					mFatigueRating.setIsIndicator(false);
					mSleepinessRating.setIsIndicator(false);
				} else {
					//SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
					//String trackDate = dateFormatter.format(new Date(System.currentTimeMillis()));
					
					try {
						UpdateBuilder<DailyLog, Integer> updateBuilder = mDailyLogDao.updateBuilder();
						updateBuilder.where().eq("trackDate", trackDate);
						updateBuilder.updateColumnValue("stress", (int) mStressRating.getRating());
						updateBuilder.updateColumnValue("depression", (int) mDepressionRating.getRating());
						updateBuilder.updateColumnValue("fatigue", (int) mFatigueRating.getRating());
						updateBuilder.updateColumnValue("sleepiness", (int) mSleepinessRating.getRating());
						updateBuilder.updateColumnValue("uploaded", false);
						updateBuilder.update();
					} catch (SQLException e) {
						Log.d(Constants.TAG, "Update dailylog failed: " + e.toString());
						e.printStackTrace();
					} finally {
						mStressRating.setIsIndicator(true);
						mDepressionRating.setIsIndicator(true);
						mFatigueRating.setIsIndicator(true);
						mSleepinessRating.setIsIndicator(true);
					}
				}
			}
		});

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNapTimeText.getText().toString().equals("")) {
                    AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
                    ad.setMessage("The nap time cannot be empty!");
                    ad.setPositiveButton("OK", null);
                    ad.create().show();
                    return;
                }

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
                        try {
                            UpdateBuilder<DailyLog, Integer> updateBuilder = mDailyLogDao.updateBuilder();
                            updateBuilder.where().eq("trackDate", trackDate);
                            updateBuilder.updateColumnValue("stress", (int) mStressRating.getRating());
                            updateBuilder.updateColumnValue("depression", (int) mDepressionRating.getRating());
                            updateBuilder.updateColumnValue("fatigue", (int) mFatigueRating.getRating());
                            updateBuilder.updateColumnValue("sleepiness", (int) mSleepinessRating.getRating());
                            updateBuilder.update();
                        } catch (SQLException e) {
                            Log.d(Constants.TAG, "Update dailylog failed: " + e.toString());
                        }

                        try {
                            UpdateBuilder<SleepLogger, Integer> ub = mSleepLogDao.updateBuilder();
                            ub.where().eq("trackDate", trackDate);
                            ub.updateColumnValue("naptime", Integer.valueOf(mNapTimeText.getText().toString()));
                            ub.update();
                        } catch (SQLException e) {
                            Log.d(Constants.TAG, "Update sleeplogger failed: " + e.toString());
                        }

                        Intent msg = new Intent(Constants.UPDATED_SLEEP_INFO);
                        mContext.sendBroadcast(msg);
                    }
                });

                builder.create().show();
            }
        });
	}

}
