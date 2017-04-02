package com.jukuproject.jukutweet.Models;

/**
 * Created by JClassic on 4/1/2017.
 */

public class DropDownMenuOption {

    private String chosenOption;
    private int buttonNumber;

    private int colorCount;

    private boolean colorSelected;

    public DropDownMenuOption(String chosenOption, int buttonNumber) {
        this.chosenOption = chosenOption;
        this.buttonNumber = buttonNumber;
    }

    public DropDownMenuOption(String chosenOption, int colorCount, boolean selected) {
        this.chosenOption = chosenOption;
        this.colorCount = colorCount;
        this.colorSelected = selected;
        this.buttonNumber = 5;
    }

    public String getChosenOption() {
        return chosenOption;
    }

    public void setChosenOption(String chosenOption) {
        this.chosenOption = chosenOption;
    }

    public int getButtonNumber() {
        return buttonNumber;
    }

    public void setButtonNumber(int buttonNumber) {
        this.buttonNumber = buttonNumber;
    }

    public int getColorCount() {
        return colorCount;
    }


    public boolean isColorSelected() {
        return colorSelected;
    }

    public void setColorSelected(boolean colorSelected) {
        this.colorSelected = colorSelected;
    }


}
