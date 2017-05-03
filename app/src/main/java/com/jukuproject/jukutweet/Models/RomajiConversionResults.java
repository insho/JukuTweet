//package com.jukuproject.jukutweet.Models;
//
//import java.lang.reflect.Array;
//import java.util.ArrayList;
//
///**
// * Holds results of the convertRomajitoKanjiOptions method in {@link com.jukuproject.jukutweet.MainActivity}
// * Used when converting romaji query inputs to japanese kanji during a database/twitter search in the search fragment
// */
//
//public class RomajiConversionResults {
//    private ArrayList<String> possibleHiraganaOptions;
//    private ArrayList<String>  possibleKatakanaOptions;
//    private StringBuilder  dbQueryPlaceHolders;
//
//    public RomajiConversionResults (ArrayList<String> possibleHiraganaOptions
//            , ArrayList<String> possibleKatakanaOptions
//            , StringBuilder dbQueryPlaceHolders) {
//        this.possibleHiraganaOptions = possibleHiraganaOptions;
//        this.possibleKatakanaOptions = possibleKatakanaOptions;
//        this.dbQueryPlaceHolders = dbQueryPlaceHolders;
//    }
//
//
//    public ArrayList<String> getPossibleHiraganaOptions() {
//        return possibleHiraganaOptions;
//    }
//
//    public ArrayList<String> getPossibleKatakanaOptions() {
//        return possibleKatakanaOptions;
//    }
//
//    public StringBuilder getDbQueryPlaceHolders() {
//        return dbQueryPlaceHolders;
//    }
//}
