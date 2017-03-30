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

import com.jukuproject.jukutweet.Adapters.BrowseTweetsAdapter;
import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.CopySavedTweetsDialog;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static java.util.concurrent.TimeUnit.SECONDS;

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
    private ArrayList<String> mSelectedEntries = new ArrayList<>(); //Tracks which entries in the adapter are currently selected (tweet_id)
    private Subscription undoSubscription;

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
                            if (isUniqueClick(1000) && event instanceof String) {

                                String tweet_id = (String) event;

                                if (!mSelectedEntries.contains(tweet_id)) {
                                    if (mSelectedEntries.size() == 0) {
                                        mCallback.showMenuMyListBrowse(true);
                                        Log.d(TAG, "showing menu");
                                    }
                                    Log.d(TAG, "selected adding: " + tweet_id);
                                    mSelectedEntries.add(tweet_id);
                                    Log.d(TAG, "selected size: " + mSelectedEntries.size());

                                } else {
                                    mSelectedEntries.remove(tweet_id);
                                    Log.d(TAG, "selected removing: " + tweet_id);
                                    Log.d(TAG, "selected size: " + mSelectedEntries.size());


                                }

                                if (mSelectedEntries.size() == 0) {
                                    mCallback.showMenuMyListBrowse(false);
                                    Log.d(TAG, "hiding menu");
                                }

                                Log.d(TAG, "selected updated entry  count: " + mSelectedEntries.size());

                            } else if(isUniqueClick(1000) && event instanceof Tweet) {

                                /*Entire tweet sent back from adapter, so open tweetbreakdownfragment with
                                  saved TweetKanjiColor object (which is part of Tweet object) fascilitaing the parsing */
                                Tweet tweet = (Tweet) event;
                                TweetBreakDownFragment fragment = TweetBreakDownFragment.newInstanceSavedTweet(tweet);
                                ((BaseContainerFragment)getParentFragment()).addFragment(fragment, true,"tweetbreakdown");
                                mCallback.showFab(false,"");
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

    public void showCopyTweetsDialog(){
        if(getActivity().getSupportFragmentManager().findFragmentByTag("dialogCopyTweet") == null || !getActivity().getSupportFragmentManager().findFragmentByTag("dialogCopyTweet").isAdded()) {
            CopySavedTweetsDialog.newInstance(mMyListEntry,mSelectedEntries).show(getActivity().getSupportFragmentManager(),"dialogCopyTweet");
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
        if(mSelectedEntries.size() != mDataset.size()) {
            mSelectedEntries.clear();
            for(Tweet entry : mDataset) {
                mSelectedEntries.add(entry.getIdString());
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    //TODO add error message to this
    public void saveAndUpdateTweets(String tweetIds,ArrayList<MyListEntry> listsToCopyTo, boolean move,MyListEntry currentList) {
        InternalDB helper = InternalDB.getInstance(getContext());
        try {
            for(MyListEntry entry : listsToCopyTo) {
                helper.addBulkTweetsToList(entry,tweetIds);
            }
            if(move) {
                removeTweetFromList(tweetIds,currentList);
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
    public void removeTweetFromList(String bulkTweetIds, MyListEntry currentList){
        try {
            InternalDB.getInstance(getContext()).removeBulkTweetsFromSavedTweets(bulkTweetIds,currentList);
            mSelectedEntries = new ArrayList<>();
            mDataset = InternalDB.getInstance(getContext()).getSavedTweets(mMyListEntry,mColorThresholds);
            mAdapter.swapDataSet(mDataset);
        } catch (NullPointerException e) {
            Log.e(TAG,"Nullpointer in SavedTweetsBrowseFragment removeTweetFromList : " + e);
            Toast.makeText(getContext(), "Unable to delete entries", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            Log.e(TAG,"SQLiteException in SavedTweetsBrowseFragment removeTweetFromList : " + e);
            Toast.makeText(getContext(), "Unable to delete entries", Toast.LENGTH_SHORT).show();
        }
    }



    public void removeTweetFromList(){
        try {
            final String tweetIds = joinSelectedStrings(mSelectedEntries);
//            Log.d(TAG,"FINAL STRING: " + tweetIds);
            InternalDB.getInstance(getContext()).removeBulkTweetsFromSavedTweets(tweetIds,mMyListEntry);
            mDataset = InternalDB.getInstance(getContext()).getSavedTweets(mMyListEntry,mColorThresholds);
            mAdapter.swapDataSet(mDataset);
            showUndoPopupTweets(tweetIds,mMyListEntry);
        } catch (NullPointerException e) {
            Log.e(TAG,"Nullpointer in SavedTweetsBrowseFragment removeTweetFromList : " + e);
            Toast.makeText(getContext(), "Unable to delete entries", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            Log.e(TAG,"SQLiteException in SavedTweetsBrowseFragment removeTweetFromList : " + e);
            Toast.makeText(getContext(), "Unable to delete entries", Toast.LENGTH_SHORT).show();
        }
    }



    //TODO CONSOLIDATE WITH TWINs (? IN COPYMYLISTITEMSDIALOG AND COPY TWEETS DIALOG
    public String joinSelectedStrings(ArrayList<String> list ) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); ++i) {
            if (i>0) {
                sb.append(", ");
            }
            sb.append(list.get(i));
        }
        return sb.toString();
    }


    public void showUndoPopupTweets(final String bulkTweetIds, final MyListEntry currentList) {

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

        undoSubscription  =  Observable.timer(3, SECONDS).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        popupWindow.dismiss();
                    }
                });

        TextView undoButton = (TextView) v.findViewById(R.id.undoButton);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InternalDB.getInstance(getContext()).addBulkTweetsToList(currentList,bulkTweetIds);
                    mDataset = InternalDB.getInstance(getContext()).getSavedTweets(mMyListEntry,mColorThresholds);
                    mSelectedEntries.clear();
                    mAdapter.swapDataSet(mDataset);
                    try {
                        popupWindow.dismiss();
                        undoSubscription.unsubscribe();
                    } catch (Exception e) {

                    }

                } catch (NullPointerException e) {
                    Log.e(TAG,"Nullpointer in SavedtweetsBrowseFragment showUndoPopup : re-add" + e);
                    Toast.makeText(getContext(), "Unable to undo delete!", Toast.LENGTH_SHORT).show();
                } catch (SQLiteException e) {
                    Log.e(TAG,"SQLiteException in SavedtweetsBrowseFragment showUndoPopup re-add : " + e);
                    Toast.makeText(getContext(), "Unable to undo delete!", Toast.LENGTH_SHORT).show();
                }

            }
        });


        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.popup_drawable));
        popupWindow.setContentView(v);
        popupWindow.showAtLocation(mRecyclerView, Gravity.BOTTOM, 0, (int)(metrics.heightPixels / (float)9.5));
        // create a single event in 10 seconds time


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

