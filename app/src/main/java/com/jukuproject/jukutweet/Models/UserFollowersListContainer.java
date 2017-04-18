package com.jukuproject.jukutweet.Models;

import java.util.List;

/**
 * Created by JukuProject on 3/20/2017.
 * Represents data for a single twitter user in the InternalDB database
 */

public class UserFollowersListContainer {


    private List<UserInfo> users;
//    private Integer next_cursor;
    private String next_cursor_str;
//    private Integer previous_cursor;
    private String previous_cursor_str;

    public UserFollowersListContainer() {}

    public List<UserInfo> getUsers() {
        return users;
    }

    public void setUsers(List<UserInfo> users) {
        this.users = users;
    }

    public String getNextCursorString() {
        return next_cursor_str;
    }

    //    // Parcelling part
//    public UserFollowersListContainer(Parcel in){
//        this.users = in.readParcelableArray(UserInfo.class.getClassLoader());
//        this.screen_name = in.readString();
//        this.id_str = in.readString();
//        this.location = in.readString();
//    }
//
//    public int describeContents(){
//        return 0;
//    }
//
//
//
//    @Override
//    public void writeToParcel(Parcel out, int flags) {
//
//        out.writeParcelable(this.users,flags);
//
//        out.writeString(this.name);
//        out.writeString(this.screen_name);
//        out.writeString(this.id_str);
//        out.writeString(this.location);
//        out.writeString(this.description);
//
//    }
//
//    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
//        public UserFollowersListContainer createFromParcel(Parcel in) {
//            return new UserFollowersListContainer(in);
//        }
//
//        public UserFollowersListContainer[] newArray(int size) {
//            return new UserFollowersListContainer[size];
//        }
//    };

}