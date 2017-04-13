package com.jukuproject.jukutweet;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Database.ExternalDB;
import com.jukuproject.jukutweet.Fragments.StatsFragmentMultipleChoice;
import com.jukuproject.jukutweet.Fragments.StatsFragmentProgress;
import com.jukuproject.jukutweet.Interfaces.QuizFragmentInteractionListener;
import com.jukuproject.jukutweet.Models.MultChoiceResult;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.TabContainers.QuizTab1Container;
import com.jukuproject.jukutweet.TabContainers.QuizTab2Container;
import com.jukuproject.jukutweet.TabContainers.Tab1Container;
import com.jukuproject.jukutweet.TabContainers.Tab2Container;
import com.jukuproject.jukutweet.TabContainers.Tab3Container;

import java.util.ArrayList;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * Quiz/PostQuiz Stats activity fragment manager
 */
public class QuizActivity extends AppCompatActivity implements QuizFragmentInteractionListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new ExternalDB(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get the intent from the options menu, with the pertinent data
        Intent mIntent = getIntent();
        String menuOption = mIntent.getStringExtra("menuOption"); //The type of quiz that was chosen in the menu
//        String quizType = mIntent.getStringExtra("quizType");
//        int tabNumber = mIntent.getIntExtra("tabNumber", 2);
//        MyListEntry myListEntry = mIntent.getParcelableExtra("myListEntry");
//        String quizSize = mIntent.getStringExtra("quizSize");
//        String selectedColorString = mIntent.getStringExtra("colorString");
//        String timer = mIntent.getStringExtra("timer"); //Timer can be "none" so passing it on raw as string
//        int mylistposition = mIntent.getIntExtra("mylistposition", 0);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        // Create the adapter that will return a fragment for each of the primary sections of the activity.
        mQuizSectionsPagerAdapter = new QuizSectionsPagerAdapter(getSupportFragmentManager(),new String[]{menuOption},mIntent);

        mTitleStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        mTitleStrip.setVisibility(View.GONE);

        // Set the title
       try {
            MyListEntry myListEntry = mIntent.getParcelableExtra("myListEntry");
            showActionBarBackButton(true,myListEntry.getListName());
        } catch (Exception e) {
            showActionBarBackButton(true,"");
        }

        mViewPager.setAdapter(mQuizSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
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

                    switch (position) {
                        case 0:
                            showFab(true,"quizRedo");
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

                Toast.makeText(QuizActivity.this, "quiz redo", Toast.LENGTH_SHORT).show();
//                if(mViewPager != null) {
//                    if(mViewPager.getCurrentItem() == 0 && isTopShowing(0)) {
//                        showAddUserDialog();
//                    }
//                    else if(mViewPager.getCurrentItem() == 1 && isTopShowing(1)) {
//                        showAddMyListDialog("TweetList");
//                    }
//                    else if(mViewPager.getCurrentItem() == 2 && isTopShowing(2)) {
//                        showAddMyListDialog("MyList");
//                    }
//                }


            }
        });


