package com.jukuproject.jukutweet.Fragments;
/**
 * Created by JClassic on 2/25/2017.
 */


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Adapters.TweetBreakDownAdapter;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.FavoritesColors;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetUrl;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SentenceParserTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

//import com.jukuproject.jukutweet.Models.ParseSentenceItem;
//import com.jukuproject.jukutweet.Models.ParseSentenceSpecialSpan;
//import com.jukuproject.jukutweet.Models.TweetKanjiColor;
//import com.jukuproject.jukutweet.SentenceParser;


/**
 * Created by Joe on 11/21/2015.
 */

public class TweetBreakDownFragment extends Fragment {

    String TAG = "TEST-breakdownpop";
    private FragmentInteractionListener mCallback;
//    private Context mContext;
//    private View mAnchorView;
//    private RxBus mRxBusTweetBreak= new RxBus();
    private Tweet mTweet;
    private RecyclerView mRecyclerView;
    private ArrayList<WordEntry> mDisectedTweet;
    private boolean mSavedTweet = false;
    /*This is the main linear layout, that we will fill row by row with horizontal linear layouts, which are
     in turn filled with vertical layouts (with furigana on top and japanese on bottom). A big sandwhich of layouts */
    private LinearLayout linearLayoutVerticalMain;
    private LinearLayout linearLayout;
    private int linewidth = 0;
    private  int displaywidth = 0;
    private int displaymarginpadding = 30; //How much to pad the edge of the screen by when laying down the sentenceblocks (so the sentence doesn't overlap the screen or get cut up too much)
    private ArrayList<String> mActiveTweetFavoriteStars;
    private Subscription parseSentenceSubscription;

    private ColorThresholds colorThresholds;
    private ArrayList<String> activeFavoriteStars;
    private SmoothProgressBar progressBar;
    private View divider;

    private ArrayList<WordEntry> mDataSet;
    private RecyclerView.Adapter mAdapter;

    private TextView txtSentence;
    private TextView txtNoLists;
    private TextView txtUserName;
    private TextView txtUserScreenName;
    private ImageButton imgStar;
    private FrameLayout imgStarLayout;


//    private ScrollView mScrollView;
    /* keep from constantly recieving button clicks through the RxBus */
//    private long mLastClickTime = 0;

    public TweetBreakDownFragment() {}

    public static TweetBreakDownFragment newInstanceTimeLine(Tweet tweet) {
        TweetBreakDownFragment fragment = new TweetBreakDownFragment();
        Bundle args = new Bundle();
        args.putParcelable("tweet", tweet);
        args.putBoolean("isSavedTweet",false);
        fragment.setArguments(args);

        return  fragment;
    }
    public static TweetBreakDownFragment newInstanceSavedTweet(Tweet tweet) {
        TweetBreakDownFragment fragment = new TweetBreakDownFragment();
        Bundle args = new Bundle();
        args.putParcelable("tweet", tweet);
        args.putBoolean("isSavedTweet",true);
        fragment.setArguments(args);

        return  fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v  = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_tweetbreakdown, null);
        mTweet = getArguments().getParcelable("tweet");
        mSavedTweet = getArguments().getBoolean("isSavedTweet");
        mRecyclerView = (RecyclerView) v.findViewById(R.id.parseSentenceRecyclerView);
        txtSentence =  (TextView) v.findViewById(R.id.sentence);
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(getContext());
        colorThresholds = sharedPrefManager.getColorThresholds();
        mActiveTweetFavoriteStars = SharedPrefManager.getInstance(getContext()).getActiveTweetFavoriteStars();

        activeFavoriteStars = sharedPrefManager.getActiveFavoriteStars();
        progressBar = (SmoothProgressBar) v.findViewById(R.id.progressbar);
        divider = (View) v.findViewById(R.id.dividerview);

        txtUserName = (TextView) v.findViewById(R.id.timelineName);
        txtUserScreenName = (TextView) v.findViewById(R.id.timelineDisplayScreenName);
        imgStar = (ImageButton) v.findViewById(R.id.favorite);
        imgStarLayout = (FrameLayout) v.findViewById(R.id.timelineStarLayout);
        txtNoLists = (TextView) v.findViewById(R.id.nolists);

        linearLayoutVerticalMain = (LinearLayout) v.findViewById(R.id.sentence_layout);
        linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

//        mScrollView = (ScrollView) v.findViewById(R.id.scrollView);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final String sentence = mTweet.getText();

