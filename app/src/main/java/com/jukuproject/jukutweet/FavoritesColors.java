package com.jukuproject.jukutweet;

import android.support.annotation.Nullable;

import com.jukuproject.jukutweet.Models.ItemFavorites;
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
            return R.color.colorJukuBlue;
        } else if(preferenceFavorites.contains("Green") && itemFavorites.getSystemGreenCount() > 0) {
            return R.color.colorJukuGreen;
        } else if(preferenceFavorites.contains("Red") && itemFavorites.getSystemRedCount() > 0) {
            return R.color.colorJukuRed;
        } else if(preferenceFavorites.contains("Yellow") && itemFavorites.getSystemYellowCount() > 0) {
            return R.color.colorJukuYellow;
        } else {
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
        if(itemFavorites.shouldOpenFavoritePopup(preferenceFavorites) &&
                itemFavorites.systemListCount(preferenceFavorites) >1) {
//            imgStar.setColorFilter(null);
//            imgStar.setImageResource(R.drawable.ic_star_multicolor);
            return null;
        } else {
//            imgStar.setImageResource(R.drawable.ic_star_black);
//            imgStar.setColorFilter(ContextCompat.getColor(mContext, );
            return getFavoritesStarColor(preferenceFavorites,itemFavorites);
        }
    }

    public static Integer assignStarResource(WordEntry wordEntry, ArrayList<String> preferenceFavorites ) {
        if(wordEntry.getItemFavorites().shouldOpenFavoritePopup(preferenceFavorites) &&
                wordEntry.getItemFavorites().systemListCount(preferenceFavorites) >1) {
            return R.drawable.ic_star_multicolor;
        } else {
            return R.drawable.ic_star_black;
        }
    }
}