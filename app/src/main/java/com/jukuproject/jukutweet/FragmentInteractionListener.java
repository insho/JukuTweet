package com.jukuproject.jukutweet;

/**
 * Created by JClassic on 3/19/2017.
 */

public interface FragmentInteractionListener {
    void getUser(final String user);
    void showRemoveDialog(String user);
    void showProgressBar(Boolean show);
}