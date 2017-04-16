package com.jukuproject.jukutweet.TabContainers;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.Fragments.MyListFragment;
import com.jukuproject.jukutweet.R;


public class Tab3Container extends BaseContainerFragment {

    private boolean mIsViewInited;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        Log.e("test", "tab 2 oncreateview");
        return inflater.inflate(R.layout.container_fragment, null);
    }

    public Tab3Container() {
    }

    public static Tab3Container newInstance() {
//        Tab2Container fragment = new Tab2Container();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
        return new Tab3Container();
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
//        Log.e("test", "tab 2 init view");
        replaceFragment(new MyListFragment(), false,"mylistfragment");
    }

    public boolean updateMyListFragment() {
        try {
            ((MyListFragment) getChildFragmentManager().findFragmentByTag("mylistfragment")).updateMyListAdapter();
            return true;
        } catch (Exception e) {
            Log.e("Tab1Container","Could not find userListFragment");
            return false;
        }
    }




    public boolean isTopFragmentShowing() {
        Log.d("TEST-Tab3Container","BACKSTACK COUNT : " + getChildFragmentManager().getBackStackEntryCount() );
        try {
            if(getChildFragmentManager().getBackStackEntryCount() == 0) {
                return true;
            } else {
                return false;
            }
//            (getChildFragmentManager().findFragmentByTag("mylistfragment")).isVisible();
//            Log.d("TEST","BACKSTACK ENTRY COUNT: " + getChildFragmentManager().getBackStackEntryCount());
//            Log.d("TEST","Apparently mylistfragment is on top...");
//            return true;
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