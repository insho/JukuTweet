package com.jukuproject.jukutweet.Models;

import java.util.List;

/**
 * Created by JClassic on 4/19/2017.
 */

public class SearchTweetsContainer {


    private List<Tweet> statuses;


    //    private Integer next_cursor;
//    private String next_cursor_str;
//    //    private Integer previous_cursor;
//    private String previous_cursor_str;
//
//    public UserFollowersListContainer() {}
//
    public List<Tweet> getTweets() {
        return statuses;
    }

//    public void setUsers(List<UserInfo> users) {
//        this.users = users;
//    }
//
//    public String getNextCursorString() {
//        return next_cursor_str;
//    }


}
