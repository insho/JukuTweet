package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Container for information on the "Colorblocks" that appear next to Tweetlists in {@link com.jukuproject.jukutweet.Fragments.TweetListFragment},
 * WordLists in {@link com.jukuproject.jukutweet.Fragments.WordListFragment}, {@link com.jukuproject.jukutweet.Dialogs.QuizMenuDialog}, and in the {@link com.jukuproject.jukutweet.Fragments.StatsFragmentProgress}, etc.
 * Counts of the words associated with the list broken down by color. Widths of the color blocks.
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
    private Integer tweetCount;


    public ColorBlockMeasurables() {}


    /**
     * Sets up the selectedColorOptions list for the {@link com.jukuproject.jukutweet.Dialogs.QuizMenuDialog} colorblock dropdown
     */
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

    /**
     *
     * @return list of {@link DropDownMenuOption} for dropdown in {@link com.jukuproject.jukutweet.Dialogs.QuizMenuDialog}, with info
     * on the colors to include in the dropdown, their selection status, etc.
     */
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

    /**
     * Removes a color from the selected color list when the user de-selects the color in the {@link com.jukuproject.jukutweet.Dialogs.QuizMenuDialog} dropdown
     * @param colorToRemove name of color to remove
     * @return updated array of selectedColorOptions
     */
    public ArrayList<String> updateSelectedColorsRemove(String colorToRemove) {
        if(selectedColorOptions == null) {
            setInitialSelectedColors();
        }

        if(selectedColorOptions.contains(colorToRemove)) {
            selectedColorOptions.remove(colorToRemove);
        }

        return selectedColorOptions;
    }

    /**
     * Adds a color to the selected color list when the user selects the color in the {@link com.jukuproject.jukutweet.Dialogs.QuizMenuDialog} dropdown
     * @param colorToAdd name of color to add
     * @return updated array of selectedColorOptions
     */
    public ArrayList<String> updateSelectedColorsAdd(String colorToAdd) {
        if(selectedColorOptions == null) {
            setInitialSelectedColors();
        }
            if(!selectedColorOptions.contains(colorToAdd)) {
                selectedColorOptions.add(colorToAdd);

        }

        return selectedColorOptions;
    }


    public Integer getTweetCount() {
        return tweetCount;
    }

    public void setTweetCount(Integer tweetCount) {
        this.tweetCount = tweetCount;
    }

    /**
     * Creates the selectedColorOptions string for a list (concatenated list of colors to include in a quiz/flashcards)
     * @return updated selectedColorOptions
     */
    public String getSelectedColorString() {
        if(selectedColorOptions== null || selectedColorOptions.size() == 0) {
            return "'Grey','Red','Yellow','Green'";
        } else {
            StringBuilder sb = new StringBuilder();
            for(String color : selectedColorOptions) {
                if(sb.length()>0) {
                    sb.append(",");
                }
                sb.append("'");
                sb.append(color);
                sb.append("'");
            }
            return sb.toString();
        }

    }


    public int getEmptyCount() {
        return emptyCount;
    }

    public void setEmptyMinWidth(Integer emptyMinWidth) {
        this.emptyMinWidth = emptyMinWidth;
    }

    public void setEmptyCount(int emptyCount) {
        this.emptyCount = emptyCount;
    }

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

    public void setGreyMinWidth(Integer greyMinWidth) {
        this.greyMinWidth = greyMinWidth;
    }

    public void setRedMinWidth(Integer redMinWidth) {
        this.redMinWidth = redMinWidth;
    }

    public void setYellowMinWidth(Integer yellowMinWidth) {
        this.yellowMinWidth = yellowMinWidth;
    }

    public void setGreenMinWidth(Integer greenMinWidth) {
        this.greenMinWidth = greenMinWidth;
    }
    public int getTotalCount() {
        return (this.greyCount + this.redCount + this.yellowCount + this.greenCount);
    }


    /**
     *  Determines the width of the grey color block in the colorblock item.
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


    /**
     *  Determines the width of the red color block in the colorblock item.
     *  Size of color blocks are proportional to the ratio of the color's count/total count, but are also constrained by
     *  the minimum widths of the other remaining color blocks. There is an order of assigning colorblock widths. It goes:
     *  Empty, Grey, Red, Yellow, Green. Where green essentially takes whatever available space is left.
     *
     * @param dimenscore_total int total available space in the row
     * @param dimenscoreremaining int remaining space in the row
     * @return width of grey color block, proportional to the number of grey entries
     */
    public int getRedDimenscore(int dimenscore_total, int dimenscoreremaining) {

        int dimenscore = Math.round((dimenscore_total * ((float) this.redCount/ (float) this.getTotalCount())));

        if((dimenscoreremaining-dimenscore)<(yellowMinWidth + greenMinWidth + emptyMinWidth)){
            dimenscore =dimenscoreremaining-(yellowMinWidth + greenMinWidth + emptyMinWidth);
        } else if(dimenscore< redMinWidth) {
            dimenscore = redMinWidth;
        }
        return dimenscore;
    }


    /**
     *  Determines the width of the yellow color block in the colorblock item.
     *  Size of color blocks are proportional to the ratio of the color's count/total count, but are also constrained by
     *  the minimum widths of the other remaining color blocks. There is an order of assigning colorblock widths. It goes:
     *  Empty, Grey, Red, Yellow, Green. Where green essentially takes whatever available space is left.
     *
     * @param dimenscore_total int total available space in the row
     * @param dimenscoreremaining int remaining space in the row
     * @return width of grey color block, proportional to the number of grey entries
     */
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
        int dimenscore = Math.round((dimenscore_total * ((float) this.greenCount/ (float) this.getTotalCount())));

        if((dimenscoreremaining-dimenscore)<emptyMinWidth){
            dimenscore =dimenscoreremaining-emptyMinWidth;
        } else if(dimenscore< greenMinWidth) {
            dimenscore = greenMinWidth;
        }
        return dimenscore;
    }

    /**
     *  Determines the width of the empty color block in the colorblock item.
     *  Size of color blocks are proportional to the ratio of the color's count/total count, but are also constrained by
     *  the minimum widths of the other remaining color blocks. There is an order of assigning colorblock widths. It goes:
     *  Empty, Grey, Red, Yellow, Green. Where green essentially takes whatever available space is left.
     *
     * @param dimenscore int total available space in the row
     * @param dimenscoreremaining int remaining space in the row
     * @return width of grey color block, proportional to the number of grey entries
     */
    public int getEmptyDimenscore(int dimenscore, int dimenscoreremaining) {
        if((dimenscoreremaining- dimenscore )<0 ){
            dimenscore =dimenscoreremaining;
        } else if(dimenscore<emptyMinWidth) {
            dimenscore = emptyMinWidth;
        }


        return dimenscore;
    }


    // Parcelling part
    public ColorBlockMeasurables(Parcel in){

        this.greyCount = in.readInt();
        this.redCount = in.readInt();
        this.yellowCount = in.readInt();
        this.greenCount = in.readInt();
        this.emptyCount = in.readInt();
        this.greyMinWidth = (Integer) in.readValue(Integer.class.getClassLoader());
        this.redMinWidth = (Integer) in.readValue(Integer.class.getClassLoader());
        this.yellowMinWidth = (Integer) in.readValue(Integer.class.getClassLoader());
        this.greenMinWidth = (Integer) in.readValue(Integer.class.getClassLoader());
        this.emptyMinWidth = (Integer) in.readValue(Integer.class.getClassLoader());
        this.selectedColorOptions = in.createStringArrayList();
        this.tweetCount = (Integer)in.readSerializable();

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
        dest.writeValue(greyMinWidth);
        dest.writeValue(redMinWidth);
        dest.writeValue(yellowMinWidth);
        dest.writeValue(greenMinWidth);
        dest.writeValue(emptyMinWidth);
        dest.writeStringList(selectedColorOptions);
        dest.writeSerializable(tweetCount);
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
