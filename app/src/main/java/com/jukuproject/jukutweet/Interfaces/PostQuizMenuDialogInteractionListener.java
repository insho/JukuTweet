package com.jukuproject.jukutweet.Interfaces;

import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.UserInfo;

/**
 * Links between e
 */

public interface PostQuizMenuDialogInteractionListener {

    void goToQuizActivityMultipleChoice(int tabNumber
            , MyListEntry listEntry
            , Integer currentExpandedPosition
            , String quizType
            , String quizSize
            , String quizTimer
            , String selectedColorString);

    void goToSingleUserQuizActivityMultipleChoice(int tabNumber
            , UserInfo userInfo
            , Integer currentExpandedPosition
            , String quizType
            , String quizSize
            , String quizTimer
            , String selectedColorString);

    void goToQuizActivityFillintheBlanks(int tabNumber
            , MyListEntry listEntry
            , Integer currentExpandedPosition
            , String quizSize
            , String selectedColorString);

    void goToSingleUserQuizActivityFillintheBlanks(int tabNumber
            , UserInfo userInfo
            , Integer currentExpandedPosition
            , String quizSize
            , String selectedColorString);
}
