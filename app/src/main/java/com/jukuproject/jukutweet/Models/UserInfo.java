package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Represents data for a single twitter user from the Twitter API call
 *
 * @see com.jukuproject.jukutweet.Fragments.UserListFragment
 * @see com.jukuproject.jukutweet.Fragments.UserTimeLineFragment
 * @see com.jukuproject.jukutweet.Dialogs.AddUserDialog
 * @see com.jukuproject.jukutweet.Dialogs.AddUserCheckDialog
 *
 * etc etc
 */

public class UserInfo implements Parcelable  {


    private String id_str;

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



    public String getName() {
        if(name == null) {
            return "";
        } else {
            return name;
        }
    }


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


    public void setScreen_name(String screen_name) {
        this.screen_name = screen_name;
    }

    // Parcelling part
    public UserInfo(Parcel in){

        this.name = in.readString();
        this.screen_name = in.readString();
        this.id_str = in.readString();
        this.location = in.readString();
        this.description = in.readString();
        this.url = in.readString();

        this.followers_count = (Integer)in.readSerializable();
        this.friends_count = (Integer)in.readSerializable();
        this.listed_count = (Integer)in.readSerializable();

        this.profile_background_image_url = in.readString();
        this.profile_image_url = in.readString();
        this.profile_banner_url = in.readString();


    }

    public int describeContents(){
        return 0;
    }



    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.name);
        out.writeString(this.screen_name);
        out.writeString(this.id_str);
        out.writeString(this.location);
        out.writeString(this.description);
        out.writeString(this.url);
        //asdf
        out.writeSerializable(this.followers_count);
        out.writeSerializable(this.friends_count);
        out.writeSerializable(this.listed_count);

        out.writeString(this.profile_background_image_url);
        out.writeString(this.profile_image_url);
        out.writeString(this.profile_banner_url);

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