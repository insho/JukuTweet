package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Adapters.TweetBreakDownAdapter;
import com.jukuproject.jukutweet.Adapters.UserListAdapter;
import com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.ChooseFavoriteListsPopupWindow;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.AddUserCheckDialog;
import com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog;
import com.jukuproject.jukutweet.FavoritesColors;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Interfaces.WordEntryFavoritesChangedListener;
import com.jukuproject.jukutweet.MainActivity;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;
import com.jukuproject.jukutweet.TabContainers.BaseContainerFragment;

import java.util.ArrayList;

import rx.functions.Action1;

/**
 * Search tab, allowing user to search Edict dictionary (offline), or Twitter API (online). Dictionary search
 * can be on a Kanji, romaji, or definition, and will return a list of WordEntry objects like those in {@link TweetBreakDownFragment}. User
 * can add them to a list by clicking on the favorites star or long click for {@link WordDetailPopupDialog}.
 *
 * Twitter API search can be for a Twitter User (in which case the results will be a list of {@link UserInfo} objects akin to the {@link UserListFragment}),
 * or for text contained in a Tweet, in which case the result set will be a list of {@link Tweet} objects like in {@link UserTimeLineFragment}.
 */
public class SearchFragment extends Fragment implements WordEntryFavoritesChangedListener {
    FragmentInteractionListener mCallback;

