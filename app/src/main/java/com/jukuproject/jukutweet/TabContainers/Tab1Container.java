package com.jukuproject.jukutweet.TabContainers;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.Fragments.UserListFragment;

//TODO -- possibly consolidate these into one?
public class Tab1Container extends BaseContainerFragment {

    private boolean mIsViewInited;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.container_fragment, null);
    }

    public Tab1Container() {}

    public static Tab1Container newInstance() {
        return new Tab1Container();
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
        replaceFragment(new UserListFragment(), false,"userlistfragment");

    }

    public boolean updateUserListFragment() {
        try {
            ((UserListFragment) getChildFragmentManager().findFragmentByTag("userlistfragment")).updateAdapter();
            return true;
        } catch (Exception e) {
            Log.e("Tab1Container","Could not find userListFragment");
            return false;
        }
    }

    public boolean isTopFragmentShowing() {
        try {
            (getChildFragmentManager().findFragmentByTag("userlistfragment")).isVisible();
            return true;
        } catch (Exception e) {
            Log.e("Tab1Container","Could not find userListFragment");
            return false;
        }
    }

}