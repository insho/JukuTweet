package com.jukuproject.jukutweet.TabContainers;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.Fragments.UserListFragment;


public class Tab1Container extends BaseContainerFragment {

    private boolean mIsViewInited;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("test", "tab 1 oncreateview");
        return inflater.inflate(R.layout.container_fragment, null);
    }

    public Tab1Container() {
    }

    public static Tab1Container newInstance() {
        Tab1Container fragment = new Tab1Container();
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
        Log.e("test", "tab 1 init view");
        replaceFragment(new UserListFragment(), false,"userListFragment");

    }

    public boolean updateUserListFragment() {
        try {
            ((UserListFragment) getChildFragmentManager().findFragmentByTag("userListFragment")).updateAdapter();
            return true;
        } catch (Exception e) {
            Log.e("Tab1Container","Could not find userListFragment");
            return false;
        }
    }

}