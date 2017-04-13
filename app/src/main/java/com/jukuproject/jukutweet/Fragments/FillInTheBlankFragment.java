package com.jukuproject.jukutweet.Fragments;

import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Models.FillinSentencesSpinner;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by JClassic on 4/3/2017.
 */

public class FillInTheBlankFragment extends Fragment  {

    String TAG = "TEST-fillinblank";

    int currentTotal = 0; //CURRENT number of questions asked
    int currentCorrect = 0; //CURRENT number of correct answers
    int currentDataSetindex = 0; //Current position within dataset (reset to 0 when shuffling)

    ArrayList<Tweet> mDataset;
    Integer mQuizSize;
    double mTotalWeight;
    MyListEntry mMyListEntry;
    String mColorString;

    int currentLineWidth = 0;
    int displayWidth = 0;
    int displayMarginPadding = 30; //How much to pad the edge of the screen by when laying down the sentenceblocks (so the sentence doesn't overlap the screen or get cut up too much)
    int spinnerWidth = 200;
    int spinnerHeight = 55;

    LinearLayout linearLayoutVerticalParagraph;  //This is the main linear layout, that we will fill row by row with horizontal linear layouts, which are     // in turn filled with vertical layouts (with furigana on top and japanese on bottom)
    LinearLayout linearLayoutHorizontalLine; //one of these layouts for each line in the vertical paragraph

    public FillInTheBlankFragment() {}

    public static FillInTheBlankFragment newInstance(ArrayList<Tweet> tweets
            , Integer quizSize
            , double totalWeight
            , String colorString
            , MyListEntry myListEntry
    ) {

        FillInTheBlankFragment fragment = new FillInTheBlankFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("tweets", tweets);
        args.putInt("quizSize",quizSize);
        args.putDouble("totalWeight",totalWeight);
        args.putString("colorString",colorString);
        args.putParcelable("myListEntry",myListEntry);
        fragment.setArguments(args);
//        long startTime = System.nanoTime();
//        long endTime = System.nanoTime();
//        Log.e("TEST","assign FRAGMENT ELLAPSED TIME: " + (endTime - startTime) / 1000000000.0);
        return  fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Set input global data
        mDataset = getArguments().getParcelableArrayList("tweets");
        mQuizSize = getArguments().getInt("quizSize");
        mTotalWeight = getArguments().getDouble("totalWeight");
        mColorString = getArguments().getString("colorString");
        mMyListEntry = getArguments().getParcelable("mylistentry");

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_fillintheblanks, null);

        /* Get width of screen */
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        displayWidth = metrics.widthPixels;
        displayMarginPadding = (int) ((float) (displayWidth) * 0.055555556);
        spinnerWidth = (int) (160.0f * metrics.density + 0.5f);
        spinnerHeight = (int) (37.0f * metrics.density + 0.5f);

        linearLayoutVerticalParagraph = (LinearLayout) view.findViewById(R.id.sentence_layout);
        linearLayoutHorizontalLine = new LinearLayout(getContext());
        linearLayoutHorizontalLine.setOrientation(LinearLayout.HORIZONTAL);

        /* Reset the lists and layouts */
        //TODO -- test set up the sentence!

        Log.d(TAG,"DatasetSize: " + mDataset.size());
        //Randomize the dataset
        Collections.shuffle(mDataset);
        currentDataSetindex = 0;

        TextView scoreButton = (TextView) view.findViewById(R.id.scoreButton);
        scoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (WordEntry wordEntry : mDataset.get(currentDataSetindex).getWordEntries()) {
                    if(wordEntry.isSpinner()) {
                        //If correct

                        Log.d(TAG,"word kanji: " + wordEntry.getKanji());
                        Log.d(TAG,"selected: " + wordEntry.getFillinSentencesSpinner().getSelectedOption());

                        if(wordEntry.getKanji().equals(wordEntry.getFillinSentencesSpinner().getSelectedOption())) {
                            wordEntry.getFillinSentencesSpinner().setCorrect(true);
                            ((Spinner)linearLayoutVerticalParagraph.findViewWithTag(wordEntry.getStartIndex())).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorJukuGreen));
                        } else {
                            wordEntry.getFillinSentencesSpinner().setCorrect(false);
                            ((Spinner)linearLayoutVerticalParagraph.findViewWithTag(wordEntry.getStartIndex())).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorJukuRed));
                        }
                    }
                }
            }
        });
        //Set up tweet and spinners
