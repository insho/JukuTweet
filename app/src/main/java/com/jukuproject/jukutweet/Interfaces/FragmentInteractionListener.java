package com.jukuproject.jukutweet.Interfaces;

import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;

/**
 * Created by JukuProject on 3/19/2017.
 */

public interface FragmentInteractionListener {
    void getInitialUserInfoForAddUserCheck(final String user);
    void showRemoveUserDialog(UserInfo userInfo);
    void showAddUserDialog();
    void showEditMyListDialog(String listType, String currentListName, Boolean isStarFavorite);
    void showProgressBar(Boolean show);
    void showActionBarBackButton(Boolean showBack, CharSequence title);
    void showFab(boolean show, String type);
    void showFab(boolean show);
    boolean isOnline();
    void updateTabs(String[] updatedTabs);
    void showMenuMyListBrowse(boolean show, int tabNumber);

    void notifySavedTweetFragmentsChanged();
    void notifySavedWordFragmentsChanged();

    void showSavedTweetsTabForIndividualUser(UserInfo userInfo);
    void onBackPressed();
    void showUserDetailFragment(UserInfo userInfo);
//void refreshFragment(String fragmentTag);
    void runDictionarySearch(String query, String queryOn);
    void runTwitterSearch(String query, String queryOn);
    void showAddUserCheckDialog(UserInfo userInfo);

    void parseAndSaveTweet(Tweet tweet);
}