package com.jukuproject.jukutweet;

import android.support.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jukuproject.jukutweet.Models.SearchTweetsContainer;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserFollowersListContainer;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.UserProfileBanner;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer;
import se.akerfeldt.okhttp.signpost.SigningInterceptor;

/**
 * Created by JClassic on 2/21/2017.
 */

public class TwitterUserClient {

    private static final String TWITTER_BASE_URL = "https://api.twitter.com/1.1/";
    private static TwitterUserClient instance;
    private TwitterService twitterService;

    private TwitterUserClient(String token, String tokenSecret) {
        final Gson gson =
                new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();


        OkHttpClient.Builder client = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpOAuthConsumer consumer = new OkHttpOAuthConsumer(BuildConfig.TWEET_API_KEY, BuildConfig.TWEET_API_SECRET);
        consumer.setTokenWithSecret(token, tokenSecret);

        client.addInterceptor(new SigningInterceptor(consumer));
        client.addInterceptor(loggingInterceptor);

        final Retrofit retrofit = new Retrofit.Builder().baseUrl(TWITTER_BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client.build())
                .build();

        twitterService = retrofit.create(TwitterService.class);
    }

    public static TwitterUserClient getInstance(String token, String tokenSecret) {

        if (instance == null) {
            instance = new TwitterUserClient(token, tokenSecret);
        }
        return instance;
    }

    public Observable<UserInfo> getUserInfo(@NonNull String username) {
        return twitterService.getUserInfo(username);
    }

    public Observable<List<Tweet>> getUserTimeline(@NonNull String username, int count) {
        return twitterService.getUserTimeline(username, count);
    }

    public Observable<UserProfileBanner> getProfileBanner(@NonNull String username) {
        return twitterService.getProfileBanner(username);
    }

    public Observable<UserFollowersListContainer> getFollowersUserInfo(@NonNull String username,Long cursor, int limit) {
        return twitterService.getFollowerUserInfo(username,cursor,limit,false,false);
    }

    public Observable<UserFollowersListContainer> getFriendsUserInfo(@NonNull String username,Long cursor, int limit) {
        return twitterService.getFriendsUserInfo(username,cursor,limit,false,false);
    }

    public Observable<SearchTweetsContainer> getSearchTweets(String query, String languageCode, int limit) {
        return twitterService.getSearchTweets(query, languageCode,limit);
    }

    public Observable<UserFollowersListContainer> getSearchUsers(@NonNull String username, int limit) {
        return twitterService.getSearchUsers(username,limit);
    }

}

