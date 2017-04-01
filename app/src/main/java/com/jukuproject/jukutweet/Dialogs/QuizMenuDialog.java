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
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;
import java.util.List;

import static com.jukuproject.jukutweet.Adapters.CopyMyListItemsAdapter.setAppCompatCheckBoxColors;

//import com.jukuproject.jukutweet.SharedPrefManager;
//import com.jukuproject.juku.Quizzes.FillInTheBlanks;
//import com.jukuproject.juku.Quizzes.FlashCards;
//import com.jukuproject.juku.Quizzes.MultipleChoice;
//import com.jukuproject.juku.Quizzes.WordBuilder;
//import com.jukuproject.juku.Quizzes.WordMatch;
//import com.jukuproject.juku.R;
//import com.jukuproject.juku.db.InternalDB;

//import static com.jukuproject.juku.AppGlobal.setAppCompatCheckBoxColors;

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
    private int mTotalWidth;

    private String colorsArray[];  //Array of colors
    private String typesArray[];  //Array of quiz types
    private String sizeArray[];  //Array of quiz sizes
    private String timerArray[];

    private PopupWindow popupWindowColors;
//    private PopupWindow popupWindowType;
//    private PopupWindow popupWindowSize;
//    private PopupWindow popupWindowTimer;

    private TextView buttonShowDropDown_Type;
    private TextView buttonShowDropDown_Size;
    private TextView buttonShowDropDown_Timer;

    private TextView textViewColorBlock_grey;
    private TextView textViewColorBlock_red;
    private TextView textViewColorBlock_yellow;
    private TextView textViewColorBlock_green;

    //Final Items passed on to quiz activity
    private List<Integer> extra_blocknumbers = new ArrayList<Integer>();  //This one holds the sql data we pull about each block
    private  ArrayList<Integer> checkedlist = new ArrayList<Integer>();

    //Should hook to a dimension resource, its the total size of the color block segment
