package com.jukuproject.jukutweet;

import android.content.Context;
import android.content.res.Configuration;
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
 * Created by JClassic on 3/24/2017.
 */

public class ChooseFavoriteListsPopupWindow {

//    private Context mContext;
//    private final DisplayMetrics mMetrics;
//    private int mKanjiId;
//    private RxBus mRxBusTweetBreak;
//    private ArrayList<MyListEntry> mFavoritesLists;

//    public ChooseFavoriteListsPopupWindow(Context context, DisplayMetrics metrics, RxBus rxBus , ArrayList<MyListEntry> favoritesLists,int kanjiId) {
//        mContext = context;
//        mMetrics = metrics;
//        mRxBusTweetBreak = rxBus;
//        mFavoritesLists = favoritesLists;
//        mKanjiId = kanjiId;
//    }

    public ChooseFavoriteListsPopupWindow() {}

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

        PopupWindow popupWindow = new PopupWindow(context);
        popupWindow.setContentView(view);

        popupWindow.setFocusable(true);
        popupWindow.setClippingEnabled(false);
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            popupWindow.setWidth(Math.round((float) metrics.widthPixels * (float) .45));
        } else {
            popupWindow.setWidth(Math.round((float) metrics.widthPixels * (float) 0.26));
        }
//        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        if(favoritesLists.size()>12) {
            popupWindow.setHeight((int)((float)metrics.heightPixels/2.0f));
        } else {
            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.popup_drawable));
        return popupWindow;
    }

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

        PopupWindow popupWindow = new PopupWindow(context);
        popupWindow.setContentView(view);

        popupWindow.setFocusable(true);
        popupWindow.setClippingEnabled(false);
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            popupWindow.setWidth(Math.round((float) metrics.widthPixels * (float) .45));
        } else {
            popupWindow.setWidth(Math.round((float) metrics.widthPixels * (float) 0.26));
        }

//        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
//        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        if(favoritesLists.size()>12) {
            popupWindow.setHeight((int)((float)metrics.heightPixels/2.0f));
        } else {
            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.popup_drawable));
        return popupWindow;
    }

}



