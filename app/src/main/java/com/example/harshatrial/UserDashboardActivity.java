package com.example.harshatrial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class UserDashboardActivity extends AppCompatActivity {

    private RecyclerView rvAllParking;
    private DatabaseHelper dbHelper;
    private List<ParkingLocation> parkingList;
    private AdminDashboardActivity.ParkingAdapter adapter;
    private int userId;
    private TextView tvWelcomeUser;
    private TextView tvSummaryAvailableValue;
    private TextView tvSummaryLocationsValue;
    private View emptyStateUser;
    private LinearProgressIndicator progressUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = sp.getInt("userId", -1);
        String userName = sp.getString("userName", "User");

        rvAllParking = findViewById(R.id.rvAllParking);
        rvAllParking.setLayoutManager(new LinearLayoutManager(this));
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvSummaryAvailableValue = findViewById(R.id.tvSummaryAvailableValue);
        tvSummaryLocationsValue = findViewById(R.id.tvSummaryLocationsValue);
        emptyStateUser = findViewById(R.id.emptyStateUser);
        progressUser = findViewById(R.id.progressUser);

        tvWelcomeUser.setText(getString(R.string.hello_name, userName, getString(R.string.wave_emoji)));

        BottomNavigationView bottomNavUser = findViewById(R.id.bottomNavUser);
        bottomNavUser.setSelectedItemId(R.id.nav_user_home);
        bottomNavUser.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_user_home) {
                return true;
            }
            if (item.getItemId() == R.id.nav_user_bookings) {
                startActivity(new Intent(this, UserBookingHistoryActivity.class));
                return true;
            }
            startActivity(new Intent(this, UserProfileActivity.class));
            return true;
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            logout();
        });

        loadAllParking();
    }

    private void logout() {
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        sp.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void loadAllParking() {
        progressUser.setVisibility(View.VISIBLE);
        parkingList = new ArrayList<>();
        Cursor cursor = dbHelper.getAllParking();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                parkingList.add(new ParkingLocation(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PARKING_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_OWNER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PARKING_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PARKING_ADDRESS)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TOTAL_SLOTS)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AVAILABLE_SLOTS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PARKING_STATUS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PARKING_FEATURES)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PARKING_PRICE_PER_HOUR))
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter = new AdminDashboardActivity.ParkingAdapter(this, parkingList, false, userId);
        rvAllParking.setAdapter(adapter);

        int totalAvailable = 0;
        for (ParkingLocation parkingLocation : parkingList) {
            totalAvailable += parkingLocation.getAvailableSlots();
        }
        tvSummaryAvailableValue.setText(String.valueOf(totalAvailable));
        tvSummaryLocationsValue.setText(String.valueOf(parkingList.size()));
        emptyStateUser.setVisibility(parkingList.isEmpty() ? View.VISIBLE : View.GONE);
        progressUser.setVisibility(View.GONE);
    }
}
