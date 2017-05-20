package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Adapter for postquiz stats in {@link com.jukuproject.jukutweet.Fragments.StatsFragmentFillintheBlanks}. Each tweet from the quiz is displayed
 * on the main row, and below it the {@link TweetWordsResultsAdapter} displays a sublist of each Spinner word in the tweet, colored Red
 * if the word was answered incorrectly, Green if correct. Click on the word to open up {@link com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog}
 *
 * @see com.jukuproject.jukutweet.Fragments.StatsFragmentFillintheBlanks
 */
public class PostQuizStatsFillintheBlankAdapter extends RecyclerView.Adapter<PostQuizStatsFillintheBlankAdapter.ViewHolder> {
    String TAG = "TEST-FillSentStatAdap";

    private Context mContext;
    private ArrayList<Tweet> mFillInSentResults;
    private int superadaptertextviewheightsize= 0;
    private int mAdapterRowHeightMultiplier;
    private RxBus mRxBus;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtRowNumber;
        public TextView txtSentence;
        public ListView lstSentenceWords;

        public ViewHolder(View v) {
            super(v);

            txtRowNumber = (TextView) v.findViewById(R.id.textViewFillInStats_RowNumber);
            txtSentence = (TextView) v.findViewById(R.id.textViewFillInStats_Sentence);
            lstSentenceWords = (ListView) v.findViewById(R.id.listViewFillInStats_SentenceWords);
        }


    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PostQuizStatsFillintheBlankAdapter(Context context
            , ArrayList<Tweet> fillInSentResults
            , Integer adapterRowHeightMultiplier
            , RxBus rxBus) {
        mFillInSentResults = fillInSentResults;
        mAdapterRowHeightMultiplier = adapterRowHeightMultiplier;
        mContext = context;
        this.mRxBus = rxBus;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PostQuizStatsFillintheBlankAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fillinsentences_stats_listitem, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }



    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if(BuildConfig.DEBUG){Log.d(TAG, "position: " + position);}

        Tweet tweet = mFillInSentResults.get(holder.getAdapterPosition());
        ArrayList<WordEntry> spinnerWords = new ArrayList<>();

        /* Input the tweet and color the spinner words according to whether they were correct or not  */
        try {
            final SpannableStringBuilder sb = new SpannableStringBuilder(tweet.getText());
            if(tweet.getWordEntries() != null) {
                for(WordEntry wordEntry : tweet.getWordEntries()) {

                    if(wordEntry.isSpinner() && wordEntry.getFillinSentencesSpinner() != null) {
                        spinnerWords.add(wordEntry);
                        ForegroundColorSpan fcs;
                        if(wordEntry.getFillinSentencesSpinner().isCorrectFirstTry()) {

                            fcs = new ForegroundColorSpan(ContextCompat.getColor(mContext,R.color.colorJukuGreen));
                        } else {
                            fcs = new ForegroundColorSpan(ContextCompat.getColor(mContext,R.color.colorJukuRed));
                        }
                        sb.setSpan(fcs, wordEntry.getStartIndex(), wordEntry.getEndIndex(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                }
                holder.txtSentence.setText(sb);

            } else {
                holder.txtSentence.setText(tweet.getText());
            }
            holder.txtRowNumber.setText(mContext.getString(R.string.rownumber, holder.getAdapterPosition()+1));
        } catch (NullPointerException e) {
            holder.txtSentence.setText(tweet.getText());
            Log.e(TAG,"fillblank Postquizstats setting tweet right/wrong word colors nullpointer failure : " + e);
        }

            /* Create list of word results that appears below the tweet. Due to there being an adapter within an adapter
            * (TweetWordsResultsAdapter within PostQuizStatsFillintheBlankAdapter), the TweetWordsResultsAdapter contents must be measured
            * and the height specified the TweetWordsResultsAdapter adapter will only show 1 row and not show 2nd or 3rd rows if they exist.
            */
            ViewGroup.LayoutParams layoutParams = holder.lstSentenceWords.getLayoutParams();

            if(superadaptertextviewheightsize==0) {
                Rect bounds = new Rect();
                TextView textView_Test =new TextView(mContext);
                String text = "\u2022 猫";
                textView_Test.setText(text);

                textView_Test.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                Paint textPaint = textView_Test.getPaint();
                textPaint.getTextBounds(text, 0, text.length(), bounds);

                if(BuildConfig.DEBUG){Log.d(TAG,"bounds.height(): " + bounds.height());}
                superadaptertextviewheightsize = (bounds.height()+mAdapterRowHeightMultiplier);
            }
        layoutParams.height = superadaptertextviewheightsize* spinnerWords.size();


        if(BuildConfig.DEBUG){Log.d(TAG,"layoutparams.height = " + layoutParams.height );}
        if(spinnerWords.size()>0) {
            TweetWordsResultsAdapter defadapter = new TweetWordsResultsAdapter(mContext,spinnerWords);
            holder.lstSentenceWords.setAdapter(defadapter);
            if(BuildConfig.DEBUG){Log.d(TAG, "layoutparams.height(2) = " + layoutParams.height);}

            holder.lstSentenceWords.setClickable(false);
            holder.lstSentenceWords.setFocusable(false);
            holder.lstSentenceWords.setItemsCanFocus(false);
            holder.lstSentenceWords.setDivider(null);
            holder.lstSentenceWords.setLayoutParams(layoutParams);
        }

    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mFillInSentResults.size();
    }


    /**
     * Adapter for sub-list of spinner words within each tweet in the quiz, and whether they were
     * answered correctly or not
     */
    private class TweetWordsResultsAdapter extends ArrayAdapter<WordEntry> {
        public TweetWordsResultsAdapter(Context context
                , ArrayList<WordEntry> wordEntries
        ) {
            super(context,0, wordEntries);
            mContext = context;
            mWordEntries = wordEntries;
        }
        private Context mContext;
        private ArrayList<WordEntry> mWordEntries;
        int superadaptertextviewheightsize= 0;
        @Override
        public View getView(final int position, View v, ViewGroup parent) {

            if (v == null) {
                v = LayoutInflater.from(getContext()).inflate(R.layout.spinner_listitem, parent, false);
            }

            WordEntry wordEntry = mWordEntries.get(position);

            TextView textWord = (TextView) v.findViewById(R.id.text1);
            String wordstring = "\u2022 " + wordEntry.getKanji();
            textWord.setText(wordstring);
                if(wordEntry.getFillinSentencesSpinner().isCorrectFirstTry()) {
                    textWord.setTextColor(ContextCompat.getColor(mContext, R.color.colorJukuGreen));
                } else {
                    textWord.setTextColor(ContextCompat.getColor(mContext, R.color.colorJukuRed));
                }


            textWord.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            textWord.setFocusable(false);
            textWord.setClickable(false);

            textWord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        mRxBus.send(mWordEntries.get(position));
                    } catch (NullPointerException e) {
                        Log.e("TEST-postquizadap","Error sending mult choice result word entry back");
                    }
                }
            });

            v.measure(ViewGroup.LayoutParams.MATCH_PARENT, View.MeasureSpec.UNSPECIFIED);
            superadaptertextviewheightsize = v.getMeasuredHeight();
            return v;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }

}