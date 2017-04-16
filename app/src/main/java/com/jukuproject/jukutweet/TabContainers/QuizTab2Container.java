package com.jukuproject.jukutweet.TabContainers;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.Fragments.StatsFragmentProgress;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.R;

//TODO -- possibly consolidate these into one?
public class QuizTab2Container extends BaseContainerFragment {

    private boolean mIsViewInited;
    private String mQuiz;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.container_fragment, null);
    }

    public QuizTab2Container() {}

    public static QuizTab2Container newInstance() {
        return new QuizTab2Container();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            mIsViewInited = savedInstanceState.getBoolean("mIsViewInited");
        }
//        Log.e("test", "tab 1 container on activity created");
        if (!mIsViewInited) {
            mIsViewInited = true;
            initView();
        }
    }

    private void initView() {
//        Log.e("test", "tab 1 init view")

        StatsFragmentProgress statsFragmentProgress = StatsFragmentProgress.newInstance(new MyListEntry("dummy",0)
                , 10
                ,new ColorBlockMeasurables());
        replaceFragment(statsFragmentProgress, true,"statsFragmentProgress");
    }

//    public boolean updateUserListFragment() {
//        try {
//            ((UserListFragment) getChildFragmentManager().findFragmentByTag("userlistfragment")).updateAdapter();
//            return true;
//        } catch (Exception e) {
//            Log.e("Tab1Container","Could not find userListFragment");
//            return false;
//        }
//    }
//
//    public boolean isTopFragmentShowing() {
//        try {
//            (getChildFragmentManager().findFragmentByTag("userlistfragment")).isVisible();
//            return true;
//        } catch (Exception e) {
//            Log.e("Tab1Container","Could not find userListFragment");
//            return false;
//        }
//    }
@Override
public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putBoolean("mIsViewInited", mIsViewInited);


}
}