//    private int msmallleftpadding;
//    private int msmallmarginpadding;
//    private int smallcolorwindowwidth;

    private float scale;
    private static boolean debug = false;

    public static QuizMenuDialog newInstance(String quizType
            , MyListEntry myListEntry
            , ColorBlockMeasurables colorBlockMeasurables) {

        QuizMenuDialog frag = new QuizMenuDialog();
        Bundle args = new Bundle();
        args.putString("quizType", quizType);
        args.putParcelable("myListEntry",myListEntry);
        args.putParcelable("colorBlockMeasurables",colorBlockMeasurables);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        quizType = getArguments().getString("quizType");
        mMyListEntry = getArguments().getParcelable("myListEntry");
        mColorBlockMeasurables = getArguments().getParcelable("colorBlockMeasurables");
        /** Get width of screen */
//        DisplayMetrics metrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        int screenwidth = metrics.widthPixels;
//        int screenheight = metrics.heightPixels;
//        int dimenscore_total = (int)((float)screenwidth*(float)(.75));
//        scale = metrics.density;

//        int yadjustinitial = (int) ((float)screenheight* (float).00520833333);
//        if(yadjustinitial<=0){
//            yadjustinitial= 5;
//        }
        final int yadjustment = 5;
//
//        int popupwindowwidth = Math.round((float) screenwidth * (float) .75) - (int) (30.0f * scale + 0.5f);
//
//        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            smallcolorwindowwidth = Math.round((float) popupwindowwidth*(float).8);
//            dimenscore_total = (int) ((float)smallcolorwindowwidth*.95);
//        } else {
//            smallcolorwindowwidth = Math.round((float) popupwindowwidth * (float) 0.5);
//            dimenscore_total = (int) ((float)smallcolorwindowwidth*.95);
//        }
//        msmallleftpadding = Math.round((float) screenwidth * (float) 0.011111111);
//        msmallmarginpadding = Math.round((float) screenwidth * (float) 0.002851852);
//        if(msmallmarginpadding<=0) {
//            msmallmarginpadding = 1;
//        }
//
//        int rowbottompadding= (int) (-2.0f * metrics.density - 0.5f);
//
        //Query the DB
//        if(quizType.equals("Tweets")) {
//            Cursor c = InternalDB.getInstance(getContext()).getTweetListColorBlocksCursor(mColorThresholds,null);
//
//            if(c.getCount()>0) {
//                c.moveToFirst();
//                List<Integer> extra = new ArrayList<Integer>();
//                extra.add(Integer.parseInt(c.getString(2))); //Count
//                extra.add(Integer.parseInt(c.getString(3))); //Grey
//                extra.add(Integer.parseInt(c.getString(4))); //Red
//                extra.add(Integer.parseInt(c.getString(5))); //Yellow
//                extra.add(Integer.parseInt(c.getString(6))); //Green
//                //PROBABLY THROW AN ERROR IF THEY DON'T MATCH...........
//                extra_blocknumbers = extra;
//                c.close();
//            } else {  //If there are no color items, it means they're all grey, so just pull the totals for the block and call it grey
//                Cursor d = db.rawQuery("Select Count([_id]) as [Total] FROM [JFavorites] WHERE ([Sys] = "  + sys +  " and [Name] = '" + listname + "')",null);
//                if(d.getCount()>0) {
//                    d.moveToFirst();
//
//                    List<Integer> extra = new ArrayList<Integer>();
//                    extra.add(Integer.parseInt(d.getString(0))); //Count
//                    extra.add(Integer.parseInt(d.getString(0))); //Grey
//                    extra.add(0); //Red
//                    extra.add(0); //Yellow
//                    extra.add(0); //Green
//                    //PROBABLY THROW AN ERROR IF THEY DON'T MATCH...........
//                    extra_blocknumbers = extra;
//
//                } else {
//                    Toast.makeText(activity, "No List Entries Found :(", Toast.LENGTH_LONG).show();
//                }
//                d.close();
//            }
//        } else {
//            Cursor c = db.rawQuery("SELECT SUM([Grey]) + SUM([Red]) + SUM([Yellow]) + SUM([Green]) as [Total],SUM([Grey]) as [Grey],SUM([Red]) as [Red],SUM([Yellow]) as [Yellow],SUM([Green]) as [Green] FROM (SELECT [_id] ,(CASE WHEN [Total] < " + sharedPrefManager.getGreyThreshold() +  " THEN 1 ELSE 0 END) as [Grey] ,(CASE WHEN [Total] >= " + sharedPrefManager.getGreyThreshold() +  " and [Percent] < "  + sharedPrefManager.getRedThreshold() + "  THEN 1  ELSE 0 END) as [Red] ,(CASE WHEN [Total] >= " + sharedPrefManager.getGreyThreshold() +  " and ([Percent] >= "  + sharedPrefManager.getRedThreshold() + "  and [Percent] <  " + sharedPrefManager.getYellowThreshold() + ") THEN 1  ELSE 0 END) as [Yellow] ,(CASE WHEN [Total] >= " + sharedPrefManager.getGreyThreshold() +  " and [Percent]>= " + sharedPrefManager.getYellowThreshold() + " THEN 1 ELSE 0 END) as [Green] FROM (SELECT a.[_id],ifnull(b.[Total],0) as [Total] ,ifnull(b.[Correct],0)  as [Correct],CAST(ifnull(b.[Correct],0)  as float)/b.[Total] as [Percent] FROM ( (SELECT [_id] FROM XREF WHERE [Block]= "  +  block +  " and [Level] =  "   + level  +   ") as a  LEFT JOIN (SELECT [_id],sum([Correct]) as [Correct],sum([Total]) as [Total] from [JScoreboard]  where [_id] in (SELECT [_id] FROM XREF WHERE [Block]= "  +  block +  " and [Level] =  "   + level  +   ") GROUP BY [_id]) as b ON a.[_id] = b.[_id])) as x) as y ",null);
//            if(c.getCount()>0) {
//                c.moveToFirst();
//                List<Integer> extra = new ArrayList<Integer>();
//                extra.add(Integer.parseInt(c.getString(0))); //Count
//                extra.add(Integer.parseInt(c.getString(1))); //Grey
//                extra.add(Integer.parseInt(c.getString(2))); //Red
//                extra.add(Integer.parseInt(c.getString(3))); //Yellow
//                extra.add(Integer.parseInt(c.getString(4))); //Green
//                //PROBABLY THROW AN ERROR IF THEY DON'T MATCH...........
//                extra_blocknumbers = extra;
//                c.close();
//            } else {  //If there are no color items, it means they're all grey, so just pull the totals for the block and call it grey
//                Cursor d = db.rawQuery("Select Count([_id]) as [Total] from XRef Where [Block]= "  +  block +  " and [Level] =  "   + level ,null);
//                d.moveToFirst();
//
//                List<Integer> extra = new ArrayList<Integer>();
//                extra.add(Integer.parseInt(d.getString(0))); //Count
//                extra.add(Integer.parseInt(d.getString(0))); //Grey
//                extra.add(0); //Red
//                extra.add(0); //Yellow
//                extra.add(0); //Green
//                //PROBABLY THROW AN ERROR IF THEY DON'T MATCH...........
//                extra_blocknumbers = extra;
//                d.close();
//            }
//        }
//
//        if(db.isOpen()){
//            db.close();
//            helper.close();
//        }
        // do many usefull things
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialogmenupopup, null);

        LinearLayout layout = (LinearLayout) view.findViewById(R.id.layoutid);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowColors.showAsDropDown(v, -yadjustment, 0);
            }
        });

        //Set up spinner TextViews
        buttonShowDropDown_Type = (TextView) view.findViewById(R.id.listitem_type_spinnerstyle);
        buttonShowDropDown_Size = (TextView) view.findViewById(R.id.listitem_size_spinnerstyle);
        buttonShowDropDown_Timer = (TextView) view.findViewById(R.id.listitem_timer_spinnerstyle);

        TextView textdescType = (TextView) view.findViewById(R.id.optionspopupTextType);
        TextView textdescSize = (TextView) view.findViewById(R.id.optionspopupTextSize);
        TextView textdescTimer = (TextView)view.findViewById(R.id.optionspopupTextTimer);

