package com.jukuproject.jukutweet.TabContainers;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.Fragments.StatsFragmentFillintheBlanks;
import com.jukuproject.jukutweet.Fragments.StatsFragmentMultipleChoice;
import com.jukuproject.jukutweet.Models.MultChoiceResult;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

//TODO -- possibly consolidate these into one?
public class PostQuizTab1Container extends BaseContainerFragment {

    private boolean mIsViewInited;
    private String mQuiz;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.container_fragment, null);
    }

    public PostQuizTab1Container() {}

    public static QuizTab1Container newInstance()
    {
//        QuizTab1Container fragment = new QuizTab1Container();
//        Bundle args = new Bundle();
//        args.putString("mQuiz",mQuiz);
//        return fragment;
        return new QuizTab1Container();
    }

    public static QuizTab1Container newFillintheBlanksStatsInstance(ArrayList<Tweet> tweets
            , String quizSize
            , double totalWeight
            , String colorString
            , MyListEntry myListEntry
    ) {

        QuizTab1Container fragment = new QuizTab1Container();
        Bundle args = new Bundle();
        args.putString("mQuiz","FillintheBlanks");
        args.putParcelableArrayList("tweets", tweets);
        args.putString("quizSize",quizSize);
        args.putDouble("totalWeight",totalWeight);
        args.putString("colorString",colorString);
        args.putParcelable("myListEntry",myListEntry);
        fragment.setArguments(args);

        return  fragment;
    }

    public static QuizTab1Container newMultipleChoiceStatsInstance(ArrayList<MultChoiceResult> dataset
            , String quizType
            , final MyListEntry myListEntry
            , boolean isWordBuilder
            , boolean isHighScore
            , Integer wordbuilderScore
            , int correct
            , int total
    ) {
        QuizTab1Container fragment = new QuizTab1Container();
        Bundle args = new Bundle();
        args.putString("mQuiz","MultipleChoice");
        args.putParcelableArrayList("dataset", dataset);
        args.putString("quizType",quizType);
        args.putParcelable("myListEntry",myListEntry);
        args.putBoolean("isWordBuilder",isWordBuilder);
        args.putBoolean("isHighScore",isHighScore);
        args.putInt("wordbuilderScore",wordbuilderScore);
        args.putInt("correct",correct);
        args.putInt("total",total);

        fragment.setArguments(args);
        return  fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        if(savedInstanceState != null) {
            mIsViewInited = savedInstanceState.getBoolean("mIsViewInited");
            mQuiz = savedInstanceState.getString("mQuiz");
        } else {
            mQuiz = getArguments().getString("mQuiz");
        }

        if (savedInstanceState != null) {

        } else
        if (!mIsViewInited) {
            mIsViewInited = true;
            initView(mQuiz);
        }


    }

    private void initView(String quiz) {
//        Log.e("test", "tab 1 init view");

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
//                replaceFragment(multipleChoiceFragment, false,"multipleChoiceFragment");
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