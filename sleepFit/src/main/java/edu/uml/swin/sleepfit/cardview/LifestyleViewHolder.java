package edu.uml.swin.sleepfit.cardview;

import edu.uml.swin.sleepfit.R;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class LifestyleViewHolder extends ViewHolder {

	public ImageView mLifestyleImageView;
	public TextView mLifetyleNameText;
	public TextView mLifestyleTimeText;
	public TextView mLifestyleNoteText;
	public TextView mLifestyleSelectionText;
	
	public LifestyleViewHolder(View itemView) {
		super(itemView);
		
		mLifestyleImageView = (ImageView) itemView.findViewById(R.id.lifestyle_card_image);
		mLifetyleNameText = (TextView) itemView.findViewById(R.id.lifestyle_card_type_name);
		mLifestyleTimeText = (TextView) itemView.findViewById(R.id.lifestyle_card_time);
		mLifestyleNoteText = (TextView) itemView.findViewById(R.id.lifestyle_card_note);
		mLifestyleSelectionText = (TextView) itemView.findViewById(R.id.lifestyle_card_selection);
	}

}