//        buttonShowDropDown_Type.getLayoutParams().width = smallcolorwindowwidth;
//        buttonShowDropDown_Size.getLayoutParams().width = smallcolorwindowwidth;
//        buttonShowDropDown_Timer.getLayoutParams().width = smallcolorwindowwidth;
//
//        textdescType.setPadding(textdescType.getPaddingLeft(),
//                textdescType.getPaddingTop(),
//                textdescType.getPaddingRight(),
//                textdescType.getPaddingBottom() + rowbottompadding);
//
//        textdescSize.setPadding(textdescSize.getPaddingLeft(),
//                textdescSize.getPaddingTop(),
//                textdescSize.getPaddingRight(),
//                textdescSize.getPaddingBottom() + rowbottompadding);
//
//        textdescTimer.setPadding(textdescTimer.getPaddingLeft(),
//                textdescTimer.getPaddingTop(),
//                textdescTimer.getPaddingRight(),
//                textdescTimer.getPaddingBottom() + rowbottompadding);

        if(quizType.equalsIgnoreCase("flashcards")) {
            buttonShowDropDown_Type.setText(getActivity().getString(R.string.menuoptionskanji));
            buttonShowDropDown_Type.setTag(1);

            buttonShowDropDown_Size.setText(getActivity().getString(R.string.menuoptionsdefinition));
            buttonShowDropDown_Size.setTag(4);

            String front = "Front: ";
            String back = "Back: ";
            textdescType.setText(front);
            textdescSize.setText(back);

            textdescTimer.setVisibility(TextView.GONE);
            buttonShowDropDown_Timer.setVisibility(View.GONE);

        } else if (quizType.equalsIgnoreCase("fillinsentences")){
            buttonShowDropDown_Type.setVisibility(TextView.GONE);
            textdescType.setVisibility(TextView.GONE);

            buttonShowDropDown_Size.setText(getActivity().getString(R.string.menuoptionsten));
            buttonShowDropDown_Size.setTag(10);

            textdescTimer.setVisibility(TextView.GONE);
            buttonShowDropDown_Timer.setVisibility(View.GONE);

        } else if(quizType.equalsIgnoreCase("wordbuilder")) {
            buttonShowDropDown_Type.setText(getActivity().getString(R.string.menuoptionswordbuildertype));
            buttonShowDropDown_Type.setTag(0);

//            buttonShowDropDown_Size.setText(activity.getString(R.string.menuoptionsdefinition));
//            buttonShowDropDown_Size.setTag(4);
            textdescSize.setVisibility(View.GONE);
            buttonShowDropDown_Size.setVisibility(View.GONE);

            textdescTimer.setVisibility(TextView.VISIBLE);
            buttonShowDropDown_Timer.setVisibility(View.VISIBLE);

            buttonShowDropDown_Timer.setText(getActivity().getString(R.string.menuoptionssixty));
            buttonShowDropDown_Timer.setTag(60);
        } else if(quizType.equalsIgnoreCase("wordmatch")) {
            buttonShowDropDown_Type.setText(getActivity().getString(R.string.menuoptionskanjitokana));
            buttonShowDropDown_Type.setTag(1);

            buttonShowDropDown_Size.setText(getActivity().getString(R.string.menuoptionsten));
            buttonShowDropDown_Size.setTag(10);

            textdescTimer.setVisibility(TextView.VISIBLE);
            buttonShowDropDown_Timer.setVisibility(View.VISIBLE);

            buttonShowDropDown_Timer.setText(getActivity().getString(R.string.menuoptionsnone));
            buttonShowDropDown_Timer.setTag(0);

        } else {
            buttonShowDropDown_Type.setText(getActivity().getString(R.string.menuoptionskanjitodef));
            buttonShowDropDown_Type.setTag(0);

            buttonShowDropDown_Size.setText(getActivity().getString(R.string.menuoptionsten));
            buttonShowDropDown_Size.setTag(10);

            textdescTimer.setVisibility(TextView.VISIBLE);
            buttonShowDropDown_Timer.setVisibility(View.VISIBLE);

            buttonShowDropDown_Timer.setText(getActivity().getString(R.string.menuoptionsnone));
            buttonShowDropDown_Timer.setTag(0);

        }


        buttonShowDropDown_Type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupDropDownWindow("Type").showAsDropDown(v, -yadjustment, 0);
            }
        });

        buttonShowDropDown_Size.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                popupWindowSize.showAsDropDown(v, -yadjustment, 0);
                popupDropDownWindow("Size").showAsDropDown(v, -yadjustment, 0);
            }
        });


        buttonShowDropDown_Timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                popupWindowTimer.showAsDropDown(v, -yadjustment, 0);
                popupDropDownWindow("Timer").showAsDropDown(v, -yadjustment, 0);

            }
        });


        //Set up the colorblocks
        textViewColorBlock_grey = (TextView) view.findViewById(R.id.listitem_colors_1);
        textViewColorBlock_red = (TextView) view.findViewById(R.id.listitem_colors_2);
        textViewColorBlock_yellow = (TextView) view.findViewById(R.id.listitem_colors_3);
        textViewColorBlock_green = (TextView) view.findViewById(R.id.listitem_colors_4);


