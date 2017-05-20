package com.jukuproject.jukutweet;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.jukuproject.jukutweet.TabContainers.Tab1Container;
import com.jukuproject.jukutweet.TabContainers.Tab2Container;
import com.jukuproject.jukutweet.TabContainers.Tab3Container;
import com.jukuproject.jukutweet.TabContainers.Tab4Container;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private String[] mCurrentTabs;

    public SectionsPagerAdapter(FragmentManager fm, String[] currentTabs) {
        super(fm);
        this.mCurrentTabs = currentTabs;
    }
//
//    public SectionsPagerAdapter2SavedInstance(FragmentManager fm, String[] currentTabs, Fragment[] savedFragments) {
//        super(fm);
//        this.mCurrentTabs = currentTabs;
//    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return Tab1Container.newInstance();
            case 1:
                return Tab2Container.newInstance();
            case 2:
                return Tab3Container.newInstance();
            case 3:
                return Tab4Container.newInstance();
            default:
                return Tab1Container.newInstance();
        }
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