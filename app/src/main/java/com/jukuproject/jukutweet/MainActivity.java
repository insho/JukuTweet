package com.jukuproject.jukutweet;

import android.support.design.widget.FloatingActionButton;
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

import android.widget.Toast;

import com.jukuproject.jukutweet.Dialogs.AddUserDialog;
import com.jukuproject.jukutweet.Dialogs.RemoveUserDialog;
import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Models.UserInfo;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Main activity fragment manager
 */
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
    private AddUserDialog addUserDialogFragment;
    private RemoveUserDialog removeUserDialogFragment;
    private MainFragment mainFragment;
    private SmoothProgressBar progressbar;
    private FloatingActionButton fabAddUser;
    private static final String TAG = "TEST-Main";
    private static final boolean debug = true;

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
        fabAddUser = (FloatingActionButton) findViewById(R.id.fab);
        progressbar = (SmoothProgressBar) findViewById(R.id.progressbar);
        fabAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mViewPager != null && mViewPager.getCurrentItem() == 0) {
                    showFollowUserDialog();
                } else {
                    //TODO replace this
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

            //TODO -- REPLACE WITH CUSTOM FRAGMENT
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


    /**
     * Shows FollowUserDialogFragment, where user can input a new twitter handle to follow
     * Called from the fabAddUser button click
     */
    public void showFollowUserDialog(){
        if (addUserDialogFragment == null || !addUserDialogFragment.isAdded()) {
            addUserDialogFragment = AddUserDialog.newInstance();
            addUserDialogFragment.show(getFragmentManager(), "dialogAdd");
        }
    }

    /**
     * Recieves input text from add user dialog
     * Checks if that user already exists in database
     * If not, inputs user into db and updates mainFragment recycler
     * @param inputText
     */
    @Override
    public void onFollowUserDialogPositiveClick(String inputText) {
        InternalDB internalDBInstance = InternalDB.getInstance(getBaseContext());
        /** Check to DB to see if the new feed is a duplicate*/
        if(internalDBInstance.duplicateUser(inputText.trim())) {
            Toast.makeText(this, "UserInfo already exists", Toast.LENGTH_SHORT).show();
        } else if(internalDBInstance.saveUser(inputText.trim()) && mainFragment!= null){
            /** Otherwise enter the URL into the DB and update the adapter */
            mainFragment.updateAdapter();

            //TODO implement interaction with API
            /* Now try to pull the feed. First check for internet connection. **/
//            if (!isOnline()) {
//                Toast.makeText(getBaseContext(), "Device is not online", Toast.LENGTH_SHORT).show();
//            } else {
//                getRSSFeed(inputText.trim());
//            }
        } else {
            Toast.makeText(this, "Unable to follow user", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets add and remove dialog fragments to null, in order to avoid multiple instances of the fragment
     * if the user repeatedly clicks the fab
     */
    @Override
    public void onUserDialogDismiss() {
        addUserDialogFragment = null;
        removeUserDialogFragment = null;
    }

    /**
     * Shows remove UserInfo Dialog
     * @param user UserInfo to "unfollow" (i.e. remove from database)
     */
    public void showRemoveUserDialog(String user) {
        if (removeUserDialogFragment == null) {
            removeUserDialogFragment = RemoveUserDialog.newInstance(user);
            removeUserDialogFragment.show(getFragmentManager(), "dialogRemove");
        }
    }

    /**
     * Removes a users screenName from the database and updates recyclerview in main fragment.
     * Is called from {@link RemoveUserDialog} via the {@link DialogInteractionListener}
     * @param screenName UserInfo to remove from database
     *
     */
    @Override
    public void onRemoveUserDialogPositiveClick(String screenName) {

        if (InternalDB.getInstance(getBaseContext()).deleteUser(screenName) && mainFragment != null) {
                mainFragment.updateAdapter();
        } else {
            Toast.makeText(this, "Could not remove item", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Shows progress bar during API lookups, hides otherwise
     * @param show boolean True for show, False for hide
     */
    public void showProgressBar(Boolean show) {
        if(show) {
            progressbar.setVisibility(View.VISIBLE);
        } else {
            progressbar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Pulls twitter feed activity for a user into a list of FeedItems
     * @param screenName
     */
    public void getUserFeed( String screenName) {

//        Toast.makeText(this, "FOLLOWING USER", Toast.LENGTH_SHORT).show();


        String token = getResources().getString(R.string.access_token);
        String tokenSecret = getResources().getString(R.string.access_token_secret);

        TwitterUserClient.getInstance(token,tokenSecret)
                .getUserInfo(screenName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserInfo>() {
                    UserInfo userInfoInstance;

                    @Override public void onCompleted() {
                        if(debug){
                            Log.d(TAG, "In onCompleted()");}

                        if(userInfoInstance != null) {

                            //TODO DO STUFF, like pull data into db, or whatever

                            Toast.makeText(MainActivity.this, "Successful pull for " + userInfoInstance.getScreenName() + "!", Toast.LENGTH_SHORT).show();

//                            if (articleListFragment == null) {
//                                articleListFragment = new ArticleListFragment();
//
//                                Bundle args = new Bundle();
//                                args.putParcelableArrayList("loadedArticles",loadedArticles);
//                                articleListFragment.setArguments(args);
//                            }
//
//                            getSupportFragmentManager().beginTransaction()
//                                    .addToBackStack("articlelistfrag")
//                                    .replace(R.id.container, articleListFragment)
//                                    .commit();


//                            showToolBarBackButton(true,getArticleRequestType(requesttype));
                        }
                    }

                    @Override public void onError(Throwable e) {
                        e.printStackTrace();
                        if(debug){Log.d(TAG, "In onError()");}
                        Toast.makeText(getBaseContext(), "Unable to connect to Twitter API", Toast.LENGTH_SHORT).show();
                    }

                    @Override public void onNext(UserInfo userInfo) {
                        if(debug) {
                            Log.d(TAG, "In onNext()");
                            Log.d(TAG, "userInfo: " + userInfo.getId() + ", " + userInfo.getDescription());
                        }

                        /***TMP**/
                        if(userInfoInstance == null) {
                            userInfoInstance = userInfo;
                        }


                    }
                });
    }

    /**
     *  Initiates twitter API lookup to check if user exists
     * @param screenName
     */
    public void checkIfUserExists(final String screenName) {

    }



}
