package com.jukuproject.jukutweet.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jukuproject.jukutweet.Interfaces.QuizOperationsInterface;
import com.jukuproject.jukutweet.Interfaces.TweetListOperationsInterface;
import com.jukuproject.jukutweet.Interfaces.UserOperationsInterface;
import com.jukuproject.jukutweet.Interfaces.WordListOperationsInterface;
import com.jukuproject.jukutweet.Models.WordLoader;
import com.jukuproject.jukutweet.TweetParser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Database helper for creation of internal tables, as well as traffic controller for
 * the other "OpsHelper" classes of sqlite database helpers.
 */
public class InternalDB extends SQLiteOpenHelper

{

    public static TweetListOperationsInterface tweetOpsHelper;
    public static UserOperationsInterface userOpsHelper;
    public static QuizOperationsInterface quizOpsHelper;
    public static WordListOperationsInterface wordOpsHelper;

    private static InternalDB sInstance;
    public static  String DB_NAME =  "JQuiz";
    public static String DATABASE_NAME = DB_NAME + ".db";
    public static final int DB_VERSION = 1;

    public class Tables {
        public static final String TABLE_USERS = "Users";
        public static final String TABLE_SCOREBOARD = "JScoreboard";
        public static final String TABLE_FAVORITES_LISTS = "JFavoritesLists";
        public static final String TABLE_SAVED_TWEETS = "JSavedTweets";
        public static final String TABLE_FAVORITES_LISTS_TWEETS = "JFavoritesTweetLists";
        public static final String TABLE_FAVORITES_LISTS_TWEETS_ENTRIES = "JFavoritesTweets";
        public static final String TABLE_SAVED_TWEET_KANJI = "JSavedTweetKanji";
        public static final String TABLE_SAVED_TWEET_URLS = "JSavedTweetUrls";
        public static final String TABLE_FAVORITES_LIST_ENTRIES = "JFavorites";
    }

    public class Columns {

        public static final String COL_ID = "_id";
        public static final String TMAIN_COL0 = "ScreenName";
        public static final String TMAIN_COL1 = "UserId";
        public static final String TMAIN_COL2 = "Description";
        public static final String TMAIN_COL3 = "FollowerCount";
        public static final String TMAIN_COL4 = "FriendCount";
        public static final String TMAIN_COL5 = "ProfileImgUrl";
        public static final String TMAIN_COL6 = "ProfileImgFilePath";
        public static final String TMAIN_COL7 = "UserName";

        public static final String TSCOREBOARD_COL0 = "Total";
        public static final String TSCOREBOARD_COL1 = "Correct";

        public static final String TFAVORITES_COL0 = "Name";
        public static final String TFAVORITES_COL1 = "Sys";

        public static final String TSAVEDTWEET_COL0 = "UserId";
        public static final String TSAVEDTWEET_COL1 = "UserScreenName";
        public static final String TSAVEDTWEET_COL2 = "Tweet_id";
        public static final String TSAVEDTWEET_COL3 = "CreatedAt";
        public static final String TSAVEDTWEET_COL4 = "Text";

        public static final String TSAVEDTWEETITEMS_COL2 = "Edict_id"; //Edict Primary Key!
        public static final String TSAVEDTWEETITEMS_COL3 = "StartIndex";
        public static final String TSAVEDTWEETITEMS_COL4 = "EndIndex";
        public static final String TSAVEDTWEETITEMS_COL5 = "CoreKanjiBlock";

        public static final String TSAVEDTWEETURLS_COL1 = "Url";
        public static final String TSAVEDTWEETURLS_COL2 = "StartIndex";
        public static final String TSAVEDTWEETURLS_COL3 = "EndIndex";

    }


