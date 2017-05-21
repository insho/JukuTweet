package com.jukuproject.jukutweet;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
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
import com.jukuproject.jukutweet.Fragments.FlashCardsFragment;
import com.jukuproject.jukutweet.Fragments.SearchFragment;
import com.jukuproject.jukutweet.Fragments.StatsFragmentProgress;
import com.jukuproject.jukutweet.Fragments.TweetBreakDownFragment;
import com.jukuproject.jukutweet.Fragments.TweetListBrowseFragment;
import com.jukuproject.jukutweet.Fragments.TweetListFragment;
import com.jukuproject.jukutweet.Fragments.TweetListSingleUserFragment;
import com.jukuproject.jukutweet.Fragments.UserPreferenceFragment;
import com.jukuproject.jukutweet.Fragments.WordListBrowseFragment;
import com.jukuproject.jukutweet.Fragments.WordListFragment;
import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
import com.jukuproject.jukutweet.Interfaces.DialogRemoveUserInteractionListener;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.PostQuizFragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.QuizMenuDialogInteractionListener;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.SearchTweetsContainer;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetUrl;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.TabContainers.BaseContainerFragment;
import com.jukuproject.jukutweet.TabContainers.Tab1Container;
import com.jukuproject.jukutweet.TabContainers.Tab2Container;
import com.jukuproject.jukutweet.TabContainers.Tab3Container;
import com.jukuproject.jukutweet.TabContainers.Tab4Container;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
 * Main activity fragment manager. Also performs traffic control for callbacks between fragments and dialogs
 *
 * @see FragmentInteractionListener
 * @see DialogInteractionListener
 * @see QuizMenuDialogInteractionListener
 *
 */
