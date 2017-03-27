package com.jukuproject.jukutweet.Fragments;
/**
 * Created by JClassic on 2/25/2017.
 */


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Adapters.TweetBreakDownAdapter;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.ParseSentenceItem;
import com.jukuproject.jukutweet.Models.ParseSentenceSpecialSpan;
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetUrl;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SentenceParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by Joe on 11/21/2015.
 */

public class TweetBreakDownFragment extends Fragment  implements View.OnTouchListener {

    String TAG = "TEST-breakdownpop";
    private FragmentInteractionListener mCallback;
//    private Context mContext;
//    private View mAnchorView;
//    private RxBus mRxBusTweetBreak= new RxBus();
    private Tweet mTweet;
    private RecyclerView mRecyclerView;
//    private View anchorView;

    /*This is the main linear layout, that we will fill row by row with horizontal linear layouts, which are
     in turn filled with vertical layouts (with furigana on top and japanese on bottom). A big sandwhich of layouts */
    private LinearLayout linearLayoutVerticalMain;
    private LinearLayout linearLayout;
    private int linewidth = 0;
    private  int displaywidth = 0;
    private int displaymarginpadding = 30; //How much to pad the edge of the screen by when laying down the sentenceblocks (so the sentence doesn't overlap the screen or get cut up too much)


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

    private ScrollView mScrollView;
    /* keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;

    public TweetBreakDownFragment() {}

    public static TweetBreakDownFragment newInstance(Tweet tweet) {
        TweetBreakDownFragment fragment = new TweetBreakDownFragment();
        Bundle args = new Bundle();
        args.putParcelable("tweet", tweet);
        fragment.setArguments(args);

        return  fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        popupView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_tweetbreakdown, null);
        baseLayout = popupView.findViewById(R.id.popuptab_layout);
        baseLayout.setOnTouchListener(this);
        mTweet = getArguments().getParcelable("tweet");
        mRecyclerView = (RecyclerView) popupView.findViewById(R.id.parseSentenceRecyclerView);
        baseLayout = popupView.findViewById(R.id.popuptab_layout);
        mSentence =  (TextView) popupView.findViewById(R.id.sentence);
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(getContext());
        colorThresholds = sharedPrefManager.getColorThresholds();
        activeFavoriteStars = sharedPrefManager.getActiveFavoriteStars();
        progressBar = (SmoothProgressBar) popupView.findViewById(R.id.progressbar);
        divider = (View) popupView.findViewById(R.id.dividerview);

        linearLayoutVerticalMain = (LinearLayout) popupView.findViewById(R.id.sentence_layout);
        linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mScrollView = (ScrollView) popupView.findViewById(R.id.scrollView);

        return popupView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);



        mSentence.setVisibility(View.VISIBLE);
        mSentence.setText(mTweet.getText());
        mSentence.setAlpha(.7f);
        //Try to add links
        try {
            List<TweetUrl> tweetUrls =  mTweet.getEntities().getUrls();

            for(TweetUrl url : tweetUrls) {
                int[] indices = url.getIndices();

                String urlToLinkify = "";

                if(mTweet.getText().substring(indices[0]).contains(url.getDisplay_url())) {
                    urlToLinkify = url.getDisplay_url();
                } else if(mTweet.getText().substring(indices[0]).contains(url.getUrl())) {
                    urlToLinkify = url.getUrl();
                }
                int startingLinkPos = mTweet.getText().indexOf(urlToLinkify,indices[0]);
                SpannableString text = new SpannableString(mTweet.getText());
                text.setSpan(new URLSpan(url.getUrl()), startingLinkPos, startingLinkPos + urlToLinkify.length(), 0);
                mSentence.setMovementMethod(LinkMovementMethod.getInstance());
                mSentence.setText(text, TextView.BufferType.SPANNABLE);

            }
        } catch (NullPointerException e) {
            Log.e(TAG,"mTweet urls are null : " + e);
        } catch (Exception e) {
            Log.e(TAG,"Error adding url info: " + e);
        }


        final String sentence = mTweet.getText();
        final ArrayList<ParseSentenceSpecialSpan> specialSpans = new ArrayList<>();

        /* Create a list of "SpecialSpan" objects from the urls contained the Tweet. These will be
        * passed into the SentenceParser, but will skip all the "parsing" and will be reintegrated at the end, and
        * designated as "url" in the ParseSentenceItems that emerge from the parser. They can then be shown as clickable links when
        * the tweet text is reassembled */
        for(TweetUrl url : mTweet.getEntities().getUrls()) {
            if(sentence.substring(url.getIndices()[0]).contains(url.getDisplay_url())) {
                specialSpans.add(new ParseSentenceSpecialSpan("url",url.getDisplay_url(),sentence));
            } else if(mTweet.getText().substring(url.getIndices()[0]).contains(url.getUrl())) {
                specialSpans.add(new ParseSentenceSpecialSpan("url",url.getUrl(),sentence));
            }
        }



