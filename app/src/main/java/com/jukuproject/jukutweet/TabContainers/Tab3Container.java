package com.jukuproject.jukutweet.TabContainers;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.Fragments.MyListBrowseFragment;
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

    public boolean selectAll() {
        try {
            ((MyListBrowseFragment) getChildFragmentManager().findFragmentByTag("mylistbrowse")).selectAll();
            return true;
        } catch (Exception e) {
            Log.e("Tab3Container","Could not find MyListBrowseFragment");
            return false;
        }
    }

    public boolean deselectAll() {
        try {
            ((MyListBrowseFragment) getChildFragmentManager().findFragmentByTag("mylistbrowse")).deselectAll();
            return true;
        } catch (Exception e) {
            Log.e("Tab3Container","Could not find MyListBrowseFragment");
            return false;
        }
    }

//    public boolean openCopyDialog() {
//        try {
//            ((MyListBrowseFragment) getChildFragmentManager().findFragmentByTag("mylistbrowse")).showCopyMyListDialog();
//            return true;
//        } catch (Exception e) {
//            Log.e("Tab3Container","Could not find MyListBrowseFragment");
//            return false;
//        }
//    }
//
//    public boolean openCopyDialog() {
//        try {
//            ((MyListBrowseFragment) getChildFragmentManager().findFragmentByTag("mylistbrowse")).showCopyMyListDialog();
//            return true;
//        } catch (Exception e) {
//            Log.e("Tab3Container","Could not find MyListBrowseFragment");
//            return false;
//        }
//    }


    public boolean isTopFragmentShowing() {
        try {
            (getChildFragmentManager().findFragmentByTag("mylistfragment")).isVisible();
            return true;
        } catch (Exception e) {
            Log.e("Tab3Container","Could not find userListFragment");
            return false;
        }
    }


}