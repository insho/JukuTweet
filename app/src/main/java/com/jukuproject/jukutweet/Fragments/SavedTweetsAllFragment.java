package com.jukuproject.jukutweet.Fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.SavedTweetsExpandableAdapter;
import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MenuHeader;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;
import java.util.Arrays;

//import com.jukuproject.jukutweet.BaseContainerFragment;

/**
 * Shows user-created lists of vocabulary
 */

public class SavedTweetsAllFragment  extends Fragment {

    String TAG = "SavedTweetsAll";
    FragmentInteractionListener mCallback;
    SavedTweetsExpandableAdapter SavedTweetsFragmentAdapter;
    ExpandableListView expListView;
    ArrayList<MenuHeader> mMenuHeader;
    private int lastExpandedPosition = -1;
    private SharedPrefManager sharedPrefManager;



    public static SavedTweetsAllFragment newInstance(Bundle bundle) {
        SavedTweetsAllFragment fragment = new SavedTweetsAllFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

//        errotest();
        //Call shared prefs to find out which star colors (i.e. favorites lists) to include
        sharedPrefManager = SharedPrefManager.getInstance(getContext());

        View v = inflater.inflate(R.layout.fragment_mylists, container, false);


        expListView = (ExpandableListView) v.findViewById(R.id.lvMyListCategory);
        expListView.setClickable(true);
        prepareListData();

        SavedTweetsFragmentAdapter = new SavedTweetsExpandableAdapter(getContext(),mMenuHeader,getdimenscore(),0);

        expListView.setAdapter(SavedTweetsFragmentAdapter);
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {


                //If the list being clicked on is empty, show (or hide) the "(empty)" header label
                if(mMenuHeader.get(groupPosition).getColorBlockMeasurables().getTotalCount() == 0) {
                    TextView lblListHeaderCount = (TextView) v.findViewById(R.id.lblListHeaderCount);
                    if (lblListHeaderCount.getVisibility() == TextView.VISIBLE && lblListHeaderCount.getText().toString().length() > 0) {
                        lblListHeaderCount.setVisibility(TextView.GONE);
                    } else {
                        lblListHeaderCount.setVisibility(TextView.VISIBLE);
                        lblListHeaderCount.setText(getString(R.string.empty_parenthesis));
                    }
                } else if (!mMenuHeader.get(groupPosition).isExpanded()) {

                    if(lastExpandedPosition != groupPosition) {
                        expListView.collapseGroup(lastExpandedPosition);
                    }

                    expListView.expandGroup(groupPosition);
                    expListView.setSelectedGroup(groupPosition);
                    mMenuHeader.get(groupPosition).setExpanded(true);
                    lastExpandedPosition = groupPosition;
                }  else {
                    expListView.collapseGroup(groupPosition);
                    mMenuHeader.get(groupPosition).setExpanded(false);
                }
                return true;   //"True" makes it impossible to collapse the list
            }
        });

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition
                        && mMenuHeader.get(groupPosition).getColorBlockMeasurables().getTotalCount()> 0
                        ) {
                    expListView.collapseGroup(lastExpandedPosition);
                    lastExpandedPosition = groupPosition;
                }

                int idx = expListView.getFirstVisiblePosition() + expListView.getFirstVisiblePosition() * groupPosition;
                expListView.setSelectionFromTop(idx, idx);
            }
        });

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
//                String Header = listDataHeader.get(groupPosition);
                String childOption = mMenuHeader.get(groupPosition).getChildOptions().get(childPosition); //This is the text in the child that the user clicked
