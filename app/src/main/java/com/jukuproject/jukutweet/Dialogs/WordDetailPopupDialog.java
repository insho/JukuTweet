package com.jukuproject.jukutweet.Dialogs;

import android.animation.Animator;
import android.animation.ObjectAnimator;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.ChooseFavoriteListsPopupWindow;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.FavoritesColors;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Interfaces.WordEntryFavoritesChangedListener;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.MyListEntry;
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
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Popup dialog showing detail for a Word in the Edict dictionary. Top half displays kanji and definition, quiz score percentages, as well
 * as a favorites star to add/subtract word from a list. Bottom half allows user to toggle between saved tweets that contain the word, and
 * a search of twitter for tweets that contain the word.
 *
 */
public class WordDetailPopupDialog extends DialogFragment implements View.OnTouchListener {

    String TAG = "TEST-worddetailpop";
    private WordEntryFavoritesChangedListener mCallback;
    private boolean favoriteStarHasBeenChanged = false; // if fav star changes, call back to refresh dialog (like for browse words, the word may no longer be contained in that list..)
    private RxBus mRxBus = new RxBus();
    private RecyclerView mRecyclerView;

    private SmoothProgressBar progressBar;
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
    private ImageButton imgStar;
    private TextView btnCollapseDefinition;
    private View baseLayout;
    private ArrayList<Tweet> mDataSet;
    private int previousFingerPosition = 0;
    private int baseLayoutPosition = 0;
    private int defaultViewHeight;
    private boolean isClosing = false;
    private boolean isScrollingUp = false;
    private boolean isScrollingDown = false;
    private ColorThresholds mColorThresholds;
    private UserTimeLineAdapter mAdapter;
    /* keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
    private String mCursorString = "-1";
    private Subscription searchQuerySubscription;
    private LinearLayoutManager mLayoutManager;
//    private int userCreatedListCount;
    private DisplayMetrics metrics;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent_NoTitleBar);
    }

    public WordDetailPopupDialog() {}

    public static WordDetailPopupDialog newInstance(WordEntry wordEntry) {
        WordDetailPopupDialog fragment = new WordDetailPopupDialog();
        Bundle args = new Bundle();
        args.putParcelable("mWordEntry", wordEntry);
        fragment.setArguments(args);
        return  fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_worddetailpopup, null);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.userInfoRecycler);
        baseLayout = view.findViewById(R.id.popuptab_layout);
        progressBar = (SmoothProgressBar) view.findViewById(R.id.progressbar);

        btnShowSavedTweetsToggle = (TextView) view.findViewById(R.id.txtShowFollowingToggle);
        btnSearchForTweetsToggle = (TextView) view.findViewById(R.id.txtShowFollowersToggle);
        txtNoTweetsFound = (TextView) view.findViewById(R.id.txtNoUsers);

        imgStarLayout = (FrameLayout) view.findViewById(R.id.popup_framelayout);
        imgStar = (ImageButton) view.findViewById(R.id.favorite);
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
            mCursorString = "-1";
        } else {
            mWordEntry = savedInstanceState.getParcelable("mWordEntry");
            showSavedTweetsSelected = savedInstanceState.getBoolean("showSavedTweetsSelected");
            mDataSet = savedInstanceState.getParcelableArrayList("mDataSet");
            mCursorString = savedInstanceState.getString("mCursorString","-1");
        }

        metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

//        userCreatedListCount = InternalDB.getTweetInterfaceInstance(getContext()).getUserCreatedTweetListCount();
        setButtonActive(btnShowSavedTweetsToggle,true);
        setButtonActive(btnSearchForTweetsToggle,false);


        StringBuilder displayKanji = new StringBuilder();
        displayKanji.append(mWordEntry.getKanji());

        /* Make display version of furigana and kanji if furigan exists */
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

        textScore.setText(mWordEntry.getCorrect() + "/" + mWordEntry.getTotal());
        textPercentage.setText(getString(R.string.percentage,(int)(mWordEntry.getPercentage()*100)));

