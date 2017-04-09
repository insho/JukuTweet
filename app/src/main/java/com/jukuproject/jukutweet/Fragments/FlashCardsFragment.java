package com.jukuproject.jukutweet.Fragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;
import java.util.Collections;

import static android.R.attr.padding;

/**
 * Created by JClassic on 3/31/2017.
 */

public class FlashCardsFragment extends Fragment {

    String TAG = "Test-FlashCards";

    ViewPager vp;	//Reference to class to swipe views
    ArrayList<WordEntry> mDataset;

    int currentPosition = 0;
    boolean mFrontShowing;
    boolean freshdeck;
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

    MyPagesAdapter_Array mAdapter;


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
                             final ViewGroup container, Bundle savedInstanceState) {
        View view  = LayoutInflater.from(getActivity()).inflate(R.layout.flashcard, null);

        mDataset = getArguments().getParcelableArrayList("wordEntries");
        mFrontValue = getArguments().getString("frontValue");
        mBackValue = getArguments().getString("backValue");
        totalcount = mDataset.size();
        page = LayoutInflater.from(getActivity()).inflate(R.layout.flashcard_item, null);

        page.setTag(mDataset.get(0).getId());
        vp=(ViewPager) view.findViewById(R.id.viewPager);
        currentPosition = 0;
//        mAdapter =
        vp.setAdapter(new MyPagesAdapter_Array());

        FloatingActionButton fab_shuffle = (FloatingActionButton) view.findViewById(R.id.fab);
        fab_shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Collections.shuffle(mDataset);
                vp.setAdapter(new MyPagesAdapter_Array());
                freshdeck = true;
                mFrontShowing = true; //toggle the boolean to reflect current flipped state

                animator_rightin = AnimatorInflater.loadAnimator(getContext(),
                        R.animator.card_flip_right_in);
                animator_rightout = AnimatorInflater.loadAnimator(getContext(),
                        R.animator.card_flip_right_out);
                animator_leftin = AnimatorInflater.loadAnimator(getContext(),
                        R.animator.card_flip_left_in);
                animator_leftout = AnimatorInflater.loadAnimator(getContext(),
                        R.animator.card_flip_left_out);

                Animation slide_in_left_again = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right);
                vp.startAnimation(slide_in_left_again);

                animator_rightin.setTarget(vp);
                animator_rightin.start();
                animator_rightin.setStartDelay(100);
                animator_rightin.start();

                /**  THIS IS THE ACTION BUTTON SHUFFLE BLOCK*/
                //Reset the currentcount to 1 (since it's a new deck now)
                currentcount = 1;
                freshdeck =true;
                String stringcount = currentcount + "/" + totalcount;
                ((TextView) page.findViewById(R.id.scorecount)).setText(stringcount);
                page.setTag(mDataset.get(0).getId());
                setCard(mDataset.get(0),true);

            }
        });


        final GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {


            @Override
            public boolean onDoubleTap(MotionEvent e) {

                animator_upin = AnimatorInflater.loadAnimator(getContext(),
                        R.animator.card_flip_up_in);
                animator_upin.setTarget(vp);
                animator_upin.start();

                WordEntry wordEntry = mDataset.get(currentPosition);
                if(BuildConfig.DEBUG){Log.d(TAG, "find view with tag: " + wordEntry.getId());}


                setCard(wordEntry,!mFrontShowing);
                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "doubletap flipped: " + mFrontShowing);
                    Log.d(TAG, "doubletap frontValue: " + mFrontValue);
                    Log.d(TAG, "doubletap backValue: " + mBackValue);
                }


                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                //toggle the boolean to reflect current flipped state
                if(BuildConfig.DEBUG){Log.d(TAG,"Singletap flipped: "+ mFrontShowing);}

                if(mFrontShowing) {
                    final WordEntry wordEntry = mDataset.get(currentPosition);
                    int currentTag = wordEntry.getId();
                    if((vp.findViewWithTag(currentTag)).findViewById(R.id.furigana).getVisibility() == View.VISIBLE) {
                        if(BuildConfig.DEBUG){Log.d(TAG, "Singletap furigana INVISIBLE");}
                        (vp.findViewWithTag(currentTag)).findViewById(R.id.furigana).setVisibility(View.INVISIBLE);
                    } else {
                        if(BuildConfig.DEBUG){Log.d(TAG,"Singletap furigana VISIBLE");}
                        (vp.findViewWithTag(currentTag)).findViewById(R.id.furigana).setVisibility(View.VISIBLE);
                    }
                }
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if(BuildConfig.DEBUG){Log.d(TAG, "onFling " + e1.getX() + " - " + e2.getX());}

                if (e1.getY() < e2.getY()) {

                    animator_upin = AnimatorInflater.loadAnimator(getContext(),
                            R.animator.card_flip_up_in);
                    animator_upin.setTarget(vp);
                    animator_upin.start();


                    //toggle the boolean to reflect current flipped state
                    setCard(mDataset.get(currentPosition),!mFrontShowing);

                    if(BuildConfig.DEBUG) {
                        Log.d(TAG, "fling flipped: " + mFrontShowing);
                        Log.d(TAG, "fling frontValue: " + mFrontValue);
                        Log.d(TAG, "fling backValue: " + mBackValue);
                    }

                }

                if (e1.getY() > e2.getY()) {

                    animator_upin = AnimatorInflater.loadAnimator(getContext(),
                            R.animator.card_flip_up_in);
                    animator_upin.setTarget(vp);
                    animator_upin.start();

                    //toggle the boolean to reflect current flipped state
                    if(BuildConfig.DEBUG) {
                        Log.d(TAG, "zdoubletap flipped: " + mFrontShowing);
                        Log.d(TAG, "zdoubletap frontValue: " + mFrontValue);
                        Log.d(TAG, "zdoubletap backValue: " + mBackValue);
                    }

                    setCard(mDataset.get(currentPosition),!mFrontShowing);

                }

                return true;
            }
        };



