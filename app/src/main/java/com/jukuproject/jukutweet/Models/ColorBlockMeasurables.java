package com.jukuproject.jukutweet.Models;

/**
 * Created by JClassic on 3/23/2017.
 */

public class ColorBlockMeasurables {

    private int greyCount;
    private int redCount;
    private int yellowCount;
    private int greenCount;

    public int getEmptyCount() {
        return emptyCount;
    }

    public Integer getEmptyMinWidth() {
        return emptyMinWidth;
    }

    public void setEmptyMinWidth(Integer emptyMinWidth) {
        this.emptyMinWidth = emptyMinWidth;
    }

    public void setEmptyCount(int emptyCount) {

        this.emptyCount = emptyCount;
    }

    private int emptyCount;

    private Integer greyMinWidth;
    private Integer redMinWidth;
    private Integer yellowMinWidth;
    private Integer greenMinWidth;
    private Integer emptyMinWidth;

    public ColorBlockMeasurables() {}
//
//    public ColorBlockMeasurables(Integer greyCount, Integer redCount, Integer yellowCount, Integer greenCount) {
//        this.greyCount = greyCount;
//        this.redCount = redCount;
//        this.yellowCount = yellowCount;
//        this.greenCount = greenCount;
//
//        this.greyMinWidth = 0;
//        this.redMinWidth = 0;
//        this.yellowMinWidth = 0;
//        this.greenMinWidth = 0;
//    }

    public int getGreyCount() {
        return greyCount;
    }

    public void setGreyCount(Integer greyCount) {
        this.greyCount = greyCount;
    }

    public int getRedCount() {
        return redCount;
    }

    public void setRedCount(Integer redCount) {
        this.redCount = redCount;
    }

    public int getYellowCount() {
        return yellowCount;
    }

    public void setYellowCount(Integer yellowCount) {
        this.yellowCount = yellowCount;
    }

    public int getGreenCount() {
        return greenCount;
    }

    public void setGreenCount(Integer greenCount) {
        this.greenCount = greenCount;
    }

    public Integer getGreyMinWidth() {
        return greyMinWidth;
    }

    public void setGreyMinWidth(Integer greyMinWidth) {
        this.greyMinWidth = greyMinWidth;
    }

    public Integer getRedMinWidth() {
        return redMinWidth;
    }

    public void setRedMinWidth(Integer redMinWidth) {
        this.redMinWidth = redMinWidth;
    }

    public Integer getYellowMinWidth() {
        return yellowMinWidth;
    }

    public void setYellowMinWidth(Integer yellowMinWidth) {
        this.yellowMinWidth = yellowMinWidth;
    }

    public Integer getGreenMinWidth() {
        return greenMinWidth;
    }

    public void setGreenMinWidth(Integer greenMinWidth) {
        this.greenMinWidth = greenMinWidth;
    }


    public int getTotalCount() {
        return (this.greyCount + this.redCount + this.yellowCount + this.greenCount);
    }


    /**
     * Determines the width of the grey color block in the header row of {@link com.jukuproject.jukutweet.Adapters.MenuExpandableListAdapter}
     *  Size of color blocks are proportional to the ratio of the color's count/total count, but are also constrained by
     *  the minimum widths of the other remaining color blocks. There is an order of assigning colorblock widths. It goes:
     *  Empty, Grey, Red, Yellow, Green. Where green essentially takes whatever available space is left.
     *
     * @param dimenscore_total int total available space in the row
     * @return width of grey color block, proportional to the number of grey entries
     */


