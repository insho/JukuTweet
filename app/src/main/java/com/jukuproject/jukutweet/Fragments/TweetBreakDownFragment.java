package com.jukuproject.jukutweet.Fragments;
/**
 * Created by JClassic on 2/25/2017.
 */


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.TweetBreakDownAdapter;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.ParseSentenceItem;
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SentenceParser;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by Joe on 11/21/2015.
 */

public class TweetBreakDownFragment extends Fragment  implements View.OnTouchListener {

    String TAG = "TEST-breakdownpop";
    private FragmentInteractionListener mCallback;
//    private Context mContext;
//    private View mAnchorView;
    private RxBus mRxBusTweetBreak= new RxBus();
    private Tweet mTweet;
    private RecyclerView mRecyclerView;
//    private View anchorView;

    private ColorThresholds colorThresholds;
    private ArrayList<String> activeFavoriteStars;
    private SmoothProgressBar progressBar;
    private View divider;

    private TextView mSentence;
    private View baseLayout;
    private View popupView;
    private PopupWindow popupWindow;
    private int previousFingerPosition = 0;
    private int baseLayoutPosition = 0;
    private int defaultViewHeight;
    private boolean isClosing = false;
    private boolean isScrollingUp = false;
    private boolean isScrollingDown = false;

    /* keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;

    public TweetBreakDownFragment() {}

    public static TweetBreakDownFragment newInstance(Tweet tweet) {
        TweetBreakDownFragment fragment = new TweetBreakDownFragment();
        Bundle args = new Bundle();
        args.putParcelable("tweet", tweet);
        fragment.setArguments(args);
        return new TweetBreakDownFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        popupView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_tweetbreakdown, null);
        baseLayout = popupView.findViewById(R.id.popuptab_layout);
        baseLayout.setOnTouchListener(this);
//        anchorView = container;
//        popupView = inflater.inflate(R.layout.fragment_tweetbreakdown, container, false);
        mTweet = getArguments().getParcelable("tweet");
        mRecyclerView = (RecyclerView) popupView.findViewById(R.id.parseSentenceRecyclerView);
        baseLayout = popupView.findViewById(R.id.popuptab_layout);
        mSentence =  (TextView) popupView.findViewById(R.id.sentence);
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(getContext());
        colorThresholds = sharedPrefManager.getColorThresholds();
        activeFavoriteStars = sharedPrefManager.getActiveFavoriteStars();
        progressBar = (SmoothProgressBar) popupView.findViewById(R.id.progressbar);
        divider = (View) popupView.findViewById(R.id.dividerview);
        return popupView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSentence.setText(mTweet.getText());


        Single<ArrayList<ParseSentenceItem>> disectTweet = Single.fromCallable(new Callable<ArrayList<ParseSentenceItem>>() {

            @Override
            public ArrayList<ParseSentenceItem> call() throws Exception {
                InternalDB helper = InternalDB.getInstance(getContext());
                SQLiteDatabase db = helper.getReadableDatabase();

                return SentenceParser.getInstance().parseSentence(mTweet.getText()
                        ,db
                        ,new ArrayList<Integer>()
                        ,new ArrayList<String>()
                        ,helper.getWordLists(db)
                        ,colorThresholds
                        ,activeFavoriteStars);
            }
        });

        showProgressBar(true);
        disectTweet.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<ArrayList<ParseSentenceItem>>() {

                    @Override
                    public void onSuccess(ArrayList<ParseSentenceItem> disectedTweet) {
                        loadArray(disectedTweet);
                        showProgressBar(false);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG,"ERROR IN PARSE SENTENCE OBSERVABLE");
                        showProgressBar(false);
                    }
                });



    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }


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

                popupWindow.dismiss();
                mCallback.onBackPressed();
                if(BuildConfig.DEBUG){Log.d(TAG,") Blue: popupWindow.dismiss A");}

                //reset the position variables
                previousFingerPosition = 0;
                baseLayoutPosition = 0;
                isClosing = false;
                isScrollingUp = false;
                isScrollingDown = false;

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

                popupWindow.dismiss();

                //reset the position variables
                previousFingerPosition = 0;
                baseLayoutPosition = 0;
                isClosing = false;
                isScrollingUp = false;
                isScrollingDown = false;
            }

        });

        positionAnimator.start();

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
            mCallback = (FragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    public void loadArray(ArrayList<ParseSentenceItem> disectedTweet) {
        ArrayList<WordEntry> kanjiEntriesInTweet = new ArrayList<>();
        for(ParseSentenceItem parseSentenceItem : disectedTweet) {
            if(parseSentenceItem.isKanji() && parseSentenceItem.getWordEntry() != null) {
                kanjiEntriesInTweet.add(parseSentenceItem.getWordEntry());
            }
        }


        /** Get width of screen */
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);


        RecyclerView.Adapter mAdapter = new TweetBreakDownAdapter(getContext(),mRxBusTweetBreak,metrics.density,kanjiEntriesInTweet,colorThresholds,activeFavoriteStars);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRxBusTweetBreak.toClickObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {

                        /* Recieve a MyListEntry (containing an updated list entry for this row kanji) from
                        * the ChooseFavoritesAdapter in the ChooseFavorites popup window */
                        if(event instanceof MyListEntry) {
                            MyListEntry myListEntry = (MyListEntry) event;
                            Log.d(TAG,"MylistEntry name: " + myListEntry.getListName());
                            Log.d(TAG,"MylistEntry sys: " + myListEntry.getListsSys());
                            Log.d(TAG,"MylistEntry selectionlevel: " + myListEntry.getSelectionLevel());

//                            Log.d(TAG,"Word entry favs test output: " + mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().testOutput());
//
//                            /*Ascertain the type of list that the kanji was added to (or subtracted from),
//                              and update that list's count */
//                            if(myListEntry.getListsSys() == 1) {
//                                switch (myListEntry.getListName()) {
//                                    case "Blue":
//
//                                        Log.d(TAG,"OLD blue count: " + mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().getSystemBlueCount());
//                                        Log.d(TAG,"OLD should open popup: " + wordEntry.getWordEntryFavorites().shouldOpenFavoritePopup());
//                                        Log.d(TAG,"mylist selection level: " + myListEntry.getSelectionLevel());
//
//                                        mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().setSystemBlueCount(myListEntry.getSelectionLevel());
//                                        Log.d(TAG,"NEW blue count: " + mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().getSystemBlueCount());
//                                        Log.d(TAG,"NEW should open popup: " + wordEntry.getWordEntryFavorites().shouldOpenFavoritePopup());
//                                        break;
//                                    case "Green":
//                                        mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().setSystemGreenCount(myListEntry.getSelectionLevel());
//                                        break;
//                                    case "Red":
//                                        mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().setSystemRedCount(myListEntry.getSelectionLevel());
//                                        break;
//                                    case "Yellow":
//                                        mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().setSystemYellowCount(myListEntry.getSelectionLevel());
//                                        break;
//                                    default:
//                                        break;
//                                }
//                            } else {
//                                if(myListEntry.getSelectionLevel() == 1) {
//                                    mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().addToUserListCount(1);
//                                } else {
//                                    mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().subtractFromUserListCount(1);
//                                }
//                            }
//                            mAdapter.assignStarColor(mWords.get(holder.getAdapterPosition()),holder.imgStar);
//


                        }

                    }

                });
