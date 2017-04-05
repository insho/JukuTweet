package com.jukuproject.jukutweet.Interfaces;

import com.jukuproject.jukutweet.Models.UserInfo;

import java.util.List;

/**
 * Created by JClassic on 4/5/2017.
 */

public interface UserOperationsInterface {

    boolean duplicateUser(String user);
    boolean saveUser(UserInfo userInfo);
    boolean deleteUser(String user);
    void addMediaURItoDB(String URI, String screenName);
    List<UserInfo> getSavedUserInfo();
    boolean compareUserInfoAndUpdate(UserInfo oldUserInfo, UserInfo recentUserInfo);

}
