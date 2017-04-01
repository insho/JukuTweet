package com.jukuproject.jukutweet.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

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
import java.util.List;

import rx.functions.Action1;

import static com.jukuproject.jukutweet.Adapters.CopyMyListItemsAdapter.setAppCompatCheckBoxColors;



/**
 * Created by JukuProject on 11/21/2016
 *
 * Popup dialog with option spinners for a quiz
 * The choices are then passed on to the quiz activity
 */

public class QuizMenuDialog extends DialogFragment {

    private String TAG = "Test-quizmendlog";
    public DialogInteractionListener mDialogCallback;

    private String quizType;
    private MyListEntry mMyListEntry;
//    private UserInfo mUserInfo;
//    private ColorThresholds mColorThresholds;
    private ColorBlockMeasurables mColorBlockMeasurables;
    private int mAvailablePopupWidth;
    private RxBus mRxBus = new RxBus();


    private String colorsArray[];  //Array of colors
//    private String typesArray[];  //Array of quiz types
//    private String sizeArray[];  //Array of quiz sizes
//    private String timerArray[];

    private PopupWindow popupWindowColors;

    private TextView textViewColorBlock_grey;
    private TextView textViewColorBlock_red;
    private TextView textViewColorBlock_yellow;
    private TextView textViewColorBlock_green;

    private TextView txtView1;
    private TextView txtView2;
    private TextView txtView3;
//    private String btn1Value;
//    private String btn2Value;
//    private String btn3Value;

    //Final Items passed on to quiz activity
    private List<Integer> extra_blocknumbers = new ArrayList<Integer>();  //This one holds the sql data we pull about each block
    private  ArrayList<Integer> checkedlist = new ArrayList<Integer>();

    private static boolean debug = false;

    public static QuizMenuDialog newInstance(String quizType
            , MyListEntry myListEntry
            , ColorBlockMeasurables colorBlockMeasurables
            , int availablePopupWidth) {

        QuizMenuDialog frag = new QuizMenuDialog();
        Bundle args = new Bundle();
        args.putString("quizType", quizType);
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

        //TODO make this a typed value or resource
        final int yadjustment = 5;

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View  view = getActivity().getLayoutInflater().inflate(R.layout.dialogmenupopup, null);

        txtView1 = (TextView) view.findViewById(R.id.btnRow1);
        txtView2 = (TextView) view.findViewById(R.id.btnRow2);
        txtView3 = (TextView) view.findViewById(R.id.btnRow3);

        switch(quizType) {
            case "flashcards":

                TextView txtShowFront = (TextView) view.findViewById(R.id.txtRow1);
                txtShowFront.setText("Front: ");
                txtView1.setText(getActivity().getString(R.string.menuoptionskanji));

                TextView txtShowBack= (TextView) view.findViewById(R.id.txtRow2);
                txtShowBack.setText("Back: ");
                txtView2.setText(getActivity().getString(R.string.menuoptionsdefinition));

                ((TextView)view.findViewById(R.id.txtRow3)).setVisibility(View.GONE);
                ((TextView) view.findViewById(R.id.btnRow3)).setVisibility(View.GONE);


                txtView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupDropDownWindow(1).showAsDropDown(v, -yadjustment, 0);
                    }
                });

                txtView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupDropDownWindow(2).showAsDropDown(v, -yadjustment, 0);
                    }
                });

                break;
            case "multiplechoice":

                break;
            default:
                view = getActivity().getLayoutInflater().inflate(R.layout.dialogmenupopup, null);
                break;

        }

//        View view = getActivity().getLayoutInflater().inflate(R.layout.dialogmenupopup, null);

        LinearLayout layout = (LinearLayout) view.findViewById(R.id.layoutid);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowColors.showAsDropDown(v, -yadjustment, 0);
            }
        });

        //Set up spinner TextViews
