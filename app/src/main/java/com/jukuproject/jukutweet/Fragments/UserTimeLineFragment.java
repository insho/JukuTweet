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
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter;
import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.TwitterUserClient;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
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
    private UserTimeLineAdapter mAdapter;
    private TextView mNoLists;
    private UserInfo mUserInfo;
    private List<Tweet> mTimeLine;

    private TweetBreakDownFragment mTweetBreakDownFragment;

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
        mUserInfo = getArguments().getParcelable("userInfo");
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerTimeLine);
        mNoLists = (TextView) view.findViewById(R.id.notweets);
        mTimeLine = new ArrayList<>();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);

        if(mUserInfo != null) {
            pullTimeLineData(mUserInfo);
        } else {
            //TODO -- put error in place if there is no userinfo
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
        mAdapter = new UserTimeLineAdapter(_rxBus,new ArrayList<Tweet>());
        mRecyclerView.setAdapter(mAdapter);

        String token = getResources().getString(R.string.access_token);
        String tokenSecret = getResources().getString(R.string.access_token_secret);

        //TODO make the number of twitter responses an option! not just 10
        TwitterUserClient.getInstance(token,tokenSecret)
                .getUserTimeline(userInfo.getScreenName(),10)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Tweet>>() {

                    @Override public void onCompleted() {
                        if(BuildConfig.DEBUG){Log.d(TAG, "In onCompleted()");}

                        mCallback.showProgressBar(false);
                        mAdapter = new UserTimeLineAdapter(_rxBus,mTimeLine);

                        _rxBus.toClickObserverable()
                                .subscribe(new Action1<Object>() {
                                    @Override
                                    public void call(Object event) {

                                        //TODO MOVE THIS METHOD TO THE FRAGMENT, AND ONLY CALL BACK TO MAIN ACTIVITY???
                                        //TODO OR only if there is no userinfo, fill that shit in. otherwise dont
                                        if(isUniqueClick(1000) && event instanceof Tweet) {

                                            if(mTweetBreakDownFragment == null || !mTweetBreakDownFragment.isShowing()) {
                                                Tweet tweet = (Tweet) event;
//                                                if(BuildConfig.DEBUG) {
//                                                    Log.d(TAG,"TWEET URL:" + tweet.getEntities().getUrls().get(0).getUrl());
//                                                }
                                                TweetBreakDownFragment fragment = new TweetBreakDownFragment();
                                                Bundle bundle = new Bundle();
                                                bundle.putParcelable("tweet",tweet);
                                                fragment.setArguments(bundle);
                                                ((BaseContainerFragment)getParentFragment()).addFragment(fragment, true,"tweetbreakdown");
                                                //Hide the fab
                                                mCallback.showFab(false,"");
                                            }
                                        }

                                    }

                                });
                        mRecyclerView.setAdapter(mAdapter);

                        //TODO Make this its own subscribable that we can chain!
                        /* Check most recent user info (if it exists within timeline api response)
                        * against the user info stored in db. update db with any changed ino */
                        //And make it its own void
                        if(mTimeLine != null && mTimeLine.size() > 0) {
                            try {
                                UserInfo recentUserInfo = mTimeLine.get(0).getUser();
                                InternalDB.getInstance(getContext()).compareUserInfoAndUpdate(userInfo,recentUserInfo);

                            } catch (NullPointerException e) {
                                Log.e(TAG,"Timeline userinfo match problem: " + e);
                            }


                        }

                    }

                    @Override public void onError(Throwable e) {
                        e.printStackTrace();
                        if(BuildConfig.DEBUG){Log.d(TAG, "In onError()");}
                        mCallback.showProgressBar(false);

                        //TODO -- GET PROPER ERROR CODE!
                        Toast.makeText(getContext(), "Unable to get timeline for @" + userInfo.getScreenName(), Toast.LENGTH_SHORT).show();
                    }

                    @Override public void onNext(List<Tweet> timeline) {
                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "In onNext()");
                            Log.d(TAG,"TIMELINE SIZE: " + timeline.size());
                        }

                        for(Tweet tweet : timeline) {
                            Log.d(TAG,"timeline thing: " + tweet.getText());
                            showRecyclerView(true);
                            mTimeLine.add(tweet);
                        }
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

}

