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

public class FillInTheBlankFragment extends Fragment {

    String TAG = "TEST-fillinblank";

    int currentTotal = 0; //CURRENT number of questions asked
    int currentCorrect = 0; //CURRENT number of correct answers
    int currentDataSetindex = 0; //Current position within dataset (reset to 0 when shuffling)

    ArrayList<Tweet> mDataset;
    Integer mQuizSize;
    double mTotalWeight;
    MyListEntry mMyListEntry;
    String mColorString;
//    ColorThresholds mColorThresholds;


    int linewidth = 0;
    int displaywidth = 0;
    int displaymarginpadding = 30; //How much to pad the edge of the screen by when laying down the sentenceblocks (so the sentence doesn't overlap the screen or get cut up too much)
    int spinnerwidth = 200;
    int spinnerheight = 55;

    LinearLayout linearLayoutVerticalMain;  //This is the main linear layout, that we will fill row by row with horizontal linear layouts, which are     // in turn filled with vertical layouts (with furigana on top and japanese on bottom)
    LinearLayout linearLayout;
//    TextView text;

    public FillInTheBlankFragment() {}

    public static FillInTheBlankFragment newInstance(ArrayList<Tweet> tweets
            , Integer quizSize
            , double totalWeight
            , String colorString
            , MyListEntry myListEntry
    ) {

        long startTime = System.nanoTime();
        FillInTheBlankFragment fragment = new FillInTheBlankFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("tweets", tweets);
        args.putInt("quizSize",quizSize);
        args.putDouble("totalWeight",totalWeight);
        args.putString("colorString",colorString);
        args.putParcelable("myListEntry",myListEntry);
        fragment.setArguments(args);
        long endTime = System.nanoTime();
        Log.e("TEST","assign FRAGMENT ELLAPSED TIME: " + (endTime - startTime) / 1000000000.0);
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


        /** Get width of screen */
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        displaywidth = metrics.widthPixels;
        displaymarginpadding = (int) ((float) (displaywidth) * 0.055555556);
        spinnerwidth = (int) (160.0f * metrics.density + 0.5f);
        spinnerheight = (int) (37.0f * metrics.density + 0.5f);



        linearLayoutVerticalMain = (LinearLayout) view.findViewById(R.id.sentence_layout);
        linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
//        text = (TextView) view.findViewById(R.id.sentence_eng);

        /* Reset the lists and layouts */
        //TODO -- test set up the sentence!


        //Randomize the dataset
        Collections.shuffle(mDataset);
        currentDataSetindex = 0;

        //Set up tweet and spinners
        setUpQuestion(mDataset.get(currentDataSetindex));


//        text.setText(mDataset.get(0).getText());


        return view;
    }

//    private void setUpQuestion(Tweet tweet) {
//
//
//    }


