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

//import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.MenuExpandableListAdapter;
import com.jukuproject.jukutweet.Models.SharedPrefManager;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows user-created lists of vocabulary
 */
//public class MyListFragment extends Fragment {
//    FragmentInteractionListener mCallback;
//
//    /*Tracks elapsed time since last click of a recyclerview row. Used to
//    * keep from constantly recieving button clicks through the RxBus */
////    private long mLastClickTime = 0;
////    private RxBus _rxBus = new RxBus();
////    private RecyclerView mRecyclerView;
////    UserListAdapter mAdapter;
////    private TextView mNoLists;
//
//    public MyListFragment() {}
//
//    /**
//     * Returns a new instance of UserListFragment
//     */
//    public static MyListFragment newInstance() {
////        UserListFragment fragment = new UserListFragment();
////        Bundle args = new Bundle();
////        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
////        fragment.setArguments(args);
//        return new MyListFragment();
//    }
//
//
//
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.testlayout, container, false);
//        TextView test = (TextView) view.findViewById(R.id.textView);
//        test.setText("MyList FRAGMENT");
////        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerMain);
////        mNoLists = (TextView) view.findViewById(R.id.nolists);
//
//        return view;
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
////        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
////        mRecyclerView.setLayoutManager(layoutManager);
////        updateAdapter();
//    }
//
//    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
////        updateAdapter();
//    }
//
//
//
//
//
////    /**
////     * Toggles between showing recycler (if there are followed users in the database)
////     * and hiding the recycler while showing the "no users found" message if there are not
////     * @param show bool True to show recycler, False to hide it
////     */
////    private void showRecyclerView(boolean show) {
////        if(show) {
////            mRecyclerView.setVisibility(View.VISIBLE);
////            mNoLists.setVisibility(View.GONE);
////        } else {
////            mRecyclerView.setVisibility(View.GONE);
////            mNoLists.setVisibility(View.VISIBLE);
////        }
////    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        try {
//            mCallback = (FragmentInteractionListener) context;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(context.toString()
//                    + " must implement OnHeadlineSelectedListener");
//        }
//    }
//
//    /**
//     * Checks how many milliseconds have elapsed since the last time "mLastClickTime" was updated
//     * If enough time has elapsed, returns True and updates mLastClickTime.
//     * This is to stop unwanted rapid clicks of the same button
//     * @param elapsedMilliSeconds threshold of elapsed milliseconds before a new button click is allowed
//     * @return bool True if enough time has elapsed, false if not
//     */
////    public boolean isUniqueClick(int elapsedMilliSeconds) {
////        if(SystemClock.elapsedRealtime() - mLastClickTime > elapsedMilliSeconds) {
////            mLastClickTime = SystemClock.elapsedRealtime();
////            return true;
////        } else {
////            return false;
////        }
////    }
//
//}

public class MyListFragment extends Fragment {

    String TAG = "MainMenuTab2";

    HashMap<String, List<Integer>> listDataHeader_extra;
    public static MenuExpandableListAdapter MainMenuTab2_listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    List<Integer> addStarList  = new ArrayList<Integer>();//This keeps track (with int = 1) of favorites lists that we want to add a star picture to in the menu.
    HashMap<String, List<String>> listDataChild;
    private int lastExpandedPosition = 0;
//    private int mylistposition;
    private int dimenscore = 0;

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
        View v = inflater.inflate(R.layout.fragment_mylists, container, false);

        getdimenscore();
//        mylistposition = -1;
        expListView = (ExpandableListView) v.findViewById(R.id.lvMyListCategory);
        expListView.setClickable(true);
        prepareListData();

        MainMenuTab2_listAdapter = new MenuExpandableListAdapter(getActivity(), listDataHeader, listDataChild,listDataHeader_extra,false,addStarList,0,24,new ArrayList<String>(),sharedPrefManager.getShowlblheadercount(),true);

