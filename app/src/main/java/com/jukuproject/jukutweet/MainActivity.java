package com.jukuproject.jukutweet;

import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
//    private CustomSectionsPagerAdapterUser mSectionsPagerAdapterUser;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private AddUserDialog addUserDialogFragment;
    private RemoveUserDialog removeUserDialogFragment;
    private MainFragment mMainFragment;
    private TimeLineFragment mTimeLineFragment;
    private SmoothProgressBar progressbar;
    private FloatingActionButton fabAddUser;
    private static final String TAG = "TEST-Main";
    private static final boolean debug = true;
//    private boolean showTimeLine = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),null);
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
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
//     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private UserInfo mUserInfo;
        public SectionsPagerAdapter(FragmentManager fm, @Nullable UserInfo userInfo) {
            super(fm);
            this.mUserInfo = userInfo;
        }

        @Override
        public Fragment getItem(int position) {

            //TODO -- REPLACE WITH CUSTOM FRAGMENT
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
//                    fabAddFeed.setVisibility(View.VISIBLE);
//                    mainFragment = MainFragment.newInstance(position);
//                    return mainFragment;
                    Log.d(TAG,"mUserInfo outside: " + (mUserInfo == null));
                    if(mUserInfo != null) {
                        Log.d(TAG,"mTimeLineFragment inside: " + (mTimeLineFragment == null));
                       if(mTimeLineFragment == null) {
                           return TimeLineFragment.newInstance(mUserInfo);
                       } else {
                           return TimeLineFragment.newInstance(mUserInfo);
                       }
                    } else {
                        if(mMainFragment == null) {
                            return MainFragment.newInstance();
                        } else {
                            return mMainFragment;
                        }
                    }

                case 1:
//                    fabAddFeed.setVisibility(View.GONE);
                    return UserListContainer.newInstance();
                default:
//                    fabAddFeed.setVisibility(View.GONE);
                    return CustomSectionsPagerAdapter.PlaceholderFragment.newInstance(position + 1);
            }
        }


        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(mTimeLineFragment != null && mTimeLineFragment.isAdded() && mTimeLineFragment.isVisible()) {
                switch (position) {
                    case 0:
                        return "CURRENT TIMELINE";
                    case 1:
                        return "USERS SAVED TWEETS";
                    case 2:
                        return "Quiz THIS USER";
                }

            } else {
                switch (position) {
                    case 0:
                        return "Feeds";
                    case 1:
                        return "My Lists";
                    case 2:
                        return "Quiz All";
                }
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


        Log.d(TAG,"mMainFragment null on ADD? " + (mMainFragment == null));

        InternalDB internalDBInstance = InternalDB.getInstance(getBaseContext());
        /** Check to DB to see if the new feed is a duplicate*/
        if(internalDBInstance.duplicateUser(inputText.trim())) {
            Toast.makeText(this, "UserInfo already exists", Toast.LENGTH_SHORT).show();
        } else if(internalDBInstance.saveUser(inputText.trim())){
            /** Otherwise enter the URL into the DB and update the adapter */

//        mSectionsPagerAdapter.onMainFragmentUpdate();
//            mMainFragment.updateAdapter();

            //TODO implement interaction with API
            //TODO check that its real?
            getUserInfo(inputText.trim());
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

        Log.d(TAG,"mMainFragment null? " + (mMainFragment == null));
        if (InternalDB.getInstance(getBaseContext()).deleteUser(screenName) ) {
//            mMainFragment.updateAdapter();
//            mSectionsPagerAdapter.onMainFragmentUpdate();
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
    public void getUserInfo(String screenName) {

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

    public void showTimeLine(UserInfo userInfo) {

//    Log.d(TAG,"mtimeline: " + (mTimeLineFragment == null) );
        Log.d(TAG,"HERE IN SHOWTIELINE -- userInfo: " + (userInfo.getScreenName()) );
//            if (mTimeLineFragment == null) {
//                mTimeLineFragment = TimeLineFragment.newInstance(userInfo);
//                //TODO -- MAKE THE PAGERVIEW THING REPRESENT THE CURRENT TIMELINE... AND THE PLUS BUTTON
//
//            }

//        mSectionsPagerAdapter.onSwitchToTimeLineFragment(userInfo);

//        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.container)).commit();

        //        ;
//        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(new CustomSectionsPagerAdapterUser(getSupportFragmentManager(),userInfo));

//        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),userInfo);
//        mSectionsPagerAdapter.notifyDataSetChanged();
//        mViewPager.setAdapter(mSectionsPagerAdapter);
//        mViewPager.getCurrentItem();
//
//            getSupportFragmentManager().beginTransaction()
//                    .addToBackStack("mainFragment")
//                    .replace(R.id.container, mTimeLineFragment)
//                    .commit();
//            showToolBarBackButton(true, "Article Categories");




    }


    public Fragment getFragment() {
        if(mTimeLineFragment != null) {
            return mTimeLineFragment;
        } else if(mMainFragment != null) {
            return mMainFragment;
        } else {
            mMainFragment = MainFragment.newInstance();
            return mMainFragment;
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }
}