        Single<ArrayList<ParseSentenceItem>> disectTweet = Single.fromCallable(new Callable<ArrayList<ParseSentenceItem>>() {
            @Override
            public ArrayList<ParseSentenceItem> call() throws Exception {
                InternalDB helper = InternalDB.getInstance(getContext());
                SQLiteDatabase db = helper.getReadableDatabase();

                return SentenceParser.getInstance().parseSentence(sentence
                        ,db
                        ,specialSpans
                        ,helper.getWordLists(db)
                        ,colorThresholds);
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
                        mSentence.setVisibility(View.GONE);
//                        mSentence.setAlpha(1.0f);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG,"ERROR IN PARSE SENTENCE OBSERVABLE: " + error);
                        showProgressBar(false);
                        linearLayoutVerticalMain.setVisibility(View.GONE);
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

//
//    /**
//     * Checks how many milliseconds have elapsed since the last time "mLastClickTime" was updated
//     * If enough time has elapsed, returns True and updates mLastClickTime.
//     * This is to stop unwanted rapid clicks of the same button
//     * @param elapsedMilliSeconds threshold of elapsed milliseconds before a new button click is allowed
//     * @return bool True if enough time has elapsed, false if not
//     */
//    public boolean isUniqueClick(int elapsedMilliSeconds) {
//        if(SystemClock.elapsedRealtime() - mLastClickTime > elapsedMilliSeconds) {
//            mLastClickTime = SystemClock.elapsedRealtime();
//            return true;
//        } else {
//            return false;
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

    public void loadArray(ArrayList<ParseSentenceItem> disectedTweet) {

         /* Get metrics to pass density/width/height to adapters */
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        displaywidth = metrics.widthPixels;
        displaymarginpadding =  (int)((float)(displaywidth)*0.055555556);



        ArrayList<WordEntry> kanjiEntriesInTweet = new ArrayList<>();
        for(ParseSentenceItem parseSentenceItem : disectedTweet) {
            if(BuildConfig.DEBUG) {
                Log.d(TAG,"OUTPUT ITEM: " + parseSentenceItem.getKanjiConjugated());
            }
             /* Pull only the kanji items out of the list, to populate the TweetBreakDownAdapter*/
            if(parseSentenceItem.isKanji() && parseSentenceItem.getWordEntry() != null) {
                kanjiEntriesInTweet.add(parseSentenceItem.getWordEntry());
            }



            /* Compile a clickable/browsable version of the tweet into the linearlayout_main,
            * by stringing together the entries in "disectedTweet"*/
            if(parseSentenceItem.getKanjiConjugated().contains(System.getProperty("line.separator"))) {
                ArrayList<ParseSentenceItem> subArray = new ArrayList<>();
                String text = parseSentenceItem.getKanjiConjugated();
                int startIndex = 0;
                while (startIndex < text.length()) {
                    int endIndex;
                    Log.d(TAG,"while start: " + startIndex + ", sentlength: " + text.length());
                    if(text.substring(startIndex,text.length()).contains(System.getProperty("line.separator"))) {
                        Log.d(TAG,"BOO");
                        endIndex = startIndex + text.substring(startIndex,text.length()).indexOf(System.getProperty("line.separator"));
                        Log.d(TAG,"BOO2");

                        subArray.add(new ParseSentenceItem(false,0,text.substring(startIndex,endIndex),null));
                        Log.d(TAG,"ADDING: " + text.substring(startIndex,endIndex));
                        subArray.add(new ParseSentenceItem(false,0,System.getProperty("line.separator"),null));

                        startIndex = endIndex + 1;
                        Log.d(TAG,"ADDING: Line seperatore, new startIndex: " + startIndex);
                    } else {
//                        endIndex = text.length();
                        Log.d(TAG,"NEW subarray: Line seperatore, new startIndex: " + startIndex + ", end: " + text.length() );
                        subArray.add(new ParseSentenceItem(false,0,text.substring(startIndex,text.length()),null));
                        startIndex = text.length() ;
                        Log.d(TAG,"ADDING: Line seperatore, new startIndex: " + startIndex);
                    }
                }

                for(ParseSentenceItem item : subArray) {
                    addToLayout(item);
                }


            } else {
                addToLayout(parseSentenceItem);
            }
        }

        /**Handle the last stragling remains of the thing*/
        if (linearLayout.getChildCount() > 0) {
            if(BuildConfig.DEBUG) {Log.d(TAG, "inserting last line");}
            linearlayoutInsert(2, 1); // insert the last line on a new line
        }


        RecyclerView.Adapter mAdapter = new TweetBreakDownAdapter(getContext(),metrics,kanjiEntriesInTweet,colorThresholds,activeFavoriteStars);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setVerticalScrollBarEnabled(true);
        mRecyclerView.setMinimumHeight(metrics.heightPixels/2);
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


    public void addToLayout(final ParseSentenceItem parseSentenceItem) {
        String onScreenText = parseSentenceItem.getKanjiConjugated();
        String onScreenFurigana = parseSentenceItem.getFuriganaClean();

        if(parseSentenceItem.getKanjiConjugated().equals(System.getProperty("line.separator"))) {
            //Log 2 rows, once to input the remaining current layout items,
            //and another black row for the seperator
            if(linewidth>0) {
                linearlayoutInsert(2, 1);
            }
            linearlayoutInsert(2, 1);

        } else if(parseSentenceItem.isKanji()) {

                /* INPUT KANJI */
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "INPUT KANJI " + parseSentenceItem.getKanjiConjugated());
            }
            TextView textView_Test = new TextView(getContext());
            textView_Test.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            textView_Test.setText(onScreenText);
            textView_Test.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);

//            TextView textView_Test_Furigana = new TextView(getContext());
//            textView_Test_Furigana.setLayoutParams(new ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT));
//
//            textView_Test_Furigana.setText(onScreenFurigana);
//            textView_Test_Furigana.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

            Rect bounds = new Rect();
            Paint textPaint = textView_Test.getPaint();
            textPaint.getTextBounds(onScreenText, 0, onScreenText.length(), bounds);

//            Paint textPaint_Furigana;
//            if (onScreenFurigana != null) {
//                textPaint_Furigana = textView_Test_Furigana.getPaint();
//                textPaint_Furigana.getTextBounds(onScreenFurigana, 0, onScreenFurigana.length(), bounds);
//                if (BuildConfig.DEBUG) {
//                    Log.d(TAG, "measureText WIDTH (furigana) = " + Math.round(textPaint_Furigana.measureText(onScreenFurigana)));
//                }
//            } else {
//                textPaint_Furigana = textPaint;
//                onScreenFurigana = "";
//            }
            int width = bounds.width();

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "REGULAR WIDTH  = " + width);
                Log.d(TAG, "REGULAR FURIGANA WIDTH  = " + width);
                Log.d(TAG, "measureText WIDTH = " + Math.round(textPaint.measureText(onScreenText)));
            }

//            if (Math.round(textPaint.measureText(onScreenText)) > Math.round(textPaint_Furigana.measureText(onScreenFurigana))) {
                width = Math.round(textPaint.measureText(onScreenText));
//            } else {
//                width = Math.round(textPaint_Furigana.measureText(onScreenFurigana));
//            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "FINAL WIDTH = " + width);
            }

