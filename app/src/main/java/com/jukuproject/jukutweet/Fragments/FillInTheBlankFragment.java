package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog;
import com.jukuproject.jukutweet.Interfaces.QuizFragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.WordEntryFavoritesChangedListener;
import com.jukuproject.jukutweet.Models.FillinSentencesSpinner;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;

import java.util.ArrayList;


/**
 * Fill in the blank quiz. For each question, a tweet is broken down into component pieces (Kanji, "spinner" quiz questions,
 * and other miscellanious text), and reassembled in the question window. User selects kanji from the spinners and clicks "Score"
 * to score answers. User cannot go on to the next question until all incorrect answers have been corrected.
 *
 * @see com.jukuproject.jukutweet.QuizActivity
 */
public class FillInTheBlankFragment extends Fragment implements WordEntryFavoritesChangedListener {

    String TAG = "TEST-fillinblank";

    private int currentTotal = 0; //CURRENT number of questions asked
    private int currentCorrect = 0; //CURRENT numer of tweets where all answers were correct on the firt try
    private int currentDataSetindex = 0; //Current position within dataset (reset to 0 when shuffling)

    private QuizFragmentInteractionListener mCallback;
    private ArrayList<Tweet> mDataset;
    private Integer mQuizSize;
    private double mTotalWeight;
    private MyListEntry mMyListEntry;
    private String mColorString;
    private UserInfo mUserInfo;
    private boolean mSingleUser; //Designates whether the quiz activity is from a single user saved tweets (true), or a tweet/word list (false)
    private int currentLineWidth = 0;
    private int displayWidth = 0;
    private int displayMarginPadding = 30; //How much to pad the edge of the screen by when laying down the sentenceblocks (so the sentence doesn't overlap the screen or get cut up too much)
    private int spinnerWidth = 200;
    private int redundentQuestionCounter = 0;

    private LinearLayout linearLayoutVerticalParagraph;  //This is the main linear layout, that we will fill row by row with horizontal linear layouts, which are     // in turn filled with vertical layouts (with furigana on top and japanese on bottom)
    private LinearLayout linearLayoutHorizontalLine; //one of these layouts for each line in the vertical paragraph
    private TextView scoreButton;
    private TextView txtQuestionNumber;

    public FillInTheBlankFragment() {}

    public static FillInTheBlankFragment newInstance(ArrayList<Tweet> tweets
            , String quizSize
            , double totalWeight
            , String colorString
            , MyListEntry myListEntry
    ) {
        FillInTheBlankFragment fragment = new FillInTheBlankFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("dataset", tweets);
        args.putString("quizSize",quizSize);
        args.putDouble("totalWeight",totalWeight);
        args.putString("colorString",colorString);
        args.putParcelable("myListEntry",myListEntry);
        fragment.setArguments(args);
        return  fragment;
    }


    public static FillInTheBlankFragment newSingleUserInstance(ArrayList<Tweet> tweets
            , String quizSize
            , double totalWeight
            , String colorString
            , UserInfo userInfo
    ) {
        FillInTheBlankFragment fragment = new FillInTheBlankFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("dataset", tweets);
        args.putString("quizSize",quizSize);
        args.putDouble("totalWeight",totalWeight);
        args.putString("colorString",colorString);
        args.putParcelable("userInfo",userInfo);
        args.putBoolean("singleUser",true);
        fragment.setArguments(args);
        return  fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_fillintheblanks, null);
        linearLayoutVerticalParagraph = (LinearLayout) view.findViewById(R.id.sentence_layout);
        txtQuestionNumber = (TextView) view.findViewById(R.id.textViewTotal);
        scoreButton = (TextView) view.findViewById(R.id.scoreButton);
        return view;
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /* Get width of screen */
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        displayWidth = metrics.widthPixels;
        displayMarginPadding = (int) ((float) (displayWidth) * 0.055555556);
        spinnerWidth = (int) (180.0f * metrics.density + 0.5f);

