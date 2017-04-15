package com.jukuproject.jukutweet.TabContainers;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment;
import com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

//TODO -- possibly consolidate these into one?
public class QuizTab1Container extends BaseContainerFragment {

    private boolean mIsViewInited;
    private String mQuiz;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.container_fragment, null);
    }

    public QuizTab1Container() {}

    public static QuizTab1Container newInstance(String mQuiz)
    {
//        QuizTab1Container fragment = new QuizTab1Container();
//        Bundle args = new Bundle();
//        args.putString("mQuiz",mQuiz);
//        return fragment;
        return new QuizTab1Container();
    }

    public static QuizTab1Container newFillintheBlanksInstance(ArrayList<Tweet> tweets
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

    public static QuizTab1Container newMultipleChoiceInstance(ArrayList<WordEntry> wordEntries
            , String quizType
            , String quizTimer
            , String quizSize
            , double totalWeight
            , String myListType
            , String colorString
            , MyListEntry myListEntry) {
        QuizTab1Container fragment = new QuizTab1Container();
        Bundle args = new Bundle();
        args.putString("mQuiz","MultipleChoice");
        args.putParcelableArrayList("wordEntries", wordEntries);
        args.putString("quizType",quizType);
        args.putInt("quizSize",Integer.parseInt((quizSize)));
        if(quizTimer.equals("None")) {
            args.putInt("quizTimer",-1);
        } else {
            args.putInt("quizTimer",Integer.parseInt(quizTimer));
        }
        args.putDouble("totalWeight",totalWeight);
        args.putString("myListType",myListType);
        args.putString("colorString",colorString);
        args.putParcelable("myListEntry",myListEntry);
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
//        Log.e("test", "tab 1 container on activity created");

            if (savedInstanceState != null) {
                //Save the fragment's state here
//                mDataset = savedInstanceState.getParcelableArrayList("mDataset");

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

                ArrayList<WordEntry> wordEntries = getArguments().getParcelableArrayList("wordEntries");
                MultipleChoiceFragment multipleChoiceFragment = MultipleChoiceFragment.newInstance(
                        wordEntries
                        , getArguments().getString("quizType")
                        , getArguments().getInt("quizTimer")
                        , getArguments().getInt("quizSize")
                        , getArguments().getDouble("totalWeight")
                        , getArguments().getString("myListType")
                        , getArguments().getString("colorString")
                        , (MyListEntry)getArguments().getParcelable("myListEntry"));
                replaceFragment(multipleChoiceFragment, false,"multipleChoiceFragment");
                break;
            case "FillintheBlanks":


                ArrayList<Tweet> tweets = getArguments().getParcelableArrayList("tweets");

                Log.d("TEST","dataset tab1container isspinner: " + tweets.get(0).getWordEntries().get(1).getKanji() + ", spinner: "
                        + tweets.get(0).getWordEntries().get(1).isSpinner());

                FillInTheBlankFragment fillInTheBlankFragment = FillInTheBlankFragment.newInstance(tweets
                        , getArguments().getString("quizSize")
                        , getArguments().getDouble("totalWeight")
                        , getArguments().getString("colorString")
                        ,  (MyListEntry)getArguments().getParcelable("myListEntry"));

                replaceFragment(fillInTheBlankFragment, false,"fillInTheBlankFragment");

                break;
            default:
//                replaceFragment(multipleChoiceFragment, false,"multipleChoiceFragment");
                break;

        }

    }

//    public boolean updateUserListFragment() {
//        try {
//            ((UserListFragment) getChildFragmentManager().findFragmentByTag("userlistfragment")).updateAdapter();
//            return true;
//        } catch (Exception e) {
//            Log.e("Tab1Container","Could not find userListFragment");
//            return false;
//        }
//    }
//
//    public boolean isTopFragmentShowing() {
//        try {
//            (getChildFragmentManager().findFragmentByTag("userlistfragment")).isVisible();
//            return true;
//        } catch (Exception e) {
//            Log.e("Tab1Container","Could not find userListFragment");
//            return false;
//        }
//    }
@Override
public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putBoolean("mIsViewInited", mIsViewInited);
    outState.putString("mQuiz",mQuiz);

}
}