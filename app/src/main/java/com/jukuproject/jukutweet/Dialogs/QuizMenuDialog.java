package com.jukuproject.jukutweet.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.MenuDropDownColorsPopupAdapter;
import com.jukuproject.jukutweet.Adapters.MenuDropDownPopupAdapter;
import com.jukuproject.jukutweet.Interfaces.QuizMenuDialogInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.DropDownMenuOption;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;
import java.util.Arrays;

import rx.functions.Action1;

import static com.jukuproject.jukutweet.Adapters.WordListExpandableAdapter.setColorBlocks;

/**
 * Popup dialog with option spinners for a quiz (or flashcards)
 * The choices are then passed on to the quiz activity
 */
public class QuizMenuDialog extends DialogFragment {

    private final String TAG = "Test-quizmendlog";
    public QuizMenuDialogInteractionListener mCallback;

    private String quizType;
    private int mTabNumber;
    private int mCurrentExpandedPosition;
    private MyListEntry mMyListEntry;
    private UserInfo mUserInfo;
    private boolean mSingleUser; // Signifies whether menu is for a normal mylist, or a single users saved tweet list

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
            ,int currentExpandedPosition
            , MyListEntry myListEntry
            , ColorBlockMeasurables colorBlockMeasurables
            , int availablePopupWidth) {

        QuizMenuDialog frag = new QuizMenuDialog();
        Bundle args = new Bundle();
        args.putString("quizType", quizType);
        args.putInt("tabNumber",tabNumber);
        args.putInt("currentExpandedPosition",currentExpandedPosition);
        args.putParcelable("myListEntry",myListEntry);
        args.putParcelable("colorBlockMeasurables",colorBlockMeasurables);
        args.putInt("availablePopupWidth",availablePopupWidth);
        args.putBoolean("singleUserFragment",false);
        frag.setArguments(args);
        return frag;
    }

    /**
     * Quizzes started from {@link com.jukuproject.jukutweet.Fragments.TweetListSingleUserFragment} require
     * different datasets than those called from {@link com.jukuproject.jukutweet.Fragments.WordListFragment} or
     * {@link com.jukuproject.jukutweet.Fragments.TweetListFragment}
     */
    public static QuizMenuDialog newSingleUserInstance(String quizType
            , int tabNumber
            ,int currentExpandedPosition
            , UserInfo userInfo
            , ColorBlockMeasurables colorBlockMeasurables
            , int availablePopupWidth) {

        QuizMenuDialog frag = new QuizMenuDialog();
        Bundle args = new Bundle();
        args.putString("quizType", quizType);
        args.putInt("tabNumber",tabNumber);
        args.putInt("currentExpandedPosition",currentExpandedPosition);
        args.putParcelable("userInfo",userInfo);
        args.putParcelable("colorBlockMeasurables",colorBlockMeasurables);
        args.putInt("availablePopupWidth",availablePopupWidth);
        args.putBoolean("singleUserFragment",true);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mSingleUser = getArguments().getBoolean("singleUserFragment");
        quizType = getArguments().getString("quizType");
        mMyListEntry = getArguments().getParcelable("myListEntry");
        mUserInfo = getArguments().getParcelable("userInfo");
        mColorBlockMeasurables = getArguments().getParcelable("colorBlockMeasurables");
        mAvailablePopupWidth = getArguments().getInt("availablePopupWidth");
        mTabNumber = getArguments().getInt("tabNumber");
        mCurrentExpandedPosition = getArguments().getInt("currentExpandedPosition");

        final int yadjustment = 5;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View  view = getActivity().getLayoutInflater().inflate(R.layout.dialogmenupopup, null);

        txtView1 = (TextView) view.findViewById(R.id.btnRow1);
        txtView2 = (TextView) view.findViewById(R.id.btnRow2);
        txtView3 = (TextView) view.findViewById(R.id.btnRow3);

        txtView1.setWidth(mAvailablePopupWidth);
        txtView2.setWidth(mAvailablePopupWidth);
        txtView3.setWidth(mAvailablePopupWidth);

        TextView txtViewRow1 = (TextView) view.findViewById(R.id.txtRow1);
        TextView txtViewRow2 = (TextView) view.findViewById(R.id.txtRow2);
        TextView txtViewRow3 = (TextView) view.findViewById(R.id.txtRow3);

        switch(quizType) {
            case "flashcards":
                txtViewRow1.setText(getString(R.string.flashcards_front_text));
                txtView1.setText(getActivity().getString(R.string.menuoptionskanji));

                txtViewRow2.setText(getString(R.string.flashcards_back_text));
                txtView2.setText(getActivity().getString(R.string.menuoptionsdefinition));

                view.findViewById(R.id.txtRow3).setVisibility(View.GONE);
                view.findViewById(R.id.btnRow3).setVisibility(View.GONE);

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
                txtViewRow1.setText(getString(R.string.menuoption_text_type));
                txtView1.setText(getActivity().getString(R.string.menuoptionskanjitodef));

                txtViewRow2.setText(getString(R.string.menuoption_text_size));
                txtView2.setText(getString(R.string.menuoptionsten));

                view.findViewById(R.id.txtRow3).setVisibility(View.VISIBLE);
                view.findViewById(R.id.btnRow3).setVisibility(View.VISIBLE);

                txtViewRow3.setText(getString(R.string.menuoption_text_timer));
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

                txtView3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupDropDownWindow(3,optionsMultipleChoiceTimer).showAsDropDown(v, -yadjustment, 0);
                    }
                });

                break;

            case "fillintheblanks":

                view.findViewById(R.id.txtRow1).setVisibility(View.GONE);
                view.findViewById(R.id.btnRow1).setVisibility(View.GONE);

                txtViewRow2.setText(getString(R.string.menuoption_text_size));
                txtView2.setText(getActivity().getString(R.string.menuoptionsten));

                view.findViewById(R.id.txtRow3).setVisibility(View.GONE);
                view.findViewById(R.id.btnRow3).setVisibility(View.GONE);

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

        textViewColorBlock_grey.setGravity(Gravity.CENTER);
        textViewColorBlock_red.setGravity(Gravity.CENTER);
        textViewColorBlock_yellow.setGravity(Gravity.CENTER);
        textViewColorBlock_green.setGravity(Gravity.CENTER);

        if(mColorBlockMeasurables.getTotalCount()>0) {
            setColorBlocks(getContext()
                    ,mColorBlockMeasurables
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
                        if(mSingleUser) {
                            mCallback.showSingleUserFlashCardFragment(mTabNumber
                                    , mUserInfo
                                    , txtView1.getText().toString()
                                    , txtView2.getText().toString()
                                    , mColorBlockMeasurables.getSelectedColorString());
                        } else {
                            mCallback.showFlashCardFragment(mTabNumber
                                    , mMyListEntry
                                    , txtView1.getText().toString()
                                    , txtView2.getText().toString()
                                    , mColorBlockMeasurables.getSelectedColorString());
                        }

                        dialog.dismiss();
                        break;
                    case "multiplechoice":
                        if(mSingleUser) {
                            mCallback.goToSingleUserQuizActivityMultipleChoice(mTabNumber
                                    , mUserInfo
                                    , mCurrentExpandedPosition
                                    , txtView1.getText().toString()
                                    , txtView2.getText().toString()
                                    , txtView3.getText().toString()
                                    , mColorBlockMeasurables.getSelectedColorString());
                        } else {
                            Log.i(TAG,"QUIZMEN tabnumber: " + mTabNumber);
                            mCallback.goToQuizActivityMultipleChoice(mTabNumber
                                    , mMyListEntry
                                    , mCurrentExpandedPosition
                                    , txtView1.getText().toString()
                                    , txtView2.getText().toString()
                                    , txtView3.getText().toString()
                                    , mColorBlockMeasurables.getSelectedColorString());
                        }

                        dialog.dismiss();
                        break;
                    case "fillintheblanks":
                        if(mSingleUser) {
                            mCallback.goToSingleUserQuizActivityFillintheBlanks(mTabNumber
                                    , mUserInfo
                                    , mCurrentExpandedPosition
                                    , txtView2.getText().toString()
                                    , mColorBlockMeasurables.getSelectedColorString());

                        } else {
                            mCallback.goToQuizActivityFillintheBlanks(mTabNumber
                                    , mMyListEntry
                                    , mCurrentExpandedPosition
                                    , txtView2.getText().toString()
                                    , mColorBlockMeasurables.getSelectedColorString());
                        }

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

    /**
     * Popup dropdown shown when user clicks one of the "textviewrow" textviews with quiz options
     * @param buttonNumber number identifying which textview was clicked (1,2 or 3)
     * @param options array of possible options for that textview button
     * @return popup window with options to select
     */
    private PopupWindow popupDropDownWindow(int buttonNumber, String[] options) {

        final PopupWindow popupWindow = new PopupWindow(getActivity());
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popup_drawable));

        RecyclerView recyclerView = new RecyclerView(getContext());

        ArrayList<String> optionsArray = new ArrayList<>(Arrays.asList(options));
        MenuDropDownPopupAdapter adapter = new MenuDropDownPopupAdapter(buttonNumber,optionsArray,mRxBus);

        mRxBus.toClickObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if(event instanceof DropDownMenuOption) {
                            DropDownMenuOption chosenOption = (DropDownMenuOption) event;
                            Log.d(TAG,"menudialog result: " + chosenOption.getChosenOption() + ", num: " + chosenOption.getButtonNumber());
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

    /**
     * Popup window dropdown where user can choose colorblocks representing which word color categories
     * will be included in the quiz (via SelectedColorString)
     * @return colorblock popup dropdown window
     */
    private PopupWindow popupWindowColors() {

        final PopupWindow popupWindow = new PopupWindow(getActivity());
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popup_drawable));

        RecyclerView recyclerView = new RecyclerView(getContext());
        MenuDropDownColorsPopupAdapter adapter = new MenuDropDownColorsPopupAdapter(getContext(),mColorBlockMeasurables.getDropDownOptions(),mColorBlockMeasurables.getSelectedColorString(),mRxBus);

        mRxBus.toClickObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if(event instanceof DropDownMenuOption) {
                            DropDownMenuOption chosenOption = (DropDownMenuOption) event;
                            if(chosenOption.getButtonNumber() == 5) {

                                if(chosenOption.isColorSelected()) {
                                    mColorBlockMeasurables.updateSelectedColorsAdd(chosenOption.getChosenOption());
                                } else if(mColorBlockMeasurables.getSelectedColorString().length() > 0){
                                    mColorBlockMeasurables.updateSelectedColorsRemove(chosenOption.getChosenOption());
                                }
                                setColorBlockVisibility(chosenOption);
                            }
                        Log.d(TAG,"colorstring: " + mColorBlockMeasurables.getSelectedColorString());
                        }
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

//    /**
//     * Sets up the colorblocks that appear as the select colors "button" which is really
//     * just a linear layout with textviews representing available word colors
//     * @param colorBlockMeasurables color block measurable object for the given set (with counts etc)
//     * @param availableWidth maximum available width for the group of colorblocks
//     * @param txtGrey  textView representing the "grey" colorblock
//     * @param txtRed textView representing the "red" colorblock
//     * @param txtYellow textView representing the "yellow" colorblock
//     * @param txtGreen textView representing the "green" colorblock
//     */
//    public void setColorBlocks(ColorBlockMeasurables colorBlockMeasurables
//            ,int availableWidth
//            , TextView txtGrey
//            , TextView txtRed
//            , TextView txtYellow
//            , TextView txtGreen) {
//
//        Drawable drawablecolorblock1 = ContextCompat.getDrawable(getContext(), R.drawable.colorblock);
//        Drawable drawablecolorblock2 = ContextCompat.getDrawable(getContext(), R.drawable.colorblock);
//        Drawable drawablecolorblock3 = ContextCompat.getDrawable(getContext(), R.drawable.colorblock);
//        Drawable drawablecolorblock4 = ContextCompat.getDrawable(getContext(), R.drawable.colorblock);
//
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            drawablecolorblock1.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuGrey), PorterDuff.Mode.MULTIPLY);
//            txtGrey.setBackground(drawablecolorblock1);
//            drawablecolorblock2.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuRed), PorterDuff.Mode.MULTIPLY);
//            txtRed.setBackground(drawablecolorblock2);
//            drawablecolorblock3.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuYellow), PorterDuff.Mode.MULTIPLY);
//            txtYellow.setBackground(drawablecolorblock3);
//            drawablecolorblock4.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuGreen), PorterDuff.Mode.MULTIPLY);
//            txtGreen.setBackground(drawablecolorblock4);
//        } else {
//            drawablecolorblock1.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuGrey), PorterDuff.Mode.MULTIPLY);
//            txtGrey.setBackgroundDrawable(drawablecolorblock1);
//            drawablecolorblock2.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuRed), PorterDuff.Mode.MULTIPLY);
//            txtRed.setBackgroundDrawable(drawablecolorblock2);
//            drawablecolorblock3.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuYellow), PorterDuff.Mode.MULTIPLY);
//            txtYellow.setBackgroundDrawable(drawablecolorblock3);
//            drawablecolorblock4.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorJukuGreen), PorterDuff.Mode.MULTIPLY);
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
//                if(BuildConfig.DEBUG) {
//                    Log.i(TAG,"dimenscoretotal: " + availableWidth);
//                    Log.i(TAG,"grey/count: " + colorBlockMeasurables.getGreyCount() + "/" + colorBlockMeasurables.getTotalCount());
//                    Log.i(TAG,"((float) grey / (float) count): " + ((float) colorBlockMeasurables.getGreyCount() / (float) colorBlockMeasurables.getTotalCount()));
//                    Log.i(TAG,"Rounded score: " + dimenscore);
//                }
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
//                if(BuildConfig.DEBUG) {
//                    Log.i(TAG,"dimenscoretotal: " + availableWidth);
//                    Log.i(TAG,"grey/count: " + colorBlockMeasurables.getGreenCount() + "/" + colorBlockMeasurables.getTotalCount());
//                    Log.i(TAG,"((float) grey / (float) count): " + ((float) colorBlockMeasurables.getGreenCount() / (float) colorBlockMeasurables.getTotalCount()));
//                    Log.i(TAG,"Rounded score: " + dimenscore);
//
//                }
//                txtGreen.setMinimumWidth(dimenscore);
//                txtGreen.setVisibility(View.VISIBLE);
//            }
//        }
//
//        if (colorBlockMeasurables.getTotalCount() == 0) {
//            txtGrey.setVisibility(View.VISIBLE);
//            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                drawablecolorblock1.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.white), PorterDuff.Mode.MULTIPLY);
//                txtGrey.setBackground(drawablecolorblock1);
//            } else {
//                drawablecolorblock1.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.white), PorterDuff.Mode.MULTIPLY);
//                txtGrey.setBackgroundDrawable(drawablecolorblock1);
//            }
//            txtGrey.setText(getContext().getString(R.string.empty));
//            txtRed.setVisibility(View.GONE);
//            txtYellow.setVisibility(View.GONE);
//            txtGreen.setVisibility(View.GONE);
//        }
//
//    }

    /**
     * When use clicks on a color in the {@link #popupWindowColors()}, if the color is
     * selected this method runs to set that color visible in the "colorblocks button" layout. Likewise,
     * it hides that textview's color if it should not be visible
     * @param option DropDownMenu object representing the dropdown menu option that was chosen
     */
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
//                if(option.isColorSelected()) {
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


    @Override
    public void onCancel(DialogInterface dialog) {
        if(!mSingleUser) {
            mCallback.showFab(true);
        }
        super.onCancel(dialog);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (QuizMenuDialogInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement mCallback");
        }
    }

}