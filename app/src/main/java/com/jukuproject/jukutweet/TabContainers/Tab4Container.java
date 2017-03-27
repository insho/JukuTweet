//package com.jukuproject.jukutweet.TabContainers;
//
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.jukuproject.jukutweet.BaseContainerFragment;
//import com.jukuproject.jukutweet.Fragments.QuizAllFragment;
//import com.jukuproject.jukutweet.R;
//import com.jukuproject.jukutweet.Fragments.UserListFragment;
//
////TODO -- possibly consolidate these into one?
//public class Tab4Container extends BaseContainerFragment {
//
//    private boolean mIsViewInited;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.container_fragment, null);
//        v.setTag(4);
//        Log.e("test", "tab 4 oncreateview");
//        return v;
//    }
//
//    public Tab4Container() {
//    }
//
//    public static Tab4Container newInstance() {
//        Tab4Container fragment = new Tab4Container();
////        Bundle args = new Bundle();
////        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
////        fragment.setArguments(args);
//        return fragment;
//    }
//
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        Log.e("test", "tab 4 container on activity created");
//        if (!mIsViewInited) {
//            mIsViewInited = true;
//            initView();
//        }
//    }
//
//    private void initView() {
//        Log.e("test", "tab 1 init view");
//        replaceFragment(new QuizAllFragment(), false,"quizallfragment");
//
//    }
//
////    public boolean updateUserListFragment() {
////        try {
////            ((UserListFragment) getChildFragmentManager().findFragmentByTag("userListFragment")).updateAdapter();
////            return true;
////        } catch (Exception e) {
////            Log.e("Tab4Container","Could not find userListFragment");
////            return false;
////        }
////    }
//
//}