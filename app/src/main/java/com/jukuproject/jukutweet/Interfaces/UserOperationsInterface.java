package com.jukuproject.jukutweet.Interfaces;

import android.content.Context;
import android.database.Cursor;

import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.UserInfo;

/**
 * Collection of internal sqlite database calls related to User operations.
 *
 * @see com.jukuproject.jukutweet.MainActivity
 * @see com.jukuproject.jukutweet.Dialogs.AddUserDialog
 * @see com.jukuproject.jukutweet.Dialogs.UserDetailPopupDialog
 * @see com.jukuproject.jukutweet.Dialogs.RemoveUserDialog
 *
 */
public interface UserOperationsInterface {

    boolean duplicateUser(String user);
    boolean saveUser(UserInfo userInfo);
    boolean deleteUser(String user);
    void addUserIconURItoDB(String URI, String screenName);
    void downloadUserIcon(final Context context, String imageUrl, final String screenName);
    void downloadTweetUserIcon(final Context context, String imageUrl, final String userId);
    //    List<UserInfo> getSavedUserInfo();
    Cursor getTweetListColorBlocksCursorForUserInfoMenuHeaders(ColorThresholds colorThresholds);
    boolean compareUserInfoAndUpdate(UserInfo oldUserInfo, UserInfo recentUserInfo);
    boolean saveUserWithoutData(String screenName);
    void updateUserInfo(UserInfo userInfo);
    void clearOutUnusedUserIcons(Context context);
//    ArrayList<String> getListOfUserIdsinDB();
//    ArrayList<String> getListOfUserIds();
}
