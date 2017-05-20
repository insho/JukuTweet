package com.jukuproject.jukutweet.Interfaces;

import com.jukuproject.jukutweet.Models.MultChoiceResult;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;

import java.util.ArrayList;


public interface QuizFragmentInteractionListener {

    void showPostQuizStatsMultipleChoice(ArrayList<MultChoiceResult> dataset
            , String quizType
            , MyListEntry myListEntry
            , boolean isWordBuilder
            , boolean isHighScore
            , Integer wordbuilderScore
            , int correct
            , int total);


    void showPostQuizStatsMultipleChoiceForSingleUsersTweets(ArrayList<MultChoiceResult> dataset
            , String quizType
            , final UserInfo userInfo
            , boolean isWordBuilder
            , boolean isHighScore
            , Integer wordbuilderScore
            , int correct
            , int total);


    void showPostQuizStatsFillintheBlanks(ArrayList<Tweet> dataset
            , MyListEntry myListEntry
            , int correct
            , int total);


    void showPostQuizStatsFillintheBlanksForSingleUsersTweets(ArrayList<Tweet> dataset
            , UserInfo userInfo
            , int correct
            , int total);

    void emergencyGoBackToMainActivity();
    void downloadTweetUserIcons(UserInfo userInfo);
}