//        int count = extra_blocknumbers.get(0);
//        int grey = extra_blocknumbers.get(1);
//        int red = extra_blocknumbers.get(2);
//        int yellow = extra_blocknumbers.get(3);
//        int green = extra_blocknumbers.get(4);
//
//        Drawable drawablecolorblock1 = ContextCompat.getDrawable(getActivity(), R.drawable.colorblock);
//        Drawable drawablecolorblock2 = ContextCompat.getDrawable(getActivity(), R.drawable.colorblock);
//        Drawable drawablecolorblock3 = ContextCompat.getDrawable(getActivity(), R.drawable.colorblock);
//        Drawable drawablecolorblock4 = ContextCompat.getDrawable(getActivity(), R.drawable.colorblock);
//asdf
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            drawablecolorblock1.setColorFilter(ContextCompat.getColor(activity, R.color.Grey_500_m), PorterDuff.Mode.MULTIPLY);
//            textViewColorBlock_grey.setBackground(drawablecolorblock1);
//            drawablecolorblock2.setColorFilter(ContextCompat.getColor(activity, R.color.answerIncorrectColor), PorterDuff.Mode.MULTIPLY);
//            textViewColorBlock_red.setBackground(drawablecolorblock2);
//            drawablecolorblock3.setColorFilter(ContextCompat.getColor(activity, R.color.spinnerYellowColor), PorterDuff.Mode.MULTIPLY);
//            textViewColorBlock_yellow.setBackground(drawablecolorblock3);
//            drawablecolorblock4.setColorFilter(ContextCompat.getColor(activity, R.color.answerCorrectColor), PorterDuff.Mode.MULTIPLY);
//            textViewColorBlock_green.setBackground(drawablecolorblock4);
//
//        } else {
//            drawablecolorblock1.setColorFilter(ContextCompat.getColor(activity, R.color.Grey_500_m), PorterDuff.Mode.MULTIPLY);
//            textViewColorBlock_grey.setBackgroundDrawable(drawablecolorblock1);
//            drawablecolorblock2.setColorFilter(ContextCompat.getColor(activity, R.color.answerIncorrectColor), PorterDuff.Mode.MULTIPLY);
//            textViewColorBlock_red.setBackgroundDrawable(drawablecolorblock2);
//            drawablecolorblock3.setColorFilter(ContextCompat.getColor(activity, R.color.spinnerYellowColor), PorterDuff.Mode.MULTIPLY);
//            textViewColorBlock_yellow.setBackgroundDrawable(drawablecolorblock3);
//            drawablecolorblock4.setColorFilter(ContextCompat.getColor(activity, R.color.answerCorrectColor), PorterDuff.Mode.MULTIPLY);
//            textViewColorBlock_green.setBackgroundDrawable(drawablecolorblock4);
//        }
//
//        textViewColorBlock_grey.setVisibility(View.GONE);
//        textViewColorBlock_red.setVisibility(View.GONE);
//        textViewColorBlock_yellow.setVisibility(View.GONE);
//        textViewColorBlock_green.setVisibility(View.GONE);

        //Set color dropdown variables
