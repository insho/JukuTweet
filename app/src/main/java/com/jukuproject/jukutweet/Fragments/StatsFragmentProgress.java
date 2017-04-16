package com.jukuproject.jukutweet.Fragments;

import android.os.Bundle;
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

import com.jukuproject.jukutweet.Adapters.MyListExpandableAdapter;
import com.jukuproject.jukutweet.Adapters.StatsTop5Adapter;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Created by JClassic on 3/31/2017.
 */

public class StatsFragmentProgress extends Fragment {

    String TAG = "Test-stats2";

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

    public StatsFragmentProgress() {}

    public static StatsFragmentProgress newInstance(MyListEntry myListEntry
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



        if(savedInstanceState != null) {
            mMyListEntry = savedInstanceState.getParcelable("mMyListEntry");
            mTopCountLimit = savedInstanceState.getInt("mTopCountLimit");
            mColorBlockMeasurables = savedInstanceState.getParcelable("mColorBlockMeasurables");

        } else {
            mMyListEntry = getArguments().getParcelable("myListEntry");
            mTopCountLimit = getArguments().getInt("topCountLimit");
            mColorBlockMeasurables = getArguments().getParcelable("colorBlockMeasurables");
        }
        Log.d(TAG,"HEREEEEE 1");

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

        MyListExpandableAdapter.setColorBlocks(getContext()
                ,mColorBlockMeasurables
                ,metrics.widthPixels/2
                ,textViewColorBlock_grey
                ,textViewColorBlock_red
                ,textViewColorBlock_yellow
                ,textViewColorBlock_green);

        Log.d(TAG,"HEREEEEE 2");


        double topbottomThreshold = .5;
//        if(greenCount>0){
//            topbottomThreshold = .6;
//        } else {
//            topbottomThreshold = .5;
//        }


        ColorThresholds colorThresholds = SharedPrefManager.getInstance(getContext()).getColorThresholds();

        //THIS IS THE BOTTOM COUNT (ascending by percent)
        ArrayList<WordEntry> bottomFive = InternalDB.getWordInterfaceInstance(getContext()).getTopFiveWordEntries("Bottom",null,mMyListEntry,colorThresholds,mTopCountLimit,topbottomThreshold);
        Log.d(TAG,"HEREEEEE 3");

        //THIS ONE IS the BOTTOM 5 Adapter
        final StatsTop5Adapter adapter_bottom = new StatsTop5Adapter(getContext(),bottomFive,colorThresholds);
        Log.d(TAG,"HEREEEEE 4");

        bottomFiveList.setAdapter(adapter_bottom);
        Log.d(TAG,"bottomFiveList: " + adapter_bottom.getCount());

        ArrayList<Integer> idsToExclude = new ArrayList<>();
        for(WordEntry wordEntry : bottomFive) {
            if(wordEntry.getId() != null) {
                idsToExclude.add(wordEntry.getId());
            }
        }

        ArrayList<WordEntry> topFive = InternalDB.getWordInterfaceInstance(getContext()).getTopFiveWordEntries("Top",idsToExclude,mMyListEntry,colorThresholds,mTopCountLimit,topbottomThreshold);
        final StatsTop5Adapter adapter_top_desc = new StatsTop5Adapter(getContext(),topFive,colorThresholds);

        topFiveList.setAdapter(adapter_top_desc);

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("mMyListEntry", mMyListEntry);
        outState.putParcelable("mColorBlockMeasurables", mColorBlockMeasurables);
        outState.putInt("mTopCountLimit", mTopCountLimit);

    }


}
