package com.jukuproject.jukutweet;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Adapters.MyListExpandableAdapter;
import com.jukuproject.jukutweet.Database.ExternalDB;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.QuizMenuDialog;
import com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment;
import com.jukuproject.jukutweet.Fragments.StatsFragmentFillintheBlanks;
import com.jukuproject.jukutweet.Fragments.StatsFragmentMultipleChoice;
import com.jukuproject.jukutweet.Fragments.StatsFragmentProgress;
import com.jukuproject.jukutweet.Interfaces.QuizFragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.QuizMenuDialogInteractionListener;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MultChoiceResult;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.TabContainers.QuizTab1Container;
import com.jukuproject.jukutweet.TabContainers.QuizTab2Container;
import com.jukuproject.jukutweet.TabContainers.Tab1Container;
import com.jukuproject.jukutweet.TabContainers.Tab2Container;
import com.jukuproject.jukutweet.TabContainers.Tab3Container;

import java.util.ArrayList;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

import static com.jukuproject.jukutweet.Fragments.MyListFragment.getExpandableAdapterColorBlockBasicWidths;
import static com.jukuproject.jukutweet.MainActivity.assignWordWeightsAndGetTotalWeight;

/**
 * Quiz/PostQuiz Stats activity fragment manager
 */
public class QuizActivity extends AppCompatActivity implements QuizFragmentInteractionListener, QuizMenuDialogInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private QuizSectionsPagerAdapter mQuizSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private SmoothProgressBar progressbar;
    private FloatingActionButton fab;
    private Menu mMenu;
    private static final String TAG = "TEST-Main";
    private static final boolean debug = true;
    private PagerTitleStrip mTitleStrip;
    private String[] mAdapterTitles;
    private String typeOfQuizThatWasCompleted;
    private MyListEntry mMyListEntry;
    private ColorBlockMeasurables mColorBlockMeasurables;
    private Integer mTabNumber;
    private Integer mLastExpandedPosition;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new ExternalDB(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mViewPager = (ViewPager) findViewById(R.id.container);
        mTitleStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            Fragment[] fragments = new Fragment[2];
            fragments[0] = (QuizTab1Container)getSupportFragmentManager().getFragment(savedInstanceState, "tab1Container");
            fragments[1] = (QuizTab2Container)getSupportFragmentManager().getFragment(savedInstanceState, "tab2Container");

            mAdapterTitles = savedInstanceState.getStringArray("adapterTitles");
            typeOfQuizThatWasCompleted = savedInstanceState.getString("savedInstanceState");
            mMyListEntry = savedInstanceState.getParcelable("myListEntry");
            mColorBlockMeasurables = savedInstanceState.getParcelable("colorBlockMeasurables");
            mTabNumber = savedInstanceState.getInt("tabNumber");
            //TODO HANDLE STRIP VISIBILITY
            mQuizSectionsPagerAdapter = new QuizSectionsPagerAdapter(getSupportFragmentManager(),mAdapterTitles,fragments);




        } else {

            //Get the intent from the options menu, with the pertinent data
            Intent mIntent = getIntent();


            //Create instances of the QuizTabContainer fragments
            /*1. get the menu option (Multiple Choice, Fill in the Blanks, etc.) from the quiz activity.
                     This should always exist. */
            typeOfQuizThatWasCompleted = mIntent.getStringExtra("typeOfQuizThatWasCompleted"); //The type of quiz that was chosen inthe menu

                /* Depending on the menuOption, pull the appropriate set of data from the intent and run the
                * appropriate fragment */
            String quizType = mIntent.getStringExtra("quizType");
            mMyListEntry = mIntent.getParcelableExtra("myListEntry");
            String quizSize = mIntent.getStringExtra("quizSize");
            String colorString = mIntent.getStringExtra("colorString");
            String timer = mIntent.getStringExtra("timer"); //Timer can be "none" so passing it on raw as string
            double totalweight = mIntent.getDoubleExtra("totalweight",0);
            String dataType = mIntent.getStringExtra("dataType");
            mTabNumber = mIntent.getIntExtra("tabNumber",2);
            mLastExpandedPosition = mIntent.getIntExtra("lastExpandedPosition",0);
            typeOfQuizThatWasCompleted = mIntent.getStringExtra("typeOfQuizThatWasCompleted");
            
            Fragment[] fragments = new Fragment[2];
            switch (typeOfQuizThatWasCompleted) {
                case "Multiple Choice":
                    final ArrayList<WordEntry> datasetMultipleChoice = mIntent.getParcelableArrayListExtra("dataset");
                    fragments[0] = QuizTab1Container.newMultipleChoiceInstance(datasetMultipleChoice
                            ,quizType,timer,quizSize,totalweight,dataType,colorString,mMyListEntry);
                    break;
                case "Fill in the Blanks":
                    final ArrayList<Tweet> datasetFillBlanks = mIntent.getParcelableArrayListExtra("dataset");
                    fragments[0] =  QuizTab1Container.newFillintheBlanksInstance(datasetFillBlanks,quizSize,totalweight,colorString,mMyListEntry);
                    break;
                default:
                    break;
            }

            fragments[1] = QuizTab2Container.newInstance();


            mTitleStrip.setVisibility(View.GONE);

            // Create the adapter that will return a fragment for each of the primary sections of the activity.
            mAdapterTitles = new String[1];
            mAdapterTitles[0] = typeOfQuizThatWasCompleted;
            mQuizSectionsPagerAdapter = new QuizSectionsPagerAdapter(getSupportFragmentManager(),mAdapterTitles,fragments);



            // Set the title
            try {
//                MyListEntry myListEntry = mIntent.getParcelableExtra("myListEntry");
                showActionBarBackButton(true,mMyListEntry.getListName());
            } catch (Exception e) {
                showActionBarBackButton(true,"");
            }
        }

        Log.d(TAG,"Madapter title: " + mAdapterTitles[0]);
        if(mAdapterTitles != null
                && mAdapterTitles.length > 0
                && mAdapterTitles[0].equals("Score")) {
            showFab(true,"quizRedo");
            mTitleStrip.setVisibility(View.VISIBLE);
        } else {
            showFab(false);
            mTitleStrip.setVisibility(View.GONE);
        }

        mViewPager.setAdapter(mQuizSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

//        mViewPager.invalidate();
        progressbar = (SmoothProgressBar) findViewById(R.id.progressbar);



    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * If the top bucket is visible after popping the fragment in the backstack, use {@link #updateTabs(String[])} to
     * include the mylist bucket and show the main page title strip items. Basically reset everything
     */
    @Override
    public void onBackPressed() {

        /*Check to see if current 1st tab fragment is the stats fragment (and not  quiz). If it is
         * just go straight back to main menu
         * If it is not a stats fragment, it must be a quiz, so show the "Are you sure" dialog.
         */

        if(mAdapterTitles != null
                && mAdapterTitles.length>0
                && mAdapterTitles[0].equals("Score")) {

            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("fragmentWasChanged", true);
            intent.putExtra("tabNumber",mTabNumber);
            intent.putExtra("lastExpandedPosition",mLastExpandedPosition);
            startActivity(intent);
            finish();
        } else {

            //TODO -- timers...
//            if(timer>0 && coundDownTimer != null) {
//                coundDownTimer.cancel();
//            }

            if(findFragmentByPosition(0) != null
                    && findFragmentByPosition(0) instanceof QuizTab1Container
                    && ((QuizTab1Container) findFragmentByPosition(0)).getChildFragmentManager().findFragmentByTag("multipleChoiceFragment") != null
                    && ((QuizTab1Container) findFragmentByPosition(0)).getChildFragmentManager().findFragmentByTag("multipleChoiceFragment").isVisible()) {
                try {
                    ((MultipleChoiceFragment)((QuizTab1Container) findFragmentByPosition(0)).getChildFragmentManager().findFragmentByTag("multipleChoiceFragment")).pauseTimer();
                } catch (NullPointerException e) {
                    Log.e(TAG,"Pause timer from quizactivity nullpointer: " + e);
                } catch (Exception e) {
                    Log.e(TAG,"pause timer other exception" + e);;
                }
            }


            final  AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final AlertDialog dialog;
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            TextView text = new TextView(this);
            text.setText("Exit Quiz?");
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            text.setTextColor(ContextCompat.getColor(getBaseContext(), android.R.color.black));
            layout.addView(text);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {


                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("fragmentWasChanged", true);
                    startActivity(intent);
                    finish();
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

            if(findFragmentByPosition(0) != null
                    && findFragmentByPosition(0) instanceof QuizTab1Container
                    && ((QuizTab1Container) findFragmentByPosition(0)).getChildFragmentManager().findFragmentByTag("multipleChoiceFragment") != null
                    && ((QuizTab1Container) findFragmentByPosition(0)).getChildFragmentManager().findFragmentByTag("multipleChoiceFragment").isVisible()) {

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {

                        if(findFragmentByPosition(0) != null
                                && findFragmentByPosition(0) instanceof QuizTab1Container
                                && ((QuizTab1Container) findFragmentByPosition(0)).getChildFragmentManager().findFragmentByTag("multipleChoiceFragment") != null
                                && ((QuizTab1Container) findFragmentByPosition(0)).getChildFragmentManager().findFragmentByTag("multipleChoiceFragment").isVisible()) {
                            try {
                                ((MultipleChoiceFragment)((QuizTab1Container) findFragmentByPosition(0)).getChildFragmentManager().findFragmentByTag("multipleChoiceFragment")).resumeTimer();
                            } catch (NullPointerException e) {
                                Log.e(TAG,"Pause timer from quizactivity nullpointer: " + e);
                            }
                        }
                    }
                });


            }

        }
        
    }



    public Fragment findFragmentByPosition(int position) {

        Log.d(TAG,"Mviewpager id: " + mViewPager.getId());
        Log.d(TAG,"mQuizSectionsPagerAdapter id: " + mQuizSectionsPagerAdapter.getItemId(position));

        return getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + mViewPager.getId() + ":"
                        + mQuizSectionsPagerAdapter.getItemId(position));
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
            if(show && type.equals("quizRedo")) {
                fab.setVisibility(View.VISIBLE);
                fab.setImageResource(R.drawable.ic_refresh_white_24dp);
            } else {
                fab.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            fab.setVisibility(View.GONE);
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
     *                    the number of tabs will decrease from ("User","Saved Tweets","Word List") to ("Timeline","SavedTweets")
     */
    public void updateTabs(String[] updatedTabs) {

        if(mQuizSectionsPagerAdapter != null) {
            mQuizSectionsPagerAdapter.updateTabs(updatedTabs);
        }
    }

    public void showPostQuizStatsMultipleChoice(ArrayList<MultChoiceResult> dataset
            , String quizType
            , final MyListEntry myListEntry
            , boolean isWordBuilder
            , boolean isHighScore
            , Integer wordbuilderScore
            , int correct
            , int total) {

        //Update the viewpager to show 2 stats tabs
        mAdapterTitles = new String[]{"Score","Stats"};


        mColorBlockMeasurables = prepareColorBlockDataForList(mMyListEntry);

        if(dataset != null && dataset.size()>0) {

            Log.d(TAG,"Creating statsfragment -- correct: " + correct + ", total: " + total);

            if (findFragmentByPosition(0) != null
                        && findFragmentByPosition(0) instanceof QuizTab1Container) {

                    StatsFragmentMultipleChoice statsFragmentMultipleChoice = StatsFragmentMultipleChoice.newInstance(dataset
                            ,quizType
                            , isWordBuilder
                    , isHighScore
                    , wordbuilderScore
                    , correct
                    , total);
                    ((BaseContainerFragment) findFragmentByPosition(0)).replaceFragment(statsFragmentMultipleChoice, true, "statsFragmentMultipleChoice");
                }

            if (findFragmentByPosition(1) != null
                    && findFragmentByPosition(1) instanceof QuizTab2Container) {

                StatsFragmentProgress statsFragmentProgress = StatsFragmentProgress.newInstance(myListEntry
                        , 10
                        ,mColorBlockMeasurables);
                ((BaseContainerFragment) findFragmentByPosition(1)).replaceFragment(statsFragmentProgress, true, "statsFragmentProgress");
            }

            } else {
                //TODO kick user out to main activity
            }
        updateTabs(mAdapterTitles );
        showFab(true,"quizRedo");
        mTitleStrip.setVisibility(View.VISIBLE);

        //SET THE FAB TO REDO THE MULT CHOICE QUIZ
        fab.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

                        QuizMenuDialog.newInstance("multiplechoice"
                                ,mTabNumber
                                ,mLastExpandedPosition
                                ,mMyListEntry
                                ,mColorBlockMeasurables
                                ,getdimenscore()).show(getSupportFragmentManager(),"dialogQuizMenu");
            }
        });
    }

    public void showPostQuizStatsFillintheBlanks(ArrayList<Tweet> dataset
            , MyListEntry myListEntry
            , int correct
            , int total) {

        //Update the viewpager to show 2 stats tabs
        mAdapterTitles = new String[]{"Score","Stats"};
        updateTabs(mAdapterTitles );

        mColorBlockMeasurables = prepareColorBlockDataForList(mMyListEntry);

        if(dataset != null && dataset.size()>0) {

            Log.d(TAG,"Creating statsfragment -- correct: " + correct + ", total: " + total);

            if (findFragmentByPosition(0) != null
                    && findFragmentByPosition(0) instanceof QuizTab1Container) {

                StatsFragmentFillintheBlanks statsFragmentFillintheBlanks = StatsFragmentFillintheBlanks.newInstance(dataset
                        , correct
                        , total);
                ((BaseContainerFragment) findFragmentByPosition(0)).replaceFragment(statsFragmentFillintheBlanks, true, "statsFragmentFillintheBlanks");
            }

            if (findFragmentByPosition(1) != null
                    && findFragmentByPosition(1) instanceof QuizTab2Container) {

                StatsFragmentProgress statsFragmentProgress = StatsFragmentProgress.newInstance(myListEntry
                        , 10
                        ,mColorBlockMeasurables);
                ((BaseContainerFragment) findFragmentByPosition(1)).replaceFragment(statsFragmentProgress, true, "statsFragmentProgress");
            }

        } else {
            //TODO kick user out to main activity
        }


        showFab(true,"quizRedo");
        //SET THE FAB TO REDO THE MULT CHOICE QUIZ
        fab.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {


                        QuizMenuDialog.newInstance("fillintheblanks"
                                ,mTabNumber
                                ,mLastExpandedPosition
                                ,mMyListEntry
                                ,mColorBlockMeasurables
                                ,getdimenscore()).show(getSupportFragmentManager(),"dialogQuizMenu");
            }
        });

    }



    //TODO combine with same thing in mylistfragment
    public  ColorBlockMeasurables prepareColorBlockDataForList(MyListEntry myListEntry) {
        ColorBlockMeasurables colorBlockMeasurables = new ColorBlockMeasurables();

        ColorThresholds colorThresholds = SharedPrefManager.getInstance(getBaseContext()).getColorThresholds();
        Cursor c = InternalDB.getWordInterfaceInstance(getBaseContext()).getWordListColorBlockCursor(colorThresholds,myListEntry);

        if(c.getCount()>0) {
            c.moveToFirst();

                /* We do not want to include favorites star lists that are not active in the user
                * preferences. So if an inactivated list shows up in the sql query, ignore it (don't add to mMenuHeader)*/

            colorBlockMeasurables.setGreyCount(c.getInt(3));
            colorBlockMeasurables.setRedCount(c.getInt(4));
            colorBlockMeasurables.setYellowCount(c.getInt(5));
            colorBlockMeasurables.setGreenCount(c.getInt(6));
            colorBlockMeasurables.setEmptyCount(0);

            colorBlockMeasurables.setGreyMinWidth(getExpandableAdapterColorBlockBasicWidths(this, String.valueOf(colorBlockMeasurables.getGreyCount())));
            colorBlockMeasurables.setRedMinWidth(getExpandableAdapterColorBlockBasicWidths(this, String.valueOf(colorBlockMeasurables.getRedCount())));
            colorBlockMeasurables.setYellowMinWidth(getExpandableAdapterColorBlockBasicWidths(this, String.valueOf(colorBlockMeasurables.getYellowCount())));
            colorBlockMeasurables.setGreenMinWidth(getExpandableAdapterColorBlockBasicWidths(this, String.valueOf(colorBlockMeasurables.getGreenCount())));
            colorBlockMeasurables.setEmptyMinWidth(0);



            c.moveToNext();
        }
        c.close();
        return  colorBlockMeasurables;
    }


    /**
     * Maximum width of the color bars in the MenuExpandableListAdapter. Right that vlue is set
     * to half of the screenwidth
     * @return maximum width in pixels of colored bars
     *
     * @see MyListExpandableAdapter
     */
    private int getdimenscore() {

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return Math.round((float)metrics.widthPixels*(float).5);

    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//    }


    /**
     * Methods pertaining to the fab redo button
     */

    public void showFlashCardFragment(int tabNumber
            , MyListEntry listEntry
            , String frontValue
            , String backValue
            , String selectedColorString) {};

    public void goToQuizActivityMultipleChoice(int tabNumber
            , MyListEntry listEntry
            ,Integer currentExpandedPosition
            , String quizType
            , String quizSize
            , String quizTimer
            , String selectedColorString) {


//        Integer timer = -1;
//        if (!quizTimer.equals("None")) {
//            timer = Integer.parseInt(quizTimer);
//        }

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


            Fragment[] fragments = new Fragment[2];
            fragments[0] = QuizTab1Container.newMultipleChoiceInstance(dataset
                    ,quizType,quizTimer,quizSize,totalweight,dataType,selectedColorString,mMyListEntry);

//
            fragments[1] = QuizTab2Container.newInstance();
            showFab(false);


            mTitleStrip.setVisibility(View.GONE);

            // Create the adapter that will return a fragment for each of the primary sections of the activity.
            mAdapterTitles = new String[1];
            mAdapterTitles[0] = typeOfQuizThatWasCompleted;
            updateTabs(mAdapterTitles);
            ((BaseContainerFragment) findFragmentByPosition(0)).replaceFragment(fragments[0], false, "multipleChoiceFragment");

//            mQuizSectionsPagerAdapter = new QuizSectionsPagerAdapter(getSupportFragmentManager(),mAdapterTitles,fragments);

        } else {
            Toast.makeText(this, "No words found to quiz on", Toast.LENGTH_SHORT).show();
        }

    }

    public void goToQuizActivityFillintheBlanks(int tabNumber
            , MyListEntry listEntry
            , Integer currentExpandedPosition
            , String quizSize
            , String selectedColorString) {


    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        if(findFragmentByPosition(0) != null
                && findFragmentByPosition(0) instanceof QuizTab1Container) {
            getSupportFragmentManager().putFragment(outState, "tab1Container", (QuizTab1Container)findFragmentByPosition(0));
        }

        if(findFragmentByPosition(1) != null
                && findFragmentByPosition(1) instanceof QuizTab2Container) {
            getSupportFragmentManager().putFragment(outState, "tab2Container", (QuizTab2Container)findFragmentByPosition(1));
        }

        outState.putStringArray("adapterTitles", mAdapterTitles);
        outState.putString("typeOfQuizThatWasCompleted", typeOfQuizThatWasCompleted);
        outState.putParcelable("myListEntry",mMyListEntry);
        outState.putParcelable("colorBlockMeasurables",mColorBlockMeasurables);
        outState.putInt("tabNumber",mTabNumber);
    }



}
