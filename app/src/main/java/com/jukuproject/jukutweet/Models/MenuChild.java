package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.jukuproject.jukutweet.Adapters.TweetListExpandableAdapter;
import com.jukuproject.jukutweet.Adapters.WordListExpandableAdapter;

/**
 * Container with information for a row in a Word or Tweet list
 *
 * @see com.jukuproject.jukutweet.Fragments.TweetListFragment
 * @see TweetListExpandableAdapter
 * @see com.jukuproject.jukutweet.Fragments.WordListFragment
 * @see WordListExpandableAdapter
 */

public class MenuChild implements Parcelable {
    private String childTitle;
    private ColorBlockMeasurables colorBlockMeasurables;
    private UserInfo userInfo;

    public MenuChild(String childTitle) {
        this.childTitle = childTitle;
        this.colorBlockMeasurables = new ColorBlockMeasurables();
        this.userInfo = new UserInfo();
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }


    public ColorBlockMeasurables getColorBlockMeasurables() {
        return colorBlockMeasurables;
    }

    public void setColorBlockMeasurables(ColorBlockMeasurables colorBlockMeasurables) {
        this.colorBlockMeasurables = colorBlockMeasurables;
    }

    public String getChildTitle() {
        return childTitle;
    }

    // Parcelling part
    public MenuChild(Parcel in){

        this.childTitle = in.readString();
        this.colorBlockMeasurables = in.readParcelable(ColorBlockMeasurables.class.getClassLoader());
        this.userInfo = in.readParcelable(UserInfo.class.getClassLoader());
    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.childTitle);
        if(this.colorBlockMeasurables==null) {setColorBlockMeasurables(new ColorBlockMeasurables());}
        dest.writeParcelable(this.colorBlockMeasurables,flags);
        if(this.userInfo==null) {setUserInfo(new UserInfo());}
        dest.writeParcelable(this.userInfo,flags);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MenuChild createFromParcel(Parcel in) {
            return new MenuChild(in);
        }

        public MenuChild[] newArray(int size) {
            return new MenuChild[size];
        }
    };


}
