package com.jukuproject.jukutweet.TabContainers;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.Fragments.MyListFragment;
import com.jukuproject.jukutweet.Fragments.QuizAllFragment;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.Fragments.UserListFragment;


public class Tab3Container extends BaseContainerFragment {

    private boolean mIsViewInited;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("test", "tab 3 oncreateview");
        return inflater.inflate(R.layout.container_fragment, null);
    }

    public Tab3Container() {
    }

    public static Tab3Container newInstance() {
        Tab3Container fragment = new Tab3Container();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("test", "tab 3 container on activity created");
        if (!mIsViewInited) {
            mIsViewInited = true;
            initView();
        }
    }

    private void initView() {
        Log.e("test", "tab 3 init view");
        replaceFragment(new QuizAllFragment(), false,"quizallfragment");
    }

}