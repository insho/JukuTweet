package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Adapters.UserListAdapter;
import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.R;

import java.util.List;

import rx.functions.Action1;


public class UserListFragment extends Fragment {
    FragmentInteractionListener mCallback;

    /*Tracks elapsed time since last click of a recyclerview row. Used to
    * keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
    private RxBus _rxBus = new RxBus();
    private RecyclerView mRecyclerView;
    UserListAdapter mAdapter;
    private TextView mNoLists;

    public UserListFragment() {}

    /**
     * Returns a new instance of UserListFragment
     */
    public static UserListFragment newInstance() {
//        UserListFragment fragment = new UserListFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
        return new UserListFragment();
    }





    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerMain);
        mNoLists = (TextView) view.findViewById(R.id.nolists);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);


        updateAdapter();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        updateAdapter();
    }


    //TODO replace the usage of this with an actual update, instead of a full repull?
    /**
     * Pulls list of followed twitter users from db and fills the adapter with them
     */
    public void updateAdapter() {



        //Pull list of followed twitter users from the database
        List<UserInfo> savedUserInfo = InternalDB.getUserInterfaceInstance(getContext()).getSavedUserInfo();

        //Create UserListAdapter and attach rxBus click listeners to it
        if(savedUserInfo != null && savedUserInfo.size() > 0) {
            mAdapter = new UserListAdapter(getContext(),savedUserInfo, _rxBus);

            showRecyclerView(true);

            _rxBus.toClickObserverable()
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {

                            //TODO MOVE THIS METHOD TO THE FRAGMENT, AND ONLY CALL BACK TO MAIN ACTIVITY???
                            //TODO OR only if there is no userinfo, fill that shit in. otherwise dont
                            if(isUniqueClick(1000) && event instanceof UserInfo) {

                                if(mCallback.isOnline()) {
                                    mCallback.showProgressBar(true);
                                    UserInfo userInfo = (UserInfo) event;
                                    UserTimeLineFragment fragment = new UserTimeLineFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable("userInfo",userInfo);
                                    fragment.setArguments(bundle);
                                    ((BaseContainerFragment)getParentFragment()).replaceFragment(fragment, true,"timeline");
                                    mCallback.showActionBarBackButton(true,userInfo.getDisplayScreenName());
                                    mCallback.showFab(false,"");
//                                    mCallback.changePagerTitle(0,"Timeline");
                                    mCallback.showSavedTweetsTabForIndividualUser(userInfo);
                                    mCallback.updateTabs(new String[]{"Timeline","Saved Tweets"});


                                } else {
                                    Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
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
                                mCallback.showRemoveUserDialog(userInfo.getScreenName());
                            }

                        }

                    });
            mRecyclerView.setAdapter(mAdapter);

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

