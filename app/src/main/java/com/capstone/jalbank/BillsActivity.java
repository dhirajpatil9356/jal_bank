package com.capstone.jalbank;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BillsActivity extends AppCompatActivity {

    private TextView billMonthYear, billingPeriod, dueDateText;
    private TextView waterChargesAmount, sewerageChargesAmount, serviceTaxAmount, totalAmount;
    private Button payBillButton;
    private LinearLayout paymentHistoryContainer;
    private TextView billStatus;
    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private final double COST_PER_LITRE = 0.07;
    private final double SEWERAGE_CHARGE_PERCENT = 0.15;
    private final double SERVICE_TAX_PERCENT = 0.05;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bills);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initializeViews();
        setupBottomNavigation();
        loadUserData();
    }

    private void initializeViews() {
        billMonthYear = findViewById(R.id.bill_month_year);
        billingPeriod = findViewById(R.id.billing_period);
        dueDateText = findViewById(R.id.due_date_text);
        waterChargesAmount = findViewById(R.id.water_charges_amount);
        sewerageChargesAmount = findViewById(R.id.sewerage_charges_amount);
        serviceTaxAmount = findViewById(R.id.service_tax_amount);
        totalAmount = findViewById(R.id.total_amount);
        payBillButton = findViewById(R.id.pay_bill_button);
        paymentHistoryContainer = findViewById(R.id.payment_history_container);
        billStatus = findViewById(R.id.bill_status);
        bottomNavigationView = findViewById(R.id.bottom_navigation_bills);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_bills);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(BillsActivity.this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_bills) {
                return true;
            } else if (itemId == R.id.navigation_usage) {
                startActivity(new Intent(BillsActivity.this, UsageActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_support) {
                startActivity(new Intent(BillsActivity.this, SupportActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(BillsActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadUserData() {
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
                        loadBillData(meterId);
                    } else {
                        Toast.makeText(BillsActivity.this, "Meter ID not found.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(BillsActivity.this, "User data not found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BillsActivity.this, "Failed to load user data.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadBillData(String meterId) {
        mDatabase.child("consumption_data").child(meterId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Double currentConsumption = dataSnapshot.child("current_consumption").getValue(Double.class);
                    List<Long> last6Months = new ArrayList<>();
                    DataSnapshot monthsSnapshot = dataSnapshot.child("last_6_months");
                    if(monthsSnapshot.exists()){
                        for (DataSnapshot month : monthsSnapshot.getChildren()) {
                            last6Months.add(month.getValue(Long.class));
                        }
                    }

                    updateCurrentBillUI(currentConsumption);
                    updatePaymentHistoryUI(last6Months);
                } else {
                    Toast.makeText(BillsActivity.this, "No billing data found for this meter.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                 Toast.makeText(BillsActivity.this, "Failed to load billing data.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateCurrentBillUI(Double currentConsumption) {
        if (currentConsumption == null) currentConsumption = 0.0;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        billMonthYear.setText(monthYearFormat.format(calendar.getTime()) + " Bill");

        SimpleDateFormat dayMonthFormat = new SimpleDateFormat("d MMM", Locale.getDefault());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = dayMonthFormat.format(calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = dayMonthFormat.format(calendar.getTime());
        billingPeriod.setText("Billing Period: " + startDate + " - " + endDate);
        
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 25);
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        dueDateText.setText(fullDateFormat.format(calendar.getTime()));

        double totalBill = currentConsumption * COST_PER_LITRE;
        double waterCharge = totalBill * (1 - SEWERAGE_CHARGE_PERCENT - SERVICE_TAX_PERCENT);
        double sewerageCharge = totalBill * SEWERAGE_CHARGE_PERCENT;
        double serviceTax = totalBill * SERVICE_TAX_PERCENT;

        DecimalFormat currencyFormat = new DecimalFormat("#,##0");
        waterChargesAmount.setText("₹" + currencyFormat.format(waterCharge));
        sewerageChargesAmount.setText("₹" + currencyFormat.format(sewerageCharge));
        serviceTaxAmount.setText("₹" + currencyFormat.format(serviceTax));
        totalAmount.setText("₹" + currencyFormat.format(totalBill));
        payBillButton.setText("Pay ₹" + currencyFormat.format(totalBill));

        payBillButton.setOnClickListener(v -> showPaymentDialog());
    }

    private void showPaymentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_payment_method, null);
        builder.setView(dialogView);

        final RadioGroup paymentMethodGroup = dialogView.findViewById(R.id.payment_method_group);

        builder.setPositiveButton("Confirm Payment", (dialog, which) -> {
            int selectedId = paymentMethodGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            } else {
                processPayment();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void processPayment() {
        Toast.makeText(this, "Payment Successful (Simulated)", Toast.LENGTH_SHORT).show();
        billStatus.setText("Paid");
        billStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        billStatus.setBackgroundColor(getResources().getColor(R.color.light_green_bg));
        payBillButton.setEnabled(false);
        payBillButton.setText("Paid");
    }

    private void updatePaymentHistoryUI(List<Long> historyData) {
        paymentHistoryContainer.removeAllViews();
        if (historyData == null || historyData.isEmpty()) return;
        
        Collections.reverse(historyData);

        Calendar calendar = Calendar.getInstance();
        for (Long consumption : historyData) {
            calendar.add(Calendar.MONTH, -1);
            View itemView = LayoutInflater.from(this).inflate(R.layout.payment_history_item, paymentHistoryContainer, false);

            TextView historyMonthYear = itemView.findViewById(R.id.history_month_year);
            TextView historyPaidDate = itemView.findViewById(R.id.history_paid_date);
            TextView historyAmount = itemView.findViewById(R.id.history_amount);
            
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            historyMonthYear.setText(monthYearFormat.format(calendar.getTime()));

            Calendar paidDate = (Calendar) calendar.clone();
            paidDate.set(Calendar.DAY_OF_MONTH, 22);
            SimpleDateFormat paidDateFormat = new SimpleDateFormat("\'Paid on\' dd MMM", Locale.getDefault());
            historyPaidDate.setText(paidDateFormat.format(paidDate.getTime()));

            double bill = consumption * COST_PER_LITRE;
            DecimalFormat currencyFormat = new DecimalFormat("#,##0");
            historyAmount.setText("₹" + currencyFormat.format(bill));

            paymentHistoryContainer.addView(itemView);
        }
    }
}
