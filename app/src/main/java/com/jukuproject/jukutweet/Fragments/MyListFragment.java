package com.jukuproject.jukutweet.Fragments;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Toast;

//import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Adapters.MenuExpandableListAdapter;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.MenuHeader;
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows user-created lists of vocabulary
 */

public class MyListFragment extends Fragment {

    String TAG = "MainMenuTab2";

//    HashMap<String, List<Integer>> listDataHeader_extra;
    MenuExpandableListAdapter MainMenuTab2_listAdapter;
    ExpandableListView expListView;
    ArrayList<MenuHeader> mMenuHeader;
    private int lastExpandedPosition = 0;
//    private ArrayList<String> mActiveStarColors;
//    private int dimenscore = 0;

    private SharedPrefManager sharedPrefManager;



    public static MyListFragment newInstance(Bundle bundle) {
        MyListFragment fragment = new MyListFragment();

//        f.setArguments(bundle);
//        f.assignBundleVars();
        return fragment;
    }

//    public void assignBundleVars(){
//        mylistposition =  getArguments().getInt("mylistposition",0);
//        mylistname =  getArguments().getString("mylistname");
//        mylistsys =  getArguments().getInt("mylistsys",-1);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Call shared prefs to find out which star colors (i.e. favorites lists) to include

        sharedPrefManager = SharedPrefManager.getInstance(getContext());
//        mActiveStarColors = new ArrayList<>();

        View v = inflater.inflate(R.layout.fragment_mylists, container, false);

        mMenuHeader = new ArrayList<>();
//        getdimenscore();
//        mylistposition = -1;
        expListView = (ExpandableListView) v.findViewById(R.id.lvMyListCategory);
        expListView.setClickable(true);
        prepareListData();

        MainMenuTab2_listAdapter = new MenuExpandableListAdapter(getContext(),mMenuHeader,getdimenscore(),0);

        expListView.setAdapter(MainMenuTab2_listAdapter);
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
                } else if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    expListView.collapseGroup(lastExpandedPosition);
                    lastExpandedPosition = groupPosition;
                    expListView.expandGroup(groupPosition);
                    expListView.setSelectedGroup(groupPosition);
                }  else {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "NO LISTS TO EXPAND");
                    }
                }
                return true;   //"True" makes it impossible to collapse the list
            }
        });

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

                if (lastExpandedPosition != -1 && groupPosition != lastExpandedPosition) {
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
//                String childOption = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition); //This is the text in the child that the user clicked
//                int sys = 0;
//                if (addStarList != null && addStarList.size() > groupPosition) {
//
//                    if (addStarList.get(groupPosition) == 1) {
//                        sys = 1;
//                    }
//
//                }
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Header: " + mMenuHeader.get(groupPosition).getHeaderTitle());
                    Log.d(TAG, "Child: " + mMenuHeader.get(groupPosition).getChildOptions().get(childPosition));
                }
                Toast.makeText(getActivity(), "Header: " + mMenuHeader.get(groupPosition).getHeaderTitle() + ", child: " + mMenuHeader.get(groupPosition).getChildOptions().get(childPosition), Toast.LENGTH_SHORT).show();

