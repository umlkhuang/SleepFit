package edu.uml.swin.sleepfit.cardview;

import java.util.ArrayList;
import java.util.List;

import edu.uml.swin.sleepfit.R;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HistoryListAdapter extends Adapter<RecyclerView.ViewHolder> {

	private Context mContext;
	private ArrayList<HistoryCard> mCards;
	private OnItemClickListener mItemClickListener;
	
	public interface OnItemClickListener {
		public void onItemClick(View view, int position);
	}
	
	public HistoryListAdapter(ArrayList<HistoryCard> cards, Context context) {
		mContext = context;
		mCards = cards;
	}
	
	@Override
	public int getItemCount() {
		return mCards == null ? 0 : mCards.size();
	}

	@Override
	public void onBindViewHolder(final ViewHolder viewHolder, int idx) {
		HistoryCard card = mCards.get(idx);
		
		((HistoryCardViewHolder) viewHolder).mDateText.setText(card.mDate);
		((HistoryCardViewHolder) viewHolder).mSleepText.setText(card.mSleepTimeStr + " - " + card.mWakeTimeStr);
		((HistoryCardViewHolder) viewHolder).mDurationText.setText(card.mDurationStr);
		if (card.mSleepDebt < 0) {
			((HistoryCardViewHolder) viewHolder).mDebtText.setTextColor(Color.parseColor("#CC0000"));
		} else {
			((HistoryCardViewHolder) viewHolder).mDebtText.setTextColor(Color.parseColor("#339933"));
		}
		((HistoryCardViewHolder) viewHolder).mDebtText.setText(card.mSleepDebtStr);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int idx) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sleep_history_card, viewGroup, false);
		return new HistoryCardViewHolder(v, mItemClickListener);
	} 

	public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
		this.mItemClickListener = mItemClickListener;
	}
}
