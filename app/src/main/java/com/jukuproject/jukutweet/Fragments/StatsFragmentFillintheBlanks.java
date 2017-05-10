package com.jukuproject.jukutweet.Fragments;

import android.os.Bundle;
import android.os.SystemClock;
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
import com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Interfaces.WordEntryFavoritesChangedListener;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;

import java.util.ArrayList;

import rx.functions.Action1;

/**
 * Created by JClassic on 3/31/2017.
 */

public class StatsFragmentFillintheBlanks extends Fragment implements WordEntryFavoritesChangedListener {

    String TAG = "Test-stats1";

    private RecyclerView resultsRecycler;
    private ArrayList<Tweet> mDataset;
    private int mCorrect;
    private int mTotal;

    LinearLayout topscoreLayout;
    TextView textScore;
    TextView textPercentage;
    private long mLastClickTime = 0;
    PostQuizStatsFillintheBlankAdapter mAdapter;

    public StatsFragmentFillintheBlanks() {}

    public static StatsFragmentFillintheBlanks newInstance(ArrayList<Tweet> dataset
            , int correct
            , int total) {
        StatsFragmentFillintheBlanks fragment = new StatsFragmentFillintheBlanks();
        Bundle args = new Bundle();
        args.putParcelableArrayList("dataset", dataset);
        Log.i("TEST","datasetsize: " + dataset.size());
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
            Log.i("TEST","datasetsize2: " + mDataset.size());
            mCorrect = getArguments().getInt("correct");
            mTotal = getArguments().getInt("total");
        }



        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        resultsRecycler.setLayoutManager(layoutManager);

        int adapterRowHeightMultiplier = Math.round((float) 10 * getResources().getDisplayMetrics().density);
        RxBus rxBus = new RxBus();
       mAdapter = new PostQuizStatsFillintheBlankAdapter(getContext()
                , mDataset
                ,adapterRowHeightMultiplier
                ,rxBus);
        resultsRecycler.setAdapter(mAdapter);

        rxBus.toClickObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {

                        if(isUniqueClick(100) && event instanceof WordEntry) {
                            WordEntry wordEntry = (WordEntry) event;
                            WordDetailPopupDialog wordDetailPopupDialog = WordDetailPopupDialog.newInstance(wordEntry);
                            wordDetailPopupDialog.setTargetFragment(StatsFragmentFillintheBlanks.this, 0);
                            wordDetailPopupDialog.show(getFragmentManager(),"wordDetailPopup");
                        }

                    }

                });

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

    /**
     * Checks how many milliseconds have elapsed since the last time "mLastClickTime" was updated
     * If enough time has elapsed, returns True and updates mLastClickTime.
     * This is to stop unwanted rapid clicks of the same button
     * @param elapsedMilliSeconds threshold of elapsed milliseconds before a new button click is allowed
     * @return bool True if enough time has elapsed, false if not
     */
    public boolean isUniqueClick(int elapsedMilliSeconds) {
        if(SystemClock.elapsedRealtime() - mLastClickTime > elapsedMilliSeconds) {
            mLastClickTime = SystemClock.elapsedRealtime();
            return true;
        } else {
            return false;
        }
    }

    public void updateWordEntryItemFavorites(WordEntry wordEntry) {
        for(Tweet tweet: mDataset) {
            if(tweet.getWordEntries()!=null && tweet.getWordEntries().contains(wordEntry)) {
                for(WordEntry tweetWordEntry : tweet.getWordEntries()) {
                    if(tweetWordEntry.getId()==wordEntry.getId()) {
                        wordEntry.setItemFavorites(wordEntry.getItemFavorites());
                    }
                }
            }
        }
        mAdapter.notifyDataSetChanged();

    }

    public void updateWordEntryFavoritesForOtherTabs(WordEntry wordEntry) {}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("mDataset", mDataset);
        outState.putInt("mCorrect", mCorrect);
        outState.putInt("mTotal", mTotal);

    }



}
