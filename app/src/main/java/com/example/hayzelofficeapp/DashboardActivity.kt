package com.example.hayzelofficeapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.example.hayzelofficeapp.databinding.ActivityDashboardBinding
import com.example.hayzelofficeapp.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var notificationHelper: NotificationHelper

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        prefs = getSharedPreferences("user_cache", MODE_PRIVATE)

        // Apply saved theme BEFORE super.onCreate
        AppCompatDelegate.setDefaultNightMode(
            if (prefs.getBoolean("dark_mode", false))
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Dashboard"

        notificationHelper = NotificationHelper(this)

        checkNotificationPermissionSafely()
        setupUI()
        loadUserSafely()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifications -> {
                startActivity(Intent(this, NotificationSettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupUI() {

        binding.employeeBtn.setOnClickListener {
            startActivity(Intent(this, EmployeeListActivity::class.java))
        }

        binding.announcementBtn.setOnClickListener {
            startActivity(Intent(this, AnnouncementsActivity::class.java))
        }

        binding.postAnnouncementBtn.setOnClickListener {
            startActivity(Intent(this, PostAnnouncementActivity::class.java))
        }

        binding.logoutBtn.setOnClickListener {
            prefs.edit().clear().apply()
            auth.signOut()
            goToLogin()
        }

        // ✅ Dark Mode switch removed from dashboard since it’s now in Notification Settings
    }

    private fun loadUserSafely() {
        val user = auth.currentUser ?: run {
            goToLogin()
            return
        }

        val name = prefs.getString("user_name", "User")
        val role = prefs.getString("user_role", "employee")

        binding.welcomeText.text =
            "Welcome, $name\n(${role!!.uppercase()})"

        applyRoleAccess(role)

        notificationHelper.subscribeToTopics(role)
        storeFCMToken(user.uid)
    }

    private fun applyRoleAccess(role: String) {
        binding.postAnnouncementBtn.visibility =
            if (role.lowercase() in listOf("admin", "hr", "manager", "ceo"))
                View.VISIBLE
            else
                View.GONE
    }

    private fun storeFCMToken(uid: String) {
        notificationHelper.getToken { token ->
            token ?: return@getToken
            firestore.collection("user_tokens")
                .document(uid)
                .set(mapOf("token" to token))
        }
    }

    private fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

    private fun checkNotificationPermissionSafely() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }
    }
}
