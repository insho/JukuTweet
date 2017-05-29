package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.StatsTopAndBottomAdapter;
import com.jukuproject.jukutweet.Adapters.TweetListExpandableAdapter;
import com.jukuproject.jukutweet.Adapters.WordListExpandableAdapter;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog;
import com.jukuproject.jukutweet.FavoritesColors;
import com.jukuproject.jukutweet.Interfaces.PostQuizFragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Interfaces.WordEntryFavoritesChangedListener;
import com.jukuproject.jukutweet.MainActivity;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;

import java.util.ArrayList;

import rx.functions.Action1;

/**
 * Overall stats for a WordList, TweetList, or Single User's Saved Tweets collection. Has the colorblock
 * breakdown for the list, as well as a list of Top 10 and Bottom 10 words for the list.
 */
public class StatsFragmentProgress extends Fragment implements WordEntryFavoritesChangedListener {

    String TAG = "Test-stats2";
    private PostQuizFragmentInteractionListener mCallback;
    private long mLastClickTime = 0;
    private MyListEntry mMyListEntry;
    private ColorBlockMeasurables mColorBlockMeasurables;
    int mTopCountLimit;
    private boolean mIsTweetList;
    private boolean mIsSingleUserTweetList;
    private UserInfo mUserInfo;

    ListView bottomFiveList;
    TextView txtTopFive;

    TextView textViewColorBlock_grey ;
    TextView textViewColorBlock_red;
    TextView textViewColorBlock_yellow;
    TextView textViewColorBlock_green;
    TextView textViewColorBlock_empty;
    TextView txtTitle;
    TextView txtCompletePercent;
    ImageButton imageButton;
    TextView txtBottomFive;
    ListView topFiveList;
    StatsTopAndBottomAdapter adapter_bottom;
    StatsTopAndBottomAdapter adapter_top;
    ArrayList<WordEntry> mTopFiveDataSet;
    ArrayList<WordEntry> mBottomFiveDataSet;


    public StatsFragmentProgress() {}

    public static StatsFragmentProgress newWordListInstance(MyListEntry myListEntry
            , int topCountLimit
            , ColorBlockMeasurables colorBlockMeasurables) {
        StatsFragmentProgress fragment = new StatsFragmentProgress();
        Bundle args = new Bundle();
        args.putParcelable("myListEntry",myListEntry);
        args.putInt("topCountLimit",topCountLimit);
        args.putParcelable("colorBlockMeasurables",colorBlockMeasurables);
        fragment.setArguments(args);
        return  fragment;
    }

    public static StatsFragmentProgress newSingleUserTweetsInstance(UserInfo userInfo
            , int topCountLimit
            , ColorBlockMeasurables colorBlockMeasurables) {
        StatsFragmentProgress fragment = new StatsFragmentProgress();
        Bundle args = new Bundle();
        args.putBoolean("isTweetList",true);
        args.putBoolean("isSingleUserTweetList",true);
        args.putParcelable("userInfo",userInfo);
        args.putInt("topCountLimit",topCountLimit);
        args.putParcelable("colorBlockMeasurables",colorBlockMeasurables);
        fragment.setArguments(args);
        return  fragment;
    }

