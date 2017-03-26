package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.Models.WordEntryFavorites;
import com.jukuproject.jukutweet.PopupChooseFavoriteLists;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.TestPopupWindow;

import java.util.ArrayList;

import rx.functions.Action1;


public class BrowseMyListAdapter extends RecyclerView.Adapter<BrowseMyListAdapter.ViewHolder>  {
    String TAG = "Test-BrowseMyAd";
    private static final boolean debug = false;

    private Context mContext;
//    private DisplayMetrics mMetrics;
    private ArrayList<WordEntry> mWords;
    private  ColorThresholds mColorThresholds;
    private ArrayList<String> mActiveFavoriteStars;
    private RxBus mRxBus;
    private ArrayList<Integer> mSelectedEntries;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtColorBar;
        public TextView txtKanji;
        public TextView txtFurigana;
        public TextView lstDefinitions;
//        public ImageButton imgStar;
//        public FrameLayout imgStarLayout;
        public RxBus rxBus;
        public LinearLayout layout;

        public ViewHolder(View v) {
            super(v);
            txtColorBar = (TextView) v.findViewById(R.id.colorbar);
            txtKanji = (TextView) v.findViewById(R.id.textViewBrowseAdapter_Kanji);
            txtFurigana  = (TextView) v.findViewById(R.id.textViewBrowseAdapter_Furigana);
            lstDefinitions = (TextView) v.findViewById(R.id.textViewlstDefinitions);
//            imgStar = (ImageButton) v.findViewById(R.id.favorite);
            layout = (LinearLayout) v.findViewById(R.id.browseitems_layout);
//            imgStarLayout = (FrameLayout) v.findViewById(R.id.browseitems_frameLayout);
        }
    }


    public BrowseMyListAdapter(Context context, ArrayList<WordEntry> words
            ,ColorThresholds colorThresholds
            , ArrayList<String> activeFavoriteStars
            ,RxBus rxbus
            ,ArrayList<Integer> selectedEntries) {
        mContext = context;
        mWords = words;
        mColorThresholds = colorThresholds;
        mRxBus = rxbus;
        mSelectedEntries = selectedEntries;
        this.mActiveFavoriteStars = activeFavoriteStars;
    }


    @Override
    public BrowseMyListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tweetbreakdown_recycler_row, parent, false);

        return new ViewHolder(v);
    }

    public WordEntry getItem(int position) {
        return mWords.get(position);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position ) {

        final WordEntry wordEntry = mWords.get(holder.getAdapterPosition());

        holder.layout.setSelected(false);
        holder.txtKanji.setText(wordEntry.getKanji());
        holder.txtFurigana.setText(wordEntry.getFurigana());

        /* Parse the definition into an array of multiple lines, if there are multiple sub-definitions in the string */
        if(wordEntry.getTotal()< mColorThresholds.getGreyThreshold()) {
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuGrey));
        } else if(wordEntry.getPercentage()< mColorThresholds.getRedthreshold()){
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuRed));
        } else if (wordEntry.getPercentage()< mColorThresholds.getYellowthreshold()){
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
            holder.itemView.setSelected(true);
        } else {
            holder.itemView.setSelected(false);
        }

        holder.layout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /**  The external (BrowseBlocks) onclick listener happens first, apparently. So the hashmap operations should already be done. Just update the visuals*/

                if(holder.itemView.isSelected()) {
                    holder.itemView.setSelected(false);
                    if(mSelectedEntries.contains(wordEntry.getId())) {
                        mSelectedEntries.remove((int)wordEntry.getId());
                    }
                    //Send id back to MyListBrowseFragment so it can be added to the selected map
                    mRxBus.send(wordEntry.getId());
                } else {
                    if(!mSelectedEntries.contains(wordEntry.getId())) {
                        mSelectedEntries.add(wordEntry.getId());
                    }
                }
//                mActivity.invalidateOptionsMenu();
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mWords.size();
    }


}