            int widthExtra = (linewidth + width + displaymarginpadding) - displaywidth;
            int maxWidthAllowed = displaywidth - linewidth - displaymarginpadding;

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "widthExtra = " + widthExtra);
                Log.d(TAG, "maxWidthAllowed= " + maxWidthAllowed);
            }

            if (widthExtra > 0) {
                linearlayoutInsert(2, 1);
            }

            LinearLayout innerLinearLayout3 = new LinearLayout(getContext());
            innerLinearLayout3.setOrientation(LinearLayout.VERTICAL);
            TextView textView = new TextView(getContext());
//            final TextView textView_furigana = new TextView(getContext());

            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

//            textView_furigana.setLayoutParams(new ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT));

            textView.setText(onScreenText);
            textView.setTextSize(24);
                textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));

//            textView_furigana.setText(onScreenFurigana);
//            textView_furigana.setTextSize(12);
////                textView_furigana.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
//            textView_furigana.setVisibility(TextView.INVISIBLE);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (textView_furigana.getVisibility() == TextView.VISIBLE) {
//                        textView_furigana.setVisibility(TextView.INVISIBLE);
//                    } else {
//                        textView_furigana.setVisibility(TextView.VISIBLE);
//                    }
                    Toast.makeText(getActivity(), "" + parseSentenceItem.getFuriganaClean(), Toast.LENGTH_SHORT).show();

                }
            });


