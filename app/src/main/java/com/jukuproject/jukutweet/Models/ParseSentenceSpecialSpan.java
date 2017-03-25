package com.jukuproject.jukutweet.Models;

import java.util.ArrayList;

/**
 * Created by JClassic on 3/25/2017.
 * Stores and retrieves a list of spans of text within a
 * sentence in the {@link com.jukuproject.jukutweet.SentenceParser} that
 * the algorythm should NOT be trying to solve (add verb conjugations, etc). Instead they
 * are passed through as whole blocks until the end of the SentenceParser
 *
 * It also produces an array of index numbers representing the indexes within the sentence that are occupied by
 */

//public class ParseSentenceSpecialInputSpans {
//    ArrayList<SpecialSpans> spans;
//
//    ParseSentenceSpecialInputSpans(){};
//
//    public void addUrlSpan(TweetUrl url) {
//
//    }

    public class ParseSentenceSpecialSpan {
    public String getType() {
        return type;
    }

    public String getSpan() {
        return span;
    }


    private String type;
        private String span;
        private int startIndex;
        private int endIndex;

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public ParseSentenceSpecialSpan(String type, String span, String entireSentence) {
            this.type = type;
            this.span = span;
            if(entireSentence.contains(span)) {
                startIndex = entireSentence.indexOf(span);
                endIndex = startIndex + type.length();
            }
        }



    }

//}

