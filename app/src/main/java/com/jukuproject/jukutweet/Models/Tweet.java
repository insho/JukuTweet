package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by JClassic on 3/20/2017.
 */

public class Tweet  implements Parcelable {

    private Boolean favorited;
    private Integer favorite_count;
    private Boolean truncated;
    private String created_at;
    private String id_str;
    private Integer retweet_count;
    private String text;

    public Tweet(){};

    /* Copy tweets when pulling saved tweets from the database */
    public Tweet(Tweet another) {
        this.favorited = another.favorited;
        this.favorite_count = another.favorite_count;
        this.truncated= another.truncated;
        this.created_at= another.created_at;
        this.id_str= another.id_str;
        this.retweet_count= another.retweet_count;
        this.text= another.text;
        this.user = another.user;
        this.wordEntries = another.wordEntries;
    }

    public Tweet(String text) {
        this.text = text;
        wordEntries = new ArrayList<>();
    }

    public Tweet(Boolean favorited, Boolean truncated, String created_at, String id_str, Integer retweet_count, String text) {
        this.favorited = favorited;
        this.truncated = truncated;
        this.created_at = created_at;
        this.id_str = id_str;
        this.retweet_count = retweet_count;
        this.text = text;
    }


    public ArrayList<WordEntry> getWordEntries() {
        return wordEntries;
    }

    private ArrayList<WordEntry> wordEntries;

    public ItemFavorites getItemFavorites() {
        return itemFavorites;
    }

    public void setItemFavorites(ItemFavorites itemFavorites) {
        this.itemFavorites = itemFavorites;
    }

    private ItemFavorites itemFavorites;

    public TweetEntities getEntities() {
        return entities;
    }
    private TweetEntities entities;

    /* Each time a saved user's timeline is clicked, pull the user info
    * (if it exists) within the api response, and check it against the userInfo from the db and
    * update db if necessary with new user data */
    private UserInfo user;
    public UserInfo getUser() {
        if(user == null) {
            user = new UserInfo();
        }
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }



    public void addWordEntry(WordEntry entry) {
        if(wordEntries  == null){
            wordEntries = new ArrayList<>();
        }
        wordEntries.add(entry);
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

    public String getCreatedAt() {
        return created_at;
    }



    public String getDatabaseInsertDate() {
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
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
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



    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }

    public String getIdString() {
        return id_str;
    }

    public void setIdString(String id_str) {
        this.id_str = id_str;
    }

    public Integer getRetweet_count() {
        return retweet_count;
    }

    public String getRetweetCountString() {
        try {
            if(retweet_count != null) {
                return NumberFormat.getNumberInstance(Locale.getDefault()).format(retweet_count);

            } else {
                return "?";
            }

        } catch (Exception e) {
            Log.e("TEST-Tweet","Tweet object displayretweetcount: " + e.toString());
            return "?";
        }
    }


//    ArrayList<>
//    public String getDisplayFavoritesCount() {
//        try {
//            return NumberFormat.getNumberInstance(Locale.getDefault()).format(favourites_count);
//        } catch (NullPointerException e) {
//            Log.e("TEST-Tweet","Tweet object getDisplayFavoritesCount: " + e);
//            return "";
//        }
//    }

    public String getFavoritesCountString() {
        try {
            if(favorite_count != null) {
                return NumberFormat.getNumberInstance(Locale.getDefault()).format(favorite_count);

            } else {
                return "?";
            }
        } catch (Exception e) {
            Log.e("TEST-Tweet","Tweet object getFavoritesCountString: " + e.toString());
            return "?";
        }
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


    // Parcelling part
    public Tweet(Parcel in){

        this.favorited = in.readByte() != 0;
        this.truncated = in.readByte() != 0;
        this.created_at = in.readString();
        this.id_str = in.readString();
        this.retweet_count = in.readInt();
        this.favorite_count  = in.readInt();
        this.text = in.readString();



    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {




        dest.writeByte((byte) (this.favorited ? 1 : 0));
        dest.writeByte((byte) (this.truncated ? 1 : 0));
        dest.writeString(this.created_at);
        dest.writeString(this.id_str);
        dest.writeInt(this.retweet_count);
        dest.writeInt(this.favorite_count);
        dest.writeString(this.text);


    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Tweet createFromParcel(Parcel in) {
            return new Tweet(in);
        }

        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };




}


