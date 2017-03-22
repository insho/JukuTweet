package com.jukuproject.jukutweet.Models;

import android.support.annotation.NonNull;

import java.util.ArrayList;


public class ParseSentencePossibleKanji {

    private Integer positionInSentence;

    public Integer getListIndex() {
        return listIndex;
    }

    public void setListIndex(Integer listIndex) {
        this.listIndex = listIndex;
    }

    private Integer listIndex;
    private String kanji;
    private ArrayList<String> prefixes;
    private ArrayList<String> suffixes;
    private ArrayList<String> verbCombos;

    public boolean isFoundInDictionary() {
        return foundInDictionary;
    }

    public void setFoundInDictionary(boolean foundInDictionary) {
        this.foundInDictionary = foundInDictionary;
    }

    private boolean foundInDictionary;

    public ArrayList<String> getBetterKanjiMatches() {
        return betterKanjiMatches;
    }

    public void setBetterKanjiMatches(ArrayList<String> betterKanjiMatches) {
        this.betterKanjiMatches = betterKanjiMatches;
    }
    public void replaceBetterKanjiMatch(String replacementMatch) {
        this.betterKanjiMatches = new ArrayList<>();
        betterKanjiMatches.add(replacementMatch);
    }

    private ArrayList<String> betterKanjiMatches;


    public ArrayList<String> getVerbCombos() {
        return verbCombos;
    }

    public void setVerbCombos(ArrayList<String> verbCombos) {
        this.verbCombos = verbCombos;
    }

    public void addToVerbCombos(String combo) {
        verbCombos.add(combo);
    }

    public String getKanji() {
        return kanji;
    }

    public void setKanji(String kanji) {
        this.kanji = kanji;
    }

    public ArrayList<String> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(ArrayList<String> prefixes) {
        this.prefixes = prefixes;
    }

    public ArrayList<String> getSuffixes() {
        return suffixes;
    }

    public void setSuffixes(ArrayList<String> suffixes) {
        this.suffixes = suffixes;
    }

    /** This initializer is used in the SentenceParser */
    public ParseSentencePossibleKanji(@NonNull Integer positionInSentence, @NonNull Integer listIndex, @NonNull String possibleKanji) {
        this.positionInSentence = positionInSentence;
        this.listIndex = listIndex;
        this.kanji = possibleKanji;
        this.prefixes = new ArrayList<>();
        this.suffixes = new ArrayList<>();
        this.verbCombos = new ArrayList<>();
        this.betterKanjiMatches = new ArrayList<>();
        this.foundInDictionary = false;
    }


    public Integer getPositionInSentence() {
        return positionInSentence;
    }

}