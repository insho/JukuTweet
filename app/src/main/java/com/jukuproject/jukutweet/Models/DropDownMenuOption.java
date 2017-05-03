package com.jukuproject.jukutweet.Models;

/**
 * Container with information pretaining to a menu option in the {@link com.jukuproject.jukutweet.Dialogs.QuizMenuDialog}
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

    public int getButtonNumber() {
        return buttonNumber;
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
