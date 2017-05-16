package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JClassic on 3/20/2017.
 */

public class TweetEntities  implements Parcelable {



    private List<TweetUrl> urls;
    private List<TweetUserMentions> user_mentions;


    public List<TweetUrl> getUrls() {
        return urls;
    }

    public TweetEntities() {
        this.urls = new ArrayList<>();
        this.user_mentions = new ArrayList<>();
    }

    public void setUrls(List<TweetUrl> urls) {
        this.urls = urls;
    }

    public List<TweetUserMentions> getUser_mentions() {
        return user_mentions;
    }

    public void setUser_mentions(List<TweetUserMentions> user_mentions) {
        this.user_mentions = user_mentions;
    }

    // Parcelling part
    public TweetEntities(Parcel in){
        urls = in.createTypedArrayList(TweetUrl.CREATOR);
        user_mentions = in.createTypedArrayList(TweetUserMentions.CREATOR);
//        this.urls = in.readArrayList(TweetUrl.class.getClassLoader());
//        this.user_mentions = in.readArrayList(TweetUserMentions.class.getClassLoader());
    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(urls);
        dest.writeList(this.user_mentions);
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


