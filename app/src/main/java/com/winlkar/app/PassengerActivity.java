package com.winlkar.app;

import android.os.Bundle;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;
import org.json.JSONArray;
import com.winlkar.app.databinding.ActivityPassengerBinding;
import com.winlkar.app.model.ActiveTrip;
import com.winlkar.app.model.Station;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PassengerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityPassengerBinding binding;
    private GoogleMap googleMap;

    private DatabaseReference activeTripsRef;
    private ChildEventListener tripsListener;

    private final Map<String, Marker> busMarkers = new HashMap<>();
    private final Map<String, ActiveTrip> activeTrips = new HashMap<>();
    private final List<Station> stations = new ArrayList<>();
    private Polyline currentRoutePolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPassengerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeStations();

        if (!FirebaseApp.getApps(this).isEmpty()) {
            activeTripsRef = FirebaseDatabase.getInstance().getReference("activeTrips");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.passengerMapContainer);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Move zoom controls and Google logo to be visible above the bottom sheet and below top card
        int topPadding = (int) (100 * getResources().getDisplayMetrics().density);
        int bottomPadding = (int) (220 * getResources().getDisplayMetrics().density);
        googleMap.setPadding(0, topPadding, 0, bottomPadding);

        drawStations();
        attachTripsRealtimeListener();

        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof Station) {
                showStationDetails((Station) tag);
            } else if (tag instanceof String) {
                // Bus IDs are stored as tags
                showBusDetails((String) tag);
            }
            return false;
        });

        LatLng defaultCenter = new LatLng(35.8256, 10.6084);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultCenter, 12f));
    }

    private void showStationDetails(Station station) {
        binding.bottomSheetTitle.setText(station.name);
        binding.bottomSheetSubtitle.setText("Nearby buses to " + station.name);
        binding.busDetailsContainer.setVisibility(android.view.View.VISIBLE);
        binding.tripsList.removeAllViews();

        int foundCount = 0;
        for (ActiveTrip trip : activeTrips.values()) {
            boolean involvesStation = (trip.getRouteFrom() != null && trip.getRouteFrom().equals(station.name))
                    || (trip.getRouteTo() != null && trip.getRouteTo().equals(station.name));

            if (involvesStation) {
                foundCount++;
                addTripItem(trip);
            }
        }

        if (foundCount == 0) {
            binding.bottomSheetSubtitle.setText("No active trips found for this station.");
        }

        BottomSheetBehavior.from(binding.bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void showBusDetails(String busId) {
        ActiveTrip trip = activeTrips.get(busId);
        if (trip == null) return;

        drawRouteLine(trip);

        binding.bottomSheetTitle.setText("Bus Details");
        binding.bottomSheetSubtitle.setText("Real-time tracking");
        binding.busDetailsContainer.setVisibility(android.view.View.VISIBLE);
        binding.tripsList.removeAllViews();
        
        addTripItem(trip);

        BottomSheetBehavior.from(binding.bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void addTripItem(ActiveTrip trip) {
        android.view.View itemView = getLayoutInflater().inflate(R.layout.item_trip, binding.tripsList, false);
        
        android.widget.TextView busIdTv = itemView.findViewById(R.id.tripBusId);
        android.widget.TextView destTv = itemView.findViewById(R.id.tripDestination);
        android.widget.TextView etaTv = itemView.findViewById(R.id.tripEta);
        android.widget.ImageView iconIv = itemView.findViewById(R.id.tripIcon);

        busIdTv.setText("Bus " + (trip.getBusId().length() > 8 ? trip.getBusId().substring(0, 8) : trip.getBusId()));
        destTv.setText("To " + (trip.getRouteTo() != null ? trip.getRouteTo() : "Unknown"));
        
        // Calculate ETA
        Marker busMarker = busMarkers.get(trip.getBusId());
        if (busMarker != null) {
            Station next = findNearestStation(busMarker.getPosition());
            if (next != null) {
                double dist = distanceKm(busMarker.getPosition(), next.location);
                if (dist > 12000) { // Only show off-route if literally on the other side of the world
                    etaTv.setText("Off-route");
                } else {
                    int eta = (int) Math.max(1, Math.round((dist / 30.0) * 60.0));
                    etaTv.setText(eta + " min");
                }
            }
        }

        itemView.setOnClickListener(v -> {
            drawRouteLine(trip);
            Marker m = busMarkers.get(trip.getBusId());
            if (m != null) googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 15f));
        });

        binding.tripsList.addView(itemView);
    }

    private void initializeStations() {
        stations.addAll(Station.getDefaultStations());
    }

    private void drawStations() {
        if (googleMap == null) {
            return;
        }

        for (Station station : stations) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(station.location)
                    .title(station.name)
                    .snippet("Station")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            if (marker != null) {
                marker.setTag(station);
            }
        }
    }

    private void attachTripsRealtimeListener() {
        if (activeTripsRef == null) {
            return;
        }

        tripsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                upsertBusMarker(snapshot);
                updateActiveBusesCount();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                upsertBusMarker(snapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String busId = snapshot.getKey();
                activeTrips.remove(busId);
                Marker marker = busMarkers.remove(busId);
                if (marker != null) {
                    marker.remove();
                }
                updateActiveBusesCount();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        activeTripsRef.addChildEventListener(tripsListener);
    }

    private void drawRouteLine(ActiveTrip trip) {
        if (googleMap == null) return;
        if (currentRoutePolyline != null) currentRoutePolyline.remove();

        Station start = findStationByName(trip.getRouteFrom());
        Station end = findStationByName(trip.getRouteTo());

        if (start != null && end != null) {
            // For now, we draw a higher-quality line. 
            // In a production environment, you would call the Directions API here to get street points.
            // I've optimized the visual style to be smooth and rounded.
            currentRoutePolyline = googleMap.addPolyline(new PolylineOptions()
                    .add(start.location, end.location)
                    .color(android.graphics.Color.parseColor("#2979FF")) // Uber Blue
                    .width(12)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap())
                    .jointType(JointType.ROUND));
            
            // If the bus is active, let's zoom to the whole path
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(start.location)
                    .include(end.location)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
        }
    }

    private Station findStationByName(String name) {
        if (name == null) return null;
        for (Station s : stations) {
            if (s.name.equals(name)) return s;
        }
        return null;
    }

    private void upsertBusMarker(DataSnapshot snapshot) {
        if (googleMap == null) {
            return;
        }

        ActiveTrip trip = snapshot.getValue(ActiveTrip.class);
        if (trip == null || trip.getBusId() == null || trip.getLat() == null || trip.getLng() == null) {
            return;
        }

        LatLng newPosition = new LatLng(trip.getLat(), trip.getLng());
        
        // --- NEW FILTER ---
        // Ignore any bus that is too far from Sousse (e.g. stuck in California)
        LatLng sousseCenter = new LatLng(35.8256, 10.6084);
        if (distanceKm(newPosition, sousseCenter) > 100) {
            // Remove the marker if it was already there but moved out of range
            Marker existing = busMarkers.remove(trip.getBusId());
            if (existing != null) existing.remove();
            activeTrips.remove(trip.getBusId());
            return;
        }
        // ------------------

        activeTrips.put(trip.getBusId(), trip);
        String snippet = buildBusSnippet(trip, newPosition);

        Marker existing = busMarkers.get(trip.getBusId());
        if (existing == null) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(newPosition)
                    .title("Bus " + trip.getBusId())
                    .snippet(snippet)
                    .zIndex(10.0f)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

            if (marker != null) {
                marker.setTag(trip.getBusId());
                busMarkers.put(trip.getBusId(), marker);
            }
        } else {
            animateMarker(existing, newPosition);
            existing.setSnippet(snippet);
        }

        zoomToFitAllBuses();
    }

    private void animateMarker(final Marker marker, final LatLng toPosition) {
        final LatLng startPosition = marker.getPosition();
        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(1000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            float v = animation.getAnimatedFraction();
            double lng = v * toPosition.longitude + (1 - v) * startPosition.longitude;
            double lat = v * toPosition.latitude + (1 - v) * startPosition.latitude;
            marker.setPosition(new LatLng(lat, lng));
        });
        valueAnimator.start();
    }

    private void zoomToFitAllBuses() {
        if (googleMap == null || busMarkers.isEmpty()) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean hasLocalBuses = false;
        LatLng sousseCenter = new LatLng(35.8256, 10.6084);

        for (Marker marker : busMarkers.values()) {
            // Only include buses within 50km of Sousse to avoid zooming out to California
            if (distanceKm(marker.getPosition(), sousseCenter) < 50) {
                builder.include(marker.getPosition());
                hasLocalBuses = true;
            }
        }

        builder.include(sousseCenter);

        int padding = 150; 
        if (hasLocalBuses) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));
        } else {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sousseCenter, 13f));
        }
    }

    private String buildBusSnippet(ActiveTrip trip, LatLng busPosition) {
        String route = trip.getRouteDescription() == null ? "Unknown route" : trip.getRouteDescription();
        Station nextStation = findNearestStation(busPosition);

        if (nextStation == null) {
            return "ID: " + trip.getBusId() + "\nRoute: " + route;
        }

        double distanceKm = distanceKm(busPosition, nextStation.location);
        int etaMinutes = (int) Math.round((distanceKm / 30.0) * 60.0);
        if (etaMinutes < 1) {
            etaMinutes = 1;
        }

        return String.format(Locale.US,
                "ID: %s\nRoute: %s\nNext: %s (~%d min)",
                trip.getBusId(),
                route,
                nextStation.name,
                etaMinutes);
    }

    private Station findNearestStation(LatLng position) {
        Station nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Station station : stations) {
            double dist = distanceKm(position, station.location);
            if (dist < minDistance) {
                minDistance = dist;
                nearest = station;
            }
        }

        return nearest;
    }

    private double distanceKm(LatLng a, LatLng b) {
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(b.latitude - a.latitude);
        double dLon = Math.toRadians(b.longitude - a.longitude);

        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a.latitude))
                * Math.cos(Math.toRadians(b.latitude))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
        return earthRadiusKm * c;
    }

    private void updateActiveBusesCount() {
        int count = busMarkers.size();
        if (count == 0) {
            binding.headerText.setText("No active buses nearby");
        } else if (count == 1) {
            binding.headerText.setText("1 active bus nearby");
        } else {
            binding.headerText.setText(count + " active buses nearby");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activeTripsRef != null && tripsListener != null) {
            activeTripsRef.removeEventListener(tripsListener);
        }
    }
}
