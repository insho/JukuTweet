package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.MultipleChoiceAdapter;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.QuizFragmentInteractionListener;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MultChoiceResult;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;
import com.jukuproject.jukutweet.TweetParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static com.jukuproject.jukutweet.Fragments.FlashCardsFragment.setTextHeightLoop;


/**
 * Multiple choice quiz, running in {@link com.jukuproject.jukutweet.QuizActivity}. Quiz is based on "mDataset", a
 * list of WordEntry objects (compiled in the MainActivity from a word list, tweet list, or singe user's saved tweets).
 *
 * @see com.jukuproject.jukutweet.MainActivity#goToQuizActivityMultipleChoice(int, MyListEntry, Integer, String, String, String, String)
 * @see com.jukuproject.jukutweet.MainActivity#goToSingleUserQuizActivityMultipleChoice(int, UserInfo, Integer, String, String, String, String)
 */
public class MultipleChoiceFragment extends Fragment {

    String TAG = "Test-quizmultchoice";
    QuizFragmentInteractionListener mCallback;
    ArrayList<WordEntry> mDataset; // Word entries that will be quizzed on
    String mQuizType; //Relationship between question and answer (i.e. "Kanji to Kana", "Kanji to Definition" etc);
    Integer mQuizSize;
    Integer mQuizTimer;
    double mTotalWeight;
    String mMyListType; //either "Word" or "Tweet" designating which list is being quizzed
    MyListEntry mMyListEntry;
    UserInfo mUserInfo;
    boolean mSingleUser;
    String mColorString;
    WordEntry currentCorrectAnswer;
    ArrayList<WordEntry> questionSet; //set of word entries for the current question (5 incorrect WordEntries and 1 Correct one from the mDataset)


    TextView txtQuestion;
    TextView txtFurigana;
    TextView txtTotal;
    TextView txtScore;
    TextView txtPlusMinus;


    CountDownTimer coundDownTimer;
    long millstogo; //milliseconds left on the timer
    int currentTotal = 0; //CURRENT number of questions asked
    int currentCorrect = 0; //CURRENT number of correct answers
    int currentPlusMinus = 0; // CURRENT plusminus of correct answers

    public View mainView;

    public ArrayList<Integer> wrongAnswerIds;
    public Integer previousId;

    public GridView answerGrid;
    public boolean isCorrectFirstTry = true;
    Integer totalheightofanswergrid; // This designates the individual size of the "answer" rows when the phone is in horizontal mode. It gets passed to the adapter and used there. Ignored if =0;

    private ArrayList<MultChoiceResult> questionResults;

    int widthofquestionpane = 0;
    private TextView txtTimer;
    public MultipleChoiceFragment() {
    }

    public static MultipleChoiceFragment newInstance(ArrayList<WordEntry> wordEntries
            , String quizType
            , Integer quizTimer
            , Integer quizSize
            , double totalWeight
            , String myListType
            , String colorString
            , MyListEntry myListEntry
    ) {
        MultipleChoiceFragment fragment = new MultipleChoiceFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("wordEntries", wordEntries);
        args.putString("quizType", quizType);
        args.putInt("quizSize", quizSize);
        args.putInt("quizTimer", quizTimer);
        args.putDouble("totalWeight", totalWeight);
        args.putString("myListType", myListType);
        args.putString("colorString", colorString);
        args.putParcelable("myListEntry", myListEntry);
        fragment.setArguments(args);
        return fragment;
    }

