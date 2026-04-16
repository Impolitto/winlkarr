package com.winlkar.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.winlkar.app.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(v -> finish());

        binding.loginButton.setOnClickListener(v -> {
            String adminId = binding.adminIdInput.getText() != null 
                ? binding.adminIdInput.getText().toString().trim() : "";
            String password = binding.passwordInput.getText() != null 
                ? binding.passwordInput.getText().toString().trim() : "";

            android.util.Log.d("LoginActivity", "Login attempt: ID=" + adminId + " PW=" + password);

            if ("admin".equalsIgnoreCase(adminId) && "admin123".equals(password)) {
                Intent intent = new Intent(this, AdminActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid credentials. Use admin/admin123", Toast.LENGTH_LONG).show();
            }
        });
    }
}
