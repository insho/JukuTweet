package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.TweetKanjiColor;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Recycler adapter for UserTimeLineFragment, displays a list of users tweets
 */
public class BrowseTweetsAdapter extends RecyclerView.Adapter<BrowseTweetsAdapter.ViewHolder> {

    private static final String TAG = "TEST-timefrag";
    private RxBus _rxbus;
    private List<Tweet> mDataset;
    private Context mContext;
    private ArrayList<Integer> mSelectedEntries;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtTweet;
        public TextView txtCreated;
        public TextView txtFavorited;
        public TextView txtReTweeted;

        public TextView txtUserName;
        public TextView txtUserScreenName;
        public ImageButton imgStar;
        public FrameLayout imgStarLayout;
        public ImageButton imgFavorite;
        public ImageButton imgRetweet;

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
            imgFavorite = (ImageButton) v.findViewById(R.id.heart);
            imgRetweet = (ImageButton) v.findViewById(R.id.retweetImage);

        }
    }

    public BrowseTweetsAdapter(Context context, RxBus rxBus, List<Tweet> myDataset, ArrayList<Integer> selectedEntries) {
        mContext = context;
        _rxbus = rxBus;
        mDataset = myDataset;
        mSelectedEntries =selectedEntries;
    }


    @Override
    public BrowseTweetsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.usertimeline_recycler_row, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Tweet tweet = mDataset.get(holder.getAdapterPosition());

        holder.txtUserName.setText(tweet.getUser().getName());
        holder.txtUserScreenName.setText(tweet.getUser().getScreenName());
        holder.txtCreated.setText(tweet.getDisplayDate());

        /* Set non-essential bits gone*/
        holder.txtReTweeted.setVisibility(View.GONE);
        holder.txtFavorited.setVisibility(View.GONE);
        holder.imgStarLayout.setVisibility(View.GONE);
        holder.imgStar.setVisibility(View.GONE);
        holder.imgRetweet.setVisibility(View.GONE);
        holder.imgFavorite.setVisibility(View.GONE);


        try {
            Log.d(TAG,"TWEET COLOR: " + tweet.getColorIndexes().size());
//            SpannableString text = new SpannableString(tweet.getText());

            final SpannableStringBuilder sb = new SpannableStringBuilder(tweet.getText());


            for(TweetKanjiColor color : tweet.getColorIndexes()) {
                Log.d(TAG,"WEET INITI COLOR: " + color.getStartIndex() + ", end: " + color.getEndIndex());
                final ForegroundColorSpan fcs = new ForegroundColorSpan(ContextCompat.getColor(mContext,color.getColorValue()));
                sb.setSpan(fcs, color.getStartIndex(), color.getEndIndex(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }

            holder.txtTweet.setText(sb);

        } catch (NullPointerException e) {
            holder.txtTweet.setText(tweet.getText());
            Log.e(TAG,"Tweet color nullpointer failure : " + e);
        } catch (Exception e) {
            holder.txtTweet.setText(tweet.getText());
            Log.e(TAG,"Tweet color generic failure: " + e);
        }


    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


//    public Tweet getTweet(int position) {
//        return mDataset.get(position);
//    }

}