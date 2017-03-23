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
import android.widget.Toast;

import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.Models.WordEntryFavorites;
import com.jukuproject.jukutweet.R;
import java.util.ArrayList;


public class TweetBreakDownAdapter extends RecyclerView.Adapter<TweetBreakDownAdapter.ViewHolder>  {
    String TAG = "TweetBreakDownAdapter";
    private static final boolean debug = false;

    private Context mContext;
    private ArrayList<WordEntry> mWords;
    private  ColorThresholds mColorThresholds;
    private ArrayList<String> mActiveFavoriteStars;
    private RxBus mRxBus;


    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtColorBar;
        public TextView txtKanji;
        public TextView txtFurigana;
        public TextView lstDefinitions;
        public ImageButton imgStar;
        public FrameLayout imgStarLayout;
        public TextView txtimgStarNumber;
        public LinearLayout layout;

        public ViewHolder(View v) {
            super(v);
            txtColorBar = (TextView) v.findViewById(R.id.colorbar);
            txtKanji = (TextView) v.findViewById(R.id.textViewBrowseAdapter_Kanji);
            txtFurigana  = (TextView) v.findViewById(R.id.textViewBrowseAdapter_Furigana);
            lstDefinitions = (TextView) v.findViewById(R.id.textViewlstDefinitions);
            imgStar = (ImageButton) v.findViewById(R.id.favorite);
            txtimgStarNumber = (TextView) v.findViewById(R.id.favoritenumber);
            layout = (LinearLayout) v.findViewById(R.id.browseitems_layout);
            imgStarLayout = (FrameLayout) v.findViewById(R.id.browseitems_frameLayout);
        }
    }


    public TweetBreakDownAdapter(Context context, ArrayList<WordEntry> words, ColorThresholds colorThresholds, ArrayList<String> activeFavoriteStars, RxBus rxBus) {
        mContext = context;
        mWords = words;
        mColorThresholds = colorThresholds;
        this.mActiveFavoriteStars = activeFavoriteStars;
        this.mRxBus = rxBus;
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

        if(debug){
            Log.d(TAG, "position: " + holder.getAdapterPosition());}

        final WordEntry wordEntry = mWords.get(holder.getAdapterPosition());

            holder.txtimgStarNumber.setVisibility(View.GONE);
            holder.layout.setSelected(false);
            holder.imgStar.setVisibility(View.VISIBLE);
            holder.imgStarLayout.setClickable(true);
            holder.imgStarLayout.setLongClickable(true);

        holder.txtKanji.setText(wordEntry.getKanji());
        holder.txtFurigana.setText(wordEntry.getFurigana());

        /* Assign a color to the image star */
        if(wordEntry.getWordEntryFavorites().shouldOpenFavoritePopup()) {
            holder.imgStar.setColorFilter(null);
            holder.imgStar.setImageResource(R.drawable.ic_star_multicolor);
        } else {
            holder.imgStar.setImageResource(R.drawable.ic_star_black);
            holder.imgStar.setColorFilter(ContextCompat.getColor(mContext, getFavoritesStarColor(mActiveFavoriteStars,wordEntry.getWordEntryFavorites())));
        }



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


        holder.imgStarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO favorite words
//                Toast.makeText(mContext, "Clicked", Toast.LENGTH_SHORT).show();
//                mRxBus.send(mWords.get(holder.getAdapterPosition()));

                if(wordEntry.getWordEntryFavorites().shouldOpenFavoritePopup()) {
                //TODO make the big popup show
                } else {
                    if(onFavoriteStarToggle(mActiveFavoriteStars,wordEntry)) {
                        holder.imgStar.setImageResource(R.drawable.ic_star_black);
                        holder.imgStar.setColorFilter(ContextCompat.getColor(mContext, getFavoritesStarColor(mActiveFavoriteStars,wordEntry.getWordEntryFavorites())));
                    } else {
                        //TODO insert an error?
                    }

                }
            }
        });


        holder.imgStarLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mRxBus.sendLongClick(mWords.get(holder.getAdapterPosition()).getId());
                return true;
            }
        });

        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //TODO multiselect words
                return true;
            }
        });



//        holder.layout.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                /**  The external (BrowseBlocks) onclick listener happens first, apparently. So the hashmap operations should already be done. Just update the visuals*/
//            }
//        });



    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mWords.size();
    }





