package com.capstone.jalbank;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsageActivity extends AppCompatActivity {

    private LineChart lineChart;
    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initializeViews();
        setupBottomNavigation();
        loadUsageData();
    }

    private void initializeViews() {
        lineChart = findViewById(R.id.usage_line_chart);
        bottomNavigationView = findViewById(R.id.bottom_navigation_usage);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_usage);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(UsageActivity.this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_bills) {
                startActivity(new Intent(UsageActivity.this, BillsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_usage) {
                return true;
            } else if (itemId == R.id.navigation_support) {
                startActivity(new Intent(UsageActivity.this, SupportActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(UsageActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadUsageData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String meterId = dataSnapshot.child("meter_id").getValue(String.class);
                    if (meterId != null) {
                        fetchConsumptionHistory(meterId);
                    } else {
                        Toast.makeText(UsageActivity.this, "Meter ID not found.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(UsageActivity.this, "User data not found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UsageActivity.this, "Failed to load user data.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchConsumptionHistory(String meterId) {
        mDatabase.child("consumption_data").child(meterId).child("last_6_months").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Long> historyData = new ArrayList<>();
                    for (DataSnapshot monthSnapshot : dataSnapshot.getChildren()) {
                        historyData.add(monthSnapshot.getValue(Long.class));
                    }
                    plotUsageChart(historyData);
                } else {
                    Toast.makeText(UsageActivity.this, "Consumption history not found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UsageActivity.this, "Failed to load consumption history.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void plotUsageChart(List<Long> historyData) {
        if (historyData == null || historyData.isEmpty()) {
            lineChart.setNoDataText("No usage data available");
            return;
        }

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < historyData.size(); i++) {
            entries.add(new Entry(i, historyData.get(i).floatValue()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Monthly Consumption (Liters)");
        dataSet.setColor(getResources().getColor(R.color.blue));
        dataSet.setValueTextColor(getResources().getColor(R.color.black));
        dataSet.setCircleColor(getResources().getColor(R.color.blue));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(true);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        lineChart.getDescription().setText("Last 6 Months Usage");
        lineChart.getXAxis().setDrawLabels(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.animateX(1000);
        lineChart.invalidate();
    }
}
