package com.jukuproject.jukutweet.Fragments;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.jukuproject.jukutweet.Adapters.WordListExpandableAdapter;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Interfaces.WordEntryFavoritesChangedListener;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;

import java.util.ArrayList;

import rx.functions.Action1;

/**
 * Created by JClassic on 3/31/2017.
 */

public class StatsFragmentProgress extends Fragment implements WordEntryFavoritesChangedListener {

    String TAG = "Test-stats2";
    private long mLastClickTime = 0;
//    private String mQuizType;
    private MyListEntry mMyListEntry;
    private ColorBlockMeasurables mColorBlockMeasurables;
    int mTopCountLimit;

    ListView bottomFiveList;
    TextView txtTopFive;

    TextView textViewColorBlock_grey ;
    TextView textViewColorBlock_red;
    TextView textViewColorBlock_yellow;
    TextView textViewColorBlock_green;
    TextView txtTitle;
    TextView txtCompletePercent;
    ImageButton imageButton;
    TextView txtBottomFive;
    ListView topFiveList;
    StatsTopAndBottomAdapter adapter_bottom;
    StatsTopAndBottomAdapter adapter_top;
    ArrayList<WordEntry> mTopFiveDataSet;
    ArrayList<WordEntry> mBottomFiveDataSet;

    private boolean mIsTweetList;
    public StatsFragmentProgress() {}

    public static StatsFragmentProgress newInstance(MyListEntry myListEntry
            , int topCountLimit
            , ColorBlockMeasurables colorBlockMeasurables
            , boolean isTweetList) {
        StatsFragmentProgress fragment = new StatsFragmentProgress();
        Bundle args = new Bundle();
        args.putBoolean("isTweetList",isTweetList);
        args.putParcelable("myListEntry",myListEntry);
        args.putInt("topCountLimit",topCountLimit);
        args.putParcelable("colorBlockMeasurables",colorBlockMeasurables);
        fragment.setArguments(args);
        return  fragment;
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
            mMyListEntry = savedInstanceState.getParcelable("mMyListEntry");
            mTopCountLimit = savedInstanceState.getInt("mTopCountLimit");
            mColorBlockMeasurables = savedInstanceState.getParcelable("mColorBlockMeasurables");
            mTopFiveDataSet = savedInstanceState.getParcelableArrayList("mTopFiveDataSet");
            mBottomFiveDataSet = savedInstanceState.getParcelableArrayList("mBottomFiveDataSet");
            mIsTweetList = savedInstanceState.getBoolean("mIsTweetList");
        } else {
            mMyListEntry = getArguments().getParcelable("myListEntry");
            mTopCountLimit = getArguments().getInt("topCountLimit");
            mColorBlockMeasurables = getArguments().getParcelable("colorBlockMeasurables");
            mIsTweetList = getArguments().getBoolean("isTweetList");

            if(mIsTweetList) {
                mBottomFiveDataSet = InternalDB.getTweetInterfaceInstance(getContext()).getTopFiveTweetWordEntries("Bottom",null,mMyListEntry,colorThresholds,mTopCountLimit,topbottomThreshold);

            } else {
                mBottomFiveDataSet = InternalDB.getWordInterfaceInstance(getContext()).getTopFiveWordEntries("Bottom",null,mMyListEntry,colorThresholds,mTopCountLimit,topbottomThreshold);
            }
            ArrayList<Integer> idsToExclude = new ArrayList<>();
            for(WordEntry wordEntry : mBottomFiveDataSet) {
                if(wordEntry.getId() != null) {
                    idsToExclude.add(wordEntry.getId());
                }
            }
            if(mIsTweetList) {
                mTopFiveDataSet = InternalDB.getTweetInterfaceInstance(getContext()).getTopFiveTweetWordEntries("Top",idsToExclude,mMyListEntry,colorThresholds,mTopCountLimit,topbottomThreshold);

            } else {
                mTopFiveDataSet = InternalDB.getWordInterfaceInstance(getContext()).getTopFiveWordEntries("Top",idsToExclude,mMyListEntry,colorThresholds,mTopCountLimit,topbottomThreshold);
            }
        }

        int greenPercent = Math.round(100*((float)mColorBlockMeasurables.getGreenCount()/(float)mColorBlockMeasurables.getTotalCount()));
        txtCompletePercent.setText(greenPercent + "% Complete ");

        String titleString;
        try {
            //if the list is a system "star" list, show the star next to the title
            if(mMyListEntry.getListsSys()==1) {
                titleString = mMyListEntry.getListName();

                imageButton.setFocusable(false);
                imageButton.setClickable(false);
                imageButton.setImageResource(R.drawable.ic_star_black);
                imageButton.setVisibility(ImageButton.VISIBLE);

                switch (mMyListEntry.getListName()) {
                    case "Blue":
                        imageButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuBlue));
                        break;
                    case "Yellow":
                        imageButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuYellow));
                        break;
                    case "Red":
                        imageButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuRed));
                        break;
                    case "Green":
                        imageButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuGreen));
                        break;
                    case "Purple":
                        imageButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuPurple));
                        break;
                    case "Orange":
                        imageButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuOrange));
                        break;
                    default:
                        break;
                }

            } else {
                titleString = mMyListEntry.getListName();

            }

        } catch (NullPointerException e) {
            titleString = "";
            Log.e(TAG,"setting title nullpointer exception: " + e);
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

        WordListExpandableAdapter.setColorBlocks(getContext()
                ,mColorBlockMeasurables
                ,metrics.widthPixels/2
                ,textViewColorBlock_grey
                ,textViewColorBlock_red
                ,textViewColorBlock_yellow
                ,textViewColorBlock_green);

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

//                            WordDetailPopupDialog.newInstance(wordEntry).show(getFragmentManager(),"wordDetailPopup");
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

    public void updateWordEntryItemFavorites(WordEntry wordEntry) {
        if(mTopFiveDataSet.contains(wordEntry)) {
            mTopFiveDataSet.get(mTopFiveDataSet.indexOf(wordEntry)).setItemFavorites(wordEntry.getItemFavorites());
        }
        if(mBottomFiveDataSet.contains(wordEntry)) {
            mBottomFiveDataSet.get(mBottomFiveDataSet.indexOf(wordEntry)).setItemFavorites(wordEntry.getItemFavorites());
        }

        adapter_bottom.notifyDataSetChanged();
        adapter_top.notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("mMyListEntry", mMyListEntry);
        outState.putParcelable("mColorBlockMeasurables", mColorBlockMeasurables);
        outState.putInt("mTopCountLimit", mTopCountLimit);
        outState.putParcelableArrayList("mTopFiveDataSet",mTopFiveDataSet);
        outState.putParcelableArrayList("mBottomFiveDataSet",mBottomFiveDataSet);
        outState.putBoolean("mIsTweetList",mIsTweetList);
    }


}
