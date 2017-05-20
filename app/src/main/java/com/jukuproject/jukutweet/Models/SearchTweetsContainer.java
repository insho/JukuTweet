package com.jukuproject.jukutweet.Models;

import java.util.List;

/**
 * Container for {@link Tweet} objects that is produced from the twitter "search" api call
 *
 * @see com.jukuproject.jukutweet.MainActivity#runTwitterSearch(String, String, Long)
 */
public class SearchTweetsContainer {
    private List<Tweet> statuses;
    public List<Tweet> getTweets() {
        return statuses;
    }
}
