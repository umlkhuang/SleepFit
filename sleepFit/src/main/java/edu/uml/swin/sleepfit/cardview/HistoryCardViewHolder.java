package edu.uml.swin.sleepfit.cardview;

import edu.uml.swin.sleepfit.R;
import edu.uml.swin.sleepfit.cardview.HistoryListAdapter.OnItemClickListener;
import edu.uml.swin.sleepfit.util.Constants;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class HistoryCardViewHolder extends ViewHolder implements View.OnClickListener {
	
	public TextView mSleepText;
	public TextView mDurationText;
	public TextView mDebtText;
	public TextView mDateText;
	public OnItemClickListener mItemClickListener;

	public HistoryCardViewHolder(View itemView, OnItemClickListener listener) {
		super(itemView);
		
		mSleepText = (TextView) itemView.findViewById(R.id.history_sleep_text);
		mDurationText = (TextView) itemView.findViewById(R.id.history_duration_text);
		mDebtText = (TextView) itemView.findViewById(R.id.history_debt_text);
		mDateText = (TextView) itemView.findViewById(R.id.history_date_text);
		
		mItemClickListener = listener;
		itemView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (mItemClickListener != null) {
			Log.d(Constants.TAG, "Clicked sleep history item: " + getPosition());
			mItemClickListener.onItemClick(v, getPosition());
		}
	}

}