//
//        mRxBus.toClickObserverable()
//                .subscribe(new Action1<Object>() {
//                    @Override
//                    public void call(Object event) {
//
//                        //TODO MOVE THIS METHOD TO THE FRAGMENT, AND ONLY CALL BACK TO MAIN ACTIVITY???
//                        //TODO OR only if there is no userinfo, fill that shit in. otherwise dont
//                        if(isUniqueClick(1000) && event instanceof Integer) {
//                            Integer kanjiId = (Integer) event;
//
//
//                        }
//
//                    }
//
//                });
//        mRxBus.toLongClickObserverable()
//                .subscribe(new Action1<Object>() {
//                    @Override
//                    public void call(Object event) {
//                        if(isUniqueClick(1000) && event instanceof Integer) {
//                            Integer kanjiID = (Integer) event;
//
//                            //TODO - open the star popup
//                        }
//
//                    }
//
//                });
//

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setVerticalScrollBarEnabled(true);

        baseLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));


        popupWindow = new PopupWindow(baseLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.getContentView().setFocusableInTouchMode(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
    }

    /**
     * Shows progress bar during API lookups, hides otherwise
     * @param show boolean True for show, False for hide
     */
    public void showProgressBar(Boolean show) {
        if(show) {
            divider.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            divider.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

}

