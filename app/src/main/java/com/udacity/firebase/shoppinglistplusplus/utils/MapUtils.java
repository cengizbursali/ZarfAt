package com.udacity.firebase.shoppinglistplusplus.utils;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.google.android.gms.maps.model.LatLng;
import com.udacity.firebase.shoppinglistplusplus.model.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rajaee on 3/7/16.
 */
public class MapUtils {
    private static final String TAG = MapUtils.class.getSimpleName();

    public static String createMessage(String messageContext, String senderEncodedMail,
                                       String receiverEncodedEmail, LatLng latLng) {

        Firebase messagesRef = new Firebase(Constants.FIREBASE_URL_SENT_MESSAGES).
                child(senderEncodedMail);
        final Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);

        Firebase newMessageRef = messagesRef.push();

        String messageId = newMessageRef.getKey();

        HashMap<String, Object> newMessageMapping = new HashMap<>();

        HashMap<String, Object> timestampCreated = new HashMap<>();
        timestampCreated.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        HashMap<String, Object> location = new HashMap<>();
        location.put(Constants.FIREBASE_PROPERTY_LATITUDE, latLng.latitude);
        location.put(Constants.FIREBASE_PROPERTY_LONGITUDE, latLng.longitude);

        Message message = new Message(messageContext, location, timestampCreated, senderEncodedMail);

        HashMap<String, Object> sentMessageMap = (HashMap<String, Object>)
                new ObjectMapper().convertValue(message, Map.class);
        HashMap<String, Object> receivedMessageMap = (HashMap<String, Object>)
                new ObjectMapper().convertValue(message, Map.class);

//        newMessageMapping.put("/" + Constants.FIREBASE_LOCATION_SENT_MESSAGES + "/" + senderEncodedMail + "/"
//                + messageId, sentMessageMap);
        newMessageMapping.put("/" + Constants.FIREBASE_LOCATION_RECEIVED_MESSAGES + "/" + receiverEncodedEmail + "/"
                + messageId, receivedMessageMap);

        firebaseRef.updateChildren(newMessageMapping, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                Log.i(TAG, "message added");
            }
        });
        return messageId;
    }
}