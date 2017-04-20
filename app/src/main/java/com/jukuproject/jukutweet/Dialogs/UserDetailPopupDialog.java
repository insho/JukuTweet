package com.jukuproject.jukutweet.Dialogs;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Adapters.UserListAdapter;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
import com.jukuproject.jukutweet.Interfaces.DialogRemoveUserInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.UserFollowersListContainer;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.UserProfileBanner;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.TwitterUserClient;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by Joe on 11/21/2015.
 */

public class UserDetailPopupDialog extends DialogFragment implements View.OnTouchListener, DialogRemoveUserInteractionListener {

    String TAG = "TEST-userdetailpop";
    private DialogInteractionListener mCallback;
    //    private Context mContext;
//    private View mAnchorView;
//    private RxBus mRxBusTweetBreak= new RxBus();
    private RecyclerView mRecyclerView;

    private SmoothProgressBar progressBar;
    private View divider;


    private boolean showFriendsSelected;
    private UserInfo mUserInfo;
    private TextView btnRemoveUser;
    private TextView btnShowFriendsToggle;
    private TextView btnShowFollowersToggle;
    private TextView txtNoUsers;
//    private TextView txtFollowersCount;
//    private ImageView imgBanner;
    private ImageView imgProfile;
    private TextView txtDisplayName;
    private TextView txtScreenName;
    private TextView txtUrl;

