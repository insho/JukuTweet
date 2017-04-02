package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.DropDownMenuOption;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;


public class MenuDropDownColorsPopupAdapter extends RecyclerView.Adapter<MenuDropDownColorsPopupAdapter.ViewHolder>  {
    private ArrayList<DropDownMenuOption> mOptions;
    private RxBus mRxBus;
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtOption;
        public AppCompatCheckBox chkBox;
        public LinearLayout layoutMain;


        public ViewHolder(View v) {
            super(v);
            txtOption = (TextView) v.findViewById(R.id.textBox);
            chkBox = (AppCompatCheckBox) v.findViewById(R.id.checkBox);
            layoutMain = (LinearLayout) v.findViewById(R.id.itemLayout);
        }
    }


    public MenuDropDownColorsPopupAdapter(Context context, ArrayList<DropDownMenuOption> options, RxBus rxbus) {
        mContext = context;
        mOptions = options;
        mRxBus = rxbus;
    }


    @Override
    public MenuDropDownColorsPopupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialogmenu_spinneritem, parent, false);
        return new ViewHolder(v);
    }

//    public DropDownMenuOption getItem(int position) {
//        return mOptions.get(position);
//    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final DropDownMenuOption option = mOptions.get(holder.getAdapterPosition());

//        holder.txtOption.setText(option.getChosenOption());
        holder.txtOption.setClickable(false);
        holder.chkBox.setChecked(option.isColorSelected());
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mOptions.get(holder.getAdapterPosition()).isColorSelected()) {
                    mOptions.get(holder.getAdapterPosition()).setColorSelected(false);
                    holder.chkBox.setChecked(false);
                } else {
                    mOptions.get(holder.getAdapterPosition()).setColorSelected(true);
                    holder.chkBox.setChecked(true);
                }
                mRxBus.send(option);
            }
        });



        final Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.colorblock);


        switch (option.getChosenOption()) {
            case "Grey":
                drawable.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuGrey), PorterDuff.Mode.MULTIPLY);
                break;
            case "Red":
                drawable.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuRed), PorterDuff.Mode.MULTIPLY);
                break;
            case "Yellow":
                drawable.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuYellow), PorterDuff.Mode.MULTIPLY);
                break;
            case "Green":
                drawable.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuGreen), PorterDuff.Mode.MULTIPLY);
                break;
        }
//        Log.d("TEST","COLORCOUNT: "+ option.getChosenOption() + "-" + option.getColorCount());


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            holder.txtOption.setBackground(drawable);
        } else {
            holder.txtOption.setBackgroundDrawable(drawable);
        }

        //TODO MAKE IT WORK
//        setAppComp
        setAppCompatCheckBoxColors(holder.chkBox, ContextCompat.getColor(mContext, android.R.color.black), ContextCompat.getColor(mContext, android.R.color.black));
        holder.txtOption.setText(String.valueOf(option.getColorCount()));


//        holder.layoutMain.addView(checkbox);
//        layout.addView(textView);

//        checkbox.measure(0, 0);

    }


    //TODO MOVE THIS TO GLOBAL
    public static void setAppCompatCheckBoxColors(final AppCompatCheckBox _checkbox,
                                                  final int _uncheckedColor, final int _checkedColor) {
        int[][] states = new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}};
        int[] colors = new int[]{_uncheckedColor, _checkedColor};
        _checkbox.setSupportButtonTintList(new ColorStateList(states, colors));
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mOptions.size();
    }

