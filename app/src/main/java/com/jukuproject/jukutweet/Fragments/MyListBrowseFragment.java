package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.Adapters.BrowseMyListAdapter;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

import rx.functions.Action1;

/**
 * Created by JClassic on 3/26/2017.
 */

public class MyListBrowseFragment extends Fragment {

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
    private ArrayList<String> mActiveFavoriteStars;
//    private HashMap<Integer,Integer> selectedHashMap = new HashMap<>(); //Tracks which entries in the adapter are currently selected (position,
    private ArrayList<Integer> mSelectedEntries = new ArrayList<>(); //Tracks which entries in the adapter are currently selected (id key)


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
        mActiveFavoriteStars = sharedPrefManager.getActiveFavoriteStars();
        updateAdapter();
    }



    public void updateAdapter() {


        //Pull list of word entries in the database for a given list
        mWords = InternalDB.getInstance(getContext()).getMyListWords(mMyListEntry);

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
//
//                               AddUserDialog addUserDialogFragment = AddUserDialog.newInstance();
//                                FragmentManager fragManager = getActivity().getSupportFragmentManager();
//                                addUserDialogFragment.show(fragManager, "dialogAdd");


                                Log.d(TAG,"selected entry count: " + mSelectedEntries.size());


                                if(!mSelectedEntries.contains(id)) {
                                        if(mSelectedEntries.size()==0) {
                                            mCallback.showMenuMyListBrowse(true);
                                        }
                                    mSelectedEntries.add(id);
                                } else {
                                    mSelectedEntries.remove(id);
                                }

                                if(mSelectedEntries.size()==0) {
                                    mCallback.showMenuMyListBrowse(false);
                                }

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

    public void selectAll() {
        mSelectedEntries.clear();

        //If every word item is already selected, deselect all
        if(mSelectedEntries.size() != mWords.size()) {
            for(WordEntry entry : mWords) {
                mSelectedEntries.add(entry.getId());
            }
        }
        mAdapter.notifyDataSetChanged();
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



}
