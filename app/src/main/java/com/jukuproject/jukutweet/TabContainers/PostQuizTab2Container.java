package com.jukuproject.jukutweet.TabContainers;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.Fragments.StatsFragmentProgress;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.R;

//TODO -- possibly consolidate these into one?
public class PostQuizTab2Container extends BaseContainerFragment {

    private boolean mIsViewInited;
//    private ColorBlockMeasurables mColorBlockMeasurables;
//    private MyListEntry mMyListEntry;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.container_fragment, null);
    }

    public PostQuizTab2Container() {}

    public static PostQuizTab2Container newInstance(ColorBlockMeasurables colorBlockMeasurables, MyListEntry myListEntry) {
        PostQuizTab2Container fragment =  new PostQuizTab2Container();
        Bundle args = new Bundle();
        args.putParcelable("mColorBlockMeasurables",colorBlockMeasurables);
        args.putParcelable("myListEntry", myListEntry);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            mIsViewInited = savedInstanceState.getBoolean("mIsViewInited");
        }
        if (!mIsViewInited) {
            mIsViewInited = true;
            initView();
        }
    }

    private void initView() {

        try {
            StatsFragmentProgress statsFragmentProgress = StatsFragmentProgress.newInstance((MyListEntry)getArguments().getParcelable("myListEntry")
                    , 10
                    ,(ColorBlockMeasurables)getArguments().getParcelable("mColorBlockMeasurables"));

            replaceFragment(statsFragmentProgress, false,"statsFragmentProgress");

        } catch (NullPointerException e) {
            Log.e("Test-posttab2container","Nullpointer initiating postquiztab2container ");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("mIsViewInited", mIsViewInited);


    }
}