//        supertest();
        setUpQuestion(mDataset.get(currentDataSetindex));
        return view;
    }

    public void setUpQuestion(Tweet tweet) {

        String sentence = tweet.getText();
        ArrayList<WordEntry> disectedSavedTweet = tweet.getWordEntries();


        Log.d(TAG,"DisectedSavedTweet size: " + tweet.getWordEntries().size());
         /* Get metrics to pass density/width/height to adapters */
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        displayWidth = metrics.widthPixels;
        displayMarginPadding =  (int)((float)(displayWidth)*0.055555556);


        currentLineWidth = 0;
        linearLayoutVerticalParagraph.removeAllViews();
        linearLayoutHorizontalLine = new LinearLayout(getContext());

        /* Set tweet color spans. If the saved Tweet object includes a "colorIndex" object (
        * which comes from the savedTweetKanji table and contains the id, positions and color designation
        * of each kanji in the TWeet), replace the normal Tweet text with colored spans for those kanji */
//        int startIndex = 0;
//        TextView textView = new TextView(getContext());

        try {

            int startIndex = 0;
            SpannableStringBuilder sb = new SpannableStringBuilder();
            for(final WordEntry wordEntry : disectedSavedTweet) {

                /* If its a spinner word:
                    * 1. Put the current stringbuilder contents into a textview
                    * and enter it into the LinearLayout input process.
                    * 2. Create the spinner and its contents, and put it into the linearlayout process
                    * */

                Log.d(TAG,"is spinner? " + wordEntry.isSpinner() + ", adding word entry: " + wordEntry.getKanji() + ", start: " + wordEntry.getStartIndex() + ", end: " + wordEntry.getEndIndex());

                /*Add the portion of normal, non-kanji, non-spinner text between the previous
                 kanji end-index and the current kanji start-index to the layout*/
                if(wordEntry.getStartIndex()>startIndex) {
                    Log.d(TAG,"adding previous word chunk: " + sentence.substring(startIndex,wordEntry.getStartIndex()));
                    sb.append(sentence.substring(startIndex,wordEntry.getStartIndex()));
                    addToLayout(sb);
//                    startIndex = wordEntry.getEndIndex();
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
                            Toast.makeText(getContext(), wordEntry.getFurigana(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(ContextCompat.getColor(getContext(),wordEntry.getColorValue()));
                            ds.setUnderlineText(false);
//                                ds.setAlpha(1);

                        }
                    };

                    sb.setSpan(kanjiClick, 0, sb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    addToLayout(sb);
                    startIndex = wordEntry.getEndIndex();
                    sb = new SpannableStringBuilder();
                }

                //TODO ADD THE LAST ONE

            }

            //Add the last part of the sentence
//            int lastIndex = disectedSavedTweet.get(disectedSavedTweet.size()-1).getEndIndex();
            if(startIndex<sentence.length()) {
                sb = new SpannableStringBuilder();
                sb.append(sentence.substring(startIndex,sentence.length()));
                addToLayout(sb);
            }


        } catch (Exception e) {
            //TODO -- MOVE TO THE NEXT QUESTION
//            txtSentence.setText(entireSentence);
            Log.e(TAG,"Adding sentence pieces to fillblanks generic failure: " + e);
        }


    }





    public void addToLayout(final SpannableStringBuilder text) {

         if(BuildConfig.DEBUG) {Log.d(TAG, "Adding to layout  " +
                 text);}

            //Estimate the length of the chunk of text
            int estimatedTextViewWidth = getEstimatedTextViewWidth(text.toString());


            //Compare the estimated length against the allowale length for a single line
            int maxWidthAllowed = displayWidth - currentLineWidth - displayMarginPadding;
            int widthExtra = estimatedTextViewWidth - displayWidth;

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

//                if(BuildConfig.DEBUG) {Log.d(TAG, "curentendpoint: " + currentendpoint);}
//                int substringend = currentendpoint;

//                if (maxWidthAllowed < 0) {
//                    estimatedSubStringEnd = text.length();
//                    if(BuildConfig.DEBUG) {Log.d(TAG, "Width overrun Substring end is the whole text");}
//                }


                int substringstart = 0;
                int estimatedSubStringEnd = Math.round(((float) maxWidthAllowed / (float) estimatedTextViewWidth) * text.length());
                CharSequence textRemaining = text;

                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "substringstart: " + substringstart);
                    Log.d(TAG, "estimatedSubStringEnd: " + estimatedSubStringEnd);
                    Log.d(TAG, "textRemaining: " + textRemaining);
                }

                while (widthExtra > 0 && estimatedSubStringEnd > substringstart) {

                    CharSequence textToAdd = textRemaining.subSequence(substringstart, estimatedSubStringEnd);
                    if(BuildConfig.DEBUG) {Log.d(TAG, "textToAdd: " + textToAdd);}

                    TextView textView = new TextView(getContext());
                    textView.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    textView.setText(textToAdd);
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                    textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    linearLayoutHorizontalLine.addView(textView);

                    int estimatedWidthOfTextViewThatWasAdded = getEstimatedTextViewWidth(textToAdd.toString());
                    if(BuildConfig.DEBUG) {Log.d(TAG, "TextAdded, estimated width: : " + estimatedWidthOfTextViewThatWasAdded);}

//                    if (substringend < substringstart) {
//                        insertLineIntoParagraph(2, 0); //DONT LOG A NEW ROW
//                    } else {

                        insertLineIntoParagraph((currentLineWidth + estimatedWidthOfTextViewThatWasAdded + displayMarginPadding), displayWidth);
//                    }


                    //Its a new line
                    widthExtra = widthExtra - estimatedWidthOfTextViewThatWasAdded;
                    estimatedTextViewWidth = estimatedTextViewWidth - estimatedWidthOfTextViewThatWasAdded;

                    substringstart = estimatedSubStringEnd;
                    textRemaining = text.subSequence(substringstart, text.length());
                    if(BuildConfig.DEBUG) {
                        Log.d(TAG, "Updated widthExtra: " + widthExtra);
                        Log.d(TAG, "Updated estimatedTextViewWidth: " + estimatedTextViewWidth);
                        Log.d(TAG, "Updated substringstart: " + substringstart);
                        Log.d(TAG, "Updated textRemaining: " + textRemaining);

                    }

//                    if ((currentLineWidth + estimatedTextViewWidth + displayMarginPadding) < displayWidth || widthExtra < 0) {
//                        substringend = text.length();
//
//                        if(BuildConfig.DEBUG) {
//                            Log.d(TAG, "choppedfragment substringstart: " + substringstart);
//                            Log.d(TAG, "choppedfragment substringend: " + substringend);
//                        }
//
//                        CharSequence choppedTextFragmentRemainder = text.subSequence(substringstart, substringend);
//                        if(BuildConfig.DEBUG) {Log.d(TAG, "choppedTextFragmentRemainder: " + choppedTextFragmentRemainder);}
//                        TextView textViewRemainder = new TextView(getContext());
//
//
//                        textViewRemainder.setLayoutParams(new ViewGroup.LayoutParams(
//                                ViewGroup.LayoutParams.WRAP_CONTENT,
//                                ViewGroup.LayoutParams.WRAP_CONTENT));
//
//                        textViewRemainder.setText(choppedTextFragmentRemainder);
//                        textViewRemainder.setMovementMethod(LinkMovementMethod.getInstance());
//                        textViewRemainder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
//
//                        textViewRemainder.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//                        linearLayoutHorizontalLine.addView(textViewRemainder);
//
//
//                        int widthOfRemainder = getEstimatedTextViewWidth(choppedTextFragmentRemainder.toString());
//                        currentLineWidth = currentLineWidth + widthOfRemainder;
//
//                        if(BuildConfig.DEBUG) {
//                            Log.d(TAG, "chopped currentLineWidth: " + currentLineWidth);
//                            Log.d(TAG, "chopped width: " + widthOfRemainder);
//                        }
//
//                        widthExtra = (currentLineWidth + displayMarginPadding) - displayWidth;
//                        maxWidthAllowed = displayWidth - currentLineWidth - displayMarginPadding;
//
//
//                        if (currentLineWidth == 0 && widthExtra > 0) {  // like if it's the last fragment of a line, starting on a new line. Just print the damn thing (on the new line)
//                            insertLineIntoParagraph((currentLineWidth + (displayWidth + widthExtra) + displayMarginPadding), displayWidth);
//                        } else if (currentLineWidth == 0 && widthExtra < 0) {
//                            insertLineIntoParagraph((currentLineWidth + (displayWidth + widthExtra) + displayMarginPadding), displayWidth);
//                        } else {
//                            insertLineIntoParagraph((currentLineWidth + displayMarginPadding), displayWidth);
//                        }
//
//                    } else {
//
//                        maxWidthAllowed = displayWidth - currentLineWidth - displayMarginPadding;
//
//                        if(BuildConfig.DEBUG) {
//                            Log.d(TAG, "substringend calculation--maxwidthallowed: " + maxWidthAllowed);
//                            Log.d(TAG, "substringend calculation--width: " + estimatedTextViewWidth);
//                            Log.d(TAG, "substringend calculation--onScreenText.length: " + text.length());
//                        }
//
//                        substringend = Math.round(((float) maxWidthAllowed / (float) estimatedTextViewWidth) * text.length()) + substringstart;
//                        if(BuildConfig.DEBUG) {Log.d(TAG, "substring rounding: " + ((float) maxWidthAllowed / (float) estimatedTextViewWidth) * text.length());}
//                    }

//                    if(BuildConfig.DEBUG) {
//                        Log.d(TAG, "NEW substringstart: " + substringstart);
//                        Log.d(TAG, "NEW substringend: " + substringend);
//                    }
//                    if (substringend > text.length()) {
//                        if(BuildConfig.DEBUG) {Log.d(TAG, "SHIT WORKAROUND substringend Revised to: " + substringend);}
//                        substringend = text.length();
//                    }

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








    public void addSpinnerToLayout(final WordEntry wordEntry) {

        if (currentLineWidth + spinnerWidth > displayWidth) {
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "special insert currentLineWidth: " + currentLineWidth);
                Log.d(TAG, "special insert displayWidth: " + displayWidth);
            }
            insertLineIntoParagraph(2, 1); // if there isn't room for the special kanji box, insert it all on a new line
        }

        FillinSentencesSpinner spinnerData = wordEntry.getFillinSentencesSpinner();

