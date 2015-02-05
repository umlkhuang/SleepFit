package edu.uml.swin.sleepfit.cardview;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import edu.uml.swin.sleepfit.DB.SleepLogger;

public class HistoryCard {

	public Date mSleepTime;
	public Date mWakeTime;
	public String mSleepTimeStr;
	public String mWakeTimeStr;
	public String mDurationStr;
	public String mSleepDebtStr;
	public String mDate;
	public float mSleepDebt;
	
	private float mWakeSleepRatio;
	
	public HistoryCard(SleepLogger sleepLog, float wakeSleepRatio) {
		mWakeSleepRatio = wakeSleepRatio;
		
		mSleepTime = sleepLog.getSleepTime();
		mWakeTime = sleepLog.getWakeupTime();
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd HH:mm", Locale.US);
        if (mSleepTime != null) {
            mSleepTimeStr = dateFormatter.format(mSleepTime);
        } else {
            mSleepTimeStr = "N/A";
        }
        if (mWakeTime != null) {
            mWakeTimeStr = dateFormatter.format(mWakeTime);
            mDate = new SimpleDateFormat("MMM. dd, yyyy", Locale.US).format(mWakeTime);
        } else {
            mWakeTimeStr = "N/A";
            mDate = new SimpleDateFormat("MMM. dd, yyyy", Locale.US).format(new Date());
        }

        if (mSleepTime != null && mWakeTime != null) {
            long duration = sleepLog.getWakeupTime().getTime() - sleepLog.getSleepTime().getTime();
            int durationMinutes = (int) duration / 1000 / 60;
            int durationHour = durationMinutes / 60;
            durationMinutes = durationMinutes % 60;
            mDurationStr = String.valueOf(durationHour) + " Hours " + String.valueOf(durationMinutes) + " Minutes";

            duration += sleepLog.getNaptime() * 60 * 1000;
            float sleepTimeHours = (float) duration / 1000 / 60 / 60;
            float neededSleep = (24 - sleepTimeHours) / mWakeSleepRatio;
            mSleepDebt = sleepTimeHours - neededSleep;
            int sleepDebtMinutes = (int) (Math.abs(mSleepDebt) * 60);
            if (mSleepDebt < 0) {
                mSleepDebtStr = "-" + String.valueOf(sleepDebtMinutes / 60) + " Hours " + String.valueOf(sleepDebtMinutes % 60) + " Minutes";
            } else {
                mSleepDebtStr = String.valueOf(sleepDebtMinutes / 60) + " Hours " + String.valueOf(sleepDebtMinutes % 60) + " Minutes";
            }
        } else {
            mDurationStr = "No sleep data available";
            mSleepDebtStr = "No sleep data available";
        }
	}
	
}