//                if (BuildConfig.DEBUG) {
//                    Log.d(TAG, "Header: " + mMenuHeader.get(groupPosition).getHeaderTitle());
//                    Log.d(TAG, "Child: " + mMenuHeader.get(groupPosition).getChildOptions().get(childPosition));
//                }
//                Toast.makeText(getActivity(), "Header: " + mMenuHeader.get(groupPosition).getHeaderTitle() + ", child: " + mMenuHeader.get(groupPosition).getChildOptions().get(childPosition), Toast.LENGTH_SHORT).show();

                switch (childOption) {
//                    case "Flash Cards":
//                        MenuOptionsDialog a = new MenuOptionsDialog(getActivity(), 0, 0, "flashcards", true, Header, sys, mylistposition);
//                        a.CreateDialog();
//                        break;
                    case "Browse/Edit":
                        SavedTweetsBrowseFragment fragment = SavedTweetsBrowseFragment.newInstance(new MyListEntry(mMenuHeader.get(groupPosition).getHeaderTitle(),mMenuHeader.get(groupPosition).getSystemList()));
                        ((BaseContainerFragment)getParentFragment()).replaceFragment(fragment, true,"savedtweetsbrowse");
                        //Hide the fab
                        mCallback.showFab(false,"");
                        break;

                    default:

                        break;
//                        sendMessage(parent, Header, sys);
//                        break;
//                    case "Multiple Choice":
//                        MenuOptionsDialog x = new MenuOptionsDialog(getActivity(), 0, 0, "multiplechoice", true, Header, sys, mylistposition);
//                        x.CreateDialog();
//                        break;
//
//                    case "Fill in the Blanks":
//                        /** I'm being lazy here, and inputing groupposition in the "levelblock" place*/
//                        MenuOptionsDialog b = new MenuOptionsDialog(getActivity(), 0, groupPosition, "fillinsentences", true, Header, sys, mylistposition);
//                        b.CreateDialog();
//
//                        break;
//
//                    case "Word Builder":
//                        MenuOptionsDialog c = new MenuOptionsDialog(getActivity(), 0, groupPosition, "wordbuilder", true, Header, sys, mylistposition);
//                        c.CreateDialog();
//                        break;
//                    case "Word Match":
//                        MenuOptionsDialog d = new MenuOptionsDialog(getActivity(), 0, groupPosition, "wordmatch", true, Header, sys, mylistposition);
//                        d.CreateDialog();
//                        break;
//
//                    case "Stats":
//
//                        Intent intent = new Intent(getActivity(), BlockStatsNoTab.class);
//                        intent.putExtra("blockNumber", 0);
//                        intent.putExtra("levelNumber", 0);
//                        intent.putExtra("mylists", true);
//                        intent.putExtra("mylistposition", lastExpandedPosition);
//                        intent.putExtra("Header", Header);
//                        intent.putExtra("Sys", sys);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        getActivity().finish();
//                        startActivity(intent);
//                        break;
                }
                return false;
            }
        });

        expListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mCallback.showEditMyListDialog(mMenuHeader.get(position).getHeaderTitle(),mMenuHeader.get(position).isSystemList());
//                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
//                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
//                    int itemType = ExpandableListView.getPackedPositionType(id);
//
//                    if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
//                        String Header = listDataHeader.get(groupPosition);
//                        groupPosition = ExpandableListView.getPackedPositionGroup(id);
//
//                            boolean favoritesclick = false;
//                            if (BuildConfig.DEBUG) {
//                                Log.d(TAG, "addStarList.size: " + addStarList.size());
//                                Log.d(TAG, "groupPosition: " + groupPosition);
//                            }
//                            favoritesclick = groupPosition < (addStarList.size());
//                            deleteDialog2(Header, position, favoritesclick);
//
//
//                        return true; //true if we consumed the click, false if not
//
//                    } else {
//                        // null item; we don't consume the click
//                        return false;
//                    }
//
//                }

                return false;
            }
        });



        /** deciding which mylist to open on activity start */

//        if(mylistposition<0 && mylistposition >=0) {
//            mylistposition = mylistposition;
//        }
//        if(mylistposition >=0) {
//            if(BuildConfig.DEBUG){Log.d(TAG,"heres the mylispos right here: " + mylistposition);}
//
//            lastExpandedPosition = mylistposition;
//            expListView.expandGroup(mylistposition);
//            expListView.setSelectedGroup(mylistposition);
//
//        } else if(listDataHeader != null && listDataHeader.size()>1) {
//
//
//            expListView.expandGroup(0);
//        } else {
//            if(BuildConfig.DEBUG){Log.d(TAG, "NO LISTS TO EXPAND");}
//        }
        expListView.setGroupIndicator(null);

        setRetainInstance(true);
        return v;
    }

