package com.jukuproject.jukutweet.Models;

/**
 * Object holding user preference data for color thresholds. These are the thresholds that determine
 * what color is assigned to a word based on its quiz score.
 */

public class ColorThresholds {
    public ColorThresholds(int greyThreshold
            , float redthreshold
            , float yellowthreshold) {
        this.greyThreshold = greyThreshold;
        this.redthreshold = redthreshold;
        this.yellowthreshold = yellowthreshold;
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

}
