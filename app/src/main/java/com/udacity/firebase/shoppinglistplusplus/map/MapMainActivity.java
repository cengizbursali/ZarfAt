package com.udacity.firebase.shoppinglistplusplus.map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.Message;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class MapMainActivity extends MapBaseActivity implements OnMapReadyCallback {

    private static final String TAG = MapMainActivity.class.getSimpleName();

    private Marker currentMarker;
    private String senderEncodedEmail;

    private Map<Marker, String> messageMap;
    private Map<Marker, String> markerMessageMap;

    @Override
    protected void onNewIntent(Intent intent) {

        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.containsKey(Constants.KEY_MESSAGE_ID)) {
            final String messageId = bundle.getString(Constants.KEY_MESSAGE_ID);
            String senderEncodedMail = bundle.getString(Constants.KEY_SENDER_ENCODED_EMAIL);

            if (currentMarker != null) {
                currentMarker.remove();
            }

            Firebase messageRef = new Firebase(Constants.FIREBASE_URL_SENT_MESSAGES)
                    .child(senderEncodedMail)
                    .child(messageId);
            messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Message message = dataSnapshot.getValue(Message.class);
                    if (message != null) {
                        LatLng latLng = new LatLng(message.getLocation().getLatitude(),
                                message.getLocation().getLongitude());
                        flyTo(latLng, Constants.MAP_ZOOM_LEVEL_CLOSE, Constants.MAP_FLY_TIME_SEC_FAST);

                        if (!messageMap.containsValue(messageId)) {
                            Marker marker = m_map.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(getString(R.string.title_message_received))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_sent_message)));
                            messageMap.put(marker, messageId);
                        }
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent showMessageIntent = new Intent(MapMainActivity.this, ShowMessageActivity.class);
                                showMessageIntent.putExtra(Constants.KEY_MESSAGE_OBJECT, message);
                                startActivity(showMessageIntent);
                            }
                        }, 1000);
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

        senderEncodedEmail = PreferenceManager.getDefaultSharedPreferences(MapMainActivity.this)
                .getString(Constants.KEY_ENCODED_EMAIL, null);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        messageMap = new HashMap<>();
        markerMessageMap = new HashMap<>();

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
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    LatLng userCurrentLoc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    createMarker(userCurrentLoc);
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
                createMarker(latLng);
            }
        });

//        m_map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//
//                return true;
//            }
//        });

        m_map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                if (marker.getTitle().equals(getString(R.string.title_message_received))) {
                    Firebase messageRef = new Firebase(Constants.FIREBASE_URL_RECEIVED_MESSAGES)
                            .child(senderEncodedEmail)
                            .child(messageMap.get(marker));
                    showMessage(messageRef);
                } else if (marker.getTitle().equals(getString(R.string.title_message_sent))) {
                    Firebase messageRef = new Firebase(Constants.FIREBASE_URL_SENT_MESSAGES)
                            .child(senderEncodedEmail)
                            .child(messageMap.get(marker));
                    showMessage(messageRef);
                } else if (marker.getTitle().equals(getString(R.string.title_leave_message))) {
                    flyTo(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude),
                            Constants.MAP_ZOOM_LEVEL_CLOSE, Constants.MAP_FLY_TIME_SEC_FAST);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(MapMainActivity.this, WriteMessageActivity.class);
                            intent.putExtra(Constants.KEY_SENDER_ENCODED_EMAIL, senderEncodedEmail);
                            intent.putExtra(Constants.FIREBASE_PROPERTY_LATITUDE, marker.getPosition().latitude);
                            intent.putExtra(Constants.FIREBASE_PROPERTY_LONGITUDE, marker.getPosition().longitude);
                            startActivity(intent);
                        }
                    }, 1000);
                }
            }
        });

        LatLng newYork = new LatLng(32.3119443, 35.0178292);
        CameraPosition target = CameraPosition.builder().target(newYork).zoom(Constants.MAP_ZOOM_LEVEL_FAR).build();
        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));
    }

    private void showMessage(Firebase messageRef) {
        messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    Intent showMessageIntent = new Intent(MapMainActivity.this, ShowMessageActivity.class);
                    showMessageIntent.putExtra(Constants.KEY_MESSAGE_OBJECT, message);
                    startActivity(showMessageIntent);
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

        getMessages(Constants.FIREBASE_URL_SENT_MESSAGES,
                R.drawable.ic_sent_message, getString(R.string.title_message_sent));

        getMessages(Constants.FIREBASE_URL_RECEIVED_MESSAGES,
                R.drawable.ic_received_message, getString(R.string.title_message_received));
    }

    private void getMessages(final String url, final int iconRecourse, final String title) {
        Firebase messagesRef = new Firebase(url).child(senderEncodedEmail);
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                while (children.iterator().hasNext()) {
                    DataSnapshot snapshot = children.iterator().next();
                    Message message = snapshot.getValue(Message.class);
                    LatLng latLng = new LatLng(message.getLocation().getLatitude(),
                            message.getLocation().getLongitude());
                    Marker marker = m_map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(title)
                            .icon(BitmapDescriptorFactory.fromResource(iconRecourse)));
                    messageMap.put(marker, snapshot.getKey());
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
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Constants.PLACE_AUTOCOMPLETE_RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                createMarker(place.getLatLng());
                flyTo(place.getLatLng(), Constants.MAP_ZOOM_LEVEL_FAR, Constants.MAP_FLY_TIME_SEC_SLOW);
                Log.i(TAG, "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == Constants.PLACE_AUTOCOMPLETE_RESULT_CANCELED) {
                Log.i(TAG, "cancelled");
            }
        }
    }

    private void flyTo(LatLng latLng, int zoom, int durationSec) {
        m_map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
                .target(latLng)
                .zoom(zoom)
                .build()), durationSec * 1000, null);
    }

    private void createMarker(final LatLng latLng) {
        if (currentMarker != null) {
            currentMarker.remove();
        }
        currentMarker = m_map.addMarker(new MarkerOptions()
                .position(latLng)
                .title(getString(R.string.title_leave_message))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_sent_message)));
        currentMarker.showInfoWindow();
    }

}