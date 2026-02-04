package com.example.hayzelofficeapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.example.hayzelofficeapp.databinding.ActivityNotificationSettingsBinding
import com.example.hayzelofficeapp.utils.NotificationHelper

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationSettingsBinding
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notificationHelper = NotificationHelper(this)

        setupToolbar()
        setupSwitches()
        setupDarkModeSwitch()
        setupButtons()
        loadCurrentSettings()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupSwitches() {
        binding.switchAnnouncements.setOnCheckedChangeListener { _, isChecked ->
            savePreference("announcements", isChecked)
        }

        binding.switchEmployeeUpdates.setOnCheckedChangeListener { _, isChecked ->
            savePreference("employee_updates", isChecked)
        }

        binding.switchMeetings.setOnCheckedChangeListener { _, isChecked ->
            savePreference("meetings", isChecked)
        }

        binding.switchGeneral.setOnCheckedChangeListener { _, isChecked ->
            savePreference("general", isChecked)
        }
    }

    private fun setupDarkModeSwitch() {
        val prefs = getSharedPreferences("user_cache", MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", false)
        binding.switchDarkMode.isChecked = isDark

        binding.switchDarkMode.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("dark_mode", checked).apply()

            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupButtons() {
        binding.btnSystemSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            startActivity(intent)
        }

        binding.btnTestNotification.setOnClickListener {
            sendTestNotification()
        }
    }

    private fun loadCurrentSettings() {
        val prefs = getSharedPreferences("notification_prefs", MODE_PRIVATE)

        binding.switchAnnouncements.isChecked = prefs.getBoolean("announcements", true)
        binding.switchEmployeeUpdates.isChecked = prefs.getBoolean("employee_updates", true)
        binding.switchMeetings.isChecked = prefs.getBoolean("meetings", true)
        binding.switchGeneral.isChecked = prefs.getBoolean("general", true)

        // Check system notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.switchSystemNotifications.isChecked = notificationHelper.areNotificationsEnabled()
            binding.switchSystemNotifications.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && !notificationHelper.areNotificationsEnabled()) {
                    requestNotificationPermission()
                }
            }
        } else {
            binding.switchSystemNotifications.isChecked = true
            binding.switchSystemNotifications.isEnabled = false
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1002
            )
        }
    }

    private fun savePreference(key: String, value: Boolean) {
        val prefs = getSharedPreferences("notification_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean(key, value).apply()
        updateFirebaseTopics()
    }

    private fun updateFirebaseTopics() {
        // Implement topic subscription/unsubscription based on preferences if needed
    }

    private fun sendTestNotification() {
        notificationHelper.showLocalNotification(
            "Test Notification",
            "This is a test notification from Hayzel Office",
            this
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