//        LinearLayout innerLinearLayout4 = new LinearLayout(getContext());
//        innerLinearLayout4.setOrientation(LinearLayout.VERTICAL);

        final Spinner spinner = (Spinner)LayoutInflater.from(getActivity()).inflate(R.layout.spinneritem, null);

        spinner.setTag(wordEntry.getStartIndex());//setting the tag so we can extract from map later

        if (spinnerData.hasBeenAnswered()) {
            if(spinnerData.isCorrect()) {

                spinner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorJukuGreen));
                ArrayList<String> spinnerArray = new ArrayList<>();
                spinnerArray.add(wordEntry.getKanji());

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, spinnerArray);
//                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerArrayAdapter);
                spinner.setClickable(false);
                //TODO set the current selected item to be spinnerData.getselecteditem
            } else {

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinneritem_red, spinnerData.getOptions()); //selected item will look like a spinner set from XML
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerArrayAdapter);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(spinnerWidth, ViewGroup.LayoutParams.MATCH_PARENT);
                params.weight = 1.0f;
                params.gravity = Gravity.BOTTOM;
                spinner.setLayoutParams(params);
            }

        } else {

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, spinnerData.getOptions()); //selected item will look like a spinner set from XML
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerArrayAdapter);
            spinnerData.setSelectedOption(spinnerData.getOptions().get(0));
            Log.d(TAG,"spinner option selected: " + spinnerData.getOptions().get(0));

