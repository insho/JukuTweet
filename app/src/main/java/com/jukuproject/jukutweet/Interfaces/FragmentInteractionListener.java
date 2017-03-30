package com.jukuproject.jukutweet.Interfaces;

/**
 * Created by JukuProject on 3/19/2017.
 */

public interface FragmentInteractionListener {
    void tryToGetUserInfo(final String user);
    void showRemoveUserDialog(String user);
    void showAddUserDialog();

    void showAddMyListDialog();
    void showEditMyListDialog(String currentListName, Boolean isStarFavorite);

    void showProgressBar(Boolean show);
    void showActionBarBackButton(Boolean showBack, CharSequence title);
//    void changePagerTitle(int position,String title);
    void showFab(boolean show, String type);
    void showFab(boolean show);

//    void onBackPressed();
    boolean isOnline();

    void updateTabs(String[] updatedTabs);

    void showMenuMyListBrowse(boolean show);

    void notifyFragmentsChanged();
}