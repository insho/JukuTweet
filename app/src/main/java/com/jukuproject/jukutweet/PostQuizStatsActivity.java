package com.jukuproject.jukutweet;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Adapters.WordListExpandableAdapter;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.QuizMenuDialog;
import com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment;
import com.jukuproject.jukutweet.Fragments.StatsFragmentFillintheBlanks;
import com.jukuproject.jukutweet.Fragments.StatsFragmentMultipleChoice;
import com.jukuproject.jukutweet.Interfaces.QuizMenuDialogInteractionListener;
import com.jukuproject.jukutweet.Interfaces.StatsFragmentInteractionListener;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MultChoiceResult;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.TabContainers.PostQuizTab1Container;
import com.jukuproject.jukutweet.TabContainers.PostQuizTab2Container;

import java.util.ArrayList;

import static com.jukuproject.jukutweet.Fragments.WordListFragment.getExpandableAdapterColorBlockBasicWidths;
import static com.jukuproject.jukutweet.MainActivity.assignTweetWeightsAndGetTotalWeight;
import static com.jukuproject.jukutweet.MainActivity.assignWordWeightsAndGetTotalWeight;

/**
 * Quiz/PostQuiz Stats activity fragment manager
 */

public class PostQuizStatsActivity extends AppCompatActivity implements StatsFragmentInteractionListener
        , QuizMenuDialogInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mQuizSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private FloatingActionButton fab;
    private static final String TAG = "TEST-Main";
    private String[] mAdapterTitles;
    private String typeOfQuizThatWasCompleted;
    private MyListEntry mMyListEntry;
    private ColorBlockMeasurables mColorBlockMeasurables;
    private Integer mTabNumber;
    private Integer mLastExpandedPosition;

    private boolean mIsWordBuilder = false;
    private boolean mIsHighScore = false;
    private Integer mWordbuilderScore = 0;
    private Integer mCorrect;
    private Integer mTotal;
    private ArrayList<MultChoiceResult> mDataSetMultipleChoice;
    private boolean mIsTweetListQuiz;
    private ArrayList<Tweet> mDataSetFillintheBlanks;
    private UserInfo mUserInfo;
    private boolean mSingleUser;  //If quiz activiy was initiated from the SingleUserTweetListFragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mViewPager = (ViewPager) findViewById(R.id.container);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_refresh_white_24dp);

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            Fragment[] fragments = new Fragment[2];
            fragments[0] = (PostQuizTab1Container)getSupportFragmentManager().getFragment(savedInstanceState, "postQuizTab1Container");
            fragments[1] = (PostQuizTab2Container)getSupportFragmentManager().getFragment(savedInstanceState, "postQuizTab2Container");

            mAdapterTitles = savedInstanceState.getStringArray("adapterTitles");
            typeOfQuizThatWasCompleted = savedInstanceState.getString("savedInstanceState");
            mMyListEntry = savedInstanceState.getParcelable("myListEntry");
            mColorBlockMeasurables = savedInstanceState.getParcelable("colorBlockMeasurables");
            mTabNumber = savedInstanceState.getInt("tabNumber");
            mIsTweetListQuiz = savedInstanceState.getBoolean("mIsTweetListQuiz");
            mQuizSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),mAdapterTitles,fragments);
            mUserInfo = savedInstanceState.getParcelable("mUserInfo");
            mSingleUser = savedInstanceState.getBoolean("mSingleUser");
        } else {

            //Get the intent from the options menu, with the pertinent data
            Intent mIntent = getIntent();

            typeOfQuizThatWasCompleted = mIntent.getStringExtra("typeOfQuizThatWasCompleted"); //The type of quiz that was chosen inthe menu
            mIsTweetListQuiz = mIntent.getBooleanExtra("mIsTweetListQuiz",false);

                /* Depending on the menuOption, pull the appropriate set of data from the intent and run the
                * appropriate fragment */
            String quizType = mIntent.getStringExtra("quizType");
            mMyListEntry = mIntent.getParcelableExtra("myListEntry");
            mTabNumber = mIntent.getIntExtra("tabNumber",2);
            mLastExpandedPosition = mIntent.getIntExtra("lastExpandedPosition",0);
            typeOfQuizThatWasCompleted = mIntent.getStringExtra("typeOfQuizThatWasCompleted");

            mDataSetMultipleChoice = mIntent.getParcelableArrayListExtra("mDataSetMultipleChoice");
            mDataSetFillintheBlanks = mIntent.getParcelableArrayListExtra("mDataSetFillintheBlanks");
            mCorrect = mIntent.getIntExtra("mCorrect",0);
            mTotal = mIntent.getIntExtra("mTotal",0);
            mWordbuilderScore = mIntent.getIntExtra("mWordbuilderScore",0);
            mIsHighScore = mIntent.getBooleanExtra("mIsHighScore",false);
            mIsWordBuilder = mIntent.getBooleanExtra("mIsWordBuilder",false);
            mColorBlockMeasurables = prepareColorBlockDataForList(mMyListEntry);
            mUserInfo = mIntent.getParcelableExtra("userInfo");
            mSingleUser = mIntent.getBooleanExtra("singleUser",false);

            Fragment[] fragments = new Fragment[2];
            switch (typeOfQuizThatWasCompleted) {
                case "Multiple Choice":

                    StatsFragmentMultipleChoice statsFragmentMultipleChoice = StatsFragmentMultipleChoice.newInstance(mDataSetMultipleChoice
                            ,quizType
                            , mIsWordBuilder
                            , mIsHighScore
                            , mWordbuilderScore
                            , mCorrect
                            , mTotal);


                    fragments[0] = statsFragmentMultipleChoice;
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

                    break;
                case "Fill in the Blanks":
                    StatsFragmentFillintheBlanks statsFragmentFillintheBlanks = StatsFragmentFillintheBlanks.newInstance(mDataSetFillintheBlanks,mCorrect,mTotal);
                    fragments[0] =  statsFragmentFillintheBlanks;
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

                    break;
                default:
                    break;
            }

            if(mSingleUser) {
                fragments[1] = PostQuizTab2Container.newSingleUserTweetsInstance(mColorBlockMeasurables,mUserInfo);
            } else {
                fragments[1] = PostQuizTab2Container.newInstance(mColorBlockMeasurables,mMyListEntry,mIsTweetListQuiz);
            }


            // Create the adapter that will return a fragment for each of the primary sections of the activity.
            mAdapterTitles = new String[2];
            mQuizSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),mAdapterTitles,fragments);
            mAdapterTitles = new String[]{"Score","Stats"};
            updateTabs(mAdapterTitles);

            // Set the title
            try {
                showActionBarBackButton(true,mMyListEntry.getListName());
            } catch (Exception e) {
                showActionBarBackButton(true,"");
            }
        }

        Log.d(TAG,"Madapter title: " + mAdapterTitles[0]);
        if(mAdapterTitles != null
                && mAdapterTitles.length > 0
                && mAdapterTitles[0].equals("Score")) {
            showFab(true);
        } else {
            showFab(false);
        }

        mViewPager.setAdapter(mQuizSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

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
                        if(typeOfQuizThatWasCompleted.equals("Multiple Choice")) {
                            try {
                                ((MultipleChoiceFragment) findFragmentByPosition(0)).resumeTimer();
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

//        Log.d(TAG,"Mviewpager id: " + mViewPager.getId());
//        Log.d(TAG,"mQuizSectionsPagerAdapter id: " + mQuizSectionsPagerAdapter.getItemId(position));

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
//     * Shows or hides the floating action button
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

    //asdf
//    public void showPostQuizStatsMultipleChoice(ArrayList<MultChoiceResult> dataset
//            , String quizType
//            , final MyListEntry myListEntry
//            , boolean isWordBuilder
//            , boolean isHighScore
//            , Integer wordbuilderScore
//            , int correct
//            , int total) {
//
//        //Update the viewpager to show 2 stats tabs
//        mAdapterTitles = new String[]{"Score","Stats"};
//        if(mTitleStrip ==null) {
//            mTitleStrip = new PagerTitleStrip(getBaseContext());
//            mTitleStrip.setLayoutParams(new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            mTitleStrip.setGravity(Gravity.TOP);
//            mTitleStrip.setTag("titleStrip");
//        }
//        mViewPager.addView(mTitleStrip);
//        mViewPager.invalidate();
//        mViewPager.forceLayout();
//        updateTabs(mAdapterTitles );
//
//        mColorBlockMeasurables = prepareColorBlockDataForList(mMyListEntry);
//
//        if(dataset != null && dataset.size()>0) {
//
////            Log.d(TAG,"Creating statsfragment -- correct: " + correct + ", total: " + total);
////
//////            StatsFragmentProgress statsFragmentProgress = StatsFragmentProgress.newWordListInstance(myListEntry
//////                    , 10
//////                    ,mColorBlockMeasurables);
////Log.d(TAG,"XXX HERE");
//////            ((BaseContainerFragment) findFragmentByPosition(0)).replaceFragment(statsFragmentProgress, true, "statsFragmentProgressx");
////            StatsFragmentMultipleChoice statsFragmentMultipleChoice = StatsFragmentMultipleChoice.newWordListInstance(dataset
////                            ,quizType
////                            , isWordBuilder
////                    , isHighScore
////                    , wordbuilderScore
////                    , correct
////                    , total);
////
////                ((BaseContainerFragment) findFragmentByPosition(0)).replaceFragment(statsFragmentMultipleChoice, true, "statsFragmentMultipleChoice");
////
////            StatsFragmentProgress statsFragmentProgress = StatsFragmentProgress.newWordListInstance(myListEntry
////                        , 10
////                        ,mColorBlockMeasurables);
////
////            ((BaseContainerFragment) findFragmentByPosition(1)).replaceFragment(statsFragmentProgress, true, "statsFragmentMultipleChoice1");
////            Log.d(TAG,"XXX HERE2");
//            if (findFragmentByPosition(0) != null
//                    && findFragmentByPosition(0) instanceof QuizTab1Container) {
//                StatsFragmentMultipleChoice statsFragmentMultipleChoice = StatsFragmentMultipleChoice.newWordListInstance(dataset
//                        ,quizType
//                        , isWordBuilder
//                        , isHighScore
//                        , wordbuilderScore
//                        , correct
//                        , total);
//
//                ((BaseContainerFragment) findFragmentByPosition(0)).replaceFragment(statsFragmentMultipleChoice, true, "statsFragmentMultipleChoice");
//            }
//
//
//            if (findFragmentByPosition(1) != null
//                    && findFragmentByPosition(1) instanceof QuizTab2Container) {
//
//
//                StatsFragmentProgress statsFragmentProgress = StatsFragmentProgress.newWordListInstance(myListEntry
//                        , 10
//                        ,mColorBlockMeasurables);
//
//                ((BaseContainerFragment) findFragmentByPosition(1)).replaceFragment(statsFragmentProgress, true, "statsFragmentProgress");
//            } else {
//                Log.d(TAG,"CANT FIND...");
//            }
//
//        } else {
//            Log.e(TAG,"quiz result dataset was 0, kicking user back to main activity...");
//            Intent intent = new Intent(getBaseContext(), MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.putExtra("fragmentWasChanged", true);
//            intent.putExtra("tabNumber",mTabNumber);
//            intent.putExtra("lastExpandedPosition",mLastExpandedPosition);
//            startActivity(intent);
//            finish();
//        }
//
//
//        showFab(true,"quizRedo");
////        mTitleStrip.setVisibility(View.VISIBLE);
//
//        //SET THE FAB TO REDO THE MULT CHOICE QUIZ
//
//    }

//    public void showPostQuizStatsFillintheBlanks(ArrayList<Tweet> dataset
//            , MyListEntry myListEntry
//            , int correct
//            , int total) {
//
//        //Update the viewpager to show 2 stats tabs
//        mAdapterTitles = new String[]{"Score","Stats"};
//        updateTabs(mAdapterTitles );
//
//        mColorBlockMeasurables = prepareColorBlockDataForList(mMyListEntry);
//
//        if(dataset != null && dataset.size()>0) {
//
//            Log.d(TAG,"Creating statsfragment -- correct: " + correct + ", total: " + total);
//
//            if (findFragmentByPosition(0) != null
//                    && findFragmentByPosition(0) instanceof QuizTab1Container) {
//
//                StatsFragmentFillintheBlanks statsFragmentFillintheBlanks = StatsFragmentFillintheBlanks.newWordListInstance(dataset
//                        , correct
//                        , total);
//                ((BaseContainerFragment) findFragmentByPosition(0)).replaceFragment(statsFragmentFillintheBlanks, true, "statsFragmentFillintheBlanks");
//            }
//
//            if (findFragmentByPosition(1) != null
//                    && findFragmentByPosition(1) instanceof QuizTab2Container) {
//
//                StatsFragmentProgress statsFragmentProgress = StatsFragmentProgress.newWordListInstance(myListEntry
//                        , 10
//                        ,mColorBlockMeasurables);
//                ((BaseContainerFragment) findFragmentByPosition(1)).replaceFragment(statsFragmentProgress, true, "statsFragmentProgress");
//            }
//
//        } else {
//            Log.e(TAG,"quiz result dataset was 0, kicking user back to main activity...");
//            Intent intent = new Intent(getBaseContext(), MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.putExtra("fragmentWasChanged", true);
//            intent.putExtra("tabNumber",mTabNumber);
//            intent.putExtra("lastExpandedPosition",mLastExpandedPosition);
//            startActivity(intent);
//            finish();
//        }
//
//
//        showFab(true,"quizRedo");
//        //SET THE FAB TO REDO THE MULT CHOICE QUIZ
//        fab.setOnClickListener(new View.OnClickListener() {
//
//
//            @Override
//            public void onClick(View view) {
//
//
//                QuizMenuDialog.newWordListInstance("fillintheblanks"
//                        ,mTabNumber
//                        ,mLastExpandedPosition
//                        ,mMyListEntry
//                        ,mColorBlockMeasurables
//                        ,getdimenscore()).show(getSupportFragmentManager(),"dialogQuizMenu");
//            }
//        });
//
//    }



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

            Log.d(TAG,"doing this");
        }
        c.close();
        return  colorBlockMeasurables;
    }


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
        //TODO REMOVE THISN HERE? LIKE MAKE A SEPARATE INTERFACE?
    public void showFlashCardFragment(int tabNumber
            , MyListEntry listEntry
            , String frontValue
            , String backValue
            , String selectedColorString) {};
    public void showSingleUserFlashCardFragment(int tabNumber
            , UserInfo userInfo
            , String frontValue
            , String backValue
            , String selectedColorString) {};

    /**
     * Runs the multiple choice quiz activity.
     * {@link QuizMenuDialog} -> {@link QuizMenuDialogInteractionListener} -> {@link QuizActivity}).
     *  Activated when the user clicks on the fab redo button to redo a quiz. Essentially a duplicate of the action
     * chooses
     * @param tabNumber Int specifying which tab (1 - Tweetlist, 2 - Wordlist) the request came from. This number was passed
     *                  on from the initial {@link MainActivity} through the {@link QuizActivity} to here.
     * @param listEntry MyListEntry object from which the words for the quiz are chosen
     * @param currentExpandedPosition Which my list object position in the tab was last expanded (so it can be expanded again
     *                                when user returns to {@link MainActivity})
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
        intent.putExtra("tabNumber", 2);
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


    public void goToSingleUserQuizActivityMultipleChoice(int tabNumber
            , UserInfo userInfo
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
                    .getWordsFromAUsersSavedTweets(userInfo
                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
                            , selectedColorString
                            , null
                            , Integer.parseInt(quizSize));
            dataType = "Tweet";

        }

        if(dataset.size()>0) {
            double totalweight = assignWordWeightsAndGetTotalWeight(getBaseContext(),dataset,.50,.025,30);


            Intent intent = new Intent(getBaseContext(), QuizActivity.class);

            intent.putExtra("singleUser",true); //The type of quiz that was chosen inthe menu
            intent.putExtra("typeOfQuizThatWasCompleted","Multiple Choice"); //The type of quiz that was chosen inthe menu
            intent.putExtra("quizType",quizType); //Multiple choice quiz option (Kanji to Definition, etc)
            intent.putExtra("tabNumber", 2);
            intent.putExtra("userInfo",userInfo);
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

            double totalweight = assignTweetWeightsAndGetTotalWeight(dataset,SharedPrefManager.getInstance(getBaseContext()).getSliderMultiplier(),.5,.025,30);

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

    public void goToSingleUserQuizActivityFillintheBlanks(int tabNumber
            , UserInfo userInfo
            , Integer currentExpandedPosition
            , String quizSize
            , String selectedColorString) {

//        ColorThresholds colorThresholds = SharedPrefManager.getInstance(getApplicationContext()).getColorThresholds();
        ArrayList<Tweet> dataset = new ArrayList<>();
        String dataType = "";

        if (tabNumber == 1) {

            //The request is coming from the saved tweets fragment
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
//            Log.d(TAG,"dataset isspinner: " + dataset.get(0).getWordEntries().get(1).getKanji() + ", spinner: "
//                    + dataset.get(0).getWordEntries().get(1).isSpinner());
            intent.putParcelableArrayListExtra("dataset",dataset);
            intent.putExtra("dataType",dataType);
            intent.putExtra("lastExpandedPosition",currentExpandedPosition);

            showFab(false);

            startActivity(intent);

        } else {
            Toast.makeText(this, "No tweets found to quiz on", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        if(findFragmentByPosition(0) != null
                && findFragmentByPosition(0) instanceof PostQuizTab1Container) {
            getSupportFragmentManager().putFragment(outState, "postQuizTab1Container", (PostQuizTab1Container)findFragmentByPosition(0));
        }

        if(findFragmentByPosition(1) != null
                && findFragmentByPosition(1) instanceof PostQuizTab2Container) {
            getSupportFragmentManager().putFragment(outState, "postQuizTab2Container", (PostQuizTab2Container)findFragmentByPosition(1));
        }

        outState.putStringArray("adapterTitles", mAdapterTitles);
        outState.putString("typeOfQuizThatWasCompleted", typeOfQuizThatWasCompleted);
        outState.putParcelable("myListEntry",mMyListEntry);
        outState.putParcelable("colorBlockMeasurables",mColorBlockMeasurables);
        outState.putInt("tabNumber",mTabNumber);
        outState.putBoolean("mIsTweetListQuiz",mIsTweetListQuiz);
        outState.putParcelable("mUserInfo",mUserInfo);
        outState.putBoolean("mSingleUser",mSingleUser);
//        outState.putParcelableArrayList("mDataSetMultipleChoice",mDataSetMultipleChoice);
//        outState.putParcelableArrayList("mDataSetMultipleChoice",mDataSetFillintheBlanks);



    }



}
