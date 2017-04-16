package com.jukuproject.jukutweet.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Interfaces.WordListOperationsInterface;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.WordEntry;

import java.util.ArrayList;

/**
 * Created by JClassic on 4/5/2017.
 */

public class WordOpsHelper implements WordListOperationsInterface {
    private SQLiteOpenHelper sqlOpener;
    private static String TAG = "TEST-wordops";

    public WordOpsHelper(SQLiteOpenHelper sqlOpener) {
        this.sqlOpener = sqlOpener;
    }

    /**
     * Checks for duplicate entries for a user-created list in the JFavoritesLists table
     * @param listName prospective new user-created mylist
     * @return true if that list name already exists, false if not
     */
    public boolean duplicateWordList(String listName) {

        /** Before inserting record, check to see if feed already exists */
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
        String queryRecordExists = "Select Name From " + InternalDB.Tables.TABLE_FAVORITES_LISTS + " where " + InternalDB.Columns.TFAVORITES_COL0 + " = ?" ;
        Cursor c = db.rawQuery(queryRecordExists, new String[]{listName});
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
     * Adds a new mylist to the JFavoritesLists table in the db
     * @param listName list name to add
     * @return boolean true if success, false if not
     */
    public boolean saveWordList(String listName) {
        try {
            SQLiteDatabase db = sqlOpener.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(InternalDB.Columns.TFAVORITES_COL0, listName.trim());
            db.insert(InternalDB.Tables.TABLE_FAVORITES_LISTS, null, values);
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
     * @see com.jukuproject.jukutweet.MainActivity#onEditMyListDialogPositiveClick(String listType, int, String, boolean)
     * @see com.jukuproject.jukutweet.MainActivity#deleteOrClearDialogFinal(String listType, Boolean, String, boolean)
     */
    public boolean clearWordList(String listName, boolean isStarFavorite) {
        try{
            SQLiteDatabase db = sqlOpener.getWritableDatabase();
            if(isStarFavorite) {
                db.delete(InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES, InternalDB.Columns.TFAVORITES_COL0 + "= ? and " + InternalDB.Columns.TFAVORITES_COL1 + "= 1", new String[]{listName});
            } else {
                db.delete(InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES, InternalDB.Columns.TFAVORITES_COL0 + "= ? and " + InternalDB.Columns.TFAVORITES_COL1 + "= 0", new String[]{listName});
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
     * @see com.jukuproject.jukutweet.MainActivity#onEditMyListDialogPositiveClick(String listType, int, String, boolean)
     * @see com.jukuproject.jukutweet.MainActivity#deleteOrClearDialogFinal(String listType, Boolean, String, boolean)
     */
    public boolean deleteWordList(String listName) {
        try{
            SQLiteDatabase db = sqlOpener.getWritableDatabase();
            db.delete(InternalDB.Tables.TABLE_FAVORITES_LISTS, InternalDB.Columns.TFAVORITES_COL0 + "= ?", new String[]{listName});
            db.delete(InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES, InternalDB.Columns.TFAVORITES_COL0 + "= ? and " + InternalDB.Columns.TFAVORITES_COL1 + "= 0", new String[]{listName});
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
    public boolean renameWordList(String oldListName, String newListName) {
        try{
            SQLiteDatabase db = sqlOpener.getWritableDatabase();

            String sql = "Update " + InternalDB.Tables.TABLE_FAVORITES_LISTS + "  SET [Name]=? WHERE [Name]=? ";
            SQLiteStatement statement = db.compileStatement(sql);
            statement.bindString(1, newListName);
            statement.bindString(2, oldListName);
            statement.executeUpdateDelete();

            String sql2 = "Update " +  InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES + " SET [Name]=? WHERE [Name]=? and [Sys] = 0";
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
     * Changes the system Word List that a kanji is associated with (when the user clicks on the favorites star next to
     * the word). Toggles from Black-> through various colors -> black again.
     * @param kanjiID id of kanji in question
     * @param originalColor current color of the favorites star for that kanji
     * @param updatedColor new color (i.e. new system list) that the kanji will be associated with
     * @return bool true if the cupdate was succesful, false if not
     */
    public boolean changeWordListStarColor(int kanjiID, String originalColor, String updatedColor) {
        SQLiteDatabase db = sqlOpener.getWritableDatabase();

        try {
            if(originalColor.equals("Black") && updatedColor.equals("Black")) {
                Log.e(TAG,"ERROR! changefavoritelistentry Both entries are BLACK. No system lists available?");
                return false;
            } else if(originalColor.equals("Black")) {
                //Insert statement only
                return addWordToWordList(kanjiID,updatedColor,1);
            } else if(updatedColor.equals("Black")) {
                //Delete statement only
                return removeWordFromWordList(kanjiID,originalColor,1);

            } else {
                String sql = "Update "  + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES + " SET "+ InternalDB.Columns.TFAVORITES_COL0 +" = ? WHERE "+ InternalDB.Columns.TFAVORITES_COL0 +"= ? and " + InternalDB.Columns.TFAVORITES_COL1+" = 1 and " + InternalDB.Columns.COL_ID + " = ?";
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
     * Adds a kanji to a wordlist
     * @param wordId id of kanji being added
     * @param listName name of list
     * @param listSys int designating the list as a system list (1) or a user-created list (0)
     * @return bool true if successful insert, false if not
     */
    public boolean addWordToWordList(int wordId, String listName, int listSys) {
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(InternalDB.Columns.COL_ID, wordId);
            values.put(InternalDB.Columns.TFAVORITES_COL0, listName);
            values.put(InternalDB.Columns.TFAVORITES_COL1, listSys);
            db.insert(InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES, null, values);
            return true;
        } catch (SQLiteException e) {
            Log.e(TAG, "addWordToWordList sqlite exception: " + e);
        } catch (NullPointerException e) {
            Log.e(TAG, "addWordToWordList something was null: " + e);
        } finally {
            db.close();
        }

        return  false;
    }




    /**
     * Removes a word from a word list (Note: word is not deleted, only the its association with a given favorites list)
     * @param wordId id of word in question
     * @param listName name of list
     * @param listSys int designating the list as a system list (1) or a user-created list (0)
     * @return bool true if successful insert, false if not
     */
    public boolean removeWordFromWordList(int wordId, String listName, int listSys) {
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
        try {
            db.delete(InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES,
                    InternalDB.Columns.TFAVORITES_COL0 + " = ? and "  + InternalDB.Columns.TFAVORITES_COL1 + " = ? and " + InternalDB.Columns.COL_ID + " = ? ", new String[]{listName,String.valueOf(listSys),String.valueOf(wordId)});
            return true;
        } catch (SQLiteException e) {
            Log.e(TAG, "removeWordFromWordList sqlite exception: " + e);
        } catch (NullPointerException e) {
            Log.e(TAG, "removeWordFromWordList something was null: " + e);
        } finally {
            db.close();
        }
        return  false;
    }


    /**
     * Removes multiple words from a word list
     * @param concatenatedWordIds a string of wordIds concatenated together with commas (ex: 123,451,6532,23)
     * @param myListEntry MyList Object containing the name and sys variables for a WordList
     * @return bool true if successful delete, false if error
     *
     * @see com.jukuproject.jukutweet.Fragments.MyListBrowseFragment
     */
    public boolean removeMultipleWordsFromWordList(String concatenatedWordIds, MyListEntry myListEntry) {
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES + " WHERE " + InternalDB.Columns.TFAVORITES_COL0 + " = ? and "  + InternalDB.Columns.TFAVORITES_COL1 + " = ? and " + InternalDB.Columns.COL_ID + " in (" + concatenatedWordIds + ")",new String[]{myListEntry.getListName(),String.valueOf(myListEntry.getListsSys())});
            return true;
        } catch (SQLiteException e) {
            Log.e(TAG, "removeMultipleWordsFromWordList sqlite exception: " + e);
        } catch (NullPointerException e) {
            Log.e(TAG, "removeMultipleWordsFromWordList something was null: " + e);
        } finally {
            db.close();
        }
        return  false;
    }

    /**
     * Pulls full list of available word lists, as well as how many of those lists contain the given words. Used when
     * creating the copy/move dialog in browse words fragment
     * @param activeFavoriteStars List of system favorites lists that are activated (in user preferences)
     * @param concatenatedWordIds a string of wordIds concatenated together with commas (ex: 123,451,6532,23)
     * @param entryToExclude When the copy/move dialog is created, it is because the user is in a certain list (lets say List A), and wants to copy
     *                       or move tweets from that list to other lists (List B,C,D etC). So the list of available options to move/copy to
     *                       should not include the current list (List A). It is excluded via this parameter.
     * @return List of MyListEntry objects, one for each tweet list
     *
     * @see com.jukuproject.jukutweet.Dialogs.CopyMyListItemsDialog
     */
    public ArrayList<MyListEntry> getWordListsForAWord(ArrayList<String> activeFavoriteStars
            , String concatenatedWordIds
            , @Nullable MyListEntry entryToExclude) {

        ArrayList<MyListEntry> myListEntries = new ArrayList<>();
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
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
                    "From " + InternalDB.Tables.TABLE_FAVORITES_LISTS + " "    +
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
                    "FROM " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES + " "    +
                    " Where _id in (?) " +
                    " Group by Name, Sys " +
                    ") as UserEntries "    +
                    " "    +
                    "ON Lists.Name = UserEntries.Name ", new String[]{concatenatedWordIds});

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
     *  Retrieves a list of word entries (and corresponding edict dictionary info) for words in a particular word list.
     *  Words are also filtered by word "color" (a grading of the words quiz score).
     * @param myListEntry Object containing information about a given favorites list (ssentially a container for a listname and sys variables).
     * @param colorThresholds Collection of thresholds which together determine what color to assign to a word/tweet based on its quiz scores
     * @param colorString concatenated (with commas) string of colors to include in the list (ex: 'Grey','Red','Green')
     * @return list of words from the word list filtered by color
     */
    public ArrayList<WordEntry> getWordsFromAWordList(MyListEntry myListEntry
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
                            "SELECT [_id] " +
                            "FROM " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES +  "  " +
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
                            "FROM " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES +  "  " +
                            "WHERE ([Sys] = ? and [Name] = ? and _id <> ?))  " +
                            "GROUP BY [_id]" +
                            ") as y " +
                            "ON x.[_id] = y.[_id] " +
                            ") " +
                            "WHERE [Color] in (" +  colorString + ") "  +
                            " ORDER BY RANDOM() " + limit + " "
                    , new String[]{String.valueOf(myListEntry.getListsSys()),myListEntry.getListName(),idToExclude,String.valueOf(myListEntry.getListsSys()),myListEntry.getListName(),idToExclude});


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
                if(BuildConfig.DEBUG) {Log.d(TAG,"getmylistwords c.getcount was 0!!");}
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
     * Inserts multiple words into a given word list
     * @param myListEntry MyList Object containing the name and sys variables for a WordList
     * @param concatenatedWordIds a string of wordIds concatenated together with commas (ex: 123,451,6532,23)
     * @return bool true if successful delete, false if error
     */
    public boolean addMultipleWordsToWordList(MyListEntry myListEntry, String concatenatedWordIds) {
        SQLiteDatabase db = sqlOpener.getReadableDatabase();
        try {
            db.execSQL("INSERT OR REPLACE INTO " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES +" SELECT DISTINCT [_id],? as [Name], ? as [Sys] FROM [Edict] WHERE [_id] in (" + concatenatedWordIds + ")",new String[]{myListEntry.getListName(),String.valueOf(myListEntry.getListsSys())});
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

    public Cursor getWordEntryForWordId(int kanjiId, ColorThresholds colorThresholds) {
        return sqlOpener.getWritableDatabase().rawQuery("SELECT [Kanji]" +
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
                "FROM " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES + " " +
                "WHERE [_id] = ?) as x Group by [_id]" +

                "))", new String[]{String.valueOf(kanjiId),String.valueOf(kanjiId),String.valueOf(kanjiId)});
    }



    public Cursor getWordListColorBlockCursor(ColorThresholds colorThresholds, MyListEntry myListEntry) {
        int ALL_LISTS_FLAG = 0;
        int sys = -1;
        String name = "";
        if(myListEntry == null) {
            ALL_LISTS_FLAG = 1;
        } else {
            sys = myListEntry.getListsSys();
            name = myListEntry.getListName();


        }
        Log.d(TAG,"name: " + name);
        Log.d(TAG,"sys: " + sys);
        return  sqlOpener.getWritableDatabase().rawQuery("SELECT xx.[Name]" +
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
                "From " +  InternalDB.Tables.TABLE_FAVORITES_LISTS + " " +
                "UNION " +
                "SELECT 'Blue' as [Name] , 1 as [Sys] " +
                "Union " +
                "SELECT 'Red' as [Name],1 as [Sys] " +
                "Union " +
                "SELECT 'Green' as [Name],1 as [Sys] " +
                "Union " +
                "SELECT 'Yellow' as [Name] ,1 as [Sys]" +
                "Union " +
                "SELECT 'Purple' as [Name] ,1 as [Sys]" +
                "Union " +
                "SELECT 'Orange' as [Name] ,1 as [Sys]" +
                ") as [x] " +
//                "WHERE ([Name] = ?) AND ([Sys] = ? ) " +
                "WHERE ([Name] = ? and [Sys] = "+sys+ ") OR " + ALL_LISTS_FLAG + " = 1 " +
                ") as [xx] " +
                "LEFT JOIN (" +
                "SELECT  [Name] " +
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
                "FROM " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES + " " +
//                "WHERE ([Name] = ?) AND ([Sys] = ? ) " +
                "WHERE ([Name] = ? and [Sys] = "+sys+ ") OR " + ALL_LISTS_FLAG + " = 1 " +
//                "WHERE ([Name] = ? OR " + ALL_LISTS_FLAG + " = 1) AND ([Sys] = ? OR " + ALL_LISTS_FLAG + " = 1) " +
                ") as a " +
                "LEFT JOIN  (" +
                "SELECT [_id]" +
                ",sum([Correct]) as [Correct]" +
                ",sum([Total]) as [Total] FROM [JScoreboard] " +
                "where [_id] in (SELECT DISTINCT [_id] FROM " + InternalDB.Tables.TABLE_FAVORITES_LIST_ENTRIES + ")" +
                " GROUP BY [_id]" +
                ") as b " +
                "ON a.[_id] = b.[_id]) " +
                " as x) as y " +
                "GROUP BY [Name],[Sys]" +
                ") as yy  " +
                "ON xx.[Name] = yy.[Name] and xx.[sys] = yy.[sys]  " +
                "Order by xx.[Sys] Desc,xx.[Name]",new String[]{name,name});

    }

//
//    public void supertest(ColorThresholds colorThresholds, MyListEntry myListEntry) {
//        int ALL_LISTS_FLAG = 0;
//        int sys = -1;
//        String name = "";
//
//            sys = myListEntry.getListsSys();
//            name = myListEntry.getListName();
//        Log.d(TAG,"name: " + name);
//        Log.d(TAG,"sys: " + sys);
//
//        Cursor c =  sqlOpener.getWritableDatabase().rawQuery(
//                "Select DISTINCT [Name],[Sys] " +
//                "FROM (" +
//                "SELECT [Name]" +
//                ",0 as [Sys] " +
//                "From " +  InternalDB.Tables.TABLE_FAVORITES_LISTS + " " +
//                "UNION " +
//                "SELECT 'Blue' as [Name] , 1 as [Sys] " +
//                "Union " +
//                "SELECT 'Red' as [Name],1 as [Sys] " +
//                "Union " +
//                "SELECT 'Green' as [Name],1 as [Sys] " +
//                "Union " +
//                "SELECT 'Yellow' as [Name] ,1 as [Sys]" +
//                "Union " +
//                "SELECT 'Purple' as [Name] ,1 as [Sys]" +
//                "Union " +
//                "SELECT 'Orange' as [Name] ,1 as [Sys]" +
//                ") as [x] " +
////                "WHERE ([Name] = ?) AND ([Sys] = ? ) " +
//                "WHERE ([Name] = ? and [Sys] = ?)",new String[]{name,String.valueOf(sys)});
//
//        if(c.getCount()>0) {
//            c.moveToFirst();
//            while (!c.isAfterLast()) {
//                Log.d(TAG,"name: " + c.getString(0) + ", sys: " + c.getString(1));
//            }
//        } else {
//            Log.d(TAG,"FUCKING NULL");
//        }
//
//        c.close();
//
//    }

    public ArrayList<WordEntry> getTopFiveWordEntries(String topOrBottom
            ,@Nullable  ArrayList<Integer> idsToExclude
            ,MyListEntry myListEntry
            ,ColorThresholds colorThresholds
            ,int totalCountLimit
            ,double topbottomThreshold) {

        SQLiteDatabase db = sqlOpener.getReadableDatabase();

        String topBottomSort;
        if(topOrBottom.equals("Top")) {
            topBottomSort = "and [ColorSort]>0 ORDER BY [ColorSort] desc, [Percent] desc,[Total] desc ";
        } else {
            topBottomSort = "ORDER BY [ColorSort] asc, [Percent] asc,[Total] desc ";
        }

        ArrayList<WordEntry> wordEntries = new ArrayList<>();
        try {

            Cursor c = db.rawQuery("SELECT [_id]" +
                    ",[Kanji]" +
                    ",[Correct]" +
                    ",[Total] " +
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
                    "FROM " +
                    "(" +
                    "SELECT [_id]" +
                    ",[Kanji]" +
                    ",[Furigana]" +
                    ",[Definition]  " +
                    "FROM [Edict] " +
                    "where [_id] in (" +
                    "SELECT [_id] " +
                    "FROM [JFavorites] " +
                    "WHERE ([Name] = ? and [Sys] = ?)" +
                    ")" +
                    ") " +
                    "NATURAL LEFT JOIN (" +
                    "SELECT [_id]" +
                    ",sum([Correct]) as [Correct]" +
                    ",sum([Total]) as [Total] " +
                    "from [JScoreboard]  " +
                    "GROUP BY [_id]" +
                    ") " +
                    ") Where [Total] >= 1 " + topBottomSort + " LIMIT " + totalCountLimit,new String[]{myListEntry.getListName(),String.valueOf(myListEntry.getListsSys())});


            if(c.getCount() == 0) {
                Log.e(TAG,"WordOpsHelper gettopfive c count is 0!");
                return wordEntries;
            }

            c.moveToFirst();
            while (!c.isAfterLast()) {

                if(wordEntries.size()<totalCountLimit
                        && ((topOrBottom.equals("Bottom") && c.getFloat(4)<=topbottomThreshold) || (topOrBottom.equals("Top") && c.getFloat(4)>0))) {

                    if(idsToExclude != null && !idsToExclude.contains(c.getInt(0))) {
                        WordEntry wordEntry = new WordEntry();
                        wordEntry.setId(c.getInt(0));
                        wordEntry.setKanji(c.getString(1));
                        wordEntry.setCorrect(c.getInt(2));
                        wordEntry.setTotal(c.getInt(3));
                        wordEntries.add(wordEntry);
                        Log.d(TAG,"ADDING KANJI: " + wordEntry.getKanji());
                    }

                }
                c.moveToNext();
            }
            c.close();

            //Add dummy word entries if necessary to round out the score
//            while(wordEntries.size()<totalCountLimit) {
////                wordEntries.add(new WordEntry());
////                Log.d(TAG,"ADDING EMPTY ENTRY");
//            }
        } catch (SQLiteException e){
            Log.e(TAG,"getTopFiveWordEntries Sqlite exception: " + e);
        } catch (Exception e) {
            Log.e(TAG,"getTopFiveWordEntries generic exception: " + e);
        } finally {
            db.close();
        }

        return  wordEntries;
    }

}