//        switch (menuOption) {
//            case "Multiple Choice":
//                Log.d(TAG,"x TIMER: " + timer);
////                goToQuizActivityMultipleChoice(tabNumber,myListEntry,quizType,quizSize,timer,selectedColorString);
//                break;
//            case "Fill in the Blanks":
////                showFillintheBlanksFragment(tabNumber,myListEntry,quizSize,selectedColorString);
//                break;
//        }

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
        if(findFragmentByPosition(0) != null
                && findFragmentByPosition(0) instanceof QuizTab1Container
                && ((QuizTab1Container) findFragmentByPosition(0)).getChildFragmentManager().findFragmentByTag("statsFragmentProgress") != null
                && ((QuizTab1Container) findFragmentByPosition(0)).getChildFragmentManager().findFragmentByTag("statsFragmentProgress").isVisible()) {

            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("fragmentWasChanged", true);
            startActivity(intent);
            finish();
        } else {

            //TODO -- timers...
//            if(timer>0 && coundDownTimer != null) {
//                coundDownTimer.cancel();
//            }

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

//            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//                    if (timer > 0 && coundDownTimer != null) {
//                        setUpTimer((int) millstogo / 1000);
//                    }
//                }
//            });

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

//
//    public void goToQuizActivityMultipleChoice(int tabNumber
//            , MyListEntry listEntry
//            , String quizType
//            , String quizSize
//            , String quizTimer
//            ,String selectedColorString) {
//
//        Integer timer = -1;
//        if (!quizTimer.equals("None")) {
//            timer = Integer.parseInt(quizTimer);
//        }
//
//        ColorThresholds colorThresholds = SharedPrefManager.getInstance(getApplicationContext()).getColorThresholds();
//
//        if (tabNumber == 1) {
//            //Its a mylist fragment
//
//
//
//            ArrayList<WordEntry> dataset = InternalDB.getTweetInterfaceInstance(getBaseContext())
//                    .getWordsFromATweetList(listEntry
//                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
//                            , selectedColorString
//                            , null
//                            , Integer.parseInt(quizSize));
//
//            if(dataset.size()>0) {
//
//                double totalweight = assignWordWeightsAndGetTotalWeight(dataset);
//                if (findFragmentByPosition(tabNumber) != null
//                        && findFragmentByPosition(tabNumber) instanceof Tab2Container) {
//
//                    MultipleChoiceFragment multipleChoiceFragment = MultipleChoiceFragment.newInstance(dataset
//                            , quizType
//                            , timer
//                            , Integer.parseInt(quizSize)
//                            , totalweight
//                            , "Tweet"
//                            , selectedColorString
//                            , listEntry);
//                    ((BaseContainerFragment) findFragmentByPosition(tabNumber)).replaceFragment(multipleChoiceFragment, true, "multipleChoiceFragment");
//                }
//
//            } else {
//                Toast.makeText(this, "No words found to quiz on", Toast.LENGTH_SHORT).show();
//            }
//
//        } else if (tabNumber == 2) {
//            //Its a mylist fragment
//            ArrayList<WordEntry> dataset = InternalDB.getWordInterfaceInstance(getBaseContext())
//                    .getWordsFromAWordList(listEntry
//                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
//                            , selectedColorString
//                            , null
//                            , Integer.parseInt(quizSize));
//
//            if(dataset.size()>0) {
//                double totalweight = assignWordWeightsAndGetTotalWeight(dataset);
////                if (findFragmentByPosition(tabNumber) != null
////                        && findFragmentByPosition(tabNumber) instanceof Tab3Container) {
//
//                    MultipleChoiceFragment multipleChoiceFragment = MultipleChoiceFragment.newInstance(dataset
//                            , quizType
//                            , timer
//                            , Integer.parseInt(quizSize)
//                            , totalweight
//                            , "Word"
//                            , selectedColorString
//                            , listEntry);
//
//                if(((BaseContainerFragment) findFragmentByPosition(1)) == null) {
//                    Log.d(TAG,"FRAGMENT IS NULL");
//                }
//                    ((BaseContainerFragment) findFragmentByPosition(1)).replaceFragment(multipleChoiceFragment, true, "multipleChoiceFragment");
//
////                }
//            } else {
//                Toast.makeText(this, "No words found to quiz on", Toast.LENGTH_SHORT).show();
//            }
//
//        }
//
//    }
//
//    public void showFillintheBlanksFragment(int tabNumber
//            , MyListEntry myListEntry
//            , String quizSize
//            ,String selectedColorString) {
//
////        ColorThresholds colorThresholds = SharedPrefManager.getInstance(getApplicationContext()).getColorThresholds();
//
//        if (tabNumber == 1) {
//
//
//            //The request is coming from the saved tweets fragment
//            //The request is coming from the saved words fragment
//            ArrayList<Tweet> dataset = InternalDB.getQuizInterfaceInstance(getBaseContext())
//                    .getFillintheBlanksTweetsForATweetList(myListEntry
//                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
//                            , selectedColorString
//                            , Integer.parseInt(quizSize));
//
//            if(dataset.size()>0) {
//                double totalweight = assignTweetWeightsAndGetTotalWeight(dataset);
//
//                if (findFragmentByPosition(tabNumber) != null
//                        && findFragmentByPosition(tabNumber) instanceof Tab2Container) {
//                    FillInTheBlankFragment fillInTheBlankFragment = FillInTheBlankFragment.newInstance(dataset
//                            , Integer.parseInt(quizSize)
//                            , totalweight
//                            , selectedColorString
//                            , myListEntry);
//                    ((BaseContainerFragment) findFragmentByPosition(tabNumber)).replaceFragment(fillInTheBlankFragment, true, "fillInTheBlankFragment");
//
//                }
//
//            } else {
//                Toast.makeText(this, "No words found to quiz on", Toast.LENGTH_SHORT).show();
//            }
//        } else if (tabNumber == 2) {
//
//            //The request is coming from the saved words fragment
////            ArrayList<Tweet> dataset  = new ArrayList<>();
//            ArrayList<Tweet> dataset = InternalDB.getQuizInterfaceInstance(getBaseContext())
//                    .getFillintheBlanksTweetsForAWordList(myListEntry
//                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
//                            , selectedColorString
//                            , Integer.parseInt(quizSize));
////            InternalDB.getQuizInterfaceInstance(getBaseContext())
////                    .superTest(myListEntry
////                            , SharedPrefManager.getInstance(getBaseContext()).getColorThresholds()
////                            , selectedColorString
////                            , Integer.parseInt(quizSize));
//
//            if(dataset.size()>0) {
//                double totalweight = assignTweetWeightsAndGetTotalWeight(dataset);
//
//                if (findFragmentByPosition(tabNumber) != null
//                        && findFragmentByPosition(tabNumber) instanceof Tab3Container) {
//
//
//                    FillInTheBlankFragment fillInTheBlankFragment = FillInTheBlankFragment.newInstance(dataset
//                            , Integer.parseInt(quizSize)
//                            , totalweight
//                            , selectedColorString
//                            , myListEntry);
//                    ((BaseContainerFragment) findFragmentByPosition(tabNumber)).replaceFragment(fillInTheBlankFragment, true, "fillInTheBlankFragment");
//
//                }
//            } else {
//                Toast.makeText(this, "No words found to quiz on", Toast.LENGTH_SHORT).show();
//            }
//
//
//        }
//
//    }
//
//    public double assignWordWeightsAndGetTotalWeight(ArrayList<WordEntry> wordEntries) {
//        final Double sliderUpperBound = .50;
//        final Double sliderLowerBound = .025;
//        final int sliderCountMax = 30;
//        final double sliderMultipler = SharedPrefManager.getInstance(getBaseContext()).getSliderMultiplier();
//
//        double totalWeight = 0.0d;
//
//        for(WordEntry wordEntry : wordEntries) {
//            double percentage =wordEntry.getPercentage();
//            double total = (double)wordEntry.getTotal();
//
//            /* The slider multiplier is what affects how rapidly a word diverges from the natural weight of .25.
//            * The higher the multiplier, the faster it will diverge with an increased count.*/
//            double countMultiplier = (double)total/(double)sliderCountMax*(percentage-(double)sliderUpperBound)*(double)sliderMultipler;
//
//            if(total>100) {
//                total = (double)100;
//            }
//
//            double a = ((double)sliderUpperBound/(double)2)-(double)sliderUpperBound*(countMultiplier) ;
//
//            double b = sliderLowerBound;
//            if(a>=sliderUpperBound) {
//                b = sliderUpperBound;
//            } else if(a>=sliderLowerBound) {
//                b = a;
//            }
//
//            wordEntry.setQuizWeight(b);
//            totalWeight += b;
//            Log.d(TAG,"Setting quiz weight: " + b  + ", new total weight: " + totalWeight);
//        }
//
//        return totalWeight;
//    }
//
//
//    public double assignTweetWeightsAndGetTotalWeight(ArrayList<Tweet> tweets) {
//        final Double sliderUpperBound = .50;
//        final Double sliderLowerBound = .025;
//        final int sliderCountMax = 30;
//        final double sliderMultipler = SharedPrefManager.getInstance(getBaseContext()).getSliderMultiplier();
//
//        double totalWeight = 0.0d;
//
//        for(Tweet tweet : tweets) {
//            double aggregatedTweetCorrect = 0;
//            double aggregatedTweetTotal = 0;
//
//            for(WordEntry wordEntry : tweet.getWordEntries()) {
//                aggregatedTweetCorrect += (double)wordEntry.getCorrect();
//                aggregatedTweetTotal += (double)wordEntry.getTotal();
//
//            }
//
//            double tweetPercentage = aggregatedTweetCorrect/aggregatedTweetTotal;
//            /* The slider multiplier is what affects how rapidly a word diverges from the natural weight of .25.
//            * The higher the multiplier, the faster it will diverge with an increased count.*/
//            double countMultiplier = (double)aggregatedTweetTotal/(double)sliderCountMax*(tweetPercentage-(double)sliderUpperBound)*(double)sliderMultipler;
//
//
//            double a = ((double)sliderUpperBound/(double)2)-(double)sliderUpperBound*(countMultiplier) ;
//
//            double b = sliderLowerBound;
//            if(a>=sliderUpperBound) {
//                b = sliderUpperBound;
//            } else if(a>=sliderLowerBound) {
//                b = a;
//            }
//
//            tweet.setQuizWeight(b);
//            totalWeight += b;
//        }
//
//        return totalWeight;
//    }
//
//

    public void showPostQuizStatsMultipleChoice(ArrayList<MultChoiceResult> dataset
            , String quizType
            , MyListEntry myListEntry
            , boolean isWordBuilder
            , boolean isHighScore
            , Integer wordbuilderScore
            , int correct
            , int total) {

        //Update the viewpager to show 2 stats tabs
        updateTabs(new String[]{"Score","Stats"});

        if(dataset != null && dataset.size()>0) {

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

                StatsFragmentProgress statsFragmentProgress = StatsFragmentProgress.newInstance(quizType
                         ,myListEntry
                        , 5);
                ((BaseContainerFragment) findFragmentByPosition(1)).replaceFragment(statsFragmentProgress, true, "statsFragmentProgress");
            }

            } else {
                //TODO kick user out to main activity
            }

    }

    public void showPostQuizStatsFillintheBlanks(ArrayList<Tweet> dataset
            , MyListEntry myListEntry
            , int total) {
        Toast.makeText(this, "SHOW POST QUIZ STATS", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }
}
