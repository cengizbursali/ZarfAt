package com.udacity.firebase.shoppinglistplusplus.map;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.geofence.GeofenceErrorMessages;
import com.udacity.firebase.shoppinglistplusplus.geofence.GeofenceReceiver;
import com.udacity.firebase.shoppinglistplusplus.model.Message;
import com.udacity.firebase.shoppinglistplusplus.ui.sharing.FriendsList;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapMainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static final String TAG = MapMainActivity.class.getSimpleName();

    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    boolean mapReady = false;
    private GoogleMap m_map;
    private Marker currentMarker;
    private Circle currentCircle;
    private ArrayList<Geofence> mGeofenceList;
    private String receiverEncodedEmail;
    private String senderEncodedMail;
    private String messageId;

    private LinearLayout writeMessageLayout;
    private ImageView userLocImageView;
    private ImageView placeAutocompleteImageView;
    private Button createMessageButton;
    private EditText messageEditText;
    private TextView addFriendTextView;
    private LinearLayout showMessageLayout;

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.containsKey(Constants.KEY_MESSAGE_ID)) {
            Log.i(TAG, "message id: " + bundle.getString(Constants.KEY_MESSAGE_ID));
            Firebase messageRef = new Firebase(Constants.FIREBASE_URL_RECEIVED_MESSAGES)
                    .child(senderEncodedMail)
                    .child(bundle.getString(Constants.KEY_MESSAGE_ID));
            messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message != null) {
                        LatLng latLng = new LatLng(Double.parseDouble(message.getLocation()
                                .get(Constants.FIREBASE_PROPERTY_LATITUDE).toString()),
                                Double.parseDouble(message.getLocation()
                                        .get(Constants.FIREBASE_PROPERTY_LONGITUDE).toString()));
                        m_map.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(getString(R.string.title_message_received))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.received_message)))
                                .showInfoWindow();
                        flyTo(latLng, Constants.MAP_ZOOM_LEVEL_CLOSE, Constants.MAP_FLY_TIME_SEC_SLOW);
                        ((TextView) showMessageLayout.findViewById(R.id.tv_message_context))
                                .setText(message.getContext());
                        ((TextView) showMessageLayout.findViewById(R.id.tv_sender))
                                .setText(message.getCreator());
                        showWriteMessageLayout(showMessageLayout);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e(TAG, getString(R.string.log_error_the_read_failed) +
                            firebaseError.getMessage());
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);

        senderEncodedMail = PreferenceManager.getDefaultSharedPreferences(MapMainActivity.this)
                .getString(Constants.KEY_ENCODED_EMAIL, null);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<>();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        initializeScreen();
        onNewIntent(getIntent());
    }

    private void initializeScreen() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        userLocImageView = (ImageView) findViewById(R.id.user_location_imageview);

        writeMessageLayout = (LinearLayout) findViewById(R.id.write_message_layout);
        addFriendTextView = (TextView) writeMessageLayout.findViewById(R.id.tv_add_friend);
        messageEditText = (EditText) findViewById(R.id.message_edit_text);
        createMessageButton = (Button) findViewById(R.id.create_message_button);

        showMessageLayout = (LinearLayout) findViewById(R.id.show_message_layout);

        placeAutocompleteImageView = (ImageView) findViewById(R.id.place_autocomplete_imageview);


        showMessages(Constants.FIREBASE_URL_SENT_MESSAGES, R.drawable.sent_message);
        showMessages(Constants.FIREBASE_URL_RECEIVED_MESSAGES, R.drawable.received_message);

        createMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                populateGeofenceList();
                createMessage();
                addGeofence();
                hideWriteMessageLayout();
                currentCircle.remove();
                currentMarker.hideInfoWindow();
            }
        });

        userLocImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MapMainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MapMainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            Constants.REQUEST_CODE_LOCATION);
                    return;
                }
                if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                        .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Intent gpsOptionsIntent = new Intent(
                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(gpsOptionsIntent);
                } else {
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mLastLocation != null) {
                        LatLng userCurrentLoc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        createCircledMarker(userCurrentLoc);
                        flyTo(userCurrentLoc, Constants.MAP_ZOOM_LEVEL_FAR, Constants.MAP_FLY_TIME_SEC_SLOW);
                    }
                }


            }
        });

        placeAutocompleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPlaceFinder();
            }
        });
    }

    private void showMessages(String url, final int iconRecourse) {
        Firebase messagesRef = new Firebase(url).child(senderEncodedMail);
        Query messages = messagesRef.orderByKey();
        messages.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                while (children.iterator().hasNext()) {
                    DataSnapshot snapshot = children.iterator().next();
                    Message message = snapshot.getValue(Message.class);
                    LatLng latLng = new LatLng(Double.parseDouble(message.getLocation()
                            .get(Constants.FIREBASE_PROPERTY_LATITUDE).toString()),
                            Double.parseDouble(message.getLocation()
                                    .get(Constants.FIREBASE_PROPERTY_LONGITUDE).toString()));
                    m_map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(getString(R.string.title_message_received))
                            .icon(BitmapDescriptorFactory.fromResource(iconRecourse)));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, getString(R.string.log_error_the_read_failed) +
                        firebaseError.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (writeMessageLayout.getVisibility() == View.VISIBLE) {
            hideWriteMessageLayout();
            if (currentCircle != null) {
                currentCircle.remove();
            }
            if (currentMarker != null) {
                currentMarker.remove();
            }
            currentCircle.remove();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Launch AddFriendActivity to find and add user to current user's friends list
     * when the button AddFriend is pressed
     */
    public void onAddFriendPressed(View view) {
        Intent intent = new Intent(MapMainActivity.this, FriendsList.class);
        startActivityForResult(intent, Constants.FRIEND_LIST_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Constants.PLACE_AUTOCOMPLETE_RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                createCircledMarker(place.getLatLng());
                flyTo(place.getLatLng(), Constants.MAP_ZOOM_LEVEL_FAR, Constants.MAP_FLY_TIME_SEC_SLOW);
                Log.i(TAG, "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == Constants.PLACE_AUTOCOMPLETE_RESULT_CANCELED) {
                Log.i(TAG, "cancelled");
            }
        } else if (requestCode == Constants.FRIEND_LIST_REQUEST_CODE) {
            if (data != null && data.hasExtra(Constants.KEY_ENCODED_EMAIL)) {
                Log.i(TAG, data.getStringExtra(Constants.KEY_ENCODED_EMAIL)
                        + " " + data.getStringExtra(Constants.KEY_USER_NAME));
                receiverEncodedEmail = data.getStringExtra(Constants.KEY_ENCODED_EMAIL);
                addFriendTextView.setText(data.getStringExtra(Constants.KEY_USER_NAME));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mapReady = true;
        m_map = map;

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (writeMessageLayout.getVisibility() == View.VISIBLE) {
                    hideWriteMessageLayout();
                    if (currentCircle != null) {
                        currentCircle.remove();
                    }
                    if (currentMarker != null) {
                        currentMarker.remove();
                    }

                } else {
                    createCircledMarker(latLng);
                }
            }
        });

        LatLng newYork = new LatLng(32.3119443, 35.0178292);
        CameraPosition target = CameraPosition.builder().target(newYork).zoom(Constants.MAP_ZOOM_LEVEL_FAR).build();
        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));

    }

    protected void hideWriteMessageLayout() {
        Animation bottomDown = AnimationUtils.loadAnimation(this,
                R.anim.bottom_down);
        writeMessageLayout.startAnimation(bottomDown);
        writeMessageLayout.setVisibility(View.GONE);
        flyTo(currentMarker.getPosition(), Constants.MAP_ZOOM_LEVEL_FAR, Constants.MAP_FLY_TIME_SEC_FAST);
        addFriendTextView.setText(R.string.text_add_friend);
        m_map.getUiSettings().setScrollGesturesEnabled(true);
        m_map.getUiSettings().setZoomGesturesEnabled(true);
    }

    protected void showWriteMessageLayout(LinearLayout layout) {
        Animation bottomUp = AnimationUtils.loadAnimation(this,
                R.anim.bottom_up);
        layout.startAnimation(bottomUp);
        layout.setVisibility(View.VISIBLE);
        m_map.getUiSettings().setScrollGesturesEnabled(false);
        m_map.getUiSettings().setZoomGesturesEnabled(false);
    }

    private void flyTo(LatLng latLng, int zoom, int durationSec) {
        m_map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
                .target(latLng)
                .zoom(zoom)
                .build()), durationSec * 1000, null);
    }

    private void createCircledMarker(final LatLng latLng) {
        if (currentMarker != null) {
            currentMarker.remove();
        }
        currentMarker = m_map.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Leave a message here!")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.sent_message)));
        currentMarker.showInfoWindow();
        m_map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                if (writeMessageLayout.getVisibility() != View.VISIBLE) {
                    flyTo(new LatLng(latLng.latitude - 0.0005, latLng.longitude),
                            Constants.MAP_ZOOM_LEVEL_CLOSE, Constants.MAP_FLY_TIME_SEC_FAST);
                    showWriteMessageLayout(writeMessageLayout);
                    currentCircle = m_map.addCircle(new CircleOptions()
                            .center(latLng)
                            .radius(Constants.GEOFENCE_RADIUS_IN_METERS)
                            .strokeColor(Color.GREEN)
                            .fillColor(Color.argb(64, 0, 255, 0)));
                }
            }
        });
    }

    private void createPlaceFinder() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(this);
            startActivityForResult(intent, Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(TAG, "Could not create place finder " + e.getMessage());
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "Could not create place finder " + e.getMessage());
        }
    }

    public void addGeofence() {
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
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, "Invalid location permission.", securityException);
        }
    }

    public void populateGeofenceList() {

        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId("message")

                        // Set the circular region of this geofence.
                .setCircularRegion(
                        currentMarker.getPosition().latitude,
                        currentMarker.getPosition().longitude,
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

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceReceiver.class);
        intent.putExtra(Constants.KEY_ENCODED_EMAIL, receiverEncodedEmail);
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
                    this,
                    getString(R.string.toast_message_created),
                    Toast.LENGTH_LONG
            ).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
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
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        Log.i(TAG, "GoogleApiClient connected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    public void createMessage() {
        String messageContext = messageEditText.getText().toString();

        if (!messageContext.equals("")) {

            Firebase messagesRef = new Firebase(Constants.FIREBASE_URL_SENT_MESSAGES).
                    child(senderEncodedMail);
            final Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);

            Firebase newMessageRef = messagesRef.push();

            messageId = newMessageRef.getKey();

            HashMap<String, Object> newMessageMapping = new HashMap<>();

            HashMap<String, Object> timestampCreated = new HashMap<>();
            timestampCreated.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

            HashMap<String, Object> location = new HashMap<>();
            location.put(Constants.FIREBASE_PROPERTY_LATITUDE, currentMarker.getPosition().latitude);
            location.put(Constants.FIREBASE_PROPERTY_LONGITUDE, currentMarker.getPosition().longitude);

            Message message = new Message(messageContext, location, timestampCreated, senderEncodedMail);

            HashMap<String, Object> sentMessageMap = (HashMap<String, Object>)
                    new ObjectMapper().convertValue(message, Map.class);
            HashMap<String, Object> receivedMessageMap = (HashMap<String, Object>)
                    new ObjectMapper().convertValue(message, Map.class);

            newMessageMapping.put("/" + Constants.FIREBASE_LOCATION_SENT_MESSAGES + "/" + senderEncodedMail + "/"
                    + messageId, sentMessageMap);
            newMessageMapping.put("/" + Constants.FIREBASE_LOCATION_RECEIVED_MESSAGES + "/" + receiverEncodedEmail + "/"
                    + messageId, receivedMessageMap);

            firebaseRef.updateChildren(newMessageMapping, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                    Log.i(TAG, "message added");
                }
            });
        }
    }
}
