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

import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.ParseSentenceItem;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetKanjiColor;
import com.jukuproject.jukutweet.Models.TweetUrl;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.Models.WordLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Database helper
 */
public class InternalDB extends SQLiteOpenHelper {

    private static boolean debug = true;
    private static String TAG = "TEST-Internal";
    private static InternalDB sInstance;

    public static  String DB_NAME =  "JQuiz";
    public static String DATABASE_NAME = DB_NAME + ".db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_MAIN = "Users";
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

    public static final String TABLE_FAVORITES_LISTS_TWEETS = "JFavoritesTweetLists";
//    public static final String TFAVORITESTWEETLISTS_COL0 = "Name";

    public static final String TABLE_FAVORITES_LISTS_TWEETS_ENTRIES = "JFavoritesTweets";
////    public static final String TFAVORITESTWEETS_COL1 = "Name";
//    public static final String TFAVORITESTWEETS_COL1 = "Sys";

    public static final String TFAVORITESTWEETS_COL2 = "TweetId";


    public static final String TABLE_FAVORITES_LISTS = "JFavoritesLists";
    public static final String TFAVORITES_COL0 = "Name";

    public static final String TABLE_FAVORITES_LIST_ENTRIES = "JFavorites";
    public static final String TFAVORITES_COL1 = "Sys";


    public static final String TABLE_SAVED_TWEETS = "JSavedTweets";
    public static final String TSAVEDTWEET_COL0 = "UserId";
    public static final String TSAVEDTWEET_COL1 = "UserScreenName";
    public static final String TSAVEDTWEET_COL2 = "Tweet_id";
    public static final String TSAVEDTWEET_COL3 = "CreatedAt";
    public static final String TSAVEDTWEET_COL4 = "Text";

    public static final String TABLE_SAVED_TWEET_KANJI = "JSavedTweetKanji";
    public static final String TSAVEDTWEETITEMS_COL0 = "Tweet_id"; //JSavedTweets Primary Key!
//    public static final String TSAVEDTWEETITEMS_COL1 = "IndexOrder"; //JSavedTweets Primary Key!
    public static final String TSAVEDTWEETITEMS_COL2 = "Edict_id"; //Edict Primary Key!
    public static final String TSAVEDTWEETITEMS_COL3 = "StartIndex"; //Edict Primary Key!
    public static final String TSAVEDTWEETITEMS_COL4 = "EndIndex"; //Edict Primary Key!





    public static final String TABLE_SAVED_TWEET_URLS = "JSavedTweetUrls";
//    public static final String TSAVEDTWEETITEMS_COL0 = "STweet_id"; //JSavedTweets Primary Key!
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
                                "%s TEXT) ", TABLE_MAIN,
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


        String sqlQueryJScoreBoard =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s INTEGER PRIMARY KEY, " +
                                "%s INTEGER, " +
                                "%s INTEGER)", TABLE_SCOREBOARD,
                        COL_ID, //_id
                        TSCOREBOARD_COL0, //Total
                        TSCOREBOARD_COL1); // Correct

        sqlDB.execSQL(sqlQueryJScoreBoard);

        //The "My Lists" table
        String sqlQueryJFavoritesLists =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s TEXT)", TABLE_FAVORITES_LISTS,
                        TFAVORITES_COL0);

        sqlDB.execSQL(sqlQueryJFavoritesLists);

        String sqlQueryJFavoritesListEntries =
                String.format("CREATE TABLE IF NOT EXISTS  %s (" +
                                "%s INTEGER, " +
                                "%s TEXT, " +
                                "%s INTEGER)", TABLE_FAVORITES_LIST_ENTRIES,
                        COL_ID,
                        TFAVORITES_COL0,
                        TFAVORITES_COL1); // if this column = 1, it is a system table (i.e. blue, red, yellow), if user-created the value is 0

        sqlDB.execSQL(sqlQueryJFavoritesListEntries);


        //The "My Lists" table
        String sqlQueryJFavoritesListsTweets =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s TEXT)", TABLE_FAVORITES_LISTS_TWEETS,
                        TFAVORITES_COL0);

        sqlDB.execSQL(sqlQueryJFavoritesListsTweets);

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

        String sqlQueryJSavedTweetEntries =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s TEXT, " +
                                "%s INTEGER, " +
                                "%s INTEGER, " +
                                "%s INTEGER)", TABLE_SAVED_TWEET_KANJI,
                        COL_ID, //_id (unique)
                        TSAVEDTWEETITEMS_COL0, //STweet_id (JSavedTweet _id)
