package com.example.hayzelofficeapp

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvDepartment: TextView
    private lateinit var tvPosition: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()

        // Initialize views
        initializeViews()

        // Load user data
        loadUserData()
    }

    private fun initializeViews() {
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        tvDepartment = findViewById(R.id.tvDepartment)
        tvPosition = findViewById(R.id.tvPosition)
        tvRole = findViewById(R.id.tvRole)
        tvStatus = findViewById(R.id.tvStatus)
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database.reference.child("users").child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Set user data to TextViews
                        tvFullName.text = snapshot.child("fullName").getValue(String::class.java) ?: "N/A"
                        tvEmail.text = snapshot.child("email").getValue(String::class.java) ?: "N/A"
                        tvDepartment.text = snapshot.child("department").getValue(String::class.java) ?: "N/A"
                        tvPosition.text = snapshot.child("position").getValue(String::class.java) ?: "N/A"
                        tvRole.text = snapshot.child("role").getValue(String::class.java) ?: "N/A"
                        tvStatus.text = snapshot.child("employmentStatus").getValue(String::class.java) ?: "N/A"
                    } else {
                        Toast.makeText(this@ProfileActivity, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProfileActivity, "Failed to load user data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}