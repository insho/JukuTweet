package com.jukuproject.jukutweet.Interfaces;

import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.UserInfo;

/**
 * Interface between {@link com.jukuproject.jukutweet.Dialogs.QuizMenuDialog} and the activity (either {@link com.jukuproject.jukutweet.MainActivity} or
 * {@link com.jukuproject.jukutweet.PostQuizStatsActivity} from which the actual quiz activity will be initiated.
 */
public interface QuizMenuDialogInteractionListener {
    //From quizmenu popup to main activity (starting quizzes)
    void showFlashCardFragment(int tabNumber
            , MyListEntry listEntry
            , String frontValue
            , String backValue
            , String selectedColorString);

    void showSingleUserFlashCardFragment(int tabNumber
            , UserInfo userInfo
            , String frontValue
            , String backValue
            , String selectedColorString);

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

    void showFab(boolean show);
}
