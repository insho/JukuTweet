package com.jukuproject.jukutweet.Models;

/**
 * Created by JClassic on 3/23/2017.
 * Tracks which favorites lists contain a given word entry
 * to be used in conjunction (usually) with {@link WordEntry}
 */

public class WordEntryFavorites {

    public int getSystemBlueCount() {
        return systemBlueCount;
    }

    public void setSystemBlueCount(int systemBlueCount) {
        this.systemBlueCount = systemBlueCount;
    }

    public int getSystemRedCount() {
        return systemRedCount;
    }

    public void setSystemRedCount(int systemRedCount) {
        this.systemRedCount = systemRedCount;
    }

    public int getSystemYellowCount() {
        return systemYellowCount;
    }

    public void setSystemYellowCount(int systemYellowCount) {
        this.systemYellowCount = systemYellowCount;
    }

    public int getSystemGreenCount() {
        return systemGreenCount;
    }

    public void setSystemGreenCount(int systemGreenCount) {
        this.systemGreenCount = systemGreenCount;
    }

    public int getUserListCount() {
        return userListCount;
    }

    public void setUserListCount(int userListCount) {
        this.userListCount = userListCount;
    }

    private int systemBlueCount;
    private int systemRedCount;
    private int systemYellowCount;
    private int systemGreenCount;
    private int userListCount;


    public WordEntryFavorites(int systemBlueCount, int systemRedCount, int systemYellowCount, int systemGreenCount, int userListCount) {
        this.systemBlueCount = systemBlueCount;
        this.systemRedCount = systemRedCount;
        this.systemYellowCount = systemYellowCount;
        this.systemGreenCount = systemGreenCount;
        this.userListCount = userListCount;
    }


    public WordEntryFavorites() {
        this.systemBlueCount = 0;
        this.systemRedCount = 0;
        this.systemYellowCount = 0;
        this.systemGreenCount = 0;
        this.userListCount = 0;
    }




}
