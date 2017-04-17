package com.jukuproject.jukutweet.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.PostQuizStatsFillintheBlankAdapter;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;

import java.util.ArrayList;

/**
 * Created by JClassic on 3/31/2017.
 */

public class StatsFragmentFillintheBlanks extends Fragment {

    String TAG = "Test-stats1";

    RecyclerView resultsRecycler;
    ArrayList<Tweet> mDataset;
    int mCorrect;
    int mTotal;

    LinearLayout topscoreLayout;
    TextView textScore;
    TextView textPercentage;


    public StatsFragmentFillintheBlanks() {}

    public static StatsFragmentFillintheBlanks newInstance(ArrayList<Tweet> dataset
            , int correct
            , int total) {
        StatsFragmentFillintheBlanks fragment = new StatsFragmentFillintheBlanks();
        Bundle args = new Bundle();
        args.putParcelableArrayList("dataset", dataset);
        args.putInt("correct",correct);
        args.putInt("total",total);
        fragment.setArguments(args);
        return  fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_stats_fillintheblanks, container, false);
        resultsRecycler = (RecyclerView) v.findViewById(R.id.my_recycler_view);
        topscoreLayout = (LinearLayout) v.findViewById(R.id.viewA);
        textScore = (TextView) v.findViewById(R.id.textViewFinalStatsScore);
        textPercentage = (TextView) v.findViewById(R.id.textViewFinalStatsPercentage);

        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            mDataset = savedInstanceState.getParcelableArrayList("mDataset");
            mCorrect = savedInstanceState.getInt("mCorrect");
            mTotal = savedInstanceState.getInt("mTotal");

        } else {
            mDataset = getArguments().getParcelableArrayList("dataset");
            mCorrect = getArguments().getInt("correct");
            mTotal = getArguments().getInt("total");
        }



        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        resultsRecycler.setLayoutManager(layoutManager);

        int adapterRowHeightMultiplier = Math.round((float) 10 * getResources().getDisplayMetrics().density);
        resultsRecycler.setAdapter(new PostQuizStatsFillintheBlankAdapter(getContext(), mDataset,adapterRowHeightMultiplier));

            try {
                ColorThresholds colorThresholds = SharedPrefManager.getInstance(getContext()).getColorThresholds();
                double percentage = 100*((double)mCorrect/(double)mTotal);
                if(percentage< (colorThresholds.getRedThreshold()*100)) {
                    topscoreLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorJukuRed));
                } else if (percentage< (colorThresholds.getYellowThreshold()*100)) {
                    topscoreLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_light));
                } else {
                    topscoreLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.holo_green_light));
                }

//                Log.d(TAG,"mCorrect: " + mCorrect);
//                Log.d(TAG,"mPercentage: " + percentage);
//
//                textScore.setText(mCorrect);
//                String txtpercentage =   String.valueOf(percentage) + "%";
//                textPercentage.setText(txtpercentage);
                //TODO replace with string vars
                textScore.setText(String.valueOf(mCorrect) + "/" + String.valueOf(mTotal));
                String txtpercentage =   String.valueOf((int)percentage) + "%";
                textPercentage.setText(txtpercentage);

            } catch (Exception e) {
                Log.e(TAG,"stats fragment multiple choice couldn't set top score bar " + e);
            }


    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("mDataset", mDataset);
        outState.putInt("mCorrect", mCorrect);
        outState.putInt("mTotal", mTotal);

    }



}
