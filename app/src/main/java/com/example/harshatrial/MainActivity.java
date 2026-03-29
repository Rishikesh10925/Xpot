package com.example.harshatrial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        boolean isLoggedIn = sp.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            String role = sp.getString("userRole", "");
            navigateToDashboard(role);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
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
