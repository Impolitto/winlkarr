package com.winlkar.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.winlkar.app.model.ActiveTrip;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private DatabaseReference activeTripsRef;
    private TripAdapter adapter;
    private List<ActiveTrip> tripList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activeTripsRef = FirebaseDatabase.getInstance().getReference("activeTrips");

        setupRecyclerView();
        loadActiveTrips();

        binding.addBusButton.setOnClickListener(v -> {
            Toast.makeText(this, "Feature to add authorized buses coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        adapter = new TripAdapter(tripList, busId -> {
            activeTripsRef.child(busId).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Bus " + busId + " removed", Toast.LENGTH_SHORT).show());
        });
        binding.adminRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.adminRecyclerView.setAdapter(adapter);
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
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Failed to load trips", Toast.LENGTH_SHORT).show();
            }
        });
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
            holder.binding.routeText.setText("Route: " + trip.getRouteDescription());
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
}
