package com.jukuproject.jukutweet.Fragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
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
import com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.WordEntryFavoritesChangedListener;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Displays a deck of flashcards that user can swipe through, corresponding to a list of WordEntry ({@link WordEntry}) objects
 */

public class FlashCardsFragment extends Fragment implements WordEntryFavoritesChangedListener {

    String TAG = "Test-FlashCards";

    ViewPager vp;	//Reference to class to swipe views
    ArrayList<WordEntry> mDataset;
    FloatingActionButton fab_shuffle;
    FragmentInteractionListener mCallback;

    boolean mFrontShowing;
    String mFrontValue;
    String mBackValue;
    View page;
    private GestureDetector mGestureDetector;
    int totalCardCount; //total count of cards in stack
    int currentPosition = 0; //current position count in stack
    int cardNumber = 1; //visible card number

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
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (FragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement FragmentInteractionListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {
        View view  = LayoutInflater.from(getActivity()).inflate(R.layout.flashcard, null);
        vp=(ViewPager) view.findViewById(R.id.viewPager);
        fab_shuffle = (FloatingActionButton) view.findViewById(R.id.fab);
        page = LayoutInflater.from(getActivity()).inflate(R.layout.flashcard_item, null);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState == null) {
            mDataset = getArguments().getParcelableArrayList("wordEntries");
            mFrontValue = getArguments().getString("frontValue");
            mBackValue = getArguments().getString("backValue");
            currentPosition = 0;

        } else {
            mDataset = savedInstanceState.getParcelableArrayList("mDataset");
            currentPosition = savedInstanceState.getInt("currentPosition");
            mFrontValue = savedInstanceState.getString("mFrontValue");
            mBackValue = savedInstanceState.getString("mBackValue");
            mFrontShowing = savedInstanceState.getBoolean("mFrontShowing");
            cardNumber = savedInstanceState.getInt("cardNumber");
        }
//        mCallback.showFab(false);
        totalCardCount = mDataset.size();

        vp.setAdapter(new MyPagesAdapter_Array());

        //When shuffle button is clicked, shuffles dataset, animates cards to simulate a "shuffle", and sets card count to 0
        fab_shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mDataset.size() > 0) {
                    Collections.shuffle(mDataset);
                    cardNumber = 1;
                    currentPosition = 0;
                    page.setTag(mDataset.get(0).getId());
                    vp.setAdapter(new MyPagesAdapter_Array());
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

                    String stringcount = cardNumber + "/" + totalCardCount;
                    ((TextView) page.findViewById(R.id.scorecount)).setText(stringcount);
                }
            }
        });


        final GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {


            //Shows the reverse side of the card
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
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                WordDetailPopupDialog wordDetailPopupDialog = WordDetailPopupDialog.newInstance(mDataset.get(currentPosition));
                wordDetailPopupDialog.setTargetFragment(FlashCardsFragment.this, 0);
                wordDetailPopupDialog.show(getFragmentManager(),"wordDetailPopup");
//                WordDetailPopupDialog.newWordListInstance(mDataset.get(currentPosition)).show(getFragmentManager(),"wordDetailPopup");
            };


            //If the Kanji entry is showing on the card, a single tap shows the Kana below the kanji
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


        mGestureDetector = new GestureDetector(getContext(), listener);
        mGestureDetector.setOnDoubleTapListener(listener);



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

            ((TextView) page.findViewById(R.id.scorecount)).setText(cardNumber + "/" + totalCardCount);

            //Set the flipped tag false to start with
            page.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, final MotionEvent event) {
                    currentPosition = position;
                    mGestureDetector.onTouchEvent(event);
                    return true;
                }
            });
