package com.jukuproject.jukutweet.Interfaces;

import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.UserInfo;

import java.util.ArrayList;

/**
 * Created by JukuProject on 3/19/2017.
 *
 * Passes click events from a dialog (either add/remove User or add/remove MyList)
 * to MainActivity (from which an InternalDB instance will handle updating db tables)
 */
public interface DialogInteractionListener {
    void onAddUserDialogPositiveClick(String user);
    void onRemoveUserDialogPositiveClick(String user);
    void onAddMyListDialogPositiveClick(String listType, String listName);
    void onEditMyListDialogPositiveClick(String listType, int selectedItem, String listName, boolean isStarFavorite);
    void onRenameMyListDialogPositiveClick(String listType, String oldListName, String listName);

    void saveAndUpdateUserInfoList(UserInfo userInfo);

    void saveAndUpdateMyLists(String kanjiIdString, ArrayList<MyListEntry> listsToCopyTo, boolean move, MyListEntry currentList);
    void saveAndUpdateTweetLists(String tweetIds, ArrayList<MyListEntry> listsToCopyTo, boolean move, MyListEntry currentList);

    //From quizmenu popup to main activity (starting quizzes)
    void showFlashCardFragment(int tabNumber
            , MyListEntry listEntry
            , String frontValue
            , String backValue
            , String selectedColorString);
    void goToQuizActivityMultipleChoice(int tabNumber
            , MyListEntry listEntry
            , String quizType
            , String quizSize
            , String quizTimer
            , String selectedColorString);

    void showFillintheBlanksFragment(int tabNumber
            , MyListEntry listEntry
            , String quizSize
            ,String selectedColorString);
}