//                switch (childOption) {
//                    case "Flash Cards":
//                        MenuOptionsDialog a = new MenuOptionsDialog(getActivity(), 0, 0, "flashcards", true, Header, sys, mylistposition);
//                        a.CreateDialog();
//                        break;
//                    case "Browse/Edit":
//
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
//                }
                return false;
            }
        });

        expListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
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


    public void prepareListData() {

        InternalDB helper =  InternalDB.getInstance(getActivity());
        SQLiteDatabase db = helper.getWritableDatabase();

        ArrayList<String> availableFavoritesStars = sharedPrefManager.getActiveFavoriteStars();

        //TODO replace this with a string array
        ArrayList<String> childOptions = new ArrayList<>();
        childOptions.add(getString(R.string.menuchildbrowse)); // Should be its own expandable list
        childOptions.add(getString(R.string.menuchildflashcards));
        childOptions.add(getString(R.string.menuchildmultiplechoice));
        childOptions.add(getString(R.string.menuchildfillinblanks));
        childOptions.add(getString(R.string.menuchildwordbuilder));
        childOptions.add(getString(R.string.menuchildwordmatch));
        childOptions.add(getString(R.string.menuchildstats));


        Cursor c = db.rawQuery("SELECT xx.[Name]" +
                ",xx.[Sys]" +
                ",ifnull(yy.[Total],0) as [Total]" +
                ",ifnull(yy.[Grey],0) as [Grey]" +
                ",ifnull(yy.[Red],0) as [Red]" +
                ",ifnull(yy.[Yellow],0) as [Yellow]" +
                ",ifnull(yy.[Green],0) as [Green] " +
                "" +
                "FROM (" +
                        "SELECT [Name]" +
                        ",0 as [Sys] " +
                        "From JFavoritesLists " +
                    "UNION " +
                        "SELECT 'Blue' as [Name]" +
                                ", 1 as [Sys] " +
                        "Union " +
                        "SELECT 'Red' as [Name]" +
                                ",1 as [Sys] " +
                        "Union " +
                        "SELECT 'Green' as [Name]" +
                                ",1 as [Sys] " +
                        "Union " +
                        "SELECT 'Yellow' as [Name]" +
                                ",1 as [Sys]" +
                        ") as [xx] " +
                "LEFT JOIN (" +
                "SELECT  [Name]" +
                        ",[Sys]" +
                        ",SUM([Grey]) + SUM([Red]) + SUM([Yellow]) + SUM([Green]) as [Total]" +
                        ",SUM([Grey]) as [Grey]" +
                        ",SUM([Red]) as [Red]" +
                        ",SUM([Yellow]) as [Yellow]" +
                        ",SUM([Green]) as [Green] " +
                        "FROM (" +
                            "SELECT [Name]" +
                                    ",[Sys]" +
                                    ",[_id] " +
                                    ",(CASE WHEN [Total] < " + sharedPrefManager.getGreyThreshold() + " THEN 1 ELSE 0 END) as [Grey] " +
                                    ",(CASE WHEN [Total] >= " + sharedPrefManager.getGreyThreshold() + " and [Percent] < " + sharedPrefManager.getRedThreshold() + "  THEN 1  ELSE 0 END) as [Red] " +
                                    ",(CASE WHEN [Total] >= " + sharedPrefManager.getGreyThreshold() + " and ([Percent] >= " + sharedPrefManager.getRedThreshold() + "  and [Percent] <  " + sharedPrefManager.getYellowThreshold() + ") THEN 1  ELSE 0 END) as [Yellow] " +
                                    ",(CASE WHEN [Total] >= " + sharedPrefManager.getGreyThreshold() + " and [Percent] >= " + sharedPrefManager.getYellowThreshold() + " THEN 1 ELSE 0 END) as [Green] " +
                                    "FROM  (" +
                                        "SELECT a.[Name]" +
                                                ",a.[Sys]" +
                                                ",a.[_id]" +
                                                ",ifnull(b.[Total],0) as [Total] " +
                                                ",ifnull(b.[Correct],0)  as [Correct]" +
                                                ",CAST(ifnull(b.[Correct],0)  as float)/b.[Total] as [Percent] " +
                                                "FROM (" +
                                                        "SELECT  DISTINCT [Name]" +
                                                                        ",[Sys]" +
                                                                        ",[_id] " +
                                                        "FROM JFavorites" +
                                                    ") as a " +
                                                "LEFT JOIN  (" +
                                                    "SELECT [_id]" +
                                                            ",sum([Correct]) as [Correct]" +
                                                            ",sum([Total]) as [Total] FROM [JScoreboard] " +
                                                            "where [_id] in (SELECT DISTINCT [_id] FROM JFavorites)" +
                                                    " GROUP BY [_id]" +
                                                    ") as b " +
                                        "ON a.[_id] = b.[_id]) " +
                                    " as x) as y " +
                            "GROUP BY [Name],[Sys]" +
                    ") as yy  " +
                "ON xx.[Name] = yy.[Name] and xx.[sys] = yy.[sys]  " +
                "Order by xx.[Sys] Desc,xx.[Name]",null);

        c.moveToFirst();
        if(c.getCount()>0) {
            while (!c.isAfterLast()) {

                if(BuildConfig.DEBUG){Log.d(TAG,"PULLING NAME: " + c.getString(0) + ", SYS: " + c.getString(1) + ", TOTAL: " + c.getString(2) + ", GREY: " + c.getString(3));}
                if(BuildConfig.DEBUG){Log.d("yes", "pulling list: " + c.getString(0) + ", sys: " + c.getString(1));}

//                    if(mylistname != null && mylistsys>=0
//                            && mylistname.equals(c.getString(0)) && mylistsys == c.getInt(1)) {
//                        mylistposition = listDataHeader.size();
//                        if(BuildConfig.DEBUG){Log.d("yes","mylistposition preparelist: " + listDataHeader.size());}
//                    }

                /* We do not want to include favorites star lists that are not active in the user
                * preferences. So if an inactivated list shows up in the sql query, ignore it (don't add to mMenuHeader)*/

                Log.d(TAG,"availableFavoritesStars: " + availableFavoritesStars);
                if(c.getInt(1) != 1 || (availableFavoritesStars.contains(c.getString(0)))) {
                    MenuHeader menuHeader = new MenuHeader(c.getString(0));
                    menuHeader.setChildOptions(childOptions);
                    menuHeader.setMyList(true);

                    if(c.getInt(1) == 1 ) {
                        if(BuildConfig.DEBUG){Log.d(TAG,c.getString(0) + " sys ==1 so adding to starlist");}
                        menuHeader.setSystemList(true);
                    }

                    ColorBlockMeasurables colorBlockMeasurables = new ColorBlockMeasurables();
                    colorBlockMeasurables.setGreyCount(c.getInt(3));
                    colorBlockMeasurables.setRedCount(c.getInt(4));
                    colorBlockMeasurables.setYellowCount(c.getInt(5));
                    colorBlockMeasurables.setGreenCount(c.getInt(6));

                    colorBlockMeasurables.setGreyMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getGreyCount())));
                    colorBlockMeasurables.setRedMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getRedCount())));
                    colorBlockMeasurables.setYellowMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getYellowCount())));
                    colorBlockMeasurables.setGreenMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getGreenCount())));
                    menuHeader.setColorBlockMeasurables(colorBlockMeasurables);
                    mMenuHeader.add(menuHeader);
                }


                c.moveToNext();
            }
        }
        c.close();
        db.close();
        helper.close();