//                        TSAVEDTWEETITEMS_COL1, // Order (order kanji appears in text)
                        TSAVEDTWEETITEMS_COL2, // Edict_id
                        TSAVEDTWEETITEMS_COL3, //STart index
                        TSAVEDTWEETITEMS_COL4); //End INdex

        sqlDB.execSQL(sqlQueryJSavedTweetEntries);


        String sqlQueryJSavedTweetUrls =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s INTEGER, " +
                                "%s TEXT, " +
                                "%s INTEGER, " +
                                "%s INTEGER)", TABLE_SAVED_TWEET_URLS,
                        COL_ID, //_id (unique)
                        TSAVEDTWEETITEMS_COL0, //STweet_id (JSavedTweet _id)
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
        SQLiteDatabase db = this.getWritableDatabase();
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
        SQLiteDatabase db = this.getWritableDatabase();
        String queryRecordExists = "Select _id From " + TABLE_MAIN + " where " + TMAIN_COL0 + " = ?" ;
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
        SQLiteDatabase db = this.getWritableDatabase();
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

                db.insert(TABLE_MAIN, null, values);

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
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_MAIN, TMAIN_COL0 + "= ?", new String[]{user});
            db.close();
            return true;
        } catch(SQLiteException exception) {
            return false;
        }

    }


