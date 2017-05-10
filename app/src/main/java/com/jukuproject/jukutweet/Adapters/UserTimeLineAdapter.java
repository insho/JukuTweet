package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jukuproject.jukutweet.ChooseFavoriteListsPopupWindow;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.FavoritesColors;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Interfaces.TweetListOperationsInterface;
import com.jukuproject.jukutweet.LongClickLinkMovementMethod;
import com.jukuproject.jukutweet.LongClickableSpan;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetUrl;
import com.jukuproject.jukutweet.Models.TweetUserMentions;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

/**
 *Adapter for UserTimeLineFragment, displays a list of users tweets drawn from the Twitter API {@link com.jukuproject.jukutweet.TwitterUserClient}.
 * Click on a tweet to be taken to the {@link com.jukuproject.jukutweet.Fragments.TweetBreakDownFragment}. User can also click on the favorites
 * star in the tweet to save it (via the saveandparsetweet method in {@link com.jukuproject.jukutweet.MainActivity}).
 *
 * @see com.jukuproject.jukutweet.Fragments.UserTimeLineFragment
 */
public class UserTimeLineAdapter extends RecyclerView.Adapter<UserTimeLineAdapter.ViewHolder> {

    private static final String TAG = "TEST-timeadapter";
    private RxBus _rxbus;
//    private UserInfo mUserInfo;
    private List<Tweet> mDataset;
    private Context mContext;
    private DisplayMetrics mMetrics;
    private ArrayList<String> mActiveTweetFavoriteStars;
    private String mFocusedWord; //used only for search adapter, to highight the word that is being searched , otherwise null
    private Boolean mShowStar;

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView txtTweet;
        private TextView txtCreated;
        private TextView txtFavorited;
        private TextView txtReTweeted;
        private TextView txtUserName;
        private TextView txtUserScreenName;
        private ImageButton imgStar;
        private FrameLayout imgStarLayout;
        private ImageButton imgReTweeted;
        private ImageButton imgFavorited;

