package com.jukuproject.jukutweet.Fragments;

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

import com.jukuproject.jukutweet.Adapters.WordListExpandableAdapter;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.QuizMenuDialog;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MenuHeader;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;
import com.jukuproject.jukutweet.TabContainers.BaseContainerFragment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Shows lists of saved vocabulary (both user-created lists and system defaults). Click
 * on a list to show sub-options ("Browse","Multiple Choice", "Stats" etc)
 */

public class WordListFragment extends Fragment {

    String TAG = "WordListFragment";
    FragmentInteractionListener mCallback;
    WordListExpandableAdapter MyListFragmentAdapter;
    ExpandableListView expListView;
    ArrayList<MenuHeader> mMenuHeader;
    private int lastExpandedPosition = -1;
    private SharedPrefManager sharedPrefManager;
    private TextView txtNoListsFound;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Call shared prefs to find out which star colors (i.e. favorites lists) to include
        sharedPrefManager = SharedPrefManager.getInstance(getContext());

        View v = inflater.inflate(R.layout.fragment_wordandtweet_lists, container, false);
        expListView = (ExpandableListView) v.findViewById(R.id.lvMyListCategory);
        txtNoListsFound = (TextView) v.findViewById(R.id.nolistsfound);
        setRetainInstance(true);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        expListView.setClickable(true);

        if(savedInstanceState != null) {
            mMenuHeader = savedInstanceState.getParcelableArrayList("mMenuHeader");
        } else {

            prepareListData();
        }

        MyListFragmentAdapter = new WordListExpandableAdapter(getContext(),mMenuHeader,getdimenscore(),0);
        expListView.setAdapter(MyListFragmentAdapter);
        if(mMenuHeader.size()==0) {
            txtNoListsFound.setVisibility(View.VISIBLE);
        } else {
            txtNoListsFound.setVisibility(View.GONE);
        }

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

                if(mMenuHeader.get(groupPosition).getColorBlockMeasurables() == null
                        || mMenuHeader.get(groupPosition).getColorBlockMeasurables() == null
                        || mMenuHeader.get(groupPosition).getColorBlockMeasurables().getTotalCount()==0) {
                    return false;
                }


                switch (childOption) {
                    case "Browse/Edit":

                        WordListBrowseFragment fragment = WordListBrowseFragment.newInstance(new MyListEntry(mMenuHeader.get(groupPosition).getHeaderTitle(),mMenuHeader.get(groupPosition).getSystemList()));
                        ((BaseContainerFragment)getParentFragment()).replaceFragment(fragment, true,"mylistbrowse");
                        //Hide the fab
                        mCallback.showFab(false,"");
                        mCallback.showActionBarBackButton(true,mMenuHeader.get(groupPosition).getHeaderTitle(),2);
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

                            mCallback.showActionBarBackButton(true,mMenuHeader.get(groupPosition).getHeaderTitle(),2);
                            mCallback.showFab(false);
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
                        }

                        break;
                    case "Stats":
                        MyListEntry myListEntry = new MyListEntry(mMenuHeader.get(groupPosition).getHeaderTitle(),mMenuHeader.get(groupPosition).getSystemList());

                        View colorBlockMinWidthEstimateView = getActivity().getLayoutInflater().inflate(R.layout.expandablelistadapter_listitem, null);
                        DisplayMetrics metrics = new DisplayMetrics();
                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                        float metricsDensity = metrics.density;

                        ColorBlockMeasurables colorBlockMeasurables = prepareColorBlockDataForAList(getContext(),myListEntry,false,colorBlockMinWidthEstimateView,metricsDensity);


                        StatsFragmentProgress statsFragmentProgress = StatsFragmentProgress.newWordListInstance(myListEntry
                                , 10
                                ,colorBlockMeasurables);
                        ((BaseContainerFragment)getParentFragment()).replaceFragment(statsFragmentProgress, true,"wordlistStats");
                        mCallback.showFab(false,"");
                        mCallback.showActionBarBackButton(true,mMenuHeader.get(groupPosition).getHeaderTitle(),2);
                        break;

