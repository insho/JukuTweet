package com.jukuproject.jukutweet;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.jukuproject.jukutweet.Adapters.ChooseFavoritesAdapter;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.MyListEntry;

import java.util.ArrayList;

/**
 * Created by JClassic on 3/24/2017.
 */

public class PopupChooseFavoriteLists  extends PopupWindow {

        private Context mContext;
        private final DisplayMetrics mMetrics;
        private int mKanjiId;
        private RxBus mRxBusTweetBreak;
        private ArrayList<String> mActiveFavoriteStars;
    private PopupWindow popupWindow;
    private ArrayList<MyListEntry> favoritesLists;
    private RecyclerView mRecyclerView;
    private ChooseFavoritesAdapter mAdapter;

        public PopupChooseFavoriteLists(Context context, DisplayMetrics metrics, RxBus rxBus , ArrayList<String> activeFavoriteStars, int kanjiId) {
            mContext = context;
            mMetrics = metrics;
            mKanjiId = kanjiId;
            mRxBusTweetBreak = rxBus;
            mActiveFavoriteStars = activeFavoriteStars;
        }



    public PopupWindow onCreateView() {

        popupWindow = new PopupWindow(mContext);

        View view = LayoutInflater.from(mContext).
                inflate(R.layout.popup_choosefavorites, null);

        favoritesLists = InternalDB.getInstance(mContext).getWordListsForAWord(mActiveFavoriteStars,String.valueOf(mKanjiId),null);


        //TODO put something in when there is no list...
        //Only show popup if there are available lists to add to, otherwise do nothing.
        if(favoritesLists.size()>0) {



        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(ContextCompat.getDrawable(mContext, R.drawable.popup_drawable));
        } else {
            view.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.popup_drawable));
        }

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.favoritesRecycler);
        mRecyclerView.setVerticalScrollBarEnabled(false);
        mRecyclerView.getLayoutParams().height = RecyclerView.LayoutParams.WRAP_CONTENT;
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ChooseFavoritesAdapter(mContext, mMetrics.density, mRxBusTweetBreak,favoritesLists, mKanjiId);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemViewCacheSize(favoritesLists.size());

        //TODO fix this width
//        popupWindow.setWidth(200);
//        popupWindow.setHeight(400);

        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        if(popupWindow.getHeight()>mMetrics.heightPixels * .75) {
            popupWindow.setHeight((int)(mMetrics.heightPixels * .75));
        }

        if(popupWindow.getWidth() < (int) (180 * mMetrics.density + 0.5f)) {
            popupWindow.setWidth((int) (180 * mMetrics.density + 0.5f));
        }
//        int pixels = (int) (180 * mMetrics.density + 0.5f);
//
//        popupWindow.setWidth(TypedValue.COMPLEX_UNIT_DIP);
//        if(favoritesLists.size()>10) {
//            popupWindow.setHeight((int)((float)mscreenheight/2.0f));
//        } else {
//            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
//        }


        /*
        *
        * */
        popupWindow.setFocusable(true);
        popupWindow.setClippingEnabled(false);

        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.popup_drawable));
        popupWindow.setContentView(view);
        return popupWindow;
    }

    public int getEstimatedPopupHeight() {
        return (int) ((favoritesLists.size()*35) * mMetrics.density + 0.5f);
    }

//    @Override
//    public void showAsDropDown(View anchor) {
//        int xadjust = popupWindow.getWidth() + (int) (25 * mMetrics.density + 0.5f);
////        Log.d(TAG,)
//        int yadjust = popupWindow.getHeight()/2;
//
//        Log.d("TEST","pop width: " + popupWindow.getWidth() + " height: " + popupWindow.getHeight());
//        Log.d("TEST","pop xadjust: " + xadjust);
//        Log.d("TEST","pop yadjust: " + xadjust);
//
//        this.showAsDropDown(anchor,-xadjust,-yadjust);
//    }
}



