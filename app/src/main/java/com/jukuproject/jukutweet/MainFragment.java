package com.jukuproject.jukutweet;

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

        import com.jukuproject.jukutweet.Adapters.UserListAdapter;
        import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
        import com.jukuproject.jukutweet.Interfaces.RxBus;
        import com.jukuproject.jukutweet.Models.UserInfo;

        import java.util.List;
        import rx.functions.Action1;


public class MainFragment extends Fragment {
    FragmentInteractionListener mCallback;

    /*Tracks elapsed time since last click of a recyclerview row. Used to
    * keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
    private RxBus _rxBus = new RxBus();
    private RecyclerView mRecyclerView;
    UserListAdapter mAdapter;
    private TextView mNoLists;

    public MainFragment() {}

    /**
     * Returns a new instance of MainFragment
     */
    public static MainFragment newInstance() {
//        MainFragment fragment = new MainFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
        return new MainFragment();
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
        List<UserInfo> savedUserInfo = InternalDB.getInstance(getContext()).getSavedUserInfo();

        //Create UserListAdapter and attach rxBus click listeners to it
        if(savedUserInfo != null && savedUserInfo.size() > 0) {
            mAdapter = new UserListAdapter(savedUserInfo, _rxBus);

            showRecyclerView(true);

            _rxBus.toClickObserverable()
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {

//                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
//                                return;
//                            }
//                            mLastClickTime = SystemClock.elapsedRealtime();
//
//                            if(event instanceof UserInfo) {
//                                UserInfo user = (UserInfo) event;
//                                mCallback.getUserInfo(user.getName());
//                            }

                            //TODO MOVE THIS METHOD TO THE FRAGMENT, AND ONLY CALL BACK TO MAIN ACTIVITY???
                            //TODO OR only if there is no userinfo, fill that shit in. otherwise dont
                            if(isUniqueClick(1000) && event instanceof UserInfo) {
                                UserInfo userInfo = (UserInfo) event;
//                                mCallback.getUserInfo(userInfo.getScreenName());
                                Log.d("TEST","MAIN FRAG SHOW TIMELINE...");

//                                Fragment timeLineFragment  = TimeLineFragment.newInstance(userInfo);
//                                getChildFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.container, timeLineFragment).commit();

//                                mCallback.showTimeLine(userInfo);

                                TimeLineFragment fragment = new TimeLineFragment();
                                // if U need to pass some data
                                Bundle bundle = new Bundle();

//                                bundle.putString("title", "LEARN DETAIL FRAG");
                                bundle.putParcelable("userInfo",userInfo);
                                fragment.setArguments(bundle);
                                ((BaseContainerFragment)getParentFragment()).replaceFragment(fragment, true);


                            }

                        }

                    });
            _rxBus.toLongClickObserverable()
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {

//                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
//                                return;
//                            }
//                            mLastClickTime = SystemClock.elapsedRealtime();
//                            if(event instanceof UserInfo) {
//                                UserInfo user = (UserInfo) event;
//                                mCallback.showRemoveUserDialog(user.getName());
//                            }
                            if(isUniqueClick(1000) && event instanceof UserInfo) {
                                UserInfo userInfo = (UserInfo) event;
                                mCallback.showRemoveUserDialog(userInfo.getScreenName());
                            }
//

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

