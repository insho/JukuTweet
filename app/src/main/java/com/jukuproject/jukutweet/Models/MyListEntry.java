package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by JClassic on 3/24/2017.
 */
//See mylistbrowsefragment

public class MyListEntry implements Parcelable {

    public MyListEntry(String listName, Integer listsSys) {
        this.listName = listName;
        this.listsSys = listsSys;
    }

    public MyListEntry(String listName, Integer listsSys, Integer selectionLevel) {
        this.listName = listName;
        this.listsSys = listsSys;
        this.selectionLevel = selectionLevel;
    }

    public String getListName() {
        return listName;
    }

    public Integer getListsSys() {
        return listsSys;
    }

    private String listName;
    private Integer listsSys;
    private Integer selectionLevel;

    /**
     * Selection level can be:
     * 0 -- unselected, no words in the BrowseBlocks window are contained in this list
     * 1 -- completely checked, meaning that ALL of selected words in the BrowseBlocks window are contained in this list
     * 2 -- greyed out, meaning that some of selected words in the BrowseBlocks window are contained in this list, but not all
     */
    public Integer getSelectionLevel() {
        return selectionLevel;
    }

    public void setSelectionLevel(Integer selectionLevel) {
        this.selectionLevel = selectionLevel;
    }



    // Parcelling part
    public MyListEntry(Parcel in){

        this.listName = in.readString();
        this.listsSys = in.readInt();
        this.selectionLevel = in.readInt();
    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.listName);
        dest.writeInt(this.listsSys);
        dest.writeInt(this.selectionLevel);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MyListEntry createFromParcel(Parcel in) {
            return new MyListEntry(in);
        }

        public MyListEntry[] newArray(int size) {
            return new MyListEntry[size];
        }
    };

}

