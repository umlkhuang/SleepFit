package edu.uml.swin.sleepfit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import edu.uml.swin.sleepfit.util.Constants;

public class LifestyleListFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private Context mContext;
    private RecyclerView mRecyclerView;
    private LifestyleListAdapter mAdapter;
    private ArrayList<LifestyleItem> mLifestyleItems;
    private static boolean mTypeTwo;

    public static LifestyleListFragment newInstance(int sectionNumber) {
        LifestyleListFragment fragment = new LifestyleListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        mTypeTwo = false;

        return fragment;
    }

    public static LifestyleListFragment newInstance() {
        LifestyleListFragment fragment = new LifestyleListFragment();
        mTypeTwo = true;

        return fragment;
    }

    public LifestyleListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lifestyle, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.lifestyle_fragment_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!mTypeTwo) {
            ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        }

        mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Constants.TAG, "In onCreate of LifestyleListFragment");

        mLifestyleItems = new ArrayList<LifestyleItem>();
        String[] typeNames = mContext.getResources().getStringArray(R.array.lifestyle_type_items);
        TypedArray typeIcons = mContext.getResources().obtainTypedArray(R.array.lifestyle_icons);
        int size = typeNames.length;
        for (int i = 0; i < size; i++) {
            LifestyleItem item = new LifestyleItem(typeNames[i], typeIcons.getResourceId(i, -1));
            mLifestyleItems.add(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(Constants.TAG, "In onResume, SleepHistoryFragment");

        mAdapter = new LifestyleListAdapter(mContext, mLifestyleItems);
        mAdapter.SetOnItemClickListener(new LifestyleListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putInt("lifestyleId", position);
                Intent intent = new Intent(mContext, LifestyleDetailActivity.class);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });

        mRecyclerView.setAdapter(mAdapter);
    }
}
