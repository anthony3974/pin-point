# Pinpoint App - Friend & Location Tracker

Pinpoint is an Android application built with **Jetpack Compose** and **Firebase** that allows users to share real-time locations, manage favorite spots, and connect with friends via MQTT.

---

## Features

### Location & Mapping

* **Real-time Tracking**: Toggle "Live" mode to share your location via MQTT.
* **Favorite Locations**: Save, name, and manage specific map coordinates.
* **Proximity Alerts**: Receive local notifications when a friend is within 1km.

### Social System

* **Friend Requests**: Search for friends by email and send requests.
* **Management**: Accept, decline, or remove friends from your network.
* **Push Notifications**: Integrated with **OneSignal** to alert users of new friend requests.

### Security & Auth

* **Firebase Auth**: Secure email-based registration and login.
* **Privacy Control**: "Ghost Mode" functionality allows you to see friends without sharing your own location.

---

## Technical Implementation

### **Core Stack**

* **UI**: Jetpack Compose with Material3.
* **Backend**: Firebase (Auth, Firestore).
* **Messaging**: MQTT (via HiveMQ) for low-latency location updates.
* **Notifications**: OneSignal for remote push and `NotificationManager` for local alerts.

### **MQTT Logic (MqttRepository.kt)**

The app uses a specific topic structure for real-time updates:

* **Location Topic**: `pinpoint/user/{uid}/location`.
* **Payloads**: Sends coordinates (`lat,lng`) or an `OFFLINE` packet when sharing is disabled.

---

## Setup Instructions

### 1. Firebase Integration

Add your `google-services.json` to the `app/` directory. Ensure **Firestore** and **Authentication** are enabled in the Firebase Console.

### 2. OneSignal Setup

Update the `ONESIGNAL_APP_ID` in `PinpointApplication.kt`:

```kotlin
const val ONESIGNAL_APP_ID = "your-app-id-here"

```

### 3. MQTT Configuration

Credentials are managed in `MqttRepository.kt`. Ensure your HiveMQ cluster URI and credentials match your instance.
