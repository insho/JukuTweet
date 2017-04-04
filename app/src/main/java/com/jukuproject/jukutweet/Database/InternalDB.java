package com.jukuproject.jukutweet.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetUrl;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.Models.WordLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;

//import com.jukuproject.jukutweet.Models.ParseSentenceItem;
//import com.jukuproject.jukutweet.Models.TweetKanjiColor;

/**
 * Database helper
 */
public class InternalDB extends SQLiteOpenHelper {


    private final PriorityBlockingQueue queue = new PriorityBlockingQueue();

    private static boolean debug = true;
    private static String TAG = "TEST-Internal";
    private static InternalDB sInstance;

    public static  String DB_NAME =  "JQuiz";
    public static String DATABASE_NAME = DB_NAME + ".db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_USERS = "Users";
    public static final String COL_ID = "_id";
    public static final String TMAIN_COL0 = "ScreenName";
    public static final String TMAIN_COL1 = "UserId";
    public static final String TMAIN_COL2 = "Description";
    public static final String TMAIN_COL3 = "FollowerCount";
    public static final String TMAIN_COL4 = "FriendCount";
    public static final String TMAIN_COL5 = "ProfileImgUrl";
    public static final String TMAIN_COL6 = "ProfileImgFilePath";
    public static final String TMAIN_COL7 = "UserName";

    public static final String TABLE_SCOREBOARD = "JScoreboard";
    public static final String TSCOREBOARD_COL0 = "Total";
    public static final String TSCOREBOARD_COL1 = "Correct";

    public static final String TABLE_FAVORITES_LISTS = "JFavoritesLists";
    public static final String TFAVORITES_COL0 = "Name";

    public static final String TABLE_FAVORITES_LIST_ENTRIES = "JFavorites";
    public static final String TFAVORITES_COL1 = "Sys";

    /* Tables for saving Tweets (including kanji associated with tweets), adding tweets to favorite lists*/
    public static final String TABLE_FAVORITES_LISTS_TWEETS = "JFavoritesTweetLists";
    public static final String TABLE_FAVORITES_LISTS_TWEETS_ENTRIES = "JFavoritesTweets";

    public static final String TABLE_SAVED_TWEETS = "JSavedTweets";
    public static final String TSAVEDTWEET_COL0 = "UserId";
    public static final String TSAVEDTWEET_COL1 = "UserScreenName";
    public static final String TSAVEDTWEET_COL2 = "Tweet_id";
    public static final String TSAVEDTWEET_COL3 = "CreatedAt";
    public static final String TSAVEDTWEET_COL4 = "Text";

    public static final String TABLE_SAVED_TWEET_KANJI = "JSavedTweetKanji";
//    public static final String TSAVEDTWEETITEMS_COL0 = "Tweet_id"; //JSavedTweets Primary Key!
    public static final String TSAVEDTWEETITEMS_COL2 = "Edict_id"; //Edict Primary Key!
    public static final String TSAVEDTWEETITEMS_COL3 = "StartIndex"; //Edict Primary Key!
    public static final String TSAVEDTWEETITEMS_COL4 = "EndIndex"; //Edict Primary Key!

    public static final String TABLE_SAVED_TWEET_URLS = "JSavedTweetUrls";
    public static final String TSAVEDTWEETURLS_COL1 = "Url";
    public static final String TSAVEDTWEETURLS_COL2 = "StartIndex";
    public static final String TSAVEDTWEETURLS_COL3 = "EndIndex";

