package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
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
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter;
import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Interfaces.TweetListOperationsInterface;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;
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
    private UserTimeLineAdapter mAdapter;
    private TextView mNoLists;
    private UserInfo mUserInfo;
    private ArrayList<Tweet> mTimeLine;
    private DisplayMetrics mMetrics;
    /* Holds a list of tweets that have been favorited (in any/all lists). Used to check
    * whether or not a tweet needs to have favorites assigned to it. This exists
    * so that we dont' have to make a sql query for each Tweet that gets returned from
    * the api lookup */
    private HashMap<String,ItemFavorites> tweetIdStringsInFavorites;

    private ArrayList<String> mActiveTweetFavoriteStars;
    private Subscription timeLineSubscription;
    private Subscription timerSubscription;

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
            mTimeLine = null;
        } else {
            mUserInfo = savedInstanceState.getParcelable("mUserInfo");
            mTimeLine = savedInstanceState.getParcelableArrayList("mTimeLine");

        }
        tweetIdStringsInFavorites = InternalDB.getTweetInterfaceInstance(getContext()).getStarFavoriteDataForAUsersTweets(mUserInfo.getUserId());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);


        mSwipeToRefreshLayout.setEnabled(false);
        mSwipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!mCallback.isOnline()) {
                    showRecyclerView(false);
                    mNoLists.setTextColor(ContextCompat.getColor(getContext(),android.R.color.holo_red_dark));
                    mNoLists.setText(getResources().getString(R.string.notimeline_nointernet));

                } else if(mUserInfo != null) {
                    pullTimeLineData(mUserInfo);
                } else {
                    //TODO -- put error in place if there is no userinfo
                }
            }
        });


        if(mTimeLine!=null) {
            Log.d(TAG,"SETTING UP ADAPTER");
            setUpAdapter();
        } else if(!mCallback.isOnline()) {

            showRecyclerView(false);
            mNoLists.setTextColor(ContextCompat.getColor(getContext(),android.R.color.holo_red_dark));
            mNoLists.setText(getResources().getString(R.string.notimeline_nointernet));
            mSwipeToRefreshLayout.setEnabled(true);

        } else if(mUserInfo != null) {
            Log.d(TAG,"PULLING DATA");
            pullTimeLineData(mUserInfo);
        } else {
            Log.d(TAG,"WHAT THE FUFUUUUUUCK");
        }

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
//        updateAdapter();
        //TODO Put something in place to repull data on resume...
    }


    /**
     * Accesses twitters api and pulls the last X tweets from a user's timeline into
     * the UserTimeLineAdapter
     * @param userInfo UserInfo object with data from a single twitter user
     */
    public void pullTimeLineData(final UserInfo userInfo){
        mCallback.showProgressBar(true);

        if(mTimeLine==null) {
            mTimeLine = new ArrayList<>();
            String token = getResources().getString(R.string.access_token);
            String tokenSecret = getResources().getString(R.string.access_token_secret);

            Observable<Long> observable = Observable.timer(5, TimeUnit.SECONDS, Schedulers.io());

            if(!mSwipeToRefreshLayout.isRefreshing()) {
                timerSubscription =  observable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                if(mTimeLine==null || mTimeLine.size()==0) {
                                    showRecyclerView(false);
                                    mNoLists.setText("Still working...");
                                    mNoLists.setTextColor(ContextCompat.getColor(getContext(),android.R.color.black));
                                }
                            }
                        });
            }

            //TODO make the number of twitter responses an option! not just 10
           timeLineSubscription =  TwitterUserClient.getInstance(token,tokenSecret)
                    .getUserTimeline(userInfo.getScreenName(),10)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<Tweet>>() {

                        @Override public void onCompleted() {
                            if(BuildConfig.DEBUG){Log.d(TAG, "In onCompleted()");}
                            if(timerSubscription!=null) {
                                timerSubscription.unsubscribe();
                            }
                            mSwipeToRefreshLayout.setRefreshing(false);

                            setUpAdapter();

                            mCallback.showProgressBar(false);
                            Log.d(TAG,"show progress FALSE");
                            //TODO Make this its own subscribable that we can chain!
                        /* Check most recent user info (if it exists within timeline api response)
                        * against the user info stored in db. update db with any changed ino */
                            //And make it its own void
                            if(mTimeLine != null && mTimeLine.size() > 0) {
                                try {
                                    UserInfo recentUserInfo = mTimeLine.get(0).getUser();
                                    InternalDB.getUserInterfaceInstance(getContext()).compareUserInfoAndUpdate(userInfo,recentUserInfo);

                                } catch (NullPointerException e) {
                                    Log.e(TAG,"Timeline userinfo match problem: " + e);
                                }


                            }


                        }

                        @Override public void onError(Throwable e) {
                            e.printStackTrace();
                            if(BuildConfig.DEBUG){Log.d(TAG, "In onError()");}
                            mCallback.showProgressBar(false);
                            mSwipeToRefreshLayout.setRefreshing(false);
                            showRecyclerView(false);
                            if(timerSubscription!=null) {
                                timerSubscription.unsubscribe();
                            }
                            mNoLists.setText("Unable to get timeline for @" + userInfo.getScreenName());
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
                            if(mTimeLine.size() == 0) {

                                for(Tweet tweet : timeline) {
                                    Log.d(TAG,"timeline thing: " + tweet.getIdString());
                                    //Attach colorfavorites to tweet, if they exists in db
                                    if(tweet.getIdString()!=null && tweetIdStringsInFavorites.keySet().contains(tweet.getIdString())) {
                                        tweet.setItemFavorites(tweetIdStringsInFavorites.get(tweet.getIdString()));
                                    } else {
                                        tweet.setItemFavorites(new ItemFavorites());
                                    }

                                    mTimeLine.add(tweet);
                                }
                            }

                        }
                    });
        } else {
            mAdapter = new UserTimeLineAdapter(getContext(),_rxBus,mTimeLine,mActiveTweetFavoriteStars,mMetrics,null,true);
            mRecyclerView.setAdapter(mAdapter);
//            mCallback.showProgressBar(false);
        }


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
            mNoLists.setVisibility(View.VISIBLE);
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



    public void setUpAdapter() {

        mAdapter = new UserTimeLineAdapter(getContext(),_rxBus,mTimeLine,mActiveTweetFavoriteStars,mMetrics,null,true);

                            /* If user info is missing from db, update the user information in db*/
        if(mUserInfo.getUserId()==null && mTimeLine.size()>0 && mTimeLine.get(0).getUser() != null) {
            InternalDB.getUserInterfaceInstance(getContext()).updateUserInfo(mTimeLine.get(0).getUser());
        }


        _rxBus.toClickObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {

                        if(isUniqueClick(1000) && event instanceof Tweet) {

                            Tweet tweet = (Tweet) event;
                            TweetBreakDownFragment fragment = TweetBreakDownFragment.newInstanceTimeLine(tweet);
                            ((BaseContainerFragment)getParentFragment()).addFragment(fragment, true,"tweetbreakdown");
                            mCallback.showFab(false,"");


                        }

                    }

                });

        _rxBus.toSaveTweetObserverable().subscribe(new Action1<Object>() {

            @Override
            public void call(Object event) {

                if(isUniqueClick(1000) && event instanceof Tweet) {
                    final Tweet tweet = (Tweet) event;

                    //Try to insert urls
                    final TweetListOperationsInterface helperTweetOps = InternalDB.getTweetInterfaceInstance(getContext());
                    helperTweetOps.saveTweetUrls(tweet);

                    //Try to insert Kanji if they do not already exist
                    if(helperTweetOps.tweetParsedKanjiExistsInDB(tweet) == 0) {
                        Log.d(TAG,"SAVING TWEET KANJI");
//                                        final WordLoader wordLoader = helper.getWordLists(db);
                        mCallback.parseAndSaveTweet(tweet);
                    } else {
                        mCallback.notifySavedTweetFragmentsChanged();
                        Log.e(TAG,"Tweet parsed kanji exists code is funky");
                    }

                }




            }

        });



        if(mTimeLine!=null && mTimeLine.size()>0) {
            mRecyclerView.setAdapter(mAdapter);
            showRecyclerView(true);
            mSwipeToRefreshLayout.setEnabled(false);

        } else {
            showRecyclerView(false);
            mSwipeToRefreshLayout.setEnabled(true);
        }
    }

    //    @Override
//    public void onResume() {
//        super.onResume();
//        if()
//    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//        outState.putStringArrayList("mActiveTweetFavoriteStars", mActiveTweetFavoriteStars);
        outState.putParcelableArrayList("mTimeLine", mTimeLine);
        outState.putParcelable("mUserInfo", mUserInfo);


    }
}

