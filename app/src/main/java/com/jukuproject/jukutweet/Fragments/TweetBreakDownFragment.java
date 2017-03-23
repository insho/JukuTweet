package com.jukuproject.jukutweet.Fragments;
/**
 * Created by JClassic on 2/25/2017.
 */


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.TweetBreakDownAdapter;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.ParseSentenceItem;
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.Models.WordLoader;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SentenceParser;

import java.util.ArrayList;

import rx.functions.Action1;


/**
 * Created by Joe on 11/21/2015.
 */

public class TweetBreakDownFragment extends Fragment  implements View.OnTouchListener {

    String TAG = "TEST-breakdownpop";
    private FragmentInteractionListener mCallback;
//    private Context mContext;
//    private View mAnchorView;
    private RxBus mRxBus = new RxBus();
    private Tweet mTweet;
    private RecyclerView mRecyclerView;
//    private View anchorView;

    private ColorThresholds colorThresholds;
    private ArrayList<String> activeFavoriteStars;

    private WordLoader wordLoader;

//    private ArrayList<String> colors = new ArrayList<String>();
//    private int totalactivelists;

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


//    public TweetBreakDownFragment(Context context, View anchorView, Integer screenheight,RxBus rxBus, Tweet tweet) {
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
//        anchorView = container;
//        popupView = inflater.inflate(R.layout.popup_sentencebreakdown_main, container, false);
        mTweet = getArguments().getParcelable("tweet");
        mRecyclerView = (RecyclerView) popupView.findViewById(R.id.parseSentenceRecyclerView);
        baseLayout = popupView.findViewById(R.id.popuptab_layout);
        mSentence =  (TextView) popupView.findViewById(R.id.sentence);
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(getContext());
        colorThresholds = sharedPrefManager.getColorThresholds();
        activeFavoriteStars = sharedPrefManager.getActiveFavoriteStars();
        return popupView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        baseLayout.setOnTouchListener(this);

        //TODO REPLACE THIS WITH PREFERENCES SET
//        colors.add("Blue");
//        colors.add("Red");
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

        RecyclerView.Adapter mAdapter = new TweetBreakDownAdapter(getContext(),kanjiEntriesInTweet,colorThresholds,activeFavoriteStars,mRxBus);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRxBus.toClickObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {

                        //TODO MOVE THIS METHOD TO THE FRAGMENT, AND ONLY CALL BACK TO MAIN ACTIVITY???
                        //TODO OR only if there is no userinfo, fill that shit in. otherwise dont
                        if(isUniqueClick(1000) && event instanceof Integer) {
                            Integer kanjiID = (Integer) event;

                            //TODO - toggle the star, or open the star popup
                        }

                    }

                });
        mRxBus.toLongClickObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if(isUniqueClick(1000) && event instanceof Integer) {
                            Integer kanjiID = (Integer) event;

                            //TODO - open the star popup
                        }

                    }

                });

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











