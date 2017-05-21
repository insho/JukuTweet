package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.ChooseFavoriteListsPopupWindow;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.FavoritesColors;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetUserMentions;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;
import com.jukuproject.jukutweet.TabContainers.BaseContainerFragment;
import com.jukuproject.jukutweet.TwitterUserClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Shows last X tweets from a twitter timeline. User can click on a tweet to bring up
 * the WordBreakDown Popup window, save the tweet and/or specific kanji from the tweet
 */
public class UserTimeLineFragment extends Fragment {
    FragmentInteractionListener mCallback;

    /* mLastClickTime Tracks elapsed time since last click of a recyclerview row. Used to
    * keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
    private RxBus _rxBus = new RxBus();
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeToRefreshLayout;
    private TextView mNoLists;
    private UserInfo mUserInfo;
    private ArrayList<Tweet> mDataSet;
    private DisplayMetrics mMetrics;
    private Long mDataSetMaxId;
    private Integer mPreviousMaxScrollPosition =0;
    /* Holds a list of tweets that have been favorited (in any/all lists). Used to check
    * whether or not a tweet needs to have favorites assigned to it. This exists
    * so that we dont' have to make a sql query for each Tweet that gets returned from
    * the api lookup */
    private HashMap<String,ItemFavorites> tweetIdStringsInFavorites;

    private ArrayList<String> mActiveTweetFavoriteStars;
    private Subscription timeLineSubscription;
    private Subscription timerSubscription;
    private LinearLayoutManager mLayoutManager;
    private Boolean searchInProgress = false;
//    private int userCreatedListCount;
//    private TweetBreakDownFragment mTweetBreakDownFragment;

    private static final String TAG = "TEST-TimeLineFrag";

    public UserTimeLineFragment() {}

    /**
     * Returns a new instance of UserListFragment
     */
    public static UserTimeLineFragment newInstance(UserInfo userInfo) {
        UserTimeLineFragment fragment = new UserTimeLineFragment();
        Bundle args = new Bundle();
        args.putParcelable("userInfo",userInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerTimeLine);
        mNoLists = (TextView) view.findViewById(R.id.notweets);
        mSwipeToRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        //Get active favorite stars for tweets, to pass on to adapter for star-clicking tweet-saving
        mActiveTweetFavoriteStars = SharedPrefManager.getInstance(getContext()).getActiveTweetFavoriteStars();

        if(savedInstanceState==null) {
            mUserInfo = getArguments().getParcelable("userInfo");
            mDataSet = null;
            mDataSetMaxId = null;

        } else {
            mUserInfo = savedInstanceState.getParcelable("mUserInfo");
            mDataSet = savedInstanceState.getParcelableArrayList("mDataSet");
            if(!savedInstanceState.getBoolean("mDataSetMaxIdisNull",true)) {
                mDataSetMaxId = savedInstanceState.getLong("mDataSetMaxId");
            } else  {

                mDataSetMaxId = null;
            }
            searchInProgress = savedInstanceState.getBoolean("searchInProgress");
        }
        tweetIdStringsInFavorites = InternalDB.getTweetInterfaceInstance(getContext()).getStarFavoriteDataForAUsersTweets(mUserInfo.getUserId());
//        userCreatedListCount = InternalDB.getTweetInterfaceInstance(getContext()).getUserCreatedTweetListCount();

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);