    public void setUpQuestion(Tweet tweet) {

        String remainingSentence = tweet.getText();
        ArrayList<WordEntry> disectedSavedTweet = tweet.getWordEntries();


         /* Get metrics to pass density/width/height to adapters */
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        displaywidth = metrics.widthPixels;
        displaymarginpadding =  (int)((float)(displaywidth)*0.055555556);

        /* Set tweet color spans. If the saved Tweet object includes a "colorIndex" object (
        * which comes from the savedTweetKanji table and contains the id, positions and color designation
        * of each kanji in the TWeet), replace the normal Tweet text with colored spans for those kanji */
//        int startIndex = 0;
//        TextView textView = new TextView(getContext());

        try {

            int startIndex = 0;
            SpannableStringBuilder sb = new SpannableStringBuilder(remainingSentence);
            for(final WordEntry wordEntry : disectedSavedTweet) {

                /* If its a spinner word:
                    * 1. Put the current stringbuilder contents into a textview
                    * and enter it into the LinearLayout input process.
                    * 2. Create the spinner and its contents, and put it into the linearlayout process
                    * */

                if(wordEntry.isSpinner()) {
                    if(sb.length()>0) {
                        addToLayout(sb);
                        startIndex = wordEntry.getEndIndex();
                        sb = new SpannableStringBuilder(remainingSentence.substring(wordEntry.getEndIndex()));
                    }
                    addSpinnerToLayout(wordEntry);

                } else {
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

                    sb.setSpan(kanjiClick, wordEntry.getStartIndex() - startIndex, wordEntry.getEndIndex() - startIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }

                //TODO ADD THE LAST ONE

            }


        } catch (Exception e) {
            //TODO -- MOVE TO THE NEXT QUESTION
//            txtSentence.setText(entireSentence);
            Log.e(TAG,"Tweet color generic failure: " + e);
        }


    }





    public void addToLayout(final SpannableStringBuilder onScreenText) {

//        if(parseSentenceItem.getKanjiConjugated().equals(System.getProperty("line.separator"))) {
//            //Log 2 rows, once to input the remaining current layout items,
//            //and another black row for the seperator
//            if(linewidth>0) {
//                linearlayoutInsert(2, 1);
//            }
//            linearlayoutInsert(2, 1);
//
//        } else if(parseSentenceItem.isKanji()) {

                /* INPUT KANJI */
//            if (BuildConfig.DEBUG) {
//                Log.d(TAG, "INPUT KANJI " + parseSentenceItem.getKanjiConjugated());
//            }
//            TextView textView_Test = new TextView(getContext());
//            textView_Test.setLayoutParams(new ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT));
//            textView_Test.setText(onScreenText);
//            textView_Test.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
//
//            Rect bounds = new Rect();
//            Paint textPaint = textView_Test.getPaint();
//            textPaint.getTextBounds(onScreenText, 0, onScreenText.length(), bounds);
//
//            int width = bounds.width();
//
//            if (BuildConfig.DEBUG) {
//                Log.d(TAG, "REGULAR WIDTH  = " + width);
//                Log.d(TAG, "REGULAR FURIGANA WIDTH  = " + width);
//                Log.d(TAG, "measureText WIDTH = " + Math.round(textPaint.measureText(onScreenText)));
//            }
//             width = Math.round(textPaint.measureText(onScreenText));
//            if (BuildConfig.DEBUG) {
//                Log.d(TAG, "FINAL WIDTH = " + width);
//            }
//
//            int widthExtra = (linewidth + width + displaymarginpadding) - displaywidth;
//            int maxWidthAllowed = displaywidth - linewidth - displaymarginpadding;
//
//            if (BuildConfig.DEBUG) {
//                Log.d(TAG, "widthExtra = " + widthExtra);
//                Log.d(TAG, "maxWidthAllowed= " + maxWidthAllowed);
//            }
//
//            if (widthExtra > 0) {
//                linearlayoutInsert(2, 1);
//            }
//
//            LinearLayout innerLinearLayout3 = new LinearLayout(getContext());
//            innerLinearLayout3.setOrientation(LinearLayout.VERTICAL);
//            TextView textView = new TextView(getContext());
//
//            textView.setLayoutParams(new ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT));
//
//            textView.setText(onScreenText);
//            textView.setTextSize(24);
//                textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
//
//            textView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    //TODO replace this
//                    Toast.makeText(getActivity(), "" + parseSentenceItem.getFuriganaClean(), Toast.LENGTH_SHORT).show();
//
//                }
//            });
//
//
//            innerLinearLayout3.addView(textView);
//            innerLinearLayout3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//            linearLayout.addView(innerLinearLayout3);
//
//            linewidth = linewidth + width;
//
//
//        } else {

            /** INPUT THE NORMAL WORD STRINGS */
            if(BuildConfig.DEBUG) {Log.d(TAG, "INPUT REGULAR:  " + onScreenText);}
            TextView textView_Test = new TextView(getContext());
            textView_Test.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            textView_Test.setText(onScreenText);
            textView_Test.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            textView_Test.setMovementMethod(LinkMovementMethod.getInstance());

            Rect bounds = new Rect();
            Paint textPaint = textView_Test.getPaint();
            textPaint.getTextBounds(onScreenText.toString(), 0, onScreenText.toString().length(), bounds);
            int height = bounds.height();
            int width = bounds.width();

            if(BuildConfig.DEBUG) {
                Log.d(TAG, "Prospective Height = " + height);
                Log.d(TAG, "REGULAR WIDTH  = " + width);
                Log.d(TAG, "measureText WIDTH = " + textPaint.measureText(onScreenText.toString()));
            }

            width = Math.round(textPaint.measureText(onScreenText.toString()));

            if(BuildConfig.DEBUG) {
                Log.d(TAG, "FINAL WIDTH = " + width);
                Log.d(TAG, "onScreenText content = " + onScreenText.toString());
                Log.d(TAG, "current linewidth = " + linewidth);
                Log.d(TAG, "onScreenText linewidth = " + width);
            }

            int widthExtra = (linewidth + width + displaymarginpadding) - displaywidth;
            int maxWidthAllowed = displaywidth - linewidth - displaymarginpadding;

            if(BuildConfig.DEBUG) {
                Log.d(TAG, "widthExtra = " + widthExtra);
                Log.d(TAG, "maxWidthAllowed= " + maxWidthAllowed);
            }

            if (widthExtra > 0) {
                int substringstart = 0;
                int currentendpoint = Math.round(((float) maxWidthAllowed / (float) width) * onScreenText.length());
                if(BuildConfig.DEBUG) {Log.d(TAG, "curentendpoint: " + currentendpoint);}
                int substringend = currentendpoint;
                if (maxWidthAllowed > width || maxWidthAllowed < 0) {
                    substringend = onScreenText.length();
                    if(BuildConfig.DEBUG) {Log.d(TAG, "Substring end is the whole onScreenText");}
                }

                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "substringstart: " + substringstart);
                    Log.d(TAG, "substringend: " + substringend);
                }

                while (widthExtra > 0) {

                    if(BuildConfig.DEBUG) {Log.d(TAG, "max width allowed: " + maxWidthAllowed);}
                    CharSequence choppedTextFragment = onScreenText.subSequence(substringstart, substringend);
//                    SpannableString choppedTextFragment = onScreenText.subSequence(substringstart, substringend);
//                    String choppedTextFragment = onScreenText.substring(substringstart, substringend);
                    if(BuildConfig.DEBUG) {Log.d(TAG, "ChoppedFragment: " + choppedTextFragment);}
                    TextView textView = new TextView(getContext());

                    /** INSERTING THE TEXT BOXES INTO THE INNER LINEAR LAYOUT */

                    LinearLayout innerLinearLayout3 = new LinearLayout(getContext());
                    innerLinearLayout3.setOrientation(LinearLayout.VERTICAL);

                    textView.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    textView.setText(choppedTextFragment);
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                    innerLinearLayout3.addView(textView);
                    innerLinearLayout3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    linearLayout.addView(innerLinearLayout3);


                    /** NEED TO FIX THIS SO IF substringend== substringstart (AND IT'S ALREADY VERY NEAR THE END OF A ROW (OR OVER IT), YOU
                     * 1. MAKE A NEW ROW
                     * 2. BUT DON'T TRY TO INPUT THE NONEXISTANT FRAGMENT, RESET THE METER AND THROW A NEW LARGER FRAGMENT IN THERE...*/
                    if (substringend < substringstart) {
                        linearlayoutInsert(2, 0); //DONT LOG A NEW ROW
                    } else {

                        linearlayoutInsert((linewidth + width + displaymarginpadding), displaywidth);
                    }


                    widthExtra = widthExtra - maxWidthAllowed;
                    if(BuildConfig.DEBUG) {Log.d(TAG, "NEW widthExtra: " + widthExtra);}
                    substringstart = substringend;

                    if ((linewidth + width + displaymarginpadding) < displaywidth || widthExtra < 0) {
                        substringend = onScreenText.length();

                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "choppedfragment substringstart: " + substringstart);
                            Log.d(TAG, "choppedfragment substringend: " + substringend);
                        }

                        CharSequence choppedTextFragmentRemainder = onScreenText.subSequence(substringstart, substringend);
//                        String choppedTextFragmentRemainder = onScreenText.substring(substringstart, substringend);
                        if(BuildConfig.DEBUG) {Log.d(TAG, "choppedTextFragmentRemainder: " + choppedTextFragmentRemainder);}
                        TextView textViewRemainder = new TextView(getContext());

                        LinearLayout innerLinearLayout3Remainder = new LinearLayout(getContext());
                        innerLinearLayout3Remainder.setOrientation(LinearLayout.VERTICAL);

                        textViewRemainder.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));

                        textViewRemainder.setText(choppedTextFragmentRemainder);
                        textViewRemainder.setMovementMethod(LinkMovementMethod.getInstance());
                        textViewRemainder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);

                        Rect bounds2 = new Rect();
                        Paint textPaint2 = textViewRemainder.getPaint();
                        textPaint2.getTextBounds(choppedTextFragmentRemainder.toString(), 0, choppedTextFragmentRemainder.length(), bounds2);

                        int width_chopped = bounds2.width();
                        innerLinearLayout3Remainder.addView(textViewRemainder);
                        innerLinearLayout3Remainder.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        linearLayout.addView(innerLinearLayout3Remainder);

                        linewidth = linewidth + width_chopped;

                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "chopped linewidth: " + linewidth);
                            Log.d(TAG, "chopped width: " + width);
                        }

                        widthExtra = (linewidth + displaymarginpadding) - displaywidth;
                        maxWidthAllowed = displaywidth - linewidth - displaymarginpadding;


                        if (linewidth == 0 && widthExtra > 0) {  // like if it's the last fragment of a line, starting on a new line. Just print the damn thing (on the new line)
                            linearlayoutInsert((linewidth + (displaywidth + widthExtra) + displaymarginpadding), displaywidth);
                        } else if (linewidth == 0 && widthExtra < 0) {
                            linearlayoutInsert((linewidth + (displaywidth + widthExtra) + displaymarginpadding), displaywidth);
                        } else {
                            linearlayoutInsert((linewidth + displaymarginpadding), displaywidth);
                        }

                    } else {

                        maxWidthAllowed = displaywidth - linewidth - displaymarginpadding;

                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "substringend calculation--maxwidthallowed: " + maxWidthAllowed);
                            Log.d(TAG, "substringend calculation--width: " + width);
                            Log.d(TAG, "substringend calculation--onScreenText.length: " + onScreenText.length());
                        }

                        substringend = Math.round(((float) maxWidthAllowed / (float) width) * onScreenText.length()) + substringstart;
                        if(BuildConfig.DEBUG) {Log.d(TAG, "substring rounding: " + ((float) maxWidthAllowed / (float) width) * onScreenText.length());}
                    }

                    if(BuildConfig.DEBUG) {
                        Log.d(TAG, "NEW substringstart: " + substringstart);
                        Log.d(TAG, "NEW substringend: " + substringend);
                    }
                    if (substringend > onScreenText.length()) {
                        if(BuildConfig.DEBUG) {Log.d(TAG, "SHIT WORKAROUND substringend Revised to: " + substringend);}
                        substringend = onScreenText.length();
                    }

                }

            } else {

                LinearLayout innerLinearLayout3 = new LinearLayout(getContext());
                innerLinearLayout3.setOrientation(LinearLayout.VERTICAL);

                textView_Test.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                textView_Test.setText(onScreenText);
                textView_Test.setMovementMethod(LinkMovementMethod.getInstance());
                textView_Test.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                innerLinearLayout3.addView(textView_Test);
                innerLinearLayout3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(innerLinearLayout3);
                linewidth = linewidth + width;
                linearlayoutInsert((linewidth + displaymarginpadding), displaywidth);

                if(BuildConfig.DEBUG) {Log.d(TAG, "new linewidth = " + linewidth);}

            }

