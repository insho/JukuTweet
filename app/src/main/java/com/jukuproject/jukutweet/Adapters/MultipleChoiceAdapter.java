package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
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
 * Adapter for multiple choice question grid in {@link com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment}
 */
public class MultipleChoiceAdapter extends ArrayAdapter<WordEntry> {

    private final String TAG = "QuizMultChoice_Adapter";

    public MultipleChoiceAdapter(Context context
            , ArrayList<WordEntry> dataset
            , int textViewResourceId
            , int rowheight
            , String quizType
    ,ArrayList<Integer> wrongAnswerIds
    ) {

        super(context,textViewResourceId, dataset);
        mFieldId = textViewResourceId;
        mrowheight = rowheight;
        mQuizType = quizType;
        mWrongAnswerIds =wrongAnswerIds;
    }

    private int mFieldId;
    private int mrowheight;
    private String mQuizType;
    private ArrayList<Integer> mWrongAnswerIds;
    private Integer mGradeCorrectAnswerId;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        WordEntry wordEntry = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mFieldId, parent, false);
        }
        TextView answer = (TextView) convertView.findViewById(R.id.text1);
        answer.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        answer.setMaxLines(2);
        //If the answer was already answered incorrectly make it invisible
        if(mWrongAnswerIds.contains(wordEntry.getId())) {
            answer.setOnClickListener(null);
            answer.setVisibility(View.INVISIBLE);
        } else if (mGradeCorrectAnswerId != null && mGradeCorrectAnswerId.equals(wordEntry.getId())) {
            answer.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorJukuGreen));
        } else {

            answer.setText(replacedDefinition(wordEntry.getQuizAnswer(mQuizType)));
            answer.setTag(wordEntry.getId());
        }

        if(mrowheight>0) {
            answer.setHeight(mrowheight);
        }


        return convertView;
    }

    /**
     * Takes a raw definition string from the Edict database and replaces the definition "markers"
     * between the definitions (i.e. (1)to go(2)to come) with semicolons (To go; to come)
     * @param sentence Raw definition
     * @return The more beautiful definition
     */
    public String replacedDefinition(String sentence) {

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

        return sentence;
    }

    /**
     * Notifies dataset in this adapter when  a user clicks on the wrong answer in
     *  {@link com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment}.
     * @param updatedWrongAnswerPositions The updated set of wrong answer positions. These must be made invisible
     * @param updatedCorrectAnswerId updated correct answer Id
     */
    public void changeRowColorsAndVisibility(ArrayList<Integer> updatedWrongAnswerPositions, Integer updatedCorrectAnswerId) {
        this.mWrongAnswerIds = updatedWrongAnswerPositions;
        this.mGradeCorrectAnswerId = updatedCorrectAnswerId;
        notifyDataSetChanged();
    }


}
