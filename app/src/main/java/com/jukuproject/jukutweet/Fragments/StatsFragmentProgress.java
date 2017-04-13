package com.jukuproject.jukutweet.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.jukuproject.jukutweet.Adapters.StatsTop5Adapter;
import com.jukuproject.jukutweet.Database.InternalDB;
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

    String mQuizType;
    MyListEntry mMyListEntry;
    int mTopCountLimit;
    public StatsFragmentProgress() {}

    public static StatsFragmentProgress newInstance(String quizType
            , MyListEntry myListEntry
            , int topCountLimit) {
        StatsFragmentProgress fragment = new StatsFragmentProgress();
        Bundle args = new Bundle();
        args.putString("quizType",quizType);
        args.putParcelable("myListEntry",myListEntry);
        args.putInt("topCountLimit",topCountLimit);
        fragment.setArguments(args);
        return  fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG,"Creating stats view");
        //Set input global data
        mQuizType = getArguments().getString("quizType");
        mMyListEntry = getArguments().getParcelable("myListEntry");
        mTopCountLimit = getArguments().getInt("topCountLimit");

        View v = inflater.inflate(R.layout.fragment_stats_progress, container, false);



        double topbottomThreshold = .5;
//        if(greenCount>0){
//            topbottomThreshold = .6;
//        } else {
//            topbottomThreshold = .5;
//        }


        ColorThresholds colorThresholds = SharedPrefManager.getInstance(getContext()).getColorThresholds();

        //THIS IS THE BOTTOM COUNT (ascending by percent)
        ArrayList<WordEntry> bottomFive = InternalDB.getWordInterfaceInstance(getContext()).getTopFiveWordEntries("Bottom",null,mMyListEntry,colorThresholds,mTopCountLimit,topbottomThreshold);

        //THIS ONE IS the BOTTOM 5 Adapter
        final StatsTop5Adapter adapter_bottom = new StatsTop5Adapter(getContext(),bottomFive,colorThresholds);
        final ListView  listView_bottom = (ListView) v.findViewById(R.id.ascending_listresults);
        listView_bottom.setAdapter(adapter_bottom);

        ArrayList<Integer> idsToExclude = new ArrayList<>();
        for(WordEntry wordEntry : bottomFive) {
            if(wordEntry.getId() != null) {
                idsToExclude.add(wordEntry.getId());
            }
        }

        ArrayList<WordEntry> topFive = InternalDB.getWordInterfaceInstance(getContext()).getTopFiveWordEntries("Top",idsToExclude,mMyListEntry,colorThresholds,mTopCountLimit,topbottomThreshold);
        final StatsTop5Adapter adapter_top_desc = new StatsTop5Adapter(getContext(),topFive,colorThresholds);
        final ListView  listView_top_desc = (ListView) v.findViewById(R.id.descending_listresults);
        listView_top_desc.setAdapter(adapter_top_desc);

        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }



}
