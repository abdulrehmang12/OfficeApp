package com.example.hayzelofficeapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.hayzelofficeapp.DashboardActivity
import com.example.hayzelofficeapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseNotificationService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        const val CHANNEL_ID = "hayzel_notifications"
        const val CHANNEL_NAME = "Hayzel Office Notifications"
    }

    /**
     * Called when message is received.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a notification payload
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "Hayzel Office"
            val body = notification.body ?: "New notification"
            val imageUrl = notification.imageUrl?.toString()

            Log.d(TAG, "Notification Title: $title")
            Log.d(TAG, "Notification Body: $body")

            // Show notification
            sendNotification(title, body, imageUrl)
        }

        // Check if message contains a data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            // Handle different types of notifications
            handleDataPayload(remoteMessage.data)
        }
    }

    /**
     * Handle different types of notifications based on data
     */
    private fun handleDataPayload(data: Map<String, String>) {
        val type = data["type"] ?: "general"

        when (type) {
            "announcement" -> {
                val announcementId = data["announcement_id"]
                val title = data["title"] ?: "New Announcement"
                val body = data["body"] ?: "Check out the latest announcement"
                sendNotification(title, body, null, announcementId)
            }
            "employee" -> {
                val title = data["title"] ?: "Employee Update"
                val body = data["body"] ?: "Employee information updated"
                sendNotification(title, body)
            }
            "meeting" -> {
                val title = data["title"] ?: "Meeting Reminder"
                val body = data["body"] ?: "You have an upcoming meeting"
                sendNotification(title, body)
            }
            else -> {
                val title = data["title"] ?: "Hayzel Office"
                val body = data["body"] ?: "New notification"
                sendNotification(title, body)
            }
        }
    }

    /**
     * Create and show a simple notification
     */
    private fun sendNotification(
        title: String,
        messageBody: String,
        imageUrl: String? = null,
        announcementId: String? = null
    ) {
        // Create intent for when notification is tapped
        val intent = Intent(this, DashboardActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            // Pass data if available
            if (announcementId != null) {
                putExtra("notification_announcement_id", announcementId)
                putExtra("from_notification", true)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set notification sound
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))

        // Add image if available (requires Glide)
        imageUrl?.let {
            // You can add image here using Glide or download bitmap
            // For simplicity, we'll skip image for now
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Hayzel Office notifications"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Show notification
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    /**
     * Called if FCM registration token is updated.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    /**
     * Send token to your app server.
     */
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server
        // Store token in Firestore for each user
        Log.d(TAG, "Token to send to server: $token")
    }
}