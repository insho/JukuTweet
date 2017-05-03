package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jukuproject.jukutweet.ChooseFavoriteListsPopupWindow;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.FavoritesColors;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

import rx.functions.Action1;

/**
 * Adapter displaying WordEntries found within a Tweet that has been broken up by the {@link com.jukuproject.jukutweet.TweetParser}.
 * User can long click the word to bring up {@link com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog}, as well as clicking on the avorites
 * star to bring up the {@link ChooseFavoriteListsPopupWindow}, where the word can be added/subtracted from word lists
 */
public class TweetBreakDownAdapter extends RecyclerView.Adapter<TweetBreakDownAdapter.ViewHolder>  {
    String TAG = "TEST-tweetBreakAdapter";
    private static final boolean debug = false;

    private Context mContext;
    private DisplayMetrics mMetrics;
    private ArrayList<WordEntry> mWords;
    private ArrayList<String> mActiveFavoriteStars;
    private RxBus mRxBus;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtColorBar;
        public TextView txtKanji;
        public TextView txtFurigana;
        public TextView lstDefinitions;
        public ImageButton imgStar;
        public FrameLayout imgStarLayout;
        public LinearLayout mainLayout;

        public ViewHolder(View v) {
            super(v);
            txtColorBar = (TextView) v.findViewById(R.id.colorbar);
            txtKanji = (TextView) v.findViewById(R.id.textViewBrowseAdapter_Kanji);
            txtFurigana  = (TextView) v.findViewById(R.id.textViewBrowseAdapter_Furigana);
            lstDefinitions = (TextView) v.findViewById(R.id.textViewlstDefinitions);
            imgStar = (ImageButton) v.findViewById(R.id.favorite);
            mainLayout = (LinearLayout) v.findViewById(R.id.browseitems_layout);
            imgStarLayout = (FrameLayout) v.findViewById(R.id.browseitems_frameLayout);
        }
    }

    public TweetBreakDownAdapter(Context context, DisplayMetrics metrics
            , ArrayList<WordEntry> words
            , ArrayList<String> activeFavoriteStars
            , RxBus rxbus) {
        mContext = context;
        mMetrics = metrics;
        mWords = words;
        mRxBus = rxbus;
        this.mActiveFavoriteStars = activeFavoriteStars;
    }


    @Override
    public TweetBreakDownAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tweetbreakdown_recycler_row, parent, false);

        return new ViewHolder(v);
    }

    public WordEntry getItem(int position) {
        return mWords.get(position);
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

        Log.d(TAG,"favs kanji: " + mWords.get(holder.getAdapterPosition()).getKanji());
        Log.d(TAG,"favs: " + (mWords.get(holder.getAdapterPosition()).getItemFavorites() == null));

        Integer starColorDrawableInt = FavoritesColors.assignStarResource(mWords.get(holder.getAdapterPosition()).getItemFavorites(),mActiveFavoriteStars);
        holder.imgStar.setImageResource(starColorDrawableInt);
        if(starColorDrawableInt!=R.drawable.ic_star_multicolor) {
            holder.imgStar.setColorFilter(ContextCompat.getColor(mContext,FavoritesColors.assignStarColor(mWords.get(holder.getAdapterPosition()).getItemFavorites(),mActiveFavoriteStars)));
        }

        try {
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, mWords.get(holder.getAdapterPosition()).getColorValue()));
        } catch (Exception e) {
            Log.e(TAG,"Tweetbreakdown adding colorbar exception");
        }


        holder.mainLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mRxBus.sendLongClick(mWords.get(holder.getAdapterPosition()));
                return false;
            }
        });
        holder.lstDefinitions.setText(mWords.get(holder.getAdapterPosition()).getDefinitionMultiLineString(10));
        holder.lstDefinitions.setTypeface(null, Typeface.ITALIC);
        holder.lstDefinitions.setTag(mWords.get(holder.getAdapterPosition()).getId());
        holder.lstDefinitions.setFocusable(false);
        holder.lstDefinitions.setClickable(false);

        holder.imgStarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO favorite words

                Log.d(TAG,"mActiveFavoriteStars: " + mActiveFavoriteStars);
                Log.d(TAG,"should open: " + mWords.get(holder.getAdapterPosition()).getItemFavorites().shouldOpenFavoritePopup(mActiveFavoriteStars));

                if(mWords.get(holder.getAdapterPosition()).getItemFavorites().shouldOpenFavoritePopup(mActiveFavoriteStars)) {
                    showFavoriteListPopupWindow(holder);
                } else {
                    if(FavoritesColors.onFavoriteStarToggle(mContext,mActiveFavoriteStars,mWords.get(holder.getAdapterPosition()))) {
                        holder.imgStar.setImageResource(R.drawable.ic_star_black);
                        holder.imgStar.setColorFilter(ContextCompat.getColor(mContext,FavoritesColors.assignStarColor(mWords.get(holder.getAdapterPosition()).getItemFavorites(),mActiveFavoriteStars)));
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

                showFavoriteListPopupWindow(holder);

                return true;
            }
        });

        holder.mainLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mRxBus.send(mWords.get(holder.getAdapterPosition()));
                return false;
            }
        });

    }

    public void showFavoriteListPopupWindow(final ViewHolder holder) {
        RxBus rxBus = new RxBus();

        ArrayList<MyListEntry> availableFavoriteLists = InternalDB.getWordInterfaceInstance(mContext).getWordListsForAWord(mActiveFavoriteStars,String.valueOf(mWords.get(holder.getAdapterPosition()).getId()),null);

        PopupWindow popupWindow =  ChooseFavoriteListsPopupWindow.createWordFavoritesPopup(mContext,mMetrics,rxBus,availableFavoriteLists,mWords.get(holder.getAdapterPosition()).getId());

        popupWindow.getContentView().measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        int xadjust = popupWindow.getContentView().getMeasuredWidth() + (int) (25 * mMetrics.density + 0.5f);
        int yadjust = (int)((popupWindow.getContentView().getMeasuredHeight()  + holder.imgStar.getMeasuredHeight())/2.0f);

        Log.d("TEST","pop width: " + popupWindow.getContentView().getMeasuredWidth() + " height: " + popupWindow.getContentView().getMeasuredHeight());
        Log.d("TEST","xadjust: " + xadjust + ", yadjust: " + yadjust);


        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mRxBus.send(mWords.get(holder.getAdapterPosition()));
            }
        });


        rxBus.toClickObserverable().subscribe(new Action1<Object>() {
            @Override
            public void call(Object event) {

                                    /* Recieve a MyListEntry (containing an updated list entry for this row kanji) from
                                    * the ChooseFavoritesAdapter in the ChooseFavorites popup window */
                if(event instanceof MyListEntry) {
                    MyListEntry myListEntry = (MyListEntry) event;

                                        /* Ascertain the type of list that the kanji was added to (or subtracted from),
                                        and update that list's count */
                    if(myListEntry.getListsSys() == 1) {
                        switch (myListEntry.getListName()) {
                            case "Blue":
                                mWords.get(holder.getAdapterPosition()).getItemFavorites().setSystemBlueCount(myListEntry.getSelectionLevel());
                                break;
                            case "Green":
                                mWords.get(holder.getAdapterPosition()).getItemFavorites().setSystemGreenCount(myListEntry.getSelectionLevel());
                                break;
                            case "Red":
                                mWords.get(holder.getAdapterPosition()).getItemFavorites().setSystemRedCount(myListEntry.getSelectionLevel());
                                break;
                            case "Yellow":
                                mWords.get(holder.getAdapterPosition()).getItemFavorites().setSystemYellowCount(myListEntry.getSelectionLevel());
                                break;
                            case "Purple":
                                mWords.get(holder.getAdapterPosition()).getItemFavorites().setSystemPurpleCount(myListEntry.getSelectionLevel());
                                break;
                            case "Orange":
                                mWords.get(holder.getAdapterPosition()).getItemFavorites().setSystemOrangeCount(myListEntry.getSelectionLevel());
                                break;
                            default:
                                break;
                        }
                    } else {
                        if(myListEntry.getSelectionLevel() == 1) {
                            mWords.get(holder.getAdapterPosition()).getItemFavorites().addToUserListCount(1);
                        } else {
                            mWords.get(holder.getAdapterPosition()).getItemFavorites().subtractFromUserListCount(1);
                        }
                    }

                    if(mWords.get(holder.getAdapterPosition()).getItemFavorites().shouldOpenFavoritePopup(mActiveFavoriteStars)
                            && mWords.get(holder.getAdapterPosition()).getItemFavorites().systemListCount(mActiveFavoriteStars) >1) {
                        holder.imgStar.setColorFilter(null);
                        holder.imgStar.setImageResource(R.drawable.ic_star_multicolor);

                    } else {
                        holder.imgStar.setImageResource(R.drawable.ic_star_black);
                        holder.imgStar.setColorFilter(ContextCompat.getColor(mContext,FavoritesColors.assignStarColor(mWords.get(holder.getAdapterPosition()).getItemFavorites(),mActiveFavoriteStars)));
                    }
                }
            }

        });


        popupWindow.showAsDropDown(holder.imgStar,-xadjust,-yadjust);

};


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mWords.size();
    }

}

