package com.jukuproject.jukutweet.Models;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by JClassic on 3/9/2017.
 */

public class WordLoader {


    private ArrayList<String> hiragana;
    private ArrayList<String> katakana;
    private ArrayList<String> symbols;
    private HashMap<String,ArrayList<String>> romajiMap;
    private HashMap<String,ArrayList<String>> verbEndingMap;
    private ArrayList<String> verbEndingsRoot;
    private ArrayList<String> verbEndingsConjugation;
    private ArrayList<String> excludedKanji;

    public ArrayList<String> getHiragana() {
        return hiragana;
    }

    public void setHiragana(ArrayList<String> hiragana) {
        this.hiragana = hiragana;
    }

    public ArrayList<String> getKatakana() {
        return katakana;
    }

    public void setKatakana(ArrayList<String> katakana) {
        this.katakana = katakana;
    }

    public ArrayList<String> getSymbols() {
        return symbols;
    }

    public void setSymbols(ArrayList<String> symbols) {
        this.symbols = symbols;
    }

    public HashMap<String, ArrayList<String>> getRomajiMap() {
        return romajiMap;
    }

    public void setRomajiMap(HashMap<String, ArrayList<String>> romajiMap) {
        this.romajiMap = romajiMap;
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

    public void addToVerbEndingMap(String root, String conjugation){
        if(verbEndingMap == null) {
            verbEndingMap = new HashMap<>();
        } else if(verbEndingMap.containsKey(root)) {
            ArrayList<String> tmp = verbEndingMap.get(root);
            tmp.add(conjugation);
            verbEndingMap.put(root,tmp);
        } else {
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add(conjugation);
            verbEndingMap.put(root,tmp);
        }
    }

    public ArrayList<String> getVerbEndingsConjugation(String root) {
        return verbEndingMap.get(root);
    }

    public ArrayList<String> getExcludedKanji() {
        excludedKanji = new ArrayList<>();
        excludedKanji.add("彼の");
        return excludedKanji;
    }

    public void setExcludedKanji(ArrayList<String> excludedKanji) {
        this.excludedKanji = excludedKanji;
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
