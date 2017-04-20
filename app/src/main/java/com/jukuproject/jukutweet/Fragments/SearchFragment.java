package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
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
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Adapters.TweetBreakDownAdapter;
import com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.functions.Action1;

import static com.jukuproject.jukutweet.Adapters.ChooseFavoritesAdapter.setAppCompatCheckBoxColors;


public class SearchFragment extends Fragment {
    FragmentInteractionListener mCallback;

    /*Tracks elapsed time since last click of a recyclerview row. Used to
    * keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
    private RxBus _rxBus = new RxBus();

    private TextView mNoLists;
    private RecyclerView mRecyclerView;
    private LinearLayout searchtoplayout;
    private ProgressBar progressBar;
//    private TextView nothingfound;
    AppCompatCheckBox checkBoxRomaji;
    AppCompatCheckBox checkBoxDefinition;
    AppCompatCheckBox checkBoxTwitter;
    AppCompatCheckBox checkBoxDictionary;
    SearchView searchView;
    String currentSearchText;

    String mCheckedOption;
    ArrayList<WordEntry> mDictionaryResults;
    ArrayList<Tweet> mTwitterResults;

    DisplayMetrics mMetrics;

    ColorThresholds mColorThresholds;
    ArrayList<String> mActiveFavoriteStars;
    ArrayList<String> mActiveTweetFavoriteStars;
    LinearLayout mDictionarySearchLayout;

    private final String TAG = "TEST-searchfrag";
    private View mDividerView;
    public SearchFragment() {}

    /**
     * Returns a new instance of UserListFragment
     */
    public static SearchFragment newInstance() {
//        UserListFragment fragment = new UserListFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
        return new SearchFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search, container, false);

        mDictionarySearchLayout = (LinearLayout) view.findViewById(R.id.searchOnOptionLayout);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.search_recycler);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        searchtoplayout = (LinearLayout) view.findViewById(R.id.searchtoplayout);
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
        mColorThresholds = sharedPrefManager.getColorThresholds();
        mActiveTweetFavoriteStars = SharedPrefManager.getInstance(getContext()).getActiveTweetFavoriteStars();
        mActiveFavoriteStars = sharedPrefManager.getActiveFavoriteStars();







//        checkBoxDictionary.setText(getString(R.string.search_dictionary));
//        checkBoxTwitter.setText(getString(R.string.search_twitter));
//        checkBoxRomaji.setText(getString(R.string.search_romaji));
//        checkBoxDefinition.setText(getString(R.string.search_definition));

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        setAppCompatCheckBoxColors(checkBoxDictionary, ContextCompat.getColor(getContext(), android.R.color.black), ContextCompat.getColor(getContext(), android.R.color.black));
        setAppCompatCheckBoxColors(checkBoxRomaji, ContextCompat.getColor(getContext(), android.R.color.black), ContextCompat.getColor(getContext(), android.R.color.black));
        setAppCompatCheckBoxColors(checkBoxDefinition, ContextCompat.getColor(getContext(), android.R.color.black), ContextCompat.getColor(getContext(), android.R.color.black));
        setAppCompatCheckBoxColors(checkBoxTwitter, ContextCompat.getColor(getContext(), android.R.color.black), ContextCompat.getColor(getContext(), android.R.color.black));
