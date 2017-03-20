package com.jukuproject.jukutweet.Models;

/**
 * Created by JukuProject on 3/20/2017.
 * Represents data for a single twitter user in the InternalDB database
 */

public class User {


    private String name;


    public User(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

}
