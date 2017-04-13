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
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.TweetParser;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by JClassic on 3/31/2017.
 */

public class MultipleChoiceFragment extends Fragment {

    String TAG = "Test-quizmultchoice";
    QuizFragmentInteractionListener mCallback;
    ArrayList<WordEntry> mDataset;
    String mQuizType;
    Integer mQuizSize;
    Integer mQuizTimer;
    double mTotalWeight;
    String mMyListType; //either "Word" or "Tweet" designating which list is being quizzed
    MyListEntry mMyListEntry;
    String mColorString;

    ArrayList<WordEntry> questionSet;

    //TODO REPLACE THIS WITH RXJAVA??
    CountDownTimer coundDownTimer;

    int currentTotal = 0; //CURRENT number of questions asked
    int currentCorrect = 0; //CURRENT number of correct answers
    int currentPlusMinus = 0; // CURRENT plusminus of correct answers

    public View mainView;

    public ArrayList<Integer> wronganswerpositions ;
    public Integer previousId;

    public TextView txtviewhighlight;
    public GridView Publicgrid;
    public boolean isCorrectFirstTry = true;
    Integer totalheightofanswergrid; // This designates the individual size of the "answer" rows when the phone is in horizontal mode. It gets passed to the adapter and used there. Ignored if =0;

    private ArrayList<MultChoiceResult> questionResults;
    Double sliderUpperBound = .50;
    Double sliderLowerBound = .025;
    int sliderCountMax = 30;
    int widthofquestionpane = 0;

    public MultipleChoiceFragment() {}

