package com.jukuproject.jukutweet.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.MenuDropDownColorsPopupAdapter;
import com.jukuproject.jukutweet.Adapters.MenuDropDownPopupAdapter;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.DropDownMenuOption;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;
import java.util.Arrays;

import rx.functions.Action1;

/**
 * Created by JukuProject on 11/21/2016
 *
 * Popup dialog with option spinners for a quiz
 * The choices are then passed on to the quiz activity
 */

public class QuizMenuDialog extends DialogFragment {

    private final String TAG = "Test-quizmendlog";
    public DialogInteractionListener mCallback;

    private String quizType;
    private int mTabNumber;
    private MyListEntry mMyListEntry;
    private ColorBlockMeasurables mColorBlockMeasurables;
    private int mAvailablePopupWidth;
    private RxBus mRxBus = new RxBus();

    private TextView textViewColorBlock_grey;
    private TextView textViewColorBlock_red;
    private TextView textViewColorBlock_yellow;
    private TextView textViewColorBlock_green;

    private TextView txtView1;
    private TextView txtView2;
    private TextView txtView3;

    public static QuizMenuDialog newInstance(String quizType
            , int tabNumber
            , MyListEntry myListEntry
            , ColorBlockMeasurables colorBlockMeasurables
            , int availablePopupWidth) {

        QuizMenuDialog frag = new QuizMenuDialog();
        Bundle args = new Bundle();
        args.putString("quizType", quizType);
        args.putInt("tabNumber",tabNumber);
        args.putParcelable("myListEntry",myListEntry);
        args.putParcelable("colorBlockMeasurables",colorBlockMeasurables);
        args.putInt("availablePopupWidth",availablePopupWidth);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        quizType = getArguments().getString("quizType");
        mMyListEntry = getArguments().getParcelable("myListEntry");
        mColorBlockMeasurables = getArguments().getParcelable("colorBlockMeasurables");
        mAvailablePopupWidth = getArguments().getInt("availablePopupWidth");
        mTabNumber = getArguments().getInt("tabNumber");

        //TODO make this a typed value or resource
        final int yadjustment = 5;

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View  view = getActivity().getLayoutInflater().inflate(R.layout.dialogmenupopup, null);

        txtView1 = (TextView) view.findViewById(R.id.btnRow1);
        txtView2 = (TextView) view.findViewById(R.id.btnRow2);
        txtView3 = (TextView) view.findViewById(R.id.btnRow3);

        TextView txtShowFront = (TextView) view.findViewById(R.id.txtRow1);
        TextView txtShowBack= (TextView) view.findViewById(R.id.txtRow2);
        TextView txtShowTimer= (TextView) view.findViewById(R.id.txtRow3);

        switch(quizType) {
            case "flashcards":
                txtShowFront.setText("Front: ");
                txtView1.setText(getActivity().getString(R.string.menuoptionskanji));

                txtShowBack.setText("Back: ");
                txtView2.setText(getActivity().getString(R.string.menuoptionsdefinition));

                ((TextView)view.findViewById(R.id.txtRow3)).setVisibility(View.GONE);
                ((TextView) view.findViewById(R.id.btnRow3)).setVisibility(View.GONE);

                final String[] optionsFlashCards = getResources().getStringArray(R.array.menuoptions_flashcards);

                txtView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupDropDownWindow(1,optionsFlashCards).showAsDropDown(v, -yadjustment, 0);
                    }
                });

                txtView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupDropDownWindow(2,optionsFlashCards).showAsDropDown(v, -yadjustment, 0);
                    }
                });

                break;
            case "multiplechoice":
                txtShowFront.setText("Type: ");
                txtView1.setText(getActivity().getString(R.string.menuoptionskanjitodef));

                txtShowBack.setText("Size: ");
                txtView2.setText(getActivity().getString(R.string.menuoptionsten));

                txtShowTimer.setText("Timer: ");
                txtView3.setText(getActivity().getString(R.string.menuoptionsnone));


                final String[] optionsMultipleChoiceType = getResources().getStringArray(R.array.menuoptions_multiplechoicetype);
                final String[] optionsMultipleChoiceSize = getResources().getStringArray(R.array.menuoptions_quizSize);
                final String[] optionsMultipleChoiceTimer = getResources().getStringArray(R.array.menuoptions_timer);

                txtView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupDropDownWindow(1,optionsMultipleChoiceType).showAsDropDown(v, -yadjustment, 0);
                    }
                });

                txtView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupDropDownWindow(2,optionsMultipleChoiceSize).showAsDropDown(v, -yadjustment, 0);
                    }
                });

                txtView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupDropDownWindow(3,optionsMultipleChoiceTimer).showAsDropDown(v, -yadjustment, 0);
                    }
                });

                break;

            case "fillintheblanks":

                ((TextView)view.findViewById(R.id.txtRow1)).setVisibility(View.GONE);
                ((TextView) view.findViewById(R.id.btnRow1)).setVisibility(View.GONE);

                txtShowBack.setText("Size: ");
                txtView2.setText(getActivity().getString(R.string.menuoptionsten));

                ((TextView)view.findViewById(R.id.txtRow3)).setVisibility(View.GONE);
                ((TextView) view.findViewById(R.id.btnRow3)).setVisibility(View.GONE);

                final String[] optionsFillintheBlanksSize = getResources().getStringArray(R.array.menuoptions_quizSize);

                txtView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupDropDownWindow(2,optionsFillintheBlanksSize).showAsDropDown(v, -yadjustment, 0);
                    }
                });

                break;

            default:
                view = getActivity().getLayoutInflater().inflate(R.layout.dialogmenupopup, null);
                break;

        }

        LinearLayout layout = (LinearLayout) view.findViewById(R.id.layoutid);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowColors().showAsDropDown(v, -yadjustment, 0);
            }
        });

        //Set up the colorblocks
        textViewColorBlock_grey = (TextView) view.findViewById(R.id.listitem_colors_1);
        textViewColorBlock_red = (TextView) view.findViewById(R.id.listitem_colors_2);
        textViewColorBlock_yellow = (TextView) view.findViewById(R.id.listitem_colors_3);
        textViewColorBlock_green = (TextView) view.findViewById(R.id.listitem_colors_4);

        if(mColorBlockMeasurables.getTotalCount()>0) {
            setColorBlocks(mColorBlockMeasurables
                    ,mAvailablePopupWidth
                    ,textViewColorBlock_grey
                    ,textViewColorBlock_red
                    ,textViewColorBlock_yellow
                    ,textViewColorBlock_green);

            mColorBlockMeasurables.setInitialSelectedColors();
            //After setting up the blocks, decide whether each block will be visible or not
            for(DropDownMenuOption option : mColorBlockMeasurables.getDropDownOptions()) {
                setColorBlockVisibility(option);
            }

        } else {

            textViewColorBlock_grey.setText("");
            textViewColorBlock_red.setText("");
            textViewColorBlock_yellow.setText("");
            textViewColorBlock_green.setText("");
        }

        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(quizType) {
                    case "flashcards":
                        mCallback.showFlashCardFragment(mTabNumber
                        , mMyListEntry
                        , txtView1.getText().toString()
                        , txtView2.getText().toString()
                        , mColorBlockMeasurables.getSelectedColorString());

                        dialog.dismiss();
                        break;
                    case "multiplechoice":
                        mCallback.showMultipleChoiceFragment(mTabNumber
                                , mMyListEntry
                                , txtView1.getText().toString()
                                , txtView2.getText().toString()
                                , txtView3.getText().toString()
                                , mColorBlockMeasurables.getSelectedColorString());

                        dialog.dismiss();
                        break;
                    case "fillintheblanks":
                        mCallback.showFillintheBlanksFragment(mTabNumber
                                , mMyListEntry
                                , txtView2.getText().toString()
                                , mColorBlockMeasurables.getSelectedColorString());

                        dialog.dismiss();
                        break;
                    default:
                        dialog.dismiss();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.show() ;
    }

    private PopupWindow popupDropDownWindow(int buttonNumber, String[] options) {

        final PopupWindow popupWindow = new PopupWindow(getActivity());
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popup_drawable));

        RecyclerView recyclerView = new RecyclerView(getContext());

        ArrayList<String> optionsArray = new ArrayList<String>(Arrays.asList(options));
        MenuDropDownPopupAdapter adapter = new MenuDropDownPopupAdapter(buttonNumber,optionsArray,mRxBus);

        mRxBus.toClickObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if(event instanceof DropDownMenuOption) {
                            DropDownMenuOption chosenOption = (DropDownMenuOption) event;
                            switch (chosenOption.getButtonNumber()) {
                                case 1:
                                    txtView1.setText(chosenOption.getChosenOption());
                                    break;
                                case 2:
                                    txtView2.setText(chosenOption.getChosenOption());
                                    break;
                                case 3:
                                    txtView3.setText(chosenOption.getChosenOption());
                                    break;
                            }

                        }
                        popupWindow.dismiss();
                    }

                });
        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        popupWindow.setContentView(recyclerView);
        popupWindow.setFocusable(true);
        popupWindow.setWidth(mAvailablePopupWidth);
        popupWindow.setClippingEnabled(false);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);


        return popupWindow;
    }

    private PopupWindow popupWindowColors() {

        final PopupWindow popupWindow = new PopupWindow(getActivity());
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popup_drawable));

        RecyclerView recyclerView = new RecyclerView(getContext());
        MenuDropDownColorsPopupAdapter adapter = new MenuDropDownColorsPopupAdapter(getContext(),mColorBlockMeasurables.getDropDownOptions(),mRxBus);

        mRxBus.toClickObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if(event instanceof DropDownMenuOption) {
                            DropDownMenuOption chosenOption = (DropDownMenuOption) event;
                            if(chosenOption.getButtonNumber() == 5) {
                                if(chosenOption.isColorSelected()) {
                                    mColorBlockMeasurables. updateSelectedColorsAdd(chosenOption.getChosenOption());
                                } else {
                                    mColorBlockMeasurables. updateSelectedColorsRemove(chosenOption.getChosenOption());
                                }
                                setColorBlockVisibility(chosenOption);
                            }

                        }
                        popupWindow.dismiss();
                    }

                });
        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        popupWindow.setContentView(recyclerView);
        popupWindow.setFocusable(true);
        popupWindow.setWidth(mAvailablePopupWidth);
        popupWindow.setClippingEnabled(false);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);


        return popupWindow;
    }

    //TODO share this with menuexpandable adapters (and share among adapters) globally
    public void setColorBlocks(ColorBlockMeasurables colorBlockMeasurables
            ,int availableWidth
            , TextView txtGrey
            , TextView txtRed
            , TextView txtYellow
            , TextView txtGreen) {

        Drawable drawablecolorblock1 = ContextCompat.getDrawable(getContext(), R.drawable.colorblock);
        Drawable drawablecolorblock2 = ContextCompat.getDrawable(getContext(), R.drawable.colorblock);
        Drawable drawablecolorblock3 = ContextCompat.getDrawable(getContext(), R.drawable.colorblock);
        Drawable drawablecolorblock4 = ContextCompat.getDrawable(getContext(), R.drawable.colorblock);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            drawablecolorblock1.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuGrey), PorterDuff.Mode.MULTIPLY);
            txtGrey.setBackground(drawablecolorblock1);
            drawablecolorblock2.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuRed), PorterDuff.Mode.MULTIPLY);
            txtRed.setBackground(drawablecolorblock2);
            drawablecolorblock3.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuYellow), PorterDuff.Mode.MULTIPLY);
            txtYellow.setBackground(drawablecolorblock3);
            drawablecolorblock4.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuGreen), PorterDuff.Mode.MULTIPLY);
            txtGreen.setBackground(drawablecolorblock4);

        } else {
            drawablecolorblock1.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuGrey), PorterDuff.Mode.MULTIPLY);
            txtGrey.setBackgroundDrawable(drawablecolorblock1);
            drawablecolorblock2.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuRed), PorterDuff.Mode.MULTIPLY);
            txtRed.setBackgroundDrawable(drawablecolorblock2);
            drawablecolorblock3.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuYellow), PorterDuff.Mode.MULTIPLY);
            txtYellow.setBackgroundDrawable(drawablecolorblock3);
            drawablecolorblock4.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuGreen), PorterDuff.Mode.MULTIPLY);
            txtGreen.setBackgroundDrawable(drawablecolorblock4);
        }

        txtGrey.setVisibility(View.GONE);
        txtRed.setVisibility(View.GONE);
        txtYellow.setVisibility(View.GONE);
        txtGreen.setVisibility(View.GONE);

        txtRed.setText(String.valueOf(colorBlockMeasurables.getRedCount()));
        txtYellow.setText(String.valueOf(colorBlockMeasurables.getYellowCount()));
        txtGreen.setText(String.valueOf(colorBlockMeasurables.getGreenCount()));

        if (colorBlockMeasurables.getRedCount()
                + colorBlockMeasurables.getYellowCount()
                + colorBlockMeasurables.getGreenCount() == 0) {
            txtGrey.setText(String.valueOf(colorBlockMeasurables.getTotalCount()));
            txtGrey.setVisibility(View.VISIBLE);
            txtGrey.setMinimumWidth(availableWidth);

        } else {

            int availableWidthRemaining = availableWidth;
            if(colorBlockMeasurables.getGreyCount()>0){
                txtGrey.setText(String.valueOf(colorBlockMeasurables.getGreyCount()));
                int dimenscore = colorBlockMeasurables.getGreyDimenscore(availableWidth);
                if(BuildConfig.DEBUG) {
                    Log.i(TAG,"dimenscoretotal: " + availableWidth);
                    Log.i(TAG,"grey/count: " + colorBlockMeasurables.getGreyCount() + "/" + colorBlockMeasurables.getTotalCount());
                    Log.i(TAG,"((float) grey / (float) count): " + ((float) colorBlockMeasurables.getGreyCount() / (float) colorBlockMeasurables.getTotalCount()));
                    Log.i(TAG,"Rounded score: " + dimenscore);
                }
                availableWidthRemaining = availableWidth-dimenscore;
                txtGrey.setMinimumWidth(dimenscore);
                txtGrey.setVisibility(View.VISIBLE);
            }

            if(colorBlockMeasurables.getRedCount()>0){
                txtRed.setText(String.valueOf(colorBlockMeasurables.getRedCount()));
                int dimenscore = colorBlockMeasurables.getRedDimenscore(availableWidth,availableWidthRemaining);

                availableWidthRemaining = availableWidthRemaining -dimenscore;
                txtRed.setMinimumWidth(dimenscore);
                txtRed.setVisibility(View.VISIBLE);

            }

            if(colorBlockMeasurables.getYellowCount()>0){
                txtYellow.setText(String.valueOf(colorBlockMeasurables.getYellowCount()));
                int dimenscore = colorBlockMeasurables.getYellowDimenscore(availableWidth,availableWidthRemaining);
                availableWidthRemaining = availableWidthRemaining -dimenscore;
                txtYellow.setMinimumWidth(dimenscore);
                txtYellow.setVisibility(View.VISIBLE);

            }

            if(colorBlockMeasurables.getGreenCount()>0){
                txtGreen.setText(String.valueOf(colorBlockMeasurables.getGreenCount()));
                int dimenscore = colorBlockMeasurables.getGreenDimenscore(availableWidth,availableWidthRemaining);
                if(BuildConfig.DEBUG) {
                    Log.i(TAG,"dimenscoretotal: " + availableWidth);
                    Log.i(TAG,"grey/count: " + colorBlockMeasurables.getGreenCount() + "/" + colorBlockMeasurables.getTotalCount());
                    Log.i(TAG,"((float) grey / (float) count): " + ((float) colorBlockMeasurables.getGreenCount() / (float) colorBlockMeasurables.getTotalCount()));
                    Log.i(TAG,"Rounded score: " + dimenscore);

                }
                txtGreen.setMinimumWidth(dimenscore);
                txtGreen.setVisibility(View.VISIBLE);
            }
        }

        if (colorBlockMeasurables.getTotalCount() == 0) {
            txtGrey.setVisibility(View.VISIBLE);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                drawablecolorblock1.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.white), PorterDuff.Mode.MULTIPLY);
                txtGrey.setBackground(drawablecolorblock1);
            } else {
                drawablecolorblock1.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.white), PorterDuff.Mode.MULTIPLY);
                txtGrey.setBackgroundDrawable(drawablecolorblock1);
            }
            txtGrey.setText(getContext().getString(R.string.empty));
            txtRed.setVisibility(View.GONE);
            txtYellow.setVisibility(View.GONE);
            txtGreen.setVisibility(View.GONE);
        }

    }


    public void setColorBlockVisibility(DropDownMenuOption option) {

            if(option.isColorSelected()) {
                switch (option.getChosenOption()) {
                    case "Grey":
                        textViewColorBlock_grey.setVisibility(View.VISIBLE);
                        break;
                    case "Red":
                        textViewColorBlock_red.setVisibility(View.VISIBLE);
                        break;
                    case "Yellow":
                        textViewColorBlock_yellow.setVisibility(View.VISIBLE);
                        break;
                    case "Green":
                        textViewColorBlock_green.setVisibility(View.VISIBLE);
                        break;
                }
            } else {
                if(option.isColorSelected()) {
                    switch (option.getChosenOption()) {
                        case "Grey":
                            textViewColorBlock_grey.setVisibility(View.GONE);
                            break;
                        case "Red":
                            textViewColorBlock_red.setVisibility(View.GONE);
                            break;
                        case "Yellow":
                            textViewColorBlock_yellow.setVisibility(View.GONE);
                            break;
                        case "Green":
                            textViewColorBlock_green.setVisibility(View.GONE);
                            break;
                    }
                }
            }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (DialogInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement mCallback");
        }
    }
}