package com.jukuproject.jukutweet.Interfaces;

import com.jukuproject.jukutweet.Models.MultChoiceResult;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;

import java.util.ArrayList;

/**
 * Traffic control between quiz fragments and Quiz Activity
 *
 * @see com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment
 * @see com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment
 * @see com.jukuproject.jukutweet.QuizActivity
 */
public interface QuizFragmentInteractionListener {

    void showPostQuizStatsMultipleChoice(ArrayList<MultChoiceResult> dataset
            , String quizType
            , Object listInformation
            , boolean isWordBuilder
            , boolean isHighScore
            , Integer wordbuilderScore
            , int correct
            , int total);


//    void showPostQuizStatsMultipleChoiceForSingleUsersTweets(ArrayList<MultChoiceResult> dataset
//            , String quizType
//            , final UserInfo userInfo
//            , boolean isWordBuilder
//            , boolean isHighScore
//            , Integer wordbuilderScore
//            , int correct
//            , int total);


    void showPostQuizStatsFillintheBlanks(ArrayList<Tweet> dataset
            , Object listInformation
            , int correct
            , int total);

//
//    void showPostQuizStatsFillintheBlanksForSingleUsersTweets(ArrayList<Tweet> dataset
//            , UserInfo userInfo
//            , int correct
//            , int total);

    void emergencyGoBackToMainActivity();
    void downloadTweetUserIcons(UserInfo userInfo);
}
