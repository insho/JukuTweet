package com.jukuproject.jukutweet.Models;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


/**
 * Object containing data from a Multiple Choice Quiz.
 * Data is parceled and passed to {@link com.jukuproject.jukutweet.PostQuizStatsActivity} after the quiz
 */
public class MultChoiceResult implements Parcelable {

    private WordEntry wordEntry;
    private Boolean correct;
    private String detailInfo;
    private Integer currentTotal;

    public MultChoiceResult(WordEntry wordEntry, @Nullable Boolean _correct, @NonNull String _detailInfo, @NonNull Integer _currentTotal) {
        this.wordEntry = wordEntry;
        this.correct = _correct;
        this.detailInfo = _detailInfo;
        this.currentTotal = _currentTotal;
    }

    public WordEntry getWordEntry() {
        return wordEntry;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public String getDetailInfo() {
        return detailInfo;
    }

    public Integer getCurrentTotal() {
        return currentTotal;
    }

    private MultChoiceResult(Parcel in) {
        this.wordEntry = in.readParcelable(WordEntry.class.getClassLoader());
        correct = in.readInt() != 0;
        detailInfo = in.readString();
        currentTotal = in.readInt();
    }


    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(this.wordEntry,flags);
        out.writeInt(correct ? 1 : 0);
        out.writeString(detailInfo);
        out.writeInt(currentTotal);

    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<MultChoiceResult> CREATOR = new Parcelable.Creator<MultChoiceResult>() {
        public MultChoiceResult createFromParcel(Parcel in) {
            return new MultChoiceResult(in);
        }

        public MultChoiceResult[] newArray(int size) {
            return new MultChoiceResult[size];
        }
    };




}