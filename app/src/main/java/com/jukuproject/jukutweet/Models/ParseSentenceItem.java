package com.jukuproject.jukutweet.Models;

import android.support.annotation.Nullable;


public class ParseSentenceItem {


    private boolean isKanji;
    private Integer kanjiID;
    private String kanjiConjugated;
    private String furiganaClean;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public WordEntry getWordEntry() {
        return wordEntry;
    }

    public void setWordEntry(WordEntry wordEntry) {
        this.wordEntry = wordEntry;
    }

    private WordEntry wordEntry;

    public boolean isKanji() {
        return isKanji;
    }

    public Integer getKanjiID() {
        return kanjiID;
    }

    public String getKanjiConjugated() {
        return kanjiConjugated;
    }

    public String getFuriganaClean() {
        return furiganaClean;
    }

    /** This initializer is used in the SentenceParser */
    public ParseSentenceItem(boolean isKanji, @Nullable Integer kanjiID, @Nullable String kanjiConjugated, @Nullable String furiganaClean) {
        this.isKanji = isKanji;
        this.kanjiID = kanjiID;
        this.kanjiConjugated = kanjiConjugated;
        this.furiganaClean = furiganaClean;
    }



}
