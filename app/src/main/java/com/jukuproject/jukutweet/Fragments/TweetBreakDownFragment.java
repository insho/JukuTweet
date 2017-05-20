package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
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
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.TweetBreakDownAdapter;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.ChooseFavoriteListsPopupWindow;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog;
import com.jukuproject.jukutweet.FavoritesColors;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Interfaces.WordEntryFavoritesChangedListener;
import com.jukuproject.jukutweet.MainActivity;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetEntities;
import com.jukuproject.jukutweet.Models.TweetUrl;
import com.jukuproject.jukutweet.Models.TweetUserMentions;
import com.jukuproject.jukutweet.Models.UserInfo;
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
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Displays the full text of the tweet in the top portion of the screen (with colored words depending on word score, url and
 * retweet links), and below that displays vocabulary words from the tweet, courtesy of the {@link TweetParser}. It can be
 * called from the {@link UserTimeLineFragment}, parsing a tweet in real time and displaying this breakdown, or it can
 * be called from {@link TweetListBrowseFragment}, displaying a saved tweet (which may or may not have the parsed kanji already
 * saved in the db)
 */
public class TweetBreakDownFragment extends Fragment implements WordEntryFavoritesChangedListener {

    String TAG = "TEST-breakdownfrag";
    private FragmentInteractionListener mCallback;
    private RxBus mRxBus= new RxBus();
    private Tweet mTweet;

    private RecyclerView mRecyclerView;
    private boolean mSavedTweet = false;
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
    private long mLastClickTime = 0;


    public TweetBreakDownFragment() {}

    /**
     * Called from the {@link UserTimeLineFragment} when user clicks on a tweet. In this instance,
     * the "Save tweet" favorites star should be showing, allowing user to save tweet to a TweetList
     */
    public static TweetBreakDownFragment newInstanceTimeLine(Tweet tweet) {
        TweetBreakDownFragment fragment = new TweetBreakDownFragment();
        Bundle args = new Bundle();
        args.putParcelable("tweet", tweet);
        args.putBoolean("isSavedTweet",false);
        fragment.setArguments(args);

        return  fragment;
    }