//        }
    }

    public void addSpinnerToLayout(WordEntry wordEntry) {
/** INPUT THE SPINNER */
        if (linewidth + spinnerwidth > displaywidth) {
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "special insert linewidth: " + linewidth);
                Log.d(TAG, "special insert displaywidth: " + displaywidth);
            }
            linearlayoutInsert(2, 1); // if there isn't room for the special kanji box, insert it all on a new line
        }

        FillinSentencesSpinner spinnerData = wordEntry.getFillinSentencesSpinner();

        LinearLayout innerLinearLayout4 = new LinearLayout(getContext());
        innerLinearLayout4.setOrientation(LinearLayout.VERTICAL);

        final Spinner spinner = (Spinner)LayoutInflater.from(getActivity()).inflate(R.layout.spinneritem, null);

//        spinner.setTag(keyvalue);//setting the tag as the index in spinnerDataTreeMap so we can extract from map later


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

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.simple_spinner_item_redwedding, spinnerData.getOptions()); //selected item will look like a spinner set from XML
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerArrayAdapter);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(spinnerwidth, ViewGroup.LayoutParams.MATCH_PARENT);
                params.weight = 1.0f;
                params.gravity = Gravity.BOTTOM;
                spinner.setLayoutParams(params);
            }

        } else {

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, spinnerData.getOptions()); //selected item will look like a spinner set from XML
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerArrayAdapter);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(spinnerwidth, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.BOTTOM;
            spinner.setLayoutParams(params);

//            String spinnerText = spinner.getSelectedItem().toString();
//
//            ArrayList<String> tmpStringArray = new ArrayList<>();
//            tmpStringArray.add(String.valueOf(kanjiID));
//            tmpStringArray.add(onScreenText); //Add the actual correctAnswer
//            tmpStringArray.add(spinnerText); //Add the selected answer on the screen
//
//            spinnerDataTreeMap.put(keyvalue, tmpStringArray);

        }


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Updating an array containing correct answer vs. selected answer.

