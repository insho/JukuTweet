package com.jukuproject.jukutweet;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
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
import com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment;
import com.jukuproject.jukutweet.Fragments.FlashCardsFragment;
import com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment;
import com.jukuproject.jukutweet.Fragments.MyListBrowseFragment;
import com.jukuproject.jukutweet.Fragments.SavedTweetsBrowseFragment;
import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.TabContainers.Tab1Container;
import com.jukuproject.jukutweet.TabContainers.Tab2Container;
import com.jukuproject.jukutweet.TabContainers.Tab3Container;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

//import com.jukuproject.jukutweet.TabContainers.TabContainer;

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
//    private AddUserDialog addUserDialogFragment;
//    private AddUserCheckDialog addUserCheckDialogFragment;
//    private RemoveUserDialog removeUserDialogFragment;
//    private AddOrRenameMyListDialog addOrRenameMyListDialogFragment;
//    private EditMyListDialog editMyListDialogFragment;
    private SmoothProgressBar progressbar;
    private FloatingActionButton fab;
    private Menu mMenu;
    private static final String TAG = "TEST-Main";
    private static final boolean debug = true;

    private boolean fragmentWasChanged = false;

    /*Tracks which tab is currently visible, necessary
     * because savedtweets (tab2) and mylistsbrowse (tab3)
     * both have the same group of menu buttons. So on menu item
      * click the visible tab helps decide which */
