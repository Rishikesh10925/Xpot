package com.example.harshatrial;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class SuperAnalyticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_analytics);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Cursor cursor = dbHelper.getSuperAnalytics();
        if (cursor != null && cursor.moveToFirst()) {
            int owners = cursor.getInt(cursor.getColumnIndexOrThrow("owners"));
            int pending = cursor.getInt(cursor.getColumnIndexOrThrow("pending_owners"));
            int locations = cursor.getInt(cursor.getColumnIndexOrThrow("total_locations"));
            int bookings = cursor.getInt(cursor.getColumnIndexOrThrow("total_bookings"));

            ((TextView) findViewById(R.id.tvSuperAnalyticsOwners)).setText(String.valueOf(owners));
            ((TextView) findViewById(R.id.tvSuperAnalyticsPending)).setText(String.valueOf(pending));
            ((TextView) findViewById(R.id.tvSuperAnalyticsLocations)).setText(String.valueOf(locations));
            ((TextView) findViewById(R.id.tvSuperAnalyticsBookings)).setText(String.valueOf(bookings));

            int ownerScore = normalizeToPercent(owners, 50);
            int pendingScore = owners == 0 ? 0 : normalizeToPercent(pending, owners);
            int locationScore = normalizeToPercent(locations, 100);
            int bookingScore = normalizeToPercent(bookings, 500);

            ((TextView) findViewById(R.id.tvOwnersChartValue)).setText(getString(R.string.metric_value_text, getString(R.string.analytics_locations_label), ownerScore + "%"));
            ((TextView) findViewById(R.id.tvPendingChartValue)).setText(getString(R.string.metric_value_text, getString(R.string.analytics_pending_label), pendingScore + "%"));
            ((TextView) findViewById(R.id.tvLocationsChartValue)).setText(getString(R.string.metric_value_text, getString(R.string.total_locations), locationScore + "%"));
            ((TextView) findViewById(R.id.tvBookingsChartValue)).setText(getString(R.string.metric_value_text, getString(R.string.total_bookings), bookingScore + "%"));

            setProgress(R.id.barOwners, ownerScore);
            setProgress(R.id.barPending, pendingScore);
            setProgress(R.id.barLocations, locationScore);
            setProgress(R.id.barBookings, bookingScore);

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



