package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter;
import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Interfaces.TweetListOperationsInterface;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetUrl;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.TweetParser;
import com.jukuproject.jukutweet.TwitterUserClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observer;
import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

//import com.jukuproject.jukutweet.Models.ParseSentenceItem;
//import com.jukuproject.jukutweet.Models.ParseSentenceSpecialSpan;
//import com.jukuproject.jukutweet.TweetParser;

/**
 * Shows last X tweets from a twitter timeline. User can click on a tweet to bring up
 * the WordBreakDown Popup window, save the tweet and/or specific kanji from the tweet
 */
public class UserTimeLineFragment extends Fragment {
    FragmentInteractionListener mCallback;

    /* mLastClickTime Tracks elapsed time since last click of a recyclerview row. Used to
    * keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
    private RxBus _rxBus = new RxBus();
    private RecyclerView mRecyclerView;
    private UserTimeLineAdapter mAdapter;
    private TextView mNoLists;
    private UserInfo mUserInfo;
    private List<Tweet> mTimeLine;

    /* Holds a list of tweets that have been favorited (in any/all lists). Used to check
    * whether or not a tweet needs to have favorites assigned to it. This exists
    * so that we dont' have to make a sql query for each Tweet that gets returned from
    * the api lookup */
    private HashMap<String,ItemFavorites> tweetIdStringsInFavorites;

    private ArrayList<String> mActiveTweetFavoriteStars;

//    private TweetBreakDownFragment mTweetBreakDownFragment;

    private static final String TAG = "TEST-TimeLineFrag";

    public UserTimeLineFragment() {}

