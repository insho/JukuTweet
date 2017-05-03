package com.jukuproject.jukutweet.Interfaces;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.WordEntry;

import java.util.ArrayList;

/**
 * Created by JClassic on 4/5/2017.
 */

public interface WordListOperationsInterface {

    boolean duplicateWordList(String listName);
    boolean saveWordList(String listName);
    boolean clearWordList(String listName, boolean isStarFavorite);
    boolean deleteWordList(String listName);
    boolean renameWordList(String oldListName, String newListName);
    boolean changeWordListStarColor(int wordId, String originalColor, String updatedColor);
    boolean addWordToWordList(int wordId, String listName, int listSys);
    boolean removeWordFromWordList(int wordId, String listName, int listSys);
    boolean removeMultipleWordsFromWordList(String concatenatedWordIds, MyListEntry myListEntry);
    ArrayList<MyListEntry> getWordListsForAWord(ArrayList<String> activeFavoriteStars
            , String concatenatedWordIds
            , @Nullable MyListEntry entryToExclude);
    ArrayList<WordEntry> getWordsFromAWordList(MyListEntry myListEntry
            , ColorThresholds colorThresholds
            , String colorString
            , @Nullable Integer excludeIdInteger
            , @Nullable Integer resultLimit);
    boolean addMultipleWordsToWordList(MyListEntry myListEntry, String concatenatedWordIds);
    Cursor getWordListColorBlockCursor(ColorThresholds colorThresholds, MyListEntry myListEntry);
    Cursor getWordEntryForWordId(int kanjiId, ColorThresholds colorThresholds);
    ArrayList<WordEntry> getTopFiveWordEntries(String topOrBottom
            ,@Nullable  ArrayList<Integer> idsToExclude
            ,MyListEntry myListEntry
            ,ColorThresholds colorThresholds
            ,int totalCountLimit
            ,double topbottomThreshold);

    ArrayList<WordEntry> getSearchWordEntriesForKanji(String hiraganaKatakanaPlaceholders
    , String wordIds
    , ColorThresholds colorThresholds);

    ArrayList<WordEntry> getSearchWordEntriesForDefinition(String wordIds, ColorThresholds colorThresholds);

    String getWordIdsForRomajiMatches(ArrayList<String> possibleHiraganaSearchQueries
            ,ArrayList<String> possibleKatakanaSearchQueries);
    String getWordIdsForDefinitionMatch(String query);
    String getWordIdsForKanjiMatch(String kanji);
    Boolean myListContainsWordEntry(MyListEntry myListEntry, WordEntry wordEntry);
//    void supertest(ColorThresholds colorThresholds, MyListEntry myListEntry);
}
