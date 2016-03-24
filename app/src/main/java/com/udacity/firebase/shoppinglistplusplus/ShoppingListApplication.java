package com.udacity.firebase.shoppinglistplusplus;

import com.facebook.FacebookSdk;
import com.firebase.client.Firebase;

/**
 * Includes one-time initialization of Firebase related code
 */
public class ShoppingListApplication extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        /* Initialize Firebase */
        Firebase.setAndroidContext(this);

        FacebookSdk.sdkInitialize(this);
        /* Enable disk persistence  */
//        Firebase.getDefaultConfig().setPersistenceEnabled(true);
//        Firebase.getDefaultConfig().setLogLevel(Logger.Level.DEBUG);
    }

}