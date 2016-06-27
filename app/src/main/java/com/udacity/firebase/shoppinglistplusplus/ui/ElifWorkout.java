package com.udacity.firebase.shoppinglistplusplus.ui;

/**
 * Created by rajaee on 3/16/16.
 */
public class ElifWorkout {
    private String name;
    private String url;

    public ElifWorkout() {
    }

    public ElifWorkout(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}