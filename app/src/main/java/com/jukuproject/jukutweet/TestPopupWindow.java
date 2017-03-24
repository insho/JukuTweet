//package com.jukuproject.jukutweet;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.content.res.Configuration;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.os.Build;
//import android.preference.PreferenceManager;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.widget.AppCompatCheckBox;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.DisplayMetrics;
//import android.util.Log;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.CompoundButton;
//import android.widget.GridLayout;
//import android.widget.ImageButton;
//import android.widget.PopupWindow;
//import android.widget.TextView;
//
//import com.jukuproject.jukutweet.Adapters.ChooseFavoritesAdapter;
//import com.jukuproject.jukutweet.Database.InternalDB;
//import com.jukuproject.jukutweet.Models.MyListEntry;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * Created by JClassic on 3/24/2017.
// */
//
//public class PopupChooseFavoriteLists  {
//
//    private Context mContext;
//    private float mDensity;
//    private int mKanjiId;
//
//    private RecyclerView mRecyclerView;
//    private ChooseFavoritesAdapter mAdapter;
//
//    public PopupChooseFavoriteLists(Context context, float density, int kanjiId) {
//
//        mContext = context;
//        mDensity = density;
//        mKanjiId = kanjiId;
//    }
//
//
//
//    public PopupWindow onCreateView() {
//
//        PopupWindow popupWindow = new PopupWindow(mContext);
//
//        View view = LayoutInflater.from(mContext).
//                inflate(R.layout.popup_choosefavorites, null);
//
//        ArrayList<MyListEntry> favoritesLists = InternalDB.getInstance(mContext).getFavoritesListsForAKanji(mKanjiId);
//
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            view.setBackground(ContextCompat.getDrawable(mContext, R.drawable.popup_drawable));
//        } else {
//            view.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.popup_drawable));
//        }
//
//        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
//        mRecyclerView = (RecyclerView) view.findViewById(R.id.favoritesRecycler);
//        mRecyclerView.setVerticalScrollBarEnabled(false);
//
//        mRecyclerView.getLayoutParams().height = RecyclerView.LayoutParams.WRAP_CONTENT;
//        mRecyclerView.setLayoutManager(mLayoutManager);
//        mAdapter = new ChooseFavoritesAdapter(mContext, mDensity, favoritesLists, mKanjiId);
//        mRecyclerView.setAdapter(mAdapter);
//
//        mRecyclerView.setItemViewCacheSize(favoritesLists.size());
//
//
//        popupWindow.setWidth(200);
//        int mscreenheight = 600;
//        if(favoritesLists.size()>12) {
//            popupWindow.setHeight((int)((float)mscreenheight/2.0f));
//        } else {
//            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
//        }
//
//        popupWindow.setFocusable(true);
//
//        popupWindow.setClippingEnabled(false);
//
//        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.popup_drawable));
////            setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
////            setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
//        popupWindow.setContentView(view);
//        return popupWindow;
//    }
////
////    @Override
////    public void showAtLocation(View parent, int gravity, int x, int y) {
////        super.showAtLocation(parent, gravity, x, y);
////    }
////
////    @Override
////    public View getContentView() {
////
////        View view = LayoutInflater.from(mContext).
////                inflate(R.layout.popup_choosefavorites, null);
//////
//////        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
//////        mRecyclerView.setLayoutManager(layoutManager);
////
//////        //TODO put shit here
//////        ArrayList<MyListEntry> favoritesLists = InternalDB.getInstance(mContext).getFavoritesListsForAKanji(mKanjiId);
////////        int popupwindowwidth;
////////        if(mscreenwidth<=0) {
////////            popupwindowwidth = 250;
////////        } else if(mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
////////            popupwindowwidth = Math.round((float) mscreenwidth * (float) .45);
////////        } else {
////////            popupwindowwidth = Math.round((float) mscreenwidth * (float) 0.26);
////////        }
//////
//////
//////
//////        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//////            view.setBackground(ContextCompat.getDrawable(mContext, R.drawable.popup_drawable));
//////        } else {
//////            view.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.popup_drawable));
//////        }
//////
//////        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
//////        mRecyclerView = (RecyclerView) view.findViewById(R.id.favoritesRecycler);
//////        mRecyclerView.setVerticalScrollBarEnabled(false);
//////
//////        mRecyclerView.getLayoutParams().height = RecyclerView.LayoutParams.WRAP_CONTENT;
//////        mRecyclerView.setLayoutManager(mLayoutManager);
//////        mAdapter = new ChooseFavoritesAdapter(mContext, mDensity, favoritesLists, mKanjiId);
//////        mRecyclerView.setAdapter(mAdapter);
//////
//////        // some other visual settings
//////
//////
////////        if(BuildConfig.DEBUG){
////////            Log.d(TAG,"mRecyclerView.getChildCount(): " + mRecyclerView.getChildCount());
////////            Log.d(TAG,"myListSelections size: " + myListSelections.size());
////////            Log.d(TAG,"myListSelections..:" + myListSelections);
////////        }
//////        mRecyclerView.setItemViewCacheSize(favoritesLists.size());
//////
//////
//////        //TODO change width thing
//////        this.setWidth(200);
//////        int mscreenheight = 600;
//////        if(favoritesLists.size()>12) {
//////            this.setHeight((int)((float)mscreenheight/2.0f));
//////        } else {
//////            this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
//////        }
//////
//////        this.setFocusable(true);
//////
//////        this.setClippingEnabled(false);
//////        this.setContentView(view);
//////        this.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.popup_drawable));
////
//////        return po;
////        return super.getContentView();
//////        return
////    }
//
////    public void show(View anchor)
////        {
////            showAtLocation(anchor, Gravity.CENTER, 0, 0);
////        }
////}
//
//
//
