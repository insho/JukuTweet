package com.jukuproject.jukutweet.Fragments;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.PostQuizStatsMultipleChoiceAdapter;
import com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Interfaces.WordEntryFavoritesChangedListener;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MultChoiceResult;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;

import java.util.ArrayList;

import rx.functions.Action1;

/**
 * Created by JClassic on 3/31/2017.
 */

public class StatsFragmentMultipleChoice extends Fragment implements WordEntryFavoritesChangedListener {

    String TAG = "Test-stats1";

    private ListView resultslistView;
    private ArrayList<MultChoiceResult> mDataset;
    private String mQuizType;
    private boolean mIsHighScore;
    private boolean mIsWordBuilder;
    private Integer mWordBuilderScore;
    private int mCorrect;
    private int mTotal;
    private long mLastClickTime = 0;

    LinearLayout topscoreLayout;
    TextView textScore;
    TextView textPercentage;


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

        View v = inflater.inflate(R.layout.fragment_stats_multiplechoice, container, false);
        resultslistView = (ListView) v.findViewById(R.id.listResults);
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
            mQuizType = savedInstanceState.getString("mQuizType");
            mIsWordBuilder = savedInstanceState.getBoolean("mIsWordBuilder");
            mIsHighScore = savedInstanceState.getBoolean("mIsHighScore");
            mWordBuilderScore = savedInstanceState.getInt("mWordBuilderScore");
            mCorrect = savedInstanceState.getInt("mCorrect");
            mTotal = savedInstanceState.getInt("mTotal");

        } else {
            mDataset = getArguments().getParcelableArrayList("dataset");
            mQuizType = getArguments().getString("quizType");
            mIsWordBuilder = getArguments().getBoolean("mIsWordBuilder");
            mIsHighScore = getArguments().getBoolean("mIsHighScore");
            mWordBuilderScore =getArguments().getInt("wordbuilderScore");
            mCorrect = getArguments().getInt("correct");
            mTotal = getArguments().getInt("total");
        }

        RxBus rxBus = new RxBus();
        resultslistView.setAdapter(new PostQuizStatsMultipleChoiceAdapter(getContext(), mDataset,mIsWordBuilder,rxBus));

        rxBus.toClickObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {

                        if(isUniqueClick(100) && event instanceof WordEntry) {
                            WordEntry wordEntry = (WordEntry) event;
                            WordDetailPopupDialog wordDetailPopupDialog = WordDetailPopupDialog.newInstance(wordEntry);
                            wordDetailPopupDialog.setTargetFragment(StatsFragmentMultipleChoice.this, 0);
                            wordDetailPopupDialog.show(getFragmentManager(),"wordDetailPopup");

                        }

                    }

                });

        if(mQuizType != null && mQuizType.equals("WordBuilder")) {
            if(mIsHighScore) {
                topscoreLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.holo_green_light));
                textScore.setText(getString(R.string.statsfrag_multchoice_wordbuilder_highscore, mWordBuilderScore));
            } else {
                topscoreLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_light));
                textScore.setText(getString(R.string.statsfrag_multchoice_wordbuilder_total, mWordBuilderScore));
            }
            if(mDataset!=null) {
                textPercentage.setText(getString(R.string.statsfrag_multchoice_wordbuilder_unique, mDataset.size()));
            }

        } else {

            try {
                ColorThresholds colorThresholds = SharedPrefManager.getInstance(getContext()).getColorThresholds();
                double percentage = 100*((double)mCorrect/(double)mTotal);
                if(percentage < (colorThresholds.getRedThreshold()*100)) {
                    topscoreLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorJukuRed));
                } else if (percentage< (colorThresholds.getYellowThreshold()*100)) {
                    topscoreLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_light));
                } else {
                    topscoreLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.holo_green_light));
                }

                textScore.setText(getString(R.string.score,mCorrect,mTotal));
//                textScore.setText(String.valueOf(mCorrect) + "/" + String.valueOf(mTotal));
//                String txtpercentage =   String.valueOf((int)percentage) + "%";
                textPercentage.setText(getString(R.string.percentage,(int)percentage));


            } catch (Exception e) {
                Log.e(TAG,"stats fragment multiple choice couldn't set top score bar " + e);
            }



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
        for(MultChoiceResult multChoiceResult: mDataset) {
            if(multChoiceResult.getWordEntry()!=null && multChoiceResult.getWordEntry().getId() == wordEntry.getId()) {
                multChoiceResult.getWordEntry().setItemFavorites(wordEntry.getItemFavorites());
            }
        }

    }

    public void updateWordEntryFavoritesForOtherTabs(WordEntry wordEntry) {}
    public void notifySavedTweetFragmentsChanged(){};
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("mDataset", mDataset);
        outState.putString("mQuizType", mQuizType);
        outState.putBoolean("mIsWordBuilder", mIsWordBuilder);
        outState.putBoolean("mIsHighScore", mIsHighScore);
        outState.putInt("mWordBuilderScore", mWordBuilderScore);
        outState.putInt("mCorrect", mCorrect);
        outState.putInt("mTotal", mTotal);

    }



}
