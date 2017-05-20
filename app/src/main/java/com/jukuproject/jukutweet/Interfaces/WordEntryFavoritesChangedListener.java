package com.jukuproject.jukutweet.Interfaces;

import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;

/**
 * Traffic control between {@link com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog} and
 * the fragment that called it, when the "favorite star" is clicked within the WordDetailPopupDialog and
 * the favorite lists associated with that word have changed
 */
public interface WordEntryFavoritesChangedListener {
    void updateWordEntryItemFavorites(WordEntry wordEntry);
    void updateWordEntryFavoritesForOtherTabs(WordEntry wordEntry);
    void notifySavedTweetFragmentsChanged();
    void downloadTweetUserIcons(UserInfo userInfo);
}
