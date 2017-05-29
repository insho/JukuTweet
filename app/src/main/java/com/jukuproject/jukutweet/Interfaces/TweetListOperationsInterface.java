package com.jukuproject.jukutweet.Interfaces;

import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetEntities;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Tweet Ops interface between the {@link com.jukuproject.jukutweet.Database.InternalDB} {@link com.jukuproject.jukutweet.Database.TweetOpsHelper}
 * and the app
 */
public interface TweetListOperationsInterface {
    boolean duplicateTweetList(String listName);
    boolean saveTweetList(String listName);
    void clearTweetList(String listName, boolean isStarFavorite);
    void deleteTweetList(String listName);
    boolean renameTweetList(String oldListName, String newListName);
    boolean changeTweetListStarColor(String tweetId, String userId, String originalColor, String updatedColor);
    boolean addTweetToTweetList(String tweetId, String userId, String listName, int listSys);
    boolean removeTweetFromTweetList(String tweetId, String listName, int listSys);
    void removeMultipleTweetsFromTweetList(String concatenatedTweetIds, MyListEntry myListEntry);

    ArrayList<Pair<MyListEntry,Tweet>> removeTweetsFromAllTweetLists(String concatenatedTweetIds);
    ArrayList<MyListEntry> getTweetListsForTweet(ArrayList<String> activeFavoriteStars
            , String concatenatedTweetIds
            , @Nullable MyListEntry entryToExclude);
    ArrayList<WordEntry> getWordsFromATweetList(MyListEntry myListEntry
            , ColorThresholds colorThresholds
            , String colorString
            , @Nullable Integer excludeIdInteger
            , @Nullable Integer resultLimit);
    void addMultipleTweetsToTweetList(MyListEntry myListEntry, String concatenatedTweetIds);
    int tweetParsedKanjiExistsInDB(Tweet tweet);
    TweetEntities getTweetEntitiesForSavedTweet(String tweetId);
    void saveParsedTweetKanji(ArrayList<WordEntry> wordEntries, String tweet_id);
    HashMap<String,ItemFavorites> getStarFavoriteDataForAUsersTweets(String userId);
    ArrayList<Tweet> getTweetsForSavedTweetsList(MyListEntry myListEntry , ColorThresholds colorThresholds);
    ArrayList<Tweet> getTweetsForSavedTweetsList(UserInfo userInfo, ColorThresholds colorThresholds);
    ArrayList<Tweet> getTweetsThatIncludeAWord(String wordIds,ColorThresholds colorThresholds);

    Cursor getTweetListColorBlocksCursor(ColorThresholds colorThresholds, @Nullable MyListEntry myListEntry);
    Cursor getTweetListColorBlocksCursorForSingleUser(ColorThresholds colorThresholds, String userId);

    ArrayList<WordEntry> getTopFiveTweetWordEntries(String topOrBottom
            ,@Nullable  ArrayList<Integer> idsToExclude
            ,MyListEntry myListEntry
            ,ColorThresholds colorThresholds
            ,int totalCountLimit
            ,double topbottomThreshold);

    void deleteTweetIfNecessary(String concatenatedTweetIds);

    ArrayList<WordEntry> getWordsFromAUsersSavedTweets(UserInfo userInfo
            , ColorThresholds colorThresholds
            , String colorString
            , @Nullable Integer excludeIdInteger
            , @Nullable Integer resultLimit);

    ArrayList<WordEntry> getTopFiveTweetSingleUserEntries(String topOrBottom
            ,@Nullable  ArrayList<Integer> idsToExclude
            ,UserInfo userInfo
            ,ColorThresholds colorThresholds
            ,int totalCountLimit
            ,double topbottomThreshold);

    int saveOrDeleteTweet(Tweet tweet);

}
