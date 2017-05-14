package com.jukuproject.jukutweet.Interfaces;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;

import java.util.ArrayList;

/**
 * Created by JClassic on 4/5/2017.
 */

public interface QuizOperationsInterface {

    Cursor getPossibleMultipleChoiceMatch(String quizType
            , WordEntry correctWordEntry
            , String alreadyaddedkanji
            , String kanjiToBreak
            , String possibleKanjiPart);
    Cursor getRandomKanji(int keysToExclude, int limit);
    int setSpinnersForTweetWithMyListWords(SQLiteDatabase db
            , String myListType
            , MyListEntry myListEntry
            , Tweet tweet
            , ArrayList<Integer> wordListEdictIds);

    ArrayList<String> getDummySpinnerOptions(SQLiteDatabase db
            , MyListEntry myListEntry
            , WordEntry wordEntry
            , String mylistType);

    ArrayList<Tweet> getFillintheBlanksTweetsForATweetList(MyListEntry myListEntry
            , ColorThresholds colorThresholds
            , String colorString
            , @Nullable Integer resultLimit);
    ArrayList<Tweet> getFillintheBlanksTweetsForAWordList(MyListEntry myListEntry
            , ColorThresholds colorThresholds
            , String colorString
            , @Nullable Integer resultLimit);
    int setRandomSpinnersForTweet(Tweet tweet
    ,SQLiteDatabase db
            , MyListEntry myListEntry
            , String myListType);
    ArrayList<Integer> getIdsForWordList(MyListEntry myListEntry);
    boolean addWordScoreToScoreBoard(int wordId, int total, int correct);

    ArrayList<Tweet> getFillintheBlanksTweetsForAUser(UserInfo userInfo
            , ColorThresholds colorThresholds
            , String colorString
            , @Nullable Integer resultLimit);

//    void superTest(MyListEntry myListEntry
//            , ColorThresholds colorThresholds
//            , String colorString
//            , @Nullable Integer resultLimit);
}
