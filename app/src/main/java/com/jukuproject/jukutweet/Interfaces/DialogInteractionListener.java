package com.jukuproject.jukutweet.Interfaces;

import com.jukuproject.jukutweet.Models.UserInfo;

/**
 * Created by JukuProject on 3/19/2017.
 *
 * Passes click events from a dialog (either add/remove User or add/remove MyList)
 * to MainActivity (from which an InternalDB instance will handle updating db tables)
 */
public interface DialogInteractionListener {
    void onAddUserDialogPositiveClick(String user);
    void onRemoveUserDialogPositiveClick(String user);
    void onAddMyListDialogPositiveClick(String mylist);

    void onEditMyListDialogPositiveClick(int selectedItem, String listName, boolean isStarFavorite);
    void onDialogDismiss();
    void onRenameMyListDialogPositiveClick(String oldListName, String listName);

    void saveAndUpdateUserInfoList(UserInfo userInfo);
//    void showAddUserCheckDialog();
}