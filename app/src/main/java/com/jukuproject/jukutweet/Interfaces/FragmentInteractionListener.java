package com.jukuproject.jukutweet.Interfaces;

/**
 * Created by JukuProject on 3/19/2017.
 */

public interface FragmentInteractionListener {
    void getUserFeed(final String user);
    void showRemoveUserDialog(String user);
    void showFollowUserDialog();
    void showProgressBar(Boolean show);
}