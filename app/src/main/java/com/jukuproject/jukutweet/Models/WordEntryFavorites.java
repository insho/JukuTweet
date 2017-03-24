package com.jukuproject.jukutweet.Models;

import android.util.Log;

import com.jukuproject.jukutweet.Database.InternalDB;

import java.util.ArrayList;

/**
 * Created by JClassic on 3/23/2017.
 * Tracks which favorites lists contain a given word entry
 * to be used in conjunction (usually) with {@link WordEntry}
 */

public class WordEntryFavorites {

    public int getSystemBlueCount() {
        if(systemBlueCount>0) {
            return 1;
        } else {
            return 0;
        }
    }

    public void setSystemBlueCount(int systemBlueCount) {
        this.systemBlueCount = systemBlueCount;
    }

    public int getSystemRedCount() {

        if(systemRedCount>0) {
            return 1;
        } else {
            return 0;
        }

    }

    public void setSystemRedCount(int systemRedCount) {
        this.systemRedCount = systemRedCount;
    }

    public int getSystemYellowCount() {

        if(systemYellowCount>0) {
            return 1;
        } else {
            return 0;
        }

    }

    public void setSystemYellowCount(int systemYellowCount) {
        this.systemYellowCount = systemYellowCount;
    }

    public int getSystemGreenCount() {

        if(systemGreenCount>0) {
            return 1;
        } else {
            return 0;
        }

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

    public void addToUserListCount(int addition) {
        this.userListCount += addition;
    }
    public void subtractFromUserListCount(int subtraction) {
        this.userListCount -= subtraction;
    }

    private int systemBlueCount;
    private int systemRedCount;
    private int systemYellowCount;
    private int systemGreenCount;
    private int userListCount;


    //TODO explain this, we can't include inactive favorites so set them to 0...
    public WordEntryFavorites(int systemBlueCount, int systemRedCount, int systemYellowCount, int systemGreenCount, int userListCount) {
            this.systemBlueCount = systemBlueCount;
            this.systemGreenCount = systemGreenCount;
            this.systemRedCount = systemRedCount;
            this.systemYellowCount = systemYellowCount;
            this.userListCount = userListCount;
    }


    public WordEntryFavorites() {
        this.systemBlueCount = 0;
        this.systemRedCount = 0;
        this.systemYellowCount = 0;
        this.systemGreenCount = 0;
        this.userListCount = 0;
    }

    /**
     * Determines whether, on a favorites star being clicked, the star should toggle
     * through the system lists (blue->green->red->yellow), or have the "favorites popup" window
     * open. The favorites window opens if there are user lists which include this entry, or if there are
     * more than one system list that includes the entry.
     * @return true if should open the favorites popup, false to toggle
     */
    public boolean shouldOpenFavoritePopup(){
        if(userListCount > 0 || getSystemBlueCount() + getSystemRedCount() + getSystemGreenCount() + getSystemYellowCount() > 1 ) {
            return true;
        } else {
            return false;
        }
    }
    public boolean shouldOpenFavoritePopup(ArrayList<String> activeFavoriteLists){
        int totalcount = 0;
        if(activeFavoriteLists.contains("Blue") && getSystemBlueCount() >0){
            totalcount += 1;
            Log.d("TEST","adding blue, total count: " + totalcount);
        }
        if(activeFavoriteLists.contains("Green") && getSystemGreenCount() >0){
            totalcount += 1;
            Log.d("TEST","adding green, total count: " + totalcount);
        }
        if(activeFavoriteLists.contains("Red") && getSystemRedCount() >0){
            totalcount += 1;
            Log.d("TEST","adding Red, total count: " + totalcount);
        }
        if(activeFavoriteLists.contains("Yellow") && getSystemYellowCount() >0){
            totalcount += 1;
            Log.d("TEST","adding Yellow, total count: " + totalcount);
        }
        Log.d("TEST","usercount " + userListCount + ", totalcount: " + totalcount);
        if(userListCount > 0 || totalcount > 1 ) {
            return true;
        } else {
            return false;
        }
    }


    public boolean isEmpty(ArrayList<String> activeFavoriteLists){
        int totalcount = 0;
        if(activeFavoriteLists.contains("Blue") && getSystemBlueCount() >0){
            totalcount += 1;
            Log.d("TEST","adding blue, total count: " + totalcount);
        }
        if(activeFavoriteLists.contains("Green") && getSystemGreenCount() >0){
            totalcount += 1;
            Log.d("TEST","adding green, total count: " + totalcount);
        }
        if(activeFavoriteLists.contains("Red") && getSystemRedCount() >0){
            totalcount += 1;
            Log.d("TEST","adding Red, total count: " + totalcount);
        }
        if(activeFavoriteLists.contains("Yellow") && getSystemYellowCount() >0){
            totalcount += 1;
            Log.d("TEST","adding Yellow, total count: " + totalcount);
        }
        Log.d("TEST","usercount " + userListCount + ", totalcount: " + totalcount);
        if(userListCount + totalcount == 0) {
            return true;
        } else {
            return false;
        }
    }


    public boolean isEmpty(){
        if(userListCount + getSystemBlueCount() + getSystemRedCount() + getSystemGreenCount() + getSystemYellowCount() == 0 ) {
            return true;
        } else {
            return false;
        }
    }

//    public int getOnStarPressedNextPosition(ArrayList<String> preferenceFavorites){
//        if(userListCount > 0 || getSystemBlueCount() + getSystemRedCount() + getSystemGreenCount() + getSystemYellowCount() > 1 ) {
//            return 5; // A "multifavorite"
//        } else if(systemBlueCount + systemRedCount + systemGreenCount + systemYellowCount == 0) {
//            return 0;
//        } else if(preferenceFavorites.contains("Blue") && getSystemBlueCount() > 0) {
//            return 1;
//        } else if(preferenceFavorites.contains("Green") && getSystemGreenCount() > 0) {
//            return 2;
//        } else if(preferenceFavorites.contains("Red") && getSystemRedCount() > 0) {
//            return 3;
//        } else if(preferenceFavorites.contains("Yellow") && getSystemYellowCount() > 0) {
//            return 4;
//        } else {
//            return 0;
//        }
//    }

    public void setSystemColor(String updatedColor) {
        switch (updatedColor) {
            case "Blue":
                systemBlueCount = 1;
                break;
            case "Green":
                systemGreenCount = 1;
                break;
            case "Red":
                systemRedCount  = 1;
                break;
            case "Yellow":
                systemYellowCount =1;
                break;
            default:
                break;
        }
    }

    //TODO REMOVE THIS
    public String testOutput() {
        return "user: " + getUserListCount() + ", blue: " + getSystemBlueCount() + ", red: " + getSystemRedCount() + ", green: " + getSystemGreenCount() + ", yellow: " + getSystemYellowCount();


    }


}
