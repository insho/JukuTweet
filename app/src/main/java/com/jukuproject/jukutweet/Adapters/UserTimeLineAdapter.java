package com.jukuproject.jukutweet.Adapters;

        import android.content.Context;
        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.TextView;

        import com.jukuproject.jukutweet.Interfaces.RxBus;
        import com.jukuproject.jukutweet.Models.Tweet;
        import com.jukuproject.jukutweet.R;

        import java.util.List;

/**
 * Recycler adapter for UserTimeLineFragment, displays a list of users tweets
 */
public class UserTimeLineAdapter extends RecyclerView.Adapter<UserTimeLineAdapter.ViewHolder> {

    private RxBus _rxbus;
    private List<Tweet> mDataset;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtTweet;
        public TextView txtCreated;
        public TextView txtFavorited;
        public TextView txtReTweeted;

        public ViewHolder(View v) {
            super(v);
            txtTweet = (TextView) v.findViewById(R.id.tweet);
            txtCreated = (TextView) v.findViewById(R.id.created);
            txtFavorited = (TextView) v.findViewById(R.id.favorited);
            txtReTweeted = (TextView) v.findViewById(R.id.retweeted);

        }
    }

    public UserTimeLineAdapter(RxBus rxBus, List<Tweet> myDataset) {
        _rxbus = rxBus;
        mDataset = myDataset;
    }


    @Override
    public UserTimeLineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.usertimeline_recycler_row, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        /* Insert tweet metadata if it exists*/
        if(getTweet(position).getDisplayDate() != null){
            holder.txtCreated.setText(getTweet(position).getDisplayDate());
        }

        if(getTweet(position).getFavorited() != null){
            holder.txtFavorited.setText(String.valueOf(getTweet(position).getFavorited()));
        }

        if(getTweet(position).getDisplayRetweetCount() != null){
            holder.txtReTweeted.setText(getTweet(position).getDisplayRetweetCount());
        }

        holder.txtTweet.setText(getTweet(position).getText());

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