    public static StatsFragmentProgress newTweetsInstance(MyListEntry myListEntry
            , int topCountLimit
            , ColorBlockMeasurables colorBlockMeasurables) {
        StatsFragmentProgress fragment = new StatsFragmentProgress();
        Bundle args = new Bundle();
        args.putBoolean("isTweetList",true);
        args.putBoolean("isSingleUserTweetList",false);
        args.putParcelable("myListEntry",myListEntry);
        args.putInt("topCountLimit",topCountLimit);
        args.putParcelable("colorBlockMeasurables",colorBlockMeasurables);
        fragment.setArguments(args);
        return  fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (PostQuizFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stats_progress, container, false);
        topFiveList = (ListView) v.findViewById(R.id.descending_listresults);
        bottomFiveList = (ListView) v.findViewById(R.id.ascending_listresults);
        txtTopFive = (TextView) v.findViewById(R.id.textViewTopFive);

        textViewColorBlock_grey = (TextView) v.findViewById(R.id.listitem_colors_1);
        textViewColorBlock_red = (TextView) v.findViewById(R.id.listitem_colors_2);
        textViewColorBlock_yellow = (TextView) v.findViewById(R.id.listitem_colors_3);
        textViewColorBlock_green = (TextView) v.findViewById(R.id.listitem_colors_4);
        textViewColorBlock_empty = (TextView) v.findViewById(R.id.listitem_colors_5);

        txtTitle = (TextView) v.findViewById(R.id.textViewTitle);
        txtCompletePercent = (TextView) v.findViewById(R.id.textViewBlockProgress);
        imageButton = (ImageButton) v.findViewById(R.id.favorite_icon);
        txtBottomFive = (TextView) v.findViewById(R.id.textViewBottomFive);
        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        textViewColorBlock_grey.setGravity(Gravity.CENTER);
        textViewColorBlock_red.setGravity(Gravity.CENTER);
        textViewColorBlock_yellow.setGravity(Gravity.CENTER);
        textViewColorBlock_green.setGravity(Gravity.CENTER);

        ColorThresholds colorThresholds = SharedPrefManager.getInstance(getContext()).getColorThresholds();
        double topbottomThreshold = .5;

        if(savedInstanceState != null) {
            mIsTweetList = savedInstanceState.getBoolean("mIsTweetList",false);
            mIsSingleUserTweetList =   savedInstanceState.getBoolean("mIsSingleUserTweetList",false);
            if(mIsSingleUserTweetList) {
                mUserInfo = savedInstanceState.getParcelable("mUserInfo");
            } else {
                mMyListEntry = savedInstanceState.getParcelable("mMyListEntry");
            }
            mTopCountLimit = savedInstanceState.getInt("mTopCountLimit");
            mColorBlockMeasurables = savedInstanceState.getParcelable("mColorBlockMeasurables");
            mTopFiveDataSet = savedInstanceState.getParcelableArrayList("mTopFiveDataSet");
            mBottomFiveDataSet = savedInstanceState.getParcelableArrayList("mBottomFiveDataSet");
        } else {
            mIsTweetList = getArguments().getBoolean("isTweetList",false);
            mIsSingleUserTweetList = getArguments().getBoolean("isSingleUserTweetList",false);
            if(mIsSingleUserTweetList) {
                mUserInfo = getArguments().getParcelable("userInfo");
            } else {
                mMyListEntry = getArguments().getParcelable("myListEntry");
            }
            mTopCountLimit = getArguments().getInt("topCountLimit");
            mColorBlockMeasurables = getArguments().getParcelable("colorBlockMeasurables");


            if(mIsSingleUserTweetList) {
                mBottomFiveDataSet = InternalDB.getTweetInterfaceInstance(getContext()).getTopFiveTweetSingleUserEntries("Bottom",null,mUserInfo,colorThresholds,mTopCountLimit,topbottomThreshold);
                mTopFiveDataSet = InternalDB.getTweetInterfaceInstance(getContext()).getTopFiveTweetSingleUserEntries("Top",getIdsToExclude(mBottomFiveDataSet),mUserInfo,colorThresholds,mTopCountLimit,topbottomThreshold);
            } else if(mIsTweetList) {
                mBottomFiveDataSet = InternalDB.getTweetInterfaceInstance(getContext()).getTopFiveTweetWordEntries("Bottom",null,mMyListEntry,colorThresholds,mTopCountLimit,topbottomThreshold);
                mTopFiveDataSet = InternalDB.getTweetInterfaceInstance(getContext()).getTopFiveTweetWordEntries("Top",getIdsToExclude(mBottomFiveDataSet),mMyListEntry,colorThresholds,mTopCountLimit,topbottomThreshold);
            } else {
                mBottomFiveDataSet = InternalDB.getWordInterfaceInstance(getContext()).getTopFiveWordEntries("Bottom",null,mMyListEntry,colorThresholds,mTopCountLimit,topbottomThreshold);
                mTopFiveDataSet = InternalDB.getWordInterfaceInstance(getContext()).getTopFiveWordEntries("Top",getIdsToExclude(mBottomFiveDataSet),mMyListEntry,colorThresholds,mTopCountLimit,topbottomThreshold);
            }
        }

        int greenPercent = Math.round(100*((float)mColorBlockMeasurables.getGreenCount()/(float)mColorBlockMeasurables.getTotalCount()));
        txtCompletePercent.setText(getString(R.string.statsComplete,greenPercent));

        String titleString;
        try {
            //if the list is a system "star" list, show the star next to the title
            if(mIsSingleUserTweetList) {
                titleString = mUserInfo.getDisplayScreenName();
            } else if(mMyListEntry.getListsSys()==1) {
                titleString = mMyListEntry.getListName();

                imageButton.setFocusable(false);
                imageButton.setClickable(false);
                imageButton.setImageResource(R.drawable.ic_star_black);
                imageButton.setVisibility(ImageButton.VISIBLE);
                FavoritesColors.setFavoritesButtonColorFilter(getContext(), imageButton, mMyListEntry.getListName());
            } else {
                titleString = mMyListEntry.getListName();

            }

        } catch (NullPointerException e) {
            titleString = "";
            Log.e(TAG,"setting title nullpointer exception: " + e.getCause());
        }


        //Set the title underlined
        SpannableString content = new SpannableString(titleString);
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        txtTitle.setText(content);

        //Set top and bottom 5 titles
        SpannableString content_TopFive = new SpannableString("Top " + mTopCountLimit);
        content_TopFive.setSpan(new UnderlineSpan(), 0, content_TopFive.length(), 0);
        txtTopFive.setText(content_TopFive);

        SpannableString content_BottomFive = new SpannableString("Bottom " + mTopCountLimit);
        content_BottomFive.setSpan(new UnderlineSpan(), 0, content_BottomFive.length(), 0);
        txtBottomFive.setText(content_BottomFive);

        /* Get width of screen */
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Assign color blocks below the list title
        if(mIsTweetList) {
            textViewColorBlock_empty.setVisibility(View.VISIBLE);
            TweetListExpandableAdapter.setTweetColorBlocks(getContext()
                    ,mColorBlockMeasurables
                    ,metrics.widthPixels/2
                    ,textViewColorBlock_grey
                    ,textViewColorBlock_red
                    ,textViewColorBlock_yellow
                    ,textViewColorBlock_green
                    ,textViewColorBlock_empty);


        } else {
            textViewColorBlock_empty.setVisibility(View.GONE);
            WordListExpandableAdapter.setColorBlocks(getContext()
                    ,mColorBlockMeasurables
                    ,metrics.widthPixels/2
                    ,textViewColorBlock_grey
                    ,textViewColorBlock_red
                    ,textViewColorBlock_yellow
                    ,textViewColorBlock_green);


        }

        RxBus rxBus = new RxBus();
        adapter_bottom = new StatsTopAndBottomAdapter(getContext(),mBottomFiveDataSet,colorThresholds,rxBus);
        bottomFiveList.setAdapter(adapter_bottom);

        adapter_top = new StatsTopAndBottomAdapter(getContext(),mTopFiveDataSet,colorThresholds,rxBus);
        topFiveList.setAdapter(adapter_top);

        rxBus.toClickObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {

                        if(isUniqueClick(100) && event instanceof WordEntry) {
                            WordEntry wordEntry = (WordEntry) event;

                            WordDetailPopupDialog wordDetailPopupDialog = WordDetailPopupDialog.newInstance(wordEntry);
                            wordDetailPopupDialog.setTargetFragment(StatsFragmentProgress.this, 0);
                            wordDetailPopupDialog.show(getFragmentManager(),"wordDetailPopup");
                        }

                    }

                });

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
     * If a word entry has been saved to a new word list in the {@link WordDetailPopupDialog}, the message is relayed back to
     * this method, which updates the  {@link com.jukuproject.jukutweet.Models.ItemFavorites} in the dataset to reflect the change
     * @param wordEntry WordEntry that was added to/removed from a new list
     */
    public void updateWordEntryItemFavorites(WordEntry wordEntry) {
        for(WordEntry topWordEntry : mTopFiveDataSet) {
            if(topWordEntry.getId().equals(wordEntry.getId())) {
                topWordEntry.setItemFavorites(wordEntry.getItemFavorites());
            }
        }

        for(WordEntry bottomWordEntry : mBottomFiveDataSet) {
            if(bottomWordEntry.getId().equals(wordEntry.getId())) {
                bottomWordEntry.setItemFavorites(wordEntry.getItemFavorites());
            }
        }

        adapter_bottom.notifyDataSetChanged();
        adapter_top.notifyDataSetChanged();
    }

    /**
     * If a group of word entries has been saved in another fragment, the message relayed to {@link MainActivity#notifySavedWordFragmentsChanged(String)}
     * , which then notifies any open tabs in the other fragments which might be affected by the new word that the change
     * has been made. This method recieves the udpdated list of words and cycles through them, looking for matches. If a match
     * is found, the {@link com.jukuproject.jukutweet.Models.ItemFavorites} object for the word is updated to reflect the new favorite list/s
     * @param updatedWordEntries ArrayList of WordEntries that were saved to/removed from a new list
     */
    public void updateWordEntryItemFavorites(ArrayList<WordEntry> updatedWordEntries) {

        if(mTopFiveDataSet!=null && mBottomFiveDataSet !=null ) {
            for(WordEntry dataSetWordEntry : mTopFiveDataSet) {
                for(WordEntry updatedWordEntry : updatedWordEntries ) {
                    if(dataSetWordEntry.getId().equals(updatedWordEntry.getId())) {
                        dataSetWordEntry.setItemFavorites(updatedWordEntry.getItemFavorites());
                    }
                }
            }

            for(WordEntry dataSetWordEntry : mBottomFiveDataSet) {
                for(WordEntry updatedWordEntry : updatedWordEntries ) {
                    if(dataSetWordEntry.getId().equals(updatedWordEntry.getId())) {
                        dataSetWordEntry.setItemFavorites(updatedWordEntry.getItemFavorites());
                    }
                }
            }

            adapter_bottom.notifyDataSetChanged();
            adapter_top.notifyDataSetChanged();

        }
    }

    public void updateWordEntryFavoritesForOtherTabs(WordEntry wordEntry) {}
    public void notifySavedTweetFragmentsChanged(){}

    /**
     * If a tweet has been saved in {@link WordDetailPopupDialog}, and the user for that tweet
     * is not saved in the db (which therefore means the user's icon is not saved in the db), this passes
     * on the message to save the icon from the {@link com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter} to the Activity,
     * which uses {@link com.jukuproject.jukutweet.Database.UserOpsHelper#downloadTweetUserIcon(Context, String, String)} in a
     * subscription to download the icon
     * @param userInfo UserInfo of user whose icon will be downloaded
     */
    public void downloadTweetUserIcons(UserInfo userInfo) {
        if(!InternalDB.getUserInterfaceInstance(getContext()).duplicateUser(userInfo.getUserId())) {
            mCallback.downloadTweetUserIcons(userInfo);
        }
    }

    /**
     * Compiles an array of edict kanji Ids from the bottomDataSet, so that they can be passed on to the
     * {@link com.jukuproject.jukutweet.Interfaces.TweetListOperationsInterface#getTopFiveTweetWordEntries(String, ArrayList, MyListEntry, ColorThresholds, int, double)}
     * when pulling hte Top 5 lists. This is because the "Bottom 5" and "Top 5" stats lists should not share any entries. So if an entry already
     * exists in the bottom set, don't include it in the top set.
     * @param bottomDataSet Bottom Five (or more) word entries set
     * @return array of edict kanji Ids from the bottomDataSet
     */
    public ArrayList<Integer> getIdsToExclude(ArrayList<WordEntry> bottomDataSet) {
        ArrayList<Integer> idsToExclude = new ArrayList<>();
        for(WordEntry wordEntry : bottomDataSet) {
            if(wordEntry.getId() != null) {
                idsToExclude.add(wordEntry.getId());
            }
        }
        return idsToExclude;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("mIsTweetList",mIsTweetList);
        outState.putBoolean("mIsSingleUserTweetList",mIsSingleUserTweetList);
        if(mIsSingleUserTweetList) {
            outState.putParcelable("mUserInfo",mUserInfo);
        } else {
            outState.putParcelable("mMyListEntry", mMyListEntry);
        }
        outState.putParcelable("mColorBlockMeasurables", mColorBlockMeasurables);
        outState.putInt("mTopCountLimit", mTopCountLimit);
        outState.putParcelableArrayList("mTopFiveDataSet",mTopFiveDataSet);
        outState.putParcelableArrayList("mBottomFiveDataSet",mBottomFiveDataSet);

    }


}