    public int getGreyDimenscore(int dimenscore_total) {
        int dimenscore = Math.round((dimenscore_total * ((float) this.greyCount/ (float) this.getTotalCount())));

        if((dimenscore_total-dimenscore)<(redMinWidth + yellowMinWidth + greenMinWidth + emptyMinWidth)){
            dimenscore =dimenscore_total-(redMinWidth + yellowMinWidth + greenMinWidth + emptyMinWidth);
        } else if(dimenscore< greyMinWidth) {
            dimenscore = greyMinWidth;
        }
        return dimenscore;
    }
    public int getRedDimenscore(int dimenscore_total, int dimenscoreremaining) {

        int dimenscore = Math.round((dimenscore_total * ((float) this.redCount/ (float) this.getTotalCount())));

        if((dimenscoreremaining-dimenscore)<(yellowMinWidth + greenMinWidth + emptyMinWidth)){
            dimenscore =dimenscoreremaining-(yellowMinWidth + greenMinWidth + emptyMinWidth);
        } else if(dimenscore< redMinWidth) {
            dimenscore = redMinWidth;
        }
        return dimenscore;

    }
    public int getYellowDimenscore(int dimenscore_total, int dimenscoreremaining) {
        int dimenscore = Math.round((dimenscore_total * ((float) this.yellowCount/ (float) this.getTotalCount())));

        if((dimenscoreremaining-dimenscore)<(greenMinWidth + emptyMinWidth)){
            dimenscore =dimenscoreremaining-(greenMinWidth + emptyMinWidth);
        } else if(dimenscore< yellowMinWidth) {
            dimenscore = yellowMinWidth;
        }
        return dimenscore;
    }

    public int getGreenDimenscore(int dimenscore_total, int dimenscoreremaining) {
        int dimenscore = Math.round((dimenscore_total * ((float) this.greyCount/ (float) this.getTotalCount())));

        if((dimenscoreremaining-dimenscore)<emptyMinWidth){
            dimenscore =dimenscoreremaining-emptyMinWidth;
        } else if(dimenscore< greenMinWidth) {
            dimenscore = greenMinWidth;
        }
        return dimenscore;
    }

    public int getEmptyDimenscore(int dimenscore, int dimenscoreremaining) {
        if((dimenscoreremaining- dimenscore )<0 ){
            dimenscore =dimenscoreremaining;
        } else if(dimenscore<emptyMinWidth) {
            dimenscore = emptyMinWidth;
        }


        return dimenscore;
    }

//    /**
//     * Determines the width of the grey color block in the header row of {@link com.jukuproject.jukutweet.Adapters.MenuExpandableListAdapter}
//     *  Size of color blocks are proportional to the ratio of the color's count/total count, but are also constrained by
//     *  the minimum widths of the other remaining color blocks. There is an order of assigning colorblock widths. It goes:
//     *  Grey, Red, Yellow, and finally Green. Where green essentially takes whatever available space is left.
//     *
//     * @param dimenscore_total int total available space in the row
//     * @return width of grey color block, proportional to the number of grey entries
//     */
//    public int getGreyDimenscore(int dimenscore_total) {
//        int dimenscore = Math.round((dimenscore_total * ((float) this.greyCount/ (float) this.getTotalCount())));
//
//        if((dimenscore_total-dimenscore)<(redMinWidth + yellowMinWidth + greenMinWidth)){
//            dimenscore =dimenscore_total-(redMinWidth + yellowMinWidth + greenMinWidth);
//        } else if(dimenscore< greyMinWidth) {
//            dimenscore = greyMinWidth;
//        }
//        return dimenscore;
//    }
//    public int getRedDimenscore(int dimenscore_total, int dimenscoreremaining) {
//
//        int dimenscore = Math.round((dimenscore_total * ((float) this.redCount/ (float) this.getTotalCount())));
//
//        if((dimenscoreremaining-dimenscore)<(yellowMinWidth + greenMinWidth)){
//            dimenscore =dimenscoreremaining-(yellowMinWidth + greenMinWidth);
//        } else if(dimenscore< redMinWidth) {
//            dimenscore = redMinWidth;
//        }
//        return dimenscore;
//
//    }
//    public int getYellowDimenscore(int dimenscore_total, int dimenscoreremaining) {
//        int dimenscore = Math.round((dimenscore_total * ((float) this.yellowCount/ (float) this.getTotalCount())));
//
//        if((dimenscoreremaining-dimenscore)<greenMinWidth){
//            dimenscore =dimenscoreremaining-greenMinWidth;
//        } else if(dimenscore< yellowMinWidth) {
//            dimenscore = yellowMinWidth;
//        }
//        return dimenscore;
//    }
//    public int getGreenDimenscore(int dimenscore, int dimenscoreremaining) {
//        if((dimenscoreremaining- dimenscore )<0 ){
//            dimenscore =dimenscoreremaining;
//        } else if(dimenscore<greenMinWidth) {
//            dimenscore = greenMinWidth;
//        }
//
//
//        return dimenscore;
//    }
}
