package com.jukuproject.jukutweet.TabContainers;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.Fragments.MyListFragment;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.Fragments.UserListFragment;


public class Tab2Container extends BaseContainerFragment {

    private boolean mIsViewInited;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("test", "tab 2 oncreateview");
        return inflater.inflate(R.layout.container_fragment, null);
    }

    public Tab2Container() {
    }

    public static Tab2Container newInstance() {
        Tab2Container fragment = new Tab2Container();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("test", "tab 1 container on activity created");
        if (!mIsViewInited) {
            mIsViewInited = true;
            initView();
        }
    }

    private void initView() {
        Log.e("test", "tab 2 init view");
        replaceFragment(new MyListFragment(), false,"mylistfragment");
    }

}