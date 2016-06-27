package com.udacity.firebase.shoppinglistplusplus.model;

import java.util.HashMap;

public class User {
    private String name;
    private String email;
    private String gender;
    private HashMap<String, Object> timestampJoined;
    private boolean hasLoggedInWithPassword;


    public User() {
    }

    public User(String name, String email, HashMap<String, Object> timestampJoined) {
        this.name = name;
        this.email = email;
        this.timestampJoined = timestampJoined;
        this.hasLoggedInWithPassword = false;
    }

    public User(String name, String email, String gender, HashMap<String, Object> timestampJoined) {
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.timestampJoined = timestampJoined;
        this.hasLoggedInWithPassword = false;
    }

    public String getGender() {
        return gender;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public HashMap<String, Object> getTimestampJoined() {
        return timestampJoined;
    }

    public boolean isHasLoggedInWithPassword() {
        return hasLoggedInWithPassword;
    }
}