        /* Listen for the user scrolling to the final position in the scrollview. IF it happens, load more
        * userinfo items into the adapter */
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            mRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                        if(mLayoutManager != null
                                && mDataSet!=null
                                && mDataSet.size()>0
                                && mLayoutManager.findFirstCompletelyVisibleItemPosition()>0
                                && mLayoutManager.findLastCompletelyVisibleItemPosition()==mDataSet.size()-1
                                && mDataSet.size()-1>mPreviousMaxScrollPosition) {
                                if(BuildConfig.DEBUG){Log.d(TAG,"pulling timeline after scroll. dataset size: " + mDataSet.size() + ", prev pos: " + mPreviousMaxScrollPosition);}
                                mPreviousMaxScrollPosition = mDataSet.size()-1;
                                pullTimeLineData(mUserInfo);
                        }
                }
            });
        } else {
            mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if(mLayoutManager != null
                            && mDataSet!=null
                            && mDataSet.size()>0
                            && mLayoutManager.findFirstCompletelyVisibleItemPosition()>0
                            &&  mLayoutManager.findLastCompletelyVisibleItemPosition()==mDataSet.size()-1
                            && mDataSet.size()-1>mPreviousMaxScrollPosition) {
                        if(BuildConfig.DEBUG) {
                            Log.d(TAG,"pulling timeline after scroll. dataset size: " + mDataSet.size() + ", prev pos: " + mPreviousMaxScrollPosition);
                        }
                            mPreviousMaxScrollPosition = mDataSet.size()-1;
                            pullTimeLineData(mUserInfo);
                        }

                }
            });
        }

        mSwipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                    if(!mCallback.isOnline()) {
                        showRecyclerView(false);
                        mNoLists.setTextColor(ContextCompat.getColor(getContext(),android.R.color.holo_red_dark));
                        mNoLists.setText(getResources().getString(R.string.nointernet));
                        mSwipeToRefreshLayout.setRefreshing(false);
                    } else if(mUserInfo != null) {
                        mDataSet = null;
                        mSwipeToRefreshLayout.setRefreshing(false);
                        Log.d(TAG,"Swipe to refresh pull dta");
                        pullTimeLineData(mUserInfo);
                    } else {
                        Log.d(TAG,"SET REFRESHING fALSE HERE...");
                        mSwipeToRefreshLayout.setRefreshing(false);
                    }

            }
        });

        if(searchInProgress && mUserInfo != null) {
            pullTimeLineData(mUserInfo);
        } else if(mDataSet !=null) {
            setUpAdapter();
        } else if(!mCallback.isOnline()) {
            showRecyclerView(false);
            mCallback.showProgressBar(false);
            mNoLists.setTextColor(ContextCompat.getColor(getContext(),android.R.color.holo_red_dark));
            mNoLists.setText(getResources().getString(R.string.nointernet));
        } else if(mUserInfo != null) {
            pullTimeLineData(mUserInfo);
        }

    }

    /**
     * Accesses twitters api and pulls the last X tweets from a user's timeline into
     * the UserTimeLineAdapter
     * @param userInfo UserInfo object with data from a single twitter user
     */
    public void pullTimeLineData(final UserInfo userInfo){
        mCallback.showProgressBar(true);

        if(mDataSet ==null) {
            mDataSet = new ArrayList<>();
        }
            String token = getResources().getString(R.string.access_token);
            String tokenSecret = getResources().getString(R.string.access_token_secret);

            Observable<Long> observable = Observable.timer(5, TimeUnit.SECONDS, Schedulers.io());

            if(!mSwipeToRefreshLayout.isRefreshing() && mDataSet.size()==0) {
                timerSubscription =  observable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                if(mDataSet ==null || mDataSet.size()==0 && isVisible()) {
                                    showRecyclerView(false);
                                    mNoLists.setText(getString(R.string.usertimeline_stillworking));
                                    mNoLists.setTextColor(ContextCompat.getColor(getContext(),android.R.color.black));
                                }
                            }
                        });
            }

          timeLineSubscription =  TwitterUserClient.getInstance(token,tokenSecret)
                    .getUserTimeline(userInfo.getScreenName(),25,mDataSetMaxId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<Tweet>>() {

                        @Override public void onCompleted() {
                            if(BuildConfig.DEBUG){Log.d(TAG, "In onCompleted()");}
                            searchInProgress = false;
                            mCallback.showProgressBar(false);
                            if(timerSubscription!=null) {
                                timerSubscription.unsubscribe();
                            }
                            mSwipeToRefreshLayout.setRefreshing(false);

                            setUpAdapter();


                        /* Check most recent user info (if it exists within timeline api response)
                        * against the user info stored in db. update db with any changed ino */
                            //And make it its own void
                            if(mDataSet != null && mDataSet.size() > 0) {
                                try {
                                    UserInfo recentUserInfo = mDataSet.get(0).getUser();
                                    if(BuildConfig.DEBUG){Log.d(TAG,"UPDATING PROFILE IMAGE? - " + recentUserInfo.getProfileImageUrl());}
                                    if(userInfo.getProfileImageFilePath()==null
                                            && recentUserInfo.getProfileImageUrl()!=null){
                                        if(BuildConfig.DEBUG){Log.i(TAG,"... YES. UPDATING PROFILE IMAGE");}
                                        InternalDB.getUserInterfaceInstance(getContext()).downloadUserIcon(getContext(),recentUserInfo.getProfileImageUrlBig(),recentUserInfo.getUserId());
                                    }
                                    InternalDB.getUserInterfaceInstance(getContext()).compareUserInfoAndUpdate(userInfo,recentUserInfo);

                                } catch (NullPointerException e) {
                                    Log.e(TAG,"Timeline userinfo match problem: " + e);
                                }


                            }


                        }

                        @Override public void onError(Throwable e) {
                            searchInProgress = false;
                            e.printStackTrace();
                            if(BuildConfig.DEBUG){Log.d(TAG, "In onError()");}
                            mCallback.showProgressBar(false);
                            mSwipeToRefreshLayout.setRefreshing(false);
                            showRecyclerView(false);
                            if(timerSubscription!=null) {
                                timerSubscription.unsubscribe();
                            }
                            mNoLists.setText(getResources().getString(R.string.notimeline_for_user,userInfo.getDisplayScreenName()));
                            mNoLists.setTextColor(ContextCompat.getColor(getContext(),android.R.color.holo_red_dark));
                        }

                        @Override public void onNext(List<Tweet> timeline) {
                            if(BuildConfig.DEBUG) {
                                Log.d(TAG, "In onNext()");
                                Log.d(TAG,"TIMELINE SIZE: " + timeline.size());
                            }
                            showRecyclerView(true);
                            if(timerSubscription!=null) {
                                timerSubscription.unsubscribe();
                            }
//                            if(mDataSet.size() == 0) {

                                for(Tweet tweet : timeline) {
                                    Log.d(TAG,"timeline urls: " + tweet.getEntities().getUrls());
                                    Log.d(TAG,"timeline mentions: " + tweet.getEntities().getUser_mentions());

                                    //Attach colorfavorites to tweet, if they exists in db
                                    if(tweet.getIdString()!=null && tweetIdStringsInFavorites.keySet().contains(tweet.getIdString())) {
                                        tweet.setItemFavorites(tweetIdStringsInFavorites.get(tweet.getIdString()));
                                    } else {
                                        tweet.setItemFavorites(new ItemFavorites());
                                    }

                                    mDataSet.add(tweet);
                                }

                                mDataSetMaxId = Long.valueOf(mDataSet.get(mDataSet.size()-1).getIdString());
                        }
                    });
    }

    /**
     * Toggles between showing recycler (if there are followed users in the database)
     * and hiding the recycler while showing the "no users found" message if there are not
     * @param show bool True to show recycler, False to hide it
     */
    private void showRecyclerView(boolean show) {
        if(show) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mNoLists.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mNoLists.setTextColor(ContextCompat.getColor(getContext(),android.R.color.black));
            mNoLists.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        setRetainInstance(true);
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

    @Override
    public void onDestroy() {
        if(timeLineSubscription!=null) {
            timeLineSubscription.unsubscribe();
        }
        if(timerSubscription!=null) {
            timerSubscription.unsubscribe();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if(timeLineSubscription!=null) {
            timeLineSubscription.unsubscribe();
        }
        if(timerSubscription!=null) {
            timerSubscription.unsubscribe();
        }
        super.onPause();
    }

    public void setUpAdapter() {

        UserTimeLineAdapter mAdapter = new UserTimeLineAdapter(getContext(),_rxBus, mDataSet,mActiveTweetFavoriteStars,null,true);

        /* If user info is missing from db, update the user information in db*/
        if(mUserInfo.getUserId()==null && mDataSet.size()>0 && mDataSet.get(0).getUser() != null) {
            InternalDB.getUserInterfaceInstance(getContext()).updateUserInfo(mDataSet.get(0).getUser());
        }


        _rxBus.toClickObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {

                        if(isUniqueClick(1000) && event instanceof Tweet) {

                            Tweet tweet = (Tweet) event;
                            TweetBreakDownFragment fragment = TweetBreakDownFragment.newInstanceTimeLine(tweet);
                            ((BaseContainerFragment)getParentFragment()).replaceFragment(fragment, true,"tweetbreakdown");
                            mCallback.showFab(false,"");


                        } else if(isUniqueClick(1000) && event instanceof TweetUserMentions) {

                            TweetUserMentions userMentions = (TweetUserMentions) event;
                            mCallback.getInitialUserInfoForAddUserCheck(userMentions.getScreen_name());

                        }

                    }

                });

        _rxBus.toSaveTweetObserverable().subscribe(new Action1<Object>() {

            @Override
            public void call(Object event) {

                if (isUniqueClick(1000) && event instanceof Integer) {

                    Integer tweetWordEntryIndex = (Integer) event;
                    //Activity windows height
                    int[] location = new int[2];

                    UserTimeLineAdapter.ViewHolder viewHolder = (UserTimeLineAdapter.ViewHolder)mRecyclerView.getChildViewHolder(mRecyclerView.getChildAt(tweetWordEntryIndex));
                    mRecyclerView.getChildAt(tweetWordEntryIndex).getLocationInWindow(location);
//                    Log.i(TAG,"location x: " + location[0] + ", y: " + location[1]);

                    showTweetFavoriteListPopupWindow(viewHolder,mDataSet.get(tweetWordEntryIndex),mMetrics,location[1]);

                } else if(isUniqueClick(150) && event instanceof Tweet) {
                    final Tweet tweet = (Tweet) event;
                    saveOrDeleteTweet(tweet);
                }


            }

        });



        if(mDataSet !=null && mDataSet.size()>0) {
            mRecyclerView.setAdapter(mAdapter);
            if(mDataSet.size()-1>mPreviousMaxScrollPosition ) {
                mRecyclerView.scrollToPosition(mPreviousMaxScrollPosition);
            }
            showRecyclerView(true);
        } else {
            showRecyclerView(false);
        }
    }

    /**
     * Decides via {@link com.jukuproject.jukutweet.Interfaces.TweetListOperationsInterface#saveOrDeleteTweet(Tweet)} whether to
     * save the tweet or remove it from the db (if it already exists and is unnecessary). If it saves the tweet, it also decides via
     * whether or not to download the tweet icon with a callback to the "downloadTweetUserIcons" method in the activity. Lastly
     * it notifies tweet related fragments that a change has been made
     * @param tweet Tweet that will be saved or deleted
     */
    public void saveOrDeleteTweet(Tweet tweet){
        //Check for tweet in db
        try {

            Log.i(TAG,"ENTERING saveOrDeleteTweet");
            //If tweet was not in the saved tweets database, and was then successfully saved, download tweet icon
            int savedOrDeleteResultCode = InternalDB.getTweetInterfaceInstance(getContext()).saveOrDeleteTweet(tweet);
            Log.i(TAG,"saveOrDeleteTweet returned result code: " + savedOrDeleteResultCode);
            if(savedOrDeleteResultCode==1) {


                if(tweet.getUser()!=null
                        && !InternalDB.getUserInterfaceInstance(getContext()).duplicateUser(tweet.getUser().getUserId())) {
                    mCallback.downloadTweetUserIcons(tweet.getUser());
                    Log.i(TAG,"saveOrDeleteTweet download tweet icon");
                }
                //Try to parse and insert Tweet Kanji if they do not already exist
                if(InternalDB.getTweetInterfaceInstance(getContext()).tweetParsedKanjiExistsInDB(tweet) == 0) {
                    mCallback.parseAndSaveTweet(tweet);
                }
            }
            if(savedOrDeleteResultCode>=1) {
                Log.i(TAG,"saveOrDeleteTweet notify changed...");
                mCallback.notifySavedTweetFragmentsChanged();
            }

        } catch (SQLiteException sqlexception){
            Log.e(TAG,"saveOrDeleteTweet - sqlite exception when tweet saving, UNABLE to save!");

        } catch (NullPointerException e){
            Log.e(TAG,"saveOrDeleteTweet - nullpointer when tweet saving, UNABLE to save!");

        }
    }


    public void showTweetFavoriteListPopupWindow(final UserTimeLineAdapter.ViewHolder holder
            ,final Tweet mTweet
            , DisplayMetrics mMetrics
            , int ylocation) {

        RxBus rxBus = new RxBus();
        ArrayList<MyListEntry> availableFavoriteLists = InternalDB.getTweetInterfaceInstance(getContext()).getTweetListsForTweet(mActiveTweetFavoriteStars,mTweet.getIdString(),null);

        PopupWindow popupWindow = ChooseFavoriteListsPopupWindow.createTweetFavoritesPopup(getContext(),mMetrics,rxBus,availableFavoriteLists,mTweet.getIdString(), mTweet.getUser().getUserId());

        popupWindow.getContentView().measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        int xadjust = popupWindow.getContentView().getMeasuredWidth() + (int) (10 * mMetrics.density + 0.5f);

        int popupMeasuredHeight;
        if(availableFavoriteLists.size()>10) {
            popupMeasuredHeight = (int)((float)mMetrics.heightPixels/2.0f);
        } else {
            popupMeasuredHeight =  popupWindow.getContentView().getMeasuredHeight();
        }

        //Firstly, center the list at the favorite star
        int yadjust = (int)(popupMeasuredHeight/2.0f  + holder.imgStar.getMeasuredHeight()/2.0f);
        final int displayheight = mMetrics.heightPixels;

        if(BuildConfig.DEBUG) {
            Log.i(TAG,"INITIAL yloc: " + ylocation);
            Log.i(TAG,"INITIAL imgstar height/2: " + holder.imgStar.getMeasuredHeight()/2.0f);
            Log.i(TAG,"INITIAL popupheight/2: " + popupMeasuredHeight/2.0f);
            Log.i(TAG,"INITIAL yadjust: " + yadjust);
            Log.i(TAG,"INITIAL Displayheight: " + displayheight);
        }

        //Overrun at bottom of screen
        while(ylocation + holder.imgStar.getMeasuredHeight() - yadjust + popupMeasuredHeight >displayheight) {
//            Log.i(TAG,"SCREEN OVERRUN BOTTOM - " + (ylocation + holder.imgStar.getMeasuredHeight() - yadjust + popupMeasuredHeight)
//                    + " to display: " + displayheight + ",, reduce yadjust " + yadjust + " - " + (yadjust+10));
            yadjust += 10;
        }

//        //Overrun at bottom of screen
        while(ylocation + holder.imgStar.getMeasuredHeight() - yadjust < 0) {
//            Log.i(TAG,"SCREEN OVERRUN TOP - increase yadjust " + yadjust + " - " + (yadjust-10));
            yadjust -= 10;
        }


        Log.d("TEST","pop width: " + popupWindow.getContentView().getMeasuredWidth() + " height: " + popupWindow.getContentView().getMeasuredHeight());
        Log.d("TEST","xadjust: " + xadjust + ", yadjust: " + yadjust);


        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {



                /*Check to see if tweet must be deleted from db. Delete if necessary.
                            * Likewise adds a tweet to the db if it has just been added to a favorites list.
                            * This bit MUST come after the favorite star toggle, because it determines whether to
                            * add or delete a tweet based on whether that tweet is in a favorites list. */
                saveOrDeleteTweet(mTweet);
            }
        });



        rxBus.toClickObserverable().subscribe(new Action1<Object>() {
            @Override
            public void call(Object event) {

                                    /* Recieve a MyListEntry (containing an updated list entry for this row kanji) from
                                    * the ChooseFavoritesAdapter in the ChooseFavorites popup window */
                if(event instanceof MyListEntry) {
                    MyListEntry myListEntry = (MyListEntry) event;

                                        /* Ascertain the type of list that the kanji was added to (or subtracted from),
                                        and update that list's count */
                    if(myListEntry.getListsSys() == 1) {
                        switch (myListEntry.getListName()) {
                            case "Blue":
                                mTweet.getItemFavorites().setSystemBlueCount(myListEntry.getSelectionLevel());
                                break;
                            case "Green":
                                mTweet.getItemFavorites().setSystemGreenCount(myListEntry.getSelectionLevel());
                                break;
                            case "Red":
                                mTweet.getItemFavorites().setSystemRedCount(myListEntry.getSelectionLevel());
                                break;
                            case "Yellow":
                                mTweet.getItemFavorites().setSystemYellowCount(myListEntry.getSelectionLevel());
                                break;
                            case "Purple":
                                mTweet.getItemFavorites().setSystemPurpleCount(myListEntry.getSelectionLevel());
                                break;
                            case "Orange":
                                mTweet.getItemFavorites().setSystemOrangeCount(myListEntry.getSelectionLevel());
                                break;
                            default:
                                break;
                        }
                    } else {
                        if(myListEntry.getSelectionLevel() == 1) {
                            mTweet.getItemFavorites().addToUserListCount(1);
                        } else {
                            mTweet.getItemFavorites().subtractFromUserListCount(1);
                        }
                    }



                    if(mTweet.getItemFavorites().shouldOpenFavoritePopup(mActiveTweetFavoriteStars)
                            && mTweet.getItemFavorites().systemListCount(mActiveTweetFavoriteStars) >1) {
                        holder.imgStar.setColorFilter(null);
                        holder.imgStar.setImageResource(R.drawable.ic_twitter_multicolor_24dp);

                    } else {
                        holder.imgStar.setImageResource(R.drawable.ic_twitter_black_24dp);
                        try {
                            holder.imgStar.setColorFilter(ContextCompat.getColor(getContext(), FavoritesColors.assignStarColor(mTweet.getItemFavorites(),mActiveTweetFavoriteStars)));
                        } catch (NullPointerException e) {
                            Log.e(TAG,"UserTimeLineAdapter setting colorfilter nullpointer: " + e.getMessage());
                        }

                    }
                }
            }

        });

        popupWindow.showAsDropDown(holder.imgStar,-xadjust,-yadjust);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("mDataSet", mDataSet);
        outState.putParcelable("mUserInfo", mUserInfo);

        if(mDataSetMaxId!=null) {
            outState.putLong("mDataSetMaxId",mDataSetMaxId);
            outState.putBoolean("mDataSetMaxIdisNull",false);
        }

        outState.putBoolean("searchInProgress",searchInProgress);

    }

}