//        public void starShortPress() {
//
//        if (holder.imgStarLayout.isPressed()) {
//
//            //retrieve the starting state of the star (which determines star color and clickability)
//            if (mcolors == null || mcolors.size() == 0) {
//                startingstate_inner = 5;
//            } else if (holder.imgStar.getTag().toString() != null) {
//                startingstate_inner = Integer.parseInt(holder.imgStar.getTag().toString());
//            } else {
//                startingstate_inner = 0;
//            }
//
//            if (debug) {
//                Log.d(TAG, "startingstate_inner= " + startingstate_inner);
//            }
//            ;
//
//            /** IF IT'S IN MULTIPLE LISTS, MAKE A CLICK OPEN UP THE MULTIPLE LIST DIALOG...*/
//            if (startingstate_inner == 5) {
//                PopupWindow popupWindowMultiFavorites = popupWindowMultiFavorites(PKey);
//                popupWindowMultiFavorites.setOnDismissListener(new PopupWindow.OnDismissListener() {
//                    @Override
//                    public void onDismiss() {
//                        updateStarColor(holder, PKey);
//                    }
//                });
//
//
//                popupWindowMultiFavorites.showAsDropDown(holder.imgStar, -xadjustment, -yadjustment);
//
//
//            } else if (startingstate_inner <= 0 && mcolors.contains("Yellow")) {
//                holder.imgStar.setImageResource(R.drawable.ic_star_black);
//                holder.imgStar.setColorFilter(ContextCompat.getColor(mActivity, R.color.spinnerYellowColor));
//                ContentValues values = new ContentValues();
//                values.clear();
//                values.put(InternalDB.Columns.COL0, PKey);
//                values.put(InternalDB.Columns.COL_T2_1, "Yellow");
//                values.put(InternalDB.Columns.COL_T2_2, 1);
//                db.insertWithOnConflict(InternalDB.TABLE2, null, values,
//                        SQLiteDatabase.CONFLICT_REPLACE);
//                startingstate_inner = 1;
//
//                /** Updating the favorites list double (i.e. mColorsHash) so view can be recreated when the user scrolls and view is recycled */
//                ArrayList<ArrayList<String>> tmpdouble = new ArrayList<ArrayList<String>>();
//                if (mColorsHash.containsKey(PKey)) {
//                    ArrayList<String> colorsarray = mColorsHash.get(PKey).get(0);
//                    colorsarray.clear();
//                    colorsarray.add("Yellow");
//                    ArrayList<String> otherarray = mColorsHash.get(PKey).get(1);
//                    tmpdouble.add(colorsarray);
//                    tmpdouble.add(otherarray);
//
//                    mColorsHash.remove(PKey);
//                } else {
//                    ArrayList<String> colorsarray = new ArrayList<String>();
//                    ArrayList<String> otherarray = new ArrayList<String>();
//                    colorsarray.add("Yellow");
//                    tmpdouble.add(colorsarray);
//                    tmpdouble.add(otherarray);
//                }
//                mColorsHash.put(PKey, tmpdouble);
//
//            } else if (startingstate_inner <= 1 && mcolors.contains("Blue")) {
//                holder.imgStar.setImageResource(R.drawable.ic_star_black);
//                holder.imgStar.setColorFilter(ContextCompat.getColor(mActivity, R.color.Blue_900));
//                ContentValues values = new ContentValues();
//                values.clear();
//                values.put(InternalDB.Columns.COL0, PKey);
//                values.put(InternalDB.Columns.COL_T2_1, "Blue");
//                values.put(InternalDB.Columns.COL_T2_2, 1);
//                db.insertWithOnConflict(InternalDB.TABLE2, null, values,
//                        SQLiteDatabase.CONFLICT_REPLACE);
//                switch (startingstate_inner) {
//                    case 1:
//                        helper.delete_favorites(db, InternalDB.TABLE2, PKey, "Yellow", 1);
//                        break;
//                }
//                startingstate_inner = 2;
//
//                /** Updating the favorites list double (i.e. mColorsHash) so view can be recreated when the user scrolls and view is recycled */
//                ArrayList<ArrayList<String>> tmpdouble = new ArrayList<ArrayList<String>>();
//                if (mColorsHash.containsKey(PKey)) {
//                    ArrayList<String> colorsarray = mColorsHash.get(PKey).get(0);
//                    colorsarray.clear();
//                    colorsarray.add("Blue");
//                    ArrayList<String> otherarray = mColorsHash.get(PKey).get(1);
//                    tmpdouble.add(colorsarray);
//                    tmpdouble.add(otherarray);
//
//                    mColorsHash.remove(PKey);
//                } else {
//                    ArrayList<String> colorsarray = new ArrayList<String>();
//                    ArrayList<String> otherarray = new ArrayList<String>();
//                    colorsarray.add("Blue");
//                    tmpdouble.add(colorsarray);
//                    tmpdouble.add(otherarray);
//                }
//                mColorsHash.put(PKey, tmpdouble);
//
//            } else if (startingstate_inner <= 2 && mcolors.contains("Red")) {
//                holder.imgStar.setImageResource(R.drawable.ic_star_black);
//                holder.imgStar.setColorFilter(ContextCompat.getColor(mActivity, R.color.answerIncorrectColor));
//                ContentValues values = new ContentValues();
//                values.clear();
//                values.put(InternalDB.Columns.COL0, PKey);
//                values.put(InternalDB.Columns.COL_T2_1, "Red");
//                values.put(InternalDB.Columns.COL_T2_2, 1);
//                db.insertWithOnConflict(InternalDB.TABLE2, null, values,
//                        SQLiteDatabase.CONFLICT_REPLACE);
//
//                switch (startingstate_inner) {
//                    case 1:
//                        helper.delete_favorites(db, InternalDB.TABLE2, PKey, "Yellow", 1);
//                        break;
//                    case 2:
//                        helper.delete_favorites(db, InternalDB.TABLE2, PKey, "Blue", 1);
//                        break;
//                }
//                startingstate_inner = 3;
//
//                /** Updating the favorites list double (i.e. mColorsHash) so view can be recreated when the user scrolls and view is recycled */
//                ArrayList<ArrayList<String>> tmpdouble = new ArrayList<ArrayList<String>>();
//                if (mColorsHash.containsKey(PKey)) {
//                    ArrayList<String> colorsarray = mColorsHash.get(PKey).get(0);
//                    colorsarray.clear();
//                    colorsarray.add("Red");
//                    ArrayList<String> otherarray = mColorsHash.get(PKey).get(1);
//                    tmpdouble.add(colorsarray);
//                    tmpdouble.add(otherarray);
//
//                    mColorsHash.remove(PKey);
//                } else {
//                    ArrayList<String> colorsarray = new ArrayList<String>();
//                    ArrayList<String> otherarray = new ArrayList<String>();
//                    colorsarray.add("red");
//                    tmpdouble.add(colorsarray);
//                    tmpdouble.add(otherarray);
//
//                }
//                mColorsHash.put(PKey, tmpdouble);
//
//            } else if (startingstate_inner <= 3 && mcolors.contains("Green")) {
//                holder.imgStar.setImageResource(R.drawable.ic_star_black);
//                holder.imgStar.setColorFilter(ContextCompat.getColor(mActivity, R.color.answerCorrectColor));
//                ContentValues values = new ContentValues();
//                values.clear();
//                values.put(InternalDB.Columns.COL0, PKey);
//                values.put(InternalDB.Columns.COL_T2_1, "Green");
//                values.put(InternalDB.Columns.COL_T2_2, 1);
//                db.insertWithOnConflict(InternalDB.TABLE2, null, values,
//                        SQLiteDatabase.CONFLICT_REPLACE);
//
//                switch (startingstate_inner) {
//                    case 1:
//                        helper.delete_favorites(db, InternalDB.TABLE2, PKey, "Yellow", 1);
//                        break;
//                    case 2:
//                        helper.delete_favorites(db, InternalDB.TABLE2, PKey, "Blue", 1);
//                        break;
//                    case 3:
//                        helper.delete_favorites(db, InternalDB.TABLE2, PKey, "Red", 1);
//                        break;
//                }
//                startingstate_inner = 4;
//
//                /** Updating the favorites list double (i.e. mColorsHash) so view can be recreated when the user scrolls and view is recycled */
//                ArrayList<ArrayList<String>> tmpdouble = new ArrayList<ArrayList<String>>();
//                if (mColorsHash.containsKey(PKey)) {
//                    ArrayList<String> colorsarray = mColorsHash.get(PKey).get(0);
//                    colorsarray.clear();
//                    colorsarray.add("Green");
//                    ArrayList<String> otherarray = mColorsHash.get(PKey).get(1);
//                    tmpdouble.add(colorsarray);
//                    tmpdouble.add(otherarray);
//
//                    mColorsHash.remove(PKey);
//                } else {
//                    ArrayList<String> colorsarray = new ArrayList<String>();
//                    ArrayList<String> otherarray = new ArrayList<String>();
//                    colorsarray.add("Green");
//                    tmpdouble.add(colorsarray);
//                    tmpdouble.add(otherarray);
//
//
//                }
//                mColorsHash.put(PKey, tmpdouble);
//
//            } else {
//                holder.imgStar.setImageResource(R.drawable.ic_star_black);
//                holder.imgStar.setColorFilter(ContextCompat.getColor(mActivity, R.color.Black));
//
//                switch (startingstate_inner) {
//                    case 1:
//                        helper.delete_favorites(db, InternalDB.TABLE2, PKey, "Yellow", 1);
//                        break;
//                    case 2:
//                        helper.delete_favorites(db, InternalDB.TABLE2, PKey, "Blue", 1);
//                        break;
//                    case 3:
//                        helper.delete_favorites(db, InternalDB.TABLE2, PKey, "Red", 1);
//                        break;
//
//                    case 4:
//                        helper.delete_favorites(db, InternalDB.TABLE2, PKey, "Green", 1);
//                        break;
//                }
//                startingstate_inner = 0;
//
//                /** Updating the favorites list double (i.e. mColorsHash) so view can be recreated when the user scrolls and view is recycled */
//                ArrayList<ArrayList<String>> tmpdouble = new ArrayList<ArrayList<String>>();
//                if (mColorsHash.containsKey(PKey)) {
//                    ArrayList<String> colorsarray = mColorsHash.get(PKey).get(0);
//                    colorsarray.clear();
//                    ArrayList<String> otherarray = mColorsHash.get(PKey).get(1);
//                    tmpdouble.add(colorsarray);
//                    tmpdouble.add(otherarray);
//
//                    mColorsHash.remove(PKey);
//                } else {
//                    ArrayList<String> colorsarray = new ArrayList<String>();
//                    ArrayList<String> otherarray = new ArrayList<String>();
//                    tmpdouble.add(colorsarray);
//                    tmpdouble.add(otherarray);
//                }
//                mColorsHash.put(PKey, tmpdouble);
//            }
//
//        } else {
//            holder.imgStar.setImageResource(R.drawable.ic_star_black);
//            holder.imgStar.setColorFilter(ContextCompat.getColor(mActivity, R.color.Black));
//            startingstate_inner = 0;
//
//            /** Updating the favorites list double (i.e. mColorsHash) so view can be recreated when the user scrolls and view is recycled */
//            ArrayList<ArrayList<String>> tmpdouble = new ArrayList<ArrayList<String>>();
//            if (mColorsHash.containsKey(PKey)) {
//                ArrayList<String> colorsarray = mColorsHash.get(PKey).get(0);
//                colorsarray.clear();
//                ArrayList<String> otherarray = mColorsHash.get(PKey).get(1);
//                tmpdouble.add(colorsarray);
//                tmpdouble.add(otherarray);
//
//                mColorsHash.remove(PKey);
//            } else {
//                ArrayList<String> colorsarray = new ArrayList<String>();
//                ArrayList<String> otherarray = new ArrayList<String>();
//                tmpdouble.add(colorsarray);
//                tmpdouble.add(otherarray);
//            }
//            mColorsHash.put(PKey, tmpdouble);
//        }
//        holder.imgStar.setTag(startingstate_inner);
//
//        if (debug) {
//            Log.d(TAG, "After click startingstate_inner: (" + PKey + ") - " + startingstate_inner);
//        }
//
//        db.close();
//    }



    //TODO fix this description
    /**
     *
     * Note: This should be used AFTER the shouldOpenFavoritePopup method, when we know the star
     * isn't going to be a "multicolor" star, and instead will only have one color
     * @param preferenceFavorites
     * @return the color (int) that the current star imagebutton should be tinted
     */
    public int getFavoritesStarColor(ArrayList<String> preferenceFavorites, WordEntryFavorites wordEntryFavorites){
//        if(wordEntryFavorites.getUserListCount() > 0 || wordEntryFavorites.getSystemBlueCount()
//                + wordEntryFavorites.getSystemRedCount()
//                + wordEntryFavorites.getSystemGreenCount()
//                + wordEntryFavorites.getSystemYellowCount() > 1 ) {
//            return 5; // A "multifavorite"
//        } else
        if(preferenceFavorites.contains("Blue") && wordEntryFavorites.getSystemBlueCount() > 0) {
            return R.color.colorJukuBlue;
        } else if(preferenceFavorites.contains("Green") && wordEntryFavorites.getSystemGreenCount() > 0) {
            return R.color.colorJukuGreen;
        } else if(preferenceFavorites.contains("Red") && wordEntryFavorites.getSystemRedCount() > 0) {
            return R.color.colorJukuRed;
        } else if(preferenceFavorites.contains("Yellow") && wordEntryFavorites.getSystemYellowCount() > 0) {
            return R.color.colorJukuYellow;
        } else {
            return android.R.color.black;
        }
    }
