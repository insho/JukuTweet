package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Adapter for Top and Bottom 10 list in {@link com.jukuproject.jukutweet.Fragments.StatsFragmentProgress}. Words are
 * colored according to their percent score, and clicking on them opens the {@link com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog}
 *
 * @see com.jukuproject.jukutweet.Fragments.StatsFragmentProgress
 */
public class StatsTopAndBottomAdapter extends ArrayAdapter<WordEntry> {
    public StatsTopAndBottomAdapter(Context context
            , ArrayList<WordEntry> wordEntries
            , ColorThresholds colorThresholds
            , RxBus rxBus
    ) {
        super(context,0, wordEntries);
        mWordEntries = wordEntries;
        this.mColorThresholds = colorThresholds;
        this.mRxBus = rxBus;

    }
    private ArrayList<WordEntry> mWordEntries;
    private ColorThresholds mColorThresholds;
    private RxBus mRxBus;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final WordEntry wordEntry = mWordEntries.get(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_stats_multiplechoice_recycler_row, parent, false);
            }
            TextView answerText = (TextView) convertView.findViewById(R.id.listResults2);

            /* If there aren't enough words to fill the top 5 list, the sql query will pass empty word entries to fill out the list
            * and in this case just put blank entries in the adapter (this is so the resulting list will be symmetrical)*/
            if(wordEntry.getId() != null) {
                answerText.setText(wordEntry.getKanji());
                answerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

                //Choose color of text (green--right, red--wrong)
                if(wordEntry.getTotal()<mColorThresholds.getGreyThreshold()) {
                    answerText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorJukuGrey));
                } else if(wordEntry.getPercentage()< mColorThresholds.getRedThreshold()){
                    answerText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorJukuRed));
                } else if (wordEntry.getPercentage()< mColorThresholds.getYellowThreshold()){
                    answerText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorJukuYellow));
                } else {
                    answerText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorJukuGreen));
                }

            } else {
                answerText.setText("");
            }
            answerText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRxBus.send(wordEntry);
                }
            });

        return convertView;
    }

}