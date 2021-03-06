package com.jukuproject.jukutweet;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageButton;

import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.WordEntry;

import java.util.ArrayList;

/**
 *
 * Container for static methods pertaining to determining/assigning the color of
 * a favorite star for a word or tweet, and updates
 */
public class FavoritesColors {


   /**
     * Assigns a color to the favorites star based on 2 criteria:
     *         1. if the word is contained in the system list of that color
     *            (i.e. ItemFavorites "getsystemcount" for the word returns a "1").
     *         2. whether or not the system list is "activated" in the user prefences (since
     *            (users can turn system lists on and off)
    *          If both are satisfied, the word is part of a system list and a color must be assigned to the star.
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
        } else if(preferenceFavorites.contains("Purple") && itemFavorites.getSystemPurpleCount() > 0) {
            return R.color.colorJukuPurple;
        } else if(preferenceFavorites.contains("Orange") && itemFavorites.getSystemOrangeCount() > 0) {
            return R.color.colorJukuOrange;
        } else {
            return android.R.color.black;
        }
    }

    /**
     * Assigns a color to the favorites star.
     * @param itemFavorites {@link ItemFavorites} object for a word
     * @param preferenceFavorites array of current "activated" system list names
     * @return the color (int) that the current star imagebutton should be tinted
     *
     * @see #getFavoritesStarColor(ArrayList, ItemFavorites)
     */
    public static Integer assignStarColor(ItemFavorites itemFavorites, ArrayList<String> preferenceFavorites ) {

        if(itemFavorites.shouldOpenFavoritePopup(preferenceFavorites) &&
                itemFavorites.systemListCount(preferenceFavorites) >1) {
            return android.R.color.black;
        } else {
            return getFavoritesStarColor(preferenceFavorites,itemFavorites);
        }
    }

    /**
     * Depending on input criteria, chooses which favorites icon to assign to the
     * favorites star (in adapters, tweet breakdown, word detail, etc -- anywhere there are favorite
     * stars for lists)
     * @param isTweet bool true if the favorite star in question is for a tweetlist (which requires tweet icon), false if for
     *                a wordlist (which requires a star)
     * @param itemFavorites {@link ItemFavorites} object with favorite counts for lists associated with the favorite star
     * @param preferenceFavorites Array of favorite lists that are available for a word/tweet to be added to
     * @return resource integer
     *
     * @see ItemFavorites
     * @see com.jukuproject.jukutweet.Adapters.TweetBreakDownAdapter
     * @see com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog
     * @see com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter
     */
    public static Integer assignStarResource(boolean isTweet,ItemFavorites itemFavorites, ArrayList<String> preferenceFavorites ) {
        if(itemFavorites.shouldOpenFavoritePopup(preferenceFavorites) &&
                itemFavorites.systemListCount(preferenceFavorites) >1) {
            if(isTweet) {
                return R.drawable.ic_twitter_multicolor_24dp;
            } else {
                return R.drawable.ic_star_multicolor;
            }
        } else {
            if(isTweet) {
                return R.drawable.ic_twitter_black_24dp;
            } else {
                return R.drawable.ic_star_black;
            }
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
     *         5. Purple
     *         6. Orange
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
        } catch (NullPointerException e) {
            Log.e("TestFavColors","onFavoriteStarToggle NullPointerException : " + e.getMessage());
            return false;
        }

    }


    /**
     * Updates the Tweet object for a row, as well as the JFavoritesTweets table in database, to reflect a
     * new saved "system list" for a TWEET. When the user clicks on the favorites star for a TWEET, and that
     * TWEET is ONLY contained in one system list, the user should be able to toggle through available system
     * lists with a single click (ex: blue-->red-->yellow--> black (i.e. unassigned to a list)-->blue-->red etc)
     *
     * There is an order to system list toggling. It goes:
     *         0. Unassigned (black)
     *         1. Blue
     *         2. Green
     *         3. Red
     *         4. Yellow
     *         5. Purple
     *         6. Orange
     *
     * The user can cycle through this list. Whatever the initial color is for a TWEET, that is where we start in the cycle.
     * If the TWEET is initially Red, for example, and the star is clicked, it should change to yellow. But if yellow is unavailable
     * in the preferences, the star should become black (unassigned) (and reference to red should be removed from the db).
     *
     * @param context Context (for opening db connection)
     * @param preferenceFavorites  array of current "activated" system list names
     * @param userId Twitter userId string
     * @param tweet Tweet Object that will be updated with the new favorite list
     *
     * @return boolean true if operation succesful, false if not
     */
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
        } catch (NullPointerException e) {
            Log.e("TestFavColors","onTweetFavoriteStarToggle NullPointerException error: " + e.getMessage());
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
    private static String findNextFavoritesColor(ArrayList<String> preferenceFavorites,String[] options) {
        for (String option: options) {
            if(preferenceFavorites.contains(option)) {
                return option;
            }
        }
        return "Black";
    }

    /**
     * Adds matching color filter to the favorites star image button of a system list
     *
     * @see com.jukuproject.jukutweet.Adapters.ChooseFavoritesAdapter
     * @see com.jukuproject.jukutweet.Adapters.ChooseFavoritesTweetAdapter
     * @see com.jukuproject.jukutweet.Adapters.CopyMyListItemsAdapter
     * */
    public static void setFavoritesButtonColorFilter(Context context, ImageButton imageButton, String listName) {

        switch (listName) {
            case "Blue":
                imageButton.setColorFilter(ContextCompat.getColor(context, R.color.colorJukuBlue));
                break;
            case "Green":
                imageButton.setColorFilter(ContextCompat.getColor(context, R.color.colorJukuGreen));
                break;
            case "Red":
                imageButton.setColorFilter(ContextCompat.getColor(context, R.color.colorJukuRed));
                break;
            case "Yellow":
                imageButton.setColorFilter(ContextCompat.getColor(context, R.color.colorJukuYellow));
                break;
            case "Purple":
                imageButton.setColorFilter(ContextCompat.getColor(context, R.color.colorJukuPurple));
                break;
            case "Orange":
                imageButton.setColorFilter(ContextCompat.getColor(context, R.color.colorJukuOrange));
                break;
            default:
                break;
        }
    }



}