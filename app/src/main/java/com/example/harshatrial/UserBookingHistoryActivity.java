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

public class UserBookingHistoryActivity extends AppCompatActivity {

    private final List<String[]> history = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_booking_history);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rvBookings = findViewById(R.id.rvBookings);
        rvBookings.setLayoutManager(new LinearLayoutManager(this));

        loadHistory();
        rvBookings.setAdapter(new HistoryAdapter());
    }

    private void loadHistory() {
        int userId = getSharedPreferences("UserSession", MODE_PRIVATE).getInt("userId", -1);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Cursor cursor = dbHelper.getBookingsByUser(userId);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow("parking_name"));
                int hours = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_HOURS));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_TOTAL_PRICE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_TIME));
                String owner = cursor.getString(cursor.getColumnIndexOrThrow("owner_phone"));
                String subtitle = String.format(Locale.getDefault(), "%s | %d hrs | $%.2f", time, hours, total);
                history.add(new String[]{title, subtitle, owner == null || owner.isEmpty() ? "N/A" : owner});
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Holder> {
        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            String[] row = history.get(position);
            holder.tvTitle.setText(row[0]);
            holder.tvSub.setText(row[1]);
            holder.tvOwner.setText(getString(R.string.owner_contact, row[2]));
        }

        @Override
        public int getItemCount() {
            return history.size();
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