//        List<String> colorsList = new ArrayList<String>();
//        colorsList.add("Grey::1");
//        colorsList.add("Red::2");
//        colorsList.add("Yellow::3");
//        colorsList.add("Green::4");
//
//        //Fill our "checkedlist", which keeps track of which multiselect colors have been clicked
//        checkedlist.clear();
//        checkedlist.add(1);
//        checkedlist.add(2);
//        checkedlist.add(3);
//        checkedlist.add(4);


//        int extratrackerbreakpoint = (int)((float)dimenscore_total*(float)0.168);
//        int extratrackerRemainderSmall =(int)((float)dimenscore_total*(float)0.004);
//        int extratracker = extratrackerRemainderSmall;
//
        if(mColorBlockMeasurables.getTotalCount()>0) {
            setColorBlocks(mColorBlockMeasurables
                    ,mTotalWidth
                    ,textViewColorBlock_grey
                    ,textViewColorBlock_red
                    ,textViewColorBlock_yellow
                    ,textViewColorBlock_green);

//
//            textViewColorBlock_red.setText(String.valueOf(red));
//            textViewColorBlock_yellow.setText(String.valueOf(yellow));
//            textViewColorBlock_green.setText(String.valueOf(green));
//
//            if(red + yellow + green == 0) {
//                textViewColorBlock_grey.setText(String.valueOf(count));
//                //Remove the red/yellow/green options from the dropdownlist
//                colorsList.remove(3);
//                colorsList.remove(2);
//                colorsList.remove(1);
//                //And remove the corresponding checked/unchecked int from the list that keeps track of it
//                checkedlist.remove(3);
//                checkedlist.remove(2);
//                checkedlist.remove(1);
//
//                textViewColorBlock_grey.setVisibility(View.VISIBLE);
//                textViewColorBlock_grey.setMinimumWidth(dimenscore_total);
//
//            } else {
//                textViewColorBlock_grey.setText(String.valueOf(grey));
//
//                if (green > 0) {
//                    textViewColorBlock_green.setVisibility(View.VISIBLE);
//                    int dimenscore = Math.round((dimenscore_total * ((float) green / (float) count)));
//                    if(debug){Log.d(TAG, "green dimenscore: " + dimenscore);}
//                    if(dimenscore<extratrackerbreakpoint) {
//                        dimenscore_total =  dimenscore_total - (extratrackerbreakpoint - dimenscore ) -extratracker;
//                        dimenscore = extratrackerbreakpoint;
//                        extratracker = extratracker + extratrackerRemainderSmall;
//                    }
//                    textViewColorBlock_green.setMinimumWidth(dimenscore);
//                } else {
//                    colorsList.remove(3);
//                    checkedlist.remove(3);
//                }
//
//                if (yellow > 0) {
//                    textViewColorBlock_yellow.setVisibility(View.VISIBLE);
//                    int dimenscore = Math.round((dimenscore_total * ((float) yellow / (float) count)));
//                    if(debug){
//                        Log.d(TAG, "yellow dimenscore: " + dimenscore);
//                        Log.d(TAG, "yellow dimenscore: " + dimenscore);
//                    }
//                    if(dimenscore<extratrackerbreakpoint) {
//                        dimenscore_total =  dimenscore_total - (extratrackerbreakpoint - dimenscore ) -extratracker;
//                        extratracker = extratracker + extratrackerRemainderSmall;
//                        dimenscore = extratrackerbreakpoint;
//                    }
//
//                    textViewColorBlock_yellow.setMinimumWidth(dimenscore);
//                } else {
//                    colorsList.remove(2);
//                    checkedlist.remove(2);
//                }
//                if (red > 0) {
//                    textViewColorBlock_red.setVisibility(View.VISIBLE);
//                    int dimenscore = Math.round((dimenscore_total * ((float) red / (float) count)));
//                    if(debug){
//                        Log.d(TAG, "red dimenscore: " + dimenscore);
//                        Log.d(TAG, "yellow dimenscore: " + dimenscore);
//                    }
//                    if(dimenscore<extratrackerbreakpoint) {
//                        dimenscore_total =  dimenscore_total - (extratrackerbreakpoint - dimenscore ) -extratracker;
//                        extratracker = extratracker + extratrackerRemainderSmall;
//                        dimenscore = extratrackerbreakpoint;
//                    }
//
//                    textViewColorBlock_red.setMinimumWidth(dimenscore);
//                } else {
//                    colorsList.remove(1);
//                    checkedlist.remove(1);
//                }
//                if (grey > 0) {
//                    textViewColorBlock_grey.setVisibility(View.VISIBLE);
//                    int dimenscore = Math.round((dimenscore_total * ((float) grey / (float) count)));
//                    if(debug) {
//                        Log.d(TAG, "grey dimenscore: " + dimenscore);
//                        Log.d(TAG, "yellow dimenscore: " + dimenscore);
//                    }
//                    if(dimenscore<extratrackerbreakpoint) {
//                        dimenscore_total =  dimenscore_total - (extratrackerbreakpoint - dimenscore ) -extratracker;
//                        extratracker = extratracker + extratrackerRemainderSmall;
//                        dimenscore = extratrackerbreakpoint;
//                    }
//
//                    textViewColorBlock_grey.setMinimumWidth(dimenscore);
//                } else {
//                    //Remove the red/yellow/green options from the dropdownlist
//                    colorsList.remove(0);
//                    checkedlist.remove(0);
//                }
//
//
//            }
//

        } else {

            textViewColorBlock_grey.setText("");
            textViewColorBlock_red.setText("");
            textViewColorBlock_yellow.setText("");
            textViewColorBlock_green.setText("");
        }

        List<String> typeslist = new ArrayList<String>();
        List<String> sizelist = new ArrayList<String>();
        List<String> timerlist = new ArrayList<String>();

        if(quizType.equalsIgnoreCase("flashcards")) {
            typeslist.add("Kanji::1");
            typeslist.add("Kana::5");
            typeslist.add("Definition::4");
            sizelist.add("Kanji::1");
            sizelist.add("Kana::5");
            sizelist.add("Definition::4");

        } else if(quizType.equalsIgnoreCase("fillinsentences")) {

            sizelist.add("5::5");
            sizelist.add("10::10");
            sizelist.add("15::15");
            sizelist.add("20::20");
            sizelist.add("25::25");
            sizelist.add("30::30");
            sizelist.add("40::40");
            sizelist.add("50::50");

        } else if(quizType.equalsIgnoreCase("wordbuilder")) {

            typeslist.add("Repeating::0");
            typeslist.add("Straight::1");

            timerlist.add("None::0");
            timerlist.add("30 Seconds::30");
            timerlist.add("60 Seconds::60");
            timerlist.add("90 Seconds::90");
            timerlist.add("120 Seconds::120");
            timerlist.add("180 Seconds::180");
            timerlist.add("240 Seconds::240");
            timerlist.add("300 Seconds::300");


        } else {
            if(!quizType.equalsIgnoreCase("wordmatch")) {
                typeslist.add("Kanji to Definition::0");
            }
            typeslist.add("Kanji to Kana::1");
            typeslist.add("Kana to Kanji::2");
            typeslist.add("Definition to Kanji::3");

            sizelist.add("5::5");
            sizelist.add("10::10");
            sizelist.add("15::15");
            sizelist.add("20::20");
            sizelist.add("25::25");
            sizelist.add("30::30");
            sizelist.add("40::40");
            sizelist.add("50::50");


            timerlist.add("None::0");
            timerlist.add("5 Seconds::5");
            timerlist.add("6 Seconds::6");
            timerlist.add("7 Seconds::7");
            timerlist.add("8 Seconds::8");
            timerlist.add("9 Seconds::9");
            timerlist.add("10 Seconds::10");
            timerlist.add("15 Seconds::15");
            timerlist.add("20 Seconds::20");
        }

        List<String> colorsList = new ArrayList<String>();
        colorsList.add("Grey::1");
        colorsList.add("Red::2");
        colorsList.add("Yellow::3");
        colorsList.add("Green::4");

        // convert to simple array
        colorsArray = new String[colorsList.size()];
        colorsList.toArray(colorsArray);

        typesArray = new String[typeslist.size()];
        typeslist.toArray(typesArray);

        sizeArray = new String[sizelist.size()];
        sizelist.toArray(sizeArray);

        timerArray = new String[timerlist.size()];
        timerlist.toArray(timerArray);

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

    private PopupWindow popupDropDownWindow(String popupType) {

        PopupWindow popupWindow = new PopupWindow(getActivity());
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popup_drawable));

        ListView listView = new ListView(getActivity());

        switch(popupType) {
            case "Size":
                listView.setAdapter(listadapter_dropdown(sizeArray));
                listView.setOnItemClickListener(new dropdownonitemclicklistener_sizes());
                break;

            case "Type":
                listView.setAdapter(listadapter_dropdown(typesArray));
                listView.setOnItemClickListener(new dropdownonitemclicklistener_types());
                break;

            case "Timer":
                listView.setAdapter(listadapter_dropdown(timerArray));
                listView.setOnItemClickListener(new dropdownonitemclicklistener_timer());

                break;

        }

        popupWindow.setContentView(listView);
        popupWindow.setFocusable(true);
        popupWindow.setWidth((int)(mTotalWidth * .5f));
        popupWindow.setClippingEnabled(false);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);


        return popupWindow;
    }

