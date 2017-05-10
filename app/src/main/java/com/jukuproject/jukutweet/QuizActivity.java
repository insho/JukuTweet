package com.jukuproject.jukutweet;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.WordListExpandableAdapter;
import com.jukuproject.jukutweet.Database.ExternalDB;
import com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment;
import com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment;
import com.jukuproject.jukutweet.Interfaces.QuizFragmentInteractionListener;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.MultChoiceResult;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;

import java.util.ArrayList;

//import com.jukuproject.jukutweet.TabContainers.QuizTab1Container;

/**
 * Quiz/PostQuiz Stats activity fragment manager
 */
public class QuizActivity extends AppCompatActivity implements  QuizFragmentInteractionListener {

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
//    private SmoothProgressBar progressbar;
//    private FloatingActionButton fab;
//    private Menu mMenu;
    private static final String TAG = "TEST-Main";
    private static final boolean debug = true;
    private PagerTitleStrip mTitleStrip;
    private String[] mAdapterTitles;
    private String typeOfQuizThatWasCompleted;
    private MyListEntry mMyListEntry;
    private ColorBlockMeasurables mColorBlockMeasurables;
    private Integer mTabNumber;
    private Integer mLastExpandedPosition;
    private UserInfo mUserInfo;

    private boolean mSingleUser; //Designates whether the quiz activity is from a single user saved tweets (true), or a tweet/word list (false)
//    private Integer tabStripHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        new ExternalDB(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mViewPager = (ViewPager) findViewById(R.id.container);
//        mTitleStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
//        fab = (FloatingActionButton) findViewById(R.id.fab);

        if (savedInstanceState != null) {
            //Restore the fragment's instance


            mAdapterTitles = savedInstanceState.getStringArray("adapterTitles");
            typeOfQuizThatWasCompleted = savedInstanceState.getString("typeOfQuizThatWasCompleted");
            mMyListEntry = savedInstanceState.getParcelable("myListEntry");
            mColorBlockMeasurables = savedInstanceState.getParcelable("colorBlockMeasurables");
            mTabNumber = savedInstanceState.getInt("tabNumber");

            mSingleUser = savedInstanceState.getBoolean("singleUser",false);
            mUserInfo = savedInstanceState.getParcelable("mUserInfo");

            Fragment[] fragments = new Fragment[2];
            if(typeOfQuizThatWasCompleted.equals("Multiple Choice")) {
                fragments[0] = (MultipleChoiceFragment)getSupportFragmentManager().getFragment(savedInstanceState, "tab1Container");
            } else {
                fragments[0] = (FillInTheBlankFragment)getSupportFragmentManager().getFragment(savedInstanceState, "tab1Container");
            }
//            fragments[1] = (QuizTab2Container)getSupportFragmentManager().getFragment(savedInstanceState, "tab2Container");
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
            mSingleUser = mIntent.getBooleanExtra("singleUser",false);
            mUserInfo = mIntent.getParcelableExtra("mUserInfo");
            Integer timerInteger;
            if(timer == null || timer.equals("None")) {
                timerInteger = -1;
            } else {
                timerInteger = Integer.parseInt(timer);
            }

            Fragment[] fragments = new Fragment[1];
            switch (typeOfQuizThatWasCompleted) {
                case "Multiple Choice":
                    final ArrayList<WordEntry> datasetMultipleChoice = mIntent.getParcelableArrayListExtra("dataset");
                    if(mSingleUser) {


                        fragments[0] = (MultipleChoiceFragment) MultipleChoiceFragment.newInstanceSingleUser(datasetMultipleChoice
                                ,quizType,timerInteger,Integer.parseInt(quizSize),totalweight,dataType,colorString,mUserInfo);

                    } else {
                        fragments[0] = (MultipleChoiceFragment) MultipleChoiceFragment.newInstance(datasetMultipleChoice
                                ,quizType,timerInteger,Integer.parseInt(quizSize),totalweight,dataType,colorString,mMyListEntry);
//                        fragments[0] = QuizTab1Container.newMultipleChoiceInstance(datasetMultipleChoice
//                                ,quizType,timer,quizSize,totalweight,dataType,colorString,mMyListEntry);
                    }

                    break;
                case "Fill in the Blanks":
                    final ArrayList<Tweet> datasetFillBlanks = mIntent.getParcelableArrayListExtra("dataset");

                    if(mSingleUser) {
                        fragments[0] = FillInTheBlankFragment.newSingleUserInstance(datasetFillBlanks
                                , quizSize
                                , totalweight
                                , colorString
                                ,  mUserInfo);
                    } else {
                        fragments[0] = FillInTheBlankFragment.newInstance(datasetFillBlanks
                                , quizSize
                                , totalweight
                                , colorString
                                ,  mMyListEntry);
                    }


//                    fragments[0] =  QuizTab1Container.newFillintheBlanksInstance(datasetFillBlanks,quizSize,totalweight,colorString,mMyListEntry);
                    break;
                default:
                    break;
            }

//            fragments[1] = QuizTab2Container.newWordListInstance();

//            mTitleStrip.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));


            // Create the adapter that will return a fragment for each of the primary sections of the activity.
            mAdapterTitles = new String[2];
            mAdapterTitles[0] = typeOfQuizThatWasCompleted;
//            mAdapterTitles[1] = "dummy";
            mQuizSectionsPagerAdapter = new QuizSectionsPagerAdapter(getSupportFragmentManager(),mAdapterTitles,fragments);


            mAdapterTitles = new String[]{typeOfQuizThatWasCompleted};
            updateTabs(mAdapterTitles);

            // Set the title
            try {
                showActionBarBackButton(true,mMyListEntry.getListName());
            } catch (Exception e) {
                showActionBarBackButton(true,"");
            }
        }


