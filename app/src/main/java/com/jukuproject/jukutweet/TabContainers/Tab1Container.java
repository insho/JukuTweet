package com.jukuproject.jukutweet.TabContainers;


import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.Fragments.UserListFragment;
import com.jukuproject.jukutweet.R;

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
        if(savedInstanceState != null) {
            mIsViewInited = savedInstanceState.getBoolean("mIsViewInited");
        }
        if (!mIsViewInited) {
            mIsViewInited = true;
            initView();
        }
    }

    private void initView() {
//        Log.e("test", "tab 1 init view");
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

    public void popAllFragments() {
        try {
            getChildFragmentManager().popBackStack("userlistfragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);


        } catch (Exception e) {
            Log.e("TEST-Tab1Container","popAllFragments failed");
        }
    }

    public boolean isTopFragmentShowing() {
        try {
            if(getChildFragmentManager().getBackStackEntryCount() == 0) {
                return true;
            } else {
                return false;
            }
//            (getChildFragmentManager().findFragmentByTag("userlistfragment")).isVisible();
//            return true;
        } catch (Exception e) {
            Log.e("TEST-Tab1Container","isTopFragmentShowing failed");
            return false;
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("mIsViewInited", mIsViewInited);


    }
}