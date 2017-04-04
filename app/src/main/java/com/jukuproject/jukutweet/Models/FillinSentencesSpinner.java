package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by JClassic on 4/4/2017.
 */

public class FillinSentencesSpinner implements Parcelable {
    private ArrayList<String> options;
    private String selectedOption;
    private boolean isCorrect;
    private boolean hasBeenAnswered;

    public FillinSentencesSpinner() {
        this.isCorrect = false;
        this.hasBeenAnswered = false;
        this.options = new ArrayList<>();

    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public boolean isHasBeenAnswered() {
        return hasBeenAnswered;
    }

    public void setHasBeenAnswered(boolean hasBeenAnswered) {
        this.hasBeenAnswered = hasBeenAnswered;
    }


    //Parcel stuff

    private FillinSentencesSpinner(Parcel in) {
        this.isCorrect = in.readByte() != 0;
        this.hasBeenAnswered = in.readByte() != 0;
        options = in.createStringArrayList();
        selectedOption = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByte((byte) (this.isCorrect ? 1 : 0));
        out.writeByte((byte) (this.hasBeenAnswered ? 1 : 0));
        out.writeStringList(this.options);
        out.writeString(this.selectedOption);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<FillinSentencesSpinner> CREATOR = new Parcelable.Creator<FillinSentencesSpinner>() {
        public FillinSentencesSpinner createFromParcel(Parcel in) {
            return new FillinSentencesSpinner(in);
        }

        public FillinSentencesSpinner[] newArray(int size) {
            return new FillinSentencesSpinner[size];
        }
    };

}
