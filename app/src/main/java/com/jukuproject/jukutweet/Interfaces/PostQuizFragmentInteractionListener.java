package com.jukuproject.jukutweet.Interfaces;

import com.jukuproject.jukutweet.Models.UserInfo;

/**
 * Interface between the Stats fragments and MainActivity/PostQuizActivity, to help
 * download tweet user icons for saved tweets (probably in the {@link com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog}, which
 * can be opened from nearly anywhere, and in which the user can search for and save new tweets... so the icons
 * for these must be downloaded and this gets sent back to the main activities for the actual process
 */
public interface PostQuizFragmentInteractionListener {
    void downloadTweetUserIcons(UserInfo userInfo);
}
