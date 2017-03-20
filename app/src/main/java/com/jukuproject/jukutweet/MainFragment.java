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
        import com.jukuproject.jukutweet.Models.User;

        import java.util.List;

        import rx.functions.Action1;
//        import com.jukuproject.jukutweet.Adapters.UserListAdapter;
//        import com.jukuproject.jukutweet.Adapters.RxBus;
//        import com.jukuproject.jukutweet.DB.InternalDB;
//        import com.jukuproject.jukutweet.XMLModel.AudioStream;
//        import com.jukuproject.jukutweet.XMLModel.RSSList;
//        import java.util.List;
//        import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
//        import rx.functions.Action1;

public class MainFragment extends Fragment {
    FragmentInteractionListener mCallback;
    private long mLastClickTime = 0;


    public MainFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MainFragment newInstance(int sectionNumber) {
        MainFragment fragment = new MainFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
        return fragment;
    }


    private RxBus _rxBus = new RxBus();
    private RecyclerView mRecyclerView;
    UserListAdapter mAdapter;
    private TextView mNoLists;


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


    public void updateAdapter() {

        //Initialize the rsslist;
        List<User> rssLists = InternalDB.getInstance(getContext()).getFollowedUsers(getContext());

//        if(rssLists != null && rssLists.size()>0) {
//            Log.d("InternalDB","Filladapterlist TITLE: " + rssLists.get(0).getImageURI());
//        }

        if(rssLists != null && rssLists.size() > 0) {
            mAdapter = new UserListAdapter(rssLists, _rxBus, getContext());
            Log.d("InternalDB", "count: " + mAdapter.getItemCount());

            showRecyclerView(true);

            _rxBus.toClickObserverable()
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {

                            //TODO -- make this a method... OR MOVE IT TO RxBus
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();

                            if(event instanceof User) {
                                User user = (User) event;
                                mCallback.followUser(user.getName());
                            }
                        }

                    });
            _rxBus.toLongClickObserverable()
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {

                            //TODO -- make this a method... OR MOVE IT TO RxBus
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();

                            if(event instanceof User) {
                                User user = (User) event;
                                mCallback.showRemoveDialog(user.getName());
                            }

                        }

                    });
            mRecyclerView.setAdapter(mAdapter);

        } else {
            showRecyclerView(false);
        }


    }


    /** If there are no saved lists, hide recycler and show No Lists message **/
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

}

