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
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
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


public class TweetBreakDownAdapter extends RecyclerView.Adapter<TweetBreakDownAdapter.ViewHolder>  {
    String TAG = "TweetBreakDownAdapter";
    private static final boolean debug = false;

    //Pass instructions to mainactivity if at least one row is selected, to show icons in action bar
    private FragmentInteractionListener mCallback;


    private Context mContext;
    private DisplayMetrics mMetrics;
    private ArrayList<WordEntry> mWords;
    private  ColorThresholds mColorThresholds;
    private ArrayList<String> mActiveFavoriteStars;
    private PopupWindow chooseFavoritesPopup = null;


    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtColorBar;
        public TextView txtKanji;
        public TextView txtFurigana;
        public TextView lstDefinitions;
        public ImageButton imgStar;
        public FrameLayout imgStarLayout;
        public LinearLayout layout;

        public ViewHolder(View v) {
            super(v);
            txtColorBar = (TextView) v.findViewById(R.id.colorbar);
            txtKanji = (TextView) v.findViewById(R.id.textViewBrowseAdapter_Kanji);
            txtFurigana  = (TextView) v.findViewById(R.id.textViewBrowseAdapter_Furigana);
            lstDefinitions = (TextView) v.findViewById(R.id.textViewlstDefinitions);
            imgStar = (ImageButton) v.findViewById(R.id.favorite);
            layout = (LinearLayout) v.findViewById(R.id.browseitems_layout);
            imgStarLayout = (FrameLayout) v.findViewById(R.id.browseitems_frameLayout);
        }
    }


    public TweetBreakDownAdapter(Context context, DisplayMetrics metrics, ArrayList<WordEntry> words, ColorThresholds colorThresholds, ArrayList<String> activeFavoriteStars) {
        mContext = context;
        mMetrics = metrics;
        mWords = words;
        mColorThresholds = colorThresholds;
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

        holder.layout.setSelected(false);
        holder.imgStar.setVisibility(View.VISIBLE);
        holder.imgStarLayout.setClickable(true);
        holder.imgStarLayout.setLongClickable(true);
        holder.txtKanji.setText(mWords.get(holder.getAdapterPosition()).getKanji());
        holder.txtFurigana.setText(mWords.get(holder.getAdapterPosition()).getFurigana());

        assignStarColor(mWords.get(holder.getAdapterPosition()),holder.imgStar);

        /* Parse the definition into an array of multiple lines, if there are multiple sub-definitions in the string */
        if(mWords.get(holder.getAdapterPosition()).getTotal()< mColorThresholds.getGreyThreshold()) {
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuGrey));
        } else if(mWords.get(holder.getAdapterPosition()).getPercentage()< mColorThresholds.getRedthreshold()){
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuRed));
        } else if (mWords.get(holder.getAdapterPosition()).getPercentage()< mColorThresholds.getYellowthreshold()){
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuYellow));
        } else {
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuGreen));
        }


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
                Log.d(TAG,"should open: " + mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().shouldOpenFavoritePopup(mActiveFavoriteStars));

                if(mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().shouldOpenFavoritePopup(mActiveFavoriteStars)) {
                //TODO make the big popup show
                    int xadjustment = -50;
                    int yadjustment = -300;

                    RxBus rxBus = new RxBus();
                    rxBus.toClickObserverable()
                            .subscribe(new Action1<Object>() {
                                @Override
                                public void call(Object event) {

                        /* Recieve a MyListEntry (containing an updated list entry for this row kanji) from
                        * the ChooseFavoritesAdapter in the ChooseFavorites popup window */
                                    if(event instanceof MyListEntry) {
                                        MyListEntry myListEntry = (MyListEntry) event;

                            if(BuildConfig.DEBUG) {
                                Log.d(TAG,"MylistEntry name: " + myListEntry.getListName());
                                Log.d(TAG,"MylistEntry sys: " + myListEntry.getListsSys());
                                Log.d(TAG,"MylistEntry selectionlevel: " + myListEntry.getSelectionLevel());
                            }

                            /*Ascertain the type of list that the kanji was added to (or subtracted from),
                              and update that list's count */
                            if(myListEntry.getListsSys() == 1) {
                                switch (myListEntry.getListName()) {
                                    case "Blue":
                                        mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().setSystemBlueCount(myListEntry.getSelectionLevel());
                                        break;
                                    case "Green":
                                        mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().setSystemGreenCount(myListEntry.getSelectionLevel());
                                        break;
                                    case "Red":
                                        mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().setSystemRedCount(myListEntry.getSelectionLevel());
                                        break;
                                    case "Yellow":
                                        mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().setSystemYellowCount(myListEntry.getSelectionLevel());
                                        break;
                                    default:
                                        break;
                                }
                            } else {
                                if(myListEntry.getSelectionLevel() == 1) {
                                    mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().addToUserListCount(1);
                                } else {
                                    mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().subtractFromUserListCount(1);
                                }
                            }
                            assignStarColor(mWords.get(holder.getAdapterPosition()),holder.imgStar);
                                    }
                                }

                            });
                    chooseFavoritesPopup = new PopupChooseFavoriteLists(mContext,mMetrics,rxBus,mActiveFavoriteStars,mWords.get(holder.getAdapterPosition()).getId()).onCreateView();
                    chooseFavoritesPopup.showAsDropDown(holder.imgStar);
                    chooseFavoritesPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            assignStarColor(mWords.get(holder.getAdapterPosition()),holder.imgStar);
                            chooseFavoritesPopup = null;
                        }
                    });

                } else {
                    if(onFavoriteStarToggle(mActiveFavoriteStars,mWords.get(holder.getAdapterPosition()))) {
                        holder.imgStar.setImageResource(R.drawable.ic_star_black);
                        holder.imgStar.setColorFilter(ContextCompat.getColor(mContext, getFavoritesStarColor(mActiveFavoriteStars,mWords.get(holder.getAdapterPosition()).getWordEntryFavorites())));
                    } else {
                        //TODO insert an error?
                        Log.e(TAG,"OnFavoriteStarToggle did not work...");
                    }

                }
            }
        });


        holder.imgStarLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//TODO favorite words

