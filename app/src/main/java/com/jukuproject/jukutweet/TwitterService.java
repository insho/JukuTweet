package com.jukuproject.jukutweet;

/**
 * Created by JClassic on 3/20/2017.
 */

        import com.jukuproject.jukutweet.Models.Tweet;
        import com.jukuproject.jukutweet.Models.TweetCollection;
        import com.jukuproject.jukutweet.Models.UserInfo;

        import java.util.ArrayList;
        import java.util.List;
        import retrofit2.http.GET;
        import retrofit2.http.Path;
        import retrofit2.http.Query;
        import rx.Observable;

public interface TwitterService {

        //Initial call to pull user information and store in DB match it to UserInfo
        @GET("users/show.json") Observable<UserInfo> getUserInfo(@Query("screen_name") String username);
        //Call to get tweets from that user
//        GET https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=twitterapi&count=2
        @GET("statuses/user_timeline.json?screen_name={username}&count={tweetcount}") Observable<ArrayList<Tweet>> getLatestTweets(@Path("username") String username, @Path("tweetcount") String count);


//    @GET("{requesttype}/{section}/{time}.json") Observable<NYTimesArticleWrapper> getArticles(@Path("requesttype") String requesttype, @Path("section") String section, @Path("time") String time,  @Query("api-key") String apiKey);

}
