package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.bignerdranch.expandablerecyclerview.model.Parent;
import com.jukuproject.jukutweet.Adapters.TweetListExpandableAdapter;
import com.jukuproject.jukutweet.Adapters.WordListExpandableAdapter;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Container with information for a row in a Word or Tweet list
 *
 * @see com.jukuproject.jukutweet.Fragments.TweetListFragment
 * @see TweetListExpandableAdapter
 * @see com.jukuproject.jukutweet.Fragments.WordListFragment
 * @see WordListExpandableAdapter
 */

public class MenuHeader implements  Parcelable,Parent<MenuChild> {
    private String headerTitle;
    private ColorBlockMeasurables colorBlockMeasurables;
    private ArrayList<String> childOptions;
    private List<MenuChild> menuChildren;
    private Boolean showLblHeaderCount;
    private Boolean myList;
    private Boolean systemList;
    private MyListEntry myListEntry;
    private Boolean isExpanded;
    private UserInfo userInfo;

    public MyListEntry getMyListEntry() {
        return myListEntry;
    }

    public void setMyListEntry(MyListEntry myListEntry) {
        this.myListEntry = myListEntry;
    }


    public Boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(Boolean expanded) {
        isExpanded = expanded;
    }

    public MenuHeader(){
        this.childOptions =  new ArrayList<>();
        this.menuChildren = new ArrayList<>();
        this.colorBlockMeasurables = new ColorBlockMeasurables();

        this.showLblHeaderCount = false;
        this.myList = false;
        this.systemList = false;
        this.isExpanded = false;
    }
    public MenuHeader(String headerTitle) {
        this.headerTitle = headerTitle;
        this.childOptions =  new ArrayList<>();
        this.colorBlockMeasurables = new ColorBlockMeasurables();
        this.menuChildren = new ArrayList<>();
        this.showLblHeaderCount = false;
        this.myList = false;
        this.systemList = false;
//        this.colorList = false;
        this.isExpanded = false;

    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setMenuChildren(ArrayList<String> childOptions,ColorBlockMeasurables colorBlockMeasurables,UserInfo userInfo) {
        if(menuChildren==null) {
            menuChildren = new ArrayList<>();
        } else {
            menuChildren.clear();
        }

        for(String childOption : childOptions) {
            MenuChild menuChild = new MenuChild(childOption);
            menuChild.setUserInfo(userInfo);

            if(childOption.equals("Saved Tweets") || childOption.equals("Browse/Edit")) {
                menuChild.setColorBlockMeasurables(colorBlockMeasurables);
            }
            menuChildren.add(menuChild);

         }
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public ColorBlockMeasurables getColorBlockMeasurables() {
        return colorBlockMeasurables;
    }

    public void setColorBlockMeasurables(ColorBlockMeasurables colorBlockMeasurables) {
        this.colorBlockMeasurables = colorBlockMeasurables;
    }

    public ArrayList<String> getChildOptions() {
        return childOptions;
    }

    public void setChildOptions(ArrayList<String> childOptions) {
        this.childOptions = childOptions;

    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    public int getSystemList() {
        if(systemList) {
            return 1;
        } else {
            return 0;
        }
    }

    public boolean isSystemList() {
        return systemList;
    }

    public void setSystemList(boolean systemList) {
        this.systemList = systemList;
    }

    public boolean isShowLblHeaderCount() {
        return showLblHeaderCount;
    }

    public void setMyList(boolean myList) {
        this.myList = myList;
    }

    public Boolean isMyList() {
        return myList;
    }

    public Integer getStarColor() {

        switch (headerTitle) {
            case "Blue":
                return R.color.colorJukuBlue;
            case "Red":
                return R.color.colorJukuRed;
            case "Green":
                return R.color.colorJukuGreen;
            case "Yellow":
                return R.color.colorJukuYellow;
            default:
                return android.R.color.black;
        }
    }

    @Override
    public List<MenuChild> getChildList() {
        return menuChildren;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }


    // Parcelling part
    public MenuHeader(Parcel in){

        this.headerTitle = in.readString();
        this.colorBlockMeasurables = in.readParcelable(ColorBlockMeasurables.class.getClassLoader());
        this.childOptions = in.createStringArrayList();
        this.showLblHeaderCount = in.readByte() != 0;
        this.myList = in.readByte() != 0;
        this.systemList = in.readByte() != 0;
        this.myListEntry = in.readParcelable(MyListEntry.class.getClassLoader());
        this.isExpanded = in.readByte() != 0;

        this.menuChildren = in.readArrayList(MenuChild.class.getClassLoader());
    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.headerTitle);
        if(this.colorBlockMeasurables==null) {
            this.colorBlockMeasurables = new ColorBlockMeasurables();
        }
        dest.writeParcelable(this.colorBlockMeasurables,flags);
        dest.writeStringList(this.childOptions);
        dest.writeByte((byte) (this.showLblHeaderCount ? 1 : 0));
        dest.writeByte((byte) (this.myList ? 1 : 0));
        dest.writeByte((byte) (this.systemList ? 1 : 0));
        dest.writeParcelable(this.myListEntry,flags);
        dest.writeByte((byte) (this.isExpanded ? 1 : 0));

        dest.writeList(this.menuChildren);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MenuHeader createFromParcel(Parcel in) {
            return new MenuHeader(in);
        }

        public MenuHeader[] newArray(int size) {
            return new MenuHeader[size];
        }
    };



}
