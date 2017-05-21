package com.jukuproject.jukutweet.TabContainers;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.Fragments.TweetListFragment;
import com.jukuproject.jukutweet.R;


public class Tab2Container extends BaseContainerFragment {

    private boolean mIsViewInited;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.container_fragment, null);
    }

    public Tab2Container() {
    }

    public static Tab2Container newInstance() {
        return new Tab2Container();
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
        replaceFragment(new TweetListFragment(), false,"savedtweetsallfragment");
    }

    public void popAllFragments() {
        try {
            int count = getChildFragmentManager().getBackStackEntryCount();
            if(count>1) {
                for(int i = 1; i < count; ++i) {
                    getChildFragmentManager().popBackStack();
                }
            }

        } catch (Exception e) {
            Log.e("TEST-Tab2Container","popAllFragments failed");
        }
    }

    public void updateTweetListFragment() {
        try {
            ((TweetListFragment) getChildFragmentManager().findFragmentByTag("savedtweetsallfragment")).updateMyListAdapter();
        } catch (Exception e) {
            Log.e("Tab2Container","Could not find userListFragment");
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mIsViewInited", mIsViewInited);
    }


}