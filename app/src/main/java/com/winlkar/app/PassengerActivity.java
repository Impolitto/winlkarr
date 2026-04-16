package com.winlkar.app;

import android.os.Bundle;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;
import org.json.JSONArray;
import com.winlkar.app.databinding.ActivityPassengerBinding;
import com.winlkar.app.model.ActiveTrip;
import com.winlkar.app.model.Feedback;
import com.winlkar.app.model.Station;

import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
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

        binding.sendFeedbackButton.setOnClickListener(v -> showFeedbackDialog());
        binding.topFeedbackButton.setOnClickListener(v -> showFeedbackDialog());
        binding.searchStationButton.setOnClickListener(v -> showStationSearchDialog());
        binding.myLocationButton.setOnClickListener(v -> {
            if (googleMap != null) {
                LatLng sousseCenter = new LatLng(35.8256, 10.6084);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sousseCenter, 13f));
            }
        });

        applyEntryAnimations();
    }

    private void applyEntryAnimations() {
        Animation slideDown = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        slideDown.setDuration(800);
        // binding.topCard.startAnimation(slideDown); // Removed because topCard ID is missing in layout
    }

    private void showStationSearchDialog() {
        String[] stationNames = new String[stations.size()];
        for (int i = 0; i < stations.size(); i++) {
            stationNames[i] = stations.get(i).name;
        }

        new AlertDialog.Builder(this)
                .setTitle("Search Station")
                .setItems(stationNames, (dialog, which) -> {
                    Station selected = stations.get(which);
                    if (googleMap != null) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selected.location, 15f));
                        showStationDetails(selected);
                    }
                })
                .show();
    }

    private void showFeedbackDialog() {
        EditText feedbackInput = new EditText(this);
        feedbackInput.setHint("Describe the issue or give feedback...");
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        feedbackInput.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle("Report / Feedback")
                .setView(feedbackInput)
                .setPositiveButton("Send", (dialog, which) -> {
                    String message = feedbackInput.getText().toString().trim();
                    if (!message.isEmpty()) {
                        sendFeedback(message);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendFeedback(String message) {
        DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference("feedbacks");
        String feedbackId = feedbackRef.push().getKey();

        Feedback feedback = new Feedback(feedbackId, "Passenger", "Passenger", message, System.currentTimeMillis());

        if (feedbackId != null) {
            feedbackRef.child(feedbackId).setValue(feedback)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Feedback sent to admin", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to send feedback", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        /*
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
            if (!success) {
                Log.e("MapStyle", "Style parsing failed.");
            }
        } catch (android.content.res.Resources.NotFoundException e) {
            Log.e("MapStyle", "Can't find style. Error: ", e);
        }
        */

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Move zoom controls and Google logo to be visible above the bottom sheet and below top card
        int topPadding = (int) (100 * getResources().getDisplayMetrics().density);
        int bottomPadding = (int) (220 * getResources().getDisplayMetrics().density);
        googleMap.setPadding(0, topPadding, 0, bottomPadding);

        drawStations();
        preConfigureDemoTrip();
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
        binding.bottomSheetSubtitle.setText("Nearby buses at " + station.name);
        binding.busDetailsContainer.setVisibility(android.view.View.VISIBLE);
        binding.tripsList.removeAllViews();

        int foundCount = 0;
        for (ActiveTrip trip : activeTrips.values()) {
            boolean involvesStation = (trip.getRouteFrom() != null && trip.getRouteFrom().equals(station.name))
                    || (trip.getRouteTo() != null && trip.getRouteTo().equals(station.name));

            if (involvesStation) {
                foundCount++;
                addTripItem(trip, station.name);
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
        
        addTripItem(trip, null);

        BottomSheetBehavior.from(binding.bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void addTripItem(ActiveTrip trip, String referenceStation) {
        android.view.View itemView = getLayoutInflater().inflate(R.layout.item_trip, binding.tripsList, false);
        
        android.widget.TextView busIdTv = itemView.findViewById(R.id.tripBusId);
        android.widget.TextView destTv = itemView.findViewById(R.id.tripDestination);
        android.widget.TextView etaTv = itemView.findViewById(R.id.tripEta);
        android.widget.ImageView iconIv = itemView.findViewById(R.id.tripIcon);

        busIdTv.setText("Bus " + (trip.getBusId().length() > 8 ? trip.getBusId().substring(0, 8) : trip.getBusId()));
        
        if (referenceStation != null && referenceStation.equals(trip.getRouteTo())) {
            destTv.setText("From " + (trip.getRouteFrom() != null ? trip.getRouteFrom() : "Unknown"));
        } else {
            destTv.setText("To " + (trip.getRouteTo() != null ? trip.getRouteTo() : "Unknown"));
        }
        
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

        Bitmap stationIcon = getBitmapFromVectorDrawable(R.drawable.ic_bus_stop_small);

        for (Station station : stations) {
            MarkerOptions options = new MarkerOptions()
                    .position(station.location)
                    .title(station.name)
                    .snippet("Bus Stop");
            
            if (stationIcon != null) {
                options.icon(BitmapDescriptorFactory.fromBitmap(stationIcon));
            } else {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            }

            Marker marker = googleMap.addMarker(options);
            if (marker != null) {
                marker.setTag(station);
            }
        }
    }

    private Bitmap getBitmapFromVectorDrawable(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(this, drawableId);
        if (drawable == null) return null;

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
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

        PolylineOptions options = new PolylineOptions()
                .color(android.graphics.Color.parseColor("#2979FF")) // Uber Blue
                .width(12)
                .startCap(new RoundCap())
                .endCap(new RoundCap())
                .jointType(JointType.ROUND);

        // If it's the demo trip, use the realistic street path
        if ("DEMO-BUS-7".equals(trip.getBusId()) && !demoPath.isEmpty()) {
            options.addAll(demoPath);
        } else {
            Station start = findStationByName(trip.getRouteFrom());
            Station end = findStationByName(trip.getRouteTo());
            if (start != null && end != null) {
                options.add(start.location, end.location);
            }
        }

        if (!options.getPoints().isEmpty()) {
            currentRoutePolyline = googleMap.addPolyline(options);
            
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng p : options.getPoints()) builder.include(p);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
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
            Bitmap busIcon = getBitmapFromVectorDrawable(R.drawable.ic_bus_top_view);
            MarkerOptions options = new MarkerOptions()
                    .position(newPosition)
                    .title("Bus " + trip.getBusId())
                    .snippet(snippet)
                    .zIndex(10.0f);

            if (busIcon != null) {
                options.icon(BitmapDescriptorFactory.fromBitmap(busIcon));
                options.anchor(0.5f, 0.5f);
                options.flat(true);
            } else {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            }

            Marker marker = googleMap.addMarker(options);
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

    private final List<LatLng> demoPath = new ArrayList<>();

    private void preConfigureDemoTrip() {
        // Define a realistic street path along the coast (N1/Corniche)
        demoPath.clear();
        demoPath.add(new LatLng(35.8282, 10.6358)); // Sousse Centrale
        demoPath.add(new LatLng(35.8324, 10.6318)); // Trocadero
        demoPath.add(new LatLng(35.8385, 10.6335)); // Corniche Start
        demoPath.add(new LatLng(35.8480, 10.6250)); // Boujaafar
        demoPath.add(new LatLng(35.8580, 10.6120)); // Hammam Sousse Bridge
        demoPath.add(new LatLng(35.8750, 10.6020)); // Menchia Area
        demoPath.add(new LatLng(35.8880, 10.5990)); // Kantaoui Entrance
        demoPath.add(new LatLng(35.8947, 10.5982)); // Port El Kantaoui

        String busId = "DEMO-BUS-7";
        LatLng startLoc = demoPath.get(0);
        
        ActiveTrip demoTrip = new ActiveTrip();
        demoTrip.setBusId(busId);
        demoTrip.setRouteFrom("Sousse Centrale");
        demoTrip.setRouteTo("Port El Kantaoui");
        demoTrip.setRouteDescription("Sousse Centrale -> Port El Kantaoui");
        demoTrip.setLat(startLoc.latitude);
        demoTrip.setLng(startLoc.longitude);
        demoTrip.setActive(true);

        activeTrips.put(busId, demoTrip);

        if (googleMap != null) {
            Bitmap busIcon = getBitmapFromVectorDrawable(R.drawable.ic_bus_top_view);
            MarkerOptions options = new MarkerOptions()
                    .position(startLoc)
                    .title("Bus " + busId)
                    .snippet("Route: " + demoTrip.getRouteDescription() + "\nStatus: DEMO")
                    .zIndex(10.0f);
            
            if (busIcon != null) {
                options.icon(BitmapDescriptorFactory.fromBitmap(busIcon));
                options.anchor(0.5f, 0.5f);
                options.flat(true);
            }

            Marker marker = googleMap.addMarker(options);
            if (marker != null) {
                marker.setTag(busId);
                busMarkers.put(busId, marker);
                startDemoAnimationAlongPath(marker, demoPath);
            }
        }
    }

    private void startDemoAnimationAlongPath(Marker marker, List<LatLng> path) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, path.size() - 1);
        animator.setDuration(60000); // 1 minute trip
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            int index = (int) fraction;
            float nextFraction = fraction - index;

            if (index < path.size() - 1) {
                LatLng start = path.get(index);
                LatLng end = path.get(index + 1);

                double lat = start.latitude + (end.latitude - start.latitude) * nextFraction;
                double lng = start.longitude + (end.longitude - start.longitude) * nextFraction;
                LatLng newPos = new LatLng(lat, lng);

                marker.setPosition(newPos);
                
                // Calculate bearing to rotate the bus correctly
                float bearing = getBearing(start, end);
                // Adjust rotation because our bus icon "front" is on the right side (90 deg)
                marker.setRotation(bearing - 90);

                ActiveTrip trip = activeTrips.get("DEMO-BUS-7");
                if (trip != null) {
                    trip.setLat(lat);
                    trip.setLng(lng);
                }
            }
        });
        animator.start();
    }

    private float getBearing(LatLng start, LatLng end) {
        double lat1 = Math.toRadians(start.latitude);
        double lon1 = Math.toRadians(start.longitude);
        double lat2 = Math.toRadians(end.latitude);
        double lon2 = Math.toRadians(end.longitude);

        double dLon = lon2 - lon1;
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        return (float) ((Math.toDegrees(Math.atan2(y, x)) + 360) % 360);
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