    public static MultipleChoiceFragment newInstance(ArrayList<WordEntry> wordEntries
            , String quizType
            , Integer quizTimer
            , Integer quizSize
            , double totalWeight
            , String myListType
            , String colorString
            , MyListEntry myListEntry) {
        MultipleChoiceFragment fragment = new MultipleChoiceFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("wordEntries", wordEntries);
        args.putString("quizType",quizType);
        args.putInt("quizSize",quizSize);
        args.putInt("quizTimer",quizTimer);
        args.putDouble("totalWeight",totalWeight);
        args.putString("myListType",myListType);
        args.putString("colorString",colorString);
        args.putParcelable("myListEntry",myListEntry);
        fragment.setArguments(args);
        return  fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Set input global data
        mDataset = getArguments().getParcelableArrayList("wordEntries");
        mQuizType = getArguments().getString("quizType");
        mQuizSize = getArguments().getInt("quizSize");
        mQuizTimer = getArguments().getInt("quizTimer");
        mTotalWeight = getArguments().getDouble("totalWeight");
        mMyListType = getArguments().getString("myListType");
        mColorString = getArguments().getString("colorString");
        mMyListEntry = getArguments().getParcelable("myListEntry");

        questionResults = new ArrayList<>();
        //Set layout depending on screen orientation
        TypedValue tv = new TypedValue();
        int displayheight = getResources().getDisplayMetrics().heightPixels;
        int actionBarHeight = 0;
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mainView = LayoutInflater.from(getActivity()).inflate(R.layout.quizmultchoice, null);

            widthofquestionpane = metrics.widthPixels;
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "Display width in px is " + metrics.widthPixels);
                Log.d(TAG, "Display height in px is " + metrics.heightPixels);
            }

            totalheightofanswergrid = (int)((float)(displayheight-actionBarHeight)*0.5);

            if(BuildConfig.DEBUG){
                Log.d(TAG,"diplayheight: " + displayheight);
                Log.d(TAG,"mactionbarsize: " + actionBarHeight);
                Log.d(TAG,"totalheightofanswergrid: " + totalheightofanswergrid);

            }
            LinearLayout layouttop = (LinearLayout) mainView.findViewById(R.id.layouttop);
            layouttop.getLayoutParams().height = (int)((float)(displayheight-actionBarHeight)*0.3);
            layouttop.requestLayout();

        } else {
            mainView = LayoutInflater.from(getActivity()).inflate(R.layout.quizmultchoice_l, null);
            totalheightofanswergrid = (int)((float)(displayheight-actionBarHeight)-(float)(displayheight-actionBarHeight)*.115);
            widthofquestionpane = (int) ((float)metrics.widthPixels/2.0f);
            if(BuildConfig.DEBUG){
//                Log.d(TAG,"VVScreenTotalHeight: " + displayheight);
                Log.d(TAG,"VVActionBarHeight: " + actionBarHeight);
                Log.d(TAG,"VVtotalheightofanswergrid: " + totalheightofanswergrid);

            }
        }


        setUpQuestion();

        return mainView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    public void setUpQuestion() {
        //Reset the question set
        questionSet = new ArrayList<>();
        isCorrectFirstTry = true;
        if(wronganswerpositions == null) {
            wronganswerpositions = new ArrayList<>();
        }
        /* Get next question from wordEntry pool, and add it as the
         first question of the set */



        final WordEntry currentCorrectAnswer = getRandomWordEntry(mDataset,mTotalWeight,previousId);

        previousId = currentCorrectAnswer.getId();

        questionSet.add(currentCorrectAnswer);


        /* Get a set of word entries to fill out the rest of the options in the multiple choice grid */
        long startTime = System.currentTimeMillis();
         questionSet.addAll(getIncorrectAnswerSet(getContext()
                ,mMyListType
                ,mMyListEntry
                ,SharedPrefManager.getInstance(getContext()).getColorThresholds()
                ,mColorString
                ,currentCorrectAnswer
                ,mQuizType));
        Log.i("TEST","ELLAPSED TIME TOTAL questionSet.addall: " + (System.currentTimeMillis() - startTime)/1000.0f);


            String stringPlusMinus;
            if(currentPlusMinus>=0) {
                stringPlusMinus = "+" + String.valueOf(currentPlusMinus);
            } else {
                stringPlusMinus = String.valueOf(currentPlusMinus);
            }

            Publicgrid = (GridView) mainView.findViewById(R.id.gridView);

            TextView txtQuestion = (TextView) mainView.findViewById(R.id.question);
            TextView txtFurigana = (TextView) mainView.findViewById(R.id.furigana);
            txtFurigana.setVisibility(View.INVISIBLE);

            //Update the Top LEFT
            TextView txtTotal = (TextView) mainView.findViewById(R.id.textViewTotal);
            TextView txtScore = (TextView) mainView.findViewById(R.id.textViewScore);
            TextView txtPlusMinus = (TextView) mainView.findViewById(R.id.textViewPlusMinus);

//            GridLayout gridTop = (GridLayout) mainView.findViewById(R.id.gridTop);

//            if (timer > 0 && millstogo >0) {
//                setUpTimer((int) millstogo / 1000);
//                millstogo =0;
//            } else if (timer>0) {
//                setUpTimer(timer);
//            }

            txtFurigana.setText("");

            String totalstring = (currentTotal +1)+ "/" + mQuizSize;
            final String totalscore = currentCorrect + "/" + currentTotal;
            txtTotal.setText(totalstring);
            txtScore.setText(totalscore);
            if(BuildConfig.DEBUG) {Log.d(TAG,"stringPlusMinus: " + stringPlusMinus);}
            txtPlusMinus.setText(stringPlusMinus);

            Collections.shuffle(questionSet);
            MultipleChoiceAdapter adapter;
            int rowheight = (int)((float)totalheightofanswergrid/(float)6);
            adapter = new MultipleChoiceAdapter(getContext(), questionSet, R.layout.quizmultchoice_listitem,rowheight, getResources().getDisplayMetrics().widthPixels,mQuizType,wronganswerpositions);
            Publicgrid.setAdapter(adapter);

            txtQuestion.setText(currentCorrectAnswer.getQuizQuestion(mQuizType));

            //TODO Replace this with flashcard size level
            int defsizelevel = 44;
            txtQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP, defsizelevel);
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

