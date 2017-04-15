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

import com.jukuproject.jukutweet.Adapters.MyListExpandableAdapter;
import com.jukuproject.jukutweet.BaseContainerFragment;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.QuizMenuDialog;
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

public class MyListFragment  extends Fragment{

    String TAG = "MyListFragment";
    FragmentInteractionListener mCallback;
    MyListExpandableAdapter MyListFragmentAdapter;
    ExpandableListView expListView;
    ArrayList<MenuHeader> mMenuHeader;
    private int lastExpandedPosition = -1;
    private SharedPrefManager sharedPrefManager;

    public static MyListFragment newInstance(Bundle bundle) {
        MyListFragment fragment = new MyListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Call shared prefs to find out which star colors (i.e. favorites lists) to include
        sharedPrefManager = SharedPrefManager.getInstance(getContext());

        View v = inflater.inflate(R.layout.fragment_mylists, container, false);


        expListView = (ExpandableListView) v.findViewById(R.id.lvMyListCategory);
        expListView.setClickable(true);
        prepareListData();

        MyListFragmentAdapter = new MyListExpandableAdapter(getContext(),mMenuHeader,getdimenscore(),0);

        expListView.setAdapter(MyListFragmentAdapter);
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

//                    if(lastExpandedPosition != groupPosition) {
//                        expListView.collapseGroup(lastExpandedPosition);
//                    }

                    expandTheListViewAtPosition(groupPosition);

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
                String childOption = mMenuHeader.get(groupPosition).getChildOptions().get(childPosition); //This is the text in the child that the user clicked
                switch (childOption) {
                    case "Browse/Edit":
                        MyListBrowseFragment fragment = MyListBrowseFragment.newInstance(new MyListEntry(mMenuHeader.get(groupPosition).getHeaderTitle(),mMenuHeader.get(groupPosition).getSystemList()));
                        ((BaseContainerFragment)getParentFragment()).replaceFragment(fragment, true,"mylistbrowse");
                        //Hide the fab
                        mCallback.showFab(false,"");
                        break;
                    case "Flash Cards":
                        if(getFragmentManager().findFragmentByTag("quizmenu") == null || !getFragmentManager().findFragmentByTag("quizmenu").isAdded()) {
                            QuizMenuDialog.newInstance("flashcards"
                                    ,2
                                    ,lastExpandedPosition
                                    ,mMenuHeader.get(groupPosition).getMyListEntry()
                                    ,mMenuHeader.get(groupPosition).getColorBlockMeasurables()
                                    ,getdimenscore()).show(getActivity().getSupportFragmentManager()
                                    ,"dialogQuizMenu");
                            mCallback.showFab(false,"");
                        }

                        break;
                    case "Multiple Choice":
                        if(getFragmentManager().findFragmentByTag("quizmenu") == null || !getFragmentManager().findFragmentByTag("quizmenu").isAdded()) {
                            QuizMenuDialog.newInstance("multiplechoice"
                                    ,2
                                    ,lastExpandedPosition
                                    ,mMenuHeader.get(groupPosition).getMyListEntry()
                                    ,mMenuHeader.get(groupPosition).getColorBlockMeasurables()
                                    ,getdimenscore()).show(getActivity().getSupportFragmentManager(),"dialogQuizMenu");
//                            mCallback.showFab(false,"");
                        }


                        break;

                    case "Fill in the Blanks":
                        if(getFragmentManager().findFragmentByTag("quizmenu") == null || !getFragmentManager().findFragmentByTag("quizmenu").isAdded()) {
                            QuizMenuDialog.newInstance("fillintheblanks"
                                    ,2
                                    ,lastExpandedPosition
                                    ,mMenuHeader.get(groupPosition).getMyListEntry()
                                    ,mMenuHeader.get(groupPosition).getColorBlockMeasurables()
                                    ,getdimenscore()).show(getActivity().getSupportFragmentManager(),"dialogQuizMenu");
//                            mCallback.showFab(false,"");
                        }

                        break;
                    case "Stats":
                        MyListEntry myListEntry = new MyListEntry(mMenuHeader.get(groupPosition).getHeaderTitle(),mMenuHeader.get(groupPosition).getSystemList());
                        ColorBlockMeasurables colorBlockMeasurables = prepareColorBlockDataForList(myListEntry);

                        StatsFragmentProgress statsFragmentProgress = StatsFragmentProgress.newInstance(myListEntry
                                , 10
                                ,colorBlockMeasurables);
                        ((BaseContainerFragment)getParentFragment()).replaceFragment(statsFragmentProgress, true,"mylistbrowse");
                        mCallback.showFab(false,"");
                        break;

                    default:
                        break;
                }
                return false;
            }
        });

        expListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mCallback.showEditMyListDialog("MyList",mMenuHeader.get(position).getHeaderTitle(),mMenuHeader.get(position).isSystemList());
                return false;
            }
        });

        expListView.setGroupIndicator(null);


        //Expand the last expanded position (or expand first availalbe non-empty list)
        if(lastExpandedPosition >=0) {
            expandTheListViewAtPosition(lastExpandedPosition);
        }


        setRetainInstance(true);
        return v;
    }

    public void expandTheListViewAtPosition(int position) {

        if(lastExpandedPosition >=0 && mMenuHeader.size()>lastExpandedPosition) {
            expListView.collapseGroup(lastExpandedPosition);
        }

        expListView.expandGroup(position);
        expListView.setSelectedGroup(position);
        mMenuHeader.get(position).setExpanded(true);
        lastExpandedPosition = position;
    };

    public void prepareListData() {
        mMenuHeader = new ArrayList<>();
//        InternalDB helper =  InternalDB.getInstance(getActivity());
//        SQLiteDatabase db = helper.getWritableDatabase();

        ArrayList<String> availableFavoritesStars = sharedPrefManager.getActiveFavoriteStars();
        ColorThresholds colorThresholds = sharedPrefManager.getColorThresholds();
       ArrayList<String> childOptions = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.menu_mylist)));

        Cursor c = InternalDB.getWordInterfaceInstance(getContext()).getWordListColorBlockCursor(colorThresholds,null);

        if(c.getCount()>0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {

                if(BuildConfig.DEBUG){Log.d(TAG,"PULLING NAME: " + c.getString(0) + ", SYS: " + c.getString(1) + ", TOTAL: " + c.getString(2) + ", GREY: " + c.getString(3));}
                if(BuildConfig.DEBUG){Log.d("yes", "pulling list: " + c.getString(0) + ", sys: " + c.getString(1));}

                /* We do not want to include favorites star lists that are not active in the user
                * preferences. So if an inactivated list shows up in the sql query, ignore it (don't add to mMenuHeader)*/

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
                    colorBlockMeasurables.setEmptyCount(0);

                      /* Set the first available non-empty list to be automatically expanded when
                      fragment is created. This is achieved by making it the "lastexpandedposition" from the get-go */
                    if(lastExpandedPosition<0 && c.getInt(2)>0) {
                        lastExpandedPosition = mMenuHeader.size();
                    }

                    colorBlockMeasurables.setGreyMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getGreyCount())));
                    colorBlockMeasurables.setRedMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getRedCount())));
                    colorBlockMeasurables.setYellowMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getYellowCount())));
                    colorBlockMeasurables.setGreenMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getGreenCount())));
                    colorBlockMeasurables.setEmptyMinWidth(0);
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
     * @see MyListExpandableAdapter
     */
    private int getdimenscore() {

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
      return Math.round((float)metrics.widthPixels*(float).5);

    }


    public static int getExpandableAdapterColorBlockBasicWidths(Activity activity, String text){
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
        MyListFragmentAdapter = new MyListExpandableAdapter(getContext(),mMenuHeader,getdimenscore(),0);
        expListView.setAdapter(MyListFragmentAdapter);
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

    public ColorBlockMeasurables prepareColorBlockDataForList(MyListEntry myListEntry) {
        ColorBlockMeasurables colorBlockMeasurables = new ColorBlockMeasurables();

        ColorThresholds colorThresholds = SharedPrefManager.getInstance(getContext()).getColorThresholds();
        Cursor c = InternalDB.getWordInterfaceInstance(getContext()).getWordListColorBlockCursor(colorThresholds,myListEntry);

        if(c.getCount()>0) {
            c.moveToFirst();

                /* We do not want to include favorites star lists that are not active in the user
                * preferences. So if an inactivated list shows up in the sql query, ignore it (don't add to mMenuHeader)*/

            colorBlockMeasurables.setGreyCount(c.getInt(3));
            colorBlockMeasurables.setRedCount(c.getInt(4));
            colorBlockMeasurables.setYellowCount(c.getInt(5));
            colorBlockMeasurables.setGreenCount(c.getInt(6));
            colorBlockMeasurables.setEmptyCount(0);

            colorBlockMeasurables.setGreyMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getGreyCount())));
            colorBlockMeasurables.setRedMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getRedCount())));
            colorBlockMeasurables.setYellowMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getYellowCount())));
            colorBlockMeasurables.setGreenMinWidth(getExpandableAdapterColorBlockBasicWidths(getActivity(), String.valueOf(colorBlockMeasurables.getGreenCount())));
            colorBlockMeasurables.setEmptyMinWidth(0);



            c.moveToNext();
        }
        c.close();
        return  colorBlockMeasurables;
    }
}

