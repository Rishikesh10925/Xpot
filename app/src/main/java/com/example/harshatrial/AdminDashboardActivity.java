package com.example.harshatrial;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    private RecyclerView rvParkingLocations;
    private DatabaseHelper dbHelper;
    private int userId;
    private ParkingAdapter adapter;
    private List<ParkingLocation> parkingList;
    private TextView tvSummaryOwnerSlotsValue;
    private TextView tvSummaryOwnerLocationsValue;
    private View emptyStateOwner;
    private LinearProgressIndicator progressOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = sp.getInt("userId", -1);

        rvParkingLocations = findViewById(R.id.rvParkingLocations);
        rvParkingLocations.setLayoutManager(new LinearLayoutManager(this));
        tvSummaryOwnerSlotsValue = findViewById(R.id.tvSummaryOwnerSlotsValue);
        tvSummaryOwnerLocationsValue = findViewById(R.id.tvSummaryOwnerLocationsValue);
        emptyStateOwner = findViewById(R.id.emptyStateOwner);
        progressOwner = findViewById(R.id.progressOwner);

        BottomNavigationView bottomNavAdmin = findViewById(R.id.bottomNavAdmin);
        bottomNavAdmin.setSelectedItemId(R.id.nav_admin_home);
        bottomNavAdmin.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_admin_add) {
                startActivity(new Intent(this, AddParkingActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.nav_admin_home) {
                return true;
            }
            startActivity(new Intent(this, OwnerProfileActivity.class));
            return true;
        });

        tvSummaryOwnerSlotsValue.setOnClickListener(v -> startActivity(new Intent(this, OwnerAnalyticsActivity.class)));
        tvSummaryOwnerLocationsValue.setOnClickListener(v -> startActivity(new Intent(this, OwnerAnalyticsActivity.class)));

        findViewById(R.id.btnAddParking).setOnClickListener(v -> {
            startActivity(new Intent(this, AddParkingActivity.class));
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            logout();
        });

        loadParkingLocations();
    }

    private void logout() {
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        sp.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadParkingLocations();
    }

    public void loadParkingLocations() {
        progressOwner.setVisibility(View.VISIBLE);
        parkingList = new ArrayList<>();
        Cursor cursor = dbHelper.getParkingByOwner(userId);
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
        adapter = new ParkingAdapter(this, parkingList, true, userId);
        rvParkingLocations.setAdapter(adapter);

        int totalAvailable = 0;
        for (ParkingLocation parkingLocation : parkingList) {
            totalAvailable += parkingLocation.getAvailableSlots();
        }
        tvSummaryOwnerSlotsValue.setText(String.valueOf(totalAvailable));
        tvSummaryOwnerLocationsValue.setText(String.valueOf(parkingList.size()));
        emptyStateOwner.setVisibility(parkingList.isEmpty() ? View.VISIBLE : View.GONE);
        progressOwner.setVisibility(View.GONE);
    }

    public static class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ViewHolder> {
        private final Context context;
        private final List<ParkingLocation> parkingList;
        private final boolean isAdmin;
        private final int currentUserId;

        public ParkingAdapter(Context context, List<ParkingLocation> parkingList, boolean isAdmin, int currentUserId) {
            this.context = context;
            this.parkingList = parkingList;
            this.isAdmin = isAdmin;
            this.currentUserId = currentUserId;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_parking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ParkingLocation parking = parkingList.get(position);
            holder.tvName.setText(parking.getName());
            holder.tvAddress.setText(parking.getAddress());
            holder.tvSlots.setText(context.getString(R.string.parking_slots_template, parking.getAvailableSlots(), parking.getTotalSlots()));
            holder.tvPrice.setText(String.format(Locale.getDefault(), "$%.2f/hr", parking.getPricePerHour()));
            holder.tvFeatures.setText(parking.getFeatures().isEmpty() ? "Standard parking" : parking.getFeatures());

            boolean isAvailable = parking.getAvailableSlots() > 0;
            if (isAvailable) {
                holder.tvStatus.setText(R.string.status_available);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.available));
                holder.statusBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.availableContainer));
            } else {
                holder.tvStatus.setText(R.string.status_full);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.full));
                holder.statusBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.fullContainer));
            }

            holder.btnAction.setOnClickListener(v -> {
                if (isAdmin) {
                    Intent editIntent = new Intent(context, AddParkingActivity.class);
                    editIntent.putExtra(AddParkingActivity.EXTRA_PARKING_ID, parking.getId());
                    context.startActivity(editIntent);
                } else {
                    Intent bookingIntent = new Intent(context, BookingDetailsActivity.class);
                    bookingIntent.putExtra("parking_id", parking.getId());
                    bookingIntent.putExtra("parking_name", parking.getName());
                    bookingIntent.putExtra("parking_address", parking.getAddress());
                    bookingIntent.putExtra("parking_price", parking.getPricePerHour());
                    context.startActivity(bookingIntent);
                }
            });

            if (isAdmin) {
                holder.btnAction.setText(R.string.edit);
                holder.btnSecondaryAction.setVisibility(View.VISIBLE);
                holder.btnSecondaryAction.setText(R.string.view_bookings);
                holder.btnSecondaryAction.setOnClickListener(v -> {
                    Intent bookingsIntent = new Intent(context, OwnerBookingsActivity.class);
                    bookingsIntent.putExtra("parking_id", parking.getId());
                    bookingsIntent.putExtra("parking_name", parking.getName());
                    context.startActivity(bookingsIntent);
                });
            } else {
                holder.btnAction.setText(R.string.book_now);
                holder.btnSecondaryAction.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() { return parkingList.size(); }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvAddress, tvSlots, tvStatus, tvPrice, tvFeatures;
            Button btnAction, btnSecondaryAction;
            MaterialCardView statusBadge;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvParkingName);
                tvAddress = itemView.findViewById(R.id.tvParkingAddress);
                tvSlots = itemView.findViewById(R.id.tvSlots);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvFeatures = itemView.findViewById(R.id.tvFeatures);
                statusBadge = itemView.findViewById(R.id.statusBadge);
                btnAction = itemView.findViewById(R.id.btnAction);
                btnSecondaryAction = itemView.findViewById(R.id.btnSecondaryAction);
            }
        }
    }
}
