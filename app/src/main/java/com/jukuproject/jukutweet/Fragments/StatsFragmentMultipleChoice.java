package com.jukuproject.jukutweet.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.PostQuizStatsAdapter;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MultChoiceResult;
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Created by JClassic on 3/31/2017.
 */

public class StatsFragmentMultipleChoice extends Fragment {

    String TAG = "Test-stats1";

    ListView resultslistView;
    ArrayList<MultChoiceResult> mDataset;
    String mQuizType;
    boolean mIsHighScore;
    boolean mIsWordBuilder;
    Integer mWordBuilderScore;
    int mCorrect;
    int mTotal;

//    public View mainView;

    public StatsFragmentMultipleChoice() {}

    public static StatsFragmentMultipleChoice newInstance(ArrayList<MultChoiceResult> dataset
            , String quizType
            , boolean isWordBuilder
            , boolean isHighScore
            , Integer wordbuilderScore
            , int correct
            , int total) {
        StatsFragmentMultipleChoice fragment = new StatsFragmentMultipleChoice();
        Bundle args = new Bundle();
        args.putParcelableArrayList("dataset", dataset);
        args.putString("quizType",quizType);
        args.putBoolean("mIsWordBuilder",isWordBuilder);
        args.putBoolean("mIsHighScore",isHighScore);
        args.putInt("wordbuilderScore",wordbuilderScore);
        args.putInt("correct",correct);
        args.putInt("total",total);
        fragment.setArguments(args);
        return  fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG,"Creating stats view");
        //Set input global data
        mDataset = getArguments().getParcelableArrayList("dataset");
        mQuizType = getArguments().getString("quizType");
        mIsWordBuilder = getArguments().getBoolean("mIsWordBuilder");
        mIsHighScore = getArguments().getBoolean("mIsHighScore");
        mWordBuilderScore =getArguments().getInt("wordbuilderScore");
        mCorrect =getArguments().getInt("correct");
        mTotal =getArguments().getInt("total");

        View v = inflater.inflate(R.layout.fragment_stats_multiplechoice, container, false);
        resultslistView = (ListView) v.findViewById(R.id.listResults);
        resultslistView.setAdapter(new PostQuizStatsAdapter(getContext(), mDataset,mIsWordBuilder));

        LinearLayout topscoreLayout = (LinearLayout) v.findViewById(R.id.viewA);
        TextView textScore = (TextView) v.findViewById(R.id.textViewFinalStatsScore);
        TextView textPercentage = (TextView) v.findViewById(R.id.textViewFinalStatsPercentage);

        //TODO replace with string vars
        if(mQuizType != null && mQuizType.equals("WordBuilder")) {
            if(mIsHighScore) {
                topscoreLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.holo_green_light));
                textScore.setText("High Score! " + mWordBuilderScore);
            } else {
                topscoreLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_light));
                textScore.setText("Total: " + mWordBuilderScore);
            }
            if(mDataset!=null) {
                textPercentage.setText("Unique: " + mDataset.size());
            }

        } else {

            try {
                ColorThresholds colorThresholds = SharedPrefManager.getInstance(getContext()).getColorThresholds();
                double percentage = (double)mCorrect/(double)mTotal;
                if(percentage< (colorThresholds.getRedThreshold()*100)) {
                    topscoreLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorJukuRed));
                } else if (percentage< (colorThresholds.getYellowThreshold()*100)) {
                    topscoreLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_light));
                } else {
                    topscoreLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.holo_green_light));
                }

                textScore.setText(mCorrect);
                String txtpercentage =   String.valueOf(percentage) + "%";
                textPercentage.setText(txtpercentage);


            } catch (Exception e) {
                Log.e(TAG,"stats fragment multiple choice couldn't set top score bar " + e);
            }



        }

        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }



}
