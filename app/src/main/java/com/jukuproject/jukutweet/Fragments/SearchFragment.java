package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
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
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Adapters.TweetBreakDownAdapter;
import com.jukuproject.jukutweet.Adapters.UserListAdapter;
import com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.AddUserCheckDialog;
import com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Interfaces.TweetListOperationsInterface;
import com.jukuproject.jukutweet.Interfaces.WordEntryFavoritesChangedListener;
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
    private SearchView searchView;
    private String currentSearchText;

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

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);


        //If saved instance state is not null, run the adapter...
        if(savedInstanceState!=null) {
            try {
                mCheckedOption = savedInstanceState.getString("mCheckedOption","Romaji");
                if(mCheckedOption.equals("Twitter")) {
                    mTwitterResults = savedInstanceState.getParcelableArrayList("mTwitterResults");
                    receiveTwitterSearchResults(mTwitterResults,currentSearchText);
                } else if(mCheckedOption.equals("Definition")) {
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
            mCheckedOption = "Romaji";
            mDictionarySearchLayout.setVisibility(View.VISIBLE);
            checkBoxDictionary.setChecked(true);
            checkBoxRomaji.setChecked(true);
            checkBoxDefinition.setChecked(false);
            checkBoxTwitter.setChecked(false);
        }


        checkBoxDictionary.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mDictionarySearchLayout.setVisibility(View.VISIBLE);
                    checkBoxTwitter.setChecked(false);
                    checkBoxRomaji.setText(getResources().getString(R.string.search_romaji));
                    checkBoxDefinition.setText(getResources().getString(R.string.search_definition));
                } else if(!checkBoxTwitter.isChecked()){
                    checkBoxDictionary.setChecked(true);
                }
            }
        });

        checkBoxRomaji.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
//                    mCheckedOption = "Romaji";
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
                    mCheckedOption = "Definition";
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
        checkBoxTwitter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCheckedOption = "Twitter";
                    checkBoxRomaji.setText("User");
                    checkBoxDefinition.setText("Tweet");
                    checkBoxDictionary.setChecked(false);
                    mDictionarySearchLayout.setVisibility(View.VISIBLE);
                } else if(!checkBoxDictionary.isChecked()){
                    checkBoxTwitter.setChecked(true);
                }
            }
        });


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
                            mCallback.runDictionarySearch(query.trim(),"Kanji");
                        } else if(checkBoxDictionary.isChecked() && checkBoxDefinition.isChecked()) {
                            showProgressBar(true);
                            mCallback.runDictionarySearch(query.trim(),"Definition");
                        } else if(checkBoxTwitter.isChecked() && checkBoxRomaji.isChecked()) {
                            if(mCallback.isOnline()) {
                                showProgressBar(true);
                                mCallback.runTwitterSearch(query.trim(),"User");
                            } else {
                                Toast.makeText(getContext(), "Device is not online", Toast.LENGTH_SHORT).show();
                            }
                        } else if(checkBoxTwitter.isChecked() && checkBoxDefinition.isChecked()) {
                            if(mCallback.isOnline()) {
                                showProgressBar(true);
                                mCallback.runTwitterSearch(query.trim(),"Tweet");
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
                    if (event instanceof WordEntry) {
                        WordEntry wordEntry = (WordEntry) event;
                        updateWordEntryItemFavorites(wordEntry);
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
            showRecyclerView(false);
        }
    }


    /**
     * Recieves twitter search results from MainActivity and displays them in the recycler view.
     * When user searches, the search criteria are passed from this fragment to {@link com.jukuproject.jukutweet.MainActivity#runTwitterSearch(String, String)} subscription. When
     * the search finishes, results are passed back to the fragment via this method.
     * @param results List of Tweet results for a twitter search.
     */
    public void receiveTwitterSearchResults(ArrayList<Tweet> results, String queryText) {
        showRecyclerView(true);
        showProgressBar(false);
        if(results.size()>0) {
            mTwitterResults = results;

            mTwitterAdapter = new UserTimeLineAdapter(getContext()
                    ,_rxBus
                    ,mTwitterResults
                    ,mActiveTweetFavoriteStars
                    ,mMetrics
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

                    if(isUniqueClick(1000) && event instanceof Tweet) {
                        final Tweet tweet = (Tweet) event;

                        //Try to insert urls
                        final TweetListOperationsInterface helperTweetOps = InternalDB.getTweetInterfaceInstance(getContext());
                        helperTweetOps.saveTweetUrls(tweet);

                        //Try to insert Kanji if they do not already exist
                        if(helperTweetOps.tweetParsedKanjiExistsInDB(tweet) == 0) {
                            mCallback.parseAndSaveTweet(tweet);
                        } else {
                            Log.e(TAG,"Tweet parsed kanji exists code is funky");
                        }

                    }

                }

            });
            mRecyclerView.setAdapter(mTwitterAdapter);
        } else {
            showRecyclerView(false);
        }
    }

    /**
     * Receives twitter search results from MainActivity and displays them in the recycler view.
     * When user searches, the search criteria are passed from this fragment to {@link com.jukuproject.jukutweet.MainActivity#runTwitterSearch(String, String)} subscription. When
     * the search finishes, results are passed back to the fragment via this method.
     * @param results List of Tweet results for a twitter search.
     */
    public void receiveTwitterUserSearchResults(ArrayList<UserInfo> results) {

        showRecyclerView(true);
        showProgressBar(false);

        if(results.size()>0) {
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

    public void updateWordEntryFavoritesForOtherTabs(WordEntry wordEntry) {
        mCallback.notifySavedWordFragmentsChanged(wordEntry);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("mCheckedOption", mCheckedOption);
        outState.putParcelableArrayList("mTwitterResults", mTwitterResults);
        outState.putParcelableArrayList("mDictionaryResults", mDictionaryResults);
        outState.putParcelableArrayList("mTwitterUserResults",mTwitterUserResults);
        outState.putBoolean("mIsShowingProgressBar",mIsShowingProgressBar);

    }

    @Override
    public void onPause() {
        if(searchView!=null) {
            searchView.clearFocus();
        }
        super.onPause();
    }
}