//    private PopupWindow popupWindowSize() {
//
//        PopupWindow popupWindow = new PopupWindow(getActivity());
//
//        ListView listViewSizes = new ListView(getActivity());
//        listViewSizes.setAdapter(listadapter_dropdown(sizeArray));
//        listViewSizes.setOnItemClickListener(new dropdownonitemclicklistener_sizes());
//
//        popupWindow.setFocusable(true);
//        popupWindow.setWidth(smallcolorwindowwidth);
//        popupWindow.setClippingEnabled(false);
//        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
//        popupWindow.setContentView(listViewSizes);
//        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popupdrawable));
//        return popupWindow;
//    }

//
//    private PopupWindow popupWindowType() {
//
//        PopupWindow popupWindow = new PopupWindow(getActivity());
//
//        ListView listViewTypes = new ListView(getActivity());
//        listViewTypes.setAdapter(listadapter_dropdown(typesArray));
//        listViewTypes.setOnItemClickListener(new dropdownonitemclicklistener_types());
//
//        popupWindow.setFocusable(true);
//        popupWindow.setWidth(smallcolorwindowwidth);
//        popupWindow.setClippingEnabled(false);
//        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
//        popupWindow.setContentView(listViewTypes);
//        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popupdrawable));
//
//        return popupWindow;
//    }

