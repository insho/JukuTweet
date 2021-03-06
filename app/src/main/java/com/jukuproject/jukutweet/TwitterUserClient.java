package com.jukuproject.jukutweet;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jukuproject.jukutweet.Interfaces.TwitterService;
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
 * twitter service client for retrofit call to twitter api
 *
 * @see TwitterService
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


    /**
     * Accesses Twitter API and gets user info for a Twitter user, if it exists
     * @param username screen_name of twitter user
     *
     * @see TwitterService
     * @return {@link TwitterService} Observable object for {@link UserInfo} from twitter API
     */
    public Observable<UserInfo> getUserInfo(@NonNull String username) {
        return twitterService.getUserInfo(username);
    }


    /**
     * Uses Twitter API to pull the "timeline" of recent Tweets for a twitter user
     * @param username Screen_name of user
     * @param count max number of results
     * @return Observable list of Tweet objects
     *
     * @see TwitterService#getUserTimeline(String, int)
     * @see TwitterService#getMoreUserTimeline(String, int, long)
     * @see com.jukuproject.jukutweet.Fragments.UserTimeLineFragment
     */
    public Observable<List<Tweet>> getUserTimeline(@NonNull String username, int count, @Nullable Long maxId) {
        if(maxId==null) {
            return twitterService.getUserTimeline(username, count);
        } else {
            return twitterService.getMoreUserTimeline(username,count,maxId);
        }
    }


    /**
     * Uses Twitter API to pull twitter banner URL info for a twitter user
     * @param username user screen_name
     * @return {@link UserProfileBanner} object for a given user
     *
     * @see com.jukuproject.jukutweet.Dialogs.AddUserCheckDialog
     * @see TwitterService#getProfileBanner(String)
     */
    public Observable<UserProfileBanner> getProfileBanner(@NonNull String username) {
        return twitterService.getProfileBanner(username);
    }


    /**
     * User Twitter API to pull a list of Followers for a given screen_name
     * @param username User screen_name whose followers are being looked up
     * @param cursor position within result set. Used to continue pulling from a given start position.
     * @param limit max number of results
     * @return Observable list of UserInfo objects for a User's Followers
     *
     * @see com.jukuproject.jukutweet.Dialogs.UserDetailPopupDialog
     * @see TwitterService#getFollowerUserInfo(String, Long, int, Boolean, Boolean)
     */
    public Observable<UserFollowersListContainer> getFollowersUserInfo(@NonNull String username,Long cursor, int limit) {
        return twitterService.getFollowerUserInfo(username,cursor,limit,false,false);
    }


    /**
     * User Twitter API to pull a list of Friends for a given screen_name
     * @param username User screen_name whose friends are being looked up
     * @param cursor position within result set. Used to continue pulling from a given start position.
     * @param limit max number of results
     * @return Observable list of UserInfo objects for a User's Friends
     *
     * @see com.jukuproject.jukutweet.Dialogs.UserDetailPopupDialog
     * @see TwitterService#getFriendsUserInfo(String, Long, int, Boolean, Boolean)
     */
    public Observable<UserFollowersListContainer> getFriendsUserInfo(@NonNull String username,Long cursor, int limit) {
        return twitterService.getFriendsUserInfo(username,cursor,limit,false,false);
    }

    /**
     * Search twitter API for tweets containing a query string
     * @param query query string (i.e. a japanese kanji)
     * @param languageCode 2 digit country code
     * @param limit max number of results
     * @return Observable list of matching Tweet objects
     *
     *  @see com.jukuproject.jukutweet.Fragments.SearchFragment
     *  @see com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog
     *  @see TwitterService#getSearchTweets(String, String, int)
     *  @see TwitterService#getMoreSearchTweets(String, String, int, long)
     */
    public Observable<SearchTweetsContainer> getSearchTweets(String query, String languageCode, int limit, @Nullable Long maxId) {
        String queryStringWithQuotes = "\"" + query + "\"";

        if(maxId==null) {
            return twitterService.getSearchTweets(queryStringWithQuotes, languageCode,limit);
        } else {
            return twitterService.getMoreSearchTweets(queryStringWithQuotes, languageCode,limit,maxId);
        }

    }

    /**
     * Search twitter API for users
     * @param username search query (user name or screen_name)
     * @param limit max number of results
     * @return Observable list of matching UserInfo objects
     *
     * @see com.jukuproject.jukutweet.Fragments.SearchFragment
     * @see TwitterService#getSearchUsers(String, int)
     */
    public Observable<List<UserInfo>> getSearchUsers(@NonNull String username, int limit) {
        return twitterService.getSearchUsers(username,limit);
    }

}

