package com.jukuproject.jukutweet;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class MainActivity extends AppCompatActivity implements FragmentInteractionListener, DialogInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private AddFeedDialog addFeedDialogFragment;
    private RemoveFeedDialog removeFeedDialogFragment;
    private MainFragment mainFragment;
    private SmoothProgressBar progressbar;
    private FloatingActionButton fabAddFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        fabAddFeed = (FloatingActionButton) findViewById(R.id.fab);
        fabAddFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mViewPager != null && mViewPager.getCurrentItem() == 0) {
                    showAddDialog();
                } else {
                    Toast.makeText(MainActivity.this, "page: " + mViewPager.getCurrentItem() , Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            //TODO -- REPLACE WITH OWN FRAGMENT
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
//                    fabAddFeed.setVisibility(View.VISIBLE);
                    mainFragment = MainFragment.newInstance(position);
                    return mainFragment;
                case 1:
//                    fabAddFeed.setVisibility(View.GONE);
                    return PlaceholderFragment.newInstance(position + 1);
                default:
//                    fabAddFeed.setVisibility(View.GONE);
                    return PlaceholderFragment.newInstance(position + 1);
            }

//            if(position == 0) {
//                return MainFragment.newInstance(position);
//            } else {
//                return PlaceholderFragment.newInstance(position + 1);
//            }
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
                    return "Feeds";
                case 1:
                    return "My Lists";
                case 2:
                    return "Quiz All";
            }
            return null;
        }
    }



    /** The user has entered a new RSS feed into the window and clicked OK */

    /**
     * Recieves input text from add user dialog, enters
     * @param inputText
     */
    @Override
    public void onAddRSSDialogPositiveClick(String inputText) {
        InternalDB internalDBInstance = InternalDB.getInstance(getBaseContext());
        /** Check to DB to see if the new feed is a duplicate*/
        if(internalDBInstance.duplicateUser(inputText.trim())) {
            Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
        } else {
            /** Otherwise enter the URL into the DB and update the adapter */
            internalDBInstance.saveUser(inputText.trim());

            if(mainFragment!= null) {
                mainFragment.updateAdapter();
            }

            //TODO implement interaction with API
            /* Now try to pull the feed. First check for internet connection. **/
//            if (!isOnline()) {
//                Toast.makeText(getBaseContext(), "Device is not online", Toast.LENGTH_SHORT).show();
//            } else {
//                getRSSFeed(inputText.trim());
//            }
        }
    }

    @Override
    public void onRemoveRSSDialogDismiss() {
        removeFeedDialogFragment = null;
    }

    @Override
    public void onAddRSSDialogDismiss() {
        addFeedDialogFragment = null;
    }

    public void showRemoveDialog(String user) {
        if (removeFeedDialogFragment == null) {
            removeFeedDialogFragment = RemoveFeedDialog.newInstance(user);
            removeFeedDialogFragment.show(getFragmentManager(), "dialogRemove");
        }
    }

    /** User has chosen to remove a feed in the RemoveFeedDialog. Delete feed and update recyclerview in MainFragment **/
    @Override
    public void onRemoveRSSDialogPositiveClick(String user) {

        if(mainFragment == null) {
            Log.d("TEST", "MAIN FRAG IS NULL");
        }
        if (InternalDB.getInstance(getBaseContext()).deleteUser(user) && mainFragment != null) {
            if(mainFragment != null) {
                mainFragment.updateAdapter();
            }
        } else {
            Toast.makeText(this, "Could not remove item", Toast.LENGTH_SHORT).show();
        }

    }

    public void showProgressBar(Boolean show) {
        if(show) {
            progressbar.setVisibility(View.VISIBLE);
        } else {
            progressbar.setVisibility(View.INVISIBLE);

        }
    }

    /** Try to pull the feed.  */
    public void followUser(final String user) {
        //TODO fill in the getUser
        Toast.makeText(this, "FOLLOWING USER", Toast.LENGTH_SHORT).show();
    }

    public void showAddDialog(){
        if (addFeedDialogFragment == null || !addFeedDialogFragment.isAdded()) {
            addFeedDialogFragment = AddFeedDialog.newInstance();
            addFeedDialogFragment.show(getFragmentManager(), "dialogAdd");
        }
    }

}
