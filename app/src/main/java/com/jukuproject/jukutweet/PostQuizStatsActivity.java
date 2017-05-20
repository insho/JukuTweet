package com.jukuproject.jukutweet;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.jukuproject.jukutweet.Adapters.WordListExpandableAdapter;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.QuizMenuDialog;
import com.jukuproject.jukutweet.Fragments.SearchFragment;
import com.jukuproject.jukutweet.Fragments.StatsFragmentFillintheBlanks;
import com.jukuproject.jukutweet.Fragments.StatsFragmentMultipleChoice;
import com.jukuproject.jukutweet.Interfaces.PostQuizFragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.QuizMenuDialogInteractionListener;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.MultChoiceResult;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.TabContainers.PostQuizTab1Container;
import com.jukuproject.jukutweet.TabContainers.PostQuizTab2Container;

import java.util.ArrayList;

import static com.jukuproject.jukutweet.Fragments.WordListFragment.prepareColorBlockDataForAList;
import static com.jukuproject.jukutweet.MainActivity.assignTweetWeightsAndGetTotalWeight;
import static com.jukuproject.jukutweet.MainActivity.assignWordWeightsAndGetTotalWeight;

/**
 * Quiz/PostQuiz Stats activity fragment manager
 *
 * @see StatsFragmentMultipleChoice
 * @see StatsFragmentFillintheBlanks
 * @see com.jukuproject.jukutweet.Fragments.StatsFragmentProgress
 */