//                Log.d(TAG,"mActiveFavoriteStars: " + mActiveFavoriteStars);
//                Log.d(TAG,"should open: " + mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().shouldOpenFavoritePopup(mActiveFavoriteStars));


                    //TODO make the big popup show
//                    int xadjustment = -300;
//                    int yadjustment = -200;

                    RxBus rxBus = new RxBus();
                    rxBus.toClickObserverable()
                            .subscribe(new Action1<Object>() {
                                @Override
                                public void call(Object event) {

                        /* Recieve a MyListEntry (containing an updated list entry for this row kanji) from
                        * the ChooseFavoritesAdapter in the ChooseFavorites popup window */
                                    if(event instanceof MyListEntry) {
                                        MyListEntry myListEntry = (MyListEntry) event;

                            /*Ascertain the type of list that the kanji was added to (or subtracted from),
                              and update that list's count */
                                        if(myListEntry.getListsSys() == 1) {
                                            switch (myListEntry.getListName()) {
                                                case "Blue":
                                                    mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().setSystemBlueCount(myListEntry.getSelectionLevel());
                                                    break;
                                                case "Green":
                                                    mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().setSystemGreenCount(myListEntry.getSelectionLevel());
                                                    break;
                                                case "Red":
                                                    mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().setSystemRedCount(myListEntry.getSelectionLevel());
                                                    break;
                                                case "Yellow":
                                                    mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().setSystemYellowCount(myListEntry.getSelectionLevel());
                                                    break;
                                                default:
                                                    break;
                                            }
                                        } else {
                                            if(myListEntry.getSelectionLevel() == 1) {
                                                mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().addToUserListCount(1);
                                            } else {
                                                mWords.get(holder.getAdapterPosition()).getWordEntryFavorites().subtractFromUserListCount(1);
                                            }
                                        }
                                        assignStarColor(mWords.get(holder.getAdapterPosition()),holder.imgStar);

                                    }

                                }

                            });
                    if(chooseFavoritesPopup == null) {

                        ArrayList<MyListEntry> availableFavoriteLists = InternalDB.getInstance(mContext).getFavoritesListsForAKanji(mActiveFavoriteStars,mWords.get(holder.getAdapterPosition()).getId());
                        chooseFavoritesPopup= new TestPopupWindow(mContext,mMetrics,rxBus,availableFavoriteLists,mWords.get(holder.getAdapterPosition()).getId()).onCreateView();


                        int xadjust = chooseFavoritesPopup.getWidth() + (int) (25 * mMetrics.density + 0.5f);
                        int yadjust = (int) ((availableFavoriteLists.size()*35) * mMetrics.density + 0.5f)/2 + holder.imgStar.getHeight()/2 ;
                        Log.d("TEST","pop width: " + chooseFavoritesPopup.getWidth() + " height: " + chooseFavoritesPopup.getHeight());
                        Log.d("TEST","xadjust: " + xadjust + ", yadjust: " + yadjust);
                        chooseFavoritesPopup.showAsDropDown(holder.imgStar,-xadjust,-yadjust);

//                        chooseFavoritesPopup.setWindowLayoutType(ViewGroup.LayoutParams.WRAP_CONTENT);
//                        asdf
//                        chooseFavoritesPopup.showAtLocation(holder.imgStarLayout,Gravity.CENTER_VERTICAL, - (chooseFavoritesPopup.getWidth()),0);

                        chooseFavoritesPopup.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        chooseFavoritesPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                assignStarColor(mWords.get(holder.getAdapterPosition()),holder.imgStar);
                                chooseFavoritesPopup = null;

                            }
                        });

                    }
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
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mWords.size();
    }


    /**
     * Assigns a color to the favorites star based on 2 things:
     *         1. whether or not the word is contained in the system list of that color
     *            (i.e. WordEntryFavorites "getsystemcount" for the word returns a "1")
     *         2. whether or not the system list is "activated" in the user prefences (since
     *            (users can turn system lists on and off)
     * @param preferenceFavorites array of current "activated" system list names
     * @return the color (int) that the current star imagebutton should be tinted
     */
    public int getFavoritesStarColor(ArrayList<String> preferenceFavorites, WordEntryFavorites wordEntryFavorites){
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

    /**
     * Updates the wordEntry object for a row, as well as the JFavorites table in database, to reflect a
     * new saved "system list" for a word. When the user clicks on the favorites star for a word, and that
     * word is ONLY contained in one system list, the user should be able to toggle through available system
     * lists with a single click (ex: blue-->red-->yellow--> black (i.e. unassigned to a list)-->blue-->red etc)
     *
     * There is an order to system list toggling. It goes:
     *         0. Unassigned (black)
     *         1. Blue
     *         2. Green
     *         3. Red
     *         4. Yellow
     *
     * The user can cycle through this list. hatever the initial color is for a word, that is where we start in the cycle.
     * If the word is initially Red, for example, and the star is clicked, it should change to yellow. But if yellow is unavailable
     * in the preferences, the star should become black (unassigned) (and reference to red should be removed from the db).
     *
     * @param preferenceFavorites  array of current "activated" system list names
     * @param wordEntry WordEntry object containing data for a single kanji (including what lists that kanji is included in)
     * @return boolean true if operation succesful, false if not
     */
    public boolean onFavoriteStarToggle(ArrayList<String> preferenceFavorites, WordEntry wordEntry){

        try {
            WordEntryFavorites wordEntryFavorites = wordEntry.getWordEntryFavorites();

            if(wordEntryFavorites.isEmpty(mActiveFavoriteStars)) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Blue","Green","Red","Yellow"});
                if(InternalDB.getInstance(mContext).changeFavoriteListEntry(wordEntry.getId(),"Black",nextColor)) {
                    wordEntryFavorites.setSystemColor(nextColor);
                }

            } else if(preferenceFavorites.contains("Blue") && wordEntryFavorites.getSystemBlueCount() > 0) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Green","Red","Yellow"});
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

    /**
     * Cycles through an array of possible color entries, looking for a match in the preferenceFavorites.
     * If one is found, that color is the "next" favorites color during a {@link #onFavoriteStarToggle(ArrayList, WordEntry)}
     * If nothing is found, the star reverts to unassigned (colored black)
     * @param preferenceFavorites array of current "activated" system list names
     * @param options array of possible next color entries that a favorites star could toggle to
     * @return the name of the next available color entry
     */
    public String findNextFavoritesColor(ArrayList<String> preferenceFavorites,String[] options) {
        for (String option: options) {
            if(preferenceFavorites.contains(option)) {
                return option;
            }
        }
        return "Black";
    }

    public void assignStarColor(WordEntry wordEntry, ImageButton imgStar) {
        if(wordEntry.getWordEntryFavorites().shouldOpenFavoritePopup(mActiveFavoriteStars) &&
                wordEntry.getWordEntryFavorites().systemListCount(mActiveFavoriteStars) >1) {
            imgStar.setColorFilter(null);
            imgStar.setImageResource(R.drawable.ic_star_multicolor);
        } else {
            imgStar.setImageResource(R.drawable.ic_star_black);
            imgStar.setColorFilter(ContextCompat.getColor(mContext, getFavoritesStarColor(mActiveFavoriteStars,wordEntry.getWordEntryFavorites())));
        }
    }
}

