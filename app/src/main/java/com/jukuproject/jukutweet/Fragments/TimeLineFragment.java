package com.jukuproject.jukutweet.Fragments;

import android.content.ContentValues;
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
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.InternalDB;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.TwitterUserClient;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.jukuproject.jukutweet.InternalDB.TMAIN_COL0;


public class TimeLineFragment extends Fragment {
    FragmentInteractionListener mCallback;

    /*Tracks elapsed time since last click of a recyclerview row. Used to
    * keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
    private RxBus _rxBus = new RxBus();
    private RecyclerView mRecyclerView;
    private UserTimeLineAdapter mAdapter;
    private TextView mNoLists;
    private UserInfo mUserInfo;

//    private TextView mHeaderScreenName;
//    private TextView mHeaderFollowers;
//    private TextView mHeaderFriends;

    private List<Tweet> mTimeLine;

    private static final String TAG = "TEST-TimeLineFrag";

    public TimeLineFragment() {}

    /**
     * Returns a new instance of UserListFragment
     */
    public static TimeLineFragment newInstance(UserInfo userInfo) {
        TimeLineFragment fragment = new TimeLineFragment();
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
        //Fill header info
//        mHeaderScreenName = (TextView) view.findViewById(R.id.name);
//        mHeaderFollowers = (TextView) view.findViewById(R.id.followers);
//        mHeaderFriends = (TextView) view.findViewById(R.id.friends);
        mTimeLine = new ArrayList<>();
        return view;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if(mUserInfo != null) {
//            mHeaderScreenName.setText(mUserInfo.getDisplayName());
//            mHeaderFollowers.setText("Followers: " + mUserInfo.getFollowerCount());
//            mHeaderFriends.setText("Friends: " + mUserInfo.getFriendCount());
//            pullTimeLineData(mUserInfo.getScreenName());
//        } else {
//            //TODO -- put error in place if there is no userinfo
//            Log.d(TAG,"ON RESUME!!!");
//        }
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);

        if(mUserInfo != null) {
//            mHeaderScreenName.setText(mUserInfo.getDisplayName());
//            mHeaderFollowers.setText("Followers: " + mUserInfo.getFollowerCountString());
//            mHeaderFriends.setText("Friends: " + mUserInfo.getFriendCountString());
            pullTimeLineData(mUserInfo);
        } else {
            //TODO -- put error in place if there is no userinfo
        }

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        updateAdapter();
    }


    public void pullTimeLineData(final UserInfo userInfo){

        mCallback.showProgressBar(true);
        mAdapter = new UserTimeLineAdapter(new ArrayList<Tweet>());
        mRecyclerView.setAdapter(mAdapter);

        String token = getResources().getString(R.string.access_token);
        String tokenSecret = getResources().getString(R.string.access_token_secret);

        //TODO fix it so the progress bar shows up
        //TODO make the number of twitter responses an option! not just 10
        //TODO AND BASE EVERYTING ON THE FUCKING TWITTER ID!!! LIKE IN THE DATABASE!!! SAVE THE TWITTER ID!!
        TwitterUserClient.getInstance(token,tokenSecret)
                .getUserTimeline(userInfo.getScreenName(),10)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Tweet>>() {

                    @Override public void onCompleted() {
                        if(BuildConfig.DEBUG){Log.d(TAG, "In onCompleted()");}

                        mCallback.showProgressBar(false);
                        mAdapter = new UserTimeLineAdapter(mTimeLine);
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
//                        mAdapter.notifyItemInserted(mTimeLine.size() - 1);
                    }
                });

    }

    //TODO replace the usage of this with an actual update, instead of a full repull?
    /**
     * Pulls list of followed twitter users from db and fills the adapter with them
     */
    public void updateAdapter() {

//        //Create UserListAdapter and attach rxBus click listeners to it
//        if(mUserInfo != null) {
//            mAdapter = new UserTimeLineAdapter(mTimeLine);
//
//            showRecyclerView(true);
//
//            _rxBus.toClickObserverable()
//                    .subscribe(new Action1<Object>() {
//                        @Override
//                        public void call(Object event) {
//
////                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
////                                return;
////                            }
////                            mLastClickTime = SystemClock.elapsedRealtime();
////
////                            if(event instanceof UserInfo) {
////                                UserInfo user = (UserInfo) event;
////                                mCallback.getUserInfo(user.getName());
////                            }
//
//                            if(isUniqueClick(1000) && event instanceof UserInfo) {
//                                UserInfo userInfo = (UserInfo) event;
//                                mCallback.getUserInfo(userInfo.getScreenName());
//                            }
//
//                        }
//
//                    });
//            _rxBus.toLongClickObserverable()
//                    .subscribe(new Action1<Object>() {
//                        @Override
//                        public void call(Object event) {
//
////                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
////                                return;
////                            }
////                            mLastClickTime = SystemClock.elapsedRealtime();
////                            if(event instanceof UserInfo) {
////                                UserInfo user = (UserInfo) event;
////                                mCallback.showRemoveUserDialog(user.getName());
////                            }
//                            if(isUniqueClick(1000) && event instanceof UserInfo) {
//                                UserInfo userInfo = (UserInfo) event;
//                                mCallback.showRemoveUserDialog(userInfo.getScreenName());
//                            }
////
//
//                        }
//
//                    });
//            mRecyclerView.setAdapter(mAdapter);
//
//        } else {
//            /* Hide recycler view and show "no users found" message */
//            showRecyclerView(false);
//        }


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