//
//    public void setColorBlocks(ColorBlockMeasurables colorBlockMeasurables
//            ,int availableWidth
//            , TextView txtColorBlock) {
//
//        Drawable drawablecolorblock1 = ContextCompat.getDrawable(mContext, R.drawable.colorblock);
//        Drawable drawablecolorblock2 = ContextCompat.getDrawable(mContext, R.drawable.colorblock);
//        Drawable drawablecolorblock3 = ContextCompat.getDrawable(mContext, R.drawable.colorblock);
//        Drawable drawablecolorblock4 = ContextCompat.getDrawable(mContext, R.drawable.colorblock);
//
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            drawablecolorblock1.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuGrey), PorterDuff.Mode.MULTIPLY);
//            txtGrey.setBackground(drawablecolorblock1);
//            drawablecolorblock2.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuRed), PorterDuff.Mode.MULTIPLY);
//            txtRed.setBackground(drawablecolorblock2);
//            drawablecolorblock3.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuYellow), PorterDuff.Mode.MULTIPLY);
//            txtYellow.setBackground(drawablecolorblock3);
//            drawablecolorblock4.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuGreen), PorterDuff.Mode.MULTIPLY);
//            txtGreen.setBackground(drawablecolorblock4);
//
//        } else {
//            drawablecolorblock1.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuGrey), PorterDuff.Mode.MULTIPLY);
//            txtGrey.setBackgroundDrawable(drawablecolorblock1);
//            drawablecolorblock2.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuRed), PorterDuff.Mode.MULTIPLY);
//            txtRed.setBackgroundDrawable(drawablecolorblock2);
//            drawablecolorblock3.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuYellow), PorterDuff.Mode.MULTIPLY);
//            txtYellow.setBackgroundDrawable(drawablecolorblock3);
//            drawablecolorblock4.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuGreen), PorterDuff.Mode.MULTIPLY);
//            txtGreen.setBackgroundDrawable(drawablecolorblock4);
//        }
//
//        txtGrey.setVisibility(View.GONE);
//        txtRed.setVisibility(View.GONE);
//        txtYellow.setVisibility(View.GONE);
//        txtGreen.setVisibility(View.GONE);
//
//        txtRed.setText(String.valueOf(colorBlockMeasurables.getRedCount()));
//        txtYellow.setText(String.valueOf(colorBlockMeasurables.getYellowCount()));
//        txtGreen.setText(String.valueOf(colorBlockMeasurables.getGreenCount()));
//
//        if (colorBlockMeasurables.getRedCount()
//                + colorBlockMeasurables.getYellowCount()
//                + colorBlockMeasurables.getGreenCount() == 0) {
//            txtGrey.setText(String.valueOf(colorBlockMeasurables.getTotalCount()));
//            txtGrey.setVisibility(View.VISIBLE);
//            txtGrey.setMinimumWidth(availableWidth);
//
//        } else {
//
//            int availableWidthRemaining = availableWidth;
//            if(colorBlockMeasurables.getGreyCount()>0){
//                txtGrey.setText(String.valueOf(colorBlockMeasurables.getGreyCount()));
//                int dimenscore = colorBlockMeasurables.getGreyDimenscore(availableWidth);
//
//                availableWidthRemaining = availableWidth-dimenscore;
//                txtGrey.setMinimumWidth(dimenscore);
//                txtGrey.setVisibility(View.VISIBLE);
//            }
//
//            if(colorBlockMeasurables.getRedCount()>0){
//                txtRed.setText(String.valueOf(colorBlockMeasurables.getRedCount()));
//                int dimenscore = colorBlockMeasurables.getRedDimenscore(availableWidth,availableWidthRemaining);
//
//                availableWidthRemaining = availableWidthRemaining -dimenscore;
//                txtRed.setMinimumWidth(dimenscore);
//                txtRed.setVisibility(View.VISIBLE);
//
//            }
//
//            if(colorBlockMeasurables.getYellowCount()>0){
//                txtYellow.setText(String.valueOf(colorBlockMeasurables.getYellowCount()));
//                int dimenscore = colorBlockMeasurables.getYellowDimenscore(availableWidth,availableWidthRemaining);
//                availableWidthRemaining = availableWidthRemaining -dimenscore;
//                txtYellow.setMinimumWidth(dimenscore);
//                txtYellow.setVisibility(View.VISIBLE);
//
//            }
//
//            if(colorBlockMeasurables.getGreenCount()>0){
//                txtGreen.setText(String.valueOf(colorBlockMeasurables.getGreenCount()));
//                int dimenscore = colorBlockMeasurables.getGreenDimenscore(availableWidth,availableWidthRemaining);
//                txtGreen.setMinimumWidth(dimenscore);
//                txtGreen.setVisibility(View.VISIBLE);
//            }
//        }
//
//        if (colorBlockMeasurables.getTotalCount() == 0) {
//            txtGrey.setVisibility(View.VISIBLE);
//            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                drawablecolorblock1.setColorFilter(ContextCompat.getColor(mContext, android.R.color.white), PorterDuff.Mode.MULTIPLY);
//                txtGrey.setBackground(drawablecolorblock1);
//            } else {
//                drawablecolorblock1.setColorFilter(ContextCompat.getColor(mContext, android.R.color.white), PorterDuff.Mode.MULTIPLY);
//                txtGrey.setBackgroundDrawable(drawablecolorblock1);
//            }
//            txtGrey.setText(mContext.getString(R.string.empty));
//            txtRed.setVisibility(View.GONE);
//            txtYellow.setVisibility(View.GONE);
//            txtGreen.setVisibility(View.GONE);
//        }
//
//    }
}

