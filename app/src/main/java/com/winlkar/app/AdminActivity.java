package com.winlkar.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.winlkar.app.databinding.ActivityAdminBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminActivity extends AppCompatActivity {

    private static final String TAB_COMPLAINTS = "complaints";
    private static final String TAB_BUSES = "buses";
    private static final String TAB_DRIVERS = "drivers";

    private ActivityAdminBinding binding;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String authToken;
    private String baseUrl;
    private String currentTab = TAB_COMPLAINTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        baseUrl = getString(R.string.api_base_url);

        binding.loginButton.setOnClickListener(v -> performLogin());
        binding.tabComplaints.setOnClickListener(v -> switchTab(TAB_COMPLAINTS));
        binding.tabBuses.setOnClickListener(v -> switchTab(TAB_BUSES));
        binding.tabDrivers.setOnClickListener(v -> switchTab(TAB_DRIVERS));
        binding.refreshButton.setOnClickListener(v -> loadCurrentTab());
        binding.addButton.setOnClickListener(v -> showAddDialog());
        binding.deleteButton.setOnClickListener(v -> showDeleteDialog());
    }

    // ── Auth ─────────────────────────────────────────────────────────────────

    private void performLogin() {
        String email = binding.emailInput.getText() == null ? "" : binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText() == null ? "" : binding.passwordInput.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.loginButton.setEnabled(false);
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("password", password);
                JSONObject response = httpPost(baseUrl + "/auth/login", body.toString(), null);
                String token = response.getJSONObject("data").getString("token");
                String role = response.getJSONObject("data").getJSONObject("user").getString("role");
                if (!"admin".equals(role)) {
                    mainHandler.post(() -> {
                        binding.loginButton.setEnabled(true);
                        Toast.makeText(this, "Access denied: not an admin account", Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                authToken = token;
                mainHandler.post(() -> {
                    binding.loginButton.setEnabled(true);
                    showAdminPanel();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    binding.loginButton.setEnabled(true);
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showAdminPanel() {
        binding.loginPanel.setVisibility(View.GONE);
        binding.adminPanel.setVisibility(View.VISIBLE);
        switchTab(TAB_COMPLAINTS);
    }

    // ── Tab navigation ────────────────────────────────────────────────────────

    private void switchTab(String tab) {
        currentTab = tab;
        boolean isBusOrDriver = TAB_BUSES.equals(tab) || TAB_DRIVERS.equals(tab);
        binding.addButton.setVisibility(isBusOrDriver ? View.VISIBLE : View.GONE);
        binding.deleteButton.setVisibility(isBusOrDriver ? View.VISIBLE : View.GONE);
        loadCurrentTab();
    }

    private void loadCurrentTab() {
        binding.listContent.setText("Loading…");
        switch (currentTab) {
            case TAB_COMPLAINTS:
                loadComplaints();
                break;
            case TAB_BUSES:
                loadBuses();
                break;
            case TAB_DRIVERS:
                loadDrivers();
                break;
        }
    }

    // ── Complaints ────────────────────────────────────────────────────────────

    private void loadComplaints() {
        executor.execute(() -> {
            try {
                JSONObject response = httpGet(baseUrl + "/complaints", authToken);
                JSONArray items = response.getJSONArray("data");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < items.length(); i++) {
                    JSONObject c = items.getJSONObject(i);
                    sb.append("ID: ").append(c.getString("_id")).append("\n");
                    sb.append("Subject: ").append(c.optString("subject", "-")).append("\n");
                    sb.append("Message: ").append(c.optString("message", "-")).append("\n");
                    sb.append("Status: ").append(c.optString("status", "-")).append("\n");
                    sb.append("Type: ").append(c.optString("type", "-")).append("\n");
                    sb.append("Priority: ").append(c.optString("priority", "-")).append("\n");
                    sb.append("─────────────────────────\n");
                }
                String text = sb.length() == 0 ? "No reports found." : sb.toString();
                mainHandler.post(() -> binding.listContent.setText(text));
            } catch (Exception e) {
                mainHandler.post(() -> binding.listContent.setText("Error: " + e.getMessage()));
            }
        });
    }

    // ── Buses ─────────────────────────────────────────────────────────────────

    private void loadBuses() {
        executor.execute(() -> {
            try {
                JSONObject response = httpGet(baseUrl + "/buses", authToken);
                JSONArray items = response.getJSONArray("data");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < items.length(); i++) {
                    JSONObject b = items.getJSONObject(i);
                    sb.append("ID: ").append(b.getString("_id")).append("\n");
                    sb.append("Number: ").append(b.optString("busNumber", "-")).append("\n");
                    sb.append("Capacity: ").append(b.optInt("capacity", 0)).append("\n");
                    sb.append("─────────────────────────\n");
                }
                String text = sb.length() == 0 ? "No buses found." : sb.toString();
                mainHandler.post(() -> binding.listContent.setText(text));
            } catch (Exception e) {
                mainHandler.post(() -> binding.listContent.setText("Error: " + e.getMessage()));
            }
        });
    }

    private void showAddBusDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        EditText numberInput = new EditText(this);
        numberInput.setHint("Bus number (e.g. BUS-001)");
        EditText capacityInput = new EditText(this);
        capacityInput.setHint("Capacity (e.g. 40)");
        capacityInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(numberInput);
        layout.addView(capacityInput);

        new AlertDialog.Builder(this)
                .setTitle("Add Bus")
                .setView(layout)
                .setPositiveButton("Add", (d, w) -> {
                    String number = numberInput.getText().toString().trim();
                    String capacityStr = capacityInput.getText().toString().trim();
                    if (number.isEmpty()) {
                        Toast.makeText(this, "Bus number is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int capacity;
                    try {
                        capacity = capacityStr.isEmpty() ? 40 : Integer.parseInt(capacityStr);
                    } catch (NumberFormatException ex) {
                        Toast.makeText(this, "Capacity must be a number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    executor.execute(() -> {
                        try {
                            JSONObject body = new JSONObject();
                            body.put("busNumber", number);
                            body.put("capacity", capacity);
                            httpPost(baseUrl + "/buses", body.toString(), authToken);
                            mainHandler.post(() -> {
                                Toast.makeText(this, "Bus added", Toast.LENGTH_SHORT).show();
                                loadBuses();
                            });
                        } catch (Exception e) {
                            mainHandler.post(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteBusDialog() {
        EditText idInput = new EditText(this);
        idInput.setHint("Bus ID to delete");
        idInput.setPadding(48, 24, 48, 24);

        new AlertDialog.Builder(this)
                .setTitle("Delete Bus")
                .setMessage("Enter the ID of the bus to delete:")
                .setView(idInput)
                .setPositiveButton("Delete", (d, w) -> {
                    String id = idInput.getText().toString().trim();
                    if (id.isEmpty()) return;
                    executor.execute(() -> {
                        try {
                            httpDelete(baseUrl + "/buses/" + id, authToken);
                            mainHandler.post(() -> {
                                Toast.makeText(this, "Bus deleted", Toast.LENGTH_SHORT).show();
                                loadBuses();
                            });
                        } catch (Exception e) {
                            mainHandler.post(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Drivers ───────────────────────────────────────────────────────────────

    private void loadDrivers() {
        executor.execute(() -> {
            try {
                JSONObject response = httpGet(baseUrl + "/users", authToken);
                JSONArray users = response.getJSONArray("data");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < users.length(); i++) {
                    JSONObject u = users.getJSONObject(i);
                    if (!"driver".equals(u.optString("role", ""))) {
                        continue;
                    }
                    sb.append("ID: ").append(u.getString("_id")).append("\n");
                    sb.append("Name: ").append(u.optString("name", "-")).append("\n");
                    sb.append("Email: ").append(u.optString("email", "-")).append("\n");
                    sb.append("─────────────────────────\n");
                }
                String text = sb.length() == 0 ? "No drivers found." : sb.toString();
                mainHandler.post(() -> binding.listContent.setText(text));
            } catch (Exception e) {
                mainHandler.post(() -> binding.listContent.setText("Error: " + e.getMessage()));
            }
        });
    }

    private void showAddDriverDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        EditText nameInput = new EditText(this);
        nameInput.setHint("Full name");
        EditText emailInput = new EditText(this);
        emailInput.setHint("Email");
        emailInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText passwordInput = new EditText(this);
        passwordInput.setHint("Password (min 6 characters)");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(nameInput);
        layout.addView(emailInput);
        layout.addView(passwordInput);

        new AlertDialog.Builder(this)
                .setTitle("Add Driver")
                .setView(layout)
                .setPositiveButton("Add", (d, w) -> {
                    String name = nameInput.getText().toString().trim();
                    String email = emailInput.getText().toString().trim();
                    String password = passwordInput.getText().toString().trim();
                    if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    executor.execute(() -> {
                        try {
                            JSONObject body = new JSONObject();
                            body.put("name", name);
                            body.put("email", email);
                            body.put("password", password);
                            body.put("role", "driver");
                            httpPost(baseUrl + "/users", body.toString(), authToken);
                            mainHandler.post(() -> {
                                Toast.makeText(this, "Driver added", Toast.LENGTH_SHORT).show();
                                loadDrivers();
                            });
                        } catch (Exception e) {
                            mainHandler.post(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteDriverDialog() {
        EditText idInput = new EditText(this);
        idInput.setHint("Driver ID to delete");
        idInput.setPadding(48, 24, 48, 24);

        new AlertDialog.Builder(this)
                .setTitle("Delete Driver")
                .setMessage("Enter the ID of the driver to delete:")
                .setView(idInput)
                .setPositiveButton("Delete", (d, w) -> {
                    String id = idInput.getText().toString().trim();
                    if (id.isEmpty()) return;
                    executor.execute(() -> {
                        try {
                            httpDelete(baseUrl + "/users/" + id, authToken);
                            mainHandler.post(() -> {
                                Toast.makeText(this, "Driver deleted", Toast.LENGTH_SHORT).show();
                                loadDrivers();
                            });
                        } catch (Exception e) {
                            mainHandler.post(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Dialog dispatchers ────────────────────────────────────────────────────

    private void showAddDialog() {
        if (TAB_BUSES.equals(currentTab)) {
            showAddBusDialog();
        } else if (TAB_DRIVERS.equals(currentTab)) {
            showAddDriverDialog();
        }
    }

    private void showDeleteDialog() {
        if (TAB_BUSES.equals(currentTab)) {
            showDeleteBusDialog();
        } else if (TAB_DRIVERS.equals(currentTab)) {
            showDeleteDriverDialog();
        }
    }

    // ── HTTP helpers ──────────────────────────────────────────────────────────

    private JSONObject httpGet(String urlStr, String token) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        return readResponse(conn);
    }

    private JSONObject httpPost(String urlStr, String jsonBody, String token) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }
        return readResponse(conn);
    }

    private void httpDelete(String urlStr, String token) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Content-Type", "application/json");
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            JSONObject result = new JSONObject(sb.toString());
            throw new Exception(result.optString("message", "HTTP " + code));
        }
    }

    private JSONObject readResponse(HttpURLConnection conn) throws Exception {
        int code = conn.getResponseCode();
        java.io.InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        JSONObject result = new JSONObject(sb.toString());
        if (code < 200 || code >= 300) {
            throw new Exception(result.optString("message", "HTTP " + code));
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