    public static synchronized InternalDB getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new InternalDB(context.getApplicationContext());
        }
        return sInstance;
    }


    public InternalDB(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);

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
                                "%s TEXT) ", TABLE_USERS,
                        COL_ID,
                        TMAIN_COL0,
                        TMAIN_COL1,
                        TMAIN_COL2,
                        TMAIN_COL3,
                        TMAIN_COL4,
                        TMAIN_COL5,
                        TMAIN_COL6,
                        TMAIN_COL7);

        sqlDB.execSQL(sqlQueryUsers);


        /* Tracks quiz scores for words in the edict dictionary (found in tweets)*/
        String sqlQueryJScoreBoard =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s INTEGER PRIMARY KEY, " +
                                "%s INTEGER, " +
                                "%s INTEGER)", TABLE_SCOREBOARD,
                        COL_ID, //_id
                        TSCOREBOARD_COL0, //Total
                        TSCOREBOARD_COL1); // Correct

        sqlDB.execSQL(sqlQueryJScoreBoard);


        /* Reference table of unique user-created word lists */
        String sqlQueryJFavoritesLists =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s TEXT)", TABLE_FAVORITES_LISTS,
                        TFAVORITES_COL0);

        sqlDB.execSQL(sqlQueryJFavoritesLists);

        /* Stores kanji entries with a mylist (either system or user-created) */
        String sqlQueryJFavoritesListEntries =
                String.format("CREATE TABLE IF NOT EXISTS  %s (" +
                                "%s INTEGER, " +
                                "%s TEXT, " +
                                "%s INTEGER)", TABLE_FAVORITES_LIST_ENTRIES,
                        COL_ID,
                        TFAVORITES_COL0,
                        TFAVORITES_COL1); // if this column = 1, it is a system table (i.e. blue, red, yellow), if user-created the value is 0

        sqlDB.execSQL(sqlQueryJFavoritesListEntries);


        /* Reference table of unique user-created tweet lists */
        String sqlQueryJFavoritesListsTweets =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s TEXT)", TABLE_FAVORITES_LISTS_TWEETS,
                        TFAVORITES_COL0);

        sqlDB.execSQL(sqlQueryJFavoritesListsTweets);

         /* Pairs tweets with a mylist (either system or user-created), for "Saved tweets" related fragments */
        String sqlQueryJFavoritesListTweetsEntries =
                String.format("CREATE TABLE IF NOT EXISTS  %s (" +
                                "%s TEXT, " +
                                "%s TEXT, " +
                                "%s TEXT, " +
                                "%s TEXT)", TABLE_FAVORITES_LISTS_TWEETS_ENTRIES,

                        COL_ID, //Tweet id!
                        TSAVEDTWEET_COL0, // User id
                        TFAVORITES_COL0, //name
                        TFAVORITES_COL1); // Sys

        sqlDB.execSQL(sqlQueryJFavoritesListTweetsEntries);


        /* Saved Tweets table, one tweet per row */
        String sqlQueryJSavedTweet =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s TEXT, " +
                                "%s TEXT, " +
                                "%s TEXT, " +
                                "%s TEXT, " +
                                "%s TEXT)", TABLE_SAVED_TWEETS,
                        COL_ID, //_id
                        TSAVEDTWEET_COL0, //UserId
                        TSAVEDTWEET_COL1, //UserScreenName
                        TSAVEDTWEET_COL2, // Tweet_id
                        TSAVEDTWEET_COL3, //CreatedAt
                        TSAVEDTWEET_COL4); // Text

        sqlDB.execSQL(sqlQueryJSavedTweet);

        /* Stores parsed kanji ids (edict_id) for a given tweet in TABLE_SAVED_TWEETS. Added asynchronously when
        * a tweet is saved to a list */
        String sqlQueryJSavedTweetEntries =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s TEXT, " +
                                "%s INTEGER, " +
                                "%s INTEGER, " +
                                "%s INTEGER)", TABLE_SAVED_TWEET_KANJI,
                        COL_ID, //_id (unique)
                        TSAVEDTWEET_COL2, //STweet_id (JSavedTweet _id)
                        TSAVEDTWEETITEMS_COL2, // Edict_id
                        TSAVEDTWEETITEMS_COL3, //STart index
                        TSAVEDTWEETITEMS_COL4); //End INdex

        sqlDB.execSQL(sqlQueryJSavedTweetEntries);

        /* Stores URLs for a tweet in the TABLE_SAVED_TWEETS */
        String sqlQueryJSavedTweetUrls =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s INTEGER, " +
                                "%s TEXT, " +
                                "%s INTEGER, " +
                                "%s INTEGER)", TABLE_SAVED_TWEET_URLS,
                        COL_ID, //_id (unique)
                        TSAVEDTWEET_COL2, //STweet_id (JSavedTweet _id)
                        TSAVEDTWEETURLS_COL1, // Url text
                        TSAVEDTWEETURLS_COL2, // start index of url
                        TSAVEDTWEETURLS_COL3); // end index of url

        sqlDB.execSQL(sqlQueryJSavedTweetUrls);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqlDB, int i, int i2) {
        onCreate(sqlDB);
    }


    /**
     * Checks for duplicate entries for a user-created list in the JFavoritesLists table
     * @param mylist prospective new user-created mylist
     * @return true if that list name already exists, false if not
     */
    public boolean duplicateMyList(String mylist) {

        /** Before inserting record, check to see if feed already exists */
        SQLiteDatabase db = sInstance.getWritableDatabase();
        String queryRecordExists = "Select Name From " + TABLE_FAVORITES_LISTS + " where " + TFAVORITES_COL0 + " = ?" ;
        Cursor c = db.rawQuery(queryRecordExists, new String[]{mylist});
        try {
            if (c.moveToFirst()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLiteException e) {
            Log.e(TAG,"Sqlite exception: " + e);
        } finally {
            c.close();
            db.close();
        }
        return false;
    }

    /**
     * Checks to see if user is already saved in table
     * @param user user's twitter handle
     * @return boolean True if user already exists, false if user does not exist
     */
    public boolean duplicateUser(String user) {

        /** Before inserting record, check to see if feed already exists */
        SQLiteDatabase db = sInstance.getWritableDatabase();
        String queryRecordExists = "Select _id From " + TABLE_USERS + " where " + TMAIN_COL0 + " = ?" ;
        Cursor c = db.rawQuery(queryRecordExists, new String[]{user});
        try {
            if (c.moveToFirst()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLiteException e) {
            Log.e(TAG,"Sqlite exception: " + e);
        } finally {
            c.close();
            db.close();
        }
      return false;
    }

    /**
     * Saves new user feed to DB
     * @param userInfo UserInfo object with user data pulled from twitter api
     * @return bool True if save worked, false if it failed
     */
    public boolean saveUser(UserInfo userInfo) {
        SQLiteDatabase db = sInstance.getWritableDatabase();
        try {
            if(userInfo.getScreenName() != null) {

                ContentValues values = new ContentValues();
                if(debug) {
                    Log.d(TAG,"saving user: " + userInfo.getScreenName() );
                }
                values.put(TMAIN_COL0, userInfo.getScreenName().trim());

                if(userInfo.getDescription() != null) {
                    values.put(TMAIN_COL1, userInfo.getUserId());
                }
                if(userInfo.getDescription() != null) {
                    values.put(TMAIN_COL2, userInfo.getDescription().trim());
                }
                if(userInfo.getFollowerCount() != null ) {
                    values.put(TMAIN_COL3, userInfo.getFollowerCount());
                }
                if(userInfo.getFriendCount() != null){
                    values.put(TMAIN_COL4, userInfo.getFriendCount());
                }
                if(userInfo.getProfileImageUrl() != null) {
                    values.put(TMAIN_COL5, userInfo.getProfileImageUrl().trim());
                }

                if(userInfo.getProfileImageUrl() != null) {
                    values.put(TMAIN_COL7, userInfo.getName().trim());
                }

                db.insert(TABLE_USERS, null, values);

                return true;
            } else {
                return false;
            }
        } catch(SQLiteException exception) {
        return false;
        } finally {
            db.close();
        }

    }


    /**
     * Removes a user from the database
     * @param user user screen_name to remove
     * @return bool True if operation is succesful, false if an error occurs
     */
    public boolean deleteUser(String user) {
        try{
            SQLiteDatabase db = sInstance.getWritableDatabase();
            db.delete(TABLE_USERS, TMAIN_COL0 + "= ?", new String[]{user});
            db.close();
            return true;
        } catch(SQLiteException exception) {
            return false;
        }

    }


//TODO == hook this to user id? instead of screenname
    public void addMediaURItoDB(String URI, String screenName) {

//        Log.d(TAG,"URI VALUE: " + rowID + " - " + URI);
        SQLiteDatabase db = sInstance.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TMAIN_COL6, URI);
        db.update(TABLE_USERS, values, TMAIN_COL0 + "= ?", new String[] {screenName});
        db.close();
        Log.d(TAG,"SUCESSFUL INSERT URI for name: " + screenName);

    }

    /**
     * Pulls user information from db and fills a list of UserInfo objects, to
     * be used in the UserListFragment recycler
     * @return list of UserInfo objects, one for each followed user saved in the db
     */
    public List<UserInfo> getSavedUserInfo() {
        List<UserInfo> userInfoList = new ArrayList<UserInfo>();

        String querySelectAll = "Select distinct ScreenName,IFNULL(Description,''),FollowerCount, FriendCount, IFNULL(ProfileImgUrl,''),UserId, ProfileImgFilePath, UserName From " + TABLE_USERS;
        SQLiteDatabase db = sInstance.getReadableDatabase();
        Cursor c = db.rawQuery(querySelectAll, null);


        try {
            if (c.moveToFirst()) {
                do {
                    UserInfo userInfo = new UserInfo(c.getString(0));
                    userInfo.setDescription(c.getString(1));

                    try {
                        userInfo.setUserId(c.getString(5));
                    } catch (SQLiteException e) {
                        Log.e(TAG,"getSavedUserInfo adding user ID sqlite problem: " + e);
                    } catch (Exception e) {
                        Log.e(TAG,"getSavedUserInfo adding user ID other exception... " + e);
                    }

                    try {
                        userInfo.setFollowerCount(c.getInt(2));
                    } catch (SQLiteException e) {
                        Log.e(TAG,"getSavedUserInfo adding followers count sqlite problem: " + e);
                    } catch (Exception e) {
                        Log.e(TAG,"getSavedUserInfo adding followers count other exception... " + e);
                    }
                    try {
                        userInfo.setFriendCount(c.getInt(3));
                    }  catch (SQLiteException e) {
                        Log.e(TAG,"getSavedUserInfo adding setFriendCount sqlite problem: " + e);
                    } catch (Exception e) {
                        Log.e(TAG,"getSavedUserInfo adding setFriendCount other exception... " + e);
                    }

                    try {
                        userInfo.setProfileImageFilePath(c.getString(6));
                    }  catch (SQLiteException e) {
                        Log.e(TAG,"getSavedUserInfo adding setProfileImgFilePath sqlite problem: " + e);
                    } catch (Exception e) {
                        Log.e(TAG,"getSavedUserInfo adding setProfileImgFilePath other exception... " + e);
                    }

                    try {
                        userInfo.setName(c.getString(7));
                    }  catch (SQLiteException e) {
                        Log.e(TAG,"getSavedUserInfo adding setProfileImgFilePath sqlite problem: " + e);
                    } catch (Exception e) {
                        Log.e(TAG,"getSavedUserInfo adding setProfileImgFilePath other exception... " + e);
                    }

                    userInfo.setProfile_image_url(c.getString(4));
                    userInfoList.add(userInfo);
                } while (c.moveToNext());
            }

            c.close();
        } finally {
            db.close();
        }

        return userInfoList;
    }


    //TODO instead use some kind of custom comparator? OR no?
    /**
     * Compares two UserInfo objects, and overwrites the old object in the database if values
     * have changed. Used when user timeline info is pulled, to check the user metadata attached to the tweets
     * against the saved data in the db
     * @param oldUserInfo old UserInfo object (saved info in db)
     * @param recentUserInfo new UserInfo object (pulled from tweet metadata)
     * @return
     *
     * @see com.jukuproject.jukutweet.Fragments.UserTimeLineFragment#pullTimeLineData(UserInfo)
     */
    public boolean compareUserInfoAndUpdate(UserInfo oldUserInfo, UserInfo recentUserInfo) {

        try {
            ContentValues values = new ContentValues();

            /*The user's screenName field can change, but the user id won't. So if possible user the userId as the
              key for making updates */
            if(oldUserInfo.getUserId() != null && !oldUserInfo.getScreenName().equals(recentUserInfo.getScreenName())) {
                values.put(InternalDB.TMAIN_COL0, recentUserInfo.getScreenName().trim());
            }
            if(recentUserInfo.getDescription() != null && !oldUserInfo.getDescription().equals(recentUserInfo.getDescription())) {
                values.put(InternalDB.TMAIN_COL2, recentUserInfo.getDescription().trim());
            }
            if(recentUserInfo.getFollowerCount() != null && oldUserInfo.getFollowerCount() != recentUserInfo.getFollowerCount()) {
                values.put(InternalDB.TMAIN_COL3, recentUserInfo.getFollowerCount());
            }
            if(recentUserInfo.getFriendCount() != null && oldUserInfo.getFriendCount() != recentUserInfo.getFriendCount()) {
                values.put(InternalDB.TMAIN_COL4, recentUserInfo.getFriendCount());
            }
            if(recentUserInfo.getProfileImageUrl() != null && !oldUserInfo.getProfileImageUrl().equals(recentUserInfo.getProfileImageUrl())) {
                values.put(InternalDB.TMAIN_COL5, recentUserInfo.getProfileImageUrl().trim());
            }

            if(values.size()>0) {
                if(oldUserInfo.getUserId() != null) {
                    SQLiteDatabase db = sInstance.getReadableDatabase();
                    db.update(TABLE_USERS, values, TMAIN_COL1 + "= ?", new String[]{String.valueOf(oldUserInfo.getUserId())});
                } else {
                    SQLiteDatabase db = sInstance.getReadableDatabase();
                    db.update(TABLE_USERS, values, TMAIN_COL0 + "= ?", new String[]{oldUserInfo.getScreenName()});
                }
            }

            return true;
        } catch (SQLiteException e) {
            Log.e(TAG,"compareUserInfo prob : " + e);
        }
        return false;
    }


    /**
     * Pulls lists of hiragana/katakana/symbols and verb endings stored in the "Characters" table
     * in the database, and populates a WordLoader object with them.
     *
     * Note: the nullable input db is used for testing. In test scenario the db has to be passed into the method (?)
//     * @param inputDB Sqlite database connection
     * @return WordLoader object with array lists and maps of various japanse characters
     *
//     * @see com.jukuproject.jukutweet.SentenceParser#parseSentence(String, SQLiteDatabase, ArrayList, ArrayList, WordLoader, ColorThresholds, ArrayList)
     */
    public WordLoader getWordLists() {
//        SQLiteDatabase db;
//        if(inputDB == null) {
//            db = this.getWritableDatabase();
//        } else {
//            db = inputDB;
//        }
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


    /**
     * Adds a new mylist to the JFavoritesLists table in the db
     * @param mylist list name to add
     * @return boolean true if success, false if not
     */
    public boolean saveMyList(String mylist) {
        try {
                SQLiteDatabase db = sInstance.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(TFAVORITES_COL0, mylist.trim());
                db.insert(TABLE_FAVORITES_LISTS, null, values);
                db.close();
                return true;
        } catch(SQLiteException exception) {
            Log.e(TAG,"savemylist db insert exception: " + exception);
            return false;
        }
    }

    /**
     * Erases kanji from a mylist (deleting their associated rows from JFavorites table)
     * The list itself is maintained in the JFavoritesLists table, and is untouched
     * @param listName list to clear
     * @param isStarFavorite boolean, true if the list is a "system list", meaning it is a colored star favorites list.
     *                       System have a 1 in the "system" column of JFavorites, and this must be accounted for in the delete statement.
     * @return boolean true if successful, false if not
     *
     * @see com.jukuproject.jukutweet.Dialogs.EditMyListDialog
     * @see com.jukuproject.jukutweet.MainActivity#onEditMyListDialogPositiveClick(int, String, boolean)
     * @see com.jukuproject.jukutweet.MainActivity#deleteOrClearDialogFinal(Boolean, String, boolean)
     */
    public boolean clearMyList(String listName, boolean isStarFavorite) {
        try{
            SQLiteDatabase db = sInstance.getWritableDatabase();
            if(isStarFavorite) {
                db.delete(TABLE_FAVORITES_LIST_ENTRIES, TFAVORITES_COL0 + "= ? and " + TFAVORITES_COL1 + "= 1", new String[]{listName});
            } else {
                db.delete(TABLE_FAVORITES_LIST_ENTRIES, TFAVORITES_COL0 + "= ? and " + TFAVORITES_COL1 + "= 0", new String[]{listName});
            }
            db.close();
            return true;
        } catch(SQLiteException exception) {
            return false;
        }

    }

    /**
     * Removes a list from the JFavoritesList table and all associated rows from JFavorites Table
     *
     * @param listName name of list to remove
     * @return boolean true if successful, false if not
     *
     * @see com.jukuproject.jukutweet.Dialogs.EditMyListDialog
     * @see com.jukuproject.jukutweet.MainActivity#onEditMyListDialogPositiveClick(int, String, boolean)
     * @see com.jukuproject.jukutweet.MainActivity#deleteOrClearDialogFinal(Boolean, String, boolean)
     */
    public boolean deleteMyList(String listName) {
        try{
            SQLiteDatabase db = sInstance.getWritableDatabase();
            db.delete(TABLE_FAVORITES_LISTS, TFAVORITES_COL0 + "= ?", new String[]{listName});
                db.delete(TABLE_FAVORITES_LIST_ENTRIES, TFAVORITES_COL0 + "= ? and " + TFAVORITES_COL1 + "= 0", new String[]{listName});
            db.close();
            return true;
        } catch(SQLiteException exception) {
            return false;
        }
    }

    /**
     * Changes the name of a user-created list
     * @param oldListName current list name in JFavoritesLists and JFavorites tables
     * @param newList new list name
     * @return boolea true if successful, false if not
     *
     * @see com.jukuproject.jukutweet.Dialogs.EditMyListDialog
     * @see com.jukuproject.jukutweet.MainActivity#onEditMyListDialogPositiveClick(int, String, boolean)
     * @see com.jukuproject.jukutweet.MainActivity#showRenameMyListDialog(String)
     */
    public boolean renameMyList(String oldListName, String newList) {
        try{
        SQLiteDatabase db = sInstance.getWritableDatabase();

            String sql = "Update " + TABLE_FAVORITES_LISTS + "  SET [Name]=? WHERE [Name]=? ";
            SQLiteStatement statement = db.compileStatement(sql);
            statement.bindString(1, newList);
            statement.bindString(2, oldListName);
            statement.executeUpdateDelete();

            String sql2 = "Update JFavorites SET [Name]=? WHERE [Name]=? and [Sys] = 0";
            SQLiteStatement statement2 = db.compileStatement(sql2);
            statement2.bindString(1, newList);
            statement2.bindString(2, oldListName);
            statement2.executeUpdateDelete();
            return true;
        } catch(SQLiteException exception) {
            return false;
        }
    }

    /**
     * For word entries
     * @param kanjiID
     * @param originalColor
     * @param updatedColor
     * @return
     */
    public boolean changeFavoriteListEntry(int kanjiID,String originalColor,String updatedColor) {
        SQLiteDatabase db = sInstance.getWritableDatabase();

        try {
            if(originalColor.equals("Black") && updatedColor.equals("Black")) {
                Log.e(TAG,"ERROR! changefavoritelistentry Both entries are BLACK. So do nothing.");
                return false;
            } else if(originalColor.equals("Black")) {
                //Insert statement only
                return addKanjiToMyList(kanjiID,updatedColor,1);
            } else if(updatedColor.equals("Black")) {
                //Delete statement only
                return removeKanjiFromMyList(kanjiID,originalColor,1);

            } else {
                String sql = "Update "  + TABLE_FAVORITES_LIST_ENTRIES + " SET "+TFAVORITES_COL0+" = ? WHERE "+ TFAVORITES_COL0 +"= ? and " + TFAVORITES_COL1+" = 1 and " + COL_ID + " = ?";
                SQLiteStatement statement = db.compileStatement(sql);
                statement.bindString(1, updatedColor);
                statement.bindString(2, originalColor);
                statement.bindString(3, String.valueOf(kanjiID));
                statement.executeUpdateDelete();

                return true;
            }

        }  catch (SQLiteException e) {
            Log.e(TAG,"changefavoritelistentry sqlite exception: " + e);
        }  catch (NullPointerException e) {
            Log.e(TAG,"changefavoritelistentry something was null: " + e);
        } finally {
            db.close();
        }

        return false;

    }

    /**
     *
     * @return
     */
    public boolean changeFavoriteListEntryTweet(String tweetId,String userId, String originalColor,String updatedColor) {
        SQLiteDatabase db = sInstance.getWritableDatabase();

        try {
            if(originalColor.equals("Black") && updatedColor.equals("Black")) {
                Log.e(TAG,"ERROR! changefavoritelistentry Both entries are BLACK. So do nothing.");
                return false;
            } else if(originalColor.equals("Black")) {
                //Insert statement only
                return addTweetToMyList(tweetId,userId,updatedColor,1);
            } else if(updatedColor.equals("Black")) {
                //Delete statement only
                return removeTweetFromMyList(tweetId,originalColor,1);

            } else {


                String sql = "Update "  + TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " SET "+ TFAVORITES_COL0 +" = ? WHERE "+ TFAVORITES_COL0 +"= ? and " + TFAVORITES_COL1+" = 1 and " + COL_ID + " = ?";
                SQLiteStatement statement = db.compileStatement(sql);
                statement.bindString(1, updatedColor);
                statement.bindString(2, originalColor);
                statement.bindString(3, tweetId);
                statement.executeUpdateDelete();

                return true;
            }

        }  catch (SQLiteException e) {
            Log.e(TAG,"changefavoritelistentry sqlite exception: " + e);
        }  catch (NullPointerException e) {
            Log.e(TAG,"changefavoritelistentry something was null: " + e);
        } finally {
            db.close();
        }

        return false;

    }

    public boolean addKanjiToMyList(int kanjiId, String listName, int listSys) {
        SQLiteDatabase db = sInstance.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_ID, kanjiId);
            values.put(TFAVORITES_COL0, listName);
            values.put(TFAVORITES_COL1, listSys);
            db.insert(TABLE_FAVORITES_LIST_ENTRIES, null, values);
            return true;
        } catch (SQLiteException e) {
            Log.e(TAG, "addKanjiToMyList sqlite exception: " + e);
        } catch (NullPointerException e) {
            Log.e(TAG, "addKanjiToMyList something was null: " + e);
        } finally {
            db.close();
        }

        return  false;
    }


    public boolean addScoreToScoreBoard(int kanjiId,  int total,int correct) {
        SQLiteDatabase db = sInstance.getReadableDatabase();

        try {
        ContentValues values = new ContentValues();
        values.clear();
        values.put(COL_ID, kanjiId);
        values.put(TSCOREBOARD_COL0, total);
        values.put(TSCOREBOARD_COL1, correct);
        db.insertWithOnConflict(InternalDB.TABLE_SCOREBOARD, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
            return true;

        } catch (SQLiteException e) {
            Log.e(TAG, "addScoreToScoreBoard sqlite exception: " + e);
            return false;

        } catch (NullPointerException e) {
            Log.e(TAG, "addScoreToScoreBoard something was null: " + e);
            return false;
        } finally {
            db.close();
        }
    }

    public boolean addTweetToMyList(String tweet_id,String user_id,String listName, int listSys) {
        SQLiteDatabase db = sInstance.getReadableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(COL_ID, tweet_id);
            values.put(TSAVEDTWEET_COL0, user_id);
            values.put(TFAVORITES_COL0, listName);
            values.put(TFAVORITES_COL1, listSys);
            db.insert(TABLE_FAVORITES_LISTS_TWEETS_ENTRIES, null, values);
            return true;

        } catch (SQLiteException e) {
            Log.e(TAG, "addKanjiToMyList sqlite exception: " + e);
            return false;

        } catch (NullPointerException e) {
            Log.e(TAG, "addKanjiToMyList something was null: " + e);
            return false;
        } finally {
            db.close();
        }

    }

    public boolean removeTweetFromMyList(String tweetid, String listName, int listSys) {
        SQLiteDatabase db = sInstance.getWritableDatabase();
        try {
            db.delete(TABLE_FAVORITES_LISTS_TWEETS_ENTRIES,
                    TFAVORITES_COL0 + " = ? and "  + TFAVORITES_COL1 + " = ? and " + COL_ID + " = ? ", new String[]{listName,String.valueOf(listSys),tweetid});
            return true;
        } catch (SQLiteException e) {
            Log.e(TAG, "removeTweetFromMyList sqlite exception: " + e);
        } catch (NullPointerException e) {
            Log.e(TAG, "removeTweetFromMyList something was null: " + e);
        } finally {
            db.close();
        }
        return  false;
    }

    public boolean removeKanjiFromMyList(int kanjiId, String listName, int listSys) {
        SQLiteDatabase db = sInstance.getWritableDatabase();
        try {
            db.delete(TABLE_FAVORITES_LIST_ENTRIES,
                    TFAVORITES_COL0 + " = ? and "  + TFAVORITES_COL1 + " = ? and " + COL_ID + " = ? ", new String[]{listName,String.valueOf(listSys),String.valueOf(kanjiId)});
            return true;
        } catch (SQLiteException e) {
            Log.e(TAG, "removeKanjiFromMyList sqlite exception: " + e);
        } catch (NullPointerException e) {
            Log.e(TAG, "removeKanjiFromMyList something was null: " + e);
        } finally {
            db.close();
        }
        return  false;
    }

    public boolean removeBulkKanjiFromMyList(String kanjiIdString, MyListEntry myListEntry) {
        SQLiteDatabase db = sInstance.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " + TABLE_FAVORITES_LIST_ENTRIES + " WHERE " + TFAVORITES_COL0 + " = ? and "  + TFAVORITES_COL1 + " = ? and " + COL_ID + " in (" + kanjiIdString + ")",new String[]{myListEntry.getListName(),String.valueOf(myListEntry.getListsSys())});
            return true;
        } catch (SQLiteException e) {
            Log.e(TAG, "removeBulkKanjiFromMyList sqlite exception: " + e);
        } catch (NullPointerException e) {
            Log.e(TAG, "removeBulkKanjiFromMyList something was null: " + e);
        } finally {
            db.close();
        }
        return  false;
    }

    public boolean removeBulkTweetsFromSavedTweets(String bulkTweetIds, MyListEntry myListEntry) {
        SQLiteDatabase db = sInstance.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " + TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " WHERE " + TFAVORITES_COL0 + " = ? and "  + TFAVORITES_COL1 + " = ? and " + COL_ID + " in (" + bulkTweetIds + ")",new String[]{myListEntry.getListName(),String.valueOf(myListEntry.getListsSys())});
            return true;
        } catch (SQLiteException e) {
            Log.e(TAG, "removeBulkKanjiFromMyList sqlite exception: " + e);
        } catch (NullPointerException e) {
            Log.e(TAG, "removeBulkKanjiFromMyList something was null: " + e);
        } finally {
            db.close();
        }
        return  false;
    }



    public ArrayList<MyListEntry> getFavoritesListsForTweet(ArrayList<String> activeFavoriteStars
            , String tweetIds
            ,@Nullable MyListEntry entryToExclude) {

        ArrayList<MyListEntry> myListEntries = new ArrayList<>();
        SQLiteDatabase db = sInstance.getWritableDatabase();
        try {
            //Get a list of distinct favorites lists and their selection levels for the given tweet
            //Except for if a certain favorite list exists that we do not want to include (as occurs in the mylist copy dialog)
            Cursor c = db.rawQuery("Select [Lists].[Name] "    +
                    ",[Lists].[Sys] "    +
                    ",(Case when UserEntries.Name is null then 0 else 1 END) as SelectionLevel "    +
                    " "    +
                    "from "    +
                    "( "    +
                    "Select distinct Name "    +
                    ", 0 as Sys "    +
                    "From " + TABLE_FAVORITES_LISTS_TWEETS + " "    +
                    "UNION "    +
                    "Select 'Blue' as Name, 1 as Sys "    +
                    "UNION "    +
                    "Select 'Green' as Name, 1 as Sys "    +
                    "UNION "    +
                    "Select 'Red' as Name, 1 as Sys "    +
                    "UNION "    +
                    "Select 'Yellow' as Name, 1 as Sys "    +
                    "UNION "    +
                    "Select 'Purple' as Name, 1 as Sys "    +
                    "UNION "    +
                    "Select 'Orange' as Name, 1 as Sys "    +
                    ") as Lists "    +
                    "LEFT JOIN "    +
                    "( "    +
                    "Select Distinct Name "    +
                    ",Sys "    +
                    ", Count(_id) as UserCount " +
                    "FROM " + TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " "    +
                    " Where _id in (?) " +
                    " Group by Name, Sys " +
                    ") as UserEntries "    +
                    " "    +
                    "ON Lists.Name = UserEntries.Name ", new String[]{tweetIds});

            c.moveToFirst();
            if(c.getCount()>0) {

                while (!c.isAfterLast()) {
                    //Add all user lists (where sys == 0)
                    //Only add system lists (sys==1), but only if the system lists are actived in user preferences
                    if((c.getInt(1) == 0 || activeFavoriteStars.contains(c.getString(0)))
                            && (entryToExclude!= null && !(entryToExclude.getListName().equals(c.getString(0)) && entryToExclude.getListsSys() == c.getInt(1)))) {
                        MyListEntry entry = new MyListEntry(c.getString(0),c.getInt(1),c.getInt(2));
                        myListEntries.add(entry);
                        if(BuildConfig.DEBUG) { Log.d(TAG,"RETURN LISTNAME: " + c.getString(0) + ", SYS: " + c.getInt(1));}
                    }
                    c.moveToNext();
                }
            }
            c.close();

        }  catch (SQLiteException e) {
            Log.e(TAG,"gettweetfavoritelists sqlite exception: " + e);
        }  catch (NullPointerException e) {
            Log.e(TAG,"gettweetfavoritelists something was null: " + e);
        } finally {
            db.close();
        }
        return myListEntries;
    }





    /**
     * Pulls all active MyList lists from the db, and notes whether each list contains ALL of a certain kanji (or multiple kanjis),
     * Some, or None.

     * @param activeFavoriteStars
     * @param kanjiIds
     * @param entryToExclude
     * @return
     */
    public ArrayList<MyListEntry> getFavoritesListsForKanji(ArrayList<String> activeFavoriteStars
            , String kanjiIds
            ,@Nullable MyListEntry entryToExclude) {

        ArrayList<MyListEntry> myListEntries = new ArrayList<>();
        SQLiteDatabase db = sInstance.getWritableDatabase();
        try {


            //Get a list of distinct favorites lists and their selection levels for the given kanji
            //Except for if a certain favorite list exists that we do not want to include (as occurs in the mylist copy dialog)
            Cursor c = db.rawQuery("Select [Lists].[Name] "    +
                    ",[Lists].[Sys] "    +
                    ",(Case when UserEntries.Name is null then 0 else 1 END) as SelectionLevel "    +
                    " "    +
                    "from "    +
                    "( "    +
                    "Select distinct Name "    +
                    ", 0 as Sys "    +
                    "From " + TABLE_FAVORITES_LISTS + " "    +
                    "UNION "    +
                    "Select 'Blue' as Name, 1 as Sys "    +
                    "UNION "    +
                    "Select 'Green' as Name, 1 as Sys "    +
                    "UNION "    +
                    "Select 'Red' as Name, 1 as Sys "    +
                    "UNION "    +
                    "Select 'Yellow' as Name, 1 as Sys "    +
                    "UNION "    +
                    "Select 'Purple' as Name, 1 as Sys "    +
                    "UNION "    +
                    "Select 'Orange' as Name, 1 as Sys "    +
                    ") as Lists "    +
                    "LEFT JOIN "    +
                    "( "    +
                    "Select Distinct Name "    +
                    ",Sys "    +
                    ", Count(_id) as UserCount " +
                    "FROM " + TABLE_FAVORITES_LIST_ENTRIES + " "    +
                    " Where _id in (?) " +
                    " Group by Name, Sys " +
                    ") as UserEntries "    +
                    " "    +
                    "ON Lists.Name = UserEntries.Name ", new String[]{kanjiIds});

            c.moveToFirst();
            if(c.getCount()>0) {

                while (!c.isAfterLast()) {
                    //Add all user lists (where sys == 0)
                    //Only add system lists (sys==1), but only if the system lists are actived in user preferences
                    if((c.getInt(1) == 0 || activeFavoriteStars.contains(c.getString(0)))
                            && (entryToExclude!= null && !(entryToExclude.getListName().equals(c.getString(0)) && entryToExclude.getListsSys() == c.getInt(1)))) {
                        MyListEntry entry = new MyListEntry(c.getString(0),c.getInt(1),c.getInt(2));
                        myListEntries.add(entry);
                        Log.d(TAG,"RETURN LISTNAME: " + c.getString(0) + ", SYS: " + c.getInt(1));
                    }
                    c.moveToNext();
                }
            }
            c.close();

        }  catch (SQLiteException e) {
            Log.e(TAG,"getlist sqlite exception: " + e);
        }  catch (NullPointerException e) {
            Log.e(TAG,"getlist something was null: " + e);
        } finally {
            db.close();
        }
        Log.d(TAG,"GETFAVS returning mylist entries: " + myListEntries.size());
        return myListEntries;
    }

    /**
     * Retrieves a list of word entries from edict dictionary for a particular favorite list. Favorites list can either by Tweet favorites
     * or regular Word favorites (using parameter mylisttype). Words are also filtered by word "color" (a grading of the words quiz score),
     * @param mylistType type of list
     * @param myListEntry
     * @param colorThresholds
     * @param colorString
     * @return
     */

    public ArrayList<WordEntry> getMyListWords(String mylistType
            , MyListEntry myListEntry
            , ColorThresholds colorThresholds
            , String colorString
            , @Nullable Integer excludeIdInteger
            , @Nullable Integer resultLimit) {

        String idToExclude;
        if(excludeIdInteger ==null) {
            idToExclude = "";
        } else {
            idToExclude = String.valueOf(excludeIdInteger);
        }

        Log.d(TAG,"ID TO EXCLUDE ENTER: " + excludeIdInteger + ", idto exclude string: " + idToExclude );
        String limit;
        if(resultLimit == null) {
            limit = "";
        } else {
            limit = "LIMIT " + String.valueOf(resultLimit);
        }

//        String favoritesTable;

        ArrayList<WordEntry> wordEntries = new ArrayList<>();
        SQLiteDatabase db = sInstance.getReadableDatabase();
        try {
            Cursor c;
            if(mylistType.equals("Tweet")) {
                c = db.rawQuery("Select [_id]" +
                                ",Kanji " +
                                ",Furigana " +
                                ",Definition " +
                                ",Correct " +
                                ",Total " +
                                ",Percent " +
                                ",Color " +
                                "FROM (" +
                                "SELECT  x.[_id]" +
                                ",x.[Kanji]" +
//                                            ",(CASE WHEN (x.Furigana is null OR  x.Furigana = '') then \"\" else \"(\" || x.Furigana || \")\" end) as [Furigana]" +
                                ",(CASE WHEN x.[Furigana] is null  then '' else x.[Furigana] end) as [Furigana] " +



                                ",x.[Definition]" +
                                ",ifnull(y.[Correct],0) as [Correct] " +
                                ",ifnull(y.[Total],0) as [Total] " +
                                ",(CASE WHEN [Total] >0 THEN CAST(ifnull([Correct],0)  as float)/[Total] ELSE 0 END) as [Percent] " +
                                " ,(CASE WHEN [Total] is NULL THEN 'Grey' " +
                                "WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 'Grey' " +
                                "WHEN CAST(ifnull([Correct],0)  as float)/[Total] < " + colorThresholds.getRedThreshold() + "  THEN 'Red' " +
                                "WHEN CAST(ifnull([Correct],0)  as float)/[Total] <  " + colorThresholds.getYellowThreshold() + " THEN 'Yellow' " +
                                "ELSE 'Green' END) as [Color] " +
                                "FROM " +
                                "(" +
                                "SELECT [_id]" +
                                ",[Kanji]" +
                                ",[Furigana]" +
                                ",[Definition]  " +
                                "FROM [Edict] " +
                                "WHERE [_id] IN (" +
                                    "SELECT DISTINCT Edict_id as [_id] " +
                                    "FROM "+
                                    "( " +
                                    "SELECT DISTINCT Tweet_id " +
                                    "FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
                                    " WHERE [Name] = ? and [Sys] = ? and [_id] <> ? " +
                                    ") as x " +
                                    "LEFT JOIN "  +
                                    "( " +
                                    "SELECT DISTINCT Tweet_id,Edict_id " +
                                    "FROM " + TABLE_SAVED_TWEET_KANJI + " " +
                                    ")  as y " +
                                    " ON x.Tweet_id = y.Tweet_id " +


                                ") " +
                                "ORDER BY [_id]" +
                                ") as x " +
                                "LEFT JOIN " +
                                "(" +
                                "SELECT [_id]" +
                                ",sum([Correct]) as [Correct]" +
                                ",sum([Total]) as [Total]  " +
                                "FROM [JScoreboard] " +
                                "WHERE [_id] IN (" +

                                    "SELECT DISTINCT Edict_id as [_id] " +
                                    "FROM "+
                                    "( " +
                                    "SELECT DISTINCT Tweet_id " +
                                    "FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
                                    " WHERE [Name] = ? and [Sys] = ? and [_id] <> ? " +
                                    ") as x " +
                                    "LEFT JOIN "  +
                                    "( " +
                                    "SELECT DISTINCT Tweet_id,Edict_id " +
                                    "FROM " + TABLE_SAVED_TWEET_KANJI + " " +
                                    ")  as y " +
                                    " ON x.Tweet_id = y.Tweet_id " +

                        ") " +
                                "GROUP BY [_id]" +
                                ") as y " +
                                "ON x.[_id] = y.[_id] " +
                                ") " +
                                "WHERE [Color] in (" +  colorString + ") "  +
                                " ORDER BY RANDOM() " + limit + " "
                        , new String[]{String.valueOf(myListEntry.getListsSys()),myListEntry.getListName(),idToExclude,String.valueOf(myListEntry.getListsSys()),myListEntry.getListName(),idToExclude});


            } else {
                c = db.rawQuery("Select [_id]" +
                                ",Kanji " +
                                ",Furigana " +
                                ",Definition " +
                                ",Correct " +
                                ",Total " +
                                ",Percent " +
                                ",Color " +
                                "FROM (" +
                                "SELECT  x.[_id]" +
                                ",x.[Kanji]" +
//                                            ",(CASE WHEN (x.Furigana is null OR  x.Furigana = '') then \"\" else \"(\" || x.Furigana || \")\" end) as [Furigana]" +
                                ",(CASE WHEN x.[Furigana] is null  then '' else x.[Furigana] end) as [Furigana] " +



                                ",x.[Definition]" +
                                ",ifnull(y.[Correct],0) as [Correct] " +
                                ",ifnull(y.[Total],0) as [Total] " +
                                ",(CASE WHEN [Total] >0 THEN CAST(ifnull([Correct],0)  as float)/[Total] ELSE 0 END) as [Percent] " +
                                " ,(CASE WHEN [Total] is NULL THEN 'Grey' " +
                                "WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 'Grey' " +
                                "WHEN CAST(ifnull([Correct],0)  as float)/[Total] < " + colorThresholds.getRedThreshold() + "  THEN 'Red' " +
                                "WHEN CAST(ifnull([Correct],0)  as float)/[Total] <  " + colorThresholds.getYellowThreshold() + " THEN 'Yellow' " +
                                "ELSE 'Green' END) as [Color] " +
                                "FROM " +
                                "(" +
                                "SELECT [_id]" +
                                ",[Kanji]" +
                                ",[Furigana]" +
                                ",[Definition]  " +
                                "FROM [Edict] " +
                                "WHERE [_id] IN (" +
                                "SELECT [_id] " +
                                "FROM " + TABLE_FAVORITES_LIST_ENTRIES +  "  " +
                                "WHERE ([Sys] = ? and [Name] = ? and _id <> ?)" +
                                ") " +
                                "ORDER BY [_id]" +
                                ") as x " +
                                "LEFT JOIN " +
                                "(" +
                                "SELECT [_id]" +
                                ",sum([Correct]) as [Correct]" +
                                ",sum([Total]) as [Total]  " +
                                "FROM [JScoreboard] " +
                                "WHERE [_id] IN (" +
                                "SELECT [_id] " +
                                "FROM " + TABLE_FAVORITES_LIST_ENTRIES +  "  " +
                                "WHERE ([Sys] = ? and [Name] = ? and _id <> ?))  " +
                                "GROUP BY [_id]" +
                                ") as y " +
                                "ON x.[_id] = y.[_id] " +
                                ") " +
                                "WHERE [Color] in (" +  colorString + ") "  +
                                " ORDER BY RANDOM() " + limit + " "
                        , new String[]{String.valueOf(myListEntry.getListsSys()),myListEntry.getListName(),idToExclude,String.valueOf(myListEntry.getListsSys()),myListEntry.getListName(),idToExclude});




            }


            if(c.getCount()>0) {
                c.moveToFirst();
                while (!c.isAfterLast())

                {
                    WordEntry wordEntry = new WordEntry();
                    wordEntry.setId(c.getInt(0));
                    wordEntry.setKanji(c.getString(1));
                    wordEntry.setFurigana(c.getString(2));
                    wordEntry.setDefinition(c.getString(3));
                    wordEntry.setCorrect(c.getInt(4));
                    wordEntry.setTotal(c.getInt(5));
                    wordEntries.add(wordEntry);

                    c.moveToNext();
                }
                c.close();
            } else {
                if(debug) {Log.d(TAG,"c.getcount was 0!!");}
            }
        } catch (SQLiteException e){
            Log.e(TAG,"getmylistwords Sqlite exception: " + e);
        } catch (Exception e) {
            Log.e(TAG,"getmylistwords generic exception: " + e);
        } finally {
            db.close();
        }
        return wordEntries;
    }



    public boolean addBulkKanjiToList(MyListEntry myListEntry,String kanjiString) {
        SQLiteDatabase db = sInstance.getReadableDatabase();
        try {
            db.execSQL("INSERT OR REPLACE INTO " + TABLE_FAVORITES_LIST_ENTRIES +" SELECT DISTINCT [_id],? as [Name], ? as [Sys] FROM [Edict] WHERE [_id] in (" + kanjiString + ")",new String[]{myListEntry.getListName(),String.valueOf(myListEntry.getListsSys())});
            return true;
        } catch (SQLiteException e){
            Log.e(TAG,"copyKanjiToList Sqlite exception: " + e);
            return false;
        } catch (Exception e) {
            Log.e(TAG,"copyKanjiToList generic exception: " + e);
            return false;
        } finally {
            db.close();
        }
    }

    public boolean addBulkTweetsToList(MyListEntry myListEntry,String bulkTweetIds) {
        SQLiteDatabase db = sInstance.getReadableDatabase();
        try {

            db.execSQL("INSERT OR REPLACE INTO " + TABLE_FAVORITES_LISTS_TWEETS_ENTRIES +" SELECT DISTINCT Tweet_id as [_id], [UserId], ? as [Name], ? as [Sys] FROM " + TABLE_SAVED_TWEETS  + " WHERE Tweet_id in (" + bulkTweetIds + ")",new String[]{myListEntry.getListName(),String.valueOf(myListEntry.getListsSys())});
            return true;
        } catch (SQLiteException e){
            Log.e(TAG,"copyBulkTweetsToList Sqlite exception: " + e);
            return false;
        } catch (Exception e) {
            Log.e(TAG,"copyBulkTweetsToList generic exception: " + e);
            return false;
        } finally {
            db.close();
        }
    }




    /**
     * Checks if an instance of parsed kanji for a given tweet already exists in the parsed kanji TABLE_SAVED_TWEET_KANJI database.
     * Check is based on Tweet_id. The check is made seperately from {@link #tweetExistsInDB(Tweet)} because the {@link #saveParsedTweetKanji(ArrayList, String)}
     * method occurs separately from {@link #saveTweetToDB(UserInfo, Tweet)}, and a tweet may be saved while the user is not online, in
     * which case the {@link #saveParsedTweetKanji(ArrayList, String)} part would occur later, when the tweet is clicked in {@link com.jukuproject.jukutweet.Fragments.SavedTweetsBrowseFragment}
     * @param tweet Tweet in question
     * @return resultvalue: -1 for error, otherwise the count of instance of the tweet in the database
     */
    public int tweetParsedKanjiExistsInDB(Tweet tweet) {
        int resultCode = -1;
        try {
            String Query = "Select * from " + TABLE_SAVED_TWEET_KANJI + " where " + TSAVEDTWEET_COL2 + " = " + tweet.getIdString();
            Cursor cursor = sInstance.getWritableDatabase().rawQuery(Query, null);
            resultCode = cursor.getCount();
            cursor.close();
        } catch (SQLiteException e){
            Log.e(TAG,"tweetParsedKanjiExistsInDB Sqlite exception: " + e);
        } catch (Exception e) {
            Log.e(TAG,"tweetParsedKanjiExistsInDB generic exception: " + e);
        }

        return resultCode;
    }

    /**
     * Checks if an instance of a tweet already exists in the databse. Check is based on Tweet_id.
     * @param tweet Tweet in question
     * @return resultvalue: -1 for error, otherwise the count of instance of the tweet in the database
     */
    public int tweetExistsInDB(Tweet tweet) {

        int resultCode = -1;
        try {
            String Query = "Select * from " + TABLE_SAVED_TWEETS + " where " + TSAVEDTWEET_COL2 + " = " + tweet.getIdString();
            Cursor cursor = sInstance.getWritableDatabase().rawQuery(Query, null);
            resultCode = cursor.getCount();
            cursor.close();
        } catch (SQLiteException e){
        Log.e(TAG,"tweetExistsInDB Sqlite exception: " + e);
        return resultCode;
    } catch (Exception e) {
        Log.e(TAG,"tweetExistsInDB generic exception: " + e);
        return resultCode;
    }
        return resultCode;
    }


    /**
     * Saves a tweet to the database. Called from {@link com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter} or
     * {@link com.jukuproject.jukutweet.Fragments.TweetBreakDownFragment} when user clicks on favorites star
     *
     * This is the first step in a three step process:
     * 1. Save the tweet information
     * 2. Save associated tweet urls to TABLE_SAVED_TWEET_URLS with {@link #saveTweetUrls(Tweet)}
     * 3. Save a list of kanji contained in the tweet with {@link #saveParsedTweetKanji(ArrayList, String)}
     *
     * @param userInfo UserInfo object, includes user name, user_id
     * @param tweet Tweet object, with all tweet info
     * @return returnvalue int, -1 for error, otherwise the index value of final inserted row
     */
    public int saveTweetToDB(UserInfo userInfo, Tweet tweet){
        int resultCode = -1;
        try {

            //At least one of these values have to exist
            if(tweet.getIdString() != null && (userInfo.getScreenName() != null || userInfo.getUserId() != null)) {
                ContentValues values = new ContentValues();

                if(userInfo.getUserId() != null) {
                    values.put(TSAVEDTWEET_COL0, userInfo.getUserId());
                }
                if(userInfo.getScreenName() != null) {
                    values.put(TSAVEDTWEET_COL1, userInfo.getScreenName().trim());
                }
                if(tweet.getIdString() != null) {
                    values.put(TSAVEDTWEET_COL2, tweet.getIdString());
                }

                if(tweet.getCreatedAt() != null) {
                    values.put(TSAVEDTWEET_COL3, tweet.getCreatedAt().trim());
                }

                values.put(TSAVEDTWEET_COL4, tweet.getText().trim());

                resultCode = (int)sInstance.getWritableDatabase().insertWithOnConflict(TABLE_SAVED_TWEETS, null, values,SQLiteDatabase.CONFLICT_REPLACE);

                return resultCode;
            } else {
                Log.e(TAG,"Can't insert tweet to db. Either tweet.getId() is null: " + (tweet.getIdString()==null)
                        + ", or both userinfo name and id are null");
                return resultCode;
            }
        } catch(SQLiteException exception) {
            return resultCode;
        }
    }


    /**
     * Saves the Urls from a tweet into the TABLE_SAVED_TWEET_URLS table. Used in conjunction with
     * other "Save Tweet" methods. Note: Urls may not exist for a given tweet.
     *
     * @see #saveTweetToDB(UserInfo, Tweet)
     * @see #saveParsedTweetKanji(ArrayList, String)
     *
     * @param tweet Tweet to be saved.
     * @return returnvalue int, -1 for error, otherwise the index value of final inserted row
     */
    public int saveTweetUrls(Tweet tweet) {
        int resultCode= -1;
        SQLiteDatabase db = sInstance.getReadableDatabase();

        try {

            for(TweetUrl tweetUrl : tweet.getEntities().getUrls()) {
                ContentValues values = new ContentValues();
                values.put(TSAVEDTWEET_COL2, tweet.getIdString());
                values.put(TSAVEDTWEETURLS_COL1, tweetUrl.getExpanded_url());
                values.put(TSAVEDTWEETURLS_COL2, tweetUrl.getIndices()[0]);
                values.put(TSAVEDTWEETURLS_COL3, tweetUrl.getIndices()[1]);
                resultCode =  (int)db.insert(TABLE_SAVED_TWEET_URLS, null, values);
            }

            return resultCode;
        } catch(SQLiteException exception) {
            Log.e(TAG,"Unable to save tweet urls");
            return resultCode;
        } finally {
            db.close();
        }
    }


    /**
     * Saves the kanji from a broken-up Tweet (represented by a list of ParseSentenceItems) into the
     * TABLE_SAVED_TWEET_KANJI table. Either called when a user clicks on a star in the {@link com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter}, in which
     * case the saving process is run in the background while the user browses tweets, OR, when the user clicks a
     * star in the {@link com.jukuproject.jukutweet.Fragments.TweetBreakDownFragment} and the parseSentenceItems are already
     * created and passed here to be saved.
     *
//     * @param parseSentenceItems List of kanji found in a tweet
     * @param tweet_id String id of tweet
     * @return returnvalue int, -1 for error, otherwise the index value of final inserted row
     */
    public int saveParsedTweetKanji(ArrayList<WordEntry> wordEntries, String tweet_id) {
        SQLiteDatabase db = sInstance.getReadableDatabase();
        int resultCode = -1;

        try {
            if(wordEntries.size()>0) {
                for(int i=0;i<wordEntries.size();i++) {
                        ContentValues values = new ContentValues();
                        values.put(TSAVEDTWEET_COL2, tweet_id);
                        values.put(TSAVEDTWEETITEMS_COL2, wordEntries.get(i).getId());
                        values.put(TSAVEDTWEETITEMS_COL3, wordEntries.get(i).getStartIndex());
                        values.put(TSAVEDTWEETITEMS_COL4, wordEntries.get(i).getEndIndex());
                        resultCode = (int)db.insert(TABLE_SAVED_TWEET_KANJI, null, values);
                }
            }
            return resultCode;
        } catch(SQLiteException exception) {
            return resultCode;
        } finally {
            db.close();
        }

    }


    /**
     * Pulls counts for the different system/user-created lists that a tweet is associated with, used to assign
     * the color of the Tweet favorites star in {@link com.jukuproject.jukutweet.Fragments.UserTimeLineFragment }
     * @param userId id of twitter user who's tweets are being viewed
     * @return HashMap with <TweetId, ItemFavorites star colors for the tweet>. This is then attached to the group of tweets
     * flowing in from the Twitter API (based on TweetId) and passed together to the {@link com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter}
     */
    public HashMap<String,ItemFavorites> getStarFavoriteDataForAUsersTweets(String userId){
        SQLiteDatabase db = sInstance.getReadableDatabase();
        HashMap<String,ItemFavorites> map = new HashMap<>();
        try {
            Cursor c = db.rawQuery(

                    " SELECT [_id]" +
                            ",SUM([Blue]) as [Blue]" +
                            ",SUM([Red]) as [Red]" +
                            ",SUM([Green]) as [Green]" +
                            ",SUM([Yellow]) as [Yellow]" +
                            ",SUM([Purple]) as [Purple]" +
                            ",SUM([Orange]) as [Orange]" +

                            ", SUM([Other]) as [Other] " +
                            "FROM (" +
                            "SELECT [_id] " +
                            ",(CASE WHEN ([Sys] = 1 and Name ='Blue') then 1 else 0 end) as [Blue]" +
                            ",(CASE WHEN ([Sys] = 1 AND Name = 'Red') then 1 else 0 end) as [Red]" +
                            ",(CASE WHEN ([Sys] = 1 AND Name = 'Green') then 1 else 0 end) as [Green]" +
                            ",(CASE WHEN ([Sys] = 1  AND Name = 'Yellow') then 1 else 0 end) as [Yellow]" +
                            ",(CASE WHEN ([Sys] = 1  AND Name = 'Purple') then 1 else 0 end) as [Purple]" +
                            ",(CASE WHEN ([Sys] = 1  AND Name = 'Orange') then 1 else 0 end) as [Orange]" +
                            ", (CASE WHEN [Sys] <> 1 THEN 1 else 0 end) as [Other] " +
                            "FROM " + TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                            " WHERE UserId = ? " +
                            ") as [All]" +
                            "Group by [_id] " , new String[]{userId});

                Log.d(TAG,"SAVED ENTRIES COUNT: " + c.getCount());
            if (c.moveToFirst()) {
                do {

                    ItemFavorites itemFavorites = new ItemFavorites(c.getInt(1)
                            ,c.getInt(2)
                            ,c.getInt(3)
                            ,c.getInt(4)
                            ,c.getInt(5)
                            ,c.getInt(6)
                            ,c.getInt(7));

                    map.put(c.getString(0),itemFavorites);

                } while (c.moveToNext());
            }
            c.close();


        } catch (SQLiteException e){
            Log.e(TAG,"getStarFavoriteDataForAUsersTweets Sqlite exception: " + e);
        } finally {
            db.close();
        }

        return  map;
    }


    /**
     * Retrives all the saved tweets in a Saved Tweets List. Used in {@link com.jukuproject.jukutweet.Fragments.SavedTweetsBrowseFragment}
     *
     * @param myListEntry The list whose kanji this method will pull. THe MyListEntry object is really just a container for a listname and sys variable.
     * @param colorThresholds Thresholds designating when a Kanji, or a tweet should be considered part of a color category (Grey, Red, Yellow, Green),
     *                        based on the quiz score of a kanji (or kanjis within a tweet)
     * @return List of tweet objects for each tweet in the list. The tweet objects contain
     * the tweet data as well as an array of TweetKanjiColor objects with data for each kanji found within the tweet
     * (saved in the SavedTweetKanji table). The TweetKanjiColor object is used to color the kanji in the {@link com.jukuproject.jukutweet.Adapters.BrowseTweetsAdapter}
     * and is also passed on to the {@link com.jukuproject.jukutweet.Fragments.TweetBreakDownFragment} when the user clicks on a tweet,
     * making it unnecessary to run the time consuming ParseSentence process again.
     *
     *
     */

    public ArrayList<Tweet> getTweetsForSavedTweetsList(MyListEntry myListEntry , ColorThresholds colorThresholds) {
        ArrayList<Tweet> savedTweets = new ArrayList<>();
        SQLiteDatabase db = sInstance.getReadableDatabase();
        try {
            Cursor c = db.rawQuery("SELECT TweetLists.[Name]" +
                                ",TweetLists.[Sys]" +
                                ",TweetLists.[UserId] " +
                                ",UserName.ScreenName " +
                                ",UserName.UserName " +
                                ",TweetLists.[Tweet_id]" +
                                ",[ALLTweets].[Text] " +
                                ",TweetKanji.Edict_id " +
                                ",TweetKanji.Kanji " +
                                ",(CASE WHEN [TweetKanji].[Furigana] is null  then '' else [TweetKanji].[Furigana] end) as [Furigana] " +
                                ",TweetKanji.Definition " +
                            ",TweetKanji.Total " +
                            ",TweetKanji.Correct " +
                            ",TweetKanji.Color " +
                                ",TweetKanji.StartIndex " +
                                ",TweetKanji.EndIndex " +
                                ",[ALLTweets].[Date]" +

                            "FROM  " +
                            " ( " +
                                "SELECT  DISTINCT [Name]" +
                                ",[Sys]" +
                                ",[UserId] " +
                                ",[_id] as [Tweet_id]" +
                                "FROM "+ TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                                "WHERE [Name] = ? and cast([Sys] as INTEGER) = ? " +
                            ") as TweetLists " +
                            " LEFT JOIN " +
                            " ( " +
                            "SELECT  DISTINCT [_id] " +
                            ",[Tweet_id]" +
                            ",[Text]" +
                            ",[CreatedAt]  as [Date] " +
                            " FROM "+ TABLE_SAVED_TWEETS + " " +
                            ") as ALLTweets " +
                            "ON TweetLists.[Tweet_id] = ALLTweets.[Tweet_id] " +
                            " LEFT JOIN " +
                            " ( " +
                                /* Get a list of  kanji ids and their word scores for each tweet */
                            "SELECT a.[Tweet_id]" +
                            ",a.[Edict_id]" +
                            ",a.[StartIndex]" +
                            ",a.[EndIndex]" +

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
//                            ",CAST(ifnull(b.[Correct],0)  as float)/b.[Total] as [Percent] " +

                            "FROM " +
                            "( " +
                            " SELECT Tweet_id" +
                            ",Edict_id " +
                            ",[StartIndex]" +
                            ",[EndIndex]" +
                            "From " + TABLE_SAVED_TWEET_KANJI  + " " +
                            " WHERE [Edict_id] is not NULL and StartIndex is not NULL and EndIndex is not NULL and EndIndex > StartIndex " +
                            ") as a " +
                            "LEFT JOIN " +
                            " (" +
                            "SELECT [_id] as [Edict_id]" +
                            ",sum([Correct]) as [Correct]" +

                            ",sum([Total]) as [Total] FROM [JScoreboard] " +
                            "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + TABLE_SAVED_TWEET_KANJI  + ")" +
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
                            "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + TABLE_SAVED_TWEET_KANJI  + ")" +
                            ") as c " +
                            "ON a.[Edict_id] = c.[Edict_id] " +

                    " ) as TweetKanji " +
                            "On TweetLists.Tweet_id = TweetKanji.Tweet_id " +


                            "LEFT JOIN " +
                            " (" +
                            "SELECT DISTINCT [UserId] " +
                            ", [ScreenName] " +
                            ", [UserName] " +
                            " FROM " + TABLE_USERS +" " +
                            ") as [UserName] " +
                            "On TweetLists.UserId = UserName.UserId " +

                    "Order by date(ALLTweets.Date) Desc,TweetLists.[Tweet_id] asc,TweetKanji.StartIndex asc"
                    , new String[]{myListEntry.getListName(),String.valueOf(myListEntry.getListsSys())});



            /* The query pulls a list of tweetdata paired with each parsed-kanji in the tweet, resulting in
            * multiple duplicate lines of tweetdata. So the cursor ony adds tweet data once, when a new tweetid is found. Meanwhile
            * the kanji data for each row is added to a TweetKanjiColor object, which is then added to the kanji and the kanji
            * added to the final "savedTweets" list when a new tweetid appears (or the cursor finishes) */

            if(c.getCount()>0) {
                c.moveToFirst();
                String currentTweetId = c.getString(5);
                Tweet tweet = new Tweet();
                while (!c.isAfterLast())
                {

                    if(c.isFirst()) {
                        tweet.setIdString(c.getString(5));
                        tweet.setCreatedAt(c.getString(11));
                        tweet.getUser().setUserId(c.getString(2));
                        tweet.setText(c.getString(6));
                        tweet.getUser().setScreen_name(c.getString(3));
                        tweet.getUser().setName(c.getString(4));

                    }

                        WordEntry wordEntry = new WordEntry(c.getInt(7)
                                ,c.getString(8)
                                ,c.getString(9)
                                ,c.getString(10)
                                ,c.getInt(11)
                                ,c.getInt(12)
                                ,c.getString(13)
                                ,c.getInt(14)
                                ,c.getInt(15));

                    /* Decide whether or not to make this word entry a "spinner" entry  in the
                    * fillintheblanks quiz, based two criteria:
                    *   1. words chosen at random
                    *   2. there is a limit for the number of words that can be spinners in the tweet,
                    *       from 1 - 3, with a 50% chance of 1, 35% chance of 2 and a 15 % chance of 3*/


                        tweet.addWordEntry(wordEntry);


                    //FLush old tweet
                    if(!currentTweetId.equals(c.getString(5))){
                        savedTweets.add(new Tweet(tweet));
                        currentTweetId = c.getString(5);
                        tweet = new Tweet();

                        tweet.setIdString(c.getString(5));
                        tweet.setCreatedAt(c.getString(11));
                        tweet.getUser().setUserId(c.getString(2));
                        tweet.setText(c.getString(6));
                        tweet.getUser().setScreen_name(c.getString(3));
                        tweet.getUser().setName(c.getString(4));
                        }

                    if(c.isLast()) {
                        savedTweets.add(new Tweet(tweet));
                    }
                    c.moveToNext();
                    }


                } else {if(debug) {Log.d(TAG,"c.getcount was 0!!");}}
                c.close();

        } catch (SQLiteException e){
            Log.e(TAG,"getTweetsForSavedTweetsList Sqlite exception: " + e);
        } catch (Exception e) {
            Log.e(TAG,"getTweetsForSavedTweetsList generic exception: " + e);
        } finally {
            db.close();
        }
        return savedTweets;
    }



    public Cursor getWordEntryForKanjiId(int kanjiId, ColorThresholds colorThresholds) {
        return sInstance.getWritableDatabase().rawQuery("SELECT [Kanji]" +
                ",(CASE WHEN Furigana is null then '' else Furigana  end) as [Furigana]" +
                ",[Definition]" +
                ",[Total]" +
                ",[Correct]" +
                " ,[Blue]" +
                " ,[Red] " +
                " ,[Green] " +
                " ,[Yellow] " +
                " ,[Purple] " +
                " ,[Orange] " +

                " ,[Other] " +
//                                            ",(CASE WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 1 WHEN [Percent] < " + colorThresholds.getRedThreshold() + "  THEN 2 WHEN ([Percent] >= " + colorThresholds.getRedThreshold() + " and [Percent] <  " + colorThresholds.getYellowThreshold() + ") THEN 3 WHEN [Percent]>= " + colorThresholds.getYellowThreshold() + " THEN 4 END) as [Color] " +
                        ",(CASE WHEN [Total] is NULL THEN 'Grey' " +
                        "WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 'Grey' " +
                        "WHEN CAST(ifnull([Correct],0)  as float)/[Total] < " + colorThresholds.getRedThreshold() + "  THEN 'Red' " +
                        "WHEN CAST(ifnull([Correct],0)  as float)/[Total] <  " + colorThresholds.getYellowThreshold() + " THEN 'Yellow' " +
                        "ELSE 'Green' END) as [Color]" +

                        "FROM (" +
                "SELECT [_id]" +
                ",[Kanji]" +
                ",[Furigana]" +
                ",[Definition]" +
                ",ifnull([Total],0) as [Total]" +
                ",ifnull([Correct],0)  as [Correct]" +
                ",CAST(ifnull([Correct],0)  as float)/[Total] as [Percent] " +
                " ,[Blue]" +
                " ,[Red] " +
                " ,[Green] " +
                " ,[Yellow] " +
                " ,[Purple] " +
                " ,[Orange] " +

                " ,[Other] " +
                "FROM (" +
                "SELECT [_id]" +
                ",[Kanji]" +
                ",[Furigana]" +
                ",[Definition]  " +
                "FROM [Edict] where [_id] = ?" +
                ") NATURAL LEFT JOIN (" +
                "SELECT [_id]" +
                ",sum([Correct]) as [Correct]" +
                ",sum([Total]) as [Total] " +
                "from [JScoreboard] " +
                "WHERE [_id] = ? GROUP BY [_id]" +
                ") NATURAL LEFT JOIN (" +
                "SELECT [_id]" +
                ",SUM([Blue]) as [Blue]" +
                ",SUM([Red]) as [Red]" +
                ",SUM([Green]) as [Green]" +
                ",SUM([Yellow]) as [Yellow]" +
                ",SUM([Yellow]) as [Purple]" +
                ",SUM([Yellow]) as [Orange]" +

                ", SUM([Other]) as [Other] " +
                "FROM (" +
                "SELECT [_id] " +
                ",(CASE WHEN ([Sys] = 1 and Name = 'Blue') then 1 else 0 end) as [Blue]" +
                ",(CASE WHEN ([Sys] = 1 AND Name = 'Red') then 1 else 0 end) as [Red]" +
                ",(CASE WHEN ([Sys] = 1 AND Name = 'Green') then 1 else 0 end) as [Green]" +
                ",(CASE WHEN ([Sys] = 1  AND Name = 'Yellow') then 1 else 0 end) as [Yellow]" +
                ",(CASE WHEN ([Sys] = 1  AND Name = 'Purple') then 1 else 0 end) as [Purple]" +
                ",(CASE WHEN ([Sys] = 1  AND Name = 'Orange') then 1 else 0 end) as [Orange]" +
                ", (CASE WHEN [Sys] <> 1 THEN 1 else 0 end) as [Other] " +
                "FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
                "WHERE [_id] = ?) as x Group by [_id]" +

                "))", new String[]{String.valueOf(kanjiId),String.valueOf(kanjiId),String.valueOf(kanjiId)});
    }



    public Cursor getTweetListColorBlocksCursor(ColorThresholds colorThresholds, @Nullable MyListEntry myListEntry) {

        int ALL_LISTS_FLAG = 0;
        int sys = -1;
        String name = "";
        if(myListEntry == null) {
            ALL_LISTS_FLAG = 1;
        } else {
            sys = myListEntry.getListsSys();
            name = myListEntry.getListName();
        }

        return  sInstance.getWritableDatabase().rawQuery(

                "SELECT xx.[Name]" +
                        ",xx.[Sys]" +
                        ",ifnull(yy.[Total],0) as [Total]" +
                        ",ifnull(yy.[Grey],0) as [Grey]" +
                        ",ifnull(yy.[Red],0) as [Red]" +
                        ",ifnull(yy.[Yellow],0) as [Yellow]" +
                        ",ifnull(yy.[Green],0) as [Green] " +
                        ",ifnull(yy.[Empty],0) as [Empty] " +

                        "" +
                        "FROM (" +
                        "Select DISTINCT [Name],[Sys] " +
                        "FROM (" +
                        "SELECT [Name]" +
                        ",0 as [Sys] " +
                        "From " + TABLE_FAVORITES_LISTS_TWEETS +  " " +
                        "UNION " +
                        "SELECT 'Blue' as [Name]" +
                        ", 1 as [Sys] " +
                        "Union " +
                        "SELECT 'Red' as [Name]" +
                        ",1 as [Sys] " +
                        "Union " +
                        "SELECT 'Green' as [Name]" +
                        ",1 as [Sys] " +
                        "Union " +
                        "SELECT 'Yellow' as [Name]" +
                        ",1 as [Sys]" +
                        "Union " +
                        "SELECT 'Purple' as [Name]" +
                        ",1 as [Sys]" +
                        "Union " +
                        "SELECT 'Orange' as [Name]" +
                        ",1 as [Sys]" +
                        ") as [x] " +
                        "WHERE ([Name] = ? OR " + ALL_LISTS_FLAG + " = 1) AND ([Sys] = ? OR " + ALL_LISTS_FLAG + " = 1) " +
                        ") as [xx] " +
                        "LEFT JOIN " +
                        " (" +

                        "Select [Name] " +
                        ",[Sys] " +
                        ",COUNT([Category]) as [Total] " +
                        ",SUM((CASE WHEN [Category] = 'Empty' THEN 1 else 0 END)) as [Empty] " +
                        ",SUM((CASE WHEN [Category] = 'Grey' THEN 1 else 0 END)) as [Grey] " +
                        ",SUM((CASE WHEN [Category] = 'Red' THEN 1 else 0 END)) as [Red] " +
                        ",SUM((CASE WHEN [Category] = 'Yellow' THEN 1 else 0 END)) as [Yellow] " +
                        ",SUM((CASE WHEN [Category] = 'Green' THEN 1 else 0 END)) as [Green] " +
                        " FROM (" +

//                /* Assign each tweet a color based on the percentages of word color scores for kanjis in the tweet */
                        "Select [Name] " +
                        ",[Sys] " +
                        ",(CASE WHEN [Total] = 0 THEN 'Empty' " +
                        " WHEN CAST(ifnull([Grey],0)  as float)/[Total] > " + colorThresholds.getTweetGreyThreshold() + " THEN 'Grey' " +
                        " WHEN CAST(ifnull([Green],0)  as float)/[Total] >= " + colorThresholds.getTweetGreenthreshold() + " THEN 'Green' " +
                        " WHEN  CAST(ifnull([Red],0)  as float)/[Total] >= " + colorThresholds.getTweetRedthreshold() + " THEN 'Red' " +
                        " WHEN CAST(ifnull([Yellow],0)  as float)/[Total] >= " + colorThresholds.getTweetYellowthreshold() +" THEN 'Yellow' " +
                        " WHEN [Grey] > [Green] and [Grey] > [Red] and [Grey] > [Yellow] THEN 'Grey' " +
                        " WHEN [Green] > [Grey] and [Green] > [Red] and [Green] > [Yellow] THEN 'Green' " +
                        " WHEN [Red] > [Green] and [Red] > [Grey] and [Red] > [Yellow] THEN 'Red' " +
                        " WHEN [Yellow] > [Green] and [Yellow] > [Red] and [Yellow] > [Grey] THEN 'Yellow' " +
                        " ELSE 'Grey' END) as [Category] " +
                        " FROM ( " +

                /* Now to pull together ListName, Tweet and the totals (by color) of the kanji in those tweets */
                        "SELECT  ListsTweetsAndAllKanjis.[Name]" +
                        ",ListsTweetsAndAllKanjis.[Sys]" +
                        ",ListsTweetsAndAllKanjis.[Tweet_id] "+

                        ",SUM([Grey]) + SUM([Red]) + SUM([Yellow]) + SUM([Green]) as [Total] " +
                        ",SUM([Grey]) as [Grey]" +
                        ",SUM([Red]) as [Red]" +
                        ",SUM([Yellow]) as [Yellow]" +
                        ",SUM([Green]) as [Green] " +
                        "FROM (" +

                                    /* Now we have a big collection of list metadata (tweetlists), and all the kanji scores and colors for
                                     each kanji (kanjilists) */
                        " Select TweetLists.[Name] " +
                        " ,TweetLists.[Sys] " +
                        ", TweetLists.[Tweet_id] " +

                        ",(CASE WHEN [Total] is not NULL AND [Total] < " + colorThresholds.getGreyThreshold() + " THEN 1 ELSE 0 END) as [Grey] " +
                        ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] < " + colorThresholds.getRedThreshold() + "  THEN 1  ELSE 0 END) as [Red] " +
                        ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and ([Percent] >= " + colorThresholds.getRedThreshold() + "  and [Percent] <  " + colorThresholds.getYellowThreshold() + ") THEN 1  ELSE 0 END) as [Yellow] " +
                        ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] >= " + colorThresholds.getYellowThreshold() + " THEN 1 ELSE 0 END) as [Green] " +

                        "FROM " +
                        "(" +

                        /* Get A list of each saved tweet and the number of kanji in those tweets */
                        "SELECT  DISTINCT [Name]" +
                        ",[Sys]" +
                        ",[UserID] " +
                        ",[_id] as [Tweet_id]" +
                        "FROM " + TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                        "WHERE ([Name] = ? OR " + ALL_LISTS_FLAG + " = 1) AND ([Sys] = ? OR " + ALL_LISTS_FLAG + " = 1) " +
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
                        " SELECT Tweet_id" +
                        ",Edict_id " +
                        "From " + TABLE_SAVED_TWEET_KANJI  + " " +
                        ") as a " +
                        "LEFT JOIN " +
                        " (" +
                        "SELECT [_id] as [Edict_id]" +
                        ",sum([Correct]) as [Correct]" +
                        ",sum([Total]) as [Total] FROM [JScoreboard] " +
                        "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + TABLE_SAVED_TWEET_KANJI  + ")" +
                        " GROUP BY [_id]" +
                        ") as b " +
                        "ON a.[Edict_id] = b.[Edict_id] " +



                        " ) as TweetKanji " +
                        "On TweetLists.Tweet_id = TweetKanji.Tweet_id " +

                        ") as [ListsTweetsAndAllKanjis] " +
                        "GROUP BY [Name],[Sys],[Tweet_id]" +
                        ") as [ListandTweets]  " +
                        ") as [Lists] " +
                        "GROUP BY [Name],[Sys]" +

                        ") as yy "  +
                        "ON xx.[Name] = yy.[Name] and cast(xx.[Sys] as INTEGER)  = cast(yy.[Sys] as INTEGER)  " +

                        "Order by xx.[Sys] Desc,xx.[Name]"
                ,new String[]{name,String.valueOf(sys),name,String.valueOf(sys)});
    }


    public Cursor getMyListColorBlockCursor(ColorThresholds colorThresholds, MyListEntry myListEntry) {
        int ALL_LISTS_FLAG = 0;
        int sys = -1;
        String name = "";
        if(myListEntry == null) {
            ALL_LISTS_FLAG = 1;
        } else {
            sys = myListEntry.getListsSys();
            name = myListEntry.getListName();
        }

        return  sInstance.getWritableDatabase().rawQuery("SELECT xx.[Name]" +
                ",xx.[Sys]" +
                ",ifnull(yy.[Total],0) as [Total]" +
                ",ifnull(yy.[Grey],0) as [Grey]" +
                ",ifnull(yy.[Red],0) as [Red]" +
                ",ifnull(yy.[Yellow],0) as [Yellow]" +
                ",ifnull(yy.[Green],0) as [Green] " +
                "" +
                "FROM (" +
                "Select DISTINCT [Name],[Sys] " +
                "FROM (" +
                "SELECT [Name]" +
                ",0 as [Sys] " +
                "From " +  TABLE_FAVORITES_LISTS_TWEETS + " " +
                "UNION " +
                "SELECT 'Blue' as [Name]" +
                ", 1 as [Sys] " +
                "Union " +
                "SELECT 'Red' as [Name]" +
                ",1 as [Sys] " +
                "Union " +
                "SELECT 'Green' as [Name]" +
                ",1 as [Sys] " +
                "Union " +
                "SELECT 'Yellow' as [Name]" +
                ",1 as [Sys]" +
                "Union " +
                "SELECT 'Purple' as [Name]" +
                ",1 as [Sys]" +
                "Union " +
                "SELECT 'Orange' as [Name]" +
                ",1 as [Sys]" +
                ") as [x] " +
                "WHERE ([Name] = ? OR " + ALL_LISTS_FLAG + " = 1) AND ([Sys] = ? OR " + ALL_LISTS_FLAG + " = 1) " +
                ") as [xx] " +
                "LEFT JOIN (" +
                "SELECT  [Name]" +
                ",[Sys]" +
                ",SUM([Grey]) + SUM([Red]) + SUM([Yellow]) + SUM([Green]) as [Total]" +
                ",SUM([Grey]) as [Grey]" +
                ",SUM([Red]) as [Red]" +
                ",SUM([Yellow]) as [Yellow]" +
                ",SUM([Green]) as [Green] " +
                "FROM (" +
                "SELECT [Name]" +
                ",[Sys]" +
                ",[_id] " +
                ",(CASE WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 1 ELSE 0 END) as [Grey] " +
                ",(CASE WHEN [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] < " + colorThresholds.getRedThreshold() + "  THEN 1  ELSE 0 END) as [Red] " +
                ",(CASE WHEN [Total] >= " + colorThresholds.getGreyThreshold() + " and ([Percent] >= " + colorThresholds.getRedThreshold() + "  and [Percent] <  " + colorThresholds.getYellowThreshold() + ") THEN 1  ELSE 0 END) as [Yellow] " +
                ",(CASE WHEN [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] >= " + colorThresholds.getYellowThreshold() + " THEN 1 ELSE 0 END) as [Green] " +
                "FROM  (" +
                "SELECT a.[Name]" +
                ",a.[Sys]" +
                ",a.[_id]" +
                ",ifnull(b.[Total],0) as [Total] " +
                ",ifnull(b.[Correct],0)  as [Correct]" +
                ",CAST(ifnull(b.[Correct],0)  as float)/b.[Total] as [Percent] " +
                "FROM (" +
                "SELECT  DISTINCT [Name]" +
                ",[Sys]" +
                ",[_id] " +
                "FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
                "WHERE ([Name] = ? OR " + ALL_LISTS_FLAG + " = 1) AND ([Sys] = ? OR " + ALL_LISTS_FLAG + " = 1) " +
                ") as a " +
                "LEFT JOIN  (" +
                "SELECT [_id]" +
                ",sum([Correct]) as [Correct]" +
                ",sum([Total]) as [Total] FROM [JScoreboard] " +
                "where [_id] in (SELECT DISTINCT [_id] FROM " + TABLE_FAVORITES_LIST_ENTRIES + ")" +
                " GROUP BY [_id]" +
                ") as b " +
                "ON a.[_id] = b.[_id]) " +
                " as x) as y " +
                "GROUP BY [Name],[Sys]" +
                ") as yy  " +
                "ON xx.[Name] = yy.[Name] and xx.[sys] = yy.[sys]  " +
                "Order by xx.[Sys] Desc,xx.[Name]",new String[]{name,String.valueOf(sys),name,String.valueOf(sys)});

    }

