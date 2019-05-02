package com.example.userasef.parentcontrolappchild.firebaseForNotifications;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

@SuppressLint("Registered")
public class MyFirebase extends FirebaseMessagingService {
    private static final String TAG = "TAGO";
    @Override
    public void onNewToken(String s) {

        super.onNewToken(s);
        Log.d("TAGO", s);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference fcmDatabaseRef = ref.child("FCM_DEVICE_TOKENS").push();

        fcmDatabaseRef.setValue(s);

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

    }
}