//    public void errotest(){
//        InternalDB helper =  InternalDB.getInstance(getActivity());
//        SQLiteDatabase db = helper.getWritableDatabase();
//                Cursor c = db.rawQuery("SELECT  DISTINCT [Name]" +
//                        ",[Sys]" +
//                        ",[UserID] " +
//                        ",[_id] as [Tweet_id]" +
//                        "FROM JFavoritesTweets ",null);
//
//
//        if(c.getCount()>0) {
//            c.moveToFirst();
//            while (!c.isAfterLast()) {
//                        Log.d(TAG,"ERRORTEST: " + c.getString(0) + ", id: " + c.getString(3));
//
//                c.moveToNext();
//                }
//
//
//
//            }
//
//        c.close();
//        db.close();
//        helper.close();
//
//
//
//    }


    public void prepareListData() {
        mMenuHeader = new ArrayList<>();
//        InternalDB helper =  InternalDB.getInstance(getActivity());
//        SQLiteDatabase db = helper.getWritableDatabase();

        ArrayList<String> availableFavoritesStars = sharedPrefManager.getActiveTweetFavoriteStars();
        ColorThresholds colorThresholds = sharedPrefManager.getColorThresholds();
        ArrayList<String> childOptions = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.menu_mylist)));

        Log.d(TAG,"Colorgrey thresh: " + colorThresholds.getTweetGreyThreshold() + ", red: " + colorThresholds.getTweetRedthreshold());

