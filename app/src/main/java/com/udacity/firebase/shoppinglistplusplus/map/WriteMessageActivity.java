package com.udacity.firebase.shoppinglistplusplus.map;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.geofence.GeofenceReceiver;
import com.udacity.firebase.shoppinglistplusplus.model.Location;
import com.udacity.firebase.shoppinglistplusplus.model.Message;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.sharing.FriendsList;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;
import com.udacity.firebase.shoppinglistplusplus.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class WriteMessageActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private final String TAG = WriteMessageActivity.class.getSimpleName();

    private String senderName;
    private String senderEncodedEmail;
    private String receiverEncodedEmail;
    private double longitude;
    private double latitude;

    private TextView tvReceiver;
    private EditText etMessageContext;
    private ImageButton ibAddReceiver;

    private ArrayList<Geofence> mGeofenceList;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_message);
        overridePendingTransition(R.anim.trans_bottom_in, R.anim.trans_bottom_out);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(getString(R.string.title_send_message));
        }

        Intent intent = getIntent();
        if (intent != null) {
            latitude = intent.getDoubleExtra(Constants.FIREBASE_PROPERTY_LATITUDE, 0);
            longitude = intent.getDoubleExtra(Constants.FIREBASE_PROPERTY_LONGITUDE, 0);
            senderEncodedEmail = intent.getStringExtra(Constants.KEY_SENDER_ENCODED_EMAIL);
            final Firebase userRef = new Firebase(Constants.FIREBASE_URL_USERS).child(senderEncodedEmail);
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        senderName = user.getName();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }

        tvReceiver = (TextView) findViewById(R.id.tv_add_receiver);
        etMessageContext = (EditText) findViewById(R.id.et_message_context);
        ibAddReceiver = (ImageButton) findViewById(R.id.button_add_receiver);

        buildGoogleApiClient();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_top_in, R.anim.trans_top_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send_message) {
            if (tvReceiver.getText().toString().equals("")) {
                Toast.makeText(WriteMessageActivity.this, getString(R.string.toast_no_receiver_selected), Toast.LENGTH_LONG).show();
            } else if (etMessageContext.getText().toString().equals("")) {
                Toast.makeText(WriteMessageActivity.this, getString(R.string.toast_message_context_empty), Toast.LENGTH_LONG).show();
            } else {
                HashMap<String, Object> timestampCreated = new HashMap<>();
                timestampCreated.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
                String messageContext = etMessageContext.getText().toString();
                Location location = new Location(latitude, longitude);

                Message message = new Message(messageContext, location, timestampCreated, senderEncodedEmail, senderName);
                Firebase sentMessagesRef = new Firebase(Constants.FIREBASE_URL_SENT_MESSAGES).
                        child(senderEncodedEmail);
                Firebase receivedMessagesRef = new Firebase(Constants.FIREBASE_URL_RECEIVED_MESSAGES).
                        child(receiverEncodedEmail);

                Firebase newMessageRef = sentMessagesRef.push();
                String messageId = newMessageRef.getKey();

                sentMessagesRef.child(messageId).setValue(message);
                receivedMessagesRef.child(messageId).setValue(message);

                populateGeofenceList();
                addGeofence(messageId);

                Intent intent = new Intent();
                intent.putExtra(Constants.KEY_MESSAGE_ID, messageId);
                setResult(Constants.CREATE_MESSAGE_REQUEST_CODE, intent);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void onAddReceiverPressed(View view) {
        Intent intent = new Intent(WriteMessageActivity.this, FriendsList.class);
        startActivityForResult(intent, Constants.FRIEND_LIST_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.FRIEND_LIST_REQUEST_CODE) {
            if (data != null && data.hasExtra(Constants.KEY_ENCODED_EMAIL)) {
                Log.i(TAG, data.getStringExtra(Constants.KEY_ENCODED_EMAIL)
                        + " " + data.getStringExtra(Constants.KEY_USER_NAME));
                receiverEncodedEmail = data.getStringExtra(Constants.KEY_ENCODED_EMAIL);
                tvReceiver.setText(data.getStringExtra(Constants.KEY_USER_NAME));
                ibAddReceiver.setImageResource(R.drawable.ic_shared_check);
            }
        }
    }

    public void addGeofence(String messageId) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent(messageId)
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, "Invalid location permission.", securityException);
        }
    }

    public void populateGeofenceList() {

        mGeofenceList = new ArrayList<>();
        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId("message")

                        // Set the circular region of this geofence.
                .setCircularRegion(
                        latitude,
                        longitude,
                        Constants.GEOFENCE_RADIUS_IN_METERS
                )

                        // Set the expiration duration of the geofence. This geofence gets automatically
                        // removed after this period of time.
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                        // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)

                        // Create the geofence.
                .build());

    }

    private PendingIntent getGeofencePendingIntent(String messageId) {
        Intent intent = new Intent(this, GeofenceReceiver.class);
        intent.putExtra(Constants.KEY_RECEIVER_ENCODED_EMAIL, receiverEncodedEmail);
        intent.putExtra(Constants.KEY_SENDER_ENCODED_EMAIL, senderEncodedEmail);
        intent.putExtra(Constants.KEY_MESSAGE_ID, messageId);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Toast.makeText(
                    WriteMessageActivity.this,
                    getString(R.string.toast_message_created),
                    Toast.LENGTH_LONG
            ).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = Utils.getErrorString(this,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Do something with result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }
}