//        TextView buttonShowDropDown_Type = (TextView) view.findViewById(R.id.listitem_type_spinnerstyle);
//        TextView buttonShowDropDown = (TextView) view.findViewById(R.id.listitem_size_spinnerstyle);
//        TextView buttonShowDropDown_Timer = (TextView) view.findViewById(R.id.listitem_timer_spinnerstyle);
//
//        TextView textdescType = (TextView) view.findViewById(R.id.optionspopupTextType);
//        TextView textdescSize = (TextView) view.findViewById(R.id.optionspopupTextSize);
//        TextView textdescTimer = (TextView)view.findViewById(R.id.optionspopupTextTimer);
//
//        if(quizType.equalsIgnoreCase("flashcards")) {
//            buttonShowDropDown_Type.setText(getActivity().getString(R.string.menuoptionskanji));
//            buttonShowDropDown_Type.setTag(1);
//
//            buttonShowDropDown_Size.setText(getActivity().getString(R.string.menuoptionsdefinition));
//            buttonShowDropDown_Size.setTag(4);
//
//            String front = "Front: ";
//            String back = "Back: ";
//            textdescType.setText(front);
//            textdescSize.setText(back);
//
//            textdescTimer.setVisibility(TextView.GONE);
//            buttonShowDropDown_Timer.setVisibility(View.GONE);
//
//        }
//        else if (quizType.equalsIgnoreCase("fillinsentences")){
//            buttonShowDropDown_Type.setVisibility(TextView.GONE);
//            textdescType.setVisibility(TextView.GONE);
//
//            buttonShowDropDown_Size.setText(getActivity().getString(R.string.menuoptionsten));
//            buttonShowDropDown_Size.setTag(10);
//
//            textdescTimer.setVisibility(TextView.GONE);
//            buttonShowDropDown_Timer.setVisibility(View.GONE);
//
//        } else if(quizType.equalsIgnoreCase("wordbuilder")) {
//            buttonShowDropDown_Type.setText(getActivity().getString(R.string.menuoptionswordbuildertype));
//            buttonShowDropDown_Type.setTag(0);
//
//            textdescSize.setVisibility(View.GONE);
//            buttonShowDropDown_Size.setVisibility(View.GONE);
//
//            textdescTimer.setVisibility(TextView.VISIBLE);
//            buttonShowDropDown_Timer.setVisibility(View.VISIBLE);
//
//            buttonShowDropDown_Timer.setText(getActivity().getString(R.string.menuoptionssixty));
//            buttonShowDropDown_Timer.setTag(60);
//        } else if(quizType.equalsIgnoreCase("wordmatch")) {
//            buttonShowDropDown_Type.setText(getActivity().getString(R.string.menuoptionskanjitokana));
//            buttonShowDropDown_Type.setTag(1);
//
//            buttonShowDropDown_Size.setText(getActivity().getString(R.string.menuoptionsten));
//            buttonShowDropDown_Size.setTag(10);
//
//            textdescTimer.setVisibility(TextView.VISIBLE);
//            buttonShowDropDown_Timer.setVisibility(View.VISIBLE);
//
//            buttonShowDropDown_Timer.setText(getActivity().getString(R.string.menuoptionsnone));
//            buttonShowDropDown_Timer.setTag(0);
//
//        } else {
//            buttonShowDropDown_Type.setText(getActivity().getString(R.string.menuoptionskanjitodef));
//            buttonShowDropDown_Type.setTag(0);
//
//            buttonShowDropDown_Size.setText(getActivity().getString(R.string.menuoptionsten));
//            buttonShowDropDown_Size.setTag(10);
//
//            textdescTimer.setVisibility(TextView.VISIBLE);
//            buttonShowDropDown_Timer.setVisibility(View.VISIBLE);
//
//            buttonShowDropDown_Timer.setText(getActivity().getString(R.string.menuoptionsnone));
//            buttonShowDropDown_Timer.setTag(0);
//
//        }





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

        } else {

            textViewColorBlock_grey.setText("");
            textViewColorBlock_red.setText("");
            textViewColorBlock_yellow.setText("");
            textViewColorBlock_green.setText("");
        }

        List<String> typeslist = new ArrayList<String>();
        List<String> sizelist = new ArrayList<String>();
        List<String> timerlist = new ArrayList<String>();