        //Try to fill in user info at the top
        try {
            txtUserName.setText(mTweet.getUser().getName());
            txtUserScreenName.setText(mTweet.getUser().getDisplayScreenName());

        } catch (NullPointerException e) {
            Log.e(TAG,"TweetBreakDownFragment mTweet doesn't contain user?: " + e);
            txtUserName.setVisibility(View.INVISIBLE);
            txtUserScreenName.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            Log.e(TAG,"TweetBreakDownFragment adding user info error: " + e);
            txtUserName.setVisibility(View.INVISIBLE);
            txtUserScreenName.setVisibility(View.INVISIBLE);
        }

        //If it is a saved tweet (i.e. there are color indexes
        if(mSavedTweet) {
            //Set up the favorites star
            imgStar.setVisibility(View.GONE);
            imgStarLayout.setVisibility(View.GONE);


        } else {

            //Set up the favorites star
            imgStarLayout.setClickable(true);
            imgStarLayout.setLongClickable(true);

            try {
                imgStar.setImageResource(FavoritesColors.assignStarResource(mTweet.getItemFavorites(),mActiveTweetFavoriteStars));
                imgStar.setColorFilter(ContextCompat.getColor(getContext(), FavoritesColors.assignStarColor(mTweet.getItemFavorites(),mActiveTweetFavoriteStars)));

                imgStarLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                /*  1. Change star color favorites star based previous star color
                    2. Check for tweet in db, save if necessary*/
                        InternalDB helper = InternalDB.getInstance(getContext());

                        //Toggle favorite list association for this tweet
                        if(FavoritesColors.onFavoriteStarToggleTweet(getContext(),mActiveTweetFavoriteStars,mTweet.getUser().getUserId(),mTweet)) {
                            imgStar.setImageResource(FavoritesColors.assignStarResource(mTweet.getItemFavorites(),mActiveTweetFavoriteStars));
                            imgStar.setColorFilter(ContextCompat.getColor(getContext(), FavoritesColors.assignStarColor(mTweet.getItemFavorites(),mActiveTweetFavoriteStars)));

                        }

                        //Check for tweet in db
                        try {

                            //If tweet doesn't already exist in db, insert it
                            if(helper.tweetExistsInDB(mTweet) == 0 && mTweet.getUser() != null){

                                int addTweetResultCode = helper.saveTweetToDB(mTweet.getUser(),mTweet);

                                if(addTweetResultCode > 0 && mDisectedTweet != null) {
                                /*DB insert successfull, now save tweet urls and parsed kanji into database */
                                    InternalDB.getInstance(getContext()).saveParsedTweetKanji(mDisectedTweet,mTweet.getIdString());
                                    InternalDB.getInstance(getContext()).saveTweetUrls(mTweet);
                                }
                            }

                        } catch (Exception e){
                            Log.e(TAG,"UserTimeLIneAdapter - star clicked, tweet doesn't exist, but UNABLE to save!");

                        }

                    }
                });
            }  catch (NullPointerException e) {
                Log.e(TAG,"TweetBreakDownFragment setting up imgStar doesn't contain itemfavs??: " + e);
                imgStar.setVisibility(View.GONE);
            } catch (Exception e) {
                Log.e(TAG,"TweetBreakDownFragment setting up imgStar error: " + e);
                imgStar.setVisibility(View.GONE);
            }




        }


        txtSentence.setVisibility(View.VISIBLE);
        txtSentence.setText(mTweet.getText());
        txtSentence.setClickable(true);
        txtSentence.setAlpha(.7f);

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
                txtSentence.setMovementMethod(LinkMovementMethod.getInstance());
                txtSentence.setText(text, TextView.BufferType.SPANNABLE);


            }
        } catch (NullPointerException e) {
            Log.e(TAG,"mTweet urls are null : " + e);
        } catch (Exception e) {
            Log.e(TAG,"Error adding url info: " + e);
        }




    if(mSavedTweet && mTweet.getWordEntries() != null) {

        /*If it is a previously saved tweet, the favorite list information for the WordEntries in the
         tweet will not have been previously attached. So attach them now: */

        for(WordEntry wordEntry : mTweet.getWordEntries()) {
            Cursor c = InternalDB.getInstance(getContext()).getWordEntryForKanjiId(wordEntry.getId(),colorThresholds);
            if(c.getCount()>0) {
                c.moveToFirst();
                wordEntry.setItemFavorites(new ItemFavorites(c.getInt(5)
                        ,c.getInt(6)
                        ,c.getInt(7)
                        ,c.getInt(8)
                        ,c.getInt(9)
                        ,c.getInt(10)
                        ,c.getInt(11)));

                c.close();
            }
        }


        loadSavedArray(mTweet.getWordEntries(),sentence,txtSentence);

    } else if(mDisectedTweet==null) {

//        final ArrayList<ParseSentenceSpecialSpan> specialSpans = new ArrayList<>();
        final ArrayList<String> spansToExclude = new ArrayList<>();

        /* Create a list of "SpecialSpan" objects from the urls contained the Tweet. These will be
        * passed into the SentenceParser, but will skip all the "parsing" and will be reintegrated at the end, and
        * designated as "url" in the ParseSentenceItems that emerge from the parser. They can then be shown as clickable links when
        * the tweet text is reassembled */

        if(mTweet.getEntities() != null && mTweet.getEntities().getUrls() != null) {
            for(TweetUrl url : mTweet.getEntities().getUrls()) {
                if(url != null) {
                    spansToExclude.add(url.getUrl());
                }

                //TODO ExCLUDE ALL HASH TAGS? or maybe not.
//
//                if(sentence.substring(url.getIndices()[0]).contains(url.getDisplay_url())) {
//                    spansToExclude.add(new ParseSentenceSpecialSpan("url",url.getDisplay_url(),sentence));
//                } else if(mTweet.getText().substring(url.getIndices()[0]).contains(url.getUrl())) {
//                    specialSpans.add(new ParseSentenceSpecialSpan("url",url.getUrl(),sentence));
//                }
            }

        }

        /* Parse the tweet */
        Single<ArrayList<WordEntry>> disectTweet = Single.fromCallable(new Callable<ArrayList<WordEntry>>() {
            @Override
            public ArrayList<WordEntry> call() throws Exception {
                return SentenceParserTest.getInstance().parseSentence(getContext()
                        ,sentence
                        ,spansToExclude
                        ,colorThresholds);
            }
        });

        showProgressBar(true);
        parseSentenceSubscription = disectTweet.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<ArrayList<WordEntry>>() {

                    @Override
                    public void onSuccess(ArrayList<WordEntry> disectedTweet) {

                        mDisectedTweet = disectedTweet;
                        if(disectedTweet.size()>0) {

                            /*Hide the filler sentence txtSentenceView, and instead user the items
//                            in disectedTweet to dynamically fill in the sentence with clickable, highlighted kanji, links etc. */
//                            loadArray(disectedTweet);
                            loadSavedArray(disectedTweet,sentence,txtSentence);
                            showProgressBar(false);
//                            txtSentence.setVisibility(View.GONE);

                        } else {
                            /*No results found. Keep the initial sentence in the txtSentence view,
                            and show no sentences found message in place of recyclerview */
                            mRecyclerView.setVisibility(View.GONE);
//                            txtNoLists.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG,"ERROR IN PARSE SENTENCE OBSERVABLE: " + error);
                        showProgressBar(false);
                        linearLayoutVerticalMain.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.GONE);
                        txtNoLists.setVisibility(View.VISIBLE);
                        txtNoLists.setText("Unable to parse tweet");
                        txtNoLists.setTextColor(ContextCompat.getColor(getContext(),android.R.color.holo_red_dark));
                    }
                });


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
    public void loadSavedArray(ArrayList<WordEntry> disectedSavedTweet, String entireSentence, TextView txtSentence ) {

         /* Get metrics to pass density/width/height to adapters */
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        displaywidth = metrics.widthPixels;
        displaymarginpadding =  (int)((float)(displaywidth)*0.055555556);

        /* Set tweet color spans. If the saved Tweet object includes a "colorIndex" object (
        * which comes from the savedTweetKanji table and contains the id, positions and color designation
        * of each kanji in the TWeet), replace the normal Tweet text with colored spans for those kanji */
            try {
                final SpannableStringBuilder sb = new SpannableStringBuilder(entireSentence);

                    for(final WordEntry wordEntry : disectedSavedTweet) {
//                        final ForegroundColorSpan fcs = new ForegroundColorSpan(ContextCompat.getColor(getContext(),color.getColorValue()));
//                        sb.setSpan(fcs, color.getStartIndex(), color.getEndIndex(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                        ClickableSpan kanjiClick = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                Toast.makeText(getContext(), wordEntry.getFurigana(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setColor(ContextCompat.getColor(getContext(),wordEntry.getColorValue()));
                                ds.setUnderlineText(false);
//                                ds.setAlpha(1);

                            }
                        };

                        sb.setSpan(kanjiClick, wordEntry.getStartIndex(), wordEntry.getEndIndex(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                txtSentence.setText(sb);
//                holder.txtTweet.setText(text, TextView.BufferType.SPANNABLE);
                txtSentence.setMovementMethod(LinkMovementMethod.getInstance());

            } catch (NullPointerException e) {
                txtSentence.setText(entireSentence);
                Log.e(TAG,"Tweet color nullpointer failure : " + e);
            } catch (Exception e) {
                txtSentence.setText(entireSentence);
                Log.e(TAG,"Tweet color generic failure: " + e);
            }


        /* The TweetKanjiColor objects contain Edict_ids for the kanji in the tweet, but they
        * do not contain the kanji, definition or word score values for those kanji. This
        * converts the TweetKanjiColor Edic_ids into a single string, passes it
        * */
//        mDataSet = InternalDB.getInstance(getContext()).convertTweetKanjiColorToWordEntry(disectedSavedTweet);
        mAdapter = new TweetBreakDownAdapter(getContext(),metrics,disectedSavedTweet,colorThresholds,activeFavoriteStars);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setVerticalScrollBarEnabled(true);

    }

//    public void loadArray(ArrayList<ParseSentenceItem> disectedTweet) {
//
//         /* Get metrics to pass density/width/height to adapters */
//        DisplayMetrics metrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        displaywidth = metrics.widthPixels;
//        displaymarginpadding =  (int)((float)(displaywidth)*0.055555556);
//
//
//        mDataSet = new ArrayList<>();
//        for(ParseSentenceItem parseSentenceItem : disectedTweet) {
//            if(BuildConfig.DEBUG) {
//                Log.d(TAG,"OUTPUT ITEM: " + parseSentenceItem.getKanjiConjugated());
//            }
//             /* Pull only the kanji items out of the list, to populate the TweetBreakDownAdapter*/
//            if(parseSentenceItem.isKanji() && parseSentenceItem.getWordEntry() != null) {
//                mDataSet.add(parseSentenceItem.getWordEntry());
//            }
//
//            /* Compile a clickable/browsable version of the tweet into the linearlayout_main,
//            * by stringing together the entries in "disectedTweet"
//            *
//            * */
//            if(parseSentenceItem.getKanjiConjugated().contains(System.getProperty("line.separator"))) {
//                ArrayList<ParseSentenceItem> subArray = new ArrayList<>();
//                String text = parseSentenceItem.getKanjiConjugated();
//                int startIndex = 0;
//                while (startIndex < text.length()) {
//                    int endIndex;
//                    Log.d(TAG,"while start: " + startIndex + ", sentlength: " + text.length());
//                    if(text.substring(startIndex,text.length()).contains(System.getProperty("line.separator"))) {
//                        Log.d(TAG,"BOO");
//                        endIndex = startIndex + text.substring(startIndex,text.length()).indexOf(System.getProperty("line.separator"));
//                        Log.d(TAG,"BOO2");
//
//                        subArray.add(new ParseSentenceItem(false,0,text.substring(startIndex,endIndex),null));
//                        Log.d(TAG,"ADDING: " + text.substring(startIndex,endIndex));
//                        subArray.add(new ParseSentenceItem(false,0,System.getProperty("line.separator"),null));
//
//                        startIndex = endIndex + 1;
//                        Log.d(TAG,"ADDING: Line seperatore, new startIndex: " + startIndex);
//                    } else {
//                        Log.d(TAG,"NEW subarray: Line seperatore, new startIndex: " + startIndex + ", end: " + text.length() );
//                        subArray.add(new ParseSentenceItem(false,0,text.substring(startIndex,text.length()),null));
//                        startIndex = text.length() ;
//                        Log.d(TAG,"ADDING: Line seperatore, new startIndex: " + startIndex);
//                    }
//                }
//
//                for(ParseSentenceItem item : subArray) {
//                    addToLayout(item);
//                }
//
//
//            } else {
//                addToLayout(parseSentenceItem);
//            }
//        }
//
//        /**Handle the last stragling remains of the thing*/
//        if (linearLayout.getChildCount() > 0) {
//            if(BuildConfig.DEBUG) {Log.d(TAG, "inserting last line");}
//            linearlayoutInsert(2, 1); // insert the last line on a new line
//        }
//
//        mAdapter = new TweetBreakDownAdapter(getContext(),metrics,mDataSet,colorThresholds,activeFavoriteStars);
//        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
//        mRecyclerView.setLayoutManager(mLayoutManager);
//        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.setVerticalScrollBarEnabled(true);
//
////        baseLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
////                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
////
////
////        popupWindow = new PopupWindow(baseLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
////        popupWindow.setBackgroundDrawable(new ColorDrawable());
////        popupWindow.getContentView().setFocusableInTouchMode(true);
////        popupWindow.setFocusable(true);
////        popupWindow.setOutsideTouchable(true);
//    }

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

//
//    public void addToLayout(final ParseSentenceItem parseSentenceItem) {
//        String onScreenText = parseSentenceItem.getKanjiConjugated();
//        String onScreenFurigana = parseSentenceItem.getFuriganaClean();
//
//        if(parseSentenceItem.getKanjiConjugated().equals(System.getProperty("line.separator"))) {
//            //Log 2 rows, once to input the remaining current layout items,
//            //and another black row for the seperator
//            if(linewidth>0) {
//                linearlayoutInsert(2, 1);
//            }
//            linearlayoutInsert(2, 1);
//
//        } else if(parseSentenceItem.isKanji()) {
//
//                /* INPUT KANJI */
//            if (BuildConfig.DEBUG) {
//                Log.d(TAG, "INPUT KANJI " + parseSentenceItem.getKanjiConjugated());
//            }
//            TextView textView_Test = new TextView(getContext());
//            textView_Test.setLayoutParams(new ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT));
//            textView_Test.setText(onScreenText);
//            textView_Test.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
//
//            Rect bounds = new Rect();
//            Paint textPaint = textView_Test.getPaint();
//            textPaint.getTextBounds(onScreenText, 0, onScreenText.length(), bounds);
//
//            int width = bounds.width();
//
//            if (BuildConfig.DEBUG) {
//                Log.d(TAG, "REGULAR WIDTH  = " + width);
//                Log.d(TAG, "REGULAR FURIGANA WIDTH  = " + width);
//                Log.d(TAG, "measureText WIDTH = " + Math.round(textPaint.measureText(onScreenText)));
//            }
//             width = Math.round(textPaint.measureText(onScreenText));
//            if (BuildConfig.DEBUG) {
//                Log.d(TAG, "FINAL WIDTH = " + width);
//            }
//
//            int widthExtra = (linewidth + width + displaymarginpadding) - displaywidth;
//            int maxWidthAllowed = displaywidth - linewidth - displaymarginpadding;
//
//            if (BuildConfig.DEBUG) {
//                Log.d(TAG, "widthExtra = " + widthExtra);
//                Log.d(TAG, "maxWidthAllowed= " + maxWidthAllowed);
//            }
//
//            if (widthExtra > 0) {
//                linearlayoutInsert(2, 1);
//            }
//
//            LinearLayout innerLinearLayout3 = new LinearLayout(getContext());
//            innerLinearLayout3.setOrientation(LinearLayout.VERTICAL);
//            TextView textView = new TextView(getContext());
//
//            textView.setLayoutParams(new ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT));
//
//            textView.setText(onScreenText);
//            textView.setTextSize(24);
//                textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
//
//            textView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    //TODO replace this
//                    Toast.makeText(getActivity(), "" + parseSentenceItem.getFuriganaClean(), Toast.LENGTH_SHORT).show();
//
//                }
//            });
//
//
//            innerLinearLayout3.addView(textView);
//            innerLinearLayout3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//            linearLayout.addView(innerLinearLayout3);
//
//            linewidth = linewidth + width;
//
//
//        } else {
//
//            /** INPUT THE NORMAL WORD STRINGS */
//            if(BuildConfig.DEBUG) {Log.d(TAG, "INPUT REGULAR:  " + onScreenText);}
//            TextView textView_Test = new TextView(getContext());
//            textView_Test.setLayoutParams(new ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT));
//            textView_Test.setText(onScreenText);
//            textView_Test.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
//
//            Rect bounds = new Rect();
//            Paint textPaint = textView_Test.getPaint();
//            textPaint.getTextBounds(onScreenText, 0, onScreenText.length(), bounds);
//            int height = bounds.height();
//            int width = bounds.width();
//
//            if(BuildConfig.DEBUG) {
//                Log.d(TAG, "Prospective Height = " + height);
//                Log.d(TAG, "REGULAR WIDTH  = " + width);
//                Log.d(TAG, "measureText WIDTH = " + textPaint.measureText(onScreenText));
//            }
//
//            width = Math.round(textPaint.measureText(onScreenText));
//
//            if(BuildConfig.DEBUG) {
//                Log.d(TAG, "FINAL WIDTH = " + width);
//                Log.d(TAG, "onScreenText content = " + onScreenText);
//                Log.d(TAG, "current linewidth = " + linewidth);
//                Log.d(TAG, "onScreenText linewidth = " + width);
//            }
//
//            int widthExtra = (linewidth + width + displaymarginpadding) - displaywidth;
//            int maxWidthAllowed = displaywidth - linewidth - displaymarginpadding;
//
//            if(BuildConfig.DEBUG) {
//                Log.d(TAG, "widthExtra = " + widthExtra);
//                Log.d(TAG, "maxWidthAllowed= " + maxWidthAllowed);
//            }
//
//            if (widthExtra > 0) {
//                int substringstart = 0;
//                int currentendpoint = Math.round(((float) maxWidthAllowed / (float) width) * onScreenText.length());
//                if(BuildConfig.DEBUG) {Log.d(TAG, "curentendpoint: " + currentendpoint);}
//                int substringend = currentendpoint;
//                if (maxWidthAllowed > width || maxWidthAllowed < 0) {
//                    substringend = onScreenText.length();
//                    if(BuildConfig.DEBUG) {Log.d(TAG, "Substring end is the whole onScreenText");}
//                }
//
//                if(BuildConfig.DEBUG) {
//                    Log.d(TAG, "substringstart: " + substringstart);
//                    Log.d(TAG, "substringend: " + substringend);
//                }
//
//                while (widthExtra > 0) {
//
//                    if(BuildConfig.DEBUG) {Log.d(TAG, "max width allowed: " + maxWidthAllowed);}
//
//                    String choppedTextFragment = onScreenText.substring(substringstart, substringend);
//                    if(BuildConfig.DEBUG) {Log.d(TAG, "ChoppedFragment: " + choppedTextFragment);}
//                    TextView textView = new TextView(getContext());
//
//                    /** INSERTING THE TEXT BOXES INTO THE INNER LINEAR LAYOUT */
//
//                    LinearLayout innerLinearLayout3 = new LinearLayout(getContext());
//                    innerLinearLayout3.setOrientation(LinearLayout.VERTICAL);
//
//                    textView.setLayoutParams(new ViewGroup.LayoutParams(
//                            ViewGroup.LayoutParams.WRAP_CONTENT,
//                            ViewGroup.LayoutParams.WRAP_CONTENT));
//
//                    textView.setText(choppedTextFragment);
//                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
//                    innerLinearLayout3.addView(textView);
//                    innerLinearLayout3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//                    linearLayout.addView(innerLinearLayout3);
//
//
//                    /** NEED TO FIX THIS SO IF substringend== substringstart (AND IT'S ALREADY VERY NEAR THE END OF A ROW (OR OVER IT), YOU
//                     * 1. MAKE A NEW ROW
//                     * 2. BUT DON'T TRY TO INPUT THE NONEXISTANT FRAGMENT, RESET THE METER AND THROW A NEW LARGER FRAGMENT IN THERE...*/
//                    if (substringend < substringstart) {
//                        linearlayoutInsert(2, 0); //DONT LOG A NEW ROW
//                    } else {
//
//                        linearlayoutInsert((linewidth + width + displaymarginpadding), displaywidth);
//                    }
//
//
//                    widthExtra = widthExtra - maxWidthAllowed;
//                    if(BuildConfig.DEBUG) {Log.d(TAG, "NEW widthExtra: " + widthExtra);}
//                    substringstart = substringend;
//
//                    if ((linewidth + width + displaymarginpadding) < displaywidth || widthExtra < 0) {
//                        substringend = onScreenText.length();
//
//                        if(BuildConfig.DEBUG) {
//                            Log.d(TAG, "choppedfragment substringstart: " + substringstart);
//                            Log.d(TAG, "choppedfragment substringend: " + substringend);
//                        }
//                        String choppedTextFragmentRemainder = onScreenText.substring(substringstart, substringend);
//                        if(BuildConfig.DEBUG) {Log.d(TAG, "choppedTextFragmentRemainder: " + choppedTextFragmentRemainder);}
//                        TextView textViewRemainder = new TextView(getContext());
//
//                        LinearLayout innerLinearLayout3Remainder = new LinearLayout(getContext());
//                        innerLinearLayout3Remainder.setOrientation(LinearLayout.VERTICAL);
//
//                        textViewRemainder.setLayoutParams(new ViewGroup.LayoutParams(
//                                ViewGroup.LayoutParams.WRAP_CONTENT,
//                                ViewGroup.LayoutParams.WRAP_CONTENT));
//
//                        textViewRemainder.setText(choppedTextFragmentRemainder);
//                        textViewRemainder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
//
//                        Rect bounds2 = new Rect();
//                        Paint textPaint2 = textViewRemainder.getPaint();
//                        textPaint2.getTextBounds(choppedTextFragmentRemainder, 0, choppedTextFragmentRemainder.length(), bounds2);
//
//                        int width_chopped = bounds2.width();
//                        innerLinearLayout3Remainder.addView(textViewRemainder);
//                        innerLinearLayout3Remainder.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//                        linearLayout.addView(innerLinearLayout3Remainder);
//
//                        linewidth = linewidth + width_chopped;
//
//                        if(BuildConfig.DEBUG) {
//                            Log.d(TAG, "chopped linewidth: " + linewidth);
//                            Log.d(TAG, "chopped width: " + width);
//                        }
//
//                        widthExtra = (linewidth + displaymarginpadding) - displaywidth;
//                        maxWidthAllowed = displaywidth - linewidth - displaymarginpadding;
//
//
//                        if (linewidth == 0 && widthExtra > 0) {  // like if it's the last fragment of a line, starting on a new line. Just print the damn thing (on the new line)
//                            linearlayoutInsert((linewidth + (displaywidth + widthExtra) + displaymarginpadding), displaywidth);
//                        } else if (linewidth == 0 && widthExtra < 0) {
//                            linearlayoutInsert((linewidth + (displaywidth + widthExtra) + displaymarginpadding), displaywidth);
//                        } else {
//                            linearlayoutInsert((linewidth + displaymarginpadding), displaywidth);
//                        }
//
//                    } else {
//
//                        maxWidthAllowed = displaywidth - linewidth - displaymarginpadding;
//
//                        if(BuildConfig.DEBUG) {
//                            Log.d(TAG, "substringend calculation--maxwidthallowed: " + maxWidthAllowed);
//                            Log.d(TAG, "substringend calculation--width: " + width);
//                            Log.d(TAG, "substringend calculation--onScreenText.length: " + onScreenText.length());
//                        }
//
//                        substringend = Math.round(((float) maxWidthAllowed / (float) width) * onScreenText.length()) + substringstart;
//                        if(BuildConfig.DEBUG) {Log.d(TAG, "substring rounding: " + ((float) maxWidthAllowed / (float) width) * onScreenText.length());}
//                    }
//
//                    if(BuildConfig.DEBUG) {
//                        Log.d(TAG, "NEW substringstart: " + substringstart);
//                        Log.d(TAG, "NEW substringend: " + substringend);
//                    }
//                    if (substringend > onScreenText.length()) {
//                        if(BuildConfig.DEBUG) {Log.d(TAG, "SHIT WORKAROUND substringend Revised to: " + substringend);}
//                        substringend = onScreenText.length();
//                    }
//
//                }
//
//            } else {
//
//                LinearLayout innerLinearLayout3 = new LinearLayout(getContext());
//                innerLinearLayout3.setOrientation(LinearLayout.VERTICAL);
//
//                textView_Test.setLayoutParams(new ViewGroup.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT));
//
//                textView_Test.setText(onScreenText);
//                textView_Test.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
//                innerLinearLayout3.addView(textView_Test);
//                innerLinearLayout3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//                linearLayout.addView(innerLinearLayout3);
//                linewidth = linewidth + width;
//                linearlayoutInsert((linewidth + displaymarginpadding), displaywidth);
//
//                if(BuildConfig.DEBUG) {Log.d(TAG, "new linewidth = " + linewidth);}
//
//            }
//
//        }
//    }

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(parseSentenceSubscription != null) {
            parseSentenceSubscription.unsubscribe();
        }
    }
}