    public static MultipleChoiceFragment newInstanceSingleUser(ArrayList<WordEntry> wordEntries
            , String quizType
            , Integer quizTimer
            , Integer quizSize
            , double totalWeight
            , String myListType
            , String colorString
            , UserInfo userInfo
    ) {
        MultipleChoiceFragment fragment = new MultipleChoiceFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("wordEntries", wordEntries);
        args.putString("quizType", quizType);
        args.putInt("quizSize", quizSize);
        args.putInt("quizTimer", quizTimer);
        args.putDouble("totalWeight", totalWeight);
        args.putString("myListType", myListType);
        args.putString("colorString", colorString);
        args.putParcelable("mUserInfo", userInfo);
        args.putBoolean("mSingleUser",true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState == null) {

            //Set input global data
            mDataset = getArguments().getParcelableArrayList("wordEntries");
            mQuizType = getArguments().getString("quizType");
            mQuizSize = getArguments().getInt("quizSize");
            mQuizTimer = getArguments().getInt("quizTimer");
            mTotalWeight = getArguments().getDouble("totalWeight");
            mMyListType = getArguments().getString("myListType");
            mColorString = getArguments().getString("colorString");
            mMyListEntry = getArguments().getParcelable("myListEntry");
            mUserInfo = getArguments().getParcelable("mUserInfo");
            mSingleUser = getArguments().getBoolean("mSingleUser",false);
            questionResults = new ArrayList<>();

        } else {
            mDataset = savedInstanceState.getParcelableArrayList("mDataset");
            mQuizType = savedInstanceState.getString("mQuizType");
            mQuizSize = savedInstanceState.getInt("mQuizSize");
            mQuizTimer = savedInstanceState.getInt("mQuizTimer");
            mTotalWeight = savedInstanceState.getDouble("mTotalWeight");
            mColorString = savedInstanceState.getString("mColorString");
            mMyListType = savedInstanceState.getString("mMyListType");
            mMyListEntry = savedInstanceState.getParcelable("mMyListEntry");
            questionSet = savedInstanceState.getParcelableArrayList("questionSet");
            currentTotal = savedInstanceState.getInt("currentTotal");
            currentCorrect = savedInstanceState.getInt("currentCorrect");
            currentPlusMinus = savedInstanceState.getInt("currentPlusMinus");
            wrongAnswerIds = savedInstanceState.getIntegerArrayList("wrongAnswerIds");
            previousId = savedInstanceState.getInt("previousId");
            isCorrectFirstTry = savedInstanceState.getBoolean("isCorrectFirstTry");
            questionResults = savedInstanceState.getParcelableArrayList("questionResults");
            millstogo = savedInstanceState.getLong("millstogo");
            currentCorrectAnswer = savedInstanceState.getParcelable("currentCorrectAnswer");
            mUserInfo = savedInstanceState.getParcelable("mUserInfo");
            mSingleUser = savedInstanceState.getBoolean("mSingleUser",false);
        }
        setUpQuestion((savedInstanceState == null));

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Set layout depending on screen orientation
        TypedValue tv = new TypedValue();
        int displayheight = getResources().getDisplayMetrics().heightPixels;
        int actionBarHeight = 0;
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mainView = LayoutInflater.from(getActivity()).inflate(R.layout.quizmultchoice, null);

            widthofquestionpane = metrics.widthPixels;
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Display width in px is " + metrics.widthPixels);
                Log.d(TAG, "Display height in px is " + metrics.heightPixels);
            }

            totalheightofanswergrid = (int) ((float) (displayheight - actionBarHeight) * 0.5);

