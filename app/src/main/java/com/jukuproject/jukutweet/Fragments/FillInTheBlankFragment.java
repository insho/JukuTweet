package com.jukuproject.jukutweet.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;


/**
 * Created by JClassic on 4/3/2017.
 */

public class FillInTheBlankFragment extends Fragment {
    int currentTotal = 0; //CURRENT number of questions asked
    int currentCorrect = 0; //CURRENT number of correct answers

    ArrayList<Tweet> mDataset;
    Integer mQuizSize;
    double mTotalWeight;
    MyListEntry mMyListEntry;
    String mColorString;
//    ColorThresholds mColorThresholds;


    boolean isCorrectFirstTry;
    int linewidth = 0;
    int displaywidth = 0;
    int displaymarginpadding = 30; //How much to pad the edge of the screen by when laying down the sentenceblocks (so the sentence doesn't overlap the screen or get cut up too much)
    int spinnerwidth = 200;
    int spinnerheight = 55;

    LinearLayout linearLayoutVerticalMain;  //This is the main linear layout, that we will fill row by row with horizontal linear layouts, which are     // in turn filled with vertical layouts (with furigana on top and japanese on bottom)
    LinearLayout linearLayout;


    public FillInTheBlankFragment() {}

    public static FillInTheBlankFragment newInstance(ArrayList<Tweet> tweets
            , Integer quizSize
            , double totalWeight
            , String colorString
            , MyListEntry myListEntry
    ) {

        long startTime = System.nanoTime();
        FillInTheBlankFragment fragment = new FillInTheBlankFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("tweets", tweets);
        args.putInt("quizSize",quizSize);
        args.putDouble("totalWeight",totalWeight);
        args.putString("colorString",colorString);
        args.putParcelable("myListEntry",myListEntry);
        fragment.setArguments(args);
        long endTime = System.nanoTime();
        Log.e("TEST","assign FRAGMENT ELLAPSED TIME: " + (endTime - startTime) / 1000000000.0);
        return  fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        long startTime = System.nanoTime();
        //Set input global data
        mDataset = getArguments().getParcelableArrayList("tweets");
        mQuizSize = getArguments().getInt("quizSize");
        mTotalWeight = getArguments().getDouble("totalWeight");
        mColorString = getArguments().getString("colorString");
        mMyListEntry = getArguments().getParcelable("mylistentry");

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_fillintheblanks, null);

        long endTime = System.nanoTime();
        Log.e("TEST","assign CREATEVIEW ELLAPSED TIME: " + (endTime - startTime) / 1000000000.0);

        /** Get width of screen */
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        displaywidth = metrics.widthPixels;
        displaymarginpadding = (int) ((float) (displaywidth) * 0.055555556);
        spinnerwidth = (int) (160.0f * metrics.density + 0.5f);
        spinnerheight = (int) (37.0f * metrics.density + 0.5f);

        linearLayoutVerticalMain = (LinearLayout) view.findViewById(R.id.sentence_layout);
        linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        /* Reset the lists and layouts */
        //TODO -- test set up the sentence!
        TextView text = (TextView) view.findViewById(R.id.sentence_eng);
        text.setText(mDataset.get(0).getText());


        return view;
    }
}