//
//            if (freshdeck) {
//                ((TextView) page.findViewById(R.id.scorecount)).setText(cardNumber + "/" + totalCardCount);
//                freshdeck = false;
//            }

            Log.d(TAG,"Setting card on instatiate");
            setCard(wordEntry,true);


            vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageSelected(int pos) {
//                    if(freshdeck) {
//                        cardNumber = pos+1;
//                    } else {
                        cardNumber = pos+1;
//                    }
                    ((TextView) page.findViewById(R.id.scorecount)).setText(cardNumber + "/" + totalCardCount);
                    if(vp.findViewWithTag(wordEntry.getId()) != null) {
                    setCard(wordEntry,true);
                    }
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

//        if(vp == null) {
//            Log.d(TAG,"VP IS NULL ");
//        } else {
//            Log.d(TAG,"vp view with tag (" + currentTag + ") null: " + ((vp.findViewWithTag(currentTag)) == null));
//        }

        try {
            ((TextView) (vp.findViewWithTag(currentTag)).findViewById(R.id.scorecount)).setText(cardNumber + "/" + totalCardCount);
            TextView textMain = (TextView) (vp.findViewWithTag(currentTag)).findViewById(R.id.textMessage);
            TextView textFurigana = (TextView) (vp.findViewWithTag(currentTag)).findViewById(R.id.furigana);
            TextView defarraylistview = (TextView) (vp.findViewWithTag(currentTag)).findViewById(R.id.flashcard_listview);

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

                    String definition = wordEntry.getFlashCardDefinitionMultiLineString(6);
                    textFurigana.setVisibility(View.GONE);

                    if(wordEntry.getDefinition().contains("(2)")) {
                        textMain.setVisibility(View.GONE);
                        defarraylistview.setVisibility(View.VISIBLE);
                        defarraylistview.setText(definition);


                        setTextHeightLoop(getContext(),defarraylistview,definition,getResources().getDisplayMetrics());
                    } else {
                        textMain.setVisibility(View.VISIBLE);
                        defarraylistview.setVisibility(View.GONE);
                        textMain.setText(definition);
                        setTextHeightLoop(getContext(),textMain,definition,getResources().getDisplayMetrics());
                    }


                    break;
                default:
                    break;
            }
        } catch (NullPointerException e) {
            Log.e(TAG,"Setting flashcard nullpointer: " + e);
        }



    }


    public static void setTextHeightLoop(Context context
            , TextView textView
            , String text
            , DisplayMetrics metrics) {
        Rect bounds = new Rect();
        Paint textPaint = textView.getPaint();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        int width = bounds.width();
        int height = bounds.height();

        int pxTextDP = 46;
        int  pxText = (int) (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, pxTextDP, metrics));
        int  pxCardSize = (int) (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 400, metrics));


//        int measuredTextHeight = getHeight(context, text, pxText, 300, padding);
        if(BuildConfig.DEBUG) {
            Log.d("TEST","BOUNDS WIDTH: " + width);
            Log.d("TEST","BOUNDS HEIGHT: " + height);
            Log.d("TEST","MEASURE TEXT WIDTH: " + Math.round(textPaint.measureText(text)));
            Log.d("TEST","SPECIAL MEASURED TEXT HEIGHT: " + method1UsingTextPaintAndStaticLayout(text,pxText,300,4));
//                    Log.d(TAG,"SPECIAL 2 MEASURED TEXT HEIGHT: " + measuredTextHeight);
            Log.d("TEST","pxCardSize: " + pxCardSize);
            Log.d("TEST","SETTING DEFINITION");

        }
        int specialMethodTextHeight = method1UsingTextPaintAndStaticLayout(text,pxText,300,4);

        while (specialMethodTextHeight > pxCardSize && pxTextDP>22) {

            pxTextDP -= 2;
            Log.d("TEST","specialMethodTextHeight OVERRUN. " + specialMethodTextHeight + " > " + pxCardSize + ", lowering to: " + pxTextDP );

            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, pxTextDP);
            pxText = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP
                    , pxTextDP
                    , metrics));
            specialMethodTextHeight = method1UsingTextPaintAndStaticLayout(text,pxText,300,4);

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

//    public static int getHeight(Context context, CharSequence text, int textSize, int deviceWidth,  int padding) {
//        TextView textView = new TextView(context);
//        textView.setPadding(padding,0,padding,padding);
//        textView.setText(text, TextView.BufferType.SPANNABLE);
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
//        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
//        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        textView.measure(widthMeasureSpec, heightMeasureSpec);
//        return textView.getMeasuredHeight();
//    }

    public void updateWordEntryItemFavorites(WordEntry wordEntry) {
        if(mDataset.contains(wordEntry)) {
            mDataset.get(mDataset.indexOf(wordEntry)).setItemFavorites(wordEntry.getItemFavorites());
        } else {
            Log.e(TAG,"Dataset doesn't contain word entry y'all...");
        }

    }
    public void updateWordEntryItemFavorites(ArrayList<WordEntry> updatedWordEntries) {
        if(mDataset!=null ) {
            for(WordEntry dataSetWordEntry : mDataset) {
                for(WordEntry updatedWordEntry : updatedWordEntries ) {
                    if(dataSetWordEntry.getId().equals(updatedWordEntry.getId())) {
                        dataSetWordEntry.setItemFavorites(updatedWordEntry.getItemFavorites());
                    }
                }
            }
        }

    }
    //        }


    public void updateWordEntryFavoritesForOtherTabs(WordEntry wordEntry) {
        mCallback.notifySavedWordFragmentsChanged(wordEntry);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("mDataset", mDataset);
        outState.putInt("currentPosition", currentPosition);
        outState.putString("mFrontValue", mFrontValue);
        outState.putString("mBackValue", mBackValue);
        outState.putBoolean("mFrontShowing",mFrontShowing);
        outState.putInt("cardNumber", cardNumber);

    }
}
