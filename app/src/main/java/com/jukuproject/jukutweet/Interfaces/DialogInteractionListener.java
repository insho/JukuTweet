package com.jukuproject.jukutweet.Interfaces;

/**
 * Created by JukuProject on 3/19/2017.
 *
 *
 */

public interface DialogInteractionListener {
    void onFollowUserDialogPositiveClick(String user);
    void onRemoveUserDialogPositiveClick(String user);
    void onUserDialogDismiss();
}