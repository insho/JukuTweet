package com.jukuproject.jukutweet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jukuproject.jukutweet.Models.UserInfo;

/**
 * Created by fJClassic on 3/21/2017.
 */

public class CustomSectionsPagerAdapterUser  extends FragmentPagerAdapter
{
    private static final int NUM_ITEMS = 3;
    private final FragmentManager mFragmentManager;
    private Fragment mFragmentAtPos0;
    private UserInfo mUserInfo;
    //    private UserInfo mUserInfo;
//    public CustomSectionsPagerAdapter(FragmentManager fm)
//    {
//        super(fm);
//        mFragmentManager = fm;
//    }
    public CustomSectionsPagerAdapterUser(FragmentManager fm, UserInfo userInfo) {
        super(fm);
        mFragmentManager = fm;
        mUserInfo = userInfo;
        mFragmentAtPos0 = TimeLineFragment.newInstance(mUserInfo);

        mFragmentManager.beginTransaction().replace(R.id.container,mFragmentAtPos0).commit();
    }
    @Override
    public Fragment getItem(int position)
    {
        if (position == 0)
        {
            Log.d("TEST", "USER FRAG! at pos 0 -- (mFragmentAtPos0 == null) " + (mFragmentAtPos0 == null));
            if (mFragmentAtPos0 == null)
            {
                mFragmentAtPos0 = TimeLineFragment.newInstance(mUserInfo);

            }
            return mFragmentAtPos0;
        } else {
            return PlaceholderFragment.newInstance(position + 1);


        }


//        if (mFragmentAtPos0 == null) {
//            mFragmentAtPos0 = MainFragment.newInstance();
//        }
//        return mFragmentAtPos0;

//        switch (position) {
//            case 0:
//                if (mFragmentAtPos0 == null) {
//                    mFragmentAtPos0 = MainFragment.newInstance();
//                }
//                return mFragmentAtPos0;
//            case 1:
//                return PlaceholderFragment.newInstance(position + 1);
//            case 2:
//                return PlaceholderFragment.newInstance(position + 1);
//            case POSITION_NONE:
//                if (mFragmentAtPos0 == null) {
//                    mFragmentAtPos0 = MainFragment.newInstance();
//                }
//                return mFragmentAtPos0;
//            default:
//                if (mFragmentAtPos0 == null) {
//                    mFragmentAtPos0 = MainFragment.newInstance();
//                }
//                return mFragmentAtPos0;
//        }

//        if (position == 0)
//        {
//            Log.d("TEST", "here at pos 0 -- (mFragmentAtPos0 == null) " + (mFragmentAtPos0 == null));
//            if (mFragmentAtPos0 == null)
//            {
//                mFragmentAtPos0 = MainFragment.newInstance();
//            }
//            return mFragmentAtPos0;
//        }
//        else
//            return PlaceholderFragment.newInstance(position + 1);
    }

    @Override
    public int getCount()
    {
        return NUM_ITEMS;
    }

    @Override
    public int getItemPosition(Object object)
    {

        return POSITION_NONE;

//        if (object instanceof MainFragment &&
//                mFragmentAtPos0 instanceof TimeLineFragment) {
//            Log.d("TEST","Pos none object switch");
//            return POSITION_NONE;
//        }
//        if (object instanceof TimeLineFragment &&
//                mFragmentAtPos0 instanceof MainFragment) {
//            return POSITION_NONE;
//        }
//        return POSITION_UNCHANGED;

//        if (object instanceof MainFragment && mFragmentAtPos0 instanceof TimeLineFragment)

//            return POSITION_NONE;
//        return POSITION_UNCHANGED;
    }

//    public void onMainFragmentUpdate() {
//        if(mFragmentAtPos0 == null) {
//            mFragmentAtPos0 = MainFragment.newInstance();
//            notifyDataSetChanged();
//        } else if (mFragmentAtPos0 instanceof MainFragment) {
//            ((MainFragment) mFragmentAtPos0).updateAdapter();
//        }
//    }

    public void onSwitchToTimeLineFragment(UserInfo userInfo){

//        if(mFragmentAtPos0 == null) {
//            Log.d("TEST","HERE in MFragment is NULL!");
//            mFragmentAtPos0 = TimeLineFragment.newInstance(userInfo);
//            notifyDataSetChanged();
//        } else
        mFragmentManager.beginTransaction().remove(mFragmentAtPos0).commit();

        if(mFragmentAtPos0 instanceof MainFragment) {
            Log.d("TEST","HERE in MFragment is instance of Main");
//            mFragmentManager.findFragmentByTag("timeline").

            mFragmentAtPos0 = TimeLineFragment.newInstance(userInfo);
//            mFragmentManager.beginTransaction().show(mFragmentAtPos0).commit();
            notifyDataSetChanged();
        }

//        if (mFragmentAtPos0 instanceof MainFragment) {
//            mFragmentManager.beginTransaction().remove(mFragmentAtPos0).commit();
//            mFragmentAtPos0 = TimeLineFragment.newInstance(userInfo);
//            ((TimeLineFragment) mFragmentAtPos0).pullTimeLineData(userInfo.getScreenName());
//            notifyDataSetChanged();
//        } else if (mFragmentAtPos0 instanceof TimeLineFragment){
//            mFragmentManager.beginTransaction().remove(mFragmentAtPos0).commit();
//            mFragmentAtPos0 = MainFragment.newInstance();
//            notifyDataSetChanged();
//        }

    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            RecyclerView xrr = (RecyclerView) rootView.findViewById(R.id.recyclerMain);
            xrr.setVisibility(View.GONE);
            TextView textView = (TextView) rootView.findViewById(R.id.nolists);
            textView.setText("BALLS");
            textView.setVisibility(View.VISIBLE);
            Log.d("TEST","IN xxxxx ADATPER");
            return rootView;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0:
                return "Feeds";
            case 1:
                return "My Lists";
            case 2:
                return "Quiz All";
            default:
                return "";
        }
    }
}

//public interface FirstPageFragmentListener
//{
//    void onSwitchToTimeLineFragment();
//}