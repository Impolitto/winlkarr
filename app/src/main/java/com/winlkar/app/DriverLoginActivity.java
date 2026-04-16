package com.winlkar.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.winlkar.app.databinding.ActivityDriverLoginBinding;

public class DriverLoginActivity extends AppCompatActivity {

    private ActivityDriverLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(v -> finish());

        binding.loginButton.setOnClickListener(v -> {
            String username = binding.usernameInput.getText() != null 
                ? binding.usernameInput.getText().toString().trim() : "";
            String password = binding.passwordInput.getText() != null 
                ? binding.passwordInput.getText().toString().trim() : "";

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("drivers")
                .child(username).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        com.winlkar.app.model.Driver driver = snapshot.getValue(com.winlkar.app.model.Driver.class);
                        if (driver != null && password.equals(driver.getPassword())) {
                            Intent intent = new Intent(DriverLoginActivity.this, DriverActivity.class);
                            intent.putExtra("DRIVER_ID", username); // Still passing as ID for internal logic
                            intent.putExtra("DRIVER_NAME", driver.getName());
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(DriverLoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                        Toast.makeText(DriverLoginActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                    }
                });
        });
    }
}
