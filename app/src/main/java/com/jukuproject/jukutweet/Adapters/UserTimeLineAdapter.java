package com.jukuproject.jukutweet.Adapters;

        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.TextView;

        import com.jukuproject.jukutweet.Models.Tweet;
        import com.jukuproject.jukutweet.R;

        import java.util.List;

public class UserTimeLineAdapter extends RecyclerView.Adapter<UserTimeLineAdapter.ViewHolder> {

//    private RxBus _rxbus;
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

    public UserTimeLineAdapter(List<Tweet> myDataset) {
        mDataset = myDataset;
    }

//    public UserTimeLineAdapter(List<Tweet> myDataset, RxBus rxBus) {
//        mDataset = myDataset;
//        _rxbus = rxBus;
//    }

    // Create new views (invoked by the layout manager)
    @Override
    public UserTimeLineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.usertimeline_recycler_row, parent, false);
        return new ViewHolder(v);
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.txtTweet.setText(getTweet(position).getText());

        if(getTweet(position).getCreated_at() != null){
            holder.txtCreated.setText("Created at: " + String.valueOf(getTweet(position).getCreated_at()));
        }
        if(getTweet(position).getFavorited() != null){
            holder.txtFavorited.setText("Favorited: " + String.valueOf(getTweet(position).getFavorited()));
        } else {
            holder.txtFavorited.setText("Favorited: TEST");
        }
        if(getTweet(position).getRetweet_count() != null){
            holder.txtReTweeted.setText("ReTweeted: " + String.valueOf(getTweet(position).getRetweet_count()));
        } else {
            holder.txtFavorited.setText("ReTweeted: TEST");
        }

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //If it's a short click, send the object (with title, image etc)
//                _rxbus.send(mDataset.get(holder.getAdapterPosition()));
//            }
//        });
//
//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                //If it's a long click, send the row id only, for deletion
//                _rxbus.sendLongClick(mDataset.get(holder.getAdapterPosition()));
//                return false;
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public Tweet getTweet(int position) {
        return mDataset.get(position);
    }

}