//            textView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    WordDetailPopupWindow x = new WordDetailPopupWindow(FillInTheBlanks.this, v, kanjiID);
//                    x.CreateView();
//                    return true;
//                }
//            });


//            innerLinearLayout3.addView(textView_furigana);
            innerLinearLayout3.addView(textView);
            innerLinearLayout3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(innerLinearLayout3);

            linewidth = linewidth + width;


        } else {

            /** INPUT THE NORMAL WORD STRINGS */

            if(BuildConfig.DEBUG) {Log.d(TAG, "INPUT REGULAR:  " + onScreenText);}
            TextView textView_Test = new TextView(getContext());
            textView_Test.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            textView_Test.setText(onScreenText);
            textView_Test.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
//                textView_Test.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));

            Rect bounds = new Rect();
            Paint textPaint = textView_Test.getPaint();
            textPaint.getTextBounds(onScreenText, 0, onScreenText.length(), bounds);
            int height = bounds.height();
            int width = bounds.width();

            if(BuildConfig.DEBUG) {
                Log.d(TAG, "Prospective Height = " + height);
                Log.d(TAG, "REGULAR WIDTH  = " + width);
                Log.d(TAG, "measureText WIDTH = " + textPaint.measureText(onScreenText));
            }

            width = Math.round(textPaint.measureText(onScreenText));

            if(BuildConfig.DEBUG) {
                Log.d(TAG, "FINAL WIDTH = " + width);
                Log.d(TAG, "onScreenText content = " + onScreenText);
                Log.d(TAG, "current linewidth = " + linewidth);
                Log.d(TAG, "onScreenText linewidth = " + width);
            }

            int widthExtra = (linewidth + width + displaymarginpadding) - displaywidth;
            int maxWidthAllowed = displaywidth - linewidth - displaymarginpadding;

            if(BuildConfig.DEBUG) {
                Log.d(TAG, "widthExtra = " + widthExtra);
                Log.d(TAG, "maxWidthAllowed= " + maxWidthAllowed);
            }

            if (widthExtra > 0) {
                int substringstart = 0;
                int currentendpoint = Math.round(((float) maxWidthAllowed / (float) width) * onScreenText.length());
                if(BuildConfig.DEBUG) {Log.d(TAG, "curentendpoint: " + currentendpoint);}
                int substringend = currentendpoint;
                if (maxWidthAllowed > width || maxWidthAllowed < 0) {
                    substringend = onScreenText.length();
                    if(BuildConfig.DEBUG) {Log.d(TAG, "Substring end is the whole onScreenText");}
                }

                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "substringstart: " + substringstart);
                    Log.d(TAG, "substringend: " + substringend);
                }

                while (widthExtra > 0) {

                    if(BuildConfig.DEBUG) {Log.d(TAG, "max width allowed: " + maxWidthAllowed);}

                    String choppedTextFragment = onScreenText.substring(substringstart, substringend);
                    if(BuildConfig.DEBUG) {Log.d(TAG, "ChoppedFragment: " + choppedTextFragment);}
                    TextView textView = new TextView(getContext());
//                    TextView textView_furigana = new TextView(getContext());


                    /** INSERTING THE TEXT BOXES INTO THE INNER LINEAR LAYOUT */

                    LinearLayout innerLinearLayout3 = new LinearLayout(getContext());
                    innerLinearLayout3.setOrientation(LinearLayout.VERTICAL);

                    textView.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

//                    textView_furigana.setLayoutParams(new ViewGroup.LayoutParams(
//                            ViewGroup.LayoutParams.WRAP_CONTENT,
//                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    textView.setText(choppedTextFragment);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
//                        textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));

//                    textView_furigana.setText(choppedTextFragment);
//                    textView_furigana.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
////                        textView_furigana.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
//                    textView_furigana.setVisibility(TextView.INVISIBLE);
//                    innerLinearLayout3.addView(textView_furigana);
                    innerLinearLayout3.addView(textView);
                    innerLinearLayout3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    linearLayout.addView(innerLinearLayout3);


                    /** NEED TO FIX THIS SO IF substringend== substringstart (AND IT'S ALREADY VERY NEAR THE END OF A ROW (OR OVER IT), YOU
                     * 1. MAKE A NEW ROW
                     * 2. BUT DON'T TRY TO INPUT THE NONEXISTANT FRAGMENT, RESET THE METER AND THROW A NEW LARGER FRAGMENT IN THERE...*/
                    if (substringend < substringstart) {
                        linearlayoutInsert(2, 0); //DONT LOG A NEW ROW
                    } else {

                        linearlayoutInsert((linewidth + width + displaymarginpadding), displaywidth);
                    }


                    widthExtra = widthExtra - maxWidthAllowed;
                    if(BuildConfig.DEBUG) {Log.d(TAG, "NEW widthExtra: " + widthExtra);}
                    substringstart = substringend;

                    if ((linewidth + width + displaymarginpadding) < displaywidth || widthExtra < 0) {
                        substringend = onScreenText.length();

                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "choppedfragment substringstart: " + substringstart);
                            Log.d(TAG, "choppedfragment substringend: " + substringend);
                        }
                        String choppedTextFragmentRemainder = onScreenText.substring(substringstart, substringend);
                        if(BuildConfig.DEBUG) {Log.d(TAG, "choppedTextFragmentRemainder: " + choppedTextFragmentRemainder);}
                        TextView textViewRemainder = new TextView(getContext());
