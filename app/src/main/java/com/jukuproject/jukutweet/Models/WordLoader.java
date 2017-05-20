package com.jukuproject.jukutweet.Models;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The {@link com.jukuproject.jukutweet.TweetParser} needs to determine which words are kanji, and which are not. Also
 * whether a string of characters is a verb ending or not.
 * To help achieve this, the WordLoader creates a collection of lists from the "Characters" table in the external db.
 *
 * @see com.jukuproject.jukutweet.TweetParser
 */
public class WordLoader {


    private ArrayList<String> hiragana;
    private ArrayList<String> katakana;
    private ArrayList<String> symbols;
    private HashMap<String,ArrayList<String>> romajiMap;
    private HashMap<String,ArrayList<String>> verbEndingMap;
    private ArrayList<String> verbEndingsRoot;
    private ArrayList<String> verbEndingsConjugation;

    public ArrayList<String> getHiragana() {
        return hiragana;
    }

    public ArrayList<String> getKatakana() {
        return katakana;
    }

    public ArrayList<String> getSymbols() {
        return symbols;
    }

    public HashMap<String, ArrayList<String>> getRomajiMap() {
        return romajiMap;
    }

    public ArrayList<String> getVerbEndingsRoot() {
        return verbEndingsRoot;
    }

    public ArrayList<String> getVerbEndingsConjugation() {
        return verbEndingsConjugation;
    }

    public HashMap<String, ArrayList<String>> getVerbEndingMap() {
        return verbEndingMap;
    }

    public ArrayList<String> getExcludedKanji() {
        ArrayList<String> excludedKanji = new ArrayList<>();
        excludedKanji.add("彼の");
        return excludedKanji;
    }

    public WordLoader( ArrayList<String> hiragana,
                       ArrayList<String> katakana,
                       ArrayList<String> symbols,
                       HashMap<String,ArrayList<String>> romajiMap,
                       HashMap<String,ArrayList<String>> verbEndingMap,
                       ArrayList<String> verbEndingsRoot,
                       ArrayList<String> verbEndingsConjugation) {

        this.hiragana = hiragana;
        this.katakana = katakana;
        this.symbols = symbols;
        this.romajiMap =romajiMap;
        this.verbEndingMap = verbEndingMap;
        this.verbEndingsRoot = verbEndingsRoot;
        this.verbEndingsConjugation = verbEndingsConjugation;
    }

}
