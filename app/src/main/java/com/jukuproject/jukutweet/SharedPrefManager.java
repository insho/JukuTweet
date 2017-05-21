package com.jukuproject.jukutweet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jukuproject.jukutweet.Models.ColorThresholds;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Repository of sharedpreference data from {@link com.jukuproject.jukutweet.Fragments.UserPreferenceFragment}
 */
public class SharedPrefManager {
    private int mGreyThreshold;
    private Set<String> mPrefStarsHash;
    private Set<String> mTweetPrefStarsHash;
    private Boolean mIncludemultiplechoicecores;
    private Boolean mIncludefillinsentencesscores;
    private Double mSliderMultiplier;
    private Boolean mDifficultAnswers;
    private ColorThresholds mColorThresholds;

    private static SharedPrefManager mSharedPrefManagerInstance = null;

    private SharedPrefManager(){
        mGreyThreshold = -1;
    }

    public static SharedPrefManager getInstance(Context context){
        if(mSharedPrefManagerInstance == null)
        {
            mSharedPrefManagerInstance = new SharedPrefManager();
            mSharedPrefManagerInstance.init(context);
        }
        return mSharedPrefManagerInstance;
    }

    /**
     * Reinitializes the shared preferences. Used when something has changed in the preference fragment,
     * the fragment closes, and things in the main tab buckets may need to be changed based on the new preferences...
     * @param context context
     * @return updated shared preference instance
     */
    public static void initialize(Context context){
        if(mSharedPrefManagerInstance == null)
        {
            mSharedPrefManagerInstance = new SharedPrefManager();
        }
        mSharedPrefManagerInstance.init(context);
    }


    public void init(Context context){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            mPrefStarsHash= prefs.getStringSet("list_favoriteslistcount", new HashSet<String>());
            mTweetPrefStarsHash= prefs.getStringSet("list_favoriteslistcount_tweet", new HashSet<String>());
            mIncludemultiplechoicecores = prefs.getBoolean("includemultiplechoicecores", true);
            mIncludefillinsentencesscores = prefs.getBoolean("includefillinsentencesscores", true);
            mSliderMultiplier = Double.parseDouble(prefs.getString("sliderMultiplier", "3"));
            mDifficultAnswers = prefs.getBoolean("preference_difficultanswers",false);

            float redthreshold = Float.parseFloat(prefs.getString("preference_redthreshold", ".3"));
            float yellowthreshold = Float.parseFloat(prefs.getString("preference_yellowthreshold", ".8"));
            mGreyThreshold = Integer.parseInt(prefs.getString("preference_greythreshold", "3"));

            mColorThresholds = new ColorThresholds(mGreyThreshold
                    ,redthreshold
                    ,yellowthreshold);
    }

    public ArrayList<String> getActiveFavoriteStars() {
        ArrayList<String> activeFavoriteStars = new ArrayList<>();
        for (String favoriteStar : mPrefStarsHash) {
            if(favoriteStar.length() > 0){
                activeFavoriteStars.add(favoriteStar);
            }
        }
        return activeFavoriteStars;
    }
    public ArrayList<String> getActiveTweetFavoriteStars() {
        ArrayList<String> activeTweetFavoriteStars = new ArrayList<>();
        for (String favoriteStar : mTweetPrefStarsHash) {
            if(favoriteStar.length() > 0){
                activeTweetFavoriteStars.add(favoriteStar);
            }
        }
        return activeTweetFavoriteStars;
    }

    public Boolean getIncludemultiplechoicecores() {
        return mIncludemultiplechoicecores;
    }

    public Boolean getIncludefillinsentencesscores() {
        return mIncludefillinsentencesscores;
    }

    public Double getSliderMultiplier(){
        return mSliderMultiplier;
    }

    public Boolean getmDifficultAnswers() {
        return mDifficultAnswers;
    }

    public ColorThresholds getColorThresholds() {
        return mColorThresholds;
    }
}


