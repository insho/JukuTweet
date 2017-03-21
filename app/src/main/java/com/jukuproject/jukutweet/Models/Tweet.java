package com.jukuproject.jukutweet.Models;

/**
 * Created by JClassic on 3/20/2017.
 */

public class Tweet {
    private Boolean favorited;
    private Boolean truncated;
    private String created_at;
    private String id;
    private Integer retweet_count;
    private String text;

    public Tweet() {};


    public Tweet(String text) {
        this.text = text;
    }

    public Tweet(Boolean favorited, Boolean truncated, String created_at, String id, Integer retweet_count, String text) {
        this.favorited = favorited;
        this.truncated = truncated;
        this.created_at = created_at;
        this.id = id;
        this.retweet_count = retweet_count;
        this.text = text;
    }

    public Boolean getFavorited() {
        return favorited;
    }

    public void setFavorited(Boolean favorited) {
        this.favorited = favorited;
    }

    public Boolean getTruncated() {
        return truncated;
    }

    public void setTruncated(Boolean truncated) {
        this.truncated = truncated;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRetweet_count() {
        return retweet_count;
    }

    public void setRetweet_count(Integer retweet_count) {
        this.retweet_count = retweet_count;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}