//TODO FIX



//            spinner.setOnItemSelectedListener(this);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                  @Override
                                                  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                                      wordEntry.getFillinSentencesSpinner().setSelectedOption(wordEntry.getFillinSentencesSpinner().getOptions().get(position));
                                                      Log.d(TAG,"spinner option selected: " + wordEntry.getFillinSentencesSpinner().getOptions().get(position));
                                                  }

                                                  @Override
                                                  public void onNothingSelected(AdapterView<?> parent) {

                                                  }
                                              }
            );
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(spinnerWidth, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.BOTTOM;
            spinner.setLayoutParams(params);
        }



//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
//                // Updating an array containing correct answer vs. selected answer.
//
////                int treemapIndex = Integer.parseInt(spinner.getTag().toString());
////                ArrayList<String> tmpStringArray = spinnerDataTreeMap.get(treemapIndex);
////                tmpStringArray.remove(2);
////                tmpStringArray.add(spinner.getSelectedItem().toString());
////                spinnerDataTreeMap.put(treemapIndex, tmpStringArray);
//
//                /** if the user has gotten an incorrect answer, make it so now when they go back and correct that answer
//                 *  they don't have to hit the score button. it automatically checks it and moves on */
////                if (isIncorrectBoolean) {
////                    scoreTheAnswers();
////TODO THIS PART
////                }
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parentView) {
//                // your code here
//            }
//
//        });

//        innerLinearLayout4.addView(spinner);
//        innerLinearLayout4.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayoutHorizontalLine.addView(spinner);
        currentLineWidth = currentLineWidth + spinnerWidth;
        insertLineIntoParagraph((currentLineWidth + displayMarginPadding), displayWidth);



    }




    public void supertest() {
        linearLayoutHorizontalLine = new LinearLayout(getContext());
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        textView.setText("supertest");
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        linearLayoutHorizontalLine.addView(textView);

        final LinearLayout tmplinearLayout = linearLayoutHorizontalLine;
        linearLayoutVerticalParagraph.addView(tmplinearLayout);
//        linearLayoutVerticalParagraph.setBackgroundColor(ContextCompat.getColor(getContext(),android.R.color.holo_purple));
        //now reset the linear layout... (hope that works)
        linearLayoutHorizontalLine = new LinearLayout(getContext());
        linearLayoutHorizontalLine.setOrientation(LinearLayout.HORIZONTAL);
    }
    public void insertLineIntoParagraph(int totallinewidth, int displaywidth) {

        if (displaywidth == 0) {
            currentLineWidth = 0;
        } else if (totallinewidth >= displaywidth) {

            final LinearLayout tmplinearLayout = linearLayoutHorizontalLine;
            linearLayoutVerticalParagraph.addView(tmplinearLayout);
            //now reset the linear layout... (hope that works)
            linearLayoutHorizontalLine = new LinearLayout(getContext());
            linearLayoutHorizontalLine.setOrientation(LinearLayout.HORIZONTAL);
            currentLineWidth = 0;
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "BIGLAYOUT childcount: " + linearLayoutVerticalParagraph.getChildCount());
                Log.d(TAG, "insertLineIntoParagraph Completed");
            }
        } else {
            if(BuildConfig.DEBUG){Log.d(TAG, "linearlayoutInset Unnecessary");}
        }
    }


    public int getEstimatedTextViewWidth(String text) {
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


//
//    public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
//        Toast.makeText(parent.getContext(),
//                "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
//                Toast.LENGTH_SHORT).show();
//        Log.d(TAG,"ITEM SELECTED");
//    }
//
//    @Override
//    public void onNothingSelected(AdapterView<?> parent) {
//
//    }

}