//            if(mQuizType.equals("English to Kanji")) { //if its english to kanji, must change size of text based on screenwidth, etc
//                defsizelevel = 40;
//                txtQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP, defsizelevel);
//
//                /** Get width of screen */
//                DisplayMetrics metrics = new DisplayMetrics();
//                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
//
//                Rect bounds = new Rect();
//                Paint textPaint = txtQuestion.getPaint();
//                textPaint.getTextBounds(PublicrandomQuestion, 0, PublicrandomQuestion.length(), bounds);
//                int width = bounds.width();
//                // if question is more than 2x size of screen
//
//
//                if(PublicrandomQuestion.contains("(1)")) {
//
//                    /** IF TEXT IS DEFINITION, SPLIT OUT THE DIFFERENT DEFINITIONS INTO BULLET POINTS */
//                    StringBuilder stringBuilder = new StringBuilder();
//                    int definitionaddedcounter = 0;
//                    for (int i=1; i<=6; i++) {
//                        String s = "(" + String.valueOf(i) + ")";
//                        String sNext = "(" + String.valueOf(i + 1) + ")";
//
//                        int slength = s.length();
//
//                        if (PublicrandomQuestion.contains(s)) {
//
//                            int endIndex = PublicrandomQuestion.length();
//                            if (PublicrandomQuestion.contains(sNext)) { //If we can find the next "(#)" in the string, we'll use it as this definition's end point
//                                endIndex = PublicrandomQuestion.indexOf(sNext);
//                            }
//
//                            String sentence = PublicrandomQuestion.substring(PublicrandomQuestion.indexOf(s) + slength, endIndex);
//                            //Capitalize it
//                            if (sentence.length() > 1) {
//                                sentence = sentence.substring(0, 1).toUpperCase() + sentence.substring(1).toLowerCase();
//                            }
//                            stringBuilder.append("\u2022 " + sentence + System.getProperty("line.separator"));
//                            definitionaddedcounter += 1;
//
//                            if(definitionaddedcounter< 3) {
//                                defsizelevel = 26;
//
//                            } else {
//                                defsizelevel = 24;
//                            }
//
//                            txtQuestion.setText(stringBuilder.toString());
//
//                        } else if (i == 1) { //if the thing doesn't contain a "(1)", just print the whole definition in line 1 of the array.
//                            String sentence = PublicrandomQuestion;
//                            if (sentence.length() > 1) {
//                                sentence = PublicrandomQuestion.substring(0, 1).toUpperCase() + PublicrandomQuestion.substring(1).toLowerCase();
//                            }
//
//                            if(widthofquestionpane >0 && width/widthofquestionpane >2) {
//                                defsizelevel = 28;
//                            } else {
//                                defsizelevel = 32;
//                            }
//                            stringBuilder.append(sentence);
//                        }
//                    }
//
//                    txtQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP,defsizelevel);
//                } else {
//                    if((float)width/(float)widthofquestionpane > 2) {
//                        defsizelevel = 28;
//                    } else {
//                        /** Shrink the single definition so it's less than the screen, unless it's like twice as big */
//                        while (width>widthofquestionpane && defsizelevel >26) {
//                            defsizelevel = defsizelevel-2;
//                            txtQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP,defsizelevel);
//                            Rect boundsQuestionShrink = new Rect();
//                            Paint textPaintQuestionShrink = txtQuestion.getPaint();
//                            textPaintQuestionShrink.getTextBounds(PublicrandomQuestion, 0, PublicrandomQuestion.length(), boundsQuestionShrink);
//                            width = bounds.width();
//                        }
//
//                    }
//
//                    txtQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP,defsizelevel);
//                }

