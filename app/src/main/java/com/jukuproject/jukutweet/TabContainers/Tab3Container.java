package com.jukuproject.jukutweet.TabContainers;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.Fragments.WordListFragment;
import com.jukuproject.jukutweet.R;

/**
 * Fragment container for bucket 3. It's top level frag is : {@link WordListFragment}
 * @see BaseContainerFragment
 */
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


    public boolean isTopFragmentShowing() {
        try {
            return (getChildFragmentManager().getBackStackEntryCount() == 0);
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