    private View baseLayout;
//    private View popupView;
    private ArrayList<UserInfo> mDataSet;
    private int previousFingerPosition = 0;
    private int baseLayoutPosition = 0;
    private int defaultViewHeight;
    private boolean isClosing = false;
    private boolean isScrollingUp = false;
    private boolean isScrollingDown = false;
    private UserListAdapter mAdapter;
    private ScrollView mScrollView;
    private ImageView imgBanner;
    /* keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
    private String mCursorString = "-1";

    private LinearLayoutManager mLayoutManager;


    private RxBus _rxBus = new RxBus();

    public UserDetailPopupDialog() {}

    public static UserDetailPopupDialog newInstance(UserInfo userInfo) {
        UserDetailPopupDialog fragment = new UserDetailPopupDialog();
        Bundle args = new Bundle();
        args.putParcelable("mUserInfo", userInfo);
        fragment.setArguments(args);
        return  fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        setStyle(DialogFragment.STYLE_NO_FRAME, getTheme());
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_NoActionBar_TranslucentDecor);
//        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateDialog(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_userdetailpopup_banner, null);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.userInfoRecycler);
        baseLayout = view.findViewById(R.id.popuptab_layout);
        progressBar = (SmoothProgressBar) view.findViewById(R.id.progressbar);
        divider = (View) view.findViewById(R.id.dividerview);
        mScrollView = (ScrollView) view.findViewById(R.id.scrollView);

        btnShowFriendsToggle = (TextView) view.findViewById(R.id.txtShowFollowingToggle);
        btnShowFollowersToggle  = (TextView) view.findViewById(R.id.txtShowFollowersToggle);
        imgBanner = (ImageView) view.findViewById(R.id.imgBanner);
        imgProfile = (ImageView) view.findViewById(R.id.imgUser);
//        txtFollowingCount = (TextView) view.findViewById(R.id.txtFollowingCount);
        txtNoUsers = (TextView) view.findViewById(R.id.txtNoUsers);

        txtDisplayName = (TextView) view.findViewById(R.id.txtName);
        txtScreenName = (TextView) view.findViewById(R.id.txtScreenName);
        btnRemoveUser = (TextView) view.findViewById(R.id.txtRemoveUserButton);
        txtUrl = (TextView) view.findViewById(R.id.txtUrl);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        baseLayout.setOnTouchListener(this);

        if(savedInstanceState == null) {
            //TODO remove
            showFriendsSelected = true;
            mUserInfo = getArguments().getParcelable("mUserInfo");
            mDataSet = new ArrayList<>();

        } else {
            mUserInfo = savedInstanceState.getParcelable("mUserInfo");
            showFriendsSelected = savedInstanceState.getBoolean("showFriendsSelected");
            mDataSet = savedInstanceState.getParcelableArrayList("mDataSet");
        }

        btnRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getContext(), "REMOVE USER", Toast.LENGTH_SHORT).show();
                        //mCallback.showRemoveUserDialog(userInfo.getScreenName());
                if(getFragmentManager().findFragmentByTag("dialogRemove") == null || !getFragmentManager().findFragmentByTag("dialogRemove").isAdded()) {
                    RemoveUserDialog.newInstance(mUserInfo).show(getFragmentManager(),"dialogRemove");
                }

            }
        });

        setButtonActive(btnShowFriendsToggle,true);
        setButtonActive(btnShowFollowersToggle,false);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);


//        final RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                if (mLayoutManager == null || mDataSet == null || mDataSet.size()==0) {
//
//                    return;
//                } else {
//                    if(mLayoutManager.findLastCompletelyVisibleItemPosition()==mDataSet.size()-1) {
//                        Toast.makeText(getContext(), "Pull more shit", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        };
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            mRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (mLayoutManager == null || mDataSet == null || mDataSet.size()==0) {

                        return;
                    } else {
                        if(mDataSet.size()>0 && mLayoutManager.findLastCompletelyVisibleItemPosition()==mDataSet.size()-1) {
                            Toast.makeText(getContext(), "Pull more shit", Toast.LENGTH_SHORT).show();
                            if(showFriendsSelected) {
                                pullFriendsUserInfoList(mUserInfo,mCursorString,60,mDataSet.size()-1);
                            } else {
                                pullFollowerUserInfoList(mUserInfo,mCursorString,60,mDataSet.size()-1);
                            }
                        }
                    }
                }
            });
        } else {
mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (mLayoutManager == null || mDataSet == null || mDataSet.size()==0) {

            return;
        } else {
            if(mDataSet.size()>0 && mLayoutManager.findLastCompletelyVisibleItemPosition()==mDataSet.size()-1) {
                if(showFriendsSelected) {
                    pullFriendsUserInfoList(mUserInfo,mCursorString,60,mDataSet.size()-1);
                } else {
                    pullFollowerUserInfoList(mUserInfo,mCursorString,60,mDataSet.size()-1);
                }
            }
        }
    }
});
        }

//        if(mUserInfo.getBannerUrl() == null) {
//            dialogView  = inflater.inflate(R.layout.fragment_dialog_addusercheck_simple, null);
//        } else {

            try {
                loadBestFitBanner(mUserInfo.getScreenName(),imgBanner);
                //TODO set onclick listener for the banner

            } catch (NullPointerException e) {
                //TODO HIDE BANNER
                Log.e(TAG,"loadbestfit Nullpointer: " + e);
            } catch (Exception e) {
                //TODO HIDE BANNER
                Log.e(TAG,"loadbestfit failed: " + e);
            }
//        }

        if(mUserInfo.getProfileImageUrl()!=null) {

            /* Change the last bit of the image profile url from _normal to _bigger,
            * and try to get a better url image... if that doesn't work, do the normal url */


            Picasso picasso = new Picasso.Builder(getContext())
                    .listener(new Picasso.Listener() {
                        @Override
                        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                            //Here your log
                            Picasso.with(getContext()).load(mUserInfo.getProfileImageUrl())
                                    .into(imgProfile);

                        }
                    })
                    .build();
            picasso.load(mUserInfo.getProfileImageUrlBig())
                    .into(imgProfile);

        }

        imgProfile.bringToFront();

        txtDisplayName.setText(mUserInfo.getName());
        txtScreenName.setText(mUserInfo.getDisplayScreenName());

//        if(mUserInfo.getUrl() != null) {
//            txtUrl.setVisibility(View.VISIBLE);
//
//            SpannableString text = new SpannableString(mUserInfo.getUrl());
//            text.setSpan(new URLSpan(mUserInfo.getUrl()), 0, mUserInfo.getUrl().length(), 0);
//            txtUrl.setMovementMethod(LinkMovementMethod.getInstance());
//            txtUrl.setText(text, TextView.BufferType.SPANNABLE);
//        }