        imgStarLayout.setClickable(true);
        imgStarLayout.setLongClickable(true);

        final ArrayList<String> activeFavoriteWordStars = SharedPrefManager.getInstance(getContext()).getActiveFavoriteStars();
        Integer starColorDrawableInt = FavoritesColors.assignStarResource(mWordEntry.getItemFavorites(), activeFavoriteWordStars);
        imgStar.setImageResource(starColorDrawableInt);

        if(starColorDrawableInt!=R.drawable.ic_star_multicolor) {
                try {
                    imgStar.setColorFilter(ContextCompat.getColor(getContext(),FavoritesColors.assignStarColor(mWordEntry.getItemFavorites(),activeFavoriteWordStars)));
                } catch (NullPointerException e) {
                    Log.e(TAG,"Nullpointer error setting star color filter in word detail popup dialog... Need to assign item favorites to WordEntry(?)" + e.getCause());
                }
        }



        imgStarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                favoriteStarHasBeenChanged = true;

                if(BuildConfig.DEBUG) {
                    Log.d(TAG,"mActiveFavoriteStars: " + activeFavoriteWordStars);
                    Log.d(TAG,"should open: " + mWordEntry.getItemFavorites().shouldOpenFavoritePopup(activeFavoriteWordStars));
                }

                if(mWordEntry.getItemFavorites().shouldOpenFavoritePopup(activeFavoriteWordStars)) {
                    showFavoriteListPopupWindow(mWordEntry,activeFavoriteWordStars,metrics);
                } else {

                    if(FavoritesColors.onFavoriteStarToggle(getContext(),activeFavoriteWordStars,mWordEntry)) {
                        imgStar.setImageResource(R.drawable.ic_star_black);
                        try {
                            imgStar.setColorFilter(ContextCompat.getColor(getContext(),FavoritesColors.assignStarColor(mWordEntry.getItemFavorites(),activeFavoriteWordStars)));
                        } catch (NullPointerException e) {
                            Log.e(TAG,"showFavoriteListPopupWindow Nullpointer error setting star color filter in word detail popup dialog... Need to assign item favorites to WordEntry(?)" + e.getCause());
                        }
                    } else {
                        Log.e(TAG,"OnFavoriteStarToggle did not work...");
                    }
                }
            }
        });


        imgStarLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                showFavoriteListPopupWindow(mWordEntry,activeFavoriteWordStars,metrics);

                return true;
            }
        });

        btnShowSavedTweetsToggle.setText(getContext().getString(R.string.menuchildviewsavedtweets));
        btnSearchForTweetsToggle.setText(getContext().getString(R.string.wordDetailPopup_SearchTwitter));

        btnShowSavedTweetsToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonActive(btnSearchForTweetsToggle,false);
                setButtonActive(btnShowSavedTweetsToggle,true);

                if(searchQuerySubscription!=null) {
                    searchQuerySubscription.unsubscribe();
                }

                if(!showSavedTweetsSelected) {

                    mRecyclerView.setVisibility(View.GONE);
                    txtNoTweetsFound.setVisibility(View.GONE);

                    mCursorString = "-1";
                    showSavedTweetsSelected = true;
                    mDataSet.clear();

                    mDataSet.addAll(InternalDB.getTweetInterfaceInstance(getContext()).getTweetsThatIncludeAWord(String.valueOf(mWordEntry.getId()),mColorThresholds));
                    mAdapter = new UserTimeLineAdapter(getContext(), mRxBus, mDataSet, mActiveTweetFavoriteStars,metrics,mWordEntry.getKanji(),false);
                    mRecyclerView.setAdapter(mAdapter);

                    if(mDataSet.size()>=0) {
                        showRecyclerView(true);
                    } else {
                        showRecyclerView(false);
                    }

                }

            }
        });

        btnSearchForTweetsToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(searchQuerySubscription!=null) {
                    searchQuerySubscription.unsubscribe();
                }

                setButtonActive(btnSearchForTweetsToggle,true);
                setButtonActive(btnShowSavedTweetsToggle,false);
                if(showSavedTweetsSelected) {
                    mRecyclerView.setVisibility(View.GONE);
                    txtNoTweetsFound.setVisibility(View.GONE);

                    showProgressBar(true);
                    mDataSet.clear();
                    showSavedTweetsSelected = false;
                    mAdapter.notifyDataSetChanged();
                    mCursorString = "-1";
                    runTwitterSearch(mWordEntry.getKanji());
                }
            }

        });

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new UserTimeLineAdapter(getContext(), mRxBus, mDataSet, mActiveTweetFavoriteStars,metrics,mWordEntry.getKanji(),false);

        if(mDataSet.size()==0) {
            showRecyclerView(false);
        } else {
            showRecyclerView(true);
            mRecyclerView.setAdapter(mAdapter);
        }

        /* If user was performing the twitter search when activity was recreated*/
        if(!showSavedTweetsSelected) {
            setButtonActive(btnSearchForTweetsToggle,true);
            setButtonActive(btnShowSavedTweetsToggle,false);
            mRecyclerView.setVisibility(View.GONE);
            txtNoTweetsFound.setVisibility(View.GONE);

            showProgressBar(true);
            mDataSet.clear();
            showSavedTweetsSelected = false;
            mAdapter.notifyDataSetChanged();
            mCursorString = "-1";
            runTwitterSearch(mWordEntry.getKanji());

        }

    };

    /**
     * TODO write this
     * @param view
     * @param event
     * @return
     */
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



    private int countNumberOfLinesinDefinition(String definition) {
        int index = 0;
        int linecounter = 0;

        while(index<definition.length() && definition.substring(index,definition.length()).contains("\u2022")) {
            Log.d(TAG,linecounter + " - " + definition.substring(index,definition.length()));

            index = definition.substring(0,index).length() + definition.substring(index,definition.length()).indexOf("\u2022") + 1;
            linecounter += 1;
        }

        return linecounter;
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
                        mActiveTweetFavoriteStars = SharedPrefManager.getInstance(getContext()).getActiveTweetFavoriteStars();
                        mAdapter = new UserTimeLineAdapter(getContext(), mRxBus, mDataSet, mActiveTweetFavoriteStars,metrics,mWordEntry.getKanji(),true);
                        mRecyclerView.setAdapter(mAdapter);
                        showRecyclerView(true);
//                        mAdapter.notifyDataSetChanged();
//                        mAdapter.showStar(true);

                    }

                    @Override public void onError(Throwable e) {
                        e.printStackTrace();
                        showProgressBar(false);
                        showRecyclerView(false);

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
        if(favoriteStarHasBeenChanged) {
            mCallback.updateWordEntryItemFavorites(mWordEntry);
            mCallback.updateWordEntryFavoritesForOtherTabs(mWordEntry);
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
    public void onPause() {
        if(searchQuerySubscription!=null) {
            searchQuerySubscription.unsubscribe();
        }
        super.onPause();
    }


    /**
     * Displays the {@link ChooseFavoriteListsPopupWindow} when the "favorites star" is clicked in the WordDetailPopupDialog
     * @param wordEntry WordEntry that is currently showing in the WordDetailPopupDialog
     * @param activeFavoriteStars list of system lists that the word can be saved to. Used to assign correct color to word after popupwindow is dismissed
     * @param metrics display metrics
     */
    public void showFavoriteListPopupWindow(final WordEntry wordEntry
            ,final ArrayList<String> activeFavoriteStars
            ,DisplayMetrics metrics
            ) {
        RxBus rxBus = new RxBus();

        ArrayList<MyListEntry> availableFavoriteLists = InternalDB.getWordInterfaceInstance(getContext()).getWordListsForAWord(activeFavoriteStars,String.valueOf(wordEntry.getId()),1,null);

        PopupWindow popupWindow =  ChooseFavoriteListsPopupWindow.createWordFavoritesPopup(getContext(),metrics,rxBus,availableFavoriteLists,wordEntry.getId());

        popupWindow.getContentView().measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        int xadjust = popupWindow.getContentView().getMeasuredWidth() + (int) (25 * metrics.density + 0.5f);

        /* Depending on the size of the popup window, the window's y-coordinates must be adjusted so the window
        * stays on the screen. */
        int yadjust;

        if(availableFavoriteLists.size()<4) {
            yadjust = (int)((popupWindow.getContentView().getMeasuredHeight()  + imgStar.getMeasuredHeight())/2.0f);
        } else {
            yadjust = (int)((popupWindow.getContentView().getMeasuredHeight()*.25f + imgStar.getMeasuredHeight())/2.0f);
        }

        if(BuildConfig.DEBUG) {
            Log.d("TEST","pop width: " + popupWindow.getContentView().getMeasuredWidth() + " height: " + popupWindow.getContentView().getMeasuredHeight());
            Log.d("TEST","xadjust: " + xadjust + ", yadjust: " + yadjust);
        }

        rxBus.toClickObserverable().subscribe(new Action1<Object>() {
            @Override
            public void call(Object event) {

                /* Recieve a MyListEntry (containing an updated list entry for this row kanji) from
                * the ChooseFavoritesAdapter in the ChooseFavorites popup window */
                if(event instanceof MyListEntry) {
                    MyListEntry myListEntry = (MyListEntry) event;

                    /* Ascertain the type of list that the kanji was added to (or subtracted from),
                    and update that list's count */
                    if(myListEntry.getListsSys() == 1) {
                        switch (myListEntry.getListName()) {
                            case "Blue":
                                wordEntry.getItemFavorites().setSystemBlueCount(myListEntry.getSelectionLevel());
                                break;
                            case "Green":
                                wordEntry.getItemFavorites().setSystemGreenCount(myListEntry.getSelectionLevel());
                                break;
                            case "Red":
                                wordEntry.getItemFavorites().setSystemRedCount(myListEntry.getSelectionLevel());
                                break;
                            case "Yellow":
                                wordEntry.getItemFavorites().setSystemYellowCount(myListEntry.getSelectionLevel());
                                break;
                            case "Purple":
                                wordEntry.getItemFavorites().setSystemPurpleCount(myListEntry.getSelectionLevel());
                                break;
                            case "Orange":
                                wordEntry.getItemFavorites().setSystemOrangeCount(myListEntry.getSelectionLevel());
                                break;
                            default:
                                break;
                        }
                    } else {
                        if(myListEntry.getSelectionLevel() == 1) {
                            wordEntry.getItemFavorites().addToUserListCount(1);
                        } else {
                            wordEntry.getItemFavorites().subtractFromUserListCount(1);
                        }
                    }

                    if(wordEntry.getItemFavorites().shouldOpenFavoritePopup(activeFavoriteStars)
                            && wordEntry.getItemFavorites().systemListCount(activeFavoriteStars) >1) {
                        imgStar.setColorFilter(null);
                        imgStar.setImageResource(R.drawable.ic_star_multicolor);

                    } else {
                        imgStar.setImageResource(R.drawable.ic_star_black);
                        try {
                            imgStar.setColorFilter(ContextCompat.getColor(getContext(),FavoritesColors.assignStarColor(wordEntry.getItemFavorites(),activeFavoriteStars)));
                        } catch (NullPointerException e) {
                            Log.e(TAG,"showFavoriteListPopupWindow Nullpointer error setting star color filter in word detail popup dialog... Need to assign item favorites to WordEntry(?)" + e.getCause());
                        }
                    }

                }

            }

        });


        popupWindow.showAsDropDown(imgStar,-xadjust,-yadjust);

    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (WordEntryFavoritesChangedListener) getTargetFragment();
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
        outState.putString("mCursorString",mCursorString);
    }

}

