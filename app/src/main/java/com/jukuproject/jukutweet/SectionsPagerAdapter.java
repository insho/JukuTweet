package com.jukuproject.jukutweet;

/**
 * Created by JClassic on 3/21/2017.
 */

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.TabContainers.Tab1Container;
import com.jukuproject.jukutweet.TabContainers.Tab2Container;
import com.jukuproject.jukutweet.TabContainers.Tab3Container;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 //     */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public String tab1Title;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
        this.tab1Title = "Users";
    }

    @Override
    public Fragment getItem(int position) {

        //TODO -- REPLACE WITH CUSTOM FRAGMENT
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                return Tab1Container.newInstance();
////                    fabAddFeed.setVisibility(View.VISIBLE);
////                    mainFragment = UserListFragment.newInstance(position);
////                    return mainFragment;
//                    Log.d(TAG,"mUserInfo outside: " + (mUserInfo == null));
//                    if(mUserInfo != null) {
//                        Log.d(TAG,"mTimeLineFragment inside: " + (mTimeLineFragment == null));
//                       if(mTimeLineFragment == null) {
//                           return TimeLineFragment.newInstance(mUserInfo);
//                       } else {
//                           return TimeLineFragment.newInstance(mUserInfo);
//                       }
//                    } else {
//                        if(mUserListFragment == null) {
//                            return UserListFragment.newInstance();
//                        } else {
//                            return mUserListFragment;
//                        }
//                    }

            case 1:
//                    fabAddFeed.setVisibility(View.GONE);
                return Tab2Container.newInstance();
            case 2:
                return Tab3Container.newInstance();
            default:
//                    fabAddFeed.setVisibility(View.GONE);
                return Tab1Container.newInstance();
        }
    }


    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }




    @Override
    public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return tab1Title;
                case 1:
                    return "My Lists";
                case 2:
                    return "Quiz All";
                default:
                    return "";
            }
    }

    public void updateTitleData(String title) {
        this.tab1Title = title;
        notifyDataSetChanged();
    }
}