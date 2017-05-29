package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Adapters.TweetListExpandableAdapter;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.QuizMenuDialog;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MenuHeader;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;
import com.jukuproject.jukutweet.TabContainers.BaseContainerFragment;

import java.util.ArrayList;
import java.util.Arrays;

import static com.jukuproject.jukutweet.Fragments.WordListFragment.getExpandableAdapterColorBlockBasicWidths;
import static com.jukuproject.jukutweet.Fragments.WordListFragment.prepareColorBlockDataForAList;


/**
 * Shows lists of saved tweets (both user-created lists and system default lists). Click
 * on a list to show sub-options ("Browse","Multiple Choice", "Stats" etc)
 */
public class TweetListFragment extends Fragment {

    private String TAG = "SavedTweetsAll";
    private FragmentInteractionListener mCallback;
    private TweetListExpandableAdapter SavedTweetsFragmentAdapter;
    private ExpandableListView expListView;
    private ArrayList<MenuHeader> mMenuHeader;
    private int lastExpandedPosition = -1;
    private SharedPrefManager sharedPrefManager;
    private TextView txtNoListsFound;
    private LinearLayout layoutMain;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_wordandtweet_lists, container, false);
        expListView = (ExpandableListView) v.findViewById(R.id.lvMyListCategory);
        txtNoListsFound = (TextView) v.findViewById(R.id.nolistsfound);
        layoutMain = (LinearLayout) v.findViewById(R.id.linearLayoutMenuMyLists);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sharedPrefManager = SharedPrefManager.getInstance(getContext());
        expListView.setClickable(true);

        if(savedInstanceState == null) {
                prepareListData();
                mCallback.showProgressBar(false);
        } else {
            mMenuHeader = savedInstanceState.getParcelableArrayList("mMenuHeader");
            lastExpandedPosition = savedInstanceState.getInt("lastExpandedPosition");
        }

        SavedTweetsFragmentAdapter = new TweetListExpandableAdapter(getContext(),mMenuHeader,getdimenscore(.36f),0);

        expListView.setAdapter(SavedTweetsFragmentAdapter);
        if(mMenuHeader.size()==0) {
            txtNoListsFound.setVisibility(View.VISIBLE);
        } else {
            txtNoListsFound.setVisibility(View.GONE);
        }

        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                ColorBlockMeasurables headerColorBlockMeasurables = mMenuHeader.get(groupPosition).getColorBlockMeasurables();
                //If the list being clicked on is empty, show (or hide) the "(empty)" header label
                if(headerColorBlockMeasurables.getTotalCount() == 0
                        && headerColorBlockMeasurables.getTweetCount()==0) {
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

                /* User shouldn't be able to click on an empty list, but should be able to open a list with
                 * saved tweets but no parsed vocab so that they can browse the list and parse the vocab in TweetBreakDown Fragment */
                ColorBlockMeasurables colorBlockMeasurables = mMenuHeader.get(groupPosition).getColorBlockMeasurables();
                if(colorBlockMeasurables == null
                        || (colorBlockMeasurables.getTotalCount()==0 && colorBlockMeasurables.getTweetCount()==0)) {
                    Toast.makeText(getContext(), "List is empty", Toast.LENGTH_SHORT).show();
                    return false;
                } else if(!childOption.equals("Browse/Edit")
                        && colorBlockMeasurables.getTotalCount()==0 && colorBlockMeasurables.getTweetCount()>0) {
                    Snackbar snackbar = Snackbar
                            .make(layoutMain, "Tweets are saved, but vocab has not been extracted from them. Go to Browse/Edit " +
                                    "and click on a tweet to create the vocab for it. ", Snackbar.LENGTH_LONG);

                    snackbar.show();
                } else {
                    switch (childOption) {
                        case "Browse/Edit":
                            TweetListBrowseFragment fragment = TweetListBrowseFragment.newInstance(new MyListEntry(mMenuHeader.get(groupPosition).getHeaderTitle()                                     ,mMenuHeader.get(groupPosition).getSystemList()));
                            ((BaseContainerFragment)getParentFragment()).replaceFragment(fragment, true,"savedtweetsbrowse");
                            mCallback.showFab(false,"");
                            mCallback.showActionBarBackButton(true,mMenuHeader.get(groupPosition).getHeaderTitle(),1);
                            break;

                        case "Flash Cards":
                            if(getFragmentManager().findFragmentByTag("quizmenu") == null || !getFragmentManager().findFragmentByTag("quizmenu").isAdded()) {
                                QuizMenuDialog.newInstance("flashcards"
                                        ,1
                                        ,lastExpandedPosition
                                        ,mMenuHeader.get(groupPosition).getMyListEntry()
                                        ,mMenuHeader.get(groupPosition).getColorBlockMeasurables()
                                        ,getdimenscore(.5f)).show(getActivity().getSupportFragmentManager()
                                        ,"dialogQuizMenu");

                                mCallback.showActionBarBackButton(true,mMenuHeader.get(groupPosition).getHeaderTitle(),1);
                                mCallback.showFab(false,"");
                            }

                            break;

                        case "Multiple Choice":
                            if(getFragmentManager().findFragmentByTag("quizmenu") == null || !getFragmentManager().findFragmentByTag("quizmenu").isAdded()) {
                                QuizMenuDialog.newInstance("multiplechoice"
                                        ,1
                                        ,lastExpandedPosition
                                        ,mMenuHeader.get(groupPosition).getMyListEntry()
                                        ,mMenuHeader.get(groupPosition).getColorBlockMeasurables()
                                        ,getdimenscore(.5f)).show(getActivity().getSupportFragmentManager(),"dialogQuizMenu");
                            }


                            break;

                        case "Fill in the Blanks":
                            if(getFragmentManager().findFragmentByTag("quizmenu") == null || !getFragmentManager().findFragmentByTag("quizmenu").isAdded()) {
                                QuizMenuDialog.newInstance("fillintheblanks"
                                        ,1
                                        ,lastExpandedPosition
                                        ,mMenuHeader.get(groupPosition).getMyListEntry()
                                        ,mMenuHeader.get(groupPosition).getColorBlockMeasurables()
                                        ,getdimenscore(.5f)).show(getActivity().getSupportFragmentManager(),"dialogQuizMenu");
                            }

                            break;
                        case "Stats":
                            MyListEntry myListEntry = new MyListEntry(mMenuHeader.get(groupPosition).getHeaderTitle(),mMenuHeader.get(groupPosition).getSystemList());

                            View colorBlockMinWidthEstimateView = getActivity().getLayoutInflater().inflate(R.layout.expandablelistadapter_listitem, null);
                            DisplayMetrics metrics = new DisplayMetrics();
                            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                            float metricsDensity = metrics.density;
                            ColorBlockMeasurables colorBlockMeasurablesForThisList = prepareColorBlockDataForAList(getContext(),myListEntry,true,colorBlockMinWidthEstimateView,metricsDensity);

                            StatsFragmentProgress statsFragmentProgress = StatsFragmentProgress.newTweetsInstance(myListEntry
                                    , 10
                                    ,colorBlockMeasurablesForThisList);
                            ((BaseContainerFragment)getParentFragment()).replaceFragment(statsFragmentProgress, true,"tweetlistStats");
                            mCallback.showFab(false,"");
                            mCallback.showActionBarBackButton(true,mMenuHeader.get(groupPosition).getHeaderTitle(),1);
                            break;


                        default:

                            break;

                    }
                }



                return false;
            }
        });

        expListView.setOnItemLongClickListener(new ExpandableListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    mCallback.showEditMyListDialog("TweetList",mMenuHeader.get(groupPosition).getHeaderTitle(),mMenuHeader.get(groupPosition).isSystemList());
                }
                return false;
            }
        });



        /* deciding which mylist to open on activity start */
        expListView.setGroupIndicator(null);

        //Expand the last expanded position (or expand first availalbe non-empty list)
        if(lastExpandedPosition >=0) {
            expandTheListViewAtPosition(lastExpandedPosition);
        }
    }

    /**
     * Prepares header entries for the mMenuHeader dataset, with {@link ColorBlockMeasurables} representing the saved tweet words
     * broken down by word score color categories (colorblocks are displayed next to the "Browse/Edit" entry for an expanded saved tweet list)
     */
    public void prepareListData() {
        mMenuHeader = new ArrayList<>();

        if(sharedPrefManager==null) {
            sharedPrefManager = SharedPrefManager.getInstance(getContext());
        }
        ArrayList<String> availableFavoritesStars = sharedPrefManager.getActiveTweetFavoriteStars();
        ColorThresholds colorThresholds = sharedPrefManager.getColorThresholds();
        ArrayList<String> childOptions = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.menu_mylist)));

        //pull pieces necessary to getExpandableAdapterColorBlockBasicWidths
        View colorBlockMinWidthEstimateView = getActivity().getLayoutInflater().inflate(R.layout.expandablelistadapter_listitem, null);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float metricsDensity = metrics.density;
        Cursor c = InternalDB.getTweetInterfaceInstance(getContext()).getTweetListColorBlocksCursor(colorThresholds,null);

        if(c.getCount()>0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {

                /* We do not want to include favorites star lists that are not active in the user
                * preferences. So if an inactivated list shows up in the sql query, ignore it (don't add to mMenuHeader)*/
                if(c.getInt(1) != 1 || (availableFavoritesStars.contains(c.getString(0)))) {
                    MenuHeader menuHeader = new MenuHeader();
                        menuHeader.setHeaderTitle(c.getString(0));
                    menuHeader.setChildOptions(childOptions);
                    menuHeader.setMyList(false);
                    menuHeader.setMyListEntry(new MyListEntry(c.getString(0),c.getInt(1)));
                    if(c.getInt(1) == 1 ) {
                        menuHeader.setSystemList(true);
                    }

                    ColorBlockMeasurables colorBlockMeasurables = new ColorBlockMeasurables();
                    colorBlockMeasurables.setGreyCount(c.getInt(3));
                    colorBlockMeasurables.setRedCount(c.getInt(4));
                    colorBlockMeasurables.setYellowCount(c.getInt(5));
                    colorBlockMeasurables.setGreenCount(c.getInt(6));
                    colorBlockMeasurables.setEmptyCount(0);
                    colorBlockMeasurables.setTweetCount(c.getInt(7));

                    /* Set the first available non-empty list to be automatically expanded when
                      fragment is created. This is achieved by making it the "lastexpandedposition" from the get-go */
                    if(lastExpandedPosition<0 && c.getInt(2)>0) {
                        lastExpandedPosition = mMenuHeader.size();
                    }

                    colorBlockMeasurables.setGreyMinWidth(getExpandableAdapterColorBlockBasicWidths(colorBlockMinWidthEstimateView,getContext(), String.valueOf(colorBlockMeasurables.getGreyCount()),metricsDensity));
                    colorBlockMeasurables.setRedMinWidth(getExpandableAdapterColorBlockBasicWidths(colorBlockMinWidthEstimateView,getContext(), String.valueOf(colorBlockMeasurables.getRedCount()),metricsDensity));
                    colorBlockMeasurables.setYellowMinWidth(getExpandableAdapterColorBlockBasicWidths(colorBlockMinWidthEstimateView,getContext(), String.valueOf(colorBlockMeasurables.getYellowCount()),metricsDensity));
                    colorBlockMeasurables.setGreenMinWidth(getExpandableAdapterColorBlockBasicWidths(colorBlockMinWidthEstimateView,getContext(), String.valueOf(colorBlockMeasurables.getGreenCount()),metricsDensity));
                    colorBlockMeasurables.setEmptyMinWidth(getExpandableAdapterColorBlockBasicWidths(colorBlockMinWidthEstimateView,getContext(), String.valueOf(colorBlockMeasurables.getEmptyCount()),metricsDensity));

                    menuHeader.setColorBlockMeasurables(colorBlockMeasurables);
                    mMenuHeader.add(menuHeader);
                }

                c.moveToNext();
            }
        }
        c.close();
    }

    /**
     * Maximum width of the color bars in the MenuExpandableListAdapter. Right that vlue is set
     * to half of the screenwidth
     * @return maximum width in pixels of colored bars
     *
     * @see TweetListExpandableAdapter
     */
    private int getdimenscore(float multiplier) {

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if(metrics.heightPixels>metrics.widthPixels) {
            return Math.round((float)metrics.widthPixels*multiplier);
        } else {
            return Math.round((float)metrics.heightPixels*multiplier);
        }
    }

    public void expandTheListViewAtPosition(int position) {

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


    public void updateMyListAdapter() {
        prepareListData();
        SavedTweetsFragmentAdapter = new TweetListExpandableAdapter(getContext(),mMenuHeader,getdimenscore(.36f),0);
        expListView.setAdapter(SavedTweetsFragmentAdapter);
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


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("lastExpandedPosition", lastExpandedPosition);
        outState.putParcelableArrayList("mMenuHeader", mMenuHeader);
    }

}



