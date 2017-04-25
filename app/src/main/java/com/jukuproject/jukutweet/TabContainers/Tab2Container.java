package com.jukuproject.jukutweet.TabContainers;


import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.Fragments.SavedTweetsListFragment;
import com.jukuproject.jukutweet.R;


public class Tab2Container extends BaseContainerFragment {

    private boolean mIsViewInited;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        Log.e("test", "tab 3 oncreateview");
        return inflater.inflate(R.layout.container_fragment, null);
    }

    public Tab2Container() {
    }

    public static Tab2Container newInstance() {
//        Tab2Container fragment = new Tab2Container();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
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
//        Log.e("test", "tab 3 init view");
        replaceFragment(new SavedTweetsListFragment(), false,"savedtweetsallfragment");
    }
//    public boolean isTopFragmentShowing() {
//        try {
//            (getChildFragmentManager().findFragmentByTag("savedtweetsallfragment")).isVisible();
//            return true;
//        } catch (Exception e) {
//            Log.e("Tab2Container","Could not find userListFragment");
//            return false;
//        }
//    }

    public void popAllFragments() {
        try {
            getChildFragmentManager().popBackStack("savedtweetsallfragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } catch (Exception e) {
            Log.e("TEST-Tab1Container","popAllFragments failed");
        }
    }
//
//    public String getTopFragmentTag() {
//        try {
//            return getChildFragmentManager().getBackStackEntryAt(getChildFragmentManager().getBackStackEntryCount() - 1).getName();
//        } catch (Exception e) {
//            Log.e("TEST-Tab1Container","popAllFragments failed");
//            return "";
//
//        }
//    }

//    public boolean isTopFragmentShowing() {
//        try {
//            if(getChildFragmentManager().getBackStackEntryCount() == 0) {
//                return true;
//            } else {
//                return false;
//            }
////            (getChildFragmentManager().findFragmentByTag("savedtweetsallfragment")).isVisible();
////            Log.d("TEST","BACKSTACK ENTRY COUNT: " + getChildFragmentManager().getBackStackEntryCount());
////            Log.d("TEST","Apparently mylistfragment is on top...");
////            return true;
//        } catch (Exception e) {
//            Log.e("Tab2Container","Could not find userListFragment");
//            return false;
//        }
//    }
    public boolean updateTweetListFragment() {
        try {
            ((SavedTweetsListFragment) getChildFragmentManager().findFragmentByTag("savedtweetsallfragment")).updateMyListAdapter();
            return true;
        } catch (Exception e) {
            Log.e("Tab2Container","Could not find userListFragment");
            return false;
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("mIsViewInited", mIsViewInited);


    }


}