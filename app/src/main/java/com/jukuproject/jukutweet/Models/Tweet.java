package com.jukuproject.jukutweet.Models;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by JClassic on 3/20/2017.
 */

public class Tweet {
    private Boolean favorited;
    private Boolean truncated;
    private String created_at;
    private String id;
    private Integer retweet_count;
    private String text;


    /* Each time a saved user's timeline is clicked, pull the user info
    * (if it exists) within the api response, and check it against the userInfo from the db and
    * update db if necessary with new user data */
    private UserInfo user;
    public UserInfo getUser() {
        return user;
    }






    public Tweet() {};


    public Tweet(String text) {
        this.text = text;
    }

    public Tweet(Boolean favorited, Boolean truncated, String created_at, String id, Integer retweet_count, String text) {
        this.favorited = favorited;
        this.truncated = truncated;
        this.created_at = created_at;
        this.id = id;
        this.retweet_count = retweet_count;
        this.text = text;
    }

    public Boolean getFavorited() {
        return favorited;
    }

    public void setFavorited(Boolean favorited) {
        this.favorited = favorited;
    }

    public Boolean getTruncated() {
        return truncated;
    }

    public void setTruncated(Boolean truncated) {
        this.truncated = truncated;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getDisplayDate() {
        try {


//            String strCurrentDate = "Wed, 18 Apr 2012 07:55:29 +0000"; Mon Mar 20 12:09:12 +0000 2017
            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy",Locale.getDefault());
            Date newDate;
            try {
                newDate = format.parse(created_at);
            } catch (ParseException e) {
                Log.e("TEST-Tweet","Tweet object date parse exception: " + e);
                return "";
            } catch (NullPointerException e) {
                Log.e("TEST-Tweet","Tweet object getdisplaydate fail: " + e);
                return "";
            } catch (IllegalArgumentException e) {
                Log.e("TEST-Tweet","Tweet object illegal arg exception: " + e);
                return "";
            }

//            format = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy HH:mm", Locale.getDefault());
//            String date = ;

            return formatter.format(newDate);


        } catch (NullPointerException e) {
            Log.e("TEST-Tweet","Tweet object getdisplaydate fail: " + e);
            return "";
        } catch (IllegalArgumentException e) {
            Log.e("TEST-Tweet","Tweet object illegal arg exception: " + e);
            return "";
        }

    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRetweet_count() {
        return retweet_count;
    }

    public void setRetweet_count(Integer retweet_count) {
        this.retweet_count = retweet_count;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}


