package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by JClassic on 4/3/2017.
 */


public class MultipleChoiceAdapter extends ArrayAdapter<WordEntry> {

    private final String TAG = "QuizMultChoice_Adapter";

    public MultipleChoiceAdapter(Context context
            , ArrayList<WordEntry> dataset
            , int textViewResourceId
//            , String[] currentArray
            , int rowheight
            , int displaywidth
            , String quizType
    ,ArrayList<Integer> wrongAnswerIds) {

        super(context,textViewResourceId, dataset);
        mFieldId = textViewResourceId;
        mDataSet = dataset;
        mrowheight = rowheight;
        mContext = context;
        mDisplayWidth = displaywidth;
        mQuizType = quizType;
        mWrongAnswerIds =wrongAnswerIds;
    }

    private Context mContext;
    private ArrayList<WordEntry> mDataSet;
    private int mFieldId;
    private int mrowheight;
    private int mDisplayWidth;
    private String mQuizType;
    private ArrayList<Integer> mWrongAnswerIds;


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        WordEntry wordEntry = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mFieldId, parent, false);
        }
        TextView answer = (TextView) convertView.findViewById(R.id.text1);

        //If the answer was already answered incorrectly make it invisible
        if(mWrongAnswerIds.contains(wordEntry.getId())) {
            answer.setOnClickListener(null);
            answer.setVisibility(View.INVISIBLE);
        } else {

            /* If the question or answer includes the words definition, we need to parse
         * out the definition so it only includes the first defintion entry. We do this by looking
         * for (1) and (2) markers in the string, and taking the text between them.
         */
            String questionOption;
            if (mQuizType.equals("Kanji to Definition") && wordEntry.getDefinition().contains("(1)")) {
                int endIndex = wordEntry.getDefinition().length();
                if (wordEntry.getDefinition().contains("(2)")) { //If we can find the next "(#)" in the string, we'll use it as this definition's end point
                    endIndex = wordEntry.getDefinition().indexOf("(2)");
                }

                String sentence = wordEntry.getDefinition().substring(wordEntry.getDefinition().indexOf("(1)") + 3, endIndex);
                //Capitalize it
                if (sentence.length() > 1) {
                    sentence = sentence.substring(0, 1).toUpperCase() + sentence.substring(1).toLowerCase();
                }
                questionOption = sentence;
            } else { //if the thing doesn't contain a "(1)", just print the whole definition in line 1 of the array.
                String sentence = wordEntry.getDefinition();
                if (sentence.length() > 1) {
                    sentence = wordEntry.getDefinition().substring(0, 1).toUpperCase() + wordEntry.getDefinition().substring(1).toLowerCase();
                }

                questionOption  = sentence;
            }

        /* Get the width on the screen of the prospective item. Then truncate it if it's over the limit. */
            TextView textView_Test = new TextView(mContext);
            textView_Test.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            textView_Test.setText(wordEntry.getQuizAnswer(mQuizType));
            textView_Test.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);


            Rect bounds = new Rect();
            Paint textPaint = textView_Test.getPaint();
            textPaint.getTextBounds(wordEntry.getQuizAnswer(mQuizType), 0, wordEntry.getQuizAnswer(mQuizType).length(), bounds);
//            int width = bounds.width();

//            if(width>mDisplayWidth) {
//                Log.d(TAG,"width: " + width + ", displaywidth: " + mDisplayWidth);
//                Log.d(TAG,"Q width overrun: " + questionOption);
//                Log.d(TAG,"answer: " + wordEntry.getQuizAnswer(mQuizType));
//
//                int excesslinewidth = width- mDisplayWidth;
//                double excesslinewidthpercentage = (double)excesslinewidth/(double)width;
//                double excesscharacters = (double)questionOption.length()*excesslinewidthpercentage;
//
//                Log.d(TAG,"excesslinewidth: " + excesslinewidth);
//                Log.d(TAG,"excesslinewidthpercentage: " + excesslinewidthpercentage);
//                Log.d(TAG,"excesscharacters: " + excesscharacters);
//
//
//                int buffer = 2;
//                if(questionOption.contains("(") || questionOption.contains(")") || questionOption.contains(",")) {
//                    buffer =4;
//                }
//
//                int maxallowablecharacters = questionOption.length()-(int)excesscharacters-buffer; //2 is the buffer here
//
//                Log.d(TAG,"questionOption length: " + questionOption.length());
//                Log.d(TAG,"excess chars: " + (int)excesscharacters);
//                Log.d(TAG,"wordentry length: " + wordEntry.getQuizAnswer(mQuizType).length());
//                Log.d(TAG,"charbuffer: " + buffer);
//                Log.d(TAG,"Max allowable chars: " + maxallowablecharacters);
//
//                String lastString = questionOption.substring(0,maxallowablecharacters);
//
//                if (lastString.contains(" ")) {
//                    lastString = lastString.substring(0,lastString.lastIndexOf(" "));
//                }
//
//                questionOption =  lastString + "...";
//                if (questionOption.contains(",...")) {
//                    questionOption = questionOption.substring(0,questionOption.lastIndexOf(",...") )+ "...";
//                }
//
//            }

