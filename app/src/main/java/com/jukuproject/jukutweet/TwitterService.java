package com.jukuproject.jukutweet;

/**
 * Created by JClassic on 3/20/2017.
 */

        import com.jukuproject.jukutweet.Models.Tweet;
        import com.jukuproject.jukutweet.Models.UserInfo;
        import com.jukuproject.jukutweet.Models.UserProfileBanner;

        import java.util.List;
        import retrofit2.http.GET;
        import retrofit2.http.Query;
        import rx.Observable;

public interface TwitterService {

        //Initial call to pull user information and store in DB match it to UserInfo
        @GET("users/show.json") Observable<UserInfo> getUserInfo(@Query("screen_name") String username);

        //Get a smaller version of the user's profile image (404 error if there is no image)
        @GET("users/profile_banner.json") Observable<UserProfileBanner> getProfileBanner(@Query("screen_name") String username);

        //Call to get tweets from that user
        @GET("statuses/user_timeline.json")
        Observable<List<Tweet>> getUserTimeline(@Query("screen_name") String screenName, @Query("count") int count);

}
