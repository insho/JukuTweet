package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by JClassic on 3/23/2017.
 */

public class ColorBlockMeasurables implements Parcelable {

    private int greyCount;
    private int redCount;
    private int yellowCount;
    private int greenCount;
    private int emptyCount;
    private Integer greyMinWidth;
    private Integer redMinWidth;
    private Integer yellowMinWidth;
    private Integer greenMinWidth;
    private Integer emptyMinWidth;
    private ArrayList<String> selectedColorOptions;



    public void setInitialSelectedColors() {
        selectedColorOptions = new ArrayList<>();
        if(greyCount>0) {
            selectedColorOptions.add("Grey");
        }
        if(redCount>0) {
            selectedColorOptions.add("Red");
        }
        if(yellowCount>0) {
            selectedColorOptions.add("Yellow");
        }
        if(greenCount>0) {
            selectedColorOptions.add("Green");
        }
    }

    public ArrayList<DropDownMenuOption> getDropDownOptions() {
        ArrayList<DropDownMenuOption> possibleDropDownOptions = new ArrayList<>();
        if(greyCount>0) {
            possibleDropDownOptions.add(new DropDownMenuOption("Grey",greyCount,true));
        }
        if(redCount>0) {
            possibleDropDownOptions.add(new DropDownMenuOption("Red",redCount,true));
        }
        if(yellowCount>0) {
            possibleDropDownOptions.add(new DropDownMenuOption("Yellow",yellowCount,true));
        }
        if(greenCount>0) {
            possibleDropDownOptions.add(new DropDownMenuOption("Green",greenCount,true));
        }

        return possibleDropDownOptions;
    }

//    public void setSelected(DropDownMenuOption option) {
//        switch (option.getChosenOption()) {
//            case "Grey":
//                updateSelectedColorsRemove()
//                break;
//            case "Red":
//                sb.append("2");
//                break;
//            case "Yellow":
//                sb.append("3");
//                break;
//            case "Green":
//                sb.append("4");
//                break;
//        }
//    }
    public ArrayList<String> updateSelectedColorsRemove(String colorToRemove) {
        if(selectedColorOptions == null) {
            setInitialSelectedColors();
        }

        if(selectedColorOptions.contains(colorToRemove)) {
            selectedColorOptions.remove(colorToRemove);
        }

        return selectedColorOptions;
    }
    public ArrayList<String> updateSelectedColorsAdd(String colorToAdd) {
        if(selectedColorOptions == null) {
            setInitialSelectedColors();
        }
            if(!selectedColorOptions.contains(colorToAdd)) {
                selectedColorOptions.add(colorToAdd);

        }

        return selectedColorOptions;
    }




    public String getSelectedColorString() {
        if(selectedColorOptions== null || selectedColorOptions.size() == 0) {
            return "1,2,3,4";
        } else {
            StringBuilder sb = new StringBuilder();
            for(String color : selectedColorOptions) {
                if(sb.length()>0) {
                    sb.append(",");
                }
                switch (color) {
                    case "Grey":
                        sb.append("1");
                        break;
                    case "Red":
                        sb.append("2");
                        break;
                    case "Yellow":
                        sb.append("3");
                        break;
                    case "Green":
                        sb.append("4");
                        break;
                }

            }
            return sb.toString();
        }

    }


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
//     * Determines the width of the grey color block in the header row of
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



    // Parcelling part
    public ColorBlockMeasurables(Parcel in){

        this.greyCount = in.readInt();
        this.redCount = in.readInt();
        this.yellowCount = in.readInt();
        this.greenCount = in.readInt();
        this.emptyCount = in.readInt();
        this.greyMinWidth = in.readInt();
        this.redMinWidth = in.readInt();
        this.yellowMinWidth = in.readInt();
        this.greenMinWidth = in.readInt();
        this.emptyMinWidth = in.readInt();
//        this.selectedColors = in.readArrayList(Class.String);


    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(greyCount);
        dest.writeInt(redCount);
        dest.writeInt(yellowCount);
        dest.writeInt(greenCount);
        dest.writeInt(emptyCount);
        dest.writeInt(greyMinWidth);
        dest.writeInt(redMinWidth);
        dest.writeInt(yellowMinWidth);
        dest.writeInt(greenMinWidth);
        dest.writeInt(emptyMinWidth);
//        dest.writeStringArray();

    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ColorBlockMeasurables createFromParcel(Parcel in) {
            return new ColorBlockMeasurables(in);
        }

        public ColorBlockMeasurables[] newArray(int size) {
            return new ColorBlockMeasurables[size];
        }
    };
}