//            Log.d(TAG,"Q OPTION FINAL: " + questionOption);

            answer.setText(replacedDefinition(wordEntry.getQuizAnswer(mQuizType),mDisplayWidth));
            answer.setTag(wordEntry.getId());

        }

        if(mrowheight>0) {
            answer.setHeight(mrowheight);
        }


        return convertView;
    }


    public String replacedDefinition(String sentence, int displayWidth) {

        //Capitalize first letter of sentence
        if(sentence.length()>1) {
            sentence = sentence.substring(0, 1).toUpperCase() + sentence.substring(1).toLowerCase();
        }
        sentence = sentence.replace("(1)",""); // get rid of the (1) at the beginning of the definition, no matter what

        //Replace definition markers: (1), (2), (3), etc, with "; "
        Matcher m = Pattern.compile("\\(([0-9])\\)").matcher(sentence);
        while(m.find()) {
            System.out.println(m.group(1));
            sentence = sentence.replace("(" + m.group(1) + ")","; ");
        }
//        String regex = "\\(([\\d]+)\\)";
//        sentence = sentence.replace(regex,"; ");
//        Log.e(TAG,"SUPERSENTENCE: " + sentence);
          /* Get the width on the screen of the prospective item. Then truncate it if it's over the limit. */
        TextView textView_Test = new TextView(getContext());
        textView_Test.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        textView_Test.setText(sentence);
        textView_Test.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);


        Rect bounds = new Rect();
        Paint textPaint = textView_Test.getPaint();
        textPaint.getTextBounds(sentence, 0, sentence.length(), bounds);
        int width = bounds.width();

        if(width>displayWidth) {
            Log.d(TAG,"width: " + width + ", displaywidth: " + displayWidth);
            Log.d(TAG,"Q width overrun: " + sentence);

            int excesslinewidth = width- displayWidth;
            double excesslinewidthpercentage = (double)excesslinewidth/(double)width;
            double excesscharacters = (double)sentence.length()*excesslinewidthpercentage;

            Log.d(TAG,"excesslinewidth: " + excesslinewidth);
            Log.d(TAG,"excesslinewidthpercentage: " + excesslinewidthpercentage);
            Log.d(TAG,"excesscharacters: " + excesscharacters);


            int buffer = 3;
            if(sentence.contains("(") || sentence.contains(")") || sentence.contains(",") || sentence.contains(". ")) {
                buffer =5;
            }

            int maxallowablecharacters = sentence.length()-(int)excesscharacters-buffer; //2 is the buffer here

            Log.d(TAG,"sentence length: " + sentence.length());
            Log.d(TAG,"excess chars: " + (int)excesscharacters);
            Log.d(TAG,"charbuffer: " + buffer);
            Log.d(TAG,"Max allowable chars: " + maxallowablecharacters);

            sentence = sentence.substring(0,maxallowablecharacters) +  "...";

//            if (lastString.contains(" ")) {
//                lastString = lastString.substring(0,lastString.lastIndexOf(" "));
//            }


            if (sentence.contains(",...")) {
                sentence = sentence.substring(0,sentence.lastIndexOf(",...") )+ "...";
            } else if (sentence.contains(", ...")) {
                sentence = sentence.substring(0,sentence.lastIndexOf(", ...") )+ "...";
            }
        }
        Log.d(TAG,"Returning sentence: " + sentence);
        return sentence;
    }


}
