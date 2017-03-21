package com.jukuproject.jukutweet.Interfaces;

import com.jukuproject.jukutweet.Models.UserInfo;

/**
 * Created by JukuProject on 3/19/2017.
 */

public interface FragmentInteractionListener {
    void getUserInfo(final String user);
    void showRemoveUserDialog(String user);
    void showFollowUserDialog();
    void showProgressBar(Boolean show);
    void showTimeLine(UserInfo userInfo);
}