//    private PopupWindow popupWindowTimer() {
//
//        PopupWindow popupWindow = new PopupWindow(getActivity());
//
//        ListView listViewTimer = new ListView(getActivity());
//        listViewTimer.setAdapter(listadapter_dropdown(timerArray));
//        listViewTimer.setOnItemClickListener(new dropdownonitemclicklistener_timer());
//
//        popupWindow.setFocusable(true);
//        popupWindow.setWidth(smallcolorwindowwidth);
//        popupWindow.setClippingEnabled(false);
//        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
//        popupWindow.setContentView(listViewTimer);
//        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popupdrawable));
//
//        return popupWindow;
//    }

    private PopupWindow popupWindowColors() {

        PopupWindow popupWindow = new PopupWindow(getActivity());

        ListView listViewColors = new ListView(getActivity());
        listViewColors.setAdapter(listadapter_colors(colorsArray, extra_blocknumbers));

        popupWindow.setFocusable(true);
        popupWindow.setWidth((int)(mTotalWidth * .5f));
        popupWindow.setClippingEnabled(false);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(listViewColors);
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popup_drawable));
        return popupWindow;
    }




    //TODO SPLIT OUT??
    private ArrayAdapter<String> listadapter_dropdown(String Array[]) {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.dialogmenu_spinneritem, Array) {

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {

                // setting the ID and text for every items in the list
                String item = getItem(position);
                String[] itemArr = item.split("::");
                String text = itemArr[0];
                String id = itemArr[1];

                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT));
                layout.getLayoutParams().height = (int) (35.0f * scale + 0.5f);
                TextView textView = new TextView(getActivity());
