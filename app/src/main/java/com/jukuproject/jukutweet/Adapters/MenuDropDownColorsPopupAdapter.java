package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.DropDownMenuOption;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Adapter for colorblock dropdown menu in {@link com.jukuproject.jukutweet.Dialogs.QuizMenuDialog}. User can select or
 * de-select the color groups that will be included in a quiz.
 * @see com.jukuproject.jukutweet.Dialogs.QuizMenuDialog
 */
public class MenuDropDownColorsPopupAdapter extends RecyclerView.Adapter<MenuDropDownColorsPopupAdapter.ViewHolder>  {
    private ArrayList<DropDownMenuOption> mOptions;
    private RxBus mRxBus;
    private Context mContext;
    private String mInitialSelectedColors;

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


    public MenuDropDownColorsPopupAdapter(Context context, ArrayList<DropDownMenuOption> options, String initialSelectedColors, RxBus rxbus) {
        mContext = context;
        mOptions = options;
        mRxBus = rxbus;
        mInitialSelectedColors = initialSelectedColors;
    }


    @Override
    public MenuDropDownColorsPopupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialogmenu_spinneritem, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final DropDownMenuOption option = mOptions.get(holder.getAdapterPosition());
        holder.txtOption.setClickable(false);
        holder.chkBox.setClickable(false);
        if(mInitialSelectedColors.contains(option.getChosenOption())) {
            option.setColorSelected(true);
        } else {
            option.setColorSelected(false);
        }
        holder.chkBox.setChecked(option.isColorSelected());
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(mOptions.get(holder.getAdapterPosition()).isColorSelected()) {

                    /*Only allow user to de-select the colorblock if it is NOT THE LAST colorblock selected.
                        There must always be at least one selected. */
                    if(getTotalSelectedColors(mOptions)>1) {
                        mOptions.get(holder.getAdapterPosition()).setColorSelected(false);
                        holder.chkBox.setChecked(false);
                        mRxBus.send(option);
                    }
                } else {
                    mOptions.get(holder.getAdapterPosition()).setColorSelected(true);
                    holder.chkBox.setChecked(true);
                    mRxBus.send(option);
                }
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

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            holder.txtOption.setBackground(drawable);
        } else {
            holder.txtOption.setBackgroundDrawable(drawable);
        }

        holder.txtOption.setText(String.valueOf(option.getColorCount()));

    }

    /**
     * Used to ensure that at least 1 of the colorblock groups is always selected. A user must have at least one group selected
     * before the quiz begins, or there will be no questions to quiz on. So this iterates through the colorblock dropdown dataset and
     * counts the selected options.
     * @param dropDownMenuOptions Array of DropDownMenuOptions, one for each colorblock group in the dropdown.
     * @return count of color groups that are currently selected
     */
    public static int getTotalSelectedColors(ArrayList<DropDownMenuOption> dropDownMenuOptions) {
        int totalSelectedColors = 0;
        for (DropDownMenuOption dropDownMenuOption : dropDownMenuOptions) {
            if(dropDownMenuOption.isColorSelected()){
                totalSelectedColors += 1;
                if(BuildConfig.DEBUG){Log.d("TEST","TOTALSELECTEDCOLORS: " + dropDownMenuOption.getChosenOption() + " selected: " + dropDownMenuOption.isColorSelected() + ", TOTAL COUNT: " + totalSelectedColors);}
            }
        }
        return totalSelectedColors;
    }

    @Override
    public int getItemCount() {
        return mOptions.size();
    }
}

