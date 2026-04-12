package com.winlkar.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.winlkar.app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Ensure Firebase is initialized
        FirebaseApp.initializeApp(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.driverButton.setOnClickListener(v ->
                startActivity(new Intent(this, DriverActivity.class)));

        binding.passengerButton.setOnClickListener(v ->
                startActivity(new Intent(this, PassengerActivity.class)));

        binding.adminButton.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }
}