//                int treemapIndex = Integer.parseInt(spinner.getTag().toString());
//                ArrayList<String> tmpStringArray = spinnerDataTreeMap.get(treemapIndex);
//                tmpStringArray.remove(2);
//                tmpStringArray.add(spinner.getSelectedItem().toString());
//                spinnerDataTreeMap.put(treemapIndex, tmpStringArray);

                /** if the user has gotten an incorrect answer, make it so now when they go back and correct that answer
                 *  they don't have to hit the score button. it automatically checks it and moves on */
//                if (isIncorrectBoolean) {
//                    scoreTheAnswers();
//TODO THIS PART
//                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        innerLinearLayout4.addView(spinner);
        innerLinearLayout4.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(innerLinearLayout4);
        linewidth = linewidth + spinnerwidth;
        linearlayoutInsert((linewidth + displaymarginpadding), displaywidth);



    }

    public void linearlayoutInsert(int totallinewidth, int displaywidth) {

        if (displaywidth == 0) {
            linewidth = 0;
        } else if (totallinewidth >= displaywidth) {

            LinearLayout tmplinearLayout = linearLayout;
            linearLayoutVerticalMain.addView(tmplinearLayout);

            //now reset the linear layout... (hope that works)
            linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linewidth = 0;
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "BIGLAYOUT childcount: " + linearLayoutVerticalMain.getChildCount());
                Log.d(TAG, "linearlayoutInsert Completed");
            }
        } else {
            if(BuildConfig.DEBUG){Log.d(TAG, "linearlayoutInset Unnecessary");}
        }
    }



}
