package com.jukuproject.jukutweet.Interfaces;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
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
    Cursor getRandomKanji(int keyToExclude, int limit);
    void setSpinnersForTweetWithMyListWords(SQLiteDatabase db
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
    void setRandomSpinnersForTweet(Tweet tweet);
    ArrayList<Integer> getIdsForWordList(MyListEntry myListEntry);
    boolean addWordScoreToScoreBoard(int wordId, int total, int correct);


//    void superTest(MyListEntry myListEntry
//            , ColorThresholds colorThresholds
//            , String colorString
//            , @Nullable Integer resultLimit);
}
