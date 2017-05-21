package com.jukuproject.jukutweet.Models;

/**
 * Created by JClassic on 3/23/2017.
 * Object holding prefence data, which is just really annoying to pass through adapters
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
