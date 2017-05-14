package com.jukuproject.jukutweet.Interfaces;


import com.jukuproject.jukutweet.Models.SearchTweetsContainer;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserFollowersListContainer;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.UserProfileBanner;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Collection of get requests for Twitter API. Called from {@link com.jukuproject.jukutweet.TwitterUserClient}
 */
public interface TwitterService {

    /**
     * Accesses Twitter API and gets user info for a Twitter user, if it exists
     * @param username screen_name of twitter user
     * @return {@link UserInfo} object for said twitter user
     */
        @GET("users/show.json") Observable<UserInfo> getUserInfo(@Query("screen_name") String username);

    /**
     * Uses Twitter API to pull twitter banner URL info for a twitter user
     * @param username user screen_name
     * @return {@link UserProfileBanner} object for a given user
     *
     * @see com.jukuproject.jukutweet.TwitterUserClient
     * @see com.jukuproject.jukutweet.Dialogs.AddUserCheckDialog
     */
    @GET("users/profile_banner.json") Observable<UserProfileBanner> getProfileBanner(@Query("screen_name") String username);

    /**
     * Uses Twitter API to pull the "timeline" of recent Tweets for a twitter user
     * @param screenName Screen_name of user
     * @param count max number of results
     * @return Observable list of Tweet objects
     *
     * @see com.jukuproject.jukutweet.TwitterUserClient
     * @see com.jukuproject.jukutweet.Fragments.UserTimeLineFragment
     */
        @GET("statuses/user_timeline.json")
        Observable<List<Tweet>> getUserTimeline(@Query("screen_name") String screenName, @Query("count") int count);


    /**
     * Uses Twitter API to pull more/older entries for the "timeline" of recent Tweets for a twitter user.
     * Like "getUserTimeline" but with the "maxId" variable, enabling the pulling of an older set of tweets.
     * Used in conjunction with {@link #getUserTimeline(String, int)}. First getUserTimeline is called to display a list of tweets,
     * then, if the user scrolls to the bottom of that list, this is called to update the list with a set of older tweets.
     *
     * @param screenName Screen_name of user
     * @param count max number of results
     * @param maxId Used to pull more results. If an id is supplied, only tweets with lower (i.e. OLDER tweets) will be included in result set.
     *              Basically can be used like a "cursor" to pull more results
     * @return Observable list of Tweet objects
     *
     * @see com.jukuproject.jukutweet.TwitterUserClient
     * @see com.jukuproject.jukutweet.Fragments.UserTimeLineFragment
     */
        @GET("statuses/user_timeline.json")
        Observable<List<Tweet>> getMoreUserTimeline(@Query("screen_name") String screenName, @Query("count") int count, @Query("max_id") long maxId);


    /**
     * User Twitter API to pull a list of Followers for a given screen_name
     * @param username User screen_name whose followers are being looked up
     * @param cursor position within result set. Used to continue pulling from a given start position.
     * @param limit max number of results
     * @param skipStatus bool true to not include users status in result set, false to include it
     * @param includeEntities bool true to not include user entities in result set, false to include
     * @return Observable list of UserInfo objects for a User's Followers
     *
     * @see com.jukuproject.jukutweet.TwitterUserClient
     * @see com.jukuproject.jukutweet.Dialogs.UserDetailPopupDialog
     */
        @GET("followers/list.json") Observable<UserFollowersListContainer> getFollowerUserInfo(@Query("screen_name") String username
                , @Query("cursor") Long cursor
                , @Query("limit") int limit
                , @Query("skip_status") Boolean skipStatus
        , @Query("include_user_entities") Boolean includeEntities);


    /**
     * User Twitter API to pull a list of Friends for a given screen_name
     * @param username User screen_name whose friends are being looked up
     * @param cursor position within result set. Used to continue pulling from a given start position.
     * @param limit max number of results
     * @param skipStatus bool true to not include users status in result set, false to include it
     * @param includeEntities bool true to not include user entities in result set, false to include
     * @return Observable list of UserInfo objects for a User's Friends
     *
     * @see com.jukuproject.jukutweet.TwitterUserClient
     * @see com.jukuproject.jukutweet.Dialogs.UserDetailPopupDialog
     */
        @GET("friends/list.json") Observable<UserFollowersListContainer> getFriendsUserInfo(@Query("screen_name") String username
                , @Query("cursor") Long cursor
                , @Query("limit") int limit
                , @Query("skip_status") Boolean skipStatus
                , @Query("include_user_entities") Boolean includeEntities);

    /**
     * Search twitter API for tweets containing a query string
     * @param query query string (i.e. a japanese kanji)
     * @param languageCode 2 digit country code
     * @param limit max number of results
     * @return Observable list of matching Tweet objects
     *
     *  @see com.jukuproject.jukutweet.TwitterUserClient
     *  @see com.jukuproject.jukutweet.Fragments.SearchFragment
     *  @see com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog
     */
        @GET("search/tweets.json") Observable<SearchTweetsContainer> getSearchTweets(@Query("q") String query
                , @Query("lang") String languageCode
                , @Query("count") int limit);

    /**
     * Search twitter API for users
     * @param query search query (user name or screen_name)
     * @param limit max number of results
     * @return Observable list of matching UserInfo objects
     *
     * @see com.jukuproject.jukutweet.TwitterUserClient
     * @see com.jukuproject.jukutweet.Fragments.SearchFragment
     */
        @GET("users/search.json") Observable<List<UserInfo>> getSearchUsers(@Query("q") String query
                , @Query("count") int limit);

}
