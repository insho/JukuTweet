package com.jukuproject.jukutweet;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import com.jukuproject.jukutweet.Adapters.ChooseFavoritesAdapter;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.WordEntry;

import java.util.ArrayList;
import rx.functions.Action1;

/**
 * Created by JClassic on 3/24/2017.
 */

public class PopupChooseFavoriteLists  {

        private Context mContext;
        private float mDensity;
        private int mKanjiId;
//        private RxBus rxBus = new RxBus();
        private RxBus mRxBusTweetBreak;
//        private PopupChooseFavoritesListener;
        private ArrayList<String> mActiveFavoriteStars;

    private RecyclerView mRecyclerView;
    private ChooseFavoritesAdapter mAdapter;

        public PopupChooseFavoriteLists(Context context, float density,RxBus rxBus , ArrayList<String> activeFavoriteStars, int kanjiId) {
            mContext = context;
            mDensity = density;
            mKanjiId = kanjiId;
            mRxBusTweetBreak = rxBus;
            mActiveFavoriteStars = activeFavoriteStars;
        }



    public PopupWindow onCreateView() {

        PopupWindow popupWindow = new PopupWindow(mContext);

        View view = LayoutInflater.from(mContext).
                inflate(R.layout.popup_choosefavorites, null);

        ArrayList<MyListEntry> favoritesLists = InternalDB.getInstance(mContext).getFavoritesListsForAKanji(mActiveFavoriteStars,mKanjiId);

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
        mAdapter = new ChooseFavoritesAdapter(mContext, mDensity, mRxBusTweetBreak,favoritesLists, mKanjiId);


//                    rxBus.toClickObserverable()
//                            .subscribe(new Action1<Object>() {
//                                @Override
//                                public void call(Object event) {
//
//                        /* Recieve a MyListEntry (containing an updated list entry for this row kanji) from
//                        * the ChooseFavoritesAdapter in the ChooseFavorites popup window */
//                                    if(event instanceof MyListEntry) {
//                                        MyListEntry myListEntry = (MyListEntry) event;
//
//                            /*Ascertain the type of list that the kanji was added to (or subtracted from),
//                              and update that list's count */
//                                        if(myListEntry.getListsSys() == 1) {
//                                            switch (myListEntry.getListName()) {
//                                                case "Blue":
//                                                    mWordEntry.getWordEntryFavorites().setSystemBlueCount(myListEntry.getSelectionLevel());
//                                                    break;
//                                                case "Green":
//                                                    mWordEntry.getWordEntryFavorites().setSystemGreenCount(myListEntry.getSelectionLevel());
//                                                    break;
//                                                case "Red":
//                                                    mWordEntry.getWordEntryFavorites().setSystemRedCount(myListEntry.getSelectionLevel());
//                                                    break;
//                                                case "Yellow":
//                                                    mWordEntry.getWordEntryFavorites().setSystemYellowCount(myListEntry.getSelectionLevel());
//                                                    break;
//                                                default:
//                                                    break;
//                                            }
//                                        } else {
//                                            if(myListEntry.getSelectionLevel() == 1) {
//                                                mWordEntry.getWordEntryFavorites().addToUserListCount(1);
//                                            } else {
//                                                mWordEntry.getWordEntryFavorites().subtractFromUserListCount(1);
//                                            }
//                                        }
//
//
//
//
//                                    }
//
//                                }
//
//                            });



        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setItemViewCacheSize(favoritesLists.size());


        popupWindow.setWidth(200);

        popupWindow.setHeight(400);
//            int mscreenheight = 600;
//            if(favoritesLists.size()>12) {
//                popupWindow.setHeight((int)((float)mscreenheight/2.0f));
//            } else {
//                popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
//            }

        popupWindow.setFocusable(true);
        popupWindow.setClippingEnabled(false);

        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.popup_drawable));
//        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
//        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(view);
        return popupWindow;
    }

    }