//
//        if(quizType.equalsIgnoreCase("flashcards")) {
//            typeslist.add("Kanji::1");
//            typeslist.add("Kana::5");
//            typeslist.add("Definition::4");
//            sizelist.add("Kanji::1");
//            sizelist.add("Kana::5");
//            sizelist.add("Definition::4");
//
//        } else if(quizType.equalsIgnoreCase("fillinsentences")) {
//
//            sizelist.add("5::5");
//            sizelist.add("10::10");
//            sizelist.add("15::15");
//            sizelist.add("20::20");
//            sizelist.add("25::25");
//            sizelist.add("30::30");
//            sizelist.add("40::40");
//            sizelist.add("50::50");
//
//        } else if(quizType.equalsIgnoreCase("wordbuilder")) {
//
//            typeslist.add("Repeating::0");
//            typeslist.add("Straight::1");
//
//            timerlist.add("None::0");
//            timerlist.add("30 Seconds::30");
//            timerlist.add("60 Seconds::60");
//            timerlist.add("90 Seconds::90");
//            timerlist.add("120 Seconds::120");
//            timerlist.add("180 Seconds::180");
//            timerlist.add("240 Seconds::240");
//            timerlist.add("300 Seconds::300");
//
//
//        } else {
//            if(!quizType.equalsIgnoreCase("wordmatch")) {
//                typeslist.add("Kanji to Definition::0");
//            }
//            typeslist.add("Kanji to Kana::1");
//            typeslist.add("Kana to Kanji::2");
//            typeslist.add("Definition to Kanji::3");
//
//            sizelist.add("5::5");
//            sizelist.add("10::10");
//            sizelist.add("15::15");
//            sizelist.add("20::20");
//            sizelist.add("25::25");
//            sizelist.add("30::30");
//            sizelist.add("40::40");
//            sizelist.add("50::50");
//
//
//            timerlist.add("None::0");
//            timerlist.add("5 Seconds::5");
//            timerlist.add("6 Seconds::6");
//            timerlist.add("7 Seconds::7");
//            timerlist.add("8 Seconds::8");
//            timerlist.add("9 Seconds::9");
//            timerlist.add("10 Seconds::10");
//            timerlist.add("15 Seconds::15");
//            timerlist.add("20 Seconds::20");
//        }

        List<String> colorsList = new ArrayList<String>();
        colorsList.add("Grey::1");
        colorsList.add("Red::2");
        colorsList.add("Yellow::3");
        colorsList.add("Green::4");

        // convert to simple array
        colorsArray = new String[colorsList.size()];
        colorsList.toArray(colorsArray);

