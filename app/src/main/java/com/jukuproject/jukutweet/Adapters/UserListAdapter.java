package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Recycler Adapter for UserListFragment, shows twitter users saved in the database
 */
public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private RxBus _rxbus;
    private List<UserInfo> mDataset;
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtUserName;
        public TextView txtUserDescription;
        public ImageView image;

        public ViewHolder(View v) {
            super(v);
            txtUserName = (TextView) v.findViewById(R.id.name);
            image = (ImageView) v.findViewById(R.id.image);
            txtUserDescription = (TextView) v.findViewById(R.id.description);
        }
    }

    public UserListAdapter(Context context, List<UserInfo> myDataset, RxBus rxBus) {
        mContext = context;
        mDataset = myDataset;
        _rxbus = rxBus;
    }

    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.userlist_recycler_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.txtUserName.setText(getUser(position).getDisplayScreenName());
        holder.txtUserDescription.setText(getUser(position).getDescription());

        holder.image.setVisibility(View.VISIBLE);
        Picasso picasso = new Picasso.Builder(mContext)
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        //Here your log
                        holder.image.setVisibility(View.GONE);

                    }
                })
                .build();
        picasso.load(getUser(position).getProfileImageUrlBig())
                .into(holder.image);
//
//
//        Picasso.with(mContext).load(mDataset.get(position).getProfileImageFilePath())
//                .into(holder.image);
        holder.image.setAdjustViewBounds(true);




        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*If it's a short click, send the UserInfo object back to UserInfoFragment,
                * to trigger opening the UserTimeLine Fragment */
                _rxbus.send(mDataset.get(holder.getAdapterPosition()));
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                /*If it's a long click, send the UserInfo object back to UserInfoFragment,
                * to trigger opening the RemoveUser dialog */
                _rxbus.sendLongClick(mDataset.get(holder.getAdapterPosition()));
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public UserInfo getUser(int position) {
        return mDataset.get(position);
    }

}