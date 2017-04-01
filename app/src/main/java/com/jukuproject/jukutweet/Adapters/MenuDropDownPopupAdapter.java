package com.jukuproject.jukutweet.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.DropDownMenuOption;

import java.util.ArrayList;


public class MenuDropDownPopupAdapter extends RecyclerView.Adapter<MenuDropDownPopupAdapter.ViewHolder>  {
    private Integer mButtonNumber;
    private ArrayList<String> mOptions;
    private RxBus mRxBus;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtOption;

        public ViewHolder(View v) {
            super(v);
            txtOption = (TextView) v.findViewById(android.R.id.text1);
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
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        return new ViewHolder(v);
    }

    public String getItem(int position) {
        return mOptions.get(position);
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

