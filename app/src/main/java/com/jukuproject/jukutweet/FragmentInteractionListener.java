package com.jukuproject.jukutweet;

/**
 * Created by JClassic on 3/19/2017.
 */

public interface FragmentInteractionListener {
    void followUser(final String user);
    void showRemoveDialog(String user);
    void showAddDialog();
    void showProgressBar(Boolean show);
}