        expListView.setAdapter(MainMenuTab2_listAdapter);
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {

                String Header = listDataHeader.get(groupPosition);
                if (listDataChild.containsKey(Header) && (listDataChild.get(Header) == null || listDataChild.get(Header).size() == 0)) {

                    TextView lblListHeaderCount = (TextView) v.findViewById(R.id.lblListHeaderCount);
                    if (lblListHeaderCount.getVisibility() == TextView.VISIBLE && lblListHeaderCount.getText().toString().length() > 0) {
                        lblListHeaderCount.setVisibility(TextView.GONE);
                    } else {
                        lblListHeaderCount.setVisibility(TextView.VISIBLE);
                        lblListHeaderCount.setText(getString(R.string.empty_parenthesis));
                    }
                } else if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition
                        && listDataChild.get(listDataHeader.get(groupPosition)).size() > 0
                        ) {
                    expListView.collapseGroup(lastExpandedPosition);
                    lastExpandedPosition = groupPosition;
                    expListView.expandGroup(groupPosition);
                    expListView.setSelectedGroup(groupPosition);
                } else if (listDataChild.containsKey(Header) && listDataChild.get(Header).size() == 0) {

                } else if (listDataHeader != null && listDataHeader.size() > 1) {


                    expListView.expandGroup(0);
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "NO LISTS TO EXPAND");
                    }
                }
                return true;   //"True" makes it impossible to collapse the list
            }
        });

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition
                        && !listDataHeader.get(groupPosition).equalsIgnoreCase("Create New List")
                        && listDataChild.get(listDataHeader.get(groupPosition)).size() > 0
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
                String Header = listDataHeader.get(groupPosition);
                String childOption = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition); //This is the text in the child that the user clicked
//                int sys = 0;
//                if (addStarList != null && addStarList.size() > groupPosition) {
//
//                    if (addStarList.get(groupPosition) == 1) {
//                        sys = 1;
//                    }
//
//                }

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Header: " + Header);
                    Log.d(TAG, "Child: " + childOption);
                }

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
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        listDataHeader_extra = new HashMap<>();
        addStarList = new ArrayList<>();


        final List<String> myListChild = new ArrayList<String>();
        InternalDB helper =  InternalDB.getInstance(getActivity());
        SQLiteDatabase db = helper.getWritableDatabase();
        HashMap<String,String> listDataHeaderKeyXRef = new HashMap<>();

        Cursor c = db.rawQuery("SELECT xx.[Name],xx.[Sys],ifnull(yy.[Total],0) as [Total] ,ifnull(yy.[Grey],0) as [Grey],ifnull(yy.[Red],0) as [Red],ifnull(yy.[Yellow],0) as [Yellow],ifnull(yy.[Green],0) as [Green] FROM (SELECT [Name],0 as [Sys] From JFavoritesLists Union SELECT 'Blue' as [Name], 1 as [Sys] Union SELECT 'Red' as [Name],1 as [Sys] Union SELECT 'Green' as [Name],1 as [Sys] Union SELECT 'Yellow' as [Name],1 as [Sys]) as [xx] LEFT JOIN (SELECT  [Name],[Sys],SUM([Grey]) + SUM([Red]) + SUM([Yellow]) + SUM([Green]) as [Total],SUM([Grey]) as [Grey],SUM([Red]) as [Red],SUM([Yellow]) as [Yellow],SUM([Green]) as [Green] FROM (SELECT [Name],[Sys],[_id] ,(CASE WHEN [Total] < " + sharedPrefManager.getGreyThreshold() + " THEN 1 ELSE 0 END) as [Grey] ,(CASE WHEN [Total] >= " + sharedPrefManager.getGreyThreshold() + " and [Percent] < " + sharedPrefManager.getRedThreshold() + "  THEN 1  ELSE 0 END) as [Red] ,(CASE WHEN [Total] >= " + sharedPrefManager.getGreyThreshold() + " and ([Percent] >= " + sharedPrefManager.getRedThreshold() + "  and [Percent] <  " + sharedPrefManager.getYellowThreshold() + ") THEN 1  ELSE 0 END) as [Yellow] ,(CASE WHEN [Total] >= " + sharedPrefManager.getGreyThreshold() + " and [Percent] >= " + sharedPrefManager.getYellowThreshold() + " THEN 1 ELSE 0 END) as [Green] FROM  (SELECT a.[Name],a.[Sys],a.[_id],ifnull(b.[Total],0) as [Total] ,ifnull(b.[Correct],0)  as [Correct],CAST(ifnull(b.[Correct],0)  as float)/b.[Total] as [Percent] FROM (SELECT  DISTINCT [Name],[Sys],[_id] FROM JFavorites) as a LEFT JOIN  (SELECT [_id],sum([Correct]) as [Correct],sum([Total]) as [Total] FROM [JScoreboard]  where [_id] in (SELECT DISTINCT [_id] FROM JFavorites) GROUP BY [_id]) as b ON a.[_id] = b.[_id]) as x) as y GROUP BY [Name],[Sys]) as yy  ON xx.[Name] = yy.[Name] and xx.[sys] = yy.[sys]  Order by xx.[Sys] Desc,xx.[Name]",null);
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
                    listDataHeader.add(c.getString(0));

                    listDataHeaderKeyXRef.put(c.getString(0) + c.getString(1), c.getString(0));

                    if(c.getInt(1) == 1 ) {
                        if(BuildConfig.DEBUG){Log.d(TAG,c.getString(0) + " sys ==1 so adding to starlist");}
                        addStarList.add(1);
                        if(BuildConfig.DEBUG) {Log.d(TAG, "New star list size: " + addStarList.size());}
                    }

                    int grey = c.getInt(3);
                    int red = c.getInt(4);
                    int yellow = c.getInt(5);
                    int green = c.getInt(6);

                    List<Integer> extra = new ArrayList<Integer>();
                    extra.add(c.getInt(2)); //Count
                    extra.add(grey); //Grey
                    extra.add(red); //Red
                    extra.add(yellow); //Yellow
                    extra.add(green); //Green