//        Cursor c = db.rawQuery(
//
//                "SELECT xx.[Name]" +
//                ",xx.[Sys]" +
//                ",ifnull(yy.[Total],0) as [Total]" +
//                ",ifnull(yy.[Grey],0) as [Grey]" +
//                ",ifnull(yy.[Red],0) as [Red]" +
//                ",ifnull(yy.[Yellow],0) as [Yellow]" +
//                ",ifnull(yy.[Green],0) as [Green] " +
//                ",ifnull(yy.[Empty],0) as [Empty] " +
//
//                "" +
//                "FROM (" +
//                        "SELECT [Name]" +
//                        ",0 as [Sys] " +
//                        "From JFavoritesTweetLists " +
//                        "UNION " +
//                        "SELECT 'Blue' as [Name]" +
//                        ", 1 as [Sys] " +
//                        "Union " +
//                        "SELECT 'Red' as [Name]" +
//                        ",1 as [Sys] " +
//                        "Union " +
//                        "SELECT 'Green' as [Name]" +
//                        ",1 as [Sys] " +
//                        "Union " +
//                        "SELECT 'Yellow' as [Name]" +
//                        ",1 as [Sys]" +
//                        "Union " +
//                        "SELECT 'Purple' as [Name]" +
//                        ",1 as [Sys]" +
//                        "Union " +
//                        "SELECT 'Orange' as [Name]" +
//                        ",1 as [Sys]" +
//                ") as [xx] " +
//                "LEFT JOIN " +
//                " (" +
//
//                "Select [Name] " +
//                ",[Sys] " +
//                ",COUNT([Category]) as [Total] " +
//                ",SUM((CASE WHEN [Category] = 'Empty' THEN 1 else 0 END)) as [Empty] " +
//                ",SUM((CASE WHEN [Category] = 'Grey' THEN 1 else 0 END)) as [Grey] " +
//                ",SUM((CASE WHEN [Category] = 'Red' THEN 1 else 0 END)) as [Red] " +
//                ",SUM((CASE WHEN [Category] = 'Yellow' THEN 1 else 0 END)) as [Yellow] " +
//                ",SUM((CASE WHEN [Category] = 'Green' THEN 1 else 0 END)) as [Green] " +
//                " FROM (" +
//
////                /* Assign each tweet a color based on the percentages of word color scores for kanjis in the tweet */
//                "Select [Name] " +
//                ",[Sys] " +
//                ",(CASE WHEN [Total] = 0 THEN 'Empty' " +
//                " WHEN CAST(ifnull([Grey],0)  as float)/[Total] > " + colorThresholds.getTweetGreyThreshold() + " THEN 'Grey' " +
//                " WHEN CAST(ifnull([Green],0)  as float)/[Total] >= " + colorThresholds.getTweetGreenthreshold() + " THEN 'Green' " +
//                " WHEN  CAST(ifnull([Red],0)  as float)/[Total] >= " + colorThresholds.getTweetRedthreshold() + " THEN 'Red' " +
//                " WHEN CAST(ifnull([Yellow],0)  as float)/[Total] >= " + colorThresholds.getTweetYellowthreshold() +" THEN 'Yellow' " +
//                " WHEN [Grey] > [Green] and [Grey] > [Red] and [Grey] > [Yellow] THEN 'Grey' " +
//                " WHEN [Green] > [Grey] and [Green] > [Red] and [Green] > [Yellow] THEN 'Green' " +
//                " WHEN [Red] > [Green] and [Red] > [Grey] and [Red] > [Yellow] THEN 'Red' " +
//                " WHEN [Yellow] > [Green] and [Yellow] > [Red] and [Yellow] > [Grey] THEN 'Yellow' " +
//                " ELSE 'Grey' END) as [Category] " +
//                " FROM ( " +
//
//                /* Now to pull together ListName, Tweet and the totals (by color) of the kanji in those tweets */
//                "SELECT  ListsTweetsAndAllKanjis.[Name]" +
//                ",ListsTweetsAndAllKanjis.[Sys]" +
//                ",ListsTweetsAndAllKanjis.[Tweet_id] "+
//
//                ",SUM([Grey]) + SUM([Red]) + SUM([Yellow]) + SUM([Green]) as [Total] " +
//                ",SUM([Grey]) as [Grey]" +
//                ",SUM([Red]) as [Red]" +
//                ",SUM([Yellow]) as [Yellow]" +
//                ",SUM([Green]) as [Green] " +
//                "FROM (" +
//
//                                    /* Now we have a big collection of list metadata (tweetlists), and all the kanji scores and colors for
//                                     each kanji (kanjilists) */
//                                    " Select TweetLists.[Name] " +
//                                    " ,TweetLists.[Sys] " +
//                                    ", TweetLists.[Tweet_id] " +
//
//                                    ",(CASE WHEN [Total] is not NULL AND [Total] < " + colorThresholds.getGreyThreshold() + " THEN 1 ELSE 0 END) as [Grey] " +
//                                    ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] < " + colorThresholds.getRedThreshold() + "  THEN 1  ELSE 0 END) as [Red] " +
//                                    ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and ([Percent] >= " + colorThresholds.getRedThreshold() + "  and [Percent] <  " + colorThresholds.getYellowThreshold() + ") THEN 1  ELSE 0 END) as [Yellow] " +
//                                    ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] >= " + colorThresholds.getYellowThreshold() + " THEN 1 ELSE 0 END) as [Green] " +
//
//                                     "FROM " +
//                                    "(" +
//
//                                        /* Get A list of each saved tweet and the number of kanji in those tweets */
//
//                                        "SELECT  DISTINCT [Name]" +
//                                        ",[Sys]" +
//                                        ",[UserID] " +
//                                        ",[_id] as [Tweet_id]" +
//                                        "FROM JFavoritesTweets " +
//
//                                    ") as TweetLists " +
//                                " LEFT JOIN " +
//                                " ( " +
//
//                                /* Get a list of  kanji ids and their word scores for each tweet */
//                                "SELECT a.[Tweet_id]" +
//                                ",a.[Edict_id]" +
//                                ",ifnull(b.[Total],0) as [Total] " +
//                                ",ifnull(b.[Correct],0)  as [Correct]" +
//                                ",CAST(ifnull(b.[Correct],0)  as float)/b.[Total] as [Percent] " +
//                                "FROM " +
//                                "( " +
//                                " SELECT Tweet_id" +
//                                        ",Edict_id " +
//                                        "From JSavedTweetKanji " +
//                                ") as a " +
//                                "LEFT JOIN " +
//                                " (" +
//                                    "SELECT [_id] as [Edict_id]" +
//                                    ",sum([Correct]) as [Correct]" +
//                                    ",sum([Total]) as [Total] FROM [JScoreboard] " +
//                                    "where [_id] in (SELECT DISTINCT [Edict_id] FROM JSavedTweetKanji)" +
//                                    " GROUP BY [_id]" +
//                                ") as b " +
//                                "ON a.[Edict_id] = b.[Edict_id] " +
//
//
//
//                        " ) as TweetKanji " +
//                        "On TweetLists.Tweet_id = TweetKanji.Tweet_id " +
//
//                ") as [ListsTweetsAndAllKanjis] " +
//                "GROUP BY [Name],[Sys],[Tweet_id]" +
//                ") as [ListandTweets]  " +
//                ") as [Lists] " +
//                "GROUP BY [Name],[Sys]" +
//
//                ") as yy "  +
//
//                "ON xx.[Name] = yy.[Name] and cast(xx.[Sys] as INTEGER)  = cast(yy.[Sys] as INTEGER)  " +
//                "Order by xx.[Sys] Desc,xx.[Name]"
//                ,null);

        Cursor c = InternalDB.getInstance(getContext()).getTweetListColorBlocksCursor(colorThresholds,null);