    public static synchronized InternalDB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new InternalDB(context.getApplicationContext());
        }
        return sInstance;
    }

    public static synchronized WordListOperationsInterface getWordInterfaceInstance(Context context) {
        if (wordOpsHelper == null) {
            wordOpsHelper = new WordOpsHelper(InternalDB.getInstance(context.getApplicationContext()));
        }
        return wordOpsHelper;
    }


    public static synchronized TweetListOperationsInterface getTweetInterfaceInstance(Context context) {
        if (tweetOpsHelper == null) {
            tweetOpsHelper = new TweetOpsHelper(InternalDB.getInstance(context.getApplicationContext()));
        }
        return tweetOpsHelper;
    }

    public static synchronized UserOperationsInterface getUserInterfaceInstance(Context context) {
        if (userOpsHelper == null) {
            userOpsHelper  = new UserOpsHelper(InternalDB.getInstance(context.getApplicationContext()));
        }
        return userOpsHelper ;
    }

    public static synchronized QuizOperationsInterface getQuizInterfaceInstance(Context context) {
        if (quizOpsHelper == null) {
            quizOpsHelper  = new QuizOpsHelper(InternalDB.getInstance(context.getApplicationContext()));
        }
        return quizOpsHelper ;
    }

    public InternalDB(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
        tweetOpsHelper = new TweetOpsHelper(this);
    }



    @Override
    public void onCreate(SQLiteDatabase sqlDB) {

        /* Table of user info for saved twitter users */
        String sqlQueryUsers =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s TEXT, " +
                                "%s TEXT, " +
                                "%s TEXT, " +
                                "%s INTEGER, " +
                                "%s INTEGER, " +
                                "%s TEXT, " +
                                "%s TEXT, " +
                                "%s TEXT) ", Tables.TABLE_USERS,
                        Columns.COL_ID,
                        Columns.TMAIN_COL0,
                        Columns.TMAIN_COL1,
                        Columns.TMAIN_COL2,
                        Columns.TMAIN_COL3,
                        Columns.TMAIN_COL4,
                        Columns.TMAIN_COL5,
                        Columns.TMAIN_COL6,
                        Columns.TMAIN_COL7);

        sqlDB.execSQL(sqlQueryUsers);


        /* Tracks quiz scores for words in the edict dictionary (found in tweets)*/
        String sqlQueryJScoreBoard =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s INTEGER PRIMARY KEY, " +
                                "%s INTEGER, " +
                                "%s INTEGER)", Tables.TABLE_SCOREBOARD,
                        Columns.COL_ID, //_id
                        Columns.TSCOREBOARD_COL0, //Total
                        Columns.TSCOREBOARD_COL1); // Correct

        sqlDB.execSQL(sqlQueryJScoreBoard);


        /* Reference table of unique user-created word lists */
        String sqlQueryJFavoritesLists =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s TEXT)", Tables.TABLE_FAVORITES_LISTS,
                        Columns.TFAVORITES_COL0);

        sqlDB.execSQL(sqlQueryJFavoritesLists);

        /* Stores kanji entries with a wordlist (either system or user-created) */
        String sqlQueryJFavoritesListEntries =
                String.format("CREATE TABLE IF NOT EXISTS  %s (" +
                                "%s INTEGER, " +
                                "%s TEXT, " +
                                "%s INTEGER)", Tables.TABLE_FAVORITES_LIST_ENTRIES,
                        Columns.COL_ID,
                        Columns.TFAVORITES_COL0,
                        Columns.TFAVORITES_COL1); // if this column = 1, it is a system table (i.e. blue, red, yellow), if user-created the value is 0

        sqlDB.execSQL(sqlQueryJFavoritesListEntries);


        /* Reference table of unique user-created tweet lists */
        String sqlQueryJFavoritesListsTweets =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s TEXT)", Tables.TABLE_FAVORITES_LISTS_TWEETS,
                        Columns.TFAVORITES_COL0);

        sqlDB.execSQL(sqlQueryJFavoritesListsTweets);

         /* Pairs tweets with a mylist (either system or user-created), for "Saved tweets" related fragments */
        String sqlQueryJFavoritesListTweetsEntries =
                String.format("CREATE TABLE IF NOT EXISTS  %s (" +
                                "%s TEXT, " +
                                "%s TEXT, " +
                                "%s TEXT, " +
                                "%s INTEGER)", Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES,

                        Columns.COL_ID, //Tweet id!
                        Columns.TSAVEDTWEET_COL0, // User id
                        Columns.TFAVORITES_COL0, //name
                        Columns.TFAVORITES_COL1); // Sys

        sqlDB.execSQL(sqlQueryJFavoritesListTweetsEntries);


        /* Saved Tweets table, one tweet per row */
        String sqlQueryJSavedTweet =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s TEXT, " +
                                "%s TEXT, " +
                                "%s TEXT, " +
                                "%s TEXT, " +
                                "%s TEXT)", Tables.TABLE_SAVED_TWEETS,
                        Columns.COL_ID, //_id
                        Columns.TSAVEDTWEET_COL0, //UserId
                        Columns.TSAVEDTWEET_COL1, //UserScreenName
                        Columns.TSAVEDTWEET_COL2, // Tweet_id
                        Columns.TSAVEDTWEET_COL3, //CreatedAt
                        Columns.TSAVEDTWEET_COL4); // Text

        sqlDB.execSQL(sqlQueryJSavedTweet);

        /* Stores parsed kanji ids (edict_id) for a given tweet in TABLE_SAVED_TWEETS. Added asynchronously when
        * a tweet is saved to a list */
        String sqlQueryJSavedTweetEntries =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s TEXT, " +
                                "%s INTEGER, " +
                                "%s INTEGER, " +
                                "%s INTEGER, " +
                                "%s INTEGER)", Tables.TABLE_SAVED_TWEET_KANJI,
                        Columns.COL_ID, //_id (unique)
                        Columns.TSAVEDTWEET_COL2, //STweet_id (JSavedTweet _id)
                        Columns.TSAVEDTWEETITEMS_COL2, // Edict_id
                        Columns.TSAVEDTWEETITEMS_COL5, // CoreKanjiBlock
                        Columns.TSAVEDTWEETITEMS_COL3, //STart index
                        Columns.TSAVEDTWEETITEMS_COL4); //End INdex

        sqlDB.execSQL(sqlQueryJSavedTweetEntries);

        /* Stores URLs for a tweet in the TABLE_SAVED_TWEETS */
        String sqlQueryJSavedTweetUrls =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s INTEGER, " +
                                "%s TEXT, " +
                                "%s INTEGER, " +
                                "%s INTEGER)", Tables.TABLE_SAVED_TWEET_URLS,
                        Columns.COL_ID, //_id (unique)
                        Columns.TSAVEDTWEET_COL2, //STweet_id (JSavedTweet _id)
                        Columns.TSAVEDTWEETURLS_COL1, // Url text
                        Columns.TSAVEDTWEETURLS_COL2, // start index of url
                        Columns.TSAVEDTWEETURLS_COL3); // end index of url

        sqlDB.execSQL(sqlQueryJSavedTweetUrls);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqlDB, int i, int i2) {
        onCreate(sqlDB);
    }



    /**
     * Pulls lists of hiragana/katakana/symbols and verb endings stored in the "Characters" table
     * in the database, and populates a WordLoader object with them.
     *
     * @see TweetParser
     */
    public WordLoader getWordLists() {

        ArrayList<String> hiragana = new ArrayList<>();
        ArrayList<String> katakana = new ArrayList<>();
        ArrayList<String> symbols = new ArrayList<>();
        HashMap<String,ArrayList<String>> romajiMap = new HashMap<>();
        ArrayList<String> verbEndingsRoot = new ArrayList<>();
        ArrayList<String> verbEndingsConjugation = new ArrayList<>();
        HashMap<String,ArrayList<String>> verbEndingMap = new HashMap<>();

        try {
            ArrayList<String> romaji = new ArrayList<>();
            Cursor c = sInstance.getWritableDatabase().rawQuery("SELECT DISTINCT Type, Key, Value FROM [Characters] ORDER BY Type",null);
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    switch (c.getString(0)) {
                        case "Hiragana":
                            hiragana.add(c.getString(1));
                            break;
                        case "Katakana":
                            katakana.add(c.getString(1));
                            break;
                        case "Symbols":
                            symbols.add(c.getString(1));
                            break;
                        case "Romaji":
                            romaji.add(c.getString(1));
                            break;
                        case "VerbEndings":
                            verbEndingsRoot.add(c.getString(1));
                            verbEndingsConjugation.add(c.getString(2));

                            if(verbEndingMap.containsKey(c.getString(2))) {
                                ArrayList<String> tmp = verbEndingMap.get(c.getString(2));
                                tmp.add(c.getString(1));
                                verbEndingMap.put(c.getString(1),tmp);
                            } else {
                                ArrayList<String> tmp = new ArrayList<>();
                                tmp.add(c.getString(1));
                                verbEndingMap.put(c.getString(2),tmp);
                            }

                            break;
                    }
                    c.moveToNext();
                }
                c.close();

                //Add to Romaji HashMap
                for(int i=0;i<romaji.size();i++) {
                    ArrayList<String> tmp;
                    if(romajiMap.containsKey(romaji.get(i))){
                        tmp = romajiMap.get(romaji.get(i));
                    } else {
                        tmp = new ArrayList<>();
                    }

                    tmp.add(hiragana.get(i));
                    tmp.add(katakana.get(i));
                    romajiMap.put(romaji.get(i),tmp);
                }

                ArrayList<String> extratmp = new ArrayList<>();
                extratmp.add("ん");
                extratmp.add("ン");
                romajiMap.put("n",extratmp);

            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return new WordLoader(hiragana,katakana,symbols,romajiMap,verbEndingMap,verbEndingsRoot,verbEndingsConjugation);
    }

}

