package com.jukuproject.jukutweet;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.WordEntry;

import java.util.ArrayList;

/**
 * Created by JClassic on 3/28/2017.
 */

public class FavoritesColors {


//
//    private static FavoritesColors mInstance = null;
//
//    private String mString;
//
//    private FavoritesColors(){
//        mString = "Hello";
//    }
//
//    public static FavoritesColors getInstance(){
//        if(mInstance == null)
//        {
//            mInstance = new FavoritesColors();
//        }
//        return mInstance;
//    }
//
//    public String getString(){
//        return this.mString;
//    }
//
//    public void setString(String value){
//        mString = value;
//    }


   /**
     * Assigns a color to the favorites star based on 2 things:
     *         1. whether or not the word is contained in the system list of that color
     *            (i.e. ItemFavorites "getsystemcount" for the word returns a "1")
     *         2. whether or not the system list is "activated" in the user prefences (since
     *            (users can turn system lists on and off)
     * @param preferenceFavorites array of current "activated" system list names
     * @return the color (int) that the current star imagebutton should be tinted
     */
    public static int getFavoritesStarColor(ArrayList<String> preferenceFavorites, ItemFavorites itemFavorites){
        if(preferenceFavorites.contains("Blue") && itemFavorites.getSystemBlueCount() > 0) {
            Log.d("TEST","RETURNING BLUE");
            return R.color.colorJukuBlue;
        } else if(preferenceFavorites.contains("Green") && itemFavorites.getSystemGreenCount() > 0) {
            return R.color.colorJukuGreen;
        } else if(preferenceFavorites.contains("Red") && itemFavorites.getSystemRedCount() > 0) {
            return R.color.colorJukuRed;
        } else if(preferenceFavorites.contains("Yellow") && itemFavorites.getSystemYellowCount() > 0) {
            return R.color.colorJukuYellow;
        } else {
            Log.d("TEST","RETURNING BLAAACK");
            return android.R.color.black;
        }
    }


//    public void assignStarColor(WordEntry wordEntry, ImageButton imgStar, ArrayList<String> preferenceFavorites ) {
//        if(wordEntry.getItemFavorites().shouldOpenFavoritePopup(preferenceFavorites) &&
//                wordEntry.getItemFavorites().systemListCount(preferenceFavorites) >1) {
//            imgStar.setColorFilter(null);
//            imgStar.setImageResource(R.drawable.ic_star_multicolor);
//        } else {
//            imgStar.setImageResource(R.drawable.ic_star_black);
//            imgStar.setColorFilter(ContextCompat.getColor(mContext, getFavoritesStarColor(preferenceFavorites,wordEntry.getItemFavorites())));
//        }
//    }

    @Nullable
    public static Integer assignStarColor(ItemFavorites itemFavorites, ArrayList<String> preferenceFavorites ) {

        Log.d("TEST","item favs should open popup: " + itemFavorites.shouldOpenFavoritePopup(preferenceFavorites));
                for(String string : preferenceFavorites) {
                    Log.d("TEST","prefs favs strings: " + string);
                }

        if(itemFavorites.shouldOpenFavoritePopup(preferenceFavorites) &&
                itemFavorites.systemListCount(preferenceFavorites) >1) {
//            imgStar.setColorFilter(null);
//            Log.d("TEST","returning null here...");
//            imgStar.setImageResource(R.drawable.ic_star_multicolor);
            return android.R.color.black;
        } else {
//            imgStar.setImageResource(R.drawable.ic_star_black);
//            imgStar.setColorFilter(ContextCompat.getColor(mContext, );
//            Log.d("TEST","getting favs star color...");
            return getFavoritesStarColor(preferenceFavorites,itemFavorites);
        }
    }

