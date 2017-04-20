package com.jukuproject.jukutweet.Fragments;
/**
 * Created by JClassic on 2/25/2017.
 */


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
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.FavoritesColors;
import com.jukuproject.jukutweet.Interfaces.TweetListOperationsInterface;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetUrl;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;
import com.jukuproject.jukutweet.TweetParser;

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
//import com.jukuproject.jukutweet.TweetParser;


/**
 * Created by Joe on 11/21/2015.
 */

public class TweetBreakDownFragment extends Fragment {

    String TAG = "TEST-breakdownpop";
//    private FragmentInteractionListener mCallback;
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

        mRecyclerView = (RecyclerView) v.findViewById(R.id.parseSentenceRecyclerView);
        txtSentence =  (TextView) v.findViewById(R.id.sentence);



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
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(getContext());
        colorThresholds = sharedPrefManager.getColorThresholds();
        mActiveTweetFavoriteStars = SharedPrefManager.getInstance(getContext()).getActiveTweetFavoriteStars();
        activeFavoriteStars = sharedPrefManager.getActiveFavoriteStars();

        if(savedInstanceState == null) {
            mTweet = getArguments().getParcelable("tweet");
            mSavedTweet = getArguments().getBoolean("isSavedTweet");
        } else {

            mTweet = savedInstanceState.getParcelable("mTweet");
            mSavedTweet = savedInstanceState.getBoolean("mSavedTweet");
            mDisectedTweet = savedInstanceState.getParcelableArrayList("mDisectedTweet");

        }




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
//                        InternalDB helper = InternalDB.getInstance(getContext());
                        TweetListOperationsInterface helperTweetOps = InternalDB.getTweetInterfaceInstance(getContext());
                        //Toggle favorite list association for this tweet
                        if(FavoritesColors.onFavoriteStarToggleTweet(getContext(),mActiveTweetFavoriteStars,mTweet.getUser().getUserId(),mTweet)) {
                            imgStar.setImageResource(FavoritesColors.assignStarResource(mTweet.getItemFavorites(),mActiveTweetFavoriteStars));
                            imgStar.setColorFilter(ContextCompat.getColor(getContext(), FavoritesColors.assignStarColor(mTweet.getItemFavorites(),mActiveTweetFavoriteStars)));

                        }

                        //Check for tweet in db
                        try {
                            Log.d(TAG,"SAVING TWEETS TO DB...");
                            //If tweet doesn't already exist in db, insert it
                            if(helperTweetOps.tweetExistsInDB(mTweet) == 0 && mTweet.getUser() != null){

                                int addTweetResultCode = helperTweetOps.saveTweetToDB(mTweet.getUser(),mTweet);
                                Log.d(TAG,"SAVING TWEETS TO DB2... " + addTweetResultCode);

                                if(addTweetResultCode > 0 && mDisectedTweet != null) {
                                /*DB insert successfull, now save tweet urls and parsed kanji into database */
                                    helperTweetOps.saveParsedTweetKanji(mDisectedTweet,mTweet.getIdString());
                                    helperTweetOps.saveTweetUrls(mTweet);
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
            Cursor c = InternalDB.getWordInterfaceInstance(getContext()).getWordEntryForWordId(wordEntry.getId(),colorThresholds);
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
        * passed into the TweetParser, but will skip all the "parsing" and will be reintegrated at the end, and
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
                return TweetParser.getInstance().parseSentence(getContext()
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


//                            Log.d(TAG,"YYY - " + disectedTweet.get(1).getKanji() + " - " + disectedTweet.get(1).getColor());
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


//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        try {
//            mCallback = (FragmentInteractionListener) context;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(context.toString()
//                    + " must implement OnHeadlineSelectedListener");
//        }
//    }
//
//

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
        mAdapter = new TweetBreakDownAdapter(getContext(),metrics,disectedSavedTweet,activeFavoriteStars);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setVerticalScrollBarEnabled(true);

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(parseSentenceSubscription != null) {
            parseSentenceSubscription.unsubscribe();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//        outState.putStringArrayList("mActiveTweetFavoriteStars", mActiveTweetFavoriteStars);
        outState.putBoolean("mSavedTweet", mSavedTweet);
        outState.putParcelableArrayList("mDisectedTweet", mDisectedTweet);
        outState.putParcelable("mTweet", mTweet);


    }

}