//TODO == hook this to user id? instead of screenname
    public void addMediaURItoDB(String URI, String screenName) {

//        Log.d(TAG,"URI VALUE: " + rowID + " - " + URI);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TMAIN_COL6, URI);
        db.update(TABLE_MAIN, values, TMAIN_COL0 + "= ?", new String[] {screenName});
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

        String querySelectAll = "Select distinct ScreenName,IFNULL(Description,''),FollowerCount, FriendCount, IFNULL(ProfileImgUrl,''),UserId, ProfileImgFilePath, UserName From " + TABLE_MAIN;
        SQLiteDatabase db = this.getReadableDatabase();
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
                    SQLiteDatabase db = this.getReadableDatabase();
                    db.update(TABLE_MAIN, values, TMAIN_COL1 + "= ?", new String[]{String.valueOf(oldUserInfo.getUserId())});
                } else {
                    SQLiteDatabase db = this.getReadableDatabase();
                    db.update(TABLE_MAIN, values, TMAIN_COL0 + "= ?", new String[]{oldUserInfo.getScreenName()});
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
     * @param inputDB Sqlite database connection
     * @return WordLoader object with array lists and maps of various japanse characters
     *
//     * @see com.jukuproject.jukutweet.SentenceParser#parseSentence(String, SQLiteDatabase, ArrayList, ArrayList, WordLoader, ColorThresholds, ArrayList)
     */
    public WordLoader getWordLists(@Nullable SQLiteDatabase inputDB) {
        SQLiteDatabase db;
        if(inputDB == null) {
            db = this.getWritableDatabase();
        } else {
            db = inputDB;
        }
        ArrayList<String> hiragana = new ArrayList<>();
        ArrayList<String> katakana = new ArrayList<>();
        ArrayList<String> symbols = new ArrayList<>();
        HashMap<String,ArrayList<String>> romajiMap = new HashMap<>();
        ArrayList<String> verbEndingsRoot = new ArrayList<>();
        ArrayList<String> verbEndingsConjugation = new ArrayList<>();
        HashMap<String,ArrayList<String>> verbEndingMap = new HashMap<>();

        try {
            ArrayList<String> romaji = new ArrayList<>();
            Cursor c = db.rawQuery("SELECT DISTINCT Type, Key, Value FROM [Characters] ORDER BY Type",null);
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

        if(inputDB == null && db.isOpen()) {
            db.close();
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
                SQLiteDatabase db = this.getWritableDatabase();
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
            SQLiteDatabase db = this.getWritableDatabase();
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
            SQLiteDatabase db = this.getWritableDatabase();
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
        SQLiteDatabase db = this.getWritableDatabase();

            String sql = "Update JFavoritesLists SET [Name]=? WHERE [Name]=? ";
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
        SQLiteDatabase db = this.getWritableDatabase();

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
        SQLiteDatabase db = this.getWritableDatabase();

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
                String sql = "Update "  + TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " SET "+ TFAVORITES_COL0+" = ? WHERE "+ TFAVORITES_COL0 +"= ? and " + TFAVORITES_COL1+" = 1 and " + COL_ID + " = ?";
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
        SQLiteDatabase db = this.getWritableDatabase();
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

//    public boolean addKanjiToMyList(int kanjiId, String listName, int listSys) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        try {
//            ContentValues values = new ContentValues();
//            values.put(COL_ID, kanjiId);
//            values.put(TFAVORITES_COL0, listName);
//            values.put(TFAVORITES_COL1, listSys);
//            db.insert(TABLE_FAVORITES_LIST_ENTRIES, null, values);
//            return true;
//        } catch (SQLiteException e) {
//            Log.e(TAG, "addKanjiToMyList sqlite exception: " + e);
//        } catch (NullPointerException e) {
//            Log.e(TAG, "addKanjiToMyList something was null: " + e);
//        } finally {
//            db.close();
//        }
//
//        return  false;
//    }

    public boolean addTweetToMyList(String tweet_id,String user_id,String listName, int listSys) {
        SQLiteDatabase db = this.getReadableDatabase();

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
        SQLiteDatabase db = this.getWritableDatabase();
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
//
//    public boolean addBulkKanjiToList(String kanjiIdString, MyListEntry myListEntry) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        try {
//            db.execSQL("INSERT OR REPLACE INTO " + TABLE_FAVORITES_LIST_ENTRIES + " WHERE " + TFAVORITES_COL0 + " = ? and "  + TFAVORITES_COL1 + " = ? and " + COL_ID + " in (" + kanjiIdString + ")",new String[]{myListEntry.getListName(),String.valueOf(myListEntry.getListsSys())});
//            return true;
//        } catch (SQLiteException e) {
//            Log.e(TAG, "removeBulkKanjiFromMyList sqlite exception: " + e);
//        } catch (NullPointerException e) {
//            Log.e(TAG, "removeBulkKanjiFromMyList something was null: " + e);
//        } finally {
//            db.close();
//        }
//        return  false;
//    }
//

    public boolean removeKanjiFromMyList(int kanjiId, String listName, int listSys) {
        SQLiteDatabase db = this.getWritableDatabase();
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
        SQLiteDatabase db = this.getWritableDatabase();
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

    /**
     * Pulls all active MyList lists from the db, and notes whether each list contains ALL of a certain kanji (or multiple kanjis),
     * Some, or None.
//     * @see com.jukuproject.jukutweet.com.jukuproject.jukutweet.PopupChooseFavoriteLists#p
     * @param activeFavoriteStars
//     * @param kanjiIds kanji id or multiple ids contatenated together. ex. "451,456,333,32"
//     * @param kanjiCount Total count of those kanjis
     * @return
     */
    public ArrayList<MyListEntry> getFavoritesListsForKanji(ArrayList<String> activeFavoriteStars, String kanjiIds,@Nullable MyListEntry entryToExclude) {
        ArrayList<MyListEntry> myListEntries = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
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

//            Log.d(TAG,"Listsname: " + name + ", listsys: " + sys);

            c.moveToFirst();
            if(c.getCount()>0) {
//                Log.d(TAG,"HERE IN THING : " + c.getCount());
//                Log.d(TAG,"HERE IN activeFavoriteStars : " + c.size());
                while (!c.isAfterLast()) {



                    //Add all user lists (where sys == 0)
                    //Only add system lists (sys==1), but only if the system lists are actived in user preferences
                    if((c.getInt(1) == 0 || activeFavoriteStars.contains(c.getString(0)))
                            && (entryToExclude!= null && !(entryToExclude.getListName().equals(c.getString(0)) && entryToExclude.getListsSys() == c.getInt(1)))) {
                        MyListEntry entry = new MyListEntry(c.getString(0),c.getInt(1),c.getInt(2));
                        myListEntries.add(entry);
//                        Log.d(TAG,"GETFAVS adding : " + entry.getListName());
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

    public ArrayList<WordEntry> getMyListWords(MyListEntry myListEntry) {
        ArrayList<WordEntry> wordEntries = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor c = db.rawQuery("SELECT  x.[_id]" +
                                            ",x.[Kanji]" +
                                            ",(CASE WHEN (x.Furigana is null OR  x.Furigana = '') then \"\" else \"(\" || x.Furigana || \")\" end) as [Furigana]" +
                                            ",x.[Definition]" +
                                            ",ifnull(y.[Correct],0)" +
                                            ",ifnull(y.[Total],0)" +
                                            ",(CASE WHEN [Total] >0 THEN CAST(ifnull([Correct],0)  as float)/[Total] ELSE 0 END) as [Percent] " +
                                        "FROM " +
                                             "(" +
                                                "SELECT [_id]" +
                                                ",[Kanji]" +
                                                ",[Furigana]" +
                                                ",[Definition]  " +
                                            "FROM [Edict] " +
                                            "WHERE [_id] IN (" +
                                                            "SELECT [_id] " +
                                                            "FROM [JFavorites] " +
                                                            "WHERE ([Sys] = ? and [Name] = ?)" +
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
                                                            "FROM [JFavorites] " +
                                                            "WHERE ([Sys] = ? and [Name] = ?))  " +
                                             "GROUP BY [_id]" +
                                             ") as y " +
                                          "ON x.[_id] = y.[_id]"
                    , new String[]{String.valueOf(myListEntry.getListsSys()),myListEntry.getListName(),String.valueOf(myListEntry.getListsSys()),myListEntry.getListName()});
            if(c.getCount()>0) {
                c.moveToFirst();
                while (!c.isAfterLast())

                {
                    WordEntry wordEntry = new WordEntry();
                    wordEntry.setId(c.getInt(0));
                    wordEntry.setKanji(c.getString(1));
                    wordEntry.setFurigana(c.getString(2));
                    wordEntry.setDefinition(c.getString(3));
                    wordEntry.setTotal(c.getInt(5));
                    wordEntry.setPercentage(c.getFloat(6));
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
        SQLiteDatabase db = this.getReadableDatabase();
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


    public int tweetParsedKanjiExistsInDB(SQLiteDatabase db, Tweet tweet) {
        int resultCode = -1;
        try {
            String Query = "Select * from " + TABLE_SAVED_TWEET_KANJI + " where " + TSAVEDTWEETITEMS_COL0 + " = " + tweet.getIdString();
            Cursor cursor = db.rawQuery(Query, null);
            resultCode = cursor.getCount();
            cursor.close();
        } catch (SQLiteException e){
            Log.e(TAG,"tweetParsedKanjiExistsInDB Sqlite exception: " + e);
        } catch (Exception e) {
            Log.e(TAG,"tweetParsedKanjiExistsInDB generic exception: " + e);
        }

        return resultCode;
    }

    //resultCode 0 means doesn't exist, 1means does, -1 is error
    public int tweetExistsInDB(SQLiteDatabase db, Tweet tweet) {
        int resultCode = -1;
        try {
            String Query = "Select * from " + TABLE_SAVED_TWEETS + " where " + TSAVEDTWEET_COL2 + " = " + tweet.getIdString();
            Cursor cursor = db.rawQuery(Query, null);
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
     * Saves a tweet to the database table JSavedTweets. The pkey  (_id) is automatically created, and this
     * will be the primary key that the other tables (tweet urls and tweet favorite lists) hook on to
     * @param userInfo
     * @param tweet
     * @return
     */
    //TODO THIS IS NOT DONE WELL...
//    public int saveNewTweetToDB(UserInfo userInfo,Tweet tweet) {
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        int tweetExists = tweetExistsInDB(db,tweet);
//
//        //IF the tweet doesn't exist, save it
//        if(tweetExists == 0) {
//           int addTweetResultCode = addTweetToDB(db,userInfo,tweet);
//            if(addTweetResultCode == -1) {
//                db.close();
//                return addTweetResultCode;
//            } else {
//                //If the tweet saved correctly,
//               if(saveTweetUrls(db,tweet)) {
//                   db.close();
//
//               }
////               else {
////                   db.close();
//////                   return -2; //Successufl insert, but error on url
////
////               }
//
//                return 2; //THIS MEANS TRY TO DOWNLOAD KANJI
//            }
//
//        } else if(tweetParsedKanjiExistsInDB(db,tweet)==0) {
//
//        } else  {
//            db.close();
//            return tweetExists;
//        }
//
//
//
//    }


    public int addTweetToDB(SQLiteDatabase db, UserInfo userInfo,Tweet tweet){
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

                resultCode = (int)db.insertWithOnConflict(TABLE_SAVED_TWEETS, null, values,SQLiteDatabase.CONFLICT_REPLACE);
                db.close();
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



    public boolean saveTweetUrls(Tweet tweet) {


        SQLiteDatabase db = this.getReadableDatabase();

        try {

            for(TweetUrl tweetUrl : tweet.getEntities().getUrls()) {
                ContentValues values = new ContentValues();
                values.put(TSAVEDTWEETITEMS_COL0, tweet.getIdString());
                values.put(TSAVEDTWEETURLS_COL1, tweetUrl.getExpanded_url());
                values.put(TSAVEDTWEETURLS_COL2, tweetUrl.getIndices()[0]);
                values.put(TSAVEDTWEETURLS_COL3, tweetUrl.getIndices()[1]);
                db.insert(TABLE_SAVED_TWEET_URLS, null, values);
            }

            return true;
        } catch(SQLiteException exception) {
            return false;
        } finally {
            db.close();
        }
    }




    public int saveParsedTweetKanji(ArrayList<ParseSentenceItem> parseSentenceItems, String tweet_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        int resultCode = -1;

        try {
            if(parseSentenceItems.size()==0) {
                resultCode = 1;
            } else {
                for(int i=0;i<parseSentenceItems.size();i++) {
                    if(parseSentenceItems.get(i).isKanji()) {
                        ContentValues values = new ContentValues();
                        values.put(TSAVEDTWEETITEMS_COL0, tweet_id);
                        values.put(TSAVEDTWEETITEMS_COL2, parseSentenceItems.get(i).getKanjiID());
                        values.put(TSAVEDTWEETITEMS_COL3, parseSentenceItems.get(i).getStartIndex());
                        values.put(TSAVEDTWEETITEMS_COL4, parseSentenceItems.get(i).getEndIndex());
                        resultCode = (int)db.insert(TABLE_SAVED_TWEET_KANJI, null, values);
                    }
                }
            }
            db.close();
            return resultCode;

        } catch(SQLiteException exception) {
            return resultCode;
        } finally {
            db.close();
        }

    }




    public HashMap<String,ItemFavorites> getStarFavoriteDataForAUsersTweets(String userId){
        SQLiteDatabase db = this.getReadableDatabase();
        HashMap<String,ItemFavorites> map = new HashMap<>();
        try {
//            Log.d(TAG,"USER ID: " + userId);
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
                            ",(CASE WHEN ([Sys] = 1 and Name = \"Blue\") then 1 else 0 end) as [Blue]" +
                            ",(CASE WHEN ([Sys] = 1 AND Name = \"Red\") then 1 else 0 end) as [Red]" +
                            ",(CASE WHEN ([Sys] = 1 AND Name = \"Green\") then 1 else 0 end) as [Green]" +
                            ",(CASE WHEN ([Sys] = 1  AND Name = \"Yellow\") then 1 else 0 end) as [Yellow]" +
                            ",(CASE WHEN ([Sys] = 1  AND Name = \"Purple\") then 1 else 0 end) as [Purple]" +
                            ",(CASE WHEN ([Sys] = 1  AND Name = \"Orange\") then 1 else 0 end) as [Orange]" +
                            ", (CASE WHEN [Sys] <> 1 THEN 1 else 0 end) as [Other] " +
                            "FROM " + TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                            " WHERE UserId = ? " +
                            ") as [All]" +
                            "Group by [_id] " , new String[]{userId});

                Log.d(TAG,"SAVED ENTRIES COUNT: " + c.getCount());
            if (c.moveToFirst()) {
                do {

                    ItemFavorites itemFavorites = new ItemFavorites(c.getInt(0)
                            ,c.getInt(1)
                            ,c.getInt(2)
                            ,c.getInt(3)
                            ,c.getInt(4)
                            ,c.getInt(5)
                            ,c.getInt(6));

                    map.put(userId,itemFavorites);

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




//    public void prepareExpandableListHeaders(ColorThresholds colorThresholds
//            , ArrayList<String> childOptions
//            , ArrayList<String> activeFavoritesStars) {
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        try {
//        Cursor c = db.rawQuery("SELECT xx.[Name]" +
//                ",xx.[Sys]" +
//                ",ifnull(yy.[Total],0) as [Total]" +
//                ",ifnull(yy.[Grey],0) as [Grey]" +
//                ",ifnull(yy.[Red],0) as [Red]" +
//                ",ifnull(yy.[Yellow],0) as [Yellow]" +
//                ",ifnull(yy.[Green],0) as [Green] " +
//                "" +
//                "FROM (" +
//                "SELECT [Name]" +
//                ",0 as [Sys] " +
//                "From JFavoritesLists " +
//                "UNION " +
//                "SELECT 'Blue' as [Name]" +
//                ", 1 as [Sys] " +
//                "Union " +
//                "SELECT 'Red' as [Name]" +
//                ",1 as [Sys] " +
//                "Union " +
//                "SELECT 'Green' as [Name]" +
//                ",1 as [Sys] " +
//                "Union " +
//                "SELECT 'Yellow' as [Name]" +
//                ",1 as [Sys]" +
//                ") as [xx] " +
//                "LEFT JOIN (" +
//                "SELECT  [Name]" +
//                ",[Sys]" +
//                ",SUM([Grey]) + SUM([Red]) + SUM([Yellow]) + SUM([Green]) as [Total]" +
//                ",SUM([Grey]) as [Grey]" +
//                ",SUM([Red]) as [Red]" +
//                ",SUM([Yellow]) as [Yellow]" +
//                ",SUM([Green]) as [Green] " +
//                "FROM (" +
//                "SELECT [Name]" +
//                ",[Sys]" +
//                ",[_id] " +
//                ",(CASE WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 1 ELSE 0 END) as [Grey] " +
//                ",(CASE WHEN [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] < " + colorThresholds.getRedThreshold() + "  THEN 1  ELSE 0 END) as [Red] " +
//                ",(CASE WHEN [Total] >= " + colorThresholds.getGreyThreshold() + " and ([Percent] >= " + colorThresholds.getRedThreshold() + "  and [Percent] <  " + colorThresholds.getYellowThreshold() + ") THEN 1  ELSE 0 END) as [Yellow] " +
//                ",(CASE WHEN [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] >= " + colorThresholds.getYellowThreshold() + " THEN 1 ELSE 0 END) as [Green] " +
//                "FROM  (" +
//                "SELECT a.[Name]" +
//                ",a.[Sys]" +
//                ",a.[_id]" +
//                ",ifnull(b.[Total],0) as [Total] " +
//                ",ifnull(b.[Correct],0)  as [Correct]" +
//                ",CAST(ifnull(b.[Correct],0)  as float)/b.[Total] as [Percent] " +
//                "FROM (" +
//                "SELECT  DISTINCT [Name]" +
//                ",[Sys]" +
//                ",[_id] " +
//                "FROM JFavorites" +
//                ") as a " +
//                "LEFT JOIN  (" +
//                "SELECT [_id]" +
//                ",sum([Correct]) as [Correct]" +
//                ",sum([Total]) as [Total] FROM [JScoreboard] " +
//                "where [_id] in (SELECT DISTINCT [_id] FROM JFavorites)" +
//                " GROUP BY [_id]" +
//                ") as b " +
//                "ON a.[_id] = b.[_id]) " +
//                " as x) as y " +
//                "GROUP BY [Name],[Sys]" +
//                ") as yy  " +
//                "ON xx.[Name] = yy.[Name] and xx.[sys] = yy.[sys]  " +
//                "Order by xx.[Sys] Desc,xx.[Name]",null);
//
//        c.moveToFirst();
//        if(c.getCount()>0) {
//            while (!c.isAfterLast()) {
//
//                if(BuildConfig.DEBUG){Log.d(TAG,"PULLING NAME: " + c.getString(0) + ", SYS: " + c.getString(1) + ", TOTAL: " + c.getString(2) + ", GREY: " + c.getString(3));}
//                if(BuildConfig.DEBUG){Log.d("yes", "pulling list: " + c.getString(0) + ", sys: " + c.getString(1));}
//
//                /* We do not want to include favorites star lists that are not active in the user
//                * preferences. So if an inactivated list shows up in the sql query, ignore it (don't add to mMenuHeader)*/
//
////                Log.d(TAG,"availableFavoritesStars: " + activeFavoritesStars);
//                if(c.getInt(1) != 1 || (activeFavoritesStars.contains(c.getString(0)))) {
//                    MenuHeader menuHeader = new MenuHeader(c.getString(0));
//                    menuHeader.setChildOptions(childOptions);
//                    menuHeader.setMyList(true);
//
//                    if(c.getInt(1) == 1 ) {
//                        if(BuildConfig.DEBUG){Log.d(TAG,c.getString(0) + " sys ==1 so adding to starlist");}
//                        menuHeader.setSystemList(true);
//                    }
//
//                    ColorBlockMeasurables colorBlockMeasurables = new ColorBlockMeasurables();
//                    colorBlockMeasurables.setGreyCount(c.getInt(3));
//                    colorBlockMeasurables.setRedCount(c.getInt(4));
//                    colorBlockMeasurables.setYellowCount(c.getInt(5));
//                    colorBlockMeasurables.setGreenCount(c.getInt(6));
//
//                    colorBlockMeasurables.setGreyMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getGreyCount())));
//                    colorBlockMeasurables.setRedMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getRedCount())));
//                    colorBlockMeasurables.setYellowMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getYellowCount())));
//                    colorBlockMeasurables.setGreenMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getGreenCount())));
//                    menuHeader.setColorBlockMeasurables(colorBlockMeasurables);
//                    mMenuHeader.add(menuHeader);
//                }
//
//
//                c.moveToNext();
//            }
//        }
//        c.close();
//
//        } catch (SQLiteException e){
//            Log.e(TAG,"copyKanjiToList Sqlite exception: " + e);
//        } finally {
//            db.close();
//        }
//    }
//
//    public int getExpandableAdapterColorBlockBasicWidths(TextView colorBlock, String text, int padding){
//        int result = 0;
//        if(!text.equals("0")) {
////            View view = activity.getLayoutInflater().inflate(R.layout.expandablelistadapter_listitem, null);
////            TextView colorBlock = (TextView) view.findViewById(R.id.listitem_colors_1);
////            Drawable drawablecolorblock1 = ContextCompat.getDrawable(activity, R.drawable.colorblock);
//
////            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
////                colorBlock.setBackground(drawablecolorblock1);
////            } else {
////                colorBlock.setBackgroundDrawable(drawablecolorblock1);
////            }
//
//            Rect bounds = new Rect();
//            Paint textPaint = colorBlock.getPaint();
//            textPaint.getTextBounds(text, 0, text.length(), bounds);
//            result = (int)textPaint.measureText(text);
//
////            DisplayMetrics metrics = new DisplayMetrics();
////            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
////            int padding = (int) (20.0f * metrics.density + 0.5f);
//            result += padding;
//        }
//        return result;
//
//    }



    public ArrayList<Tweet> getSavedTweets(MyListEntry myListEntry , ColorThresholds colorThresholds) {
        ArrayList<Tweet> savedTweets = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        try {


            Cursor c = db.rawQuery("SELECT TweetLists.[Name]" +
                                ",TweetLists.[Sys]" +
                                ",TweetLists.[UserId] " +
                                ",UserName.ScreenName " +
                                ",UserName.UserName " +
                                ",TweetLists.[Tweet_id]" +
                                ",[ALLTweets].[Text] " +
                                ",TweetKanji.Edict_id " +
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

                            "FROM " +
                            "( " +
                            " SELECT Tweet_id" +
                            ",Edict_id " +
                            ",[StartIndex]" +
                            ",[EndIndex]" +
                            "From JSavedTweetKanji " +
                            " WHERE [Edict_id] is not NULL and StartIndex is not NULL and EndIndex is not NULL and EndIndex > StartIndex " +
                            ") as a " +
                            "LEFT JOIN " +
                            " (" +
                            "SELECT [_id] as [Edict_id]" +
                            ",sum([Correct]) as [Correct]" +
                            ",sum([Total]) as [Total] FROM [JScoreboard] " +
                            "where [_id] in (SELECT DISTINCT [Edict_id] FROM JSavedTweetKanji)" +
                            " GROUP BY [_id]" +
                            ") as b " +
                            "ON a.[Edict_id] = b.[Edict_id] " +
                    " ) as TweetKanji " +
                            "On TweetLists.Tweet_id = TweetKanji.Tweet_id " +


                            "LEFT JOIN " +
                            " (" +
                            "SELECT DISTINCT [UserId] " +
                            ", [ScreenName] " +
                            ", [UserName] " +
                            " FROM " + TABLE_MAIN +" " +
                            ") as [UserName] " +
                            "On TweetLists.UserId = UserName.UserId " +

                    "Order by date(ALLTweets.Date) Desc,TweetLists.[Tweet_id] asc,TweetKanji.StartIndex asc"
                    , new String[]{myListEntry.getListName(),String.valueOf(myListEntry.getListsSys())});

            Log.d("XXX","count: " + c.getCount() );
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
                    if(!c.isFirst() && !c.isLast()) {

                        Log.d("XXX","KANJI COLOR!: " + c.getInt(7) + ", " + c.getInt(8));
                        TweetKanjiColor tweetKanjiColor = new TweetKanjiColor(c.getInt(7)
                                ,c.getString(8)
                                ,c.getInt(9)
                                ,c.getInt(10));
                        tweet.addColorIndex(tweetKanjiColor);
                    }

                    if(!currentTweetId.equals(c.getString(5)) || c.isLast()){
                        //FLush old tweet
                        Log.d("XXX","saving tweet: " + tweet.getIdString() );
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





                    c.moveToNext();
//                        addedIdStrings.add(c.getString(5));
                    }


                } else {if(debug) {Log.d(TAG,"c.getcount was 0!!");}}
                c.close();

        } catch (SQLiteException e){
            Log.e(TAG,"getSavedTweets Sqlite exception: " + e);
        } catch (Exception e) {
            Log.e(TAG,"getSavedTweets generic exception: " + e);
        } finally {
            db.close();
        }
        return savedTweets;
    }


}