//
//    public int getNextFavoritesPosition(ArrayList<String> preferenceFavorites, WordEntryFavorites wordEntryFavorites, int CurrentPosition){
//
//        if(preferenceFavorites.contains("Blue") && wordEntryFavorites.getSystemBlueCount() > 0) {
//            return R.color.colorJukuBlue;
//        } else if(preferenceFavorites.contains("Green") && wordEntryFavorites.getSystemGreenCount() > 0) {
//            return R.color.colorJukuGreen;
//        } else if(preferenceFavorites.contains("Red") && wordEntryFavorites.getSystemRedCount() > 0) {
//            return R.color.colorJukuRed;
//        } else if(preferenceFavorites.contains("Yellow") && wordEntryFavorites.getSystemYellowCount() > 0) {
//            return R.color.colorJukuYellow;
//        } else {
//            return android.R.color.black;
//        }
//    }

    public boolean onFavoriteStarToggle(ArrayList<String> preferenceFavorites, WordEntry wordEntry){

        try {
            WordEntryFavorites wordEntryFavorites = wordEntry.getWordEntryFavorites();

            if(wordEntryFavorites.isEmpty()) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Blue","Green","Red","Yellow"});
                if(InternalDB.getInstance(mContext).changeFavoriteListEntry(wordEntry.getId(),"Black",nextColor)) {
                    wordEntryFavorites.setSystemColor(nextColor);
                }

            } else if(preferenceFavorites.contains("Blue") && wordEntryFavorites.getSystemBlueCount() > 0) {
                Log.d(TAG,"In blue!");
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Green","Red","Yellow"});
                Log.d(TAG,"In blue, NEXTCOLOR:  " + nextColor);
                if(InternalDB.getInstance(mContext).changeFavoriteListEntry(wordEntry.getId(),"Blue",nextColor)) {
                    wordEntryFavorites.setSystemBlueCount(0);
                    wordEntryFavorites.setSystemColor(nextColor);
                }

            } else if(preferenceFavorites.contains("Green") && wordEntryFavorites.getSystemGreenCount() > 0) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Red","Yellow"});
                if(InternalDB.getInstance(mContext).changeFavoriteListEntry(wordEntry.getId(),"Green",nextColor)) {
                    wordEntryFavorites.setSystemGreenCount(0);
                    wordEntryFavorites.setSystemColor(nextColor);
                }

            } else if(preferenceFavorites.contains("Red") && wordEntryFavorites.getSystemRedCount() > 0) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Yellow"});
                if(InternalDB.getInstance(mContext).changeFavoriteListEntry(wordEntry.getId(),"Red",nextColor)) {
                    wordEntryFavorites.setSystemRedCount(0);
                    wordEntryFavorites.setSystemColor(nextColor);
                }

            } else if(preferenceFavorites.contains("Yellow") && wordEntryFavorites.getSystemYellowCount() > 0) {

                if(InternalDB.getInstance(mContext).changeFavoriteListEntry(wordEntry.getId(),"Yellow","Black")) {
                    wordEntryFavorites.setSystemYellowCount(0);
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG,"onFavoriteStarToggle error: " + e);
            return false;
        }

    }

    public String findNextFavoritesColor(ArrayList<String> preferenceFavorites,String[] options) {
        for (String option: options) {
            if(preferenceFavorites.contains(option)) {
                return option;
            }
        }
        return "Black";
    }

}

