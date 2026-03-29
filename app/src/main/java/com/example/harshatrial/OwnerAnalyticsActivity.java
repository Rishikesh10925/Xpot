package com.example.harshatrial;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Locale;

public class OwnerAnalyticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_analytics);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        int ownerId = getSharedPreferences("UserSession", MODE_PRIVATE).getInt("userId", -1);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Cursor cursor = dbHelper.getOwnerAnalytics(ownerId);

        if (cursor != null && cursor.moveToFirst()) {
            int locations = cursor.getInt(cursor.getColumnIndexOrThrow("total_locations"));
            int bookings = cursor.getInt(cursor.getColumnIndexOrThrow("total_bookings"));
            double revenue = cursor.getDouble(cursor.getColumnIndexOrThrow("revenue"));

            ((TextView) findViewById(R.id.tvOwnerAnalyticsLocations)).setText(String.valueOf(locations));
            ((TextView) findViewById(R.id.tvOwnerAnalyticsBookings)).setText(String.valueOf(bookings));
            ((TextView) findViewById(R.id.tvOwnerAnalyticsRevenue)).setText(String.format(Locale.getDefault(), "$%.2f", revenue));

            int revenueScore = normalizeToPercent(revenue, 5000.0);
            int bookingScore = normalizeToPercent(bookings, Math.max(10, locations * 5));
            int locationScore = normalizeToPercent(locations, 20);

            ((TextView) findViewById(R.id.tvRevenueChartValue)).setText(getString(R.string.metric_value_text, getString(R.string.analytics_revenue_label), String.valueOf(revenueScore) + "%"));
            ((TextView) findViewById(R.id.tvBookingsChartValue)).setText(getString(R.string.metric_value_text, getString(R.string.analytics_bookings_label), String.valueOf(bookingScore) + "%"));
            ((TextView) findViewById(R.id.tvLocationsChartValue)).setText(getString(R.string.metric_value_text, getString(R.string.analytics_locations_label), String.valueOf(locationScore) + "%"));

            setProgress(R.id.barRevenue, revenueScore);
            setProgress(R.id.barBookings, bookingScore);
            setProgress(R.id.barLocations, locationScore);

            cursor.close();
        }
    }

    private void setProgress(int viewId, int value) {
        LinearProgressIndicator bar = findViewById(viewId);
        bar.setProgressCompat(Math.max(0, Math.min(100, value)), true);
    }

    private int normalizeToPercent(double value, double max) {
        if (max <= 0) {
            return 0;
        }
        return (int) Math.round((Math.min(value, max) / max) * 100.0);
    }
}


