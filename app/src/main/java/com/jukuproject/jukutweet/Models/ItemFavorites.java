package com.jukuproject.jukutweet.Models;

import java.util.ArrayList;

/**
 * Created by JClassic on 3/23/2017.
 * Tracks which favorites lists contain a given {@link WordEntry} OR {@link Tweet}
 */

public class ItemFavorites {

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
    private int systemPurpleCount;

    public int getSystemOrangeCount() {
        return systemOrangeCount;
    }

    public void setSystemOrangeCount(int systemOrangeCount) {
        this.systemOrangeCount = systemOrangeCount;
    }

    public int getSystemPurpleCount() {
        return systemPurpleCount;
    }

    public void setSystemPurpleCount(int systemPurpleCount) {
        this.systemPurpleCount = systemPurpleCount;
    }

    private int systemOrangeCount;

    private int userListCount;


    //TODO explain this, we can't include inactive favorites so set them to 0...
    public ItemFavorites(int systemBlueCount
            , int systemRedCount
            , int systemYellowCount
            , int systemGreenCount
            , int systemPurpleCount
            , int systemOrangeCount
            , int userListCount) {
            this.systemBlueCount = systemBlueCount;
            this.systemGreenCount = systemGreenCount;
            this.systemRedCount = systemRedCount;
            this.systemYellowCount = systemYellowCount;
            this.systemPurpleCount = systemPurpleCount;
            this.systemOrangeCount = systemOrangeCount;

            this.userListCount = userListCount;
    }


    public ItemFavorites() {
        this.systemBlueCount = 0;
        this.systemRedCount = 0;
        this.systemYellowCount = 0;
        this.systemGreenCount = 0;
        this.systemPurpleCount = 0;
        this.systemOrangeCount = 0;

        this.userListCount = 0;

    }

    /**
     * Determines whether, on a favorites star being clicked, the star should toggle
     * through the system lists (blue->green->red->yellow), or have the "favorites popup" window
     * open. The favorites window opens if there are user lists which include this entry, or if there are
     * more than one system list that includes the entry.
     * @return true if should open the favorites popup, false to toggle
     */
//    public boolean shouldOpenFavoritePopup(){
//        if(userListCount > 0 || getSystemBlueCount() + getSystemRedCount() + getSystemGreenCount() + getSystemYellowCount() > 1 ) {
//            return true;
//        } else {
//            return false;
//        }
//    }
    public boolean shouldOpenFavoritePopup(ArrayList<String> activeFavoriteLists){
        int totalcount = 0;
        if(activeFavoriteLists.contains("Blue") && getSystemBlueCount() >0){
            totalcount += 1;
        }
        if(activeFavoriteLists.contains("Green") && getSystemGreenCount() >0){
            totalcount += 1;
        }
        if(activeFavoriteLists.contains("Red") && getSystemRedCount() >0){
            totalcount += 1;
        }
        if(activeFavoriteLists.contains("Yellow") && getSystemYellowCount() >0){
            totalcount += 1;
        }

        if(activeFavoriteLists.contains("Purple") && getSystemPurpleCount() >0){
            totalcount += 1;
        }

        if(activeFavoriteLists.contains("Orange") && getSystemOrangeCount() >0){
            totalcount += 1;
        }

        if(userListCount > 0 || totalcount > 1 ) {
            return true;
        } else {
            return false;
        }
    }

    public int systemListCount(ArrayList<String> activeFavoriteLists){
        int totalcount = 0;
        if(activeFavoriteLists.contains("Blue") && getSystemBlueCount() >0){
            totalcount += 1;
        }
        if(activeFavoriteLists.contains("Green") && getSystemGreenCount() >0){
            totalcount += 1;
        }
        if(activeFavoriteLists.contains("Red") && getSystemRedCount() >0){
            totalcount += 1;
        }
        if(activeFavoriteLists.contains("Yellow") && getSystemYellowCount() >0){
            totalcount += 1;
        }

        if(activeFavoriteLists.contains("Purple") && getSystemPurpleCount() >0){
            totalcount += 1;
        }

        if(activeFavoriteLists.contains("Orange") && getSystemOrangeCount() >0){
            totalcount += 1;
        }
        return totalcount;
    }

    public boolean isEmpty(ArrayList<String> activeFavoriteLists){
        int totalcount = 0;
        if(activeFavoriteLists.contains("Blue") && getSystemBlueCount() >0){
            totalcount += 1;
        }
        if(activeFavoriteLists.contains("Green") && getSystemGreenCount() >0){
            totalcount += 1;
        }
        if(activeFavoriteLists.contains("Red") && getSystemRedCount() >0){
            totalcount += 1;
        }
        if(activeFavoriteLists.contains("Yellow") && getSystemYellowCount() >0){
            totalcount += 1;
        }

        if(activeFavoriteLists.contains("Purple") && getSystemPurpleCount() >0){
            totalcount += 1;
        }

        if(activeFavoriteLists.contains("Orange") && getSystemOrangeCount() >0){
            totalcount += 1;
        }

        if(userListCount + totalcount == 0) {
            return true;
        } else {
            return false;
        }
    }


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
            case "Purple":
                systemPurpleCount =1;
                break;
            case "Orange":
                systemOrangeCount =1;
                break;
            default:
                break;
        }
    }


}