//            }

            if(mQuizType.equals("Kanji to Definition") && !currentCorrectAnswer.getFurigana().equals(currentCorrectAnswer.getKanji())) { // Don't show the furigana if the question itself is a furigna-->kanji quiz
                txtFurigana.setText(currentCorrectAnswer.getFurigana());
            }




            Publicgrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView parent, View v, int position, long id) {

                    //Pull answer that user gave from the array of values (at [type of answer][gridview position of user click])


                    //THE ANSWER WAS CORRECT
                    if (questionSet.get(position).getId() == currentCorrectAnswer.getId()) {

                        //Cancel the timer if it exists
//                        if (coundDownTimer != null) {
//                            coundDownTimer.cancel();
//                            millstogo = 0;
//                        }

                        //Find the clicked textview and change the background color (Green)
                        txtviewhighlight = (TextView) v.findViewById(R.id.text1);
                        txtviewhighlight.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorJukuGreen));

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
                                    currentCorrectAnswer.setTotal(currentCorrectAnswer.getTotal()+1);
                                    currentCorrectAnswer.setCorrect(currentCorrectAnswer.getCorrect()+1);
                                    //pass the fact that user was correct to tab 2
                                    correct = true;
                                } else {

                                    currentTotal = currentTotal + 1;
                                    currentPlusMinus = currentPlusMinus - 1;
                                    currentCorrectAnswer.setTotal(currentCorrectAnswer.getTotal()+1);
                                    correct = false;
                                }



                                if(SharedPrefManager.getInstance(getContext()).getIncludemultiplechoicecores()) {
                                    InternalDB.getQuizInterfaceInstance(getContext()).addWordScoreToScoreBoard(currentCorrectAnswer.getId(),currentCorrectAnswer.getTotal(),currentCorrectAnswer.getCorrect());
                                }

                                //String that gets passed to tab 2 (and displayed there)
                                //It changes depending on quiz type
                                String hashmapResult;
                                switch (mQuizType) {
                                    case "Kanji to Definition":

                                        if(currentCorrectAnswer.getFurigana().length()>0 && !currentCorrectAnswer.getFurigana().equals(currentCorrectAnswer.getKanji())) {
                                            hashmapResult = currentCorrectAnswer.getKanji() + " (" + currentCorrectAnswer.getFurigana() +" )" ;
                                        } else {
                                            hashmapResult = currentCorrectAnswer.getKanji() ;
                                        }
                                        break;
                                    case "Kanji to Kana":
                                        if(currentCorrectAnswer.getFurigana().length()>0 && !currentCorrectAnswer.getFurigana().equals(currentCorrectAnswer.getKanji())) {
                                            hashmapResult = currentCorrectAnswer.getKanji() + " (" + currentCorrectAnswer.getFurigana() +" )" ;
                                        } else {
                                            hashmapResult = currentCorrectAnswer.getKanji() ;
                                        }

                                        break;
                                    case "Kana to Kanji":
                                        hashmapResult = currentCorrectAnswer.getKanji() + " (" + currentCorrectAnswer.getFurigana() +" )" ;
                                        break;
                                    case "Definition to Kanji":
                                        hashmapResult = currentCorrectAnswer.getKanji() + "--" + currentCorrectAnswer.getDefinitionMultiLineString(10);

                                        StringBuilder stringBuilder = new StringBuilder();
                                        SpannableString sentenceStart = new SpannableString(currentCorrectAnswer.getKanji());


                                        stringBuilder.append(sentenceStart);

                                        for (int i=1; i<=6; i++) {
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

                                                SpannableString ss1=  new SpannableString(System.getProperty("line.separator") + "\u2022 " + sentence );
                                                ss1.setSpan(new AbsoluteSizeSpan(12), 0, ss1.length(), 0);
                                                stringBuilder.append(ss1);
                                                hashmapResult = stringBuilder.toString();

                                            } else if (i == 1) { //if the thing doesn't contain a "(1)", just print the whole definition in line 1 of the array.
                                                String sentence = currentCorrectAnswer.getDefinition();
                                                if (sentence.length() > 1) {
                                                    SpannableString ss1=  new SpannableString(System.getProperty("line.separator") + "\u2022 " + currentCorrectAnswer.getDefinition().substring(0, 1).toUpperCase() + currentCorrectAnswer.getDefinition().substring(1).toLowerCase());
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

                                Log.d(TAG,"currentCorrectAnswer.getId() " + currentCorrectAnswer.getId());
                                Log.d(TAG,"correct " + correct);
                                Log.d(TAG,"hashmapResult " + hashmapResult);
                                Log.d(TAG,"currentTotal " + currentTotal);


                                questionResults.add(new MultChoiceResult(currentCorrectAnswer.getId(),correct,hashmapResult,currentTotal));
                                wronganswerpositions.clear();

                                if (mQuizSize <= currentTotal) {
                                    mCallback.showPostQuizStatsMultipleChoice(questionResults
                                            ,mQuizType
                                            ,mMyListEntry
                                    ,false
                                    ,false
                                    ,0
                                    ,currentCorrect
                                    ,currentTotal);

                                } else {
                                    setUpQuestion();
                                }


                            }
                        };

                        // This handler sends the runnable after a slight delay (showing the new color in the textview)
                        Handler myHandler = new Handler();
                        myHandler.postDelayed(mMyRunnable, 90); //Message will delivered after delay

                    } else {

                        // Find the clicked textview and change the background color (Red)
                        final TextView txtviewhighlight = (TextView) v.findViewById(R.id.text1);
                        txtviewhighlight.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorJukuRed));
                        wronganswerpositions.add(questionSet.get(position).getId());
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


    public static WordEntry getRandomWordEntry(ArrayList<WordEntry> wordEntries, double totalWeight, @Nullable Integer previousId) {
        final String TAG = "TEST-Multchoice";
        if(previousId == null) {
            previousId = -1;
        }
        if(BuildConfig.DEBUG) {Log.d(TAG,"(wordweight) weightedrand total weight: " + totalWeight);}
        int randomIndex = -1;
        double random = Math.random() * totalWeight;
        if(BuildConfig.DEBUG) {Log.d(TAG,"(wordweight) FIRST random (random # * totalweight): " + random);}

        boolean foundFreshIndex = false;
        int spinnerCounter = 0;
        while(!foundFreshIndex && spinnerCounter<6) {
            for (int i = 0; i < wordEntries.size(); ++i)
            {
                if(BuildConfig.DEBUG) {Log.d(TAG,"(wordweight) Hashmap index and double: " + i + " - " + wordEntries.get(i).getQuizWeight());}
                random -= wordEntries.get(i).getQuizWeight();
                if(BuildConfig.DEBUG) {Log.d(TAG, "(wordweight) New Random: " + random);}
                if (random <= 0.0d)
                {

                    randomIndex = i;
                    /*If its the last spin on the counter, just take whatever word is found,
                     even if it is a repeat */
                    if((randomIndex >=0 && wordEntries.size()>randomIndex && wordEntries.get(randomIndex).getId()!= previousId)) {
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
    public static ArrayList<WordEntry> getIncorrectAnswerSet(Context mContext
            , String myListType
            , MyListEntry myListEntry
            , ColorThresholds colorThresholds
            , String colorString
            , WordEntry correctWordEntry
            , String quizType) {

        ArrayList<WordEntry> incorrectAnswerSet = new ArrayList<>();

        /*If the user has chosen difficult incorrect answers setting, break up the kanji/furigana correct answer
         into pieces, search for words that contain those pieces, and user these as the incorrect answers. This will
         result in incorrect answers that are more difficult to solve. */

        if(SharedPrefManager.getInstance(mContext).getmDifficultAnswers() && (!quizType.equals("Kanji to Definition"))) { //If we are choosing comparable answers and the answers are either 1: kanji to furigana, or 2: furigana to kanji

            //Determine whether to create incorrect answer set for the WordEntry's kanji, or furigana
            String kanjiToBreak;
            if(quizType.equals("Kanji to Kana") && correctWordEntry.getFurigana().length() > 0){
                kanjiToBreak = correctWordEntry.getFurigana();
            } else {
                kanjiToBreak = correctWordEntry.getKanji();
            }

            //Break up word into a list of word parts
            ArrayList<String> probablekanjiparts = TweetParser.chopKanjiIntoASingleSetOfCombinations(kanjiToBreak);

            int i = 0;
            ArrayList<String> finalids = new ArrayList<>();
            if(probablekanjiparts.size()>0){

                while(i<probablekanjiparts.size() && finalids.size()<5) {
                    String alreadyaddedkanji = getSelectedItemsAsString(finalids);
                        Cursor c = InternalDB.getQuizInterfaceInstance(mContext).getPossibleMultipleChoiceMatch(quizType
                                ,correctWordEntry
                                ,alreadyaddedkanji
                                ,kanjiToBreak
                                ,probablekanjiparts.get(i));

                    if(c.getCount()>0) {
                        c.moveToFirst();
                        while(!c.isAfterLast() && finalids.size()<=5) {
                            finalids.add(c.getString(0));

                            WordEntry wordEntry = new WordEntry();
                            wordEntry.setId(c.getInt(0));
                            wordEntry.setKanji(c.getString(1));
                            wordEntry.setFurigana(c.getString(2));
                            wordEntry.setDefinition(c.getString(3));
                            wordEntry.setCorrect(c.getInt(4));
                            wordEntry.setTotal(c.getInt(5));
                            incorrectAnswerSet.add(wordEntry);

                            if(BuildConfig.DEBUG) {Log.d("TEST", "Finalids adding: (" + probablekanjiparts.get(i) + ") - " + c.getString(0));}
                            c.moveToNext();
                        }
                    }
                    i++;
                    c.close();
                }
            }
        } else if(myListType.equals("Tweet")) {
        incorrectAnswerSet = InternalDB.getTweetInterfaceInstance(mContext).getWordsFromATweetList(myListEntry,colorThresholds,colorString,correctWordEntry.getId(),6);
        } else {

            incorrectAnswerSet = InternalDB.getWordInterfaceInstance(mContext).getWordsFromAWordList(myListEntry,colorThresholds,colorString,correctWordEntry.getId(),6);

        }



        /* Fill in remaining entries if necessary */
        final int remainingIncorrectAnswerSlots = 5-incorrectAnswerSet.size();
        if(remainingIncorrectAnswerSlots>0) { //We're filling the rest of the array with random records (in the case that we didn't have 6 initial records to begin with)
            long startTime = System.currentTimeMillis();
            Cursor c = InternalDB.getQuizInterfaceInstance(mContext).getRandomKanji(String.valueOf(correctWordEntry.getId()),remainingIncorrectAnswerSlots);
            Log.i("TEST","ELLAPSED TIME TOTAL other block: " + (System.currentTimeMillis() - startTime)/1000.0f);
            if(c.getCount()>0) {
                c.moveToFirst();
                while (!c.isAfterLast()) {

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


        return incorrectAnswerSet;
    }


    //TODO -- COMBINE THIS SHIT
    public static String getSelectedItemsAsString(ArrayList<String> list ) {
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

//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        ...
//        if (savedInstanceState != null) {
//            //Restore the fragment's state here
//        }
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        //Save the fragment's state here
//
//    }

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

}
