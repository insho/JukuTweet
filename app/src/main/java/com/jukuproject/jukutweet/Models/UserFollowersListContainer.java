package com.jukuproject.jukutweet.Models;

import java.util.List;

/**
 * Represents data for a single twitter user in the InternalDB database
 */

public class UserFollowersListContainer {


    private List<UserInfo> users;
    private String next_cursor_str;

    public UserFollowersListContainer() {}

    public List<UserInfo> getUsers() {
        return users;
    }

    public void setUsers(List<UserInfo> users) {
        this.users = users;
    }

    public String getNextCursorString() {
        return next_cursor_str;
    }


}