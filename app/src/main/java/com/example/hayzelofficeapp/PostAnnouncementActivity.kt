package com.example.hayzelofficeapp

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hayzelofficeapp.databinding.ActivityPostAnnouncementBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class PostAnnouncementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostAnnouncementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Role check
        val prefs = getSharedPreferences("user_cache", MODE_PRIVATE)
        val role = prefs.getString("user_role", "employee")

        if (role?.lowercase() !in listOf("admin", "hr", "manager", "ceo")) {
            Toast.makeText(this, "You don't have permission to post announcements", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding = ActivityPostAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "New Announcement"
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        // Setup the category spinner with better styling
        val categories = listOf("General", "HR", "Finance", "IT", "Marketing", "Operations", "Sales")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter

        // Set the default priority
        binding.priorityChipGroup.check(R.id.chipNormal)

        // Set the post button click listener
        binding.postButton.setOnClickListener {
            postAnnouncement()
        }
    }

    private fun postAnnouncement() {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        val title = binding.titleEditText.text.toString().trim()
        val message = binding.messageEditText.text.toString().trim()

        if (title.isEmpty()) {
            binding.titleEditText.error = "Title is required"
            binding.titleEditText.requestFocus()
            return
        }

        if (message.isEmpty()) {
            binding.messageEditText.error = "Message is required"
            binding.messageEditText.requestFocus()
            return
        }

        val priority = when (binding.priorityChipGroup.checkedChipId) {
            R.id.chipUrgent -> "URGENT"
            R.id.chipImportant -> "IMPORTANT"
            else -> "NORMAL"
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.postButton.isEnabled = false
        binding.postButton.text = "Posting..."

        val announcement = hashMapOf(
            "title" to title,
            "message" to message,
            "category" to binding.categorySpinner.selectedItem.toString(),
            "priority" to priority,
            "createdBy" to user.uid,
            "createdAt" to Timestamp.now(),
            "expiryAt" to Timestamp(Date(System.currentTimeMillis() + 7 * 86400000)) // Expires in 7 days
        )

        FirebaseFirestore.getInstance()
            .collection("announcements")
            .add(announcement)
            .addOnSuccessListener {
                Toast.makeText(this, "Announcement posted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.postButton.isEnabled = true
                binding.postButton.text = "POST ANNOUNCEMENT"
                Toast.makeText(this, "Failed to post announcement: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}