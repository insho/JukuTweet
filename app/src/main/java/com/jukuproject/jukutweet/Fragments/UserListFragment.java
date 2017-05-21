package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.database.Cursor;
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

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.jukuproject.jukutweet.Adapters.UserListExpandableRecyclerAdapter;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.UserDetailPopupDialog;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MenuChild;
import com.jukuproject.jukutweet.Models.MenuHeader;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;
import com.jukuproject.jukutweet.TabContainers.BaseContainerFragment;

import java.util.ArrayList;
import java.util.Arrays;

import rx.functions.Action1;

public class UserListFragment extends Fragment {
    FragmentInteractionListener mCallback;

    /*Tracks elapsed time since last click of a recyclerview row. Used to
    * keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
    private RxBus _rxBus = new RxBus();
    private RecyclerView mRecyclerView;
    private UserListExpandableRecyclerAdapter mAdapter;
    private TextView mNoLists;
    private String TAG = "TEST-userlistfrag";
    ArrayList<MenuHeader> mMenuHeader;
    private int lastExpandedPosition = -1;

    public UserListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerMain);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mNoLists = (TextView) view.findViewById(R.id.nolists);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState==null) {
            updateAdapter(null);
        } else {
            mMenuHeader = savedInstanceState.getParcelableArrayList("mMenuHeader");
            lastExpandedPosition = savedInstanceState.getInt("lastExpandedPosition");
            updateAdapter(mMenuHeader);
        }

    }

    /* Pulls list of followed twitter users from db and fills the adapter with them */
    public void updateAdapter(@Nullable ArrayList<MenuHeader> menuHeaders) {

            if(menuHeaders==null) {
                mMenuHeader = new ArrayList<>();

                ColorThresholds colorThresholds = SharedPrefManager.getInstance(getContext()).getColorThresholds();
                ArrayList<String> childOptions = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.menu_userslist)));

                Cursor c  = InternalDB.getUserInterfaceInstance(getContext()).getTweetListColorBlocksCursorForUserInfoMenuHeaders(colorThresholds);
                if(c.getCount()>0) {
                    c.moveToFirst();
                    while (!c.isAfterLast()) {

                        MenuHeader menuHeader = new MenuHeader();
                        menuHeader.setHeaderTitle(c.getString(0));
                        menuHeader.setChildOptions(childOptions);
                        menuHeader.setMyList(false);
                        menuHeader.setSystemList(false);

                        UserInfo userInfo = new UserInfo(c.getString(0));
                        userInfo.setDescription(c.getString(1));

                        try {
                            if(c.getString(5)!=null) {
                                userInfo.setUserId(c.getString(5));
                            }
                            if(c.getInt(2)>=0) {
                                userInfo.setFollowerCount(c.getInt(2));
                            }
                            if(c.getInt(3)>=0) {
                                userInfo.setFriendCount(c.getInt(3));
                            }
                            userInfo.setName(c.getString(7));
                            userInfo.setProfile_image_url(c.getString(4));
                        } catch (NullPointerException e) {
                            Log.e(TAG,"getSavedUserInfo adding extra user info NullPointerException... " + e);
                        }

                        try {
                            userInfo.setProfileImageFilePath(c.getString(6));
                        } catch (NullPointerException e) {
                            Log.e(TAG,"getSavedUserInfo adding setProfileImgFilePath other NullPointerException... " + e);
                        }

                        menuHeader.setUserInfo(userInfo);


                        ColorBlockMeasurables colorBlockMeasurables = new ColorBlockMeasurables();
                        colorBlockMeasurables.setTweetCount(c.getInt(13));

                    /* Set the first available non-empty list to be automatically expanded when
                      fragment is created. This is achieved by making it the "lastexpandedposition" from the get-go */

                        menuHeader.setColorBlockMeasurables(colorBlockMeasurables);
                        menuHeader.setMenuChildren(childOptions,colorBlockMeasurables,userInfo);
                        mMenuHeader.add(menuHeader);

                        c.moveToNext();
                    }
                }
                c.close();
            }

        //Create UserListAdapter and attach rxBus click listeners to it
        if(mMenuHeader != null && mMenuHeader.size() > 0) {

            mAdapter = new UserListExpandableRecyclerAdapter(getContext(),mMenuHeader,_rxBus);
            showRecyclerView(true);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setExpandCollapseListener(new ExpandableRecyclerAdapter.ExpandCollapseListener() {
                @Override
                public void onParentExpanded(int parentPosition) {

                    if (lastExpandedPosition != -1
                            && parentPosition != lastExpandedPosition
                            ) {
                        mAdapter.collapseParent(lastExpandedPosition);
                    }
                    lastExpandedPosition = parentPosition;
                }

                @Override
                public void onParentCollapsed(int parentPosition) {}
            });

            _rxBus.toClickObserverable()
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {

                            //If the child option is clicked
                            if(isUniqueClick(1000) && event instanceof MenuChild) {
                                MenuChild menuChild = (MenuChild) event;

                    switch (menuChild.getChildTitle()) {
                        case "Timeline":

                            if(!mCallback.isOnline()) {
                                Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();
                            } else {
                                showUserTimelineFragment(menuChild.getUserInfo());
                            }
                            break;
                        case "Saved Tweets":

                            if(menuChild.getColorBlockMeasurables().getTweetCount()>0) {
                                mCallback.showActionBarBackButton(true,menuChild.getUserInfo().getDisplayScreenName(),0);
                                mCallback.showFab(false);

                                TweetListSingleUserFragment tweetListSingleUserFragment = TweetListSingleUserFragment.newInstance(menuChild.getUserInfo());
                                ((BaseContainerFragment)getParentFragment()).replaceFragment(tweetListSingleUserFragment,true,"savedTweetsAllFragmentIndividual");
                            }
                            break;
                        default:
                            break;
                    }
                            }

                        }

                    });
            _rxBus.toLongClickObserverable()
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {
                            if(isUniqueClick(1000) && event instanceof UserInfo) {
                                UserInfo userInfo = (UserInfo) event;
                                if(userInfo.getUserId()==null) {
                                    mCallback.showRemoveUserDialog(userInfo);
                                } else {
                                    mCallback.showFab(false);
                                    UserDetailPopupDialog.newInstance(userInfo).show(getFragmentManager(),"userDetailPopup");
                                }
                            }
                        }

                    });


        } else {
            /* Hide recycler view and show "no users found" message */
            showRecyclerView(false);
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
     * Show user timeline fragment, and update the main activity tab pager title strip
     * to show "timeline"
     * @param userInfo UserInfo object for user whose timeline will be shown
     */
    public void showUserTimelineFragment(UserInfo userInfo) {
        mCallback.showProgressBar(true);
        mCallback.showActionBarBackButton(true,userInfo.getDisplayScreenName(),0);
        mCallback.updateTabs(new String[]{"Timeline","Tweet Lists","Word Lists","Search"});
        ((BaseContainerFragment)getParentFragment()).replaceFragment(UserTimeLineFragment.newInstance(userInfo), true,"timeline");
        mCallback.showFab(false);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("lastExpandedPosition", lastExpandedPosition);
        outState.putParcelableArrayList("mMenuHeader", mMenuHeader);
    }


}