//    public void updateStarColor(ImageButton favoritesStar, WordEntry wordEntry) {
//        /** Deal with the color star update */
//        favoritesStar.setImageResource(R.drawable.ic_star_black);
//
//        if (mColorsHash.containsKey(PKey)) {
//            /** Pull the lists that contain the  _id for that kanji  */
//            ArrayList<String> colorListNameArray  = mColorsHash.get(PKey).get(0);
//            ArrayList<String> totalListCountArray = mColorsHash.get(PKey).get(1);
//
//            if(debug) {
//                for (int xxx = 0; xxx < totalListCountArray.size(); xxx++) {
//                    Log.d(TAG, "otherarray item " + xxx + ": " + totalListCountArray.get(xxx));
//                }
//            };
//
//            int totalListCount = 0;
//            if (totalListCountArray.size() > 0) {
//                totalListCount = Integer.parseInt(totalListCountArray.get(0)); // all lists that the word is included in
//            }
//            String color1;
//
//            if(debug){
//                Log.d(TAG, PKey + " - colorsarray size : " + colorListNameArray.size());
//                Log.d(TAG,"debug " + totalListCount);
//                Log.d(TAG,"mcolors: " + mcolors);
//                Log.d(TAG, "PKey = " + PKey + " = initial color = " + startingstate_inner);
//            }
//
//            /** If the _id is in more than one color list, fill the star icon with the first 2 colors  */
//            if (colorListNameArray.size() > 1) {
//                if(debug){Log.d(TAG, "xvx set MULTICOLOR!");}
//                holder.imgStar.setColorFilter(null);
//                holder.imgStar.setImageResource(R.drawable.ic_star_multicolor);
//                startingstate_inner = 5; // MULTICOLOR
//                holder.imgStar.setTag(5);
//
//                if(debug){Log.d(TAG, "starting color is now set to 5");}
//
//            } else if((totalListCount >0 && colorListNameArray.size()==0) || (mcolors== null || mcolors.size() ==0)){
//                holder.imgStar.setColorFilter(null);
//                holder.imgStar.setImageResource(R.drawable.ic_star_black);
//                startingstate_inner = 5; // MULTICOLOR
//                holder.imgStar.setTag(5);
//
//            } else if (colorListNameArray.size() == 1) {
//
//                if(debug){Log.d(TAG, "PKey = " + PKey + " - We're here somehow (in the one arraything)");};
//                /** If it's in one color list, fill the star icon with that color */
//                color1 = colorListNameArray.get(0);
//                switch (color1.toLowerCase()) {
//                    case "yellow":
//                        holder.imgStar.setColorFilter(ContextCompat.getColor(mActivity, R.color.spinnerYellowColor));
//                        startingstate_inner = 1;
//                        holder.imgStar.setTag(1);
//                        break;
//                    case "blue":
//                        holder.imgStar.setColorFilter(ContextCompat.getColor(mActivity, R.color.Blue_900));
//                        startingstate_inner = 2;
//                        holder.imgStar.setTag(2);
//                        break;
//                    case "red":
//                        holder.imgStar.setColorFilter(ContextCompat.getColor(mActivity, R.color.answerIncorrectColor));
//                        startingstate_inner = 3;
//                        holder.imgStar.setTag(3);
//                        break;
//                    case "green":
//                        holder.imgStar.setColorFilter(ContextCompat.getColor(mActivity, R.color.answerCorrectColor));
//                        startingstate_inner = 4;
//                        holder.imgStar.setTag(4);
//                        break;
//                }
//
//                /** If this value is in multiple lists (user created ones), then make hte starting color = 5*/
//                if (totalListCount >= 1) {
//
//                    if(debug){Log.d(TAG, "PKey = " + PKey + " - We're making it = 5 here...");};
//                    startingstate_inner = 5;
//                    holder.imgStar.setTag(5);
//                }
//
//            } else {
//
//                if(debug){Log.d(TAG, "PKey = " + PKey + " - coloring the star black...");};
//                holder.imgStar.setColorFilter(ContextCompat.getColor(mActivity, R.color.Black));
//                startingstate_inner = 0;
//                holder.imgStar.setTag(0);
//            }
//
//        } else {
//            if(debug){Log.d(TAG, "xvx set 5 - colors hash doesn't contain PKEy");
//                Log.d(TAG, "PKey = " + PKey + " - it also turned BLACK...");
//            }
//            holder.imgStar.setImageResource(R.drawable.ic_star_black);
//            holder.imgStar.setColorFilter(ContextCompat.getColor(mActivity, R.color.Black));
//            startingstate_inner = 0;
//            holder.imgStar.setTag(0);
//        }
//
//        if(debug) {
//            Log.d(TAG, "PKey = " + PKey + " = final startstate = " + startingstate_inner);
//            Log.d(TAG, "PKey = " + PKey + " = final tag = " + holder.imgStar.getTag());
//        }
//    }
}