//f
//
//    public ArrayList<WordEntry> fillMultipleChoiceKeyPool(Context context, int tabNumber, MyListEntry listEntry, String quizType, String colorString) {
//        final Double sliderUpperBound = .50;
//        final Double sliderLowerBound = .025;
//        final int sliderCountMax = 30;
//
//        SQLiteDatabase db = sInstance.getWritableDatabase();
//        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(context);
//        ColorThresholds colorThresholds = sharedPrefManager.getColorThresholds();
//        ArrayList<WordEntry> wordEntries = new ArrayList<>();
//
//
//        try {
//
//            Cursor c;
//            //User is in mylist WORD tab
//            if(tabNumber == 2) {
//                if(BuildConfig.DEBUG) {Log.d(TAG,"we're in the mylists bit");}
//                if(quizType.equals("Kanji to Kana") || quizType.equals("Kana to Kanji")) {
//                    c = db.rawQuery("SELECT a.[_id]" +
//                            ",a.[Percent]" +
//                            ",a.[Total] " +
//                            "FROM " +
//                            "(" +
//                            "SELECT [_id]" +
//                            ",(CASE WHEN [Color] = 'Grey' THEN .4 ELSE [Percent] END) as [Percent]" +
//                            ",[Total] " +
//                            "FROM " +
//                            "(" +
//                            "SELECT [_id]" +
////                                ",(CASE WHEN [Total] < " + colorThresholds.getGreyThreshold() +  " THEN 1 WHEN [Percent] < " +  colorThresholds.getRedThreshold()  + "  THEN 2 WHEN ([Percent] >= "  + colorThresholds.getRedThreshold() + " and [Percent] <  " + colorThresholds.getYellowThreshold() + ") THEN 3 WHEN [Percent]>= " + colorThresholds.getYellowThreshold()  + " THEN 4 END) as [Color]" +
//                            " ,(CASE WHEN [Total] is NULL THEN 'Grey' " +
//                            "WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 'Grey' " +
//                            "WHEN CAST(ifnull([Correct],0)  as float)/[Total] < " + colorThresholds.getRedThreshold() + "  THEN 'Red' " +
//                            "WHEN CAST(ifnull([Correct],0)  as float)/[Total] <  " + colorThresholds.getYellowThreshold() + " THEN 'Yellow' " +
//                            "ELSE 'Green' END) as [Color] " +
//
//                            ",[Percent]" +
//                            ",[Total]  " +
//                            "FROM  " +
//                            "( " +
//                            "SELECT x.[_id]" +
//                            ",ifnull([Total],0) as [Total] " +
//                            ",ifnull([Correct],0)  as [Correct]" +
//                            ",(CASE WHEN ifnull([Total],0) = 0 THEN 0 ELSE CAST(ifnull([Correct],0)  as float)/[Total] END) as [Percent]  " +
//                            "FROM  " +
//                            "(" +
//                            "SELECT [_id] " +
//                            "FROM [JFavorites] " +
//                            "WHERE ([Sys] = "  + listEntry.getListsSys() +  " and [Name] = '" + listEntry.getListName()  + "')" +
//                            ") x " +
//                            "LEFT JOIN " +
//                            "(" +
//                            "SELECT [_id]" +
//                            ",sum([Correct]) as [Correct]" +
//                            ",sum([Total]) as [Total]  " +
//                            "FROM [JScoreboard]  " +
//                            "GROUP BY [_id]" +
//                            ") y " +
//                            "ON x.[_id] = y.[_id]" +
//                            ")" +
//                            ") " +
//                            "WHERE [Color] in (" +  colorString + ") " +
//                            "ORDER BY RANDOM()" +
//                            ") as a " +
//                            "LEFT JOIN " +
//                            "(" +
//                            "SELECT DISTINCT [_id]" +
//                            ",[Furigana] " +
//                            ",[Definition] " +
//                            ",[Furigana] " +
//
//                            "FROM [Edict] " +
//                            "WHERE [_id] in (SELECT [_id] FROM [JFavorites] WHERE ([Sys] = "  + listEntry.getListsSys() +  " and [Name] = '" + listEntry.getListName()  + "'))" +
//                            ") as b " +
//                            "ON a.[_id]  = b.[_id]  ",null);
//                }
//
//            }
////        else {
////            //User is in mylist TWEET tab
////            if(BuildConfig.DEBUG) {Log.d(TAG,"we're in the mylists bit");}
////            if(quizType.equals("Kanji to Kana") || quizType.equals("Kana to Kanji")) {
////                c = db.rawQuery("SELECT a.[_id],a.[Percent],a.[Total] FROM (SELECT [_id],(CASE WHEN [Color] = 'Grey' THEN .4 ELSE [Percent] END) as [Percent],[Total] FROM (SELECT [_id],(CASE WHEN [Total] < " + colorThresholds.getGreyThreshold() +  " THEN 1 WHEN [Percent] < " +  colorThresholds.getRedThreshold()  + "  THEN 2 WHEN ([Percent] >= "  + colorThresholds.getRedThreshold() + " and [Percent] <  " + colorThresholds.getYellowThreshold() + ") THEN 3 WHEN [Percent]>= " + colorThresholds.getYellowThreshold()  + " THEN 4 END) as [Color],[Percent],[Total]  FROM  ( SELECT x.[_id],ifnull([Total],0) as [Total] ,ifnull([Correct],0)  as [Correct],(CASE WHEN ifnull([Total],0) = 0 THEN 0 ELSE CAST(ifnull([Correct],0)  as float)/[Total] END) as [Percent]  FROM  (SELECT [_id] FROM [JFavorites] WHERE ([Sys] = "  + listEntry.getListsSys() +  " and [Name] = '" + listEntry.getListName()  + "')) x LEFT JOIN (SELECT [_id],sum([Correct]) as [Correct],sum([Total]) as [Total]  FROM [JScoreboard]  GROUP BY [_id]) y ON x.[_id] = y.[_id])) WHERE [Color] in (" +  colorString + ") ORDER BY RANDOM()) as a LEFT JOIN (SELECT DISTINCT [_id],[Furigana] FROM [Edict] WHERE [_id] in (SELECT [_id] FROM [JFavorites] WHERE ([Sys] = "  + listEntry.getListsSys() +  " and [Name] = '" + listEntry.getListName()  + "'))) as b ON a.[_id]  = b.[_id]  ",null);
////            } else {
////                c = db.rawQuery("SELECT [_id],(CASE WHEN [Color] = 'Grey' THEN .4 ELSE [Percent] END) as [Percent],[Total] FROM (SELECT [_id],(CASE WHEN [Total] < " + colorThresholds.getGreyThreshold() +  " THEN 1 WHEN [Percent] < " +  colorThresholds.getRedThreshold()  + "  THEN 2 WHEN ([Percent] >= "  + colorThresholds.getRedThreshold() + " and [Percent] <  " + colorThresholds.getYellowThreshold() + ") THEN 3 WHEN [Percent]>= " + colorThresholds.getYellowThreshold()  + " THEN 4 END) as [Color],[Percent],[Total]  FROM  ( SELECT x.[_id],ifnull([Total],0) as [Total] ,ifnull([Correct],0)  as [Correct],(CASE WHEN ifnull([Total],0) = 0 THEN 0 ELSE CAST(ifnull([Correct],0)  as float)/[Total] END) as [Percent]  FROM  (SELECT [_id] FROM [JFavorites] WHERE ([Sys] = "  + listEntry.getListsSys() +  " and [Name] = '" + listEntry.getListName()  + "')) x LEFT JOIN (SELECT [_id],sum([Correct]) as [Correct],sum([Total]) as [Total]  FROM [JScoreboard]  GROUP BY [_id]) y ON x.[_id] = y.[_id])) WHERE [Color] in (" +  colorString + ") ORDER BY RANDOM()",null);
////            }
////        }
//
//            if(BuildConfig.DEBUG) {Log.d(TAG,"c.count: " + c.getCount());}
//
//            if(c.getCount()>0) {
//                c.moveToFirst();
//                int i=0;
//                while (!c.isAfterLast()) {
//                    /** Add the PKey (_id) to the keypool array (we'll pull it out by index)... */
//                    keyPool.add(c.getInt(0)); //[Key]
//                    if(BuildConfig.DEBUG) {Log.d(TAG,"(wordweight) ****** START ID: " + c.getString(0) );}
//                    /** Add index (i) and weight to the hashmap */
//                    double percentage =c.getDouble(1);
//                    double total = c.getDouble(2);
//
//                    /* The slider multiplier is what affects how rapidly a word diverges from the natural weight of .25.
//                     * The higher the multiplier, the faster it will diverge with an increased count.*/
//                    double countMultiplier = (double)total/(double)sliderCountMax*(percentage-(double)sliderUpperBound)*(double)sharedPrefManager.getSliderMultiplier();
//
//                    if(total>100) {
//                        total = (double)100;
//                    }
//                    if(BuildConfig.DEBUG) {
//                        Log.d(TAG,"(wordweight) (" + c.getString(0) + ") Percentage: " + c.getDouble(1) + ", Total: " + c.getString(2));
//                        Log.d(TAG,"(wordweight) count multiplier: " + countMultiplier);
//                    }
//                    double a = ((double)sliderUpperBound/(double)2)-(double)sliderUpperBound*(countMultiplier) ;
//                    if(BuildConfig.DEBUG) {Log.d(TAG,"(wordweight) (" + c.getString(0) + ") a: " + a);}
//
//
//                    double b = sliderLowerBound;
//                    if(a>=sliderUpperBound) {
//                        b = sliderUpperBound;
//                    } else if(a>=sliderLowerBound) {
//                        b = a;
//                    }
//                    if(BuildConfig.DEBUG) {
//                        Log.d(TAG, "(wordweight) FINAL b value = " + b);
//                        Log.d(TAG, "(wordweight) (" + c.getString(0) + ") final weight: " + b);
//                    }
//                    keypoolhashmap.put(i, b); // put _id and weight value into map
//                    i++;
//                    c.moveToNext();
//                }
//            }
//            c.close();
//
//
//        } catch (SQLiteException e) {
//            Log.e(TAG,"fillMultipleChoiceKeyPool sqlite problem: " + e);
//        } catch (Exception e) {
//            Log.e(TAG,"fillMultipleChoiceKeyPool other exception... " + e);
//        } finally {
//            db.close();
//        }
//
//
//
//        return wordEntries;
//
//    }


    public Cursor getPossibleMultipleChoiceMatch(String quizType
            , WordEntry correctWordEntry
            , String alreadyaddedkanji
            , String kanjiToBreak
            , String possibleKanjiPart) {
        if (quizType.equals("Kanji to Kana")) {
            return sInstance.getWritableDatabase().rawQuery("SELECT  x.[_id]" +
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

//                    "SELECT DISTINCT [_id] " +
//                    "FROM Edict " +
//                    "Where  [Common] = 1 " +
//                    "and [Furigana] is not null " +
//                    "AND [_ID] <>  " + correctWordEntry.getId() + " " +
//                    "AND [_ID] not in (" + alreadyaddedkanji + ") " +
//                    "and [Furigana] <> \"" + correctWordEntry.getFurigana()  + "\"  " +
//                    "and Length([Furigana]) between "+ (kanjiToBreak.length() ) + " and "+ (kanjiToBreak.length() + 1) + " " +
//                    "and [Furigana] like \"%" + possibleKanjiPart + "%\"   LIMIT 5",null);
        } else {
            return sInstance.getWritableDatabase().rawQuery("SELECT  x.[_id]" +
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


//                    "SELECT DISTINCT [_id]  " +
//                    "FROM Edict " +
//                    "Where  [Common] = 1 " +
//                    "and [_ID] <> " + correctWordEntry.getId() + " " +
//                    "AND [_ID] not in (" + alreadyaddedkanji + ") " +
//                    "and Length([Kanji]) between "+ (kanjiToBreak.length() ) + " and " + (kanjiToBreak.length() + 1) + " " +
//                    "and [Kanji] <> \"" +  correctWordEntry.getKanji()  + "\" " +
//                    "and [Kanji] like \"%" + possibleKanjiPart + "%\"   LIMIT 5",null);
        }

    }

    public Cursor getRandomKanji(String keysToExclude, int limit) {
        return sInstance.getWritableDatabase().rawQuery("SELECT [_id]" +
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
                "where [_id]  not in (" +  keysToExclude + ") " +
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



//    public ArrayList<Tweet> getSavedTweetsThatContainWords(String kanjiIds, ColorThresholds colorThresholds) {
//        ArrayList<Tweet> savedTweets = new ArrayList<>();
//        SQLiteDatabase db = sInstance.getReadableDatabase();
//        try {
//            Cursor c = db.rawQuery("SELECT TweetIds.[Tweet_id]" +
//                            ",[MetaData].ScreenName " +
//                            ",[MetaData].UserName " +
//                            ",[MetaData].[UserId] " +
//                            ",[MetaData].[Text] " +
//                            ",[MetaData].[Date]" +
//                            ",TweetKanji.Edict_id " +
//                            ",TweetKanji.Kanji " +
//                            ",(CASE WHEN [TweetKanji].[Furigana] is null  then '' else [TweetKanji].[Furigana] end) as [Furigana] " +
//                            ",TweetKanji.Definition " +
//                            ",TweetKanji.Total " +
//                            ",TweetKanji.Correct " +
//                            ",TweetKanji.Color " +
//                            ",TweetKanji.StartIndex " +
//                            ",TweetKanji.EndIndex " +
//
//                            "FROM  " +
//
//                    " ( " +
//                    /* Get a list of tweets that contain the kanji we are interested in */
//                        "SELECT DISTINCT [Tweet_id]" +
//                        " FROM " + TABLE_SAVED_TWEET_KANJI  + " " +
//                        "WHERE [Edict_id] in (" + kanjiIds + ") " +
//                    ") as [TweetIds] " +
//
//                            " LEFT JOIN " +
//
//                            /* Attach metadata about tweet and user */
//                            "( " +
//                            "SELECT DISTINCT [TweetData].[Tweet_id]" +
//                            ",[TweetData].[Text]" +
//                            ",[TweetData].[UserId] " +
//                            ",[TweetData].[Date] " +
//                            ",[UserData].[ScreenName] " +
//                            ",[UserData].[UserName] " +
//                            " FROM " +
//                            " ( " +
//                            "SELECT  DISTINCT [Tweet_id]" +
//                            ",[Text]" +
//                            ",[UserId] " +
//                            ",[CreatedAt]  as [Date] " +
//                            " FROM "+ TABLE_SAVED_TWEETS + " " +
//                            ") as [TweetData] " +
//
//
//                            "LEFT JOIN " +
//                            " (" +
//                            "SELECT DISTINCT [UserId] " +
//                            ", [ScreenName] " +
//                            ", [UserName] " +
//                            " FROM " + TABLE_USERS +" " +
//                            ") as [UserData] " +
//                            "On TweetData.UserId = UserData.UserId " +
//                            ") as [MetaData] " +
//                            " On TweetIds.Tweet_id = MetaData.Tweet_id " +
//
//
//                    "LEFT JOIN " +
//                           /* Attach all the rows for individual kanji in each saved tweet */
//                    " ( " +
//                                /* Get a list of  kanji ids and their word scores for each tweet */
//                            "SELECT a.[Tweet_id]" +
//                            ",a.[Edict_id]" +
//                            ",a.[StartIndex]" +
//                            ",a.[EndIndex]" +
//
//                            ",(CASE WHEN [Total] is NULL THEN 'Grey' " +
//                            "WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 'Grey' " +
//                            "WHEN CAST(ifnull(b.[Correct],0)  as float)/b.[Total] < " + colorThresholds.getRedThreshold() + "  THEN 'Red' " +
//                            "WHEN CAST(ifnull(b.[Correct],0)  as float)/b.[Total] <  " + colorThresholds.getYellowThreshold() + " THEN 'Yellow' " +
//                            "ELSE 'Green' END) as [Color]" +
//                            ",c.Furigana as [Furigana]" +
//                            ",c.Definition as [Definition] " +
//                            ",c.Kanji as [Kanji] " +
//                            ",b.[Total] as [Total]" +
//                            ",b.[Correct] as [Correct] " +
////                            ",CAST(ifnull(b.[Correct],0)  as float)/b.[Total] as [Percent] " +
//
//                            "FROM " +
//                            "( " +
//                            " SELECT Tweet_id" +
//                            ",Edict_id " +
//                            ",[StartIndex]" +
//                            ",[EndIndex]" +
//                            "From " + TABLE_SAVED_TWEET_KANJI  + " " +
//                            " WHERE [Edict_id] is not NULL and StartIndex is not NULL and EndIndex is not NULL and EndIndex > StartIndex " +
//                                    "and Tweet_id in (" +
//                                /* Get a list of tweets that contain the kanji we are interested in */
//                                    "SELECT DISTINCT [Tweet_id]" +
//                                    " FROM " + TABLE_SAVED_TWEET_KANJI  + " " +
//                                    "WHERE [Edict_id] in (" + kanjiIds + ") " +
//                                    ") " +
//
//
//                            ") as a " +
//                            "LEFT JOIN " +
//                            " (" +
//                            "SELECT [_id] as [Edict_id]" +
//                            ",sum([Correct]) as [Correct]" +
//
//                            ",sum([Total]) as [Total] FROM [JScoreboard] " +
//                            "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + TABLE_SAVED_TWEET_KANJI  + ")" +
//                            " GROUP BY [_id]" +
//                            ") as b " +
//                            "ON a.[Edict_id] = b.[Edict_id] " +
//                            "LEFT JOIN " +
//                            " (" +
//                            "SELECT DISTINCT [_id] as [Edict_id]" +
//                            ",[Kanji]" +
//                            ",[Furigana]" +
//                            ",[Definition]" +
//                            "FROM [Edict] " +
//                            ") as c " +
//                            "ON a.[Edict_id] = c.[Edict_id] " +
//
//                    " ) as TweetKanji " +
//                        "On TweetIds.Tweet_id = TweetKanji.Tweet_id " +
//
//
//
//                            "Order by date(MetaData.Date) Desc,TweetIds.[Tweet_id] asc,TweetKanji.StartIndex asc"
//                    , null);
//
//
//
//            /* The query pulls a list of tweetdata paired with each parsed-kanji in the tweet, resulting in
//            * multiple duplicate lines of tweetdata. So the cursor ony adds tweet data once, when a new tweetid is found. Meanwhile
//            * the kanji data for each row is added to a TweetKanjiColor object, which is then added to the kanji and the kanji
//            * added to the final "savedTweets" list when a new tweetid appears (or the cursor finishes) */
//
//
//            if(c.getCount()>0) {
//                c.moveToFirst();
//                String currentTweetId = c.getString(0);
//                Tweet tweet = new Tweet();
//                while (!c.isAfterLast())
//                {
//
//                    if(c.isFirst()) {
//                        tweet.setIdString(c.getString(0));
//                        tweet.setCreatedAt(c.getString(5));
//                        tweet.getUser().setUserId(c.getString(3));
//                        tweet.setText(c.getString(4));
//                        tweet.getUser().setScreen_name(c.getString(1));
//                        tweet.getUser().setName(c.getString(2));
//
//                    }
//
//
//                    WordEntry wordEntry = new WordEntry(c.getInt(6)
//                            ,c.getString(7)
//                            ,c.getString(8)
//                            ,c.getString(9)
//                            ,c.getInt(10)
//                            ,c.getInt(11)
//                            ,c.getString(12)
//                            ,c.getInt(13)
//                            ,c.getInt(14));
//
//                    //Designate spinner kanji if they exist
//                    if(kanjiIds.contains(c.getString(6))) {
//                        wordEntry.setSpinner(true);
//                    }
//                    tweet.addWordEntry(wordEntry);
//
//
//                    //FLush old tweet
//                    if(!currentTweetId.equals(c.getString(0))){
//                        savedTweets.add(new Tweet(tweet));
//                        currentTweetId = c.getString(0);
//                        tweet = new Tweet();
//
//                        tweet.setIdString(c.getString(0));
//                        tweet.setCreatedAt(c.getString(5));
//                        tweet.getUser().setUserId(c.getString(3));
//                        tweet.setText(c.getString(4));
//                        tweet.getUser().setScreen_name(c.getString(1));
//                        tweet.getUser().setName(c.getString(2));
//                    }
//
//                    if(c.isLast()) {
//                        savedTweets.add(new Tweet(tweet));
//                    }
//                    c.moveToNext();
//                }
//
//
//            } else {if(debug) {Log.d(TAG,"c.getcount was 0!!");}}
//            c.close();
//
//        } catch (SQLiteException e){
//            Log.e(TAG,"getTweetsForSavedTweetsList Sqlite exception: " + e);
//        } catch (Exception e) {
//            Log.e(TAG,"getTweetsForSavedTweetsList generic exception: " + e);
//        } finally {
//            db.close();
//        }
//        return savedTweets;
//    }

//
//
//    public ArrayList<Tweet> getFillintheBlanksTweetsForAWordList(MyListEntry myListEntry
//            , ColorThresholds colorThresholds
//            , String colorString
//            , @Nullable Integer resultLimit) {
//
//        long startTime = System.nanoTime();
//        ArrayList<Integer> possibleSpinners = getIdsForWordList(myListEntry);
//        long endTime = System.nanoTime();
//        Log.e(TAG,"ELLAPSED POSSIBLE SPINNERS: " + (endTime - startTime) / 1000000000.0);
//
//        String limit;
//        if(resultLimit == null) {
//            limit = "";
//        } else {
//            limit = "LIMIT " + String.valueOf(resultLimit);
//        }
//
//        ArrayList<Tweet> savedTweets = new ArrayList<>();
//        SQLiteDatabase db = sInstance.getReadableDatabase();
//        try {
//
//
//
//
//
//
//
//            Cursor c = db.rawQuery("SELECT TweetIds.[Tweet_id]" +
//                            ",[MetaData].ScreenName " +
//                            ",[MetaData].UserName " +
//                            ",[MetaData].[UserId] " +
//                            ",[MetaData].[Text] " +
//                            ",[MetaData].[Date]" +
//                            ",TweetKanji.Edict_id " +
//                            ",TweetKanji.Kanji " +
//                            ",(CASE WHEN [TweetKanji].[Furigana] is null  then '' else [TweetKanji].[Furigana] end) as [Furigana] " +
//                            ",TweetKanji.Definition " +
//                            ",TweetKanji.Total " +
//                            ",TweetKanji.Correct " +
//                            ",TweetKanji.Color " +
//                            ",TweetKanji.StartIndex " +
//                            ",TweetKanji.EndIndex " +
//
//                            "FROM  " +
//
//                            " ( " +
//                     /* Get a list of tweets that contain the kanji we are interested in */
//
//
//                            "Select DISTINCT Tweet_id " +
//                            " FROM (" +
//
////                /* Assign each tweet a color based on the percentages of word color scores for kanjis in the tweet */
//                            "Select Tweet_id " +
//                            ",(CASE WHEN [Total] = 0 THEN 'Empty' " +
//                            " WHEN CAST(ifnull([Grey],0)  as float)/[Total] > " + colorThresholds.getTweetGreyThreshold() + " THEN 'Grey' " +
//                            " WHEN CAST(ifnull([Green],0)  as float)/[Total] >= " + colorThresholds.getTweetGreenthreshold() + " THEN 'Green' " +
//                            " WHEN  CAST(ifnull([Red],0)  as float)/[Total] >= " + colorThresholds.getTweetRedthreshold() + " THEN 'Red' " +
//                            " WHEN CAST(ifnull([Yellow],0)  as float)/[Total] >= " + colorThresholds.getTweetYellowthreshold() +" THEN 'Yellow' " +
//                            " WHEN [Grey] > [Green] and [Grey] > [Red] and [Grey] > [Yellow] THEN 'Grey' " +
//                            " WHEN [Green] > [Grey] and [Green] > [Red] and [Green] > [Yellow] THEN 'Green' " +
//                            " WHEN [Red] > [Green] and [Red] > [Grey] and [Red] > [Yellow] THEN 'Red' " +
//                            " WHEN [Yellow] > [Green] and [Yellow] > [Red] and [Yellow] > [Grey] THEN 'Yellow' " +
//                            " ELSE 'Grey' END) as [Category] " +
//                            " FROM ( " +
//
//                /* Now to pull together ListName, Tweet and the totals (by color) of the kanji in those tweets */
//                            "SELECT ListsTweetsAndAllKanjis.[Tweet_id] "+
//
//                            ",SUM([Grey]) + SUM([Red]) + SUM([Yellow]) + SUM([Green]) as [Total] " +
//                            ",SUM([Grey]) as [Grey]" +
//                            ",SUM([Red]) as [Red]" +
//                            ",SUM([Yellow]) as [Yellow]" +
//                            ",SUM([Green]) as [Green] " +
//                            "FROM (" +
//
//                                    /* Now we have a big collection of list metadata (tweetlists), and all the kanji scores and colors for
//                                     each kanji (kanjilists) */
//                            " Select TweetLists.[Tweet_id] " +
//
//                            ",(CASE WHEN [Total] is not NULL AND [Total] < " + colorThresholds.getGreyThreshold() + " THEN 1 ELSE 0 END) as [Grey] " +
//                            ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] < " + colorThresholds.getRedThreshold() + "  THEN 1  ELSE 0 END) as [Red] " +
//                            ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and ([Percent] >= " + colorThresholds.getRedThreshold() + "  and [Percent] <  " + colorThresholds.getYellowThreshold() + ") THEN 1  ELSE 0 END) as [Yellow] " +
//                            ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] >= " + colorThresholds.getYellowThreshold() + " THEN 1 ELSE 0 END) as [Green] " +
//
//                            "FROM " +
//                            "(" +
//                            /* Select all tweets that contain words from the MyList */
//                            "SELECT DISTINCT [Tweet_id]" +
//                            " FROM " + TABLE_SAVED_TWEET_KANJI  + " " +
//                            "WHERE [Edict_id] in (" +
//                                        "SELECT DISTINCT [_id]" +
//                                        " FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
//                                        " WHERE [Name] = ?  and [Sys] = ? " +
//                                                ") " +
//                            ") as TweetLists " +
//                            " LEFT JOIN " +
//                            " ( " +
//
//                            /* Get a list of  kanji ids and their word scores for each tweet */
//                            "SELECT a.[Tweet_id]" +
//                            ",a.[Edict_id]" +
//                            ",ifnull(b.[Total],0) as [Total] " +
//                            ",ifnull(b.[Correct],0)  as [Correct]" +
//                            ",CAST(ifnull(b.[Correct],0)  as float)/b.[Total] as [Percent] " +
//                            "FROM " +
//                            "( " +
//                            " SELECT Tweet_id" +
//                            ",Edict_id " +
//                            "From " + TABLE_SAVED_TWEET_KANJI  + " " +
//                            " WHERE Tweet_id in(" +
//                                        "SELECT DISTINCT [Tweet_id]" +
//                                        " FROM " + TABLE_SAVED_TWEET_KANJI  + " " +
//                                        "WHERE [Edict_id] in (" +
//                                        "SELECT DISTINCT [_id]" +
//                                        " FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
//                                            " WHERE [Name] = ? and  [Sys] = ?   " +
//                                        ") " +
//                                    ") " +
//                            ") as a " +
//                            "LEFT JOIN " +
//                            " (" +
//                            "SELECT [_id] as [Edict_id]" +
//                            ",sum([Correct]) as [Correct]" +
//                            ",sum([Total]) as [Total] FROM [JScoreboard] " +
//                            "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + TABLE_SAVED_TWEET_KANJI  + ")" +
//                            " GROUP BY [_id]" +
//                            ") as b " +
//                            "ON a.[Edict_id] = b.[Edict_id] " +
//
//                            " ) as TweetKanji " +
//                            "On TweetLists.Tweet_id = TweetKanji.Tweet_id " +
//
//                            ") as [ListsTweetsAndAllKanjis] " +
//                            "GROUP BY [Tweet_id]" +
//                            ") as [ListandTweets]  " +
//                            ") as [Lists] " +
//                            "WHERE [Category] in (" + colorString + ") " +
//                            "ORDER BY RANDOM() " + limit + " " +
//
//
//
//
//
//
//
//
//
//
//
//
//
//                            ") as [TweetIds] " +
//
//                            " LEFT JOIN " +
//
//                            /* Attach metadata about tweet and user */
//                            "( " +
//                            "SELECT DISTINCT [TweetData].[Tweet_id]" +
//                            ",[TweetData].[Text]" +
//                            ",[TweetData].[UserId] " +
//                            ",[TweetData].[Date] " +
//                            ",[UserData].[ScreenName] " +
//                            ",[UserData].[UserName] " +
//                            " FROM " +
//                            " ( " +
//                            "SELECT  DISTINCT [Tweet_id]" +
//                            ",[Text]" +
//                            ",[UserId] " +
//                            ",[CreatedAt]  as [Date] " +
//                            " FROM "+ TABLE_SAVED_TWEETS + " " +
//                            ") as [TweetData] " +
//
//
//                            "LEFT JOIN " +
//                            " (" +
//                            "SELECT DISTINCT [UserId] " +
//                            ", [ScreenName] " +
//                            ", [UserName] " +
//                            " FROM " + TABLE_USERS +" " +
//                            ") as [UserData] " +
//                            "On TweetData.UserId = UserData.UserId " +
//                            ") as [MetaData] " +
//                            " On TweetIds.Tweet_id = MetaData.Tweet_id " +
//
//
//                            "LEFT JOIN " +
//                           /* Attach all the rows for individual kanji in each saved tweet */
//                            " ( " +
//                                /* Get a list of  kanji ids and their word scores for each tweet */
//                            "SELECT a.[Tweet_id]" +
//                            ",a.[Edict_id]" +
//                            ",a.[StartIndex]" +
//                            ",a.[EndIndex]" +
//
//                            ",(CASE WHEN [Total] is NULL THEN 'Grey' " +
//                            "WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 'Grey' " +
//                            "WHEN CAST(ifnull(b.[Correct],0)  as float)/b.[Total] < " + colorThresholds.getRedThreshold() + "  THEN 'Red' " +
//                            "WHEN CAST(ifnull(b.[Correct],0)  as float)/b.[Total] <  " + colorThresholds.getYellowThreshold() + " THEN 'Yellow' " +
//                            "ELSE 'Green' END) as [Color]" +
//                            ",c.Furigana as [Furigana]" +
//                            ",c.Definition as [Definition] " +
//                            ",c.Kanji as [Kanji] " +
//                            ",b.[Total] as [Total]" +
//                            ",b.[Correct] as [Correct] " +
////                            ",CAST(ifnull(b.[Correct],0)  as float)/b.[Total] as [Percent] " +
//
//                            "FROM " +
//                            "( " +
//                            " SELECT Tweet_id" +
//                            ",Edict_id " +
//                            ",[StartIndex]" +
//                            ",[EndIndex]" +
//                            "From " + TABLE_SAVED_TWEET_KANJI  + " " +
//                            " WHERE [Edict_id] is not NULL and StartIndex is not NULL and EndIndex is not NULL and EndIndex > StartIndex " +
//                            "and Tweet_id in (" +
//                                /* Get a list of tweets that contain the kanji we are interested in */
//                            "SELECT DISTINCT [Tweet_id]" +
//                            " FROM " + TABLE_SAVED_TWEET_KANJI  + " " +
//                            "WHERE [Edict_id] in (" +
//                            "SELECT DISTINCT [_id]" +
//                            " FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
//                            " WHERE [Name] = ? and  [Sys] = ?   " +
//
//                            ") " +
//                            ") " +
//
//
//                            ") as a " +
//                            "LEFT JOIN " +
//                            " (" +
//                            "SELECT [_id] as [Edict_id]" +
//                            ",sum([Correct]) as [Correct]" +
//
//                            ",sum([Total]) as [Total] FROM [JScoreboard] " +
//                            "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + TABLE_SAVED_TWEET_KANJI  + ")" +
//                            " GROUP BY [_id]" +
//                            ") as b " +
//                            "ON a.[Edict_id] = b.[Edict_id] " +
//                            "LEFT JOIN " +
//                            " (" +
//                            "SELECT DISTINCT [_id] as [Edict_id]" +
//                            ",[Kanji]" +
//                            ",[Furigana]" +
//                            ",[Definition]" +
//                            "FROM [Edict] " +
//                            ") as c " +
//                            "ON a.[Edict_id] = c.[Edict_id] " +
//
//                            " ) as TweetKanji " +
//                            "On TweetIds.Tweet_id = TweetKanji.Tweet_id " +
//
//
//                            "Order by date(MetaData.Date) Desc,TweetIds.[Tweet_id] asc,TweetKanji.StartIndex asc"
//                    ,  new String[]{myListEntry.getListName()
//                            ,String.valueOf(myListEntry.getListsSys())
//                            ,myListEntry.getListName()
//                            ,String.valueOf(myListEntry.getListsSys())
//                            ,myListEntry.getListName()
//                            ,String.valueOf(myListEntry.getListsSys())});
//
//            long startTime5 = System.nanoTime();
//            Log.d(TAG,"C COUNT : " + c.getCount());
//            long endTime5 = System.nanoTime();
//            Log.e(TAG,"ELLAPSED TIME C COUNT: " + (endTime5 - startTime5) / 1000000000.0);
//
//            /* The query pulls a list of tweetdata paired with each parsed-kanji in the tweet, resulting in
//            * multiple duplicate lines of tweetdata. So the cursor ony adds tweet data once, when a new tweetid is found. Meanwhile
//            * the kanji data for each row is added to a TweetKanjiColor object, which is then added to the kanji and the kanji
//            * added to the final "savedTweets" list when a new tweetid appears (or the cursor finishes) */
//
//
//            if(c.getCount()>0) {
//                c.moveToFirst();
//                String currentTweetId = c.getString(0);
//                Tweet tweet = new Tweet();
//                while (!c.isAfterLast())
//                {
//
//                    if(c.isFirst()) {
//                        long startTime2 = System.nanoTime();
//                        tweet.setIdString(c.getString(0));
//                        tweet.setCreatedAt(c.getString(5));
//                        tweet.getUser().setUserId(c.getString(3));
//                        tweet.setText(c.getString(4));
//                        tweet.getUser().setScreen_name(c.getString(1));
//                        tweet.getUser().setName(c.getString(2));
//                        long endTime2 = System.nanoTime();
//                        Log.e(TAG,"ELLAPSED TIME FIRST: " + (endTime2 - startTime2) / 1000000000.0);
//                    }
//
//
//                    WordEntry wordEntry = new WordEntry(c.getInt(6)
//                            ,c.getString(7)
//                            ,c.getString(8)
//                            ,c.getString(9)
//                            ,c.getInt(10)
//                            ,c.getInt(11)
//                            ,c.getString(12)
//                            ,c.getInt(13)
//                            ,c.getInt(14));
//
//                    //Designate spinner kanji if they exist
//
//                    tweet.addWordEntry(wordEntry);
//
//
//                    //FLush old tweet
//                    if(!currentTweetId.equals(c.getString(0))){
//
//
//                        long startTime1 = System.nanoTime();
//
//                        setSpinnersForTweetWithMyListWords(tweet,possibleSpinners);
//                        savedTweets.add(new Tweet(tweet));
//
//
//                        long endTime1 = System.nanoTime();
//                        Log.e(TAG,"ELLAPSED FLUSH TWEET: " + (endTime1 - startTime1) / 1000000000.0);
//
//                        currentTweetId = c.getString(0);
//                        tweet = new Tweet();
//                        tweet.setIdString(c.getString(0));
//                        tweet.setCreatedAt(c.getString(5));
//                        tweet.getUser().setUserId(c.getString(3));
//                        tweet.setText(c.getString(4));
//                        tweet.getUser().setScreen_name(c.getString(1));
//                        tweet.getUser().setName(c.getString(2));
//                    }
//
//                    if(c.isLast()) {
//                        Tweet lastTweet = new Tweet(tweet);
//                        setSpinnersForTweetWithMyListWords(lastTweet,possibleSpinners);
//                        savedTweets.add(lastTweet);
//                    }
//                    c.moveToNext();
//                }
//
//
//            } else {if(debug) {Log.d(TAG,"c.getcount was 0!!");}}
//            c.close();
//
//        } catch (SQLiteException e){
//            Log.e(TAG,"getTweetsForSavedTweetsList Sqlite exception: " + e);
//        } catch (Exception e) {
//            Log.e(TAG,"getTweetsForSavedTweetsList generic exception: " + e);
//        } finally {
//            db.close();
//        }
//        return savedTweets;
//    }

    public void setSpinnersForTweetWithMyListWords(SQLiteDatabase db
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
        Collections.shuffle(wordEntries);

        for(int i=0;i<wordEntries.size() && spinnerAddedCount<spinnerLimit;i++) {
            if(wordListEdictIds.contains(wordEntries.get(i).getId())) {
                wordEntries.get(i).setSpinner(true);

                ArrayList<String> arrayOptions = getDummySpinnerOptions(db,myListEntry,wordEntries.get(i),myListType);
                wordEntries.get(i).getFillinSentencesSpinner().setOptions(arrayOptions);

                spinnerAddedCount += 1;
            }
        }
    }

    public ArrayList<String> getDummySpinnerOptions(SQLiteDatabase db
            , MyListEntry myListEntry
            , WordEntry wordEntry
            , String mylistType) {
        ArrayList<String> dummyOptions = new ArrayList<>();
//        String favoritesTable;

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
                        "SELECT DISTINCT Tweet_id " +
                        "FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
                        " WHERE [Name] = ? and [Sys] = ? and [_id] <> ? " +
                        ") as x " +
                        "LEFT JOIN "  +
                        "( " +
                        "SELECT DISTINCT Tweet_id,Edict_id " +
                        "FROM " + TABLE_SAVED_TWEET_KANJI + " " +
                        ")  as y " +
                        " ON x.Tweet_id = y.Tweet_id " +
                    " ORDER BY RANDOM()  LIMIT 8 " +
                    ") OR [_id] in (" +
                    "SELECT DISTINCT [_id] " +
                    "FROM [XRef] " +
                    "WHERE [_id] <> ? " +
                    "ORDER BY RANDOM()  LIMIT 4" +
                    ") ORDER BY RANDOM() LIMIT 3)  " +
                    "UNION " +
                    "SELECT '" + wordEntry.getKanji() + "' as [Kanji]) " +
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
                    "FROM " +  TABLE_FAVORITES_LIST_ENTRIES + " " +
                    " WHERE [Name] = ? and [Sys] = ? and [_id] <> ? " +
                    "ORDER BY RANDOM()  LIMIT 8 " +
                    ") OR [_id] in (" +
                    "SELECT DISTINCT [_id] " +
                    "FROM [XRef] " +
                    "WHERE [_id] <> ? " +
                    "ORDER BY RANDOM()  LIMIT 4" +
                    ") ORDER BY RANDOM() LIMIT 3)  " +
                    "UNION " +
                    "SELECT '" + wordEntry.getKanji() + "' as [Kanji]) " +
                    "ORDER BY RANDOM() ",new String[]{myListEntry.getListName()
                    ,String.valueOf(myListEntry.getListsSys())
                    ,String.valueOf(wordEntry.getId())
                    ,String.valueOf(wordEntry.getId())});
        }

//            Cursor c = db.rawQuery("SELECT [Kanji] " +
//                    "FROM " +
//                    "(" +
//                    "SELECT [Kanji] " +
//                    "FROM " +
//                    "(" +
//                    "Select DISTINCT [Kanji] " +
//                    "FROM [Edict] where [_id] in (" +
//                    "SELECT DISTINCT [_id] " +
//                    "FROM " +  favoritesTable + " " +
//                    " WHERE [Name] = ? and [Sys] = ? and [_id] <> ? " +
//                    "ORDER BY RANDOM()  LIMIT 8 " +
//                    ") OR [_id] in (" +
//                    "SELECT DISTINCT [_id] " +
//                    "FROM [XRef] " +
//                    "WHERE [_id] <> ? " +
//                    "ORDER BY RANDOM()  LIMIT 4" +
//                    ") ORDER BY RANDOM() LIMIT 3)  " +
//                    "UNION " +
//                    "SELECT '" + wordEntry.getKanji() + "' as [Kanji]) " +
//                    "ORDER BY RANDOM() ",new String[]{myListEntry.getListName()
//                    ,String.valueOf(myListEntry.getListsSys())
//                    ,String.valueOf(wordEntry.getId())
//                    ,String.valueOf(wordEntry.getId())});

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
        } catch (Exception e) {
            Log.e(TAG,"getDummySpinnerOptions generic exception: " + e);
        } finally {
            db.close();
        }

        return dummyOptions;

    }

    public ArrayList<Integer> getIdsForWordList(MyListEntry myListEntry) {
        ArrayList<Integer> ids = new ArrayList<>();
        SQLiteDatabase db = sInstance.getReadableDatabase();
        try {
            Cursor c = db.rawQuery(
    "SELECT DISTINCT [_id]" +
            " FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
            " WHERE [Sys] = ? and [Name] = ? ",new String[]{myListEntry.getListName()
            ,String.valueOf(myListEntry.getListsSys())});
            if(c.getCount()>0) {
                c.moveToFirst();
                String currentTweetId = c.getString(0);
                Tweet tweet = new Tweet();
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






    public ArrayList<Tweet> getFillintheBlanksTweetsForATweetList(MyListEntry myListEntry
            , ColorThresholds colorThresholds
            , String colorString
            , @Nullable Integer resultLimit) {

        String limit;
        if(resultLimit == null) {
            limit = "";
        } else {
            limit = "LIMIT " + String.valueOf(resultLimit);
        }

        ArrayList<Tweet> savedTweets = new ArrayList<>();
        SQLiteDatabase db = sInstance.getReadableDatabase();
        try {
            Cursor c = db.rawQuery("SELECT TweetIds.[Tweet_id]" +
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

                            "FROM  " +

                            " ( " +
                     /* Get a list of tweets that contain the kanji we are interested in */







                            "Select DISTINCT Tweet_id " +
                            " FROM (" +

//                /* Assign each tweet a color based on the percentages of word color scores for kanjis in the tweet */
                            "Select Tweet_id " +
                            ",(CASE WHEN [Total] = 0 THEN 'Empty' " +
                            " WHEN CAST(ifnull([Grey],0)  as float)/[Total] > " + colorThresholds.getTweetGreyThreshold() + " THEN 'Grey' " +
                            " WHEN CAST(ifnull([Green],0)  as float)/[Total] >= " + colorThresholds.getTweetGreenthreshold() + " THEN 'Green' " +
                            " WHEN  CAST(ifnull([Red],0)  as float)/[Total] >= " + colorThresholds.getTweetRedthreshold() + " THEN 'Red' " +
                            " WHEN CAST(ifnull([Yellow],0)  as float)/[Total] >= " + colorThresholds.getTweetYellowthreshold() +" THEN 'Yellow' " +
                            " WHEN [Grey] > [Green] and [Grey] > [Red] and [Grey] > [Yellow] THEN 'Grey' " +
                            " WHEN [Green] > [Grey] and [Green] > [Red] and [Green] > [Yellow] THEN 'Green' " +
                            " WHEN [Red] > [Green] and [Red] > [Grey] and [Red] > [Yellow] THEN 'Red' " +
                            " WHEN [Yellow] > [Green] and [Yellow] > [Red] and [Yellow] > [Grey] THEN 'Yellow' " +
                            " ELSE 'Grey' END) as [Category] " +
                            " FROM ( " +

                /* Now to pull together ListName, Tweet and the totals (by color) of the kanji in those tweets */
                            "SELECT ListsTweetsAndAllKanjis.[Tweet_id] "+

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
                            "SELECT DISTINCT _id as [Tweet_id]" +
                            " FROM " + TABLE_FAVORITES_LISTS_TWEETS_ENTRIES  + " " +
                            " WHERE [Name] = ? and  [Sys] = ?   " +
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
                            " SELECT Tweet_id" +
                            ",Edict_id " +
                            "From " + TABLE_SAVED_TWEET_KANJI  + " " +
                            " WHERE Tweet_id in(" +
                            /* Select all tweets that contain words from the MyList */
                            "SELECT DISTINCT _id as [Tweet_id]" +
                            " FROM " + TABLE_FAVORITES_LISTS_TWEETS_ENTRIES  + " " +
                            " WHERE [Name] = ? and  [Sys] = ?   " +
                            ") " +
                            ") as a " +
                            "LEFT JOIN " +
                            " (" +
                            "SELECT [_id] as [Edict_id]" +
                            ",sum([Correct]) as [Correct]" +
                            ",sum([Total]) as [Total] FROM [JScoreboard] " +
                            "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + TABLE_SAVED_TWEET_KANJI  + ")" +
                            " GROUP BY [_id]" +
                            ") as b " +
                            "ON a.[Edict_id] = b.[Edict_id] " +

                            " ) as TweetKanji " +
                            "On TweetLists.Tweet_id = TweetKanji.Tweet_id " +

                            ") as [ListsTweetsAndAllKanjis] " +
                            "GROUP BY [Tweet_id]" +
                            ") as [ListandTweets]  " +
                            ") as [Lists] " +
                            "WHERE [Category] in (" + colorString + ") " +
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
                            " FROM "+ TABLE_SAVED_TWEETS + " " +
                            ") as [TweetData] " +


                            "LEFT JOIN " +
                            " (" +
                            "SELECT DISTINCT [UserId] " +
                            ", [ScreenName] " +
                            ", [UserName] " +
                            " FROM " + TABLE_USERS +" " +
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
//                            ",CAST(ifnull(b.[Correct],0)  as float)/b.[Total] as [Percent] " +

                            "FROM " +
                            "( " +
                            " SELECT Tweet_id" +
                            ",Edict_id " +
                            ",[StartIndex]" +
                            ",[EndIndex]" +
                            "From " + TABLE_SAVED_TWEET_KANJI  + " " +
                            " WHERE [Edict_id] is not NULL and StartIndex is not NULL and EndIndex is not NULL and EndIndex > StartIndex " +
                            "and Tweet_id in (" +
                                /* Get a list of tweets that contain the kanji we are interested in */
                            "SELECT DISTINCT [Tweet_id]" +
                            " FROM " + TABLE_SAVED_TWEET_KANJI  + " " +
                            "WHERE [Edict_id] in (" +
                            "SELECT DISTINCT [_id]" +
                            " FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
                            " WHERE [Name] = ? and  [Sys] = ?   " +

                            ") " +
                            ") " +


                            ") as a " +
                            "LEFT JOIN " +
                            " (" +
                            "SELECT [_id] as [Edict_id]" +
                            ",sum([Correct]) as [Correct]" +

                            ",sum([Total]) as [Total] FROM [JScoreboard] " +
                            "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + TABLE_SAVED_TWEET_KANJI  + ")" +
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
                            "SELECT DISTINCT [_id]" +
                            " FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
                            " WHERE [Name] = ? and  [Sys] = ?   " +

                            ") " +
                            ") as c " +
                            "ON a.[Edict_id] = c.[Edict_id] " +

                            " ) as TweetKanji " +
                            "On TweetIds.Tweet_id = TweetKanji.Tweet_id " +


                            "Order by date(MetaData.Date) Desc,TweetIds.[Tweet_id] asc,TweetKanji.StartIndex asc"
                    ,  new String[]{myListEntry.getListName()
                            ,String.valueOf(myListEntry.getListsSys())
                            ,myListEntry.getListName()
                            ,String.valueOf(myListEntry.getListsSys())
                            ,myListEntry.getListName()
                            ,String.valueOf(myListEntry.getListsSys())});



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

                    }


                    WordEntry wordEntry = new WordEntry(c.getInt(6)
                            ,c.getString(7)
                            ,c.getString(8)
                            ,c.getString(9)
                            ,c.getInt(10)
                            ,c.getInt(11)
                            ,c.getString(12)
                            ,c.getInt(13)
                            ,c.getInt(14));

                    //Designate spinner kanji if they exist

                    tweet.addWordEntry(wordEntry);


                    //FLush old tweet
                    if(!currentTweetId.equals(c.getString(0))){

                        Tweet tweetToAdd = new Tweet(tweet);
                        setRandomSpinnersForTweet(tweetToAdd);
                        savedTweets.add(tweetToAdd);

                        currentTweetId = c.getString(0);
                        tweet = new Tweet();
                        tweet.setIdString(c.getString(0));
                        tweet.setCreatedAt(c.getString(5));
                        tweet.getUser().setUserId(c.getString(3));
                        tweet.setText(c.getString(4));
                        tweet.getUser().setScreen_name(c.getString(1));
                        tweet.getUser().setName(c.getString(2));
                    }

                    if(c.isLast()) {
                        Tweet lastTweet = new Tweet(tweet);
                        setRandomSpinnersForTweet(lastTweet);
                        savedTweets.add(lastTweet);
                    }
                    c.moveToNext();
                }


            } else {if(debug) {Log.d(TAG,"c.getcount was 0!!");}}
            c.close();

        } catch (SQLiteException e){
            Log.e(TAG,"getFillintheBlanksTweetsForATweetList Sqlite exception: " + e);
        } catch (Exception e) {
            Log.e(TAG,"getFillintheBlanksTweetsForATweetList generic exception: " + e);
        } finally {
            db.close();
        }
        return savedTweets;
    }


    public ArrayList<Tweet> getFillintheBlanksTweetsForAWordList(MyListEntry myListEntry
            , ColorThresholds colorThresholds
            , String colorString
            , @Nullable Integer resultLimit) {

        long startTime = System.nanoTime();
        ArrayList<Integer> possibleSpinners = getIdsForWordList(myListEntry);
        long endTime = System.nanoTime();
        Log.e(TAG,"ELLAPSED POSSIBLE SPINNERS: " + (endTime - startTime) / 1000000000.0);

        String limit;
        if(resultLimit == null) {
            limit = "";
        } else {
            limit = "LIMIT " + String.valueOf(resultLimit);
        }

        ArrayList<Tweet> savedTweets = new ArrayList<>();
        SQLiteDatabase db = sInstance.getReadableDatabase();
        try {


            Cursor c = db.rawQuery("SELECT TweetIds.[Tweet_id]" +
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

                            "FROM  " +

                            " ( " +
                     /* Get a list of tweets that contain the kanji we are interested in */


                            "Select DISTINCT Tweet_id " +
                            " FROM (" +

//                /* Assign each tweet a color based on the percentages of word color scores for kanjis in the tweet */
                            "Select Tweet_id " +
                            ",(CASE WHEN [Total] = 0 THEN 'Empty' " +
                            " WHEN CAST(ifnull([Grey],0)  as float)/[Total] > " + colorThresholds.getTweetGreyThreshold() + " THEN 'Grey' " +
                            " WHEN CAST(ifnull([Green],0)  as float)/[Total] >= " + colorThresholds.getTweetGreenthreshold() + " THEN 'Green' " +
                            " WHEN  CAST(ifnull([Red],0)  as float)/[Total] >= " + colorThresholds.getTweetRedthreshold() + " THEN 'Red' " +
                            " WHEN CAST(ifnull([Yellow],0)  as float)/[Total] >= " + colorThresholds.getTweetYellowthreshold() +" THEN 'Yellow' " +
                            " WHEN [Grey] > [Green] and [Grey] > [Red] and [Grey] > [Yellow] THEN 'Grey' " +
                            " WHEN [Green] > [Grey] and [Green] > [Red] and [Green] > [Yellow] THEN 'Green' " +
                            " WHEN [Red] > [Green] and [Red] > [Grey] and [Red] > [Yellow] THEN 'Red' " +
                            " WHEN [Yellow] > [Green] and [Yellow] > [Red] and [Yellow] > [Grey] THEN 'Yellow' " +
                            " ELSE 'Grey' END) as [Category] " +
                            " FROM ( " +

                /* Now to pull together ListName, Tweet and the totals (by color) of the kanji in those tweets */
                            "SELECT ListsTweetsAndAllKanjis.[Tweet_id] "+

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
                            " FROM " + TABLE_SAVED_TWEET_KANJI  + " " +
                            "WHERE [Edict_id] in (" +
                            "SELECT DISTINCT [_id]" +
                            " FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
                            " WHERE [Name] = ?  and [Sys] = ? " +
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
                            " SELECT Tweet_id" +
                            ",Edict_id " +
                            "From " + TABLE_SAVED_TWEET_KANJI  + " " +
                            " WHERE Tweet_id in(" +
                            "SELECT DISTINCT [Tweet_id]" +
                            " FROM " + TABLE_SAVED_TWEET_KANJI  + " " +
                            "WHERE [Edict_id] in (" +
                            "SELECT DISTINCT [_id]" +
                            " FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
                            " WHERE [Name] = ? and  [Sys] = ?   " +
                            ") " +
                            ") " +
                            ") as a " +
                            "LEFT JOIN " +
                            " (" +
                            "SELECT [_id] as [Edict_id]" +
                            ",sum([Correct]) as [Correct]" +
                            ",sum([Total]) as [Total] FROM [JScoreboard] " +
                            "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + TABLE_SAVED_TWEET_KANJI  + ")" +
                            " GROUP BY [_id]" +
                            ") as b " +
                            "ON a.[Edict_id] = b.[Edict_id] " +

                            " ) as TweetKanji " +
                            "On TweetLists.Tweet_id = TweetKanji.Tweet_id " +

                            ") as [ListsTweetsAndAllKanjis] " +
                            "GROUP BY [Tweet_id]" +
                            ") as [ListandTweets]  " +
                            ") as [Lists] " +
                            "WHERE [Category] in (" + colorString + ") " +
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
                            " FROM "+ TABLE_SAVED_TWEETS + " " +
                            ") as [TweetData] " +


                            "LEFT JOIN " +
                            " (" +
                            "SELECT DISTINCT [UserId] " +
                            ", [ScreenName] " +
                            ", [UserName] " +
                            " FROM " + TABLE_USERS +" " +
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
//                            ",CAST(ifnull(b.[Correct],0)  as float)/b.[Total] as [Percent] " +

                            "FROM " +
                            "( " +
                            " SELECT Tweet_id" +
                            ",Edict_id " +
                            ",[StartIndex]" +
                            ",[EndIndex]" +
                            "From " + TABLE_SAVED_TWEET_KANJI  + " " +
                            " WHERE [Edict_id] is not NULL and StartIndex is not NULL and EndIndex is not NULL and EndIndex > StartIndex " +
                            "and Tweet_id in (" +
                                /* Get a list of tweets that contain the kanji we are interested in */
                            "SELECT DISTINCT [Tweet_id]" +
                            " FROM " + TABLE_SAVED_TWEET_KANJI  + " " +
                            "WHERE [Edict_id] in (" +
                            "SELECT DISTINCT [_id]" +
                            " FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
                            " WHERE [Name] = ? and  [Sys] = ?   " +

                            ") " +
                            ") " +


                            ") as a " +
                            "LEFT JOIN " +
                            " (" +
                            "SELECT [_id] as [Edict_id]" +
                            ",sum([Correct]) as [Correct]" +

                            ",sum([Total]) as [Total] FROM [JScoreboard] " +
                            "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + TABLE_SAVED_TWEET_KANJI  + ")" +
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
                            "SELECT DISTINCT [_id]" +
                            " FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
                            " WHERE [Name] = ? and  [Sys] = ?   " +

                            ") " +
                            ") as c " +
                            "ON a.[Edict_id] = c.[Edict_id] " +

                            " ) as TweetKanji " +
                            "On TweetIds.Tweet_id = TweetKanji.Tweet_id " +


                            "Order by date(MetaData.Date) Desc,TweetIds.[Tweet_id] asc,TweetKanji.StartIndex asc"
                    ,  new String[]{myListEntry.getListName()
                            ,String.valueOf(myListEntry.getListsSys())
                            ,myListEntry.getListName()
                            ,String.valueOf(myListEntry.getListsSys())
                            ,myListEntry.getListName()
                            ,String.valueOf(myListEntry.getListsSys())});


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
                    }


                    WordEntry wordEntry = new WordEntry(c.getInt(6)
                            ,c.getString(7)
                            ,c.getString(8)
                            ,c.getString(9)
                            ,c.getInt(10)
                            ,c.getInt(11)
                            ,c.getString(12)
                            ,c.getInt(13)
                            ,c.getInt(14));

                    //Designate spinner kanji if they exist
                    tweet.addWordEntry(wordEntry);


                    //FLush old tweet
                    if(!currentTweetId.equals(c.getString(0))){

                        setSpinnersForTweetWithMyListWords(db,"Word",myListEntry,tweet,possibleSpinners);
                        savedTweets.add(new Tweet(tweet));

                        currentTweetId = c.getString(0);
                        tweet = new Tweet();
                        tweet.setIdString(c.getString(0));
                        tweet.setCreatedAt(c.getString(5));
                        tweet.getUser().setUserId(c.getString(3));
                        tweet.setText(c.getString(4));
                        tweet.getUser().setScreen_name(c.getString(1));
                        tweet.getUser().setName(c.getString(2));
                    }

                    if(c.isLast()) {
                        Tweet lastTweet = new Tweet(tweet);
                        setSpinnersForTweetWithMyListWords(db,"Word",myListEntry,lastTweet,possibleSpinners);
                        savedTweets.add(lastTweet);
                    }
                    c.moveToNext();
                }


            } else {if(debug) {Log.d(TAG,"c.getcount was 0!!");}}
            c.close();

        } catch (SQLiteException e){
            Log.e(TAG,"getTweetsForSavedTweetsList Sqlite exception: " + e);
        } catch (Exception e) {
            Log.e(TAG,"getTweetsForSavedTweetsList generic exception: " + e);
        } finally {
            db.close();
        }
        return savedTweets;
    }