Log.d(TAG,"CCOUNT: " +c.getCount());
        if(c.getCount()>0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {

                if(BuildConfig.DEBUG){Log.d(TAG,"NAME: ==" + c.getString(0)
                        + "==, SYS: " + c.getString(1)
                        + ", TOTAL: " + c.getString(2)
                        + ", GREY: " + c.getString(3)
                        + ", (4): " + c.getString(4)+ ", (5): " + c.getString(5));}

                /* We do not want to include favorites star lists that are not active in the user
                * preferences. So if an inactivated list shows up in the sql query, ignore it (don't add to mMenuHeader)*/

                Log.d(TAG,"availableFavoritesStars: " + availableFavoritesStars);
                if(c.getInt(1) != 1 || (availableFavoritesStars.contains(c.getString(0)))) {
                    MenuHeader menuHeader = new MenuHeader(c.getString(0));
                    menuHeader.setChildOptions(childOptions);
                    menuHeader.setMyList(true);

                    menuHeader.setMyListEntry(new MyListEntry(c.getString(0),c.getInt(1)));
                    if(c.getInt(1) == 1 ) {
                        if(BuildConfig.DEBUG){Log.d(TAG,c.getString(0) + " sys ==1 so adding to starlist");}
                        menuHeader.setSystemList(true);
                    }

                    ColorBlockMeasurables colorBlockMeasurables = new ColorBlockMeasurables();
                    colorBlockMeasurables.setGreyCount(c.getInt(3));
                    colorBlockMeasurables.setRedCount(c.getInt(4));
                    colorBlockMeasurables.setYellowCount(c.getInt(5));
                    colorBlockMeasurables.setGreenCount(c.getInt(6));
                    colorBlockMeasurables.setEmptyCount(c.getInt(7));

                    colorBlockMeasurables.setGreyMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getGreyCount())));
                    colorBlockMeasurables.setRedMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getRedCount())));
                    colorBlockMeasurables.setYellowMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getYellowCount())));
                    colorBlockMeasurables.setGreenMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getGreenCount())));
                    colorBlockMeasurables.setEmptyMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getEmptyCount())));

                    menuHeader.setColorBlockMeasurables(colorBlockMeasurables);
                    mMenuHeader.add(menuHeader);
                }

                c.moveToNext();
            }
        }
        c.close();
//        db.close();
//        helper.close();

    }


    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Maximum width of the color bars in the MenuExpandableListAdapter. Right that vlue is set
     * to half of the screenwidth
     * @return maximum width in pixels of colored bars
     *
     * @see SavedTweetsExpandableAdapter
     */
    private int getdimenscore() {

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return Math.round((float)metrics.widthPixels*(float).5);

    }


    public int getExpandableAdapterColorBlockBasicWidths(Activity activity, String text){
        int result = 0;
        if(!text.equals("0")) {
            View view = activity.getLayoutInflater().inflate(R.layout.expandablelistadapter_listitem, null);
            TextView colorBlock = (TextView) view.findViewById(R.id.listitem_colors_1);
            Drawable drawablecolorblock1 = ContextCompat.getDrawable(activity, R.drawable.colorblock);

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                colorBlock.setBackground(drawablecolorblock1);
            } else {
                colorBlock.setBackgroundDrawable(drawablecolorblock1);
            }

            Rect bounds = new Rect();
            Paint textPaint = colorBlock.getPaint();
            textPaint.getTextBounds(text, 0, text.length(), bounds);
            result = (int)textPaint.measureText(text);

            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int padding = (int) (20.0f * metrics.density + 0.5f);
            result += padding;
        }
        return result;

    }

    public void updateMyListAdapter() {
        prepareListData();
        SavedTweetsFragmentAdapter = new SavedTweetsExpandableAdapter(getContext(),mMenuHeader,getdimenscore(),0);
        expListView.setAdapter(SavedTweetsFragmentAdapter);
//        expListView.invalidateViews();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (FragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
}