    /*Tracks elapsed time since last click of a recyclerview row. Used to
    * keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
    private RxBus _rxBus = new RxBus();

    private TextView mNoLists;
    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    private AppCompatCheckBox checkBoxRomaji;
    private AppCompatCheckBox checkBoxDefinition;
    private AppCompatCheckBox checkBoxTwitter;
    private AppCompatCheckBox checkBoxDictionary;
    private LinearLayoutManager mLayoutManager;

    private SearchView searchView;
    private String currentSearchText;
    private String currentActiveTwitterSearchQuery; // twitter query that resulted in current result set (can be different than currentSearchText if user has changed it)


    private String mCheckedOption;
    private ArrayList<WordEntry> mDictionaryResults;
    private ArrayList<Tweet> mTwitterResults;
    private ArrayList<UserInfo> mTwitterUserResults;
    private DisplayMetrics mMetrics;

    private ArrayList<String> mActiveFavoriteStars;
    private ArrayList<String> mActiveTweetFavoriteStars;
    private LinearLayout mDictionarySearchLayout;
    private TweetBreakDownAdapter mDictionaryAdapter;
    private UserTimeLineAdapter mTwitterAdapter;
    private boolean mIsShowingProgressBar;
    private Long mDataSetMaxId;
    private Integer mPreviousMaxScrollPosition =0;
    private final String TAG = "TEST-searchfrag";
    private View mDividerView;

    public SearchFragment() {}

    public static SearchFragment newInstance() {
        return new SearchFragment();
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
        View view = inflater.inflate(R.layout.search, container, false);

        mDictionarySearchLayout = (LinearLayout) view.findViewById(R.id.searchOnOptionLayout);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.search_recycler);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        checkBoxTwitter = (AppCompatCheckBox) view.findViewById(R.id.checkBoxTwitter);
        checkBoxDictionary = (AppCompatCheckBox) view.findViewById(R.id.checkBoxDictionary);
        checkBoxRomaji = (AppCompatCheckBox) view.findViewById(R.id.checkBoxRomaji);
        checkBoxDefinition = (AppCompatCheckBox) view.findViewById(R.id.checkBoxDefinition);
        searchView = (SearchView) view.findViewById(R.id.dbsearch);
        mNoLists = (TextView) view.findViewById(R.id.noresults);
        mDividerView = (View) view.findViewById(R.id.dividerview);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

         /* Get metrics to pass density/width/height to adapters */
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);

        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(getContext());
        mActiveTweetFavoriteStars = SharedPrefManager.getInstance(getContext()).getActiveTweetFavoriteStars();
        mActiveFavoriteStars = sharedPrefManager.getActiveFavoriteStars();

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);


        //If saved instance state is not null, run the adapter...
        if(savedInstanceState!=null) {

            if(!savedInstanceState.getBoolean("mDataSetMaxIdisNull",true)) {
                mDataSetMaxId = savedInstanceState.getLong("mDataSetMaxId");
            } else  {
                mDataSetMaxId = null;
            }

            currentActiveTwitterSearchQuery = savedInstanceState.getString("currentActiveTwitterSearchQuery",currentActiveTwitterSearchQuery);
            mCheckedOption = savedInstanceState.getString("mCheckedOption","Romaji");

            Log.i(TAG,"checkoption: " + mCheckedOption + ", currentactive: " + currentActiveTwitterSearchQuery);
            try {

                if(mCheckedOption.equals("Tweet")) {
                    mTwitterResults = savedInstanceState.getParcelableArrayList("mTwitterResults");
                    receiveTwitterSearchResults(mTwitterResults,currentActiveTwitterSearchQuery);
                } else if(mCheckedOption.equals("User")) {
                    mTwitterUserResults = savedInstanceState.getParcelableArrayList("mTwitterUserResults");
                    receiveTwitterUserSearchResults(mTwitterUserResults);
                } else if(mCheckedOption.equals("Definition") || mCheckedOption.equals("Romaji")) {
                    mDictionaryResults = savedInstanceState.getParcelableArrayList("mDictionaryResults");
                    recieveDictionarySearchResults(mDictionaryResults);
                }

                /* If the activity was destroyed and recreated during an ongoing search, show the progress bar
                * when activity recreates itself */
                mIsShowingProgressBar = savedInstanceState.getBoolean("mIsShowingProgressBar");
                if(mIsShowingProgressBar) {
                    showProgressBar(true);
                }
            } catch (NullPointerException e) {
                mCheckedOption = "Romaji";
                mDictionarySearchLayout.setVisibility(View.VISIBLE);
                checkBoxDictionary.setChecked(true);
                checkBoxRomaji.setChecked(true);
                checkBoxDefinition.setChecked(false);
                checkBoxTwitter.setChecked(false);
                Log.e(TAG,"Nullpointer exception in loading saved state on search fragment");
            }

        } else {
            mDataSetMaxId = null;
            mCheckedOption = "Romaji";
            mDictionarySearchLayout.setVisibility(View.VISIBLE);
            checkBoxDictionary.setChecked(true);
            checkBoxRomaji.setChecked(true);
            checkBoxDefinition.setChecked(false);
            checkBoxTwitter.setChecked(false);
        }



        setUpCheckBoxes();

        currentSearchText= null;

        /* Set the sub-criteria checkboxes visible every time the searchview is clicked*/
        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    mDictionarySearchLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                /* Check to make sure the query is valid. If so, run the query. */
                if(query.trim().length()>25) {
                    Toast.makeText(getContext(), getString(R.string.searchquerytoolong), Toast.LENGTH_LONG).show();
                }else if(query.trim().length()==0) {
                    Toast.makeText(getContext(), getString(R.string.searchinputtexttosearch), Toast.LENGTH_LONG).show();
                } else if (!checkBoxDefinition.isChecked() &&!checkBoxRomaji.isChecked()){
                    Toast.makeText(getContext(), getString(R.string.searchchoosecriteria), Toast.LENGTH_LONG).show();
                } else {
                        /* Run the query */
                        showRecyclerView(true);

                        searchView.clearFocus();
                        mDictionarySearchLayout.setVisibility(View.GONE);
                        if(checkBoxDictionary.isChecked() && checkBoxRomaji.isChecked()) {
                            showProgressBar(true);
                            mDataSetMaxId = null;
                            mPreviousMaxScrollPosition = 0;
                            mCallback.runDictionarySearch(query.trim(),"Kanji");
                        } else if(checkBoxDictionary.isChecked() && checkBoxDefinition.isChecked()) {
                            showProgressBar(true);
                            mDataSetMaxId = null;
                            mPreviousMaxScrollPosition = 0;
                            mCallback.runDictionarySearch(query.trim(),"Definition");
                        } else if(checkBoxTwitter.isChecked() && checkBoxRomaji.isChecked()) {
                            if(mCallback.isOnline()) {
                                showProgressBar(true);
                                mDataSetMaxId = null;
                                mPreviousMaxScrollPosition = 0;
                                currentActiveTwitterSearchQuery = query.trim();
                                mCallback.runTwitterSearch(currentActiveTwitterSearchQuery,"User",mDataSetMaxId);
                            } else {
                                Toast.makeText(getContext(), "Device is not online", Toast.LENGTH_SHORT).show();
                            }
                        } else if(checkBoxTwitter.isChecked() && checkBoxDefinition.isChecked()) {
                            if(mCallback.isOnline()) {
                                mDataSetMaxId = null;
                                mPreviousMaxScrollPosition = 0;
                                showProgressBar(true);
                                currentActiveTwitterSearchQuery = query.trim();
                                mCallback.runTwitterSearch(currentActiveTwitterSearchQuery,"Tweet",mDataSetMaxId);
                            } else {
                                Toast.makeText(getContext(), "Device is not online", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Select a search option", Toast.LENGTH_SHORT).show();
                        }
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if(currentSearchText!=null && currentSearchText.length()>newText.length()){

                    mNoLists.setVisibility(TextView.GONE);

                }
                currentSearchText = newText;
                return false;
            }
        });


        /* Listen for the user scrolling to the final position in the scrollview. IF it happens, load more
        * userinfo items into the adapter */
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            mRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    addMoreTwitterSearchResults();
                }
            });
        } else {
            mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    addMoreTwitterSearchResults();
                }
            });
        }

    }


    private void setUpCheckBoxes() {
        checkBoxDictionary.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if(checkBoxDefinition.isChecked()) {
                        mCheckedOption = "Definition";
                    } else {
                        mCheckedOption = "Romaji";
                    }
                    mDictionarySearchLayout.setVisibility(View.VISIBLE);
                    checkBoxTwitter.setChecked(false);
                    checkBoxRomaji.setText(getResources().getString(R.string.search_romaji));
                    checkBoxDefinition.setText(getResources().getString(R.string.search_definition));
                } else if(!checkBoxTwitter.isChecked()){
                    checkBoxDictionary.setChecked(true);
                }
            }
        });

        checkBoxTwitter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if(checkBoxDefinition.isChecked()) {
                        mCheckedOption = "Tweet";
                    } else {
                        mCheckedOption = "User";
                    }

                    checkBoxRomaji.setText(getString(R.string.search_user_text));
                    checkBoxDefinition.setText(getString(R.string.search_tweet_text));
                    checkBoxDictionary.setChecked(false);
                    mDictionarySearchLayout.setVisibility(View.VISIBLE);
                } else if(!checkBoxDictionary.isChecked()){
                    checkBoxTwitter.setChecked(true);
                }
            }
        });


        checkBoxRomaji.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if(checkBoxTwitter.isChecked()) {
                        mCheckedOption = "User";
                    } else {
                        mCheckedOption = "Romaji";
                    }
                    checkBoxDefinition.setChecked(false);
                } else if(!checkBoxDefinition.isChecked()){
                    checkBoxRomaji.setChecked(true);
                }
            }
        });

        checkBoxDefinition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if(checkBoxTwitter.isChecked()) {
                        mCheckedOption = "Tweet";
                    } else {
                        mCheckedOption = "Definition";
                    }
                    checkBoxRomaji.setChecked(false);
                } else if(!checkBoxRomaji.isChecked()){
                    checkBoxDefinition.setChecked(true);
                }

            }
        });
        checkBoxDictionary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDictionarySearchLayout.setVisibility(View.VISIBLE);
            }
        });
        checkBoxTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDictionarySearchLayout.setVisibility(View.VISIBLE);
            }
        });

    }


    /**
     * When user scrolls to the bottom of a twitter search result set, decides whether or not to pull more
     * results from the Twitter API and add them to the dataset.
     */
    private void addMoreTwitterSearchResults() {
        if(!checkBoxDictionary.isChecked()
                && checkBoxDefinition.isChecked()
                && mLayoutManager != null
                && mTwitterResults!=null
                && currentActiveTwitterSearchQuery !=null
                && mTwitterResults.size()>0
                && mLayoutManager.findFirstCompletelyVisibleItemPosition()>0
                && mLayoutManager.findLastCompletelyVisibleItemPosition()==mTwitterResults.size()-1
                && mTwitterResults.size()-1>mPreviousMaxScrollPosition) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "pulling timeline after scroll. dataset size: " + mTwitterResults.size() + ", prev pos: " + mPreviousMaxScrollPosition);
            }


            mPreviousMaxScrollPosition = mTwitterResults.size() - 1;
            if (!mCallback.isOnline()) {
                Toast.makeText(getContext(), "Device is not online", Toast.LENGTH_SHORT).show();
            } else {
                showProgressBar(true);
                mCallback.runTwitterSearch(currentActiveTwitterSearchQuery, "Tweet", mDataSetMaxId);
            }
        }
    }


    /**
     * Toggles between showing recycler (if there are followed users in the database)
     * and hiding the recycler while showing the "no users found" message if there are not
     * @param show bool True to show recycler, False to hide it
     */
    public void showRecyclerView(boolean show) {
        if(show) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mNoLists.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            mNoLists.setVisibility(View.VISIBLE);
        }

    }


    /**
     * Recieves dictionary search results from MainActivity and displays them in the recycler view.
     * When user searches, the search criteria are passed from this fragment to {@link com.jukuproject.jukutweet.MainActivity#runDictionarySearch(String, String)} subscription. When
     * the search finishes, results are passed back to the fragment via this method.
     * @param results List of WordEntry results for a dictionary search.
     */
    public void recieveDictionarySearchResults(ArrayList<WordEntry> results) {
        mDataSetMaxId = null;
        mTwitterResults = null;
        mTwitterUserResults = null;

        showRecyclerView(true);
        showProgressBar(false);
        if(results.size()>0) {
            RxBus rxBus = new RxBus();
            mDictionaryResults = results;
            mDictionaryAdapter = new TweetBreakDownAdapter(getContext()
                    ,mMetrics
                    ,mDictionaryResults
                    ,mActiveFavoriteStars
                    ,rxBus);
            rxBus.toClickObserverable().subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {

                    if (isUniqueClick(150) && event instanceof Integer) {

                        Integer tweetWordEntryIndex = (Integer) event;
                        //Activity windows height
                        int[] location = new int[2];

                        TweetBreakDownAdapter.ViewHolder viewHolder = (TweetBreakDownAdapter.ViewHolder)mRecyclerView.getChildViewHolder(mRecyclerView.getChildAt(tweetWordEntryIndex));
                        mRecyclerView.getChildAt(tweetWordEntryIndex).getLocationInWindow(location);
                        Log.i(TAG,"location x: " + location[0] + ", y: " + location[1]);

                        showFavoriteListPopupWindow(viewHolder,mDictionaryResults.get(tweetWordEntryIndex),mMetrics,location[1]);

                    } else if (isUniqueClick(150) && event instanceof WordEntry) {
                        WordEntry wordEntry = (WordEntry) event;
                        updateWordEntryFavoritesForOtherTabs(wordEntry);

                    }
                }
            });

            rxBus.toLongClickObserverable().subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof WordEntry) {
                        WordEntry wordEntry = (WordEntry) event;
                        WordDetailPopupDialog wordDetailPopupDialog = WordDetailPopupDialog.newInstance(wordEntry);
                        wordDetailPopupDialog.setTargetFragment(SearchFragment.this, 0);
                        wordDetailPopupDialog.show(getFragmentManager(),"wordDetailPopup");
                    }
                }
            });

            mRecyclerView.setAdapter(mDictionaryAdapter);
        } else {
            if(mDictionaryResults!=null) {
                mDictionaryResults.clear();
            }
            showRecyclerView(false);
        }
    }


    /**
     * Recieves twitter search results from MainActivity and displays them in the recycler view.
     * When user searches, the search criteria are passed from this fragment to {@link com.jukuproject.jukutweet.MainActivity#runTwitterSearch(String, String, Long)} subscription. When
     * the search finishes, results are passed back to the fragment via this method.
     * @param results List of Tweet results for a twitter search.
     */
    public void receiveTwitterSearchResults(ArrayList<Tweet> results, String queryText) {
        showRecyclerView(true);
        showProgressBar(false);
        if(results.size()>0) {

            mTwitterUserResults = null;
            mTwitterResults = results;

            if(currentActiveTwitterSearchQuery!=null
                    && currentActiveTwitterSearchQuery.equals(queryText)
                    && mTwitterAdapter!=null) {
                mTwitterResults.addAll(results);
                mTwitterAdapter.notifyDataSetChanged();

                if(mTwitterResults.size()-1>mPreviousMaxScrollPosition ) {
                    Log.d(TAG,"SCrolling to position...");
                    mRecyclerView.scrollToPosition(mPreviousMaxScrollPosition);
                }
            } else {
                mTwitterAdapter = new UserTimeLineAdapter(getContext()
                        ,_rxBus
                        ,mTwitterResults
                        ,mActiveTweetFavoriteStars
                        ,queryText
                        ,true);



                _rxBus.toClickObserverable()
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object event) {

                                if(isUniqueClick(1000) && event instanceof Tweet) {
                                    Tweet tweet = (Tweet) event;
                                    TweetBreakDownFragment fragment = TweetBreakDownFragment.newInstanceTimeLine(tweet);
                                    ((BaseContainerFragment)getParentFragment()).addFragment(fragment, true,"tweetbreakdownSearch");
                                }

                            }

                        });

                _rxBus.toLongClickObserverable()
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object event) {

                                if(isUniqueClick(1000) && event instanceof UserInfo) {
                                    UserInfo userInfo = (UserInfo) event;
                                    mCallback.showAddUserCheckDialog(userInfo);
                                }

                            }

                        });

                _rxBus.toSaveTweetObserverable().subscribe(new Action1<Object>() {

                    @Override
                    public void call(Object event) {

                        if (isUniqueClick(100) && event instanceof Integer) {

                            Integer tweetWordEntryIndex = (Integer) event;
                            //Activity windows height
                            int[] location = new int[2];

                            UserTimeLineAdapter.ViewHolder viewHolder = (UserTimeLineAdapter.ViewHolder)mRecyclerView.getChildViewHolder(mRecyclerView.getChildAt(tweetWordEntryIndex));
                            mRecyclerView.getChildAt(tweetWordEntryIndex).getLocationInWindow(location);
//                    Log.i(TAG,"location x: " + location[0] + ", y: " + location[1]);

                            showTweetFavoriteListPopupWindow(viewHolder,mTwitterResults.get(tweetWordEntryIndex),mMetrics,location[1]);

                        } else if(isUniqueClick(150) && event instanceof Tweet) {
                            final Tweet tweet = (Tweet) event;
                            saveOrDeleteTweet(tweet);
                        }
//
//                        if(isUniqueClick(1000) && event instanceof Tweet) {
//                            final Tweet tweet = (Tweet) event;
////                            if(tweet.getUser()!=null) {
////                                mCallback.downloadTweetUserIcons(tweet.getUser());
////                            }
////                            if(InternalDB.getTweetInterfaceInstance(getContext()).tweetParsedKanjiExistsInDB(tweet) == 0) {
////                                mCallback.parseAndSaveTweet(tweet);
////                            } else {
////                                Log.e(TAG,"Tweet parsed kanji exists code is funky");
////                            }
////                            mCallback.notifySavedTweetFragmentsChanged();
//                            saveOrDeleteTweet(tweet);
//                        }

                    }

                });
                mRecyclerView.setAdapter(mTwitterAdapter);
            }

            mDataSetMaxId = (Long) Long.valueOf(mTwitterResults.get(mTwitterResults.size()-1).getIdString());


        } else {
            if(mTwitterResults!=null) {
                mTwitterResults.clear();
            }

            showRecyclerView(false);
        }
    }

    /**
     * Receives twitter search results from MainActivity and displays them in the recycler view.
     * When user searches, the search criteria are passed from this fragment to {@link com.jukuproject.jukutweet.MainActivity#runTwitterSearch(String, String, Long)} subscription. When
     * the search finishes, results are passed back to the fragment via this method.
     * @param results List of Tweet results for a twitter search.
     */
    public void receiveTwitterUserSearchResults(ArrayList<UserInfo> results) {

        showRecyclerView(true);
        showProgressBar(false);



        if(results.size()>0) {

            mTwitterResults = null;
            mDataSetMaxId = null;
            mTwitterUserResults = results;

           UserListAdapter listAdapter = new UserListAdapter(getContext()
                    ,mTwitterUserResults
                   ,_rxBus);

            _rxBus.toClickObserverable()
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {

                            if(isUniqueClick(1000) && event instanceof UserInfo) {
                                UserInfo userInfo = (UserInfo) event;
                                if(getFragmentManager().findFragmentByTag("dialogAddCheck") == null || !getFragmentManager().findFragmentByTag("dialogAddCheck").isAdded()) {
                                    AddUserCheckDialog.newInstance(userInfo).show(getFragmentManager(),"dialogAddCheck");
                                }
                            }

                        }

                    });
            mRecyclerView.setAdapter(listAdapter);
        } else {
            if(mTwitterUserResults!=null) {
                mTwitterUserResults.clear();
            }
            showRecyclerView(false);
        }
    }

    /**
     * Show/hide progress bar while search is active but unfinished
     * @param show true to show progress bar, false to hide
     */
    public void showProgressBar(boolean show){
        if(show) {
            mIsShowingProgressBar = true;
            progressBar.setVisibility(View.VISIBLE);
            mDividerView.setVisibility(View.GONE);

        } else {
            mIsShowingProgressBar = false;
            progressBar.setVisibility(View.GONE);
            mDividerView.setVisibility(View.VISIBLE);
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


    /**
     * Verifies if this fragment is showing some search results or not. When user clicks "onBackPressed"
     * if there are search results, activate {@link #showRecyclerView(boolean)} to false, otherwise pop the fragment and exit the app
     * @return true if there are active search results to clear, false if not
     */
    public boolean clearSearchResults() {
        if(mRecyclerView!= null
                && mRecyclerView.getAdapter()!=null
                && mRecyclerView.getAdapter().getItemCount()>0
                && mRecyclerView.getVisibility()==View.VISIBLE) {
            mRecyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            mNoLists.setVisibility(View.GONE);
            searchView.setQuery("",false);
            searchView.clearFocus();
            return true;
        } else {
            return false;
        }
    }


    /**
     * Updates WordEntries in the dataset of dictionary results when the user has changed the
     * favorites for a word in the {@link WordDetailPopupDialog} via the {@link WordEntryFavoritesChangedListener}
     * @param wordEntry WordEntry for the word that has new itemfavorites info
     */
    public void updateWordEntryItemFavorites(WordEntry wordEntry) {

        if(mDictionaryResults!=null ) {
            for(WordEntry tweetWordEntry : mDictionaryResults) {
                if(tweetWordEntry.getId().equals(wordEntry.getId())) {

                    tweetWordEntry.setItemFavorites(wordEntry.getItemFavorites());
                }
            }
            mDictionaryAdapter.notifyDataSetChanged();
        }

    }

    /**
     * If a group of word entries has been saved in another fragment, the message relayed to {@link MainActivity#notifySavedWordFragmentsChanged(String)}
     * , which then notifies any open tabs in the other fragments which might be affected by the new word that the change
     * has been made. This method recieves the udpdated list of words and cycles through them, looking for matches. If a match
     * is found, the {@link com.jukuproject.jukutweet.Models.ItemFavorites} object for the word is updated to reflect the new favorite list/s
     * @param updatedWordEntries ArrayList of WordEntries that were saved to/removed from a new list
     */
    public void updateWordEntryItemFavorites(ArrayList<WordEntry> updatedWordEntries) {

        if(mDictionaryResults!=null ) {
            for(WordEntry dataSetWordEntry : mDictionaryResults) {
                for(WordEntry updatedWordEntry : updatedWordEntries ) {
                    if(dataSetWordEntry.getId().equals(updatedWordEntry.getId())) {
                        dataSetWordEntry.setItemFavorites(updatedWordEntry.getItemFavorites());
                    }
                }
            }
            mDictionaryAdapter.notifyDataSetChanged();
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



    /**
     * Decides via {@link com.jukuproject.jukutweet.Interfaces.TweetListOperationsInterface#saveOrDeleteTweet(Tweet)} whether to
     * save the tweet or remove it from the db (if it already exists and is unnecessary). If it saves the tweet, it also decides via
     * whether or not to download the tweet icon with a callback to the "downloadTweetUserIcons" method in the activity. Lastly
     * it notifies tweet related fragments that a change has been made
     * @param tweet Tweet to be saved or deleted
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
     * Displays the {@link ChooseFavoriteListsPopupWindow} when the "favorites star" is clicked for a
     * word in one of the TweetBreakDown recycler rows
     * @param holder ViewHolder for the row
     */
    public void showFavoriteListPopupWindow(final TweetBreakDownAdapter.ViewHolder holder
            ,final WordEntry wordEntry
            , DisplayMetrics mMetrics
            , int ylocation) {
        RxBus rxBus = new RxBus();

        ArrayList<MyListEntry> availableFavoriteLists = InternalDB.getWordInterfaceInstance(getContext()).getWordListsForAWord(mActiveFavoriteStars,String.valueOf(wordEntry.getId()),1,null);
        PopupWindow popupWindow =  ChooseFavoriteListsPopupWindow.createWordFavoritesPopup(getContext(),mMetrics,rxBus,availableFavoriteLists,wordEntry.getId());
        popupWindow.getContentView().measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        int xadjust = popupWindow.getContentView().getMeasuredWidth() + (int) (15 * mMetrics.density + 0.5f);

        int popupMeasuredHeight;
        if(availableFavoriteLists.size()>10) {
            popupMeasuredHeight = (int)((float)mMetrics.heightPixels/2.0f);
        } else {
            popupMeasuredHeight =  popupWindow.getContentView().getMeasuredHeight();
        }

        //Firstly, center the list at the favorite star
        int yadjust = (int)(popupMeasuredHeight/2.0f  + holder.imgStar.getMeasuredHeight()/2.0f);
        final int displayheight = mMetrics.heightPixels;

        if(BuildConfig.DEBUG) {
            Log.i(TAG,"INITIAL yloc: " + ylocation);
            Log.i(TAG,"INITIAL imgstar height/2: " + holder.imgStar.getMeasuredHeight()/2.0f);
            Log.i(TAG,"INITIAL popupheight/2: " + popupMeasuredHeight/2.0f);
            Log.i(TAG,"INITIAL yadjust: " + yadjust);
            Log.i(TAG,"INITIAL Displayheight: " + displayheight);
        }

        //Overrun at bottom of screen
        while(ylocation + holder.imgStar.getMeasuredHeight() - yadjust + popupMeasuredHeight >displayheight) {
//            Log.i(TAG,"SCREEN OVERRUN BOTTOM - " + (ylocation + holder.imgStar.getMeasuredHeight() - yadjust + popupMeasuredHeight)
//                    + " to display: " + displayheight + ",, reduce yadjust " + yadjust + " - " + (yadjust+10));
            yadjust += 10;
        }

//        //Overrun at bottom of screen
        while(ylocation + holder.imgStar.getMeasuredHeight() - yadjust < 0) {
//            Log.i(TAG,"SCREEN OVERRUN TOP - increase yadjust " + yadjust + " - " + (yadjust-10));
            yadjust -= 10;
        }

        if(BuildConfig.DEBUG) {
            Log.d("TEST", "pop width: " + popupWindow.getContentView().getMeasuredWidth() + " height: " + popupWindow.getContentView().getMeasuredHeight());
            Log.d("TEST", "xadjust: " + xadjust + ", yadjust: " + yadjust);
        }

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                updateWordEntryFavoritesForOtherTabs(wordEntry);
            }
        });

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

                    if(wordEntry.getItemFavorites().shouldOpenFavoritePopup(mActiveFavoriteStars)
                            && wordEntry.getItemFavorites().systemListCount(mActiveFavoriteStars) >1) {
                        holder.imgStar.setColorFilter(null);
                        holder.imgStar.setImageResource(R.drawable.ic_star_multicolor);

                    } else {
                        holder.imgStar.setImageResource(R.drawable.ic_star_black);
                        try {
                            holder.imgStar.setColorFilter(ContextCompat.getColor(getContext(), FavoritesColors.assignStarColor(wordEntry.getItemFavorites(),mActiveFavoriteStars)));
                        } catch (NullPointerException e) {
                            Log.e(TAG,"tweetBreakDownAdapter Nullpointer error setting star color filter in word detail popup dialog... Need to assign item favorites to WordEntry(?)" + e.getCause());
                        }
                    }
                }
            }

        });

        popupWindow.showAsDropDown(holder.imgStar,-xadjust,-yadjust);

    }


    public void showTweetFavoriteListPopupWindow(final UserTimeLineAdapter.ViewHolder holder
            ,final Tweet mTweet
            , DisplayMetrics mMetrics
            , int ylocation) {

        RxBus rxBus = new RxBus();
        ArrayList<MyListEntry> availableFavoriteLists = InternalDB.getTweetInterfaceInstance(getContext()).getTweetListsForTweet(mActiveTweetFavoriteStars,mTweet.getIdString(),null);

        PopupWindow popupWindow = ChooseFavoriteListsPopupWindow.createTweetFavoritesPopup(getContext(),mMetrics,rxBus,availableFavoriteLists,mTweet.getIdString(), mTweet.getUser().getUserId());

        popupWindow.getContentView().measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        int xadjust = popupWindow.getContentView().getMeasuredWidth() + (int) (10 * mMetrics.density + 0.5f);

        int popupMeasuredHeight;
        if(availableFavoriteLists.size()>10) {
            popupMeasuredHeight = (int)((float)mMetrics.heightPixels/2.0f);
        } else {
            popupMeasuredHeight =  popupWindow.getContentView().getMeasuredHeight();
        }

        //Firstly, center the list at the favorite star
        int yadjust = (int)(popupMeasuredHeight/2.0f  + holder.imgStar.getMeasuredHeight()/2.0f);
        final int displayheight = mMetrics.heightPixels;

        if(BuildConfig.DEBUG) {
            Log.i(TAG,"INITIAL yloc: " + ylocation);
            Log.i(TAG,"INITIAL imgstar height/2: " + holder.imgStar.getMeasuredHeight()/2.0f);
            Log.i(TAG,"INITIAL popupheight/2: " + popupMeasuredHeight/2.0f);
            Log.i(TAG,"INITIAL yadjust: " + yadjust);
            Log.i(TAG,"INITIAL Displayheight: " + displayheight);
        }

        //Overrun at bottom of screen
        while(ylocation + holder.imgStar.getMeasuredHeight() - yadjust + popupMeasuredHeight >displayheight) {
//            Log.i(TAG,"SCREEN OVERRUN BOTTOM - " + (ylocation + holder.imgStar.getMeasuredHeight() - yadjust + popupMeasuredHeight)
//                    + " to display: " + displayheight + ",, reduce yadjust " + yadjust + " - " + (yadjust+10));
            yadjust += 10;
        }

//        //Overrun at bottom of screen
        while(ylocation + holder.imgStar.getMeasuredHeight() - yadjust < 0) {
//            Log.i(TAG,"SCREEN OVERRUN TOP - increase yadjust " + yadjust + " - " + (yadjust-10));
            yadjust -= 10;
        }



        Log.d("TEST","pop width: " + popupWindow.getContentView().getMeasuredWidth() + " height: " + popupWindow.getContentView().getMeasuredHeight());
        Log.d("TEST","xadjust: " + xadjust + ", yadjust: " + yadjust);


        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {



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

                                    /* Recieve a MyListEntry (containing an updated list entry for this row kanji) from
                                    * the ChooseFavoritesAdapter in the ChooseFavorites popup window */
                if(event instanceof MyListEntry) {
                    MyListEntry myListEntry = (MyListEntry) event;

                                        /* Ascertain the type of list that the kanji was added to (or subtracted from),
                                        and update that list's count */
                    if(myListEntry.getListsSys() == 1) {
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
                        holder.imgStar.setColorFilter(null);
                        holder.imgStar.setImageResource(R.drawable.ic_twitter_multicolor_24dp);

                    } else {
                        holder.imgStar.setImageResource(R.drawable.ic_twitter_black_24dp);
                        try {
                            holder.imgStar.setColorFilter(ContextCompat.getColor(getContext(), FavoritesColors.assignStarColor(mTweet.getItemFavorites(),mActiveTweetFavoriteStars)));
                        } catch (NullPointerException e) {
                            Log.e(TAG,"UserTimeLineAdapter setting colorfilter nullpointer: " + e.getMessage());
                        }

                    }
                }
            }

        });

        popupWindow.showAsDropDown(holder.imgStar,-xadjust,-yadjust);
    }


    @Override
    public void onPause() {
        if(searchView!=null) {
            searchView.clearFocus();
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mDataSetMaxId!=null) {
            outState.putLong("mDataSetMaxId",mDataSetMaxId);
            outState.putBoolean("mDataSetMaxIdisNull",false);
        }

        outState.putString("mCheckedOption", mCheckedOption);
        outState.putParcelableArrayList("mTwitterResults", mTwitterResults);
        outState.putParcelableArrayList("mDictionaryResults", mDictionaryResults);
        outState.putParcelableArrayList("mTwitterUserResults",mTwitterUserResults);
        outState.putBoolean("mIsShowingProgressBar",mIsShowingProgressBar);
        outState.putString("currentActiveTwitterSearchQuery",currentActiveTwitterSearchQuery);
    }

}

