package com.jukuproject.jukutweet.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Interfaces.QuizOperationsInterface;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Collection of internal sqlite database calls related to quiz operations.
 *
 * @see InternalDB
 * @see QuizOperationsInterface
 * @see com.jukuproject.jukutweet.Fragments.FlashCardsFragment
 * @see com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment
 * @see com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment
 */
public class QuizOpsHelper implements QuizOperationsInterface {
    private SQLiteOpenHelper sqlOpener;
    private static String TAG = "TEST-quizops";

    public QuizOpsHelper(SQLiteOpenHelper sqlOpener) {
        this.sqlOpener = sqlOpener;
    }

    /**
     * Looks for matches in the Edict db for pieces of a kanji word in the {@link com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment}.
     * If the "hard answers" setting is enabled for the Multiple Choice quiz, the correct answer is broken up into pieces and similar matches
     * are found in the db using this method and used as decoy false answers. It makes for a more difficult quiz.
     * @param quizType Type of quiz ("Kanji to Kana", "Kanji to Definition" etc)
     * @param correctWordEntry The correct answer wordentry
     * @param alreadyaddedkanji Kanji that have already been chosen as false answer, so they do not show up twice
     * @param kanjiToBreak The correct answer Kanji or Furigana (depending on the quiz type)
     * @param possibleKanjiPart The broken up piece of the correct answer that will be matched against the database
     * @return Cursor containing similar words to the correct answer
     */
    public Cursor getPossibleMultipleChoiceMatch(String quizType
            , WordEntry correctWordEntry
            , String alreadyaddedkanji
            , String kanjiToBreak
            , String possibleKanjiPart) {
        if (quizType.equals("Kanji to Kana")) {
            return sqlOpener.getWritableDatabase().rawQuery("SELECT  x.[_id]" +
                    ",x.[Kanji]" +
                    ",(CASE WHEN x.[Furigana] is null  then '' else x.[Furigana] end) as [Furigana] " +
                    ",x.[Definition]" +
                    ",ifnull(y.[Correct],0) as [Correct] " +
                    ",ifnull(y.[Total],0) as [Total] " +
                    ",(CASE WHEN [Total] >0 THEN CAST(ifnull([Correct],0)  as float)/[Total] ELSE 0 END) as [Percent] " +
                    "FROM " +
                    "(" +
                        "SELECT [_id]" +
                        ",[Kanji]" +
                        ",[Furigana]" +
                        ",[Definition]  " +
                        "FROM [Edict] " +
                        "Where  [Common] = 1 " +
                        "and [Furigana] is not null " +
                        "AND [_ID] <>  " + correctWordEntry.getId() + " " +
                        "AND [_ID] not in (" + alreadyaddedkanji + ") " +
                        "and [Furigana] <> \"" + correctWordEntry.getFurigana() + "\"  " +
                        "and Length([Furigana]) between " + (kanjiToBreak.length()) + " and " + (kanjiToBreak.length() + 1) + " " +
                        "and [Furigana] like \"%" + possibleKanjiPart + "%\"   LIMIT 5" +
                    ") as x " +
                    "LEFT JOIN " +
                    "(" +
                        "SELECT [_id]" +
                        ",sum([Correct]) as [Correct]" +
                        ",sum([Total]) as [Total]  " +
                        "FROM [JScoreboard] " +
                        "GROUP BY [_id]" +
                    ") as y " +
                    "ON x.[_id] = y.[_id] ", null);
        } else {
            return sqlOpener.getWritableDatabase().rawQuery("SELECT  x.[_id]" +
                    ",x.[Kanji]" +
                    ",(CASE WHEN x.[Furigana] is null  then '' else x.[Furigana] end) as [Furigana] " +
                    ",x.[Definition]" +
                    ",ifnull(y.[Correct],0) as [Correct] " +
                    ",ifnull(y.[Total],0) as [Total] " +
                    ",(CASE WHEN [Total] >0 THEN CAST(ifnull([Correct],0)  as float)/[Total] ELSE 0 END) as [Percent] " +
                    "FROM " +
                    "(" +
                        "SELECT [_id]" +
                        ",[Kanji]" +
                        ",[Furigana]" +
                        ",[Definition]  " +
                        "FROM [Edict] " +
                        "Where  [Common] = 1 " +
                        "and [_ID] <> " + correctWordEntry.getId() + " " +
                        "AND [_ID] not in (" + alreadyaddedkanji + ") " +
                        "and Length([Kanji]) between " + (kanjiToBreak.length()) + " and " + (kanjiToBreak.length() + 1) + " " +
                        "and [Kanji] <> \"" + correctWordEntry.getKanji() + "\" " +
                        "and [Kanji] like \"%" + possibleKanjiPart + "%\"   LIMIT 5" +
                    ") as x " +
                    "LEFT JOIN " +
                    "(" +
                        "SELECT [_id]" +
                        ",sum([Correct]) as [Correct]" +
                        ",sum([Total]) as [Total]  " +
                        "FROM [JScoreboard] " +
                        "GROUP BY [_id]" +
                    ") as y " +
                    "ON x.[_id] = y.[_id] ", null);
        }
    }


    /**
     * Picks random kanji from the database to round out the dummy answers in the {@link com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment}
     * @param keysToExclude Kanji ids to be excluded from the dataset
     * @param limit maximum number of kanji to be pulled
     * @return Cursor with random kanji from db
     */
    public Cursor getRandomKanji(int keysToExclude, int limit) {
        return sqlOpener.getWritableDatabase().rawQuery("SELECT [_id]" +
                ",[Kanji]" +
                ",[Furigana]" +
                ",[Definition]" +
                ",ifnull([Correct],0) " +
                ",ifnull([Total],0)" +
                ",(CASE WHEN [Total] >0 THEN CAST(ifnull([Correct],0)  as float)/[Total] ELSE 0 END) as [Percent] " +
                "FROM " +
                "(" +
                    "SELECT [_id]" +
                    ",[Kanji]" +
                    ",[Furigana]" +
                    ",[Definition]  " +
                    "FROM [Edict] " +
                    "where [Common] = 1 " +
                    " and [_id]  not in ( " +  keysToExclude + ")" +
                    " and [Furigana] is not null " +
                    " ORDER BY RANDOM() LIMIT "+ limit +" " +
                ") " +
                " NATURAL LEFT JOIN " +
                "(" +
                    "SELECT [_id]" +
                    ",sum([Correct]) as [Correct]" +
                    ",sum([Total]) as [Total] " +
                    "from [JScoreboard] " +
                    "GROUP BY [_id]" +
                ") ORDER BY RANDOM()  Limit " + limit + " ",null);
    }