        mViewPager.setAdapter(mQuizSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(1);

//        mViewPager.invalidate();
//        progressbar = (SmoothProgressBar) findViewById(R.id.progressbar);
//        mViewPager.forceLayout();

//        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_content);
//        coordinatorLayout.requestLayout();

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

            if(typeOfQuizThatWasCompleted.equals("Multiple Choice")) {
                try {
                    ((MultipleChoiceFragment) findFragmentByPosition(0)).pauseTimer();
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
            if(typeOfQuizThatWasCompleted.equals("Multiple Choice")) {
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {

                            try {
                                ((MultipleChoiceFragment) findFragmentByPosition(0)).resumeTimer();
                            } catch (NullPointerException e) {
                                Log.e(TAG,"Pause timer from quizactivity nullpointer: " + e);
                            }

                    }
                });
            }
//            if(findFragmentByPosition(0) != null
//                    && findFragmentByPosition(0) instanceof QuizTab1Container
//                    && ((QuizTab1Container) findFragmentByPosition(0)).getChildFragmentManager().findFragmentByTag("multipleChoiceFragment") != null
//                    && ((QuizTab1Container) findFragmentByPosition(0)).getChildFragmentManager().findFragmentByTag("multipleChoiceFragment").isVisible()) {
//
//                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//
//                        if(findFragmentByPosition(0) != null
//                                && findFragmentByPosition(0) instanceof QuizTab1Container
//                                && ((QuizTab1Container) findFragmentByPosition(0)).getChildFragmentManager().findFragmentByTag("multipleChoiceFragment") != null
//                                && ((QuizTab1Container) findFragmentByPosition(0)).getChildFragmentManager().findFragmentByTag("multipleChoiceFragment").isVisible()) {
//                            try {
//                                ((MultipleChoiceFragment)((QuizTab1Container) findFragmentByPosition(0)).getChildFragmentManager().findFragmentByTag("multipleChoiceFragment")).resumeTimer();
//                            } catch (NullPointerException e) {
//                                Log.e(TAG,"Pause timer from quizactivity nullpointer: " + e);
//                            }
//                        }
//                    }
//                });
//
//
//            }

        }
        
    }


//asdf
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



