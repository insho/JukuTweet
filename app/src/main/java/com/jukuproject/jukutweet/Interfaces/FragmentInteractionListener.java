package com.jukuproject.jukutweet.Interfaces;

import android.support.annotation.Nullable;

import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;

/**
 * Manages interactions between fragments and {@link com.jukuproject.jukutweet.MainActivity}
 */
public interface FragmentInteractionListener {
    void getInitialUserInfoForAddUserCheck(final String user);
    void showRemoveUserDialog(UserInfo userInfo);
    void showEditMyListDialog(String listType, String currentListName, Boolean isStarFavorite);
    void showProgressBar(Boolean show);
    void showActionBarBackButton(Boolean showBack, CharSequence title, int tabNumber);
    void showFab(boolean show, String type);
    void showFab(boolean show);
    boolean isOnline();
    void updateTabs(String[] updatedTabs);
    void showMenuMyListBrowse(boolean show, int tabNumber);
    void notifySavedWordFragmentsChanged(String wordEntriesString);
    void notifySavedTweetFragmentsChanged();
    void onBackPressed();
    void runDictionarySearch(String query, String queryOn);
    void runTwitterSearch(String query, String queryOn, @Nullable Long maxId);
    void showAddUserCheckDialog(UserInfo userInfo);
    void parseAndSaveTweet(Tweet tweet);
    void downloadTweetUserIcons(UserInfo userInfo);
}