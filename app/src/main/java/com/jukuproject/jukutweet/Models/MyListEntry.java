package com.jukuproject.jukutweet.Models;

/**
 * Created by JClassic on 3/24/2017.
 */


public class MyListEntry {

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

}

