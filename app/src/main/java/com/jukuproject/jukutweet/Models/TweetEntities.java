package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by JClassic on 3/20/2017.
 */

public class TweetEntities  implements Parcelable {

    public List<TweetUrl> getUrls() {
        return urls;
    }

    public TweetEntities(List<TweetUrl> urls) {

        this.urls = urls;
    }

    private List<TweetUrl> urls;


    // Parcelling part
    public TweetEntities(Parcel in){
        urls = in.createTypedArrayList(TweetUrl.CREATOR);
    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(urls);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public TweetEntities createFromParcel(Parcel in) {
            return new TweetEntities(in);
        }

        public TweetEntities[] newArray(int size) {
            return new TweetEntities[size];
        }
    };


}