        linearLayoutHorizontalLine = new LinearLayout(getContext());
        linearLayoutHorizontalLine.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutHorizontalLine.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));


        if (savedInstanceState != null) {
            //Save the fragment's state here
            mDataset = savedInstanceState.getParcelableArrayList("mDataset");
            mQuizSize = savedInstanceState.getInt("mQuizSize");
            mTotalWeight = savedInstanceState.getDouble("mTotalWeight");
            mColorString = savedInstanceState.getString("mColorString");
            mMyListEntry = savedInstanceState.getParcelable("mMyListEntry");
            currentTotal = savedInstanceState.getInt("currentTotal");
            currentCorrect = savedInstanceState.getInt("currentCorrect");
            currentLineWidth = savedInstanceState.getInt("currentLineWidth");
            mUserInfo = savedInstanceState.getParcelable("mUserInfo");
            mSingleUser = savedInstanceState.getBoolean("mSingleUser",false);
            currentDataSetindex = savedInstanceState.getInt("currentDataSetindex",0);
        } else {
            mDataset = getArguments().getParcelableArrayList("dataset");
            mQuizSize = Integer.parseInt(getArguments().getString("quizSize"));
            mTotalWeight = getArguments().getDouble("totalWeight");
            mColorString = getArguments().getString("colorString");
            mMyListEntry = getArguments().getParcelable("myListEntry");
            mUserInfo = getArguments().getParcelable("userInfo");
            mSingleUser = getArguments().getBoolean("singleUser",false);

            currentDataSetindex = 0;

        }

        /* Reset the lists and layouts */
        txtQuestionNumber.setText((currentTotal +1) + "/" + mQuizSize);
        scoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               scoreTheSpinners();
            }
        });

        //Set up tweet and spinners
        setUpQuestion(mDataset.get(currentDataSetindex));
    }


    /**
     * Sets up a fresh question, creating a set of textviews for the contents of the tweet(broken
     * down into "spinners" that the user interacts with to pick the right answer, WordEntries that
     * are not part of the quiz but can be clicked on to show the {@link WordDetailPopupDialog}, and misc text) and
     * arraying these textviews in the linearLayoutVerticalParagraph view so that the
     * tweet is recreated
     * @param tweet Tweet that is being used as the quiz question
     */
    private void setUpQuestion(Tweet tweet) {
        String sentence = tweet.getText();
        ArrayList<WordEntry> disectedSavedTweet = tweet.getWordEntries();

        /*In the case that the tweet does not, for some reason, have any words
          associated with it, more to the next tweet in the dataset and add a new tweet
          at the end to keep the total question numbers correct. If, for whatever reason,
          the process keeps spinning through the process of trying to get a fresh question, the
          user will be kicked back to the main menu with an error message */
        if(redundentQuestionCounter>4) {
            mCallback.emergencyGoBackToMainActivity();
        } else if(disectedSavedTweet.size()==0) {
            Log.e(TAG,"Saved tweets in fillintheblanks were empty! moving to next question, and pulling a new question");
            redundentQuestionCounter +=1;
            replaceDataSet(tweet.getIdString(),currentDataSetindex);
            moveToNextQuestion();
        } else {
            currentLineWidth = 0;
            linearLayoutVerticalParagraph.removeAllViews();
            linearLayoutHorizontalLine = new LinearLayout(getContext());
            linearLayoutHorizontalLine.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            /* Set tweet color spans. If the saved Tweet object includes a "colorIndex" object (
            * which comes from the savedTweetKanji table and contains the id, positions and color designation
            * of each kanji in the TWeet), replace the normal Tweet text with colored spans for those kanji */
            try {

                int startIndex = 0;
                SpannableStringBuilder sb = new SpannableStringBuilder();
                for(final WordEntry wordEntry : disectedSavedTweet) {

                    if(wordEntry.getKanji() == null) {
                        throw new NullPointerException("fillintheblank word entry has null kanji!");
                    }
                /* If its a spinner word:
                    * 1. Put the current stringbuilder contents into a textview
                    * and enter it into the LinearLayout input process.
                    * 2. Create the spinner and its contents, and put it into the linearlayout process
                    * */
                    if(BuildConfig.DEBUG) {
                        Log.d(TAG,"is spinner? " + wordEntry.isSpinner() + ", adding word entry: " + wordEntry.getKanji() + ", start: " + wordEntry.getStartIndex() + ", end: " + wordEntry.getEndIndex());
                    }

                /*Add the portion of normal, non-kanji, non-spinner text between the previous
                 kanji end-index and the current kanji start-index to the layout*/
                    if(wordEntry.getStartIndex()>startIndex) {
                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "adding previous word chunk: " + sentence.substring(startIndex, wordEntry.getStartIndex()));
                        }
                        sb.append(sentence.substring(startIndex,wordEntry.getStartIndex()));

                        if(sb.toString().contains(System.getProperty("line.separator"))) {
                            insertTextWithLineSeperators(sb.toString());

                        } else {
                            addToLayout(sb);
                        }

                        sb = new SpannableStringBuilder();

                    }

                    if(wordEntry.isSpinner()) {
                        addSpinnerToLayout(wordEntry);
                        startIndex = wordEntry.getEndIndex();
                    } else {

                        sb.append(sentence.substring(wordEntry.getStartIndex(),wordEntry.getEndIndex()));

                        ClickableSpan kanjiClick = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                WordDetailPopupDialog wordDetailPopupDialog = WordDetailPopupDialog.newInstance(wordEntry);
                                wordDetailPopupDialog.setTargetFragment(FillInTheBlankFragment.this, 0);
                                wordDetailPopupDialog.show(getFragmentManager(),"wordDetailPopup");
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setColor(ContextCompat.getColor(getContext(),wordEntry.getColorValue()));
                                ds.setUnderlineText(false);
                            }
                        };

                        sb.setSpan(kanjiClick, 0, sb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        addToLayout(sb);
                        startIndex = wordEntry.getEndIndex();
                        sb = new SpannableStringBuilder();
                    }

                }

                //Add the last part of the sentence
                if(startIndex<sentence.length()) {
                    sb = new SpannableStringBuilder();
                    sb.append(sentence.substring(startIndex,sentence.length()));
                    if(sb.toString().contains(System.getProperty("line.separator"))) {
                        insertTextWithLineSeperators(sb.toString());
                    } else {
                        addToLayout(sb);
                    }


                }
                //It's the last line, so insert it regardless
                if(currentLineWidth>0) {
                    if(BuildConfig.DEBUG) {
                        Log.d(TAG,"INSERTING LAST LINE INTO PARAGRAPH");
                    }
                    insertLineIntoParagraph();
                }

            } catch (NullPointerException e) {
                if(redundentQuestionCounter<4) {
                    moveToNextQuestion();
                    redundentQuestionCounter += 1;
                    Log.e(TAG,"Adding sentence pieces to fillblanks generic failure: " + e.getCause());
                } else {
                    //Kick back to main menu
                    mCallback.emergencyGoBackToMainActivity();
                }
            }

        }

    }


    /**
     * Takes a piece of the tweet and adds it to linearLayoutHorizontalLine as a textView.
     *
     * Note: It gets tricky if the text that is being added is longer than the edge of the screen. In this case, it will
     * be broken into two textviews, one will be added to the current view in teh paragraph, then a new line
     * will be created, and the remainder will be added
     * @param text text to be added to the layout
     */
    public void addToLayout(final SpannableStringBuilder text) {

         if(BuildConfig.DEBUG) {Log.d(TAG, "Adding to layout  " +
                 text);}

            //Estimate the length of the chunk of text
            int estimatedTextViewWidth = getEstimatedTextViewWidth(text.toString());
            int maxWidthAllowed = displayWidth - currentLineWidth - displayMarginPadding;
            int widthExtra = estimatedTextViewWidth - maxWidthAllowed;

            if(BuildConfig.DEBUG) {
                    Log.d(TAG, "text content = " + text.toString());
                    Log.d(TAG, "estimatedTextViewWidth = " + estimatedTextViewWidth);
                    Log.d(TAG, "currentLineWidth = " + currentLineWidth);
                Log.d(TAG, "maxWidthAllowed= " + maxWidthAllowed);
                Log.d(TAG, "widthExtra = " + widthExtra);

            }

            /* If the width of text chunk is overruns the allowable length of a line:
            * 1. estimate the maximum allowable characters that will fit on the line,
            * 2. chop the text chunk into 2 pieces, one that fills the line and a "remainder" piece
            * 3. input the full line and re-run the process for the remainder piece until all  of the text has been completely added */

            if (widthExtra > 0) {

                int substringstart = 0;
                int estimatedSubStringEnd = Math.round(((float) maxWidthAllowed / (float) estimatedTextViewWidth) * text.length());
                CharSequence textRemaining = text;

                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "substringstart: " + substringstart);
                    Log.d(TAG, "estimatedSubStringEnd: " + estimatedSubStringEnd);
                    Log.d(TAG, "textRemaining: " + textRemaining);
                }

                while (widthExtra > 0) {

                    if(estimatedSubStringEnd>substringstart) {
                        /* Create the first portion of the chopped up text what will fit to the end of the current line */
                        CharSequence textToAdd = textRemaining.subSequence(substringstart, estimatedSubStringEnd);
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "textToAdd: " + textToAdd);
                        }
                        TextView textView = new TextView(getContext());
                        textView.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));

                        textView.setText(textToAdd);
                        textView.setMovementMethod(LinkMovementMethod.getInstance());
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        linearLayoutHorizontalLine.addView(textView);

                        insertLineIntoParagraph();

                    } else if(substringstart == estimatedSubStringEnd ){
                            insertLineIntoParagraph();
                    }

                    estimatedTextViewWidth = getEstimatedTextViewWidth(textRemaining.subSequence(estimatedSubStringEnd, textRemaining.length()).toString());
                    maxWidthAllowed = displayWidth - currentLineWidth - displayMarginPadding;
                    widthExtra = estimatedTextViewWidth - maxWidthAllowed;

                    /* ADD THE TRAILING TEXT */
                    if(widthExtra<0) {
                        CharSequence textToAdd = textRemaining.subSequence(estimatedSubStringEnd, textRemaining.length());
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "trailing textToAdd: " + textToAdd);
                        }
                        TextView textView = new TextView(getContext());
                        textView.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));

                        textView.setText(textToAdd);
                        textView.setMovementMethod(LinkMovementMethod.getInstance());
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        linearLayoutHorizontalLine.addView(textView);

                        currentLineWidth = currentLineWidth + getEstimatedTextViewWidth(textToAdd.toString());
                    }

                        substringstart = estimatedSubStringEnd;
                        textRemaining = text.subSequence(substringstart, text.length());
                        estimatedSubStringEnd = Math.round(((float) maxWidthAllowed / (float) estimatedTextViewWidth) * textRemaining.length());
                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "Updated widthExtra: " + widthExtra);
                            Log.d(TAG, "Updated estimatedTextViewWidth: " + estimatedTextViewWidth);
                            Log.d(TAG, "Updated substringstart: " + substringstart);
                            Log.d(TAG, "Updated textRemaining: " + textRemaining);
                        }

                    }

            } else {

                TextView textView = new TextView(getContext());
                textView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                textView.setText(text);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                linearLayoutHorizontalLine.addView(textView);
                currentLineWidth = currentLineWidth + estimatedTextViewWidth;
                insertLineIntoParagraph((currentLineWidth + displayMarginPadding), displayWidth);

                if(BuildConfig.DEBUG) {Log.d(TAG, "new currentLineWidth = " + currentLineWidth);}

            }
    }

    /**
     * Like {@link #addToLayout(SpannableStringBuilder)}, but handles a specific situation in which
     * the text to be added to the linearLayoutHorizontalLine contains line seperators. This process
     * finds them and mimicks them by forcing a new line in the paragraph via {@link #insertLineIntoParagraph()}. Otherwise
     * the final result looks all screwy.
     * @param textToInsert text to be added to the layout
     */
    private void insertTextWithLineSeperators(String textToInsert) {
        String remainingLineSepText = textToInsert;
        if(BuildConfig.DEBUG) {Log.d(TAG, "initial text to insert: " + textToInsert);}

        while(remainingLineSepText.contains(System.getProperty("line.separator"))) {

            int lineSepEndIndex = remainingLineSepText.indexOf(System.getProperty("line.separator"));
            if(BuildConfig.DEBUG) {Log.d(TAG, "BEGIN LOOP linesependindex: " + lineSepEndIndex + ", texttoinsert length: " + textToInsert.length());}
            String preLineSepTextToAdd = remainingLineSepText.substring(0,lineSepEndIndex);
            if(BuildConfig.DEBUG) {Log.d(TAG,"ADding to layout pre line sep text: " + preLineSepTextToAdd);}
            addToLayout(new SpannableStringBuilder(preLineSepTextToAdd));

            //Mimic the line seperator by creating a new line
            insertLineIntoParagraph();

            //Remaining text begins after the line seperatore, until the end of the stringbuilder
            remainingLineSepText =  remainingLineSepText.substring(lineSepEndIndex + 1,remainingLineSepText.length());
            if(BuildConfig.DEBUG) {Log.d(TAG, "remainingLineSepText: " + remainingLineSepText);}
        }
        if(remainingLineSepText.length()>0) {
            if(BuildConfig.DEBUG){Log.d(TAG,"final remainingLineSepText to add: " + remainingLineSepText);}
            addToLayout(new SpannableStringBuilder(remainingLineSepText));
        }
    }


    /**
     * Adds the "question" word entry to the linearLayoutHorizontalLine as a "spinner" which can
     * be clicked on to show a dropdown with 4 options to choose from, one of which is the correct
     * Kanji.
     *
     * @param wordEntry WordEntry object for the question/spinner word
     */
    private void addSpinnerToLayout(final WordEntry wordEntry) {

        if (currentLineWidth + spinnerWidth > displayWidth) {
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "special insert currentLineWidth: " + currentLineWidth);
                Log.d(TAG, "special insert displayWidth: " + displayWidth);
            }
            insertLineIntoParagraph(); // if there isn't room for the special kanji box, insert it all on a new line
        }

        FillinSentencesSpinner spinnerData = wordEntry.getFillinSentencesSpinner();

        final Spinner spinner = (Spinner)LayoutInflater.from(getActivity()).inflate(R.layout.spinneritem, null);

        spinner.setTag(wordEntry.getStartIndex());//setting the tag so we can extract from map later
        spinner.setMinimumWidth(spinnerWidth);
        spinner.setLayoutParams(new ViewGroup.LayoutParams(spinnerWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

        if(BuildConfig.DEBUG){Log.d(TAG,"spinnerdata: " + wordEntry.getKanji() + ", " + spinnerData.hasBeenAnswered() + ", " + spinnerData.isCorrect());}

        /* If the activity is being recreated and a spinner has already been answered, correct or not,
        * color the spinner accordingly and, if it was correct, disallow further editing */
        if (spinnerData.hasBeenAnswered()) {
            if(spinnerData.isCorrect()) {

                spinner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorJukuGreen));
                ArrayList<String> spinnerArray = new ArrayList<>();
                spinnerArray.add(wordEntry.getKanji());
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinnerArray);
                spinner.setAdapter(spinnerArrayAdapter);
                spinner.setClickable(false);
            } else {
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinnerData.getOptions()); //selected item will look like a spinner set from XML
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerArrayAdapter);
                spinner.setMinimumWidth(spinnerWidth);
                spinner.setLayoutParams(new ViewGroup.LayoutParams(spinnerWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
                if(spinnerData.getSelectedOption()!=null) {
                    spinner.setSelection(spinnerData.getOptions().indexOf(spinnerData.getSelectedOption()));
                }
                spinner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorJukuRed));
                spinner.setOnItemSelectedListener(spinnerSelectedListener(wordEntry));
            }

        } else {
            // Otherwise cretae a fresh spinner for the word
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinnerData.getOptions()); //selected item will look like a spinner set from XML
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerArrayAdapter);

            if(spinnerData.getSelectedOption()!=null) {
                spinner.setSelection(spinnerData.getOptions().indexOf(spinnerData.getSelectedOption()));
            } else {
                spinnerData.setSelectedOption(spinnerData.getOptions().get(0));
            }
            if(BuildConfig.DEBUG) {Log.d(TAG, "spinner option selected: " + spinnerData.getOptions().get(0));}
            spinner.setOnItemSelectedListener(spinnerSelectedListener(wordEntry));
            spinner.setMinimumWidth(spinnerWidth);
            spinner.setLayoutParams(new ViewGroup.LayoutParams(spinnerWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        linearLayoutHorizontalLine.addView(spinner);
        currentLineWidth = currentLineWidth + spinnerWidth;
        insertLineIntoParagraph((currentLineWidth + displayMarginPadding), displayWidth);

    }


    /**
     * Adds a linearLayoutHorizontalLine view, which should be full of textviews representing
     * part of the tweet, to the linearLayoutVerticalParagraph. Effectively adding a line of text
     * in the tweet to the paragraph as a whole, BUT only if the total line width exceeds the edge of the screen.
     * Otherwise the insert is not necessary and more textviews can be added to the linearLayoutHorizontalLine
     *
     * @param totallinewidth total estimated width of the views in the linearLayoutHorizontalLine
     * @param displaywidth total screenwidth
     */
    public void insertLineIntoParagraph(int totallinewidth, int displaywidth) {

        if (displaywidth == 0) {
            currentLineWidth = 0;
        } else if (totallinewidth >= displaywidth) {

            final LinearLayout tmplinearLayout = linearLayoutHorizontalLine;
            linearLayoutVerticalParagraph.addView(tmplinearLayout);
            //now reset the linear layout... (hope that works)
            linearLayoutHorizontalLine = new LinearLayout(getContext());
            linearLayoutHorizontalLine.setOrientation(LinearLayout.HORIZONTAL);
            linearLayoutHorizontalLine.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            currentLineWidth = 0;
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "BIGLAYOUT childcount: " + linearLayoutVerticalParagraph.getChildCount());
                Log.d(TAG, "insertLineIntoParagraph Completed");
            }
        } else {
            if(BuildConfig.DEBUG){Log.d(TAG, "linearlayoutInset Unnecessary");}
        }
    }

    /**
     * Adds a linearLayoutHorizontalLine view to the linearLayoutVerticalParagraph, effectively adding a line of text
     * in the tweet to the paragraph as a whole.
     *
     * Like {@link #insertLineIntoParagraph(int, int)}, but this forces the text to be entered and a new line to be
     * created no matter what
     */
    public void insertLineIntoParagraph() {

            final LinearLayout tmplinearLayout = linearLayoutHorizontalLine;
            linearLayoutVerticalParagraph.addView(tmplinearLayout);
            //now reset the linear layout... (hope that works)
            linearLayoutHorizontalLine = new LinearLayout(getContext());
            linearLayoutHorizontalLine.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutHorizontalLine.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
            currentLineWidth = 0;
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "Direct BIGLAYOUT childcount: " + linearLayoutVerticalParagraph.getChildCount());
                Log.d(TAG, "Direct insertLineIntoParagraph Completed");
            }
    }



    /**
     * Estimates the width on screen for a textview containing a string of text
     * @param text text to include in textview
     * @return width on screen of textview containing text
     */
    private int getEstimatedTextViewWidth(String text) {
        TextView textView_Test = new TextView(getContext());
        textView_Test.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        textView_Test.setText(text);
        textView_Test.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);

        Rect bounds = new Rect();
        Paint textPaint = textView_Test.getPaint();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        int height = bounds.height();
        int width = bounds.width();

        if(BuildConfig.DEBUG) {
            Log.d(TAG, "Prospective Height = " + height);
            Log.d(TAG, "REGULAR WIDTH  = " + width);
            Log.d(TAG, "measureText WIDTH = " + textPaint.measureText(text));
        }

        return Math.round(textPaint.measureText(text));
    }

    /**
     * When the "Score" button is pressed, each "spinner" word in the tweet is graded against the
     * current chosen answer in the spinner dropdown item. Then the quiz moves to a new question or the user
     * is forced to correct the incorrect spinners, which have been colored red.
     *
     * The scoring is done inside the Tweet Object. The Tweet object contains a list of WordEntry objects for
     * each of the parsed words in the tweet, and each {@link WordEntry} contains a {@link FillinSentencesSpinner} object,
     * which tracks the correct/incorrect answers and whether that spinner has been answered, etc. So this cycles through the
     * tweet, finds spinner WordEntries, and grades them (also updateing the FillinSentencesSpinner object they contain).
     *
     */
    private void scoreTheSpinners(){
        boolean allSpinnersAreCorrect = true;
        int tweetWasAnsweredCorrectlyFirstTry = 1;
        for (WordEntry wordEntry : mDataset.get(currentDataSetindex).getWordEntries()) {
            if(wordEntry.isSpinner()) {
                //If correct
                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "word kanji: " + wordEntry.getKanji());
                    Log.d(TAG, "word corekanjiblock: " + wordEntry.getCoreKanjiBlock());
                    Log.d(TAG, "selected: " + wordEntry.getFillinSentencesSpinner().getSelectedOption());
                }

                if(wordEntry.getCoreKanjiBlock() != null && wordEntry.getCoreKanjiBlock().equals(wordEntry.getFillinSentencesSpinner().getSelectedOption())) {
                    wordEntry.getFillinSentencesSpinner().setCorrect(true);
                    if(!wordEntry.getFillinSentencesSpinner().hasBeenAnswered()) {
                        wordEntry.getFillinSentencesSpinner().setCorrectFirstTry(true);
                        wordEntry.getFillinSentencesSpinner().setHasBeenAnswered(true);
                        if(BuildConfig.DEBUG){Log.i(TAG,"Setting correct first try: " + wordEntry.getKanji());}
                    }

                    Spinner correctSpinner = (Spinner)linearLayoutVerticalParagraph.findViewWithTag(wordEntry.getStartIndex());
                    correctSpinner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorJukuGreen));
                    correctSpinner.setClickable(false);

                } else {
                    wordEntry.getFillinSentencesSpinner().setCorrect(false);
                    wordEntry.getFillinSentencesSpinner().setHasBeenAnswered(true);
                    wordEntry.getFillinSentencesSpinner().setCorrectFirstTry(false);

                    if(BuildConfig.DEBUG){Log.d(TAG,"Incorrect spinner found!");}
                    linearLayoutVerticalParagraph.findViewWithTag(wordEntry.getStartIndex()).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorJukuRed));
                    allSpinnersAreCorrect = false;
                }

                if(!wordEntry.getFillinSentencesSpinner().isCorrectFirstTry()) {
                    tweetWasAnsweredCorrectlyFirstTry = 0;
                }

            }
        }

        final int additiontoCorrectTotal = tweetWasAnsweredCorrectlyFirstTry;

        //IF everything is correct, score answers and move on
        if(allSpinnersAreCorrect) {

            Runnable questionFinishedRunnable = new Runnable() {
                @Override
                public void run() {

                    //Score the answers
                    for (WordEntry wordEntry : mDataset.get(currentDataSetindex).getWordEntries()) {
                        if(wordEntry.isSpinner()) {
                            //If correct
                            int correct = wordEntry.getCorrect();
                            if(wordEntry.getFillinSentencesSpinner().isCorrectFirstTry()) {
                                correct += 1;
                            }
                            if(SharedPrefManager.getInstance(getContext()).getIncludefillinsentencesscores()) {
                                InternalDB.getQuizInterfaceInstance(getContext()).addWordScoreToScoreBoard(wordEntry.getId(),wordEntry.getTotal()+1,correct);
                            }
                        }
                    }

                    //Move to the next question
                    currentTotal += 1;
                    currentCorrect += additiontoCorrectTotal;
                    redundentQuestionCounter = 0;
                    moveToNextQuestion();
                }
            };
            Handler myHandler = new Handler();
            myHandler.postDelayed(questionFinishedRunnable, 100); //Message will delivered after delay
        }
    }

    /**
     * Either moves to the next question with {@link #setUpQuestion(Tweet)}, or on to
     * the PostQuizStats if there are no more questions (or kicks back to main activity if there
     * was an error)
     */
    private void moveToNextQuestion() {

        currentDataSetindex += 1;
        //Move to stats if we have reached the end of the quiz
        if(redundentQuestionCounter>4) {
            //Kick back to main menu
            mCallback.emergencyGoBackToMainActivity();
        } else if(currentTotal>= mQuizSize || currentDataSetindex >= mDataset.size()) {

            Object listInformationObject;
            if(mSingleUser) {
                listInformationObject = mUserInfo;
            } else {
                listInformationObject = mMyListEntry;
            }
                    mCallback.showPostQuizStatsFillintheBlanks(mDataset
                            ,listInformationObject
                            ,currentCorrect
                            ,currentTotal);
        } else {
                txtQuestionNumber.setText((currentTotal +1) + "/" + mQuizSize);
                redundentQuestionCounter += 1;
                setUpQuestion(mDataset.get(currentDataSetindex));
        }

    }


    public void updateWordEntryFavoritesForOtherTabs(WordEntry wordEntry) {}
    public void notifySavedTweetFragmentsChanged(){}

    /**
     * If a word entry has been saved to a new word list in the {@link WordDetailPopupDialog}, the message is relayed back to
     * this method, which updates the  {@link com.jukuproject.jukutweet.Models.ItemFavorites} in the dataset to reflect the change
     * @param wordEntry WordEntry that was added to/removed from a new list
     */
    public void updateWordEntryItemFavorites(WordEntry wordEntry) {
        for(Tweet tweet: mDataset) {
            if(tweet.getWordEntries()!=null && tweet.getWordEntries().contains(wordEntry)) {
                for(WordEntry tweetWordEntry : tweet.getWordEntries()) {
                    if(tweetWordEntry.getId().equals(wordEntry.getId())) {
                        wordEntry.setItemFavorites(wordEntry.getItemFavorites());
                    }
                }
            }
        }
    }

    /**
     * If a tweet has been saved in {@link WordDetailPopupDialog}, and the user for that tweet
     * is not saved in the db (which therefore means the user's icon is not saved in the db), this passes
     * on the message to save the icon from the {@link com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter} to the Activity,
     * which uses {@link com.jukuproject.jukutweet.Database.UserOpsHelper#downloadTweetUserIcon(Context, String, String)} in a
     * subscription to download the icon
     * @param userInfo UserInfo of user whose icon will be downloaded
     */
    public void downloadTweetUserIcons(UserInfo userInfo) {
        mCallback.downloadTweetUserIcons(userInfo);
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


    /**
     * Listener updating the word entry's {@link FillinSentencesSpinner} object when the user has
     * selected a dropdown option for one of the spinners. AND, if the question has already been scored with some
     * incorrect spinners, automatically try to score the spinners again (this way user doens't have to click "score" button
     * any more after the first try if they got it wrong).
     * @param wordEntry WordEntry associated with the spinner
     * @return OnItemSelectedListener for the spinner
     */
    private AdapterView.OnItemSelectedListener spinnerSelectedListener (final WordEntry wordEntry) {
    return new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            wordEntry.getFillinSentencesSpinner().setSelectedOption(wordEntry.getFillinSentencesSpinner().getOptions().get(position));

                                                      /*If the word has already been answered wrongly, and the user changes the answer
                                                      to the correct one, automatically score the tweet again without them having to
                                                      click the score button */
            if(BuildConfig.DEBUG) {
                Log.i(TAG,"corekanji block: " + wordEntry.getCoreKanjiBlock());
                Log.i(TAG,"isselected option: " + wordEntry.getFillinSentencesSpinner().getSelectedOption());
                Log.i(TAG,"isCorrectFirstTry: " + wordEntry.getFillinSentencesSpinner().isCorrectFirstTry());
            }

            if(wordEntry.getCoreKanjiBlock() != null && !wordEntry.getFillinSentencesSpinner().isCorrectFirstTry() && wordEntry.getCoreKanjiBlock().equals(wordEntry.getFillinSentencesSpinner().getSelectedOption())) {
                scoreTheSpinners();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
}

    /**
     * The way the process is set up, there should be exactly the right amount of Tweets in the dataset to
     * create the number of quiz questions. So if there is an error with a tweet for some reason, another random
     * tweet from the dataset is added to the end of a duplicate set, and the duplicate replaces the current dataset, so that
     * the total number of questions remains constant. Then the dataset index is advanced by one, and nobody is the wiser.
     * @param currentTweetId Tweet id of the current tweet which had some kind of problem (like no attached word entries? etc)
     * @param currentDataSetIndex The current index of the problem tweet
     */
    private void replaceDataSet(String currentTweetId, int currentDataSetIndex) {
            Log.e(TAG,"REPLACING DATASETS");
            ArrayList<Tweet> replacementDataSet = new ArrayList<>(mDataset);
            replacementDataSet.remove(currentDataSetIndex);
            for(Tweet tweet :replacementDataSet) {
                if(!tweet.getIdString().equals(currentTweetId)
                        && tweet.getWordEntries()!=null
                        && tweet.getWordEntries().size()>0) {
                    Parcel p = Parcel.obtain();
                    p.writeValue(tweet);
                    p.setDataPosition(0);
                    Tweet replacementTweet = (Tweet)p.readValue(Tweet.class.getClassLoader());
                    p.recycle();
                    replacementDataSet.add(replacementTweet);
                }
            }
            mDataset = replacementDataSet;
        }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("mDataset", mDataset);
        outState.putInt("mQuizSize", mQuizSize);
        outState.putDouble("mTotalWeight", mTotalWeight);
        outState.putString("mColorString", mColorString);
        outState.putParcelable("mMyListEntry", mMyListEntry);
        outState.putInt("currentTotal", currentTotal);
        outState.putInt("currentCorrect",currentCorrect);
        outState.putInt("currentLineWidth",currentLineWidth);
        outState.putBoolean("mSingleUser",mSingleUser);
        outState.putParcelable("mUserInfo",mUserInfo);
        outState.putInt("currentDataSetindex",currentDataSetindex);
    }


}