//        typesArray = new String[typeslist.size()];
//        typeslist.toArray(typesArray);
//
//        sizeArray = new String[sizelist.size()];
//        sizelist.toArray(sizeArray);
//
//        timerArray = new String[timerlist.size()];
//        timerlist.toArray(timerArray);

        // initialize pop up window
        popupWindowColors = popupWindowColors();

        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                if(destination.equalsIgnoreCase("flashcards")) {
//                    sendMessage_FlashCards(level, block, Integer.parseInt(buttonShowDropDown_Type.getTag().toString()), Integer.parseInt(buttonShowDropDown_Size.getTag().toString()), mylists, listname, sys);
//                } else if(destination.equalsIgnoreCase("fillinsentences")) {
//                    sendMessage_FillInSentences(level, block, Integer.parseInt(buttonShowDropDown_Size.getTag().toString()),mylists, listname,sys);
//                } else if(destination.equalsIgnoreCase("wordbuilder")) {
//                    sendMessage_WordBuilder(level, block, 0, Integer.parseInt(buttonShowDropDown_Type.getTag().toString()), mylists, listname, sys, Integer.parseInt(buttonShowDropDown_Timer.getTag().toString()), mylistposition);
//                } else if(destination.equalsIgnoreCase("wordmatch")) {
//                    sendMessage_WordMatch(level, block, Integer.parseInt(buttonShowDropDown_Size.getTag().toString()), Integer.parseInt(buttonShowDropDown_Type.getTag().toString()), mylists, listname, sys, Integer.parseInt(buttonShowDropDown_Timer.getTag().toString()), mylistposition);
//                }  else {
//                    sendMessage_MultChoice(level, block, Integer.parseInt(buttonShowDropDown_Size.getTag().toString()), Integer.parseInt(buttonShowDropDown_Type.getTag().toString()),mylists, listname,sys,Integer.parseInt(buttonShowDropDown_Timer.getTag().toString()),mylistposition);
//                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog d = builder.show();

//        db.close();
//        helper.close();
        return d ;
    }

    private PopupWindow popupDropDownWindow(int buttonNumber) {

        final PopupWindow popupWindow = new PopupWindow(getActivity());
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popup_drawable));

        RecyclerView recyclerView = new RecyclerView(getContext());
        String[] optionsStringArray = getResources().getStringArray(R.array.menuoptions_flashcards);
        ArrayList<String> optionsArray = new ArrayList<String>(Arrays.asList(optionsStringArray));
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
//
//        switch(buttonNumber) {
//            case 1:
//                mRxBus.toClickObserverable()
//                        .subscribe(new Action1<Object>() {
//                            @Override
//                            public void call(Object event) {
//                                if(event instanceof String) {
//                                    String chosenOption = (String) event;
//                                    txtView1.setText(chosenOption);
//                                }
//
//                            }
//
//                        });
//                recyclerView.setAdapter(adapter);
//
//                break;
//            case 2:
//                mRxBus.toClickObserverable()
//                        .subscribe(new Action1<Object>() {
//                            @Override
//                            public void call(Object event) {
//                                if(event instanceof String) {
//                                    String chosenOption = (String) event;
//                                    txtView2.setText(chosenOption);
//                                }
//
//                            }
//
//                        });
//                recyclerView.setAdapter(adapter);
//
//                break;
//            case 3:
//                mRxBus.toClickObserverable()
//                        .subscribe(new Action1<Object>() {
//                            @Override
//                            public void call(Object event) {
//                                if(event instanceof String) {
//                                    String chosenOption = (String) event;
//                                    txtView3.setText(chosenOption);
//                                }
//
//                            }
//
//                        });
//                recyclerView.setAdapter(adapter);
//
//                break;
//
//        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        popupWindow.setContentView(recyclerView);
        popupWindow.setFocusable(true);
        popupWindow.setWidth(mAvailablePopupWidth);
        popupWindow.setClippingEnabled(false);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);


        return popupWindow;
    }
//
//    public class DropDownAdapter extends ArrayAdapter<String> {
//
//        RxBus mRxBus;
//
//        public DropDownAdapter(RxBus rxBus) {
//            this.mRxBus = rxBus;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//
//            // ...
//
//            // Lookup view for data population
//
//            Button btButton = (Button) convertView.findViewById(R.id.btButton);
//
//            // Cache row position inside the button using `setTag`
//
//            btButton.setTag(position);
//
//            // Attach the click event handler
//
//            btButton.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//
//                public void onClick(View view) {
//
//                    int position = (Integer) view.getTag();
//
//                    // Access the row position here to get the correct data item
//
//                    User user = getItem(position);
//
//                    // Do what you want here...
//
//                }
//
//            });
//
//            // ... other view population as needed...
//
//            // Return the completed view
//
//            return convertView;
//
//        }
//
//    }

    private PopupWindow popupWindowColors() {

        PopupWindow popupWindow = new PopupWindow(getActivity());

        ListView listViewColors = new ListView(getActivity());
        listViewColors.setAdapter(listadapter_colors(colorsArray, extra_blocknumbers));

        popupWindow.setFocusable(true);
        popupWindow.setWidth(mAvailablePopupWidth);
        popupWindow.setClippingEnabled(false);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(listViewColors);
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popup_drawable));
        return popupWindow;
    }


