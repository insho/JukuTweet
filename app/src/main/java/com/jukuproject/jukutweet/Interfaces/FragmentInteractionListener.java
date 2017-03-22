package com.jukuproject.jukutweet.Interfaces;

import com.jukuproject.jukutweet.Models.UserInfo;

/**
 * Created by JukuProject on 3/19/2017.
 */

public interface FragmentInteractionListener {
    void tryToGetUserInfo(final String user);
    void showRemoveUserDialog(String user);
    void showAddUserDialog();
    void showProgressBar(Boolean show);
    void showActionBarBackButton(Boolean showBack, CharSequence title);
    void changePagerTitle(int position,String title);
}