//
//    public ArrayList<Tweet> getFillintheBlanksTweetsForAWordList_TEST(MyListEntry myListEntry
//            , ColorThresholds colorThresholds
//            , String colorString
//            , @Nullable Integer resultLimit) {
//
//        long startTime = System.nanoTime();
//        ArrayList<Integer> possibleSpinners = getIdsForWordList(myListEntry);
//        long endTime = System.nanoTime();
//        Log.e(TAG,"ELLAPSED POSSIBLE SPINNERS: " + (endTime - startTime) / 1000000000.0);
//
//        String limit;
//        if(resultLimit == null) {
//            limit = "";
//        } else {
//            limit = "LIMIT " + String.valueOf(resultLimit);
//        }
//
//        ArrayList<Tweet> savedTweets = new ArrayList<>();
//        SQLiteDatabase db = sInstance.getReadableDatabase();
//        try {
//
//
//
//
//
//
//
//            Cursor c = db.rawQuery("SELECT TweetIds.[Tweet_id]" +
//                            ",[MetaData].ScreenName " +
//                            ",[MetaData].UserName " +
//                            ",[MetaData].[UserId] " +
//                            ",[MetaData].[Text] " +
//                            ",[MetaData].[Date]" +
//                            ",TweetKanji.Edict_id " +
//                            ",TweetKanji.Kanji " +
//                            ",(CASE WHEN [TweetKanji].[Furigana] is null  then '' else [TweetKanji].[Furigana] end) as [Furigana] " +
//                            ",TweetKanji.Definition " +
//                            ",TweetKanji.Total " +
//                            ",TweetKanji.Correct " +
////                            ",TweetKanji.Color " +
//                            ", 'BALLs' as [Color]" +
//                            ",TweetKanji.StartIndex " +
//                            ",TweetKanji.EndIndex " +
//
//                            "FROM  " +
//
//                            " ( " +
//                     /* Get a list of tweets that contain the kanji we are interested in */
//
//
//                            "SELECT DISTINCT [Tweet_id]" +
//                            " FROM " + TABLE_SAVED_TWEET_KANJI  + " " +
//                            "WHERE [Edict_id] in (" +
//                            "SELECT DISTINCT [_id]" +
//                            " FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
//                            " WHERE [Name] = ?  and [Sys] = ? " +
//
//                                ") " +
//                            ") as [TweetIds] " +
//
//                            " LEFT JOIN " +
//
//                            /* Attach metadata about tweet and user */
//                            "( " +
//                            "SELECT DISTINCT [TweetData].[Tweet_id]" +
//                            ",[TweetData].[Text]" +
//                            ",[TweetData].[UserId] " +
//                            ",[TweetData].[Date] " +
//                            ",[UserData].[ScreenName] " +
//                            ",[UserData].[UserName] " +
//                            " FROM " +
//                            " ( " +
//                            "SELECT  DISTINCT [Tweet_id]" +
//                            ",[Text]" +
//                            ",[UserId] " +
//                            ",[CreatedAt]  as [Date] " +
//                            " FROM "+ TABLE_SAVED_TWEETS + " " +
//                            ") as [TweetData] " +
//
//
//                            "LEFT JOIN " +
//                            " (" +
//                            "SELECT DISTINCT [UserId] " +
//                            ", [ScreenName] " +
//                            ", [UserName] " +
//                            " FROM " + TABLE_USERS +" " +
//                            ") as [UserData] " +
//                            "On TweetData.UserId = UserData.UserId " +
//                            ") as [MetaData] " +
//                            " On TweetIds.Tweet_id = MetaData.Tweet_id " +
//
//                    // TODO ITS BELOW THIS LINE THATS THE PROBLEM....
//
//                            "LEFT JOIN " +
//                           /* Attach all the rows for individual kanji in each saved tweet */
//                            " ( " +
//                                /* Get a list of  kanji ids and their word scores for each tweet */
//                            "SELECT a.[Tweet_id]" +
//                            ",a.[Edict_id]" +
//                            ",a.[StartIndex]" +
//                            ",a.[EndIndex]" +
//
//
//                            ",c.Furigana as [Furigana]" +
//                            ",c.Definition as [Definition] " +
//                            ",c.Kanji as [Kanji] " +
//                            ",b.[Total] as [Total]" +
//                            ",b.[Correct] as [Correct] " +
////                            ",CAST(ifnull(b.[Correct],0)  as float)/b.[Total] as [Percent] " +
//
//                            "FROM " +
//                            "( " +
//                            " SELECT Tweet_id" +
//                            ",Edict_id " +
//                            ",[StartIndex]" +
//                            ",[EndIndex]" +
//                            "From " + TABLE_SAVED_TWEET_KANJI  + " " +
//                            " WHERE [Edict_id] is not NULL and StartIndex is not NULL and EndIndex is not NULL and EndIndex > StartIndex " +
//                            ") as a " +
//                            "LEFT JOIN " +
//                            " (" +
//                            "SELECT [_id] as [Edict_id]" +
//                            ",sum([Correct]) as [Correct]" +
//
//                            ",sum([Total]) as [Total] FROM [JScoreboard] " +
////                            "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + TABLE_SAVED_TWEET_KANJI  + ")" +
//                            " GROUP BY [_id]" +
//                            ") as b " +
//                            "ON a.[Edict_id] = b.[Edict_id] " +
//                            "LEFT JOIN " +
//                            " (" +
//                            "SELECT DISTINCT [_id] as [Edict_id]" +
//                            ",[Kanji]" +
//                            ",[Furigana]" +
//                            ",[Definition]" +
//                            "FROM [Edict] " +
//                            ") as c " +
//                            "ON a.[Edict_id] = c.[Edict_id] " +
//
//                            " ) as TweetKanji " +
//                            "On TweetIds.Tweet_id = TweetKanji.Tweet_id "
//                    ,  new String[]{myListEntry.getListName()
//                            ,String.valueOf(myListEntry.getListsSys())});
//
//            long startTime5 = System.nanoTime();
//            Log.d(TAG,"C COUNT : " + c.getCount());
//            long endTime5 = System.nanoTime();
//            Log.e(TAG,"ELLAPSED TIME C COUNT: " + (endTime5 - startTime5) / 1000000000.0);
//
//            /* The query pulls a list of tweetdata paired with each parsed-kanji in the tweet, resulting in
//            * multiple duplicate lines of tweetdata. So the cursor ony adds tweet data once, when a new tweetid is found. Meanwhile
//            * the kanji data for each row is added to a TweetKanjiColor object, which is then added to the kanji and the kanji
//            * added to the final "savedTweets" list when a new tweetid appears (or the cursor finishes) */
//
//
//            if(c.getCount()>0) {
//                c.moveToFirst();
//                String currentTweetId = c.getString(0);
//                Tweet tweet = new Tweet();
//                while (!c.isAfterLast())
//                {
//
//                    if(c.isFirst()) {
//                        long startTime2 = System.nanoTime();
//                        tweet.setIdString(c.getString(0));
//                        tweet.setCreatedAt(c.getString(5));
//                        tweet.getUser().setUserId(c.getString(3));
//                        tweet.setText(c.getString(4));
//                        tweet.getUser().setScreen_name(c.getString(1));
//                        tweet.getUser().setName(c.getString(2));
//                        long endTime2 = System.nanoTime();
//                        Log.e(TAG,"ELLAPSED TIME FIRST: " + (endTime2 - startTime2) / 1000000000.0);
//                    }
//
//
//                    WordEntry wordEntry = new WordEntry(c.getInt(6)
//                            ,c.getString(7)
//                            ,c.getString(8)
//                            ,c.getString(9)
//                            ,c.getInt(10)
//                            ,c.getInt(11)
//                            ,c.getString(12)
//                            ,c.getInt(13)
//                            ,c.getInt(14));
//
//                    //Designate spinner kanji if they exist
//
//                    tweet.addWordEntry(wordEntry);
//
//
//                    //FLush old tweet
//                    if(!currentTweetId.equals(c.getString(0))){
//
//
//                        long startTime1 = System.nanoTime();
//
//                        setSpinnersForTweetWithMyListWords(tweet,possibleSpinners);
//                        savedTweets.add(new Tweet(tweet));
//
//
//                        long endTime1 = System.nanoTime();
//                        Log.e(TAG,"ELLAPSED FLUSH TWEET: " + (endTime1 - startTime1) / 1000000000.0);
//
//                        currentTweetId = c.getString(0);
//                        tweet = new Tweet();
//                        tweet.setIdString(c.getString(0));
//                        tweet.setCreatedAt(c.getString(5));
//                        tweet.getUser().setUserId(c.getString(3));
//                        tweet.setText(c.getString(4));
//                        tweet.getUser().setScreen_name(c.getString(1));
//                        tweet.getUser().setName(c.getString(2));
//                    }
//
//                    if(c.isLast()) {
//                        Tweet lastTweet = new Tweet(tweet);
//                        setSpinnersForTweetWithMyListWords(lastTweet,possibleSpinners);
//                        savedTweets.add(lastTweet);
//                    }
//                    c.moveToNext();
//                }
//
//
//            } else {if(debug) {Log.d(TAG,"c.getcount was 0!!");}}
//            c.close();
//
//        } catch (SQLiteException e){
//            Log.e(TAG,"getTweetsForSavedTweetsList Sqlite exception: " + e);
//        } catch (Exception e) {
//            Log.e(TAG,"getTweetsForSavedTweetsList generic exception: " + e);
//        } finally {
//            db.close();
//        }
//        return savedTweets;
//    }

