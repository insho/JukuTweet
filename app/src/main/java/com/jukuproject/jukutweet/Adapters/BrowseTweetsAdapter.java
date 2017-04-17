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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Recycler adapter for UserTimeLineFragment, displays a list of users tweets
 */
public class BrowseTweetsAdapter extends RecyclerView.Adapter<BrowseTweetsAdapter.ViewHolder> {

    private static final String TAG = "TEST-timefrag";
    private RxBus mRxBus;
    private List<Tweet> mDataset;
    private Context mContext;
    private ArrayList<String> mSelectedEntries;

    public class ViewHolder extends RecyclerView.ViewHolder {


        public LinearLayout layoutMain;
        public TextView txtColorBar;
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
            layoutMain = (LinearLayout) v.findViewById(R.id.tweetLayout);
            txtTweet = (TextView) v.findViewById(R.id.tweet);
            txtCreated = (TextView) v.findViewById(R.id.createdAt);
            txtFavorited = (TextView) v.findViewById(R.id.favorited);
            txtReTweeted = (TextView) v.findViewById(R.id.retweeted);

            txtColorBar = (TextView) v.findViewById(R.id.colorbar);
            txtUserName = (TextView) v.findViewById(R.id.timelineName);
            txtUserScreenName = (TextView) v.findViewById(R.id.timelineDisplayScreenName);
            imgStar = (ImageButton) v.findViewById(R.id.favorite);
            imgStarLayout = (FrameLayout) v.findViewById(R.id.timelineStarLayout);
            imgFavorite = (ImageButton) v.findViewById(R.id.heart);
            imgRetweet = (ImageButton) v.findViewById(R.id.retweetImage);

        }
    }

    public BrowseTweetsAdapter(Context context, RxBus rxBus, List<Tweet> myDataset, ArrayList<String> selectedEntries) {
        mContext = context;
        mRxBus = rxBus;
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
        holder.txtUserScreenName.setText(tweet.getUser().getDisplayScreenName());
        holder.txtCreated.setText(tweet.getDisplayDate());

        /* Set non-essential bits gone*/
        holder.txtReTweeted.setVisibility(View.GONE);
        holder.txtFavorited.setVisibility(View.GONE);
        holder.imgStarLayout.setVisibility(View.GONE);
        holder.imgStar.setVisibility(View.GONE);
        holder.imgRetweet.setVisibility(View.GONE);
        holder.imgFavorite.setVisibility(View.GONE);


        /* Parse the definition into an array of multiple lines, if there are multiple sub-definitions in the string */
        if(tweet.getTweetColor() == null || tweet.getTweetColor().equals("Empty")) {
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.white));
        } else if(tweet.getTweetColor().equals("Grey")) {
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuGrey));
        } else if(tweet.getTweetColor().equals("Red")){
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuRed));
        } else if (tweet.getTweetColor().equals("Yellow")){
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuYellow));
        } else if(tweet.getTweetColor().equals("Green")){
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuGreen));
        }


        /* IF the activity is being recreated, set any entries in the selectedEntries
        * list to be selected again */
        if(mSelectedEntries!=null && mSelectedEntries.contains(tweet.getIdString())){
            holder.layoutMain.setSelected(true);
        } else {
            holder.layoutMain.setSelected(false);
        }

        holder.layoutMain.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG,"in click");

                /* Click behavior:
                 *  If mSelectedEntries contains a value (i.e. one or more entires have been selected already), each
                 *  additional click will select a row, and add that row to the mSelectedEntries
                 *  However if no entries are selected already, the click will take the user to the SavedTweetsBreakdownFragment for
                 *  that tweet. User must long-click on a row to select it if no rows are currently selected
                 *  */
                if(mSelectedEntries.size()>0) {
                    if(holder.layoutMain.isSelected()) {
                        holder.layoutMain.setSelected(false);
                        //Send id back to WordListBrowseFragment so it can be added to the selected map
                    } else {
                        holder.layoutMain.setSelected(true);
                    }
                    notifyItemChanged(holder.getAdapterPosition());
                    mRxBus.send(mDataset.get(holder.getAdapterPosition()).getIdString());
                } else {
                    Log.d(TAG,"click is sent");
                    /*Send the whole tweet back to the RXReceiver in SavedTweetsBrowseFragment,
                      where it will be passed on to TweetBreakDownFragment */
                    mRxBus.send(mDataset.get(holder.getAdapterPosition()));
//                    Toast.makeText(mContext, "Send to SavedTweetsBreakdownFragment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.layoutMain.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(holder.layoutMain.isSelected()) {
                    holder.layoutMain.setSelected(false);
                    //Send id back to WordListBrowseFragment so it can be added to the selected map
                } else {
                    holder.layoutMain.setSelected(true);
                }
                notifyItemChanged(holder.getAdapterPosition());
                mRxBus.send(mDataset.get(holder.getAdapterPosition()).getIdString());

                return false;
            }
        });


        /* Set tweet color spans. If the saved Tweet object includes a "colorIndex" object (
        * which comes from the savedTweetKanji table and contains the id, positions and color designation
        * of each kanji in the TWeet), replace the normal Tweet text with colored spans for those kanji */
        try {
            final SpannableStringBuilder sb = new SpannableStringBuilder(tweet.getText());
            if(tweet.getWordEntries() != null) {
                for(WordEntry wordEntry : tweet.getWordEntries()) {
                    final ForegroundColorSpan fcs = new ForegroundColorSpan(ContextCompat.getColor(mContext,wordEntry.getColorValue()));
                    sb.setSpan(fcs, wordEntry.getStartIndex(), wordEntry.getEndIndex(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }
                holder.txtTweet.setText(sb);

            } else {
                holder.txtTweet.setText(tweet.getText());
            }

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

    public void swapDataSet(ArrayList<Tweet> updatedDataSet) {
        this.mDataset = updatedDataSet;
        notifyDataSetChanged();
    }
}