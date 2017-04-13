package com.jukuproject.jukutweet;

/**
 * Created by JClassic on 3/21/2017.
 */

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.TabContainers.QuizTab1Container;
import com.jukuproject.jukutweet.TabContainers.QuizTab2Container;

import java.util.ArrayList;
//import com.jukuproject.jukutweet.TabContainers.Tab4Container;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 //     */
public class QuizSectionsPagerAdapter extends FragmentPagerAdapter {

    String[] mCurrentTabs;
    Intent mIntent;

    public QuizSectionsPagerAdapter(FragmentManager fm, String[] currentTabs,Intent mIntent) {
        super(fm);
        this.mCurrentTabs = currentTabs;
        this.mIntent = mIntent;

    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
//                if(getPageTitle(position).equals("Multiple Choice")) {
//                    return QuizTab1Container.newInstance();
//                } else {
                String menuOption = mIntent.getStringExtra("menuOption"); //The type of quiz that was chosen inthe menu
                String quizType = mIntent.getStringExtra("quizType");
                int tabNumber = mIntent.getIntExtra("tabNumber", 2);
                MyListEntry myListEntry = mIntent.getParcelableExtra("myListEntry");
                String quizSize = mIntent.getStringExtra("quizSize");
                String colorString = mIntent.getStringExtra("colorString");
                String timer = mIntent.getStringExtra("timer"); //Timer can be "none" so passing it on raw as string
                double totalweight = mIntent.getDoubleExtra("totalweight",0);
                ArrayList<WordEntry> x = mIntent.getParcelableArrayListExtra("dataset");
                return QuizTab1Container.newMultipleChoiceInstance( x
                ,quizType,timer,quizSize,totalweight,"Word",colorString,myListEntry);
//                    return QuizTab1Container.newInstance(mIntent.getStringExtra("Multiple Choice"));
//                }
            case 1:
                return QuizTab2Container.newInstance();
            default:
                return QuizTab1Container.newInstance(mIntent.getStringExtra("mQuiz"));
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
    }

    public void updateTabs(String[] updatedTabs) {
        this.mCurrentTabs = updatedTabs;
        notifyDataSetChanged();
    }


}