            LinearLayout layouttop = (LinearLayout) mainView.findViewById(R.id.layouttop);
            layouttop.getLayoutParams().height = (int) ((float) (displayheight - actionBarHeight) * 0.3);
            layouttop.requestLayout();

        } else {
            mainView = LayoutInflater.from(getActivity()).inflate(R.layout.quizmultchoice_l, null);
            totalheightofanswergrid = (int) ((float) (displayheight - actionBarHeight) - (float) (displayheight - actionBarHeight) * .115);
            widthofquestionpane = (int) ((float) metrics.widthPixels / 2.0f);
        }
        answerGrid = (GridView) mainView.findViewById(R.id.gridView);
        txtQuestion = (TextView) mainView.findViewById(R.id.question);
        txtFurigana = (TextView) mainView.findViewById(R.id.furigana);
        txtTotal = (TextView) mainView.findViewById(R.id.textViewTotal);
        txtScore = (TextView) mainView.findViewById(R.id.textViewScore);
        txtPlusMinus = (TextView) mainView.findViewById(R.id.textViewPlusMinus);
        return mainView;
    }


    public void setUpQuestion(boolean freshQuestion) {
        //Reset the question set
        txtTimer = (TextView) mainView.findViewById(R.id.textViewTimer);

        if(freshQuestion) {
            questionSet = new ArrayList<>();
            isCorrectFirstTry = true;
            if (wrongAnswerIds == null) {
                wrongAnswerIds = new ArrayList<>();
            }


            /* Get next question from wordEntry pool, and add it as the
            first question of the set */

            currentCorrectAnswer = getRandomWordEntry(mDataset, mTotalWeight, previousId);
            previousId = currentCorrectAnswer.getId();
            questionSet.add(currentCorrectAnswer);


            /* Get a set of word entries to fill out the rest of the options in the multiple choice grid */

            questionSet.addAll(getIncorrectAnswerSet(getContext()
                    , mMyListType
//                    , mMyListEntry
                    , SharedPrefManager.getInstance(getContext()).getColorThresholds()
                    , mColorString
                    , currentCorrectAnswer
                    , mQuizType));

            Collections.shuffle(questionSet);
        }




//        Log.i("TEST", "ELLAPSED TIME TOTAL questionSet.addall: " + (System.currentTimeMillis() - startTime) / 1000.0f);


        String stringPlusMinus;
        if (currentPlusMinus >= 0) {
            stringPlusMinus = "+" + String.valueOf(currentPlusMinus);
        } else {
            stringPlusMinus = String.valueOf(currentPlusMinus);
        }


        txtFurigana.setVisibility(View.INVISIBLE);

        //Update the Top LEFT

//            GridLayout gridTop = (GridLayout) mainView.findViewById(R.id.gridTop);


                Log.d(TAG,"mQuizTimer: " + mQuizTimer + ", millstogo: " + millstogo);
        if (mQuizTimer > 0 && millstogo > 0) {
            setUpTimer((int) millstogo / 1000);
            millstogo = 0;
        } else if (mQuizTimer > 0) {
            setUpTimer(mQuizTimer);
        }


        txtFurigana.setText("");

        String totalstring = (currentTotal + 1) + "/" + mQuizSize;
        final String totalscore = currentCorrect + "/" + currentTotal;
        txtTotal.setText(totalstring);
        txtScore.setText(totalscore);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "stringPlusMinus: " + stringPlusMinus);
        }
        txtPlusMinus.setText(stringPlusMinus);


        MultipleChoiceAdapter adapter;
        int rowheight = (int) ((float) totalheightofanswergrid / (float) 6);
        adapter = new MultipleChoiceAdapter(getContext(), questionSet, R.layout.quizmultchoice_listitem, rowheight, getResources().getDisplayMetrics().widthPixels, mQuizType, wrongAnswerIds);
        answerGrid.setAdapter(adapter);

        txtQuestion.setText(currentCorrectAnswer.getQuizQuestion(mQuizType));




