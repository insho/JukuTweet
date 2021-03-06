package com.jukuproject.jukutweet;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.jukuproject.jukutweet.Adapters.ChooseFavoritesAdapter;
import com.jukuproject.jukutweet.Adapters.ChooseFavoritesTweetAdapter;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.MyListEntry;

import java.util.ArrayList;

/**
 * Static methods for creating the popup window that allows a user to choose multiple lists for
 * a tweet or word to belong to. Appears when a tweet/word favorite star is long-pressed
 *
 * @see com.jukuproject.jukutweet.Adapters.TweetBreakDownAdapter
 * @see com.jukuproject.jukutweet.Fragments.TweetBreakDownFragment
 * @see com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog
 */
public class ChooseFavoriteListsPopupWindow {

    /**
     * Creates the generic popup window from which the Word and Tweet ChooseFavorites popups will be created
     * @param context Context
     * @param favoritesLists Array of possible favorite lists to add the word/tweet to
     * @param displayMetrics display metrics
     * @return ChooseFavorites popup window
     */
    private static PopupWindow genericPopupWindow(Context context, ArrayList<MyListEntry>  favoritesLists, DisplayMetrics displayMetrics) {
        PopupWindow popupWindow = new PopupWindow(context);

        popupWindow.setFocusable(true);
        popupWindow.setClippingEnabled(false);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        if(favoritesLists.size()>10) {
            popupWindow.setHeight((int)((float)displayMetrics.heightPixels/2.0f));
        } else {
            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.popup_drawable));
        return popupWindow;
    }

    /**
     * Creates the favorite lists popup window for a WordEntry, in which the user can
     * add a Word to various WordLists
     * @param context Context
     * @param metrics display metrics
     * @param rxBus RxBus back to calling fragment, so that changes to favorite lists can be passed along
     * @param favoritesLists Array of possible favorite lists to add the word to
     * @param kanjiId current word id
     * @return ChooseFavorites popup window
     */
    public static PopupWindow createWordFavoritesPopup(Context context, DisplayMetrics metrics, RxBus rxBus , ArrayList<MyListEntry> favoritesLists,int kanjiId) {

        View view = LayoutInflater.from(context).inflate(R.layout.popup_choosefavorites, null);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.favoritesRecycler);
        mRecyclerView.setVerticalScrollBarEnabled(false);
        mRecyclerView.getLayoutParams().height = RecyclerView.LayoutParams.WRAP_CONTENT;
        mRecyclerView.setLayoutManager(mLayoutManager);
        ChooseFavoritesAdapter mAdapter = new ChooseFavoritesAdapter(context, metrics.density, rxBus,favoritesLists, kanjiId);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemViewCacheSize(favoritesLists.size());

        PopupWindow popupWindow = genericPopupWindow(context,favoritesLists,metrics);
        popupWindow.setContentView(view);

        return popupWindow;
    }

    /**
     * Creates the favorite lists popup window for a Tweet, in which the user can
     * add a Tweet to various TweetLists
     * @param context Context
     * @param metrics display metrics
     * @param rxBus RxBus back to calling fragment, so that changes to favorite lists can be passed along
     * @param favoritesLists Array of possible favorite lists to add the tweet to
     * @param tweetIdString tweetid
     * @param userIdString userid
     * @return ChooseFavorites popup window
     */
    public static PopupWindow createTweetFavoritesPopup(Context context
            , DisplayMetrics metrics
            , RxBus rxBus
            , ArrayList<MyListEntry> favoritesLists
            ,String tweetIdString
            ,String userIdString) {

        View view = LayoutInflater.from(context).inflate(R.layout.popup_choosefavorites, null);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.favoritesRecycler);
        mRecyclerView.setVerticalScrollBarEnabled(false);
        mRecyclerView.getLayoutParams().height = RecyclerView.LayoutParams.WRAP_CONTENT;
        mRecyclerView.setLayoutManager(mLayoutManager);
        ChooseFavoritesTweetAdapter mAdapter = new ChooseFavoritesTweetAdapter(context, metrics.density, rxBus,favoritesLists, tweetIdString,userIdString);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemViewCacheSize(favoritesLists.size());

        PopupWindow popupWindow = genericPopupWindow(context,favoritesLists,metrics);
        popupWindow.setContentView(view);

        return popupWindow;
    }

}



