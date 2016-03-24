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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
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
import com.udacity.firebase.shoppinglistplusplus.utils.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapMainActivity extends MapBaseActivity implements OnMapReadyCallback, ResultCallback<Status> {

    private static final String TAG = MapMainActivity.class.getSimpleName();

    private SupportMapFragment mapFragment;
    private Location mLastLocation;
    private Marker currentMarker;
    private Circle currentCircle;
    private ArrayList<Geofence> mGeofenceList;
    private String receiverEncodedEmail;
    private String senderEncodedMail;

    private LinearLayout writeMessageLayout;
    private Button createMessageButton;
    private EditText messageEditText;
    private TextView addFriendTextView;
    private LinearLayout showMessageLayout;

    private Map<Marker, String> messageMap;

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.containsKey(Constants.KEY_MESSAGE_ID)) {
            final String messageId = bundle.getString(Constants.KEY_MESSAGE_ID);
            Log.i(TAG, "message id: " + messageId);
            Firebase messageRef = new Firebase(Constants.FIREBASE_URL_RECEIVED_MESSAGES)
                    .child(senderEncodedMail)
                    .child(messageId);
            messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message != null) {
                        LatLng latLng = new LatLng(Double.parseDouble(message.getLocation()
                                .get(Constants.FIREBASE_PROPERTY_LATITUDE).toString()),
                                Double.parseDouble(message.getLocation()
                                        .get(Constants.FIREBASE_PROPERTY_LONGITUDE).toString()));
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

        senderEncodedMail = PreferenceManager.getDefaultSharedPreferences(MapMainActivity.this)
                .getString(Constants.KEY_ENCODED_EMAIL, null);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGeofenceList = new ArrayList<>();

        messageMap = new HashMap<>();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        initializeScreen();
        onNewIntent(getIntent());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search_place) {
            createPlaceFinder();
        } else if (id == R.id.action_user_location) {
            if (ActivityCompat.checkSelfPermission(MapMainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MapMainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.REQUEST_CODE_LOCATION);
                return false;
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mapReady = true;
        m_map = map;

        m_map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.i(TAG, "map click");
                if (writeMessageLayout.getVisibility() == View.VISIBLE) {
                    hideWriteMessageLayout(writeMessageLayout);
                    addFriendTextView.setText(R.string.text_add_friend);
                    if (currentCircle != null) {
                        currentCircle.remove();
                    }
                    if (currentMarker != null) {
                        currentMarker.remove();
                    }
                } else if (showMessageLayout.getVisibility() == View.VISIBLE) {
                    hideWriteMessageLayout(showMessageLayout);
                } else {
                    createCircledMarker(latLng);
                }
            }
        });

        m_map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (writeMessageLayout.getVisibility() == View.GONE
                        && showMessageLayout.getVisibility() == View.GONE) {
                    marker.showInfoWindow();
                }
                return true;
            }
        });

        m_map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.getTitle().equals(getString(R.string.title_message_received))) {
                    showMessage(Constants.FIREBASE_URL_RECEIVED_MESSAGES, marker);
                } else if (marker.getTitle().equals(getString(R.string.title_message_left))) {
                    showMessage(Constants.FIREBASE_URL_SENT_MESSAGES, marker);
                } else if (marker.getTitle().equals(getString(R.string.title_leave_message))
                        && writeMessageLayout.getVisibility() != View.VISIBLE) {
                    flyTo(new LatLng(marker.getPosition().latitude - 0.0005, marker.getPosition().longitude),
                            Constants.MAP_ZOOM_LEVEL_CLOSE, Constants.MAP_FLY_TIME_SEC_FAST);
                    showWriteMessageLayout(writeMessageLayout);
                    currentCircle = m_map.addCircle(new CircleOptions()
                            .center(marker.getPosition())
                            .radius(Constants.GEOFENCE_RADIUS_IN_METERS)
                            .strokeColor(ContextCompat.getColor(getBaseContext(), R.color.accent))
                            .fillColor(Color.argb(64, 0, 255, 0)));
                }
            }
        });

        LatLng newYork = new LatLng(32.3119443, 35.0178292);
        CameraPosition target = CameraPosition.builder().target(newYork).zoom(Constants.MAP_ZOOM_LEVEL_FAR).build();
        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));
    }

    private void showMessage(String url, Marker marker) {
        Firebase messageRef = new Firebase(url)
                .child(senderEncodedMail)
                .child(messageMap.get(marker));
        messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    LatLng latLng = new LatLng(Double.parseDouble(message.getLocation()
                            .get(Constants.FIREBASE_PROPERTY_LATITUDE).toString()),
                            Double.parseDouble(message.getLocation()
                                    .get(Constants.FIREBASE_PROPERTY_LONGITUDE).toString()));
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

    private void initializeScreen() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        writeMessageLayout = (LinearLayout) findViewById(R.id.write_message_layout);
        addFriendTextView = (TextView) writeMessageLayout.findViewById(R.id.tv_add_friend);
        messageEditText = (EditText) findViewById(R.id.message_edit_text);
        createMessageButton = (Button) findViewById(R.id.create_message_button);

        showMessageLayout = (LinearLayout) findViewById(R.id.show_message_layout);

        showMessages(Constants.FIREBASE_URL_SENT_MESSAGES, R.drawable.sent_message);
        showMessages(Constants.FIREBASE_URL_RECEIVED_MESSAGES, R.drawable.received_message);

        createMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                populateGeofenceList();
                String messageContext = messageEditText.getText().toString();
                String messageId;
                if (!messageContext.equals("")) {
                    messageId = MapUtils.createMessage(messageContext, senderEncodedMail, receiverEncodedEmail,
                            currentMarker.getPosition());
                    addGeofence(messageId);
                    hideWriteMessageLayout(writeMessageLayout);
                    addFriendTextView.setText(R.string.text_add_friend);
                    currentCircle.remove();
                    currentMarker.setTitle(getString(R.string.title_message_left));
                    currentMarker.hideInfoWindow();
                    currentMarker = null;
                } else {
                    Toast.makeText(
                            MapMainActivity.this,
                            getString(R.string.toast_message_context_empty),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });

    }

    private void showMessages(final String url, final int iconRecourse) {
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
                    Marker marker = m_map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(getString(R.string.title_message_received))
                            .icon(BitmapDescriptorFactory.fromResource(iconRecourse)));
                    if (url.equals(Constants.FIREBASE_URL_RECEIVED_MESSAGES)) {
                        messageMap.put(marker, snapshot.getKey());
                    }
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
            hideWriteMessageLayout(writeMessageLayout);
            addFriendTextView.setText(R.string.text_add_friend);
            if (currentCircle != null) {
                currentCircle.remove();
            }
            if (currentMarker != null) {
                currentMarker.remove();
            }
        } else if (showMessageLayout.getVisibility() == View.VISIBLE) {
            hideWriteMessageLayout(showMessageLayout);
        } else {
            super.onBackPressed();
        }
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

    protected void hideWriteMessageLayout(LinearLayout layout) {
        Animation bottomDown = AnimationUtils.loadAnimation(this,
                R.anim.bottom_down);
        layout.startAnimation(bottomDown);
        layout.setVisibility(View.GONE);
        flyTo(m_map.getCameraPosition().target, Constants.MAP_ZOOM_LEVEL_FAR, Constants.MAP_FLY_TIME_SEC_FAST);
        m_map.getUiSettings().setAllGesturesEnabled(true);
    }

    protected void showWriteMessageLayout(LinearLayout layout) {
        Animation bottomUp = AnimationUtils.loadAnimation(this,
                R.anim.bottom_up);
        layout.startAnimation(bottomUp);
        layout.setVisibility(View.VISIBLE);
        m_map.getUiSettings().setAllGesturesEnabled(false);
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
                .title(getString(R.string.title_leave_message))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.sent_message)));
        currentMarker.showInfoWindow();
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

    private PendingIntent getGeofencePendingIntent(String messageId) {
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
                    MapMainActivity.this,
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

    /**
     * Launch AddFriendActivity to find and add user to current user's friends list
     * when the button AddFriend is pressed
     */
    public void onAddFriendPressed(View view) {
        Intent intent = new Intent(MapMainActivity.this, FriendsList.class);
        startActivityForResult(intent, Constants.FRIEND_LIST_REQUEST_CODE);
    }

}