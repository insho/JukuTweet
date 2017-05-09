package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.jukuproject.jukutweet.Adapters.TweetListExpandableAdapter;
import com.jukuproject.jukutweet.Adapters.UserListExpandableRecyclerAdapter;
import com.jukuproject.jukutweet.BuildConfig;
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
import com.jukuproject.jukutweet.TwitterUserClient;

import java.util.ArrayList;
import java.util.Arrays;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.jukuproject.jukutweet.Fragments.TweetListSingleUserFragment.getExpandableAdapterColorBlockBasicWidths;


public class UserListFragment extends Fragment {
    FragmentInteractionListener mCallback;

    /*Tracks elapsed time since last click of a recyclerview row. Used to
    * keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
    private RxBus _rxBus = new RxBus();
    private RecyclerView mRecyclerView;
//    private ExpandableListView expListView;
    private UserListExpandableRecyclerAdapter mAdapter;
//    private UserListExpandableAdapter mAdapter;
    private TextView mNoLists;
    private Subscription userInfoSubscription;
    private String TAG = "TEST-userlistfrag";
    ArrayList<MenuHeader> mMenuHeader;
    private int lastExpandedPosition = -1;

    public UserListFragment() {}

    /**
     * Returns a new instance of UserListFragment
     */
    public static UserListFragment newInstance() {
        return new UserListFragment();
    }





    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
//        expListView = (ExpandableListView) view.findViewById(R.id.expListView);
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

//    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//        updateAdapter();
//    }


    /**
     * Pulls list of followed twitter users from db and fills the adapter with them
     */
    public void updateAdapter(@Nullable ArrayList<MenuHeader> menuHeaders) {

            if(menuHeaders==null) {
                mMenuHeader = new ArrayList<>();

//            ArrayList<String> availableFavoritesStars = sharedPrefManager.getActiveTweetFavoriteStars();
                ColorThresholds colorThresholds = SharedPrefManager.getInstance(getContext()).getColorThresholds();
                ArrayList<String> childOptions = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.menu_userslist)));

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
                        colorBlockMeasurables.setGreyCount(c.getInt(9));
                        colorBlockMeasurables.setRedCount(c.getInt(10));
                        colorBlockMeasurables.setYellowCount(c.getInt(11));
                        colorBlockMeasurables.setGreenCount(c.getInt(12));
                        colorBlockMeasurables.setEmptyCount(0);
                        colorBlockMeasurables.setTweetCount(c.getInt(13));

                    /* Set the first available non-empty list to be automatically expanded when
                      fragment is created. This is achieved by making it the "lastexpandedposition" from the get-go */

                        colorBlockMeasurables.setGreyMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getGreyCount())));
                        colorBlockMeasurables.setRedMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getRedCount())));
                        colorBlockMeasurables.setYellowMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getYellowCount())));
                        colorBlockMeasurables.setGreenMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getGreenCount())));
                        colorBlockMeasurables.setEmptyMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getEmptyCount())));

                        menuHeader.setColorBlockMeasurables(colorBlockMeasurables);
                        menuHeader.setMenuChildren(childOptions,colorBlockMeasurables,userInfo);
                        mMenuHeader.add(menuHeader);

                        c.moveToNext();
                    }
                }
                c.close();
            }


        //Pull list of followed twitter users from the database
//        List<MenuHeader> savedUserInfo = InternalDB.getUserInterfaceInstance(getContext()).getSavedUserInfo();

        //Create UserfListAdapter and attach rxBus click listeners to it
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

//                    int idx = expListView.getFirstVisiblePosition() + expListView.getFirstVisiblePosition() * groupPosition;
//                    expListView.setSelectionFromTop(idx, idx);


//                    Recipe expandedRecipe = recipes.get(position);
                    // ...
                }

                @Override
                public void onParentCollapsed(int parentPosition) {
//                    Recipe collapsedRecipe = recipes.get(position);
                    // ...
                }
            });
