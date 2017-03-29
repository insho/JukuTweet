package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by JukuProject on 3/20/2017.
 * Represents data for a single twitter user in the InternalDB database
 */

public class UserInfo implements Parcelable  {


    private String id_str;

    public String getName() {
        if(name == null) {
            return "";
        } else {
            return name;
        }
    }

    private String name;
    private String location;
    private String description;
    private String url;
    private Integer followers_count;
    private Integer friends_count;
    private Integer listed_count;
    private String profile_background_image_url;
    private String profile_image_url;
    private String profile_banner_url;

    private  String profileImageFilePath;

    public String getProfileImageFilePath() {
        return profileImageFilePath;
    }

    public void setProfileImageFilePath(String profileImageFilePath) {
        this.profileImageFilePath = profileImageFilePath;
    }

    private String screen_name;

    public String getBannerUrl() {
        return profile_banner_url;
    }

    public UserInfo() {}

    public UserInfo(String screen_name) {
        this.screen_name = screen_name;
    }

    public void setName(String name) {
        this.name = name;


    }

    public String getScreenName() {
        return screen_name;
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

    public String getFollowerCountString() {

        try {
            return NumberFormat.getNumberInstance(Locale.getDefault()).format(followers_count);
        } catch (Exception e) {
            return "?";
        }

    }

    public Integer getFollowerCount() {
            return followers_count;

    }

    public void setFollowerCount(Integer followers_count) {
        this.followers_count = followers_count;
    }

    public String getFriendCountString() {

        try {
            Log.d("TEST","friends count: " + friends_count );
            return NumberFormat.getNumberInstance(Locale.getDefault()).format(friends_count);
        } catch (Exception e) {
            return "?";
        }


    }
    public Integer getFriendCount() {
        return friends_count;

    }

    public void setFriendCount(Integer friends_count) {
        this.friends_count = friends_count;
    }

//    public String getBannerUrl() {
//        return profile_background_image_url;
//    }

    public String getProfileImageUrl() {
        return profile_image_url;
    }

    public String getProfileImageUrlBig() {
        return profile_image_url.replace("_normal","_bigger");
    }

    public void setProfile_image_url(String profile_image_url) {
        this.profile_image_url = profile_image_url;
    }

    public String getDisplayScreenName() {

        return "\u0040" + screen_name;
    }

    public String getUserId() {
        return id_str;
    }

    public void setUserId(String user_id) {
        this.id_str = user_id;
    }


    // Parcelling part
    public UserInfo(Parcel in){
        String[] data = new String[8];

        in.readStringArray(data);
        this.name = data[0];
        this.screen_name = data[1];
        this.id_str = data[2];
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
        dest.writeStringArray(new String[] {
                this.name,
                this.screen_name,
                this.id_str,
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