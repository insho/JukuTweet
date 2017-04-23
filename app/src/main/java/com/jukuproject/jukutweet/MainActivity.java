package com.jukuproject.jukutweet;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Database.ExternalDB;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.AddOrRenameMyListDialog;
import com.jukuproject.jukutweet.Dialogs.AddUserCheckDialog;
import com.jukuproject.jukutweet.Dialogs.AddUserDialog;
import com.jukuproject.jukutweet.Dialogs.EditMyListDialog;
import com.jukuproject.jukutweet.Dialogs.RemoveUserDialog;
import com.jukuproject.jukutweet.Dialogs.UserDetailPopupDialog;
import com.jukuproject.jukutweet.Fragments.FlashCardsFragment;
import com.jukuproject.jukutweet.Fragments.SavedTweetsBrowseFragment;
import com.jukuproject.jukutweet.Fragments.SavedTweetsListFragment;
import com.jukuproject.jukutweet.Fragments.SearchFragment;
import com.jukuproject.jukutweet.Fragments.WordListBrowseFragment;
import com.jukuproject.jukutweet.Fragments.WordListFragment;
import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
import com.jukuproject.jukutweet.Interfaces.DialogRemoveUserInteractionListener;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.QuizMenuDialogInteractionListener;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.SearchTweetsContainer;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetUrl;
import com.jukuproject.jukutweet.Models.UserFollowersListContainer;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.TabContainers.Tab1Container;
import com.jukuproject.jukutweet.TabContainers.Tab2Container;
import com.jukuproject.jukutweet.TabContainers.Tab3Container;
import com.jukuproject.jukutweet.TabContainers.Tab4Container;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Main activity fragment manager
 */
