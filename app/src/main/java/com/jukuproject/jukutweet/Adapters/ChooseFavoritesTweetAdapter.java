package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jukuproject.jukutweet.ChooseFavoriteListsPopupWindow;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.FavoritesColors;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;


/**
 * Adapter for ChooseFavoriteListsPopupWindow (for tweets. appears when the favorites star is clicked next to a tweet).
 * User can add/subtract a Tweet from a Tweet list.
 *
 * @see ChooseFavoriteListsPopupWindow
 */
public class ChooseFavoritesTweetAdapter extends RecyclerView.Adapter<ChooseFavoritesTweetAdapter.ViewHolder> {

    private ArrayList<MyListEntry> mMyListEntries;
    private Context mContext;
    private String mTweetIdString;
    private String mUserIdString;
    private float mDensity;
    private RxBus mRxBusTweetBreakdownAdapter;
    private String TAG = "TEST-tweetfavsadapter";

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private ImageButton imageButton;
        private AppCompatCheckBox checkbox;
        private GridLayout gridLayout;

        public ViewHolder(View v) {
            super(v);
            imageButton = (ImageButton) v.findViewById(R.id.listitemstar);
            checkbox = (AppCompatCheckBox) v.findViewById(R.id.listitemcheckbox);
            textView = (TextView) v.findViewById(R.id.listitemtextview);
            gridLayout = (GridLayout) v.findViewById(R.id.listitemgridview);

        }

    }

    public ChooseFavoritesTweetAdapter(Context context
            , float density
            , RxBus rxBus
            , ArrayList<MyListEntry> myListEntries
            , String tweetIdString
            , String userIdString) {
        mMyListEntries = myListEntries;
        mContext =context;
        mTweetIdString = tweetIdString;
        mUserIdString = userIdString;
        mDensity = density;
        mRxBusTweetBreakdownAdapter = rxBus;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChooseFavoritesTweetAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.popup_choosefavorites_recycler_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final MyListEntry initialMyListEntry = mMyListEntries.get(holder.getAdapterPosition());

        holder.checkbox.setOnCheckedChangeListener(null);
        holder.gridLayout.setOnClickListener(null);

        if(initialMyListEntry.getSelectionLevel()==1){
            holder.checkbox.setChecked(true);
        } else {
            holder.checkbox.setChecked(false);
        }
        holder.imageButton.setPadding(0,
                (int) (2.0f * mDensity + 0.5f),
                0,
                (int) (2.0f * mDensity + 0.5f));


        //If favorite list is a system list, add a colored star before the listname label
        if(initialMyListEntry.getListsSys() == 1){
            FavoritesColors.setFavoritesButtonColorFilter(mContext,holder.imageButton,initialMyListEntry.getListName());
            holder.checkbox.setText(null);
            holder.textView.setText(mContext.getString(R.string.favorites_text));
            holder.textView.setVisibility(View.VISIBLE);
            holder.imageButton.setVisibility(View.VISIBLE);
        } else {
            holder.checkbox.setText(initialMyListEntry.getListName());
            holder.checkbox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            holder.textView.setVisibility(View.GONE);
            holder.imageButton.setVisibility(View.GONE);

        }

        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rowSelected(holder, mMyListEntries.get(holder.getAdapterPosition()));
            }
        });

        holder.gridLayout.setClickable(true);
        holder.gridLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowSelected(holder, mMyListEntries.get(holder.getAdapterPosition()));
            }
        });


    }

    @Override
    public int getItemCount() {
        return mMyListEntries.size();
    }

    /**
     * When row is selected, the MyList is added immediately to the database as a favorite for this tweet, and
     * confirmation is sent back via RxBus to the {@link ChooseFavoriteListsPopupWindow}.
     * where the Tweet for that list can be updated accordingly with a new {@link com.jukuproject.jukutweet.Models.ItemFavorites}
     * @param holder Viewholder
     * @param myListEntry MyList object with info for a particular favorite list
     */
    private void rowSelected(ViewHolder holder, MyListEntry myListEntry) {

        //If initially not selected, set selected
        if(myListEntry.getSelectionLevel() == 0) {
            if(InternalDB.getTweetInterfaceInstance(mContext).addTweetToTweetList(mTweetIdString,mUserIdString,myListEntry.getListName(),myListEntry.getListsSys())) {
                holder.checkbox.setChecked(true);
                myListEntry.setSelectionLevel(1);
                /* Send the updated mylistentry back to the ChooseFavoriteListsPopupWindow, where the Tweet.ItemFavorites object
                *  and the star button will be updated to reflect the addition */
                mRxBusTweetBreakdownAdapter.send(myListEntry);
            } else {
                Log.e(TAG,"Unable to addkanji to mylist in choosefavorites adapter!");
            }
        } else {
            if(InternalDB.getTweetInterfaceInstance(mContext).removeTweetFromTweetList(mTweetIdString,myListEntry.getListName(),myListEntry.getListsSys())) {
                holder.checkbox.setChecked(false);
                myListEntry.setSelectionLevel(0);
                /* Send the updated mylistentry back to the ChooseFavoriteListsPopupWindow adapter, where the Tweet.ItemFavorites object
                *  and the star button will be updated to reflect the subtraction */
                mRxBusTweetBreakdownAdapter.send(myListEntry);
            } else {
                Log.e(TAG,"Unable to removekanji from mylist in choosefavorites adapter!");
            }
        }
    }

}