//        int defsizelevel = 44;
        txtQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44);

        if(mQuizType.equals("Definition to Kanji")) {
            setTextHeightLoop(txtQuestion
                    ,currentCorrectAnswer.getDefinitionMultiLineString(10)
                    ,getResources().getDisplayMetrics());

        }


        txtQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView txtFurigana = (TextView) mainView.findViewById(R.id.furigana);
                if (mQuizType.equals("Kanji to Definition")) {
                    if (txtFurigana.getVisibility() == View.VISIBLE) {
                        txtFurigana.setVisibility(View.INVISIBLE);
                    } else {
                        txtFurigana.setVisibility(View.VISIBLE);
                    }
                }

            }
        });

        if (mQuizType.equals("Kanji to Definition") && !currentCorrectAnswer.getFurigana().equals(currentCorrectAnswer.getKanji())) { // Don't show the furigana if the question itself is a furigna-->kanji quiz
            txtFurigana.setText(currentCorrectAnswer.getFurigana());
        }


        answerGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView parent, View v, int position, long id) {

                //Pull answer that user gave from the array of values (at [type of answer][gridview position of user click])

                //THE ANSWER WAS CORRECT
                if (questionSet.get(position).getId().equals(currentCorrectAnswer.getId())) {

                    gradeQuestionResult();


                } else {

                    // Find the clicked textview and change the background color (Red)
                    final TextView txtviewhighlight = (TextView) v.findViewById(R.id.text1);
                    txtviewhighlight.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorJukuRed));
                    wrongAnswerIds.add(questionSet.get(position).getId());
                    isCorrectFirstTry = false;
                    Runnable mMyRunnable = new Runnable() {
                        @Override
                        public void run() {
                            //Incorrect answer turns textview (and text) invisible
                            txtviewhighlight.setOnClickListener(null);
                            txtviewhighlight.setText("");
                            txtviewhighlight.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));


                        }
                    };

                    ////We're wrapping these commands in a runnable, because we want to delay their execution with the handler
                    Handler myHandler = new Handler();
                    myHandler.postDelayed(mMyRunnable, 200); //runnable contents will be delivered after delay
                }
            }
        });


    }


    public void gradeQuestionResult() {
        //Cancel the timer if it exists
        if (coundDownTimer != null) {
            coundDownTimer.cancel();
            millstogo = 0;
        }

        //Find the clicked textview and change the background color (Green), while making the others invisible
        for(WordEntry wordEntry : questionSet) {
            if(!wordEntry.getId().equals( currentCorrectAnswer.getId())) {
                wrongAnswerIds.add(wordEntry.getId());
            }
        }
        ((MultipleChoiceAdapter) answerGrid.getAdapter()).changeRowColorsAndVisibility(wrongAnswerIds,currentCorrectAnswer.getId());


        //We're wrapping these commands in a runnable, because we want to delay their execution with the handler
        Runnable mMyRunnable = new Runnable() {
            @Override
            public void run() {
                final Boolean correct;

                //IF user is correct (on first try)
                if (isCorrectFirstTry) {
                    currentTotal = currentTotal + 1;
                    currentCorrect = currentCorrect + 1;
                    currentPlusMinus = currentPlusMinus + 1;
                    currentCorrectAnswer.setTotal(currentCorrectAnswer.getTotal() + 1);
                    currentCorrectAnswer.setCorrect(currentCorrectAnswer.getCorrect() + 1);
                    //pass the fact that user was correct to tab 2
                    correct = true;
                } else {

                    currentTotal = currentTotal + 1;
                    currentPlusMinus = currentPlusMinus - 1;
                    currentCorrectAnswer.setTotal(currentCorrectAnswer.getTotal() + 1);
                    correct = false;
                }


                if (SharedPrefManager.getInstance(getContext()).getIncludemultiplechoicecores()) {
                    InternalDB.getQuizInterfaceInstance(getContext()).addWordScoreToScoreBoard(currentCorrectAnswer.getId(), currentCorrectAnswer.getTotal(), currentCorrectAnswer.getCorrect());
                }

                //String that gets passed to tab 2 (and displayed there)
                //It changes depending on quiz type
                String hashmapResult;
                switch (mQuizType) {
                    case "Kanji to Definition":

                        if (currentCorrectAnswer.getFurigana().length() > 0 && !currentCorrectAnswer.getFurigana().equals(currentCorrectAnswer.getKanji())) {
                            hashmapResult = currentCorrectAnswer.getKanji() + " (" + currentCorrectAnswer.getFurigana() + " )";
                        } else {
                            hashmapResult = currentCorrectAnswer.getKanji();
                        }
                        break;
                    case "Kanji to Kana":
                        if (currentCorrectAnswer.getFurigana().length() > 0 && !currentCorrectAnswer.getFurigana().equals(currentCorrectAnswer.getKanji())) {
                            hashmapResult = currentCorrectAnswer.getKanji() + " (" + currentCorrectAnswer.getFurigana() + " )";
                        } else {
                            hashmapResult = currentCorrectAnswer.getKanji();
                        }

                        break;
                    case "Kana to Kanji":
                        hashmapResult = currentCorrectAnswer.getKanji() + " (" + currentCorrectAnswer.getFurigana() + " )";
                        break;
                    case "Definition to Kanji":
                        hashmapResult = currentCorrectAnswer.getKanji() + "--" + currentCorrectAnswer.getDefinitionMultiLineString(10);

                        StringBuilder stringBuilder = new StringBuilder();
                        SpannableString sentenceStart = new SpannableString(currentCorrectAnswer.getKanji());


                        stringBuilder.append(sentenceStart);

                        for (int i = 1; i <= 6; i++) {
                            String s = "(" + String.valueOf(i) + ")";
                            String sNext = "(" + String.valueOf(i + 1) + ")";
                            int slength = s.length();

                            if (currentCorrectAnswer.getDefinitionMultiLineString(10).contains(s)) {
                                int endIndex = currentCorrectAnswer.getDefinitionMultiLineString(10).length();
                                if (currentCorrectAnswer.getDefinitionMultiLineString(10).contains(sNext)) { //If we can find the next "(#)" in the string, we'll use it as this definition's end point
                                    endIndex = currentCorrectAnswer.getDefinitionMultiLineString(10).indexOf(sNext);
                                }

                                String sentence = currentCorrectAnswer.getDefinitionMultiLineString(10).substring(currentCorrectAnswer.getDefinitionMultiLineString(10).indexOf(s) + slength, endIndex);
                                //Capitalize it
                                if (sentence.length() > 1) {
                                    sentence = sentence.substring(0, 1).toUpperCase() + sentence.substring(1).toLowerCase();
                                }

                                SpannableString ss1 = new SpannableString(System.getProperty("line.separator") + "\u2022 " + sentence);
                                ss1.setSpan(new AbsoluteSizeSpan(12), 0, ss1.length(), 0);
                                stringBuilder.append(ss1);
                                hashmapResult = stringBuilder.toString();

                            } else if (i == 1) { //if the thing doesn't contain a "(1)", just print the whole definition in line 1 of the array.
                                String sentence = currentCorrectAnswer.getDefinition();
                                if (sentence.length() > 1) {
                                    SpannableString ss1 = new SpannableString(System.getProperty("line.separator") + "\u2022 " + currentCorrectAnswer.getDefinition().substring(0, 1).toUpperCase() + currentCorrectAnswer.getDefinition().substring(1).toLowerCase());
                                    ss1.setSpan(new AbsoluteSizeSpan(12), 0, ss1.length(), 0);
                                    stringBuilder.append(ss1);
                                }

                                hashmapResult = stringBuilder.toString();

                            }

                        }

                        ForegroundColorSpan foregroundSpan;
                        if (correct) {
                            foregroundSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.colorJukuGreen));
                        } else {
                            foregroundSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.colorJukuRed));
                        }
                        sentenceStart.setSpan(foregroundSpan, 0, sentenceStart.length(), 0);


                        break;
                    default:
                        System.out.println("Invalid Quiz Type - " + mQuizType);
                        hashmapResult = "????";
                        break;
                }

                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "currentCorrectAnswer.getId() " + currentCorrectAnswer.getId());
                    Log.d(TAG, "correct " + correct);
                    Log.d(TAG, "hashmapResult " + hashmapResult);
                    Log.d(TAG, "currentTotal " + currentTotal);
                }


                questionResults.add(new MultChoiceResult(currentCorrectAnswer, correct, hashmapResult, currentTotal));
                wrongAnswerIds.clear();

                if (mQuizSize <= currentTotal) {
                    if(mSingleUser) {
                        mCallback.showPostQuizStatsMultipleChoiceForSingleUsersTweets(questionResults
                                , mQuizType
                                , mUserInfo
                                , false
                                , false
                                , 0
                                , currentCorrect
                                , currentTotal);
                    } else {
                        mCallback.showPostQuizStatsMultipleChoice(questionResults
                                , mQuizType
                                , mMyListEntry
                                , false
                                , false
                                , 0
                                , currentCorrect
                                , currentTotal);

                    }

                } else {
                    setUpQuestion(true);
                }
            }
        };
        // This handler sends the runnable after a slight delay (showing the new color in the textview)
        Handler myHandler = new Handler();
        myHandler.postDelayed(mMyRunnable, 90); //Message will delivered after delay

    }


    public static WordEntry getRandomWordEntry(ArrayList<WordEntry> wordEntries, double totalWeight, @Nullable Integer previousId) {
        final String TAG = "TEST-Multchoice";
        if (previousId == null) {
            previousId = -1;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "(wordweight) weightedrand total weight: " + totalWeight);
        }
        int randomIndex = -1;
        double random = Math.random() * totalWeight;
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "(wordweight) FIRST random (random # * totalweight): " + random);
        }

        boolean foundFreshIndex = false;
        int spinnerCounter = 0;
        while (!foundFreshIndex && spinnerCounter < 6) {
            for (int i = 0; i < wordEntries.size(); ++i) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "(wordweight) Hashmap index and double: " + i + " - " + wordEntries.get(i).getQuizWeight());
                }
                random -= wordEntries.get(i).getQuizWeight();
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "(wordweight) New Random: " + random);
                }
                if (random <= 0.0d) {

                    randomIndex = i;
                    /*If its the last spin on the counter, just take whatever word is found,
                     even if it is a repeat */
                    if ((randomIndex >= 0 && wordEntries.size() > randomIndex && wordEntries.get(randomIndex).getId() != previousId)) {
                        foundFreshIndex = true;
                    } else {
                        spinnerCounter += 1;
                    }
                    break;
                }
            }

        }
        return wordEntries.get(randomIndex);
    }


    //Pull and initialize the other rows (the wrong answers)
    private ArrayList<WordEntry> getIncorrectAnswerSet(Context mContext
            , String myListType
            , ColorThresholds colorThresholds
            , String colorString
            , WordEntry correctWordEntry
            , String quizType) {

        ArrayList<WordEntry> incorrectAnswerSet = new ArrayList<>();

        /*If the user has chosen difficult incorrect answers setting, break up the kanji/furigana correct answer
         into pieces, search for words that contain those pieces, and user these as the incorrect answers. This will
         result in incorrect answers that are more difficult to solve. */
        Log.i(TAG,"DIFFICULTE ANSWERS: " + SharedPrefManager.getInstance(mContext).getmDifficultAnswers());
        if (SharedPrefManager.getInstance(mContext).getmDifficultAnswers() && (!quizType.equals("Kanji to Definition"))) { //If we are choosing comparable answers and the answers are either 1: kanji to furigana, or 2: furigana to kanji

            Log.d("TEST-multchoice","DIFFICULT ANSWER WORD BREAKUP");
            //Determine whether to create incorrect answer set for the WordEntry's kanji, or furigana
            String kanjiToBreak;
            if (quizType.equals("Kanji to Kana") && correctWordEntry.getFurigana().length() > 0) {
                kanjiToBreak = correctWordEntry.getFurigana();
            } else {
                kanjiToBreak = correctWordEntry.getKanji();
            }

            //Break up word into a list of word parts
            ArrayList<String> probablekanjiparts = TweetParser.chopKanjiIntoASingleSetOfCombinations(kanjiToBreak);

            int i = 0;
            ArrayList<String> finalids = new ArrayList<>();
            if (probablekanjiparts.size() > 0) {

                while (i < probablekanjiparts.size() && finalids.size() < 5) {
                    String alreadyaddedkanji = getSelectedItemsAsString(finalids);
                    Cursor c = InternalDB.getQuizInterfaceInstance(mContext).getPossibleMultipleChoiceMatch(quizType
                            , correctWordEntry
                            , alreadyaddedkanji
                            , kanjiToBreak
                            , probablekanjiparts.get(i));

                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        while (!c.isAfterLast() && finalids.size() <= 5) {
                            finalids.add(c.getString(0));

                            if (BuildConfig.DEBUG) {
                                Log.d("Test-quizmultchoice", "Adding breakup incorrect words - Adding incorrect set word:" + c.getString(1));
                            }

                            WordEntry wordEntry = new WordEntry();
                            wordEntry.setId(c.getInt(0));
                            wordEntry.setKanji(c.getString(1));
                            wordEntry.setFurigana(c.getString(2));
                            wordEntry.setDefinition(c.getString(3));
                            wordEntry.setCorrect(c.getInt(4));
                            wordEntry.setTotal(c.getInt(5));
                            incorrectAnswerSet.add(wordEntry);

                            if (BuildConfig.DEBUG) {
                                Log.d("TEST", "Finalids adding: (" + probablekanjiparts.get(i) + ") - " + c.getString(0));
                            }
                            c.moveToNext();
                        }
                    }
                    i++;
                    c.close();
                }
            }
        } else if (myListType.equals("Tweet")) {
            if(BuildConfig.DEBUG){Log.d("TEST-multchoice","pulling tweet incorrect answers from DB");}

            int randomMaxRelatedEntries = new Random().nextInt(6 - 1) + 1;

            if(mSingleUser) {
                incorrectAnswerSet = InternalDB.getTweetInterfaceInstance(mContext).getWordsFromAUsersSavedTweets(mUserInfo, colorThresholds, colorString, correctWordEntry.getId(), randomMaxRelatedEntries);
            } else {
                incorrectAnswerSet = InternalDB.getTweetInterfaceInstance(mContext).getWordsFromATweetList(mMyListEntry, colorThresholds, colorString, correctWordEntry.getId(), randomMaxRelatedEntries);
            }
        } else {
            int randomMaxRelatedEntries = new Random().nextInt(6 - 1) + 1;
            if(BuildConfig.DEBUG){Log.d("TEST-multchoice","pulling regular word incorrect answers from DB");}
                incorrectAnswerSet = InternalDB.getWordInterfaceInstance(mContext).getWordsFromAWordList(mMyListEntry, colorThresholds, colorString, correctWordEntry.getId(), randomMaxRelatedEntries);
        }

        if(BuildConfig.DEBUG){Log.d("Test-multchoice","Initial pull wrong answer size: "+ incorrectAnswerSet.size());}


        /* Fill in remaining entries if necessary */
        final int remainingIncorrectAnswerSlots = 5 - incorrectAnswerSet.size();
        if (remainingIncorrectAnswerSlots > 0) { //We're filling the rest of the array with random records (in the case that we didn't have 6 initial records to begin with)
            Cursor c = InternalDB.getQuizInterfaceInstance(mContext).getRandomKanji(correctWordEntry.getId(), remainingIncorrectAnswerSlots);
            if (c.getCount() > 0) {
                c.moveToFirst();
                while (!c.isAfterLast()) {

                    if (BuildConfig.DEBUG) {
                        Log.d("Test-quizmultchoice", "Fill in remaining entries if necessary - Adding incorrect set word:" + c.getString(1));
                    }

                        WordEntry wordEntry = new WordEntry();
                    wordEntry.setId(c.getInt(0));
                    wordEntry.setKanji(c.getString(1));
                    wordEntry.setFurigana(c.getString(2));
                    wordEntry.setDefinition(c.getString(3));
                    wordEntry.setCorrect(c.getInt(4));
                    wordEntry.setTotal(c.getInt(5));
                    incorrectAnswerSet.add(wordEntry);
                    c.moveToNext();
                }
            }
            c.close();
        }

        Log.d("Test-multchoice","FINALL incorrect answer size: "+ incorrectAnswerSet.size());

        return incorrectAnswerSet;
    }


    public static String getSelectedItemsAsString(ArrayList<String> list) {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < list.size(); ++i) {
            if (foundOne) {
                sb.append(", ");
            }
            foundOne = true;
            sb.append(list.get(i));
        }
        return sb.toString();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (QuizFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }





    public void setUpTimer(@Nullable Integer timerseconds){

        if(timerseconds == null) {
            timerseconds = (int) millstogo / 1000;
        }
        Log.d(TAG,"timerseconds: " + timerseconds + ", countdowntimer: " + (coundDownTimer !=null));
        if(mQuizTimer>0) {
            //Update the timer, if necessary
            txtTimer.setVisibility(TextView.VISIBLE);


            coundDownTimer = new CountDownTimer((timerseconds+1)*1000, 1000) {
                public void onTick(long millisUntilFinished) {
                    txtTimer.setText(String.valueOf(millisUntilFinished / 1000));
                    millstogo = millisUntilFinished;
                }


                public void onFinish() {
                    millstogo = 0;
                    isCorrectFirstTry = false;
                      gradeQuestionResult();

                }
            }.start();

        }
    }


    public void pauseTimer() {
        if(mQuizTimer>0 && coundDownTimer != null) {
            coundDownTimer.cancel();
        }
    }

    public void resumeTimer() {
        if (mQuizTimer > 0 && coundDownTimer != null) {
            setUpTimer((int) millstogo / 1000);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (coundDownTimer != null) {
            coundDownTimer.cancel();
        }

        outState.putParcelableArrayList("mDataset", mDataset);
        outState.putString("mQuizType", mQuizType);
        outState.putInt("mQuizSize", mQuizSize);
        outState.putInt("mQuizTimer", mQuizTimer);
        outState.putDouble("mTotalWeight", mTotalWeight);
        outState.putString("mColorString", mColorString);
        outState.putString("mMyListType", mMyListType);
        outState.putParcelable("mMyListEntry", mMyListEntry);
        outState.putParcelableArrayList("questionSet", questionSet);
        outState.putLong("millstogo",millstogo);
        outState.putInt("currentTotal", currentTotal);
        outState.putInt("currentCorrect", currentCorrect);
        outState.putInt("currentPlusMinus", currentPlusMinus);
        outState.putIntegerArrayList("wrongAnswerIds", wrongAnswerIds);
        outState.putInt("previousId", previousId);
        outState.putBoolean("isCorrectFirstTry", isCorrectFirstTry);
        outState.putParcelable("currentCorrectAnswer",currentCorrectAnswer);
        outState.putParcelableArrayList("questionResults", questionResults);
        outState.putBoolean("mSingleUser",mSingleUser);
        outState.putParcelable("mUserInfo",mUserInfo);
    }
}