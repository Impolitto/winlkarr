package com.winlkar.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.winlkar.app.databinding.ActivityAdminBinding;
import com.winlkar.app.databinding.ItemActiveTripBinding;
import com.winlkar.app.databinding.ItemBusBinding;
import com.winlkar.app.databinding.ItemDriverBinding;
import com.winlkar.app.databinding.ItemFeedbackBinding;
import com.winlkar.app.model.ActiveTrip;
import com.winlkar.app.model.Bus;
import com.winlkar.app.model.Driver;
import com.winlkar.app.model.Feedback;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private DatabaseReference activeTripsRef;
    private DatabaseReference busesRef;
    private DatabaseReference driversRef;
    private TripAdapter tripAdapter;
    private BusAdapter busAdapter;
    private DriverAdapter driverAdapter;
    private List<ActiveTrip> tripList = new ArrayList<>();
    private List<Bus> busList = new ArrayList<>();
    private List<Driver> driverList = new ArrayList<>();
    private FeedbackAdapter feedbackAdapter;
    private List<Feedback> feedbackList = new ArrayList<>();
    private DatabaseReference feedbacksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
            Toast.makeText(this, "Firebase not initialized. Check google-services.json", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        activeTripsRef = FirebaseDatabase.getInstance().getReference("activeTrips");
        busesRef = FirebaseDatabase.getInstance().getReference("buses");
        driversRef = FirebaseDatabase.getInstance().getReference("drivers");
        feedbacksRef = FirebaseDatabase.getInstance().getReference("feedbacks");

        setupRecyclerView();
        setupTabs();
        loadActiveTrips();
        loadBuses();
        loadDrivers();
        loadFeedbacks();

        binding.addBusButton.setOnClickListener(v -> showAddBusDialog());
        binding.addDriverButton.setOnClickListener(v -> showAddDriverDialog());
    }

    private void setupTabs() {
        binding.tabs.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                binding.tripsContainer.setVisibility(tab.getPosition() == 0 ? View.VISIBLE : View.GONE);
                binding.busesContainer.setVisibility(tab.getPosition() == 1 ? View.VISIBLE : View.GONE);
                binding.driversContainer.setVisibility(tab.getPosition() == 2 ? View.VISIBLE : View.GONE);
                binding.reportsContainer.setVisibility(tab.getPosition() == 3 ? View.VISIBLE : View.GONE);
                
                updateEmptyState();
            }

            @Override
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
        });
    }

    private void updateEmptyState() {
        int position = binding.tabs.getSelectedTabPosition();
        if (position == 0) {
            binding.emptyText.setVisibility(tripList.isEmpty() ? View.VISIBLE : View.GONE);
            binding.emptyText.setText("No active trips found");
        } else if (position == 1) {
            binding.emptyText.setVisibility(busList.isEmpty() ? View.VISIBLE : View.GONE);
            binding.emptyText.setText("No authorized buses found");
        } else if (position == 2) {
            binding.emptyText.setVisibility(driverList.isEmpty() ? View.VISIBLE : View.GONE);
            binding.emptyText.setText("No driver accounts found");
        } else {
            binding.emptyText.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        tripAdapter = new TripAdapter(tripList, busId -> {
            activeTripsRef.child(busId).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Trip " + busId + " removed", Toast.LENGTH_SHORT).show());
        });
        binding.adminRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.adminRecyclerView.setAdapter(tripAdapter);

        busAdapter = new BusAdapter(busList, busId -> {
            busesRef.child(busId).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Bus " + busId + " removed from authorized list", Toast.LENGTH_SHORT).show());
        });
        binding.busesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.busesRecyclerView.setAdapter(busAdapter);

        driverAdapter = new DriverAdapter(driverList, driverId -> {
            driversRef.child(driverId).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Driver account " + driverId + " removed", Toast.LENGTH_SHORT).show());
        });
        binding.driversRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.driversRecyclerView.setAdapter(driverAdapter);

        feedbackAdapter = new FeedbackAdapter(feedbackList, feedbackId -> {
            feedbacksRef.child(feedbackId).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Feedback removed", Toast.LENGTH_SHORT).show());
        });
        binding.feedbacksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.feedbacksRecyclerView.setAdapter(feedbackAdapter);
    }

    private void loadActiveTrips() {
        activeTripsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tripList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    ActiveTrip trip = postSnapshot.getValue(ActiveTrip.class);
                    if (trip != null) {
                        tripList.add(trip);
                    }
                }
                tripAdapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Failed to load trips", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBuses() {
        busesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                busList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Bus bus = postSnapshot.getValue(Bus.class);
                    if (bus != null) {
                        busList.add(bus);
                    }
                }
                busAdapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Failed to load buses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDrivers() {
        driverList.clear();
        driversRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                driverList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Driver driver = postSnapshot.getValue(Driver.class);
                    if (driver != null) {
                        driverList.add(driver);
                    }
                }
                driverAdapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Failed to load drivers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFeedbacks() {
        feedbacksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                feedbackList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Feedback feedback = postSnapshot.getValue(Feedback.class);
                    if (feedback != null) {
                        feedbackList.add(0, feedback); // Newest first
                    }
                }
                feedbackAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Failed to load feedbacks", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddBusDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_bus, null);
        EditText idInput = dialogView.findViewById(R.id.busIdInput);
        EditText plateInput = dialogView.findViewById(R.id.plateInput);
        EditText modelInput = dialogView.findViewById(R.id.modelInput);

        new AlertDialog.Builder(this)
                .setTitle("Add Authorized Bus")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String id = idInput.getText().toString().trim();
                    String plate = plateInput.getText().toString().trim();
                    String model = modelInput.getText().toString().trim();

                    if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(plate)) {
                        Bus newBus = new Bus(id, plate, model);
                        busesRef.child(id).setValue(newBus)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Bus added successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add bus", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "ID and Plate Number are required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddDriverDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_driver, null);
        EditText usernameInput = dialogView.findViewById(R.id.driverIdInput);
        EditText nameInput = dialogView.findViewById(R.id.driverNameInput);
        EditText passwordInput = dialogView.findViewById(R.id.driverPasswordInput);
        EditText busIdInput = dialogView.findViewById(R.id.assignedBusIdInput);

        new AlertDialog.Builder(this)
                .setTitle("Add Driver Account")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String username = usernameInput.getText().toString().trim();
                    String name = nameInput.getText().toString().trim();
                    String password = passwordInput.getText().toString().trim();
                    String busId = busIdInput.getText().toString().trim();

                    if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)) {
                        Driver newDriver = new Driver(username, name, password, busId);
                        driversRef.child(username).setValue(newDriver)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Driver added successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add driver", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Username, Name and Password are required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
        private List<ActiveTrip> trips;
        private OnDeleteClickListener listener;

        interface OnDeleteClickListener {
            void onDelete(String busId);
        }

        TripAdapter(List<ActiveTrip> trips, OnDeleteClickListener listener) {
            this.trips = trips;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemActiveTripBinding itemBinding = ItemActiveTripBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ActiveTrip trip = trips.get(position);
            holder.binding.busIdText.setText("Bus ID: " + trip.getBusId());
            holder.binding.routeText.setText("Route: " + (trip.getRouteDescription() != null ? trip.getRouteDescription() : "N/A"));
            holder.binding.deleteButton.setOnClickListener(v -> listener.onDelete(trip.getBusId()));
        }

        @Override
        public int getItemCount() {
            return trips.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ItemActiveTripBinding binding;
            ViewHolder(ItemActiveTripBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    private static class BusAdapter extends RecyclerView.Adapter<BusAdapter.ViewHolder> {
        private List<Bus> buses;
        private OnDeleteClickListener listener;

        interface OnDeleteClickListener {
            void onDelete(String busId);
        }

        BusAdapter(List<Bus> buses, OnDeleteClickListener listener) {
            this.buses = buses;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemBusBinding itemBinding = ItemBusBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Bus bus = buses.get(position);
            holder.binding.busIdText.setText("Bus ID: " + bus.getBusId());
            holder.binding.plateNumberText.setText("Plate: " + bus.getPlateNumber());
            holder.binding.modelText.setText("Model: " + (bus.getModel() != null ? bus.getModel() : "N/A"));
            holder.binding.deleteButton.setOnClickListener(v -> listener.onDelete(bus.getBusId()));
        }

        @Override
        public int getItemCount() {
            return buses.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ItemBusBinding binding;
            ViewHolder(ItemBusBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    private static class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.ViewHolder> {
        private List<Driver> drivers;
        private OnDeleteClickListener listener;

        interface OnDeleteClickListener {
            void onDelete(String driverId);
        }

        DriverAdapter(List<Driver> drivers, OnDeleteClickListener listener) {
            this.drivers = drivers;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemDriverBinding itemBinding = ItemDriverBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Driver driver = drivers.get(position);
            holder.binding.driverNameText.setText(driver.getName());
            holder.binding.driverIdText.setText("User: " + driver.getUsername());
            holder.binding.assignedBusText.setText("Bus: " + (driver.getAssignedBusId() != null && !driver.getAssignedBusId().isEmpty() ? driver.getAssignedBusId() : "Not assigned"));
            holder.binding.deleteButton.setOnClickListener(v -> listener.onDelete(driver.getUsername()));
        }

        @Override
        public int getItemCount() {
            return drivers.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ItemDriverBinding binding;
            ViewHolder(ItemDriverBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    private static class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {
        private List<Feedback> feedbacks;
        private OnDeleteClickListener listener;

        interface OnDeleteClickListener {
            void onDelete(String feedbackId);
        }

        FeedbackAdapter(List<Feedback> feedbacks, OnDeleteClickListener listener) {
            this.feedbacks = feedbacks;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemFeedbackBinding itemBinding = ItemFeedbackBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Feedback feedback = feedbacks.get(position);
            holder.binding.feedbackDriverName.setText(feedback.getDriverName());
            holder.binding.feedbackMessage.setText(feedback.getMessage());
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault());
            holder.binding.feedbackTimestamp.setText(sdf.format(new java.util.Date(feedback.getTimestamp())));

            holder.binding.deleteFeedbackButton.setOnClickListener(v -> {
                if (feedback.getId() != null) {
                    listener.onDelete(feedback.getId());
                }
            });
        }

        @Override
        public int getItemCount() {
            return feedbacks.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ItemFeedbackBinding binding;
            ViewHolder(ItemFeedbackBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
