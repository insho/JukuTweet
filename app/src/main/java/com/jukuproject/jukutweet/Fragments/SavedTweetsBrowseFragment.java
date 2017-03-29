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

import com.jukuproject.jukutweet.Adapters.BrowseTweetsAdapter;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

import rx.functions.Action1;

/**
 * Shows last X tweets from a twitter timeline. User can click on a tweet to bring up
 * the WordBreakDown Popup window, save the tweet and/or specific kanji from the tweet
 */
public class SavedTweetsBrowseFragment extends Fragment {
    String TAG = "TEST-savetweetbrow";
    private RxBus mRxBus = new RxBus();
    private RecyclerView mRecyclerView;
    /*Tracks elapsed time since last click of a recyclerview row. Used to
    * keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
    FragmentInteractionListener mCallback;
    private BrowseTweetsAdapter mAdapter;
    private ArrayList<Tweet> mDataset;
    private MyListEntry mMyListEntry;
    private ColorThresholds mColorThresholds;
    private ArrayList<Integer> mSelectedEntries = new ArrayList<>(); //Tracks which entries in the adapter are currently selected (id key)


    public SavedTweetsBrowseFragment() {}

    /**
     * Returns a new instance of UserListFragment
     */
    public static SavedTweetsBrowseFragment newInstance(MyListEntry myListEntry) {
        SavedTweetsBrowseFragment fragment = new SavedTweetsBrowseFragment();

        Bundle args = new Bundle();
        args.putParcelable("mylistentry", myListEntry);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerTimeLine);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMyListEntry = getArguments().getParcelable("mylistentry");
//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
//        mRecyclerView.setLayoutManager(layoutManager);
        mColorThresholds = SharedPrefManager.getInstance(getContext()).getColorThresholds();


        pullData(mMyListEntry);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
//        updateAdapter();

        //TODO Put something in place to repull data on resume...
    }


    public void pullData(MyListEntry myListEntry){
        mDataset = InternalDB.getInstance(getContext()).getSavedTweets(myListEntry,mColorThresholds);

        for(Tweet tweet : mDataset) {
            Log.d("XXX-Z:", "tweet id receieved: " + tweet.getIdString() + ", text: " + tweet.getText());
        }
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));



        //Create UserListAdapter and attach rxBus click listeners to it
        if(mDataset != null && mDataset.size() > 0){
            mAdapter = new BrowseTweetsAdapter(getContext(), mRxBus, mDataset, mSelectedEntries);

            mRxBus.toClickObserverable()
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {

                            //TODO MOVE THIS METHOD TO THE FRAGMENT, AND ONLY CALL BACK TO MAIN ACTIVITY???
                            //TODO OR only if there is no userinfo, fill that shit in. otherwise dont
                            if (isUniqueClick(100) && event instanceof Integer) {

                                Integer id = (Integer) event;


                                if (!mSelectedEntries.contains(id)) {
                                    if (mSelectedEntries.size() == 0) {
                                        mCallback.showMenuMyListBrowse(true);
                                        Log.d(TAG, "showing menu");
                                    }
                                    Log.d(TAG, "selected adding: " + id);
                                    mSelectedEntries.add(id);
                                    Log.d(TAG, "selected size: " + mSelectedEntries.size());

                                } else {
                                    mSelectedEntries.remove(id);
                                    Log.d(TAG, "selected removing: " + id);
                                    Log.d(TAG, "selected size: " + mSelectedEntries.size());


                                }

                                if (mSelectedEntries.size() == 0) {
                                    mCallback.showMenuMyListBrowse(false);
                                    Log.d(TAG, "hiding menu");
                                }

                                Log.d(TAG, "selected updated entry  count: " + mSelectedEntries.size());

                            }

                        }

                    });


            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setVerticalScrollBarEnabled(true);

        }

    }

//    /**
//     * Toggles between showing recycler (if there are followed users in the database)
//     * and hiding the recycler while showing the "no users found" message if there are not
//     * @param show bool True to show recycler, False to hide it
//     */
//    private void showRecyclerView(boolean show) {
//        if(show) {
//            mRecyclerView.setVisibility(View.VISIBLE);
////            mNoLists.setVisibility(View.GONE);
//        } else {
//            mRecyclerView.setVisibility(View.GONE);
////            mNoLists.setVisibility(View.VISIBLE);
//        }
//    }

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

//    @Override
//    public void onResume() {
//        super.onResume();
//        if()
//    }
}

