package com.jukuproject.jukutweet.Interfaces;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by JClassic on 4/5/2017.
 */

public interface TweetListOperationsInterface {
    boolean duplicateTweetList(String listName);
    boolean saveTweetList(String listName);
    boolean clearTweetList(String listName, boolean isStarFavorite);
    boolean deleteTweetList(String listName);
    boolean renameTweetList(String oldListName, String newListName);
    boolean changeTweetListStarColor(String tweetId, String userId, String originalColor, String updatedColor);
    boolean addTweetToTweetList(String tweetId, String userId, String listName, int listSys);
    boolean removeTweetFromTweetList(String tweetId, String listName, int listSys);
    boolean removeMultipleTweetsFromTweetList(String concatenatedTweetIds, MyListEntry myListEntry);
    ArrayList<MyListEntry> getTweetListsForTweet(ArrayList<String> activeFavoriteStars
            , String concatenatedTweetIds
            , @Nullable MyListEntry entryToExclude);
    ArrayList<WordEntry> getWordsFromATweetList(MyListEntry myListEntry
            , ColorThresholds colorThresholds
            , String colorString
            , @Nullable Integer excludeIdInteger
            , @Nullable Integer resultLimit);
    boolean addMultipleTweetsToTweetList(MyListEntry myListEntry, String concatenatedTweetIds);
    int tweetExistsInDB(Tweet tweet);
    int tweetParsedKanjiExistsInDB(Tweet tweet);
    int saveTweetToDB(UserInfo userInfo, Tweet tweet);
    int saveTweetUrls(Tweet tweet);
    int saveParsedTweetKanji(ArrayList<WordEntry> wordEntries, String tweet_id);
    HashMap<String,ItemFavorites> getStarFavoriteDataForAUsersTweets(String userId);
    ArrayList<Tweet> getTweetsForSavedTweetsList(MyListEntry myListEntry , ColorThresholds colorThresholds);
    ArrayList<Tweet> getTweetsForSavedTweetsList(UserInfo userInfo, ColorThresholds colorThresholds);
    //    Cursor getWordEntryForWordId(int kanjiId, ColorThresholds colorThresholds);
    Cursor getTweetListColorBlocksCursor(ColorThresholds colorThresholds, @Nullable MyListEntry myListEntry);
    Cursor getTweetListColorBlocksCursorForSingleUser(ColorThresholds colorThresholds, String userId);
}
