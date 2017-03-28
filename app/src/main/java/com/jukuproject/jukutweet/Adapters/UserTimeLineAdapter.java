package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetUrl;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.R;

import java.util.List;

/**
 * Recycler adapter for UserTimeLineFragment, displays a list of users tweets
 */
public class UserTimeLineAdapter extends RecyclerView.Adapter<UserTimeLineAdapter.ViewHolder> {

    private static final String TAG = "TEST-timefrag";
    private RxBus _rxbus;
    private UserInfo mUserInfo;
    private List<Tweet> mDataset;
    private Context mContext;

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

    public UserTimeLineAdapter(Context context, RxBus rxBus, UserInfo userInfo, List<Tweet> myDataset) {
        mContext = context;
        _rxbus = rxBus;
        mDataset = myDataset;
        mUserInfo = userInfo;
    }


    @Override
    public UserTimeLineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.usertimeline_recycler_row, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {


        holder.txtUserName.setText(mUserInfo.getName());
        holder.txtUserScreenName.setText(mUserInfo.getDisplayScreenName());


        /* Insert tweet metadata if it exists*/
        holder.txtCreated.setText(getTweet(position).getDisplayDate());
        holder.txtReTweeted.setText(getTweet(position).getRetweetCountString());


        if(getTweet(position).getFavorited() != null){
            holder.txtFavorited.setText(getTweet(position).getFavoritesCountString());
        }


        holder.imgStarLayout.setClickable(true);
        holder.imgStarLayout.setLongClickable(true);

        holder.imgStarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "STAR CLICK", Toast.LENGTH_SHORT).show();

                InternalDB.getInstance(mContext).saveTweet(mUserInfo,mDataset.get(holder.getAdapterPosition()));


            }
        });


        try {
            SpannableString text = new SpannableString(getTweet(position).getText());
//        holder.txtTweet.setText(getTweet(position).getText());


            ClickableSpan normalClick = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    _rxbus.send(mDataset.get(holder.getAdapterPosition()));

                }


                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(ContextCompat.getColor(mContext, android.R.color.black));
                    ds.setUnderlineText(false);

                }
            };



            text.setSpan(normalClick, 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//            normalClick.updateDrawState(new TextPaint());
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
//            holder.txtTweet.setHighlightColor(ContextCompat.getColor(mContext, android.R.color.transparent));
            holder.txtTweet.setText(text, TextView.BufferType.SPANNABLE);
            holder.txtTweet.setMovementMethod(LinkMovementMethod.getInstance());
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

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public Tweet getTweet(int position) {
        return mDataset.get(position);
    }

}