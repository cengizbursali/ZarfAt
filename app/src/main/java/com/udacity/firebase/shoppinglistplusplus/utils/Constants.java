package com.udacity.firebase.shoppinglistplusplus.utils;

import com.udacity.firebase.shoppinglistplusplus.BuildConfig;

/**
 * Constants class store most important strings and paths of the app
 */
public final class Constants {

    public static final int MAP_FLY_TIME_SEC_SLOW = 2;
    public static final int MAP_FLY_TIME_SEC_FAST = 1;
    public static final int MAP_ZOOM_LEVEL_CLOSE = 18;
    public static final int MAP_ZOOM_LEVEL_FAR = 14;

    /**
     * Constants related to locations in Firebase, such as the name of the node
     * where user lists are stored (ie "userLists")
     */
    public static final String FIREBASE_LOCATION_SHOPPING_LIST_ITEMS = "shoppingListItems";
    public static final String FIREBASE_LOCATION_USERS = "users";
    public static final String FIREBASE_LOCATION_USER_LISTS = "userLists";
    public static final String FIREBASE_LOCATION_USER_FRIENDS = "userFriends";
    public static final String FIREBASE_LOCATION_LISTS_SHARED_WITH = "sharedWith";
    public static final String FIREBASE_LOCATION_UID_MAPPINGS = "uidMappings";
    public static final String FIREBASE_LOCATION_OWNER_MAPPINGS = "ownerMappings";
    public static final String FIREBASE_LOCATION_SENT_MESSAGES = "sentMessages";
    public static final String FIREBASE_LOCATION_RECEIVED_MESSAGES = "receivedMessages";


    /**
     * Constants for Firebase object properties
     */
    public static final String FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED = "timestampLastChanged";
    public static final String FIREBASE_PROPERTY_TIMESTAMP = "timestamp";
    public static final String FIREBASE_PROPERTY_EMAIL = "email";
    public static final String FIREBASE_PROPERTY_NAME = "name";
    public static final String FIREBASE_PROPERTY_USER_HAS_LOGGED_IN_WITH_PASSWORD = "hasLoggedInWithPassword";
    public static final String FIREBASE_PROPERTY_CONTEXT = "context";
    public static final String FIREBASE_PROPERTY_LOCATION = "location";
    public static final String FIREBASE_PROPERTY_LONGITUDE = "longitude";
    public static final String FIREBASE_PROPERTY_LATITUDE = "latitude";


    /**
     * Constants for Firebase URL
     */
    public static final String FIREBASE_URL = "https://sizzling-heat-3535.firebaseio.com/";
    public static final String FIREBASE_URL_SHOPPING_LIST_ITEMS = FIREBASE_URL + "/" + FIREBASE_LOCATION_SHOPPING_LIST_ITEMS;
    public static final String FIREBASE_URL_USERS = FIREBASE_URL + "/" + FIREBASE_LOCATION_USERS;
    public static final String FIREBASE_URL_USER_LISTS = FIREBASE_URL + "/" + FIREBASE_LOCATION_USER_LISTS;
    public static final String FIREBASE_URL_USER_FRIENDS = FIREBASE_URL + "/" + FIREBASE_LOCATION_USER_FRIENDS;
    public static final String FIREBASE_URL_LISTS_SHARED_WITH = FIREBASE_URL + "/" + FIREBASE_LOCATION_LISTS_SHARED_WITH;
    public static final String FIREBASE_URL_SENT_MESSAGES = FIREBASE_URL + "/" + FIREBASE_LOCATION_SENT_MESSAGES;
    public static final String FIREBASE_URL_RECEIVED_MESSAGES = FIREBASE_URL + "/" + FIREBASE_LOCATION_RECEIVED_MESSAGES;

    /**
     * Constants for bundles, extras and shared preferences keys
     */
    public static final String KEY_SIGNUP_EMAIL = "SIGNUP_EMAIL";
    public static final String KEY_PROVIDER = "PROVIDER";
    public static final String KEY_ENCODED_EMAIL = "ENCODED_EMAIL";
    public static final String KEY_MESSAGE_ID = "MESSAGE_ID";
    public static final String KEY_USER_NAME = "USER_NAME";
    public static final String KEY_SENDER_ENCODED_EMAIL = "KEY_SENDER_ENCODED_EMAIL";
    public static final String KEY_RECEIVER_ENCODED_EMAIL = "KEY_RECEIVER_ENCODED_EMAIL";
    public static final String KEY_MESSAGE_OBJECT = "KEY_MESSAGE_OBJECT";


    /**
     * Constants for location permission
     */
    public static final int REQUEST_CODE_LOCATION = 2;

    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    public static final int PLACE_AUTOCOMPLETE_RESULT_OK = -1;
    public static final int PLACE_AUTOCOMPLETE_RESULT_CANCELED = 0;

    public static final int FRIEND_LIST_REQUEST_CODE = 3;
    public static final int CREATE_MESSAGE_REQUEST_CODE = 4;

    /**
     * Constants for Firebase login
     */
    public static final String PASSWORD_PROVIDER = "password";
    public static final String GOOGLE_PROVIDER = "google";
    public static final String FACEBOOK_PROVIDER = "facebook";
    public static final String PROVIDER_DATA_DISPLAY_NAME = "displayName";


    public static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";
    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";
    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 50; // 1 mile, 1.6 km

    private Constants() {
    }

}