//    /**
//     * Checks whether the current visible fragment for a given position of the fragmentpager adapter
//     * is the top fragment in the stack for that bucket
//     * @param position position of the viewpager adapter
//     * @return bool true if the top fragment is showing, false if not
//     */
//    public boolean isTopShowing(int position) {
//
//        switch(position) {
//            case 0:
//                try {
//                    return ((Tab1Container)findFragmentByPosition(0)).isTopFragmentShowing();
//                } catch(NullPointerException e) {
//                    return false;
//                }
//            case 1:
//                try {
//                    return ((Tab2Container)findFragmentByPosition(1)).isTopFragmentShowing();
//                } catch(NullPointerException e) {
//                    return false;
//                }
//            case 2:
//                try {
//                    return  ((Tab3Container)findFragmentByPosition(2)).isTopFragmentShowing();
//
//                } catch(NullPointerException e) {
//                    return false;
//                }
//            default:
//                break;
//        }
//        return true;
//    }

//    /**
//     * Shows or hides the floating action button, and changes the image resource based on the current
//     * fragment showing
//     * @param show bool true to show, false to hide
//     * @param type type of fragment (which dictates the type of image resource)
//     */
//    public void showFab(boolean show, String type) {
//        try {
//            if(show && type.equals("quizRedo")) {
//                fab.setVisibility(View.VISIBLE);
//                fab.setImageResource(R.drawable.ic_refresh_white_24dp);
//            } else {
//                fab.setVisibility(View.GONE);
//            }
//        } catch (NullPointerException e) {
//            fab.setVisibility(View.GONE);
//            Log.e(TAG,"FAB IS NULL: "  + e);
//        }
//
//    }

//    /**
//     * Shows or hides the floating action button
//     * @param show bool true to show, false to hide
//     */
//    public void showFab(boolean show) {
//        try {
//            if(show) {
//                fab.setVisibility(View.VISIBLE);
//            } else {
//                fab.setVisibility(View.GONE);
//            }
//        } catch (NullPointerException e) {
//            Log.e(TAG,"FAB IS NULL: "  + e);
//        }
//
//    }

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

        Intent intent = new Intent(getBaseContext(), PostQuizStatsActivity.class);

        intent.putExtra("typeOfQuizThatWasCompleted","Multiple Choice"); //The type of quiz that was chosen inthe menu
        intent.putExtra("quizType",quizType);
        intent.putExtra("tabNumber", 2);
        intent.putExtra("myListEntry",myListEntry);
        intent.putParcelableArrayListExtra("dataset",dataset);
        intent.putExtra("mTabNumber",mTabNumber);
        intent.putExtra("lastExpandedPosition",mLastExpandedPosition);
        intent.putExtra("mDataSetMultipleChoice",dataset);
        intent.putExtra("mCorrect",correct);
        intent.putExtra("mTotal",total);
        intent.putExtra("mWordbuilderScore",wordbuilderScore);
        intent.putExtra("mIsHighScore",isHighScore);
        intent.putExtra("mIsWordBuilder",isWordBuilder);
        if(mTabNumber!=null && mTabNumber==1) {
            intent.putExtra("isTweetList",true);
        } else {
            intent.putExtra("isTweetList",false);
        }
        startActivity(intent);
    }

    public void showPostQuizStatsMultipleChoiceForSingleUsersTweets(ArrayList<MultChoiceResult> dataset
            , String quizType
            , final UserInfo userInfo
            , boolean isWordBuilder
            , boolean isHighScore
            , Integer wordbuilderScore
            , int correct
            , int total) {

        Intent intent = new Intent(getBaseContext(), PostQuizStatsActivity.class);

        intent.putExtra("userInfo",userInfo);
        intent.putExtra("singleUser",true);
        intent.putExtra("isTweetList",true);

        intent.putExtra("typeOfQuizThatWasCompleted","Multiple Choice"); //The type of quiz that was chosen inthe menu
        intent.putExtra("quizType",quizType);
        intent.putExtra("tabNumber", 1);
//        intent.putParcelableArrayListExtra("dataset",dataset);
        intent.putExtra("mTabNumber",mTabNumber);
        intent.putExtra("lastExpandedPosition",mLastExpandedPosition);
        intent.putExtra("mDataSetMultipleChoice",dataset);
        intent.putExtra("mCorrect",correct);
        intent.putExtra("mTotal",total);
        intent.putExtra("mWordbuilderScore",wordbuilderScore);
        intent.putExtra("mIsHighScore",isHighScore);
        intent.putExtra("mIsWordBuilder",isWordBuilder);
        startActivity(intent);
    }

    public void showPostQuizStatsFillintheBlanks(ArrayList<Tweet> dataset
            , MyListEntry myListEntry
            , int correct
            , int total) {

        Intent intent = new Intent(getBaseContext(), PostQuizStatsActivity.class);

        intent.putExtra("typeOfQuizThatWasCompleted","Fill in the Blanks"); //The type of quiz that was chosen inthe menu
        intent.putExtra("tabNumber", 2);
        intent.putExtra("myListEntry",myListEntry);
//        intent.putParcelableArrayListExtra("mDataSetFillintheBlanks",dataset);
        intent.putExtra("mTabNumber",mTabNumber);
        intent.putExtra("lastExpandedPosition",mLastExpandedPosition);
        intent.putExtra("mDataSetFillintheBlanks",dataset);
        intent.putExtra("mCorrect",correct);
        intent.putExtra("mTotal",total);
        if(mTabNumber!=null && mTabNumber==1) {
            intent.putExtra("isTweetList",true);
        } else {
            intent.putExtra("isTweetList",false);
        }



        startActivity(intent);

    }

    public void showPostQuizStatsFillintheBlanksForSingleUsersTweets(ArrayList<Tweet> dataset
            , UserInfo userInfo
            , int correct
            , int total) {

        Intent intent = new Intent(getBaseContext(), PostQuizStatsActivity.class);

        intent.putExtra("typeOfQuizThatWasCompleted","Fill in the Blanks"); //The type of quiz that was chosen inthe menu
        intent.putExtra("tabNumber", 1);
        intent.putExtra("userInfo",userInfo);
        intent.putExtra("singleUser",true);
        intent.putParcelableArrayListExtra("dataset",dataset);
        intent.putExtra("mTabNumber",mTabNumber);
        intent.putExtra("lastExpandedPosition",mLastExpandedPosition);
        intent.putExtra("mDataSetMultipleChoice",dataset);
        intent.putExtra("mCorrect",correct);
        intent.putExtra("mTotal",total);
        if(mTabNumber!=null && mTabNumber==1) {
            intent.putExtra("isTweetList",true);
        } else {
            intent.putExtra("isTweetList",false);
        }



        startActivity(intent);

    }
