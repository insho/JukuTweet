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

import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.FavoritesColors;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Interfaces.TweetListOperationsInterface;
import com.jukuproject.jukutweet.LongClickLinkMovementMethod;
import com.jukuproject.jukutweet.LongClickableSpan;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetUrl;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.TestPopupWindow;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

/**
 * Recycler adapter for UserTimeLineFragment, displays a list of users tweets
 */
public class UserTimeLineAdapter extends RecyclerView.Adapter<UserTimeLineAdapter.ViewHolder> {

    private static final String TAG = "TEST-timefrag";
    private RxBus _rxbus;
//    private UserInfo mUserInfo;
    private List<Tweet> mDataset;
    private Context mContext;
    private DisplayMetrics mMetrics;
    private ArrayList<String> mActiveTweetFavoriteStars;
    private String mFocusedWord; //used only for search adapter, to highight the word that is being searched , otherwise null
    private Boolean mShowStar;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtTweet;
        public TextView txtCreated;
        public TextView txtFavorited;
        public TextView txtReTweeted;
        public TextView txtUserName;
        public TextView txtUserScreenName;
        public ImageButton imgStar;
        public FrameLayout imgStarLayout;

        public ViewHolder(View v) {
            super(v);
            txtTweet = (TextView) v.findViewById(R.id.tweet);
            txtCreated = (TextView) v.findViewById(R.id.createdAt);
            txtFavorited = (TextView) v.findViewById(R.id.favorited);
            txtReTweeted = (TextView) v.findViewById(R.id.retweeted);
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



        /* Insert tweet metadata if it exists*/
        holder.txtCreated.setText(getTweet(position).getDisplayDate());
        holder.txtReTweeted.setText(getTweet(position).getRetweetCountString());


        if(getTweet(position).getFavorited() != null){
            holder.txtFavorited.setText(getTweet(position).getFavoritesCountString());
        }

        if(mShowStar) {
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
//                //Toggle favorite list association for this tweet
//                if(FavoritesColors.onFavoriteStarToggleTweet(mContext,mActiveTweetFavoriteStars,mDataset.get(holder.getAdapterPosition()).getUser().getUserId(),mDataset.get(holder.getAdapterPosition()))) {
//                    holder.imgStar.setImageResource(FavoritesColors.assignStarResource(mDataset.get(holder.getAdapterPosition()).getItemFavorites(),mActiveTweetFavoriteStars));
//                    holder.imgStar.setColorFilter(ContextCompat.getColor(mContext, FavoritesColors.assignStarColor(mDataset.get(holder.getAdapterPosition()).getItemFavorites(),mActiveTweetFavoriteStars)));
//
//                } else {
//                    //TODO insert an error?
//                    Log.e(TAG,"OnFavoriteStarToggle did not work...");
//                }



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
            holder.imgStar.setVisibility(View.INVISIBLE);
            holder.imgStarLayout.setVisibility(View.INVISIBLE);
        }


        try {
            SpannableString text = new SpannableString(getTweet(position).getText());


            LongClickableSpan longClick = new LongClickableSpan() {
                @Override
                public void onLongClick(View view) {
                    try {
                        Log.d(TAG,"LOOOONG CLICK!!!!");
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

            if(getTweet(position).getEntities() != null && getTweet(position).getEntities().getUrls() != null) {
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

//            Log.i(TAG,"focusedword: " + mFocusedWord);
//            Log.i(TAG,"text: " + text.toString());
            if(getTweet(position).getWordEntries()!=null){
                BackgroundColorSpan fcs = new BackgroundColorSpan(ContextCompat.getColor(mContext,R.color.colorJukuYellow));
                for(WordEntry wordEntry : getTweet(position).getWordEntries()) {
                    if(wordEntry.getKanji().equals(mFocusedWord)) {
                        try {
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
                    Log.i(TAG,"index: " + startIndex + " - setting span");
                    index = startIndex + mFocusedWord.length() + 1;
                }


            }



            //TODO remove if this doesn't work
//            holder.txtTweet.setLongClickable(false);
            holder.txtTweet.setText(text, TextView.BufferType.SPANNABLE);
            holder.txtTweet.setMovementMethod(LongClickLinkMovementMethod.getInstance());
        } catch (NullPointerException e) {
            holder.txtTweet.setText(getTweet(position).getText());
            Log.e(TAG,"mTweet urls are null : " + e);
        } catch (Exception e) {
            holder.txtTweet.setText(getTweet(position).getText());
            Log.e(TAG,"Error adding url info: " + e);
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

        PopupWindow popupWindow = TestPopupWindow.createTweetFavoritesPopup(mContext,mMetrics,rxBus,availableFavoriteLists,mTweet.getIdString(), mTweet.getUser().getUserId());
//        PopupWindow popupWindow =  new TestPopupWindow(getContext(),metrics,rxBus,availableFavoriteLists,mWords.get(holder.getAdapterPosition()).getId()).onCreateView();

        popupWindow.getContentView().measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        int xadjust = popupWindow.getContentView().getMeasuredWidth() + (int) (25 * mMetrics.density + 0.5f);
        int yadjust = (int)((holder.imgStar.getMeasuredHeight())/2.0f);

        Log.d("TEST","pop width: " + popupWindow.getContentView().getMeasuredWidth() + " height: " + popupWindow.getContentView().getMeasuredHeight());
        Log.d("TEST","xadjust: " + xadjust + ", yadjust: " + yadjust);


        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

                _rxbus.sendSaveTweet(mTweet);
                try {
                    holder.imgStar.setImageResource(FavoritesColors.assignStarResource(mTweet.getItemFavorites(),mActiveTweetFavoriteStars));
                    holder.imgStar.setColorFilter(ContextCompat.getColor(mContext,FavoritesColors.assignStarColor(mTweet.getItemFavorites(),mActiveTweetFavoriteStars)));
                } catch (NullPointerException e) {
                    Log.e(TAG,"UserTimeLineAdapter popupwindow ondismisslistener setting colorfilter nullpointer: " + e.getMessage());
                }

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


}