    public static Integer assignStarResource(ItemFavorites itemFavorites, ArrayList<String> preferenceFavorites ) {
        if(itemFavorites.shouldOpenFavoritePopup(preferenceFavorites) &&
                itemFavorites.systemListCount(preferenceFavorites) >1) {
            return R.drawable.ic_star_multicolor;
        } else {
            return R.drawable.ic_star_black;
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
    public static boolean onFavoriteStarToggle(Context context, ArrayList<String> preferenceFavorites, WordEntry wordEntry){

        try {
            ItemFavorites itemFavorites = wordEntry.getItemFavorites();

            if(itemFavorites.isEmpty(preferenceFavorites)) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Blue","Red","Green","Yellow","Purple","Orange"});
                if(InternalDB.getWordInterfaceInstance(context).changeWordListStarColor(wordEntry.getId(),"Black",nextColor)) {
                    itemFavorites.setSystemColor(nextColor);
                }

            } else if(preferenceFavorites.contains("Blue") && itemFavorites.getSystemBlueCount() > 0) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Red","Green","Yellow","Purple","Orange"});
                if(InternalDB.getWordInterfaceInstance(context).changeWordListStarColor(wordEntry.getId(),"Blue",nextColor)) {
                    itemFavorites.setSystemBlueCount(0);
                    itemFavorites.setSystemColor(nextColor);
                }

            }
            else if(preferenceFavorites.contains("Red") && itemFavorites.getSystemRedCount() > 0) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Green","Yellow","Purple","Orange"});
                if(InternalDB.getWordInterfaceInstance(context).changeWordListStarColor(wordEntry.getId(),"Red",nextColor)) {
                    itemFavorites.setSystemRedCount(0);
                    itemFavorites.setSystemColor(nextColor);
                }

            }
            else if(preferenceFavorites.contains("Green") && itemFavorites.getSystemGreenCount() > 0) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Yellow","Purple","Orange"});
                if(InternalDB.getWordInterfaceInstance(context).changeWordListStarColor(wordEntry.getId(),"Green",nextColor)) {
                    itemFavorites.setSystemGreenCount(0);
                    itemFavorites.setSystemColor(nextColor);
                }

            }
            else if(preferenceFavorites.contains("Yellow") && itemFavorites.getSystemYellowCount() > 0) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Purple","Orange"});
                if(InternalDB.getWordInterfaceInstance(context).changeWordListStarColor(wordEntry.getId(),"Yellow",nextColor)) {
                    itemFavorites.setSystemYellowCount(0);
                    itemFavorites.setSystemColor(nextColor);
                }

            }
            else if(preferenceFavorites.contains("Purple") && itemFavorites.getSystemPurpleCount() > 0) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Orange"});
                if(InternalDB.getWordInterfaceInstance(context).changeWordListStarColor(wordEntry.getId(),"Purple",nextColor)) {
                    itemFavorites.setSystemPurpleCount(0);
                    itemFavorites.setSystemColor(nextColor);
                }

            } else if(preferenceFavorites.contains("Orange") && itemFavorites.getSystemOrangeCount() > 0) {

                if(InternalDB.getWordInterfaceInstance(context).changeWordListStarColor(wordEntry.getId(),"Orange","Black")) {
                    itemFavorites.setSystemOrangeCount(0);
                }
            }
            return true;
        } catch (Exception e) {
            Log.e("TestFavColors","onFavoriteStarToggle error: " + e);
            return false;
        }

    }



    public static boolean onFavoriteStarToggleTweet(Context context, ArrayList<String> preferenceFavorites, String userId, Tweet tweet){

        try {
            ItemFavorites itemFavorites = tweet.getItemFavorites();

            if(itemFavorites.isEmpty(preferenceFavorites)) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Blue","Red","Green","Yellow","Purple","Orange"});
                if(InternalDB.getTweetInterfaceInstance(context).changeTweetListStarColor(tweet.getIdString(),userId,"Black",nextColor)) {
                    itemFavorites.setSystemColor(nextColor);
                }

            } else if(preferenceFavorites.contains("Blue") && itemFavorites.getSystemBlueCount() > 0) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Red","Green","Yellow","Purple","Orange"});
                if(InternalDB.getTweetInterfaceInstance(context).changeTweetListStarColor(tweet.getIdString(),userId,"Blue",nextColor)) {
                    itemFavorites.setSystemBlueCount(0);
                    itemFavorites.setSystemColor(nextColor);
                }

            }
            else if(preferenceFavorites.contains("Red") && itemFavorites.getSystemRedCount() > 0) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Green","Yellow","Purple","Orange"});
                if(InternalDB.getTweetInterfaceInstance(context).changeTweetListStarColor(tweet.getIdString(),userId,"Red",nextColor)) {
                    itemFavorites.setSystemRedCount(0);
                    itemFavorites.setSystemColor(nextColor);
                }

            }
            else if(preferenceFavorites.contains("Green") && itemFavorites.getSystemGreenCount() > 0) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Yellow","Purple","Orange"});
                if(InternalDB.getTweetInterfaceInstance(context).changeTweetListStarColor(tweet.getIdString(),userId,"Green",nextColor)) {
                    itemFavorites.setSystemGreenCount(0);
                    itemFavorites.setSystemColor(nextColor);
                }

            }
            else if(preferenceFavorites.contains("Yellow") && itemFavorites.getSystemYellowCount() > 0) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Purple","Orange"});
                if(InternalDB.getTweetInterfaceInstance(context).changeTweetListStarColor(tweet.getIdString(),userId,"Yellow",nextColor)) {
                    itemFavorites.setSystemYellowCount(0);
                    itemFavorites.setSystemColor(nextColor);
                }

            }
            else if(preferenceFavorites.contains("Purple") && itemFavorites.getSystemPurpleCount() > 0) {
                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Orange"});
                if(InternalDB.getTweetInterfaceInstance(context).changeTweetListStarColor(tweet.getIdString(),userId,"Purple",nextColor)) {
                    itemFavorites.setSystemPurpleCount(0);
                    itemFavorites.setSystemColor(nextColor);
                }

            } else if(preferenceFavorites.contains("Orange") && itemFavorites.getSystemOrangeCount() > 0) {
                if(InternalDB.getTweetInterfaceInstance(context).changeTweetListStarColor(tweet.getIdString(),userId,"Orange","Black")) {
                    itemFavorites.setSystemOrangeCount(0);
                }
            }
            return true;
        } catch (Exception e) {
            Log.e("TestFavColors","onFavoriteStarToggle error: " + e);
            return false;
        }

    }


    /**
     * Cycles through an array of possible color entries, looking for a match in the preferenceFavorites.
     * If one is found, that color is the "next" favorites color during a {@link #onFavoriteStarToggle(Context, ArrayList, WordEntry)}
     * If nothing is found, the star reverts to unassigned (colored black)
     * @param preferenceFavorites array of current "activated" system list names
     * @param options array of possible next color entries that a favorites star could toggle to
     * @return the name of the next available color entry
     */
    public static String findNextFavoritesColor(ArrayList<String> preferenceFavorites,String[] options) {
        for (String option: options) {
            if(preferenceFavorites.contains(option)) {
                Log.d("FAVTEST","findnextfavs: " + options + ", returning: " + option);
                return option;
            }
        }
        Log.d("FAVTEST","findnextfavs: " + options + ", returning: " + "BLACK");
        return "Black";
    }

