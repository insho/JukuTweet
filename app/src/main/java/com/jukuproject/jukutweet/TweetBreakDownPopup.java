package com.jukuproject.jukutweet;
/**
 * Created by JClassic on 2/25/2017.
 */


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.TweetBreakDownAdapter;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.ParseSentenceItem;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.Models.WordLoader;

import java.util.ArrayList;


/**
 * Created by Joe on 11/21/2015.
 */

public class TweetBreakDownPopup extends Fragment  implements View.OnTouchListener {

    String TAG = "TEST-breakdownpop";

//    private Context mContext;
//    private View mAnchorView;
    private RxBus mRxBus = new RxBus();
    private Tweet mTweet;
    private RecyclerView mRecyclerView;
    private View anchorView;

    //TODO remove these!
    int greyThreshold = 2;
    float redThreshold = .3f;
    float yellowThreshold = .8f;


    private WordLoader wordLoader;

    private ArrayList<String> colors = new ArrayList<String>();
    private int totalactivelists;

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

    public TweetBreakDownPopup() {}


    public static TweetBreakDownPopup newInstance(Tweet tweet) {
        TweetBreakDownPopup fragment = new TweetBreakDownPopup();
        Bundle args = new Bundle();
        args.putParcelable("tweet", tweet);
        fragment.setArguments(args);
        return new TweetBreakDownPopup();
    }


//    public TweetBreakDownPopup(Context context, View anchorView, Integer screenheight,RxBus rxBus, Tweet tweet) {
//        this.mContext = context;
//        this.mAnchorView = anchorView;
//        this.mRxBus = rxBus;
//        this.mTweet = tweet;
//        this.mScreenHeight = screenheight;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        popupView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_sentencebreakdown_main, null);
        baseLayout = popupView.findViewById(R.id.popuptab_layout);
        baseLayout.setOnTouchListener(this);
        anchorView = container;
//        popupView = inflater.inflate(R.layout.popup_sentencebreakdown_main, container, false);
        mTweet = getArguments().getParcelable("tweet");
        mRecyclerView = (RecyclerView) popupView.findViewById(R.id.parseSentenceRecyclerView);
        baseLayout = popupView.findViewById(R.id.popuptab_layout);
        mSentence =  (TextView) popupView.findViewById(R.id.sentence);

        return popupView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        baseLayout.setOnTouchListener(this);

        //TODO REPLACE THIS WITH PREFERENCES SET
        colors.add("Blue");
        colors.add("Red");
        wordLoader = InternalDB.getInstance(getContext()).getWordLists(null);

        mSentence.setText(mTweet.getText());

        InternalDB helper = InternalDB.getInstance(getContext());
        SQLiteDatabase db = helper.getReadableDatabase();
        ArrayList<ParseSentenceItem> brokenUpTweet = SentenceParser.getInstance().parseSentence(mTweet.getText(),db,new ArrayList<Integer>(),new ArrayList<String>(),wordLoader);

        ArrayList<WordEntry> kanjiEntriesInTweet = new ArrayList<>();
        for(ParseSentenceItem parseSentenceItem : brokenUpTweet) {
            if(parseSentenceItem.isKanji() && parseSentenceItem.getWordEntry() != null) {
                kanjiEntriesInTweet.add(parseSentenceItem.getWordEntry());
            }
        }

        RecyclerView.Adapter mAdapter = new TweetBreakDownAdapter(getContext(),kanjiEntriesInTweet,greyThreshold,redThreshold,yellowThreshold);
//        RecyclerView mRecyclerView  = (RecyclerView) popupView.findViewById(R.id.parseSentenceRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setVerticalScrollBarEnabled(true);

        db.close();
        helper.close();

        baseLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));


        popupWindow = new PopupWindow(baseLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.getContentView().setFocusableInTouchMode(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);


        //TODO is this necessary???
//        int location[] = new int[2];
//        anchorView.getLocationOnScreen(new int[2]);
//        popupWindow.showAtLocation(baseLayout, Gravity.NO_GRAVITY, 0, 180);
//        int location[] = new int[2];
//
//        anchorView.getLocationOnScreen(location);
//        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, 0, 180);
//        return popupView;
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




}

