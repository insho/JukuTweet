package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by JClassic on 5/4/2017.
 */

public class TweetUserMentions implements Parcelable {

    private String screen_name;
    private String name;
    private String id_str;
    private int[] indices;

    public TweetUserMentions(String screen_name, String name, String id_str) {
        this.screen_name = screen_name;
        this.name = name;
        this.id_str = id_str;
    }


    public void setIndices(int[] indices) {
        this.indices = indices;
    }

    public String getScreen_name() {
        return screen_name;
    }

    public String getName() {
        return name;
    }

    public String getId_str() {
        return id_str;
    }

    public int[] getIndices() {
        return indices;
    }

    // Parcelling part
    public TweetUserMentions(Parcel in){

        this.screen_name = in.readString();
        this.name = in.readString();
        this.id_str = in.readString();
        this.indices = in.createIntArray();

    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(this.screen_name);
        dest.writeString(this.name);
        dest.writeString(this.id_str);
        dest.writeIntArray(this.indices);
    }

    public static final Parcelable.Creator<TweetUserMentions> CREATOR
            = new Parcelable.Creator<TweetUserMentions>() {
        public TweetUserMentions createFromParcel(Parcel in) {
            return new TweetUserMentions(in);
        }

        public TweetUserMentions[] newArray(int size) {
            return new TweetUserMentions[size];
        }
    };


}
