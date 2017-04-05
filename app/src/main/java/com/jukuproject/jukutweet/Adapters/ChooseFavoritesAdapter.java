package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
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

import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Created by JClassic on 3/24/2017.
 */

public class ChooseFavoritesAdapter extends RecyclerView.Adapter<ChooseFavoritesAdapter.ViewHolder> {

    private ArrayList<MyListEntry> mMyListEntries;
    private Context mContext;
    private int mKanjiId;
    private float mDensity;
    private RxBus mRxBusTweetBreakdownAdapter;
    private String TAG = "TEST-choosefavsadapter";

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

    public ChooseFavoritesAdapter(Context context, float density, RxBus rxBus, ArrayList<MyListEntry> myListEntries, int kanjiId) {
        mMyListEntries = myListEntries;
        mContext =context;
        mKanjiId = kanjiId;
        mDensity = density;
        mRxBusTweetBreakdownAdapter = rxBus;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChooseFavoritesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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


        setAppCompatCheckBoxColors(holder.checkbox, ContextCompat.getColor(mContext, android.R.color.black), ContextCompat.getColor(mContext, android.R.color.black));
        if(initialMyListEntry.getListsSys() == 1){

            switch (initialMyListEntry.getListName()) {
                case "Blue":
                    holder.imageButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuBlue));
                    break;
                case "Green":
                    holder.imageButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuGreen));
                    break;
                case "Red":
                    holder.imageButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuRed));
                    break;
                case "Yellow":
                    holder.imageButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuYellow));
                    break;
                default:
                    break;
            }

            holder.checkbox.setText(null);
            holder.textView.setText("Favorites");
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



    private void rowSelected(ViewHolder holder, MyListEntry myListEntry) {

        //If initially not selected, set selected
        if(myListEntry.getSelectionLevel() == 0) {
            if(InternalDB.getInstance(mContext).addWordToWordList(mKanjiId,myListEntry.getListName(),myListEntry.getListsSys())) {
                holder.checkbox.setChecked(true);
                myListEntry.setSelectionLevel(1);
                /* Send the updated mylistentry back to the tweetbreakdown adapter, where the WordEntry.ItemFavorites object
                *  and the star button will be updated to reflect the addition */
                mRxBusTweetBreakdownAdapter.send(myListEntry);
            } else {
                Log.e(TAG,"Unable to addkanji to mylist in choosefavorites adapter!");
            }
        } else {
            if(InternalDB.getInstance(mContext).removeWordFromWordList(mKanjiId,myListEntry.getListName(),myListEntry.getListsSys())) {
                holder.checkbox.setChecked(false);
                myListEntry.setSelectionLevel(0);
                /* Send the updated mylistentry back to the tweetbreakdown adapter, where the WordEntry.ItemFavorites object
                *  and the star button will be updated to reflect the subtraction */
                mRxBusTweetBreakdownAdapter.send(myListEntry);
            } else {
                Log.e(TAG,"Unable to removekanji from mylist in choosefavorites adapter!");
            }
        }
    }


    //TODO MOVE THIS TO GLOBAL
    public static void setAppCompatCheckBoxColors(final AppCompatCheckBox _checkbox,
                                                  final int _uncheckedColor, final int _checkedColor) {
        int[][] states = new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}};
        int[] colors = new int[]{_uncheckedColor, _checkedColor};
        _checkbox.setSupportButtonTintList(new ColorStateList(states, colors));
    }

}