public class MainActivity extends AppCompatActivity implements FragmentInteractionListener
        , DialogInteractionListener
        , QuizMenuDialogInteractionListener
        , DialogRemoveUserInteractionListener
        , PostQuizFragmentInteractionListener
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

    private static final String TAG = "TEST-Main";
    private ViewPager mViewPager;
    private SmoothProgressBar progressbar;
    private FloatingActionButton fab;
    private Menu mMenu;

    /*array keeping track of which tabs (either tab2,tab3, both, or neither)
      have the browsemenu icons showing. defaults to false*/
    private boolean[] tabsShowingBrowseMenu = new boolean[4];
    /*array keeping track of which tabs have the action bar back button showing, and what text is
    displayed next to the bak button. An empty string means no back button showed. */
    private String[] tabsShowingBackButton = new String[4];

    /* Array of titles in pager adapter */
    private String[] mAdapterTitles;
    private Subscription searchQuerySubscription;
    private Subscription userInfoSubscription;

    /* Used to save and reactivate the "searchQuerySubscription" on activity destroyed.
     * the First string is the type of search "Dictionary" or "Twitter". The second is the "query" paramater to start a new subscription.
      * and third is the "queryOption" parameter. */
    private String[] searchSubscriptionCriteria;

    /* Queues up any parse tweet tasks so they complete one by one */
    Scheduler parseTweetScheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    /*Tracks which tweets are getting parsed in the scheduler. This is to avoid
  letting the user keep cycling through tweet favorite star clicks and inadvertently adding
  more identical TweetParser requests to the parseTweetScheduler.*/
  ArrayList<String> currentParsingTweetBackLog;

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


            boolean previouslyStarted = sharedPref.getBoolean(getString(R.string.pref_previously_started), false);
            if(!previouslyStarted) {
                SharedPreferences.Editor edit = sharedPref.edit();
                edit.putBoolean(getString(R.string.pref_previously_started), Boolean.TRUE);
                edit.apply();

                if(!InternalDB.getUserInterfaceInstance(getBaseContext()).duplicateUser(getString(R.string.jukusetup_screenname))){
                    UserInfo userInfo = new UserInfo();
                    userInfo.setUserId(getString(R.string.jukusetup_userid));
                    userInfo.setName(getString(R.string.jukusetup_name));
                    userInfo.setDescription(getString(R.string.jukusetup_description));
                    userInfo.setScreen_name(getString(R.string.jukusetup_screenname));
                    userInfo.setProfile_image_url(getString(R.string.jukusetup_url));
                    InternalDB.getUserInterfaceInstance(getBaseContext()).saveUser(userInfo);
                    InternalDB.getUserInterfaceInstance(getBaseContext()).loadJukuIcon(getBaseContext(),userInfo.getUserId());
                }
            }
        }

        mAdapterTitles = new String[]{"Users","Tweet Lists","Word Lists","Search"};
        // Create the adapter that will return a fragment for each of the primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),mAdapterTitles);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        /* choose a tab to focus on initially. Default to 0. tabNumber is passed
        to quiz activity when a quiz is chosen, and passed back via intent to mainactivity on Back Pressed, and
        this tabNumber is used to focus on the last open tab. */
        if(getIntent()!= null) {
            mViewPager.setCurrentItem(getIntent().getIntExtra("tabNumber",0));
            setPreviousMyListExpanded(getIntent().getIntExtra("tabNumber",0),getIntent().getIntExtra("lastExpandedPosition",0));
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            /**
             * Changes visibility and function of floating action button depending on which bucket is currently visible
             * @param position viewpager position (bucket)
             */
            @Override
            public void onPageSelected(int position) {
                /* The fab has different functions and appearance depending on which tab is visible */
                setUpFabsAndMenus(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        progressbar = (SmoothProgressBar) findViewById(R.id.progressbar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mViewPager != null) {
                   if(mViewPager.getCurrentItem() == 0 && isTopShowing(0)) {
                        showAddUserDialog();
                        showRoomSetUp();
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

            /* Setting up browse menu and action bar back buttons if activity is being recreated */
            if(savedInstanceState!=null) {
            tabsShowingBackButton = savedInstanceState.getStringArray("tabsShowingBackButton");
            tabsShowingBrowseMenu = savedInstanceState.getBooleanArray("tabsShowingBrowseMenu");
            try {
                setUpFabsAndMenus(mViewPager.getCurrentItem());
            } catch (NullPointerException e) {
                Log.e(TAG,"reactivating activity setUpFabsAndMenus Nullpointer");
            }

            if((searchSubscriptionCriteria = savedInstanceState.getStringArray("searchSubscriptionCriteria")) !=null) {
                if(searchSubscriptionCriteria.length==4) {
                    if(searchSubscriptionCriteria[0].equals("Dictionary")) {
                        runDictionarySearch(searchSubscriptionCriteria[1],searchSubscriptionCriteria[2]);
                    } else if(searchSubscriptionCriteria[0].equals("Twitter")) {

                        Long maxId = null;
                        if(searchSubscriptionCriteria[3].length()>0) {
                            maxId = Long.valueOf(searchSubscriptionCriteria[3]);
                        }
                        runTwitterSearch(searchSubscriptionCriteria[1],searchSubscriptionCriteria[2],maxId);
                    }
                }
            }
        }
    }

    private void showRoomSetUp() {
        ContextWrapper cw = new ContextWrapper(getBaseContext());
        File directory = cw.getDir("icons", Context.MODE_PRIVATE);
        File lister = directory.getAbsoluteFile();
        for (String list : lister.list())
        {
            Log.i(TAG,"FILE: " + list);
        }



        SQLiteDatabase db = InternalDB.getInstance(getBaseContext()).getWritableDatabase();
//
//        Cursor c = db.rawQuery(                        "Select distinct ScreenName" +
//                ",IFNULL(Description,'') as [Description]" +
//                ", IFNULL(ProfileImgUrl,'') as [ProfileImgUrl]" +
//                ",UserId" +
//                ", ProfileImgFilePath" +
//                ", UserName " +
//                "From " + InternalDB.Tables.TABLE_USERS + " WHERE UserName = 'JukuProject'" +
//                "",null);
//
//        if(c.getCount()>0) {
//            c.moveToFirst();
//            while (!c.isAfterLast()) {
//                Log.d(TAG,"screenname: "  + c.getString(0));
//                Log.d(TAG,"description: "  + c.getString(1));
//                Log.d(TAG,"profileimgurl: "  + c.getString(2));
//                Log.d(TAG,"userid: "  + c.getString(3));
//                Log.d(TAG,"profilepath: "  + c.getString(4));
//                Log.d(TAG,"username: "  + c.getString(5));
//                c.moveToNext();
//            }
//            c.close();
//        }


        db.execSQL("DELETE FROM JScoreboard");

    db.execSQL("INSERT INTO JScoreboard " +
            "SELECT _id " +
            ",ABS(RANDOM()) % (31- 17) + 17 as [Total] " +
            ",ABS(RANDOM()) % (16- 4) + 4 as [Correct] " +
            "FROM XRef  " +
            "WHERE _id not in (SELECT DISTINCT _id FROM JScoreboard) " +
            "ORDER BY RANDOM() LIMIT  1000");

    db.execSQL("INSERT INTO JScoreboard " +
            "SELECT _id " +
            ",ABS(RANDOM()) % (16 - 14) + 14 as [Total] " +
            ",ABS(RANDOM()) % (13- 11) + 11 as [Correct] " +
            "FROM XRef  " +
            "WHERE _id not in (SELECT DISTINCT _id FROM JScoreboard) " +
            "ORDER BY RANDOM() LIMIT  1000");

    db.execSQL("INSERT INTO JScoreboard " +
            "SELECT _id " +
            ",ABS(RANDOM()) % (9 - 8) + 8 as [Total] " +
            ",ABS(RANDOM()) % (7- 2) + 2 as [Correct] " +
            "FROM XRef  " +
            "WHERE _id not in (SELECT DISTINCT _id FROM JScoreboard) " +
            "ORDER BY RANDOM() LIMIT  1000");

    db.execSQL("DELETE FROM JScoreboard where Total < Correct ");

    //ADD FAVS LISTS....
    db.execSQL("DELETE FROM JFavoritesLists ");
    db.execSQL("INSERT INTO JFavoritesLists " +
            "SELECT 'Quiz Words' as Name UNION " +
            "SELECT 'JLPT Words' as Name UNION " +
            "SELECT 'Words I Forget' as Name ");

    db.execSQL("DELETE FROM JFavoritesTweetLists ");
    db.execSQL("INSERT INTO JFavoritesTweetLists " +
            "SELECT 'Super Cool Tweets' as Name UNION " +
            "SELECT 'Tuesday Stuff' as Name UNION " +
            "SELECT 'Other Tweets' as Name ");

}
    /**
     * Each tab "bucket" has its own fab action and actionbar text/back button display depending
     * on where the user is navigating within the tab bucket. This method initializes the fab/action bar
     * for the given tab, and also is called from the mViewPager onPageSelected listener, to keep the tabs/fab updated
     * @param position Position of mViewPager (i.e. current visible tab bucket)
     */
    public void setUpFabsAndMenus(int position) {
        if(isTopShowing(position)) {
            switch (position) {
                case 0:
                    showFab(true,"addUser");
                    showMenuMyListBrowse(false,position);
                    showActionBarBackButton(false,"JukuTweet",position);

                    break;
                case 1:
                    showFab(true,"addTweetList");
                    showMenuMyListBrowse(false,position);
                    showActionBarBackButton(false,"JukuTweet",position);
                    break;
                case 2:
                    showFab(true,"addMyList");
                    showMenuMyListBrowse(false,position);
                    showActionBarBackButton(false,"JukuTweet",position);
                    break;
                default:
                    showMenuMyListBrowse(false,position);
                    showFab(false);
                    showActionBarBackButton(false,"JukuTweet",position);
                    break;
            }

        } else {
            showFab(false);
            if(tabsShowingBrowseMenu!=null && tabsShowingBrowseMenu[position]) {
                showMenuMyListBrowse(true,position);
            } else {
                showMenuMyListBrowse(false,position);
            }

            if(tabsShowingBackButton!=null && tabsShowingBackButton[position] !=null) {
                showActionBarBackButton(true,tabsShowingBackButton[position],position);
            } else {
                showActionBarBackButton(false,"",position);
            }
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
     * Handle clicks to action bar menu items. Mostly the set of "Delete", "Select All", "Copy" and "Cancel"
     * buttons that appear when words/tweets are selected in the browse fragments.
     * @see WordListBrowseFragment
     * @see TweetListBrowseFragment
     * @see TweetListSingleUserFragment
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new UserPreferenceFragment(),"preferenceFragment")
                        .commit();
            return true;
        }

        switch(id) {
             case android.R.id.home:
                        onBackPressed();
                       break;

            //Cancel the selections
            case R.id.action_cancel:
                try {
                    switch (mViewPager.getCurrentItem()) {
                        case 0:
                            if(tabsShowingBrowseMenu[0] && findFragmentByPosition(0) != null) {
                                ((TweetListBrowseFragment) findFragmentByPosition(0).getChildFragmentManager().findFragmentByTag("savedtweetsbrowseSingleUser")).deselectAll();
                                showMenuMyListBrowse(false, 0);
                            }
                            break;
                        case 1:
                            if(tabsShowingBrowseMenu[1] && findFragmentByPosition(1) != null) {
                                ((TweetListBrowseFragment) findFragmentByPosition(1).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse")).deselectAll();
                                showMenuMyListBrowse(false, 1);
                            }
                            break;
                        case 2:
                            if(tabsShowingBrowseMenu[2] && findFragmentByPosition(2) != null) {
                                ((WordListBrowseFragment) findFragmentByPosition(2).getChildFragmentManager().findFragmentByTag("mylistbrowse")).deselectAll();
                                showMenuMyListBrowse(false, 2);
                            }
                            break;
                    }

                } catch (NullPointerException e) {
                    Log.e(TAG,"TabContainer->BrowseFragment deselectALL Nullpointer: " + e.toString());
                }


                break;

            //Show copy/move dialog
            case R.id.action_copy:

                try {
                    switch (mViewPager.getCurrentItem()) {
                        case 0:
                            if(tabsShowingBrowseMenu[0] && findFragmentByPosition(0) != null) {
                                ((TweetListBrowseFragment) findFragmentByPosition(0).getChildFragmentManager().findFragmentByTag("savedtweetsbrowseSingleUser")).showCopyTweetsDialog();
                            }
                            break;
                        case 1:
                            if(tabsShowingBrowseMenu[1] && findFragmentByPosition(1) != null) {
                                ((TweetListBrowseFragment) findFragmentByPosition(1).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse")).showCopyTweetsDialog();
                            }
                            break;
                        case 2:
                            if(tabsShowingBrowseMenu[2] && findFragmentByPosition(2) != null) {
                                ((WordListBrowseFragment) findFragmentByPosition(2).getChildFragmentManager().findFragmentByTag("mylistbrowse")).showCopyMyListDialog();
                            }
                            break;
                    }

                } catch (NullPointerException e) {
                    Log.e(TAG,"TabContainer->BrowseFragment copy Nullpointer: " + e.toString());
                }

                break;

            //Delete selected items
            case R.id.action_delete:
                try {
                    switch (mViewPager.getCurrentItem()) {
                        case 0:
                            if(tabsShowingBrowseMenu[0] && findFragmentByPosition(0) != null) {
                                ((TweetListBrowseFragment) findFragmentByPosition(0).getChildFragmentManager().findFragmentByTag("savedtweetsbrowseSingleUser")).removeTweetFromList();
                                showMenuMyListBrowse(false, 0);
                            }
                            break;
                        case 1:
                            if(tabsShowingBrowseMenu[1] && findFragmentByPosition(1) != null) {
                                ((TweetListBrowseFragment) findFragmentByPosition(1).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse")).removeTweetFromList();
                                showMenuMyListBrowse(false, 1);
                            }
                            break;
                        case 2:
                            if(tabsShowingBrowseMenu[2] && findFragmentByPosition(2) != null) {
                                ((WordListBrowseFragment) findFragmentByPosition(2).getChildFragmentManager().findFragmentByTag("mylistbrowse")).removeKanjiFromList();
                                showMenuMyListBrowse(false, 2);
                            }
                            break;
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG,"TabContainer->BrowseFragment delete Nullpointer: " + e.toString());
                }


                break;

            //Select all items in browse list
            case R.id.action_selectall:
                try {
                switch (mViewPager.getCurrentItem()) {
                    case 0:
                        if(tabsShowingBrowseMenu[0] && findFragmentByPosition(0) != null) {
                            ((TweetListBrowseFragment) findFragmentByPosition(0).getChildFragmentManager().findFragmentByTag("savedtweetsbrowseSingleUser")).selectAll();
                        }
                        break;
                    case 1:
                        if(tabsShowingBrowseMenu[1] && findFragmentByPosition(1) != null) {
                            ((TweetListBrowseFragment) findFragmentByPosition(1).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse")).selectAll();
                        }
                        break;
                    case 2:
                        if(tabsShowingBrowseMenu[2] && findFragmentByPosition(2) != null) {
                            ((WordListBrowseFragment) findFragmentByPosition(2).getChildFragmentManager().findFragmentByTag("mylistbrowse")).selectAll();
                        }
                        break;
                }

                } catch (NullPointerException e) {
                    Log.e(TAG,"TabContainer->BrowseFragment selectALL Nullpointer: " + e.toString());
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows {@link AddUserDialog}, where user can input a twitter handle whom they would like to follow.
     * This method is called from the fabAddUser button click in Tab0 {@link com.jukuproject.jukutweet.Fragments.UserListFragment}
     */
    public void showAddUserDialog(){

            if(getFragmentManager().findFragmentByTag("dialogAdd") == null || !getFragmentManager().findFragmentByTag("dialogAdd").isAdded()) {
                AddUserDialog.newInstance().show(getSupportFragmentManager(),"dialogAdd");
        }
    }

    /**
     * Shows {@link AddUserCheckDialog}, with information for the prospective twitter feed. User can decide whether to add that feed
     * to the UserList or not. This dialog is called after the showAddUserDialog onOkPressed.
     * @param userInfo userInfo for prospective twitter feed
     */
    public void showAddUserCheckDialog(UserInfo userInfo){

        if(getFragmentManager().findFragmentByTag("dialogAddCheck") == null || !getFragmentManager().findFragmentByTag("dialogAddCheck").isAdded()) {
            AddUserCheckDialog.newInstance(userInfo).show(getSupportFragmentManager(),"dialogAddCheck");
        }
    }

    /**
     * Recieves input text from add user dialog
     * Checks if the user already exists in database
     * If not, inputs user into db and updates mainFragment recycler
     * Then uses twitter api to pull "UserInfo" for the user,
     * and initiates the {@link AddUserCheckDialog} via {@link MainActivity#showAddUserCheckDialog(UserInfo)}
     * @param inputText twitter user screen name entered into the "add user" dialog box
     */
    @Override
    public void onAddUserDialogPositiveClick(String inputText) {

        /* Check to DB to see if the new feed is a duplicate*/
        if(InternalDB.getUserInterfaceInstance(getBaseContext()).duplicateUser(inputText.trim())) {
            Toast.makeText(this, "UserInfo already exists", Toast.LENGTH_SHORT).show();
        } else if (!isOnline()) {
            if(!InternalDB.getUserInterfaceInstance(getBaseContext()).saveUserWithoutData(inputText.trim())) {
                Toast.makeText(getBaseContext(), "Unable to save " + inputText.trim(), Toast.LENGTH_SHORT).show();
            } else {
                try {
                    ((Tab1Container) findFragmentByPosition(0)).updateUserListFragment();
                } catch (NullPointerException e) {
                    Log.e(TAG,"nullpointer error onAddUserDialogPOsitiveclick " + e.toString());
                }
            }

        } else {
            getInitialUserInfoForAddUserCheck(inputText.trim());
        }

    }

    /**
     * Shows remove User Dialog when "remove" user is clicked in the {@link com.jukuproject.jukutweet.Dialogs.UserDetailPopupDialog}
     * @param userInfo UserInfo to "unfollow" (i.e. remove from database)
     *
     * @see com.jukuproject.jukutweet.Dialogs.UserDetailPopupDialog
     * @see com.jukuproject.jukutweet.Fragments.UserListFragment
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
     * @see RemoveUserDialog
     * @see com.jukuproject.jukutweet.Fragments.UserListFragment
     */
    @Override
    public void onRemoveUserDialogPositiveClick(String userId) {

        if (InternalDB.getUserInterfaceInstance(getBaseContext()).deleteUser(userId) ) {

            /*If the tweet's user is not saved in the db and there are no tweets for the user anymore
        , delete the user's icon from the app icons folder */
            InternalDB.getUserInterfaceInstance(getBaseContext()).clearOutUnusedUserIcons(getBaseContext());

            // Locate Tab1Continer and update the UserListInfo adapter to reflect removed item
                try {
                    ((Tab1Container) findFragmentByPosition(0)).updateUserListFragment();
                } catch (NullPointerException e) {
                    Log.e(TAG,"nullpointer error onRemoveUserDialogPOsitiveclick " + e.getCause());
                }
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
        showProgressBar(true);
        if(userInfoSubscription==null) {
            String token = getResources().getString(R.string.access_token);
            String tokenSecret = getResources().getString(R.string.access_token_secret);
            userInfoSubscription = TwitterUserClient.getInstance(token,tokenSecret)
                    .getUserInfo(screenName)
                    .subscribeOn(Schedulers.io())
                    .timeout(6, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<UserInfo>() {
                        UserInfo userInfoInstance;

                        @Override public void onCompleted() {
                            if(BuildConfig.DEBUG){Log.d(TAG, "getInitialUserInfoForAddUserCheck In onCompleted()");}

                            /* If the user exists and a UserInfo object has been populated,
                            * save it to the database and update the UserInfoFragment adapter */
                            if(userInfoInstance != null) {
                                showAddUserCheckDialog(userInfoInstance);
                            } else {
                                Toast.makeText(MainActivity.this, "Unable to add user " + screenName, Toast.LENGTH_SHORT).show();
                            }
                            userInfoSubscription = null;
                            showProgressBar(false);
                        }

                        @Override public void onError(Throwable e) {
                            e.printStackTrace();
                            showProgressBar(false);

                            if(BuildConfig.DEBUG){Log.d(TAG, "getInitialUserInfoForAddUserCheck In onError()");}
                            Toast.makeText(getBaseContext(), "Unable to connect to Twitter API, or user screen name is incorrect", Toast.LENGTH_LONG).show();

                            /* If process is unable to get userInfo, save the screenName only as a placeholder to be accessed later */
                            if(!InternalDB.getUserInterfaceInstance(getBaseContext()).duplicateUser(screenName)) {
                                InternalDB.getUserInterfaceInstance(getBaseContext()).saveUserWithoutData(screenName);
                            }
                            userInfoSubscription = null;
                            Log.d(TAG,"ERROR CAUSE: " + e.getCause());
                        }

                        @Override public void onNext(UserInfo userInfo) {
                            if(BuildConfig.DEBUG) {
                                Log.d(TAG, "In onNext()");
                                Log.d(TAG, "userInfo: " + userInfo.getUserId() + ", " + userInfo.getDescription());
                            }

                            if(userInfoInstance == null) {
                                userInfoInstance = userInfo;
                            }


                        }
                    });

        }

    }


    /**
     * Adds a user to {@link com.jukuproject.jukutweet.Fragments.UserListFragment} and downloads icon for user. Called
     *  from {@link AddUserCheckDialog} when user clicks "Ok"
     * @param userInfoInstance UserInfo object for prospective user to add to db
     */
    public void saveAndUpdateUserInfoList(UserInfo userInfoInstance) {

        if(InternalDB.getUserInterfaceInstance(getBaseContext()).duplicateUser(userInfoInstance.getScreenName())) {
            Toast.makeText(this, "User is already saved", Toast.LENGTH_SHORT).show();
        } else if(InternalDB.getUserInterfaceInstance(getBaseContext()).saveUser(userInfoInstance)) {
            try{

                InternalDB.getUserInterfaceInstance(getBaseContext()).downloadUserIcon(getBaseContext(),userInfoInstance.getProfileImageUrlBig(),userInfoInstance.getUserId());
            } catch (Exception e) {
                Log.e(TAG,"Image download failed: " + e.toString());
            }
            // Locate Tab1Continer and update the UserListInfo adapter to reflect removed item
            if(findFragmentByPosition(0) != null && findFragmentByPosition(0) instanceof Tab1Container) {
                ((Tab1Container) findFragmentByPosition(0)).updateUserListFragment();
            }
        } else {
            Log.e(TAG,"saveAndUpdateUserInfoList saving user failed ");
        }
    }





    /**
     * If the top bucket is visible after popping the fragment in the backstack, use {@link #updateTabs(String[])} to
     * include the mylist bucket and show the main page title strip items. Basically reset everything
     */
    @Override
    public void onBackPressed() {

        boolean isPopFragment;
        boolean clearingSearchOrDismissingPreferences = false;

        /*If user is closing the preference fragment, update prefences and reload word/tweet list tabs, as
        they may have changed */
        if(getFragmentManager().findFragmentByTag("preferenceFragment") != null
                && getFragmentManager().findFragmentByTag("preferenceFragment").isAdded()) {
            getFragmentManager().beginTransaction()
                    .remove(getFragmentManager().findFragmentByTag("preferenceFragment"))
                    .commit();
            clearingSearchOrDismissingPreferences = true;
            SharedPrefManager.initialize(getBaseContext());

            //List fragments may have changed due to preference fragment changes, so update them
            if(findFragmentByPosition(2).getChildFragmentManager().findFragmentByTag("mylistfragment") != null) {
                ((WordListFragment) findFragmentByPosition(2).getChildFragmentManager().findFragmentByTag("mylistfragment")).updateMyListAdapter();
            }
            if(findFragmentByPosition(1).getChildFragmentManager().findFragmentByTag("savedtweetsallfragment") != null) {
                ((TweetListFragment) findFragmentByPosition(1).getChildFragmentManager().findFragmentByTag("savedtweetsallfragment")).updateMyListAdapter();
            }

        } else if(mViewPager.getCurrentItem() == 3) {
            //If it is a searchfragment, and a search has some results, on back should clear the search, not pop the fragment
            if(clearingSearchOrDismissingPreferences = (findFragmentByPosition(3) != null
                    && findFragmentByPosition(3) instanceof Tab4Container
                    && findFragmentByPosition(3).getChildFragmentManager().findFragmentByTag("searchFragment") != null)) {
                clearingSearchOrDismissingPreferences = ((SearchFragment) findFragmentByPosition(3).getChildFragmentManager().findFragmentByTag("searchFragment")).clearSearchResults();
            }
        }

        //Otherwise pop the fragment or close the program
        if(!clearingSearchOrDismissingPreferences) {
            showMenuMyListBrowse(false,mViewPager.getCurrentItem());
            isPopFragment = ((BaseContainerFragment)findFragmentByPosition(mViewPager.getCurrentItem())).popFragment();
            try {
                if(findFragmentByPosition(mViewPager.getCurrentItem()).getChildFragmentManager().getBackStackEntryCount()<=1) {

                    /* The UserTimeline and User SavedTweets fragments work in concert. So when the user backs out
                    * to the top of */
                    if(mAdapterTitles.length == 2 && mAdapterTitles[0].equals("Timeline") && mViewPager.getCurrentItem()==0) {
                        ((Tab2Container)findFragmentByPosition(1)).popAllFragments();
                    } else if(mAdapterTitles.length == 2 && mAdapterTitles[0].equals("Timeline") && mViewPager.getCurrentItem()==1) {
                        ((Tab1Container)findFragmentByPosition(0)).popAllFragments();
                    }

                    showActionBarBackButton(false,getString(R.string.app_name),mViewPager.getCurrentItem());
                    updateTabs(new String[]{"Users","Tweet Lists","Word Lists","Search"});
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

            showProgressBar(false);

            if (!isPopFragment) {
                finish();
            }
        }

    }


    /**
     * Returns fragment for a given position in {@link PostQuizStatsPagerAdapter}
     * @param position pager adapter position
     * @return Fragment corresponding to position
     */
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
    public void showActionBarBackButton(Boolean showBack
            , CharSequence title
            ,int tabNumber) {

        if(showBack) {
            tabsShowingBackButton[tabNumber] = title.toString();
        } else {
            tabsShowingBackButton[tabNumber] = null;
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(showBack);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * Shows dialog where user can create a new list (either word list or tweet list)
     * @param listType string designating the list to be created as a Word List (aka "MyList")  or Tweet List
     *
     *@see AddOrRenameMyListDialog
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
     *
     *@see AddOrRenameMyListDialog
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
     *
     *@see EditMyListDialog
     */
    public void showEditMyListDialog(String listType, String currentListName, Boolean isStarFavorite){

        if(getFragmentManager().findFragmentByTag("dialogEditMyList") == null || !getFragmentManager().findFragmentByTag("dialogEditMyList").isAdded()) {
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
                                    /* Locate Tab2Continer and update the MyList adapter to reflect removed item */
                try {
                    ((WordListFragment) findFragmentByPosition(2).getChildFragmentManager().findFragmentByTag("mylistfragment")).updateMyListAdapter();
                } catch (NullPointerException e) {
                    Log.e(TAG,"onAddMyListDialogPositiveClick Nullpointer: " + e.getCause());
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
    }

    /**
     * Action that occurs when the user presses OK on the edit my list dialog. They can either clear a list of its
     * contents, rename it or delete it entirely. The user can do this for word lists (AKA "mylists") or for Tweet Lists
     * @param listType designates whether the list being edited is a Word list, or a Tweet list
     * @param selectedItem integer representing the row the user clicked 1-clear, 2-rename,3-delete
     * @param listName name of list that user is editing
     * @param isStarFavorite bool designating the list as a system favorites list (true) or user list (false). If it is a starfavorite list
     *                       the final "are you sure" dialog will be simpler than the one for a user-created list
     *
     *@see EditMyListDialog
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
     *
     *@see AddOrRenameMyListDialog
     */
    @Override
    public void onRenameMyListDialogPositiveClick(String listType,String oldListName, String listName) {
        if(listType.equals("MyList")) {

            //If save is successful, update wordlist fragment
            if(InternalDB.getWordInterfaceInstance(getBaseContext()).renameWordList(oldListName,listName)) {
                try {
                    ((WordListFragment) findFragmentByPosition(2).getChildFragmentManager().findFragmentByTag("mylistfragment")).updateMyListAdapter();
                } catch (NullPointerException e) {
                    Log.e(TAG,"onRenameMyListDialogPositiveClick Nullpointer: " + e.getCause());
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
    }

    /**
     * Action that occurs when the user presses OK to delete or clear a list (which occurs in the {@link #onEditMyListDialogPositiveClick(String, int, String, boolean)}.
     * @param listType designates whether the list being edited is a Word list, or a Tweet list
     * @param delete bool true to delete the list, false to clear the list of its contents (but keep the list)
     * @param name listname
     * @param isStarFavorite bool designating the list as a system favorites list (true) or user list (false). If it is a starfavorite list
     *                       the final "are you sure" dialog will be simpler than the one for a user-created list
     *
     *@see EditMyListDialog
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
                    try {
                        ((WordListFragment) findFragmentByPosition(2).getChildFragmentManager().findFragmentByTag("mylistfragment")).updateMyListAdapter();
                    } catch (NullPointerException e) {
                        Log.e(TAG,"deleteOrClearDialogFinal Nullpointer: " + e.getCause());
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
     * is the top fragment in the stack for that bucket. Used with the mViewPager.addOnPageChangeListener to change the function/visibility
     * of the floating action button depending on the current tab.
     * @param position position of the viewpager adapter
     * @return bool true if the top fragment is showing, false if not
     *
     */
    public boolean isTopShowing(int position) {

        try {
            return ((BaseContainerFragment)findFragmentByPosition(position)).isTopFragmentShowing();
        } catch(NullPointerException e) {
            return false;
        }
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
            } else if(show && type.equals("addMyList")) {
                fab.setVisibility(View.VISIBLE);
                fab.setImageResource(R.drawable.ic_star_black);
                fab.setColorFilter(ContextCompat.getColor(getBaseContext(), android.R.color.white));
            } else if(show && type.equals("addTweetList")) {
            fab.setVisibility(View.VISIBLE);
            fab.setImageResource(R.drawable.ic_twitter_black_24dp);
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
     * the PostQuizStatsPagerAdapter, which will update the number of available tab buckets and
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
     * Looks for current tweet fragment in tab 1 (either saved tweet lists
     * or individual users saved tweets) and refreshes the list. Initiated from the onSuccess portion of the parseAndSaveTweet method.
     * @see #parseAndSaveTweet(Tweet)
     * @see TweetBreakDownFragment
     * @see com.jukuproject.jukutweet.Fragments.UserTimeLineFragment
     */
    public void notifySavedTweetFragmentsChanged() {
        try {
            for(int i=0;i<4;i++) {
                    Fragment currentFragment = ((BaseContainerFragment) findFragmentByPosition(i)).getTopFragment();
                    if(currentFragment instanceof TweetListSingleUserFragment) {
                        ((TweetListSingleUserFragment) currentFragment).updateMyListAdapter();
                    } else if(currentFragment instanceof TweetListFragment) {
                        ((TweetListFragment) currentFragment).updateMyListAdapter();
                    } else if(currentFragment instanceof TweetListBrowseFragment) {
                        ((TweetListBrowseFragment) currentFragment).updateMyListAdapter();
                    }
            }
        } catch (NullPointerException e) {
            Log.e(TAG,"currentFragment updates failed for notifySavedTweetFragmentsChanged");
        }
    }

    /**
     * Coordinates status of word favorite lists as they change across the tabs. When the user changes a word entry favorite list in one tab,
     * the others must immediately reflect that change because the user can just swipe to them.
     *
     * For example, If the user has opened the tweet breakdown
     * fragment in tab 1 (for a single user's saved tweets), and has added a word to a list, the word related fragments in tabs 2 and must
     * be updated to reflect that change, as well as the search tab if applicable..
     *
     * The loop checks if a fragment is one of a certain type (i.e. one that is of interest because the WordEntry
     * list it contains might need to be updated to reflect a change in another tab), and updates that fragment if it is. This is
     * called via {@link #notifySavedWordFragmentsChanged(String)} when words were removed from a word list in the {@link WordListBrowseFragment}
     *
     * @param wordEntryIdString concatenated comma-delimited string of edict kanji ids for the words that were removed
     */
    public void notifySavedWordFragmentsChanged(String wordEntryIdString) {

        ArrayList<WordEntry> wordEntries = InternalDB.getWordInterfaceInstance(getBaseContext()).getWordsFromAStringofWordIds(wordEntryIdString,SharedPrefManager.getInstance(getBaseContext()).getColorThresholds());
        Log.i(TAG,"WordEntry size: " + wordEntries.size());
        try {
            for(int i=0;i<4;i++) {
                if(mViewPager.getCurrentItem()!=i) {
                    Fragment currentFragment = ((BaseContainerFragment) findFragmentByPosition(i)).getTopFragment();
                    if(currentFragment instanceof TweetBreakDownFragment) {
                        ((TweetBreakDownFragment) currentFragment).updateWordEntryItemFavorites(wordEntries);
                    } else if(currentFragment instanceof FlashCardsFragment) {
                        ((FlashCardsFragment) currentFragment).updateWordEntryItemFavorites(wordEntries);
                    } else if(currentFragment instanceof StatsFragmentProgress) {
                        ((StatsFragmentProgress) currentFragment).updateWordEntryItemFavorites(wordEntries);
                    } else  if(currentFragment instanceof SearchFragment) {
                        ((SearchFragment) currentFragment).updateWordEntryItemFavorites(wordEntries);
                    } else if(currentFragment instanceof WordListBrowseFragment ) {
                        for(WordEntry wordEntry : wordEntries) {
                            ((WordListBrowseFragment) currentFragment).updateWordEntryItemFavorites(wordEntry);
                        }
                    } else if(currentFragment instanceof WordListFragment) {
                        ((WordListFragment) currentFragment).updateMyListAdapter();
                    }
                }
            }
        } catch (NullPointerException e) {
            Log.e(TAG,"currentFragment updates failed for notifySavedWordFragmentsChanged");
        }
    }


    /**
     * Traffic control, passing result of  {@link com.jukuproject.jukutweet.Dialogs.CopyMyListItemsDialog}  to the
     * {@link WordListBrowseFragment} so the words can be saved and the recycler refreshed
     *
     * @param kanjiIdString concatenated string of kanji ids that will be moved/copied
     * @param listsToCopyTo MyList object of word lists that the words will be copied to
     * @param move bool true for MOVE the words, false to only COPY the words
     * @param currentList The current list from which the copy dialog is called. This is so the list is not included
     *                    as an option to move/copy to.
     */
    public void saveAndUpdateMyLists(String kanjiIdString
            , ArrayList<MyListEntry> listsToCopyTo
            , boolean move
            , MyListEntry currentList) {
        if(findFragmentByPosition(2) != null && findFragmentByPosition(2) instanceof Tab3Container) {
            try {
                ((WordListBrowseFragment) findFragmentByPosition(2).getChildFragmentManager().findFragmentByTag("mylistbrowse")).saveAndUpdateMyLists(kanjiIdString,listsToCopyTo,move,currentList);
            } catch (NullPointerException e) {
                Log.e(TAG,"Tab3Container->WordListBrowseFragment saveAndUpdateMyLists Nullpointer: " + e.toString());
            }

        }
    }

    /**
     * Traffic control, passing result of  {@link com.jukuproject.jukutweet.Dialogs.CopySavedTweetsDialog}  to the
     * {@link TweetListBrowseFragment} so the tweets can be saved and the recycler refreshed
     *
     *
     * @param tweetIds tweetIds of tweets to be copied/moved
     * @param listsToCopyTo Lists that the tweets will be copied to
     * @param move bool true for MOVE the tweet, false to only COPY the tweet
     * @param currentList The current list from which the copy dialog is called. This is so the list is not included
     *                    as an option to move/copy to.
     */
    public void saveAndUpdateTweetLists(String tweetIds
            , ArrayList<MyListEntry> listsToCopyTo
            , boolean move
            , MyListEntry currentList) {

        if(findFragmentByPosition(1) != null && findFragmentByPosition(1) instanceof Tab2Container) {
            try {
                ((TweetListBrowseFragment) findFragmentByPosition(1).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse")).saveAndUpdateTweets(tweetIds,listsToCopyTo,move,currentList);
            } catch (NullPointerException e) {
                Log.e(TAG,"Tab2Container->TweetListBrowseFragment saveAndUpdateMyLists Nullpointer: " + e.toString());
            }

        }
    }

    /**
     * Shows the flashcard fragment (Replacing the current fragment) for specified words in a wordlist or savedtweet list
     * @see com.jukuproject.jukutweet.Dialogs.QuizMenuDialog
     * @param tabNumber the number of the tab that was open before the activity was destroyed
     * @param listEntry Word or Tweet list from which the flashcards will be picked
     * @param frontValue tag specifying type of text on the front of the flashcard (kanji, furigana, definition)
     * @param backValue tag specifying type of text on the back of the flashcard (kanji, furigana, definition)
     * @param selectedColorString concatenated string specifying which word colors (based on the percentage score for that word)
     *                            will be allowed to be included in the quiz (ex: 'Grey,Red,Green')
     */
    public void showFlashCardFragment(int tabNumber
            , MyListEntry listEntry
            , String frontValue
            , String backValue
            ,String selectedColorString) {

        showFab(false);

        //If its a WordList fragment
        if(tabNumber == 1) {

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

            //If its a TweetList fragment
        } else if(tabNumber == 2) {
            ArrayList<WordEntry> dataset = InternalDB.getWordInterfaceInstance(getBaseContext())
                    .getWordsFromAWordList(listEntry
                            ,SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            ,selectedColorString
                            ,null
                            ,null);

            if(findFragmentByPosition(tabNumber) != null
                    && findFragmentByPosition(tabNumber) instanceof Tab3Container) {
                FlashCardsFragment flashCardsFragment = FlashCardsFragment.newInstance(dataset,frontValue,backValue);
                ((BaseContainerFragment)findFragmentByPosition(tabNumber)).replaceFragment(flashCardsFragment,true,"flashcards");
            }
        }
    }

    /**
     * Shows the flashcard fragment (Replacing the current fragment) for specified words in the saved tweets of
     * a single user (from the {@link TweetListSingleUserFragment})
     * @see com.jukuproject.jukutweet.Dialogs.QuizMenuDialog
     * @param tabNumber the number of the tab that was open before the activity was destroyed
     * @param userInfo UserInfo object for the user whose tweets will be used in the quiz
     * @param frontValue tag specifying type of text on the front of the flashcard (kanji, furigana, definition)
     * @param backValue tag specifying type of text on the back of the flashcard (kanji, furigana, definition)
     * @param selectedColorString concatenated string specifying which word colors (based on the percentage score for that word)
     *                            will be allowed to be included in the quiz (ex: 'Grey,Red,Green')
     */
    public void showSingleUserFlashCardFragment(int tabNumber
            , UserInfo userInfo
            , String frontValue
            , String backValue
            ,String selectedColorString) {

            ArrayList<WordEntry> dataset = InternalDB.getTweetInterfaceInstance(getBaseContext())
                    .getWordsFromAUsersSavedTweets(userInfo
                            ,SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            ,selectedColorString
                            ,null
                            ,null);

            if(findFragmentByPosition(tabNumber) != null
                    && findFragmentByPosition(tabNumber) instanceof Tab1Container) {

                FlashCardsFragment flashCardsFragment = FlashCardsFragment.newInstance(dataset,frontValue,backValue);
                ((BaseContainerFragment)findFragmentByPosition(tabNumber)).replaceFragment(flashCardsFragment,true,"flashcards");
            }
    }


    /**
     * Sends intent to {@link QuizActivity} to begin the {@link com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment}
     * multiple choice quiz
     * @param tabNumber the number of the tab that was open before the activity was destroyed
     * @param listEntry Word or Tweet list from which the quiz answers will be picked
     * @param currentExpandedPosition position of word/tweet list entry that was last expanded. In case user
     *                                goes back to the Main Activity, the correct list can be expanded on activity creation
     * @param quizType Quiz menu option for multiple choice quiz ("Kanji to Definition" etc)
     * @param quizSize Quiz menu option, number of questions to ask in the quiz
     * @param quizTimer Quiz menu option, number of seconds in timer (or can be "None"). IS A STRINg.
     * @param selectedColorString concatenated string of word colors that are available for this quiz (ex: blue,red,yellow)
     */
    public void goToQuizActivityMultipleChoice(int tabNumber
            , MyListEntry listEntry
            ,Integer currentExpandedPosition
            , String quizType
            , String quizSize
            , String quizTimer
            , String selectedColorString) {

        ArrayList<WordEntry> dataset = new ArrayList<>();
        String dataType = "";
        if (tabNumber == 1) {

            //Its a tweetlist fragment
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
                double totalweight = assignWordWeightsAndGetTotalWeight(getBaseContext(),dataset,.50,.025,30);
                Intent intent = new Intent(getBaseContext(), QuizActivity.class);

                intent.putExtra("typeOfQuizThatWasCompleted","Multiple Choice"); //The type of quiz that was chosen inthe menu
                intent.putExtra("quizType",quizType);
                intent.putExtra("tabNumber", tabNumber);
                intent.putExtra("myListEntry",listEntry);
                intent.putExtra("quizSize",quizSize);
                intent.putExtra("colorString",selectedColorString);
                intent.putExtra("timer",quizTimer); //Timer can be "none" so passing it on raw as string
                intent.putExtra("totalweight",totalweight);
                intent.putParcelableArrayListExtra("dataset",dataset);
                intent.putExtra("dataType",dataType);
                intent.putExtra("lastExpandedPosition",currentExpandedPosition);
                startActivity(intent);
          } else {
                Toast.makeText(this, "No words found to quiz on", Toast.LENGTH_SHORT).show();
            }
    }


    /**
     * Sends intent to {@link QuizActivity} to begin the {@link com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment}
     * multiple choice quiz for a Single User's saved tweets. A different set of sql queries and such must be used to pull
     * the datasets for savedtweets from a single user, so this is a seperate method
     * @param tabNumber the number of the tab that was open before the activity was destroyed
     * @param userInfo UserInfo object for the user whose tweets will be used in the quiz
     * @param currentExpandedPosition position of word/tweet list entry that was last expanded. In case user
     *                                goes back to the Main Activity, the correct list can be expanded on activity creation
     * @param quizType Quiz menu option for multiple choice quiz ("Kanji to Definition" etc)
     * @param quizSize Quiz menu option, number of questions to ask in the quiz
     * @param quizTimer Quiz menu option, number of seconds in timer (or can be "None"). IS A STRINg.
     * @param selectedColorString concatenated string of word colors that are available for this quiz (ex: blue,red,yellow)
     */
    public void goToSingleUserQuizActivityMultipleChoice(int tabNumber
            , UserInfo userInfo
            ,Integer currentExpandedPosition
            , String quizType
            , String quizSize
            , String quizTimer
            , String selectedColorString) {

        ArrayList<WordEntry> dataset;
        String dataType;

            dataset = InternalDB.getTweetInterfaceInstance(getBaseContext())
                    .getWordsFromAUsersSavedTweets(userInfo
                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            , selectedColorString
                            , null
                            , Integer.parseInt(quizSize));
            dataType = "Tweet";

        if(dataset.size()>0) {
            double totalweight = assignWordWeightsAndGetTotalWeight(getBaseContext(),dataset,.50,.025,30);

            Intent intent = new Intent(getBaseContext(), QuizActivity.class);

            intent.putExtra("singleUser",true); //The type of quiz that was chosen inthe menu
            intent.putExtra("typeOfQuizThatWasCompleted","Multiple Choice"); //The type of quiz that was chosen inthe menu
            intent.putExtra("quizType",quizType); //Multiple choice quiz option (Kanji to Definition, etc)
            intent.putExtra("tabNumber", 0);
            intent.putExtra("userInfo",userInfo);
            intent.putExtra("quizSize",quizSize);
            intent.putExtra("colorString",selectedColorString);
            intent.putExtra("timer",quizTimer); //Timer can be "none" so passing it on raw as string
            intent.putExtra("totalweight",totalweight);
            intent.putParcelableArrayListExtra("dataset",dataset);
            intent.putExtra("dataType",dataType);
            intent.putExtra("lastExpandedPosition",currentExpandedPosition);
            startActivity(intent);
        } else {
            Toast.makeText(this, "No words found to quiz on", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Sends intent to {@link QuizActivity} to begin the {@link com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment}
     * fill in the blanks quiz
     * @param tabNumber the number of the tab that was open before the activity was destroyed
     * @param myListEntry Word or Tweet list from which the quiz answers will be picked
     * @param currentExpandedPosition position of word/tweet list entry that was last expanded. In case user
     *                                goes back to the Main Activity, the correct list can be expanded on activity creation
     * @param quizSize Quiz menu option, number of questions to ask in the quiz
     * @param selectedColorString concatenated string of word colors that are available for this quiz (ex: blue,red,yellow)
     */
    public void goToQuizActivityFillintheBlanks(int tabNumber
            , MyListEntry myListEntry
            , Integer currentExpandedPosition
            , String quizSize
            , String selectedColorString) {

        ArrayList<Tweet> dataset = new ArrayList<>();
        String dataType = "";

        //The request is coming from the saved tweets fragment
        if (tabNumber == 1) {

            dataset = InternalDB.getQuizInterfaceInstance(getBaseContext())
                    .getFillintheBlanksTweetsForATweetList(myListEntry
                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            , selectedColorString
                            , Integer.parseInt(quizSize));
            dataType = "Tweet";

        //The request is coming from the saved words fragment
        } else if (tabNumber == 2) {

           dataset = InternalDB.getQuizInterfaceInstance(getBaseContext())
                    .getFillintheBlanksTweetsForAWordList(myListEntry
                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            , selectedColorString
                            , Integer.parseInt(quizSize));

            dataType = "Word";

        }

        if(dataset.size()>0) {
            double totalweight = assignTweetWeightsAndGetTotalWeight(dataset,SharedPrefManager.getInstance(getBaseContext()).getSliderMultiplier(),.5,.025,30);

            Intent intent = new Intent(getBaseContext(), QuizActivity.class);

            intent.putExtra("typeOfQuizThatWasCompleted","Fill in the Blanks"); //The type of quiz that was chosen inthe menu
            intent.putExtra("tabNumber", tabNumber);
            intent.putExtra("myListEntry",myListEntry);
            intent.putExtra("quizSize",quizSize);
            intent.putExtra("colorString",selectedColorString);
            intent.putExtra("totalweight",totalweight);
            intent.putParcelableArrayListExtra("dataset",dataset);
            intent.putExtra("dataType",dataType);
            intent.putExtra("lastExpandedPosition",currentExpandedPosition);

            showFab(false);

            startActivity(intent);
        } else {
            Toast.makeText(this, "No tweets found to quiz on", Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * Sends intent to {@link QuizActivity} to begin the {@link com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment}
     * fill in the blank quiz for a Single User's saved tweets. A different set of sql queries and such must be used to pull
     * the datasets for savedtweets from a single user, so this is a seperate method
     * @param tabNumber the number of the tab that was open before the activity was destroyed
     * @param userInfo UserInfo object for the user whose tweets will be used in the quiz
     * @param currentExpandedPosition position of word/tweet list entry that was last expanded. In case user
     *                                goes back to the Main Activity, the correct list can be expanded on activity creation
     * @param quizSize Quiz menu option, number of questions to ask in the quiz
     * @param selectedColorString concatenated string of word colors that are available for this quiz (ex: blue,red,yellow)
     */
    public void goToSingleUserQuizActivityFillintheBlanks(int tabNumber
            , UserInfo userInfo
            , Integer currentExpandedPosition
            , String quizSize
            , String selectedColorString) {

        ArrayList<Tweet> dataset = new ArrayList<>();
        String dataType = "";

        if (tabNumber == 0) {
            dataset = InternalDB.getQuizInterfaceInstance(getBaseContext())
                    .getFillintheBlanksTweetsForAUser(userInfo
                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            , selectedColorString
                            , Integer.parseInt(quizSize));

            dataType = "Tweet";

        }

        if(dataset.size()>0) {
            double totalweight = assignTweetWeightsAndGetTotalWeight(dataset,SharedPrefManager.getInstance(getBaseContext()).getSliderMultiplier(),.5,.025,30);

            Intent intent = new Intent(getBaseContext(), QuizActivity.class);

            intent.putExtra("singleUser",true); //The type of quiz that was chosen in the menu
            intent.putExtra("typeOfQuizThatWasCompleted","Fill in the Blanks"); //The type of quiz that was chosen inthe menu
            intent.putExtra("tabNumber", tabNumber);
            intent.putExtra("userInfo",userInfo);
            intent.putExtra("quizSize",quizSize);
            intent.putExtra("colorString",selectedColorString);
            intent.putExtra("totalweight",totalweight);
            intent.putParcelableArrayListExtra("dataset",dataset);
            intent.putExtra("dataType",dataType);
            intent.putExtra("lastExpandedPosition",currentExpandedPosition);

            showFab(false);

            startActivity(intent);

        } else {
            Toast.makeText(this, "No tweets found to quiz on", Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * Assigns percentage weights to the WordEntry objects in the wordEntries list (the higher the weight,
     * the more likely the word will be chosen as a quiz answer).
      * @param context Context
     * @param wordEntries List of WordEntries that will be passed on to the quiz activity
     * @param sliderUpperBound The highest possible weight for a word (default .5)
     * @param sliderLowerBound The lower possible weight for a word (default .025)
     * @param sliderCountMax
     * @return the total combined weight of all the words in the list.
     */
    public static double assignWordWeightsAndGetTotalWeight(Context context
            , ArrayList<WordEntry> wordEntries
            , Double sliderUpperBound
            , Double sliderLowerBound
            , int sliderCountMax
    ) {
        final double sliderMultipler = SharedPrefManager.getInstance(context).getSliderMultiplier();

        double totalWeight = 0.0d;

        for(WordEntry wordEntry : wordEntries) {
            double percentage =wordEntry.getPercentage();
            double total = (double)wordEntry.getTotal();

            /* The slider multiplier is what affects how rapidly a word diverges from the natural weight of .25.
            * The higher the multiplier, the faster it will diverge with an increased count.*/
            double countMultiplier = total /(double)sliderCountMax*(percentage- sliderUpperBound)* sliderMultipler;
            double a = (sliderUpperBound /(double)2)- sliderUpperBound *(countMultiplier) ;
            double b = sliderLowerBound;
            if(a>=sliderUpperBound) {
                b = sliderUpperBound;
            } else if(a>=sliderLowerBound) {
                b = a;
            }
            wordEntry.setQuizWeight(b);
            totalWeight += b;
            if(BuildConfig.DEBUG){Log.d(TAG,"Setting quiz weight: " + b  + ", new total weight: " + totalWeight);}
        }

        return totalWeight;
    }


    /**
     * Assigns percentage weights to the WordEntry objects in a list of Tweets (the higher the weight,
     * the more likely the WordEntry will be chosen during the quiz).
     * @param tweets List of Tweets that will be passed on to the quiz activity
     * @param sliderUpperBound The highest possible weight for a word (default .5)
     * @param sliderLowerBound The lower possible weight for a word (default .025)
     * @param sliderCountMax
     * @return the total combined weight of all the words
     * contained in all the Tweets in the tweet list.
     */
    public static double assignTweetWeightsAndGetTotalWeight(ArrayList<Tweet> tweets
            , double sliderMultiplier
            , Double sliderUpperBound
            , Double sliderLowerBound
            , int sliderCountMax
    ) {

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
            double countMultiplier = aggregatedTweetTotal /(double)sliderCountMax*(tweetPercentage- sliderUpperBound)* sliderMultiplier;
            double a = (sliderUpperBound /(double)2)- sliderUpperBound *(countMultiplier) ;
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

    /**
     * Called during activity recreation to expand the previously expanded position in the word or tweet list.
     * @param tabNumber the number of the tab that was open before the activity was destroyed
     * @param positionToExpand position in the word/tweet list (if the open tab was 1 or 2)
     */
    public void setPreviousMyListExpanded(int tabNumber,int positionToExpand) {

        try {
        if(findFragmentByPosition(tabNumber) != null && findFragmentByPosition(tabNumber) instanceof Tab2Container) {
                Tab2Container container = ((Tab2Container) findFragmentByPosition(tabNumber));
                ((TweetListFragment) container.getChildFragmentManager().findFragmentByTag("savedtweetsallfragment")).expandTheListViewAtPosition(positionToExpand);
        } else if(findFragmentByPosition(tabNumber) != null && findFragmentByPosition(tabNumber) instanceof Tab3Container) {
                Tab3Container container = ((Tab3Container) findFragmentByPosition(tabNumber));
                ((WordListFragment) container.getChildFragmentManager().findFragmentByTag("mylistfragment")).expandTheListViewAtPosition(positionToExpand);
        }

        } catch (NullPointerException e) {
            Log.e(TAG,"setPreviousMyListExpanded could not set prev expanded");
        }
    }

    /**
     * Searches twitter for either "User", resulting in a list of possible users, or for a "Tweet",
     * resulting in a list of tweets that contain the query word. Called from {@link SearchFragment}
     * @param query query string from searchview
     * @param queryOption tag designating the search as either "User" or "Tweet". The search client will
     *                    run different searches depending on the option
     *
     * @see SearchFragment
     */
 public void runTwitterSearch(final String query, final String queryOption, @Nullable Long maxId) {
    if(searchQuerySubscription !=null && !searchQuerySubscription.isUnsubscribed()) {
        searchQuerySubscription.unsubscribe();
    }

     String token = getResources().getString(R.string.access_token);
     String tokenSecret = getResources().getString(R.string.access_token_secret);

     if(queryOption.equals("Tweet")) {

    /*If query is in romaji(and we are looking for a matching bit of text within a tweet),
    convert it to a kanji and run the search on that kanji if possible.  */
    final String kanjiQuery ;
    if(isRomaji(query)) {
        ArrayList<WordEntry> conversionResults = querytheDictionary(query,"Kanji");
        if(conversionResults != null && conversionResults.size()>0) {
            kanjiQuery = conversionResults.get(0).getKanji();
        } else {
            kanjiQuery = query;
        }

    } else {
        kanjiQuery = query;
    }


     searchQuerySubscription = TwitterUserClient.getInstance(token,tokenSecret)
            .getSearchTweets(kanjiQuery,"ja",20,maxId)

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<SearchTweetsContainer>() {

                ArrayList<Tweet> mDataSet = new ArrayList<>();


                @Override public void onCompleted() {
                    if(BuildConfig.DEBUG){Log.d(TAG, "runTwitterSearch In onCompleted()");}
                    searchSubscriptionCriteria = null;

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

                                //Attach colorfavorites to tweet, if they exists in db, so that the favorite stars will have the correct color
                                for(Tweet tweet : mDataSet) {
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

                        //Try to refresh the search fragment recyclerview with the updated search results
                        try {
                            ((SearchFragment) findFragmentByPosition(3).getChildFragmentManager().findFragmentByTag("searchFragment")).receiveTwitterSearchResults(mDataSet,kanjiQuery);
                        } catch (NullPointerException e) {
                            Log.e(TAG,"runTwitterSearch onsuccess error updating SearchFragment, null: " + e.toString());
                        }

                }

                @Override public void onError(Throwable e) {
                    if(findFragmentByPosition(3) != null) {
                        try {
                            SearchFragment searchFragment = ((SearchFragment) findFragmentByPosition(3).getChildFragmentManager().findFragmentByTag("searchFragment"));
                            searchFragment.showRecyclerView(false);
                            searchFragment.showProgressBar(false);
                        } catch (NullPointerException ee) {
                            Log.e(TAG,"runTwitterSearch onError error updating SearchFragment, null: " + ee.toString());
                        }
                    }

                    e.printStackTrace();
                    if(BuildConfig.DEBUG){Log.d(TAG, "runTwitterSearch In onError()");}
                    searchSubscriptionCriteria = null;


                }

                @Override public void onNext(SearchTweetsContainer results) {
                    if(BuildConfig.DEBUG) {
                        Log.d(TAG, "runTwitterSearch In onNext()");
                    }
                    if(mDataSet.size() == 0) {
                        try{
                            mDataSet.addAll(results.getTweets());
                        } catch (Exception e){
                            Log.e(TAG,"exception in runTwitterSearch get tweets");
                        }
                    }
                }
            });
     } else if(queryOption.equals("User")){

         searchQuerySubscription =   TwitterUserClient.getInstance(token,tokenSecret)
                     .getSearchUsers(query,15)
                     .subscribeOn(Schedulers.io())
                     .observeOn(AndroidSchedulers.mainThread())
                     .subscribe(new Observer<List<UserInfo>>() {
                         ArrayList<UserInfo> mDataSet = new ArrayList<>();
                         @Override public void onCompleted() {

                             //Try to refresh the search fragment recyclerview with the updated search results
                                 try {
                                     ((SearchFragment) findFragmentByPosition(3).getChildFragmentManager().findFragmentByTag("searchFragment")).receiveTwitterUserSearchResults(mDataSet);

                                 } catch (NullPointerException e) {
                                     Log.e(TAG,"runTwitterSearch onsuccess error updating SearchFragment, null: " + e.toString());
                                 }
                             searchSubscriptionCriteria = null;

                         }

                         @Override public void onError(Throwable e) {
                             e.printStackTrace();

                             if(BuildConfig.DEBUG){Log.d(TAG, "runTwitterSearch Users In onError()");}
                             searchSubscriptionCriteria = null;
                             try {
                                 SearchFragment searchFragment = ((SearchFragment) findFragmentByPosition(3).getChildFragmentManager().findFragmentByTag("searchFragment"));
                                 searchFragment.showRecyclerView(false);
                                 searchFragment.showProgressBar(false);
                             } catch (NullPointerException ee) {
                                 Log.e(TAG,"runTwitterSearch onError error updating SearchFragment, null: " + ee.toString());
                             }
                         }

                         @Override public void onNext(List<UserInfo> followers) {
                             if(BuildConfig.DEBUG) {
                                 Log.d(TAG, "In onNext()");
                             }

                                 try {
                                     mDataSet.addAll(followers);
                                 } catch (Exception e) {
                                     Log.e(TAG,"Exception trying to pull follower data... "  + e.toString());
                                 }

                         }
                     });
     }

     String maxIdString = "";
     if(maxId!=null) {
         maxIdString = String.valueOf(maxId);
     }
     searchSubscriptionCriteria = new String[]{"Twitter",query,queryOption,maxIdString};

 }

    /**
     * Searches the Edict dictionary database for a Kanji (in japanese text or romanized japanese, which is then
     *                    translated to japanese text before searching) or Definition (in english)
     * @param query query string from searchview
     * @param queryOption tag designating the search as either "Kanji" or "Definition". The search client will
     *                    run different searches depending on the option
     */
    public void runDictionarySearch(final String query, final String queryOption) {

        if(searchQuerySubscription !=null && !searchQuerySubscription.isUnsubscribed()) {
            searchQuerySubscription.unsubscribe();
        }

        searchQuerySubscription = Observable.fromCallable(new Callable<ArrayList<WordEntry>>() {
            @Override
            public ArrayList<WordEntry> call() throws Exception {
                return querytheDictionary(query.trim(),queryOption);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ArrayList<WordEntry>>() {
                    ArrayList<WordEntry> searchResults = new ArrayList<>();
                    @Override
                    public void onCompleted() {

                            //Update the search fragment recycler with search results
                            try {
                                ((SearchFragment) findFragmentByPosition(3).getChildFragmentManager().findFragmentByTag("searchFragment")).recieveDictionarySearchResults(searchResults);

                            } catch (NullPointerException e) {
                                Log.e(TAG,"searchquerysubscription onsuccess error updating SearchFragment, null: " + e.toString());
                            }
                        searchSubscriptionCriteria = null;
                        }

                    @Override
                    public void onError(Throwable e) {
                        if(BuildConfig.DEBUG) {
                            Log.e(TAG,"IN ON ERROR OF searchQuerySubscription in MAIN ACTIVITY");
                        }
                        //Hide the search bar
                        try {
                            ((SearchFragment) findFragmentByPosition(3).getChildFragmentManager().findFragmentByTag("searchFragment")).showProgressBar(false);
                        } catch (Exception ee) {
                            Log.e(TAG,"searchQuerySubscription in MAIN A inerror error hiding progress bar! generic exception: " + e.toString());

                        }
                        searchSubscriptionCriteria = null;

                    }

                    @Override
                    public void onNext(ArrayList<WordEntry> wordEntries) {
                        searchResults = wordEntries;
                    }
                });

        searchSubscriptionCriteria = new String[]{"Dictionary",query,queryOption,""};
    }


    /**
     * Checks whether a piece of text uses the roman alphabet.
     * @param query text to check
     * @return bool true if text is roman, false if not
     */
    public boolean isRomaji(String query) {
        Pattern ps = Pattern.compile("^[a-zA-Z0-9 ]+$");
        Matcher ms = ps.matcher(query.trim());
        return ms.matches();
    }

    /**
     * Queries the Edict dictionary, looking for matches for a query in the {@link SearchFragment} . Search can be
     * on Kanji, or on Definition. Kanji searches can also be in Japanese kanji or in English Romaji. If they are in Romaji, the
     * query must first be translated to Japanese before searching.
     * @param query search query
     * @param queryOption tag specifying what part of the dictionary to search on: "Kanji" or "Definition"
     * @return A list of possible matching WordEntries
     *
     */
    public ArrayList<WordEntry> querytheDictionary(String query, String queryOption) {
        StringBuilder FinalHiraganaKatakanaPlaceHolders = new StringBuilder();
        ArrayList<WordEntry> searchResults = new ArrayList<>();
        String idstopasson;


        /* If the Query is ROMAJI (i.e. romanized Japanese) it muset be converted to
         * the kanji equivalent of that query string before a search can be conducted */
        if(queryOption.equals("Kanji") && isRomaji(query)) {
            Pair<ArrayList<String>,ArrayList<String>> possibleJapaneseSearchOptions = convertRomajitoKanjiOptions(query,FinalHiraganaKatakanaPlaceHolders);
            idstopasson = InternalDB.getWordInterfaceInstance(getBaseContext()).getWordIdsForRomajiMatches(possibleJapaneseSearchOptions.first,possibleJapaneseSearchOptions.second);
        } else if(queryOption.equals("Kanji") && !query.contains(" ")){
            idstopasson = InternalDB.getWordInterfaceInstance(getBaseContext()).getWordIdsForKanjiMatch(query.trim());
        } else {
            idstopasson = InternalDB.getWordInterfaceInstance(getBaseContext()).getWordIdsForDefinitionMatch(query);
        }

        /* If we have a match, pull the edict info for those matching ids */
        if(idstopasson!=null && idstopasson.length()>0) {

            /* If we have at least on positive match, we pull further edict data from Edict
             * If there isn't, we can skip all this and just iterate through the list showing "item not found"s*/
            if(BuildConfig.DEBUG) {Log.d(TAG, "ids to pass on: " + idstopasson);}
            ColorThresholds colorThresholds = SharedPrefManager.getInstance(getBaseContext()).getColorThresholds();
            if(queryOption.equals("Kanji")) {
                searchResults = InternalDB.getWordInterfaceInstance(getBaseContext()).getSearchWordEntriesForKanji(FinalHiraganaKatakanaPlaceHolders.toString(),idstopasson,colorThresholds);
            } else {
                searchResults = InternalDB.getWordInterfaceInstance(getBaseContext()).getSearchWordEntriesForDefinition(idstopasson,colorThresholds);
            }


        }
        return searchResults;
    }

    /**
     * Converts Romanized queries in the searchview of the {@link SearchFragment} to Japanese queries so they can
     * be passed on to the querytheDictionary method. It also does dual duty by filling the FinalHiraganaKatakanaPlaceHolders stringbuilder
     * with a "?" for each word that will be matched against the dictionary.
     * @param query Romanji query string
     * @param FinalHiraganaKatakanaPlaceHolders stringbuilder concatenating a collection of "?", one for each possible word that will be
     *                                          matched against the database.
     * @return A pair of arraylists, one for Hiragana translations of the romanji, the other for Katakana translations. Both must
     * be matched against the dictionary
     */
    public Pair<ArrayList<String>, ArrayList<String>> convertRomajitoKanjiOptions(String query, StringBuilder FinalHiraganaKatakanaPlaceHolders) {
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
                    if(BuildConfig.DEBUG){Log.d(TAG,"QUERYCHUNK: " + querychunk);}

                    /*If it's a 3 char double like: ppu --> っぷ , throw in the っ character, unless it's a "nn"*/
                    if(iterator==2 && String.valueOf(querychunk.charAt(0)).equals(String.valueOf(querychunk.charAt(1))) && !String.valueOf(querychunk.charAt(0)).equals("n")) {

                        //an even number, meaning its a hiragana
                        StringBuilder matchBuilder = new StringBuilder();
                        ArrayList<String> newPossibleHiraganaSearchQueries = new ArrayList<>();
                        if(possibleHiraganaSearchQueries.size()>0){
                            for(int ii=0;ii<possibleHiraganaSearchQueries.size();ii++){
                                matchBuilder.append(possibleHiraganaSearchQueries.get(ii));
                                matchBuilder.append("っ");
                                newPossibleHiraganaSearchQueries.add(matchBuilder.toString());
                                if(BuildConfig.DEBUG){Log.d(TAG,"(1)Appending hiragana chunk: " + possibleHiraganaSearchQueries.get(ii) + " + " + matchBuilder.toString());}
                                matchBuilder.setLength(0);
                            }
                            possibleHiraganaSearchQueries = new ArrayList<>(newPossibleHiraganaSearchQueries);

                        } else{
                            if(BuildConfig.DEBUG){Log.d(TAG,"Adding first hiragana chunk: " + "っ");}
                            possibleHiraganaSearchQueries.add("っ");
                        }

                        //an odd number, meaning its a Katakana
                        StringBuilder matchBuilder2 = new StringBuilder();
                        ArrayList<String> newPossibleKatakanaSearchQueries = new ArrayList<>();
                        if(possibleKatakanaSearchQueries.size()>0){
                            for(int ii=0;ii<possibleKatakanaSearchQueries.size();ii++){
                                matchBuilder2.append(possibleKatakanaSearchQueries.get(ii));
                                matchBuilder2.append("ッ");
                                newPossibleKatakanaSearchQueries.add(matchBuilder2.toString());
                                if(BuildConfig.DEBUG){Log.d(TAG,"(1)Appending katakana chunk: " + possibleKatakanaSearchQueries.get(ii) + " + " + matchBuilder2.toString());}
                                matchBuilder2.setLength(0);
                            }
                            possibleKatakanaSearchQueries = new ArrayList<>(newPossibleKatakanaSearchQueries);

                        } else{
                            if(BuildConfig.DEBUG){Log.d(TAG,"Adding first katakana chunk: ッ");}
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
                                if(BuildConfig.DEBUG){Log.d(TAG,"Current Furigana Option: " + currentFuriganaOption);}
                                if((i%2)==0){
                                    //an even number, meaning its a hiragana
                                    StringBuilder matchBuilder = new StringBuilder();
                                    ArrayList<String> newPossibleHiraganaSearchQueries = new ArrayList<>();
                                    if(possibleHiraganaSearchQueries.size()>0){
                                        for(int ii=0;ii<possibleHiraganaSearchQueries.size();ii++){
                                            matchBuilder.append(possibleHiraganaSearchQueries.get(ii));
                                            matchBuilder.append(currentFuriganaOption);
                                            newPossibleHiraganaSearchQueries.add(matchBuilder.toString());
                                            if(BuildConfig.DEBUG){Log.d(TAG,"Appending hiragana chunk: " + possibleHiraganaSearchQueries.get(ii) + " + " + matchBuilder.toString());}
                                            matchBuilder.setLength(0);
                                        }
                                        possibleHiraganaSearchQueries = new ArrayList<>(newPossibleHiraganaSearchQueries);

                                    } else{
                                        if(BuildConfig.DEBUG){Log.d(TAG,"Adding first hiragana chunk: " + currentFuriganaOption);}
                                        possibleHiraganaSearchQueries.add(currentFuriganaOption);
                                    }

                                } else {
                                    //an odd number, meaning its a Katakana
                                    StringBuilder matchBuilder = new StringBuilder();
                                    ArrayList<String> newPossibleKatakanaSearchQueries = new ArrayList<>();
                                    if(possibleKatakanaSearchQueries.size()>0){
                                        for(int ii=0;ii<possibleKatakanaSearchQueries.size();ii++){
                                            matchBuilder.append(possibleKatakanaSearchQueries.get(ii));
                                            matchBuilder.append(currentFuriganaOption);
                                            newPossibleKatakanaSearchQueries.add(matchBuilder.toString());
                                            if(BuildConfig.DEBUG){Log.d(TAG, "Appending katakana chunk: " + possibleKatakanaSearchQueries.get(ii) + " + " + matchBuilder.toString());}

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
                                        possibleKatakanaSearchQueries = new ArrayList<>(newPossibleKatakanaSearchQueries);

                                    } else{
                                        if(BuildConfig.DEBUG){Log.d(TAG,"Adding first katakana chunk: " + currentFuriganaOption);}
                                        possibleKatakanaSearchQueries.add(currentFuriganaOption);
                                    }

                                }
                            }

                        } else {
                            if(BuildConfig.DEBUG){Log.d(TAG,"iterator subtract (1)");}
                            iterator -= 1;

                        }

                    }

                }

                if(!foundone) {
                    if(BuildConfig.DEBUG){Log.d(TAG,"nothing found, moving 3 char ahead");}
                    iterator = 3;
                    startposition = (startposition + 3);

                } else {
                    if(BuildConfig.DEBUG){
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
                if(BuildConfig.DEBUG){Log.d(TAG,"iterator at end of loop: " + iterator);}
            }

            if(BuildConfig.DEBUG){

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

        /*Adding hiragana entries to the katakana ones, so we can look for hiragana and katakan ones in db all at once baby*/
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

        return new Pair<>(possibleHiraganaSearchQueries,possibleKatakanaSearchQueries);
    }


    /**
     * Runs the {@link TweetParser} and saves the resulting tweets into the database
     * @param tweet Tweet that will be parsed
     */
    public void parseAndSaveTweet(final Tweet tweet) {

        if(currentParsingTweetBackLog == null) {
            currentParsingTweetBackLog = new ArrayList<>();
        }

        //IF the tweet is not already being parsed
        if(!currentParsingTweetBackLog.contains(tweet.getIdString())) {
            currentParsingTweetBackLog.add(tweet.getIdString());

            Single.fromCallable(new Callable<ArrayList<WordEntry>>() {
                @Override
                public ArrayList<WordEntry> call() throws Exception {

                    final ArrayList<String> spansToExclude = new ArrayList<>();

                    if(tweet.getEntities().getUrls() != null) {
                        for(TweetUrl url : tweet.getEntities().getUrls()) {
                            if(url != null) {
                                spansToExclude.add(url.getUrl());
                            }
                        }

                    }

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
                            if(currentParsingTweetBackLog!=null && currentParsingTweetBackLog.contains(tweet.getIdString())) {
                                currentParsingTweetBackLog.remove(tweet.getIdString());
                            }
                            InternalDB.getTweetInterfaceInstance(getBaseContext()).saveParsedTweetKanji(disectedTweet,tweet.getIdString());
                            notifySavedTweetFragmentsChanged();
                        }

                        @Override
                        public void onError(Throwable error) {
                            if(currentParsingTweetBackLog!=null && currentParsingTweetBackLog.contains(tweet.getIdString())) {
                                currentParsingTweetBackLog.remove(tweet.getIdString());
                            }
                            Log.e(TAG,"ERROR IN PARSE KANJI (for saved tweet) OBSERVABLE: " + error);
                        }
                    });
        }

    }


    @Override
    protected void onDestroy() {

        if(searchQuerySubscription!=null) {
            searchQuerySubscription.unsubscribe();
        }
        if(userInfoSubscription!=null) {
            userInfoSubscription.unsubscribe();
        }
        InternalDB.getInstance(getBaseContext()).close();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if(searchQuerySubscription!=null) {
            searchQuerySubscription.unsubscribe();
        }
        if(userInfoSubscription!=null) {
            userInfoSubscription.unsubscribe();
        }
        super.onPause();
    }

    /**
     * Downloads and saves icons for tweets which are not attached to one of the saved users in the {@link com.jukuproject.jukutweet.Fragments.UserListFragment}.
     * @param userInfo UserInfo object for user whose icon will be downloaded
     *
     * @see com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter
     * @see SearchFragment
     * @see com.jukuproject.jukutweet.Fragments.UserTimeLineFragment
     * @see com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog#runTwitterSearch(String)
     */
    public void downloadTweetUserIcons(UserInfo userInfo) {
        InternalDB.getUserInterfaceInstance(getBaseContext()).downloadTweetUserIcon(getBaseContext(),userInfo.getProfileImageUrl(),userInfo.getUserId());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(BuildConfig.DEBUG){Log.d(TAG,"in main activity on saved instance out state...");}

        if(findFragmentByPosition(0) != null
                && findFragmentByPosition(0) instanceof Tab1Container) {
            getSupportFragmentManager().putFragment(outState, "tab1Container", findFragmentByPosition(0));
        }
        if(findFragmentByPosition(1) != null
                && findFragmentByPosition(1) instanceof Tab2Container) {
            getSupportFragmentManager().putFragment(outState, "tab2Container", findFragmentByPosition(1));
        }
        if(findFragmentByPosition(2) != null
                && findFragmentByPosition(2) instanceof Tab3Container) {
            getSupportFragmentManager().putFragment(outState, "tab3Container", findFragmentByPosition(2));
        }
        if(findFragmentByPosition(3) != null
                && findFragmentByPosition(3) instanceof Tab3Container) {
            getSupportFragmentManager().putFragment(outState, "tab4Container", findFragmentByPosition(3));
        }

        outState.putStringArray("adapterTitles", mAdapterTitles);
        outState.putBooleanArray("tabsShowingBrowseMenu",tabsShowingBrowseMenu);
        outState.putStringArray("tabsShowingBackButton",tabsShowingBackButton);

        if(searchQuerySubscription!=null && searchSubscriptionCriteria != null) {
            outState.putStringArray("searchSubscriptionCriteria",searchSubscriptionCriteria);
        }
    }
}
