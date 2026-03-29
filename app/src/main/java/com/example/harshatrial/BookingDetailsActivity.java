package com.example.harshatrial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;

public class BookingDetailsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int userId;
    private int parkingId;
    private String parkingName;
    private String parkingAddress;
    private double parkingPrice;

    private String profileName = "";
    private String profilePhone = "";
    private String profileVehicle = "";

    private EditText etBookingName;
    private EditText etBookingPhone;
    private EditText etBookingVehicle;
    private EditText etBookingHours;
    private TextView tvBookingTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = sp.getInt("userId", -1);

        parkingId = getIntent().getIntExtra("parking_id", -1);
        parkingName = getIntent().getStringExtra("parking_name");
        parkingAddress = getIntent().getStringExtra("parking_address");
        parkingPrice = getIntent().getDoubleExtra("parking_price", 5.0);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ((TextView) findViewById(R.id.tvParkingNameDetail)).setText(parkingName);
        ((TextView) findViewById(R.id.tvParkingAddressDetail)).setText(parkingAddress);
        ((TextView) findViewById(R.id.tvPriceDetail)).setText(String.format(Locale.getDefault(), "$%.2f/hr", parkingPrice));

        etBookingName = findViewById(R.id.etBookingName);
        etBookingPhone = findViewById(R.id.etBookingPhone);
        etBookingVehicle = findViewById(R.id.etBookingVehicle);
        etBookingHours = findViewById(R.id.etBookingHours);
        tvBookingTotal = findViewById(R.id.tvBookingTotal);
        CheckBox cbBookForSelf = findViewById(R.id.cbBookForSelf);

        loadUserProfile();
        cbBookForSelf.setChecked(true);
        applyProfileValues(true);
        updateTotal();

        etBookingHours.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateTotal();
            }
        });

        cbBookForSelf.setOnCheckedChangeListener((buttonView, isChecked) -> applyProfileValues(isChecked));

        findViewById(R.id.btnConfirmBooking).setOnClickListener(v -> confirmBooking());
    }

    private void loadUserProfile() {
        Cursor cursor = dbHelper.getUserById(userId);
        if (cursor != null && cursor.moveToFirst()) {
            profileName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME));
            profilePhone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PHONE));
            profileVehicle = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_VEHICLE));
            cursor.close();
        }
    }

    private void applyProfileValues(boolean useProfile) {
        if (useProfile) {
            etBookingName.setText(profileName);
            etBookingPhone.setText(profilePhone);
            etBookingVehicle.setText(profileVehicle);
        }
    }

    private int getHours() {
        String hoursText = etBookingHours.getText().toString().trim();
        if (hoursText.isEmpty()) {
            return 1;
        }
        return Math.max(1, Integer.parseInt(hoursText));
    }

    private void updateTotal() {
        int hours = getHours();
        double total = hours * parkingPrice;
        tvBookingTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
    }

    private void confirmBooking() {
        String name = etBookingName.getText().toString().trim();
        String phone = etBookingPhone.getText().toString().trim();
        String vehicle = etBookingVehicle.getText().toString().trim();
        int hours = getHours();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please add name and phone", Toast.LENGTH_SHORT).show();
            return;
        }

        long bookingId = dbHelper.addBookingDetailed(userId, parkingId, name, phone, vehicle, hours);
        if (bookingId == -1) {
            Toast.makeText(this, "No slots available!", Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerContact = "N/A";
        Cursor owner = dbHelper.getOwnerContactForParking(parkingId);
        if (owner != null && owner.moveToFirst()) {
            String ownerName = owner.getString(owner.getColumnIndexOrThrow("owner_name"));
            String ownerPhone = owner.getString(owner.getColumnIndexOrThrow("owner_phone"));
            ownerContact = ownerName + " - " + (ownerPhone == null || ownerPhone.isEmpty() ? "N/A" : ownerPhone);
            owner.close();
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.booking_success))
                .setMessage("Booking ID: #" + bookingId + "\n" + getString(R.string.owner_contact, ownerContact))
                .setPositiveButton("View History", (dialog, which) -> {
                    startActivity(new Intent(this, UserBookingHistoryActivity.class));
                    finish();
                })
                .setNegativeButton("Close", (dialog, which) -> finish())
                .show();
    }
}

