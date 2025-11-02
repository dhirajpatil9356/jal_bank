package com.capstone.jalbank;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, emailEditText, passwordEditText, meterIdEditText;
    private Button registerButton;
    private TextView goToLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        meterIdEditText = findViewById(R.id.meter_id_edit_text);
        registerButton = findViewById(R.id.register_button);
        goToLogin = findViewById(R.id.go_to_login_textview);

        registerButton.setOnClickListener(v -> registerUser());
        goToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser() {
        final String name = nameEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        final String meterId = meterIdEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(meterId)) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, authTask -> {
                    if (authTask.isSuccessful()) {
                        FirebaseUser firebaseUser = authTask.getResult().getUser();
                        String userId = firebaseUser.getUid();

                        HashMap<String, String> userData = new HashMap<>();
                        userData.put("name", name);
                        userData.put("email", email);
                        userData.put("meter_id", meterId);

                        mDatabase.child("users").child(userId).setValue(userData).addOnCompleteListener(dbTask -> {
                            runOnUiThread(() -> {
                                if (dbTask.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Database write failed: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Authentication failed: " + authTask.getException().getMessage(), Toast.LENGTH_LONG).show());
                    }
                });
    }
}