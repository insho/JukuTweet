package com.jukuproject.jukutweet.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
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
     * Decides which WordEntries within a Tweet will be designated as "spinners", meaning that they will be
     * pickable dropdowns in the FillintheBlanks quiz. More than one possible "spinner" word may appear in a tweet, so the number
     * of spinners is assigned via a weighted average type deal, with a max of 3 spinners per question. The spinner is designated
     * as such by turning the "isSpinner" boolean in the WordEntry object for the word to "true"
     * @param db Sqlite db connection
     * @param myListType tag designating the type of List, either Word or Tweet (to be passed on to getDummySpinnerOptions method)
     * @param myListEntry List entry object (to be passed on to getDummySpinnerOptions method)
     * @param tweet Tweet containg the words (to be passed on to getDummySpinnerOptions method)
     * @param wordListEdictIds a concatenated string of edict ids for the words contained in the Tweet. Used to randomly assign the spinner
     */
    public int setSpinnersForTweetWithMyListWords(SQLiteDatabase db
            , String myListType
            , MyListEntry myListEntry
            , Tweet tweet
            , ArrayList<Integer> wordListEdictIds) {

        ArrayList<WordEntry> wordEntries = tweet.getWordEntries();
        int spinnerAddedCount = 0;

        /* Determine maxiumum number of spinners that can be added to the tweet, from 1 - 3,
        with a 50% chance of 1, 40% chance of 2 and a 10 % chance of 3*/
        int[] possibleSpinnerLimits = new int[]{1,1,1,1,1,2,2,2,2,3};
        int spinnerLimit = possibleSpinnerLimits[(new Random()).nextInt(possibleSpinnerLimits.length)];

        /* Pick random kanji from the wordEntries list to be spinners (with maximum of the spinner limit)*/
        ArrayList<WordEntry> shuffledEntries =  new ArrayList<>(wordEntries);
        Collections.shuffle(shuffledEntries);
        if(BuildConfig.DEBUG){Log.d(TAG,"spinnerlimit: " + spinnerLimit);}
        for(int i=0;i<shuffledEntries.size() && spinnerAddedCount<spinnerLimit;i++) {
            if(BuildConfig.DEBUG){Log.d(TAG,"adding word: " + wordEntries.get(i).getKanji());};
            if(wordListEdictIds.contains(shuffledEntries.get(i).getId())) {
                wordEntries.get(wordEntries.indexOf(shuffledEntries.get(i))).setSpinner(true);
                if(BuildConfig.DEBUG){Log.d(TAG,"setting word spinner TRUE " + wordEntries.get(wordEntries.indexOf(shuffledEntries.get(i))).getKanji());}
                ArrayList<String> arrayOptions = getDummySpinnerOptions(db
                        ,myListEntry
                        ,wordEntries.get(wordEntries.indexOf(shuffledEntries.get(i)))
                        ,myListType);
                wordEntries.get(wordEntries.indexOf(shuffledEntries.get(i))).getFillinSentencesSpinner().setOptions(arrayOptions);
                spinnerAddedCount += 1;
            }
        }

        return spinnerAddedCount;
    }

    /**
     * Pulls false options for a spinner dropdown object (in FillintheBlanks quiz)
     * @param db sqlite database connection
     * @param myListEntry MyList object
     * @param wordEntry WordEntry object for "spinner" word
     * @param mylistType tag designating the type of List, either Word or Tweet
     * @return An array of dummy options for the spinner words in the FillintheBlanks Quiz
     *
     * @see com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment
     */
    public ArrayList<String> getDummySpinnerOptions(SQLiteDatabase db
            , MyListEntry myListEntry
            , WordEntry wordEntry
            , String mylistType) {
        ArrayList<String> dummyOptions = new ArrayList<>();

        try {
            Cursor c;
            if(mylistType.equals("Tweet")) {

                c = db.rawQuery("SELECT [Kanji] " +
                        "FROM " +
                        "(" +
                        "SELECT [Kanji] " +
                        "FROM " +
                        "(" +
                        "Select DISTINCT [Kanji] " +
                        "FROM [Edict] where [_id] in (" +
                            "SELECT DISTINCT Edict_id as [_id] " +
                            "FROM "+
                                "( " +
                                    "SELECT DISTINCT _id as Tweet_id " +
                                    "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                                    " WHERE [Name] = ? and [Sys] = ? " +
                                ") as x " +
                            "LEFT JOIN "  +
                                "( " +
                                "SELECT DISTINCT Tweet_id,Edict_id " +
                                "FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI + " " +
                                "WHERE [_id] <> ? " +
                                ")  as y " +
                            " ON x.Tweet_id = y.Tweet_id " +
                        " ORDER BY RANDOM()  LIMIT 8 " +
                        ") OR [_id] in (" +
                            "SELECT DISTINCT [_id] " +
                            "FROM [XRef] " +
                            "WHERE [_id] <> ? " +
                            "ORDER BY RANDOM()  LIMIT 4" +
                        ") " +
                        "ORDER BY RANDOM() LIMIT 3)  " +
                        "UNION " +
                        "SELECT '" + wordEntry.getCoreKanjiBlock() + "' as [Kanji]) " +
                        "ORDER BY RANDOM() ",new String[]{myListEntry.getListName()
                        ,String.valueOf(myListEntry.getListsSys())
                        ,String.valueOf(wordEntry.getId())
                        ,String.valueOf(wordEntry.getId())});
            } else {

                c = db.rawQuery("SELECT [Kanji] " +
                        "FROM " +
                        "(" +
                        "SELECT [Kanji] " +
                        "FROM " +
                        "(" +
                        "Select DISTINCT [Kanji] " +
                        "FROM [Edict] where [_id] in (" +
                        "SELECT DISTINCT [_id] " +
                        "FROM " +  InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES + " " +
                        " WHERE [Name] = ? and [Sys] = ? and [_id] <> ? " +
                        "ORDER BY RANDOM()  LIMIT 8 " +
                        ") OR [_id] in (" +
                        "SELECT DISTINCT [_id] " +
                        "FROM [XRef] " +
                        "WHERE [_id] <> ? " +
                        "ORDER BY RANDOM()  LIMIT 4" +
                        ") ORDER BY RANDOM() LIMIT 3)  " +
                        "UNION " +
                        "SELECT '" + wordEntry.getCoreKanjiBlock() + "' as [Kanji]) " +
                        "ORDER BY RANDOM() ",new String[]{myListEntry.getListName()
                        ,String.valueOf(myListEntry.getListsSys())
                        ,String.valueOf(wordEntry.getId())
                        ,String.valueOf(wordEntry.getId())});
            }
            Log.i(TAG,mylistType +" CCOUNT RANDOM: " + c.getCount() + ", for word: " + wordEntry.getKanji());
            if(c.getCount()>0) {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    dummyOptions.add(c.getString(0));
                    c.moveToNext();
                }
                c.close();
            }
        } catch (SQLiteException e){
            Log.e(TAG,"getDummySpinnerOptions Sqlite exception: " + e);
        }

        return dummyOptions;

    }

    /**
     * Pulls false options for a spinner dropdown object (in FillintheBlanks quiz), for Single User's saved tweets.
     * The words being pulled are based off of the user's UserInfo object, instead of a Tweet or Word list.
     * @param db sqlite database connection
     * @param userInfo UserInfo object for user whose saved tweets are being quizzed on
     * @param wordEntry WordEntry object for "spinner" word
     * @return An array of dummy options for the spinner words in the FillintheBlanks Quiz
     *
     * @see com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment
     */
    public ArrayList<String> getDummySpinnerOptions(SQLiteDatabase db
            , UserInfo userInfo
            , WordEntry wordEntry) {
        ArrayList<String> dummyOptions = new ArrayList<>();

        try {
            Cursor c = db.rawQuery("SELECT [Kanji] " +
                        "FROM " +
                        "(" +
                        "SELECT [Kanji] " +
                        "FROM " +
                        "(" +
                        "Select DISTINCT [Kanji] " +
                        "FROM [Edict] where [_id] in (" +
                            "SELECT DISTINCT Edict_id as _id" +
                            " FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI + " " +
                            "WHERE [Edict_id] <> ? and Tweet_id in (" +
                                    "SELECT  DISTINCT _id as [Tweet_id]" +
                                    "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                                    "WHERE [UserId] = ? " +
                                ")" +
                        " ORDER BY RANDOM()  LIMIT 8 " +
                        ") OR [_id] in (" +
                        "SELECT DISTINCT [_id] " +
                        "FROM [XRef] " +
                        "WHERE [_id] <> ? " +
                        "ORDER BY RANDOM()  LIMIT 4" +
                        ") ORDER BY RANDOM() LIMIT 3)  " +
                        "UNION " +
                        "SELECT '" + wordEntry.getCoreKanjiBlock() + "' as [Kanji]) " +
                        "ORDER BY RANDOM() ",new String[]{userInfo.getUserId()
                        ,String.valueOf(wordEntry.getId())
                    ,String.valueOf(wordEntry.getId())});


            Log.i(TAG,"USER INFO CCOUNT RANDOM: " + c.getCount() + ", for word: " + wordEntry.getKanji());
            if(c.getCount()>0) {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    dummyOptions.add(c.getString(0));
                    c.moveToNext();
                }
                c.close();
            }
        } catch (SQLiteException e){
            Log.e(TAG,"getDummySpinnerOptions (singleuser) Sqlite exception: " + e);
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

            String limit = "LIMIT " + String.valueOf(resultLimit);

        ArrayList<Tweet> savedTweets = new ArrayList<>();
        SQLiteDatabase db = sqlOpener.getReadableDatabase();
        try {
            Cursor c = db.rawQuery("SELECT DISTINCT TweetIds.[Tweet_id]" +
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
//                                ",c.Furigana as [Furigana]" +
//                                ",c.Definition as [Definition] " +
////                                ",c.Kanji as [Kanji] " +
//                                ",b.[Total] as [Total]" +
//                                ",b.[Correct] as [Correct] " +
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
                                " WHERE [Name] = ? and  [Sys] = ?   " +
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
                                            " WHERE [Name] = ? and  [Sys] = ?   " +
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

                            "Order by date(MetaData.Date) Desc,TweetIds.[Tweet_id] asc,TweetKanji.StartIndex asc"
                    ,  new String[]{myListEntry.getListName()
                            ,String.valueOf(myListEntry.getListsSys())
//                            ,myListEntry.getListName()
//                            ,String.valueOf(myListEntry.getListsSys())
                            ,myListEntry.getListName()
                            ,String.valueOf(myListEntry.getListsSys())});


            /* The query pulls a list of tweetdata paired with each parsed-kanji in the tweet, resulting in
            * multiple duplicate lines of tweetdata. So the cursor ony adds the tweet "metadata" once when a new tweetid is found.
            * Meanwhile the kanji data for each row is added to a TweetKanjiColor object, which is then added to the kanji and the kanji
            * added to the final "savedTweets" list when a new tweetid appears (or the cursor finishes) */
            if(c.getCount()>0) {
                c.moveToFirst();
                String currentTweetId = c.getString(0);
                Tweet tweet = new Tweet();
                while (!c.isAfterLast())
                {

                    Log.d(TAG,"adding setIdString: " + c.getString(0));
                    Log.d(TAG,"adding tweet text: " + c.getString(4));
                    Log.d(TAG,"adding setName: " + c.getString(2));
                    Log.d(TAG,"adding setScreen_name: " + c.getString(1));
                    Log.d(TAG,"adding WORDID: " + c.getString(6));
                    Log.d(TAG,"adding KANJI: " + c.getString(7));


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
                        if(tweet.getWordEntries()!=null  && tweet.getWordEntries().size()>0 && (setRandomSpinnersForTweet(tweetToAdd,db,myListEntry,"Tweet")>0)) {
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
                        Log.d(TAG,"adding word entry: " + c.getString(6) + " - " + c.getString(7));
                        tweet.addWordEntry(wordEntry);
                    }


                    if(c.isLast()) {
                        Tweet lastTweet = new Tweet(tweet);
//                        setSpinnersForTweetWithMyListWords(db,"Word",myListEntry,lastTweet,possibleSpinners);

                        if(tweet.getWordEntries()!=null  && tweet.getWordEntries().size()>0 && (setRandomSpinnersForTweet(lastTweet,db,myListEntry,"Tweet")>0)) {
                            savedTweets.add(lastTweet);
                        }
                    }
                    c.moveToNext();
                }


            } else {if(BuildConfig.DEBUG) {Log.d(TAG,"c.getcount was 0!!");}}
            c.close();

            /* If there are not enough unique results to fill out the quiz size, start doubling the
            * savedTweets until there are enough entries, and then shuffle them. */
            if(savedTweets.size()>0) {
                ArrayList<Tweet> tmpSavedTweets = new ArrayList<>(savedTweets);
                while (savedTweets.size()<resultLimit) {
                    Collections.shuffle(tmpSavedTweets);
                    setRandomSpinnersForTweet(tmpSavedTweets.get(0),db,myListEntry,"Tweet");
                    savedTweets.add(tmpSavedTweets.get(0));
                }
            }

        } catch (SQLiteException e){
            Log.e(TAG,"getFillintheBlanksTweetsForATweetList Sqlite exception: " + e);
        } finally {
            db.close();
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

        String limit;
//        if(resultLimit == null) {
//            limit = "";
//        } else {
            limit = "LIMIT " + String.valueOf(resultLimit);
//        }

        ArrayList<Tweet> savedTweets = new ArrayList<>();
        SQLiteDatabase db = sqlOpener.getReadableDatabase();
        try {
            Cursor c = db.rawQuery("SELECT DISTINCT TweetIds.[Tweet_id]" +
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
//                                ",c.Furigana as [Furigana]" +
//                                ",c.Definition as [Definition] " +
//                                ",c.Kanji as [Kanji] " +
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
                                    " FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                                    " WHERE [UserId] = ? " +

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
                                    "WHERE [UserId] = ? " +
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
            * multiple duplicate lines of tweetdata. So the cursor ony adds tweet data once, when a new tweetid is found. Meanwhile
            * the kanji data for each row is added to a TweetKanjiColor object, which is then added to the kanji and the kanji
            * added to the final "savedTweets" list when a new tweetid appears (or the cursor finishes) */

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
                        if(tweet.getWordEntries()!=null  && tweet.getWordEntries().size()>0 && (setRandomSpinnersForTweet(tweetToAdd,db,userInfo)>0)) {
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
                        if(tweet.getWordEntries()!=null  && tweet.getWordEntries().size()>0 && (setRandomSpinnersForTweet(lastTweet,db,userInfo)>0)) {
                            savedTweets.add(lastTweet);
                        }
                    }
                    c.moveToNext();
                }

            /* If there are not enough unique results to fill out the quiz size, start doubling the
            * savedTweets until there are enough entries, and then shuffle them. */
                if(savedTweets.size()>0) {
                    ArrayList<Tweet> tmpSavedTweets = new ArrayList<>(savedTweets);
                    while (savedTweets.size()<resultLimit) {

                        Collections.shuffle(tmpSavedTweets);
                        setRandomSpinnersForTweet(tmpSavedTweets.get(0),db,userInfo);
                        savedTweets.add(tmpSavedTweets.get(0));
                    }
                }
            } else {
                if(BuildConfig.DEBUG) {Log.e(TAG,"getFillintheBlanksTweetsForASingleUserSavedTweetList c.getcount was 0!!");}
            }
            c.close();

        } catch (SQLiteException e){
            Log.e(TAG,"getFillintheBlanksTweetsForASingleUserSavedTweetList Sqlite exception: " + e);
        } finally {
            db.close();
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

        supertest(colorThresholds,myListEntry);
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

        SQLiteDatabase db = sqlOpener.getReadableDatabase();
        try {


            Cursor c = db.rawQuery("SELECT TweetIds.[Tweet_id]" +
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
//                                                "SELECT DISTINCT [_id] " +
//                                                " FROM [Edict] " +
//                                                " WHERE [_id] IN (" +
                                                " SELECT DISTINCT [_id] " +
                                                " FROM " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES +  "  " +
                                                " WHERE [Name] = ?  and [Sys] = " + myListEntry.getListsSys() +" " +
//                                                ") " +
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
                                        " WHERE [Name] = ? and  [Sys] = "+ myListEntry.getListsSys() +"  " +
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
//                            ") as [ListandTweets]  " +
//                            ") as [Lists] " +
//                            "WHERE [Category] in (" + colorString + ") " +
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
                            " WHERE [Name] = ? and  [Sys] = " + myListEntry.getListsSys() +"   " +
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
            * multiple duplicate lines of tweetdata. So the cursor ony adds tweet data once, when a new tweetid is found. Meanwhile
            * the kanji data for each row is added to a TweetKanjiColor object, which is then added to the kanji and the kanji
            * added to the final "savedTweets" list when a new tweetid appears (or the cursor finishes) */


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


                            if(tweet.getWordEntries()!=null  && tweet.getWordEntries().size()>0 && setSpinnersForTweetWithMyListWords(db,"Word",myListEntry,tweet,possibleSpinners)>0) {
                                Log.d(TAG,"Flushing tweet: " + tweet.getText());
                                Log.d(TAG,"number of kanji in tweet: " + tweet.getWordEntries().size());
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

                    Log.d(TAG,"adding word entry: " + c.getString(6) + " - " + c.getString(7));

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
                        //Designate spinner kanji if they exist
                        tweet.addWordEntry(wordEntry);
                    }

                    if(c.isLast()) {
                        Log.d(TAG,"Flushing last tweet: " + tweet.getText());
                        Log.d(TAG,"last  number of kanji in tweet: " + tweet.getWordEntries().size());
                        Tweet lastTweet = new Tweet(tweet);
                        if(tweet.getWordEntries()!=null  && tweet.getWordEntries().size()>0 && setSpinnersForTweetWithMyListWords(db,"Word",myListEntry,lastTweet,possibleSpinners)>0) {
                            savedTweets.add(new Tweet(tweet));
                        }
//                        setSpinnersForTweetWithMyListWords(db,"Word",myListEntry,lastTweet,possibleSpinners);
//                        savedTweets.add(lastTweet);
                    }
                    c.moveToNext();
                }


            /* If there are not enough unique results to fill out the quiz size, start doubling the
            * savedTweets until there are enough entries, and then shuffle them. */
                if(savedTweets.size()>0) {
                    ArrayList<Tweet> tmpSavedTweets = new ArrayList<>(savedTweets);
                    while (savedTweets.size()<resultLimit) {

                        Collections.shuffle(tmpSavedTweets);
                        setSpinnersForTweetWithMyListWords(db,"Word",myListEntry,tmpSavedTweets.get(0),possibleSpinners);
                        savedTweets.add(tmpSavedTweets.get(0));
                    }
                }
            } else {if(BuildConfig.DEBUG) {Log.d(TAG,"c.getcount was 0!!");}}
            c.close();

        } catch (SQLiteException e){
            Log.e(TAG,"getFillintheBlanksTweetsForWordList Sqlite exception: " + e);
        }  finally {
            db.close();
        }
        return savedTweets;
    }


        public void supertest(ColorThresholds colorThresholds,MyListEntry myListEntry) {
        SQLiteDatabase db = sqlOpener.getReadableDatabase();

            Cursor c = db.rawQuery(
                    "SELECT  x.[_id]" +
                            " ,(CASE WHEN [Total] is NULL THEN 'Grey' " +
                            "WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 'Grey' " +
                            "WHEN CAST(ifnull([Correct],0)  as float)/[Total] < " + colorThresholds.getRedThreshold() + "  THEN 'Red' " +
                            "WHEN CAST(ifnull([Correct],0)  as float)/[Total] <  " + colorThresholds.getYellowThreshold() + " THEN 'Yellow' " +
                            "ELSE 'Green' END) as [Color] " +
                            ",[Total] " +
                            ",[Correct] " +
                    "FROM " +
                            "(" +
//                                                "SELECT DISTINCT [_id] " +
//                                                " FROM [Edict] " +
//                                                " WHERE [_id] IN (" +
                            " SELECT DISTINCT [_id] " +
                            " FROM " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES +  "  " +
                            " WHERE [Name] = ?  and [Sys] = " + myListEntry.getListsSys() +" " +
//                                                ") " +
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
                            "ON x.[_id] = y.[_id] "

//                            ") as [IdsForWordsWithColorString] "
                ,new String[]{myListEntry.getListName()
                            ,myListEntry.getListName()});

        if(c.getCount()>0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                    Log.d(TAG,"SuperTESt id:  "  + c.getString(0) + ", color: " + c.getString(1)
//                            + ", total: " + c.getString(2)
                            + ", correct/total: " + c.getString(3) + "/" + c.getString(2) );

                    c.moveToNext();

                }
            }
            c.close();

    };

    /**
     * Pulls edict ids of all saved kanji in a WordList. These will be used as "possible spinners" in the getFillintheBlanksTweetsforAWordList method.
     *
     * @param myListEntry WordList whose saved kanji's ids will be pulled
     * @return array of edict ids of all saved kanji in a WordList
     */
    public ArrayList<Integer> getIdsForWordList(MyListEntry myListEntry) {
        ArrayList<Integer> ids = new ArrayList<>();
        SQLiteDatabase db = sqlOpener.getReadableDatabase();
        try {
            Cursor c = db.rawQuery(
                    "SELECT DISTINCT [_id]" +
                            " FROM " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES + " " +
                            " WHERE [Name] = ? and [Sys] = ? ",new String[]{myListEntry.getListName()
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
        } finally {
            db.close();
        }

        return ids;
    }


    /**
     * Chooses which WordEntries within a Tweet will be the designated "spinner" questions.
     * @param tweet The tweet in question
     */
    public int setRandomSpinnersForTweet(Tweet tweet, SQLiteDatabase db,UserInfo userInfo) {
        ArrayList<WordEntry> wordEntries = tweet.getWordEntries();
        int spinnerAddedCount = 0;



        /* Determine maxiumum number of spinners that can be added to the tweet, from 1 - 3,
        with a 50% chance of 1, 40% chance of 2 and a 10 % chance of 3*/
        int[] possibleSpinnerLimits = new int[]{1,1,1,1,1,2,2,2,2,3};
        int spinnerLimit = possibleSpinnerLimits[(new Random()).nextInt(possibleSpinnerLimits.length)];

        /* Pick random kanji from the wordEntries list to be spinners (with maximum of the spinner limit)*/
        ArrayList<WordEntry> shuffledEntries =  new ArrayList<>(wordEntries);
        Collections.shuffle(shuffledEntries);

        for(int i=0;i<shuffledEntries.size() && spinnerAddedCount<spinnerLimit;i++) {

                if(BuildConfig.DEBUG){Log.d(TAG,"setting word spinner TRUE " + wordEntries.get(wordEntries.indexOf(shuffledEntries.get(i))).getKanji());}

            ArrayList<String> arrayOptions = getDummySpinnerOptions(db
                    ,userInfo
                    ,wordEntries.get(wordEntries.indexOf(shuffledEntries.get(i))));
            if(BuildConfig.DEBUG){Log.d(TAG,"getDummySpinnerOptions COMPELETE... - " + arrayOptions.size());};

            if(arrayOptions.size()>0) {
                wordEntries.get(wordEntries.indexOf(shuffledEntries.get(i))).setSpinner(true);
                wordEntries.get(wordEntries.indexOf(shuffledEntries.get(i))).getFillinSentencesSpinner().setOptions(arrayOptions);
                spinnerAddedCount += 1;
            }
        }

//        for(int i=0;i<wordEntries.size() && spinnerAddedCount<spinnerLimit;i++) {
//
//
//            ArrayList<String> arrayOptions = getDummySpinnerOptions(db
//                    ,userInfo
//                    ,wordEntries.get(i));
//            if(arrayOptions.size()>0) {
//                wordEntries.get(i).setSpinner(true);
//                wordEntries.get(i).getFillinSentencesSpinner().setOptions(arrayOptions);
//                spinnerAddedCount += 1;
//            }
//        }

        if(BuildConfig.DEBUG) {
            for(WordEntry wordEntry :wordEntries) {
                Log.d(TAG,"randomspinner wordentries: " + wordEntry.getKanji());
            }
        }
        Log.i(TAG,"spinner limit: " + spinnerLimit + ", Spinner count! - " + spinnerAddedCount);
        return spinnerAddedCount;
    }


    /**
     * Chooses which WordEntries within a Tweet will be the designated "spinner" questions.
     * @param tweet The tweet in question
     */
    public int setRandomSpinnersForTweet(Tweet tweet, SQLiteDatabase db,MyListEntry myListEntry, String myListType) {
        ArrayList<WordEntry> wordEntries = tweet.getWordEntries();
        int spinnerAddedCount = 0;



        /* Determine maxiumum number of spinners that can be added to the tweet, from 1 - 3,
        with a 50% chance of 1, 40% chance of 2 and a 10 % chance of 3*/
        int[] possibleSpinnerLimits = new int[]{1,1,1,1,1,2,2,2,2,3};
        int spinnerLimit = possibleSpinnerLimits[(new Random()).nextInt(possibleSpinnerLimits.length)];

        /* Pick random kanji from the wordEntries list to be spinners (with maximum of the spinner limit)*/
//        Collections.shuffle(wordEntries);\

                /* Pick random kanji from the wordEntries list to be spinners (with maximum of the spinner limit)*/
        ArrayList<WordEntry> shuffledEntries =  new ArrayList<>(wordEntries);
        Collections.shuffle(shuffledEntries);
        for(int i=0;i<wordEntries.size() && spinnerAddedCount<spinnerLimit;i++) {


            ArrayList<String> arrayOptions = getDummySpinnerOptions(db
                    ,myListEntry
                    ,wordEntries.get(i)
                    ,myListType);
            if(arrayOptions.size()>0) {
                wordEntries.get(i).setSpinner(true);
                wordEntries.get(i).getFillinSentencesSpinner().setOptions(arrayOptions);
                spinnerAddedCount += 1;
            }
        }


        for(int i=0;i<shuffledEntries.size() && spinnerAddedCount<spinnerLimit;i++) {

            if(BuildConfig.DEBUG){Log.d(TAG,"setting word spinner TRUE " + wordEntries.get(wordEntries.indexOf(shuffledEntries.get(i))).getKanji());}

            ArrayList<String> arrayOptions = getDummySpinnerOptions(db
                    ,myListEntry
                    ,wordEntries.get(wordEntries.indexOf(shuffledEntries.get(i)))
                    ,myListType);

            if(arrayOptions.size()>0) {
                wordEntries.get(wordEntries.indexOf(shuffledEntries.get(i))).setSpinner(true);
                wordEntries.get(wordEntries.indexOf(shuffledEntries.get(i))).getFillinSentencesSpinner().setOptions(arrayOptions);
                spinnerAddedCount += 1;
            }
        }


        if(BuildConfig.DEBUG) {
            for(WordEntry wordEntry :wordEntries) {
                Log.d(TAG,"randomspinner wordentries: " + wordEntry.getKanji());
            }
        }
        Log.i(TAG,"spinner limit: " + spinnerLimit + ", Spinner count! - " + spinnerAddedCount);
        return spinnerAddedCount;
    }

    /**
     * Adds the score for a word to the scoreboard table
     * @param wordId id of word in question
     * @param total updated total questions involving that word
     * @param correct update total correct answers for the word
     * @return bool true if insert was succesful, false if not
     */
    public boolean addWordScoreToScoreBoard(int wordId, int total, int correct) {
        SQLiteDatabase db = sqlOpener.getReadableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(InternalDB.Columns.COL_ID, wordId);
            values.put(InternalDB.Columns.TSCOREBOARD_COL0, total);
            values.put(InternalDB.Columns.TSCOREBOARD_COL1, correct);
            db.insertWithOnConflict(InternalDB.Tables.TABLE_SCOREBOARD, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
            return true;

        } catch (SQLiteException e) {
            Log.e(TAG, "addWordScoreToScoreBoard sqlite exception: " + e);
            return false;

        } catch (NullPointerException e) {
            Log.e(TAG, "addWordScoreToScoreBoard something was null: " + e);
            return false;
        } finally {
            db.close();
        }
    }

}