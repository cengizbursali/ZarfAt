package com.udacity.firebase.shoppinglistplusplus.model;

import java.util.HashMap;

/**
 * Created by rajaee on 2/27/16.
 */
public class Message {
    private String context;
    private String creator;
    private HashMap<String, Object> location;
    private HashMap<String, Object> timestampCreated;

    public Message() {
    }

    public Message(String context, HashMap<String, Object> location,
                   HashMap<String, Object> timestampCreated, String creator) {
        this.context = context;
        this.location = location;
        this.timestampCreated = timestampCreated;
        this.creator = creator;

    }

    public HashMap<String, Object> getTimestampCreated() {
        return timestampCreated;
    }

    public String getCreator() {
        return creator;
    }

    public HashMap<String, Object> getLocation() {
        return location;
    }

    public String getContext() {
        return context;
    }


}
