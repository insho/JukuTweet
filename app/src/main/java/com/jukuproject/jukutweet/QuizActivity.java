package com.jukuproject.jukutweet;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private PostQuizStatsPagerAdapter mQuizPostQuizStatsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private static final String TAG = "TEST-QuizActivity";
    private String[] mAdapterTitles;
    private String typeOfQuizThatWasCompleted;
    private MyListEntry mMyListEntry;
    private ColorBlockMeasurables mColorBlockMeasurables;
    private Integer mTabNumber;
    private Integer mLastExpandedPosition;
    private UserInfo mUserInfo;
    private boolean mSingleUser; //Designates whether the quiz activity is from a single user saved tweets (true), or a tweet/word list (false)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        new ExternalDB(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mViewPager = (ViewPager) findViewById(R.id.container);

        if (savedInstanceState != null) {

            //Restore the fragment's instance
            mAdapterTitles = savedInstanceState.getStringArray("adapterTitles");
            typeOfQuizThatWasCompleted = savedInstanceState.getString("typeOfQuizThatWasCompleted");
            mMyListEntry = savedInstanceState.getParcelable("myListEntry");
            mColorBlockMeasurables = savedInstanceState.getParcelable("colorBlockMeasurables");
            mTabNumber = savedInstanceState.getInt("tabNumber");
            mSingleUser = savedInstanceState.getBoolean("mSingleUser",false);
            mUserInfo = savedInstanceState.getParcelable("mUserInfo");

            Fragment[] fragments = new Fragment[2];
            if(typeOfQuizThatWasCompleted.equals("Multiple Choice")) {
                fragments[0] = (MultipleChoiceFragment)getSupportFragmentManager().getFragment(savedInstanceState, "tab1Container");
            } else {
                fragments[0] = (FillInTheBlankFragment)getSupportFragmentManager().getFragment(savedInstanceState, "tab1Container");
            }
            mQuizPostQuizStatsPagerAdapter = new PostQuizStatsPagerAdapter(getSupportFragmentManager(),mAdapterTitles,fragments);

            mLastExpandedPosition = savedInstanceState.getInt("mLastExpandedPosition",0);
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
            mUserInfo = mIntent.getParcelableExtra("userInfo");
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
                    break;
                default:
                    break;
            }

            // Create the adapter that will return a fragment for each of the primary sections of the activity.
            mAdapterTitles = new String[2];
            mAdapterTitles[0] = typeOfQuizThatWasCompleted;
            mQuizPostQuizStatsPagerAdapter = new PostQuizStatsPagerAdapter(getSupportFragmentManager(),mAdapterTitles,fragments);
            mAdapterTitles = new String[]{typeOfQuizThatWasCompleted};
            updateTabs(mAdapterTitles);

        }

        // Set the title
        try {
            if(mSingleUser) {
                showActionBarBackButton(true,mUserInfo.getDisplayScreenName());
            } else {
                showActionBarBackButton(true,mMyListEntry.getListName());
            }
        } catch (Exception e) {
            showActionBarBackButton(true,"");
        }

        mViewPager.setAdapter(mQuizPostQuizStatsPagerAdapter);
        mViewPager.setOffscreenPageLimit(1);

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
                    intent.putExtra("tabNumber",mTabNumber);
                    intent.putExtra("lastExpandedPosition",mLastExpandedPosition);
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

        }
        
    }


    /**
     * If something went wrong in the MultipleChoice or FillintheBlanks fragments
     * and they are unable to pull a question, or are cycling repeatedly through the "movetonextquestion" process,
     * this method can be called via the {@link QuizFragmentInteractionListener} and the user will be taken back to the
     * Main Activity with a Toast notification
     */
    public void emergencyGoBackToMainActivity(){
        Log.e(TAG,"Error unable to create quiz... ");
        Toast.makeText(this, "Error. Unable to create quiz.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("fragmentWasChanged", true);
        intent.putExtra("tabNumber",mTabNumber);
        intent.putExtra("lastExpandedPosition",mLastExpandedPosition);
        startActivity(intent);
        finish();
    }

    public Fragment findFragmentByPosition(int position) {

        return getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + mViewPager.getId() + ":"
                        + mQuizPostQuizStatsPagerAdapter.getItemId(position));
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
     * Traffic control method which takes callback from fragment and passes it to
     * the PostQuizStatsPagerAdapter, which will update the number of available tab buckets and
     * titles of those buckets
     * @param updatedTabs String array of tab titles. The number of tab titles in the array dictates
     *                    the number of tab buckets that will be available. So when the user drills down
     *                    from {@link com.jukuproject.jukutweet.Fragments.UserListFragment} to {@link com.jukuproject.jukutweet.Fragments.UserTimeLineFragment}
     *                    the number of tabs will decrease from ("User","Saved Tweets","Word List") to ("Timeline","SavedTweets")
     */
    public void updateTabs(String[] updatedTabs) {

        if(mQuizPostQuizStatsPagerAdapter != null) {
            mQuizPostQuizStatsPagerAdapter.updateTabs(updatedTabs);
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
        intent.putExtra("tabNumber", mTabNumber);
        intent.putExtra("myListEntry",myListEntry);
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
        finish();
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
        intent.putExtra("tabNumber", 0);
        intent.putExtra("tabNumber",mTabNumber);
        intent.putExtra("lastExpandedPosition",mLastExpandedPosition);
        intent.putExtra("mDataSetMultipleChoice",dataset);
        intent.putExtra("mCorrect",correct);
        intent.putExtra("mTotal",total);
        intent.putExtra("mWordbuilderScore",wordbuilderScore);
        intent.putExtra("mIsHighScore",isHighScore);
        intent.putExtra("mIsWordBuilder",isWordBuilder);
        startActivity(intent);
        finish();
    }

    public void showPostQuizStatsFillintheBlanks(ArrayList<Tweet> dataset
            , MyListEntry myListEntry
            , int correct
            , int total) {

        Intent intent = new Intent(getBaseContext(), PostQuizStatsActivity.class);

        intent.putExtra("typeOfQuizThatWasCompleted","Fill in the Blanks"); //The type of quiz that was chosen inthe menu
        intent.putExtra("myListEntry",myListEntry);
        intent.putExtra("tabNumber",mTabNumber);
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
        finish();

    }

    public void showPostQuizStatsFillintheBlanksForSingleUsersTweets(ArrayList<Tweet> dataset
            , UserInfo userInfo
            , int correct
            , int total) {

        Intent intent = new Intent(getBaseContext(), PostQuizStatsActivity.class);

        if(BuildConfig.DEBUG){Log.d(TAG,"showPostQuizStatsFillintheBlanksForSingleUsersTweets USERINFO is null? " + (userInfo==null));};
        intent.putExtra("typeOfQuizThatWasCompleted","Fill in the Blanks"); //The type of quiz that was chosen inthe menu
        intent.putExtra("tabNumber", 0);
        intent.putExtra("userInfo",userInfo);
        intent.putExtra("singleUser",true);
        intent.putExtra("tabNumber",mTabNumber);
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
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(findFragmentByPosition(0) != null
                && findFragmentByPosition(0) instanceof MultipleChoiceFragment) {
            getSupportFragmentManager().putFragment(outState, "tab1Container", (MultipleChoiceFragment)findFragmentByPosition(0));
        }
        outState.putStringArray("adapterTitles", mAdapterTitles);
        outState.putString("typeOfQuizThatWasCompleted", typeOfQuizThatWasCompleted);
        outState.putParcelable("myListEntry",mMyListEntry);
        outState.putParcelable("mUserInfo",mUserInfo);
        outState.putParcelable("colorBlockMeasurables",mColorBlockMeasurables);
        outState.putInt("tabNumber",mTabNumber);
        outState.putBoolean("mSingleUser",mSingleUser);
        outState.putInt("mLastExpandedPosition",mLastExpandedPosition);
    }
}