//        for ( Map.Entry<String,String> entry : listDataHeaderKeyXRef.entrySet()) {
//            String namekey= entry.getKey();
//            String name = entry.getValue();
//            int count;
//
//            if(listDataHeader_extra.containsKey(namekey)) {
//                count = listDataHeader_extra.get(namekey).get(0);
//            } else {
//                count = 0;
//            }
//            if(BuildConfig.DEBUG){Log.d(TAG,"adding listdatachild " +  namekey + " count: " + count);}
//            if(name.equalsIgnoreCase("Create New List") || count == 0) {
//                List<String> emptylist = new ArrayList<String>();
//                listDataChild.put(name,emptylist);
//            } else {
//                listDataChild.put(name,myListChild);
//            }
//        }
    }


    @Override
    public void onResume() {

        super.onResume();

//        prepareListData();
//        if(dimenscore==0){getdimenscore();}
//        MainMenuTab2_listAdapter = new MenuExpandableListAdapter(getActivity(), listDataHeader, listDataChild, listDataHeader_extra, false, addStarList,dimenscore,24,new ArrayList<String>(),sharedPrefManager.getShowlblheadercount(),true);
//        expListView.setAdapter(MainMenuTab2_listAdapter);
//        expListView.invalidateViews();
//
//        if(listDataHeader != null && listDataHeader.size()>1) {
//            expListView.expandGroup(lastExpandedPosition);
//            expListView.setSelectedGroup(lastExpandedPosition);
//        } else {
//            if(BuildConfig.DEBUG){Log.d(TAG, "NO LISTS TO EXPAND");}
//        }


    }

    private int getdimenscore() {
        /** Get width of screen */
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
}