//                        TextView textView_furiganaRemainder = new TextView(getContext());

                        LinearLayout innerLinearLayout3Remainder = new LinearLayout(getContext());
                        innerLinearLayout3Remainder.setOrientation(LinearLayout.VERTICAL);

                        textViewRemainder.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));

//                        textView_furiganaRemainder.setLayoutParams(new ViewGroup.LayoutParams(
//                                ViewGroup.LayoutParams.WRAP_CONTENT,
//                                ViewGroup.LayoutParams.WRAP_CONTENT));

                        textViewRemainder.setText(choppedTextFragmentRemainder);
                        textViewRemainder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
//                            textViewRemainder.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));

                        Rect bounds2 = new Rect();
                        Paint textPaint2 = textViewRemainder.getPaint();
                        textPaint2.getTextBounds(choppedTextFragmentRemainder, 0, choppedTextFragmentRemainder.length(), bounds2);

                        int width_chopped = bounds2.width();

//                        textView_furiganaRemainder.setText(choppedTextFragmentRemainder);
//                        textView_furiganaRemainder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
////                            textView_furiganaRemainder.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
//                        textView_furiganaRemainder.setVisibility(TextView.INVISIBLE);
//                        innerLinearLayout3Remainder.addView(textView_furiganaRemainder);
                        innerLinearLayout3Remainder.addView(textViewRemainder);
                        innerLinearLayout3Remainder.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        linearLayout.addView(innerLinearLayout3Remainder);


                        linewidth = linewidth + width_chopped;

                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "chopped linewidth: " + linewidth);
                            Log.d(TAG, "chopped width: " + width);
                        }

                        widthExtra = (linewidth + displaymarginpadding) - displaywidth;
                        maxWidthAllowed = displaywidth - linewidth - displaymarginpadding;


                        if (linewidth == 0 && widthExtra > 0) {  // like if it's the last fragment of a line, starting on a new line. Just print the damn thing (on the new line)
                            linearlayoutInsert((linewidth + (displaywidth + widthExtra) + displaymarginpadding), displaywidth);
                        } else if (linewidth == 0 && widthExtra < 0) {
                            linearlayoutInsert((linewidth + (displaywidth + widthExtra) + displaymarginpadding), displaywidth);
                        } else {
                            linearlayoutInsert((linewidth + displaymarginpadding), displaywidth);
                        }

                    } else {

                        maxWidthAllowed = displaywidth - linewidth - displaymarginpadding;

                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "substringend calculation--maxwidthallowed: " + maxWidthAllowed);
                            Log.d(TAG, "substringend calculation--width: " + width);
                            Log.d(TAG, "substringend calculation--onScreenText.length: " + onScreenText.length());
                        }

                        substringend = Math.round(((float) maxWidthAllowed / (float) width) * onScreenText.length()) + substringstart;
                        if(BuildConfig.DEBUG) {Log.d(TAG, "substring rounding: " + ((float) maxWidthAllowed / (float) width) * onScreenText.length());}
                    }

                    if(BuildConfig.DEBUG) {
                        Log.d(TAG, "NEW substringstart: " + substringstart);
                        Log.d(TAG, "NEW substringend: " + substringend);
                    }
                    if (substringend > onScreenText.length()) {
                        if(BuildConfig.DEBUG) {Log.d(TAG, "SHIT WORKAROUND substringend Revised to: " + substringend);}
                        substringend = onScreenText.length();
                    }

                }

            } else {
//                TextView textView_furigana = new TextView(getContext());

                LinearLayout innerLinearLayout3 = new LinearLayout(getContext());
                innerLinearLayout3.setOrientation(LinearLayout.VERTICAL);

                textView_Test.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

//                textView_furigana.setLayoutParams(new ViewGroup.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT));

                textView_Test.setText(onScreenText);
                textView_Test.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
//                    textView_Test.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));

//                textView_furigana.setText(onScreenText);
//                textView_furigana.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
////                    textView_furigana.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
//                textView_furigana.setVisibility(TextView.INVISIBLE);
//                innerLinearLayout3.addView(textView_furigana);
                innerLinearLayout3.addView(textView_Test);
                innerLinearLayout3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(innerLinearLayout3);
                linewidth = linewidth + width;
                linearlayoutInsert((linewidth + displaymarginpadding), displaywidth);

                if(BuildConfig.DEBUG) {Log.d(TAG, "new linewidth = " + linewidth);}

            }

        }
    }

    public void linearlayoutInsert(int totallinewidth, int displaywidth) {

        if (displaywidth == 0) {
            linewidth = 0;
        } else if (totallinewidth >= displaywidth) {

            LinearLayout tmplinearLayout = linearLayout;
            linearLayoutVerticalMain.addView(tmplinearLayout);

            //now reset the linear layout... (hope that works)
            linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linewidth = 0;
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "BIGLAYOUT childcount: " + linearLayoutVerticalMain.getChildCount());
                Log.d(TAG, "linearlayoutInsert Completed");
            }
        } else {
            if(BuildConfig.DEBUG){Log.d(TAG, "linearlayoutInset Unnecessary");}
        }
    }

}

