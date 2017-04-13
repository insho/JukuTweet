package com.jukuproject.jukutweet.Interfaces;

import com.jukuproject.jukutweet.Models.MultChoiceResult;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;

import java.util.ArrayList;

/**
 * Created by JClassic on 4/13/2017.
 */

public interface QuizFragmentInteractionListener {
    void showFab(boolean show, String type);
    void showFab(boolean show);
    void showPostQuizStatsMultipleChoice(ArrayList<MultChoiceResult> dataset
            , String quizType
            , MyListEntry myListEntry
            , boolean isWordBuilder
            , boolean isHighScore
            , Integer wordbuilderScore
            , int correct
            , int total);

    void showPostQuizStatsFillintheBlanks(ArrayList<Tweet> dataset
            , MyListEntry myListEntry
            , int total);
}