    /**
     * Mixes a random assortment of words, as well as words from the same tweet/word list, into
     * one pool of up to 75 words from which the dummy spinner options (3 per spinner) in the fill
     * in the blanks quiz are chosen
     * @param listEntryObject Object with info about list, either a MyListEntry for tweet/word lists, or a UserInfo object for a single
     *                        user's saved tweets
     * @param mylistType Either "Tweet" for tweetlist, or "Word" for wordlist
     * @return Array of possible dummy options for spinners in spinner quizzes
     *
     * @see #getFillintheBlanksTweetsForAWordList(MyListEntry, ColorThresholds, String, Integer)
     * @see #getFillintheBlanksTweetsForATweetList(MyListEntry, ColorThresholds, String, Integer)
     * @see #getFillintheBlanksTweetsForAUser(UserInfo, ColorThresholds, String, Integer)
     * @see com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment
     */
    private ArrayList<String> getDummySpinnerOptionsForList(Object listEntryObject
            , String mylistType) {
        ArrayList<String> dummyOptions = new ArrayList<>();

        try {
            Cursor c;

            if(listEntryObject instanceof MyListEntry) {
                    MyListEntry myListEntry = (MyListEntry) listEntryObject ;
                if(mylistType.equals("Tweet")) {

                    c = sqlOpener.getReadableDatabase().rawQuery(
                            "Select DISTINCT [Kanji] " +
                                    "FROM [Edict] where [_id] in (" +
                                    "SELECT DISTINCT Edict_id as [_id] " +
                                    "FROM "+
                                    "( " +
                                    "SELECT DISTINCT _id as Tweet_id " +
                                    "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                                    " WHERE [Name] = ? and [Sys] = ?  and [_id] in (SELECT DISTINCT Tweet_id FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS + " ) " +
                                    ") as x " +
                                    "LEFT JOIN "  +
                                    "( " +
                                    "SELECT DISTINCT Tweet_id,Edict_id " +
                                    "FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI + " " +
                                    ")  as y " +
                                    " ON x.Tweet_id = y.Tweet_id " +
                                    " ORDER BY RANDOM()  LIMIT 50 " +
                                    ") OR [_id] in (" +
                                    "SELECT DISTINCT [_id] " +
                                    "FROM [XRef] " +
                                    "ORDER BY RANDOM()  LIMIT 25" +
                                    ") " +
                                    "ORDER BY RANDOM()  ",new String[]{myListEntry.getListName()
                                    ,String.valueOf(myListEntry.getListsSys())});
                } else {

                    c = sqlOpener.getReadableDatabase().rawQuery(
                            "Select DISTINCT [Kanji] " +
                                    "FROM [Edict] where [_id] in (" +
                                    "SELECT DISTINCT [_id] " +
                                    "FROM " +  InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES + " " +
                                    " WHERE [Name] = ? and [Sys] = ?  and [_id] in (SELECT DISTINCT Tweet_id FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS + " ) " +
                                    "ORDER BY RANDOM()  LIMIT 50 " +
                                    ") OR [_id] in (" +
                                    "SELECT DISTINCT [_id] " +
                                    "FROM [XRef] " +
                                    "ORDER BY RANDOM()  LIMIT 25" +
                                    ") ORDER BY RANDOM()  ",new String[]{myListEntry.getListName()
                                    ,String.valueOf(myListEntry.getListsSys())

                            });
                }
            } else if(listEntryObject instanceof UserInfo) {
                UserInfo userInfo = (UserInfo) listEntryObject;
                c = sqlOpener.getReadableDatabase().rawQuery(
                        "Select DISTINCT [Kanji] " +
                                "FROM [Edict] where [_id] in (" +
                                    "SELECT DISTINCT Edict_id as _id" +
                                    " FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI + " " +
                                    "WHERE Tweet_id in (" +
                                                "SELECT  DISTINCT _id as [Tweet_id]" +
                                                "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                                                "WHERE [UserId] = ? and [_id] in (SELECT DISTINCT [Edict_id] FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + ")" +
                                                ")" +
                                    " ORDER BY RANDOM()  LIMIT 50 " +
                                ") OR [_id] in (" +
                                "SELECT DISTINCT [_id] " +
                                "FROM [XRef] " +
                                "ORDER BY RANDOM()  LIMIT 25" +
                                ") " +
                                "ORDER BY RANDOM()  ",new String[]{userInfo.getUserId()});

            } else {
                c = sqlOpener.getReadableDatabase().rawQuery(
                        "Select DISTINCT [Kanji] " +
                                "FROM [Edict] where [_id] in (" +
                                "SELECT DISTINCT [_id] " +
                                "FROM [XRef] " +
                                "ORDER BY RANDOM()  LIMIT 60" +
                                ") ORDER BY RANDOM()  ",null);
            }


            if(c.getCount()>0) {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    dummyOptions.add(c.getString(0));
                    c.moveToNext();
                }
                c.close();
            }
        } catch (SQLiteException e){
            Log.e(TAG,"getDummySpinnerOptionsForListFAST Sqlite exception: " + e);
        }

        return dummyOptions;

    }


    /**
     * Pulls array of Tweets for a given TweetList, for use in the {@link com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment}
     * @param myListEntry TweetList that is being quizzed on
     * @param colorThresholds ColorThreshold object
     * @param colorString concatenated string of word colors that are available for this quiz (ex: blue,red,yellow)
     * @param resultLimit max number of results
     * @return array of Tweets that will be used as the dataset for a FillintheBlanks quiz
     *
     * @see com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment
     */
    public ArrayList<Tweet> getFillintheBlanksTweetsForATweetList(MyListEntry myListEntry
            , ColorThresholds colorThresholds
            , String colorString
            , Integer resultLimit) {

        ArrayList<Tweet> savedTweets = new ArrayList<>();
        try {
            Cursor c = sqlOpener.getReadableDatabase().rawQuery("SELECT DISTINCT TweetIds.[Tweet_id]" +
                            ",[MetaData].ScreenName " +
                            ",[MetaData].UserName " +
                            ",[MetaData].[UserId] " +
                            ",[MetaData].[Text] " +
                            ",[MetaData].[Date]" +
                            ",TweetKanji.Edict_id " +
                            ",TweetKanji.Kanji " +
                            ",(CASE WHEN [TweetKanji].[Furigana] is null  then '' else [TweetKanji].[Furigana] end) as [Furigana] " +
                            ",TweetKanji.Definition " +
                            ",TweetKanji.Total " +
                            ",TweetKanji.Correct " +
                            ",TweetKanji.Color " +
                            ",TweetKanji.StartIndex " +
                            ",TweetKanji.EndIndex " +
                            ",TweetKanji.CoreKanjiBlock " +
                            "FROM  " +
                            " ( " +
                          /* Get a list of tweets that contain the kanji we are interested in */

                            " SELECT DISTINCT [Tweet_id]" +
                            " FROM " +
                            "(" +
                                " SELECT DISTINCT a.[Tweet_id]" +
                                ",(CASE WHEN [Total] is NULL THEN 'Grey' " +
                                "WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 'Grey' " +
                                "WHEN CAST(ifnull(b.[Correct],0)  as float)/b.[Total] < " + colorThresholds.getRedThreshold() + "  THEN 'Red' " +
                                "WHEN CAST(ifnull(b.[Correct],0)  as float)/b.[Total] <  " + colorThresholds.getYellowThreshold() + " THEN 'Yellow' " +
                                "ELSE 'Green' END) as [Color]" +
                                "FROM " +
                                "( " +
                                " SELECT DISTINCT Tweet_id" +
                                ",Edict_id " +
                                ",[StartIndex]" +
                                ",[EndIndex]" +
                                ",[CoreKanjiBlock] " +
                                "From " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
                                " WHERE [Edict_id] is not NULL and StartIndex is not NULL and EndIndex is not NULL and EndIndex > StartIndex " +
                                "and Tweet_id in (" +
                                "SELECT DISTINCT _id as [Tweet_id]" +
                                " FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES  + " " +
                                " WHERE [Name] = ? and  [Sys] = ?  and [_id] in (SELECT DISTINCT Tweet_id FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS + " ) " +
                                ") " +

                                ") as a " +
                                "LEFT JOIN " +
                                " (" +
                                "SELECT [_id] as [Edict_id]" +
                                ",sum([Correct]) as [Correct]" +

                                ",sum([Total]) as [Total] FROM [JScoreboard] " +
                                "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + ")" +
                                " GROUP BY [_id]" +
                                ") as b " +
                                "ON a.[Edict_id] = b.[Edict_id] " +
                            ") as [TweetsandColors] " +
                            " WHERE Color in (" + colorString + ") " +
                            ") as [TweetIds] " +

                            " LEFT JOIN " +

                            /* Attach metadata about tweet and user */
                            "( " +
                            "SELECT DISTINCT [TweetData].[Tweet_id]" +
                            ",[TweetData].[Text]" +
                            ",[TweetData].[UserId] " +
                            ",[TweetData].[Date] " +
                            ",[UserData].[ScreenName] " +
                            ",[UserData].[UserName] " +
                            " FROM " +
                            " ( " +
                            "SELECT  DISTINCT [Tweet_id]" +
                            ",[Text]" +
                            ",[UserId] " +
                            ",[CreatedAt]  as [Date] " +
                            " FROM "+ InternalDB.Tables.TABLE_SAVED_TWEETS + " " +
                            ") as [TweetData] " +

                            "LEFT JOIN " +
                            " (" +
                            "SELECT DISTINCT [UserId] " +
                            ", [ScreenName] " +
                            ", [UserName] " +
                            " FROM " + InternalDB.Tables.TABLE_USERS +" " +
                            ") as [UserData] " +
                            "On TweetData.UserId = UserData.UserId " +
                            ") as [MetaData] " +
                            " On TweetIds.Tweet_id = MetaData.Tweet_id " +

                            "LEFT JOIN " +
                           /* Attach all the rows for individual kanji in each saved tweet */
                            " ( " +
                                /* Get a list of  kanji ids and their word scores for each tweet */
                            "SELECT DISTINCT a.[Tweet_id]" +
                            ",a.[Edict_id]" +
                            ",a.[StartIndex]" +
                            ",a.[EndIndex]" +
                            ",a.[CoreKanjiBlock] " +
                            ",(CASE WHEN [Total] is NULL THEN 'Grey' " +
                            "WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 'Grey' " +
                            "WHEN CAST(ifnull(b.[Correct],0)  as float)/b.[Total] < " + colorThresholds.getRedThreshold() + "  THEN 'Red' " +
                            "WHEN CAST(ifnull(b.[Correct],0)  as float)/b.[Total] <  " + colorThresholds.getYellowThreshold() + " THEN 'Yellow' " +
                            "ELSE 'Green' END) as [Color]" +
                            ",c.Furigana as [Furigana]" +
                            ",c.Definition as [Definition] " +
                            ",c.Kanji as [Kanji] " +
                            ",b.[Total] as [Total]" +
                            ",b.[Correct] as [Correct] " +
                            "FROM " +
                            "( " +
                            " SELECT DISTINCT Tweet_id" +
                            ",Edict_id " +
                            ",[StartIndex]" +
                            ",[EndIndex]" +
                            ",[CoreKanjiBlock] " +
                            "From " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
                                " WHERE [Edict_id] is not NULL and StartIndex is not NULL and EndIndex is not NULL and EndIndex > StartIndex " +
                                "and Tweet_id in (" +
                                             "SELECT DISTINCT _id as [Tweet_id]" +
                                            " FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES  + " " +
                                            " WHERE [Name] = ? and  [Sys] = ?  and [_id] in (SELECT DISTINCT Tweet_id FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS + " ) " +
                                ") " +

