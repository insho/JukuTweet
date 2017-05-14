package com.jukuproject.jukutweet;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class PostQuizStatsPagerAdapter extends FragmentPagerAdapter {

    private String[] mCurrentTabs;
    private Fragment[] mSavedFragments;

    public PostQuizStatsPagerAdapter(FragmentManager fm, String[] currentTabs, Fragment[] savedFragments) {
        super(fm);
        this.mCurrentTabs = currentTabs;
        this.mSavedFragments = savedFragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mSavedFragments[position];
    }


    @Override
    public int getCount() {
        return mCurrentTabs.length;
    }

    //this is called when notifyDataSetChanged() is called
    @Override
    public int getItemPosition(Object object) {
        // refresh all fragments when data set changed
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return mCurrentTabs[position];
    }

    public void updateTabs(String[] updatedTabs) {
        this.mCurrentTabs = updatedTabs;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
}