public class PostQuizStatsActivity extends AppCompatActivity
        implements QuizMenuDialogInteractionListener
        , PostQuizFragmentInteractionListener {

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
    private FloatingActionButton fab;
    private static final String TAG = "TEST-Main";
    private String[] mAdapterTitles;
    private String typeOfQuizThatWasCompleted;
    private MyListEntry mMyListEntry;
    private ColorBlockMeasurables mColorBlockMeasurables;
    private Integer mTabNumber;
    private Integer mLastExpandedPosition;
    private boolean mIsTweetListQuiz;
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
            typeOfQuizThatWasCompleted = savedInstanceState.getString("typeOfQuizThatWasCompleted");
            mMyListEntry = savedInstanceState.getParcelable("myListEntry");
            mColorBlockMeasurables = savedInstanceState.getParcelable("colorBlockMeasurables");
            mTabNumber = savedInstanceState.getInt("tabNumber");
            mIsTweetListQuiz = savedInstanceState.getBoolean("mIsTweetListQuiz");
            mQuizPostQuizStatsPagerAdapter = new PostQuizStatsPagerAdapter(getSupportFragmentManager(),mAdapterTitles,fragments);
            mUserInfo = savedInstanceState.getParcelable("mUserInfo");
            mSingleUser = savedInstanceState.getBoolean("mSingleUser");
            mLastExpandedPosition = savedInstanceState.getInt("mLastExpandedPosition",0);
        } else {
            //Get the intent from the options menu, with the pertinent data
            Intent mIntent = getIntent();
            typeOfQuizThatWasCompleted = mIntent.getStringExtra("typeOfQuizThatWasCompleted"); //The type of quiz that was chosen inthe menu
            mIsTweetListQuiz = mIntent.getBooleanExtra("isTweetList",false);

            String quizType = mIntent.getStringExtra("quizType");
            mMyListEntry = mIntent.getParcelableExtra("myListEntry");
            mTabNumber = mIntent.getIntExtra("tabNumber",2);
            mLastExpandedPosition = mIntent.getIntExtra("lastExpandedPosition",0);
            typeOfQuizThatWasCompleted = mIntent.getStringExtra("typeOfQuizThatWasCompleted");
            mUserInfo = mIntent.getParcelableExtra("userInfo");
            mSingleUser = mIntent.getBooleanExtra("singleUser",false);

            View colorBlockMinWidthEstimateView = getLayoutInflater().inflate(R.layout.expandablelistadapter_listitem, null);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            float metricsDensity = metrics.density;
            if(mSingleUser) {
                mColorBlockMeasurables = prepareColorBlockDataForAList(getBaseContext(),mUserInfo,true,colorBlockMinWidthEstimateView,metricsDensity);
            } else {
                mColorBlockMeasurables = prepareColorBlockDataForAList(getBaseContext(),mMyListEntry,mIsTweetListQuiz,colorBlockMinWidthEstimateView,metricsDensity);
            }

            ArrayList<MultChoiceResult> mDataSetMultipleChoice = mIntent.getParcelableArrayListExtra("mDataSetMultipleChoice");
            ArrayList<Tweet> mDataSetFillintheBlanks = mIntent.getParcelableArrayListExtra("mDataSetFillintheBlanks");
            Integer  mCorrect = mIntent.getIntExtra("mCorrect",0);
            Integer mTotal = mIntent.getIntExtra("mTotal",0);
            Integer mWordbuilderScore = mIntent.getIntExtra("mWordbuilderScore",0);
            boolean mIsHighScore = mIntent.getBooleanExtra("mIsHighScore",false);
            boolean mIsWordBuilder = mIntent.getBooleanExtra("mIsWordBuilder",false);

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


                    break;
                case "Fill in the Blanks":
                    StatsFragmentFillintheBlanks statsFragmentFillintheBlanks = StatsFragmentFillintheBlanks.newInstance(mDataSetFillintheBlanks,mCorrect,mTotal);
                    fragments[0] =  statsFragmentFillintheBlanks;


                    break;
                default:
                    break;
            }

            if(mSingleUser) {
                if(BuildConfig.DEBUG){Log.i(TAG,"Creating single user postquiz stats "  + mUserInfo.getUserId());}
                fragments[1] = PostQuizTab2Container.newSingleUserTweetsInstance(mColorBlockMeasurables,mUserInfo);
            } else {
                if(BuildConfig.DEBUG) {
                    Log.i(TAG,"istweetlistquiz: " + mIsTweetListQuiz);
                    Log.i(TAG,"mylistname: " + mMyListEntry.getListName() + ", sys: " + mMyListEntry.getListsSys());
                }
                fragments[1] = PostQuizTab2Container.newInstance(mColorBlockMeasurables,mMyListEntry,mIsTweetListQuiz);
            }

            // Create the adapter that will return a fragment for each of the primary sections of the activity.
            mAdapterTitles = new String[2];
            mQuizPostQuizStatsPagerAdapter = new PostQuizStatsPagerAdapter(getSupportFragmentManager(),mAdapterTitles,fragments);
            mAdapterTitles = new String[]{"Score","Stats"};
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

        setUpTheFab(typeOfQuizThatWasCompleted,mSingleUser);

        if(mAdapterTitles != null
                && mAdapterTitles.length > 0
                && mAdapterTitles[0].equals("Score")) {
            showFab(true);
        } else {
            showFab(false);
        }

        mViewPager.setAdapter(mQuizPostQuizStatsPagerAdapter);
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


    @Override
    public void onBackPressed() {

            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("fragmentWasChanged", true);
            intent.putExtra("tabNumber",mTabNumber);
            intent.putExtra("lastExpandedPosition",mLastExpandedPosition);
            startActivity(intent);
            finish();

    }


    /**
     * Returns fragment for a given position in {@link PostQuizStatsPagerAdapter}
     * @param position pager adapter position
     * @return Fragment corresponding to position
     */
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
     *                    the number of tabs will decrease from ("User","Saved Tweets","Word List") to ("Timeline","SavedTweets")
     */
    public void updateTabs(String[] updatedTabs) {

        if(mQuizPostQuizStatsPagerAdapter != null) {
            mQuizPostQuizStatsPagerAdapter.updateTabs(updatedTabs);
        }
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

        ArrayList<WordEntry> dataset = new ArrayList<>();
        String dataType = "";

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

    public void goToQuizActivityFillintheBlanks(int tabNumber
            , MyListEntry myListEntry
            , Integer currentExpandedPosition
            , String quizSize
            , String selectedColorString) {

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
            if(BuildConfig.DEBUG){Log.d(TAG,"dataset isspinner: " + dataset.get(0).getWordEntries().get(1).getKanji() + ", spinner: "
                    + dataset.get(0).getWordEntries().get(1).isSpinner());}
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

        //The request is coming from the saved tweets fragment
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


    /**
     * Sets up the action of the floating action button. Clicking it will always pull up the {@link QuizMenuDialog}, but
     * the type of dialog depends on the type of quiz that was completed, and whether that quiz was for a word/tweet list or
     * for a single user's saved tweets
     * @param typeOfQuizThatWasCompleted string with the type of quiz completed ("Multiple Choice", "Fill in the Blanks")
     * @param singleUser true if the completed quiz was a single user's saved tweets, or a word/tweet list
     */
    private void setUpTheFab(String typeOfQuizThatWasCompleted, boolean singleUser) {
        if(typeOfQuizThatWasCompleted.equals("Multiple Choice")) {
            if(singleUser) {
                fab.setOnClickListener(new View.OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        QuizMenuDialog.newSingleUserInstance("multiplechoice"
                                ,mTabNumber
                                ,mLastExpandedPosition
                                ,mUserInfo
                                ,mColorBlockMeasurables
                                ,getdimenscore()).show(getSupportFragmentManager(),"dialogQuizMenu");
                    }
                });
            } else {
                fab.setOnClickListener(new View.OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        Log.i(TAG,"TABNUMBER: " + mTabNumber);
                        QuizMenuDialog.newInstance("multiplechoice"
                                ,mTabNumber
                                ,mLastExpandedPosition
                                ,mMyListEntry
                                ,mColorBlockMeasurables
                                ,getdimenscore()).show(getSupportFragmentManager(),"dialogQuizMenu");
                    }
                });

            }
        } else if (typeOfQuizThatWasCompleted.equals("Fill in the Blanks")) {
            if(singleUser) {
                fab.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        QuizMenuDialog.newSingleUserInstance("fillintheblanks"
                                ,mTabNumber
                                ,mLastExpandedPosition
                                ,mUserInfo
                                ,mColorBlockMeasurables
                                ,getdimenscore()).show(getSupportFragmentManager(),"dialogQuizMenu");
                    }
                });

            } else {
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
        }
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
        outState.putInt("mLastExpandedPosition",mLastExpandedPosition);

    }

    @Override
    protected void onDestroy() {
        InternalDB.getInstance(getBaseContext()).close();
        super.onDestroy();
    }


}
