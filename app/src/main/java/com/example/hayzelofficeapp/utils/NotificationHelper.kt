package com.example.hayzelofficeapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.hayzelofficeapp.R
import com.google.firebase.messaging.FirebaseMessaging

class NotificationHelper(private val context: Context) {

    companion object {
        private const val TAG = "NotificationHelper"
        const val CHANNEL_ID = "hayzel_notifications"
        const val CHANNEL_NAME = "Hayzel Office Notifications"
    }

    /**
     * Initialize notification channels (required for Android 8.0+)
     */
    fun initializeNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            // Create main notification channel
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Receive notifications from Hayzel Office"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    /**
     * Subscribe to topics based on user role
     */
    fun subscribeToTopics(userRole: String?) {
        // Subscribe to general topic
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to all_users topic")
                } else {
                    Log.e(TAG, "Failed to subscribe to all_users topic")
                }
            }

        // Subscribe based on role
        when (userRole?.lowercase()) {
            "admin", "ceo" -> {
                FirebaseMessaging.getInstance().subscribeToTopic("admins")
                Log.d(TAG, "Subscribed to admins topic")
            }
            "hr" -> {
                FirebaseMessaging.getInstance().subscribeToTopic("hr_department")
                Log.d(TAG, "Subscribed to hr_department topic")
            }
            "manager" -> {
                FirebaseMessaging.getInstance().subscribeToTopic("managers")
                Log.d(TAG, "Subscribed to managers topic")
            }
        }
    }

    /**
     * Unsubscribe from all topics (on logout)
     */
    fun unsubscribeFromAllTopics() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("all_users")
        FirebaseMessaging.getInstance().unsubscribeFromTopic("admins")
        FirebaseMessaging.getInstance().unsubscribeFromTopic("hr_department")
        FirebaseMessaging.getInstance().unsubscribeFromTopic("managers")
        Log.d(TAG, "Unsubscribed from all topics")
    }

    /**
     * Get FCM token
     */
    fun getToken(callback: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "FCM Token: $token")
                callback(token)
            } else {
                Log.e(TAG, "Failed to get FCM token", task.exception)
                callback(null)
            }
        }
    }

    /**
     * Check if notifications are enabled
     */
    fun areNotificationsEnabled(): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
            channel?.importance != NotificationManager.IMPORTANCE_NONE
        } else {
            // For older versions, assume enabled
            true
        }
    }

    /**
     * Show a local notification for testing
     */
    fun showLocalNotification(title: String, message: String, context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}