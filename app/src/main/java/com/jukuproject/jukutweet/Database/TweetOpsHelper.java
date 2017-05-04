package com.jukuproject.jukutweet.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Fragments.TweetListBrowseFragment;
import com.jukuproject.jukutweet.Interfaces.TweetListOperationsInterface;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetUrl;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Collection of internal sqlite database calls related to Tweet operations.
 *
 * @see com.jukuproject.jukutweet.MainActivity
 * @see com.jukuproject.jukutweet.Fragments.TweetListFragment
 * @see TweetListBrowseFragment
 *
 */
public class TweetOpsHelper implements TweetListOperationsInterface {
    private SQLiteOpenHelper sqlOpener;
    private static String TAG = "TEST-tweetops";

    public TweetOpsHelper(SQLiteOpenHelper sqlOpener) {
        this.sqlOpener = sqlOpener;
    }

    /**
     * Checks for duplicate entries for a user-created saved tweetslist in the JFavoritesTweetsLists table
     * @param listName prospective new user-created mylist
     * @return true if that list name already exists, false if not
     */
    public boolean duplicateTweetList(String listName) {

        /** Before inserting record, check to see if feed already exists */
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
        String queryRecordExists = "Select Name From " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS + " where " + InternalDB.Columns.TFAVORITES_COL0 + " = ?" ;
        Cursor c = db.rawQuery(queryRecordExists, new String[]{listName});
        try {
            if (c.moveToFirst()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLiteException e) {
            Log.e(TAG,"duplicateTweetList Sqlite exception: " + e);
        } finally {
            c.close();
            db.close();
        }
        return false;
    }
    /**
     * Adds a new mylist to the JFavoriteListTweets table in the db
     * @param listName list name to add
     * @return boolean true if success, false if not
     */
    public boolean saveTweetList(String listName) {
        try {
            SQLiteDatabase db = sqlOpener.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(InternalDB.Columns.TFAVORITES_COL0, listName.trim());
            db.insert(InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS, null, values);
            db.close();
            return true;
        } catch(SQLiteException exception) {
            Log.e(TAG,"savemylist db insert exception: " + exception);
            return false;
        }
    }

    /**
     * Erases kanji from a tweetlist (deleting their associated rows from JFavorites table)
     * The list itself is maintained in the JFavoritesLists table, and is untouched
     * @param listName list to clear
     * @param isStarFavorite boolean, true if the list is a "system list", meaning it is a colored star favorites list.
     *                       System have a 1 in the "system" column of JFavorites, and this must be accounted for in the delete statement.
     * @return boolean true if successful, false if not
     *
     * @see com.jukuproject.jukutweet.Dialogs.EditMyListDialog
     * @see com.jukuproject.jukutweet.MainActivity#onEditMyListDialogPositiveClick(String listType, int, String, boolean)
     * @see com.jukuproject.jukutweet.MainActivity#deleteOrClearDialogFinal(String listType, Boolean, String, boolean)
     */
    public boolean clearTweetList(String listName, boolean isStarFavorite) {
        try{
            SQLiteDatabase db = sqlOpener.getWritableDatabase();
            if(isStarFavorite) {
                db.delete(InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES, InternalDB.Columns.TFAVORITES_COL0 + "= ? and " + InternalDB.Columns.TFAVORITES_COL1 + "= 1", new String[]{listName});
            } else {
                db.delete(InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES, InternalDB.Columns.TFAVORITES_COL0 + "= ? and " + InternalDB.Columns.TFAVORITES_COL1 + "= 0", new String[]{listName});
            }
            db.close();
            return true;
        } catch(SQLiteException exception) {
            return false;
        }

    }

    /**
     * Removes a list from the JFavoritesTweetLists table and all associated rows from JFavoritesTweet Table
     *
     * @param listName name of list to remove
     * @return boolean true if successful, false if not
     *
     * @see com.jukuproject.jukutweet.Dialogs.EditMyListDialog
     * @see com.jukuproject.jukutweet.MainActivity#onEditMyListDialogPositiveClick(String listType, int, String, boolean)
     * @see com.jukuproject.jukutweet.MainActivity#deleteOrClearDialogFinal(String listType, Boolean, String, boolean)
     */
    public boolean deleteTweetList(String listName) {
        try{
            SQLiteDatabase db = sqlOpener.getWritableDatabase();
            db.delete(InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS, InternalDB.Columns.TFAVORITES_COL0 + "= ?", new String[]{listName});
            db.delete(InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES, InternalDB.Columns.TFAVORITES_COL0 + "= ? and " + InternalDB.Columns.TFAVORITES_COL1 + "= 0", new String[]{listName});
            db.close();
            return true;
        } catch(SQLiteException exception) {

            return false;
        }
    }

    /**
     * Changes the name of a user-created list
     * @param oldListName current list name in JFavoritesLists and JFavorites tables
     * @param newListName new list name
     * @return boolea true if successful, false if not
     *
     * @see com.jukuproject.jukutweet.Dialogs.EditMyListDialog
     * @see com.jukuproject.jukutweet.MainActivity#onEditMyListDialogPositiveClick(String listType, int, String, boolean)
     * @see com.jukuproject.jukutweet.MainActivity#showRenameMyListDialog(String listType, String)
     */
    public boolean renameTweetList(String oldListName, String newListName) {
        try{
            SQLiteDatabase db = sqlOpener.getWritableDatabase();

            String sql = "Update " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS + "  SET [Name]=? WHERE [Name]=? ";
            SQLiteStatement statement = db.compileStatement(sql);
            statement.bindString(1, newListName);
            statement.bindString(2, oldListName);
            statement.executeUpdateDelete();

            String sql2 = "Update " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " SET [Name]=? WHERE [Name]=? and [Sys] = 0";
            SQLiteStatement statement2 = db.compileStatement(sql2);
            statement2.bindString(1, newListName);
            statement2.bindString(2, oldListName);
            statement2.executeUpdateDelete();
            return true;
        } catch(SQLiteException exception) {
            return false;
        }
    }



    /**
     * Changes the system Tweet List that a tweet is associated with (when the user clicks on the favorites star next to
     * the tweet). Toggles from Black-> through various available colors -> black again.
     * @param tweetId id of tweet in question
     * @param userId id of user who created the tweet
     * @param originalColor current color of the favorites star for that kanji
     * @param updatedColor new color (i.e. new system list) that the kanji will be associated with
     * @return bool true if the cupdate was succesful, false if not
     */
    public boolean changeTweetListStarColor(String tweetId, String userId, String originalColor, String updatedColor) {
        SQLiteDatabase db = sqlOpener.getWritableDatabase();

        try {
            if(originalColor.equals("Black") && updatedColor.equals("Black")) {
                Log.e(TAG,"ERROR! changefavoritelistentry Both entries are BLACK. So do nothing.");
                return false;
            } else if(originalColor.equals("Black")) {
                //Insert statement only
                return addTweetToTweetList(tweetId,userId,updatedColor,1);
            } else if(updatedColor.equals("Black")) {
                //Delete statement only
                return removeTweetFromTweetList(tweetId,originalColor,1);
            } else {
                String sql = "Update " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " SET "+ InternalDB.Columns.TFAVORITES_COL0 +" = ? WHERE "+ InternalDB.Columns.TFAVORITES_COL0 +"= ? and " + InternalDB.Columns.TFAVORITES_COL1+" = 1 and " + InternalDB.Columns.COL_ID + " = ?";
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


    /**
     * Adds a tweet to the tweet list table
     * @param tweetId id of tweet
     * @param userId id of user who created the tweet
     * @param listName name of list
     * @param listSys int designating the list as a system list (1) or a user-created list (0)
     * @return bool true if successful insert, false if not
     */
    public boolean addTweetToTweetList(String tweetId, String userId, String listName, int listSys) {
        SQLiteDatabase db = sqlOpener.getReadableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(InternalDB.Columns.COL_ID, tweetId);
            values.put(InternalDB.Columns.TSAVEDTWEET_COL0, userId);
            values.put(InternalDB.Columns.TFAVORITES_COL0, listName);
            values.put(InternalDB.Columns.TFAVORITES_COL1, listSys);
            db.insert(InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES, null, values);
            return true;

        } catch (SQLiteException e) {
            Log.e(TAG, "addWordToWordList sqlite exception: " + e);
            return false;

        } catch (NullPointerException e) {
            Log.e(TAG, "addWordToWordList something was null: " + e);
            return false;
        } finally {
            db.close();
        }

    }


    /**
     * Removes a tweet from a saved tweets list (Note: tweet is not deleted, only the its association with a given favorites list)
     * @param tweetId id of tweet
     * @param listName name of list
     * @param listSys int designating the list as a system list (1) or a user-created list (0)
     * @return bool true if successful delete, false if not
     */
    public boolean removeTweetFromTweetList(String tweetId, String listName, int listSys) {
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
        try {
            db.delete(InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES,
                    InternalDB.Columns.TFAVORITES_COL0 + " = ? and "  + InternalDB.Columns.TFAVORITES_COL1 + " = ? and " + InternalDB.Columns.COL_ID + " = ? ", new String[]{listName,String.valueOf(listSys),tweetId});
//            Log.i(TAG, "ABOUT TO DELETE SINGLE TWEETS");
            deleteTweetIfNecessary(db,tweetId);
//            Log.i(TAG, "DELETE SUCCESSFUL");
            return true;
        } catch (SQLiteException e) {
            Log.e(TAG, "removeTweetFromTweetList sqlite exception: " + e);
        } catch (NullPointerException e) {
            Log.e(TAG, "removeTweetFromTweetList something was null: " + e);
        } finally {
            db.close();
        }
        return  false;
    }


    /**
     * Removes multiple tweets from a tweet list
     * @param concatenatedTweetIds a string of tweetIds concatenated together with commas (ex: 123,451,6532,23)
     * @param myListEntry MyList Object containing the name and sys variables for a WordList
     *
     * @see TweetListBrowseFragment
     * @return bool true if successful delete, false if error
     */
    public boolean removeMultipleTweetsFromTweetList(String concatenatedTweetIds, MyListEntry myListEntry) {
        SQLiteDatabase db = sqlOpener.getWritableDatabase();

        try {

//            db.execSQL("DELETE FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " WHERE " + InternalDB.Columns.TFAVORITES_COL0 + " = ? and "  + InternalDB.Columns.TFAVORITES_COL1 + " = ? and " + InternalDB.Columns.COL_ID + " in (" + concatenatedTweetIds + ")",new String[]{myListEntry.getListName(),String.valueOf(myListEntry.getListsSys())});
            db.delete(InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES ,InternalDB.Columns.TFAVORITES_COL0 + " = ? and "  + InternalDB.Columns.TFAVORITES_COL1 + " = ? and " + InternalDB.Columns.COL_ID + " in (" + concatenatedTweetIds + ")",new String[]{myListEntry.getListName(),String.valueOf(myListEntry.getListsSys())});
//            Log.i(TAG, "ABOUT TO DELETE MULT TWEETS");
            deleteTweetIfNecessary(db,concatenatedTweetIds);
//            Log.i(TAG, "DELETE SUCCESSFUL ");

            return true;
        } catch (SQLiteException e) {
            Log.e(TAG, "removeMultipleWordsFromWordList sqlite exception: " + e);
        } catch (NullPointerException e) {
            Log.e(TAG, "removeMultipleWordsFromWordList something was null: " + e);
        } finally {
            db.close();
        }
        return  true;
    }

    /**
     * Removes Tweets, not just from a single TweetList, but from all associated lists. Deletes the Tweet. This
     * is called when a Tweet is deleted in the {@link TweetListBrowseFragment} for a Single User
     *
     * @param concatenatedTweetIds a string of tweetIds concatenated together with commas (ex: 123,451,6532,23)
     * @return bool true if succesful delete, false if not
     *
     * @see TweetListBrowseFragment
     * @see com.jukuproject.jukutweet.Fragments.TweetListSingleUserFragment
     */
    public ArrayList<Pair<MyListEntry,Tweet>> removeTweetsFromAllTweetLists(String concatenatedTweetIds) {
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
        ArrayList<Pair<MyListEntry,Tweet>> undoArray = new ArrayList<>();
        try {

            Cursor c = db.rawQuery("SELECT DISTINCT _id as  Tweet_id " +
                    ",[Name]" +
                    ",[Sys]" +
                    ",[UserId]" +
                    "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                    "WHERE _id in (" + concatenatedTweetIds + ")",null);

            if(c.getCount()>0) {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    undoArray.add(new Pair<MyListEntry, Tweet>(new MyListEntry(c.getString(1),c.getInt(2)),new Tweet(c.getString(0),c.getString(3))));
                    c.moveToNext();
                }
            }
            c.close();


            db.delete(InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES ,InternalDB.Columns.COL_ID + " in (" + concatenatedTweetIds + ")",null);

        } catch (SQLiteException e) {
            Log.e(TAG, "removeTweetsFromAllTweetLists sqlite exception: " + e);
        } catch (NullPointerException e) {
            Log.e(TAG, "removeTweetsFromAllTweetLists something was null: " + e);
        } finally {
            db.close();
        }
            return undoArray;
    }

    /**
     * Deletes Tweet entries and Saved Kanji for a Tweet from the database
     * @param db Sqlite db connection
     * @param concatenatedTweetIds a string of tweetIds concatenated together with commas (ex: 123,451,6532,23)
     */
    public void deleteTweetIfNecessary(SQLiteDatabase db, String concatenatedTweetIds) {
        db.delete(InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES," _id in ( " +
                "SELECT x.Tweet_id " +
                "FROM " +
                "(" +
                "Select Tweet_id "    +
                " FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS +
                " WHERE " + InternalDB.Columns.TSAVEDTWEET_COL2 + " in (" + concatenatedTweetIds + ")" +
                " ) as x " +
                " LEFT JOIN " +
                "(" +
                "Select _id as Tweet_id "    +
                " FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES +
                " WHERE " + InternalDB.Columns.COL_ID + " in (" + concatenatedTweetIds + ")" +
                " ) as y " +
                " ON x.Tweet_id = y.Tweet_id " +
                " WHERE y.Tweet_id is NULL " +
                " ) ",null);

        db.delete(InternalDB.Tables.TABLE_SAVED_TWEET_KANJI," Tweet_id in ( " +
                "SELECT x.Tweet_id " +
                "FROM " +
                "(" +
                "Select Tweet_id "    +
                " FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS +
                " WHERE " + InternalDB.Columns.TSAVEDTWEET_COL2 + " in (" + concatenatedTweetIds + ")" +
                " ) as x " +
                " LEFT JOIN " +
                "(" +
                "Select _id as Tweet_id "    +
                " FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES +
                " WHERE " + InternalDB.Columns.COL_ID + " in (" + concatenatedTweetIds + ")" +
                " ) as y " +
                " ON x.Tweet_id = y.Tweet_id " +
                " WHERE y.Tweet_id is NULL " +
                " ) ",null);
    };

    /**
     * Deletes Tweet entries and Saved Kanji for a Tweet from the database
     * @param concatenatedTweetIds a string of tweetIds concatenated together with commas (ex: 123,451,6532,23)
     */
    public void deleteTweetIfNecessary(String concatenatedTweetIds) {
        SQLiteDatabase db = sqlOpener.getWritableDatabase();

        try {
        db.delete(InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES," _id in ( " +
                "SELECT Tweet_id " +
                "FROM " +
                "(" +
                "Select Tweet_id "    +
                " FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS +
                " WHERE " + InternalDB.Columns.TSAVEDTWEET_COL2 + " in (" + concatenatedTweetIds + ")" +
                " ) as x " +
                " LEFT JOIN " +
                "(" +
                "Select _id as Tweet_id "    +
                " FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES +
                " WHERE " + InternalDB.Columns.COL_ID + " in (" + concatenatedTweetIds + ")" +
                " ) as y " +
                " ON x.Tweet_id = y.Tweet_id " +
                " WHERE y.Tweet_id is NULL " +
                " ) ",null);

        db.delete(InternalDB.Tables.TABLE_SAVED_TWEET_KANJI," Tweet_id in ( " +
                "SELECT Tweet_id " +
                "FROM " +
                "(" +
                "Select Tweet_id "    +
                " FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS +
                " WHERE " + InternalDB.Columns.TSAVEDTWEET_COL2 + " in (" + concatenatedTweetIds + ")" +
                " ) as x " +
                " LEFT JOIN " +
                "(" +
                "Select _id as Tweet_id "    +
                " FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES +
                " WHERE " + InternalDB.Columns.COL_ID + " in (" + concatenatedTweetIds + ")" +
                " ) as y " +
                " ON x.Tweet_id = y.Tweet_id " +
                " WHERE y.Tweet_id is NULL " +
                " ) ",null);

    } catch (SQLiteException e) {
        Log.e(TAG, "deleteTweetIfNecessary sqlite exception: " + e);
    } catch (NullPointerException e) {
        Log.e(TAG, "deleteTweetIfNecessary something was null: " + e);
    } finally {
        db.close();
    }
    };



    /**
     * Pulls full list of available tweet lists, as well as how many of those lists contain the given tweets. Used when
     * creating the copy/move dialog in browse tweets fragment
     * @param activeFavoriteStars List of system favorites lists that are activated (in user preferences)
     * @param concatenatedTweetIds a string of tweetIds concatenated together with commas (ex: 123,451,6532,23)
     * @param entryToExclude When the copy/move dialog is created, it is because the user is in a certain list (lets say List A), and wants to copy
     *                       or move tweets from that list to other lists (List B,C,D etC). So the list of available options to move/copy to
     *                       should not include the current list (List A). It is excluded via this parameter.
     * @return List of MyListEntry objects, one for each tweet list
     *
     * @see com.jukuproject.jukutweet.Dialogs.CopySavedTweetsDialog
     */
    public ArrayList<MyListEntry> getTweetListsForTweet(ArrayList<String> activeFavoriteStars
            , String concatenatedTweetIds
            , @Nullable MyListEntry entryToExclude) {

        ArrayList<MyListEntry> myListEntries = new ArrayList<>();
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
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
                    "From " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS + " "    +
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
                    "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " "    +
                    " Where _id in (" +  concatenatedTweetIds + ") " +
                    " Group by Name, Sys " +
                    ") as UserEntries "    +
                    " "    +
                    "ON Lists.Name = UserEntries.Name ", null);


            if(c.getCount()>0) {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    //Add all user lists (where sys == 0)
                    //Only add system lists (sys==1), but only if the system lists are actived in user preferences
                    if((c.getInt(1) == 0 || activeFavoriteStars.contains(c.getString(0)))
                            && (entryToExclude== null || !(entryToExclude.getListName().equals(c.getString(0)) && entryToExclude.getListsSys() == c.getInt(1)))) {
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
     *  Retrieves a list of word entries (and corresponding edict dictionary info) for words in tweets saved in a particular tweet list.
     *  Words are also filtered by word "color" (a grading of the words quiz score).
     * @param myListEntry Object containing information about a given favorites list (ssentially a container for a listname and sys variables).
     * @param colorThresholds Collection of thresholds which together determine what color to assign to a word/tweet based on its quiz scores
     * @param colorString concatenated (with commas) string of colors to include in the list (ex: 'Grey','Red','Green')
     * @return list of words from the word list filtered by color
     */
    public ArrayList<WordEntry> getWordsFromATweetList(MyListEntry myListEntry
            , ColorThresholds colorThresholds
            , String colorString
            , @Nullable Integer excludeIdInteger
            , @Nullable Integer resultLimit) {


        if(excludeIdInteger ==null) {
            excludeIdInteger = -1;
        }

        String limit;
        if(resultLimit == null) {
            limit = "";
        } else {
            limit = "ORDER BY RANDOM() LIMIT " + String.valueOf(resultLimit);
        }

        ArrayList<WordEntry> wordEntries = new ArrayList<>();
        SQLiteDatabase db = sqlOpener.getReadableDatabase();
        try {
            Cursor c;

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
                            "SELECT DISTINCT _id as  Tweet_id " +
                            "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                            " WHERE [Name] = ? and [Sys] = " + myListEntry.getListsSys() + " and [_id] <> "  + excludeIdInteger + " " +
                            ") as x " +
                            "LEFT JOIN "  +
                            "( " +
                            "SELECT DISTINCT Tweet_id,Edict_id " +
                            "FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI + " " +
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
                            "SELECT DISTINCT _id as Tweet_id " +
                            "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                            " WHERE [Name] = ? and [Sys] = " + myListEntry.getListsSys() + " and [_id] <> "  + excludeIdInteger + " " +
                            ") as x " +
                            "LEFT JOIN "  +
                            "( " +
                            "SELECT DISTINCT Tweet_id,Edict_id " +
                            "FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI + " " +
                            ")  as y " +
                            " ON x.Tweet_id = y.Tweet_id " +

                            ") " +
                            "GROUP BY [_id]" +
                            ") as y " +
                            "ON x.[_id] = y.[_id] " +
                            ") " +
                            "WHERE [Color] in (" +  colorString + ") "  +
                            " " + limit + " "
                    , new String[]{myListEntry.getListName(),myListEntry.getListName()});

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
                if(BuildConfig.DEBUG) {Log.d(TAG,"c.getcount was 0!!");}
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

    /**
     * Returns a list of WordEntries pulled from a User's saved tweets. Used in compiling dataset for
     * Quizzes and flashcards for the single user's saved tweets {@link com.jukuproject.jukutweet.Fragments.TweetListSingleUserFragment}
     * @param userInfo UserInfo object for the user whose tweets will be browsed/quizzed on
     * @param colorThresholds Thresholds designating when a Kanji, or a tweet should be considered part of a color category (Grey, Red, Yellow, Green),
     *                        based on the quiz score of a kanji (or kanjis within a tweet)
     * @param colorString concatenated (with commas) string of colors to include in the list (ex: 'Grey','Red','Green')
     * @param excludeIdInteger Edict id to exclude from the results
     * @param resultLimit Maximum number of results
     * @return Array of WordEntries for the words contained in a user's Tweets
     *
     * @see com.jukuproject.jukutweet.Fragments.FlashCardsFragment
     * @see com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment
     */
    public ArrayList<WordEntry> getWordsFromAUsersSavedTweets(UserInfo userInfo
            , ColorThresholds colorThresholds
            , String colorString
            , @Nullable Integer excludeIdInteger
            , @Nullable Integer resultLimit) {


        if(excludeIdInteger ==null) {
            excludeIdInteger = -1;
        }

        String limit;
        if(resultLimit == null) {
            limit = "";
        } else {
            limit = "ORDER BY RANDOM() LIMIT " + String.valueOf(resultLimit);
        }

        ArrayList<WordEntry> wordEntries = new ArrayList<>();
        SQLiteDatabase db = sqlOpener.getReadableDatabase();
        try {
            Cursor c = db.rawQuery("Select [_id]" +
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
                            " WHERE [_id] <> "  + excludeIdInteger + " " +
                    "and [_id] IN (" +

                            "SELECT DISTINCT Edict_id as [_id] " +
                            "FROM "+
                            "( " +

                "SELECT  DISTINCT  _id as [Tweet_id]" +
                "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                "WHERE [UserId] = ? " +

                            ") as x " +
                            "LEFT JOIN "  +
                            "( " +
                            "SELECT DISTINCT Tweet_id,Edict_id " +
                            "FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI + " " +
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


                "SELECT  DISTINCT  _id as [Tweet_id]" +
                "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                "WHERE [UserId] = ? " +


                ") as x " +
                            "LEFT JOIN "  +
                            "( " +
                            "SELECT DISTINCT Tweet_id,Edict_id " +
                            "FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI + " " +
                            ")  as y " +
                            " ON x.Tweet_id = y.Tweet_id " +

                            ") " +
                            "GROUP BY [_id]" +
                            ") as y " +
                            "ON x.[_id] = y.[_id] " +
                            ") " +


                            "WHERE [Color] in (" +  colorString + ") "  +
                            " " + limit + " "
                    , new String[]{userInfo.getUserId(),userInfo.getUserId()});

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
                if(BuildConfig.DEBUG) {Log.d(TAG,"c.getcount was 0!!");}
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

    /**
     * Inserts multiple tweets into a given tweet list
     * @param myListEntry MyList Object containing the name and sys variables for a tweetList
     * @param concatenatedTweetIds a string of tweetIds concatenated together with commas (ex: 123,451,6532,23)
     * @return bool true if successful insert, false if error
     */
    public boolean addMultipleTweetsToTweetList(MyListEntry myListEntry, String concatenatedTweetIds) {
        SQLiteDatabase db = sqlOpener.getReadableDatabase();
        try {
            db.execSQL("INSERT OR REPLACE INTO " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES +" SELECT DISTINCT Tweet_id as [_id], [UserId], ? as [Name], ? as [Sys] FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS  + " WHERE Tweet_id in (" + concatenatedTweetIds + ")",new String[]{myListEntry.getListName(),String.valueOf(myListEntry.getListsSys())});
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
     * which case the {@link #saveParsedTweetKanji(ArrayList, String)} part would occur later, when the tweet is clicked in {@link TweetListBrowseFragment}
     * @param tweet Tweet in question
     * @return resultvalue: -1 for error, otherwise the count of instance of the tweet in the database
     */
    public int tweetParsedKanjiExistsInDB(Tweet tweet) {
        int resultCode = -1;
        try {
            String Query = "Select * from " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI + " where " + InternalDB.Columns.TSAVEDTWEET_COL2 + " = " + tweet.getIdString();
            Cursor cursor = sqlOpener.getWritableDatabase().rawQuery(Query, null);
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
            String Query = "Select * from " + InternalDB.Tables.TABLE_SAVED_TWEETS + " where " + InternalDB.Columns.TSAVEDTWEET_COL2 + " = " + tweet.getIdString();
            Cursor cursor = sqlOpener.getWritableDatabase().rawQuery(Query, null);
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
                    values.put(InternalDB.Columns.TSAVEDTWEET_COL0, userInfo.getUserId());
                }
                if(userInfo.getScreenName() != null) {
                    values.put(InternalDB.Columns.TSAVEDTWEET_COL1, userInfo.getScreenName().trim());
                }
                if(tweet.getIdString() != null) {
                    values.put(InternalDB.Columns.TSAVEDTWEET_COL2, tweet.getIdString());
                }

                if(tweet.getDatabaseInsertDate() != null) {
                    values.put(InternalDB.Columns.TSAVEDTWEET_COL3, tweet.getDatabaseInsertDate().trim());
                } else {
                    Log.e(TAG,"DB INSERT DATE IS NUL:? " + tweet.getDatabaseInsertDate());
                }

                values.put(InternalDB.Columns.TSAVEDTWEET_COL4, tweet.getText().trim());

                resultCode = (int)sqlOpener.getWritableDatabase().insertWithOnConflict(InternalDB.Tables.TABLE_SAVED_TWEETS, null, values,SQLiteDatabase.CONFLICT_REPLACE);

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
        SQLiteDatabase db = sqlOpener.getReadableDatabase();

        try {

            for(TweetUrl tweetUrl : tweet.getEntities().getUrls()) {
                ContentValues values = new ContentValues();
                values.put(InternalDB.Columns.TSAVEDTWEET_COL2, tweet.getIdString());
                values.put(InternalDB.Columns.TSAVEDTWEETURLS_COL1, tweetUrl.getExpanded_url());
                values.put(InternalDB.Columns.TSAVEDTWEETURLS_COL2, tweetUrl.getIndices()[0]);
                values.put(InternalDB.Columns.TSAVEDTWEETURLS_COL3, tweetUrl.getIndices()[1]);
                resultCode = (int)db.insert(InternalDB.Tables.TABLE_SAVED_TWEET_URLS, null, values);
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
        SQLiteDatabase db = sqlOpener.getReadableDatabase();
        int resultCode = -1;
        try {

            //Remove saved kanji for the tweet, if some already exists
            db.delete(InternalDB.Tables.TABLE_SAVED_TWEET_KANJI ,InternalDB.Columns.TSAVEDTWEET_COL2 + " = ?" ,new String[]{tweet_id});

            //Add word entries for the tweet to the saved kanji table
            if(wordEntries.size()>0) {
                for(int i=0;i<wordEntries.size();i++) {
                    Log.d(TAG,"SAVING TWEET: " + wordEntries.get(i).getKanji() + ", core: "
                            + wordEntries.get(i).getCoreKanjiBlock()
                            + ", " + wordEntries.get(i).getStartIndex() + " - " + wordEntries.get(i).getEndIndex());
                    ContentValues values = new ContentValues();
                    values.put(InternalDB.Columns.TSAVEDTWEET_COL2, tweet_id);
                    values.put(InternalDB.Columns.TSAVEDTWEETITEMS_COL2, wordEntries.get(i).getId());
                    values.put(InternalDB.Columns.TSAVEDTWEETITEMS_COL5, wordEntries.get(i).getCoreKanjiBlock());
                    values.put(InternalDB.Columns.TSAVEDTWEETITEMS_COL3, wordEntries.get(i).getStartIndex());
                    values.put(InternalDB.Columns.TSAVEDTWEETITEMS_COL4, wordEntries.get(i).getEndIndex());
                    resultCode = (int)db.insert(InternalDB.Tables.TABLE_SAVED_TWEET_KANJI, null, values);
                }
            } else {
                Log.d(TAG,"Word entries 0....");
            }
            return resultCode;
        } catch(SQLiteException exception) {
            Log.e(TAG,"Error saving tweets");
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
        SQLiteDatabase db = sqlOpener.getReadableDatabase();
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
                            "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                            " WHERE UserId in (" + userId + ") " +
                            ") as [All]" +
                            "Group by [_id] " , null);

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
     * Retrives all the saved tweets in a Saved Tweets List. Used in {@link TweetListBrowseFragment}
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
    public ArrayList<Tweet> getTweetsForSavedTweetsList(MyListEntry myListEntry, ColorThresholds colorThresholds) {
        ArrayList<Tweet> savedTweets = new ArrayList<>();
        SQLiteDatabase db = sqlOpener.getReadableDatabase();
        try {
            Cursor c = db.rawQuery("SELECT TweetLists.[UserId] " +
                            ",(CASE WHEN UserName.ScreenName is null THEN ALLTweets.UserScreenName ELSE UserName.ScreenName end) as [ScreenName] " +
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
                            ",[ALLTweets].[Date] " +

                            "FROM  " +
                            " ( " +
                                                    "SELECT DISTINCT UserId " +
                                                        ", _id as Tweet_id " +
                                                            "FROM "+ InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                                                            "WHERE Tweet_id is not Null and [Name] = ? and [Sys] = " + myListEntry.getListsSys() +  " " +
                            ") as TweetLists " +
                            " LEFT JOIN " +
                            " ( " +
                            "SELECT  DISTINCT [_id] " +
                            ",[Tweet_id]" +
                            ",[UserScreenName] " +
                            ",[Text]" +
                            ",[CreatedAt]  as [Date] " +
                            " FROM "+ InternalDB.Tables.TABLE_SAVED_TWEETS + " " +
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

                            "FROM " +
                            "( " +
                            " SELECT Tweet_id" +
                            ",Edict_id " +
                            ",[StartIndex]" +
                            ",[EndIndex]" +
                            "From " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
                            " WHERE [Edict_id] is not NULL and StartIndex is not NULL and EndIndex is not NULL and EndIndex > StartIndex " +
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
                            "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + ")" +
                            ") as c " +
                            "ON a.[Edict_id] = c.[Edict_id] " +

                            " ) as TweetKanji " +
                            "On TweetLists.Tweet_id = TweetKanji.Tweet_id " +


                            "LEFT JOIN " +
                            " (" +
                            "SELECT DISTINCT [UserId] " +
                            ", [ScreenName] " +
                            ", [UserName] " +
                            " FROM " + InternalDB.Tables.TABLE_USERS +" " +
                            ") as [UserName] " +
                            "On TweetLists.UserId = UserName.UserId " +
                            " WHERE TweetLists.[Tweet_id] is not NULL and [ALLTweets].[Text] is not NULL and Length([ALLTweets].[Text])>0 " +
                            "Order by date(ALLTweets.Date) Desc,TweetLists.[Tweet_id] asc,TweetKanji.StartIndex asc"
                    , new String[]{myListEntry.getListName()});

            /* The query pulls a list of tweetdata paired with each parsed-kanji in the tweet, resulting in
            * multiple duplicate lines of tweetdata. So the cursor ony adds tweet data once, when a new tweetid is found. Meanwhile
            * the kanji data for each row is added to a TweetKanjiColor object, which is then added to the kanji and the kanji
            * added to the final "savedTweets" list when a new tweetid appears (or the cursor finishes) */
            if(c.getCount()>0) {
                c.moveToFirst();
                String currentTweetId = c.getString(3);
                Tweet tweet = new Tweet();
                while (!c.isAfterLast())
                {
                    if(BuildConfig.DEBUG) {
                        Log.i(TAG,"tweetId: " + c.getString(3) + ", created: " + c.getString(14)
                                + ", userId: "  + c.getString(0) +", name: " + c.getString(1) +", KANJI: " + c.getString(6));
                    }
                    if(c.isFirst()) {
                        tweet.setIdString(c.getString(3));
                        tweet.setCreatedAt(c.getString(14));
                        tweet.getUser().setUserId(c.getString(0));
                        tweet.setText(c.getString(4));
                        tweet.getUser().setScreen_name(c.getString(1));
                        tweet.getUser().setName(c.getString(2));

                    }

                    //FLush old tweet
                     else if(!currentTweetId.equals(c.getString(3))){
                        tweet.assignTweetColorToTweet(colorThresholds);
                        savedTweets.add(new Tweet(tweet));
                        currentTweetId = c.getString(3);
                        tweet = new Tweet();

                        tweet.setIdString(c.getString(3));
                        tweet.setCreatedAt(c.getString(14));
                        tweet.getUser().setUserId(c.getString(0));
                        tweet.setText(c.getString(4));
                        tweet.getUser().setScreen_name(c.getString(1));
                        tweet.getUser().setName(c.getString(2));
                    }

                    if(c.getString(5) != null && c.getString(6) != null) {
                        WordEntry wordEntry = new WordEntry(c.getInt(5)
                                ,c.getString(6)
                                ,c.getString(7)
                                ,c.getString(8)
                                ,c.getInt(9)
                                ,c.getInt(10)
                                ,c.getString(11)
                                ,c.getInt(12)
                                ,c.getInt(13));

                        tweet.addWordEntry(wordEntry);

                    }



                    if(c.isLast()) {
                        tweet.assignTweetColorToTweet(colorThresholds);
                        savedTweets.add(new Tweet(tweet));
                    }
                    c.moveToNext();
                }


            } else {if(BuildConfig.DEBUG) {Log.d(TAG,"c.getcount was 0!!");}}
            c.close();

        } catch (SQLiteException e){
            Log.e(TAG,"getTweetsForSavedTweetsList (singleuser) Sqlite exception: " + e);
        } catch (Exception e) {
            Log.e(TAG,"getTweetsForSavedTweetsList(singleuser) generic exception: " + e);
        } finally {
            db.close();
        }
//        testcase(myListEntry);
        return savedTweets;
    }
//

//    private void testcase(MyListEntry myListEntry){
//
//        SQLiteDatabase db = sqlOpener.getReadableDatabase();
////            Cursor c = db.rawQuery(
////        "SELECT x.*,a.Edict_id,a.StartIndex " +
////        "FROM " +
////                "( " +
////                    "SELECT DISTINCT UserId " +
////                    ", _id as Tweet_id " +
////                    "FROM "+ InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
////                    "WHERE [Name] = ? and [Sys] = " + myListEntry.getListsSys() +  " " +
////                ") as x " +
////                "LEFT JOIN " +
////                "( " +
////                "LEFT JOIN " +
////                "( " +
////                    " SELECT Tweet_id" +
////                    ",Edict_id " +
////                    ",[StartIndex]" +
////                    ",[EndIndex]" +
////                    "From " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
////                    " WHERE [Edict_id] is not NULL and StartIndex is not NULL and EndIndex is not NULL and EndIndex > StartIndex " +
////                ") as a " +
////                "ON x.Tweet_id = a.Tweet_id ",new String[]{myListEntry.getListName()});
//        Cursor c = db.rawQuery(
//                "SELECT x.*,a.Edict_id,a.StartIndex " +
//                        "FROM " +
//                        "( " +
//                        "SELECT DISTINCT UserId " +
//                        ", _id as Tweet_id " +
//                        "FROM "+ InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
//                        ,null);
//            if(c.getCount()>0) {
//                c.moveToFirst();
//
//                while (!c.isAfterLast()) {
//                    Log.d(TAG,"userId: " + c.getString(0) + ", tweet_id: "
//                            + c.getString(1)  + ", edictid: " + c.getString(2));
//                    c.moveToNext();
//                }
//            }
//        c.close();
//
//        db.close();
//    };
//

    /**
     * Retrives all the saved tweets in a SINGLE USER'S Saved Tweets. Used in {@link com.jukuproject.jukutweet.Fragments.TweetListSingleUserFragment}
     *
     * @param userInfo UserInfo of the User whose saved tweets will be pulled.
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
    public ArrayList<Tweet> getTweetsForSavedTweetsList(UserInfo userInfo, ColorThresholds colorThresholds) {
        ArrayList<Tweet> savedTweets = new ArrayList<>();
        SQLiteDatabase db = sqlOpener.getReadableDatabase();
        try {
            Cursor c = db.rawQuery("SELECT TweetLists.[UserId] " +
                            ",(CASE WHEN UserName.ScreenName is null THEN ALLTweets.UserScreenName ELSE UserName.ScreenName end) as [ScreenName] " +
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
                            "SELECT  DISTINCT [UserId]" +
                            ", _id as [Tweet_id]" +
                            "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                            "WHERE [UserId] = ? " +
                            ") as TweetLists " +
                                " LEFT JOIN " +
                            " ( " +
                            "SELECT  DISTINCT [_id] " +
                            ",[Tweet_id]" +
                            ",[UserScreenName] " +
                            ",[Text]" +
                            ",[CreatedAt]  as [Date] " +
                            " FROM "+ InternalDB.Tables.TABLE_SAVED_TWEETS + " " +
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

                            "FROM " +
                            "( " +
                            " SELECT Tweet_id" +
                            ",Edict_id " +
                            ",[StartIndex]" +
                            ",[EndIndex]" +
                            "From " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
                            " WHERE [Edict_id] is not NULL and StartIndex is not NULL and EndIndex is not NULL and EndIndex > StartIndex " +
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
                            "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + ")" +
                            ") as c " +
                            "ON a.[Edict_id] = c.[Edict_id] " +

                            " ) as TweetKanji " +
                            "On TweetLists.Tweet_id = TweetKanji.Tweet_id " +


                            "LEFT JOIN " +
                            " (" +
                            "SELECT DISTINCT [UserId] " +
                            ", [ScreenName] " +
                            ", [UserName] " +
                            " FROM " + InternalDB.Tables.TABLE_USERS +" " +
                            ") as [UserName] " +
                            "On TweetLists.UserId = UserName.UserId " +

                            "Order by date(ALLTweets.Date) Desc,TweetLists.[Tweet_id] asc,TweetKanji.StartIndex asc"
                    , new String[]{userInfo.getUserId()});

            /* The query pulls a list of tweetdata paired with each parsed-kanji in the tweet, resulting in
            * multiple duplicate lines of tweetdata. So the cursor ony adds tweet data once, when a new tweetid is found. Meanwhile
            * the kanji data for each row is added to a TweetKanjiColor object, which is then added to the kanji and the kanji
            * added to the final "savedTweets" list when a new tweetid appears (or the cursor finishes) */

            if(c.getCount()>0) {
                c.moveToFirst();
                String currentTweetId = c.getString(3);
                Tweet tweet = new Tweet();
                while (!c.isAfterLast())
                {

                    if(c.isFirst()) {
                        tweet.setIdString(c.getString(3));
                        tweet.setCreatedAt(c.getString(14));
                        tweet.getUser().setUserId(c.getString(0));
                        tweet.setText(c.getString(4));
                        tweet.getUser().setScreen_name(c.getString(1));
                        tweet.getUser().setName(c.getString(2));

                    } else
                        //FLush old tweet
                        if(!currentTweetId.equals(c.getString(3))){
                            tweet.assignTweetColorToTweet(colorThresholds);
                            savedTweets.add(new Tweet(tweet));
                            currentTweetId = c.getString(3);
                            tweet = new Tweet();

                            tweet.setIdString(c.getString(3));
                            tweet.setCreatedAt(c.getString(14));
                            tweet.getUser().setUserId(c.getString(0));
                            tweet.setText(c.getString(4));
                            tweet.getUser().setScreen_name(c.getString(1));
                            tweet.getUser().setName(c.getString(2));
                        }



                    if(c.getString(5) != null && c.getString(6) != null) {
                        WordEntry wordEntry = new WordEntry(c.getInt(5)
                                ,c.getString(6)
                                ,c.getString(7)
                                ,c.getString(8)
                                ,c.getInt(9)
                                ,c.getInt(10)
                                ,c.getString(11)
                                ,c.getInt(12)
                                ,c.getInt(13));

                        tweet.addWordEntry(wordEntry);
                    }
//                    WordEntry wordEntry = new WordEntry(c.getInt(5)
//                            ,c.getString(6)
//                            ,c.getString(7)
//                            ,c.getString(8)
//                            ,c.getInt(9)
//                            ,c.getInt(10)
//                            ,c.getString(11)
//                            ,c.getInt(12)
//                            ,c.getInt(13));




                    if(c.isLast()) {
                        tweet.assignTweetColorToTweet(colorThresholds);
                        savedTweets.add(new Tweet(tweet));
                    }
                    c.moveToNext();
                }


            } else {if(BuildConfig.DEBUG) {Log.d(TAG,"c.getcount was 0!!");}}
            c.close();

        } catch (SQLiteException e){
            Log.e(TAG,"getTweetsForSavedTweetsList (singleuser) Sqlite exception: " + e);
        } catch (Exception e) {
            Log.e(TAG,"getTweetsForSavedTweetsList(singleuser) generic exception: " + e);
        } finally {
            db.close();
        }
        return savedTweets;
    }


    /**
     * Pulls tweets containing a kanji (or kanjis)
     * @param wordIds Edict ids of kanji that we are searching for
     * @param colorThresholds ColorThreshold used to assign color categories to the WordEntries within the Tweets that get returned
     * @return Array of tweets that include a kanji (or kanjis)
     *
     * @see com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog
     */
    public ArrayList<Tweet> getTweetsThatIncludeAWord(String wordIds,ColorThresholds colorThresholds) {

    ArrayList<Tweet> savedTweets = new ArrayList<>();
    SQLiteDatabase db = sqlOpener.getReadableDatabase();
    try {
        Cursor c = db.rawQuery("SELECT TweetLists.[UserId] " +
                        ",UserName.ScreenName " +
                        ",UserName.UserName " +
                        ",TweetLists.[Tweet_id] " +
                        ",[TweetLists].[Text] " +
                        ",TweetKanji.Edict_id " +
                        ",TweetKanji.Kanji " +
                        ",(CASE WHEN [TweetKanji].[Furigana] is null  then '' else [TweetKanji].[Furigana] end) as [Furigana] " +
                        ",TweetKanji.Definition " +
                        ",TweetKanji.Total " +
                        ",TweetKanji.Correct " +
                        ",TweetKanji.Color " +
                        ",TweetKanji.StartIndex " +
                        ",TweetKanji.EndIndex " +
                        ",[TweetLists].[Date]" +

                        "FROM  " +

                        " ( " +
                        "SELECT  DISTINCT [UserId]" +

                        ",[Tweet_id]" +
                        ",[Text]" +
                        ",[CreatedAt]  as [Date] " +
                        " FROM "+ InternalDB.Tables.TABLE_SAVED_TWEETS + " " +
                                " WHERE [Tweet_id] in (" +
                                " SELECT DISTINCT Tweet_id " +
                                "From " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
                                " WHERE [Edict_id] in (" + wordIds + ") " +
                                ")" +

                        ") as TweetLists " +
//                        "ON TweetLists.[Tweet_id] = ALLTweets.[Tweet_id] " +
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
                        " SELECT DISTINCT Tweet_id" +
                        ",Edict_id " +
                        ",[StartIndex]" +
                        ",[EndIndex]" +
                        "From " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
                        " WHERE [Edict_id] is not NULL and StartIndex is not NULL and EndIndex is not NULL and EndIndex > StartIndex " +
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
                        "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + ")" +
                        ") as c " +
                        "ON a.[Edict_id] = c.[Edict_id] " +

                        " ) as TweetKanji " +
                        "On TweetLists.Tweet_id = TweetKanji.Tweet_id " +


                        "LEFT JOIN " +
                        " (" +
                        "SELECT DISTINCT [UserId] " +
                        ", [ScreenName] " +
                        ", [UserName] " +
                        " FROM " + InternalDB.Tables.TABLE_USERS +" " +
                        ") as [UserName] " +
                        "On TweetLists.UserId = UserName.UserId " +

                        "Order by date(TweetLists.Date) Desc,TweetLists.[Tweet_id] asc,TweetKanji.StartIndex asc"
                , null);

            /* The query pulls a list of tweetdata paired with each parsed-kanji in the tweet, resulting in
            * multiple duplicate lines of tweetdata. So the cursor ony adds tweet data once, when a new tweetid is found. Meanwhile
            * the kanji data for each row is added to a TweetKanjiColor object, which is then added to the kanji and the kanji
            * added to the final "savedTweets" list when a new tweetid appears (or the cursor finishes) */

        if(c.getCount()>0) {
            c.moveToFirst();
            String currentTweetId = c.getString(3);
            Tweet tweet = new Tweet();
            while (!c.isAfterLast())
            {

                if(c.isFirst()) {
                    tweet.setIdString(c.getString(3));
                    tweet.setCreatedAt(c.getString(14));
                    tweet.getUser().setUserId(c.getString(0));
                    tweet.setText(c.getString(4));
                    tweet.getUser().setScreen_name(c.getString(1));
                    tweet.getUser().setName(c.getString(2));

                }

//                WordEntry wordEntry = new WordEntry(c.getInt(5)
//                        ,c.getString(6)
//                        ,c.getString(7)
//                        ,c.getString(8)
//                        ,c.getInt(9)
//                        ,c.getInt(10)
//                        ,c.getString(11)
//                        ,c.getInt(12)
//                        ,c.getInt(13));

                if(c.getString(5) != null && c.getString(6) != null) {
                    WordEntry wordEntry = new WordEntry(c.getInt(5)
                            ,c.getString(6)
                            ,c.getString(7)
                            ,c.getString(8)
                            ,c.getInt(9)
                            ,c.getInt(10)
                            ,c.getString(11)
                            ,c.getInt(12)
                            ,c.getInt(13));

                    tweet.addWordEntry(wordEntry);
                }
//
//                Log.e(TAG,"TWEETOPS, start: " + c.getString(6) + ": " + c.getString(12) + " = " + c.getString(13));
//
//                tweet.addWordEntry(wordEntry);

                //FLush old tweet
                if(!currentTweetId.equals(c.getString(3))){
                    tweet.assignTweetColorToTweet(colorThresholds);
                    savedTweets.add(new Tweet(tweet));
                    currentTweetId = c.getString(3);
                    tweet = new Tweet();

                    tweet.setIdString(c.getString(3));
                    tweet.setCreatedAt(c.getString(14));
                    tweet.getUser().setUserId(c.getString(0));
                    tweet.setText(c.getString(4));
                    tweet.getUser().setScreen_name(c.getString(1));
                    tweet.getUser().setName(c.getString(2));
                }

                if(c.isLast()) {
                    tweet.assignTweetColorToTweet(colorThresholds);
                    savedTweets.add(new Tweet(tweet));
                }
                c.moveToNext();
            }


        } else {if(BuildConfig.DEBUG) {Log.d(TAG,"c.getcount was 0!!");}}
        c.close();

    } catch (SQLiteException e){
        Log.e(TAG,"getTweetsThatIncludeAWord  Sqlite exception: " + e);
    } catch (Exception e) {
        Log.e(TAG,"getTweetsThatIncludeAWord generic exception: " + e);
    } finally {
        db.close();
    }
    return savedTweets;

}

    /**
     * Used to prepare colorblock information in {@link com.jukuproject.jukutweet.Fragments.TweetListFragment}
     * @param colorThresholds Collection of thresholds which together determine what color to assign to a word/tweet based on its quiz scores
     * @param myListEntry Object containing information about a given favorites list (ssentially a container for a listname and sys variables).
     *
     * @return Cursor with colorblock details for each saved TweetList in the db
     */
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

        return  sqlOpener.getWritableDatabase().rawQuery(

                "SELECT xx.[Name]" +
                        ",xx.[Sys]"  +
                        ",ifnull(yy.[Total],0) as [Total]" +
                        ",ifnull(yy.[Grey],0) as [Grey]" +
                        ",ifnull(yy.[Red],0) as [Red]" +
                        ",ifnull(yy.[Yellow],0) as [Yellow]" +
                        ",ifnull(yy.[Green],0) as [Green] " +
//                        ",ifnull(yy.[Empty],0) as [Empty] " +
                                ",ifnull(xx.Count,0) as [TweetCount]" +

                        "" +
                        "FROM (" +

                        "SELECT lists.Name,lists.Sys,tweetcounts.Count " +
                        "FROM (" +
                        "Select DISTINCT [Name],[Sys] " +
                        "FROM (" +
                        "SELECT [Name]" +
                        ",0 as [Sys] " +
                        "From " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS +  " " +
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
                        "SELECT 'Yellow' as [Name] " +
                        ",1 as [Sys]" +
                        "Union " +
                        "SELECT 'Purple' as [Name] " +
                        ",1 as [Sys]" +
                        "Union " +
                        "SELECT 'Orange' as [Name] "  +
                        ",1 as [Sys]" +
                        ") as [x] " +
                        " WHERE ([Name] = ? AND [Sys] = ?) OR " + ALL_LISTS_FLAG + " = 1 " +
                ") as lists " +
                        " LEFT JOIN " +
                "( " +

                        /* Get A list of each saved tweet and the number of kanji in those tweets */
                            "SELECT  [Name]" +
                            ",[Sys]" +
                            ",count(_id) as Count " +
                            "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                            " WHERE ([Name] = ? AND [Sys] = ?) OR " + ALL_LISTS_FLAG + " = 1 " +
                            " Group by [Name] ,[Sys] " +

                        ") as tweetcounts " +
                        "ON lists.[Name] = tweetcounts.[Name] and lists.[Sys] = tweetcounts.[Sys] " +
                        ") as [xx] " +
                        "LEFT JOIN " +
                        " (" +

////                /* Assign each tweet a color based on the percentages of word color scores for kanjis in the tweet */
                        "Select [Name] " +
                        ",[Sys] " +
//                                ",Count([Tweet_id]) as [TweetCount] " +
                                ",SUM([Grey]) + SUM([Red]) + SUM([Yellow]) + SUM([Green]) as [Total] " +
                                ",SUM([Grey]) as [Grey]" +
                                ",SUM([Red]) as [Red]" +
                                ",SUM([Yellow]) as [Yellow] " +
                                ",SUM([Green]) as [Green] " +
                        " FROM ( " +

                /* Now to pull together ListName, Tweet and the totals (by color) of the kanji in those tweets */
                        "SELECT  ListsTweetsAndAllKanjis.[Name]" +
                        ",ListsTweetsAndAllKanjis.[Sys]" +
                        ",SUM([Grey]) + SUM([Red]) + SUM([Yellow]) + SUM([Green]) as [Total] " +
                        ",SUM([Grey]) as [Grey]" +
                        ",SUM([Red]) as [Red]" +
                        ",SUM([Yellow]) as [Yellow]" +
                        ",SUM([Green]) as [Green] " +
                        "FROM (" +

                        /* Now we have a big collection of list metadata (tweetlists), and all the kanji scores and colors for
                            each kanji (kanjilists) */
                        " Select [Name] " +
                        " ,[Sys] " +
//                        ", TweetLists.[Tweet_id] " +

                        ",(CASE WHEN [Total] is not NULL AND [Total] < " + colorThresholds.getGreyThreshold() + " THEN 1 ELSE 0 END) as [Grey] " +
                        ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] < " + colorThresholds.getRedThreshold() + "  THEN 1  ELSE 0 END) as [Red] " +
                        ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and ([Percent] >= " + colorThresholds.getRedThreshold() + "  and [Percent] <  " + colorThresholds.getYellowThreshold() + ") THEN 1  ELSE 0 END) as [Yellow] " +
                        ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] >= " + colorThresholds.getYellowThreshold() + " THEN 1 ELSE 0 END) as [Green] " +


                        " fROM ( " +
                        " Select DISTINCT TweetLists.[Name] " +
                        " ,TweetLists.[Sys] " +
                        " ,TweetKanji.[Edict_id] " +
                        ",[Total]" +
                        ",[Percent] " +
                        "FROM " +
                        "(" +

                        /* Get A list of each saved tweet and the number of kanji in those tweets */
                        "SELECT  [Name]" +
                        ",[Sys]" +
                        ",[UserID] " +
                        ",[_id] as [Tweet_id]" +
                        "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
//                        "WHERE ([Name] = ? OR " + ALL_LISTS_FLAG + " = 1) AND ([Sys] = ? OR " + ALL_LISTS_FLAG + " = 1) " +
                        " WHERE ([Name] = ? AND [Sys] = ?) OR " + ALL_LISTS_FLAG + " = 1 " +

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
                        "From " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
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

                        " ) as TweetKanji " +
                        "On TweetLists.Tweet_id = TweetKanji.Tweet_id " +


                        ") as x " +

                        ") as [ListsTweetsAndAllKanjis] " +
                        "GROUP BY [Name],[Sys]" +
//                        ",[Tweet_id] " +
                        ") as [ListandTweets]  " +
                    "GROUP BY [Name],[Sys]" +

//                        ") as [Lists] " +

                        ") as yy "  +
                        "ON xx.[Name] = yy.[Name] and cast(xx.[Sys] as INTEGER)  = cast(yy.[Sys] as INTEGER)  " +

                        "Order by xx.[Sys] Desc,xx.[Name]"
                ,new String[]{name,String.valueOf(sys),name,String.valueOf(sys),name,String.valueOf(sys)});
    }


    /**
     * Used to prepare colorblock information in {@link com.jukuproject.jukutweet.Fragments.TweetListSingleUserFragment}
     * @param colorThresholds Collection of thresholds which together determine what color to assign to a word/tweet based on its quiz scores
     * @param userId Twitter user id for the single user whose tweet colorblocks will be assembled
     *
     * @return Cursor with colorblock details for each saved TweetList in the db
     *
     * @see com.jukuproject.jukutweet.Fragments.TweetListSingleUserFragment
     */
    public Cursor getTweetListColorBlocksCursorForSingleUser(ColorThresholds colorThresholds, String userId) {

        return  sqlOpener.getWritableDatabase().rawQuery(
                "Select a.[UserId]" +
                        ",0 as [Sys] " +
                        ",a.[Total] " +
                        ",a.[Grey] " +
                        ",a.[Red] " +
                        ",a.[Yellow] " +
                        ",a.[Green] " +
                        ",ifnull(b.[TweetCount],0) as [TweetCount] " +
                        "FROM " +
                        "(" +

                /* Now to pull together ListName, Tweet and the totals (by color) of the kanji in those tweets */
                        "SELECT  ListsTweetsAndAllKanjis.[UserId] " +
                        ",SUM([Grey]) + SUM([Red]) + SUM([Yellow]) + SUM([Green]) as [Total] " +
                        ",SUM([Grey]) as [Grey]" +
                        ",SUM([Red]) as [Red]" +
                        ",SUM([Yellow]) as [Yellow]" +
                        ",SUM([Green]) as [Green] " +
                        "FROM (" +

                        /* Now we have a big collection of list metadata (tweetlists), and all the kanji scores and colors for
                            each kanji (kanjilists) */
                        " Select DISTINCT TweetLists.[UserId] " +
//                        " ,TweetLists.[TweetCount]  " +
                        " ,TweetKanji.Edict_id "   +
                        ",(CASE WHEN [Total] is not NULL AND [Total] < " + colorThresholds.getGreyThreshold() + " THEN 1 ELSE 0 END) as [Grey] " +
                        ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] < " + colorThresholds.getRedThreshold() + "  THEN 1  ELSE 0 END) as [Red] " +
                        ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and ([Percent] >= " + colorThresholds.getRedThreshold() + "  and [Percent] <  " + colorThresholds.getYellowThreshold() + ") THEN 1  ELSE 0 END) as [Yellow] " +
                        ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] >= " + colorThresholds.getYellowThreshold() + " THEN 1 ELSE 0 END) as [Green] " +

                        "FROM " +

                                "(" +

                        /* Get A list of each saved tweet and the number of kanji in those tweets */
                         "SELECT  DISTINCT [UserId]" +
                        ", _id as [Tweet_id]" +
                        "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                        "WHERE [UserId] = ? " +

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
                        "From " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
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



                        " ) as TweetKanji " +
                        "On TweetLists.Tweet_id = TweetKanji.Tweet_id " +

                        ") as [ListsTweetsAndAllKanjis] " +
                        "GROUP BY [UserId]" +
//                        ") as [ListandTweets]  " +
//                        "GROUP BY [UserId]" +


                ") as a " +
                        "LEFT JOIN " +
                        "(" +

                        /* Get A list of each saved tweet and the number of kanji in those tweets */
                        "SELECT [UserId]" +
                        ",COUNT(_id) as TweetCount " +
                        "FROM " +
                        " ( " +
                        "SELECT  DISTINCT [UserId]" +
                        ", _id " +
                        "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                        "WHERE [UserId] = ? " +
                        ") as x " +
                        " Group by [UserId] " +
                        ")  as b " +
                        " ON a.[UserId] = b.[UserId] "


                ,new String[]{userId,userId});
    }



