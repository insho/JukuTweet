package com.jukuproject.jukutweet;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.ChooseFavoritesAdapter;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.WordEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import rx.functions.Action1;

/**
 * Created by JClassic on 3/24/2017.
 */

public class PopupChooseFavoriteLists  {

        private Context mContext;
        private float mDensity;
        private WordEntry mWordEntry;
        private RxBus rxBus = new RxBus();

    private RecyclerView mRecyclerView;
    private ChooseFavoritesAdapter mAdapter;

        public PopupChooseFavoriteLists(Context context, float density,WordEntry wordEntry) {

            mContext = context;
            mDensity = density;
            mWordEntry = wordEntry;
        }



    public PopupWindow onCreateView() {

        PopupWindow popupWindow = new PopupWindow(mContext);

        View view = LayoutInflater.from(mContext).
                inflate(R.layout.popup_choosefavorites, null);

        ArrayList<MyListEntry> favoritesLists = InternalDB.getInstance(mContext).getFavoritesListsForAKanji(mContext,mWordEntry.getId());

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
        mAdapter = new ChooseFavoritesAdapter(mContext, mDensity, rxBus,favoritesLists, mWordEntry.getId());


                    rxBus.toClickObserverable()
                            .subscribe(new Action1<Object>() {
                                @Override
                                public void call(Object event) {

                        /* Recieve a MyListEntry (containing an updated list entry for this row kanji) from
                        * the ChooseFavoritesAdapter in the ChooseFavorites popup window */
                                    if(event instanceof MyListEntry) {
                                        MyListEntry myListEntry = (MyListEntry) event;

                            /*Ascertain the type of list that the kanji was added to (or subtracted from),
                              and update that list's count */
                                        if(myListEntry.getListsSys() == 1) {
                                            switch (myListEntry.getListName()) {
                                                case "Blue":
                                                    mWordEntry.getWordEntryFavorites().setSystemBlueCount(myListEntry.getSelectionLevel());
                                                    break;
                                                case "Green":
                                                    mWordEntry.getWordEntryFavorites().setSystemGreenCount(myListEntry.getSelectionLevel());
                                                    break;
                                                case "Red":
                                                    mWordEntry.getWordEntryFavorites().setSystemRedCount(myListEntry.getSelectionLevel());
                                                    break;
                                                case "Yellow":
                                                    mWordEntry.getWordEntryFavorites().setSystemYellowCount(myListEntry.getSelectionLevel());
                                                    break;
                                                default:
                                                    break;
                                            }
                                        } else {
                                            if(myListEntry.getSelectionLevel() == 1) {
                                                mWordEntry.getWordEntryFavorites().addToUserListCount(1);
                                            } else {
                                                mWordEntry.getWordEntryFavorites().subtractFromUserListCount(1);
                                            }
                                        }




                                    }

                                }

                            });



        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setItemViewCacheSize(favoritesLists.size());


        popupWindow.setWidth(200);
            int mscreenheight = 600;
            if(favoritesLists.size()>12) {
                popupWindow.setHeight((int)((float)mscreenheight/2.0f));
            } else {
                popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            }

        popupWindow.setFocusable(true);
        popupWindow.setClippingEnabled(false);

        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.popup_drawable));
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(view);
        return popupWindow;
    }
//
//    @Override
//    public void showAtLocation(View parent, int gravity, int x, int y) {
//        super.showAtLocation(parent, gravity, x, y);
//    }
//
//    @Override
//    public View getContentView() {
//
//        View view = LayoutInflater.from(mContext).
//                inflate(R.layout.popup_choosefavorites, null);
////
////        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
////        mRecyclerView.setLayoutManager(layoutManager);
//
////        //TODO put shit here
////        ArrayList<MyListEntry> favoritesLists = InternalDB.getInstance(mContext).getFavoritesListsForAKanji(mKanjiId);
//////        int popupwindowwidth;
//////        if(mscreenwidth<=0) {
//////            popupwindowwidth = 250;
//////        } else if(mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
//////            popupwindowwidth = Math.round((float) mscreenwidth * (float) .45);
//////        } else {
//////            popupwindowwidth = Math.round((float) mscreenwidth * (float) 0.26);
//////        }
////
////
////
////        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
////            view.setBackground(ContextCompat.getDrawable(mContext, R.drawable.popup_drawable));
////        } else {
////            view.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.popup_drawable));
////        }
////
////        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
////        mRecyclerView = (RecyclerView) view.findViewById(R.id.favoritesRecycler);
////        mRecyclerView.setVerticalScrollBarEnabled(false);
////
////        mRecyclerView.getLayoutParams().height = RecyclerView.LayoutParams.WRAP_CONTENT;
////        mRecyclerView.setLayoutManager(mLayoutManager);
////        mAdapter = new ChooseFavoritesAdapter(mContext, mDensity, favoritesLists, mKanjiId);
////        mRecyclerView.setAdapter(mAdapter);
////
////        // some other visual settings
////
////
//////        if(BuildConfig.DEBUG){
//////            Log.d(TAG,"mRecyclerView.getChildCount(): " + mRecyclerView.getChildCount());
//////            Log.d(TAG,"myListSelections size: " + myListSelections.size());
//////            Log.d(TAG,"myListSelections..:" + myListSelections);
//////        }
////        mRecyclerView.setItemViewCacheSize(favoritesLists.size());
////
////
////        //TODO change width thing
////        this.setWidth(200);
////        int mscreenheight = 600;
////        if(favoritesLists.size()>12) {
////            this.setHeight((int)((float)mscreenheight/2.0f));
////        } else {
////            this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
////        }
////
////        this.setFocusable(true);
////
////        this.setClippingEnabled(false);
////        this.setContentView(view);
////        this.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.popup_drawable));
//
////        return po;
//        return super.getContentView();
////        return
//    }

//    public void show(View anchor)
//        {
//            showAtLocation(anchor, Gravity.CENTER, 0, 0);
//        }
    }



