package com.jukuproject.jukutweet.Models;

/**
 * Created by JClassic on 3/23/2017.
 * Object holding prefence data, which is just really annoying to pass through adapters
 */

public class ColorThresholds {
    public ColorThresholds(int greyThreshold
            , float redthreshold
            , float yellowthreshold
            , float tweetGreyThreshold
            , float tweetRedthreshold
            , float tweetYellowthreshold
            , float tweetGreenYellowthreshold) {
        this.greyThreshold = greyThreshold;
        this.redthreshold = redthreshold;
        this.yellowthreshold = yellowthreshold;
        this.tweetGreyThreshold = tweetGreyThreshold;
        this.tweetRedthreshold = tweetRedthreshold;
        this.tweetYellowthreshold = tweetYellowthreshold;
        this.tweetGreenthreshold = tweetGreenYellowthreshold;
    }


    public int getGreyThreshold() {
        return greyThreshold;
    }

    public float getRedThreshold() {
        return redthreshold;
    }

    public float getYellowThreshold() {
        return yellowthreshold;
    }

    private int greyThreshold;
    private float  redthreshold;
    private float  yellowthreshold;

    private float tweetGreyThreshold;
    private float  tweetRedthreshold;

    public float getTweetGreenthreshold() {
        return tweetGreenthreshold;
    }

    private float tweetGreenthreshold;
    public float getTweetYellowthreshold() {
        return tweetYellowthreshold;
    }

    public float getTweetGreyThreshold() {
        return tweetGreyThreshold;
    }

    public float getTweetRedthreshold() {
        return tweetRedthreshold;
    }

    private float  tweetYellowthreshold;



}