    /**
     * Pulls words for Top and Bottom in {@link com.jukuproject.jukutweet.Fragments.StatsFragmentProgress} for PostQuizStats
     * after a TWEET based quiz (i.e. a quiz that began in {@link com.jukuproject.jukutweet.Fragments.TweetListFragment}).
     *
     * @param topOrBottom tag designating the list as a "Top" list of words with best scores or "Bottom" list of words with worst scores
     * @param idsToExclude Word ids to exclude from the results (for example, if they have already been added to the "Top" list, exclude
     *                     them from the "Bottom" list
     * @param myListEntry Saved TweetList which was quizzed on
     * @param colorThresholds Collection of thresholds which together determine what color to assign to a word/tweet based on its quiz scores
     * @param totalCountLimit maximum number of results for the list
     * @param topbottomThreshold percentage threshold necessary to be deemed a "Top" word
     * @return Array of word entries for the Top X or Bottom X words in a saved TweetList
     *
     * @see com.jukuproject.jukutweet.Fragments.StatsFragmentProgress
     */
    public ArrayList<WordEntry> getTopFiveTweetWordEntries(String topOrBottom
            ,@Nullable  ArrayList<Integer> idsToExclude
            ,MyListEntry myListEntry
            ,ColorThresholds colorThresholds
            ,int totalCountLimit
            ,double topbottomThreshold) {

        SQLiteDatabase db = sqlOpener.getReadableDatabase();

        String topBottomSort;
        if(topOrBottom.equals("Top")) {
            topBottomSort = "WHERE [ColorSort]>0 ORDER BY [ColorSort] desc, [Percent] desc,[Total] desc ";
        } else {
            topBottomSort = "ORDER BY [ColorSort] asc, [Percent] asc,[Total] desc ";
        }

        ArrayList<WordEntry> wordEntries = new ArrayList<>();
        try {

            Cursor c = db.rawQuery("SELECT [_id]" +
                    ",[Kanji]" +
                    ",[Correct]" +
                    ",[Total] " +

                    ",(CASE WHEN Furigana is null then '' else Furigana  end) as [Furigana]" +
                    ",[Definition]  " +

                    " ,[Blue]" +
                    " ,[Red] " +
                    " ,[Green] " +
                    " ,[Yellow] " +
                    " ,[Purple] " +
                    " ,[Orange] " +
                    ",[Other] " +
                    ",[Percent] " +
                    "FROM " +
                    "(" +
                    "SELECT " +
                    "[_id]," +
                    "[Kanji]," +
                    "[Furigana]," +
                    "[Definition]," +
                    "ifnull([Total],0) as [Total] " +

                    ",(CASE WHEN [Total] < " + colorThresholds.getGreyThreshold() + " THEN 1 " +
                    "WHEN [Total] >= " + colorThresholds.getGreyThreshold() + " and CAST(ifnull([Correct],0)  as float)/[Total] < " + colorThresholds.getRedThreshold() + "  THEN 0  " +
                    "WHEN [Total] >= " + colorThresholds.getGreyThreshold() + " and (CAST(ifnull([Correct],0)  as float)/[Total] >= " + colorThresholds.getRedThreshold() + "  and CAST(ifnull([Correct],0)  as float)/[Total] <  " + colorThresholds.getYellowThreshold() + ") THEN 2  " +
                    "WHEN [Total] >= " + colorThresholds.getGreyThreshold() + " and CAST(ifnull([Correct],0)  as float)/[Total] >= " + colorThresholds.getYellowThreshold() + " THEN 3 " +
                    "ELSE 0 END) as [ColorSort] " +

                    ",ifnull([Correct],0)  as [Correct]" +
                    ",CAST(ifnull([Correct],0)  as float)/[Total] as [Percent] " +


                    " ,[Blue]" +
                    " ,[Red] " +
                    " ,[Green] " +
                    " ,[Yellow] " +
                    " ,[Purple] " +
                    " ,[Orange] " +
                    ",[Other] " +

                    "FROM " +
                    "(" +
                    "SELECT [_id]" +
                    ",[Kanji]" +
                    ",[Furigana]" +
                    ",[Definition]  " +
                    "FROM [Edict] " +
                    "where [_id] in (" +

                    "Select Edict_id " +
                    "FROM " +
                    "(" +
                    "Select _id as Tweet_Id " +
                    "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES +
                    " WHERE ([Name] = ? and [Sys] = ?)" +
                    ") as a " +
                    "Left JOIN " +
                    "(" +
                    "Select DISTINCT Tweet_id" +
                    ",[Edict_id] " +
                    "FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI +
                    " WHERE [Edict_id] is not NULL " +
                    ") as b " +
                    "ON a.[Tweet_Id] = b.[Tweet_Id] " +
                    ")" +
                    ") " +
                    "NATURAL LEFT JOIN (" +
                    "SELECT [_id]" +
                    ",sum([Correct]) as [Correct]" +
                    ",sum([Total]) as [Total] " +
                    "from [JScoreboard]  " +
                    "GROUP BY [_id]" +
//                    ") " +
                    ") NATURAL LEFT JOIN (" +
                    "SELECT [_id]" +
                    ",SUM([Blue]) as [Blue]" +
                    ",SUM([Red]) as [Red]" +
                    ",SUM([Green]) as [Green]" +
                    ",SUM([Yellow]) as [Yellow]" +
                    ",SUM([Purple]) as [Purple]" +
                    ",SUM([Orange]) as [Orange]" +

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
                    "FROM " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES + " " +
//                    "WHERE [_id] = ?" +
                    "where [_id] in ( " +
                    "SELECT [_id] " +
                    "FROM [JFavorites] " +
                    "WHERE ([Name] = ? and [Sys] = ?)" +
                    ")" +
                    ") as x Group by [_id]" +
                    ")" +
                    ") " + topBottomSort + " LIMIT " + totalCountLimit,new String[]{myListEntry.getListName(),String.valueOf(myListEntry.getListsSys()),myListEntry.getListName(),String.valueOf(myListEntry.getListsSys())});

//                    ") Where [Total] >= 1 " + topBottomSort + " LIMIT " + totalCountLimit,new String[]{myListEntry.getListName(),String.valueOf(myListEntry.getListsSys())});


            if(c.getCount() == 0) {
                Log.e(TAG,"TweetOpsHelper gettopfive c count is 0!");
                return wordEntries;
            }

            c.moveToFirst();
            while (!c.isAfterLast()) {

                if(wordEntries.size()<totalCountLimit
                        && ((topOrBottom.equals("Bottom") && c.getFloat(13)<=topbottomThreshold) || (topOrBottom.equals("Top") && c.getFloat(13)>0))) {

                    if(idsToExclude == null || !idsToExclude.contains(c.getInt(0))) {
                        WordEntry wordEntry = new WordEntry();
                        wordEntry.setId(c.getInt(0));
                        wordEntry.setKanji(c.getString(1));
                        wordEntry.setCorrect(c.getInt(2));
                        wordEntry.setTotal(c.getInt(3));
                        wordEntry.setFurigana(c.getString(4));
                        wordEntry.setDefinition(c.getString(5));
                        wordEntries.add(wordEntry);
                        Log.d(TAG,"ADDING KANJI: " + wordEntry.getKanji());

                        wordEntry.setItemFavorites(new ItemFavorites(c.getInt(6)
                                ,c.getInt(7)
                                ,c.getInt(8)
                                ,c.getInt(9)
                                ,c.getInt(10)
                                ,c.getInt(11)
                                ,c.getInt(12)));
                    }

                }
                c.moveToNext();
            }
            c.close();

        } catch (SQLiteException e){
            Log.e(TAG,"getTopFiveTweetWordEntries Sqlite exception: " + e);
        } catch (Exception e) {
            Log.e(TAG,"getTopFiveTweetWordEntries generic exception: " + e);
        } finally {
            db.close();
        }

        return  wordEntries;
    }


}


