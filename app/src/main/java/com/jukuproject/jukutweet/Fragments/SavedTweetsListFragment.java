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
import com.jukuproject.jukutweet.Dialogs.QuizMenuDialog;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MenuHeader;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;

import java.util.ArrayList;
import java.util.Arrays;

//import com.jukuproject.jukutweet.BaseContainerFragment;

/**
 * Shows user-created lists of vocabulary
 */

public class SavedTweetsListFragment extends Fragment {

    String TAG = "SavedTweetsAll";
    FragmentInteractionListener mCallback;
    SavedTweetsExpandableAdapter SavedTweetsFragmentAdapter;
    ExpandableListView expListView;
    ArrayList<MenuHeader> mMenuHeader;
    private int lastExpandedPosition = -1;
    private SharedPrefManager sharedPrefManager;
    private UserInfo mUserInfo;

    public static SavedTweetsListFragment newInstance() {
        SavedTweetsListFragment fragment = new SavedTweetsListFragment();
//        Bundle args = new Bundle();
//        args.putParcelable("userInfo", null);
//        fragment.setArguments(args);
        return fragment;
    }

    public static SavedTweetsListFragment newInstance(UserInfo userInfo) {
        SavedTweetsListFragment fragment = new SavedTweetsListFragment();
        Bundle args = new Bundle();
        args.putParcelable("userInfo", userInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_mylists, container, false);
        expListView = (ExpandableListView) v.findViewById(R.id.lvMyListCategory);


        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sharedPrefManager = SharedPrefManager.getInstance(getContext());
        expListView.setClickable(true);

        if(savedInstanceState == null) {
            mUserInfo = null;
            if (getArguments() != null && ((mUserInfo = getArguments().getParcelable("userInfo")) != null)) {
                // do something with task
                prepareListData(mUserInfo);

            } else {
                prepareListData(null);
                mCallback.showProgressBar(false);
            }
//            try {
//                if(getArguments() != null && getArguments().hasp)
//                mUserInfo = getArguments().getParcelable("userInfo");
//            } catch (NullPointerException e) {
//                mUserInfo = null;
//            }
        } else {
            mMenuHeader = savedInstanceState.getParcelableArrayList("mMenuHeader");

            lastExpandedPosition = savedInstanceState.getInt("lastExpandedPosition");

            if ((mUserInfo = savedInstanceState.getParcelable("mUserInfo")) != null) {
                // do something with task
                prepareListData(mUserInfo);
            } else {
                prepareListData(null);
            }

        }

        if(mUserInfo != null) {
            prepareListData(mUserInfo);
        } else {
            prepareListData(null);
        }



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
                        if(mUserInfo != null) {
                            SavedTweetsBrowseFragment fragment = SavedTweetsBrowseFragment.newInstance(mUserInfo);
                            ((BaseContainerFragment)getParentFragment()).replaceFragment(fragment, true,"savedtweetsbrowse");
                        } else {

                            SavedTweetsBrowseFragment fragment = SavedTweetsBrowseFragment.newInstance(new MyListEntry(mMenuHeader.get(groupPosition).getHeaderTitle()
                                                                                                                        ,mMenuHeader.get(groupPosition).getSystemList()));
                            ((BaseContainerFragment)getParentFragment()).replaceFragment(fragment, true,"savedtweetsbrowse");
                        }
                        break;

                    case "Flash Cards":
                        if(getFragmentManager().findFragmentByTag("quizmenu") == null || !getFragmentManager().findFragmentByTag("quizmenu").isAdded()) {
                            QuizMenuDialog.newInstance("flashcards"
                                    ,1
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
                                    ,1
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
                                    ,1
                                    ,lastExpandedPosition
                                    ,mMenuHeader.get(groupPosition).getMyListEntry()
                                    ,mMenuHeader.get(groupPosition).getColorBlockMeasurables()
                                    ,getdimenscore()).show(getActivity().getSupportFragmentManager(),"dialogQuizMenu");
//                            mCallback.showFab(false,"");
                        }

                        break;
                    case "Stats":
//                        MyListEntry myListEntry = new MyListEntry(mMenuHeader.get(groupPosition).getHeaderTitle(),mMenuHeader.get(groupPosition).getSystemList());
//                        ColorBlockMeasurables colorBlockMeasurables = prepareColorBlockDataForList(myListEntry);
//
//
//                        StatsFragmentProgress statsFragmentProgress = StatsFragmentProgress.newInstance(myListEntry
//                                , 10
//                                ,colorBlockMeasurables);
//                        ((BaseContainerFragment)getParentFragment()).replaceFragment(statsFragmentProgress, true,"mylistbrowse");
//                        mCallback.showFab(false,"");
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
//                        MenuOptionsDialog b = new MenuOptionsDialog(getActivity(), 0, groupPosition, "fragment_fillintheblanks", true, Header, sys, mylistposition);
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
                mCallback.showEditMyListDialog("TweetList",mMenuHeader.get(position).getHeaderTitle(),mMenuHeader.get(position).isSystemList());

                return false;
            }
        });



        /* deciding which mylist to open on activity start */
        expListView.setGroupIndicator(null);

        //Expand the last expanded position (or expand first availalbe non-empty list)
        if(lastExpandedPosition >=0) {
            expandTheListViewAtPosition(lastExpandedPosition);
        }

        setRetainInstance(true);
    }

    public void prepareListData(@Nullable UserInfo userInfo) {
        mMenuHeader = new ArrayList<>();

        ArrayList<String> availableFavoritesStars = sharedPrefManager.getActiveTweetFavoriteStars();
        ColorThresholds colorThresholds = sharedPrefManager.getColorThresholds();
        ArrayList<String> childOptions = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.menu_mylist)));

        Cursor c;
        if(userInfo != null) {
            c = InternalDB.getTweetInterfaceInstance(getContext()).getTweetListColorBlocksCursorForSingleUser(colorThresholds,userInfo.getUserId());
        } else {

            c = InternalDB.getTweetInterfaceInstance(getContext()).getTweetListColorBlocksCursor(colorThresholds,null);
        }
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
                    MenuHeader menuHeader = new MenuHeader();
                    if(userInfo!=null) {
                        menuHeader.setHeaderTitle(userInfo.getDisplayScreenName());
                    } else {
                        menuHeader.setHeaderTitle(c.getString(0));
                    }
