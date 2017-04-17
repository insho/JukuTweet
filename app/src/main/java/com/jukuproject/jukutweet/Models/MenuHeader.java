package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Created by JClassic on 3/23/2017.
 */

public class MenuHeader implements  Parcelable {
    private String headerTitle;
    private ColorBlockMeasurables colorBlockMeasurables;
    private ArrayList<String> childOptions;
    private Boolean showLblHeaderCount;
    private Boolean myList;
    private Boolean systemList;
    private MyListEntry myListEntry;
    private Boolean isExpanded;

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

        this.showLblHeaderCount = false;
        this.myList = false;
        this.systemList = false;
//        this.colorList = false;
        this.isExpanded = false;

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

    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.headerTitle);
        dest.writeParcelable(this.colorBlockMeasurables,flags);
        dest.writeStringList(this.childOptions);
        dest.writeByte((byte) (this.showLblHeaderCount ? 1 : 0));
        dest.writeByte((byte) (this.myList ? 1 : 0));
        dest.writeByte((byte) (this.systemList ? 1 : 0));
        dest.writeParcelable(this.myListEntry,flags);
        dest.writeByte((byte) (this.isExpanded ? 1 : 0));
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
