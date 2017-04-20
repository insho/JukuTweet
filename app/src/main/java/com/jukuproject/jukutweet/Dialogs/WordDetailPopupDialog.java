package com.jukuproject.jukutweet.Dialogs;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.FavoritesColors;
import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.SearchTweetsContainer;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;
import com.jukuproject.jukutweet.TwitterUserClient;

import java.util.ArrayList;
import java.util.HashMap;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by Joe on 11/21/2015.
 */

public class WordDetailPopupDialog extends DialogFragment implements View.OnTouchListener {

    String TAG = "TEST-userdetailpop";
        private DialogInteractionListener mCallback;
    //    private Context mContext;
//    private View mAnchorView;
    private RxBus mRxBus = new RxBus();
    private RecyclerView mRecyclerView;

    private SmoothProgressBar progressBar;
    private View divider;
    private ArrayList<String> mActiveTweetFavoriteStars;

    private WordEntry mWordEntry;
    private boolean showSavedTweetsSelected;
    private TextView btnShowSavedTweetsToggle;
    private TextView btnSearchForTweetsToggle;
    private TextView txtNoTweetsFound;

    private TextView textKanji;
    private TextView listViewPopupDefinition;
    private TextView textPercentage;
    private TextView textScore;
    private FrameLayout imgStarLayout;
    private ImageButton btnImageStar;
    private TextView btnCollapseDefinition;
    //    private TextView txtFollowersCount;
    private View baseLayout;
    //    private View popupView;
    private ArrayList<Tweet> mDataSet;
    private int previousFingerPosition = 0;
    private int baseLayoutPosition = 0;
    private int defaultViewHeight;
    private boolean isClosing = false;
    private boolean isScrollingUp = false;
    private boolean isScrollingDown = false;
    private ColorThresholds mColorThresholds;
    private UserTimeLineAdapter mAdapter;
    private ScrollView mScrollView;
    private ImageView imgBanner;
    /* keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
    private String mCursorString = "-1";
    private Subscription searchQuerySubscription;
    private LinearLayoutManager mLayoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);

    }


    //    private RxBus _rxBus = new RxBus();

    public WordDetailPopupDialog() {}

    public static WordDetailPopupDialog newInstance(WordEntry wordEntry) {
        WordDetailPopupDialog fragment = new WordDetailPopupDialog();
        Bundle args = new Bundle();
        args.putParcelable("mWordEntry", wordEntry);
        fragment.setArguments(args);
        return  fragment;
    }

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
//        return super.onCreateDialog(savedInstanceState);
//
//    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_worddetailpopup, null);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.userInfoRecycler);
        baseLayout = view.findViewById(R.id.popuptab_layout);
        progressBar = (SmoothProgressBar) view.findViewById(R.id.progressbar);
        divider = (View) view.findViewById(R.id.dividerview);
        mScrollView = (ScrollView) view.findViewById(R.id.scrollView);

        btnShowSavedTweetsToggle = (TextView) view.findViewById(R.id.txtShowFollowingToggle);
        btnSearchForTweetsToggle = (TextView) view.findViewById(R.id.txtShowFollowersToggle);
        txtNoTweetsFound = (TextView) view.findViewById(R.id.txtNoUsers);

        imgStarLayout = (FrameLayout) view.findViewById(R.id.popup_framelayout);
        btnImageStar = (ImageButton) view.findViewById(R.id.favorite);
        textKanji = (TextView) view.findViewById(R.id.textViewPopupKanji);
        listViewPopupDefinition = (TextView) view.findViewById(R.id.listViewPopupDefinition);
        textPercentage = (TextView) view.findViewById(R.id.textViewPopupPercentage);
        textScore = (TextView) view.findViewById(R.id.textViewPopupScore);
        btnCollapseDefinition = (TextView) view.findViewById(R.id.textViewCollapsButton1);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        baseLayout.setOnTouchListener(this);
        mActiveTweetFavoriteStars = SharedPrefManager.getInstance(getContext()).getActiveTweetFavoriteStars();
        mColorThresholds = SharedPrefManager.getInstance(getContext()).getColorThresholds();

        if(savedInstanceState == null) {
            showSavedTweetsSelected = true;
            mWordEntry = getArguments().getParcelable("mWordEntry");

            mDataSet = InternalDB.getTweetInterfaceInstance(getContext()).getTweetsThatIncludeAWord(String.valueOf(mWordEntry.getId()),mColorThresholds);

        } else {
            mWordEntry = savedInstanceState.getParcelable("mWordEntry");
            showSavedTweetsSelected = savedInstanceState.getBoolean("showSavedTweetsSelected");
            mDataSet = savedInstanceState.getParcelableArrayList("mDataSet");
        }



        setButtonActive(btnShowSavedTweetsToggle,true);
        setButtonActive(btnSearchForTweetsToggle,false);


        StringBuilder displayKanji = new StringBuilder();
        displayKanji.append(mWordEntry.getKanji());
        if(mWordEntry.getFurigana()!=null
                && mWordEntry.getFurigana().length()>0
                && !mWordEntry.getFurigana().equals(mWordEntry.getKanji())) {
            displayKanji.append(" (");
            displayKanji.append(mWordEntry.getFurigana());
            displayKanji.append(")");
        }
        textKanji.setText(displayKanji.toString());

        listViewPopupDefinition.setText(mWordEntry.getDefinitionMultiLineString(20));
        listViewPopupDefinition.setTypeface(null, Typeface.ITALIC);
        listViewPopupDefinition.setFocusable(false);
        listViewPopupDefinition.setClickable(false);

        int defLineCount = countNumberOfLinesinDefinition(listViewPopupDefinition.getText().toString());
        listViewPopupDefinition.setTag(defLineCount);
        if(defLineCount>2) {
            btnCollapseDefinition.setVisibility(View.VISIBLE);
        } else {
            btnCollapseDefinition.setVisibility(View.GONE);
        }

        btnCollapseDefinition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listViewPopupDefinition.getTag() != null
                        && (int)listViewPopupDefinition.getTag()>2) {
                    listViewPopupDefinition.setText(mWordEntry.getDefinitionMultiLineString(2));
                    listViewPopupDefinition.setTag(2);
                    btnCollapseDefinition.setText("+");
                } else {
                    listViewPopupDefinition.setText(mWordEntry.getDefinitionMultiLineString(20));
                    listViewPopupDefinition.setTag(20);
                    btnCollapseDefinition.setText("-");
                }
            }
        });

        //TODO convert to word strings
        textScore.setText(mWordEntry.getCorrect() + "/" + mWordEntry.getTotal());
        textPercentage.setText((int)(mWordEntry.getPercentage()*100) + "%");

        imgStarLayout.setClickable(true);
        imgStarLayout.setLongClickable(true);
        Integer starColorDrawableInt = FavoritesColors.assignStarResource(mWordEntry.getItemFavorites(), SharedPrefManager.getInstance(getContext()).getActiveFavoriteStars());
        btnImageStar.setImageResource(starColorDrawableInt);


//        if (android.os.Build.VERSION.SDK_INT >= 23) {
//            mRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//                @Override
//                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                    if (mLayoutManager == null || mDataSet == null || mDataSet.size()==0) {
//
//                        return;
//                    } else {
//                        if(mDataSet.size()>0 && mLayoutManager.findLastCompletelyVisibleItemPosition()==mDataSet.size()-1) {
//                            Toast.makeText(getContext(), "Pull more shit", Toast.LENGTH_SHORT).show();
//                            if(!showSavedTweetsSelected) {
//                                searchForTweetsWithWord(mUserInfo,mCursorString,60,mDataSet.size()-1);
//                            } else {
//                                pullFollowerUserInfoList(mUserInfo,mCursorString,60,mDataSet.size()-1);
//                            }
//                        }
//                    }
//                }
//            });
//        } else {
//            mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//                @Override
//                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                    super.onScrolled(recyclerView, dx, dy);
//                    if (mLayoutManager == null || mDataSet == null || mDataSet.size()==0) {
//
//                        return;
//                    } else {
//                        if(mDataSet.size()>0 && mLayoutManager.findLastCompletelyVisibleItemPosition()==mDataSet.size()-1) {
//                            if(!showSavedTweetsSelected) {
//                                searchForTweetsWithWord(mUserInfo,mCursorString,60,mDataSet.size()-1);
//                            } else {
//                                pullFollowerUserInfoList(mUserInfo,mCursorString,60,mDataSet.size()-1);
//                            }
//                        }
//                    }
//                }
//            });
//        }






        //TODO clean this up
        btnShowSavedTweetsToggle.setText("Saved Tweets");
        btnSearchForTweetsToggle.setText("Search Twitter");

        btnShowSavedTweetsToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonActive(btnSearchForTweetsToggle,false);
                setButtonActive(btnShowSavedTweetsToggle,true);

                if(!showSavedTweetsSelected) {
//                    mDataSet = new ArrayList<Tweet>();
                    mCursorString = "-1";
                    showSavedTweetsSelected = true;
                    mDataSet.clear();
                    mDataSet.addAll(InternalDB.getTweetInterfaceInstance(getContext()).getTweetsThatIncludeAWord(String.valueOf(mWordEntry.getId()),mColorThresholds));
                    mAdapter.notifyDataSetChanged();
                }

            }
        });

        btnSearchForTweetsToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonActive(btnSearchForTweetsToggle,true);
                setButtonActive(btnShowSavedTweetsToggle,false);
                if(showSavedTweetsSelected) {
                    showProgressBar(true);
                    mDataSet.clear();
                    mAdapter.notifyDataSetChanged();
                    mCursorString = "-1";
                    runTwitterSearch(mWordEntry.getKanji());
                    showSavedTweetsSelected = false;
                }
            }

        });

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

//        String coreKanjiBlock;
//        try {
//            Log.e(TAG,"BULLSHIT, start: " +mWordEntry.getKanji() + ": " + mWordEntry.getStartIndex() + " = " + mWordEntry.getEndIndex());
//            coreKanjiBlock = mWordEntry.getKanji().substring(0,mWordEntry.getEndIndex()-mWordEntry.getStartIndex());
//        } catch (Exception e) {
//            coreKanjiBlock = mWordEntry.getKanji();
//            Log.e(TAG,"FAILED TO SET CORE KANJI BLOCK, start: " + mWordEntry.getStartIndex() + " = " + mWordEntry.getEndIndex());
//        }

        mAdapter = new UserTimeLineAdapter(getContext(), mRxBus, mDataSet, mActiveTweetFavoriteStars,mWordEntry.getKanji());

        if(mDataSet.size()==0) {
            showRecyclerView(false);
        } else {
            showRecyclerView(true);
            mRecyclerView.setAdapter(mAdapter);
        }


//        if(mDataSet.size() == 0) {
//            pullFollowerUserInfoList(mUserInfo,mCursorString,60,0);
//        }
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
                if(BuildConfig.DEBUG){Log.d(TAG,") Blue: popupWindow.dismiss A");}

                //reset the position variables
                previousFingerPosition = 0;
                baseLayoutPosition = 0;
                isClosing = false;
                isScrollingUp = false;
                isScrollingDown = false;
//                mCallback.onBackPressed();
                dismiss();
//                mCallback.showFab(true);
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

                //reset the position variables
                previousFingerPosition = 0;
                baseLayoutPosition = 0;
                isClosing = false;
                isScrollingUp = false;
                isScrollingDown = false;
                dismiss();
//                mCallback.showFab(true);

            }

        });

        positionAnimator.start();

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




//
//    public void searchForTweetsWithWord(final UserInfo userInfo, String cursorString, int limit, final int prevMaxPosition){
//
//        showProgressBar(true);
//
//        String token = getResources().getString(R.string.access_token);
//        String tokenSecret = getResources().getString(R.string.access_token_secret);
//
//        //TODO make the number of twitter responses an option! not just 10
//        TwitterUserClient.getInstance(token,tokenSecret)
//                .getFriendsUserInfo(userInfo.getScreenName(),Long.parseLong(cursorString),limit)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<UserFollowersListContainer>() {
//                    //                        List<UserInfo> mDataSet;
//                    @Override public void onCompleted() {
//                        if(BuildConfig.DEBUG){Log.d(TAG, "In getFriendsUserInfo onCompleted()");}
//                        showProgressBar(false);
//                        if(mDataSet.size()==0) {
//                            showRecyclerView(false);
//                        } else {
////                            mAdapter = new UserListAdapter(getContext(),mDataSet, _rxBus);
//                            if(mAdapter==null) {
//                                mAdapter = new UserListAdapter(getContext(),mDataSet, _rxBus);
//                            } else {
//                                mAdapter.notifyDataSetChanged();
//                                if(mDataSet.size()>prevMaxPosition) {
//                                    mRecyclerView.smoothScrollToPosition(prevMaxPosition);
//                                }
//
//                            }
//
//                            _rxBus.toClickObserverable()
//                                    .subscribe(new Action1<Object>() {
//                                        @Override
//                                        public void call(Object event) {
//
//                                            if(isUniqueClick(1000) && event instanceof UserInfo) {
//                                                UserInfo userInfo = (UserInfo) event;
//                                                if(getFragmentManager().findFragmentByTag("dialogAddCheck") == null || !getFragmentManager().findFragmentByTag("dialogAddCheck").isAdded()) {
//                                                    AddUserCheckDialog.newInstance(userInfo).show(getFragmentManager(),"dialogAddCheck");
//                                                }
//                                            }
//
//                                        }
//
//                                    });
//
//                            mRecyclerView.setAdapter(mAdapter);
//                            Log.d(TAG,"show progress FALSE");
//                        }
//
//
//
//                    }
//
//                    @Override public void onError(Throwable e) {
//                        e.printStackTrace();
//                        if(BuildConfig.DEBUG){Log.d(TAG, "In onError()");}
//                        showProgressBar(false);
//                        showRecyclerView(false);
//                        Toast.makeText(getContext(), "Unable to get users for @" + userInfo.getScreenName(), Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override public void onNext(UserFollowersListContainer followers) {
//                        if(BuildConfig.DEBUG) {
//                            Log.d(TAG, "In onNext()");
//                            Log.d(TAG,"FOLLOWERS SIZE: " + followers.getUsers().size());
//                        }
//
//                        if(!mCursorString.equals(followers.getNextCursorString())) {
//                            try {
//                                mDataSet.addAll(followers.getUsers());
//                                mCursorString = followers.getNextCursorString();
//                                showRecyclerView(true);
//                            } catch (Exception e) {
//                                Log.e(TAG,"Exception trying to pull follower data... "  + e.toString());
//                                showRecyclerView(false);
//                            }
//                        }
//
//                    }
//                });
//
//    }





    /**
     * Toggles between showing recycler (if there are followed users in the database)
     * and hiding the recycler while showing the "no users found" message if there are not
     * @param show bool True to show recycler, False to hide it
     */
    private void showRecyclerView(boolean show) {
        if(show) {
            mRecyclerView.setVisibility(View.VISIBLE);
            txtNoTweetsFound.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            txtNoTweetsFound.setVisibility(View.VISIBLE);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (DialogInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("mWordEntry", mWordEntry);
        outState.putBoolean("showSavedTweetsSelected", showSavedTweetsSelected);
        outState.putParcelableArrayList("mDataSet",mDataSet);
    }

    private int countNumberOfLinesinDefinition(String definition) {
        int index = 0;
        int linecounter = 0;

        while(index<definition.length() && definition.substring(index,definition.length()).contains("\u2022")) {
            Log.d(TAG,linecounter + " - " + definition.substring(index,definition.length()));

            index = definition.substring(0,index).length() + definition.substring(index,definition.length()).indexOf("\u2022") + 1;
            linecounter += 1;
        }
//

//        int lastIndex = 0;
//        int count = 0;
//
//        while ((lastIndex = definition.indexOf("\u2022", lastIndex)) != -1) {
//            count++;
//            lastIndex += "\u2022".length() - 1;
//        }

return linecounter;
//        return count;
    }



    public void runTwitterSearch(final String query) {
        if(searchQuerySubscription !=null && !searchQuerySubscription.isUnsubscribed()) {
            searchQuerySubscription.unsubscribe();
        }

        String token = getResources().getString(R.string.access_token);
        String tokenSecret = getResources().getString(R.string.access_token_secret);

        searchQuerySubscription = TwitterUserClient.getInstance(token,tokenSecret)
                .getSearchTweets(query,"ja",25)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<SearchTweetsContainer>() {

//                    ArrayList<Tweet> mDataSet = new ArrayList<>();

                    @Override public void onCompleted() {
                        if(BuildConfig.DEBUG){Log.d(TAG, "runTwitterSearch In onCompleted()");}
                        showProgressBar(false);

                        try {
                            //Compile a string concatenation of all user ids in the set of tweets
                            StringBuilder stringBuilder = new StringBuilder();
                            for(Tweet tweet : mDataSet) {
                                if(stringBuilder.length()>0) {
                                    stringBuilder.append(",");

                                }
                                stringBuilder.append(tweet.getUser().getUserId());
                            }

                            //Pull a list of favorited tweets for those user ids (if any exist)
                            if(stringBuilder.length()>0) {
                                HashMap<String,ItemFavorites> tweetIdStringsInFavorites = InternalDB.getTweetInterfaceInstance(getContext()).getStarFavoriteDataForAUsersTweets(stringBuilder.toString());

                                if(tweetIdStringsInFavorites.size()>0) {
                                    for(Tweet tweet : mDataSet) {

                                        //Attach colorfavorites to tweet, if they exists in db
                                        if(tweet.getIdString()!=null && tweetIdStringsInFavorites.keySet().contains(tweet.getIdString())) {
                                            tweet.setItemFavorites(tweetIdStringsInFavorites.get(tweet.getIdString()));
                                        } else {
                                            tweet.setItemFavorites(new ItemFavorites());
                                        }

                                    }
                                }
                            }

                        } catch (Exception e){
                            Log.e(TAG,"Adding favorite information to tweets exception: " + e.toString());
                        }


                       //TODO add shit to adapter and run it... and save it if necessary...
//                        mActiveTweetFavoriteStars = SharedPrefManager.getInstance(getContext()).getActiveTweetFavoriteStars();
//                        mAdapter = new UserTimeLineAdapter(getContext(), mRxBus, mDataSet, mActiveTweetFavoriteStars);
//                        mRecyclerView.setAdapter(mAdapter)
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override public void onError(Throwable e) {
                        e.printStackTrace();
                        showProgressBar(false);

                        if(BuildConfig.DEBUG){Log.d(TAG, "runTwitterSearch In onError()");}
                    }

                    @Override public void onNext(SearchTweetsContainer results) {
                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "runTwitterSearch In onNext()");
//                        Log.d(TAG," tweet search results SIZE: " + results.size());
                        }
                        if(mDataSet.size() == 0) {
                            try{
                                mDataSet.addAll(results.getTweets());
                            } catch (Exception e){
                                Log.e(TAG,"exception in runTwitterSearch get tweets");
                            }
//
                        }

                    }
                });

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if(searchQuerySubscription!=null) {
            searchQuerySubscription.unsubscribe();
        }
        super.onDismiss(dialog);
    }

    @Override
    public void onDestroy() {
        if(searchQuerySubscription!=null) {
            searchQuerySubscription.unsubscribe();
        }
        super.onDestroy();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}

