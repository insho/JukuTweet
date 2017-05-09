package com.jukuproject.jukutweet.TabContainers;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.Fragments.SearchFragment;
import com.jukuproject.jukutweet.R;


public class Tab4Container extends BaseContainerFragment {

    private boolean mIsViewInited;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        Log.e("test", "tab 2 oncreateview");
        return inflater.inflate(R.layout.container_fragment, null);
    }

    public Tab4Container() {
    }

    public static Tab4Container newInstance() {
//        Tab2Container fragment = new Tab2Container();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
        return new Tab4Container();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            mIsViewInited = savedInstanceState.getBoolean("mIsViewInited",false);
        }
//        Log.e("test", "tab 1 container on activity created");
        if (!mIsViewInited) {
            mIsViewInited = true;
            initView();
        }
    }

    private void initView() {
        replaceFragment(new SearchFragment(), false,"searchFragment");
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("mIsViewInited", mIsViewInited);


    }
}