//                    MenuHeader menuHeader = new MenuHeader(c.getString(0));
                    menuHeader.setChildOptions(childOptions);
                    menuHeader.setMyList(false);

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
                    colorBlockMeasurables.setTweetCount(c.getInt(7));

                    /* Set the first available non-empty list to be automatically expanded when
                      fragment is created. This is achieved by making it the "lastexpandedposition" from the get-go */
                    if(lastExpandedPosition<0 && c.getInt(2)>0) {
                        lastExpandedPosition = mMenuHeader.size();
                    }


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
//        return Math.round((float)metrics.widthPixels*(float).5);
        if(metrics.heightPixels>metrics.widthPixels) {
            return Math.round((float)metrics.widthPixels*(float).4);
        } else {
            return Math.round((float)metrics.heightPixels*(float).4);
        }
    }

    public void expandTheListViewAtPosition(int position) {
        expListView.expandGroup(position);
        expListView.setSelectedGroup(position);
        mMenuHeader.get(position).setExpanded(true);
        lastExpandedPosition = position;
    };


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
        if(mUserInfo!= null && mUserInfo.getScreenName() != null) {
            prepareListData(mUserInfo);
        } else {
            prepareListData(null);
        }
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


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("lastExpandedPosition", lastExpandedPosition);
        outState.putParcelable("mUserInfo", mUserInfo);
        outState.putParcelableArrayList("mMenuHeader", mMenuHeader);
    }

}