        public ViewHolder(View v) {
            super(v);
            txtTweet = (TextView) v.findViewById(R.id.tweet);
            txtCreated = (TextView) v.findViewById(R.id.createdAt);
            txtFavorited = (TextView) v.findViewById(R.id.favorited);
            txtReTweeted = (TextView) v.findViewById(R.id.retweeted);
            imgReTweeted = (ImageButton) v.findViewById(R.id.retweetImage);
            imgFavorited = (ImageButton) v.findViewById(R.id.heart);
            txtUserName = (TextView) v.findViewById(R.id.timelineName);
            txtUserScreenName = (TextView) v.findViewById(R.id.timelineDisplayScreenName);
            imgStar = (ImageButton) v.findViewById(R.id.favorite);
            imgStarLayout = (FrameLayout) v.findViewById(R.id.timelineStarLayout);


        }
    }

    public UserTimeLineAdapter(Context context
            , RxBus rxBus
//            , UserInfo userInfo
            , List<Tweet> myDataset
            , ArrayList<String> activeTweetFavoriteStars
                      ,DisplayMetrics metrics
            , @Nullable String focusedWord
            , Boolean showStar) {
        mContext = context;
        _rxbus = rxBus;
        mDataset = myDataset;
        mFocusedWord = focusedWord;
        mActiveTweetFavoriteStars = activeTweetFavoriteStars;
        mMetrics = metrics;
        mShowStar = showStar;
    }


    @Override
    public UserTimeLineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.usertimeline_recycler_row, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.txtUserName.setText(mDataset.get(holder.getAdapterPosition()).getUser().getName());
        holder.txtUserScreenName.setText(mDataset.get(holder.getAdapterPosition()).getUser().getDisplayScreenName());







        if(mShowStar) {
            /* Insert tweet metadata if it exists*/
            holder.txtCreated.setText(getTweet(holder.getAdapterPosition()).getDatabaseInsertDate());
            holder.txtReTweeted.setText(getTweet(holder.getAdapterPosition()).getRetweetCountString());
            if(getTweet(position).getFavorited() != null){
                holder.txtFavorited.setText(getTweet(holder.getAdapterPosition()).getFavoritesCountString());
            }

            holder.imgFavorited.setVisibility(View.VISIBLE);
            holder.imgReTweeted.setVisibility(View.VISIBLE);
            holder.txtReTweeted.setVisibility(View.VISIBLE);
            holder.txtFavorited.setVisibility(View.VISIBLE);

            holder.imgStarLayout.setClickable(true);
            holder.imgStarLayout.setLongClickable(true);
            holder.imgStar.setImageResource(FavoritesColors.assignStarResource(mDataset.get(holder.getAdapterPosition()).getItemFavorites(),mActiveTweetFavoriteStars));
            holder.imgStar.setColorFilter(ContextCompat.getColor(mContext, FavoritesColors.assignStarColor(mDataset.get(holder.getAdapterPosition()).getItemFavorites(),mActiveTweetFavoriteStars)));

            if(mDataset.get(holder.getAdapterPosition()).getItemFavorites().shouldOpenFavoritePopup(mActiveTweetFavoriteStars)
                    && mDataset.get(holder.getAdapterPosition()).getItemFavorites().systemListCount(mActiveTweetFavoriteStars) >1) {
                holder.imgStar.setColorFilter(null);
                holder.imgStar.setImageResource(R.drawable.ic_star_multicolor);

            } else {
                try {
                    holder.imgStar.setImageResource(R.drawable.ic_star_black);
                    holder.imgStar.setColorFilter(ContextCompat.getColor(mContext,FavoritesColors.assignStarColor(mDataset.get(holder.getAdapterPosition()).getItemFavorites(),mActiveTweetFavoriteStars)));
                } catch (NullPointerException e) {
                    Log.e(TAG,"UserTimeLineAdapter setting colorfilter nullpointer 1: " + e.getMessage());
                }

            }


            holder.imgStarLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                /* Check for, and save */
                    Tweet currentTweet = mDataset.get(holder.getAdapterPosition());
                    TweetListOperationsInterface helperTweetOps = InternalDB.getTweetInterfaceInstance(mContext);


                    if(mDataset.get(holder.getAdapterPosition()).getItemFavorites().shouldOpenFavoritePopup(mActiveTweetFavoriteStars)) {
                        showTweetFavoriteListPopupWindow(mDataset.get(holder.getAdapterPosition()),holder);
                    } else {
                        if (FavoritesColors.onFavoriteStarToggleTweet(mContext, mActiveTweetFavoriteStars, mDataset.get(holder.getAdapterPosition()).getUser().getUserId(), mDataset.get(holder.getAdapterPosition()))) {
                            holder.imgStar.setImageResource(R.drawable.ic_star_black);
                            holder.imgStar.setColorFilter(ContextCompat.getColor(mContext, FavoritesColors.assignStarColor(mDataset.get(holder.getAdapterPosition()).getItemFavorites(), mActiveTweetFavoriteStars)));
                            _rxbus.sendSaveTweet(mDataset.get(holder.getAdapterPosition()));
                        } else {
                            Log.e(TAG, "OnFavoriteStarToggle did not work...");
                        }
                    }

                    //Check for tweet in db
                    try {
                        //If tweet doesn't already exist in db, insert it
                        if(helperTweetOps.tweetExistsInDB(currentTweet) == 0){
                            //Otherwise enter the tweet into the database and then toggle
                            Log.d(TAG,"createdAt: " + currentTweet.getCreatedAt() + ", db insert: " + currentTweet.getDatabaseInsertDate());
                            int addTweetResultCode = helperTweetOps.saveTweetToDB(mDataset.get(holder.getAdapterPosition()).getUser(),currentTweet);
//                            Log.d(TAG,"addTweetResultCode: " + addTweetResultCode);
                            if(addTweetResultCode < 0) {
                                //TODO handle error -- can't insert tweet
                            } else {
                                Log.e(TAG,"saving TWEET:" + mDataset.get(holder.getAdapterPosition()).getText());
                                /*DB insert successfull, now send callback to fragment and run observable to add
                                 urls for the tweet to database, as well as parse the kanji (in observable) and add those to database */
                                _rxbus.sendSaveTweet(mDataset.get(holder.getAdapterPosition()));
                            }
                        }


                    } catch (Exception e){
                        Log.e(TAG,"UserTimeLIneAdapter - star clicked, tweet doesn't exist, but UNABLE to save!");

                    }




                }
            });



            holder.imgStarLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    showTweetFavoriteListPopupWindow(mDataset.get(holder.getAdapterPosition()),holder);

                    return true;
                }
            });
        } else {
            holder.txtCreated.setText(getTweet(position).getDisplayDate());

            holder.imgStar.setVisibility(View.INVISIBLE);
            holder.imgStarLayout.setVisibility(View.INVISIBLE);
            holder.imgFavorited.setVisibility(View.INVISIBLE);
            holder.imgReTweeted.setVisibility(View.INVISIBLE);
            holder.txtReTweeted.setVisibility(View.INVISIBLE);
            holder.txtFavorited.setVisibility(View.INVISIBLE);
        }


        try {
            SpannableString text = new SpannableString(getTweet(position).getText());
            LongClickableSpan longClick = new LongClickableSpan() {
                @Override
                public void onLongClick(View view) {
                    try {
                        _rxbus.sendLongClick(mDataset.get(holder.getAdapterPosition()).getUser());
                    } catch (Exception e) {
                        Log.e(TAG,"usertimeline adapter long click error. no user attached to tweet?");
                    }
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(ContextCompat.getColor(mContext, android.R.color.black));
                    ds.setUnderlineText(false);
                }

                @Override
                public void onClick(View widget) {
                    _rxbus.send(mDataset.get(holder.getAdapterPosition()));
                }
            };


            text.setSpan(longClick, 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            if(getTweet(position).getEntities()!= null && getTweet(position).getEntities().getUrls() != null ) {
                List<TweetUrl> tweetUrls =  getTweet(position).getEntities().getUrls();
                for(TweetUrl url : tweetUrls) {
                    int[] indices = url.getIndices();

                    String urlToLinkify = "";

                    if(getTweet(position).getText().substring(indices[0]).contains(url.getDisplay_url())) {
                        urlToLinkify = url.getDisplay_url();
                    } else if(getTweet(position).getText().substring(indices[0]).contains(url.getUrl())) {
                        urlToLinkify = url.getUrl();
                    }
                    int startingLinkPos = getTweet(position).getText().indexOf(urlToLinkify,indices[0]);

                    text.setSpan(new URLSpan(url.getUrl()), startingLinkPos, startingLinkPos + urlToLinkify.length(), 0);
                }

            }

            if(getTweet(position).getEntities() != null && getTweet(position).getEntities().getUser_mentions() != null ) {
                List<TweetUserMentions> tweetUserMentionses =  getTweet(position).getEntities().getUser_mentions();
                for(final TweetUserMentions userMentionses : tweetUserMentionses) {
                    int[] indices = userMentionses.getIndices();

                    String userMentionToLinkify = "";

                    if(getTweet(position).getText().substring(indices[0]).contains(userMentionses.getScreen_name())) {
                        userMentionToLinkify = userMentionses.getScreen_name();
                    } else if(getTweet(position).getText().substring(indices[0]).contains(userMentionses.getName())) {
                        userMentionToLinkify = userMentionses.getName();
                    }

                    int startingLinkPos = getTweet(position).getText().indexOf(userMentionToLinkify,indices[0]);
                    ClickableSpan userMentionClickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View textView) {
                            Log.d(TAG,"BALLS");
                            _rxbus.send(userMentionses);
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(ContextCompat.getColor(mContext,R.color.colorAccent));
                            ds.setAlpha(90);
                            ds.setUnderlineText(false);

                        }
                    };
                    text.setSpan(userMentionClickableSpan, startingLinkPos, startingLinkPos + userMentionToLinkify.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                }

            }

            if(getTweet(holder.getAdapterPosition()).getWordEntries()!=null){
                for(WordEntry wordEntry : getTweet(holder.getAdapterPosition()).getWordEntries()) {
                    if(wordEntry.getKanji().equals(mFocusedWord) ) {
                        try {
                            BackgroundColorSpan fcs = new BackgroundColorSpan(ContextCompat.getColor(mContext,R.color.colorJukuYellow));
                            text.setSpan(fcs, wordEntry.getStartIndex(), wordEntry.getEndIndex(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        } catch (Exception e) {
                            Log.e(TAG,"FAILED to set background of highlighted kanji");
                        }

                    }
                }
            } else if(mFocusedWord!=null && text.toString().contains(mFocusedWord)) {

                int index = 0;
                while(index<text.toString().length() && text.toString().substring(index,text.toString().length()).contains(mFocusedWord)) {
                    int startIndex = text.toString().substring(0,index).length() + text.toString().substring(index,text.toString().length()).indexOf(mFocusedWord);
                    BackgroundColorSpan fcs = new BackgroundColorSpan(ContextCompat.getColor(mContext,R.color.colorJukuYellow));
                    text.setSpan(fcs, startIndex, startIndex + mFocusedWord.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    index = startIndex + mFocusedWord.length() + 1;
                }


            } else {
                Log.e(TAG,"Is mfocused word null? "  + mFocusedWord);
            }

            holder.txtTweet.setText(text, TextView.BufferType.SPANNABLE);
            holder.txtTweet.setMovementMethod(LongClickLinkMovementMethod.getInstance());
        } catch (NullPointerException e) {
            holder.txtTweet.setText(getTweet(position).getText());
            Log.e(TAG,"mTweet urls are null : " + e);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If it's a short click, send the tweet back to UserTimeLineFragment
                _rxbus.send(mDataset.get(holder.getAdapterPosition()));
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    _rxbus.sendLongClick(mDataset.get(holder.getAdapterPosition()).getUser());
                } catch (Exception e) {
                    Log.e(TAG,"usertimeline adapter long click error. no user attached to tweet?");
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public Tweet getTweet(int position) {
        return mDataset.get(position);
    }

    public void showTweetFavoriteListPopupWindow(final Tweet mTweet, final ViewHolder holder) {

        RxBus rxBus = new RxBus();
        ArrayList<MyListEntry> availableFavoriteLists = InternalDB.getTweetInterfaceInstance(mContext).getTweetListsForTweet(mActiveTweetFavoriteStars,mTweet.getIdString(),null);

        PopupWindow popupWindow = ChooseFavoriteListsPopupWindow.createTweetFavoritesPopup(mContext,mMetrics,rxBus,availableFavoriteLists,mTweet.getIdString(), mTweet.getUser().getUserId());
//        PopupWindow popupWindow =  new ChooseFavoriteListsPopupWindow(getContext(),metrics,rxBus,availableFavoriteLists,mWords.get(holder.getAdapterPosition()).getId()).onCreateView();

        popupWindow.getContentView().measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        int xadjust = popupWindow.getContentView().getMeasuredWidth() + (int) (25 * mMetrics.density + 0.5f);
//        int yadjust = (int)((holder.imgStar.getMeasuredHeight())/2.0f);

        int yadjust;
        if(availableFavoriteLists.size()<4) {
            yadjust = (int)((popupWindow.getContentView().getMeasuredHeight()  + holder.imgStar.getMeasuredHeight())/2.0f);
        } else {
            yadjust = getYAdjustmentForPopupWindowBigTweetList(availableFavoriteLists.size(),holder.getAdapterPosition(),mMetrics.scaledDensity,holder.itemView.getMeasuredHeight());
        }


        Log.d("TEST","pop width: " + popupWindow.getContentView().getMeasuredWidth() + " height: " + popupWindow.getContentView().getMeasuredHeight());
        Log.d("TEST","xadjust: " + xadjust + ", yadjust: " + yadjust);


        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

                _rxbus.sendSaveTweet(mTweet);

            }
        });



        rxBus.toClickObserverable().subscribe(new Action1<Object>() {
            @Override
            public void call(Object event) {

                                    /* Recieve a MyListEntry (containing an updated list entry for this row kanji) from
                                    * the ChooseFavoritesAdapter in the ChooseFavorites popup window */
                if(event instanceof MyListEntry) {
                    MyListEntry myListEntry = (MyListEntry) event;

                                        /* Ascertain the type of list that the kanji was added to (or subtracted from),
                                        and update that list's count */
                    if(myListEntry.getListsSys() == 1) {
                        switch (myListEntry.getListName()) {
                            case "Blue":
                                mTweet.getItemFavorites().setSystemBlueCount(myListEntry.getSelectionLevel());
                                break;
                            case "Green":
                                mTweet.getItemFavorites().setSystemGreenCount(myListEntry.getSelectionLevel());
                                break;
                            case "Red":
                                mTweet.getItemFavorites().setSystemRedCount(myListEntry.getSelectionLevel());
                                break;
                            case "Yellow":
                                mTweet.getItemFavorites().setSystemYellowCount(myListEntry.getSelectionLevel());
                                break;
                            case "Purple":
                                mTweet.getItemFavorites().setSystemPurpleCount(myListEntry.getSelectionLevel());
                                break;
                            case "Orange":
                                mTweet.getItemFavorites().setSystemOrangeCount(myListEntry.getSelectionLevel());
                                break;
                            default:
                                break;
                        }
                    } else {
                        if(myListEntry.getSelectionLevel() == 1) {
                            mTweet.getItemFavorites().addToUserListCount(1);
                        } else {
                            mTweet.getItemFavorites().subtractFromUserListCount(1);
                        }
                    }

                    if(mTweet.getItemFavorites().shouldOpenFavoritePopup(mActiveTweetFavoriteStars)
                            && mTweet.getItemFavorites().systemListCount(mActiveTweetFavoriteStars) >1) {
                        holder.imgStar.setColorFilter(null);
                        holder.imgStar.setImageResource(R.drawable.ic_star_multicolor);

                    } else {
                        try {
                            holder.imgStar.setImageResource(R.drawable.ic_star_black);
                            holder.imgStar.setColorFilter(ContextCompat.getColor(mContext,FavoritesColors.assignStarColor(mTweet.getItemFavorites(),mActiveTweetFavoriteStars)));
                        } catch (NullPointerException e) {
                            Log.e(TAG,"UserTimeLineAdapter setting colorfilter nullpointer: " + e.getMessage());
                        }

                    }
//                    holder.imgStar.setImageResource(FavoritesColors.assignStarResource(mWords.get(holder.getAdapterPosition()).getItemFavorites(),mActiveFavoriteStars));

                }

            }

        });


        popupWindow.showAsDropDown(holder.imgStar,-xadjust,-yadjust);

    };


    public int getYAdjustmentForPopupWindowBigTweetList(int totalActiveLists
            , int adapterPosition
            ,float scale
            ,float heightOfView) {

        final int yadjustment;



        if(totalActiveLists>10) {
            totalActiveLists = 10;
        }

        int estimatedheightofpopup =  (int) ((totalActiveLists*35) * scale + 0.5f);
        int multiplier = -(mDataset.size()-adapterPosition-1);

        if((adapterPosition>(mDataset.size()-5) && adapterPosition>((float)mDataset.size()/2.0f))){

            if(totalActiveLists < 5){
                multiplier = 1;
            }

            yadjustment = estimatedheightofpopup + (int) (multiplier* ((int) ((35) * scale + 0.5f)));

//            if(BuildConfig.DEBUG){
//                Log.d(TAG,"rowsize: " + totalActiveLists);
//                Log.d(TAG,"estimatedheightofpopup: " + estimatedheightofpopup);
//                Log.d(TAG,"multiplier: " + multiplier);
//            }

        } else {

            float defmult = heightOfView;
//            if(definition.contains("(9)")){
//                defmult= 80.0f;
//            } else if(definition.contains("(6)")){
//                defmult= 50.0f;
//            } else if(definition.contains("(3)")){
//                defmult= 30.0f;
//            }

            float listizemultiplier = 0;
            switch (totalActiveLists) {
                case 0:
                    listizemultiplier = 35.0f+defmult;
                    break;
                case 1:
                    listizemultiplier = 35.0f+defmult;
                    break;
                case 2:
                    listizemultiplier = 35.0f+defmult;
                    break;
                case 3:
                    listizemultiplier = 40.0f+defmult;
                    break;
                case 4:
                    listizemultiplier = 45.0f+defmult;
                    break;

                default:
                    listizemultiplier = 95.0f+defmult;
                    break;
            }

            yadjustment =(int) ((float)listizemultiplier * scale + 0.5f);
//            if(BuildConfig.DEBUG){
//                Log.d(TAG,"listsizemultiplier: " +  listizemultiplier);
//            }
        }

        return yadjustment;
    }

}