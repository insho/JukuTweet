package com.jukuproject.jukutweet.Models;

import java.util.ArrayList;

/**
 * Created by JClassic on 3/22/2017.
 */

public class ParseSentenceMatchCombination {

    private Integer matchCount;
    private ArrayList<String> matches;

    public Boolean getNoMatchforFinalKanji() {
        return noMatchforFinalKanji;
    }

    public void setNoMatchforFinalKanji(Boolean noMatchforFinalKanji) {
        this.noMatchforFinalKanji = noMatchforFinalKanji;
    }

    private Boolean noMatchforFinalKanji;


    public ArrayList<Integer> getMatchIDs() {
        return matchIDs;
    }

    public void setMatchIDs(ArrayList<Integer> matchIDs) {
        this.matchIDs = matchIDs;
    }

    public void addMatchID(Integer matchID) {
        if(this.matchIDs == null) {
            this.matchIDs = new ArrayList<>();
        }
        this.matchIDs.add(matchID);
    }

    private ArrayList<Integer> matchIDs;


    public Integer getMatchCount() {
        return matchCount;
    }

    public ArrayList<String> getMatches() {
        return matches;
    }

    public void setMatches(ArrayList<String> matches) {
        this.matches = matches;
    }

    public void addMatches(String match) {
        if(this.matches == null) {
            this.matches = new ArrayList<>();
        }
        this.matches.add(match);
    }

    public void addMatchCount() {
        this.matchCount += 1;
    }

    public String getLastMatch() {
        if(this.matches.size()>1) {
            return this.matches.get(this.matches.size()-1);
        } else {
            return this.matches.get(0);
        }

    }


    public ParseSentenceMatchCombination(){
        this.matches = new ArrayList<>();
        this.matchIDs = new ArrayList<>();
        this.matchCount = 0;
        this.noMatchforFinalKanji = false;
    };



}
