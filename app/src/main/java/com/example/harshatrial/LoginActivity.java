package com.example.harshatrial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = dbHelper.loginUser(email, password);
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME));
                String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROLE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_STATUS));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PHONE));
                String vehicle = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_VEHICLE));

                if (role.equals("Admin") && !status.equals("Approved")) {
                    Toast.makeText(this, "Your account is pending approval", Toast.LENGTH_SHORT).show();
                } else {
                    saveSession(id, name, email, role, phone, vehicle);
                    navigateToDashboard(role);
                }
                cursor.close();
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void saveSession(int id, String name, String email, String role, String phone, String vehicle) {
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("userId", id);
        editor.putString("userName", name);
        editor.putString("userEmail", email);
        editor.putString("userRole", role);
        editor.putString("userPhone", phone == null ? "" : phone);
        editor.putString("userVehicle", vehicle == null ? "" : vehicle);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        switch (role) {
            case "Super Admin":
                intent = new Intent(this, SuperAdminDashboardActivity.class);
                break;
            case "Admin":
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            default:
                intent = new Intent(this, UserDashboardActivity.class);
                break;
        }
        startActivity(intent);
        finish();
    }
}
