package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Container for URL info contained in a Tweet object (pulled from twitter API)
 */

public class TweetUrl implements Parcelable {


    public TweetUrl(String dbUrl){
        this.expanded_url = dbUrl;
        this.url = dbUrl;
        this.display_url = dbUrl;
    }

    public void setIndices(int[] indices) {
        this.indices = indices;
    }

    public String getExpanded_url() {
            return expanded_url;
        }

        public String getUrl() {
            return url;
        }

        public int[] getIndices() {
            return indices;
        }

        public String getDisplay_url() {
            return display_url;
        }

        private String expanded_url;
        private String url;
        private int[] indices;
        private String display_url;



        // Parcelling part
        public TweetUrl(Parcel in){

            this.expanded_url = in.readString();
            this.url = in.readString();
            this.indices = in.createIntArray();
            this.display_url = in.readString();

        }

        public int describeContents(){
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            dest.writeString(this.expanded_url);
            dest.writeString(this.url);
            dest.writeIntArray(this.indices);
            dest.writeString(this.display_url);
        }

    public static final Parcelable.Creator<TweetUrl> CREATOR
            = new Parcelable.Creator<TweetUrl>() {
        public TweetUrl createFromParcel(Parcel in) {
            return new TweetUrl(in);
        }

        public TweetUrl[] newArray(int size) {
            return new TweetUrl[size];
        }
    };


}
