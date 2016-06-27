package com.udacity.firebase.shoppinglistplusplus.model;

import java.io.Serializable;
import java.util.HashMap;

public class Message implements Serializable {
    private String context;
    private String creatorEmail;
    private String creatorName;
    private Location location;
    private HashMap<String, Object> timestampCreated;
    private HashMap<String, Object> timestampRead;

    public Message() {
    }

    public Message(String context, Location location, HashMap<String, Object> timestampCreated,
                   String creatorEmail, String creatorName) {
        this.context = context;
        this.location = location;
        this.timestampCreated = timestampCreated;
        this.creatorEmail = creatorEmail;
        this.creatorName = creatorName;

    }

    public void setTimestampRead(HashMap<String, Object> timestampRead) {
        this.timestampRead = timestampRead;
    }

    public HashMap<String, Object> getTimestampRead() {
        return timestampRead;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public HashMap<String, Object> getTimestampCreated() {
        return timestampCreated;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public Location getLocation() {
        return location;
    }

    public String getContext() {
        return context;
    }


}
