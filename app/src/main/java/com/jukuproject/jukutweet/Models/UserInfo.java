package com.jukuproject.jukutweet.Models;

/**
 * Created by JukuProject on 3/20/2017.
 * Represents data for a single twitter user in the InternalDB database
 */

public class UserInfo {
    public UserInfo() {}

    public UserInfo(String screen_name) {
        this.screen_name = screen_name;
    }

    public String getScreenName() {
        return screen_name;
    }

    public void setScreenName(String screen_name) {
        this.screen_name = screen_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getFollowers_count() {
        return followers_count;
    }

    public void setFollowers_count(Integer followers_count) {
        this.followers_count = followers_count;
    }

    public Integer getFriends_count() {
        return friends_count;
    }

    public void setFriends_count(Integer friends_count) {
        this.friends_count = friends_count;
    }

    public Integer getListed_count() {
        return listed_count;
    }

    public void setListed_count(Integer listed_count) {
        this.listed_count = listed_count;
    }

    public String getProfile_background_image_url() {
        return profile_background_image_url;
    }

    public void setProfile_background_image_url(String profile_background_image_url) {
        this.profile_background_image_url = profile_background_image_url;
    }

    public String getProfile_image_url() {
        return profile_image_url;
    }

    public void setProfile_image_url(String profile_image_url) {
        this.profile_image_url = profile_image_url;
    }

    public String getProfile_banner_url() {
        return profile_banner_url;
    }

    public void setProfile_banner_url(String profile_banner_url) {
        this.profile_banner_url = profile_banner_url;
    }

    private String screen_name;
    private int id;
    private String location;
    private String description;
    private String url;
//    private String expanded_url;
//    private String display_url;
    private Integer followers_count;
    private Integer friends_count;
    private Integer listed_count;
    private String profile_background_image_url;
    private String profile_image_url;
    private String profile_banner_url;



}