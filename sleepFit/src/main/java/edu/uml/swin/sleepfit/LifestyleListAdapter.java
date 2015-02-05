package edu.uml.swin.sleepfit;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class LifestyleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private OnItemClickListener mItemClickListener;
    private ArrayList<LifestyleItem> mLifestyleItems;

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public LifestyleListAdapter(Context context, ArrayList<LifestyleItem> lifestyleItems) {
        mContext = context;
        mLifestyleItems = lifestyleItems;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int idx) {
        LifestyleItem item = mLifestyleItems.get(idx);

        ((LifestyleItemViewHolder) viewHolder).mIconImage.setImageResource(item.getIcon());
        ((LifestyleItemViewHolder) viewHolder).mLifestyleType.setText(item.getType());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lifestyle_list_item, parent, false);
        return new LifestyleItemViewHolder(v, mItemClickListener);
    }

    @Override
    public int getItemCount() {
        return mLifestyleItems.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

}