//        txtFollowingCount.setText(mUserInfo.getFriendCountString());
//        txtFollowersCount.setText(mUserInfo.getFollowerCountString());
        //TODO clean this up
        btnShowFriendsToggle.setText(mUserInfo.getFriendCountString() + " Friends");
        btnShowFollowersToggle.setText(mUserInfo.getFollowerCountString() + " Followers");

        /* Add the "Move / Copy"  buttons*/
        btnShowFriendsToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonActive(btnShowFollowersToggle,false);
                setButtonActive(btnShowFriendsToggle,true);

                if(!showFriendsSelected) {
                    mDataSet = new ArrayList<UserInfo>();
                    mCursorString = "-1";
                    pullFriendsUserInfoList(mUserInfo,mCursorString,60,0);
                    showFriendsSelected = true;
                }

            }
        });

        btnShowFollowersToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonActive(btnShowFollowersToggle,true);
                setButtonActive(btnShowFriendsToggle,false);
                if(showFriendsSelected) {
                    mDataSet = new ArrayList<UserInfo>();
                    mCursorString = "-1";
                    pullFollowerUserInfoList(mUserInfo,mCursorString,60,0);
                    showFriendsSelected = false;
                }
            }

        });

        if(mDataSet.size() == 0) {
            pullFollowerUserInfoList(mUserInfo,mCursorString,60,0);
        }
    };

    public boolean onTouch(View view, MotionEvent event) {

        // Get finger position on screen
        final int Y = (int) event.getRawY();

        // Switch on motion event type
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                // save default base layout height
                defaultViewHeight = baseLayout.getHeight();
                previousFingerPosition = Y;
                baseLayoutPosition = (int) baseLayout.getY();
                break;

            case MotionEvent.ACTION_UP:
                // If user was doing a scroll up
                if (isScrollingUp) {
                    // Reset baselayout position
                    baseLayout.setY(0);
                    // We are not in scrolling up mode anymore
                    isScrollingUp = false;
                }

                // If user was doing a scroll down
                if (isScrollingDown) {
                    // Reset baselayout position
                    baseLayout.setY(0);
                    // Reset base layout size
                    baseLayout.getLayoutParams().height = defaultViewHeight;
                    baseLayout.requestLayout();
                    // We are not in scrolling down mode anymore
                    isScrollingDown = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isClosing) {
                    int currentYPosition = (int) baseLayout.getY();

                    // If we scroll up
                    if (previousFingerPosition > Y) {
                        // First time android rise an event for "up" move
                        if (!isScrollingUp) {
                            isScrollingUp = true;
                        }

                        // Has user scroll down before -> view is smaller than it's default size -> resize it instead of change it position
                        if (baseLayout.getHeight() < defaultViewHeight) {
                            baseLayout.getLayoutParams().height = baseLayout.getHeight() - (Y - previousFingerPosition);
                            baseLayout.requestLayout();
                        } else {
                            // Has user scroll enough to "auto close" popup ?
                            if ((baseLayoutPosition - currentYPosition) > defaultViewHeight / 6) {
                                closeUpAndDismissDialog(currentYPosition);

                                return true;
                            }

                            //
                        }
                        baseLayout.setY(baseLayout.getY() + (Y - previousFingerPosition));

                    }
                    // If we scroll down
                    else {
                        // First time android rise an event for "down" move
                        if (!isScrollingDown) {
                            isScrollingDown = true;
                        }

                        // Has user scroll enough to "auto close" popup ?
                        if (Math.abs(baseLayoutPosition - currentYPosition) > defaultViewHeight / 6) {
                            closeDownAndDismissDialog(currentYPosition);
                            return true;
                        }

                        // Change base layout size and position (must change position because view anchor is top left corner)
                        baseLayout.setY(baseLayout.getY() + (Y - previousFingerPosition));
                        baseLayout.getLayoutParams().height = baseLayout.getHeight() - (Y - previousFingerPosition);
                        baseLayout.requestLayout();
                    }

                    // Update position
                    previousFingerPosition = Y;
                }
                break;


        }

        return true; //gestureDetector.onTouchEvent(event);
    }

    private void closeDownAndDismissDialog(int currentPosition) {
        isClosing = true;

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenHeight = size.y;
        ObjectAnimator positionAnimator = ObjectAnimator.ofFloat(baseLayout, "y", currentPosition, screenHeight + baseLayout.getHeight());
        positionAnimator.setDuration(300);
        positionAnimator.addListener(new Animator.AnimatorListener() {
            //            . . .
            @Override
            public void onAnimationStart(Animator animation) {

            }


            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

//                popupWindow.dismiss();
                mCallback.onBackPressed();
                if(BuildConfig.DEBUG){Log.d(TAG,") Blue: popupWindow.dismiss A");}

                //reset the position variables
                previousFingerPosition = 0;
                baseLayoutPosition = 0;
                isClosing = false;
                isScrollingUp = false;
                isScrollingDown = false;
                dismiss();
            }
        });
        positionAnimator.start();
    }

    /**
     *
     * @param currentPosition
     */
    private void closeUpAndDismissDialog(int currentPosition) {


        isClosing = true;
        ObjectAnimator positionAnimator = ObjectAnimator.ofFloat(baseLayout, "y", currentPosition, -baseLayout.getHeight());
        positionAnimator.setDuration(300);
        positionAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }


            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            //            . . .
            @Override
            public void onAnimationEnd(Animator animator) {

//                popupWindow.dismiss();
                mCallback.onBackPressed();
                //reset the position variables
                previousFingerPosition = 0;
                baseLayoutPosition = 0;
                isClosing = false;
                isScrollingUp = false;
                isScrollingDown = false;
//                dismiss();
            }

        });

        positionAnimator.start();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (DialogInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    /**
     * Shows progress bar during API lookups, hides otherwise
     * @param show boolean True for show, False for hide
     */
    public void showProgressBar(Boolean show) {
        if(show) {
//            divider.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
//            divider.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }



    private void setButtonActive(TextView textView,boolean active){
        textView.setSelected(active);

        if(active) {
            textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            textView.setAlpha(1.0f);
            textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        } else {
            textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
            textView.setAlpha(.80f);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTextDark));
        }

    }



    public void pullFollowerUserInfoList(final UserInfo userInfo,String cursorString,int limit,final int prevMaxPosition){

        showProgressBar(true);

            String token = getResources().getString(R.string.access_token);
            String tokenSecret = getResources().getString(R.string.access_token_secret);

            //TODO make the number of twitter responses an option! not just 10
            TwitterUserClient.getInstance(token,tokenSecret)
                    .getFollowersUserInfo(userInfo.getScreenName(),Long.parseLong(cursorString),limit)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<UserFollowersListContainer>() {
//                        List<UserInfo> mDataSet;
                        @Override public void onCompleted() {
                            if(BuildConfig.DEBUG){Log.d(TAG, "In onCompleted()");}
                            showProgressBar(false);
                            if(mDataSet.size()==0) {
                                showRecyclerView(false);
                            } else {
                                if(mAdapter==null) {
                                    mAdapter = new UserListAdapter(getContext(),mDataSet, _rxBus);
                                } else {
                                    mAdapter.notifyDataSetChanged();
                                    if(mDataSet.size()>prevMaxPosition) {
                                        mRecyclerView.smoothScrollToPosition(prevMaxPosition);
                                    }
                                }

                                _rxBus.toClickObserverable()
                                        .subscribe(new Action1<Object>() {
                                            @Override
                                            public void call(Object event) {

                                                if(isUniqueClick(1000) && event instanceof UserInfo) {
                                                    UserInfo userInfo = (UserInfo) event;
//                                                    Toast.makeText(getContext(), "CLICK: " + userInfo.getScreenName(), Toast.LENGTH_SHORT).show();
//                                                //TODO -- make it so only one instance of breakdown fragment exists
//                                                Tweet tweet = (Tweet) event;
//                                                TweetBreakDownFragment fragment = TweetBreakDownFragment.newInstanceTimeLine(tweet);
//                                                ((BaseContainerFragment)getParentFragment()).addFragment(fragment, true,"tweetbreakdown");

                                                    if(getFragmentManager().findFragmentByTag("dialogAddCheck") == null || !getFragmentManager().findFragmentByTag("dialogAddCheck").isAdded()) {
                                                        AddUserCheckDialog.newInstance(userInfo).show(getFragmentManager(),"dialogAddCheck");
                                                    }
                                                }

                                            }

                                        });

                                mRecyclerView.setAdapter(mAdapter);
                                Log.d(TAG,"show progress FALSE");
                            }



                        }

                        @Override public void onError(Throwable e) {
                            e.printStackTrace();
                            if(BuildConfig.DEBUG){Log.d(TAG, "In onError()");}
                            showProgressBar(false);
                            showRecyclerView(false);
                            Toast.makeText(getContext(), "Unable to get users for @" + userInfo.getScreenName(), Toast.LENGTH_SHORT).show();
                        }

                        @Override public void onNext(UserFollowersListContainer followers) {
                            if(BuildConfig.DEBUG) {
                                Log.d(TAG, "In onNext()");
                                Log.d(TAG,"FOLLOWERS SIZE: " + followers.getUsers().size());
                            }

                            if(!mCursorString.equals(followers.getNextCursorString())) {
                                try {
                                    mDataSet.addAll(followers.getUsers());
                                    mCursorString = followers.getNextCursorString();
                                    showRecyclerView(true);
                                } catch (Exception e) {
                                    Log.e(TAG,"Exception trying to pull follower data... "  + e.toString());
                                    showRecyclerView(false);
                                }
                            }

                        }
                    });

    }




    public void pullFriendsUserInfoList(final UserInfo userInfo,String cursorString,int limit,final int prevMaxPosition){

        showProgressBar(true);

        String token = getResources().getString(R.string.access_token);
        String tokenSecret = getResources().getString(R.string.access_token_secret);

        //TODO make the number of twitter responses an option! not just 10
        TwitterUserClient.getInstance(token,tokenSecret)
                .getFriendsUserInfo(userInfo.getScreenName(),Long.parseLong(cursorString),limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserFollowersListContainer>() {
                    //                        List<UserInfo> mDataSet;
                    @Override public void onCompleted() {
                        if(BuildConfig.DEBUG){Log.d(TAG, "In getFriendsUserInfo onCompleted()");}
                        showProgressBar(false);
                        if(mDataSet.size()==0) {
                            showRecyclerView(false);
                        } else {
//                            mAdapter = new UserListAdapter(getContext(),mDataSet, _rxBus);
                            if(mAdapter==null) {
                                mAdapter = new UserListAdapter(getContext(),mDataSet, _rxBus);
                            } else {
                                mAdapter.notifyDataSetChanged();
                                if(mDataSet.size()>prevMaxPosition) {
                                    mRecyclerView.smoothScrollToPosition(prevMaxPosition);
                                }

                            }

                            _rxBus.toClickObserverable()
                                    .subscribe(new Action1<Object>() {
                                        @Override
                                        public void call(Object event) {

                                            if(isUniqueClick(1000) && event instanceof UserInfo) {
                                                UserInfo userInfo = (UserInfo) event;
//                                                Toast.makeText(getContext(), "CLICK: " + userInfo.getScreenName(), Toast.LENGTH_SHORT).show();
//                                                //TODO -- make it so only one instance of breakdown fragment exists
//                                                Tweet tweet = (Tweet) event;
//                                                TweetBreakDownFragment fragment = TweetBreakDownFragment.newInstanceTimeLine(tweet);
//                                                ((BaseContainerFragment)getParentFragment()).addFragment(fragment, true,"tweetbreakdown");

                                                if(getFragmentManager().findFragmentByTag("dialogAddCheck") == null || !getFragmentManager().findFragmentByTag("dialogAddCheck").isAdded()) {
                                                    AddUserCheckDialog.newInstance(userInfo).show(getFragmentManager(),"dialogAddCheck");
                                                }
                                            }

                                        }

                                    });

                            mRecyclerView.setAdapter(mAdapter);
                            Log.d(TAG,"show progress FALSE");
                        }



                    }

                    @Override public void onError(Throwable e) {
                        e.printStackTrace();
                        if(BuildConfig.DEBUG){Log.d(TAG, "In onError()");}
                        showProgressBar(false);
                        showRecyclerView(false);
                        Toast.makeText(getContext(), "Unable to get users for @" + userInfo.getScreenName(), Toast.LENGTH_SHORT).show();
                    }

                    @Override public void onNext(UserFollowersListContainer followers) {
                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "In onNext()");
                            Log.d(TAG,"FOLLOWERS SIZE: " + followers.getUsers().size());
                        }

                        if(!mCursorString.equals(followers.getNextCursorString())) {
                            try {
                                mDataSet.addAll(followers.getUsers());
                                mCursorString = followers.getNextCursorString();
                                showRecyclerView(true);
                            } catch (Exception e) {
                                Log.e(TAG,"Exception trying to pull follower data... "  + e.toString());
                                showRecyclerView(false);
                            }
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
            txtNoUsers.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            txtNoUsers.setVisibility(View.VISIBLE);
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

    private void loadBestFitBanner(final String screenName, final ImageView imgBanner) {

        Log.d(TAG,"INSIDE loading best fit banner");
        String token = getResources().getString(R.string.access_token);
        String tokenSecret = getResources().getString(R.string.access_token_secret);

//        final String bestFitUrl;
        TwitterUserClient.getInstance(token,tokenSecret)
                .getProfileBanner(screenName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserProfileBanner>() {
                    UserProfileBanner bannerInstance;


                    @Override public void onCompleted() {
                        if(BuildConfig.DEBUG){
                            Log.d(TAG, "In onCompleted()");}

                            /* If the user exists and a UserInfo object has been populated,
                            * save it to the database and update the UserInfoFragment adapter */
                        if(bannerInstance != null) {

                            //Set the banner that best fits phone size

                            try {
                                DisplayMetrics metrics = new DisplayMetrics();
                                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                String bannerUrl = bannerInstance.getBestFittingBannerUrl(metrics.widthPixels);
                                if(bannerUrl.length()>0 ) {
                                    Picasso.with(getActivity()).load(bannerUrl).fit() // resizes the image to these dimensions (in pixel). does not respect aspect ratio
                                            .into(imgBanner, new com.squareup.picasso.Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    //TODO load with banner
                                                }

                                                @Override
                                                public void onError() {
                                                    //TODO load without banner
                                                    onCreateDialog(null);
                                                }
                                            });




                                }
                            } catch (NullPointerException e) {
                                Log.e(TAG,"Banner url pull Nullpointed: " + e);
                            } catch (Exception e) {
                                Log.e(TAG,"Banner url pull Other Exception: " + e);
                            }



                        } else {
//                            Toast.makeText(MainActivity.this, "Unable to add user " + screenName, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override public void onError(Throwable e) {
                        e.printStackTrace();
                        if(BuildConfig.DEBUG){Log.d(TAG, "BestFitBannerURL In onError()");}
                        Log.d(TAG,"ERROR CAUSE: " + e.getCause());
                    }

                    @Override public void onNext(UserProfileBanner userProfileBanner) {
                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "BestFitBannerURL In onNext()");
                        }

                        /***TMP**/
                        if(bannerInstance == null) {
                            bannerInstance = userProfileBanner;
                        }


                    }
                });

    }



    /**
     * Removes a users screenName from the database and updates recyclerview in main fragment.
     * Is called from {@link RemoveUserDialog} via the {@link DialogInteractionListener}
//     * @param screenName UserInfo to remove from database
     *
     */
    @Override
    public void onRemoveUserDialogPositiveClick(String userId) {
            dismiss();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("mUserInfo", mUserInfo);
        outState.putBoolean("showFriendsSelected", showFriendsSelected);
        outState.putParcelableArrayList("mDataSet",mDataSet);
    }


}

