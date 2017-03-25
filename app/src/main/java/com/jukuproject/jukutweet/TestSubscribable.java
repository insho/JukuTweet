//package com.jukuproject.jukutweet;
//
//import android.database.sqlite.SQLiteDatabase;
//
//import com.jukuproject.jukutweet.Database.InternalDB;
//import com.jukuproject.jukutweet.Models.ParseSentenceItem;
//
//import java.util.ArrayList;
//import java.util.concurrent.Callable;
//
//import rx.Single;
//
///**
// * Created by JClassic on 3/25/2017.
// */
//
//public class TestSubscribable {
//}
//    Single<ArrayList<ParseSentenceItem>> disectTweet = Single.fromCallable(new Callable<ArrayList<ParseSentenceItem>>() {
//
//        @Override
//        public ArrayList<ParseSentenceItem> call() throws Exception {
//            InternalDB helper = InternalDB.getInstance(getContext());
//            SQLiteDatabase db = helper.getReadableDatabase();
//
//            return SentenceParser.getInstance().parseSentence(text
//                    ,db
//                    ,helper.getWordLists(db)
//                    ,colorThresholds);
//        }
//    });