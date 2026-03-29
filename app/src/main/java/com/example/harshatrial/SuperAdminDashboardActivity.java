package com.example.harshatrial;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SuperAdminDashboardActivity extends AppCompatActivity {

    private RecyclerView rvPendingAdmins;
    private DatabaseHelper dbHelper;
    private List<UserAccount> pendingAdmins;
    private AdminApprovalAdapter adapter;
    private View emptyStateSuper;
    private LinearProgressIndicator progressSuper;
    private TextView tvSummaryPendingValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin_dashboard);

        dbHelper = new DatabaseHelper(this);
        rvPendingAdmins = findViewById(R.id.rvPendingAdmins);
        rvPendingAdmins.setLayoutManager(new LinearLayoutManager(this));
        emptyStateSuper = findViewById(R.id.emptyStateSuper);
        progressSuper = findViewById(R.id.progressSuper);
        tvSummaryPendingValue = findViewById(R.id.tvSummaryPendingValue);

        BottomNavigationView bottomNavSuper = findViewById(R.id.bottomNavSuper);
        bottomNavSuper.setSelectedItemId(R.id.nav_super_home);
        bottomNavSuper.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_super_home) {
                return true;
            }
            if (item.getItemId() == R.id.nav_super_security) {
                startActivity(new Intent(this, SuperSecurityActivity.class));
                return true;
            }
            startActivity(new Intent(this, SuperProfileActivity.class));
            return true;
        });

        tvSummaryPendingValue.setOnClickListener(v -> startActivity(new Intent(this, SuperAnalyticsActivity.class)));

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());

        loadPendingAdmins();
    }

    private void logout() {
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        sp.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void loadPendingAdmins() {
        progressSuper.setVisibility(View.VISIBLE);
        pendingAdmins = new ArrayList<>();
        Cursor cursor = dbHelper.getPendingAdmins();
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                pendingAdmins.add(new UserAccount(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL))
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter = new AdminApprovalAdapter();
        rvPendingAdmins.setAdapter(adapter);
        tvSummaryPendingValue.setText(String.valueOf(pendingAdmins.size()));
        emptyStateSuper.setVisibility(pendingAdmins.isEmpty() ? View.VISIBLE : View.GONE);
        progressSuper.setVisibility(View.GONE);
    }

    class AdminApprovalAdapter extends RecyclerView.Adapter<AdminApprovalAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UserAccount user = pendingAdmins.get(position);
            holder.tvName.setText(user.name);
            holder.tvEmail.setText(user.email);
            holder.tvStatus.setText(R.string.role_parking_owner_pending);
            holder.tvBadgeStatus.setText(R.string.pending);
            holder.tvBadgeStatus.setTextColor(ContextCompat.getColor(SuperAdminDashboardActivity.this, R.color.full));
            holder.badge.setCardBackgroundColor(ContextCompat.getColor(SuperAdminDashboardActivity.this, R.color.fullContainer));
            holder.tvPrice.setText(R.string.verification_required);
            holder.btnApprove.setText(R.string.approve_now);
            holder.btnApprove.setOnClickListener(v -> {
                dbHelper.approveAdmin(user.id);
                Toast.makeText(SuperAdminDashboardActivity.this, "Owner Approved Successfully!", Toast.LENGTH_SHORT).show();
                loadPendingAdmins(); // Refresh list
            });
        }

        @Override
        public int getItemCount() { return pendingAdmins.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail, tvStatus, tvBadgeStatus, tvPrice;
            Button btnApprove;
            MaterialCardView badge;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvParkingName);
                tvEmail = itemView.findViewById(R.id.tvParkingAddress);
                tvStatus = itemView.findViewById(R.id.tvSlots);
                tvBadgeStatus = itemView.findViewById(R.id.tvStatus);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                badge = itemView.findViewById(R.id.statusBadge);
                btnApprove = itemView.findViewById(R.id.btnAction);
            }
        }
    }

    static class UserAccount {
        int id;
        String name, email;
        UserAccount(int id, String name, String email) {
            this.id = id; this.name = name; this.email = email;
        }
    }
}
