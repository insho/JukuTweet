//package com.jukuproject.jukutweet.Models;
//
//import android.os.Parcel;
//import android.os.Parcelable;
//
///**
// * Created by JClassic on 3/22/2017.
// */
//
//public class TabStatus implements  Parcelable {
//
//    public TabStatus(int tabNumber) {
//        this.tabNumber = tabNumber;
//        this.isViewInited = false;
//    }
//
//    private int tabNumber;
//    private boolean isViewInited;
//
//    public int getTabNumber() {
//        return tabNumber;
//    }
//
//    public boolean isViewInited() {
//        return isViewInited;
//    }
//
//    public void setViewInited(boolean viewInited) {
//        isViewInited = viewInited;
//    }
//
//
//
//    // Parcelling part
//    public TabStatus(Parcel in){
////        String[] data = new String[8];
//          this.tabNumber =  in.readInt();
//        this.isViewInited = in.readByte() != 0;
//
//
//    }
//
//    public int describeContents(){
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//       dest.writeInt(this.tabNumber);
//        dest.writeByte((byte) (this.isViewInited ? 1 : 0));
//    }
//
//    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
//        public TabStatus createFromParcel(Parcel in) {
//            return new TabStatus(in);
//        }
//
//        public TabStatus[] newArray(int size) {
//            return new TabStatus[size];
//        }
//    };
//}
