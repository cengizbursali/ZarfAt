package com.udacity.firebase.shoppinglistplusplus.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.firebase.client.Firebase;
import com.firebase.client.ServerValue;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.Message;

import java.util.HashMap;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static String createMessage(String messageContext, String userMail, LatLng latLng) {

        Firebase messagesRef = new Firebase(Constants.FIREBASE_URL_SENT_MESSAGES).
                child(userMail);

        Firebase newMessageRef = messagesRef.push();
        String messageId = newMessageRef.getKey();

        HashMap<String, Object> timestampCreated = new HashMap<>();
        timestampCreated.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        HashMap<String, Object> location = new HashMap<>();
        location.put(Constants.FIREBASE_PROPERTY_LATITUDE, latLng.latitude);
        location.put(Constants.FIREBASE_PROPERTY_LONGITUDE, latLng.longitude);

        Message message = new Message(messageContext, null, timestampCreated, userMail, null);

        messagesRef.child(messageId).setValue(message);
        Log.i(TAG, "message has been saved to firebase");
        return messageId;
    }

    public static String createMessage(Message message, String userMail) {

        Firebase messagesRef = new Firebase(Constants.FIREBASE_URL_RECEIVED_MESSAGES).
                child(userMail);

        Firebase newMessageRef = messagesRef.push();
        String messageId = newMessageRef.getKey();

        messagesRef.child(messageId).setValue(message);
        Log.i(TAG, "message has been saved to firebase");
        return messageId;
    }

    public static String getErrorString(Context context, int errorCode) {
        Resources mResources = context.getResources();
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return mResources.getString(R.string.geofence_not_available);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return mResources.getString(R.string.geofence_too_many_geofences);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return mResources.getString(R.string.geofence_too_many_pending_intents);
            default:
                return mResources.getString(R.string.unknown_geofence_error);
        }
    }

    public static String encodeEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

    public static String decodeEmail(String userEmail) {
        return userEmail.replace(",", ".");
    }
}