//
//        checkBoxRomaji.setHighlightColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
//        checkBoxDefinition.setHighlightColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
//        checkBoxTwitter.setHighlightColor(ContextCompat.getColor(getContext(), android.R.color.transparent));

        mCheckedOption = "Romaji";
        mDictionarySearchLayout.setVisibility(View.VISIBLE);
        checkBoxDictionary.setChecked(true);
        checkBoxRomaji.setChecked(true);
        checkBoxDefinition.setChecked(false);
        checkBoxTwitter.setChecked(false);
        checkBoxDictionary.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mDictionarySearchLayout.setVisibility(View.VISIBLE);
                    checkBoxTwitter.setChecked(false);
                }
            }
        });
        checkBoxRomaji.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCheckedOption = "Romaji";
                    checkBoxDefinition.setChecked(false);
                }
            }
        });
        checkBoxDefinition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCheckedOption = "Definition";
                    checkBoxRomaji.setChecked(false);
                }

            }
        });

        checkBoxTwitter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCheckedOption = "Twitter";
                    checkBoxDictionary.setChecked(false);
                    mDictionarySearchLayout.setVisibility(View.GONE);
                }
            }
        });


        currentSearchText= null;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if(query.trim().length()>25) {
                    Toast.makeText(getContext(), getString(R.string.searchquerytoolong), Toast.LENGTH_LONG).show();
                }else if(query.trim().length()==0) {
                    Toast.makeText(getContext(), getString(R.string.searchinputtexttosearch), Toast.LENGTH_LONG).show();
                } else if (!checkBoxDefinition.isChecked() &&!checkBoxRomaji.isChecked()){
                    Toast.makeText(getContext(), getString(R.string.searchchoosecriteria), Toast.LENGTH_LONG).show();
                } else {

                    Pattern ps = Pattern.compile("^[a-zA-Z0-9 ]+$");
                    Matcher ms = ps.matcher(query.trim());
//                    boolean okquery = ms.matches();
//                    if (!okquery) {
//                        Toast.makeText(getContext(), getString(R.string.searchalphanumericsonly), Toast.LENGTH_LONG).show();
//                    } else {

                        /* Run the query */
                        showRecyclerView(true);
                        showProgressBar(true);
                        searchView.clearFocus();
                        if(checkBoxDictionary.isChecked() && checkBoxRomaji.isChecked()) {
                            mCallback.runDictionarySearch(query.trim(),true);
                        } else if(checkBoxDictionary.isChecked() && checkBoxDefinition.isChecked()) {
                            mCallback.runDictionarySearch(query.trim(),false);
                        } else if(checkBoxTwitter.isChecked()) {
                            //TODO
                            mCallback.runTwitterSearch(query.trim());
//                            mCallback.runTwitterSearch(query);
                        } else {
                            Toast.makeText(getContext(), "Select a search option", Toast.LENGTH_SHORT).show();
                        }
//                    }

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


        //If saved instance state is not null, run the adapter...
        if(savedInstanceState!=null) {
            try {
                mCheckedOption = savedInstanceState.getString("mCheckedOption");
                if(mCheckedOption.equals("Twitter")) {
                    mTwitterResults = savedInstanceState.getParcelableArrayList("mTwitterResults");
                    recieveTwitterSearchResults(mTwitterResults);
                } else {
                    mDictionaryResults = savedInstanceState.getParcelableArrayList("mDictionaryResults");
                    recieveDictionarySearchResults(mDictionaryResults);
                }
            } catch (NullPointerException e) {
                Log.e(TAG,"Nullpointer exception in loading saved state on search fragment");
            }

        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
//        updateAdapter();
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

    public void recieveDictionarySearchResults(ArrayList<WordEntry> results) {
        showProgressBar(false);
        if(results.size()>0) {
            mDictionaryResults = results;
            TweetBreakDownAdapter mAdapter = new TweetBreakDownAdapter(getContext()
                    ,mMetrics
                    ,mDictionaryResults
//                    ,mColorThresholds
                    ,mActiveFavoriteStars);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setVerticalScrollBarEnabled(true);
        } else {
            showRecyclerView(false);
        }
    }

    public void recieveTwitterSearchResults(ArrayList<Tweet> results) {
        showProgressBar(false);
        if(results.size()>0) {
            mTwitterResults = results;

            UserTimeLineAdapter mAdapter = new UserTimeLineAdapter(getContext()
                    ,_rxBus
                    ,mTwitterResults
                    ,mActiveTweetFavoriteStars);

            _rxBus.toLongClickObserverable()
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {

//                            if(isUniqueClick(1000) && event instanceof UserInfo) {
//                                Log.d(TAG,"LONG CLICK TO CALLBACK")
                                UserInfo userInfo = (UserInfo) event;
                                mCallback.showAddUserCheckDialog(userInfo);
//                            }

                        }

                    });

            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setVerticalScrollBarEnabled(true);
        } else {
            showRecyclerView(false);
        }
    }

    public void showProgressBar(boolean show){
        if(show) {
            progressBar.setVisibility(View.VISIBLE);
            mDividerView.setVisibility(View.GONE);

        } else {
            progressBar.setVisibility(View.GONE);
            mDividerView.setVisibility(View.VISIBLE);
        }
    }
//    public void recieveSearchResults(ArrayList<Tweet> results) {
//
//    }

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("mCheckedOption", mCheckedOption);
        if(mCheckedOption.equals("Twitter")) {
            outState.putParcelableArrayList("mTwitterResults", mTwitterResults);
        } else {
            outState.putParcelableArrayList("mDictionaryResults", mDictionaryResults);
        }


    }

}

