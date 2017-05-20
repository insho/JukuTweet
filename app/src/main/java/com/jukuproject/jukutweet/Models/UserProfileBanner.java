package com.jukuproject.jukutweet.Models;

import android.util.Log;

/**
 * Container for User profile banner image sizes and information (pulled from twitter API)
 */



public class UserProfileBanner {

    private BannerCollection sizes;
    public BannerCollection getBannerCollection() {
        return sizes;
    }

    public String getBestFittingBannerUrl(int screenWidth) {
        Banner currentBestBanner = new Banner();
        currentBestBanner = compareBanners(currentBestBanner,sizes.getIpad(),screenWidth);
        currentBestBanner = compareBanners(currentBestBanner,sizes.getIpad_retina(),screenWidth);
        currentBestBanner = compareBanners(currentBestBanner,sizes.getMobile(),screenWidth);
        currentBestBanner = compareBanners(currentBestBanner,sizes.getMobile_retina(),screenWidth);
        currentBestBanner = compareBanners(currentBestBanner,sizes.getWeb(),screenWidth);

        Log.d("TEST","BestFIT BANNER: h:" + currentBestBanner.getH() + ", w:" + currentBestBanner.getW());
        if(currentBestBanner.isEmpty()) {
            return "";
        } else {
            return currentBestBanner.getUrl();
        }

    }

    private Banner compareBanners(Banner currentBest, Banner possibleCandidate, int screenWidth) {
        if(!possibleCandidate.isEmpty() && (currentBest.isEmpty() || currentBest.getSizeDiff(screenWidth) > possibleCandidate.getSizeDiff(screenWidth))) {
                return possibleCandidate;
        } else {
            return currentBest;
        }
    }


    private class BannerCollection {
        public Banner getIpad() {
            return ipad;
        }

        public Banner getIpad_retina() {
            return ipad_retina;
        }

        public Banner getWeb() {
            return web;
        }

        public Banner getMobile() {
            return mobile;
        }

        public Banner getMobile_retina() {
            return mobile_retina;
        }

        Banner ipad;
        Banner ipad_retina;
        Banner web;
        Banner mobile;
        Banner mobile_retina;

    }


    private class Banner {
        public Banner(){}
        private int h;
        private int w;
        private String url;

        public int getH() {
            return h;
        }

        public boolean isEmpty() {
            return (url==null);
        }

        public int getSizeDiff(int screenWidth) {
            return Math.abs(screenWidth - w);
        }

        public int getW() {
            return w;
        }

        public String getUrl() {
            return url;
        }

        public Banner(int h, int w, String url) {
            this.h = h;
            this.w = w;
            this.url = url;
        }
    }
}
