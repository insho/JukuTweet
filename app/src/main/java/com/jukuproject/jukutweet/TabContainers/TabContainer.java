//package com.jukuproject.jukutweet.TabContainers;
//
//
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentTransaction;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
////import com.jukuproject.jukutweet.BaseContainerFragment;
//import com.jukuproject.jukutweet.BaseContainerFragment;
//import com.jukuproject.jukutweet.Fragments.MyListFragment;
//import com.jukuproject.jukutweet.Fragments.QuizAllFragment;
//import com.jukuproject.jukutweet.Fragments.SavedTweetsAllFragment;
//import com.jukuproject.jukutweet.Models.TabStatus;
//import com.jukuproject.jukutweet.R;
//import com.jukuproject.jukutweet.Fragments.UserListFragment;
//
//import java.util.List;
//
////TODO -- possibly consolidate these into one?
//public class TabContainer extends BaseContainerFragment {
//
////    private boolean mIsViewInited;
//    private TabStatus mTabStatus;
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        Log.d("TEST", "tabcontainer oncreateview");
//        return inflater.inflate(R.layout.container_fragment, null);
//    }
//
////    public TabContainer(TabStatus tabStatus) {
////        this.mTabStatus = tabStatus;
////    }
//
////    public static TabContainer newInstance(TabStatus tabStatus) {
////
////
////        TabContainer fragment = new TabContainer();
////        Bundle args = new Bundle();
////        args.putParcelable("tabStatus", tabStatus);
////        fragment.setArguments(args);
////        return fragment;
////    }
//
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        mTabStatus = getArguments().getParcelable("tabStatus");
//        Log.d("TEST", "tab container on activity created " + mTabStatus.getTabNumber());
//        if (!mTabStatus.isViewInited()) {
//            mTabStatus.setViewInited(true);
//            initView();
//        }
//    }
//
//    private void initView() {
//        Log.e("TEST", "tab init view "  + mTabStatus.getTabNumber());
//        switch (mTabStatus.getTabNumber()) {
//            case 0:
//                replaceFragment(new UserListFragment(), false,"userlistfragment");
//            case 1:
//                replaceFragment(new MyListFragment(), false,"mylistfragment");
//            case 2:
//                replaceFragment(new SavedTweetsAllFragment(), false,"savedtweetsallfragment");
//            case 3:
//                replaceFragment(new QuizAllFragment(), false,"quizallfragment");
//            default:
//                break;
//        }
//
//
//    }
//
//    public boolean updateFragment() {
//
//            switch (mTabStatus.getTabNumber()) {
//                case 0:
//                    try {
//                    ((UserListFragment) getChildFragmentManager().findFragmentByTag("userlistfragment")).updateAdapter();
//                    } catch (Exception e) {
//                        Log.e("Tab1Container","Could not find userListFragment");
//                        return false;
//                    }
//                case 1:
//                    break;
//                case 2:
//                    break;
//                case 3:
//                    break;
//                default:
//                    break;
//            }
//
//            return true;
//
//    }
//
//
////    public void replaceFragment(Fragment fragment, boolean addToBackStack, String tag) {
////        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
////        if (addToBackStack) {
////            transaction.addToBackStack(null);
////        }
////        transaction.replace(R.id.container_framelayout, fragment, tag);
////        transaction.commit();
////        getChildFragmentManager().executePendingTransactions();
////    }
////
//////    public int getChildStackCount() {
//////        return getChildFragmentManager().getBackStackEntryCount();
//////    }
////
////    public boolean popFragment() {
////        Log.d("TEST", "pop fragment: " + getChildFragmentManager().getBackStackEntryCount());
////        boolean isPop = false;
////        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
////            isPop = true;
////            getChildFragmentManager().popBackStack();
////        }
////        return isPop;
////    }
//
//}