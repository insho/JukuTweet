//package com.jukuproject.jukutweet.Models;
//
//import com.jukuproject.jukutweet.R;
//
///**
// * Created by JClassic on 3/29/2017.
// */
//
////For filling a tweet with beautiful color
////TODO rename this to "SavedTweetKanji" OR Add this piece to word entry
//public class TweetKanjiColor {
//    private int kanjiId;
//    private String color;
//    private int startIndex;
//    private int endIndex;
//    private String furigana;
//
//    public String getFurigana() {
//        if(furigana == null){
//            return "";
//        }
//        return furigana;
//    }
//
//    public void setFurigana(String furigana) {
//        this.furigana = furigana;
//    }
//
//    public TweetKanjiColor(int kanjiId, String color, int startIndex, int endIndex, String furigana) {
//        this.kanjiId = kanjiId;
//        this.color = color;
//        this.startIndex = startIndex;
//        this.endIndex = endIndex;
//        this.furigana = furigana;
//
//    }
//
//    public int getKanjiId() {
//        return kanjiId;
//    }
//
//    public void setKanjiId(int kanjiId) {
//        this.kanjiId = kanjiId;
//    }
//
//    public String getColor() {
//        return color;
//    }
//
//    public void setColor(String color) {
//        this.color = color;
//    }
//
//    public int getStartIndex() {
//        return startIndex;
//    }
//
//    public void setStartIndex(int startIndex) {
//        this.startIndex = startIndex;
//    }
//
//    public int getEndIndex() {
//        return endIndex;
//    }
//
//    public void setEndIndex(int endIndex) {
//        this.endIndex = endIndex;
//    }
//
//    public int getColorValue() {
//        if(color == null) {
//            return android.R.color.black;
//        } else if(color.equals("Grey")){
//            return R.color.colorJukuGrey;
//        }  else if(color.equals("Red")){
//            return R.color.colorJukuRed;
//        }  else if(color.equals("Yellow")){
//            return R.color.colorJukuGrey;
//        }  else if(color.equals("Green")){
//            return R.color.colorJukuYellow;
//        } else {
//            return android.R.color.black;
//        }
//
//    }
//
//}