package com.example.harshatrial;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class OwnerProfileActivity extends AppCompatActivity {

    private EditText etProfileName;
    private EditText etProfileEmail;
    private EditText etProfilePhone;
    private EditText etProfileVehicle;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_profile);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = sp.getInt("userId", -1);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle(R.string.profile_title);

        etProfileName = findViewById(R.id.etProfileName);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        etProfilePhone = findViewById(R.id.etProfilePhone);
        etProfileVehicle = findViewById(R.id.etProfileVehicle);

        loadProfile();

        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> {
            String name = etProfileName.getText().toString().trim();
            String phone = etProfilePhone.getText().toString().trim();
            String vehicle = etProfileVehicle.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            int updated = dbHelper.updateUserProfile(userId, name, phone, vehicle);
            if (updated > 0) {
                sp.edit().putString("userName", name).putString("userPhone", phone).putString("userVehicle", vehicle).apply();
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Unable to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfile() {
        Cursor cursor = dbHelper.getUserById(userId);
        if (cursor != null && cursor.moveToFirst()) {
            etProfileName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME)));
            etProfileEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)));
            etProfilePhone.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PHONE)));
            etProfileVehicle.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_VEHICLE)));
            cursor.close();
        }
    }
}

