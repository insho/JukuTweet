package com.jukuproject.jukutweet.Models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by JClassic on 3/23/2017.
 */

public class SharedPrefManager {
    private int mGreyThreshold;
//    private float  mRedthreshold;
//    private float  mYellowthreshold;
    private Boolean mShowlblheadercount;
    private Set<String> mPrefStarsHash;
    private Set<String> mTweetPrefStarsHash;
    private Boolean mIncludemultiplechoicecores;
    private Boolean mIncludefillinsentencesscores;
    private Boolean mWordbuilderintrolayout;
    private Boolean mIncludewordbuilderscores;
    private Boolean mIncludewordmatchscores;
    private Integer mPreferencewordbuilderscorethreshold;
    private Double mSliderMultiplier;
    private Boolean mDifficultAnswers;
    private Integer mWronganswercountbeforeshow;
    private Boolean mHidethescores;
    private ColorThresholds mColorThresholds;

    private static SharedPrefManager mSharedPrefManagerInstance = null;

//        private String mString;

    private SharedPrefManager(){
//            mString = "Hello";

        mGreyThreshold = -1;
//        mRedthreshold = 0.3f;
//        mYellowthreshold = 0.8f;
    }

    public static SharedPrefManager getInstance(Context context){
        if(mSharedPrefManagerInstance == null)
        {
            mSharedPrefManagerInstance = new SharedPrefManager();
            mSharedPrefManagerInstance.init(context);
        }
        return mSharedPrefManagerInstance;
    }

    public void init(Context context){
        if(mGreyThreshold < 0) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            mShowlblheadercount = prefs.getBoolean("showlblheadercount", false);
            mPrefStarsHash= prefs.getStringSet("list_favoriteslistcount", new HashSet<String>());
            mTweetPrefStarsHash= prefs.getStringSet("list_favoriteslistcount_tweet", new HashSet<String>());

            mIncludemultiplechoicecores = prefs.getBoolean("includemultiplechoicecores", true);
            mIncludefillinsentencesscores = prefs.getBoolean("includefillinsentencesscores", true);
            mWordbuilderintrolayout = prefs.getBoolean("wordbuilderintrolayout", true);
            mIncludewordbuilderscores  = prefs.getBoolean("includewordbuilderscores", true);
            mIncludewordmatchscores = prefs.getBoolean("includewordmatchscores", true);
            mPreferencewordbuilderscorethreshold = Integer.parseInt(prefs.getString("preference_wordbuilderscorethreshold", "3"));
            mSliderMultiplier = Double.parseDouble(prefs.getString("sliderMultiplier", "3"));
            mDifficultAnswers = prefs.getBoolean("preference_difficultanswers",false);
            mWronganswercountbeforeshow = Integer.parseInt(prefs.getString("wronganswercountBeforeShow", "2"));
            mHidethescores = prefs.getBoolean("hidethescores",false);


            float redthreshold = Float.parseFloat(prefs.getString("preference_redthreshold", ".3"));
            float yellowthreshold = Float.parseFloat(prefs.getString("preference_yellowthreshold", ".8"));
            mGreyThreshold = Integer.parseInt(prefs.getString("preference_greythreshold", "3"));
            float greythresholdTweet = Float.parseFloat(prefs.getString("preference_greythreshold_tweet", ".3"));
            float redthresholdTweet = Float.parseFloat(prefs.getString("preference_redthreshold_tweet", ".25"));
            float yellowthresholdTweet = Float.parseFloat(prefs.getString("preference_yellowthreshold_tweet", ".40"));
            float greenthresholdTweet = Float.parseFloat(prefs.getString("preference_greenthreshold_tweet", ".75"));

            mColorThresholds = new ColorThresholds(mGreyThreshold
                    ,redthreshold
                    ,yellowthreshold
                    ,greythresholdTweet
                    ,redthresholdTweet
                    ,yellowthresholdTweet
                    ,greenthresholdTweet);


        }

    }

//    public int getGreyThreshold(){
//        return this.mGreyThreshold;
//    }

//    public float getRedThreshold() {
//        return mRedthreshold;
//    }

//    public float getYellowThreshold() {
//        return mYellowthreshold;
//    }

    public Boolean getShowlblheadercount() {
        return mShowlblheadercount;
    }

    public Set<String> getPrefStarsHash() {
        return mPrefStarsHash;
    }

    public ArrayList<String> getActiveFavoriteStars() {
        ArrayList<String> activeFavoriteStars = new ArrayList<>();
        for (String favoriteStar : mPrefStarsHash) {
            if(favoriteStar.length() > 0){
//                String upperS = s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
                activeFavoriteStars.add(favoriteStar);
            }
        }
        return activeFavoriteStars;
    }
    public ArrayList<String> getActiveTweetFavoriteStars() {
        ArrayList<String> activeTweetFavoriteStars = new ArrayList<>();
        for (String favoriteStar : mTweetPrefStarsHash) {
            if(favoriteStar.length() > 0){
//                String upperS = s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
                activeTweetFavoriteStars.add(favoriteStar);
            }
        }
        return activeTweetFavoriteStars;
    }

    public Boolean getIncludemultiplechoicecores() {
        return mIncludemultiplechoicecores;
    }

    public Boolean getIncludewordbuilderscores() {
        return mIncludewordbuilderscores;
    }

    public Boolean getIncludewordmatchscores() {
        return mIncludewordmatchscores;
    }

    public Boolean getWordbuilderintrolayout() {
        return mWordbuilderintrolayout;
    }

    public Boolean getIncludefillinsentencesscores() {
        return mIncludefillinsentencesscores;
    }

    public Integer getPreferencewordbuilderscorethreshold() {
        return mPreferencewordbuilderscorethreshold;
    }

    public Double getSliderMultiplier(){
        return mSliderMultiplier;
    }

    public Boolean getmDifficultAnswers() {
        return mDifficultAnswers;
    }

    public Integer getWronganswercountbeforeshow() {
        return mWronganswercountbeforeshow;
    }

    public Boolean getHidethescores() {
        return mHidethescores;
    }

    public ColorThresholds getColorThresholds() {
        return mColorThresholds;
    }
}


