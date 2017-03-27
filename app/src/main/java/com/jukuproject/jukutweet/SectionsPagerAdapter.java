package com.jukuproject.jukutweet;

/**
 * Created by JClassic on 3/21/2017.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.jukuproject.jukutweet.TabContainers.Tab1Container;
import com.jukuproject.jukutweet.TabContainers.Tab2Container;
import com.jukuproject.jukutweet.TabContainers.Tab3Container;
//import com.jukuproject.jukutweet.TabContainers.Tab4Container;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 //     */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

//    public String tab1Title;
//    public String tab2Title;
//    public int mTabCount;
    String[] mCurrentTabs;
//    private long baseId = 0;

    public SectionsPagerAdapter(FragmentManager fm,String[] currentTabs) {
        super(fm);
//        this.tab1Title = "Users";
//        this.tab1Title = ""
//        this.mTabCount = tabCount;
        this.mCurrentTabs = currentTabs;
    }

    @Override
    public Fragment getItem(int position) {

        //TODO -- REPLACE WITH CUSTOM FRAGMENT
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                return Tab1Container.newInstance();
            case 1:
                return Tab2Container.newInstance();
            case 2:
                return Tab3Container.newInstance();
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

//    @Override
//    public long getItemId(int position) {
//        // give an ID different from position when position has been changed
//        return baseId + position;
//    }
    @Override
    public CharSequence getPageTitle(int position) {

        return mCurrentTabs[position];
//            switch (position) {
//                case 0:
//                    return tab1Title;
//                case 1:
//                    return "My Lists";
//                case 2:
//                    return "Saved Tweets";
//                default:
//                    return "";
//            }
    }

//    public void updateTitleData(String title) {
//        this.tab1Title = title;
//        notifyDataSetChanged();
//    }

    public void updateTabs(String[] updatedTabs) {
        this.mCurrentTabs = updatedTabs;
        notifyDataSetChanged();
    }


}