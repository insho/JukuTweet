package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Adapter for CopyMyListItemsDialog, where user can choose which list to move/copy selected items to.
 *
 * @see com.jukuproject.jukutweet.Dialogs.CopyMyListItemsDialog
 */

public class CopyMyListItemsAdapter extends RecyclerView.Adapter<CopyMyListItemsAdapter.ViewHolder> {

    private ArrayList<MyListEntry> mMyListEntries;
    private Context mContext;
    private float mDensity;
    private RxBus mRxBus;

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

    public CopyMyListItemsAdapter(Context context, float density, RxBus rxBus, ArrayList<MyListEntry> myListEntries) {
        mMyListEntries = myListEntries;
        mContext =context;
        mDensity = density;
        mRxBus = rxBus;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CopyMyListItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.popup_choosefavorites_recycler_row, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final MyListEntry initialMyListEntry = mMyListEntries.get(holder.getAdapterPosition());

        holder.checkbox.setOnCheckedChangeListener(null);
        holder.gridLayout.setOnClickListener(null);


        if(initialMyListEntry.getSelectionLevel()>=1){
            holder.checkbox.setChecked(true);
//            holder.checkbox.setAlpha(1.0f);
//        } else if(initialMyListEntry.getSelectionLevel()==2) {
//            holder.checkbox.setChecked(true);
//            holder.checkbox.setAlpha(.4f);
        } else {
            holder.checkbox.setChecked(false);
//            holder.checkbox.setAlpha(1.0f);
        }
        holder.imageButton.setPadding(0,
                (int) (2.0f * mDensity + 0.5f),
                0,
                (int) (2.0f * mDensity + 0.5f));

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
                case "Purple":
                    holder.imageButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuPurple));
                    break;
                case "Orange":
                    holder.imageButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuOrange));
                    break;
                default:
                    break;
            }

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



    private void rowSelected(ViewHolder holder, MyListEntry myListEntry) {

        //If initially not selected, set selected
        if(myListEntry.getSelectionLevel() != 1) {
            holder.checkbox.setChecked(true);
            myListEntry.setSelectionLevel(1);
//            holder.checkbox.setAlpha(1.0f);

        } else {
            holder.checkbox.setChecked(false);
            myListEntry.setSelectionLevel(0);
//            holder.checkbox.setAlpha(1.0f);

        }
        mRxBus.send(myListEntry);
    }
}


