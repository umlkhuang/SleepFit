package edu.uml.swin.sleepfit.cardview;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;

import edu.uml.swin.sleepfit.R;
import edu.uml.swin.sleepfit.DB.DailyLog;
import edu.uml.swin.sleepfit.DB.DatabaseHelper;
import edu.uml.swin.sleepfit.util.Constants;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.ToggleButton;

public class DiaryViewHolder extends ViewHolder {
	
	private DatabaseHelper mDatabaseHelper;
	private Dao<DailyLog, Integer> mDailyLogDao;
	
	public RatingBar mStressRating;
	public RatingBar mDepressionRating;
	public RatingBar mFatigueRating;
	public RatingBar mSleepinessRating;
	public ToggleButton mToggleButton;
    public String trackDate;

	public DiaryViewHolder(View itemView) {
		super(itemView);
		
		mStressRating = (RatingBar) itemView.findViewById(R.id.stressRatingBar);
		mDepressionRating = (RatingBar) itemView.findViewById(R.id.depressionRatingBar);
		mFatigueRating = (RatingBar) itemView.findViewById(R.id.fatigueRatingBar);
		mSleepinessRating = (RatingBar) itemView.findViewById(R.id.sleepinessRatingBar);
		mToggleButton = (ToggleButton) itemView.findViewById(R.id.saveDiaryToggleButton);

		mDatabaseHelper = OpenHelperManager.getHelper(itemView.getContext(), DatabaseHelper.class);
		try {
			mDailyLogDao = mDatabaseHelper.getDailyLogDao();
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
	}

}
