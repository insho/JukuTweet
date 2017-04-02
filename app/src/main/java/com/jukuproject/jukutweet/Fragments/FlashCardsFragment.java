package com.jukuproject.jukutweet.Fragments;

import android.animation.Animator;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Created by JClassic on 3/31/2017.
 */

public class FlashCardsFragment extends Fragment {

    String TAG = "FlashCards";

    LayoutInflater inflater;	//Used to create individual pages
    ViewPager vp;	//Reference to class to swipe views
    ArrayList<WordEntry> mDataset;

    int currentPosition = 0;
    boolean frontShowing = true;
    boolean freshdeck = true;
    String mFrontValue;
    String mBackValue;

    private GestureDetectorCompat mDetector;
    View page;
    private GestureDetector mGestureDetector;
    int totalcount; //total count of cards in stack
    int currentcount = 1; //current position count in stack
    Animator animator_rightin;
    Animator animator_rightout;
    Animator animator_leftin;
    Animator animator_leftout;
    Animator animator_upin;




    public FlashCardsFragment() {}

    public static FlashCardsFragment newInstance(ArrayList<WordEntry> wordEntries, String frontValue, String backValue) {
        FlashCardsFragment fragment = new FlashCardsFragment();
        Bundle args = new Bundle();
        args.putString("frontValue",frontValue);
        args.putString("backValue",backValue);
        args.putParcelableArrayList("wordEntries", wordEntries);
        fragment.setArguments(args);

        return  fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view  = LayoutInflater.from(getActivity()).inflate(R.layout.flashcard, null);
        mDataset = getArguments().getParcelableArrayList("wordEntries");
        mFrontValue = getArguments().getString("frontValue");
        mBackValue = getArguments().getString("backValue");

        page = LayoutInflater.from(getActivity()).inflate(R.layout.flashcard_item, null);
        vp=(ViewPager) view.findViewById(R.id.viewPager);
        currentPosition = 0;
//        vp.setAdapter(new MyPagesAdapter_Array());


        FloatingActionButton fab_shuffle = (FloatingActionButton) view.findViewById(R.id.fab);
//        fab_shuffle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Collections.shuffle(mDataset);
//                vp.setAdapter(new MyPagesAdapter_Array());
//                freshdeck = true;
//
//                animator_rightin = AnimatorInflater.loadAnimator(getContext(),
//                        R.animator.card_flip_right_in);
//                animator_rightout = AnimatorInflater.loadAnimator(getContext(),
//                        R.animator.card_flip_right_out);
//                animator_leftin = AnimatorInflater.loadAnimator(getContext(),
//                        R.animator.card_flip_left_in);
//                animator_leftout = AnimatorInflater.loadAnimator(getContext(),
//                        R.animator.card_flip_left_out);
//
//                Animation slide_in_left_again = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right);
//                vp.startAnimation(slide_in_left_again);
//
//                animator_rightin.setTarget(vp);
//                animator_rightin.start();
//                animator_rightin.setStartDelay(100);
//                animator_rightin.start();
//                frontShowing = true; //toggle the boolean to reflect current flipped state
//
//                /**  THIS IS THE ACTION BUTTON SHUFFLE BLOCK*/
//                //Reset the currentcount to 1 (since it's a new deck now)
//                currentcount = 1;
//                freshdeck =true;
//                String stringcount = currentcount + "/" + totalcount;
//                ((TextView) page.findViewById(R.id.scorecount)).setText(stringcount);
//
//                if(mFrontValue.equals("Kanji") && tmparray.get(mFrontValue) == null) { //If the furigana entry is null, use the kanji one homie
//                    ((TextView) page.findViewById(R.id.textMessage)).setText(tmparray.get(1));
//                } else {
//                    ((TextView) page.findViewById(R.id.textMessage)).setText(tmparray.get(frontValue));
//                    ((TextView) page.findViewById(R.id.furigana)).setText(tmparray.get(5));
//                }
//
//                if(BuildConfig.DEBUG) {
//                    Log.d(TAG, "fronvalue (shuffle): " + mFrontValue);
//                    Log.d(TAG, "furigana (shuffle): " + tmparray.get(5));
//                }
//
//            }
//        });

//
//        final GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
//            @Override
//            public boolean onDoubleTap(MotionEvent e) {
//
//                animator_upin = AnimatorInflater.loadAnimator(getContext(),
//                        R.animator.card_flip_up_in);
//                animator_upin.setTarget(vp);
//                animator_upin.start();
//
//                //toggle the boolean to reflect current flipped state
//                flipped = !flipped;
//                final ArrayList<String> tmparray = arrayRecord.get(currentPosition);
//                if(debug){Log.d(TAG, "find view with tag: " + tmparray.get(0));}
//                int currentTag  = Integer.parseInt(tmparray.get(0));
//
//                TextView textMain = (TextView) vp.findViewWithTag(currentTag).findViewById(R.id.textMessage);
//                TextView textFurigana = (TextView) vp.findViewWithTag(currentTag).findViewById(R.id.furigana);
//                TextView defarraylistview = (TextView) vp.findViewWithTag(currentTag).findViewById(R.id.flashcard_listview);
//
//                if(debug) {
//                    Log.d(TAG, "doubletap flipped: " + flipped);
//                    Log.d(TAG, "doubletap frontValue: " + frontValue);
//                    Log.d(TAG, "doubletap backValue: " + backValue);
//                }
//
//                if(flipped) {
//
//                    switch (backValue) {
//                        case "Definition":
//                            textMain.setVisibility(View.GONE);
//                            textFurigana.setVisibility(View.INVISIBLE);
//                            defarraylistview.setVisibility(View.VISIBLE);
//                            break;
//                        case "Kanji":
//                            textMain.setVisibility(View.VISIBLE);
//                            textFurigana.setVisibility(View.INVISIBLE);
//                            defarraylistview.setVisibility(View.GONE);
//
//                            if(tmparray.get(backValue) == null) { //If the furigana entry is null, use the kanji one homie
//                                textMain.setText(tmparray.get(1));
//                            } else {
//                                textMain.setText(tmparray.get(backValue));
//                            }
//
//                            break;
//
//                        default:
//                            textMain.setVisibility(View.VISIBLE);
//                            textFurigana.setVisibility(View.INVISIBLE);
//                            defarraylistview.setVisibility(View.GONE);
//                            textMain.setText(tmparray.get(backValue));
//                            break;
//                    }
//
//                } else {
//
//                    switch (frontValue) {
//                        case "Definition":
//                            textMain.setVisibility(View.GONE);
//                            textFurigana.setVisibility(View.INVISIBLE);
//                            defarraylistview.setVisibility(View.VISIBLE);
//
//                            break;
//                        case "Kanji":
//                            textMain.setVisibility(View.VISIBLE);
//                            textFurigana.setVisibility(View.INVISIBLE);
//                            defarraylistview.setVisibility(View.GONE);
//
//
//                            if(tmparray.get(frontValue) == null) { //If the furigana entry is null, use the kanji one homie
//                                textMain.setText(tmparray.get(1));
//
//                            } else {
//                                textMain.setText(tmparray.get(frontValue));
//                            }
//
//                            break;
//
//                        default:
//                            textMain.setVisibility(View.VISIBLE);
//                            textFurigana.setVisibility(View.INVISIBLE);
//                            defarraylistview.setVisibility(View.GONE);
//                            textMain.setText(tmparray.get(frontValue));
//                            break;
//                    }
//
//                }
//
//                return true;
//            }
//
//            @Override
//            public boolean onSingleTapConfirmed(MotionEvent e) {
//                //toggle the boolean to reflect current flipped state
//                if(debug){Log.d(TAG,"Singletap flipped: "+ flipped);}
//
//                if(!flipped) {
//                    final ArrayList<String> tmparray = arrayRecord.get(currentPosition);
//                    int currentTag = Integer.parseInt(tmparray.get(0));
//                    if((vp.findViewWithTag(currentTag)).findViewById(R.id.furigana).getVisibility() == View.VISIBLE) {
//                        if(debug){Log.d(TAG, "Singletap furigana INVISIBLE");}
//                        (vp.findViewWithTag(currentTag)).findViewById(R.id.furigana).setVisibility(View.INVISIBLE);
//                    } else {
//                        if(debug){Log.d(TAG,"Singletap furigana VISIBLE");}
//                        (vp.findViewWithTag(currentTag)).findViewById(R.id.furigana).setVisibility(View.VISIBLE);
//                    }
//
//                    if(frontValue == 5 || backValue == 5) {
//                        if(debug){Log.d(TAG,"We're doing this (4)");}
//                        (vp.findViewWithTag(currentTag)).findViewById(R.id.furigana).setVisibility(View.INVISIBLE);
//                    }
//
//                }
//                return true;
//            }
//
//
////            @Override
////            public void onLongPress(MotionEvent e) {
////
////                final ArrayList<String> tmparray = arrayRecord.get(currentPosition);
////                int currentTag  = Integer.parseInt(tmparray.get(0));
////
////                WordDetailPopupWindow x = new WordDetailPopupWindow(FlashCards.this,page , currentTag );
////                x.CreateView();
////            }
//
//            @Override
//            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//                if(debug){Log.d(TAG, "onFling " + e1.getX() + " - " + e2.getX());}
//
//                if (e1.getY() < e2.getY()) {
//
//                    animator_upin = AnimatorInflater.loadAnimator(getContext(),
//                            R.animator.card_flip_up_in);
//                    animator_upin.setTarget(vp);
//                    animator_upin.start();
//
//
//                    //toggle the boolean to reflect current flipped state
//                    flipped = !flipped;
//                    final ArrayList<String> tmparray = arrayRecord.get(currentPosition);
//                    if(debug){Log.d(TAG, "find view with tag: " + tmparray.get(0));}
//                    int currentTag  = Integer.parseInt(tmparray.get(0));
//
//                    TextView textMain = (TextView) vp.findViewWithTag(currentTag).findViewById(R.id.textMessage);
//                    TextView textFurigana = (TextView) vp.findViewWithTag(currentTag).findViewById(R.id.furigana);
//                    TextView defarraylistview = (TextView) vp.findViewWithTag(currentTag).findViewById(R.id.flashcard_listview);
//
//                    if(debug) {
//                        Log.d(TAG, "fling flipped: " + flipped);
//                        Log.d(TAG, "fling frontValue: " + frontValue);
//                        Log.d(TAG, "fling backValue: " + backValue);
//                    }
//
//                    if(flipped) {
//
//                        switch (backValue) {
//                            case "Definition":
//                                if(debug){Log.d(TAG,"showing definition...");}
//                                textMain.setVisibility(View.GONE);
//                                textFurigana.setVisibility(View.INVISIBLE);
//                                defarraylistview.setVisibility(View.VISIBLE);
//
//                                break;
//                            case "Kanji":
//                                textMain.setVisibility(View.VISIBLE);
//                                textFurigana.setVisibility(View.INVISIBLE);
//                                defarraylistview.setVisibility(View.GONE);
//
//                                if(tmparray.get(backValue) == null) { //If the furigana entry is null, use the kanji one homie
//                                    textMain.setText(tmparray.get(1));
//                                } else {
//                                    textMain.setText(tmparray.get(backValue));
//                                }
//
//
//                                break;
//
//                            default:
//                                textMain.setVisibility(View.VISIBLE);
//                                textFurigana.setVisibility(View.INVISIBLE);
//                                if(debug){Log.d(TAG, "doing default... (5)");}
//
//                                defarraylistview.setVisibility(View.GONE);
//                                textMain.setText(tmparray.get(backValue));
//                                break;
//                        }
//
//
//                    } else {
//
//                        switch (frontValue) {
//                            case "Definition":
//                                textMain.setVisibility(View.GONE);
//                                textFurigana.setVisibility(View.INVISIBLE);
//                                defarraylistview.setVisibility(View.VISIBLE);
//
//                                break;
//                            case "Kanji":
//                                textMain.setVisibility(View.VISIBLE);
//                                textFurigana.setVisibility(View.INVISIBLE);
//                                defarraylistview.setVisibility(View.GONE);
//
//                                if(tmparray.get(frontValue) == null) { //If the furigana entry is null, use the kanji one homie
//                                    if(debug){Log.d(TAG, "we're doing this (2)");}
//                                    textMain.setText(tmparray.get(1));
//
//                                } else {
//                                    if(debug){Log.d(TAG,"implementing backvalue???");}
//                                    textMain.setText(tmparray.get(frontValue));
//                                }
//
//                                break;
//
//                            default:
//                                textMain.setVisibility(View.VISIBLE);
//                                textFurigana.setVisibility(View.INVISIBLE);
//                                if(debug){Log.d(TAG, "doing default...(6)");}
//
//                                defarraylistview.setVisibility(View.GONE);
//                                textMain.setText(tmparray.get(frontValue));
//                                break;
//                        }
//                    }
//                }
//
//                if (e1.getY() > e2.getY()) {
//
//                    animator_upin = AnimatorInflater.loadAnimator(getContext(),
//                            R.animator.card_flip_up_in);
//                    animator_upin.setTarget(vp);
//                    animator_upin.start();
//
//                    //toggle the boolean to reflect current flipped state
//                    flipped = !flipped;
//                    final ArrayList<String> tmparray = arrayRecord.get(currentPosition);
//                    if(debug){Log.d(TAG, "find view with tag: " + tmparray.get(0));}
//                    int currentTag  = Integer.parseInt(tmparray.get(0));
//
//                    TextView textMain = (TextView) vp.findViewWithTag(currentTag).findViewById(R.id.textMessage);
//                    TextView textFurigana = (TextView) vp.findViewWithTag(currentTag).findViewById(R.id.furigana);
//                    TextView defarraylistview = (TextView) vp.findViewWithTag(currentTag).findViewById(R.id.flashcard_listview);
//
//                    if(debug) {
//                        Log.d(TAG, "zdoubletap flipped: " + flipped);
//                        Log.d(TAG, "zdoubletap frontValue: " + frontValue);
//                        Log.d(TAG, "zdoubletap backValue: " + backValue);
//                    }
//
//                    if(flipped) {
//
//                        switch (backValue) {
//                            case "Definition":
//                                if(debug){Log.d(TAG,"showing definition...");}
//                                textMain.setVisibility(View.GONE);
//                                textFurigana.setVisibility(View.INVISIBLE);
//                                defarraylistview.setVisibility(View.VISIBLE);
//
//                                break;
//                            case "Kanji":
//                                textMain.setVisibility(View.VISIBLE);
//                                textFurigana.setVisibility(View.INVISIBLE);
//                                defarraylistview.setVisibility(View.GONE);
//
//                                if(tmparray.get(backValue) == null) { //If the furigana entry is null, use the kanji one homie
//                                    if(debug){Log.d(TAG, "we're doing this (2)");}
//                                    textMain.setText(tmparray.get(1));
//
//                                } else {
//                                    if(debug){Log.d(TAG,"implementing backvalue???");}
//                                    textMain.setText(tmparray.get(backValue));
//                                }
//
//                                break;
//
//                            default:
//                                textMain.setVisibility(View.VISIBLE);
//                                textFurigana.setVisibility(View.INVISIBLE);
//                                if(debug){Log.d(TAG, "doing default... (7)");}
//
//                                defarraylistview.setVisibility(View.GONE);
//                                textMain.setText(tmparray.get(backValue));
//                                break;
//                        }
//
//
//                    } else {
//
//                        switch (frontValue) {
//                            case "Definition":
//                                textMain.setVisibility(View.GONE);
//                                textFurigana.setVisibility(View.INVISIBLE);
//                                defarraylistview.setVisibility(View.VISIBLE);
//
//                                break;
//                            case "Kanji":
//                                textMain.setVisibility(View.VISIBLE);
//                                textFurigana.setVisibility(View.INVISIBLE);
//                                defarraylistview.setVisibility(View.GONE);
//
//                                if(tmparray.get(frontValue) == null) { //If the furigana entry is null, use the kanji one homie
//                                    if(debug){Log.d(TAG, "we're doing this (2)");}
//                                    textMain.setText(tmparray.get(1));
//
//                                } else {
//                                    if(debug){Log.d(TAG,"implementing backvalue???");}
//                                    textMain.setText(tmparray.get(frontValue));
//                                }
//
//                                break;
//
//                            default:
//                                textMain.setVisibility(View.VISIBLE);
//                                textFurigana.setVisibility(View.INVISIBLE);
//                                if(debug){Log.d(TAG, "doing default... (8)");}
//                                defarraylistview.setVisibility(View.GONE);
//                                textMain.setText(tmparray.get(frontValue));
//                                break;
//                        }
//                    }
//                }
//
//                return true;
//            }
//        };
//
//
//
//        final GestureDetector detector = new GestureDetector(listener);
//
//        detector.setOnDoubleTapListener(listener);
//        detector.setIsLongpressEnabled(true);
//
//        getActivity().getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                return detector.onTouchEvent(event);
//            }
//        });
//
//        mGestureDetector = new GestureDetector(getContext(), listener);
//        mGestureDetector.setOnDoubleTapListener(listener);
//
//
//
//        return v;
       View v = new View(getActivity());
        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    //Implement PagerAdapter Class to handle individual page creation
//    class MyPagesAdapter_Array extends PagerAdapter {
//
//
//        @Override
//        public int getCount() {
//            //Return total pages, here one for each data item
//            return mDataset.size();
//        }
//        //Create the given page (indicated by position)
//        @Override
//        public Object instantiateItem(ViewGroup container, final int position) {
//
//            final View page = inflater.inflate(R.layout.flashcard_item, null);
////            final ArrayList<String> tmparray = arrayRecord.get(position);
//            page.setTag(Integer.parseInt(tmparray.get(0)));
//
//            if(debug){Log.e(TAG, "instantiate tag: " + page.getTag());}
//
//            /**  APPARENTLY THIS ONE RUNS IF YOU HIT THE SHUFFLE FAB*/
//            currentcount=1;
//            ((TextView) page.findViewById(R.id.scorecount)).setText(currentcount + "/" + totalcount);
//
//            //Set the flipped tag false to start with
//            flipped = false;
//            page.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, final MotionEvent event) {
//                    currentPosition = position;
//                    mGestureDetector.onTouchEvent(event);
//                    return true;
//                }
//            });
//
//            vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//
//                @Override
//                public void onPageSelected(int pos) {
//                    if(debug){Log.e(TAG,"RUNNING onPageSelected...");}
//                    if(freshdeck) {
//                        currentcount = pos+1;
//                    } else {
//                        currentcount = pos+1;
//                    }
//                    flipped = false;
//                    ((TextView) page.findViewById(R.id.scorecount)).setText(currentcount + "/" + totalcount);
//                    TextView textMain = (TextView) page.findViewById(R.id.textMessage);
//                    TextView textFurigana = (TextView) page.findViewById(R.id.furigana);
//                    TextView defarraylistview = (TextView) page.findViewById(R.id.flashcard_listview);
//
//                    String newText = tmparray.get(backValue);
//
//                    /** SET THE Kanji/Furigana boxes */
//                    switch (frontValue) {
//                        case "Kanji":
//                            if(tmparray.get(frontValue) == null) { //If the furigana entry is null, use the kanji one homie
//                                if(debug){ Log.d(TAG,"we're doing this (2)");}
//                                ((TextView) page.findViewById(R.id.textMessage)).setText(tmparray.get(1));
//                                page.findViewById(R.id.furigana).setVisibility(TextView.INVISIBLE);
//                            } else {
//                                ((TextView) page.findViewById(R.id.textMessage)).setText(tmparray.get(frontValue));
//                                ((TextView) page.findViewById(R.id.furigana)).setText(tmparray.get(5));
//                            }
//                            break;
//
//                        default:
//                            textMain.setText(tmparray.get(frontValue));
//                            textFurigana.setText(tmparray.get(5));
//                            break;
//                    }
//
//                    //This variable keeps track of how big the definition is
//                    //it can be either 1 (small), 2 medium or 3 large
//                    int defsizelevel = 1;
//
//                    //i.e. if we're showing the definition on the flipside
//                    if(backValue.equals("Definition") || frontValue.equals("Definition")) {
//                        newText = tmparray.get(4);
//
//                        /** IF TEXT IS DEFINITION, SPLIT OUT THE DIFFERENT DEFINITIONS INTO BULLET POINTS */
//                        List<String> defarray = new ArrayList<>();
//                        defarray.clear();
//                        StringBuilder stringBuilder = new StringBuilder();
//                        int definitionaddedcounter = 0;
//                        for (int i=1; i<=6; i++) {
//                            String s = "(" + String.valueOf(i) + ")";
//                            String sNext = "(" + String.valueOf(i + 1) + ")";
//                            int slength = s.length();
//                            if (newText.contains(s)) {
//                                int endIndex = newText.length();
//                                if (newText.contains(sNext)) { //If we can find the next "(#)" in the string, we'll use it as this definition's end point
//                                    endIndex = newText.indexOf(sNext);
//                                }
//
//                                String sentence = newText.substring(newText.indexOf(s) + slength, endIndex);
//                                //Capitalize it
//                                if (sentence.length() > 1) {
//                                    sentence = sentence.substring(0, 1).toUpperCase() + sentence.substring(1).toLowerCase();
//                                }
//                                stringBuilder.append("\u2022 " + sentence + System.getProperty("line.separator"));
//                                defarray.add("\u2022 " + sentence);
//                                definitionaddedcounter += 1;
//
//                                if(definitionaddedcounter< 3) {
//                                    defsizelevel = 2;
//                                } else {
//                                    defsizelevel = 1;
//                                }
//
//                            } else if (i == 1) { //if the thing doesn't contain a "(1)", just print the whole definition in line 1 of the array.
//                                String sentence = newText;
//                                if (sentence.length() > 1) {
//                                    sentence = newText.substring(0, 1).toUpperCase() + newText.substring(1).toLowerCase();
//                                }
//                                if(debug){Log.d(TAG,"sentence length: " + sentence.length());}
//                                if(sentence.length()<20) {
//                                    defsizelevel = 4;
//                                } else {
//                                    defsizelevel = 3;
//                                }
//                                stringBuilder.append(sentence);
//                                defarray.add(sentence);
//                            }
//
//                        }
//
//                        defarraylistview.setText(stringBuilder.toString());
//                        if(defarray.size()<=1) {
//                            defarraylistview.setGravity(Gravity.CENTER);
//                        }
//                        switch (defsizelevel) {
//                            case 1:
//                                defarraylistview.setTextSize(TypedValue.COMPLEX_UNIT_PT, 7);
//                                break;
//                            case 2:
//                                defarraylistview.setTextSize(TypedValue.COMPLEX_UNIT_PT, 9);
//                                break;
//                            case 3:
//                                defarraylistview.setTextSize(TypedValue.COMPLEX_UNIT_PT, 10);
//                                break;
//                            case 4:
//                                defarraylistview.setTextSize(TypedValue.COMPLEX_UNIT_PT, 12);
//                                break;
//                            default:
//                                defarraylistview.setTextSize(TypedValue.COMPLEX_UNIT_PT, 7);
//                                break;
//                        }
//
//                    }
//
//                    switch (frontValue) {
//                        case "Definition":
//                            textMain.setVisibility(View.GONE);
//                            textFurigana.setVisibility(View.INVISIBLE);
//                            defarraylistview.setVisibility(View.VISIBLE);
//                            break;
//                        case "Kanji":
//                            textMain.setVisibility(View.VISIBLE);
//                            textFurigana.setVisibility(View.INVISIBLE);
//                            defarraylistview.setVisibility(View.GONE);
//                            break;
//
//                        default:
//                            textMain.setVisibility(View.VISIBLE);
//                            textFurigana.setVisibility(View.INVISIBLE);
//                            if(debug){Log.d(TAG, "doing default... (11)");}
//                            defarraylistview.setVisibility(View.GONE);
//                            break;
//                    }
//
//
//
//                }
//
//                @Override
//                public void onPageScrolled(int arg0, float arg1, int arg2) {
//
//                }
//
//                @Override
//                public void onPageScrollStateChanged(int arg0) {
//                }
//            });
//
//
//
//            if (freshdeck) {
//                ((TextView) page.findViewById(R.id.scorecount)).setText(currentcount + "/" + totalcount);
//                freshdeck = false;
//            }
//
//            TextView textMain = (TextView) page.findViewById(R.id.textMessage);
//            TextView textFurigana = (TextView) page.findViewById(R.id.furigana);
//            TextView defarraylistview = (TextView) page.findViewById(R.id.flashcard_listview);
//
//
//            /** SET THE Kanji/Furigana boxes */
//            switch (frontValue) {
//                case "Kanji":
//
//                    if(tmparray.get(frontValue) == null) { //If the furigana entry is null, use the kanji one homie
//                        if(debug){Log.d(TAG,"we're doing this (3)");}
//                        ((TextView) page.findViewById(R.id.textMessage)).setText(tmparray.get(1));
//                        page.findViewById(R.id.furigana).setVisibility(TextView.INVISIBLE);
//                    } else {
//                        ((TextView) page.findViewById(R.id.textMessage)).setText(tmparray.get(frontValue));
//                        ((TextView) page.findViewById(R.id.furigana)).setText(tmparray.get(5));
//                    }
//                    break;
//
//                default:
//                    textMain.setText(tmparray.get(frontValue));
//                    textFurigana.setText(tmparray.get(5));
////                    textFurigana.setTypeface(null, Typeface.ITALIC);
//                    break;
//            }
//
//
//
//            /** SET THE DEFINITION LISTVIEW */
//
//            //Set the reverse side text
//            String newText = tmparray.get(backValue);
//
//            //i.e. if we're showing the definition on the flipside
//            int defsizelevel = 1;
//            if(backValue.equals("Definition") || frontValue.equals("Definition")) {
//                newText = tmparray.get(4);
//                /** IF TEXT IS DEFINITION, SPLIT OUT THE DIFFERENT DEFINITIONS INTO BULLET POINTS */
//                List<String> defarray = new ArrayList<>();
//                defarray.clear();
//                StringBuilder stringBuilder = new StringBuilder();
//                int definitionaddedcounter = 0;
//                for (int i=1; i<=6; i++) {
//                    String s = "(" + String.valueOf(i) + ")";
//                    String sNext = "(" + String.valueOf(i + 1) + ")";
//                    int slength = s.length();
//
//                    if (newText.contains(s)) {
//                        int endIndex = newText.length();
//                        if (newText.contains(sNext)) { //If we can find the next "(#)" in the string, we'll use it as this definition's end point
//                            endIndex = newText.indexOf(sNext);
//                        }
//
//                        String sentence = newText.substring(newText.indexOf(s) + slength, endIndex);
//
//                        if (sentence.length() > 1) {
//                            sentence = sentence.substring(0, 1).toUpperCase() + sentence.substring(1).toLowerCase();
//                        }
//
//                        stringBuilder.append("\u2022 " + sentence + System.getProperty("line.separator"));
//                        defarray.add("\u2022 " + sentence);
//                        definitionaddedcounter += 1;
//
//                        if(definitionaddedcounter< 3) {
//                            defsizelevel = 2;
//                        } else {
//                            defsizelevel = 1;
//                        }
//
//                    } else if (i == 1) { //if the thing doesn't contain a "(1)", just print the whole definition in line 1 of the array.
//                        String sentence = newText;
//                        if (sentence.length() > 1) {
//                            sentence = newText.substring(0, 1).toUpperCase() + newText.substring(1).toLowerCase();
//                        }
//                        if(sentence.length()<20) {
//                            defsizelevel = 4;
//                        } else {
//                            defsizelevel = 3;
//                        }
//                        stringBuilder.append(sentence);
//                        defarray.add(sentence);
//                    }
//
//                }
//                defarraylistview.setText(stringBuilder.toString());
//                if(defarray.size()<=1) {
//                    defarraylistview.setGravity(Gravity.CENTER);
//                }
//                switch (defsizelevel) {
//                    case 1:
//                        defarraylistview.setTextSize(TypedValue.COMPLEX_UNIT_PT, 7);
//                        break;
//                    case 2:
//                        defarraylistview.setTextSize(TypedValue.COMPLEX_UNIT_PT, 9);
//                        break;
//                    case 3:
//                        defarraylistview.setTextSize(TypedValue.COMPLEX_UNIT_PT, 10);
//                        break;
//                    case 4:
//                        defarraylistview.setTextSize(TypedValue.COMPLEX_UNIT_PT, 12);
//                        break;
//                    default:
//                        defarraylistview.setTextSize(TypedValue.COMPLEX_UNIT_PT, 7);
//                        break;
//                }
//            }
//
//
//            /** SET THE INITIAL VISIBILITY */
//            switch (frontValue) {
//                case "Definition":
//                    textMain.setVisibility(View.GONE);
//                    textFurigana.setVisibility(View.INVISIBLE);
//                    defarraylistview.setVisibility(View.VISIBLE);
//
//                    break;
//                case "Kanji":
//                    textMain.setVisibility(View.VISIBLE);
//                    textFurigana.setVisibility(View.INVISIBLE);
//                    defarraylistview.setVisibility(View.GONE);
//                    break;
//
//                default:
//                    if(debug){Log.d(TAG, "doing default...");}
//                    textMain.setVisibility(View.VISIBLE);
//                    textFurigana.setVisibility(View.INVISIBLE);
//                    defarraylistview.setVisibility(View.GONE);
//                    break;
//            }
//
//
//            container.addView(page, 0);
//            return page;
//        }
//
//
//
//        @Override
//        public boolean isViewFromObject(View arg0, Object arg1) {
//            return arg0== arg1;
//        }
//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object) {
//            container.removeView((View) object);
//            object=null;
//        }
//
//    }


}
