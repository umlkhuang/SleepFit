package edu.uml.swin.sleepfit;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import edu.uml.swin.sleepfit.util.Constants;

public class LifestyleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView mIconImage;
    public TextView mLifestyleType;
    public LifestyleListAdapter.OnItemClickListener mItemClickListener;

    public LifestyleItemViewHolder(View itemView, LifestyleListAdapter.OnItemClickListener listener) {
        super(itemView);

        mIconImage = (ImageView) itemView.findViewById(R.id.lifestyle_icon);
        mLifestyleType = (TextView) itemView.findViewById(R.id.lifestyle_type);

        mItemClickListener = listener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mItemClickListener != null) {
            Log.d(Constants.TAG, "Clicked lifestyle item: " + getPosition());
            mItemClickListener.onItemClick(v, getPosition());
        }
    }
}