//                    AppGlobal globalContext = (AppGlobal)getActivity().getApplication();
                    extra.add(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(grey)));
                    extra.add(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(red)));
                    extra.add(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(yellow)));
                    extra.add(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(green)));

                    //PROBABLY THROW AN ERROR IF THEY DON'T MATCH...........
                    String namekey =  c.getString(0) + c.getString(1);
                    listDataHeader_extra.put(namekey, extra);

                    if(BuildConfig.DEBUG){Log.d(TAG,"putting: " + namekey + " - count = " + c.getString(2));}



                c.moveToNext();
            }
        }
        c.close();
        db.close();
        helper.close();

        myListChild.add(getString(R.string.menuchildbrowse)); // Should be its own expandable list
        myListChild.add(getString(R.string.menuchildflashcards));
        myListChild.add(getString(R.string.menuchildmultiplechoice));
        myListChild.add(getString(R.string.menuchildfillinblanks));
        myListChild.add(getString(R.string.menuchildwordbuilder));
        myListChild.add(getString(R.string.menuchildwordmatch));

        myListChild.add(getString(R.string.menuchildstats));


        for ( Map.Entry<String,String> entry : listDataHeaderKeyXRef.entrySet()) {
            String namekey= entry.getKey();
            String name = entry.getValue();
            int count;

            if(listDataHeader_extra.containsKey(namekey)) {
                count = listDataHeader_extra.get(namekey).get(0);
            } else {
                count = 0;
            }
            if(BuildConfig.DEBUG){Log.d(TAG,"adding listdatachild " +  namekey + " count: " + count);}
            if(name.equalsIgnoreCase("Create New List") || count == 0) {
                List<String> emptylist = new ArrayList<String>();
                listDataChild.put(name,emptylist);
            } else {
                listDataChild.put(name,myListChild);
            }
        }
    }


    @Override
    public void onResume() {

        super.onResume();

        prepareListData();
        if(dimenscore==0){getdimenscore();}
        MainMenuTab2_listAdapter = new MenuExpandableListAdapter(getActivity(), listDataHeader, listDataChild, listDataHeader_extra, false, addStarList,dimenscore,24,new ArrayList<String>(),sharedPrefManager.getShowlblheadercount(),true);
        expListView.setAdapter(MainMenuTab2_listAdapter);
        expListView.invalidateViews();

        if(listDataHeader != null && listDataHeader.size()>1) {
            expListView.expandGroup(lastExpandedPosition);
            expListView.setSelectedGroup(lastExpandedPosition);
        } else {
            if(BuildConfig.DEBUG){Log.d(TAG, "NO LISTS TO EXPAND");}
        }


    }

    private void getdimenscore() {
        /** Get width of screen */
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        dimenscore = Math.round((float)metrics.widthPixels*(float).5);

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

