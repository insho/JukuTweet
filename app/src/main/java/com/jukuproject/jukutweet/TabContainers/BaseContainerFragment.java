package com.jukuproject.jukutweet.TabContainers;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.jukuproject.jukutweet.R;

public class BaseContainerFragment extends Fragment {


    public void replaceFragment(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.replace(R.id.container_framelayout, fragment, tag);
        transaction.commit();
        getChildFragmentManager().executePendingTransactions();

    }

    public void addFragment(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.add(R.id.container_framelayout, fragment, tag);
        transaction.commit();
        getChildFragmentManager().executePendingTransactions();
    }

    public boolean isTopFragmentShowing() {
        try {
            if(getChildFragmentManager().getBackStackEntryCount() == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e("TEST","isTopFragmentShowing error");
            return false;
        }
    }

    public boolean popFragment() {
        boolean isPop = false;
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            isPop = true;
            getChildFragmentManager().popBackStack();
        }
//        Log.d("TEST", "pop fragment NEW BACKSTACK COUNT: " + getChildFragmentManager().getBackStackEntryCount());
        return isPop;
    }



}