//
////    private ArrayAdapter<String> testAdapter = new ArrayAdapter<String>()
//    //TODO SPLIT OUT??
//    private ArrayAdapter<String> listadapter_dropdown(String Array[], String buttonNumber) {
//
////        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.dialogmenu_spinneritem, Array) {
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, Array) {
//
//            @Override
//            public View getView(final int position, View convertView, ViewGroup parent) {
//
//                // setting the ID and text for every items in the list
////                String item = getItem(position);
//                TextView txt1 = (TextView) convertView.findViewById(android.R.id.text1);
//                txt1.setText(getItem(position));
////                txt1.setOnClickListener(new View.OnClickListener() {
////                    @Override
////                    public void onClick(View v) {
////
////                    }
////                });
//                txt1.setClickable(false);
//                convertView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        switch (btnNumber) {
//                            case 1:
//                                break;
//                            case 2:
//                                break;
//                            case 3:
//                                break;
//                            asdf
//                        }
//                    }
//                });
//                LinearLayout layout = new LinearLayout(getContext());
//                layout.setOrientation(LinearLayout.HORIZONTAL);
//                layout.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT));
//                layout.getLayoutParams().height = (int) (35.0f * scale + 0.5f);
//                TextView textView = new TextView(getActivity());
////                textView.setId(R.id.listdropdown_insidelayout_textview);
//                textView.setText(text);
//                textView.setTag(id);
//                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
////                textView.setPadding(msmallleftpadding, msmallmarginpadding, msmallmarginpadding, msmallmarginpadding);
//                textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
//
//                layout.addView(textView);
//
//                return layout;
//            }
//        };
//
//        return adapter;
//    }
//


    private ArrayAdapter<String> listadapter_colors(String colorArray[], final List<Integer> blocknumbers) {

        return new ArrayAdapter<String>(getActivity(), R.layout.dialogmenu_spinneritem, colorArray) {


            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {

                // setting the ID and text for every items in the list
                String item = getItem(position);
                String[] itemArr = item.split("::");
                String text = itemArr[0];
                final String id = itemArr[1];

                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.HORIZONTAL);

                final AppCompatCheckBox checkbox = new AppCompatCheckBox(getActivity());
                final TextView textView = new TextView(getActivity());
                final Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.colorblock);

                String grey = "";
                String red = "";
                String yellow = "";
                String green = "";

                if (blocknumbers.size() > 0) {
                    //count = String.valueOf(blocknumbers.get(0));
                    red = String.valueOf(blocknumbers.get(2));
                    yellow = String.valueOf(blocknumbers.get(3));
                    green = String.valueOf(blocknumbers.get(4));

                    //Basically if all colors are 0, it means user hasn't done this block, so "Grey" == "total count of block"
                    if (blocknumbers.get(2) + blocknumbers.get(3) + blocknumbers.get(4) == 0) {
                        grey = String.valueOf(blocknumbers.get(0));
                    } else {
                        grey = String.valueOf(blocknumbers.get(1));
                    }
                }


                if (checkedlist.get(position) >= 1) {
                    checkbox.setChecked(true);
                } else {
                    checkbox.setChecked(false);
                }

                switch (text) {
                    case "Grey":
                        drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorJukuGrey), PorterDuff.Mode.MULTIPLY);
                        textView.setText(grey);
                        break;
                    case "Red":
                        drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorJukuRed), PorterDuff.Mode.MULTIPLY);
                        textView.setText(red);
                        break;
                    case "Yellow":
                        drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorJukuYellow), PorterDuff.Mode.MULTIPLY);
                        textView.setText(yellow);
                        break;
                    case "Green":
                        drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorJukuGreen), PorterDuff.Mode.MULTIPLY);
                        textView.setText(green);
                        break;
                }


                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    textView.setBackground(drawable);
                } else {
                    textView.setBackgroundDrawable(drawable);
                }

                checkbox.setTag(id);
                checkbox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

                //TODO MAKE IT WORK
                setAppCompatCheckBoxColors(checkbox, ContextCompat.getColor(getActivity(), android.R.color.black), ContextCompat.getColor(getActivity(), android.R.color.black));