                    default:
                        break;
                }
                return false;
            }
        });

        expListView.setOnItemLongClickListener(new ExpandableListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    mCallback.showEditMyListDialog("MyList",mMenuHeader.get(groupPosition).getHeaderTitle(),mMenuHeader.get(groupPosition).isSystemList());
                }

                return false;
            }
        });

        //Expand the last expanded position (or expand first availalbe non-empty list)
        if(lastExpandedPosition >=0) {
            expandTheListViewAtPosition(lastExpandedPosition);
        }

    }

    /**
     * Expands a listview group at a given position, closing any other open groups
     * @param position listview position to expand
     */
    public void expandTheListViewAtPosition(int position) {

        if(lastExpandedPosition >=0 && mMenuHeader.size()>lastExpandedPosition) {
            expListView.collapseGroup(lastExpandedPosition);
        }

        try {
            expListView.expandGroup(position);
            expListView.setSelectedGroup(position);
            mMenuHeader.get(position).setExpanded(true);
            lastExpandedPosition = position;
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG,"expandthelistview at position " + position + " index out of bounds");
            lastExpandedPosition = -1;
        }
    }

    /**
     * Prepares header entries for the mMenuHeader dataset, with {@link ColorBlockMeasurables} representing the WordList
     * broken down by word score color categories (colorblocks are displayed next to the "Browse/Edit" entry for an expanded word list)
     */
    public void prepareListData() {
        mMenuHeader = new ArrayList<>();
        ArrayList<String> availableFavoritesStars = sharedPrefManager.getActiveFavoriteStars();
        ColorThresholds colorThresholds = sharedPrefManager.getColorThresholds();
       ArrayList<String> childOptions = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.menu_mylist)));

        //pull pieces necessary to getExpandableAdapterColorBlockBasicWidths
        View colorBlockMinWidthEstimateView = getActivity().getLayoutInflater().inflate(R.layout.expandablelistadapter_listitem, null);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float metricsDensity = metrics.density;

        Cursor c = InternalDB.getWordInterfaceInstance(getContext()).getWordListColorBlockCursor(colorThresholds,null);
        if(c.getCount()>0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {

                if(BuildConfig.DEBUG){Log.d(TAG,"PULLING NAME: " + c.getString(0) + ", SYS: " + c.getString(1) + ", TOTAL: " + c.getString(2)
                        + ", GREY: " + c.getString(3)
                        + ", YELLOW: " + c.getString(5));}

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

                    /* Coloblock measurables determine which blocks appear next to the "Browse/Edit" listView
                    * child option. They are based on the word score of the words in the list. */
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

                    colorBlockMeasurables.setGreyMinWidth(getExpandableAdapterColorBlockBasicWidths(colorBlockMinWidthEstimateView,getContext(), String.valueOf(colorBlockMeasurables.getGreyCount()),metricsDensity));
                    colorBlockMeasurables.setRedMinWidth(getExpandableAdapterColorBlockBasicWidths(colorBlockMinWidthEstimateView,getContext(), String.valueOf(colorBlockMeasurables.getRedCount()),metricsDensity));
                    colorBlockMeasurables.setYellowMinWidth(getExpandableAdapterColorBlockBasicWidths(colorBlockMinWidthEstimateView,getContext(), String.valueOf(colorBlockMeasurables.getYellowCount()),metricsDensity));
                    colorBlockMeasurables.setGreenMinWidth(getExpandableAdapterColorBlockBasicWidths(colorBlockMinWidthEstimateView,getContext(), String.valueOf(colorBlockMeasurables.getGreenCount()),metricsDensity));
                    colorBlockMeasurables.setEmptyMinWidth(0);
                    menuHeader.setColorBlockMeasurables(colorBlockMeasurables);
                    mMenuHeader.add(menuHeader);

                }


                c.moveToNext();
            }
        }
        c.close();
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
     * @see WordListExpandableAdapter
     */
    private int getdimenscore() {

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if(metrics.heightPixels>metrics.widthPixels) {
            return Math.round((float)metrics.widthPixels*(float).5);
        } else {
            return Math.round((float)metrics.heightPixels*(float).5);
        }

    }


    /**
     * Returns the minimum possible width of a "colorblock" textview, given the text that must
     * be displayed inside the block. If the block has to display "135", it's minimum width will be larger
     * than if it only had to display a one or two digit string like "5"
     * @param view inflated "expandablelistadapter_listitem" view to help estimate the minimum width of a color block
     * @param context context
     * @param text text to be displayed in colorblock
     * @param metricDensity density to help get accurate padding for colorblock width estimate
     * @return min possible width of a colorblock
     *
     * @see #prepareColorBlockDataForAList(Context, Object, boolean, View, float)
     * @see com.jukuproject.jukutweet.PostQuizStatsActivity
     */
    public static int getExpandableAdapterColorBlockBasicWidths(View view, Context context, String text, float metricDensity){
        int result = 0;
        if(!text.equals("0")) {
            TextView colorBlock = (TextView) view.findViewById(R.id.listitem_colors_1);
            Drawable drawablecolorblock1 = ContextCompat.getDrawable(context, R.drawable.colorblock);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                colorBlock.setBackground(drawablecolorblock1);
            } else {
                colorBlock.setBackgroundDrawable(drawablecolorblock1);
            }

            Rect bounds = new Rect();
            Paint textPaint = colorBlock.getPaint();
            textPaint.getTextBounds(text, 0, text.length(), bounds);
            result = (int)textPaint.measureText(text);
            int padding = (int) (20.0f * metricDensity + 0.5f);
            result += padding;
        }
        return result;

    }

    /**
     * Pulls a fresh set of wordlist data from the database and resets the adapter. Called from MainActivity when
     * a wordlist is created/edited/deleted
     *
     * @see com.jukuproject.jukutweet.MainActivity#deleteOrClearDialogFinal(String, Boolean, String, boolean)
     * @see com.jukuproject.jukutweet.MainActivity#onAddMyListDialogPositiveClick(String, String)
     * @see com.jukuproject.jukutweet.MainActivity#onRenameMyListDialogPositiveClick(String, String, String)
     */
    public void updateMyListAdapter() {
        if(isAdded() && !isDetached()) {
            prepareListData();
            MyListFragmentAdapter = new WordListExpandableAdapter(getContext(),mMenuHeader,getdimenscore(),0);
            expListView.setAdapter(MyListFragmentAdapter);
            if(mMenuHeader.size()==0) {
                txtNoListsFound.setVisibility(View.VISIBLE);
            } else {
                txtNoListsFound.setVisibility(View.GONE);
            }
            //Expand the last expanded position (or expand first availalbe non-empty list)
            if(lastExpandedPosition >=0) {
                expandTheListViewAtPosition(lastExpandedPosition);
            }
        }
    }

    /**
     * Assembles data for the colorblocks in a WordList, TweetList or Single User Saved Tweets list. Includes
     * the counts of words assigned to each color block as well as the color block widths
     * @param context context
     * @param listEntryOrUserInfo List entry object ({@link MyListEntry} for Word/Tweet list, {@link UserInfo} for SingleUser list)
     * @param isTweetList bool true if the listentry is a tweetlist or singleuser tweet list, false if it is a wordlist
     * @param basicWidthEstimateView inflated "expandablelistadapter_listitem" view to pass to {@link #getExpandableAdapterColorBlockBasicWidths(View, Context, String, float)}
     *             to help estimate the minimum width of a color block
     * @param metricDensityForBlockEstimate  density to help get accurate padding for colorblock width estimate in getExpandableAdapterColorBlockBasicWidths
     * @return {@link ColorBlockMeasurables} object with data for the colorblocks in the "Browse/Edit" row of
     * a WordList, TweetList or Single User Saved Tweets list.
     *
     * @see WordListFragment
     * @see TweetListFragment
     * @see TweetListSingleUserFragment
     */
    public static ColorBlockMeasurables prepareColorBlockDataForAList(Context context
            , Object listEntryOrUserInfo
            ,boolean isTweetList
            ,View basicWidthEstimateView
            ,float metricDensityForBlockEstimate) {
        ColorBlockMeasurables colorBlockMeasurables = new ColorBlockMeasurables();

        ColorThresholds colorThresholds = SharedPrefManager.getInstance(context).getColorThresholds();

        Cursor c;
        if(listEntryOrUserInfo instanceof MyListEntry && isTweetList) {
            //Pulling ColorBlockMeasurables for a TweetList
            MyListEntry myListEntry = (MyListEntry) listEntryOrUserInfo;
            c = InternalDB.getTweetInterfaceInstance(context).getTweetListColorBlocksCursor(colorThresholds,myListEntry);
        } else if(listEntryOrUserInfo instanceof MyListEntry) {
            //Pulling ColorBlockMeasurables for a WordList
            MyListEntry myListEntry = (MyListEntry) listEntryOrUserInfo;
            c = InternalDB.getWordInterfaceInstance(context).getWordListColorBlockCursor(colorThresholds,myListEntry);
        } else if(listEntryOrUserInfo instanceof  UserInfo) {
            UserInfo userInfo = (UserInfo) listEntryOrUserInfo;
            c = InternalDB.getTweetInterfaceInstance(context).getTweetListColorBlocksCursorForSingleUser(colorThresholds,userInfo.getUserId());
        } else {
            return colorBlockMeasurables;
        }

        if(c.getCount()>0) {
            c.moveToFirst();

            colorBlockMeasurables.setGreyCount(c.getInt(3));
            colorBlockMeasurables.setRedCount(c.getInt(4));
            colorBlockMeasurables.setYellowCount(c.getInt(5));
            colorBlockMeasurables.setGreenCount(c.getInt(6));
            colorBlockMeasurables.setEmptyCount(0);

            colorBlockMeasurables.setGreyMinWidth(getExpandableAdapterColorBlockBasicWidths(basicWidthEstimateView,context, String.valueOf(colorBlockMeasurables.getGreyCount()),metricDensityForBlockEstimate));
            colorBlockMeasurables.setRedMinWidth(getExpandableAdapterColorBlockBasicWidths(basicWidthEstimateView, context, String.valueOf(colorBlockMeasurables.getRedCount()),metricDensityForBlockEstimate));
            colorBlockMeasurables.setYellowMinWidth(getExpandableAdapterColorBlockBasicWidths(basicWidthEstimateView, context, String.valueOf(colorBlockMeasurables.getYellowCount()),metricDensityForBlockEstimate));
            colorBlockMeasurables.setGreenMinWidth(getExpandableAdapterColorBlockBasicWidths(basicWidthEstimateView, context, String.valueOf(colorBlockMeasurables.getGreenCount()),metricDensityForBlockEstimate));
            colorBlockMeasurables.setEmptyMinWidth(0);

        } else {
            Log.e("TEST-WordListFrag","STATIC prepareColorBlockMeasurables no results for measurable query");
        }
        c.close();
        return  colorBlockMeasurables;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (FragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("mMenuHeader", mMenuHeader);
    }
}


