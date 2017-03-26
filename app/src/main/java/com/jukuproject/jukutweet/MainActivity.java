package com.jukuproject.jukutweet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
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
import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.TabContainers.Tab1Container;
import com.jukuproject.jukutweet.TabContainers.Tab2Container;
//import com.jukuproject.jukutweet.TabContainers.TabContainer;

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
    private AddUserCheckDialog addUserCheckDialogFragment;
    private RemoveUserDialog removeUserDialogFragment;
    private AddOrRenameMyListDialog addOrRenameMyListDialogFragment;
    private EditMyListDialog editMyListDialogFragment;
    private SmoothProgressBar progressbar;
    private FloatingActionButton fab;
    private static final String TAG = "TEST-Main";
    private static final boolean debug = true;

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
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        if(isTopShowing()) {
                            showFab(true,"addUser");
                        } else {
                            showFab(false);
                        }
                        break;
                    case 1:
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
                    } else if(mViewPager.getCurrentItem() == 1 && isTopShowing()) {
                       showAddMyListDialog();
                   }
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



    public void showAddUserCheckDialog(UserInfo userInfo){
        if (addUserCheckDialogFragment == null || !addUserCheckDialogFragment.isAdded()) {
            Log.d(TAG,"loading addusercheck");
            addUserCheckDialogFragment = AddUserCheckDialog.newInstance(userInfo);
            addUserCheckDialogFragment.show(getFragmentManager(), "dialogAddCheck");
        }
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
        addUserDialogFragment = null;
        removeUserDialogFragment = null;
        addOrRenameMyListDialogFragment = null;
        editMyListDialogFragment = null;
        addUserCheckDialogFragment = null;
    }

    /**r
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

                                //TODO DO STUFF, like pull data into db, or whatever
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

            // Locate Tab1Continer and update the UserListInfo adapter to reflect removed item
            if(findFragmentByPosition(0) != null && findFragmentByPosition(0) instanceof Tab1Container) {
                ((Tab1Container) findFragmentByPosition(0)).updateUserListFragment();
            }
        }
    }



    @Override
    public void onBackPressed() {
        boolean isPopFragment = false;

        //Pop backstack depending on the overall tab position (if applicable)
        switch (mViewPager.getCurrentItem()) {
            case 0:
                isPopFragment = ((BaseContainerFragment)findFragmentByPosition(0)).popFragment();
                try {
                    boolean isTopShowing = ((Tab1Container)findFragmentByPosition(0)).isTopFragmentShowing();
                    if(isTopShowing) {
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
            case 3:
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

    public void changePagerTitle(int position, String title) {
        if(mSectionsPagerAdapter != null) {
            mSectionsPagerAdapter.updateTitleData(title);
        }
    }


    public void showAddMyListDialog(){
        if (addOrRenameMyListDialogFragment == null || !addOrRenameMyListDialogFragment.isAdded()) {
            addOrRenameMyListDialogFragment = AddOrRenameMyListDialog.newInstance();
            addOrRenameMyListDialogFragment.show(getFragmentManager(), "dialogAddMyList");
        }
    }

    public void showRenameMyListDialog(String oldListName){
        if (addOrRenameMyListDialogFragment == null || !addOrRenameMyListDialogFragment.isAdded()) {
            addOrRenameMyListDialogFragment = AddOrRenameMyListDialog.newInstance(oldListName);
            addOrRenameMyListDialogFragment.show(getFragmentManager(), "dialogAddMyList");
        }
    }

    public void showEditMyListDialog(String currentListName, Boolean isStarFavorite){
        if (editMyListDialogFragment == null || !editMyListDialogFragment.isAdded()) {
            editMyListDialogFragment = EditMyListDialog.newInstance(currentListName, isStarFavorite);
            editMyListDialogFragment.show(getFragmentManager(), "dialogEditMyList");
        }
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
                ((Tab2Container) findFragmentByPosition(1)).updateMyListFragment();
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
            if(findFragmentByPosition(1) != null && findFragmentByPosition(1) instanceof Tab2Container) {
                ((Tab2Container) findFragmentByPosition(1)).updateMyListFragment();
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
                if(findFragmentByPosition(1) != null && findFragmentByPosition(1) instanceof Tab2Container) {
                    ((Tab2Container) findFragmentByPosition(1)).updateMyListFragment();
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
                break;
            case 3:
                break;
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


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }
}
