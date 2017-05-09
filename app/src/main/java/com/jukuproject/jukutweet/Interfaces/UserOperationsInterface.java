package com.jukuproject.jukutweet.Interfaces;

import android.database.Cursor;

import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.UserInfo;

/**
 * Created by JClassic on 4/5/2017.
 */

public interface UserOperationsInterface {

    boolean duplicateUser(String user);
    boolean saveUser(UserInfo userInfo);
    boolean deleteUser(String user);
    void addMediaURItoDB(String URI, String screenName);
//    List<UserInfo> getSavedUserInfo();
    Cursor getTweetListColorBlocksCursorForUserInfoMenuHeaders(ColorThresholds colorThresholds);
    boolean compareUserInfoAndUpdate(UserInfo oldUserInfo, UserInfo recentUserInfo);
    boolean saveUserWithoutData(String screenName);
    void updateUserInfo(UserInfo userInfo);
//    ArrayList<String> getListOfUserIds();
}
