package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Adapters.BrowseMyListAdapter;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.CopyMyListItemsDialog;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static java.util.concurrent.TimeUnit.SECONDS;

//import com.jukuproject.jukutweet.Interfaces.MyListCopyDialogListener;

/**
 * Created by JClassic on 3/26/2017.
 */

public class MyListBrowseFragment extends Fragment  {

    String TAG = "MyListFragment";
    private RxBus mRxBus = new RxBus();
    private RecyclerView mRecyclerView;
    /*Tracks elapsed time since last click of a recyclerview row. Used to
    * keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
    FragmentInteractionListener mCallback;
    private BrowseMyListAdapter mAdapter;
    private ArrayList<WordEntry> mWords;
    private MyListEntry mMyListEntry;
    private ColorThresholds mColorThresholds;
    private ArrayList<Integer> mSelectedEntries = new ArrayList<>(); //Tracks which entries in the adapter are currently selected (id key)
    private Subscription undoSubscription;

    public static MyListBrowseFragment newInstance(MyListEntry myListEntry) {
        MyListBrowseFragment fragment = new MyListBrowseFragment();
        Bundle args = new Bundle();
        args.putParcelable("mylistentry", myListEntry);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerMain);


        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMyListEntry = getArguments().getParcelable("mylistentry");
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(getContext());
        mColorThresholds = sharedPrefManager.getColorThresholds();
//        mActiveFavoriteStars = sharedPrefManager.getActiveFavoriteStars();
        updateAdapter();
    }



    public void updateAdapter() {


        //Pull list of word entries in the database for a given list
        mWords = InternalDB.getInstance(getContext()).getMyListWords("Word"
                ,mMyListEntry
                ,mColorThresholds
                ,"'Grey','Red','Yellow','Green'"
                ,null
                ,null);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));



        //Create UserListAdapter and attach rxBus click listeners to it
        if(mWords != null && mWords.size() > 0) {
            mAdapter = new BrowseMyListAdapter(getContext(),mWords,mColorThresholds,mRxBus,mSelectedEntries);

            mRxBus.toClickObserverable()
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {

                            //TODO MOVE THIS METHOD TO THE FRAGMENT, AND ONLY CALL BACK TO MAIN ACTIVITY???
                            //TODO OR only if there is no userinfo, fill that shit in. otherwise dont
                            if(isUniqueClick(100) && event instanceof Integer) {

                                Integer id = (Integer) event;

                                if(!mSelectedEntries.contains(id)) {
                                        if(mSelectedEntries.size()==0) {
                                            mCallback.showMenuMyListBrowse(true);
                                            Log.d(TAG,"showing menu");
                                        }
                                    Log.d(TAG,"selected adding: " + id);
                                    mSelectedEntries.add(id);
                                    Log.d(TAG,"selected size: " + mSelectedEntries.size());

                                } else {
                                    mSelectedEntries.remove(id);
                                    Log.d(TAG,"selected removing: " + id);
                                    Log.d(TAG,"selected size: " + mSelectedEntries.size());
                                }

                                if(mSelectedEntries.size()==0) {
                                    mCallback.showMenuMyListBrowse(false);
                                    Log.d(TAG,"hiding menu");
                                }

                                Log.d(TAG,"selected updated entry  count: " + mSelectedEntries.size());

                            }

                        }

                    });


            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setVerticalScrollBarEnabled(true);

        } else {
            /* Hide recycler view and show "no users found" message */
//            showRecyclerView(false);
        }


    }

    /**
     * Checks how many milliseconds have elapsed since the last time "mLastClickTime" was updated
     * If enough time has elapsed, returns True and updates mLastClickTime.
     * This is to stop unwanted rapid clicks of the same button
     * @param elapsedMilliSeconds threshold of elapsed milliseconds before a new button click is allowed
     * @return bool True if enough time has elapsed, false if not
     */
    public boolean isUniqueClick(int elapsedMilliSeconds) {
        if(SystemClock.elapsedRealtime() - mLastClickTime > elapsedMilliSeconds) {
            mLastClickTime = SystemClock.elapsedRealtime();
            return true;
        } else {
            return false;
        }
    }

    public void deselectAll(){
        mSelectedEntries.clear();
        mAdapter.notifyDataSetChanged();
    }

    public void selectAll() {

//        //If every word item is already selected, deselect all
        if(mSelectedEntries.size() != mWords.size()) {
            mSelectedEntries.clear();
            for(WordEntry entry : mWords) {
                mSelectedEntries.add(entry.getId());
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    public void showCopyMyListDialog(){
        if(getActivity().getSupportFragmentManager().findFragmentByTag("dialogCopy") == null || !getActivity().getSupportFragmentManager().findFragmentByTag("dialogCopy").isAdded()) {
            CopyMyListItemsDialog.newInstance(mMyListEntry,mSelectedEntries).show(getActivity().getSupportFragmentManager(),"dialogCopy");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (FragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }


    //TODO add error message to this
    public void saveAndUpdateMyLists(String kanjiIdString,ArrayList<MyListEntry> listsToCopyTo, boolean move,MyListEntry currentList) {
        InternalDB helper = InternalDB.getInstance(getContext());

        try {
            for(MyListEntry entry : listsToCopyTo) {
                helper.addBulkKanjiToList(entry,kanjiIdString);
            }

            if(move) {
                removeKanjiFromList(kanjiIdString,currentList);
            }
            deselectAll();
            mCallback.showMenuMyListBrowse(false);
        } catch (NullPointerException e) {
            Log.e(TAG,"Nullpointer in MyListBrowseFragment saveAndUpdateMyLists : " + e);
            Toast.makeText(getContext(), "Unable to update lists", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            Log.e(TAG,"SQLiteException in MyListBrowseFragment saveAndUpdateMyLists : " + e);
            Toast.makeText(getContext(), "Unable to update lists", Toast.LENGTH_SHORT).show();
        } finally {
            helper.close();
        }



    }

    public void removeKanjiFromList(String kanjiIdString, MyListEntry currentList){
        try {
            InternalDB.getInstance(getContext()).removeBulkKanjiFromMyList(kanjiIdString,currentList);
            mWords = InternalDB.getInstance(getContext()).getMyListWords("Word"
                    ,mMyListEntry
                    ,mColorThresholds
                    ,"'Grey','Red','Yellow','Green'"
                    ,null
                    ,null);
            mSelectedEntries = new ArrayList<>();
            mAdapter.swapDataSet(mWords);
        } catch (NullPointerException e) {
            Log.e(TAG,"Nullpointer in MyListBrowseFragment removeKanjiFromList : " + e);
            Toast.makeText(getContext(), "Unable to delete entries", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            Log.e(TAG,"SQLiteException in MyListBrowseFragment removeKanjiFromList : " + e);
            Toast.makeText(getContext(), "Unable to delete entries", Toast.LENGTH_SHORT).show();
        }
    }


    public void removeKanjiFromList(){
        try {
            final String kanjiString = getSelectedIntsAsString(mSelectedEntries);
            InternalDB.getInstance(getContext()).removeBulkKanjiFromMyList(kanjiString,mMyListEntry);
            mWords = InternalDB.getInstance(getContext()).getMyListWords("Word"
                    ,mMyListEntry
                    ,mColorThresholds
                    ,"'Grey','Red','Yellow','Green'"
                    ,null
                    ,null);
            mAdapter.swapDataSet(mWords);
            showUndoPopup(kanjiString,mMyListEntry);
        } catch (NullPointerException e) {
            Log.e(TAG,"Nullpointer in MyListBrowseFragment removeKanjiFromList : " + e);
            Toast.makeText(getContext(), "Unable to delete entries", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            Log.e(TAG,"SQLiteException in MyListBrowseFragment removeKanjiFromList : " + e);
            Toast.makeText(getContext(), "Unable to delete entries", Toast.LENGTH_SHORT).show();
        }
    }


    public void showUndoPopup(final String kanjiIdString, final MyListEntry currentList) {

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.llSortChangePopup);
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.popup_undo, null);

        final PopupWindow popupWindow = new PopupWindow(getContext());
//        popupWindow.setWidth(popupwindowwidth);
//        popupWindow.setHeight(poupwindowheight);
        popupWindow.setFocusable(true);
        popupWindow.setClippingEnabled(false);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
//        View v = getLayoutInflater().inflate(R.layout.popup_undo,null);

        popupWindow.setWidth((int)(metrics.widthPixels*.66f));

        TextView undoButton = (TextView) v.findViewById(R.id.undoButton);
        undoSubscription  =  Observable.timer(3, SECONDS).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        popupWindow.dismiss();
                    }
                });


        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    InternalDB.getInstance(getContext()).addBulkKanjiToList(currentList,kanjiIdString);
                    mWords = InternalDB.getInstance(getContext()).getMyListWords("Word"
                            ,mMyListEntry
                            ,mColorThresholds
                            ,"'Grey','Red','Yellow','Green'"
                            ,null
                            ,null);
                    mSelectedEntries.clear();
                    mAdapter.swapDataSet(mWords);
                    try {
                        popupWindow.dismiss();
                        undoSubscription.unsubscribe();
                    } catch (Exception e) {

                    }

                } catch (NullPointerException e) {
                    Log.e(TAG,"Nullpointer in MyListBrowseFragment showUndoPopup : re-add" + e);
                    Toast.makeText(getContext(), "Unable to undo delete!", Toast.LENGTH_SHORT).show();
                } catch (SQLiteException e) {
                    Log.e(TAG,"SQLiteException in MyListBrowseFragment showUndoPopup re-add : " + e);
                    Toast.makeText(getContext(), "Unable to undo delete!", Toast.LENGTH_SHORT).show();
                }

            }
        });


        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.popup_drawable));
        popupWindow.setContentView(v);
        popupWindow.showAtLocation(mRecyclerView, Gravity.BOTTOM, 0, (int)(metrics.heightPixels / (float)9.5));
        // create a single event in 10 seconds time








    }

    //TODO CONSOLIDATE WITH TWIN IN COPYMYLISTITEMSDIALOG
    public String getSelectedIntsAsString(ArrayList<Integer> list ) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); ++i) {
            if (i>0) {
                sb.append(", ");
            }
            sb.append(list.get(i).toString());
        }
        return sb.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            undoSubscription.unsubscribe();
        } catch (Exception e) {

        }
    }
}
