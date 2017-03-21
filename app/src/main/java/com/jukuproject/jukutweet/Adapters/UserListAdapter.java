package com.jukuproject.jukutweet.Adapters;

        import android.content.Context;
        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;

        import com.jukuproject.jukutweet.Models.UserInfo;
        import com.jukuproject.jukutweet.R;
        import com.jukuproject.jukutweet.Interfaces.RxBus;

        import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private RxBus _rxbus;
    private List<UserInfo> mDataset;

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

    public UserListAdapter(List<UserInfo> myDataset, RxBus rxBus) {
        mDataset = myDataset;
        _rxbus = rxBus;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.userlist_recycler_row, parent, false);
        return new ViewHolder(v);
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.txtUserName.setText(getUser(position).getDisplayName());
        holder.txtUserDescription.setText(getUser(position).getDescription());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If it's a short click, send the object (with title, image etc)
                _rxbus.send(mDataset.get(holder.getAdapterPosition()));
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //If it's a long click, send the row id only, for deletion
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