public class MainActivity extends AppCompatActivity implements FragmentInteractionListener
        , DialogInteractionListener
        , QuizMenuDialogInteractionListener
        , DialogRemoveUserInteractionListener
{

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
    private SmoothProgressBar progressbar;
    private FloatingActionButton fab;
    private Menu mMenu;
    //array keeping track of which tabs (either tab2,tab3, both, or neither) have the browsemenu icons showing. defaults to false
    private boolean[] tabsShowingBrowseMenu = new boolean[4];
    private String[] mAdapterTitles;
    private static final String TAG = "TEST-Main";
    private static final boolean debug = true;
//    private  Observable<ArrayList<WordEntry>> queryDictionary;
    private Subscription searchQuerySubscription;
    private Subscription userInfoSubscription;

    Scheduler parseTweetScheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    private boolean fragmentWasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new ExternalDB(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPref = getBaseContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        /* Check for the existence of sharedPreferences. Insert defaults if they don't exist */
        if (sharedPref != null) {
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        }

        Fragment[] fragments = new Fragment[4];
        if (savedInstanceState != null) {
            //Restore the fragment's instance
            fragments[0] = (Tab1Container)getSupportFragmentManager().getFragment(savedInstanceState, "tab1Container");
            fragments[1] = (Tab2Container)getSupportFragmentManager().getFragment(savedInstanceState, "tab2Container");
            fragments[2] = (Tab3Container)getSupportFragmentManager().getFragment(savedInstanceState, "tab3Container");
            fragments[3] = (Tab4Container)getSupportFragmentManager().getFragment(savedInstanceState, "tab4Container");

            mAdapterTitles = savedInstanceState.getStringArray("adapterTitles");
            tabsShowingBrowseMenu = savedInstanceState.getBooleanArray("tabsShowingBrowseMenu");
            fragmentWasChanged = savedInstanceState.getBoolean("fragmentWasChanged");

        } else {
            fragments[0] = Tab1Container.newInstance();
            fragments[1] = Tab2Container.newInstance();
            fragments[2] = Tab3Container.newInstance();
            fragments[3] = Tab4Container.newInstance();
            mAdapterTitles = new String[]{"Users","Saved Tweets","Word Lists","Search"};
            fragmentWasChanged = getIntent().getBooleanExtra("fragmentWasChanged",false);
        }


        // Create the adapter that will return a fragment for each of the primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),mAdapterTitles,fragments);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        /*choose a tab to focus on initially. Default to 0. tabNumber is passed
        to quiz activity when a quiz is chosen, and passed back via intent to mainactivity onBackPressed, and
        this tabNumber is used to focus on the last open tab.
         */

        if(getIntent()!= null) {
            mViewPager.setCurrentItem(getIntent().getIntExtra("tabNumber",0));
            setPreviousMyListExpanded(getIntent().getIntExtra("tabNumber",0),getIntent().getIntExtra("lastExpandedPosition",0));
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            /**
             * Changes visibility and function of floating action button depending on which bucket is currently visible
             * @param position viewpager position (bucket)
             */
            @Override
            public void onPageSelected(int position) {
                if(fragmentWasChanged && mSectionsPagerAdapter != null) {
                    mSectionsPagerAdapter.notifyDataSetChanged();
                    fragmentWasChanged = false;
                }

                Log.d(TAG,"ISTOP SHOWING FOR - " + position + ", " + isTopShowing(position));

                /* The fab has different functions and appearance depending on which tab is visible */
                if(isTopShowing(position)) {
                    switch (position) {
                        case 0:
                            showFab(true,"addUser");
                            showMenuMyListBrowse(false,position);
                            break;
                        case 1:
                            showFab(true,"addTweetList");
                            showMenuMyListBrowse(false,position);
                            break;
                        case 2:
                            showFab(true,"addMyList");
                            showMenuMyListBrowse(false,position);
                            break;
                        default:
                            showMenuMyListBrowse(false,position);
                            showFab(false);
                            break;
                    }

                } else {
                    showFab(false);
                    if(tabsShowingBrowseMenu!=null && tabsShowingBrowseMenu[position]) {
                        showMenuMyListBrowse(true,position);
                    } else {
                        showMenuMyListBrowse(false,position);
                    }
                }



            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        progressbar = (SmoothProgressBar) findViewById(R.id.progressbar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mViewPager != null) {
                   if(mViewPager.getCurrentItem() == 0 && isTopShowing(0)) {
                        showAddUserDialog();
                    }
                   else if(mViewPager.getCurrentItem() == 1 && isTopShowing(1)) {
                       showAddMyListDialog("TweetList");
                   }
                   else if(mViewPager.getCurrentItem() == 2 && isTopShowing(2)) {
                       showAddMyListDialog("MyList");
                   }
            }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
     * Displays the actionbar icon set for selecting/copying/deleting items from a word or tweet list
     * @param show true to show, false to hide
     */
    public void showMenuMyListBrowse(boolean show, int tabNumber){
        if(mMenu == null) {
            return;
        }
        mMenu.setGroupVisible(R.id.menu_main_group, !show);
        tabsShowingBrowseMenu[tabNumber] = show;
        mMenu.setGroupVisible(R.id.menu_browsemylist_group, show);

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

        switch(id) {
             case android.R.id.home:
                        onBackPressed();
                       break;

            case R.id.action_cancel:
                try {
                    if(tabsShowingBrowseMenu[2] && findFragmentByPosition(2) != null
                            && findFragmentByPosition(2) instanceof Tab3Container
                            && ((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse") != null
                            && ((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse").isVisible()) {
                        ((WordListBrowseFragment)((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse")).deselectAll();

                        //Hide the action icons
                        showMenuMyListBrowse(false, 2);
                    } else if(tabsShowingBrowseMenu[1] && findFragmentByPosition(1) != null
                            && findFragmentByPosition(1) instanceof Tab2Container
                            && ((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse") != null
                            && ((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse").isVisible()) {
                        ((SavedTweetsBrowseFragment)((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse")).deselectAll();
                        //Hide the action icons
                        showMenuMyListBrowse(false, 1);
                    }

                } catch (NullPointerException e) {
                    Log.e(TAG,"TabContainer->BrowseFragment deselectALL Nullpointer: " + e.toString());
                } catch (Exception e) {
                    Log.e(TAG,"TabContainer->BrowseFragment  deselectALL error: " + e.toString());
                }


                break;
            case R.id.action_copy:

                try {
                    if(tabsShowingBrowseMenu[2] && findFragmentByPosition(2) != null
                            && findFragmentByPosition(2) instanceof Tab3Container
                            && ((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse") != null
                            && ((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse").isVisible()) {
                        ((WordListBrowseFragment)((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse")).showCopyMyListDialog();
                    } else if(tabsShowingBrowseMenu[1] && findFragmentByPosition(1) != null
                            && findFragmentByPosition(1) instanceof Tab2Container
                            && ((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse") != null
                            && ((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse").isVisible()) {
                        ((SavedTweetsBrowseFragment)((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse")).showCopyTweetsDialog();
                    }

                } catch (NullPointerException e) {
                    Log.e(TAG,"TabContainer->BrowseFragment copy Nullpointer: " + e.toString());
                } catch (Exception e) {
                    Log.e(TAG,"TabContainer->BrowseFragment  copy error: " + e.toString());
                }

                break;
            case R.id.action_delete:
                try {
                    if(tabsShowingBrowseMenu[2] && findFragmentByPosition(2) != null
                            && findFragmentByPosition(2) instanceof Tab3Container
                            && ((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse") != null
                            && ((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse").isVisible()) {
                        ((WordListBrowseFragment)((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse")).removeKanjiFromList();

                        //Hide the action icons
                        showMenuMyListBrowse(false, 2);

                    } else if(tabsShowingBrowseMenu[1] && findFragmentByPosition(1) != null
                            && findFragmentByPosition(1) instanceof Tab2Container
                            && ((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse") != null
                            && ((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse").isVisible()) {
                        ((SavedTweetsBrowseFragment)((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse")).removeTweetFromList();

                        //Hide the action icons
                        showMenuMyListBrowse(false, 1);
                    }

                } catch (NullPointerException e) {
                    Log.e(TAG,"TabContainer->BrowseFragment delete Nullpointer: " + e.toString());
                } catch (Exception e) {
                    Log.e(TAG,"TabContainer->BrowseFragment  delete error: " + e.toString());
                }


                break;
            case R.id.action_selectall:


                try {
                    if(tabsShowingBrowseMenu[2] && findFragmentByPosition(2) != null
                            && findFragmentByPosition(2) instanceof Tab3Container
                            && ((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse") != null
                            && ((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse").isVisible()) {
                        ((WordListBrowseFragment)((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse")).selectAll();
                    } else if(tabsShowingBrowseMenu[1] && findFragmentByPosition(1) != null
                            && findFragmentByPosition(1) instanceof Tab2Container
                            && ((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse") != null
                            && ((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse").isVisible()) {
                        ((SavedTweetsBrowseFragment)((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse")).selectAll();
                    }

                } catch (NullPointerException e) {
                    Log.e(TAG,"TabContainer->BrowseFragment selectALL Nullpointer: " + e.toString());
                } catch (Exception e) {
                    Log.e(TAG,"TabContainer->BrowseFragment  selectALL error: " + e.toString());
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * Shows FollowUserDialogFragment, where user can input a new twitter handle to follow
     * Called from the fabAddUser button click
     */
    public void showAddUserDialog(){

            if(getFragmentManager().findFragmentByTag("dialogAdd") == null || !getFragmentManager().findFragmentByTag("dialogAdd").isAdded()) {
                AddUserDialog.newInstance().show(getSupportFragmentManager(),"dialogAdd");
        }
    }


    public void showAddUserCheckDialog(UserInfo userInfo){

        if(getFragmentManager().findFragmentByTag("dialogAddCheck") == null || !getFragmentManager().findFragmentByTag("dialogAddCheck").isAdded()) {
            AddUserCheckDialog.newInstance(userInfo).show(getSupportFragmentManager(),"dialogAddCheck");
        }
    }

    /**
     * Recieves input text from add user dialog
     * Checks if the user already exists in database
     * If not, inputs user into db and updates mainFragment recycler
     * @param inputText
     */
    @Override
    public void onAddUserDialogPositiveClick(String inputText) {

        /** Check to DB to see if the new feed is a duplicate*/
        if(InternalDB.getUserInterfaceInstance(getBaseContext()).duplicateUser(inputText.trim())) {
            Toast.makeText(this, "UserInfo already exists", Toast.LENGTH_SHORT).show();
        } else if (!isOnline()) {

            if(!InternalDB.getUserInterfaceInstance(getBaseContext()).saveUserWithoutData(inputText.trim())) {
                Toast.makeText(getBaseContext(), "Unable to save " + inputText.trim(), Toast.LENGTH_SHORT).show();
            } else {
                try {
                    ((Tab1Container) findFragmentByPosition(0)).updateUserListFragment();
                } catch (NullPointerException e) {
                    Log.e(TAG,"nullpointer error onRemoveUserDialogPOsitiveclick " + e.toString());
                } catch (Exception e) {
                    Log.e(TAG,"generic exception error onRemoveUserDialogPOsitiveclick " + e.toString());
                }
            }

//            Toast.makeText(getBaseContext(), "Unable to access internet", Toast.LENGTH_SHORT).show();
            } else {
            getInitialUserInfoForAddUserCheck(inputText.trim());
        }

    }

    /**
     * Shows remove UserInfo Dialog
     * @param userInfo UserInfo to "unfollow" (i.e. remove from database)
     */
    public void showRemoveUserDialog(UserInfo userInfo) {
        if(getFragmentManager().findFragmentByTag("dialogRemove") == null || !getFragmentManager().findFragmentByTag("dialogRemove").isAdded()) {
            RemoveUserDialog.newInstance(userInfo).show(getSupportFragmentManager(),"dialogRemove");
        }
    }

    /**
     * Removes a users screenName from the database and updates recyclerview in main fragment.
     * Is called from {@link RemoveUserDialog} via the {@link DialogInteractionListener}
     * @param userId userId to remove from database
     *
     */
    @Override
    public void onRemoveUserDialogPositiveClick(String userId) {

        if (InternalDB.getUserInterfaceInstance(getBaseContext()).deleteUser(userId) ) {

            // Locate Tab1Continer and update the UserListInfo adapter to reflect removed item
//            if(findFragmentByPosition(0) != null && findFragmentByPosition(0) instanceof Tab1Container) {
                try {
                    ((Tab1Container) findFragmentByPosition(0)).updateUserListFragment();
                } catch (NullPointerException e) {
                    Log.e(TAG,"nullpointer error onRemoveUserDialogPOsitiveclick " + e.getCause());
                } catch (Exception e) {
                    Log.e(TAG,"generic exception error onRemoveUserDialogPOsitiveclick " + e.getCause());
                }
//            }
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
     * @param screenName name of user whose feed will be pulled from api
     */
    public void getInitialUserInfoForAddUserCheck(final String screenName) {

            String token = getResources().getString(R.string.access_token);
            String tokenSecret = getResources().getString(R.string.access_token_secret);


        userInfoSubscription = TwitterUserClient.getInstance(token,tokenSecret)
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
                                showAddUserCheckDialog(userInfoInstance);
                            } else {
                                Toast.makeText(MainActivity.this, "Unable to add user " + screenName, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override public void onError(Throwable e) {
                            e.printStackTrace();
                            if(debug){Log.d(TAG, "In onError()");}
                            Toast.makeText(getBaseContext(), "Unable to connect to Twitter API", Toast.LENGTH_SHORT).show();

                            /* If process is unable to get userInfo, save the screenName only as a placeholder to be accessed later */
                            if(!InternalDB.getUserInterfaceInstance(getBaseContext()).duplicateUser(screenName)) {
                                InternalDB.getUserInterfaceInstance(getBaseContext()).saveUserWithoutData(screenName);
                            }
                            Log.d(TAG,"ERROR CAUSE: " + e.getCause());
                        }

                        @Override public void onNext(UserInfo userInfo) {
                            if(debug) {
                                Log.d(TAG, "In onNext()");
                                Log.d(TAG, "userInfo: " + userInfo.getUserId() + ", " + userInfo.getDescription());
                            }

                            //TODO replace this?
                            if(userInfoInstance == null) {
                                userInfoInstance = userInfo;
                            }


                        }
                    });

    }


    public void saveAndUpdateUserInfoList(UserInfo userInfoInstance) {

        if(InternalDB.getUserInterfaceInstance(getBaseContext()).duplicateUser(userInfoInstance.getScreenName())) {
            Toast.makeText(this, "User is already saved", Toast.LENGTH_SHORT).show();
        } else  if(InternalDB.getUserInterfaceInstance(getBaseContext()).saveUser(userInfoInstance)) {
            try{

                downloadUserIcon(userInfoInstance.getProfileImageUrlBig(),userInfoInstance.getScreenName());

            } catch (Exception e) {
                Log.e(TAG,"Image download failed: " + e.toString());
            }

            // Locate Tab1Continer and update the UserListInfo adapter to reflect removed item
            if(findFragmentByPosition(0) != null && findFragmentByPosition(0) instanceof Tab1Container) {
                ((Tab1Container) findFragmentByPosition(0)).updateUserListFragment();
            }
        }
    }

    /**
     * Checks whether directory for saving twitter user icons already exist. If not, it creates the directory
     * @param title title of image icon
     * @return file for image icon (to then be saved)
     */
    public File checkForImagePath(String title) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("JukuTweetUserIcons", Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }
        return new File(directory, title + ".png");
    }

    //TODO keep checking images if they are not initially downloaded?
    /**
     * Takes the url of an icon image from a UserInfo object, downloads the image with picasso
     * and saves it to a file
     * @param imageUrl Url of icon image
     * @param screenName user screenname which will become the file name of the icon
     */
    public void downloadUserIcon(String imageUrl, final String screenName) {

        Picasso.with(getBaseContext()).load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            File file = checkForImagePath(screenName);
                            if(!file.exists()) {
                                FileOutputStream ostream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                                ostream.flush();
                                ostream.close();

                                //TODO == clearer way of saving this image to a file...
                                Uri uri = Uri.fromFile(file);
                                InternalDB.getUserInterfaceInstance(getBaseContext()).addMediaURItoDB(uri.toString(),screenName);
                            }

                        } catch (IOException e) {
                            Log.e("IOException", e.getLocalizedMessage());
                        }
                    }
                }).start();

            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }


    /**
     * If the top bucket is visible after popping the fragment in the backstack, use {@link #updateTabs(String[])} to
     * include the mylist bucket and show the main page title strip items. Basically reset everything
     */
    @Override
    public void onBackPressed() {
        boolean isPopFragment = false;

        showMenuMyListBrowse(false,mViewPager.getCurrentItem());
        showProgressBar(false);
        /* If the user is in the 2 tab (timeline,saved tweets) section, pop tab container 1 AND tab container 2,
         * as well as changing tabs to the main menu 3 container setting  */
        if(mAdapterTitles.length == 2 && mAdapterTitles[0].equals("Timeline")) {
            isPopFragment = (((BaseContainerFragment)findFragmentByPosition(0)).popFragment()  && ((BaseContainerFragment)findFragmentByPosition(1)).popFragment());
            showActionBarBackButton(false,getString(R.string.app_name));
            updateTabs(new String[]{"Users","Saved Tweets","Word Lists","Search"});
        } else {
            isPopFragment = ((BaseContainerFragment)findFragmentByPosition(mViewPager.getCurrentItem())).popFragment();
            try {
                if(isTopShowing(mViewPager.getCurrentItem())) {
                    showActionBarBackButton(false,getString(R.string.app_name));
                    updateTabs(new String[]{"Users","Saved Tweets","Word Lists","Search"});
                    showMenuMyListBrowse(false,mViewPager.getCurrentItem());
                    switch (mViewPager.getCurrentItem()) {
                        case 0:
                            showFab(true,"addUser");
                            break;
                        case 1:
                            showFab(true,"addTweetList");
                            break;
                        case 2:
                            showFab(true,"addMyList");
                            break;
                        default:
                            showFab(false);
                            break;
                    }
                }
            } catch (NullPointerException e) {
                Log.e(TAG,"OnBackPressed child entrycount null : " + e);
            }

        }
        if(fragmentWasChanged) {
            mSectionsPagerAdapter.notifyDataSetChanged();
            fragmentWasChanged = false;
        }



        if (!isPopFragment) {
            finish();
        }
    }



    public Fragment findFragmentByPosition(int position) {
        return getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + mViewPager.getId() + ":"
                        + mSectionsPagerAdapter.getItemId(position));
    }

    /**
     * Checks whether device is online. Used when pulling user information & user timeline data
     * @return bool true if device can access a network
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    /**
     * Shows or hides the actionbar back arrow
     * @param showBack bool true to show the button
     * @param title title to show next to button
     */
    public void showActionBarBackButton(Boolean showBack, CharSequence title) {

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(showBack);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * Shows dialog where user can create a new list (either word list or tweet list)
     * @param listType string designating the list to be created as a Word List (aka "MyList")  or Tweet List
     */
    public void showAddMyListDialog(String listType){
        if(getFragmentManager().findFragmentByTag("dialogAddMyList") == null || !getFragmentManager().findFragmentByTag("dialogAddMyList").isAdded()) {
            AddOrRenameMyListDialog.newInstance(listType).show(getSupportFragmentManager(),"dialogAddMyList");
        }

    }

    /**
     * Shows dialog where user can rename one of the user-created lists (either word list or tweet list)
     * @param listType string designating the list to be renamed as a Word List (aka "MyList") or Tweet List
     * @param oldListName current name of the list that will be renamed
     */
    public void showRenameMyListDialog(String listType, String oldListName){
        if(getFragmentManager().findFragmentByTag("dialogAddMyList") == null || !getFragmentManager().findFragmentByTag("dialogAddMyList").isAdded()) {
            AddOrRenameMyListDialog.newInstance(listType,oldListName).show(getSupportFragmentManager(),"dialogAddMyList");
        }
    }

    /**
     * Shows dialog where user can edit (rename, clear, or delete) a user-created list, or clear a system list
     * @param listType string designating the list to be edited as a Word List (aka "MyList") or Tweet List
     * @param currentListName current name of the list that will be renamed
     * @param isStarFavorite bool designating the list as a system favorites list (true) or user list (false). If it is a starfavorite list
     *                       the final "are you sure" dialog will be simpler than the one for a user-created list
     */
    public void showEditMyListDialog(String listType, String currentListName, Boolean isStarFavorite){
        if(getFragmentManager().findFragmentByTag("dialogEditMyList") != null && !getFragmentManager().findFragmentByTag("dialogEditMyList").isAdded()) {
            EditMyListDialog.newInstance(listType,currentListName, isStarFavorite).show(getSupportFragmentManager(),"dialogEditMyList");
        }
    }

    /**
     * Recieves input text from add user dialog
     * Checks if that user already exists in database
     * If not, inputs user into db and updates mainFragment recycler
     * @param listType designates whether the list being added is a Word list, or a Tweet list
     * @param listName name of the list to add
     */
    @Override
    public void onAddMyListDialogPositiveClick(String listType, String listName) {
        if(listType.equals("MyList")) {

            if(InternalDB.getWordInterfaceInstance(getBaseContext()).saveWordList(listName)) {
            /* Locate Tab2Continer and update the MyList adapter to reflect removed item */
                if(findFragmentByPosition(2) != null && findFragmentByPosition(2) instanceof Tab3Container) {
                    ((Tab3Container) findFragmentByPosition(2)).updateMyListFragment();
                }
            }
        } else if(listType.equals("TweetList")) {

            if(InternalDB.getTweetInterfaceInstance(getBaseContext()).saveTweetList(listName)) {
            /* Locate Tab2Continer and update the MyList adapter to reflect removed item */
                if(findFragmentByPosition(1) != null && findFragmentByPosition(1) instanceof Tab2Container) {
                    ((Tab2Container) findFragmentByPosition(1)).updateTweetListFragment();
                }
            }
        }
    };


    /**
     * Action that occurs when the user presses OK on the edit my list dialog. They can either clear a list of its
     * contents, rename it or delete it entirely. The user can do this for word lists (AKA "mylists") or for Tweet Lists
     * @param listType designates whether the list being edited is a Word list, or a Tweet list
     * @param selectedItem integer representing the row the user clicked 1-clear, 2-rename,3-delete
     * @param listName name of list that user is editing
     * @param isStarFavorite bool designating the list as a system favorites list (true) or user list (false). If it is a starfavorite list
     *                       the final "are you sure" dialog will be simpler than the one for a user-created list
     */
    @Override
    public void onEditMyListDialogPositiveClick(String listType, int selectedItem, String listName, boolean isStarFavorite) {

        switch (selectedItem){
            //Clear list
            case 1:
                deleteOrClearDialogFinal(listType, false,listName,isStarFavorite);
                break;
            //Rename list
            case 2:
                showRenameMyListDialog(listType, listName);
                break;
            //Remove list
            case 3:
                deleteOrClearDialogFinal(listType, true,listName,isStarFavorite);
                break;
        }
    }


    /**
     * Action that occurs when the user presses OK to rename a list (which occurs in the {@link AddOrRenameMyListDialog}. They can either clear a list of its
     * contents, rename it or delete it entirely. The user can do this for word lists (AKA "mylists") or for Tweet Lists
     * @param listType designates whether the list being edited is a Word list, or a Tweet list
     * @param oldListName current name of the list that will be renamed
     * @param listName new name to name the list
     */
    @Override
    public void onRenameMyListDialogPositiveClick(String listType,String oldListName, String listName) {
        if(listType.equals("MyList")) {
            if(InternalDB.getWordInterfaceInstance(getBaseContext()).renameWordList(oldListName,listName)) {
                if(findFragmentByPosition(2) != null && findFragmentByPosition(2) instanceof Tab3Container) {
                    ((Tab3Container) findFragmentByPosition(2)).updateMyListFragment();
                }
            } else {
                Toast.makeText(this, "Unable to rename list", Toast.LENGTH_SHORT).show();
            }
        } else if(listType.equals("TweetList")) {
            if(InternalDB.getTweetInterfaceInstance(getBaseContext()).renameTweetList(oldListName,listName)) {
                if(findFragmentByPosition(1) != null && findFragmentByPosition(1) instanceof Tab2Container) {
                    ((Tab2Container) findFragmentByPosition(1)).updateTweetListFragment();
                }
            } else {
                Toast.makeText(this, "Unable to rename list", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * Action that occurs when the user presses OK to delete or clear a list (which occurs in the {@link #onEditMyListDialogPositiveClick(String, int, String, boolean)}.
     * @param listType designates whether the list being edited is a Word list, or a Tweet list
     * @param delete bool true to delete the list, false to clear the list of its contents (but keep the list)
     * @param name listname
     * @param isStarFavorite bool designating the list as a system favorites list (true) or user list (false). If it is a starfavorite list
     *                       the final "are you sure" dialog will be simpler than the one for a user-created list
     **/
    public void deleteOrClearDialogFinal(final String listType, final Boolean delete,final String name, final boolean isStarFavorite) {

        final  AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView text = new TextView(this);
        if(delete) {
            text.setText(getString(R.string.removelistwarning,name));
        } else if(isStarFavorite){
            text.setText(getString(R.string.areyousure));
        } else {
            text.setText(getString(R.string.clearlistwarning,name));
        }
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        text.setGravity(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(text);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(listType.equals("MyList")) {
                    if(delete) {
                        InternalDB.getWordInterfaceInstance(getBaseContext()).deleteWordList(name);
                    } else {
                        InternalDB.getWordInterfaceInstance(getBaseContext()).clearWordList(name,isStarFavorite);
                    }

                    /* Locate Tab2Continer and update the MyList adapter to reflect removed item */
                    if(findFragmentByPosition(2) != null && findFragmentByPosition(2) instanceof Tab3Container) {
                        ((Tab3Container) findFragmentByPosition(2)).updateMyListFragment();
                    }

                } else if(listType.equals("TweetList")) {
                    if(delete) {
                        InternalDB.getTweetInterfaceInstance(getBaseContext()).deleteTweetList(name);
                    } else {
                        InternalDB.getTweetInterfaceInstance(getBaseContext()).clearTweetList(name,isStarFavorite);
                    }

                    /* Locate Tab1Continer and update the TweetList adapter to reflect removed item */
                    if(findFragmentByPosition(1) != null && findFragmentByPosition(1) instanceof Tab2Container) {
                        ((Tab2Container) findFragmentByPosition(1)).updateTweetListFragment();
                    }
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setView(layout);
        dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);

    }

    /**
     * Checks whether the current visible fragment for a given position of the fragmentpager adapter
     * is the top fragment in the stack for that bucket
     * @param position position of the viewpager adapter
     * @return bool true if the top fragment is showing, false if not
     */
    public boolean isTopShowing(int position) {

        switch(position) {
            case 0:
                try {
                    return ((Tab1Container)findFragmentByPosition(0)).isTopFragmentShowing();
                } catch(NullPointerException e) {
                    return false;
                }
            case 1:
                try {
                    return ((Tab2Container)findFragmentByPosition(1)).isTopFragmentShowing();
                } catch(NullPointerException e) {
                    return false;
                }
            case 2:
                try {
                    return  ((Tab3Container)findFragmentByPosition(2)).isTopFragmentShowing();

                } catch(NullPointerException e) {
                    return false;
                }
            default:
                break;
        }
        return true;
    }

    /**
     * Shows or hides the floating action button, and changes the image resource based on the current
     * fragment showing
     * @param show bool true to show, false to hide
     * @param type type of fragment (which dictates the type of image resource)
     */
    public void showFab(boolean show, String type) {
        try {
            if(show && type.equals("addUser")) {
                fab.setVisibility(View.VISIBLE);
                fab.setImageResource(R.drawable.ic_add_white_24dp);
            } else if(show && (type.equals("addMyList") || type.equals("addTweetList"))) {
                fab.setVisibility(View.VISIBLE);
                fab.setImageResource(R.drawable.ic_star_black);
                fab.setColorFilter(ContextCompat.getColor(getBaseContext(), android.R.color.white));
            } else {
                fab.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            Log.e(TAG,"FAB IS NULL: "  + e);
        }

    }

    /**
     * Shows or hides the floating action button
     * @param show bool true to show, false to hide
     */
    public void showFab(boolean show) {
        try {
            if(show) {
                fab.setVisibility(View.VISIBLE);
            } else {
                fab.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            Log.e(TAG,"FAB IS NULL: "  + e);
        }

    }

    /**
     * Traffic control method which takes callback from fragment and passes it to
     * the SectionsPagerAdapter, which will update the number of available tab buckets and
     * titles of those buckets
     * @param updatedTabs String array of tab titles. The number of tab titles in the array dictates
     *                    the number of tab buckets that will be available. So when the user drills down
     *                    from {@link com.jukuproject.jukutweet.Fragments.UserListFragment} to {@link com.jukuproject.jukutweet.Fragments.UserTimeLineFragment}
     *                    the number of tabs will decrease from ("User","Saved Tweets","Word List",,"Search") to ("Timeline","SavedTweets")
     */
    public void updateTabs(String[] updatedTabs) {

        if(mSectionsPagerAdapter != null) {
            mAdapterTitles = updatedTabs;
            mSectionsPagerAdapter.updateTabs(mAdapterTitles);
        }
    }

    /**
     * When user clicks on a timeline for a twitter user in the {@link com.jukuproject.jukutweet.Fragments.UserListFragment}
     * the  {@link SavedTweetsListFragment} in the  tab2 bucket should switch from showing ALL saved tweet lists, to a single list of
     * saved tweets for that specific user. The method replaces the savedtweets all fragment with a new one for the specific user
      * @param userInfo Userinfo of twitter user whose timeline is being shown, and whose saved tweets will be displayed in the saved tweets fragment
     */
    public void showSavedTweetsTabForIndividualUser(UserInfo userInfo){

        try {
            SavedTweetsListFragment savedTweetsListFragment = SavedTweetsListFragment.newInstance(userInfo);
            ((BaseContainerFragment)findFragmentByPosition(1)).replaceFragment(savedTweetsListFragment,true,"savedTweetsAllFragmentIndividual");
        } catch (Exception e) {
            Log.e("TEST","showSavedTweetsTabForIndividualUser failed");
        }

    }

    //Traffic control from CopyDialog to BrowseItemsFragment
    public void saveAndUpdateMyLists(String kanjiIdString, ArrayList<MyListEntry> listsToCopyTo, boolean move, MyListEntry currentList) {
        if(findFragmentByPosition(2) != null && findFragmentByPosition(2) instanceof Tab3Container) {
            try {
                ((WordListBrowseFragment)((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse")).saveAndUpdateMyLists(kanjiIdString,listsToCopyTo,move,currentList);

            } catch (NullPointerException e) {
                Log.e(TAG,"Tab3Container->WordListBrowseFragment saveAndUpdateMyLists Nullpointer: " + e.toString());
            } catch (Exception e) {
                Log.e(TAG,"Tab3Container->WordListBrowseFragment saveAndUpdateMyLists error: " + e.toString());
            }

        }
    }

    //Traffic control from CopyDialog to SavedTweetsBrowseFragment
    public void saveAndUpdateTweetLists(String tweetIds, ArrayList<MyListEntry> listsToCopyTo, boolean move, MyListEntry currentList) {
        if(findFragmentByPosition(1) != null && findFragmentByPosition(1) instanceof Tab2Container) {
            try {
                ((SavedTweetsBrowseFragment)((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse")).saveAndUpdateTweets(tweetIds,listsToCopyTo,move,currentList);
            } catch (NullPointerException e) {
                Log.e(TAG,"Tab2Container->SavedTweetsBrowseFragment saveAndUpdateMyLists Nullpointer: " + e.toString());
            } catch (Exception e) {
                Log.e(TAG,"Tab2Container->SavedTweetsBrowseFragment saveAndUpdateMyLists error: " + e.toString());
            }

        }
    }

    public void showFlashCardFragment(int tabNumber
            , MyListEntry listEntry
            , String frontValue
            , String backValue
            ,String selectedColorString) {


        //Pull data for flashcard fragment
        if(tabNumber == 1) {
            //Its a mylist fragment
            ArrayList<WordEntry> dataset = InternalDB.getTweetInterfaceInstance(getBaseContext())
                    .getWordsFromATweetList(listEntry
                            ,SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            ,selectedColorString
                            ,null
                            ,null);

            if(findFragmentByPosition(tabNumber) != null
                    && findFragmentByPosition(tabNumber) instanceof Tab2Container) {

                FlashCardsFragment flashCardsFragment = FlashCardsFragment.newInstance(dataset,frontValue,backValue);
                ((BaseContainerFragment)findFragmentByPosition(tabNumber)).replaceFragment(flashCardsFragment,true,"flashcards");
            }
        } else if(tabNumber == 2) {
            //Its a mylist fragment
            ArrayList<WordEntry> dataset = InternalDB.getWordInterfaceInstance(getBaseContext())
                    .getWordsFromAWordList(listEntry
                            ,SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            ,selectedColorString
                            ,null
                            ,null);

            if(findFragmentByPosition(tabNumber) != null
                    && findFragmentByPosition(tabNumber) instanceof Tab3Container) {

//                Log.d(TAG,"kanji: " + dataset.get(0).getKanji() + ", furigana: " + dataset.get(0).getFurigana());
                //Initialize Flashcards fragment
                FlashCardsFragment flashCardsFragment = FlashCardsFragment.newInstance(dataset,frontValue,backValue);
                //Replace the current fragment bucket with flashcards
                ((BaseContainerFragment)findFragmentByPosition(tabNumber)).replaceFragment(flashCardsFragment,true,"flashcards");

            }
        }

    }

    public void goToQuizActivityMultipleChoice(int tabNumber
            , MyListEntry listEntry
            ,Integer currentExpandedPosition
            , String quizType
            , String quizSize
            , String quizTimer
            , String selectedColorString) {

        Integer timer = -1;
        if (!quizTimer.equals("None")) {
            timer = Integer.parseInt(quizTimer);
        }

//        ColorThresholds colorThresholds = SharedPrefManager.getInstance(getApplicationContext()).getColorThresholds();

        ArrayList<WordEntry> dataset = new ArrayList<>();
        String dataType = "";
        if (tabNumber == 1) {
            //Its a mylist fragment

            dataset = InternalDB.getTweetInterfaceInstance(getBaseContext())
                    .getWordsFromATweetList(listEntry
                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            , selectedColorString
                            , null
                            , Integer.parseInt(quizSize));
            dataType = "Tweet";

        } else if (tabNumber == 2) {


            //Its a mylist fragment
            dataset = InternalDB.getWordInterfaceInstance(getBaseContext())
                    .getWordsFromAWordList(listEntry
                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            , selectedColorString
                            , null
                            , Integer.parseInt(quizSize));
            dataType = "Word";
        }

            if(dataset.size()>0) {
                double totalweight = assignWordWeightsAndGetTotalWeight(getBaseContext(),dataset);


                Intent intent = new Intent(getBaseContext(), QuizActivity.class);

                intent.putExtra("typeOfQuizThatWasCompleted","Multiple Choice"); //The type of quiz that was chosen inthe menu
                intent.putExtra("quizType",quizType);
                intent.putExtra("tabNumber", 2);
                intent.putExtra("myListEntry",listEntry);
                intent.putExtra("quizSize",quizSize);
                intent.putExtra("colorString",selectedColorString);
                intent.putExtra("timer",quizTimer); //Timer can be "none" so passing it on raw as string
                intent.putExtra("totalweight",totalweight);
                intent.putParcelableArrayListExtra("dataset",dataset);
                intent.putExtra("dataType",dataType);
                intent.putExtra("lastExpandedPosition",currentExpandedPosition);



                Log.d(TAG,"a TIMER: " + quizTimer);
                startActivity(intent);
          } else {
                Toast.makeText(this, "No words found to quiz on", Toast.LENGTH_SHORT).show();
            }



    }

    public void goToQuizActivityFillintheBlanks(int tabNumber
            , MyListEntry myListEntry
            , Integer currentExpandedPosition
            , String quizSize
            , String selectedColorString) {

//        ColorThresholds colorThresholds = SharedPrefManager.getInstance(getApplicationContext()).getColorThresholds();
        ArrayList<Tweet> dataset = new ArrayList<>();
        String dataType = "";

        if (tabNumber == 1) {

            //The request is coming from the saved tweets fragment
            dataset = InternalDB.getQuizInterfaceInstance(getBaseContext())
                    .getFillintheBlanksTweetsForATweetList(myListEntry
                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            , selectedColorString
                            , Integer.parseInt(quizSize));

            dataType = "Tweet";
        } else if (tabNumber == 2) {

            //The request is coming from the saved words fragment
           dataset = InternalDB.getQuizInterfaceInstance(getBaseContext())
                    .getFillintheBlanksTweetsForAWordList(myListEntry
                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            , selectedColorString
                            , Integer.parseInt(quizSize));


            dataType = "Word";

        }

        if(dataset.size()>0) {
            double totalweight = assignTweetWeightsAndGetTotalWeight(dataset);

            Intent intent = new Intent(getBaseContext(), QuizActivity.class);

            intent.putExtra("typeOfQuizThatWasCompleted","Fill in the Blanks"); //The type of quiz that was chosen inthe menu
            intent.putExtra("tabNumber", tabNumber);
            intent.putExtra("myListEntry",myListEntry);
            intent.putExtra("quizSize",quizSize);
            intent.putExtra("colorString",selectedColorString);
            intent.putExtra("totalweight",totalweight);
            Log.d(TAG,"dataset isspinner: " + dataset.get(0).getWordEntries().get(1).getKanji() + ", spinner: "
                    + dataset.get(0).getWordEntries().get(1).isSpinner());
            intent.putParcelableArrayListExtra("dataset",dataset);
            intent.putExtra("dataType",dataType);
            intent.putExtra("lastExpandedPosition",currentExpandedPosition);

            showFab(false);

            startActivity(intent);

        } else {
            Toast.makeText(this, "No tweets found to quiz on", Toast.LENGTH_SHORT).show();
        }

    }

    public static double assignWordWeightsAndGetTotalWeight(Context context, ArrayList<WordEntry> wordEntries) {
        final Double sliderUpperBound = .50;
        final Double sliderLowerBound = .025;
        final int sliderCountMax = 30;
        final double sliderMultipler = SharedPrefManager.getInstance(context).getSliderMultiplier();

        double totalWeight = 0.0d;

        for(WordEntry wordEntry : wordEntries) {
            double percentage =wordEntry.getPercentage();
            double total = (double)wordEntry.getTotal();

            /* The slider multiplier is what affects how rapidly a word diverges from the natural weight of .25.
            * The higher the multiplier, the faster it will diverge with an increased count.*/
            double countMultiplier = (double)total/(double)sliderCountMax*(percentage-(double)sliderUpperBound)*(double)sliderMultipler;

            if(total>100) {
                total = (double)100;
            }

            double a = ((double)sliderUpperBound/(double)2)-(double)sliderUpperBound*(countMultiplier) ;

            double b = sliderLowerBound;
            if(a>=sliderUpperBound) {
                b = sliderUpperBound;
            } else if(a>=sliderLowerBound) {
                b = a;
            }

            wordEntry.setQuizWeight(b);
            totalWeight += b;
            Log.d(TAG,"Setting quiz weight: " + b  + ", new total weight: " + totalWeight);
        }

        return totalWeight;
    }


    public double assignTweetWeightsAndGetTotalWeight(ArrayList<Tweet> tweets) {
        final Double sliderUpperBound = .50;
        final Double sliderLowerBound = .025;
        final int sliderCountMax = 30;
        final double sliderMultipler = SharedPrefManager.getInstance(getBaseContext()).getSliderMultiplier();

        double totalWeight = 0.0d;

        for(Tweet tweet : tweets) {
            double aggregatedTweetCorrect = 0;
            double aggregatedTweetTotal = 0;

            for(WordEntry wordEntry : tweet.getWordEntries()) {
                aggregatedTweetCorrect += (double)wordEntry.getCorrect();
                aggregatedTweetTotal += (double)wordEntry.getTotal();

            }

            double tweetPercentage = aggregatedTweetCorrect/aggregatedTweetTotal;
            /* The slider multiplier is what affects how rapidly a word diverges from the natural weight of .25.
            * The higher the multiplier, the faster it will diverge with an increased count.*/
            double countMultiplier = (double)aggregatedTweetTotal/(double)sliderCountMax*(tweetPercentage-(double)sliderUpperBound)*(double)sliderMultipler;


            double a = ((double)sliderUpperBound/(double)2)-(double)sliderUpperBound*(countMultiplier) ;

            double b = sliderLowerBound;
            if(a>=sliderUpperBound) {
                b = sliderUpperBound;
            } else if(a>=sliderLowerBound) {
                b = a;
            }

            tweet.setQuizWeight(b);
            totalWeight += b;
        }

        return totalWeight;
    }

    //Changes sss
    public void notifyFragmentsChanged() {
        fragmentWasChanged = true;
    }


    public void setPreviousMyListExpanded(int tabNumber,int positionToExpand) {
        if(findFragmentByPosition(tabNumber) != null && findFragmentByPosition(tabNumber) instanceof Tab2Container) {

            try {
                Tab2Container container = ((Tab2Container) findFragmentByPosition(tabNumber));
                ((SavedTweetsListFragment) container.getChildFragmentManager().findFragmentByTag("savedtweetsallfragment")).expandTheListViewAtPosition(positionToExpand);
            } catch (Exception e) {
                Log.e("TEST","setPreviousMyListExpanded savedtweetsallfragment could not set prev expanded");
            }

        } else if(findFragmentByPosition(tabNumber) != null && findFragmentByPosition(tabNumber) instanceof Tab3Container) {
            try {
                Tab3Container container = ((Tab3Container) findFragmentByPosition(tabNumber));
                ((WordListFragment) container.getChildFragmentManager().findFragmentByTag("mylistfragment")).expandTheListViewAtPosition(positionToExpand);
            } catch (Exception e) {
                Log.e("TEST","setPreviousMyListExpanded mylistfragment could not set prev expanded");
            }
        }


    }

    public void showUserDetailFragment(UserInfo userInfo) {
        UserDetailPopupDialog userDetailFragment = UserDetailPopupDialog.newInstance(userInfo);
        userDetailFragment.show(getSupportFragmentManager(),"xxx");
    };

 public void runTwitterSearch(final String query, final String queryOption) {
    if(searchQuerySubscription !=null && !searchQuerySubscription.isUnsubscribed()) {
        searchQuerySubscription.unsubscribe();
    }


     String token = getResources().getString(R.string.access_token);
     String tokenSecret = getResources().getString(R.string.access_token_secret);


     if(queryOption.equals("Tweet")) {


    //If query is in romaji, convert it to a kanji and run the search on that kanji if possible
    String kanjiQuery = query;
    if(isRomaji(query)) {
        ArrayList<WordEntry> conversionResults = actuallyRuntheSearch(query,"Romaji");
        if(conversionResults != null && conversionResults.size()>0) {
            kanjiQuery = conversionResults.get(0).getKanji();
        }

    };




         /* Holds a list of tweets that have been favorited (in any/all lists). Used to check
    * whether or not a tweet needs to have favorites assigned to it. This exists
    * so that we dont' have to make a sql query for each Tweet that gets returned from
    * the api lookup */




     searchQuerySubscription = TwitterUserClient.getInstance(token,tokenSecret)
            .getSearchTweets(kanjiQuery,"ja",25)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<SearchTweetsContainer>() {

                ArrayList<Tweet> mDataSet = new ArrayList<>();

                @Override public void onCompleted() {
                    if(BuildConfig.DEBUG){Log.d(TAG, "runTwitterSearch In onCompleted()");}

                    try {
                        //Compile a string concatenation of all user ids in the set of tweets
                        StringBuilder stringBuilder = new StringBuilder();
                        for(Tweet tweet : mDataSet) {
                            if(stringBuilder.length()>0) {
                                stringBuilder.append(",");

                            }



                            stringBuilder.append(tweet.getUser().getUserId());
                        }

                        //Pull a list of favorited tweets for those user ids (if any exist)
                        if(stringBuilder.length()>0) {



                            HashMap<String,ItemFavorites> tweetIdStringsInFavorites = InternalDB.getTweetInterfaceInstance(getBaseContext()).getStarFavoriteDataForAUsersTweets(stringBuilder.toString());

                                for(Tweet tweet : mDataSet) {
                                    //Attach colorfavorites to tweet, if they exists in db
                                    if(tweet.getIdString()!=null && tweetIdStringsInFavorites.keySet().contains(tweet.getIdString())) {
                                        tweet.setItemFavorites(tweetIdStringsInFavorites.get(tweet.getIdString()));
                                    } else {
                                        tweet.setItemFavorites(new ItemFavorites());
                                    }
                                }
                        }

                    } catch (Exception e){
                        Log.e(TAG,"Adding favorite information to tweets exception: " + e.toString());
                    }


                    if(findFragmentByPosition(3) != null) {
                        try {

                            ((SearchFragment) ((Tab4Container) findFragmentByPosition(3)).getChildFragmentManager().findFragmentByTag("searchFragment")).recieveTwitterSearchResults(mDataSet);

                        } catch (NullPointerException e) {
                            Log.e(TAG,"runTwitterSearch onsuccess error updating SearchFragment, null: " + e.toString());
                        } catch (Exception e) {
                            Log.e(TAG,"runTwitterSearch onsuccess error updating SearchFragment, generic exception: " + e.toString());

                        }
                    }

                }

                @Override public void onError(Throwable e) {
                    e.printStackTrace();
                    if(BuildConfig.DEBUG){Log.d(TAG, "runTwitterSearch In onError()");}
                }

                @Override public void onNext(SearchTweetsContainer results) {
                    if(BuildConfig.DEBUG) {
                        Log.d(TAG, "runTwitterSearch In onNext()");
//                        Log.d(TAG," tweet search results SIZE: " + results.size());
                    }
                    if(mDataSet.size() == 0) {
                        try{
                            mDataSet.addAll(results.getTweets());
                        } catch (Exception e){
                            Log.e(TAG,"exception in runTwitterSearch get tweets");
                        }
//
                    }
//
//                    if(mDataSet.size() == 0) {
//
//                        for(Tweet tweet : results.getTweets()) {
//
//
////                            Log.d(TAG,"timeline thing: " + tweet.getIdString());
//                            //Attach colorfavorites to tweet, if they exists in db
//                            if(tweet.getIdString()!=null && tweetIdStringsInFavorites.keySet().contains(tweet.getIdString())) {
//                                tweet.setItemFavorites(tweetIdStringsInFavorites.get(tweet.getIdString()));
//                            } else {
//                                tweet.setItemFavorites(new ItemFavorites());
//                            }
//
//                            mDataSet.add(tweet);
//                        }
//                    }

                }
            });
     } else if(queryOption.equals("User")){



             //TODO make the number of twitter responses an option! not just 10
             TwitterUserClient.getInstance(token,tokenSecret)
                     .getSearchUsers(query,10)
                     .subscribeOn(Schedulers.io())
                     .observeOn(AndroidSchedulers.mainThread())
                     .subscribe(new Observer<UserFollowersListContainer>() {
                         ArrayList<UserInfo> mDataSet = new ArrayList<>();
                         @Override public void onCompleted() {

                             if(findFragmentByPosition(3) != null) {
                                 try {

                                     ((SearchFragment) ((Tab4Container) findFragmentByPosition(3)).getChildFragmentManager().findFragmentByTag("searchFragment")).recieveTwitterUserSearchResults(mDataSet);

                                 } catch (NullPointerException e) {
                                     Log.e(TAG,"runTwitterSearch onsuccess error updating SearchFragment, null: " + e.toString());
                                 } catch (Exception e) {
                                     Log.e(TAG,"runTwitterSearch onsuccess error updating SearchFragment, generic exception: " + e.toString());

                                 }
                             }




                         }

                         @Override public void onError(Throwable e) {
                             e.printStackTrace();
                             if(BuildConfig.DEBUG){Log.d(TAG, "runTwitterSearch Users In onError()");}
                         }

                         @Override public void onNext(UserFollowersListContainer followers) {
                             if(BuildConfig.DEBUG) {
                                 Log.d(TAG, "In onNext()");
                                 Log.d(TAG,"FOLLOWERS SIZE: " + followers.getUsers().size());
                             }

                                 try {
                                     mDataSet.addAll(followers.getUsers());
                                 } catch (Exception e) {
                                     Log.e(TAG,"Exception trying to pull follower data... "  + e.toString());
                                 }

                         }
                     });




     }

}



    public void runDictionarySearch(final String query, final String queryOption) {

        if(searchQuerySubscription !=null && !searchQuerySubscription.isUnsubscribed()) {
            searchQuerySubscription.unsubscribe();
        }

//        actuallyRuntheSearch(query.trim(),isRomaji);

        searchQuerySubscription = Observable.fromCallable(new Callable<ArrayList<WordEntry>>() {
            @Override
            public ArrayList<WordEntry> call() throws Exception {
                return actuallyRuntheSearch(query.trim(),queryOption);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ArrayList<WordEntry>>() {
                    ArrayList<WordEntry> searchResults = new ArrayList<WordEntry>();
                    @Override
                    public void onCompleted() {
                        if(findFragmentByPosition(3) != null) {
                            try {

                                ((SearchFragment) ((Tab4Container) findFragmentByPosition(3)).getChildFragmentManager().findFragmentByTag("searchFragment")).recieveDictionarySearchResults(searchResults);

                            } catch (NullPointerException e) {
                                Log.e(TAG,"searchquerysubscription onsuccess error updating SearchFragment, null: " + e.toString());
                            } catch (Exception e) {
                                Log.e(TAG,"searchquerysubscription onsuccess error updating SearchFragment, generic exception: " + e.toString());

                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG,"IN ON ERROR OF searchQuerySubscription in MAIN ACTIVITY");

                        try {
                            ((SearchFragment) ((Tab4Container) findFragmentByPosition(3)).getChildFragmentManager().findFragmentByTag("searchFragment")).showProgressBar(false);
                        } catch (Exception ee) {
                            Log.e(TAG,"searchQuerySubscription in MAIN A inerror error hiding progress bar! generic exception: " + e.toString());

                        }
                    }

                    @Override
                    public void onNext(ArrayList<WordEntry> wordEntries) {
                        searchResults = wordEntries;
                    }
                });
//                .subscribe(new SingleSubscriber<ArrayList<WordEntry>>() {
//
//                    @Override
//                    public void onSuccess(ArrayList<WordEntry> searchResults) {
//
//                        //Update the search fragment with the search results
//                        if(findFragmentByPosition(3) != null) {
//                            try {
//
//                                ((SearchFragment) ((Tab4Container) findFragmentByPosition(3)).getChildFragmentManager().findFragmentByTag("searchFragment")).recieveDictionarySearchResults(searchResults);
//
//                            } catch (NullPointerException e) {
//                                Log.e(TAG,"searchquerysubscription onsuccess error updating SearchFragment, null: " + e.toString());
//                            } catch (Exception e) {
//                                Log.e(TAG,"searchquerysubscription onsuccess error updating SearchFragment, generic exception: " + e.toString());
//
//                            }
//                        }
//
//                    }
//
//                    @Override
//                    public void onError(Throwable error) {
//                        Log.e(TAG,"ERROR IN PARSE SENTENCE OBSERVABLE: " + error);
//                        showProgressBar(false);
//
//                    }
//                }).cache();
//
//        queryDictionary =  Observable.from(actuallyRuntheSearch(query,isRomaji))
//                .create(new Observable<ArrayList<WordEntry>>.OnSubscribe(new Action1<ArrayList<WordEntry>>() {
//                      @Override
//                      public void call(ArrayList<WordEntry>) {
//                          actuallyRuntheSearch(query,isRomaji);
//                      }
//                  })).cache();

    }

//    public ArrayList<WordEntry> actuallyRuntheSearch(String query, boolean isRomaji) {
//
//        return new ArrayList<>();
//    }

    public boolean isRomaji(String query) {
        Pattern ps = Pattern.compile("^[a-zA-Z0-9 ]+$");
        Matcher ms = ps.matcher(query.trim());
//        boolean okquery = ms.matches();
        return ms.matches();
    }

    public ArrayList<WordEntry> actuallyRuntheSearch(String query, String queryOption) {

        ArrayList<WordEntry> searchResults = new ArrayList<>();
        String idstopasson;
        StringBuilder FinalHiraganaKatakanaPlaceHolders = new StringBuilder();
            /* If the Query is ROMAJI */
        if(queryOption.equals("Kanji") && isRomaji(query)) {

            StringBuilder FinalHiraganaKatakanaEntries = new StringBuilder();

            ArrayList<String> possibleHiraganaSearchQueries = new ArrayList<>();
            ArrayList<String> possibleKatakanaSearchQueries = new ArrayList<>();


            final HashMap<String,ArrayList<String>> romajiSearchMap = InternalDB.getInstance(getBaseContext()).getWordLists().getRomajiMap();

            /* First convert the query from Romaji --> Furigana and Katakana */

            if(!query.contains(" ") && query.length()>0) {

                int startposition = 0;
                int iterator = 3;
                if(query.length()<iterator){
                    iterator = 2;
                }
                boolean foundone = false;
                FinalHiraganaKatakanaEntries = new StringBuilder();
                FinalHiraganaKatakanaPlaceHolders = new StringBuilder();

                while(query.length()>=(startposition+ iterator) && query.length()-startposition >0) {

                    while(iterator>0 && !foundone) {
                        String querychunk = query.substring(startposition,(startposition+iterator)).toLowerCase();
                        if(debug){Log.d(TAG,"QUERYCHUNK: " + querychunk);}

                    /*If it's a 3 char double like: ppu --> っぷ , throw in the っ character, unless it's a "nn"*/
                        if(iterator==2 && String.valueOf(querychunk.charAt(0)).equals(String.valueOf(querychunk.charAt(1))) && !String.valueOf(querychunk.charAt(0)).equals("n")) {

                            //an even number, meaning its a hiragana
                            StringBuilder matchBuilder = new StringBuilder();
                            ArrayList<String> newPossibleHiraganaSearchQueries = new ArrayList<String>();
                            if(possibleHiraganaSearchQueries.size()>0){
                                for(int ii=0;ii<possibleHiraganaSearchQueries.size();ii++){
                                    matchBuilder.append(possibleHiraganaSearchQueries.get(ii));
                                    matchBuilder.append("っ");
                                    newPossibleHiraganaSearchQueries.add(matchBuilder.toString());
                                    if(debug){Log.d(TAG,"(1)Appending hiragana chunk: " + possibleHiraganaSearchQueries.get(ii) + " + " + matchBuilder.toString());}
                                    matchBuilder.setLength(0);
                                }
                                possibleHiraganaSearchQueries = new ArrayList<String>(newPossibleHiraganaSearchQueries);

                            } else{
                                if(debug){Log.d(TAG,"Adding first hiragana chunk: " + "っ");}
                                possibleHiraganaSearchQueries.add("っ");
                            }

                            //an odd number, meaning its a Katakana
                            StringBuilder matchBuilder2 = new StringBuilder();
                            ArrayList<String> newPossibleKatakanaSearchQueries = new ArrayList<String>();
                            if(possibleKatakanaSearchQueries.size()>0){
                                for(int ii=0;ii<possibleKatakanaSearchQueries.size();ii++){
                                    matchBuilder2.append(possibleKatakanaSearchQueries.get(ii));
                                    matchBuilder2.append("ッ");
                                    newPossibleKatakanaSearchQueries.add(matchBuilder2.toString());
                                    if(debug){Log.d(TAG,"(1)Appending katakana chunk: " + possibleKatakanaSearchQueries.get(ii) + " + " + matchBuilder2.toString());}
                                    matchBuilder2.setLength(0);
                                }
                                possibleKatakanaSearchQueries = new ArrayList<String>(newPossibleKatakanaSearchQueries);

                            } else{
                                if(debug){Log.d(TAG,"Adding first katakana chunk: ッ");}
                                possibleKatakanaSearchQueries.add("ッ");
                            }


                            iterator -= 1;
                            foundone = true;
                        } else {

                            if(romajiSearchMap.containsKey(querychunk)){
                                foundone = true;
                                ArrayList<String> furiganaoptions = romajiSearchMap.get(querychunk);
                                for(int i=0;i<furiganaoptions.size();i++){
                                    String currentFuriganaOption = furiganaoptions.get(i);
                                    if(debug){Log.d(TAG,"Current Furigana Option: " + currentFuriganaOption);}
                                    if((i%2)==0){
                                        //an even number, meaning its a hiragana
                                        StringBuilder matchBuilder = new StringBuilder();
                                        ArrayList<String> newPossibleHiraganaSearchQueries = new ArrayList<String>();
                                        if(possibleHiraganaSearchQueries.size()>0){
                                            for(int ii=0;ii<possibleHiraganaSearchQueries.size();ii++){
                                                matchBuilder.append(possibleHiraganaSearchQueries.get(ii));
                                                matchBuilder.append(currentFuriganaOption);
                                                newPossibleHiraganaSearchQueries.add(matchBuilder.toString());
                                                if(debug){Log.d(TAG,"Appending hiragana chunk: " + possibleHiraganaSearchQueries.get(ii) + " + " + matchBuilder.toString());}
                                                matchBuilder.setLength(0);
                                            }
                                            possibleHiraganaSearchQueries = new ArrayList<String>(newPossibleHiraganaSearchQueries);

                                        } else{
                                            if(debug){Log.d(TAG,"Adding first hiragana chunk: " + currentFuriganaOption);}
                                            possibleHiraganaSearchQueries.add(currentFuriganaOption);
                                        }

                                    } else {
                                        //an odd number, meaning its a Katakana
                                        StringBuilder matchBuilder = new StringBuilder();
                                        ArrayList<String> newPossibleKatakanaSearchQueries = new ArrayList<String>();
                                        if(possibleKatakanaSearchQueries.size()>0){
                                            for(int ii=0;ii<possibleKatakanaSearchQueries.size();ii++){
                                                matchBuilder.append(possibleKatakanaSearchQueries.get(ii));
                                                matchBuilder.append(currentFuriganaOption);
                                                newPossibleKatakanaSearchQueries.add(matchBuilder.toString());
                                                if(debug){Log.d(TAG, "Appending katakana chunk: " + possibleKatakanaSearchQueries.get(ii) + " + " + matchBuilder.toString());}

                                                if (FinalHiraganaKatakanaEntries.length() > 0) {
                                                    FinalHiraganaKatakanaEntries.append(",");
                                                }
                                                FinalHiraganaKatakanaEntries.append(matchBuilder.toString());

                                                if (FinalHiraganaKatakanaPlaceHolders.length() > 0) {
                                                    FinalHiraganaKatakanaPlaceHolders.append(", ");
                                                }
                                                FinalHiraganaKatakanaPlaceHolders.append("?");

                                                matchBuilder.setLength(0);
                                            }
                                            possibleKatakanaSearchQueries = new ArrayList<String>(newPossibleKatakanaSearchQueries);

                                        } else{
                                            if(debug){Log.d(TAG,"Adding first katakana chunk: " + currentFuriganaOption);}
                                            possibleKatakanaSearchQueries.add(currentFuriganaOption);
                                        }

                                    }
                                }

                            } else {
                                if(debug){Log.d(TAG,"iterator subtract (1)");}
                                iterator -= 1;

                            }

                        }

                    }


                    if(!foundone) {
                        if(debug){Log.d(TAG,"nothing found, moving 3 char ahead");}
                        iterator = 3;
                        startposition = (startposition + 3);

                    }else {
                        if(debug){
                            Log.d(TAG,"Chunk added, moving " + iterator + " chars ahead(2)");
                            Log.d(TAG,"old start pos: " + startposition);
                            Log.d(TAG,"new start pos: " + (startposition + iterator));
                        }
                        startposition = (startposition + iterator);
                        iterator = 3;
                        foundone = false;
                    }

                    while(iterator>0 && query.length()-startposition >0 && query.length()<(startposition+iterator)){
                        iterator -=1;
                    }
                    if(debug){Log.d(TAG,"iterator at end of loop: " + iterator);}
                }

                if(debug){

                    if(possibleHiraganaSearchQueries.size()>0 ){
                        for(int a = 0; a<possibleHiraganaSearchQueries.size();a++) {
                            Log.d(TAG,"FINAL possible hiragana query: " + possibleHiraganaSearchQueries.get(a));
                        }
                    }
                    if(possibleKatakanaSearchQueries.size()>0 ){
                        for(int a = 0; a<possibleKatakanaSearchQueries.size();a++) {
                            Log.d(TAG,"FINAL possible katakana query: " + possibleKatakanaSearchQueries.get(a));
                        }

                    }
                }
            }


            /**Adding hiragana entries to the katakana ones, so we can look for hiragana and katakan ones in db all at once baby*/
            if(possibleHiraganaSearchQueries.size()>0) {
                for(int a=0;a<possibleHiraganaSearchQueries.size();a++) {
                    possibleKatakanaSearchQueries.add(possibleHiraganaSearchQueries.get(a));
                    if (FinalHiraganaKatakanaEntries.length() > 0) {
                        FinalHiraganaKatakanaEntries.append(",");
                    }
                    FinalHiraganaKatakanaEntries.append(possibleHiraganaSearchQueries.get(a));

                    if (FinalHiraganaKatakanaPlaceHolders.length() > 0) {
                        FinalHiraganaKatakanaPlaceHolders.append(", ");
                    }
                    FinalHiraganaKatakanaPlaceHolders.append("?");

                }
            }
            idstopasson = InternalDB.getWordInterfaceInstance(getBaseContext()).getWordIdsForRomajiMatches(possibleHiraganaSearchQueries,possibleKatakanaSearchQueries);
        } else if(queryOption.equals("Kanji") && !query.contains(" ")){
            idstopasson = InternalDB.getWordInterfaceInstance(getBaseContext()).getWordIdsForKanjiMatch(query.trim());
        } else {
            idstopasson = InternalDB.getWordInterfaceInstance(getBaseContext()).getWordIdsForDefinitionMatch(query);
        }


        /***
         *
         * If we have a match, pull the edict info for those matching ids
         *
         * **/
        if(idstopasson!=null && idstopasson.length()>0) {

            /* If we have at least on positive match, we pull further edict data from Edict
             * If there isn't, we can skip all this and just iterate through the list showing "item not found"s*/
            if(debug) {Log.d(TAG, "ids to pass on: " + idstopasson);}
            ColorThresholds colorThresholds = SharedPrefManager.getInstance(getBaseContext()).getColorThresholds();
            if(queryOption.equals("Kanji")) {
                searchResults = InternalDB.getWordInterfaceInstance(getBaseContext()).getSearchWordEntriesForRomaji(FinalHiraganaKatakanaPlaceHolders.toString(),idstopasson,colorThresholds);
            } else {
                searchResults = InternalDB.getWordInterfaceInstance(getBaseContext()).getSearchWordEntriesForDefinition(idstopasson,colorThresholds);
            }


        }
        return searchResults;

    }



    public void parseAndSaveTweet(final Tweet tweet) {


        Single.fromCallable(new Callable<ArrayList<WordEntry>>() {
            @Override
            public ArrayList<WordEntry> call() throws Exception {

                final ArrayList<String> spansToExclude = new ArrayList<>();

                if(tweet.getEntities() != null && tweet.getEntities().getUrls() != null) {
                    for(TweetUrl url : tweet.getEntities().getUrls()) {
                        if(url != null) {
                            spansToExclude.add(url.getUrl());
                        }
                    }

                }
//                                            Log.d(TAG,"DB OPEN BEFORE: " + db.isOpen());
                ColorThresholds colorThresholds = SharedPrefManager.getInstance(getBaseContext()).getColorThresholds();
                return TweetParser.getInstance().parseSentence(getBaseContext()
                        ,tweet.getText()
                        ,spansToExclude
                        ,colorThresholds);
            }
        }).subscribeOn(parseTweetScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<ArrayList<WordEntry>>() {

                    @Override
                    public void onSuccess(ArrayList<WordEntry> disectedTweet) {
                        //load the parsed kanji ids into the database
                        InternalDB.getTweetInterfaceInstance(getBaseContext()).saveParsedTweetKanji(disectedTweet,tweet.getIdString());
                        notifyFragmentsChanged();
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG,"ERROR IN PARSE KANJI (for saved tweet) OBSERVABLE: " + error);
                    }
                });
    }


    @Override
    protected void onDestroy() {


        if(searchQuerySubscription!=null) {
            searchQuerySubscription.unsubscribe();
        }
        if(userInfoSubscription!=null) {
            userInfoSubscription.unsubscribe();
        }
        super.onDestroy();
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        if(findFragmentByPosition(0) != null
                && findFragmentByPosition(0) instanceof Tab1Container) {
            getSupportFragmentManager().putFragment(outState, "tab1Container", (Tab1Container)findFragmentByPosition(0));
        }

        if(findFragmentByPosition(1) != null
                && findFragmentByPosition(1) instanceof Tab2Container) {
            getSupportFragmentManager().putFragment(outState, "tab2Container", (Tab2Container)findFragmentByPosition(1));
        }
        if(findFragmentByPosition(2) != null
                && findFragmentByPosition(2) instanceof Tab3Container) {
            getSupportFragmentManager().putFragment(outState, "tab3Container", (Tab3Container)findFragmentByPosition(2));
        }

        if(findFragmentByPosition(3) != null
                && findFragmentByPosition(3) instanceof Tab3Container) {
            getSupportFragmentManager().putFragment(outState, "tab4Container", (Tab4Container)findFragmentByPosition(3));
        }

        outState.putStringArray("adapterTitles", mAdapterTitles);
//        outState.putInt("tabNumber",mSectionsPagerAdapter.);
        outState.putBooleanArray("tabsShowingBrowseMenu",tabsShowingBrowseMenu);
        outState.putBoolean("fragmentWasChanged",fragmentWasChanged);
    }
}