//    public void setSpinnersForTweetWithMyListWords(Tweet tweet, ArrayList<Integer> wordListEdictIds) {
//        ArrayList<WordEntry> wordEntries = tweet.getWordEntries();
//        int spinnerAddedCount = 0;
//
//        /* Determine maxiumum number of spinners that can be added to the tweet, from 1 - 3,
//        with a 50% chance of 1, 40% chance of 2 and a 10 % chance of 3*/
//        int[] possibleSpinnerLimits = new int[]{1,1,1,1,1,2,2,2,2,3};
//        int spinnerLimit = possibleSpinnerLimits[(new Random()).nextInt(possibleSpinnerLimits.length)];
//
//        /* Pick random kanji from the wordEntries list to be spinners (with maximum of the spinner limit)*/
//        Collections.shuffle(wordEntries);
//        for(int i=0;i<wordEntries.size() && spinnerAddedCount<spinnerLimit;i++) {
//            if(wordListEdictIds.contains(wordEntries.get(i).getId())) {
//                wordEntries.get(i).setSpinner(true);
//                spinnerAddedCount += 1;
//            }
//        }
//    }
//
//
//    public ArrayList<Integer> getIdsForWordList(MyListEntry myListEntry) {
//        ArrayList<Integer> ids = new ArrayList<>();
//        SQLiteDatabase db = sInstance.getReadableDatabase();
//        try {
//            Cursor c = db.rawQuery(
//                    "SELECT DISTINCT [_id]" +
//                            " FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
//                            " WHERE [Sys] = ? and [Name] = ? ",new String[]{myListEntry.getListName()
//                            ,String.valueOf(myListEntry.getListsSys())});
//            if(c.getCount()>0) {
//                c.moveToFirst();
//                String currentTweetId = c.getString(0);
//                Tweet tweet = new Tweet();
//                while (!c.isAfterLast()) {
//                    ids.add(c.getInt(0));
//                    c.moveToNext();
//                }
//
//                c.close();
//            }
//
//        } catch (SQLiteException e){
//            Log.e(TAG,"getIdsForWordList Sqlite exception: " + e);
//        } catch (Exception e) {
//            Log.e(TAG,"getIdsForWordList generic exception: " + e);
//        } finally {
//            db.close();
//        }
//
//        return ids;
//    }






    public void setRandomSpinnersForTweet(Tweet tweet) {
        ArrayList<WordEntry> wordEntries = tweet.getWordEntries();
        int spinnerAddedCount = 0;

        /* Determine maxiumum number of spinners that can be added to the tweet, from 1 - 3,
        with a 50% chance of 1, 40% chance of 2 and a 10 % chance of 3*/
        int[] possibleSpinnerLimits = new int[]{1,1,1,1,1,2,2,2,2,3};
        int spinnerLimit = possibleSpinnerLimits[(new Random()).nextInt(possibleSpinnerLimits.length)];

        /* Pick random kanji from the wordEntries list to be spinners (with maximum of the spinner limit)*/
        Collections.shuffle(wordEntries);
        for(int i=0;i<wordEntries.size() && spinnerAddedCount<spinnerLimit;i++) {
            wordEntries.get(i).setSpinner(true);
            spinnerAddedCount += 1;
        }
    }

    public void TEST(MyListEntry myListEntry) {

        SQLiteDatabase db = sInstance.getWritableDatabase();
        String queryRecordExists =
                "SELECT DISTINCT [Tweet_id]" +
                        " FROM " + TABLE_SAVED_TWEET_KANJI  + " " +
                        "WHERE [Edict_id] in (" +
                        "SELECT DISTINCT [_id]" +
                        " FROM " + TABLE_FAVORITES_LIST_ENTRIES + " " +
                        " WHERE [Name] =  ?  and [Sys] = ? " +
                        ") " ;


        Cursor c = db.rawQuery(queryRecordExists, new String[]{myListEntry.getListName()
                ,String.valueOf(myListEntry.getListsSys())});
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    Log.e(TAG,"tweetid: " + c.getString(0));
                    c.moveToNext();
                }
            } else {
                Log.e(TAG,"C IS EMPTY");
            }
     db.close();

    }
}