//
//    //TODO combine with same thing in mylistfragment
//    public  ColorBlockMeasurables prepareColorBlockDataForList(MyListEntry myListEntry) {
//        ColorBlockMeasurables colorBlockMeasurables = new ColorBlockMeasurables();
//
//        ColorThresholds colorThresholds = SharedPrefManager.getInstance(getBaseContext()).getColorThresholds();
//        Cursor c = InternalDB.getWordInterfaceInstance(getBaseContext()).getWordListColorBlockCursor(colorThresholds,myListEntry);
//
//        if(c.getCount()>0) {
//            c.moveToFirst();
//
//                /* We do not want to include favorites star lists that are not active in the user
//                * preferences. So if an inactivated list shows up in the sql query, ignore it (don't add to mMenuHeader)*/
//
//            colorBlockMeasurables.setGreyCount(c.getInt(3));
//            colorBlockMeasurables.setRedCount(c.getInt(4));
//            colorBlockMeasurables.setYellowCount(c.getInt(5));
//            colorBlockMeasurables.setGreenCount(c.getInt(6));
//            colorBlockMeasurables.setEmptyCount(0);
//
//            colorBlockMeasurables.setGreyMinWidth(getExpandableAdapterColorBlockBasicWidths(this, String.valueOf(colorBlockMeasurables.getGreyCount())));
//            colorBlockMeasurables.setRedMinWidth(getExpandableAdapterColorBlockBasicWidths(this, String.valueOf(colorBlockMeasurables.getRedCount())));
//            colorBlockMeasurables.setYellowMinWidth(getExpandableAdapterColorBlockBasicWidths(this, String.valueOf(colorBlockMeasurables.getYellowCount())));
//            colorBlockMeasurables.setGreenMinWidth(getExpandableAdapterColorBlockBasicWidths(this, String.valueOf(colorBlockMeasurables.getGreenCount())));
//            colorBlockMeasurables.setEmptyMinWidth(0);
//
//            Log.d(TAG,"doing this");
//        }
//        c.close();
//        return  colorBlockMeasurables;
//    }
//

    /**
     * Maximum width of the color bars in the MenuExpandableListAdapter. Right that vlue is set
     * to half of the screenwidth
     * @return maximum width in pixels of colored bars
     *
     * @see WordListExpandableAdapter
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

//    public void goToQuizActivityMultipleChoice(int tabNumber
//            , MyListEntry listEntry
//            ,Integer currentExpandedPosition
//            , String quizType
//            , String quizSize
//            , String quizTimer
//            , String selectedColorString) {
//
//
////        Integer timer = -1;
////        if (!quizTimer.equals("None")) {
////            timer = Integer.parseInt(quizTimer);
////        }
//
////        ColorThresholds colorThresholds = SharedPrefManager.getInstance(getApplicationContext()).getColorThresholds();
//
//        ArrayList<WordEntry> dataset = new ArrayList<>();
//        String dataType = "";
//        if (tabNumber == 1) {
//            //Its a mylist fragment
//
//            dataset = InternalDB.getTweetInterfaceInstance(getBaseContext())
//                    .getWordsFromATweetList(listEntry
//                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
//                            , selectedColorString
//                            , null
//                            , Integer.parseInt(quizSize));
//            dataType = "Tweet";
//
//        } else if (tabNumber == 2) {
//
//
//            //Its a mylist fragment
//            dataset = InternalDB.getWordInterfaceInstance(getBaseContext())
//                    .getWordsFromAWordList(listEntry
//                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
//                            , selectedColorString
//                            , null
//                            , Integer.parseInt(quizSize));
//
//            dataType = "Word";
//        }
//
//        if(dataset.size()>0) {
//            double totalweight = assignWordWeightsAndGetTotalWeight(getBaseContext(),dataset);
//
//
//            Fragment[] fragments = new Fragment[1];
//            fragments[0] = QuizTab1Container.newMultipleChoiceInstance(dataset
//                    ,quizType,quizTimer,quizSize,totalweight,dataType,selectedColorString,mMyListEntry
////                    ,tabStripHeight
//            );
//
////
////            fragments[1] = QuizTab2Container.newWordListInstance();
////            showFab(false);
//
//
////            mTitleStrip.setVisibility(View.GONE);
//
//            // Create the adapter that will return a fragment for each of the primary sections of the activity.
//            mAdapterTitles = new String[1];
//            mAdapterTitles[0] = typeOfQuizThatWasCompleted;
//            updateTabs(mAdapterTitles);
//            ((BaseContainerFragment) findFragmentByPosition(0)).replaceFragment(fragments[0], true, "multipleChoiceFragment");
//
////            mQuizSectionsPagerAdapter = new QuizSectionsPagerAdapter(getSupportFragmentManager(),mAdapterTitles,fragments);
//
//        } else {
//            Toast.makeText(this, "No words found to quiz on", Toast.LENGTH_SHORT).show();
//        }
//
//    }
//
//    public void goToQuizActivityFillintheBlanks(int tabNumber
//            , MyListEntry listEntry
//            , Integer currentExpandedPosition
//            , String quizSize
//            , String selectedColorString) {
//    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        if(findFragmentByPosition(0) != null
                && findFragmentByPosition(0) instanceof MultipleChoiceFragment) {
            getSupportFragmentManager().putFragment(outState, "tab1Container", (MultipleChoiceFragment)findFragmentByPosition(0));
        }

//        if(findFragmentByPosition(1) != null
//                && findFragmentByPosition(1) instanceof QuizTab2Container) {
//            getSupportFragmentManager().putFragment(outState, "tab2Container", (QuizTab2Container)findFragmentByPosition(1));
//        }

        outState.putStringArray("adapterTitles", mAdapterTitles);
        outState.putString("typeOfQuizThatWasCompleted", typeOfQuizThatWasCompleted);
        outState.putParcelable("myListEntry",mMyListEntry);
        outState.putParcelable("mUserInfo",mUserInfo);
        outState.putParcelable("colorBlockMeasurables",mColorBlockMeasurables);
        outState.putInt("tabNumber",mTabNumber);
        outState.putBoolean("mSingleUser",mSingleUser);

    }



}
