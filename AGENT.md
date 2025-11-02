System Prompt / Context for Coding Agent — JalBank Smart Water Management App
🏗️ Project Overview

You are building an Android application in Java named JalBank: Smart Water Management System.
The goal of the app is to allow users (citizens) to view their monthly water consumption, predicted usage, and receive anomaly alerts using data provided from a pre-trained ML backend.

The ML work is already completed externally (in Python / Colab) and the model predictions are stored in Firebase Realtime Database, so the Android app does not need to perform machine learning computations.

The app must be demo-ready and functional — able to fetch and display real user data based on Meter IDs, with a clean UI inspired by a Figma prototype (provided by the user).
*never add comments in any xml file*
⚙️ System Components
1️⃣ Machine Learning Backend (Offline)

Python scripts handle training, preprocessing, and predictions.

Models used:

Random Forest Regressor (for consumption prediction)

Random Forest Classifier (for category: Low/Medium/High)

Isolation Forest & Z-Score (for anomaly detection)

Output is a JSON file containing predictions per meter ID, uploaded to Firebase.

2️⃣ Firebase Integration

Firebase acts as the cloud backend for this system.

Firebase Components:

Firebase Authentication: Handles user login/registration using email/password.

Firebase Realtime Database: Stores both user profiles and meter-wise consumption data.

Database Structure:

{
"users": {
"uid_dhiraj": {
"name": "Dhiraj",
"email": "dhiraj@gmail.com",
"meter_id": "57254690"
},
"uid_lakhan": {
"name": "Lakhan",
"email": "lakhan@gmail.com",
"meter_id": "57254758"
}
},
"consumption_data": {
"57254690": {
"current_consumption": 32000,
"predicted_next": 34500,
"category": "Medium",
"anomaly": false,
"last_6_months": [28000, 29000, 30000, 32000, 34000, 34500]
},
"57254758": {
"current_consumption": 54000,
"predicted_next": 62000,
"category": "High",
"anomaly": true,
"last_6_months": [50000, 52000, 54000, 56000, 62000, 61000]
}
}
}

📱 Android App Requirements
🧩 Core Technologies

Language: Java

IDE: Android Studio

Database: Firebase Realtime Database

Authentication: FirebaseAuth (Email + Password)

Charting: MPAndroidChart (for history graphs)

UI: XML (Material Components preferred)

🧭 Navigation Flow
1️⃣ Splash Screen

Displays app name + logo (“JalBank”)

Automatically checks if user logged in → navigates to Login or Dashboard

2️⃣ Register Screen (RegisterActivity.java)

Inputs: Name, Email, Password, Meter ID

On submit:

Creates FirebaseAuth user

Stores user info (name, email, meter_id) under /users

Redirects to Dashboard

3️⃣ Login Screen (LoginActivity.java)

Inputs: Email + Password

On success:

Fetch user’s meter_id from /users node

Redirect to Dashboard

4️⃣ Dashboard Screen (DashboardActivity.java)

Automatically retrieves the logged-in user’s meter_id

Fetches corresponding data from /consumption_data/[meter_id]

Displays:

Current Consumption

Predicted Next Month

Category (Low/Medium/High)

Anomaly Alert (if true, show ⚠️ warning card)

“View History” button

5️⃣ History Screen (HistoryActivity.java)

Displays the last_6_months data in a line chart (MPAndroidChart).

X-axis → Months

Y-axis → Consumption

6️⃣ (Later) Admin Dashboard (AdminActivity.java)

Hardcoded admin login

Displays all meter data + anomalies

Optional: Send notifications or update records.

🎨 Design Reference (Based on Figma)

Color palette:

Primary Blue (#64B5F6)

White (#FFFFFF)

Success Green (#4CAF50)

Alert Red (#E53935)

Font: Roboto / Google Sans

Icons: Material Icons (water_drop, warning, bar_chart, person)

Layout: Clean, minimal, card-based UI for each stat.

⚡ Expected Deliverables for the Coding Agent

The coding agent must:

Generate Java classes for all activities:

SplashActivity.java

RegisterActivity.java

LoginActivity.java

DashboardActivity.java

HistoryActivity.java

(Optional later) AdminActivity.java

Generate corresponding XML layout files with modern Material Components.

Implement Firebase Authentication for registration and login.

Implement Firebase Database fetching using meter_id.

Use MPAndroidChart for plotting user history.

Handle errors gracefully (no crashes if data missing).

Ensure UI and navigation are smooth and consistent.

🧠 Development Logic Summary

The model is already trained offline.

The app should not train or run ML code.

The app only fetches and displays results from Firebase.

Each user sees only their data (based on saved meter_id).

The admin (later) can see all users’ data.

🚀 Demo Objectives

By October 30th, the app should:

Successfully allow registration/login via Firebase.

Fetch user-specific meter data and show it on dashboard.

Display:

Current & Predicted consumption

Category

Anomaly warning (if any)

Usage history chart

Function offline using cached Firebase data (optional bonus).


Summary for the Agent:

Build an Android app in Java (Firebase-integrated) for water consumption tracking called “JalBank.”
Users log in via Firebase, and their unique meter_id links them to water consumption data fetched from Firebase.
The app displays current usage, predicted usage, category, and anomaly alerts with a chart of historical data.
The backend ML model already generates predictions offline; your task is to build the frontend + Firebase integration.