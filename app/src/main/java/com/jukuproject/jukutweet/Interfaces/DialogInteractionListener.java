package com.jukuproject.jukutweet.Interfaces;

/**
 * Created by JukuProject on 3/19/2017.
 *
 * Passes click events from a dialog (either add/remove User or add/remove MyList)
 * to MainActivity (from which an InternalDB instance will handle updating db tables)
 */
public interface DialogInteractionListener {
    void onAddUserDialogPositiveClick(String user);
    void onRemoveUserDialogPositiveClick(String user);
    void onUserDialogDismiss();
}