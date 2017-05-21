package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.ChooseFavoriteListsPopupWindow;
import com.jukuproject.jukutweet.FavoritesColors;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Adapter displaying WordEntries found within a Tweet that has been broken up by the {@link com.jukuproject.jukutweet.TweetParser}.
 * User can long click the word to bring up {@link com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog}, as well as clicking on the avorites
 * star to bring up the {@link ChooseFavoriteListsPopupWindow}, where the word can be added/subtracted from word lists
 */
public class TweetBreakDownAdapter extends RecyclerView.Adapter<TweetBreakDownAdapter.ViewHolder>  {
    private String TAG = "TEST-tweetBreakAdapter";
    private Context mContext;
    private ArrayList<WordEntry> mWords;
    private ArrayList<String> mActiveFavoriteStars;
    private RxBus mRxBus;

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView txtColorBar;
        private TextView txtKanji;
        private TextView txtFurigana;
        private TextView lstDefinitions;
        public ImageButton imgStar;
        private FrameLayout imgStarLayout;
        private LinearLayout mainLayout;

        public ViewHolder(View v) {
            super(v);
            txtColorBar = (TextView) v.findViewById(R.id.colorbar);
            txtKanji = (TextView) v.findViewById(R.id.textViewBrowseAdapter_Kanji);
            txtFurigana  = (TextView) v.findViewById(R.id.textViewBrowseAdapter_Furigana);
            lstDefinitions = (TextView) v.findViewById(R.id.textViewlstDefinitions);
            imgStar = (ImageButton) v.findViewById(R.id.favorite);
            mainLayout = (LinearLayout) v.findViewById(R.id.browseitems_layout2);
            imgStarLayout = (FrameLayout) v.findViewById(R.id.browseitems_frameLayout);
        }
    }

    public TweetBreakDownAdapter(Context context
            , ArrayList<WordEntry> words
            , ArrayList<String> activeFavoriteStars
            , RxBus rxbus) {
        this.mContext = context;
        this.mWords = words;
        this.mRxBus = rxbus;
        this.mActiveFavoriteStars = activeFavoriteStars;
    }


    @Override
    public TweetBreakDownAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tweetbreakdown_recycler_row, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position ) {

        holder.mainLayout.setSelected(false);
        holder.imgStar.setVisibility(View.VISIBLE);
        holder.imgStar.setClickable(false);
        holder.imgStarLayout.setClickable(true);
        holder.imgStarLayout.setLongClickable(true);
        holder.txtKanji.setText(mWords.get(holder.getAdapterPosition()).getKanji());
        holder.txtFurigana.setText(mWords.get(holder.getAdapterPosition()).getFurigana());

        if(BuildConfig.DEBUG) {
            Log.d(TAG,"favs kanji: " + mWords.get(holder.getAdapterPosition()).getKanji());
            Log.d(TAG,"favs: " + (mWords.get(holder.getAdapterPosition()).getItemFavorites() == null));
        }

        Integer starColorDrawableInt = FavoritesColors.assignStarResource(false,mWords.get(holder.getAdapterPosition()).getItemFavorites(),mActiveFavoriteStars);
        holder.imgStar.setImageResource(starColorDrawableInt);
        if(starColorDrawableInt!=R.drawable.ic_star_multicolor) {
            try {
                holder.imgStar.setColorFilter(ContextCompat.getColor(mContext,FavoritesColors.assignStarColor(mWords.get(holder.getAdapterPosition()).getItemFavorites(),mActiveFavoriteStars)));
            } catch (NullPointerException e) {
                Log.e(TAG,"tweetBreakDownAdapter multistar Nullpointer error setting star color filter in word detail popup dialog... Need to assign item favorites to WordEntry(?)" + e.getCause());
            }
        } else {
            holder.imgStar.setColorFilter(null);
        }

        try {
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, mWords.get(holder.getAdapterPosition()).getColorValue()));
        } catch (Exception e) {
            Log.e(TAG,"Tweetbreakdown adding colorbar exception");
        }

        holder.lstDefinitions.setText(mWords.get(holder.getAdapterPosition()).getDefinitionMultiLineString(10));
        holder.lstDefinitions.setTypeface(null, Typeface.ITALIC);
        holder.lstDefinitions.setTag(mWords.get(holder.getAdapterPosition()).getId());
        holder.lstDefinitions.setFocusable(false);
        holder.lstDefinitions.setClickable(false);


        holder.imgStarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "mActiveFavoriteStars: " + mActiveFavoriteStars);
                    Log.d(TAG, "should open: " + mWords.get(holder.getAdapterPosition()).getItemFavorites().shouldOpenFavoritePopup(mActiveFavoriteStars));
                }
                if(mWords.get(holder.getAdapterPosition()).getItemFavorites().shouldOpenFavoritePopup(mActiveFavoriteStars)) {
//                    showFavoriteListPopupWindow(holder);
                    mRxBus.send(holder.getAdapterPosition());
                } else {
                    if(FavoritesColors.onFavoriteStarToggle(mContext,mActiveFavoriteStars,mWords.get(holder.getAdapterPosition()))) {
                        holder.imgStar.setImageResource(R.drawable.ic_star_black);

                        try {
                            holder.imgStar.setColorFilter(ContextCompat.getColor(mContext,FavoritesColors.assignStarColor(mWords.get(holder.getAdapterPosition()).getItemFavorites(),mActiveFavoriteStars)));
                        } catch (NullPointerException e) {
                            Log.e(TAG,"tweetBreakDownAdapter blackstar Nullpointer error setting star color filter in word detail popup dialog... Need to assign item favorites to WordEntry(?)" + e.getCause());
                        }
                        mRxBus.send(mWords.get(holder.getAdapterPosition()));
                    } else {
                        Log.e(TAG,"OnFavoriteStarToggle did not work...");
                    }
                }
            }
        });



        holder.imgStarLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                mRxBus.send(holder.getAdapterPosition());
//                showFavoriteListPopupWindow(holder);

                return true;
            }
        });

        holder.mainLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mRxBus.sendLongClick(mWords.get(holder.getAdapterPosition()));
                return false;
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mWords.size();
    }

}

