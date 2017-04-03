package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

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
            if (mQuizType.equals("Kanji to English") && wordEntry.getDefinition().contains("(1)")) {
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
            textView_Test.setText(wordEntry.getQuizAnswer(mQuizType));
            textView_Test.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);


            Rect bounds = new Rect();
            Paint textPaint = textView_Test.getPaint();
            textPaint.getTextBounds(wordEntry.getQuizAnswer(mQuizType), 0, wordEntry.getQuizAnswer(mQuizType).length(), bounds);
            int width = bounds.width();


            if(width>mDisplayWidth) {
                int excesslinewidth = width- mDisplayWidth;
                double excesslinewidthpercentage = (double)excesslinewidth/(double)width;
                double excesscharacters = (double)wordEntry.getQuizAnswer(mQuizType).length()*excesslinewidthpercentage;

                int buffer = 2;
                if(wordEntry.getQuizAnswer(mQuizType).contains("(") || wordEntry.getQuizAnswer(mQuizType).contains(")") || wordEntry.getQuizAnswer(mQuizType).contains(",")) {
                    buffer =4;
                }

                int maxallowablecharacters = wordEntry.getQuizAnswer(mQuizType).length()-(int)excesscharacters-buffer; //2 is the buffer here

                String lastString = questionOption;

                lastString = questionOption.substring(0,maxallowablecharacters);
                String superlastString = lastString;

                if (lastString.contains(" ")) {
                    superlastString = lastString.substring(0,lastString.lastIndexOf(" "));
                }

                questionOption =  superlastString + "...";
                if (questionOption.contains(",...")) {
                    questionOption= questionOption.substring(0,questionOption.lastIndexOf(",...") )+ "...";
                } else {

                    questionOption=  superlastString + "...";
                }


//            if(debug) {
//                Log.d(TAG,"sentencewidth: " + width);
//                Log.d(TAG,"displaywidth: " + displaywidth);
//                Log.d(TAG,"excesslinewidthpercentage: " + excesslinewidthpercentage);
//                Log.d(TAG,"excesscharacters: " + excesscharacters);
//                Log.d(TAG,"maxallowablecharacters: " + maxallowablecharacters);
//                Log.d(TAG,"finalarrayitem:"  + arrayitem);
//            }
            }



            answer.setText(questionOption);
            answer.setTag(wordEntry.getId());

            //if it's already wrong? then make it invisible and unclickable
//        for(String s: mDataSet){
////            if(debug) {
////                Log.d(TAG, "xyzWrongAnswer:" + s + ":");
////                Log.d(TAG, "xyzarrayitem:" + fullArrayItem + ":");
////            }
//            if(s.equals(fullArrayItem)) {
//                answer.setOnClickListener(null);
//                answer.setVisibility(View.INVISIBLE);
//            }
//        }
        }

        if(mrowheight>0) {
            answer.setHeight(mrowheight);
        }


        return convertView;
    }

}
