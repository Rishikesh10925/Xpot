package com.example.harshatrial;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class AddParkingActivity extends AppCompatActivity {

    public static final String EXTRA_PARKING_ID = "extra_parking_id";

    private EditText etParkingName, etAddress, etTotalSlots, etPricePerHour, etFeatures;
    private Button btnSaveParking;
    private DatabaseHelper dbHelper;
    private int ownerId;
    private int editParkingId = -1;
    private int currentAvailableSlots = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_parking);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        ownerId = sp.getInt("userId", -1);

        etParkingName = findViewById(R.id.etParkingName);
        etAddress = findViewById(R.id.etAddress);
        etTotalSlots = findViewById(R.id.etTotalSlots);
        etPricePerHour = findViewById(R.id.etPricePerHour);
        etFeatures = findViewById(R.id.etFeatures);
        btnSaveParking = findViewById(R.id.btnSaveParking);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        editParkingId = getIntent().getIntExtra(EXTRA_PARKING_ID, -1);
        if (editParkingId != -1) {
            btnSaveParking.setText(R.string.update_parking_location);
            preloadParking(editParkingId);
        }

        btnSaveParking.setOnClickListener(v -> {
            String name = etParkingName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String totalSlotsStr = etTotalSlots.getText().toString().trim();
            String priceStr = etPricePerHour.getText().toString().trim();
            String features = etFeatures.getText().toString().trim();

            if (name.isEmpty() || address.isEmpty() || totalSlotsStr.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int totalSlots = Integer.parseInt(totalSlotsStr);
            double pricePerHour = Double.parseDouble(priceStr);

            if (editParkingId != -1) {
                int safeAvailable = currentAvailableSlots == -1 ? totalSlots : currentAvailableSlots;
                int updated = dbHelper.updateParking(editParkingId, name, address, totalSlots, safeAvailable, features, pricePerHour);
                if (updated > 0) {
                    Toast.makeText(this, "Parking location updated!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                Toast.makeText(this, "Error updating parking", Toast.LENGTH_SHORT).show();
                return;
            }

            long result = dbHelper.addParking(ownerId, name, address, totalSlots, features, pricePerHour);
            if (result > 0) {
                Toast.makeText(this, "Parking location added!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error adding parking", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void preloadParking(int parkingId) {
        Cursor cursor = dbHelper.getParkingById(parkingId);
        if (cursor != null && cursor.moveToFirst()) {
            etParkingName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PARKING_NAME)));
            etAddress.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PARKING_ADDRESS)));
            etTotalSlots.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TOTAL_SLOTS))));
            currentAvailableSlots = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AVAILABLE_SLOTS));
            etPricePerHour.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PARKING_PRICE_PER_HOUR))));
            etFeatures.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PARKING_FEATURES)));
            cursor.close();
        }
    }
}
