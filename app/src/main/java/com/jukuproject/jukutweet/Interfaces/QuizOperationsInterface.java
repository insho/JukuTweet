package com.jukuproject.jukutweet.Interfaces;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;

import java.util.ArrayList;

/**
 * Manages database calls to {@link com.jukuproject.jukutweet.Database.QuizOpsHelper}
 */
public interface QuizOperationsInterface {

    Cursor getPossibleMultipleChoiceMatch(String quizType
            , WordEntry correctWordEntry
            , String alreadyaddedkanji
            , String kanjiToBreak
            , String possibleKanjiPart);
    Cursor getRandomKanji(int keysToExclude, int limit);

    ArrayList<Tweet> getFillintheBlanksTweetsForATweetList(MyListEntry myListEntry
            , ColorThresholds colorThresholds
            , String colorString
            , @Nullable Integer resultLimit);
    ArrayList<Tweet> getFillintheBlanksTweetsForAWordList(MyListEntry myListEntry
            , ColorThresholds colorThresholds
            , String colorString
            , @Nullable Integer resultLimit);

    void addWordScoreToScoreBoard(int wordId, int total, int correct);

    ArrayList<Tweet> getFillintheBlanksTweetsForAUser(UserInfo userInfo
            , ColorThresholds colorThresholds
            , String colorString
            , @Nullable Integer resultLimit);

}
