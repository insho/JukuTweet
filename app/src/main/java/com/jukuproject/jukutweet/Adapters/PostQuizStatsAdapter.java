package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jukuproject.jukutweet.Models.MultChoiceResult;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Created by JClassic on 4/13/2017.
 */

public class PostQuizStatsAdapter extends ArrayAdapter<ArrayList<MultChoiceResult>> {

    public PostQuizStatsAdapter(Context context
            , ArrayList<MultChoiceResult> questionResults
            , Boolean iswordbuilder) {
        super(context,0);
        mQuestionResults = questionResults;
        mIsWordBuilder = iswordbuilder;
        this.mContext =context;

    }
    private ArrayList<MultChoiceResult> mQuestionResults;
    Context mContext;
    private boolean mIsWordBuilder;


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
            //TODO set popupwindow
//            answerText.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    WordDetailPopupWindow x = new WordDetailPopupWindow(mActivity, v, multChoiceResult.getPKey());
//                    x.CreateView();
//                }
//            });
        } else {
//                final String text = arrayList.get(1);
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