//                textView.setId(R.id.listdropdown_insidelayout_textview);
                textView.setText(text);
                textView.setTag(id);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//                textView.setPadding(msmallleftpadding, msmallmarginpadding, msmallmarginpadding, msmallmarginpadding);
                textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));

                layout.addView(textView);

                return layout;
            }
        };

        return adapter;
    }



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

                if (debug) {
                    Log.d(TAG, "checkbox scale : " + scale);
                }
                ;
                checkbox.setPadding(checkbox.getPaddingLeft() + (int) (5.0f * scale + 0.5f),
                        checkbox.getPaddingLeft() + (int) (10.0f * scale + 0.5f),
                        checkbox.getPaddingRight(),
                        checkbox.getPaddingLeft() + (int) (10.0f * scale + 0.5f));

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


    private String getSelectedItemsAsString(ArrayList<Integer> list ) {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < list.size(); ++i) {
            if(debug){Log.d(TAG,"list.get(i): " + list.get(i));}
            if(list.get(i) > 0) {

                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;
                sb.append(list.get(i).toString());

            }
        }
        return sb.toString();
    }


    private class dropdownonitemclicklistener_timer implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {

//            Animation fadeInAnimation = AnimationUtils.loadAnimation(v.getContext(), android.R.anim.fade_in);
//            fadeInAnimation.setDuration(5);
//            v.startAnimation(fadeInAnimation);
//
//            String selectedItemText = ((TextView) v.findViewById(R.id.listdropdown_insidelayout_textview)).getText().toString();
//            buttonShowDropDown_Timer.setText(selectedItemText);
//
//            String selectedItemTag  = v.findViewById(R.id.listdropdown_insidelayout_textview).getTag().toString();
//            buttonShowDropDown_Timer.setTag(selectedItemTag);
//            popupWindowTimer.dismiss();
            Toast.makeText(getActivity(), "TIMER ITEM SELECTED", Toast.LENGTH_SHORT).show();
        }

    }


    private class dropdownonitemclicklistener_types implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {

            Animation fadeInAnimation = AnimationUtils.loadAnimation(v.getContext(), android.R.anim.fade_in);
            fadeInAnimation.setDuration(10);
            v.startAnimation(fadeInAnimation);

            Toast.makeText(getActivity(), "TYPE ITEM SELECTED", Toast.LENGTH_SHORT).show();

//            String selectedItemText = ((TextView) v.findViewById(R.id.listdropdown_insidelayout_textview)).getText().toString();
//            buttonShowDropDown_Type.setText(selectedItemText);
//
//            String selectedItemTag  = v.findViewById(R.id.listdropdown_insidelayout_textview).getTag().toString();
//            buttonShowDropDown_Type.setTag(selectedItemTag);
//            popupWindowType.dismiss();
        }

    }

    private class dropdownonitemclicklistener_sizes implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {

            Animation fadeInAnimation = AnimationUtils.loadAnimation(v.getContext(), android.R.anim.fade_in);
            fadeInAnimation.setDuration(10);
            v.startAnimation(fadeInAnimation);


            Toast.makeText(getActivity(), "SIZE ITEM SELECTED", Toast.LENGTH_SHORT).show();

//            String selectedItemText = ((TextView) v.findViewById(R.id.listdropdown_insidelayout_textview)).getText().toString();
//            buttonShowDropDown_Size.setText(selectedItemText);
//
//            String selectedItemTag  = v.findViewById(R.id.listdropdown_insidelayout_textview).getTag().toString();
//            buttonShowDropDown_Size.setTag(selectedItemTag);
//            popupWindowSize.dismiss();

        }

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

            //Remove the red/yellow/green options from the dropdownlist
//            colorsList.remove(3);
//            colorsList.remove(2);
//            colorsList.remove(1);
//            //And remove the corresponding checked/unchecked int from the list that keeps track of it
//            checkedlist.remove(3);
//            checkedlist.remove(2);
//            checkedlist.remove(1);


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