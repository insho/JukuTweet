package com.jukuproject.jukutweet.Models;

/**
 * Created by JClassic on 4/1/2017.
 */

public class DropDownMenuOption {

    private String chosenOption;
    private int buttonNumber;

    public DropDownMenuOption(String chosenOption, int buttonNumber) {
        this.chosenOption = chosenOption;
        this.buttonNumber = buttonNumber;
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


}