//        final GestureDetector detector = new GestureDetector(listener);
//
//        detector.setOnDoubleTapListener(listener);
//        detector.setIsLongpressEnabled(true);

//        getActivity().getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                return detector.onTouchEvent(event);
//            }
//        });

        mGestureDetector = new GestureDetector(getContext(), listener);
        mGestureDetector.setOnDoubleTapListener(listener);


        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

   // Implement PagerAdapter Class to handle individual page creation
    class MyPagesAdapter_Array extends PagerAdapter {

        @Override
        public int getCount() {
            //Return total pages, here one for each data item
            return mDataset.size();
        }

       @Override
       public int getItemPosition(Object object) {
           return super.getItemPosition(object);
       }

       //Create the given page (indicated by position)
        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            page = LayoutInflater.from(getActivity()).inflate(R.layout.flashcard_item, null);
            container.addView(page, 0);
            final WordEntry wordEntry = mDataset.get(position);
            page.setTag(wordEntry.getId());

            /* THIS ONE RUNS IF YOU HIT THE SHUFFLE FAB*/
            currentcount=1;
            ((TextView) page.findViewById(R.id.scorecount)).setText(currentcount + "/" + totalcount);

            //Set the flipped tag false to start with
            page.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, final MotionEvent event) {
                    currentPosition = position;
                    mGestureDetector.onTouchEvent(event);
                    return true;
                }
            });

            if (freshdeck) {
                ((TextView) page.findViewById(R.id.scorecount)).setText(currentcount + "/" + totalcount);
                freshdeck = false;
            }
            setCard(wordEntry,true);


            vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageSelected(int pos) {
                    if(freshdeck) {
                        currentcount = pos+1;
                    } else {
                        currentcount = pos+1;
                    }

                    setCard(wordEntry,true);

                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {

                }

                @Override
                public void onPageScrollStateChanged(int arg0) {
                }


            });


            return page;
        }



        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0== arg1;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            object=null;
        }

    }

    public void setCard(WordEntry wordEntry, boolean frontShowing) {
        Log.d(TAG,"SETTING CARD: flipped to front? - " + frontShowing);
        mFrontShowing = frontShowing;
        String cardValue;
        if(frontShowing) {
            cardValue = mFrontValue;
        } else {
            cardValue = mBackValue;
        }

        int currentTag = wordEntry.getId();

        if(vp == null) {
            Log.d(TAG,"VP IS NULL ");
        } else {
            Log.d(TAG,"vp view with tag (" + currentTag + ") null: " + ((vp.findViewWithTag(currentTag)) == null));
        }


//        ((TextView) (vp.findViewWithTag(currentTag)).findViewById(R.id.scorecount)).setText(currentcount + "/" + totalcount);
        TextView textMain = (TextView) (vp.findViewWithTag(currentTag)).findViewById(R.id.textMessage);
        TextView textFurigana = (TextView) (vp.findViewWithTag(currentTag)).findViewById(R.id.furigana);
        TextView defarraylistview = (TextView) (vp.findViewWithTag(currentTag)).findViewById(R.id.flashcard_listview);


        /* SET THE Kanji/Furigana boxes */
        switch (cardValue) {
            case "Kanji":
                //Show kanji textview, hide definition listview
                textMain.setVisibility(View.VISIBLE);
                textFurigana.setVisibility(View.INVISIBLE);
                defarraylistview.setVisibility(View.GONE);

                textMain.setText(wordEntry.getKanji());
                textFurigana.setText(wordEntry.getFurigana());
                Log.d(TAG,"KANJI VISIBLE");
                break;
            case "Kana":
                textMain.setVisibility(View.VISIBLE);
                textFurigana.setVisibility(View.INVISIBLE);
                defarraylistview.setVisibility(View.GONE);

                textFurigana.setVisibility(TextView.INVISIBLE);
                if(wordEntry.getFurigana() == null || wordEntry.getFurigana().length() == 0) { //If the furigana entry is null, use the kanji one homie
                    if(BuildConfig.DEBUG){ Log.d(TAG,"we're doing this (2)");}
                    textMain.setText(wordEntry.getKanji());
                } else {
                    Log.d(TAG,"SETTING FURIGANA");
                    textMain.setText(wordEntry.getFurigana());
                }
                Log.d(TAG,"KANA VISIBLE");

                break;
            case "Definition":
                textMain.setVisibility(View.GONE);
                textFurigana.setVisibility(View.GONE);
                defarraylistview.setVisibility(View.VISIBLE);


                String definition = wordEntry.getDefinitionMultiLineString(6);
                defarraylistview.setText(definition);

                Rect bounds = new Rect();
                Paint textPaint = defarraylistview.getPaint();
                textPaint.getTextBounds(definition, 0, definition.length(), bounds);
                int width = bounds.width();
                int height = bounds.height();

                int  px = (int) (TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 22, getResources().getDisplayMetrics()));

                int measuredTextHeight = getHeight(getContext(), definition, 22, 300, padding);

                if(BuildConfig.DEBUG) {
                    Log.d(TAG,"BOUNDS WIDTH: " + width);
                    Log.d(TAG,"BOUNDS HEIGHT: " + height);
                    Log.d(TAG,"MEASURE TEXT WIDTH: " + Math.round(textPaint.measureText(definition)));
                    Log.d(TAG,"SPECIAL MEASURED TEXT HEIGHT: " + method1UsingTextPaintAndStaticLayout(definition,px,300,4));
                    Log.d(TAG,"SPECIAL 2 MEASURED TEXT HEIGHT: " + measuredTextHeight);
                    Log.d(TAG,"SETTING DEFINITION");
                }
                break;
            default:
                break;
        }


    }


    public static int method1UsingTextPaintAndStaticLayout(
            final CharSequence text,
            final int textSize, // in pixels
            final int deviceWidth, // in pixels
            final int padding // in pixels
    ) {

        TextPaint myTextPaint = new TextPaint();
        myTextPaint.setAntiAlias(true);
        myTextPaint.setTextSize(textSize);
        Layout.Alignment alignment = Layout.Alignment.ALIGN_NORMAL;
        float spacingMultiplier = 1;
        float spacingAddition = padding; // optionally apply padding here
        boolean includePadding = padding != 0;
        StaticLayout myStaticLayout = new StaticLayout(text, myTextPaint, deviceWidth, alignment, spacingMultiplier, spacingAddition, includePadding);
        return myStaticLayout.getHeight();
    }

    public static int getHeight(Context context, CharSequence text, int textSize, int deviceWidth,  int padding) {
        TextView textView = new TextView(context);
        textView.setPadding(padding,0,padding,padding);
        textView.setText(text, TextView.BufferType.SPANNABLE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        return textView.getMeasuredHeight();
    }
}
