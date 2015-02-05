package edu.uml.swin.sleepfit.cardview;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import mirko.android.datetimepicker.time.RadialPickerLayout;
import mirko.android.datetimepicker.time.TimePickerDialog;
import mirko.android.datetimepicker.time.TimePickerDialog.OnTimeSetListener;
import edu.uml.swin.sleepfit.R;
import edu.uml.swin.sleepfit.util.Constants;

import android.app.ActionBar;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	
	private final int VIEWTYPECOUNT = 3;
	private Context mContext;
	private ArrayList<HomeCard> mCards;
	
	public HomeListAdapter(ArrayList<HomeCard> cards, Context context) {
		mContext = context;
		mCards = cards;
	}

	@Override
	public int getItemCount() {
		return mCards == null ? 0 : mCards.size();
	}

	@Override
	public void onBindViewHolder(final ViewHolder viewHolder, int idx) {
		HomeCard card = mCards.get(idx);

		if (idx == 0) {
            if (!card.mVisable) {
                ((SummaryViewHolder) viewHolder).mCardView.setVisibility(View.GONE);
                ((SummaryViewHolder) viewHolder).mCardView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
                return;
            }

			((SummaryViewHolder) viewHolder).mBedtimeText.setText(card.mBedtimeStr);
			((SummaryViewHolder) viewHolder).mOldBedtimeStr = card.mBedtimeStr;
			//((SummaryViewHolder) viewHolder).mBedtimeText.setEnabled(false);
			((SummaryViewHolder) viewHolder).mOldBedtime = card.mSleepTime;
			((SummaryViewHolder) viewHolder).mNewBedtime = card.mSleepTime;
			
			((SummaryViewHolder) viewHolder).mWaketimeText.setText(card.mWaketimeStr);
			((SummaryViewHolder) viewHolder).mOldWaketimeStr = card.mWaketimeStr;
			//((SummaryViewHolder) viewHolder).mWaketimeText.setEnabled(false);
			((SummaryViewHolder) viewHolder).mOldWaketime = card.mWakeTime;
			((SummaryViewHolder) viewHolder).mNewWaketime = card.mWakeTime;
			
			//SimpleDateFormat sleepFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
			int bedtimeHour, bedtimeMin, waketimeHour, waketimeMin;
			((SummaryViewHolder) viewHolder).mOldBedtimeStr = card.mBedtimeStr;
			//Log.d(Constants.TAG, "Sleep Time = " + card.mBedtimeStr);
			Calendar cal1 = Calendar.getInstance();
			if (((SummaryViewHolder) viewHolder).mOldBedtime != null) {
				cal1.setTime(((SummaryViewHolder) viewHolder).mOldBedtime);
			} else {
				cal1.setTimeInMillis(System.currentTimeMillis());
			}
			bedtimeHour = cal1.get(Calendar.HOUR_OF_DAY);
			bedtimeMin  = cal1.get(Calendar.MINUTE);
			
			((SummaryViewHolder) viewHolder).mOldWaketimeStr = card.mWaketimeStr;
			//Log.d(Constants.TAG, "Wakeup Time = " + card.mWaketimeStr);
			Calendar cal2 = Calendar.getInstance();
			if (((SummaryViewHolder) viewHolder).mOldWaketime != null) {
				cal2.setTime(((SummaryViewHolder) viewHolder).mOldWaketime);
			} else {
				cal2.setTimeInMillis(System.currentTimeMillis());
			}
			waketimeHour = cal2.get(Calendar.HOUR_OF_DAY);
			waketimeMin  = cal2.get(Calendar.MINUTE);
			
			((SummaryViewHolder) viewHolder).mBedtimePicker = TimePickerDialog.newInstance(new OnTimeSetListener() {
				@Override
				public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
					((SummaryViewHolder) viewHolder).mBedtimeSelectedHour = hourOfDay;
					((SummaryViewHolder) viewHolder).mBedtimeSleectedMinute = minute;
					
					SimpleDateFormat sleepFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
					Calendar cal = Calendar.getInstance();
					cal.setTime(((SummaryViewHolder) viewHolder).mOldBedtime);
					if (hourOfDay < 12 && cal.get(Calendar.HOUR_OF_DAY) > 12) {
						cal.add(Calendar.DAY_OF_YEAR, 1);
					} else if (hourOfDay > 12 && cal.get(Calendar.HOUR_OF_DAY) < 12) {
						cal.add(Calendar.DAY_OF_YEAR, -1);
					} 
					cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
					cal.set(Calendar.MINUTE, minute);
					((SummaryViewHolder) viewHolder).mBedtimeText.setText(sleepFormat.format(cal.getTime()));
					((SummaryViewHolder) viewHolder).mOldBedtimeStr = sleepFormat.format(cal.getTime());
					((SummaryViewHolder) viewHolder).mNewBedtime = cal.getTime();
					((SummaryViewHolder) viewHolder).mOldBedtime = cal.getTime();
					((SummaryViewHolder) viewHolder).mNewWaketime = ((SummaryViewHolder) viewHolder).mOldWaketime;
					
					// Update the sleep duration text 
					if (((SummaryViewHolder) viewHolder).mOldWaketime != null) {
						long duration = ((SummaryViewHolder) viewHolder).mOldWaketime.getTime() - cal.getTimeInMillis();
						int durationMins = (int) duration / 1000 / 60;
						int durationHour = durationMins / 60;
						durationMins = durationMins % 60;
						String durationStr = String.valueOf(durationHour) + " Hours " + String.valueOf(durationMins) + " Minutes";
						((SummaryViewHolder) viewHolder).mDurationText.setText(durationStr);
					} else {
						((SummaryViewHolder) viewHolder).mDurationText.setText("N/A");
					}
				}
			}, bedtimeHour, bedtimeMin, true);
			
			((SummaryViewHolder) viewHolder).mWaketimePicker = TimePickerDialog.newInstance(new OnTimeSetListener() {
				@Override
				public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
					((SummaryViewHolder) viewHolder).mWaketimeSelectedHour = hourOfDay;
					((SummaryViewHolder) viewHolder).mWaketimeSelectedMinute = minute;
					
					SimpleDateFormat sleepFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
					Calendar cal = Calendar.getInstance();
					cal.setTime(((SummaryViewHolder) viewHolder).mOldWaketime);
					cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
					cal.set(Calendar.MINUTE, minute);
					((SummaryViewHolder) viewHolder).mWaketimeText.setText(sleepFormat.format(cal.getTime()));
					((SummaryViewHolder) viewHolder).mOldWaketimeStr = sleepFormat.format(cal.getTime());
					((SummaryViewHolder) viewHolder).mNewWaketime = cal.getTime();
					((SummaryViewHolder) viewHolder).mOldWaketime = cal.getTime();
					((SummaryViewHolder) viewHolder).mNewBedtime = ((SummaryViewHolder) viewHolder).mOldBedtime;
					
					// Update the sleep duration text 
					if (((SummaryViewHolder) viewHolder).mOldBedtime != null) {
						long duration = cal.getTimeInMillis() - ((SummaryViewHolder) viewHolder).mOldBedtime.getTime();
						int durationMins = (int) duration / 1000 / 60;
						int durationHour = durationMins / 60;
						durationMins = durationMins % 60;
						String durationStr = String.valueOf(durationHour) + " Hours " + String.valueOf(durationMins) + " Minutes";
						((SummaryViewHolder) viewHolder).mDurationText.setText(durationStr);
					} else {
						((SummaryViewHolder) viewHolder).mDurationText.setText("N/A");
					}
				}
			}, waketimeHour, waketimeMin, true);
			
			((SummaryViewHolder) viewHolder).mDurationText.setText(card.mDurationStr);
			
			((SummaryViewHolder) viewHolder).mQualityRating.setRating(card.mQualityRate);
			//((SummaryViewHolder) viewHolder).mQualityRating.setIsIndicator(true);
			
			((SummaryViewHolder) viewHolder).mRestoredRating.setRating(card.mRestoredRate);
			//((SummaryViewHolder) viewHolder).mRestoredRating.setIsIndicator(true);
			
			((SummaryViewHolder) viewHolder).mSaveButton.setChecked(false);
			
			((SummaryViewHolder) viewHolder).mSleepDebt = ((SummaryViewHolder) viewHolder).getSleepDebt();
			((SummaryViewHolder) viewHolder).updateSleepDebt();
		} else if (idx == 1) {
            if (!card.mVisable) {
                ((DiaryViewHolder) viewHolder).mCardView.setVisibility(View.GONE);
                ((DiaryViewHolder) viewHolder).mCardView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
                return;
            }

            if (card.mNapTime > 0) {
                ((DiaryViewHolder) viewHolder).mNapTimeText.setText(String.valueOf(card.mNapTime));
            } else {
                ((DiaryViewHolder) viewHolder).mNapTimeText.setText("0");
            }

			((DiaryViewHolder) viewHolder).mStressRating.setRating(card.mStressRate);
			//((DiaryViewHolder) viewHolder).mStressRating.setIsIndicator(true);
			
			((DiaryViewHolder) viewHolder).mDepressionRating.setRating(card.mDepressionRate);
			//((DiaryViewHolder) viewHolder).mDepressionRating.setIsIndicator(true);
			
			((DiaryViewHolder) viewHolder).mFatigueRating.setRating(card.mFatigueRate);
			//((DiaryViewHolder) viewHolder).mFatigueRating.setIsIndicator(true);
			
			((DiaryViewHolder) viewHolder).mSleepinessRating.setRating(card.mSleepinessRate);
			//((DiaryViewHolder) viewHolder).mSleepinessRating.setIsIndicator(true);
			
			//((DiaryViewHolder) viewHolder).mToggleButton.setChecked(false);
            ((DiaryViewHolder) viewHolder).trackDate = card.mTrackDate;
		} else {
			((LifestyleViewHolder) viewHolder).mLifestyleImageView.setImageResource(card.mLifestyleIconId);
			((LifestyleViewHolder) viewHolder).mLifetyleNameText.setText(card.mLifestyleType);
			SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
			((LifestyleViewHolder) viewHolder).mLifestyleTimeText.setText(dateFormatter.format(card.mLifestyleWhen));
			if (!card.mLifestyleNote.equals(""))
				((LifestyleViewHolder) viewHolder).mLifestyleNoteText.setText(card.mLifestyleNote);
			else
				((LifestyleViewHolder) viewHolder).mLifestyleNoteText.setText("N/A");
			
			String[] mSelectionOptions = null;
			switch (card.mLifestyleTypeId) {
			case 0:
				mSelectionOptions = mContext.getResources().getStringArray(R.array.lifestyle_caffeine_selections);
				break;
			case 1:
				mSelectionOptions = mContext.getResources().getStringArray(R.array.lifestyle_food_selections);
				break;
			case 2:
				mSelectionOptions = mContext.getResources().getStringArray(R.array.lifestyle_cigarette_selections);
				break;
			case 3:
				mSelectionOptions = mContext.getResources().getStringArray(R.array.lifestyle_alcohol_selections);
				break;
			case 4:
				mSelectionOptions = mContext.getResources().getStringArray(R.array.lifestyle_exercise_selection);
				break;
			case 5:
                mSelectionOptions = mContext.getResources().getStringArray(R.array.lifestyle_medication_selection);
				break;
			case 6:
                mSelectionOptions = mContext.getResources().getStringArray(R.array.lifestyle_napping_selection);
				break;
			case 7:
                mSelectionOptions = mContext.getResources().getStringArray(R.array.lifestyle_emotion_selection);
				break;
			case 8:
                mSelectionOptions = mContext.getResources().getStringArray(R.array.lifestyle_fatigue_selection);
				break;
			default:
				break;
			}
			((LifestyleViewHolder) viewHolder).mLifestyleSelectionText.setText(mSelectionOptions[card.mLifestyleSelection]);
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int idx) {
		if (idx == 0) {
			View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.home_summary_card, viewGroup, false);
			return new SummaryViewHolder(v);
		} else if (idx == 1) {
			View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.home_dailylog_card, viewGroup, false);
			return new DiaryViewHolder(v);
		} else {
			View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.home_lifestyle_card, viewGroup, false);
			return new LifestyleViewHolder(v);
		}
	}
	
	@Override
	public int getItemViewType(int position) {
		int type;
		if (position == 0) type = 0;
		else if (position == 1) type = 1;
		else type = 2;
		
		return type;
	}
	
}
