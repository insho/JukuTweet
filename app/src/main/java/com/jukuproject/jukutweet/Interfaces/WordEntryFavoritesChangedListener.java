package com.jukuproject.jukutweet.Interfaces;

import com.jukuproject.jukutweet.Models.WordEntry;

/**
 * Created by JClassic on 4/23/2017.
 */

public interface WordEntryFavoritesChangedListener {
    void updateWordEntryItemFavorites(WordEntry wordEntry);
    void updateWordEntryFavoritesForOtherTabs(WordEntry wordEntry);

}
