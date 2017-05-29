package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.MultChoiceResult;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Adapter for postquiz stats in {@link com.jukuproject.jukutweet.Fragments.StatsFragmentMultipleChoice}. The correct answer
 * is displayed in each row, and colored Red if incorrect, Green if correct. Click on the word to open up {@link com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog}
 *
 * @see com.jukuproject.jukutweet.Fragments.StatsFragmentMultipleChoice
 */
public class PostQuizStatsMultipleChoiceAdapter extends ArrayAdapter<ArrayList<MultChoiceResult>> {

    private ArrayList<MultChoiceResult> mQuestionResults;
    private boolean mIsWordBuilder;
    private RxBus mRxBus;


    public PostQuizStatsMultipleChoiceAdapter(Context context
            , ArrayList<MultChoiceResult> questionResults
            , Boolean iswordbuilder
            , RxBus rxBus) {
        super(context,0);
        mQuestionResults = questionResults;
        mIsWordBuilder = iswordbuilder;
        this.mRxBus = rxBus;

    }

    @Override
    public int getCount() {
        return mQuestionResults.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        final MultChoiceResult multChoiceResult = mQuestionResults.get(position);


        if(!mIsWordBuilder) {

            final String questionNumber = multChoiceResult.getCurrentTotal() + ". ";

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_stats_multiplechoice_recycler_row, parent, false);
            }

            TextView questionNumberTextView = (TextView) convertView.findViewById(R.id.listResults1);
            questionNumberTextView.setVisibility(TextView.VISIBLE);
            questionNumberTextView.setText(questionNumber);

            TextView answerText = (TextView) convertView.findViewById(R.id.listResults2);
            answerText.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
            convertView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));

            answerText.setText(multChoiceResult.getDetailInfo());

            if(multChoiceResult.getCorrect()){
                answerText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorJukuGreen));
            } else {
                answerText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorJukuRed));
            }
            answerText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        mRxBus.send(multChoiceResult.getWordEntry());
                    } catch (NullPointerException e) {
                        Log.e("TEST-postquizadap","Error sending mult choice result word entry back");
                    }
                }
            });
        } else {
            final String questionNumber = multChoiceResult.getCurrentTotal() + " - ";

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_stats_multiplechoice_recycler_row, parent, false);
            }

            TextView questionNumberTextView = (TextView) convertView.findViewById(R.id.listResults1);
            questionNumberTextView.setVisibility(TextView.VISIBLE);
            questionNumberTextView.setText(questionNumber);

            TextView answerText = (TextView) convertView.findViewById(R.id.listResults2);
            answerText.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
            convertView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));

            answerText.setText(multChoiceResult.getDetailInfo());

        }
        return convertView;
    }


}