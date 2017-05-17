package com.jukuproject.jukutweet.Interfaces;

/**
 * Passes the "remove user" click event from {@link com.jukuproject.jukutweet.Dialogs.UserDetailPopupDialog}
 * to MainActivity (where an InternalDB instance will handle updating db tables) and refreshing {@link com.jukuproject.jukutweet.Fragments.UserListFragment}
 */
public interface DialogRemoveUserInteractionListener {
    void onRemoveUserDialogPositiveClick(String user);
}
