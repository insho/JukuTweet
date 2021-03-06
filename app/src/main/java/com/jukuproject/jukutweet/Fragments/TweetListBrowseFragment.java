package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Adapters.BrowseTweetsAdapter;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.CopySavedTweetsDialog;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Interfaces.TweetListOperationsInterface;
import com.jukuproject.jukutweet.MainActivity;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;
import com.jukuproject.jukutweet.TabContainers.BaseContainerFragment;

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
public class TweetListBrowseFragment extends Fragment {
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
    private UserInfo mUserInfo;
    private ArrayList<Pair<MyListEntry,Tweet>> mSingleUserUndoPairs;

    public TweetListBrowseFragment() {}

    public static TweetListBrowseFragment newInstance(MyListEntry myListEntry) {
        TweetListBrowseFragment fragment = new TweetListBrowseFragment();

        Bundle args = new Bundle();
        args.putParcelable("mylistentry", myListEntry);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Create new instance of fragment focusing on saved tweets of a SINGLE USER
     * @param userInfo Info of specific user whose saved tweets will be displayed
     * @return TweetListBrowseFragment
     */
    public static TweetListBrowseFragment newInstance(UserInfo userInfo) {
        TweetListBrowseFragment fragment = new TweetListBrowseFragment();

        Bundle args = new Bundle();
        args.putParcelable("userInfo",userInfo);
        fragment.setArguments(args);
        return fragment;
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
        mColorThresholds = SharedPrefManager.getInstance(getContext()).getColorThresholds();

        if(savedInstanceState != null) {
            mDataset = savedInstanceState.getParcelableArrayList("mDataset");
            mMyListEntry = savedInstanceState.getParcelable("mMyListEntry");
            mUserInfo = savedInstanceState.getParcelable("mUserInfo");
            mSelectedEntries = savedInstanceState.getStringArrayList("mSelectedEntries");

        } else {
            if (getArguments() != null
                    && ((mMyListEntry = getArguments().getParcelable("mylistentry")) != null)) {
                mDataset = InternalDB.getTweetInterfaceInstance(getContext()).getTweetsForSavedTweetsList(mMyListEntry,mColorThresholds);
                mUserInfo = null;
            } else if(getArguments() != null && ((mUserInfo = getArguments().getParcelable("userInfo")) != null))  {
                mDataset = InternalDB.getTweetInterfaceInstance(getContext()).getTweetsForSavedTweetsList(mUserInfo,mColorThresholds);
                mMyListEntry = null;
            } else {
                Toast.makeText(getContext(), "Unable to access tweets", Toast.LENGTH_SHORT).show();
                mCallback.onBackPressed();
            }
        }

        /*If orientation changed and some rows were selected before activity restarts,
         show the browse menu when activity is recreated */
        if(mSelectedEntries != null && mSelectedEntries.size()>0) {
            showMenuMyListBrowse(true);
        }

        setUpAdapter();


    }

    /**
     * If saved fragments have changed (tweets added/deleted) in the MainActivity, this is called to update
     * the current dataset
     *
     * @see MainActivity#notifySavedTweetFragmentsChanged()
     */
    public void updateMyListAdapter() {
        if (mMyListEntry != null) {
            mDataset = InternalDB.getTweetInterfaceInstance(getContext()).getTweetsForSavedTweetsList(mMyListEntry,mColorThresholds);
            setUpAdapter();
        } else if(mUserInfo != null) {
            mDataset = InternalDB.getTweetInterfaceInstance(getContext()).getTweetsForSavedTweetsList(mUserInfo,mColorThresholds);
            setUpAdapter();
        }
    }

    public void setUpAdapter(){

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //Create UserListAdapter and attach rxBus click listeners to it
        if(mDataset != null && mDataset.size() > 0){
            mAdapter = new BrowseTweetsAdapter(getContext(), mRxBus, mDataset, mSelectedEntries);

            mRxBus.toClickObserverable()
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {


                        if(isUniqueClick(500)) {

                            if (event instanceof String) {

                                String tweet_id = (String) event;

                                if (!mSelectedEntries.contains(tweet_id)) {
                                    if (mSelectedEntries.size() == 0) {
                                        showMenuMyListBrowse(true);
                                        if(BuildConfig.DEBUG){Log.d(TAG, "showing menu");}
                                    }
                                    if(BuildConfig.DEBUG){Log.d(TAG, "selected adding: " + tweet_id);}
                                    mSelectedEntries.add(tweet_id);
                                    if(BuildConfig.DEBUG){Log.d(TAG, "selected size: " + mSelectedEntries.size());}

                                } else {
                                    mSelectedEntries.remove(tweet_id);
                                    if(BuildConfig.DEBUG){
                                        Log.d(TAG, "selected removing: " + tweet_id);
                                        Log.d(TAG, "selected size: " + mSelectedEntries.size());
                                    }
                                }

                                if (mSelectedEntries.size() == 0) {
                                    showMenuMyListBrowse(false);
                                    if(BuildConfig.DEBUG){Log.d(TAG, "hiding menu");}
                                }

                            } else if(event instanceof Tweet) {

                                /*Entire tweet sent back from adapter, so open tweetbreakdownfragment with
                                  saved TweetKanjiColor object (which is part of Tweet object) fascilitaing the parsing */
                                Tweet tweet = (Tweet) event;
                                TweetBreakDownFragment fragment = TweetBreakDownFragment.newInstanceSavedTweet(tweet);
                                ((BaseContainerFragment)getParentFragment()).addFragment(fragment, true,"savedtweetbreakdown");
                                mCallback.showFab(false,"");
                            }

                        }


                        }

                    });



            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setVerticalScrollBarEnabled(true);

        }

    }

    /**
     * Displays {@link CopySavedTweetsDialog} when user presses copy button in navigation bar. This is a callback
     * from {@link MainActivity#onOptionsItemSelected(MenuItem)}
     */
    public void showCopyTweetsDialog(){
        if(getActivity().getSupportFragmentManager().findFragmentByTag("dialogCopyTweet") == null || !getActivity().getSupportFragmentManager().findFragmentByTag("dialogCopyTweet").isAdded()) {
            CopySavedTweetsDialog.newInstance(mMyListEntry,mSelectedEntries).show(getActivity().getSupportFragmentManager(),"dialogCopyTweet");
        }
    }

    /**
     * Clears selected entries when user presses cancel button in navigation bar. This is a callback
     * from {@link MainActivity#onOptionsItemSelected(MenuItem)}
     */
    public void deselectAll(){
        mSelectedEntries.clear();
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Selects all items when user presses select-all button in navigation bar. This is a callback
     * from {@link MainActivity#onOptionsItemSelected(MenuItem)}
     */
    public void selectAll() {
        if(mSelectedEntries.size() != mDataset.size()) {
            mSelectedEntries.clear();
            for(Tweet entry : mDataset) {
                mSelectedEntries.add(entry.getIdString());
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * This performs the transaction when user has chosen tweets to move/copy in the {@link com.jukuproject.jukutweet.Dialogs.CopySavedTweetsDialog}.
     * Tweets are saved to the chosen lists, and removed (if the user so chose) from this list, and then the table refreshed
     *
     * @param tweetIds Tweet ids to move/copy
     * @param listsToCopyTo Tweet Lists to move/copy to
     * @param move bool true to move the lists (removing them from the current list as well), or just to copy to other lists
     * @param currentList The current MyList object
     */
    public void saveAndUpdateTweets(String tweetIds,ArrayList<MyListEntry> listsToCopyTo, boolean move,MyListEntry currentList) {
        TweetListOperationsInterface helperTweetOps = InternalDB.getTweetInterfaceInstance(getContext());
        try {
            for(MyListEntry entry : listsToCopyTo) {
                helperTweetOps.addMultipleTweetsToTweetList(entry,tweetIds);
            }
            if(move) {
                Toast.makeText(getContext(), "Items moved successfully", Toast.LENGTH_SHORT).show();
                removeTweetFromList(tweetIds,currentList);
            } else {
                mCallback.notifySavedTweetFragmentsChanged();
                Toast.makeText(getContext(), "Items copied successfully", Toast.LENGTH_SHORT).show();

            }

            deselectAll();
            showMenuMyListBrowse(false);
        } catch (NullPointerException e) {
            Log.e(TAG,"Nullpointer in WordListBrowseFragment saveAndUpdateMyLists : " + e);
            Toast.makeText(getContext(), "Unable to update lists", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            Log.e(TAG,"SQLiteException in WordListBrowseFragment saveAndUpdateMyLists : " + e);
            Toast.makeText(getContext(), "Unable to update lists", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Performs "delete tweet" transaction when user has chosen to MOVE the selected tweets from the current
     * tweet list. THis deletes the tweets in the current list after they've been copied.
     * @param bulkTweetIds concatenated comma delimited string of tweet ids
     * @param currentList the current mylist from which the tweets are being deleted
     *
     * @see CopySavedTweetsDialog
     */
    public void removeTweetFromList(String bulkTweetIds, MyListEntry currentList){
        try {
            InternalDB.getTweetInterfaceInstance(getContext()).removeMultipleTweetsFromTweetList(bulkTweetIds,currentList);


            mSelectedEntries.clear();
            mDataset = InternalDB.getTweetInterfaceInstance(getContext()).getTweetsForSavedTweetsList(mMyListEntry,mColorThresholds);
            mAdapter.swapDataSet(mDataset);
            mCallback.notifySavedTweetFragmentsChanged();

        } catch (NullPointerException e) {
            Log.e(TAG,"Nullpointer in TweetListBrowseFragment removeTweetFromList : " + e);
            Toast.makeText(getContext(), "Unable to delete entries", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            Log.e(TAG,"SQLiteException in TweetListBrowseFragment removeTweetFromList : " + e);
            Toast.makeText(getContext(), "Unable to delete entries", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Performs "delete tweet" transaction when user deletes a selected tweet or tweets by pressing the trash icon
     * in the navbar. This is a callback from {@link MainActivity#onOptionsItemSelected(MenuItem)}
     */
    public void removeTweetFromList(){
        try {
            final String tweetIds = joinSelectedStrings(mSelectedEntries);

            if(mMyListEntry!=null) {
                InternalDB.getTweetInterfaceInstance(getContext()).removeMultipleTweetsFromTweetList(tweetIds,mMyListEntry);
                mDataset = InternalDB.getTweetInterfaceInstance(getContext()).getTweetsForSavedTweetsList(mMyListEntry,mColorThresholds);
            } else if(mUserInfo!=null) {
                mSingleUserUndoPairs = InternalDB.getTweetInterfaceInstance(getContext()).removeTweetsFromAllTweetLists(tweetIds);
                mDataset = InternalDB.getTweetInterfaceInstance(getContext()).getTweetsForSavedTweetsList(mUserInfo,mColorThresholds);
            }

            mCallback.notifySavedTweetFragmentsChanged();

            mSelectedEntries.clear();
            mAdapter.swapDataSet(mDataset);
            showUndoPopupTweets(tweetIds,mMyListEntry);

        } catch (NullPointerException e) {
            Log.e(TAG,"Nullpointer in TweetListBrowseFragment removeTweetFromList : " + e);
            Toast.makeText(getContext(), "Unable to delete entries", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            Log.e(TAG,"SQLiteException in TweetListBrowseFragment removeTweetFromList : " + e);
            Toast.makeText(getContext(), "Unable to delete entries", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Joins an array of strings into a single comma-delimited string
     * @param list string array to flatten into a comma-delimited string
     * @return comma delimited string
     */
    public static String joinSelectedStrings(ArrayList<String> list ) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); ++i) {
            if (i>0) {
                sb.append(", ");
            }
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    /**
     * Shows the "undo changes" popup window after user deletes selected rows. The window stays visible for 3 seconds,
     * giving the user a chance to click on the "undo" button and reverse the changes.
     * @param bulkTweetIds concatenate (and comma seperated) string of tweet ids to move
     * @param currentList The current my list that is being browsed
     */
    public void showUndoPopupTweets(final String bulkTweetIds, final MyListEntry currentList) {

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.popup_undo, null);

        final PopupWindow popupWindow = new PopupWindow(getContext());
        popupWindow.setFocusable(true);
        popupWindow.setClippingEnabled(false);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
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

                    if(mMyListEntry!=null) {
                        InternalDB.getTweetInterfaceInstance(getContext()).addMultipleTweetsToTweetList(currentList,bulkTweetIds);
                        mDataset = InternalDB.getTweetInterfaceInstance(getContext()).getTweetsForSavedTweetsList(mMyListEntry,mColorThresholds);
                    } else if(mUserInfo!=null && mSingleUserUndoPairs!=null){
                        TweetListOperationsInterface tweetOps = InternalDB.getTweetInterfaceInstance(getContext());
                        for(Pair<MyListEntry,Tweet> pair : mSingleUserUndoPairs ) {
                            tweetOps.addTweetToTweetList(pair.second.getIdString(),pair.second.getUser().getUserId(),pair.first.getListName(),pair.first.getListsSys());
                        }
                        mDataset = InternalDB.getTweetInterfaceInstance(getContext()).getTweetsForSavedTweetsList(mUserInfo,mColorThresholds);

                    }

                        mCallback.notifySavedTweetFragmentsChanged();

                    mSelectedEntries.clear();
                    mAdapter.swapDataSet(mDataset);
                    try {
                        popupWindow.dismiss();
                        undoSubscription.unsubscribe();
                    } catch (NullPointerException e) {
                        Log.e(TAG,"tweetlistbrowsefragment nullpointer on undo button : " + e.getCause());
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

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if(mUserInfo!=null) {
                    Log.d(TAG,"Dismiss listener is firing!");
                    InternalDB.getTweetInterfaceInstance(getContext()).deleteTweetIfNecessary(bulkTweetIds);

                    /*If the tweet's user is not saved in the db and there are no tweets for the user anymore
                     * , delete the user's icon from the app icons folder */
                    InternalDB.getUserInterfaceInstance(getContext()).clearOutUnusedUserIcons(getContext());
                }
            }
        });

        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.popup_drawable));
        popupWindow.setContentView(v);
        popupWindow.showAtLocation(mRecyclerView, Gravity.BOTTOM, 0, (int)(metrics.heightPixels / (float)9.5));
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

    /**
     * Displays the actionbar icon set for selecting/copying/deleting items from a tweet list,
     * either for a single user saved tweets (UserInfo) or tweetlist (MyListEntry)
     * @param show bool true for show the array in navbar, false for hide
     *
     * @see MainActivity#showMenuMyListBrowse(boolean, int)
     */
    private void showMenuMyListBrowse(boolean show) {
        if(mUserInfo!=null) {
            mCallback.showMenuMyListBrowse(show,0);
        } else if(mMyListEntry!=null) {
            mCallback.showMenuMyListBrowse(show,1);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(undoSubscription!=null ) {
            undoSubscription.unsubscribe();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("mMyListEntry", mMyListEntry);
        outState.putParcelable("mUserInfo", mUserInfo);
        outState.putStringArrayList("mSelectedEntries", mSelectedEntries);
        outState.putParcelableArrayList("mDataset", mDataset);
    }
}

