package com.jukuproject.jukutweet.TabContainers;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.Fragments.WordListFragment;
import com.jukuproject.jukutweet.R;


public class Tab3Container extends BaseContainerFragment {

    private boolean mIsViewInited;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.container_fragment, null);
    }

    public Tab3Container() {
    }

    public static Tab3Container newInstance() {
        return new Tab3Container();
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
        replaceFragment(new WordListFragment(), false,"mylistfragment");
    }

    public boolean updateMyListFragment() {
        try {
            ((WordListFragment) getChildFragmentManager().findFragmentByTag("mylistfragment")).updateMyListAdapter();
            return true;
        } catch (Exception e) {
            Log.e("Tab3Container","Could not find userListFragment");
            return false;
        }
    }




    public boolean isTopFragmentShowing() {
//        Log.d("TEST-Tab3Container","BACKSTACK COUNT : " + getChildFragmentManager().getBackStackEntryCount() );
        try {
            if(getChildFragmentManager().getBackStackEntryCount() == 0) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            Log.e("TEST-Tab3Container","Could not find userListFragment");
            return false;
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mIsViewInited", mIsViewInited);
    }
}