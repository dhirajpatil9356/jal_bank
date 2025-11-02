package com.capstone.jalbank;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SupportActivity extends AppCompatActivity {

    private Button reportIssueButton, contactPhoneButton;
    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupBottomNavigation();
        setupClickListeners();
    }

    private void initializeViews() {
        reportIssueButton = findViewById(R.id.report_issue_button);
        contactPhoneButton = findViewById(R.id.contact_phone_button);
        bottomNavigationView = findViewById(R.id.bottom_navigation_support);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_support);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(SupportActivity.this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_bills) {
                startActivity(new Intent(SupportActivity.this, BillsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_usage) {
                startActivity(new Intent(SupportActivity.this, UsageActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_support) {
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(SupportActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void setupClickListeners() {
        reportIssueButton.setOnClickListener(v -> {
            openReportIssueEmail();
        });

        contactPhoneButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+918767342249"));
            startActivity(intent);
        });
    }

    private void openReportIssueEmail() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : "N/A";
        String userEmail = (currentUser != null) ? currentUser.getEmail() : "N/A";

        String emailSubject = "JalBank App - Issue Report";
        String emailBody = "Hello Support Team,\n\nI am reporting an issue with the JalBank app.\n\n[Please describe the issue you are facing in detail here]\n\n---\nDevice Information (auto-generated):\nUser ID: " + userId + "\nUser Email: " + userEmail;

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:suryawanshidhiraj2003@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        intent.putExtra(Intent.EXTRA_TEXT, emailBody);

        startActivity(Intent.createChooser(intent, "Send email via..."));
    }
}
