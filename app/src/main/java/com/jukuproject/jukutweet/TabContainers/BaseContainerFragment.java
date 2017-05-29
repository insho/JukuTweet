package com.jukuproject.jukutweet.TabContainers;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.jukuproject.jukutweet.R;

/**
 * Basic "ground floor" fragment that is the initial fragment added to the Tab Buckets in the {@link com.jukuproject.jukutweet.SectionsPagerAdapter} in
 * the {@link com.jukuproject.jukutweet.MainActivity} and {@link com.jukuproject.jukutweet.PostQuizStatsActivity}. This way,
 * fragment transactions on sub-fragments and the fragment stack for each bucket can be run through this fragment
 */
public class BaseContainerFragment extends Fragment {

    /**
     * Replace current fragment in the tab bucket
     * @param fragment fragment to add
     * @param addToBackStack bool true to add to backstack, false to not
     * @param tag backstack entry tag associated with fragment
     */
    public void replaceFragment(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.replace(R.id.container_framelayout, fragment, tag);
        transaction.commit();
        getChildFragmentManager().executePendingTransactions();

    }

    /**
     * Adds fragment to backstack for tab bucket
     * @param fragment fragment to add
     * @param addToBackStack bool true to add to backstack, false to not
     * @param tag backstack entry tag associated with fragment
     */
    public void addFragment(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.add(R.id.container_framelayout, fragment, tag);
        transaction.commit();
        getChildFragmentManager().executePendingTransactions();
    }

    /**
     * @return bool true if there are no fragments in backstack for a tab bucket
     */
    public boolean isTopFragmentShowing() {
        try {
            return (getChildFragmentManager().getBackStackEntryCount() == 0);
        } catch (Exception e) {
            Log.e("TEST","isTopFragmentShowing error");
            return false;
        }
    }

    /**
     * Pops the fragment backstack
     * @return true if popped, false if stack only has one entry
     */
    public boolean popFragment() {
        boolean isPop = false;
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            isPop = true;
            getChildFragmentManager().popBackStack();
        }
        return isPop;
    }

    /**
     * Returns the top fragment in the stack
     * @return top fragment
     */
    public Fragment getTopFragment() {
        return getChildFragmentManager().findFragmentById(R.id.container_framelayout);
    }
}