                            ") as a " +
                            "LEFT JOIN " +
                            " (" +
                            "SELECT [_id] as [Edict_id]" +
                            ",sum([Correct]) as [Correct]" +

                            ",sum([Total]) as [Total] FROM [JScoreboard] " +
                            "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + ")" +
                            " GROUP BY [_id]" +
                            ") as b " +
                            "ON a.[Edict_id] = b.[Edict_id] " +
                            "LEFT JOIN " +
                            " (" +
                            "SELECT DISTINCT [_id] as [Edict_id]" +
                            ",[Kanji]" +
                            ",[Furigana]" +
                            ",[Definition]" +
                            "FROM [Edict] " +

                            "WHERE [_id] in (" +
                            "SELECT DISTINCT [Edict_id] FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  +

                            ") " +
                            ") as c " +
                            "ON a.[Edict_id] = c.[Edict_id] " +

                            " ) as TweetKanji " +
                            "On TweetIds.Tweet_id = TweetKanji.Tweet_id " +

                            "Order by TweetIds.[Tweet_id] asc,TweetKanji.StartIndex asc"
                    ,  new String[]{myListEntry.getListName()
                            ,String.valueOf(myListEntry.getListsSys())
                            ,myListEntry.getListName()
                            ,String.valueOf(myListEntry.getListsSys())});

            /* The query pulls a list of tweetdata paired with each parsed-kanji in the tweet, resulting in
            * multiple duplicate lines of tweetdata. So the cursor ony adds tweet data once when a new tweetid is found. Meanwhile
            * the kanji data for each row is added to the tweet as WordEntry objects, and when a new tweetid appears (or the cursor finishes)
            * the final Tweet is added to the "savedTweets" array. */
            if(c.getCount()>0) {
                c.moveToFirst();
                String currentTweetId = c.getString(0);
                Tweet tweet = new Tweet();
                while (!c.isAfterLast())
                {

                    if(BuildConfig.DEBUG) {
                        Log.d(TAG,"adding setIdString: " + c.getString(0));
                        Log.d(TAG,"adding tweet text: " + c.getString(4));
                        Log.d(TAG,"adding setName: " + c.getString(2));
                        Log.d(TAG,"adding setScreen_name: " + c.getString(1));
                        Log.d(TAG,"adding WORDID: " + c.getString(6));
                        Log.d(TAG,"adding KANJI: " + c.getString(7));
                    }

                    if(c.isFirst()) {
                        tweet.setIdString(c.getString(0));
                        tweet.setCreatedAt(c.getString(5));
                        tweet.getUser().setUserId(c.getString(3));
                        tweet.setText(c.getString(4));
                        tweet.getUser().setScreen_name(c.getString(1));
                        tweet.getUser().setName(c.getString(2));
                    } else if(!currentTweetId.equals(c.getString(0))){

                        //FLush old tweet
                        Tweet tweetToAdd = new Tweet(tweet);
                        if(tweet.getWordEntries()!=null && tweet.getWordEntries().size()>0) {
                            savedTweets.add(tweetToAdd);
                        }

                        currentTweetId = c.getString(0);
                        tweet = new Tweet();
                        tweet.setIdString(c.getString(0));
                        tweet.setCreatedAt(c.getString(5));
                        tweet.getUser().setUserId(c.getString(3));
                        tweet.setText(c.getString(4));
                        tweet.getUser().setScreen_name(c.getString(1));
                        tweet.getUser().setName(c.getString(2));
                    }

                    /* Add word entries for tweet (to create links, spinners etc) */
                    if(c.getString(6) != null && c.getString(7) != null) {
                        WordEntry wordEntry = new WordEntry(c.getInt(6)
                                , c.getString(7)
                                , c.getString(8)
                                , c.getString(9)
                                , c.getInt(10)
                                , c.getInt(11)
                                , c.getString(12)
                                , c.getInt(13)
                                , c.getInt(14));
                        wordEntry.setCoreKanjiBlock(c.getString(15));
                        if(BuildConfig.DEBUG){Log.d(TAG,"adding word entry: " + c.getString(6) + " - " + c.getString(7));}
                        tweet.addWordEntry(wordEntry);
                    }

                    if(c.isLast()) {
                        Tweet lastTweet = new Tweet(tweet);
                        if(tweet.getWordEntries()!=null && tweet.getWordEntries().size()>0) {
                            savedTweets.add(lastTweet);
                        }
                    }
                    c.moveToNext();
                }

                /* Randomize dataset*/
                Collections.shuffle(savedTweets);

            /* If there are not enough unique results to fill out the quiz size, start adding random
            entries from the dataset to itself until there are enough tweets . */
                if(savedTweets.size()>0) {

                    while (savedTweets.size()<resultLimit) {
                        ArrayList<Tweet> tmpSavedTweets = new ArrayList<>(savedTweets);
                        Collections.shuffle(tmpSavedTweets);

                        for(int i= 0;i<tmpSavedTweets.size() && savedTweets.size()<resultLimit;i++) {
                            //Create deep copy of Tweet via parcel
                            Parcel p = Parcel.obtain();
                            p.writeValue(tmpSavedTweets.get(i));
                            p.setDataPosition(0);
                            Tweet randomTweet = (Tweet)p.readValue(Tweet.class.getClassLoader());
                            p.recycle();
                            savedTweets.add(randomTweet);
                        }

                    }
                }

                /* Now assign spinners to each tweet in the list */
                ArrayList<String> dummySpinnerRandomWords  = getDummySpinnerOptionsForList(myListEntry,"Tweet");
                for(Tweet savedTweet : savedTweets) {
                    setRandomSpinnersForTweetFAST(savedTweet,dummySpinnerRandomWords,colorString,null);
                }

            } else {if(BuildConfig.DEBUG) {Log.d(TAG,"c.getcount was 0!!");}}
            c.close();



        } catch (SQLiteException e){
            Log.e(TAG,"getFillintheBlanksTweetsForATweetList Sqlite exception: " + e);
        }
        return savedTweets;
    }



    /**
     * Pulls array of Tweets for the saved tweets of a single twitter user, for use in the {@link com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment}
     * @param userInfo UserInfo for the user whose saved tweets will be quizzed
     * @param colorThresholds ColorThreshold object
     * @param colorString concatenated string of word colors that are available for this quiz (ex: blue,red,yellow)
     * @param resultLimit max number of results
     * @return array of Tweets that will be used as the dataset for a FillintheBlanks quiz
     *
     * @see com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment
     */
    public ArrayList<Tweet> getFillintheBlanksTweetsForAUser(UserInfo userInfo
            , ColorThresholds colorThresholds
            , String colorString
            , Integer resultLimit) {

        ArrayList<Tweet> savedTweets = new ArrayList<>();
        try {
            Cursor c = sqlOpener.getReadableDatabase().rawQuery("SELECT DISTINCT TweetIds.[Tweet_id]" +
                            ",[MetaData].ScreenName " +
                            ",[MetaData].UserName " +
                            ",[MetaData].[UserId] " +
                            ",[MetaData].[Text] " +
                            ",[MetaData].[Date]" +
                            ",TweetKanji.Edict_id " +
                            ",TweetKanji.Kanji " +
                            ",(CASE WHEN [TweetKanji].[Furigana] is null  then '' else [TweetKanji].[Furigana] end) as [Furigana] " +
                            ",TweetKanji.Definition " +
                            ",TweetKanji.Total " +
                            ",TweetKanji.Correct " +
                            ",TweetKanji.Color " +
                            ",TweetKanji.StartIndex " +
                            ",TweetKanji.EndIndex " +
                            ",TweetKanji.CoreKanjiBlock " +
                            "FROM  " +
                            " ( " +
                          /* Get a list of tweets that contain the kanji we are interested in */
                            "Select DISTINCT Tweet_id " +
                            " FROM (" +

                                " SELECT DISTINCT [Tweet_id]" +
                                " FROM " +
                                "(" +
                                " SELECT DISTINCT a.[Tweet_id]" +
                                ",(CASE WHEN [Total] is NULL THEN 'Grey' " +
                                "WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 'Grey' " +
                                "WHEN CAST(ifnull(b.[Correct],0)  as float)/b.[Total] < " + colorThresholds.getRedThreshold() + "  THEN 'Red' " +
                                "WHEN CAST(ifnull(b.[Correct],0)  as float)/b.[Total] <  " + colorThresholds.getYellowThreshold() + " THEN 'Yellow' " +
                                "ELSE 'Green' END) as [Color]" +
                                ",b.[Total] as [Total]" +
                                ",b.[Correct] as [Correct] " +
                                "FROM " +
                                "( " +
                                " SELECT Tweet_id" +
                                ",Edict_id " +
                                ",[StartIndex]" +
                                ",[EndIndex]" +
                                ",[CoreKanjiBlock] " +
                                "From " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
                                " WHERE [Edict_id] is not NULL and StartIndex is not NULL and EndIndex is not NULL and EndIndex > StartIndex " +
                                " and Tweet_id in (" +
                                    "SELECT  DISTINCT  _id as [Tweet_id]" +
                                    " FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                                    " WHERE [UserId] = ? and [_id] in (SELECT DISTINCT Tweet_id FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS + " ) " +

                                     ") " +

                                ") as a " +
                                "LEFT JOIN " +
                                " (" +
                                    "SELECT [_id] as [Edict_id]" +
                                    ",sum([Correct]) as [Correct]" +

                                    ",sum([Total]) as [Total] FROM [JScoreboard] " +
                                    "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + ")" +
                                    " GROUP BY [_id]" +
                                ") as b " +
                                "ON a.[Edict_id] = b.[Edict_id] " +
                                ") as [TweetsandColors] " +
                                " WHERE Color in (" + colorString + ") " +
                                ") "  +
                            ") as [TweetIds] " +

                            " LEFT JOIN " +

                            /* Attach metadata about tweet and user */
                            "( " +
                                "SELECT DISTINCT [TweetData].[Tweet_id]" +
                                ",[TweetData].[Text]" +
                                ",[TweetData].[UserId] " +
                                ",[TweetData].[Date] " +
                                ",[UserData].[ScreenName] " +
                                ",[UserData].[UserName] " +
                                " FROM " +
                                " ( " +
                                    "SELECT  DISTINCT [Tweet_id]" +
                                    ",[Text]" +
                                    ",[UserId] " +
                                    ",[CreatedAt]  as [Date] " +
                                    " FROM "+ InternalDB.Tables.TABLE_SAVED_TWEETS + " " +
                                        " WHERE [UserId] = ? " +
                                ") as [TweetData] " +

                                "LEFT JOIN " +
                                " (" +
                                    "SELECT DISTINCT [UserId] " +
                                    ", [ScreenName] " +
                                    ", [UserName] " +
                                    " FROM " + InternalDB.Tables.TABLE_USERS +" " +
                                ") as [UserData] " +
                                "On TweetData.UserId = UserData.UserId " +
                            ") as [MetaData] " +
                            " On TweetIds.Tweet_id = MetaData.Tweet_id " +


                            "LEFT JOIN " +
                           /* Attach all the rows for individual kanji in each saved tweet */
                            " ( " +
                                /* Get a list of  kanji ids and their word scores for each tweet */
                                "SELECT a.[Tweet_id]" +
                                ",a.[Edict_id]" +
                                ",a.[StartIndex]" +
                                ",a.[EndIndex]" +
                                ",a.[CoreKanjiBlock] " +
                                ",(CASE WHEN [Total] is NULL THEN 'Grey' " +
                                "WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 'Grey' " +
                                "WHEN CAST(ifnull(b.[Correct],0)  as float)/b.[Total] < " + colorThresholds.getRedThreshold() + "  THEN 'Red' " +
                                "WHEN CAST(ifnull(b.[Correct],0)  as float)/b.[Total] <  " + colorThresholds.getYellowThreshold() + " THEN 'Yellow' " +
                                "ELSE 'Green' END) as [Color]" +
                                ",c.Furigana as [Furigana]" +
                                ",c.Definition as [Definition] " +
                                ",c.Kanji as [Kanji] " +
                                ",b.[Total] as [Total]" +
                                ",b.[Correct] as [Correct] " +
                                "FROM " +
                                "( " +
                                " SELECT Tweet_id" +
                                ",Edict_id " +
                                ",[StartIndex]" +
                                ",[EndIndex]" +
                                ",[CoreKanjiBlock] " +
                                "From " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
                                " WHERE [Edict_id] is not NULL and StartIndex is not NULL and EndIndex is not NULL and EndIndex > StartIndex " +
                                "and Tweet_id in (" +
                                    "SELECT  DISTINCT  _id as [Tweet_id]" +
                                    "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                                    "WHERE [UserId] = ? and [_id] in (SELECT DISTINCT Tweet_id FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS + " ) " +
                                    ") " +
                                ") as a " +
                                "LEFT JOIN " +
                                " (" +
                                "SELECT [_id] as [Edict_id]" +
                                ",sum([Correct]) as [Correct]" +

                                ",sum([Total]) as [Total] FROM [JScoreboard] " +
                                "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + ")" +
                                " GROUP BY [_id]" +
                                ") as b " +
                                "ON a.[Edict_id] = b.[Edict_id] " +
                                "LEFT JOIN " +
                                " (" +
                                "SELECT DISTINCT [_id] as [Edict_id]" +
                                ",[Kanji]" +
                                ",[Furigana]" +
                                ",[Definition]" +
                                "FROM [Edict] " +

                                "WHERE [_id] in (" +

                                    "SELECT DISTINCT[Edict_id]" +
                                    " FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI + " " +
                                    " WHERE  [Tweet_id] in (   " +
                                            "SELECT  DISTINCT  _id " +
                                            "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                                            "WHERE [UserId] = ? " +
                                            ")" +

                                    ") " +
                                ") as c " +
                                "ON a.[Edict_id] = c.[Edict_id] " +

                            " ) as TweetKanji " +
                            "On TweetIds.Tweet_id = TweetKanji.Tweet_id " +


                            "Order by date(MetaData.Date) Desc,TweetIds.[Tweet_id] asc,TweetKanji.StartIndex asc"
                    ,  new String[]{userInfo.getUserId()
                            ,userInfo.getUserId()
                            ,userInfo.getUserId()
                    ,userInfo.getUserId()});

            /* The query pulls a list of tweetdata paired with each parsed-kanji in the tweet, resulting in
            * multiple duplicate lines of tweetdata. So the cursor ony adds tweet data once when a new tweetid is found. Meanwhile
            * the kanji data for each row is added to the tweet as WordEntry objects, and when a new tweetid appears (or the cursor finishes)
            * the final Tweet is added to the "savedTweets" array. */
            if(BuildConfig.DEBUG){Log.i(TAG,"C.GET COUNT = " + c.getCount());}
            if(c.getCount()>0) {
                c.moveToFirst();
                String currentTweetId = c.getString(0);
                Tweet tweet = new Tweet();
                while (!c.isAfterLast())
                {
                    if(c.isFirst()) {
                        tweet.setIdString(c.getString(0));
                        tweet.setCreatedAt(c.getString(5));
                        tweet.getUser().setUserId(c.getString(3));
                        tweet.setText(c.getString(4));
                        tweet.getUser().setScreen_name(c.getString(1));
                        tweet.getUser().setName(c.getString(2));
                    } else if(!currentTweetId.equals(c.getString(0))){

                        //FLush old tweet
                        Tweet tweetToAdd = new Tweet(tweet);
                        if(tweet.getWordEntries()!=null  && tweet.getWordEntries().size()>0) {
                            savedTweets.add(tweetToAdd);
                        }

                        currentTweetId = c.getString(0);
                        tweet = new Tweet();
                        tweet.setIdString(c.getString(0));
                        tweet.setCreatedAt(c.getString(5));
                        tweet.getUser().setUserId(c.getString(3));
                        tweet.setText(c.getString(4));
                        tweet.getUser().setScreen_name(c.getString(1));
                        tweet.getUser().setName(c.getString(2));
                    }

                    if(c.getString(6) != null && c.getString(7) != null) {
                        WordEntry wordEntry = new WordEntry(c.getInt(6)
                                , c.getString(7)
                                , c.getString(8)
                                , c.getString(9)
                                , c.getInt(10)
                                , c.getInt(11)
                                , c.getString(12)
                                , c.getInt(13)
                                , c.getInt(14));
                        wordEntry.setCoreKanjiBlock(c.getString(15));
                        tweet.addWordEntry(wordEntry);
                    }


                    if(c.isLast()) {
                        Tweet lastTweet = new Tweet(tweet);
                        if(tweet.getWordEntries()!=null  && tweet.getWordEntries().size()>0) {
                            savedTweets.add(lastTweet);
                        }
                    }
                    c.moveToNext();
                }

                 /* Randomize dataset*/
                Collections.shuffle(savedTweets);

            /* If there are not enough unique results to fill out the quiz size, start adding random
            entries from the dataset to itself until there are enough tweets . */
                if(savedTweets.size()>0) {

                    while (savedTweets.size()<resultLimit) {
                        ArrayList<Tweet> tmpSavedTweets = new ArrayList<>(savedTweets);
                        Collections.shuffle(tmpSavedTweets);

                        for(int i= 0;i<tmpSavedTweets.size() && savedTweets.size()<resultLimit;i++) {
                            //Create deep copy of Tweet via parcel
                            Parcel p = Parcel.obtain();
                            p.writeValue(tmpSavedTweets.get(i));
                            p.setDataPosition(0);
                            Tweet randomTweet = (Tweet)p.readValue(Tweet.class.getClassLoader());
                            p.recycle();
                            savedTweets.add(randomTweet);
                        }

                    }
                }

            /* Now assign spinners to each tweet in the list */
                ArrayList<String> dummySpinnerRandomWords  = getDummySpinnerOptionsForList(userInfo,"Tweet");
                for(Tweet savedTweet : savedTweets) {
                    setRandomSpinnersForTweetFAST(savedTweet,dummySpinnerRandomWords,colorString,null);
                }

            } else {
                if(BuildConfig.DEBUG) {Log.e(TAG,"getFillintheBlanksTweetsForASingleUserSavedTweetList c.getcount was 0!!");}
            }
            c.close();

        } catch (SQLiteException e){
            Log.e(TAG,"getFillintheBlanksTweetsForASingleUserSavedTweetList Sqlite exception: " + e);
        }

        return savedTweets;
    }


    /**
     * Pulls array of Tweets for a given WordList, for use in the {@link com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment}
     * @param myListEntry WordList that is being quizzed on
     * @param colorThresholds ColorThreshold object
     * @param colorString concatenated string of word colors that are available for this quiz (ex: blue,red,yellow)
     * @param resultLimit max number of results
     * @return array of Tweets that will be used as the dataset for a FillintheBlanks quiz
     *
     * @see com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment
     */
    public ArrayList<Tweet> getFillintheBlanksTweetsForAWordList(MyListEntry myListEntry
            , ColorThresholds colorThresholds
            , String colorString
            , Integer resultLimit) {

        if(BuildConfig.DEBUG) {
            Log.d(TAG,"mylist entry name: " + myListEntry.getListName());
            Log.d(TAG,"mylist entry sys: " + myListEntry.getListsSys());
            Log.d(TAG,"colorString: " + colorString);
            Log.d(TAG,"resultLimit: " + resultLimit);
        }

        ArrayList<Tweet> savedTweets = new ArrayList<>();

        /*Get a list of possible spinner kanji ids, to be used in assigning
            random spinners to the tweet (setSpinnersForTweetWithMyListWords).
             IF there are none, return empty and show error toast
        */
        ArrayList<Integer> possibleSpinners = getIdsForWordList(myListEntry);
        if(possibleSpinners.size() == 0) {
            return savedTweets;
        }

        String limit = "LIMIT " + String.valueOf(resultLimit);

        try {

            Cursor c = sqlOpener.getReadableDatabase().rawQuery("SELECT TweetIds.[Tweet_id]" +
                            ",[MetaData].ScreenName " +
                            ",[MetaData].UserName " +
                            ",[MetaData].[UserId] " +
                            ",[MetaData].[Text] " +
                            ",[MetaData].[Date]" +
                            ",TweetKanjiData.Edict_id " +
                            ",TweetKanjiData.Kanji " +
                            ",(CASE WHEN [TweetKanjiData].[Furigana] is null  then '' else [TweetKanjiData].[Furigana] end) as [Furigana] " +
                            ",TweetKanjiData.Definition " +
                            ",TweetKanjiData.Total " +
                            ",TweetKanjiData.Correct " +
                            ",TweetKanjiData.Color " +
                            ",TweetKanjiData.StartIndex " +
                            ",TweetKanjiData.EndIndex " +
                            ",TweetKanjiData.CoreKanjiBlock " +
                            "FROM  " +
                            " ( " +
                            /* Now to pull together ListName, Tweet and the totals (by color) of the kanji in those tweets */
                            "SELECT DISTINCT ListsTweetsAndAllKanjis.[Tweet_id] "+

                            ",SUM([Grey]) + SUM([Red]) + SUM([Yellow]) + SUM([Green]) as [Total] " +
                            ",SUM([Grey]) as [Grey]" +
                            ",SUM([Red]) as [Red]" +
                            ",SUM([Yellow]) as [Yellow]" +
                            ",SUM([Green]) as [Green] " +
                            "FROM (" +

                                    /* Now we have a big collection of list metadata (tweetlists), and all the kanji scores and colors for
                                     each kanji (kanjilists) */
                            " Select TweetLists.[Tweet_id] " +

                            ",(CASE WHEN [Total] is not NULL AND [Total] < " + colorThresholds.getGreyThreshold() + " THEN 1 ELSE 0 END) as [Grey] " +
                            ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] < " + colorThresholds.getRedThreshold() + "  THEN 1  ELSE 0 END) as [Red] " +
                            ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and ([Percent] >= " + colorThresholds.getRedThreshold() + "  and [Percent] <  " + colorThresholds.getYellowThreshold() + ") THEN 1  ELSE 0 END) as [Yellow] " +
                            ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] >= " + colorThresholds.getYellowThreshold() + " THEN 1 ELSE 0 END) as [Green] " +

                            "FROM " +
                                "(" +
                                            /* Select all tweets that contain words from the MyList */
                                            "SELECT DISTINCT [Tweet_id]" +
                                            " FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
                                            "WHERE [Edict_id] in (" +

                                                    /* We want Tweet_ids for tweets that contain the words in the word list that is being quizzed. AND, we only
                                                     * want tweet_ids for tweets that contain words for the given color string */
                                                    "SELECT DISTINCT [_id] " +
                                                    " FROM " +
                                                    " (" +
                                                        "SELECT  x.[_id]" +
                                                        " ,(CASE WHEN [Total] is NULL THEN 'Grey' " +
                                                        "WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 'Grey' " +
                                                        "WHEN CAST(ifnull([Correct],0)  as float)/[Total] < " + colorThresholds.getRedThreshold() + "  THEN 'Red' " +
                                                        "WHEN CAST(ifnull([Correct],0)  as float)/[Total] <  " + colorThresholds.getYellowThreshold() + " THEN 'Yellow' " +
                                                        "ELSE 'Green' END) as [Color] " +
                                                        "FROM " +
                                                        "(" +
                                                            " SELECT DISTINCT [_id] " +
                                                            " FROM " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES +  "  " +
                                                            " WHERE [Name] = ?  and [Sys] = " + myListEntry.getListsSys() +" and [_id] in (SELECT DISTINCT Tweet_id FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS + " ) " +
                                                        ") as x " +
                                                        "LEFT JOIN " +
                                                        "(" +
                                                            "SELECT [_id]" +
                                                            ",sum([Correct]) as [Correct]" +
                                                            ",sum([Total]) as [Total]  " +
                                                            " FROM [JScoreboard] " +
                                                            " WHERE [_id] IN (" +
                                                                " SELECT [_id] " +
                                                                " FROM " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES +  "  " +
                                                                " WHERE [Name] = ?  and [Sys] = " + myListEntry.getListsSys() +" " +

                                                            ") " +
                                                         "GROUP BY [_id]" +
                                                        ") as y " +
                                                        "ON x.[_id] = y.[_id] " +

                                                     ") as [IdsForWordsWithColorString] " +
                                                    " Where Color in (" + colorString + ") " +
                                        ") " +

                                ") as TweetLists " +
                            " LEFT JOIN " +
                            " ( " +

                            /* Get a list of  kanji ids and their word scores for each tweet */
                            "SELECT a.[Tweet_id]" +
                            ",a.[Edict_id]" +
                            ",ifnull(b.[Total],0) as [Total] " +
                            ",ifnull(b.[Correct],0)  as [Correct]" +
                            ",CAST(ifnull(b.[Correct],0)  as float)/b.[Total] as [Percent] " +
                            "FROM " +
                            "( " +
                                /*Pull saved tweet data for only those tweets that have favorite list words we are interested in */
                            " SELECT Tweet_id" +
                            ",Edict_id " +
                            "From " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
                                " WHERE Tweet_id in(" +
                                    "SELECT DISTINCT [Tweet_id]" +
                                    " FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
                                    "WHERE [Edict_id] in (" +
                                         "SELECT DISTINCT [_id]" +
                                        " FROM " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES + " " +
                                        " WHERE [Name] = ? and  [Sys] = "+ myListEntry.getListsSys() +"  and [_id] in (SELECT DISTINCT Tweet_id FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS + " ) " +
                                             ") " +
                                        ") " +
                            ") as a " +
                            "LEFT JOIN " +
                                " (" +
                                    "SELECT [_id] as [Edict_id]" +
                                    ",sum([Correct]) as [Correct]" +
                                    ",sum([Total]) as [Total] FROM [JScoreboard] " +
                                    " where [_id] in (SELECT DISTINCT [Edict_id] FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + ")" +
                                    " GROUP BY [_id]" +
                                ") as b " +
                            "ON a.[Edict_id] = b.[Edict_id] " +

                            " ) as TweetKanji " +
                            "On TweetLists.Tweet_id = TweetKanji.Tweet_id " +

                            ") as [ListsTweetsAndAllKanjis] " +
                            "GROUP BY [Tweet_id]" +
                            "ORDER BY RANDOM() " + limit + " " +
                            ") as [TweetIds] " +

                            " LEFT JOIN " +

                            /* Attach metadata about tweet and user */
                            "( " +
                            "SELECT DISTINCT [TweetData].[Tweet_id]" +
                            ",[TweetData].[Text]" +
                            ",[TweetData].[UserId] " +
                            ",[TweetData].[Date] " +
                            ",[UserData].[ScreenName] " +
                            ",[UserData].[UserName] " +
                            " FROM " +
                            " ( " +
                                "SELECT  DISTINCT [Tweet_id]" +
                                ",[Text]" +
                                ",[UserId] " +
                                ",[CreatedAt]  as [Date] " +
                                " FROM "+ InternalDB.Tables.TABLE_SAVED_TWEETS + " " +
                            ") as [TweetData] " +

                            "LEFT JOIN " +
                            " (" +
                            "SELECT DISTINCT [UserId] " +
                            ", [ScreenName] " +
                            ", [UserName] " +
                            " FROM " + InternalDB.Tables.TABLE_USERS +" " +
                            ") as [UserData] " +
                            "On TweetData.UserId = UserData.UserId " +
                            ") as [MetaData] " +
                            " On TweetIds.Tweet_id = MetaData.Tweet_id " +


                            "LEFT JOIN " +
                           /* Attach all the rows for individual kanji in each saved tweet */
                            " ( " +
                                /* Get a list of  kanji ids and their word scores for each tweet */
                            "SELECT a.[Tweet_id]" +
                            ",a.[Edict_id]" +
                            ",a.[StartIndex]" +
                            ",a.[EndIndex]" +
                            ",a.[CoreKanjiBlock] " +
                            ",(CASE WHEN [Total] is NULL THEN 'Grey' " +
                            "WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 'Grey' " +
                            "WHEN CAST(ifnull(b.[Correct],0)  as float)/b.[Total] < " + colorThresholds.getRedThreshold() + "  THEN 'Red' " +
                            "WHEN CAST(ifnull(b.[Correct],0)  as float)/b.[Total] <  " + colorThresholds.getYellowThreshold() + " THEN 'Yellow' " +
                            "ELSE 'Green' END) as [Color]" +
                            ",c.Furigana as [Furigana]" +
                            ",c.Definition as [Definition] " +
                            ",c.Kanji as [Kanji] " +
                            ",b.[Total] as [Total]" +
                            ",b.[Correct] as [Correct] " +
                            "FROM " +
                            "( " +
                            " SELECT Tweet_id" +
                            ",Edict_id " +
                            ",[StartIndex]" +
                            ",[EndIndex]" +
                            ",[CoreKanjiBlock] " +
                            "From " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
                            " WHERE [Edict_id] is not NULL and StartIndex is not NULL and EndIndex is not NULL and EndIndex > StartIndex " +
                            "and Tweet_id in (" +
                                /* Get a list of tweets that contain the kanji we are interested in */
                            "SELECT DISTINCT [Tweet_id]" +
                            " FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
                            " WHERE [Edict_id] in (" +
                            "SELECT DISTINCT [_id]" +
                            " FROM " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES + " " +
                            " WHERE [Name] = ? and  [Sys] = " + myListEntry.getListsSys() +"  and [_id] in (SELECT DISTINCT Tweet_id FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS + " ) " +
                            ") " +
                            ") " +
                            ") as a " +
                            "LEFT JOIN " +
                            " (" +
                            "SELECT [_id] as [Edict_id]" +
                            ",sum([Correct]) as [Correct]" +
                            ",sum([Total]) as [Total] FROM [JScoreboard] " +
                            "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + ")" +
                            " GROUP BY [_id]" +
                            ") as b " +
                            "ON a.[Edict_id] = b.[Edict_id] " +
                            "LEFT JOIN " +
                            " (" +
                            "SELECT DISTINCT [_id] as [Edict_id]" +
                            ",[Kanji]" +
                            ",[Furigana]" +
                            ",[Definition]" +
                            "FROM [Edict] " +
                            "WHERE [_id] in (" +

                            " SELECT DISTINCT Edict_id " +
                            "From " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
                            " WHERE [Edict_id] is not NULL " +


                            ") " +
                            ") as c " +
                            "ON a.[Edict_id] = c.[Edict_id] " +

                            " ) as TweetKanjiData " +
                            "On TweetIds.Tweet_id = TweetKanjiData.Tweet_id " +


                            "Order by date(MetaData.Date) Desc,TweetIds.[Tweet_id] asc,TweetKanjiData.StartIndex asc"
                    ,  new String[]{myListEntry.getListName()
                            ,myListEntry.getListName()
                            ,myListEntry.getListName()
                            ,myListEntry.getListName()
 });


            /* The query pulls a list of tweetdata paired with each parsed-kanji in the tweet, resulting in
            * multiple duplicate lines of tweetdata. So the cursor ony adds tweet data once when a new tweetid is found. Meanwhile
            * the kanji data for each row is added to the tweet as WordEntry objects, and when a new tweetid appears (or the cursor finishes)
            * the final Tweet is added to the "savedTweets" array. */
            if(c.getCount()>0) {
                c.moveToFirst();
                String currentTweetId = c.getString(0);
                Tweet tweet = new Tweet();
                while (!c.isAfterLast())
                {
                    if(c.isFirst()) {
                        tweet.setIdString(c.getString(0));
                        tweet.setCreatedAt(c.getString(5));
                        tweet.getUser().setUserId(c.getString(3));
                        tweet.setText(c.getString(4));
                        tweet.getUser().setScreen_name(c.getString(1));
                        tweet.getUser().setName(c.getString(2));
                    } else if(!currentTweetId.equals(c.getString(0))){

                            //FLush old tweet
                            if(tweet.getWordEntries()!=null  && tweet.getWordEntries().size()>0) {
                                if(BuildConfig.DEBUG) {
                                    Log.d(TAG,"Flushing tweet: " + tweet.getText());
                                    Log.d(TAG,"number of kanji in tweet: " + tweet.getWordEntries().size());
                                }
                                savedTweets.add(new Tweet(tweet));
                            }

                            currentTweetId = c.getString(0);
                            tweet = new Tweet();
                            tweet.setIdString(c.getString(0));
                            tweet.setCreatedAt(c.getString(5));
                            tweet.getUser().setUserId(c.getString(3));
                            tweet.setText(c.getString(4));
                            tweet.getUser().setScreen_name(c.getString(1));
                            tweet.getUser().setName(c.getString(2));
                        }
                    if(BuildConfig.DEBUG) {
                        Log.d(TAG,"adding word entry: " + c.getString(6) + " - " + c.getString(7));
                    }

                    if(c.getString(6) != null && c.getString(7) != null) {
                        WordEntry wordEntry = new WordEntry(c.getInt(6)
                                ,c.getString(7)
                                ,c.getString(8)
                                ,c.getString(9)
                                ,c.getInt(10)
                                ,c.getInt(11)
                                ,c.getString(12)
                                ,c.getInt(13)
                                ,c.getInt(14));
                        wordEntry.setCoreKanjiBlock(c.getString(15));
                        tweet.addWordEntry(wordEntry);
                    }

                    if(c.isLast()) {
                        if(BuildConfig.DEBUG) {
                            Log.d(TAG,"Flushing last tweet: " + tweet.getText());
                            Log.d(TAG,"last  number of kanji in tweet: " + tweet.getWordEntries().size());
                        }
                        if(tweet.getWordEntries()!=null  && tweet.getWordEntries().size()>0) {
                            savedTweets.add(new Tweet(tweet));
                        }
                    }
                    c.moveToNext();
                }

                // Randomize dataset
                Collections.shuffle(savedTweets);

            /* If there are not enough unique results to fill out the quiz size, start adding random
            entries from the dataset to itself until there are enough tweets . */
                if(savedTweets.size()>0) {

                    while (savedTweets.size()<resultLimit) {
                        ArrayList<Tweet> tmpSavedTweets = new ArrayList<>(savedTweets);
                        Collections.shuffle(tmpSavedTweets);

                        for(int i= 0;i<tmpSavedTweets.size() && savedTweets.size()<resultLimit;i++) {
                            //Create deep copy of Tweet via parcel
                            Parcel p = Parcel.obtain();
                            p.writeValue(tmpSavedTweets.get(i));
                            p.setDataPosition(0);
                            Tweet randomTweet = (Tweet)p.readValue(Tweet.class.getClassLoader());
                            p.recycle();
                            savedTweets.add(randomTweet);
                        }

                    }
                }

            /* Now assign spinners to each tweet in the list */
                ArrayList<String> dummySpinnerRandomWords  = getDummySpinnerOptionsForList(myListEntry,"Word");
                for(Tweet savedTweet : savedTweets) {
                    setRandomSpinnersForTweetFAST(savedTweet,dummySpinnerRandomWords,colorString,possibleSpinners);
                }

            } else {if(BuildConfig.DEBUG) {Log.d(TAG,"c.getcount was 0!!");}}
            c.close();

        } catch (SQLiteException e){
            Log.e(TAG,"getFillintheBlanksTweetsForWordList Sqlite exception: " + e);
        }
        return savedTweets;
    }

    /**
     * Pulls edict ids of all saved kanji in a WordList. These will be used as "possible spinners" in the getFillintheBlanksTweetsforAWordList method.
     *
     * @param myListEntry WordList whose saved kanji's ids will be pulled
     * @return array of edict ids of all saved kanji in a WordList
     */
    public ArrayList<Integer> getIdsForWordList(MyListEntry myListEntry) {
        ArrayList<Integer> ids = new ArrayList<>();
        try {
            Cursor c = sqlOpener.getReadableDatabase().rawQuery(
                    "SELECT DISTINCT [_id]" +
                            " FROM " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES + " " +
                            " WHERE [Name] = ? and [Sys] = ? ",new String[]{myListEntry.getListName() + " and [_id] in (SELECT DISTINCT Tweet_id FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS + " ) "
                            ,String.valueOf(myListEntry.getListsSys())});
            if(c.getCount()>0) {
                c.moveToFirst();

                while (!c.isAfterLast()) {
                    ids.add(c.getInt(0));
                    c.moveToNext();
                }

                c.close();
            }

        } catch (SQLiteException e){
            Log.e(TAG,"getIdsForWordList Sqlite exception: " + e);
        } catch (Exception e) {
            Log.e(TAG,"getIdsForWordList generic exception: " + e);
        }

        return ids;
    }