//            expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
//                @Override
//                public boolean onGroupClick(ExpandableListView parent, View v,
//                                            int groupPosition, long id) {
//
//                    Log.d(TAG,"GROUP CLIIIICK!");
//                    //If the list being clicked on is empty, show (or hide) the "(empty)" header label
////                    if(mMenuHeader.get(groupPosition).getColorBlockMeasurables().getTotalCount() == 0) {
////                        TextView lblListHeaderCount = (TextView) v.findViewById(R.id.lblListHeaderCount);
////                        if (lblListHeaderCount.getVisibility() == TextView.VISIBLE && lblListHeaderCount.getText().toString().length() > 0) {
////                            lblListHeaderCount.setVisibility(TextView.GONE);
////                        } else {
////                            lblListHeaderCount.setVisibility(TextView.VISIBLE);
////                            lblListHeaderCount.setText(getString(R.string.empty_parenthesis));
////                        }
////                    } else
//                    if (!mMenuHeader.get(groupPosition).isExpanded()) {
//
//                        if(lastExpandedPosition != groupPosition) {
//                            expListView.collapseGroup(lastExpandedPosition);
//                        }
//                        expandTheListViewAtPosition(groupPosition);
//                    }  else {
//                        expListView.collapseGroup(groupPosition);
//                        mMenuHeader.get(groupPosition).setExpanded(false);
//                    }
//                    return true;   //"True" makes it impossible to collapse the list
//                }
//            });
//
//            expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
//
//                @Override
//                public void onGroupExpand(int groupPosition) {
//
//                    if (lastExpandedPosition != -1
//                            && groupPosition != lastExpandedPosition
//                            && mMenuHeader.get(groupPosition).getColorBlockMeasurables().getTotalCount()> 0
//                            ) {
//                        expListView.collapseGroup(lastExpandedPosition);
//                        lastExpandedPosition = groupPosition;
//                    }
//
//                    int idx = expListView.getFirstVisiblePosition() + expListView.getFirstVisiblePosition() * groupPosition;
//                    expListView.setSelectionFromTop(idx, idx);
//                }
//            });
//
//
//            expListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//                @Override
//                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                    if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
//                        int groupPosition = ExpandableListView.getPackedPositionGroup(id);
//                       //
//                                if(mMenuHeader.get(groupPosition).getUserInfo().getUserId()==null) {
//                                    mCallback.showRemoveUserDialog(mMenuHeader.get(groupPosition).getUserInfo());
//                                } else {
//                                    mCallback.showFab(false);
//                                    UserDetailPopupDialog.newInstance(mMenuHeader.get(groupPosition).getUserInfo()).show(getFragmentManager(),"userDetailPopup");
//                                }
//
//                    }
//
//                    return false;
//                }
//            });
//
//
//            expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//
//                @Override
//                public boolean onChildClick(ExpandableListView parent, View v,
//                                            int groupPosition, int childPosition, long id) {
//                    String childOption = mMenuHeader.get(groupPosition).getChildOptions().get(childPosition); //This is the text in the child that the user clicked
//
//                /* User shouldn't be able to click on an empty list */
//                    if(mMenuHeader.get(groupPosition).getColorBlockMeasurables() == null
//                            || mMenuHeader.get(groupPosition).getColorBlockMeasurables() == null
//                            || mMenuHeader.get(groupPosition).getColorBlockMeasurables().getTotalCount()==0) {
//                        Toast.makeText(getContext(), "List is empty", Toast.LENGTH_SHORT).show();
//                        return false;
//                    }
//
//
//                    switch (childOption) {
//                        case "View Timeline":
////                            TweetListBrowseFragment fragment = TweetListBrowseFragment.newInstance(mMenuHeader.get(groupPosition).getUserInfo());
////                            ((BaseContainerFragment)getParentFragment()).replaceFragment(fragment, true,"savedtweetsbrowse");
//
//                            if(mMenuHeader.get(groupPosition).getUserInfo()!=null && mMenuHeader.get(groupPosition).getUserInfo().getUserId()==null) {
//                                if(!mCallback.isOnline()) {
//                                    Toast.makeText(getContext(), "Connect to internet to access information/timeline for " + mMenuHeader.get(groupPosition).getUserInfo().getDisplayScreenName(), Toast.LENGTH_SHORT).show();
//                                } else if(mMenuHeader.get(groupPosition).getUserInfo()!=null && mMenuHeader.get(groupPosition).getUserInfo().getScreenName()!=null){
//                                    updateUserInfoBeforeShowingUserTimeline(mMenuHeader.get(groupPosition).getUserInfo().getScreenName());
//                                } else {
//                                    /* This would be considered an error.*/
//                                    Log.e(TAG,"Userinfo has null screenname and null user id!?");
//                                }
//
//                            } else {
//                                Log.d(TAG,"Showing timeline off of button click");
//                                showUserTimelineFragment(mMenuHeader.get(groupPosition).getUserInfo());
//                            }
//
//
//                            break;
//
//                        case "Saved Tweets":
//
////                            try {
////                                TweetListSingleUserFragment tweetListSingleUserFragment = TweetListSingleUserFragment.newInstance(userInfo);
////                                ((BaseContainerFragment)findFragmentByPosition(1)).replaceFragment(tweetListSingleUserFragment,true,"savedTweetsAllFragmentIndividual");
////                            } catch (Exception e) {
////                                Log.e("TEST","showSavedTweetsTabForIndividualUser failed");
////                            }
//
//
//                            mCallback.showActionBarBackButton(true,mMenuHeader.get(groupPosition).getUserInfo().getDisplayScreenName());
//                            mCallback.showFab(false);
////                            mCallback.showSavedTweetsTabForIndividualUser(userInfo);
////        mCallback.updateTabs(new String[]{"Timeline","Saved Tweets"});
////                            mCallback.updateTabs(new String[]{"Timeline","Saved Tweets","Word Lists","Search"});
////                            ((BaseContainerFragment)getParentFragment()).replaceFragment(UserTimeLineFragment.newInstance(mMenuHeader.get(groupPosition).getUserInfo()), true,"timeline");
//
//                            TweetListSingleUserFragment tweetListSingleUserFragment = TweetListSingleUserFragment.newInstance(mMenuHeader.get(groupPosition).getUserInfo());
//                            ((BaseContainerFragment)getParentFragment()).replaceFragment(tweetListSingleUserFragment,true,"savedTweetsAllFragmentIndividual");
//
////                            if(getFragmentManager().findFragmentByTag("quizmenu") == null || !getFragmentManager().findFragmentByTag("quizmenu").isAdded()) {
////                                QuizMenuDialog.newSingleUserInstance("flashcards"
////                                        ,1
////                                        ,lastExpandedPosition
////                                        ,mUserInfo
////                                        ,mMenuHeader.get(groupPosition).getColorBlockMeasurables()
////                                        ,getdimenscore(.5f)).show(getActivity().getSupportFragmentManager()
////                                        ,"dialogQuizMenu");
////                                mCallback.showFab(false,"");
////                            }
//                            break;
//
//
//                        default:
//
//                            break;
//
//                    }
//                    return false;
//                }
//            });



            _rxBus.toClickObserverable()
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {

                            //If the child option is clicked
                            if(isUniqueClick(1000) && event instanceof MenuChild) {
                                MenuChild menuChild = (MenuChild) event;

                    switch (menuChild.getChildTitle()) {
                        case "Timeline":

                            if(menuChild.getUserInfo()!=null && menuChild.getUserInfo().getUserId()==null) {
                                if(!mCallback.isOnline()) {
                                    Toast.makeText(getContext(), "Connect to internet to access information/timeline for " + menuChild.getUserInfo().getDisplayScreenName(), Toast.LENGTH_SHORT).show();
                                } else if(menuChild.getUserInfo()!=null && menuChild.getUserInfo().getScreenName()!=null){
                                    updateUserInfoBeforeShowingUserTimeline(menuChild.getUserInfo().getScreenName());
                                } else {
                                    /* This would be considered an error.*/
                                    Log.e(TAG,"Userinfo has null screenname and null user id!?");
                                }

                            } else {
                                Log.d(TAG,"Showing timeline off of button click");
                                showUserTimelineFragment(menuChild.getUserInfo());
                            }


                            break;

                        case "Saved Tweets":

//                            try {
//                                TweetListSingleUserFragment tweetListSingleUserFragment = TweetListSingleUserFragment.newInstance(userInfo);
//                                ((BaseContainerFragment)findFragmentByPosition(1)).replaceFragment(tweetListSingleUserFragment,true,"savedTweetsAllFragmentIndividual");
//                            } catch (Exception e) {
//                                Log.e("TEST","showSavedTweetsTabForIndividualUser failed");
//                            }

                            if(menuChild.getColorBlockMeasurables().getTweetCount()>0) {
                                mCallback.showActionBarBackButton(true,menuChild.getUserInfo().getDisplayScreenName());
                                mCallback.showFab(false);
//                            mCallback.showSavedTweetsTabForIndividualUser(userInfo);
//        mCallback.updateTabs(new String[]{"Timeline","Saved Tweets"});
//                            mCallback.updateTabs(new String[]{"Timeline","Saved Tweets","Word Lists","Search"});
//                            ((BaseContainerFragment)getParentFragment()).replaceFragment(UserTimeLineFragment.newInstance(mMenuHeader.get(groupPosition).getUserInfo()), true,"timeline");

                                TweetListSingleUserFragment tweetListSingleUserFragment = TweetListSingleUserFragment.newInstance(menuChild.getUserInfo());
                                ((BaseContainerFragment)getParentFragment()).replaceFragment(tweetListSingleUserFragment,true,"savedTweetsAllFragmentIndividual");

//                            if(getFragmentManager().findFragmentByTag("quizmenu") == null || !getFragmentManager().findFragmentByTag("quizmenu").isAdded()) {
//                                QuizMenuDialog.newSingleUserInstance("flashcards"
//                                        ,1
//                                        ,lastExpandedPosition
//                                        ,mUserInfo
//                                        ,mMenuHeader.get(groupPosition).getColorBlockMeasurables()
//                                        ,getdimenscore(.5f)).show(getActivity().getSupportFragmentManager()
//                                        ,"dialogQuizMenu");
//                                mCallback.showFab(false,"");
//                            }

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


    public void updateUserInfoBeforeShowingUserTimeline(final String screenName) {

        String token = getResources().getString(R.string.access_token);
        String tokenSecret = getResources().getString(R.string.access_token_secret);


        userInfoSubscription = TwitterUserClient.getInstance(token,tokenSecret)
                .getUserInfo(screenName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserInfo>() {
                    UserInfo userInfoInstance;

                    @Override public void onCompleted() {
                        if(BuildConfig.DEBUG){
                            Log.d(TAG, "In onCompleted()");}

                            /* If the user exists and a UserInfo object has been populated,
                            * save it to the database and update the UserInfoFragment adapter */
                        if(userInfoInstance != null) {
                            InternalDB.getUserInterfaceInstance(getContext()).updateUserInfo(userInfoInstance);
                            showUserTimelineFragment(userInfoInstance);
                        } else {
                            Toast.makeText(getContext(), "Unable to access timeline for " + screenName, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override public void onError(Throwable e) {
                        e.printStackTrace();
                        if(BuildConfig.DEBUG){Log.d(TAG, "In onError()");}
                        Toast.makeText(getContext(), "Unable to connect to Twitter API", Toast.LENGTH_SHORT).show();
                        Log.d(TAG,"ERROR CAUSE: " + e.getCause());
                    }

                    @Override public void onNext(UserInfo userInfo) {
                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "In onNext()");
                            Log.d(TAG, "userInfo: " + userInfo.getUserId() + ", " + userInfo.getDescription());
                        }
                        if(userInfoInstance == null) {
                            userInfoInstance = userInfo;
                        }


                    }
                });

    }


    public void showUserTimelineFragment(UserInfo userInfo) {
        mCallback.showProgressBar(true);
        mCallback.showActionBarBackButton(true,userInfo.getDisplayScreenName());
//        mCallback.showSavedTweetsTabForIndividualUser(userInfo);
//        mCallback.updateTabs(new String[]{"Timeline","Saved Tweets"});
        mCallback.updateTabs(new String[]{"Timeline","Saved Tweets","Word Lists","Search"});
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
    public void onPause() {

        if(userInfoSubscription!=null) {
            userInfoSubscription.unsubscribe();
        }
        super.onPause();

    }

    @Override
    public void onDestroy() {
        if(userInfoSubscription!=null) {
            userInfoSubscription.unsubscribe();
        }

        super.onDestroy();
    }

    /**
     * Maximum width of the color bars in the MenuExpandableListAdapter. Right that vlue is set
     * to half of the screenwidth
     * @return maximum width in pixels of colored bars
     *
     * @see TweetListExpandableAdapter
     */
    private int getdimenscore(float multiplier) {

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        return Math.round((float)metrics.widthPixels*(float).5);
        if(metrics.heightPixels>metrics.widthPixels) {
            return Math.round((float)metrics.widthPixels*multiplier);
        } else {
            return Math.round((float)metrics.heightPixels*multiplier);
        }
    }


//    public void expandTheListViewAtPosition(int position) {
//        mRecyclerView.expandGroup(position);
//        expListView.setSelectedGroup(position);
//        mMenuHeader.get(position).setExpanded(true);
//        lastExpandedPosition = position;
//
//    };




    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("lastExpandedPosition", lastExpandedPosition);
//        outState.putParcelable("mUserInfo", mUserInfo);
        outState.putParcelableArrayList("mMenuHeader", mMenuHeader);
    }


}

