package com.jukuproject.jukutweet.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.DropDownMenuOption;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Adapter for regular (i.e. non-colorblock) dropdown menu in {@link com.jukuproject.jukutweet.Dialogs.QuizMenuDialog}.
 * User can specify quiz criteria (size, timer, quiz type, etc).
 * @see com.jukuproject.jukutweet.Dialogs.QuizMenuDialog
 */
public class MenuDropDownPopupAdapter extends RecyclerView.Adapter<MenuDropDownPopupAdapter.ViewHolder>  {
    private Integer mButtonNumber;
    private ArrayList<String> mOptions;
    private RxBus mRxBus;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtOption;

        public ViewHolder(View v) {
            super(v);
            txtOption = (TextView) v.findViewById(R.id.text1);
        }
    }


    public MenuDropDownPopupAdapter(int buttonNumber, ArrayList<String> options
            ,RxBus rxbus) {
        mButtonNumber = buttonNumber;
        mOptions = options;
        mRxBus = rxbus;
    }


    @Override
    public MenuDropDownPopupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.spinner_listitem, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final String option = mOptions.get(holder.getAdapterPosition());

        holder.txtOption.setText(option);
        holder.txtOption.setClickable(false);
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mRxBus.send(new DropDownMenuOption(option,mButtonNumber));
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mOptions.size();
    }


}

