package com.example.harshatrial;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OwnerBookingsActivity extends AppCompatActivity {

    private final List<String[]> rows = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_bookings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rvOwnerBookings = findViewById(R.id.rvOwnerBookings);
        rvOwnerBookings.setLayoutManager(new LinearLayoutManager(this));

        loadBookings();
        rvOwnerBookings.setAdapter(new OwnerBookingsAdapter());
    }

    private void loadBookings() {
        int ownerId = getSharedPreferences("UserSession", MODE_PRIVATE).getInt("userId", -1);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Cursor cursor = dbHelper.getBookingsByOwner(ownerId);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String parking = cursor.getString(cursor.getColumnIndexOrThrow("parking_name"));
                String booker = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_NAME));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_PHONE));
                int hours = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_HOURS));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_TOTAL_PRICE));
                String title = parking + " - " + (booker == null || booker.isEmpty() ? "Guest" : booker);
                String sub = String.format(Locale.getDefault(), "%s | %d hrs | $%.2f", phone == null ? "N/A" : phone, hours, total);
                rows.add(new String[]{title, sub, ""});
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private class OwnerBookingsAdapter extends RecyclerView.Adapter<OwnerBookingsAdapter.Holder> {
        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            String[] row = rows.get(position);
            holder.tvTitle.setText(row[0]);
            holder.tvSub.setText(row[1]);
            holder.tvOwner.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            TextView tvSub;
            TextView tvOwner;

            Holder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvBookingTitle);
                tvSub = itemView.findViewById(R.id.tvBookingSub);
                tvOwner = itemView.findViewById(R.id.tvBookingOwnerContact);
            }
        }
    }
}

