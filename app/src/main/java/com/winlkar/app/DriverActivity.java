package com.winlkar.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.winlkar.app.databinding.ActivityDriverBinding;
import com.winlkar.app.model.Station;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class DriverActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "DriverActivity";
    private static final int LOCATION_PERMISSION_CODE = 101;

    private ActivityDriverBinding binding;
    private GoogleMap googleMap;
    private Marker driverMarker;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private DatabaseReference activeTripsRef;

    private Place startPlace;
    private Place endPlace;
    private String currentBusId;
    private boolean isTripActive;
    private boolean firebaseReady;
    private List<Station> stations = new ArrayList<>();
    private boolean pickingStart = false;
    private boolean pickingEnd = false;
    private String selectedStartName;
    private String selectedEndName;
    private LatLng selectedStartLatLng;
    private LatLng selectedEndLatLng;
    private Marker startMarker;
    private Marker endMarker;

    // Use standard StartActivityForResult contract as the custom Places contract is not available
    private final ActivityResultLauncher<Intent> startPlaceLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    startPlace = Autocomplete.getPlaceFromIntent(result.getData());
                    refreshRouteSummary();
                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                    Status status = Autocomplete.getStatusFromIntent(result.getData());
                    handleAutocompleteError(status, true);
                }
            });

    private final ActivityResultLauncher<Intent> endPlaceLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    endPlace = Autocomplete.getPlaceFromIntent(result.getData());
                    refreshRouteSummary();
                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                    Status status = Autocomplete.getStatusFromIntent(result.getData());
                    handleAutocompleteError(status, false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        stations = Station.getDefaultStations();
        initializeSdkClients();
        initializeMap();
        initializeUi();
    }

    private void initializeSdkClients() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.maps_api_key));
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        firebaseReady = !FirebaseApp.getApps(this).isEmpty();
        if (!firebaseReady) {
            Toast.makeText(this, "Firebase not configured. Add google-services.json.", Toast.LENGTH_LONG).show();
        } else {
            activeTripsRef = FirebaseDatabase.getInstance().getReference("activeTrips");
        }

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(1500)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location == null) {
                    return;
                }

                LatLng rawLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                
                // Smart Filter: If GPS is in California (10,000km away), ignore it and use the station loc
                LatLng sousseCenter = new LatLng(35.8256, 10.6084);
                double distanceToSousse = calculateDistance(rawLatLng, sousseCenter);
                
                LatLng filteredLatLng;
                if (distanceToSousse > 1000) { // If more than 1000km away, it's the emulator default
                    if (isTripActive) {
                        filteredLatLng = startPlace != null ? startPlace.getLatLng() : selectedStartLatLng;
                    } else {
                        // If not in a trip, just show them where they are but don't broadcast
                        updateDriverMarker(rawLatLng);
                        return; 
                    }
                } else {
                    filteredLatLng = rawLatLng;
                }

                if (filteredLatLng != null) {
                    updateDriverMarker(filteredLatLng);
                    if (isTripActive) {
                        publishLiveLocation(filteredLatLng);
                    }
                }
            }
        };
    }

    private double calculateDistance(LatLng a, LatLng b) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results);
        return results[0] / 1000.0; // km
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.driverMapContainer);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initializeUi() {
        binding.selectStartButton.setOnClickListener(v -> {
            pickingStart = true;
            pickingEnd = false;
            Toast.makeText(this, "Tap a station on the map to set Start", Toast.LENGTH_SHORT).show();
        });
        binding.selectEndButton.setOnClickListener(v -> {
            pickingEnd = true;
            pickingStart = false;
            Toast.makeText(this, "Tap a station on the map to set End", Toast.LENGTH_SHORT).show();
        });

        binding.startTripButton.setOnClickListener(v -> startTrip());
        binding.endTripButton.setOnClickListener(v -> endTrip());
    }

    private void openPlacePicker(boolean isStart) {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);

        if (isStart) {
            startPlaceLauncher.launch(intent);
        } else {
            endPlaceLauncher.launch(intent);
        }
    }

    private void handleAutocompleteError(Status status, boolean isStartPicker) {
        String pickerName = isStartPicker ? "Start" : "End";
        String rawMessage = status.getStatusMessage() == null ? "Unknown error" : status.getStatusMessage();
        String lowerMessage = rawMessage.toLowerCase(Locale.US);

        Log.e(TAG, "Autocomplete Error (" + pickerName + ") code=" + status.getStatusCode()
                + " (" + CommonStatusCodes.getStatusCodeString(status.getStatusCode()) + ")"
                + " message=" + rawMessage);

        if (lowerMessage.contains("legacy")) {
            Toast.makeText(this,
                    "Places is configured as legacy in Google Cloud. Enable Places API (New), "
                            + "keep Maps SDK for Android enabled, and allow this key to use both APIs.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Error: " + rawMessage, Toast.LENGTH_LONG).show();
    }

    private void refreshRouteSummary() {
        String from = startPlace != null ? startPlace.getName() : (selectedStartName != null ? selectedStartName : "?");
        String to = endPlace != null ? endPlace.getName() : (selectedEndName != null ? selectedEndName : "?");
        binding.routeSummaryText.setText(String.format(Locale.US, "Route: %s -> %s", from, to));
    }

    private void startTrip() {
        if (isTripActive) {
            return;
        }

        if (!firebaseReady) {
            Toast.makeText(this, "Firebase is required to start a trip", Toast.LENGTH_SHORT).show();
            return;
        }

        String from = startPlace != null ? startPlace.getName() : selectedStartName;
        String to = endPlace != null ? endPlace.getName() : selectedEndName;

        if (from == null || to == null) {
            Toast.makeText(this, "Please select start and end points", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasLocationPermission()) {
            requestLocationPermission();
            return;
        }

        zoomToFitRoute();

        String typedBusId = binding.busIdInput.getText() == null
                ? ""
                : binding.busIdInput.getText().toString().trim();

        currentBusId = typedBusId.isEmpty()
                ? "BUS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.US)
                : typedBusId;

        binding.busIdInput.setText(currentBusId);
        isTripActive = true;
        binding.startTripButton.setEnabled(false);
        binding.endTripButton.setEnabled(true);

        startLocationUpdates();

        // Broadcast initial location immediately so passengers see the bus right away
        LatLng initialLoc = startPlace != null ? startPlace.getLatLng() : selectedStartLatLng;
        if (initialLoc != null) {
            publishLiveLocation(initialLoc);
        }

        Toast.makeText(this, "Trip started for " + currentBusId, Toast.LENGTH_SHORT).show();
    }

    private void endTrip() {
        if (!isTripActive) {
            return;
        }

        stopLocationUpdates();
        if (activeTripsRef != null) {
            activeTripsRef.child(currentBusId).removeValue();
        }

        isTripActive = false;
        binding.startTripButton.setEnabled(true);
        binding.endTripButton.setEnabled(false);

        Toast.makeText(this, "Trip ended", Toast.LENGTH_SHORT).show();
    }

    private void publishLiveLocation(LatLng latLng) {
        if (activeTripsRef == null) {
            return;
        }

        String from = startPlace != null ? startPlace.getName() : selectedStartName;
        String to = endPlace != null ? endPlace.getName() : selectedEndName;

        Map<String, Object> payload = new HashMap<>();
        payload.put("busId", currentBusId);
        payload.put("routeFrom", from);
        payload.put("routeTo", to);
        payload.put("routeDescription", from + " -> " + to);
        payload.put("lat", latLng.latitude);
        payload.put("lng", latLng.longitude);
        payload.put("lastUpdated", System.currentTimeMillis());
        payload.put("active", true);

        activeTripsRef.child(currentBusId).setValue(payload);
    }

    private void zoomToFitRoute() {
        if (googleMap == null) return;

        com.google.android.gms.maps.model.LatLngBounds.Builder builder = new com.google.android.gms.maps.model.LatLngBounds.Builder();
        boolean hasPoints = false;

        LatLng start = startPlace != null ? startPlace.getLatLng() : selectedStartLatLng;
        LatLng end = endPlace != null ? endPlace.getLatLng() : selectedEndLatLng;

        if (start != null) { builder.include(start); hasPoints = true; }
        if (end != null) { builder.include(end); hasPoints = true; }
        if (driverMarker != null) { builder.include(driverMarker.getPosition()); hasPoints = true; }

        if (hasPoints) {
            int padding = 200; // offset from edges of the map in pixels
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));
        }
    }

    private void updateDriverMarker(LatLng latLng) {
        if (googleMap == null) {
            return;
        }

        if (driverMarker == null) {
            driverMarker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Current bus location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        } else {
            driverMarker.setPosition(latLng);
        }
        // Removed forced camera snap here to allow user to see the route
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                }
            }
            if (isTripActive) {
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Move zoom controls and Google logo to be visible below the top card
        int topPadding = (int) (280 * getResources().getDisplayMetrics().density);
        googleMap.setPadding(0, topPadding, 0, 0);

        if (hasLocationPermission()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            }
        } else {
            requestLocationPermission();
        }

        drawStations();

        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof Station) {
                Station station = (Station) tag;
                handleStationSelection(station);
                return true;
            }
            return false;
        });

        LatLng defaultCenter = new LatLng(35.8256, 10.6084);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultCenter, 12f));
    }

    private void drawStations() {
        if (googleMap == null) return;
        for (Station station : stations) {
            Marker m = googleMap.addMarker(new MarkerOptions()
                    .position(station.location)
                    .title(station.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            if (m != null) m.setTag(station);
        }
    }

    private void handleStationSelection(Station station) {
        if (pickingStart) {
            selectedStartName = station.name;
            selectedStartLatLng = station.location;
            startPlace = null; // Clear place picker selection if any
            if (startMarker != null) startMarker.remove();
            startMarker = googleMap.addMarker(new MarkerOptions()
                    .position(station.location)
                    .title("Start: " + station.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            pickingStart = false;
            Toast.makeText(this, "Start set to: " + station.name, Toast.LENGTH_SHORT).show();
        } else if (pickingEnd) {
            selectedEndName = station.name;
            selectedEndLatLng = station.location;
            endPlace = null; // Clear place picker selection if any
            if (endMarker != null) endMarker.remove();
            endMarker = googleMap.addMarker(new MarkerOptions()
                    .position(station.location)
                    .title("End: " + station.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            pickingEnd = false;
            Toast.makeText(this, "End set to: " + station.name, Toast.LENGTH_SHORT).show();
        }
        refreshRouteSummary();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isTripActive) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
}
