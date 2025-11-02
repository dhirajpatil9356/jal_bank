package com.capstone.jalbank;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
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
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DashboardActivity extends AppCompatActivity {

    private TextView greetingTextView, meterIdTextView, currentBillAmount, dueDateLabel, usageToday, usageWeek, usageMonth, usagePercentageText;
    private TextView aiForecastBillText, aiForecastSavingsText, waterSavingTipText, paymentReminderAlertText;
    private ImageView shareTipButton;
    private ProgressBar usageProgressBar;
    private LinearLayout highUsageAlert, paymentReminderAlert;
    private MaterialCardView payBillCard, usageHistoryCard;
    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private final double COST_PER_LITRE = 0.25;
    private Double currentBill = 0.0;

    private final List<String> waterSavingTips = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initializeViews();
        setupBottomNavigation();
        setupClickListeners();
        loadUserData();
        setupWaterSavingTips();
    }

    private void initializeViews() {
        greetingTextView = findViewById(R.id.greeting_textview);
        meterIdTextView = findViewById(R.id.meter_id_textview);
        currentBillAmount = findViewById(R.id.current_bill_amount);
        dueDateLabel = findViewById(R.id.due_date_label);
        usageToday = findViewById(R.id.usage_today_text);
        usageWeek = findViewById(R.id.usage_week_text);
        usageMonth = findViewById(R.id.usage_month_text);
        usageProgressBar = findViewById(R.id.usage_progress_bar);
        usagePercentageText = findViewById(R.id.usage_percentage_text);
        highUsageAlert = findViewById(R.id.high_usage_alert);
        paymentReminderAlert = findViewById(R.id.payment_reminder_alert);
        paymentReminderAlertText = findViewById(R.id.payment_reminder_alert_text);
        aiForecastBillText = findViewById(R.id.ai_forecast_bill_text);
        aiForecastSavingsText = findViewById(R.id.ai_forecast_savings_text);
        waterSavingTipText = findViewById(R.id.water_saving_tip_text);
        shareTipButton = findViewById(R.id.share_tip_button);
        payBillCard = findViewById(R.id.pay_bill_card);
        usageHistoryCard = findViewById(R.id.usage_history_card);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                return true;
            } else if (itemId == R.id.navigation_bills) {
                startActivity(new Intent(DashboardActivity.this, BillsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_usage) {
                startActivity(new Intent(DashboardActivity.this, UsageActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_support) {
                startActivity(new Intent(DashboardActivity.this, SupportActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
    
    private void setupClickListeners() {
        payBillCard.setOnClickListener(v -> {
            if(currentBill > 0){
                showPaymentDialog();
            } else {
                Toast.makeText(this, "No outstanding bill to pay.", Toast.LENGTH_SHORT).show();
            }
        });

        usageHistoryCard.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, UsageActivity.class));
            overridePendingTransition(0, 0);
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
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String meterId = dataSnapshot.child("meter_id").getValue(String.class);

                    if (name != null) {
                        greetingTextView.setText("Good Morning, " + name);
                    }
                    if (meterId != null) {
                        meterIdTextView.setText("Meter ID: " + meterId);
                        loadConsumptionData(meterId);
                    } else {
                        Toast.makeText(DashboardActivity.this, "Meter ID not found for this user.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(DashboardActivity.this, "User data not found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DashboardActivity.this, "Failed to load user data: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadConsumptionData(String meterId) {
        mDatabase.child("consumption_data").child(meterId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Double currentConsumption = dataSnapshot.child("current_consumption").getValue(Double.class);
                    Double predictedNext = dataSnapshot.child("predicted_next").getValue(Double.class);
                    Boolean anomaly = dataSnapshot.child("anomaly").getValue(Boolean.class);
                    List<Long> last6Months = new ArrayList<>();
                    DataSnapshot monthsSnapshot = dataSnapshot.child("last_6_months");
                    if (monthsSnapshot.exists()) {
                        for (DataSnapshot month : monthsSnapshot.getChildren()) {
                            last6Months.add(month.getValue(Long.class));
                        }
                    }

                    updateConsumptionUI(currentConsumption, last6Months);
                    updateAlerts(anomaly, currentConsumption);
                    updateForecast(currentConsumption, predictedNext);

                } else {
                    Toast.makeText(DashboardActivity.this, "Consumption data not found for meter: " + meterId, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DashboardActivity.this, "Failed to load consumption data: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupWaterSavingTips() {
        waterSavingTips.add("Fixing a leaky faucet can save up to 75 liters of water a day.");
        waterSavingTips.add("Turn off the tap while brushing your teeth to save over 200 liters a month.");
        waterSavingTips.add("Water your plants during the early morning to reduce evaporation.");
        waterSavingTips.add("A 5-minute shower uses 40-50 liters of water. Try to keep showers short!");
        waterSavingTips.add("Use a broom instead of a hose to clean your driveway or sidewalk.");

        displayRandomTip();

        shareTipButton.setOnClickListener(v -> {
            String currentTip = waterSavingTipText.getText().toString();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, currentTip + " #JalBank #SaveWater");
            startActivity(Intent.createChooser(shareIntent, "Share this tip"));
        });
    }
    
    private void showPaymentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_payment_method, null);
        builder.setView(dialogView);

        final RadioGroup paymentMethodGroup = dialogView.findViewById(R.id.payment_method_group);
        final TextView totalAmount = dialogView.findViewById(R.id.dialog_total_amount);
        DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
        totalAmount.setText("Total to Pay: ₹" + currencyFormat.format(currentBill));

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
    
    private void processPayment(){
        Toast.makeText(this, "Payment Successful (Simulated)", Toast.LENGTH_LONG).show();

        currentBill = 0.0;
        updateConsumptionUI(0.0, null);
        paymentReminderAlert.setVisibility(View.GONE);
    }

    private void displayRandomTip() {
        if (!waterSavingTips.isEmpty()) {
            int randomIndex = new Random().nextInt(waterSavingTips.size());
            waterSavingTipText.setText(waterSavingTips.get(randomIndex));
        }
    }

    private void updateConsumptionUI(Double currentConsumption, List<Long> last6Months) {
        if (currentConsumption == null) currentConsumption = 0.0;
        
        this.currentBill = currentConsumption * COST_PER_LITRE;

        DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
        currentBillAmount.setText("₹" + currencyFormat.format(this.currentBill));
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 25);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        dueDateLabel.setText("Due: " + sdf.format(cal.getTime()));

        usageMonth.setText(String.format(Locale.getDefault(), "%,.0fL", currentConsumption));
        usageWeek.setText(String.format(Locale.getDefault(), "%,.0fL", currentConsumption / 4.0));
        usageToday.setText(String.format(Locale.getDefault(), "%,.0fL", currentConsumption / 30.0));

        if (last6Months != null && !last6Months.isEmpty()) {
            long sum = 0;
            for (long monthVal : last6Months) {
                sum += monthVal;
            }
            double average = sum / (double) last6Months.size();

            if (average > 0) {
                int percentage = (int) ((currentConsumption / average) * 100);
                usageProgressBar.setProgress(Math.min(percentage, 100));
                usagePercentageText.setText(String.format(Locale.getDefault(), "%d%% of average monthly usage", percentage));
            } else {
                usageProgressBar.setProgress(0);
                usagePercentageText.setText("No average usage data available");
            }
        }
    }

    private void updateAlerts(Boolean anomaly, Double currentConsumption) {
        if (anomaly != null && anomaly) {
            highUsageAlert.setVisibility(View.VISIBLE);
        } else {
            highUsageAlert.setVisibility(View.GONE);
        }

        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        int dueDate = 25;
        int daysRemaining = dueDate - today;

        if (currentConsumption != null && currentConsumption > 0) {
            if (daysRemaining >= 0 && daysRemaining <= 5) {
                paymentReminderAlert.setVisibility(View.VISIBLE);
                if (daysRemaining == 0) {
                    paymentReminderAlertText.setText("Your bill is due today. Pay now to avoid late fees.");
                } else if (daysRemaining == 1) {
                    paymentReminderAlertText.setText("Your bill is due tomorrow.");
                } else {
                    paymentReminderAlertText.setText("Your bill is due in " + daysRemaining + " days.");
                }
            } else {
                paymentReminderAlert.setVisibility(View.GONE);
            }
        } else {
            paymentReminderAlert.setVisibility(View.GONE);
        }
    }

    private void updateForecast(Double currentConsumption, Double predictedNext) {
        if (currentConsumption == null || predictedNext == null) return;

        double predictedBill = predictedNext * COST_PER_LITRE;
        double currentBill = currentConsumption * COST_PER_LITRE;

        DecimalFormat currencyFormat = new DecimalFormat("#,##0");
        String forecastText = "Based on your usage pattern, your next bill is predicted to be around ₹" + currencyFormat.format(predictedBill);
        aiForecastBillText.setText(forecastText);

        if (currentBill > 0) {
            double difference = ((predictedBill - currentBill) / currentBill) * 100;
            if (difference < 0) {
                aiForecastSavingsText.setText(String.format(Locale.getDefault(), "%.0f%% lower than this month", Math.abs(difference)));
                aiForecastSavingsText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                aiForecastSavingsText.setText(String.format(Locale.getDefault(), "%.0f%% higher than this month", difference));
                aiForecastSavingsText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        } else {
            aiForecastSavingsText.setText("-");
        }
    }
}