//    public static boolean onFavoriteStarToggle(Context context, ArrayList<String> preferenceFavorites, WordEntry wordEntry){
//
//        try {
//            ItemFavorites itemFavorites = wordEntry.getItemFavorites();
//
//            if(itemFavorites.isEmpty(preferenceFavorites)) {
//                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Blue","Green","Red","Yellow"});
//                if(InternalDB.getInstance(context).changeWordListStarColor(wordEntry.getId(),"Black",nextColor)) {
//                    itemFavorites.setSystemColor(nextColor);
//                }
//
//            } else if(preferenceFavorites.contains("Blue") && itemFavorites.getSystemBlueCount() > 0) {
//                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Green","Red","Yellow"});
//                if(InternalDB.getInstance(context).changeWordListStarColor(wordEntry.getId(),"Blue",nextColor)) {
//                    itemFavorites.setSystemBlueCount(0);
//                    itemFavorites.setSystemColor(nextColor);
//                }
//
//            } else if(preferenceFavorites.contains("Green") && itemFavorites.getSystemGreenCount() > 0) {
//                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Red","Yellow"});
//                if(InternalDB.getInstance(context).changeWordListStarColor(wordEntry.getId(),"Green",nextColor)) {
//                    itemFavorites.setSystemGreenCount(0);
//                    itemFavorites.setSystemColor(nextColor);
//                }
//
//            } else if(preferenceFavorites.contains("Red") && itemFavorites.getSystemRedCount() > 0) {
//                String nextColor = findNextFavoritesColor(preferenceFavorites,new String[]{"Yellow"});
//                if(InternalDB.getInstance(context).changeWordListStarColor(wordEntry.getId(),"Red",nextColor)) {
//                    itemFavorites.setSystemRedCount(0);
//                    itemFavorites.setSystemColor(nextColor);
//                }
//
//            } else if(preferenceFavorites.contains("Yellow") && itemFavorites.getSystemYellowCount() > 0) {
//
//                if(InternalDB.getInstance(context).changeWordListStarColor(wordEntry.getId(),"Yellow","Black")) {
//                    itemFavorites.setSystemYellowCount(0);
//                }
//            }
//            return true;
//        } catch (Exception e) {
//            Log.e("TestFavColors","onFavoriteStarToggle error: " + e);
//            return false;
//        }
//
//    }


}