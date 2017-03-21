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

import com.jukuproject.jukutweet.Adapters.UserListAdapter;
import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.InternalDB;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.R;

import java.util.List;
import rx.functions.Action1;


public class QuizAllFragment extends Fragment {
    FragmentInteractionListener mCallback;

    /*Tracks elapsed time since last click of a recyclerview row. Used to
    * keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
//    private RxBus _rxBus = new RxBus();
//    private RecyclerView mRecyclerView;
//    UserListAdapter mAdapter;
//    private TextView mNoLists;

    public QuizAllFragment() {}

    /**
     * Returns a new instance of UserListFragment
     */
    public static MyListFragment newInstance() {
//        UserListFragment fragment = new UserListFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
        return new MyListFragment();
    }





    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.testlayout, container, false);
        TextView test = (TextView) view.findViewById(R.id.textView);
        test.setText("Quiz All FRAGMENT");
//        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerMain);
//        mNoLists = (TextView) view.findViewById(R.id.nolists);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
//        mRecyclerView.setLayoutManager(layoutManager);
//        updateAdapter();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
//        updateAdapter();
    }





//    /**
//     * Toggles between showing recycler (if there are followed users in the database)
//     * and hiding the recycler while showing the "no users found" message if there are not
//     * @param show bool True to show recycler, False to hide it
//     */
//    private void showRecyclerView(boolean show) {
//        if(show) {
//            mRecyclerView.setVisibility(View.VISIBLE);
//            mNoLists.setVisibility(View.GONE);
//        } else {
//            mRecyclerView.setVisibility(View.GONE);
//            mNoLists.setVisibility(View.VISIBLE);
//        }
//    }

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
//    public boolean isUniqueClick(int elapsedMilliSeconds) {
//        if(SystemClock.elapsedRealtime() - mLastClickTime > elapsedMilliSeconds) {
//            mLastClickTime = SystemClock.elapsedRealtime();
//            return true;
//        } else {
//            return false;
//        }
//    }

}

