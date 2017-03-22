package com.jukuproject.jukutweet;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
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
import com.jukuproject.jukutweet.Fragments.UserListFragment;
import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.TabContainers.Tab1Container;

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
//    private UserListFragment mUserListFragment;
//    private TimeLineFragment mTimeLineFragment;
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
                    showAddUserDialog();
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
     * Shows FollowUserDialogFragment, where user can input a new twitter handle to follow
     * Called from the fabAddUser button click
     */
    public void showAddUserDialog(){
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
    public void onAddUserDialogPositiveClick(String inputText) {


//        Log.d(TAG,"mUserListFragment null on ADD? " + (mUserListFragment == null));


        /** Check to DB to see if the new feed is a duplicate*/
        if(InternalDB.getInstance(getBaseContext()).duplicateUser(inputText.trim())) {
            Toast.makeText(this, "UserInfo already exists", Toast.LENGTH_SHORT).show();
        } else if (!isOnline()) {
            Toast.makeText(getBaseContext(), "Unable to access internet", Toast.LENGTH_SHORT).show();
            } else {
            tryToGetUserInfo(inputText.trim());
        }


            /** Otherwise enter the URL into the DB and update the adapter */



            /* Now try to pull the feed. First check for internet connection. **/
//            if (!isOnline()) {
//                Toast.makeText(getBaseContext(), "Device is not online", Toast.LENGTH_SHORT).show();
//            } else {
//                getRSSFeed(inputText.trim());
//            }

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

//        Log.d(TAG,"mUserListFragment null? " + (mUserListFragment == null));
        if (InternalDB.getInstance(getBaseContext()).deleteUser(screenName) ) {

            Log.d(TAG,"findfragbypos: " + findFragmentByPosition(0));

            // Locate Tab1Continer and update the UserListInfo adapter to reflect removed item
            if(findFragmentByPosition(0) != null && findFragmentByPosition(0) instanceof Tab1Container) {
                ((Tab1Container) findFragmentByPosition(0)).updateUserListFragment();
            }
//            mUserListFragment.updateAdapter();
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
    public void tryToGetUserInfo(final String screenName) {

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

                            /* If the user exists and a UserInfo object has been populated,
                            * save it to the database and update the UserInfoFragment adapter */
                            if(userInfoInstance != null) {

                                //TODO DO STUFF, like pull data into db, or whatever


                                if(InternalDB.getInstance(getBaseContext()).saveUser(userInfoInstance)) {
                                    Toast.makeText(MainActivity.this, "Successful pull for " + userInfoInstance.getScreenName() + "!", Toast.LENGTH_SHORT).show();

                                    // Locate Tab1Continer and update the UserListInfo adapter to reflect removed item
                                    if(findFragmentByPosition(0) != null && findFragmentByPosition(0) instanceof Tab1Container) {
                                        ((Tab1Container) findFragmentByPosition(0)).updateUserListFragment();
                                    }

                                }


//                            showToolBarBackButton(true,getArticleRequestType(requesttype));

                            } else {
                                Toast.makeText(MainActivity.this, "Unable to add user " + screenName, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override public void onError(Throwable e) {
                            e.printStackTrace();
                            if(debug){Log.d(TAG, "In onError()");}
                            Toast.makeText(getBaseContext(), "Unable to connect to Twitter API", Toast.LENGTH_SHORT).show();

                            Log.d(TAG,"ERROR CAUSE: " + e.getCause());
                        }

                        @Override public void onNext(UserInfo userInfo) {
                            if(debug) {
                                Log.d(TAG, "In onNext()");
                                Log.d(TAG, "userInfo: " + userInfo.getUserId() + ", " + userInfo.getDescription());
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
//        mViewPager.setAdapter(new CustomSectionsPagerAdapterUser(getSupportFragmentManager(),userInfo));

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


//    public Fragment getFragment() {
//        if(mTimeLineFragment != null) {
//            return mTimeLineFragment;
//        } else if(mUserListFragment != null) {
//            return mUserListFragment;
//        } else {
//            mUserListFragment = UserListFragment.newInstance();
//            return mUserListFragment;
//        }
//    }

    @Override
    public void onBackPressed() {
        boolean isPopFragment = false;

//Pop backstack depending on the overall tab position (if applicable)

        switch (mViewPager.getCurrentItem()) {
            case 0:
                isPopFragment = ((BaseContainerFragment)findFragmentByPosition(0)).popFragment();
                try {
                   int entryCount = ((BaseContainerFragment)findFragmentByPosition(0)).getChildFragmentManager().getBackStackEntryCount();
                    Log.d(TAG,"ENTRY IN BACKSTACK: " + entryCount);

                    /*the BaseContainerFragment always should contain at least one child,
                      so if the child count is 1 we can assume we are at the top level */
                    if(entryCount <= 1) {
                        showActionBarBackButton(false,getString(R.string.app_name));
                        changePagerTitle(0,"Users");
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG,"OnBackPressed child entrycount null : " + e);
                }

                    break;
            case 1:
                break;
            case 2:
                break;
            default:
                break;
        }

        if (!isPopFragment) {
            finish();
        }
//        super.onBackPressed();
    }



    public Fragment findFragmentByPosition(int position) {
//        FragmentPagerAdapter fragmentPagerAdapter = mSectionsPagerAdapter;
        return getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + mViewPager.getId() + ":"
                        + mSectionsPagerAdapter.getItemId(position));
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    public void showActionBarBackButton(Boolean showBack, CharSequence title) {

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(showBack);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(title);

//            if(showBack) {
//                hideOption(R.id.addFeed);
//                showOption(R.id.shareFacebook);
//            } else {
//
//                hideOption(R.id.shareFacebook);
//                showOption(R.id.addFeed);
//
//            }
        }

    }

    public void changePagerTitle(int position, String title) {
        if(mSectionsPagerAdapter != null) {
            mSectionsPagerAdapter.updateTitleData(title);
        }
    }

}
