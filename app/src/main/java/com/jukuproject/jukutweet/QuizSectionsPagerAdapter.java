package com.jukuproject.jukutweet;

/**
 * Created by JClassic on 3/21/2017.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
//import com.jukuproject.jukutweet.TabContainers.Tab4Container;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 //     */
public class QuizSectionsPagerAdapter extends FragmentPagerAdapter {

    String[] mCurrentTabs;
    Fragment[] mSavedFragments;
//    Intent mIntent;

    public QuizSectionsPagerAdapter(FragmentManager fm, String[] currentTabs, Fragment[] savedFragments) {
        super(fm);
        this.mCurrentTabs = currentTabs;
//        this.mIntent = mIntent;
        this.mSavedFragments = savedFragments;

    }

    @Override
    public Fragment getItem(int position) {

        return mSavedFragments[position];
//        switch (position) {
//            case 0:
//
//                if(mSavedFragments.length>position) {
//
//                }
//
//                /*1. get the menu option (Multiple Choice, Fill in the Blanks, etc.) from the quiz activity.
//                     This should always exist. */
//                String menuOption = mIntent.getStringExtra("menuOption"); //The type of quiz that was chosen inthe menu
//
//                /* Depending on the menuOption, pull the appropriate set of data from the intent and run the
//                * appropriate fragment */
//                String quizType = mIntent.getStringExtra("quizType");
//                MyListEntry myListEntry = mIntent.getParcelableExtra("myListEntry");
//                String quizSize = mIntent.getStringExtra("quizSize");
//                String colorString = mIntent.getStringExtra("colorString");
//                String timer = mIntent.getStringExtra("timer"); //Timer can be "none" so passing it on raw as string
//                double totalweight = mIntent.getDoubleExtra("totalweight",0);
//                String dataType = mIntent.getStringExtra("dataType");
//
//                switch (menuOption) {
//                    case "Multiple Choice":
//                        final ArrayList<WordEntry> datasetMultipleChoice = mIntent.getParcelableArrayListExtra("dataset");
//                        return QuizTab1Container.newMultipleChoiceInstance(datasetMultipleChoice
//                                ,quizType,timer,quizSize,totalweight,dataType,colorString,myListEntry);
//                    case "Fill in the Blanks":
//                        final ArrayList<Tweet> datasetFillBlanks = mIntent.getParcelableArrayListExtra("dataset");
//                        return QuizTab1Container.newFillintheBlanksInstance(datasetFillBlanks,quizSize,totalweight,colorString,myListEntry);
//                    default:
//                        break;
//                }
//
//            case 1:
//                return QuizTab2Container.newInstance();
//            default:
//                return QuizTab1Container.newInstance(mIntent.getStringExtra("mQuiz"));
//        }
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