//    private int visibleTabBucket = 1;

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

        // Create the adapter that will return a fragment for each of theprimary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),new String[]{"Users","Saved Tweets","My Lists"});
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(fragmentWasChanged) {
                    mSectionsPagerAdapter.notifyDataSetChanged();
                    fragmentWasChanged = false;
                }

                switch (position) {
                    case 0:
                        if(isTopShowing()) {
                            showFab(true,"addUser");
                        } else {
                            showFab(false);
                        }
                        break;
                    case 2:
                        if(isTopShowing()) {
                            showFab(true,"addMyList");
                        }
                        break;
                    default:
                        showFab(false);
                        break;
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
                   if(mViewPager.getCurrentItem() == 0 && isTopShowing()) {
                        showAddUserDialog();
                    }
                   else if(mViewPager.getCurrentItem() == 2 && isTopShowing()) {
                       showAddMyListDialog();
//                   InternalDB.getInstance(getBaseContext()).TESTKANJI();
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


    public void showMenuMyListBrowse(boolean show){

        if(mMenu == null) {
            return;
        }
        mMenu.setGroupVisible(R.id.menu_main_group, !show);
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
            case R.id.action_cancel:
                try {
                    if(findFragmentByPosition(2) != null
                            && findFragmentByPosition(2) instanceof Tab3Container
                            && ((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse") != null
                            && ((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse").isVisible()) {
                        ((MyListBrowseFragment)((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse")).deselectAll();
                    } else if(findFragmentByPosition(1) != null
                            && findFragmentByPosition(1) instanceof Tab2Container
                            && ((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse") != null
                            && ((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse").isVisible()) {
                        ((SavedTweetsBrowseFragment)((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse")).deselectAll();
                    }

                } catch (NullPointerException e) {
                    Log.e(TAG,"TabContainer->BrowseFragment deselectALL Nullpointer: " + e.toString());
                } catch (Exception e) {
                    Log.e(TAG,"TabContainer->BrowseFragment  deselectALL error: " + e.toString());
                }

                //Hide the action icons
                showMenuMyListBrowse(false);

                break;
            case R.id.action_copy:

                try {
                    if(findFragmentByPosition(2) != null
                            && findFragmentByPosition(2) instanceof Tab3Container
                            && ((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse") != null
                            && ((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse").isVisible()) {
                        ((MyListBrowseFragment)((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse")).showCopyMyListDialog();
                    } else if(findFragmentByPosition(1) != null
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


//                if(findFragmentByPosition(2) != null && findFragmentByPosition(2) instanceof Tab3Container) {
//                    try {
//                        ((MyListBrowseFragment)((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse")).showCopyMyListDialog();
//
//                    } catch (NullPointerException e) {
//                        Log.e(TAG,"Tab3Container->MyListBrowseFragment copy Nullpointer: " + e.toString());
//                    } catch (Exception e) {
//                        Log.e(TAG,"Tab3Container->MyListBrowseFragment copy error: " + e.toString());
//                    }
//
//                }
                break;
            case R.id.action_delete:
                try {
                    if(findFragmentByPosition(2) != null
                            && findFragmentByPosition(2) instanceof Tab3Container
                            && ((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse") != null
                            && ((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse").isVisible()) {
                        ((MyListBrowseFragment)((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse")).removeKanjiFromList();
                    } else if(findFragmentByPosition(1) != null
                            && findFragmentByPosition(1) instanceof Tab2Container
                            && ((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse") != null
                            && ((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse").isVisible()) {
                        ((SavedTweetsBrowseFragment)((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsbrowse")).removeTweetFromList();
                    }

                } catch (NullPointerException e) {
                    Log.e(TAG,"TabContainer->BrowseFragment delete Nullpointer: " + e.toString());
                } catch (Exception e) {
                    Log.e(TAG,"TabContainer->BrowseFragment  delete error: " + e.toString());
                }

            //Hide the action icons
            showMenuMyListBrowse(false);

                break;
            case R.id.action_selectall:


                try {
                    if(findFragmentByPosition(2) != null
                            && findFragmentByPosition(2) instanceof Tab3Container
                            && ((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse") != null
                            && ((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse").isVisible()) {
                        ((MyListBrowseFragment)((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse")).selectAll();
                    } else if(findFragmentByPosition(1) != null
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



                if(findFragmentByPosition(2) != null && findFragmentByPosition(2) instanceof Tab3Container) {
                    //If the open fragment is MyListBrowse
                    try {
                        ((MyListBrowseFragment)((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse")).selectAll();
                    } catch (NullPointerException e) {
                        Log.e(TAG,"Tab3Container->MyListBrowseFragment selectALL Nullpointer: " + e.toString());
                    } catch (Exception e) {
                        Log.e(TAG,"Tab3Container->MyListBrowseFragment selectALL error: " + e.toString());
                    }

                } else if(findFragmentByPosition(1) != null && findFragmentByPosition(1) instanceof Tab2Container) {
                    //If the open fragment is SavedTweetsBrowse
                    try {
                        ((SavedTweetsBrowseFragment)((Tab2Container) findFragmentByPosition(1)).getChildFragmentManager().findFragmentByTag("savedtweetsallfragment")).selectAll();
                    } catch (NullPointerException e) {
                        Log.e(TAG,"Tab2Container->SavedTweetsBrowseFragment selectALL Nullpointer: " + e.toString());
                    } catch (Exception e) {
                        Log.e(TAG,"Tab2Container->SavedTweetsBrowseFragment selectALL error: " + e.toString());
                    }

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
//        if (addUserDialogFragment == null || !addUserDialogFragment.isAdded()) {
//            addUserDialogFragment = AddUserDialog.newInstance();
//            addUserDialogFragment.show(getFragmentManager(), "dialogAdd");

            if(getFragmentManager().findFragmentByTag("dialogAdd") == null || !getFragmentManager().findFragmentByTag("dialogAdd").isAdded()) {
                AddUserDialog.newInstance().show(getSupportFragmentManager(),"dialogAdd");
        }
    }


    public void showAddUserCheckDialog(UserInfo userInfo){

        if(getFragmentManager().findFragmentByTag("dialogAddCheck") == null || !getFragmentManager().findFragmentByTag("dialogAddCheck").isAdded()) {
            AddUserCheckDialog.newInstance(userInfo).show(getSupportFragmentManager(),"dialogAddCheck");
        }

//        if (addUserCheckDialogFragment == null || !addUserCheckDialogFragment.isAdded()) {
//            Log.d(TAG,"loading addusercheck");
//            addUserCheckDialogFragment = AddUserCheckDialog.newInstance(userInfo);
//            addUserCheckDialogFragment.show(getFragmentManager(), "dialogAddCheck");
//        }
    }


//    public void showAddUserCheckDialog(){
//        Log.d(TAG,"SHOWING add user check: " + (addUserCheckDialogFragment != null) + ", " +!addUserCheckDialogFragment.isAdded() );
//        if (addUserCheckDialogFragment != null && !addUserCheckDialogFragment.isAdded()) {
//            addUserCheckDialogFragment.show(getFragmentManager(), "dialogAddCheck");
//        }
//    }


    /**
     * Recieves input text from add user dialog
     * Checks if that user already exists in database
     * If not, inputs user into db and updates mainFragment recycler
     * @param inputText
     */
    @Override
    public void onAddUserDialogPositiveClick(String inputText) {

        /** Check to DB to see if the new feed is a duplicate*/
        if(InternalDB.getInstance(getBaseContext()).duplicateUser(inputText.trim())) {
            Toast.makeText(this, "UserInfo already exists", Toast.LENGTH_SHORT).show();
        } else if (!isOnline()) {
            Toast.makeText(getBaseContext(), "Unable to access internet", Toast.LENGTH_SHORT).show();
            } else {
            tryToGetUserInfo(inputText.trim());
        }
    }

    /**
     * Sets add and remove dialog fragments to null, in order to avoid multiple instances of the fragment
     * if the user repeatedly clicks the fab
     */
    @Override
    public void onDialogDismiss() {
//        addUserDialogFragment = null;
//        removeUserDialogFragment = null;
//        addOrRenameMyListDialogFragment = null;
//        editMyListDialogFragment = null;
//        addUserCheckDialogFragment = null;
    }

    /**r
     * Shows remove UserInfo Dialog
     * @param user UserInfo to "unfollow" (i.e. remove from database)
     */
    public void showRemoveUserDialog(String user) {
//        if (removeUserDialogFragment == null) {
//            removeUserDialogFragment = RemoveUserDialog.newInstance(user);
//            removeUserDialogFragment.show(getFragmentManager(), "dialogRemove");
//        }
        if(getFragmentManager().findFragmentByTag("dialogRemove") == null || !getFragmentManager().findFragmentByTag("dialogRemove").isAdded()) {
            RemoveUserDialog.newInstance(user).show(getSupportFragmentManager(),"dialogRemove");
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

        if (InternalDB.getInstance(getBaseContext()).deleteUser(screenName) ) {

            Log.d(TAG,"findfragbypos: " + findFragmentByPosition(0));

            // Locate Tab1Continer and update the UserListInfo adapter to reflect removed item
            if(findFragmentByPosition(0) != null && findFragmentByPosition(0) instanceof Tab1Container) {
                ((Tab1Container) findFragmentByPosition(0)).updateUserListFragment();
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
     * @param screenName
     */
    public void tryToGetUserInfo(final String screenName) {

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
                                showAddUserCheckDialog(userInfoInstance);
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


    public void saveAndUpdateUserInfoList(UserInfo userInfoInstance) {
        if(InternalDB.getInstance(getBaseContext()).saveUser(userInfoInstance)) {

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
    //save image
//    public static void imageDownload(Context ctx, String url){
//        Picasso.with(ctx)
//                .load("http://blog.concretesolutions.com.br/wp-content/uploads/2015/04/Android1.png")
//                .into(getTarget(url));
//    }
//
//    //target to save
//    private static Target getTarget(final String url){
//        Target target = new Target(){
//
//            @Override
//            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
//                new Thread(new Runnable() {
//
//                    @Override
//                    public void run() {
//
//                        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + url);
//                        try {
//                            file.createNewFile();
//                            FileOutputStream ostream = new FileOutputStream(file);
//                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
//                            ostream.flush();
//                            ostream.close();
//                        } catch (IOException e) {
//                            Log.e("IOException", e.getLocalizedMessage());
//                        }
//                    }
//                }).start();
//
//            }
//
//            @Override
//            public void onBitmapFailed(Drawable errorDrawable) {
//
//            }
//
//            @Override
//            public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//            }
//        };
//        return target;
//    }

    public File checkForImagePath(String title) {
//        String appDirectoryName = "JukuTweetUserIcons";
//
//        File imageRoot = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES), appDirectoryName);
//        if(imageRoot.exists() && imageRoot.isDirectory()) {
//            // do something here
//
//        }
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("JukuTweetUserIcons", Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }
        return new File(directory, title + ".png");

    }

    public void downloadUserIcon(String imageUrl, final String screenName) {
//        success = false;
//
//
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream(mypath);
//            resizedbitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
//            fos.close();
//        } catch (Exception e) {
//            Log.e("SAVE_IMAGE", e.getMessage(), e);
//        }
        Picasso.with(getBaseContext()).load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

//                        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + url);
                        try {
                            File file = checkForImagePath(screenName);
                            if(!file.exists()) {
                                FileOutputStream ostream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                                ostream.flush();
                                ostream.close();

                                //TODO == clearer way of saving this image to a file...
                                Uri uri = Uri.fromFile(file);
                                InternalDB.getInstance(getBaseContext()).addMediaURItoDB(uri.toString(),screenName);
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

//        return  success;
//        Picasso.with(getBaseContext()).load(imageUrl).into(new TargetPhoneGallery(getContentResolver(), rowID, rssList.getTitle(), getBaseContext()));
    }



    @Override
    public void onBackPressed() {
        boolean isPopFragment = false;
        if(fragmentWasChanged) {
            mSectionsPagerAdapter.notifyDataSetChanged();
            fragmentWasChanged = false;
        }
        //Pop backstack depending on the overall tab position (if applicable)
        switch (mViewPager.getCurrentItem()) {
            case 0:
                isPopFragment = ((BaseContainerFragment)findFragmentByPosition(0)).popFragment();
                try {
                    boolean isTopShowing = ((Tab1Container)findFragmentByPosition(0)).isTopFragmentShowing();
                    if(isTopShowing) {
                        showActionBarBackButton(false,getString(R.string.app_name));
                        updateTabs(new String[]{"Users","Saved Tweets","My Lists"});

                    }
                } catch (NullPointerException e) {
                    Log.e(TAG,"OnBackPressed child entrycount null : " + e);
                }
                    break;
            case 1:
                isPopFragment = ((BaseContainerFragment)findFragmentByPosition(1)).popFragment();
                try {
                    boolean isTopShowing = ((Tab2Container)findFragmentByPosition(1)).isTopFragmentShowing();
                    if(isTopShowing) {
                        showActionBarBackButton(false,getString(R.string.app_name));
                        updateTabs(new String[]{"Users","Saved Tweets","My Lists"});
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG,"OnBackPressed child entrycount null : " + e);
                }
                break;
            case 2:
                isPopFragment = ((BaseContainerFragment)findFragmentByPosition(2)).popFragment();
                try {
                    boolean isTopShowing = ((Tab3Container)findFragmentByPosition(2)).isTopFragmentShowing();
                    if(isTopShowing) {
                        showActionBarBackButton(false,getString(R.string.app_name));
                        updateTabs(new String[]{"Users","Saved Tweets","My Lists"});
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG,"OnBackPressed child entrycount null : " + e);
                }
                break;
            default:
                break;
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

    public boolean isOnline() {
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
        }

    }

//    public void changePagerTitle(int position, String title) {
//        if(mSectionsPagerAdapter != null) {
//            mSectionsPagerAdapter.updateTitleData(title);
//        }
//    }


    public void showAddMyListDialog(){
//        if (addOrRenameMyListDialogFragment == null || !addOrRenameMyListDialogFragment.isAdded()) {
//            addOrRenameMyListDialogFragment = AddOrRenameMyListDialog.newInstance();
//            addOrRenameMyListDialogFragment.show(getFragmentManager(), "dialogAddMyList");
//        }

        if(getFragmentManager().findFragmentByTag("dialogAddMyList") == null || !getFragmentManager().findFragmentByTag("dialogAddMyList").isAdded()) {
            AddOrRenameMyListDialog.newInstance().show(getSupportFragmentManager(),"dialogAddMyList");
        }

    }

    public void showRenameMyListDialog(String oldListName){
        if(getFragmentManager().findFragmentByTag("dialogAddMyList") == null || !getFragmentManager().findFragmentByTag("dialogAddMyList").isAdded()) {
            AddOrRenameMyListDialog.newInstance(oldListName).show(getSupportFragmentManager(),"dialogAddMyList");
        }

//        if (addOrRenameMyListDialogFragment == null || !addOrRenameMyListDialogFragment.isAdded()) {
//            addOrRenameMyListDialogFragment = AddOrRenameMyListDialog.newInstance(oldListName);
//            addOrRenameMyListDialogFragment.show(getFragmentManager(), "dialogAddMyList");
//        }
    }

    public void showEditMyListDialog(String currentListName, Boolean isStarFavorite){
        if(getFragmentManager().findFragmentByTag("dialogEditMyList") != null && !getFragmentManager().findFragmentByTag("dialogEditMyList").isAdded()) {
            EditMyListDialog.newInstance(currentListName, isStarFavorite).show(getSupportFragmentManager(),"dialogEditMyList");
        }

//        if (editMyListDialogFragment == null || !editMyListDialogFragment.isAdded()) {
//            editMyListDialogFragment = EditMyListDialog.newInstance(currentListName, isStarFavorite);
//            editMyListDialogFragment.show(getFragmentManager(), "dialogEditMyList");
//        }
    }

    /**
     * Recieves input text from add user dialog
     * Checks if that user already exists in database
     * If not, inputs user into db and updates mainFragment recycler
     * @param inputText
     */
    @Override
    public void onAddMyListDialogPositiveClick(String inputText) {
        if(InternalDB.getInstance(getBaseContext()).saveMyList(inputText)) {
            /* Locate Tab2Continer and update the MyList adapter to reflect removed item */
            if(findFragmentByPosition(1) != null && findFragmentByPosition(1) instanceof Tab2Container) {
                ((Tab3Container) findFragmentByPosition(1)).updateMyListFragment();
            }
        }

    };


    /**
     * asdf
     * selectedItem options:
     * 1 = clear list
     * 2 = rename list
     * 3 = delete list
     *
     * @param selectedItem
     * @param listName
     */
    @Override
    public void onEditMyListDialogPositiveClick(int selectedItem, String listName, boolean isStarFavorite) {

        switch (selectedItem){
            //Clear list
            case 1:
                deleteOrClearDialogFinal(false,listName,isStarFavorite);
                break;
            //Rename list
            case 2:
                showRenameMyListDialog(listName);
                break;
            //Remove list
            case 3:
                deleteOrClearDialogFinal(true,listName,isStarFavorite);
                break;

        }

    };

    @Override
    public void onRenameMyListDialogPositiveClick(String oldListName, String listName) {
        if(InternalDB.getInstance(getBaseContext()).renameMyList(oldListName,listName)) {
            if(findFragmentByPosition(2) != null && findFragmentByPosition(2) instanceof Tab2Container) {
                ((Tab3Container) findFragmentByPosition(2)).updateMyListFragment();
            }
        } else {
            Toast.makeText(this, "Unable to rename list", Toast.LENGTH_SHORT).show();
        }
    };

    public void deleteOrClearDialogFinal(final Boolean delete,final String name, final boolean isStarFavorite) {

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
//        text.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));

        text.setGravity(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(text);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(delete) {
                    InternalDB.getInstance(getBaseContext()).deleteMyList(name);
                } else {
                    InternalDB.getInstance(getBaseContext()).clearMyList(name,isStarFavorite);
                }

                /* Locate Tab2Continer and update the MyList adapter to reflect removed item */
                if(findFragmentByPosition(2) != null && findFragmentByPosition(2) instanceof Tab2Container) {
                    ((Tab3Container) findFragmentByPosition(2)).updateMyListFragment();
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

    public boolean isTopShowing() {
        switch (mViewPager.getCurrentItem()) {
            case 0:
                return ((Tab1Container)findFragmentByPosition(0)).isTopFragmentShowing();
            case 1:
                break;
            case 2:
                return ((Tab3Container)findFragmentByPosition(2)).isTopFragmentShowing();
            default:
                break;
        }

        return true;

    }

    public void showFab(boolean show, String type) {
        try {
            if(show && type.equals("addUser")) {
                fab.setVisibility(View.VISIBLE);
                fab.setImageResource(R.drawable.ic_add_white_24dp);
            } else if(show && type.equals("addMyList")) {
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

    public void updateTabs(String[] updatedTabs) {

        if(mSectionsPagerAdapter != null) {
            mSectionsPagerAdapter.updateTabs(updatedTabs);
        }
    }

    //Traffic control from CopyDialog to BrowseItemsFragment
    public void saveAndUpdateMyLists(String kanjiIdString, ArrayList<MyListEntry> listsToCopyTo, boolean move, MyListEntry currentList) {
        if(findFragmentByPosition(2) != null && findFragmentByPosition(2) instanceof Tab3Container) {
            try {
                ((MyListBrowseFragment)((Tab3Container) findFragmentByPosition(2)).getChildFragmentManager().findFragmentByTag("mylistbrowse")).saveAndUpdateMyLists(kanjiIdString,listsToCopyTo,move,currentList);

            } catch (NullPointerException e) {
                Log.e(TAG,"Tab3Container->MyListBrowseFragment saveAndUpdateMyLists Nullpointer: " + e.toString());
            } catch (Exception e) {
                Log.e(TAG,"Tab3Container->MyListBrowseFragment saveAndUpdateMyLists error: " + e.toString());
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
            ArrayList<WordEntry> dataset = InternalDB.getInstance(getBaseContext())
                    .getMyListWords("Tweet"
                            ,listEntry
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
            ArrayList<WordEntry> dataset = InternalDB.getInstance(getBaseContext())
                    .getMyListWords("Word"
                            ,listEntry
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

    public void showMultipleChoiceFragment(int tabNumber
            , MyListEntry listEntry
            , String quizType
            , String quizSize
            , String quizTimer
            ,String selectedColorString) {

        Integer timer = -1;
        if (!quizTimer.equals("None")) {
            timer = Integer.parseInt(quizTimer);
        }

        ColorThresholds colorThresholds = SharedPrefManager.getInstance(getApplicationContext()).getColorThresholds();

        if (tabNumber == 1) {
            //Its a mylist fragment
            ArrayList<WordEntry> dataset = InternalDB.getInstance(getBaseContext())
                    .getMyListWords("Tweet"
                            , listEntry
                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            , selectedColorString
                            , null
                            , Integer.parseInt(quizSize));

            double totalweight = assignWordWeightsAndGetTotalWeight(dataset);

            if (findFragmentByPosition(tabNumber) != null
                    && findFragmentByPosition(tabNumber) instanceof Tab2Container) {

                MultipleChoiceFragment multipleChoiceFragment = MultipleChoiceFragment.newInstance(dataset
                        , quizType
                        , timer
                        , Integer.parseInt(quizSize)
                        , totalweight
                        , "Tweet"
                        , selectedColorString
                        , listEntry);
                ((BaseContainerFragment) findFragmentByPosition(tabNumber)).replaceFragment(multipleChoiceFragment, true, "multiplechoice");
            }
        } else if (tabNumber == 2) {
            //Its a mylist fragment
            ArrayList<WordEntry> dataset = InternalDB.getInstance(getBaseContext())
                    .getMyListWords("Word"
                            , listEntry
                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            , selectedColorString
                            , null
                            , Integer.parseInt(quizSize));

            double totalweight = assignWordWeightsAndGetTotalWeight(dataset);

            if (findFragmentByPosition(tabNumber) != null
                    && findFragmentByPosition(tabNumber) instanceof Tab3Container) {

                MultipleChoiceFragment multipleChoiceFragment = MultipleChoiceFragment.newInstance(dataset
                        , quizType
                        , timer
                        , Integer.parseInt(quizSize)
                        , totalweight
                        , "Word"
                        , selectedColorString
                        , listEntry);
                ((BaseContainerFragment) findFragmentByPosition(tabNumber)).replaceFragment(multipleChoiceFragment, true, "multiplechoice");

            }
        }

    }

    public void showFillintheBlanksFragment(int tabNumber
            , MyListEntry myListEntry
            , String quizSize
            ,String selectedColorString) {

        ColorThresholds colorThresholds = SharedPrefManager.getInstance(getApplicationContext()).getColorThresholds();

        if (tabNumber == 1) {


            //The request is coming from the saved tweets fragment
            //The request is coming from the saved words fragment
            ArrayList<Tweet> dataset = InternalDB.getInstance(getBaseContext())
                    .getFillintheBlanksTweetsForATweetList(myListEntry
                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            , selectedColorString
                            , Integer.parseInt(quizSize));

            double totalweight = assignTweetWeightsAndGetTotalWeight(dataset);

            if (findFragmentByPosition(tabNumber) != null
                    && findFragmentByPosition(tabNumber) instanceof Tab2Container) {


                FillInTheBlankFragment fillInTheBlankFragment = FillInTheBlankFragment.newInstance(dataset
                        , Integer.parseInt(quizSize)
                        , totalweight
                        , selectedColorString
                        , myListEntry);
                ((BaseContainerFragment) findFragmentByPosition(tabNumber)).replaceFragment(fillInTheBlankFragment, true, "fillintheblanks");

            }
        } else if (tabNumber == 2) {

            //The request is coming from the saved words fragment
           ArrayList<Tweet> dataset = InternalDB.getInstance(getBaseContext())
                    .getFillintheBlanksTweetsForAWordList(myListEntry
                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            , selectedColorString
                            , Integer.parseInt(quizSize));


            double totalweight = assignTweetWeightsAndGetTotalWeight(dataset);

            if (findFragmentByPosition(tabNumber) != null
                    && findFragmentByPosition(tabNumber) instanceof Tab3Container) {


                FillInTheBlankFragment fillInTheBlankFragment = FillInTheBlankFragment.newInstance(dataset
                        , Integer.parseInt(quizSize)
                        , totalweight
                        , selectedColorString
                        , myListEntry);
                ((BaseContainerFragment) findFragmentByPosition(tabNumber)).replaceFragment(fillInTheBlankFragment, true, "fillintheblanks");

            }
        }

    }






//        ArrayList<WordEntry> wordEntries = InternalDB.getInstance(getBaseContext()).fillMultipleChoiceKeyPool(tabNumber,listEntry,quizType,colorThresholds,selectedColorString);
//
//        if(tabNumber == 1) {
//            //Its a mylist fragment
//            ArrayList<WordEntry> dataset = InternalDB.getInstance(getBaseContext())
//                    .getMyListWords("Tweet",listEntry,SharedPrefManager.getInstance(getBaseContext()).getColorThresholds(),selectedColorString);
//
//            if(findFragmentByPosition(tabNumber) != null
//                    && findFragmentByPosition(tabNumber) instanceof Tab2Container) {
//
//                FlashCardsFragment flashCardsFragment = FlashCardsFragment.newInstance(dataset,frontValue,backValue);
//                ((BaseContainerFragment)findFragmentByPosition(tabNumber)).replaceFragment(flashCardsFragment,true,"flashcards");
//            }
//        } else if(tabNumber == 2) {
//            //Its a mylist fragment
//            ArrayList<WordEntry> dataset = InternalDB.getInstance(getBaseContext())
//                    .getMyListWords("Word",listEntry,SharedPrefManager.getInstance(getBaseContext()).getColorThresholds(),selectedColorString);
//
//            if(findFragmentByPosition(tabNumber) != null
//                    && findFragmentByPosition(tabNumber) instanceof Tab3Container) {
//
////                Log.d(TAG,"kanji: " + dataset.get(0).getKanji() + ", furigana: " + dataset.get(0).getFurigana());
//                //Initialize Flashcards fragment
//                FlashCardsFragment flashCardsFragment = FlashCardsFragment.newInstance(dataset,frontValue,backValue);
//                //Replace the current fragment bucket with flashcards
//                ((BaseContainerFragment)findFragmentByPosition(tabNumber)).replaceFragment(flashCardsFragment,true,"flashcards");
//
//            }
//        }

//    }

    public double assignWordWeightsAndGetTotalWeight(ArrayList<WordEntry> wordEntries) {
        final Double sliderUpperBound = .50;
        final Double sliderLowerBound = .025;
        final int sliderCountMax = 30;
        final double sliderMultipler = SharedPrefManager.getInstance(getBaseContext()).getSliderMultiplier();

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


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }
}
