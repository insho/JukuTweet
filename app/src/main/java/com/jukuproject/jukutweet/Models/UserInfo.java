package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by JukuProject on 3/20/2017.
 * Represents data for a single twitter user in the InternalDB database
 */

public class UserInfo implements Parcelable  {
    public UserInfo() {}

    public UserInfo(String screen_name) {
        this.screen_name = screen_name;
        this.displayName = "\u0040" + screen_name;
    }

    public String getScreenName() {
        return screen_name;
    }

    public void setScreenName(String screen_name) {
        this.screen_name = screen_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFollowers_count() {
        if(followers_count == null) {
            return "";
        } else {
            return String.valueOf(followers_count);
        }

    }


    public void setFollowers_count(Integer followers_count) {
        this.followers_count = followers_count;
    }

    public String getFriends_count() {
        if(friends_count == null) {
            return "";
        } else {
            return String.valueOf(friends_count);
        }

    }

    public void setFriends_count(Integer friends_count) {
        this.friends_count = friends_count;
    }

    public Integer getListed_count() {
        return listed_count;
    }

    public void setListed_count(Integer listed_count) {
        this.listed_count = listed_count;
    }

    public String getProfile_background_image_url() {
        return profile_background_image_url;
    }

    public void setProfile_background_image_url(String profile_background_image_url) {
        this.profile_background_image_url = profile_background_image_url;
    }

    public String getProfile_image_url() {
        return profile_image_url;
    }

    public void setProfile_image_url(String profile_image_url) {
        this.profile_image_url = profile_image_url;
    }

    public String getProfile_banner_url() {
        return profile_banner_url;
    }

    public void setProfile_banner_url(String profile_banner_url) {
        this.profile_banner_url = profile_banner_url;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    private String displayName;
    private String screen_name;
    private String id;
    private String location;
    private String description;
    private String url;
    private Integer followers_count;
    private Integer friends_count;
    private Integer listed_count;
    private String profile_background_image_url;
    private String profile_image_url;
    private String profile_banner_url;

    // Parcelling part
    public UserInfo(Parcel in){
        String[] data = new String[8];

        in.readStringArray(data);
        this.displayName = data[0];
        this.screen_name = data[1];
        this.id = data[2];
        this.location = data[3];
        this.description = data[4];
        this.url = data[5];

        this.followers_count = Integer.parseInt(data[6]);
        this.friends_count = Integer.parseInt(data[7]);
        this.listed_count = Integer.parseInt(data[8]);

        this.profile_background_image_url = data[9];
        this.profile_image_url = data[10];
        this.profile_banner_url = data[11];


    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.displayName,
                this.screen_name,
                this.id,
                this.location,
                this.description,
                this.url,
                String.valueOf(this.followers_count),
                String.valueOf(this.friends_count),
                String.valueOf(this.listed_count),
                this.profile_background_image_url,
                this.profile_image_url,
                this.profile_banner_url

        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

}