package edu.uml.swin.sleepfit.cardview;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.uml.swin.sleepfit.R;
import edu.uml.swin.sleepfit.DB.DailyLog;
import edu.uml.swin.sleepfit.DB.LifestyleRaw;
import edu.uml.swin.sleepfit.DB.SleepLogger;
import android.content.Context;
import android.content.res.TypedArray;

public class HomeCard {
	// Fields for lifestyle card 
	public String mLifestyleType;
	public int mLifestyleTypeId;
	public Date mLifestyleWhen;
	public String mLifestyleNote;
	public TypedArray mLifestyleIcons;
	public int mLifestyleIconId;
	public int mLifestyleSelection;
	public Context mContext;
	
	// Fields for DailyLog card
	public int mStressRate;
	public int mDepressionRate;
	public int mFatigueRate;
	public int mSleepinessRate;
	
	// Fields for summary card 
	public String mBedtimeStr;
	public String mWaketimeStr;
	public String mDurationStr;
	public int mQualityRate;
	public int mRestoredRate;
	public Date mSleepTime;
	public Date mWakeTime;
    public String mTrackDate;
	
	public HomeCard(Context context, LifestyleRaw lifestyleRaw) {
		mContext = context;
		
		mLifestyleType = lifestyleRaw.getType();
		mLifestyleTypeId = lifestyleRaw.getTypeId();
		mLifestyleWhen = lifestyleRaw.getLogTime();
		mLifestyleNote = lifestyleRaw.getNote();
		mLifestyleSelection = lifestyleRaw.getSelection();
		
		mLifestyleIcons = mContext.getResources().obtainTypedArray(R.array.lifestyle_icons);
		mLifestyleIconId = mLifestyleIcons.getResourceId(mLifestyleTypeId, -1);
		mLifestyleIcons.recycle();
	}
	
	public HomeCard(Context context, DailyLog dailyLog) {
		mContext = context;
		
		mStressRate = dailyLog.getStress();
		mDepressionRate = dailyLog.getDepression();
		mFatigueRate = dailyLog.getFatigue();
		mSleepinessRate = dailyLog.getSleepiness();
        mTrackDate = dailyLog.getTrackDate();
	}
	
	public HomeCard(Context context, SleepLogger sleepLog, DailyLog dailyLog) {
		mContext = context;
		
		if (sleepLog == null) {
			mBedtimeStr = "N/A";
			mWaketimeStr = "N/A";
			mDurationStr = "N/A";
		} else {
			mSleepTime = sleepLog.getSleepTime();
			mWakeTime = sleepLog.getWakeupTime();
			
			SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
			if (sleepLog.getSleepTime() == null) {
				// Something is wrong 
				mBedtimeStr = "N/A";
				mWaketimeStr = "N/A";
				mDurationStr = "N/A";
				mQualityRate = 0;
				mRestoredRate = 0;
				return;
			} else {
				mBedtimeStr = dateFormatter.format(sleepLog.getSleepTime());
			}
			
			if (sleepLog.getWakeupTime() == null) {
				// Something is wrong 
				mWaketimeStr = "N/A";
				mWaketimeStr = "N/A";
				mDurationStr = "N/A";
				mQualityRate = 0;
				mRestoredRate = 0;
				return;
			} else {
				mWaketimeStr = dateFormatter.format(sleepLog.getWakeupTime());
			}
			long duration = sleepLog.getWakeupTime().getTime() - sleepLog.getSleepTime().getTime();
			int durationMins = (int) duration / 1000 / 60;
			int durationHour = durationMins / 60;
			durationMins = durationMins % 60;
			mDurationStr = String.valueOf(durationHour) + " Hours " + String.valueOf(durationMins) + " Minutes";
		}
		mQualityRate = dailyLog.getQuality();
		mRestoredRate = dailyLog.getRestored();
        mTrackDate = dailyLog.getTrackDate();
	}
	
	public HomeCard() {
		
	}
	
}