/**
 * Decides which WordEntries within a Tweet will be designated as "spinners", meaning that they will be
 * pickable dropdowns in the FillintheBlanks quiz. More than one possible "spinner" word may appear in a tweet, so the number
 * of spinners is assigned via a weighted average type deal, with a max of 3 spinners per question. The spinner is designated
 * as such by turning the "isSpinner" boolean in the WordEntry object for the word to "true"
 * @param tweet Tweet containg the WordEntries
 * @param poolOfFalseAnswers list of wrong answers, of which 3 will be randomly chosen as dummy answers for the spinner dropdown
 * @param colorString concatenated string of word colors that are available for this quiz (ex: blue,red,yellow)
 * @param wordEntryIdsInWordList For {@link #getFillintheBlanksTweetsForAWordList(MyListEntry, ColorThresholds, String, Integer)}, the
 *                               spinner words within the tweet must be from the wordlist. So this is a list of those word ids that are OK to
 *                               become spinners. WordEntries are checked against both the colorString param and this list before they are accepted as spinners
 */
    private void setRandomSpinnersForTweetFAST(Tweet tweet
            , ArrayList<String> poolOfFalseAnswers
            , String colorString
            , @Nullable ArrayList<Integer> wordEntryIdsInWordList) {
        ArrayList<WordEntry> wordEntries = tweet.getWordEntries();
        ArrayList<Integer> spinnerAddedIndex = new ArrayList<>();

        /* Determine maxiumum number of spinners that can be added to the tweet, from 1 - 3,
        with a 60% chance of 1, 30% chance of 2 and a 10 % chance of 3*/
        int[] possibleSpinnerLimits = new int[]{1,1,1,1,1,1,2,2,2,3};
        int spinnerLimit = possibleSpinnerLimits[(new Random()).nextInt(possibleSpinnerLimits.length)];

        /*Not all word entries can become spinners. If the user has chosen a set of word score colors
        * to focus on, only word entries that are of those colors can be spinners. So filter out a list
        * of indexes of word entries that have the correct colors and use these while assigning spinners */
        ArrayList<Integer> wordEntryIndexesThatCanBeSpinners = new ArrayList<>();
        if(wordEntryIdsInWordList!=null) {
            for(int i=0;i<wordEntries.size();i++) {
                if(colorString.contains(wordEntries.get(i).getColor()) && wordEntryIdsInWordList.contains(wordEntries.get(i).getId())) {
                    wordEntryIndexesThatCanBeSpinners.add(i);
                }
            }
        } else {
            for(int i=0;i<wordEntries.size();i++) {
                if(colorString.contains(wordEntries.get(i).getColor())) {
                    wordEntryIndexesThatCanBeSpinners.add(i);
                }
            }
        }


        //Only attach spinners if there are enough dummy answers to make at least one set
        if(wordEntryIndexesThatCanBeSpinners.size()>0 &&(poolOfFalseAnswers.size()>4)) {

            while(spinnerAddedIndex.size()<spinnerLimit
                    && (wordEntryIndexesThatCanBeSpinners.size()>spinnerLimit || spinnerAddedIndex.size()<wordEntryIndexesThatCanBeSpinners.size())) {
                int randomWordEntryIndex = new Random().nextInt(wordEntryIndexesThatCanBeSpinners.size());
                if(!spinnerAddedIndex.contains(wordEntryIndexesThatCanBeSpinners.get(randomWordEntryIndex))) {
                    WordEntry randomWordEntry = wordEntries.get(wordEntryIndexesThatCanBeSpinners.get(randomWordEntryIndex));
                    randomWordEntry.setSpinner(true);

                    /* Create dummy answer options from random/related kanji pools */
                    ArrayList<String> answerSet = new ArrayList<>();
                    answerSet.add(randomWordEntry.getCoreKanjiBlock());

                    while(answerSet.size()<4) {
                        int indexOfRandomFalseAnswer = new Random().nextInt(poolOfFalseAnswers.size());
                        String randomFalseAnswer = poolOfFalseAnswers.get(indexOfRandomFalseAnswer);
                        if(!answerSet.contains(randomFalseAnswer)) {
                            answerSet.add(randomFalseAnswer);
                        }
                    }
                    Collections.shuffle(answerSet);
                    randomWordEntry.getFillinSentencesSpinner().setOptions(answerSet);
                    spinnerAddedIndex.add(wordEntryIndexesThatCanBeSpinners.get(randomWordEntryIndex));
                }
            }
        }

    }


    /**
     * Adds the score for a word to the scoreboard table
     * @param wordId id of word in question
     * @param total updated total questions involving that word
     * @param correct update total correct answers for the word
     */
    public void addWordScoreToScoreBoard(int wordId, int total, int correct) {

        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(InternalDB.Columns.COL_ID, wordId);
            values.put(InternalDB.Columns.TSCOREBOARD_COL0, total);
            values.put(InternalDB.Columns.TSCOREBOARD_COL1, correct);
            sqlOpener.getReadableDatabase().insertWithOnConflict(InternalDB.Tables.TABLE_SCOREBOARD, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLiteException e) {
            Log.e(TAG, "addWordScoreToScoreBoard sqlite exception: " + e);
        } catch (NullPointerException e) {
            Log.e(TAG, "addWordScoreToScoreBoard something was null: " + e);
        }
    }

}