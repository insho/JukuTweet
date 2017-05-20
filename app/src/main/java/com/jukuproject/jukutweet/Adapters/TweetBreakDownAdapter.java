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

import com.jukuproject.jukutweet.BuildConfig;
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
            mainLayout = (LinearLayout) v.findViewById(R.id.browseitems_layout2);
            imgStarLayout = (FrameLayout) v.findViewById(R.id.browseitems_frameLayout);
        }
    }

    public TweetBreakDownAdapter(Context context, DisplayMetrics metrics
            , ArrayList<WordEntry> words
            , ArrayList<String> activeFavoriteStars
            , RxBus rxbus) {
        this.mContext = context;
        this.mMetrics = metrics;
        this.mWords = words;
        this.mRxBus = rxbus;
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
                    showFavoriteListPopupWindow(holder);
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

                showFavoriteListPopupWindow(holder);

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

    /**
     * Displays the {@link ChooseFavoriteListsPopupWindow} when the "favorites star" is clicked for a
     * word in one of the TweetBreakDown recycler rows
     * @param holder ViewHolder for the row
     */
    public void showFavoriteListPopupWindow(final ViewHolder holder) {
        RxBus rxBus = new RxBus();

        ArrayList<MyListEntry> availableFavoriteLists = InternalDB.getWordInterfaceInstance(mContext).getWordListsForAWord(mActiveFavoriteStars,String.valueOf(mWords.get(holder.getAdapterPosition()).getId()),1,null);
        PopupWindow popupWindow =  ChooseFavoriteListsPopupWindow.createWordFavoritesPopup(mContext,mMetrics,rxBus,availableFavoriteLists,mWords.get(holder.getAdapterPosition()).getId());
        popupWindow.getContentView().measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        int xadjust = popupWindow.getContentView().getMeasuredWidth() + (int) (25 * mMetrics.density + 0.5f);
        int yadjust;
//        if(availableFavoriteLists.size()<4) {
            yadjust = (int)((popupWindow.getContentView().getMeasuredHeight()  + holder.imgStar.getMeasuredHeight())/2.0f);
//        } else {
//            yadjust = getYAdjustmentForPopupWindowBigList(availableFavoriteLists.size(),holder.getAdapterPosition(),mMetrics.scaledDensity,holder.itemView.getMeasuredHeight());
//        }

        if(BuildConfig.DEBUG) {
            Log.d("TEST", "pop width: " + popupWindow.getContentView().getMeasuredWidth() + " height: " + popupWindow.getContentView().getMeasuredHeight());
            Log.d("TEST", "xadjust: " + xadjust + ", yadjust: " + yadjust);
        }

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
                        try {
                            holder.imgStar.setColorFilter(ContextCompat.getColor(mContext,FavoritesColors.assignStarColor(mWords.get(holder.getAdapterPosition()).getItemFavorites(),mActiveFavoriteStars)));
                        } catch (NullPointerException e) {
                            Log.e(TAG,"tweetBreakDownAdapter Nullpointer error setting star color filter in word detail popup dialog... Need to assign item favorites to WordEntry(?)" + e.getCause());
                        }
                    }
                }
            }

        });


        popupWindow.showAsDropDown(holder.imgStar,-xadjust,-yadjust);

}

    /**
     * Used to adjust the positioning of the {@link ChooseFavoriteListsPopupWindow} when {@link TweetBreakDownAdapter#showFavoriteListPopupWindow(ViewHolder)} appears
     * and there are a large number of available lists to display. We don't want the popup window to overflow the bounds of the screen.
     * @param totalActiveLists number of available lists that a word can be saved to (i.e. number of rows to be displayed in the ChooseFavorites popup)
     * @param adapterPosition position of row in adapter. If the row is at the bottom of the screen, the list must be adjusted upwards, and visa versa
     * @param scale metrics scale in pixels
     * @param heightOfView measured height of the view that was clicked (to better center the list. The views can be of variable height)
     * @return Int specifying how far the list must be adjusted from its default position as a dropdown below the favorites star
     */
    public int getYAdjustmentForPopupWindowBigList(int totalActiveLists
            , int adapterPosition
            ,float scale
            ,float heightOfView) {

        final int yadjustment;



        if(totalActiveLists>10) {
            totalActiveLists = 10;
        }

        int estimatedheightofpopup =  (int) ((totalActiveLists*35) * scale + 0.5f);
        int multiplier = -(mWords.size()-adapterPosition-1);

        if((adapterPosition>(mWords.size()-5) && adapterPosition>((float)mWords.size()/2.0f))){

            if(totalActiveLists < 5){
                multiplier = 1;
            }

            yadjustment = estimatedheightofpopup + (int) (multiplier* ((int) ((35) * scale + 0.5f)));

            if(BuildConfig.DEBUG){
                Log.d(TAG,"rowsize: " + totalActiveLists);
                Log.d(TAG,"estimatedheightofpopup: " + estimatedheightofpopup);
                Log.d(TAG,"multiplier: " + multiplier);
            }

        } else {

            float defmult = heightOfView*.8f;
            float listizemultiplier;
            switch (totalActiveLists) {
                case 0:
                    listizemultiplier = 35.0f+defmult;
                    break;
                case 1:
                    listizemultiplier = 35.0f+defmult;
                    break;
                case 2:
                    listizemultiplier = 35.0f+defmult;
                    break;
                case 3:
                    listizemultiplier = 40.0f+defmult;
                    break;
                case 4:
                    listizemultiplier = 45.0f+defmult;
                    break;

                default:
                    listizemultiplier = 95.0f+defmult;
                    break;
            }

            yadjustment =(int) ((float)listizemultiplier * scale + 0.5f);
            if(BuildConfig.DEBUG){
                Log.d(TAG,"listsizemultiplier: " +  listizemultiplier);
            }
        }

        return yadjustment;
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mWords.size();
    }

}

