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
import com.jukuproject.jukutweet.MainActivity;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Displays a deck of flashcards that user can swipe through, corresponding to a list of WordEntry ({@link WordEntry}) objects.
 * Double-tap or swipe up/down to show "reverse side" of card. Long click to show {@link WordDetailPopupDialog} for card entry
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

                    Animation slide_in_left_again = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right);
                    vp.startAnimation(slide_in_left_again);

                    animator_rightin.setTarget(vp);
                    animator_rightin.start();
                    animator_rightin.setStartDelay(100);
                    animator_rightin.start();
                    ((TextView) page.findViewById(R.id.scorecount)).setText(getString(R.string.score,cardNumber,totalCardCount));
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
            }


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

            //Fling up or down shows reverse side of card, same as double tap
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

            ((TextView) page.findViewById(R.id.scorecount)).setText(getString(R.string.score,cardNumber,totalCardCount));

            //Set the flipped tag false to start with
            page.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, final MotionEvent event) {
                    currentPosition = position;
                    mGestureDetector.onTouchEvent(event);
                    return true;
                }
            });

            setCard(wordEntry,true);
            vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageSelected(int pos) {
                    cardNumber = pos+1;
                    ((TextView) page.findViewById(R.id.scorecount)).setText(getString(R.string.score,cardNumber,totalCardCount));
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
        }

    }

    /**
     * Sets up the next card in the deck when user swipes forward/backwards. Each "face" of a card has
     * three options: 1. show Kanji with hidden furigana (that will show with a single click) 2. Show Furigana only
     * 3. Show definition only
     * @param wordEntry WordEntry for this card
     * @param frontShowing whether or not the front "face" of the card is showing. true for showing, false for back "face" showing.
     */
    public void setCard(WordEntry wordEntry, boolean frontShowing) {
        if(BuildConfig.DEBUG){Log.d(TAG,"SETTING CARD: flipped to front? - " + frontShowing);}
        mFrontShowing = frontShowing;
        String cardValue;
        if(frontShowing) {
            cardValue = mFrontValue;
        } else {
            cardValue = mBackValue;
        }

        int currentTag = wordEntry.getId();

        try {
            ((TextView) (vp.findViewWithTag(currentTag)).findViewById(R.id.scorecount)).setText(getString(R.string.score,cardNumber,totalCardCount));
            TextView textMain = (TextView) (vp.findViewWithTag(currentTag)).findViewById(R.id.textMessage);
            TextView textFurigana = (TextView) (vp.findViewWithTag(currentTag)).findViewById(R.id.furigana);
            TextView defarraylistview = (TextView) (vp.findViewWithTag(currentTag)).findViewById(R.id.flashcard_listview);

            switch (cardValue) {
                case "Kanji":
                    textMain.setVisibility(View.VISIBLE);
                    textFurigana.setVisibility(View.INVISIBLE);
                    defarraylistview.setVisibility(View.GONE);

                    textMain.setText(wordEntry.getKanji());
                    textFurigana.setText(wordEntry.getFurigana());
                    break;
                case "Kana":
                    textMain.setVisibility(View.VISIBLE);
                    textFurigana.setVisibility(View.INVISIBLE);
                    defarraylistview.setVisibility(View.GONE);

                    if(wordEntry.getFurigana() == null || wordEntry.getFurigana().length() == 0) { //If the furigana entry is null, use the kanji one homie
                        textMain.setText(wordEntry.getKanji());
                    } else {
                        textMain.setText(wordEntry.getFurigana());
                    }

                    break;
                case "Definition":

                    String definition = wordEntry.getFlashCardDefinitionMultiLineString(6);
                    textFurigana.setVisibility(View.GONE);

                    if(wordEntry.getDefinition().contains("(2)")) {
                        textMain.setVisibility(View.GONE);
                        defarraylistview.setVisibility(View.VISIBLE);
                        defarraylistview.setText(definition);


                        setTextHeightLoop(defarraylistview
                                ,definition
                                ,getResources().getDisplayMetrics()
                                ,getResources().getDimension(R.dimen.flashcard_width)
                                ,getResources().getDimension(R.dimen.flashcard_height));
                    } else {
                        textMain.setVisibility(View.VISIBLE);
                        defarraylistview.setVisibility(View.GONE);
                        textMain.setText(definition);
                        setTextHeightLoop(textMain
                                ,definition
                                ,getResources().getDisplayMetrics()
                                ,getResources().getDimension(R.dimen.flashcard_width)
                                ,getResources().getDimension(R.dimen.flashcard_height));
                    }


                    break;
                default:
                    break;
            }
        } catch (NullPointerException e) {
            Log.e(TAG,"Setting flashcard nullpointer: " + e);
        }

    }

    /**
     * Measures text height for the contents of a flashcard, and if the height is greater than the bounds
     * of the card, reduces text size by 2 points and tries again in a loop until the text fits
     * @param textView textView container for flashcard (that text must fit in to)
     * @param text text that is being fit into container
     * @param metrics display metrics
     */
    public static void setTextHeightLoop(TextView textView
            , String text
            , DisplayMetrics metrics
            , float flashcardWidth
            , float flashcardHeight
    ) {
        Rect bounds = new Rect();
        Paint textPaint = textView.getPaint();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        int width = bounds.width();
        int height = bounds.height();

        int pxTextDP = 46;
        int  pxText = (int) (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, pxTextDP, metrics));
        int  pxCardSize = (int) (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, flashcardHeight, metrics));

        if(BuildConfig.DEBUG) {
            Log.d("TEST-txtheight","BOUNDS WIDTH: " + width);
            Log.d("TEST-txtheight","BOUNDS HEIGHT: " + height);
            Log.d("TEST-txtheight","MEASURE TEXT WIDTH: " + Math.round(textPaint.measureText(text)));
            Log.d("TEST-txtheight","SPECIAL MEASURED TEXT HEIGHT: " + estimateHeightOfTextView(text,pxText,(int)flashcardWidth,4));
            Log.d("TEST-txtheight","pxCardSize: " + pxCardSize);
            Log.d("TEST-txtheight","SETTING DEFINITION");
        }

        int specialMethodTextHeight = estimateHeightOfTextView(text,pxText,(int)flashcardWidth,4);
        while (specialMethodTextHeight > pxCardSize && pxTextDP>22) {

            pxTextDP -= 2;
            if(BuildConfig.DEBUG){Log.d("TEST-txtheight","specialMethodTextHeight OVERRUN. " + specialMethodTextHeight + " > " + pxCardSize + ", lowering to: " + pxTextDP );}

            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, pxTextDP);
            pxText = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP
                    , pxTextDP
                    , metrics));
            specialMethodTextHeight = estimateHeightOfTextView(text,pxText,(int)flashcardWidth,4);

        }
    }

    /**
     * Method for measuring prospective height of a string of text
     * @param text text that is being measured
     * @param textSize text size
     * @param deviceWidth width of device
     * @param padding any padding
     * @return estimated height of textview with text inside
     */
    public static int estimateHeightOfTextView(
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
//        float spacingAddition = padding;
        boolean includePadding = padding != 0;
        StaticLayout myStaticLayout = new StaticLayout(text, myTextPaint, deviceWidth, alignment, spacingMultiplier, padding, includePadding);
        return myStaticLayout.getHeight();
    }


    /**
     * If a word entry has been saved to a new word list in the {@link WordDetailPopupDialog}, the message is relayed back to
     * this method, which updates the  {@link com.jukuproject.jukutweet.Models.ItemFavorites} in the dataset to reflect the change
     * @param wordEntry WordEntry that was added to/removed from a new list
     */
    public void updateWordEntryItemFavorites(WordEntry wordEntry) {
        if(mDataset.contains(wordEntry)) {
            mDataset.get(mDataset.indexOf(wordEntry)).setItemFavorites(wordEntry.getItemFavorites());
        } else {
            Log.e(TAG,"Dataset doesn't contain word entry y'all...");
        }
    }

    /**
     * If a group of word entries has been saved in another fragment, the message relayed to {@link MainActivity#notifySavedWordFragmentsChanged(String)}
     * , which then notifies any open tabs in the other fragments which might be affected by the new word that the change
     * has been made. This method recieves the udpdated list of words and cycles through them, looking for matches. If a match
     * is found, the {@link com.jukuproject.jukutweet.Models.ItemFavorites} object for the word is updated to reflect the new favorite list/s
     * @param updatedWordEntries ArrayList of WordEntries that were saved to/removed from a new list
     */
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

    /**
     * If a word has been saved in the {@link WordDetailPopupDialog}, a message is passed
     * from worddetail to this fragment, and then back to {@link MainActivity#notifySavedWordFragmentsChanged(String)}
     * notifying to updated any fragments affected by the new word
     * @param wordEntry WordEntry that was saved
     */
    public void updateWordEntryFavoritesForOtherTabs(WordEntry wordEntry) {
        mCallback.notifySavedWordFragmentsChanged(String.valueOf(wordEntry.getId()));
    }

    /**
     * If a tweet has been saved in the {@link WordDetailPopupDialog}, a message is passed
     * from worddetail to this fragment, and then back to {@link MainActivity#notifySavedTweetFragmentsChanged()}
     * notifying to updated any fragments affected by the new tweet
     */
    public void notifySavedTweetFragmentsChanged(){
        mCallback.notifySavedTweetFragmentsChanged();
    }

    /**
     * If a tweet has been saved in {@link WordDetailPopupDialog}, and the user for that tweet
     * is not saved in the db (which therefore means the user's icon is not saved in the db), this passes
     * on the message to save the icon from the {@link com.jukuproject.jukutweet.Adapters.UserTimeLineAdapter} to the Activity,
     * which uses {@link com.jukuproject.jukutweet.Database.UserOpsHelper#downloadTweetUserIcon(Context, String, String)} in a
     * subscription to download the icon
     * @param userInfo UserInfo of user whose icon will be downloaded
     */
    public void downloadTweetUserIcons(UserInfo userInfo) {
        mCallback.downloadTweetUserIcons(userInfo);
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
