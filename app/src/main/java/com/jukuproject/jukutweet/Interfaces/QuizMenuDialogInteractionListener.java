package com.jukuproject.jukutweet.Interfaces;

import com.jukuproject.jukutweet.Models.MyListEntry;

/**
 * Created by JClassic on 4/14/2017.
 */

public interface QuizMenuDialogInteractionListener {
    //From quizmenu popup to main activity (starting quizzes)
    void showFlashCardFragment(int tabNumber
            , MyListEntry listEntry
            , String frontValue
            , String backValue
            , String selectedColorString);
    void goToQuizActivityMultipleChoice(int tabNumber
            , MyListEntry listEntry
            , String quizType
            , String quizSize
            , String quizTimer
            , String selectedColorString);

    void showFillintheBlanksFragment(int tabNumber
            , MyListEntry listEntry
            , String quizSize
            ,String selectedColorString);
}
