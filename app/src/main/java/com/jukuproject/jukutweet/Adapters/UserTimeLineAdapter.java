package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jukuproject.jukutweet.FavoritesColors;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.LongClickLinkMovementMethod;
import com.jukuproject.jukutweet.LongClickableSpan;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetUrl;
import com.jukuproject.jukutweet.Models.TweetUserMentions;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

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
    private List<Tweet> mDataset;
    private Context mContext;
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
        public ImageButton imgStar;
        public FrameLayout imgStarLayout;
        private ImageButton imgReTweeted;
        private ImageButton imgFavorited;
        private ImageView image;

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
            image = (ImageView) v.findViewById(R.id.image);
            imgStarLayout = (FrameLayout) v.findViewById(R.id.timelineStarLayout);


        }
    }

    public UserTimeLineAdapter(Context context
            , RxBus rxBus
            , List<Tweet> myDataset
            , ArrayList<String> activeTweetFavoriteStars
            , @Nullable String focusedWord
            , Boolean showStar) {
        mContext = context;
        _rxbus = rxBus;
        mDataset = myDataset;
        mFocusedWord = focusedWord;
        mActiveTweetFavoriteStars = activeTweetFavoriteStars;
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
        final Tweet tweet = mDataset.get(holder.getAdapterPosition());

        holder.txtUserName.setText(tweet.getUser().getName());
        holder.txtUserScreenName.setText(tweet.getUser().getDisplayScreenName());
        String userIconImagePath;
        if(mShowStar) {
            /* Insert tweet metadata if it exists*/
            holder.txtCreated.setText(tweet.getDatabaseInsertDate());
            holder.txtReTweeted.setText(tweet.getRetweetCountString());
            if(getTweet(position).getFavorited() != null){
                holder.txtFavorited.setText(tweet.getFavoritesCountString());
            }

            holder.imgFavorited.setVisibility(View.VISIBLE);
            holder.imgReTweeted.setVisibility(View.VISIBLE);
            holder.txtReTweeted.setVisibility(View.VISIBLE);
            holder.txtFavorited.setVisibility(View.VISIBLE);

            holder.imgStarLayout.setClickable(true);
            holder.imgStarLayout.setLongClickable(true);

            Integer starColorDrawableInt = FavoritesColors.assignStarResource(true,tweet.getItemFavorites(),mActiveTweetFavoriteStars);
            holder.imgStar.setImageResource(starColorDrawableInt);

            if(starColorDrawableInt!=R.drawable.ic_twitter_multicolor_24dp) {
                try {
                    holder.imgStar.setColorFilter(ContextCompat.getColor(mContext,FavoritesColors.assignStarColor(tweet.getItemFavorites(),mActiveTweetFavoriteStars)));
                } catch (NullPointerException e) {
                    Log.e(TAG,"tweetBreakDownAdapter multistar Nullpointer error setting star color filter in word detail popup dialog... Need to assign item favorites to WordEntry(?)" + e.getCause());
                }
            } else {
                holder.imgStar.setColorFilter(null);
            }

            holder.imgStarLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                /* Check for, and save */
                    if(tweet.getItemFavorites().shouldOpenFavoritePopup(mActiveTweetFavoriteStars)) {
                        _rxbus.sendSaveTweet(holder.getAdapterPosition());
                    } else {

                        if (FavoritesColors.onFavoriteStarToggleTweet(mContext, mActiveTweetFavoriteStars, tweet.getUser().getUserId(), tweet)) {
                            holder.imgStar.setImageResource(R.drawable.ic_twitter_black_24dp);
                            holder.imgStar.setColorFilter(ContextCompat.getColor(mContext, FavoritesColors.assignStarColor(tweet.getItemFavorites(), mActiveTweetFavoriteStars)));
                        } else {
                            Log.e(TAG, "OnFavoriteStarToggle did not work...");
                        }
                    /*Check to see if tweet must be deleted from db. Delete if necessary.
                            * Likewise adds a tweet to the db if it has just been added to a favorites list.
                            * This bit MUST come after the favorite star toggle, because it determines whether to
                            * add or delete a tweet based on whether that tweet is in a favorites list. */
                        _rxbus.sendSaveTweet(tweet);
                    }





                }
            });



            holder.imgStarLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    _rxbus.sendSaveTweet(holder.getAdapterPosition());
                    return true;
                }
            });
            userIconImagePath = tweet.getUser().getProfileImageUrl();
        } else {
            holder.txtCreated.setText(getTweet(position).getDisplayDate());

            holder.imgStar.setVisibility(View.INVISIBLE);
            holder.imgStarLayout.setVisibility(View.INVISIBLE);
            holder.imgFavorited.setVisibility(View.GONE);
            holder.imgReTweeted.setVisibility(View.GONE);
            holder.txtReTweeted.setVisibility(View.GONE);
            holder.txtFavorited.setVisibility(View.GONE);
            userIconImagePath = tweet.getUser().getProfileImageFilePath();
        }

        /* Load User Icon */
        Picasso picasso = new Picasso.Builder(mContext)
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        holder.image.setVisibility(View.GONE);
                    }
                })
                .build();
        picasso.load(userIconImagePath)
                .into(holder.image);
        holder.image.setAdjustViewBounds(true);

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

//            ForegroundColorSpan regularColorSpan = new ForegroundColorSpan(ContextCompat.getColor(mContext,android.R.color.black));

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


                   URLSpan urlSpan = new URLSpan(url.getUrl()) {
                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);

                            ds.setColor(ContextCompat.getColor(mContext,R.color.colorAccent));
                            ds.setAlpha(90);
                        }
                    };

                    text.setSpan(urlSpan, startingLinkPos, startingLinkPos + urlToLinkify.length(), 0);
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

                    ForegroundColorSpan userMentionForegroundSpan = new ForegroundColorSpan(ContextCompat.getColor(mContext,R.color.colorAccent)) {
                        @Override
                        public void updateDrawState(TextPaint ds) {

                            super.updateDrawState(ds);
                            ds.setAlpha(90);
                        }
                    };

//                    ClickableSpan userMentionClickableSpan = new ClickableSpan() {
//                        @Override
//                        public void onClick(View textView) {
//                            Log.d(TAG,"BALLS");
//                            _rxbus.send(userMentionses);
//                        }
//
//                        @Override
//                        public void updateDrawState(TextPaint ds) {
//                            super.updateDrawState(ds);
//                            ds.setColor(ContextCompat.getColor(mContext,R.color.colorAccent));
//
//                            ds.setUnderlineText(false);
//
//                        }
//                    };
                    text.setSpan(userMentionForegroundSpan, startingLinkPos, startingLinkPos + userMentionToLinkify.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

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

}