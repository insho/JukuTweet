package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Recycler adapter for UserTimeLineFragment, displays a list of users tweets
 *
 * @see com.jukuproject.jukutweet.Fragments.UserTimeLineFragment
 */
public class BrowseTweetsAdapter extends RecyclerView.Adapter<BrowseTweetsAdapter.ViewHolder> {

    private static final String TAG = "TEST-browstweetadap";
    private RxBus mRxBus;
    private List<Tweet> mDataset;
    private Context mContext;
    private ArrayList<String> mSelectedEntries;

    public class ViewHolder extends RecyclerView.ViewHolder {


        private LinearLayout layoutMain;
        private TextView txtTweet;
        private TextView txtCreated;
        private TextView txtUserName;
        private TextView txtUserScreenName;
        private ImageButton imgStar;
        private FrameLayout imgStarLayout;
        private ImageView image;

        public ViewHolder(View v) {
            super(v);
            layoutMain = (LinearLayout) v.findViewById(R.id.tweetLayout);
            txtTweet = (TextView) v.findViewById(R.id.tweet);
            txtCreated = (TextView) v.findViewById(R.id.createdAt);
            txtUserName = (TextView) v.findViewById(R.id.timelineName);
            txtUserScreenName = (TextView) v.findViewById(R.id.timelineDisplayScreenName);
            imgStar = (ImageButton) v.findViewById(R.id.favorite);
            imgStarLayout = (FrameLayout) v.findViewById(R.id.timelineStarLayout);

            image = (ImageView) v.findViewById(R.id.image);

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.browsetweets_recycler_row, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Tweet tweet = mDataset.get(holder.getAdapterPosition());

        holder.txtUserName.setText(tweet.getUser().getName());
        holder.txtUserScreenName.setText(tweet.getUser().getDisplayScreenName());
        Log.d(TAG,"tweet.getDisplayDate()");
        holder.txtCreated.setText(tweet.getDisplayDate());

        /* Set non-essential bits gone*/
        holder.imgStarLayout.setVisibility(View.GONE);
        holder.imgStar.setVisibility(View.GONE);

        /* Load user icon */
        Log.i(TAG,"TWEETPATH: " + tweet.getUser().getProfileImageFilePath());
        try {
            Picasso picasso = new Picasso.Builder(mContext)
                    .listener(new Picasso.Listener() {
                        @Override
                        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                            Log.e(TAG,"Load image failed for " + tweet.getText());
                            holder.image.setVisibility(View.GONE);
                        }
                    })
                    .build();
            picasso.load(tweet.getUser().getProfileImageFilePath())
                    .into(holder.image);
            holder.image.setAdjustViewBounds(true);


        } catch (NullPointerException e) {
            Log.e(TAG,"Browsetweetsadapter Holder image nullpointer " + e.getCause());
        }


        /* If the activity is being recreated, set any entries in the selectedEntries
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
                    /*Send the whole tweet back to the RXReceiver in TweetListBrowseFragment,
                      where it will be passed on to TweetBreakDownFragment */
                    mRxBus.send(mDataset.get(holder.getAdapterPosition()));
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


    /**
     * Updates the dataset and selected entries with new items when, for instance,
     * kanji are removed from a list in {@link com.jukuproject.jukutweet.Fragments.TweetListBrowseFragment}
     *
     * @param updatedDataSet new dataset
     */
    public void swapDataSet(ArrayList<Tweet> updatedDataSet) {
        this.mDataset = updatedDataSet;
        notifyDataSetChanged();
    }
}