//                if (debug) {
//                    Log.d(TAG, "checkbox scale : " + scale);
//                }
//                ;
//                checkbox.setPadding(checkbox.getPaddingLeft() + (int) (5.0f * scale + 0.5f),
//                        checkbox.getPaddingLeft() + (int) (10.0f * scale + 0.5f),
//                        checkbox.getPaddingRight(),
//                        checkbox.getPaddingLeft() + (int) (10.0f * scale + 0.5f));

                layout.addView(checkbox);
                layout.addView(textView);

                checkbox.measure(0, 0);
                if (debug) {
                    Log.d(TAG, "checkbox width: " + checkbox.getWidth());
                }
                ;
                checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        int checkedcount = 0;
                        for (int x = 0; x < checkedlist.size(); x++) {
                            if (checkedlist.get(x) > 0) {
                                checkedcount += 1;
                            }
                        }

                        if (debug) {
                            Log.d(TAG, "checkedcount = " + checkedcount);
                        }

                        if (!checkbox.isChecked() && checkedcount <= 1) {

                            checkbox.setChecked(true);
                            checkedlist.set(position, Integer.parseInt(id));


                        } else if (isChecked) {
                            checkbox.setChecked(true);
                            checkedlist.set(position, Integer.parseInt(id));
                        } else {
                            checkbox.setChecked(false);
                            checkedlist.set(position, 0);
                        }

                        assigncolorblocks();

                    }
                });

                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int checkedcount = 0;
                        for (int x = 0; x < checkedlist.size(); x++) {
                            if (checkedlist.get(x) > 0) {
                                checkedcount += 1;
                            }

                        }

                        if (debug) {
                            Log.d(TAG, "checkedcount = " + checkedcount);
                        }

                        if (checkbox.isChecked() && checkedcount <= 1) {

                        } else if (checkbox.isChecked()) {
                            checkbox.setChecked(false);
                            checkedlist.set(position, 0);
                        } else {

                            checkbox.setChecked(true);
                            checkedlist.set(position, Integer.parseInt(id));
                        }

                        assigncolorblocks();

                    }
                });


                return layout;
            }

        };
    };



    private void assigncolorblocks() {

        textViewColorBlock_grey.setVisibility(View.GONE);
        textViewColorBlock_red.setVisibility(View.GONE);
        textViewColorBlock_yellow.setVisibility(View.GONE);
        textViewColorBlock_green.setVisibility(View.GONE);

        int allcategories = 0;
        for (int i = 0; i < checkedlist.size(); ++i) {
            if (checkedlist.get(i) >= 1) {

                String item = colorsArray[i];
                String[] itemArr = item.split("::");
                String text = itemArr[0];
                if(debug){Log.d(TAG, "text--" + text);}
                switch (text) {
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
                ++allcategories;
            }
        }

        if(allcategories == 0) {
            textViewColorBlock_grey.setVisibility(View.INVISIBLE);
            textViewColorBlock_red.setVisibility(View.INVISIBLE);
            textViewColorBlock_yellow.setVisibility(View.INVISIBLE);
            textViewColorBlock_green.setVisibility(View.INVISIBLE);
        }

    }


//
//    private class dropdownonitemclicklistener_timer implements AdapterView.OnItemClickListener {
//
//        @Override
//        public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
//
//            Toast.makeText(getActivity(), "TIMER ITEM SELECTED", Toast.LENGTH_SHORT).show();
//        }
//
//    }
//    private class dropdownonitemclicklistener_timer implements AdapterView.OnItemClickListener {
//
//        @Override
//        public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
//
//            Toast.makeText(getActivity(), "TIMER ITEM SELECTED", Toast.LENGTH_SHORT).show();
//        }
//
//    }

//
//    private class dropdownonitemclicklistener_types implements AdapterView.OnItemClickListener {
//
//        @Override
//        public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
//
//            Animation fadeInAnimation = AnimationUtils.loadAnimation(v.getContext(), android.R.anim.fade_in);
//            fadeInAnimation.setDuration(10);
//            v.startAnimation(fadeInAnimation);
//
//            Toast.makeText(getActivity(), "TYPE ITEM SELECTED", Toast.LENGTH_SHORT).show();
//
//        }
//
//    }
//
//    public class dropDownItemClickListener implements AdapterView.OnItemClickListener {
//
//    private int mWhichButtonToUpdate;
//
//    public dropDownItemClickListener(int whichButtonToUpdate) {
//        this.mWhichButtonToUpdate = whichButtonToUpdate;
//    }
//        @Override
//        public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
//
//            Animation fadeInAnimation = AnimationUtils.loadAnimation(v.getContext(), android.R.anim.fade_in);
//            fadeInAnimation.setDuration(10);
//            v.startAnimation(fadeInAnimation);
//
//            switch(mWhichButtonToUpdate) {
//                case 1:
//                    btn1Value =
//                    break;
//                case 2:
//                    break;
//                case 3:
//                    break;
//
//            }
//            String item =
//
//            Toast.makeText(getActivity(), "SIZE ITEM SELECTED", Toast.LENGTH_SHORT).show();
//            asdf
//
//        }
//
//    }






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



}