    /**
     * Returns a new instance of UserListFragment
     */
    public static UserTimeLineFragment newInstance(UserInfo userInfo) {
        UserTimeLineFragment fragment = new UserTimeLineFragment();
        Bundle args = new Bundle();
        args.putParcelable("userInfo",userInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);
        mUserInfo = getArguments().getParcelable("userInfo");
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerTimeLine);
        mNoLists = (TextView) view.findViewById(R.id.notweets);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Get active favorite stars for tweets, to pass on to adapter for star-clicking tweet-saving
        mActiveTweetFavoriteStars = SharedPrefManager.getInstance(getContext()).getActiveTweetFavoriteStars();
        tweetIdStringsInFavorites = InternalDB.getTweetInterfaceInstance(getContext()).getStarFavoriteDataForAUsersTweets(mUserInfo.getUserId());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);

        if(mUserInfo != null) {
            pullTimeLineData(mUserInfo);
        } else {
            //TODO -- put error in place if there is no userinfo
        }

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
//        updateAdapter();
        //TODO Put something in place to repull data on resume...
    }


    /**
     * Accesses twitters api and pulls the last X tweets from a user's timeline into
     * the UserTimeLineAdapter
     * @param userInfo UserInfo object with data from a single twitter user
     */
    public void pullTimeLineData(final UserInfo userInfo){

        mCallback.showProgressBar(true);
//        mAdapter = new UserTimeLineAdapter(getContext(),_rxBus,mUserInfo,new ArrayList<Tweet>());
//        mRecyclerView.setAdapter(mAdapter);
        if(mTimeLine==null) {
            mTimeLine = new ArrayList<>();
            String token = getResources().getString(R.string.access_token);
            String tokenSecret = getResources().getString(R.string.access_token_secret);

            //TODO make the number of twitter responses an option! not just 10
            TwitterUserClient.getInstance(token,tokenSecret)
                    .getUserTimeline(userInfo.getScreenName(),10)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<Tweet>>() {



                        @Override public void onCompleted() {
                            if(BuildConfig.DEBUG){Log.d(TAG, "In onCompleted()");}

                            mCallback.showProgressBar(false);

                            mAdapter = new UserTimeLineAdapter(getContext(),_rxBus,mUserInfo,mTimeLine,mActiveTweetFavoriteStars);

                            _rxBus.toClickObserverable()
                                    .subscribe(new Action1<Object>() {
                                        @Override
                                        public void call(Object event) {

                                            if(isUniqueClick(1000) && event instanceof Tweet) {

                                                //TODO -- make it so only one instance of breakdown fragment exists
                                                Tweet tweet = (Tweet) event;
                                                TweetBreakDownFragment fragment = TweetBreakDownFragment.newInstanceTimeLine(tweet);
                                                ((BaseContainerFragment)getParentFragment()).addFragment(fragment, true,"tweetbreakdown");
                                                mCallback.showFab(false,"");
                                            }

                                        }

                                    });

                            _rxBus.toSaveTweetObserverable().subscribe(new Action1<Object>() {

                                @Override
                                public void call(Object event) {

                                    if(isUniqueClick(1000) && event instanceof Tweet) {
                                        final Tweet tweet = (Tweet) event;

                                        //Try to insert urls
                                        final TweetListOperationsInterface helperTweetOps = InternalDB.getTweetInterfaceInstance(getContext());
                                        helperTweetOps.saveTweetUrls(tweet);

                                        //Try to insert Kanji
                                        if(helperTweetOps.tweetParsedKanjiExistsInDB(tweet) == 0) {
                                            Log.d(TAG,"SAVING TWEET KANJI");

//                                        final WordLoader wordLoader = helper.getWordLists(db);
                                            Single.fromCallable(new Callable<ArrayList<WordEntry>>() {
                                                @Override
                                                public ArrayList<WordEntry> call() throws Exception {

                                                    final ArrayList<String> spansToExclude = new ArrayList<>();

                                                    if(tweet.getEntities() != null && tweet.getEntities().getUrls() != null) {
                                                        for(TweetUrl url : tweet.getEntities().getUrls()) {
                                                            if(url != null) {
                                                                spansToExclude.add(url.getUrl());
                                                            }
                                                        }

                                                    }
//                                            Log.d(TAG,"DB OPEN BEFORE: " + db.isOpen());
                                                    ColorThresholds colorThresholds = SharedPrefManager.getInstance(getContext()).getColorThresholds();
                                                    return TweetParser.getInstance().parseSentence(getContext()
                                                            ,tweet.getText()
                                                            ,spansToExclude
                                                            ,colorThresholds);
                                                }
                                            }).subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(new SingleSubscriber<ArrayList<WordEntry>>() {

                                                        @Override
                                                        public void onSuccess(ArrayList<WordEntry> disectedTweet) {
                                                            //load the parsed kanji ids into the database
                                                            InternalDB.getTweetInterfaceInstance(getContext()).saveParsedTweetKanji(disectedTweet,tweet.getIdString());

//                                                            for(WordEntry item : disectedTweet) {
//                                                                Log.d("XXXX",item.getKanjiConjugated());
//                                                            }
                                                            //TODO handle errors on insert?
                                                            mCallback.notifyFragmentsChanged();
//                                                            helper.close();
//                                                        db.close();
                                                        }

                                                        @Override
                                                        public void onError(Throwable error) {
                                                            Log.e(TAG,"ERROR IN PARSE KANJI (for saved tweet) OBSERVABLE: " + error);
//                                                            helper.close();
//                                                        db.close();
                                                        }
                                                    });
//
//                                    public Observable<List<Weather>> getWeatherForLargeUsCapitals() {
//                                        return cityDirectory.getUsCapitals()
//                                                .flatMap(cityList -> Observable.from(cityList))
//                                                .filter(city -> city.getPopulation() > 500,000)
//                                                .flatMap(city -> weatherService.getCurrentWeather(city)) //each runs in parallel
//                                                .toSortedList((cw1,cw2) -> cw1.getCityName().compare(cw2.getCityName()));
//                                    }

                                        } else {
                                            Log.e(TAG,"Tweet parsed kanji exists code if funky");
                                        }

                                    }

                                }

                            });




                            mRecyclerView.setAdapter(mAdapter);

                            //TODO Make this its own subscribable that we can chain!
                        /* Check most recent user info (if it exists within timeline api response)
                        * against the user info stored in db. update db with any changed ino */
                            //And make it its own void
                            if(mTimeLine != null && mTimeLine.size() > 0) {
                                try {
                                    UserInfo recentUserInfo = mTimeLine.get(0).getUser();
                                    InternalDB.getUserInterfaceInstance(getContext()).compareUserInfoAndUpdate(userInfo,recentUserInfo);

                                } catch (NullPointerException e) {
                                    Log.e(TAG,"Timeline userinfo match problem: " + e);
                                }


                            }

                        }

                        @Override public void onError(Throwable e) {
                            e.printStackTrace();
                            if(BuildConfig.DEBUG){Log.d(TAG, "In onError()");}
                            mCallback.showProgressBar(false);

                            //TODO -- GET PROPER ERROR CODE!
                            Toast.makeText(getContext(), "Unable to get timeline for @" + userInfo.getScreenName(), Toast.LENGTH_SHORT).show();
                        }

                        @Override public void onNext(List<Tweet> timeline) {
                            if(BuildConfig.DEBUG) {
                                Log.d(TAG, "In onNext()");
                                Log.d(TAG,"TIMELINE SIZE: " + timeline.size());
                            }
                            showRecyclerView(true);

                            if(mTimeLine.size() == 0) {

                                for(Tweet tweet : timeline) {
                                    Log.d(TAG,"timeline thing: " + tweet.getIdString());

                                    //Attach colorfavorites to tweet, if they exists in db
                                    if(tweet.getIdString()!=null && tweetIdStringsInFavorites.keySet().contains(tweet.getIdString())) {
                                        tweet.setItemFavorites(tweetIdStringsInFavorites.get(tweet.getIdString()));
                                    } else {
                                        tweet.setItemFavorites(new ItemFavorites());
                                    }

                                    mTimeLine.add(tweet);
                                }
                            }

                        }
                    });
        } else {
            Log.d(TAG,"MTIMELINE WAS NULL SO MAKING THE SHIT INVISIBLE");
            mAdapter = new UserTimeLineAdapter(getContext(),_rxBus,mUserInfo,mTimeLine,mActiveTweetFavoriteStars);
            mRecyclerView.setAdapter(mAdapter);
            mCallback.showProgressBar(false);
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
            mNoLists.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mNoLists.setVisibility(View.VISIBLE);
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

//    @Override
//    public void onResume() {
//        super.onResume();
//        if()
//    }
}

