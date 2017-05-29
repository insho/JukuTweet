package com.jukuproject.jukutweet.TabContainers;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.Fragments.StatsFragmentFillintheBlanks;
import com.jukuproject.jukutweet.Fragments.StatsFragmentMultipleChoice;
import com.jukuproject.jukutweet.Models.MultChoiceResult;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Fragment container for PostQuizStats {@link StatsFragmentMultipleChoice} and {@link StatsFragmentFillintheBlanks}
 * @see BaseContainerFragment
 */
public class PostQuizTab1Container extends BaseContainerFragment {

    private boolean mIsViewInited;
    private String mQuiz;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.container_fragment, null);
    }

    public PostQuizTab1Container() {}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        if(savedInstanceState != null) {
            mIsViewInited = savedInstanceState.getBoolean("mIsViewInited");
            mQuiz = savedInstanceState.getString("mQuiz");
        } else {
            mQuiz = getArguments().getString("mQuiz");
        }

        if (savedInstanceState == null && !mIsViewInited) {
            mIsViewInited = true;
            initView(mQuiz);
        }
    }

    private void initView(String quiz) {
        switch (quiz){
            case "MultipleChoice":

                ArrayList<MultChoiceResult> dataset = getArguments().getParcelableArrayList("dataset");
                    StatsFragmentMultipleChoice statsFragmentMultipleChoice = StatsFragmentMultipleChoice.newInstance(dataset
                            ,getArguments().getString("quizType")
                            , getArguments().getBoolean("isWordBuilder")
                            , getArguments().getBoolean("isHighScore")
                            , getArguments().getInt("wordbuilderScore")
                            , getArguments().getInt("correct")
                            , getArguments().getInt("total"));
                replaceFragment(statsFragmentMultipleChoice, false,"statsFragmentMultipleChoice");
                break;
            case "FillintheBlanks":

                ArrayList<Tweet> tweets = getArguments().getParcelableArrayList("tweets");
                StatsFragmentFillintheBlanks statsFragmentFillintheBlanks = StatsFragmentFillintheBlanks.newInstance(tweets
                        ,getArguments().getInt("correct")
                        , getArguments().getInt("total"));

                replaceFragment(statsFragmentFillintheBlanks, false,"statsFragmentFillintheBlanks");

                break;
            default:
                break;

        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("mIsViewInited", mIsViewInited);
        outState.putString("mQuiz",mQuiz);

    }
}