package com.jukuproject.jukutweet;

/**
 * Created by JClassic on 3/19/2017.
 */

public interface DialogInteractionListener {
    void onAddRSSDialogPositiveClick(String user);
    void onAddRSSDialogDismiss();

    void onRemoveRSSDialogPositiveClick(String user);
    void onRemoveRSSDialogDismiss();
}