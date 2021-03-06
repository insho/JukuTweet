package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Adapter for Recycler in {@link com.jukuproject.jukutweet.Fragments.WordListBrowseFragment}. Shows word data,
 * and star for adding the word to a WordList ({@link com.jukuproject.jukutweet.Fragments.WordListFragment})
 *
 * @see com.jukuproject.jukutweet.Fragments.WordListBrowseFragment
 */
public class BrowseWordsAdapter extends RecyclerView.Adapter<BrowseWordsAdapter.ViewHolder>  {

    private Context mContext;
    private ArrayList<WordEntry> mWords;
    private  ColorThresholds mColorThresholds;
    private RxBus mRxBus;
    private ArrayList<Integer> mSelectedEntries;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtColorBar;
        public TextView txtKanji;
        public TextView txtFurigana;
        public TextView lstDefinitions;

        public LinearLayout layout;

        public ViewHolder(View v) {
            super(v);
            txtColorBar = (TextView) v.findViewById(R.id.colorbar);
            txtKanji = (TextView) v.findViewById(R.id.textViewBrowseAdapter_Kanji);
            txtFurigana  = (TextView) v.findViewById(R.id.textViewBrowseAdapter_Furigana);
            lstDefinitions = (TextView) v.findViewById(R.id.textViewlstDefinitions);
            layout = (LinearLayout) v.findViewById(R.id.browseitems_layout2);

        }
    }


    public BrowseWordsAdapter(Context context, ArrayList<WordEntry> words
            , ColorThresholds colorThresholds
            , RxBus rxbus
            , ArrayList<Integer> selectedEntries) {
        mContext = context;
        mWords = words;
        mColorThresholds = colorThresholds;
        mRxBus = rxbus;
        mSelectedEntries = selectedEntries;
    }


    @Override
    public BrowseWordsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tweetbreakdown_recycler_row, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final WordEntry wordEntry = mWords.get(holder.getAdapterPosition());

        holder.layout.setSelected(false);
        holder.txtKanji.setText(wordEntry.getKanji());
        holder.txtFurigana.setText(wordEntry.getFurigana());

        /* Parse the definition into an array of multiple lines, if there are multiple sub-definitions in the string */
        if(wordEntry.getTotal()< mColorThresholds.getGreyThreshold()) {
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuGrey));
        } else if(wordEntry.getPercentage()< mColorThresholds.getRedThreshold()){
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuRed));
        } else if (wordEntry.getPercentage()< mColorThresholds.getYellowThreshold()){
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuYellow));
        } else {
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuGreen));
        }


        holder.lstDefinitions.setText(wordEntry.getDefinitionMultiLineString(10));
        holder.lstDefinitions.setTypeface(null, Typeface.ITALIC);
        holder.lstDefinitions.setTag(wordEntry.getId());
        holder.lstDefinitions.setFocusable(false);
        holder.lstDefinitions.setClickable(false);

        if(mSelectedEntries.contains(wordEntry.getId())){
            holder.layout.setSelected(true);
        } else {
            holder.layout.setSelected(false);
        }

        holder.layout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /**  The external (BrowseBlocks) onclick listener happens first, apparently. So the hashmap operations should already be done. Just update the visuals*/

                if(holder.layout.isSelected()) {
                    holder.layout.setSelected(false);
                    //Send id back to WordListBrowseFragment so it can be added to the selected map
                } else {
                    holder.layout.setSelected(true);
                }
                notifyItemChanged(holder.getAdapterPosition());
                mRxBus.send(wordEntry.getId());
            }
        });

        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mRxBus.sendLongClick(wordEntry);
                return false;
            }
        });

    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mWords.size();
    }

    /**
     * Updates the dataset and selected entries with new items when, for instance,
     * kanji are removed from a list in {@link com.jukuproject.jukutweet.Fragments.WordListBrowseFragment}
     *
     * @param updatedDataSet new dataset
     * @param updatedSelectedEntries new set of selected entries
     */
    public void swapDataSet(ArrayList<WordEntry> updatedDataSet,ArrayList<Integer> updatedSelectedEntries) {
        this.mWords = updatedDataSet;
        this.mSelectedEntries = updatedSelectedEntries;
        notifyDataSetChanged();
    }
}

