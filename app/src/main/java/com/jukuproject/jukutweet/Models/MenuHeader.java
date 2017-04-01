package com.jukuproject.jukutweet.Models;

import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Created by JClassic on 3/23/2017.
 */

public class MenuHeader {
    private String headerTitle;
    private ColorBlockMeasurables colorBlockMeasurables;
    private ArrayList<String> childOptions;
    private Boolean showLblHeaderCount;
    private Boolean myList;
    private Boolean systemList;

    public MyListEntry getMyListEntry() {
        return myListEntry;
    }

    public void setMyListEntry(MyListEntry myListEntry) {
        this.myListEntry = myListEntry;
    }

    private MyListEntry myListEntry;

    private Boolean colorList;
    private Boolean isExpanded;

    public Boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(Boolean expanded) {
        isExpanded = expanded;
    }

    public MenuHeader(){}
    public MenuHeader(String headerTitle) {
        this.headerTitle = headerTitle;
        this.childOptions =  new ArrayList<>();
        this.colorBlockMeasurables = new ColorBlockMeasurables();

        this.showLblHeaderCount = false;
        this.myList = false;
        this.systemList = false;
        this.colorList = false;
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
//
//    public boolean isShowColorBar() {
//        return showColorBar;
//    }
//
//    public void setShowColorBar(boolean showColorBar) {
//        this.showColorBar = showColorBar;
//    }
//
    public boolean isShowLblHeaderCount() {
        return showLblHeaderCount;
    }

    public void setShowLblHeaderCount(boolean showLblHeaderCount) {
        this.showLblHeaderCount = showLblHeaderCount;
    }

    public boolean isMyList() {
        return myList;
    }

    public void setMyList(boolean myList) {
        this.myList = myList;
    }





    public Boolean isColorList() {
        return colorList;
    }

    public void setColorList(Boolean colorList) {
        this.colorList = colorList;
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



}