    /**
     * Called from the {@link TweetListBrowseFragment} when user clicks on a tweet. In this instance,
     * the "Save tweet" favorites star should be hidden, showing only the tweet breakdown
     */
    public static TweetBreakDownFragment newInstanceSavedTweet(Tweet tweet) {
        TweetBreakDownFragment fragment = new TweetBreakDownFragment();
        Bundle args = new Bundle();
        args.putParcelable("tweet", tweet);
        args.putBoolean("isSavedTweet",true);
        fragment.setArguments(args);

        return  fragment;
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
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(getContext());
        colorThresholds = sharedPrefManager.getColorThresholds();
        mActiveTweetFavoriteStars = SharedPrefManager.getInstance(getContext()).getActiveTweetFavoriteStars();
        activeFavoriteStars = sharedPrefManager.getActiveFavoriteStars();

        if(savedInstanceState != null) {
            mTweet = savedInstanceState.getParcelable("mTweet");
            mSavedTweet = savedInstanceState.getBoolean("mSavedTweet");
        } else {

            mTweet = getArguments().getParcelable("tweet");
            mSavedTweet = getArguments().getBoolean("isSavedTweet",false);
        }

        txtSentence.setVisibility(View.VISIBLE);
        txtSentence.setText(mTweet.getText());

        final String sentence = mTweet.getText();

        setUpFavoritesStar();


    if(BuildConfig.DEBUG){Log.d(TAG,"SAVED TWEET: " + mSavedTweet);}

        //Set the tweet entities (urls and user_mentions) for a saved tweet
        if(mSavedTweet) {
            TweetEntities entities = InternalDB.getTweetInterfaceInstance(getContext()).getTweetEntitiesForSavedTweet(mTweet.getIdString());
            mTweet.setEntities(entities);
        }

    /* In the case that the tweet is saved to the db, and that it has already been parsed and the words in it saved
    * to the db as well, all that needs to be done is pull relevent favorite list information for those words and then
    * pass the dataset onto the adapter */
    if(mSavedTweet && mTweet.getWordEntries()!=null) {

        //If the user icon was not saved, save it
        if(mTweet.getUser()!=null
                && !InternalDB.getUserInterfaceInstance(getContext()).duplicateUser(mTweet.getUser().getUserId())) {
            mCallback.downloadTweetUserIcons(mTweet.getUser());
        }



            /* If it is a previously saved tweet, the favorite list information for the WordEntries in the
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


        showDisectedTweet(mTweet.getWordEntries(),txtSentence);

    } else  {
        /* Otherwise, if the tweet is saved but not yet parsed, or the tweet hasn't been saved, run
         * the tweet through the TweetParser first before displaying parsed tweet */
        if(BuildConfig.DEBUG){ Log.d(TAG,"getwordentries is null");}
        final ArrayList<String> spansToExclude = new ArrayList<>();

        /* Create a list of "SpecialSpan" objects from the urls contained the Tweet. These will be
        * passed into the TweetParser, but will skip all the "parsing" and will be reintegrated at the end, and
        * designated as "url" in the ParseSentenceItems that emerge from the parser. They can then be shown as clickable links when
        * the tweet text is reassembled */

        if(mTweet.getEntities()!= null && mTweet.getEntities().getUrls() != null) {
            for(TweetUrl url : mTweet.getEntities().getUrls()) {
                if(url != null) {
                    spansToExclude.add(url.getUrl());
                }

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

        if(parseSentenceSubscription!=null) {
            parseSentenceSubscription.unsubscribe();
        }
        showProgressBar(true);
        parseSentenceSubscription = disectTweet.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<ArrayList<WordEntry>>() {

                    @Override
                    public void onSuccess(ArrayList<WordEntry> disectedTweet) {

                        if(disectedTweet.size()>0) {
                        mTweet.setWordEntries(disectedTweet);
                            showDisectedTweet(mTweet.getWordEntries(),txtSentence);
                            InternalDB.getTweetInterfaceInstance(getContext()).saveParsedTweetKanji(disectedTweet,mTweet.getIdString());
                            showProgressBar(false);
                        } else {
                            /*No results found. Keep the initial sentence in the txtSentence view,
                            and show no sentences found message in place of recyclerview */
                            mRecyclerView.setVisibility(View.GONE);
                            txtNoLists.setVisibility(View.VISIBLE);
                            txtNoLists.setText(getString(R.string.tweetbreak_unabletoparse));
                            txtNoLists.setTextColor(ContextCompat.getColor(getContext(),android.R.color.holo_red_dark));
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG,"ERROR IN PARSE SENTENCE OBSERVABLE: " + error);
                        showProgressBar(false);
//                        linearLayoutVerticalMain.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.GONE);
                        txtNoLists.setVisibility(View.VISIBLE);
                        txtNoLists.setText(getString(R.string.tweetbreak_unabletoparse));
                        txtNoLists.setTextColor(ContextCompat.getColor(getContext(),android.R.color.holo_red_dark));
                    }
                });


        }
    }


    private void setUpFavoritesStar() {
        //Try to fill in user info at the top
        try {
            txtUserName.setText(mTweet.getUser().getName());
            txtUserScreenName.setText(mTweet.getUser().getDisplayScreenName());
        } catch (NullPointerException e) {
            Log.e(TAG,"TweetBreakDownFragment mTweet doesn't contain user?: " + e);
            txtUserName.setVisibility(View.INVISIBLE);
            txtUserScreenName.setVisibility(View.INVISIBLE);
        }

        /*If it is a saved tweet don't show the favorites star. It would complicate things alot
         to let the user change the favorites entry for a star from within fthe breakdown fragment */
        if(mSavedTweet) {
            //Set up the favorites star
            imgStar.setVisibility(View.GONE);
            imgStarLayout.setVisibility(View.GONE);
        } else {

            //Set up the favorites star
            imgStarLayout.setClickable(true);
            imgStarLayout.setLongClickable(true);

            try {

                Integer starColorDrawableInt = FavoritesColors.assignStarResource(true,mTweet.getItemFavorites(),mActiveTweetFavoriteStars);
                imgStar.setImageResource(starColorDrawableInt);

                if(starColorDrawableInt!=R.drawable.ic_twitter_multicolor_24dp) {
                    try {
                        imgStar.setColorFilter(ContextCompat.getColor(getContext(),FavoritesColors.assignStarColor(mTweet.getItemFavorites(),mActiveTweetFavoriteStars)));
                    } catch (NullPointerException e) {
                        Log.e(TAG,"tweetBreakDownAdapter multistar Nullpointer error setting star color filter in word detail popup dialog... Need to assign item favorites to WordEntry(?)" + e.getCause());
                    }
                } else {
                    imgStar.setColorFilter(null);
                }

                imgStarLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(BuildConfig.DEBUG){Log.d(TAG,"mActiveFavoriteStars: " + mActiveTweetFavoriteStars);}



                        if(mTweet.getItemFavorites().shouldOpenFavoritePopup(mActiveTweetFavoriteStars)) {
                            showTweetFavoriteListPopupWindow();
                        } else {
                            if(FavoritesColors.onFavoriteStarToggleTweet(getContext(),mActiveTweetFavoriteStars,mTweet.getUser().getUserId(),mTweet)) {
                                imgStar.setImageResource(R.drawable.ic_twitter_black_24dp);
                                try {
                                    imgStar.setColorFilter(ContextCompat.getColor(getContext(),FavoritesColors.assignStarColor(mTweet.getItemFavorites(),mActiveTweetFavoriteStars)));
                                } catch (NullPointerException e) {
                                    Log.e(TAG,"tweetbreakdown fragment blackstar Nullpointer error setting star color filter in word detail popup dialog... Need to assign item favorites to WordEntry(?)" + e.getCause());
                                }
                            } else {
                                Log.e(TAG,"OnFavoriteStarToggle did not work...");
                            }

                            /*Check to see if tweet must be deleted from db. Delete if necessary.
                            * Likewise adds a tweet to the db if it has just been added to a favorites list.
                            * This bit MUST come after the favorite star toggle, because it determines whether to
                            * add or delete a tweet based on whether that tweet is in a favorites list. */
                            saveOrDeleteTweet(mTweet);
                        }


                    }
                });

                imgStarLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        showTweetFavoriteListPopupWindow();

                        return true;
                    }
                });

            }  catch (NullPointerException e) {
                Log.e(TAG,"TweetBreakDownFragment setting up imgStar doesn't contain itemfavs??: " + e);
                imgStar.setVisibility(View.GONE);
            }
        }
    }




    public void showDisectedTweet(ArrayList<WordEntry> disectedSavedTweet, TextView txtSentence ) {

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        /* Set tweet color spans. If the saved Tweet object includes a "colorIndex" object (
        * which comes from the savedTweetKanji table and contains the id, positions and color designation
        * of each kanji in the TWeet), replace the normal Tweet text with colored spans for those kanji */

            try {

                SpannableString text = new SpannableString(mTweet.getText());

                //Try to add links
                if(mTweet.getEntities() != null && mTweet.getEntities().getUrls()!=null) {


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

                        text.setSpan(new URLSpan(url.getUrl()), startingLinkPos, startingLinkPos + urlToLinkify.length(), 0);


                    }

                }

                if(mTweet.getEntities() != null && mTweet.getEntities().getUser_mentions()!=null) {


                    List<TweetUserMentions> tweetUserMentions =  mTweet.getEntities().getUser_mentions();

                    for(final TweetUserMentions userMention : tweetUserMentions) {
                        int[] indices = userMention.getIndices();

                        String mentionToLinkify = "";

                        if(mTweet.getText().substring(indices[0]).contains(userMention.getName())) {
                            mentionToLinkify = userMention.getName();
                        } else if(mTweet.getText().substring(indices[0]).contains(userMention.getScreen_name())) {
                            mentionToLinkify = userMention.getScreen_name();
                        }
                        int startingLinkPos = mTweet.getText().indexOf(mentionToLinkify,indices[0]);
                        ClickableSpan userMentionClickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                mCallback.getInitialUserInfoForAddUserCheck(userMention.getScreen_name());
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setUnderlineText(false);
                            }
                        };
                        text.setSpan(userMentionClickableSpan, startingLinkPos, startingLinkPos + mentionToLinkify.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }

                }

                for(final WordEntry wordEntry : disectedSavedTweet) {

                    ClickableSpan kanjiClick = new ClickableSpan() {
                        @Override
                        public void onClick(View textView) {
                            if(isUniqueClick(150)) {

                                WordDetailPopupDialog wordDetailPopupDialog = WordDetailPopupDialog.newInstance(wordEntry);
                                wordDetailPopupDialog.setTargetFragment(TweetBreakDownFragment.this, 0);
                                wordDetailPopupDialog.show(getFragmentManager(),"wordDetailPopup");
                            }
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(ContextCompat.getColor(getContext(),wordEntry.getColorValue()));
                            ds.setUnderlineText(false);

                        }
                    };
                    text.setSpan(kanjiClick, wordEntry.getStartIndex(), wordEntry.getEndIndex(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }

                txtSentence.setMovementMethod(LinkMovementMethod.getInstance());
                txtSentence.setText(text, TextView.BufferType.SPANNABLE);

            } catch (NullPointerException e) {
                Log.e(TAG,"nullpointer error in setting up url/user_mention/word color spans: " + e.getCause());
            }


        /* The TweetKanjiColor objects contain Edict_ids for the kanji in the tweet, but they
        * do not contain the kanji, definition or word score values for those kanji. This
        * converts the TweetKanjiColor Edic_ids into a single string, passes it
        * */
        mAdapter = new TweetBreakDownAdapter(getContext(),metrics,disectedSavedTweet,activeFavoriteStars,mRxBus);
        mRxBus.toClickObserverable().subscribe(new Action1<Object>() {
                                                      @Override
                                                      public void call(Object event) {
                                                          if (isUniqueClick(150) && event instanceof WordEntry) {
                                                              WordEntry wordEntry = (WordEntry) event;
                                                              updateWordEntryFavoritesForOtherTabs(wordEntry);
                                                          }
                                                      }
                                                  });
            mRxBus.toLongClickObserverable().subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (isUniqueClick(1000) && event instanceof WordEntry) {
                        WordEntry wordEntry = (WordEntry) event;
                        WordDetailPopupDialog wordDetailPopupDialog = WordDetailPopupDialog.newInstance(wordEntry);
                        wordDetailPopupDialog.setTargetFragment(TweetBreakDownFragment.this, 0);
                        wordDetailPopupDialog.show(getFragmentManager(),"wordDetailPopup");
                    }
                }
            });
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

    /**
     * Displays {@link ChooseFavoriteListsPopupWindow} when the tweet list favorite star is long-clicked (
     * or short-clicked if the tweet already belongs to multiple lists). Allows user to add/remove the tweet
     * from favorite lists via the popup window.
     */
    public void showTweetFavoriteListPopupWindow() {

        RxBus rxBus = new RxBus();
        ArrayList<MyListEntry> availableFavoriteLists = InternalDB.getTweetInterfaceInstance(getContext()).getTweetListsForTweet(mActiveTweetFavoriteStars,mTweet.getIdString(),null);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        PopupWindow popupWindow = ChooseFavoriteListsPopupWindow.createTweetFavoritesPopup(getContext(),metrics,rxBus,availableFavoriteLists,mTweet.getIdString(), mTweet.getUser().getUserId());
        popupWindow.getContentView().measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        /* Popup window is displayed as a dropdown below the favorite star. The window must be adjusted up and left
        * so it is displayed properly on the screen.*/
        int xadjust = popupWindow.getContentView().getMeasuredWidth() + (int) (25 * metrics.density + 0.5f);
        int yadjust = (int)((imgStar.getMeasuredHeight())/2.0f);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mCallback.notifySavedTweetFragmentsChanged();
                /*Check to see if tweet must be deleted from db. Delete if necessary.
                            * Likewise adds a tweet to the db if it has just been added to a favorites list.
                            * This bit MUST come after the favorite star toggle, because it determines whether to
                            * add or delete a tweet based on whether that tweet is in a favorites list. */
                saveOrDeleteTweet(mTweet);
            }
        });

        rxBus.toClickObserverable().subscribe(new Action1<Object>() {
            @Override
            public void call(Object event) {

                /* Recieve a WordList entry (containing an updated list entry for this row kanji) from
                 * the ChooseFavoritesAdapter in the ChooseFavorites popup window */
                if(event instanceof MyListEntry) {
                    MyListEntry myListEntry = (MyListEntry) event;

                    /* Ascertain the type of list that the kanji was added to (or subtracted from),
                     and update that list's count */
                    if(myListEntry.getListsSys() == 1) {
                        //Update the color
                        switch (myListEntry.getListName()) {
                            case "Blue":
                                mTweet.getItemFavorites().setSystemBlueCount(myListEntry.getSelectionLevel());
                                break;
                            case "Green":
                                mTweet.getItemFavorites().setSystemGreenCount(myListEntry.getSelectionLevel());
                                break;
                            case "Red":
                                mTweet.getItemFavorites().setSystemRedCount(myListEntry.getSelectionLevel());
                                break;
                            case "Yellow":
                                mTweet.getItemFavorites().setSystemYellowCount(myListEntry.getSelectionLevel());
                                break;
                            case "Purple":
                                mTweet.getItemFavorites().setSystemPurpleCount(myListEntry.getSelectionLevel());
                                break;
                            case "Orange":
                                mTweet.getItemFavorites().setSystemOrangeCount(myListEntry.getSelectionLevel());
                                break;
                            default:
                                break;
                        }
                    } else {
                        if(myListEntry.getSelectionLevel() == 1) {
                            mTweet.getItemFavorites().addToUserListCount(1);
                        } else {
                            mTweet.getItemFavorites().subtractFromUserListCount(1);
                        }
                    }

                    if(mTweet.getItemFavorites().shouldOpenFavoritePopup(mActiveTweetFavoriteStars)
                            && mTweet.getItemFavorites().systemListCount(mActiveTweetFavoriteStars) >1) {
                        imgStar.setColorFilter(null);
                        imgStar.setImageResource(R.drawable.ic_twitter_multicolor_24dp);

                    } else {
                        imgStar.setImageResource(R.drawable.ic_twitter_black_24dp);
                        try {
                            imgStar.setColorFilter(ContextCompat.getColor(getContext(),FavoritesColors.assignStarColor(mTweet.getItemFavorites(),mActiveTweetFavoriteStars)));
                        } catch (NullPointerException e) {
                            Log.e(TAG,"tweetbreakdown fragment blackstar Nullpointer error setting star color filter in word detail popup dialog... Need to assign item favorites to WordEntry(?)" + e.getCause());
                        }
                    }

                }

            }

        });


        popupWindow.showAsDropDown(imgStar,-xadjust,-yadjust);

    }


    /**
     * If a word entry has been saved to a new word list in the {@link WordDetailPopupDialog}, the message is relayed back to
     * this method, which updates the  {@link com.jukuproject.jukutweet.Models.ItemFavorites} in the dataset to reflect the change
     * @param wordEntry WordEntry that was added to/removed from a new list
     */
    public void updateWordEntryItemFavorites(WordEntry wordEntry) {

                if(mTweet.getWordEntries()!=null ) {
                    for(WordEntry tweetWordEntry : mTweet.getWordEntries()) {
                        if(tweetWordEntry.getId().equals(wordEntry.getId())) {
                            tweetWordEntry.setItemFavorites(wordEntry.getItemFavorites());
                        }
                    }
                }

        mAdapter.notifyDataSetChanged();
    }

    /**
     * If a group of word entries has been saved in another fragment, the message relayed to {@link MainActivity#notifySavedWordFragmentsChanged(String)}
     * , which then notifies any open tabs in the other fragments which might be affected by the new word that the change
     * has been made. This method recieves the udpdated list of words and cycles through them, looking for matches. If a match
     * is found, the {@link com.jukuproject.jukutweet.Models.ItemFavorites} object for the word is updated to reflect the new favorite list/s
     * @param updatedWordEntries ArrayList of WordEntries that were saved to/removed from a new list
     */
    public void updateWordEntryItemFavorites(ArrayList<WordEntry> updatedWordEntries) {


        if(mTweet.getWordEntries()!=null ) {
            for(WordEntry tweetWordEntry : mTweet.getWordEntries()) {
                for(WordEntry updatedWordEntry : updatedWordEntries ) {
                    if(tweetWordEntry.getId().equals(updatedWordEntry.getId())) {
                        tweetWordEntry.setItemFavorites(updatedWordEntry.getItemFavorites());
                        mAdapter.notifyItemChanged(mTweet.getWordEntries().indexOf(tweetWordEntry));
                    }
                }
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    /**
     * Decides via {@link com.jukuproject.jukutweet.Interfaces.TweetListOperationsInterface#saveOrDeleteTweet(Tweet)} whether to
     * save the tweet or remove it from the db (if it already exists and is unnecessary). If it saves the tweet, it also decides via
     * whether or not to download the tweet icon with a callback to the "downloadTweetUserIcons" method in the activity. Lastly
     * it notifies tweet related fragments that a change has been made
     * @param tweet Tweet to be saved
     */
    public void saveOrDeleteTweet(Tweet tweet){
        //Check for tweet in db
        try {

            //If tweet was not in the saved tweets database, and was then successfully saved, download tweet icon
            int savedOrDeleteResultCode = InternalDB.getTweetInterfaceInstance(getContext()).saveOrDeleteTweet(tweet);
            if(savedOrDeleteResultCode==1) {
                if(tweet.getUser()!=null
                        && !InternalDB.getUserInterfaceInstance(getContext()).duplicateUser(tweet.getUser().getUserId())) {
                    mCallback.downloadTweetUserIcons(tweet.getUser());
                }

                //Try to parse and insert Tweet Kanji if they do not already exist
                if(InternalDB.getTweetInterfaceInstance(getContext()).tweetParsedKanjiExistsInDB(tweet) == 0) {
                    mCallback.parseAndSaveTweet(tweet);
                }
            }


            if(savedOrDeleteResultCode>=1) {

                mCallback.notifySavedTweetFragmentsChanged();
            }

        } catch (SQLiteException sqlexception){
            Log.e(TAG,"saveOrDeleteTweet - sqlite exception when tweet saving, UNABLE to save!");

        } catch (NullPointerException e){
            Log.e(TAG,"saveOrDeleteTweet - nullpointer when tweet saving, UNABLE to save!");

        }

    }



    /**
     * If a word has been saved in the {@link WordDetailPopupDialog}, a message is passed
     * from worddetail to this fragment, and then back to {@link MainActivity#notifySavedWordFragmentsChanged(String)}
     * notifying to updated any fragments affected by the new word
     * @param wordEntry WordEntry that was saved
     */
    public void updateWordEntryFavoritesForOtherTabs(WordEntry wordEntry) {
        mCallback.notifySavedWordFragmentsChanged(String.valueOf(wordEntry.getId()));
    }

    /**
     * If a tweet has been saved in the {@link WordDetailPopupDialog}, a message is passed
     * from worddetail to this fragment, and then back to {@link MainActivity#notifySavedTweetFragmentsChanged()}
     * notifying to updated any fragments affected by the new tweet
     */
    public void notifySavedTweetFragmentsChanged(){
        mCallback.notifySavedTweetFragmentsChanged();
    }

    /**
     * If a tweet has been saved in {@link WordDetailPopupDialog}, and the user for that tweet
     * is not saved in the db (which therefore means the user's icon is not saved in the db), this passes
     * on the message to save the icon from the {@link com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter} to the Activity,
     * which uses {@link com.jukuproject.jukutweet.Database.UserOpsHelper#downloadTweetUserIcon(Context, String, String)} in a
     * subscription to download the icon
     * @param userInfo UserInfo of user whose icon will be downloaded
     */
    public void downloadTweetUserIcons(UserInfo userInfo) {
        mCallback.downloadTweetUserIcons(userInfo);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(parseSentenceSubscription != null) {
            parseSentenceSubscription.unsubscribe();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(parseSentenceSubscription != null) {
            parseSentenceSubscription.unsubscribe();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mSavedTweet", mSavedTweet);
        outState.putParcelable("mTweet", mTweet);
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
}

