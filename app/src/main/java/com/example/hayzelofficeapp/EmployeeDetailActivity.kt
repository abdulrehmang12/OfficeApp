package com.example.hayzelofficeapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.hayzelofficeapp.models.Employee

class EmployeeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_detail)

        val employee = intent.getSerializableExtra("employee_data") as? Employee

        if (employee != null) {
            displayEmployeeDetails(employee)
        }

        // Back button
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }
    }

    private fun displayEmployeeDetails(employee: Employee) {
        // Find views
        val nameTextView: TextView = findViewById(R.id.nameTextView)
        val roleTextView: TextView = findViewById(R.id.roleTextView)
        val departmentTextView: TextView = findViewById(R.id.departmentTextView)
        val emailTextView: TextView = findViewById(R.id.emailTextView)
        val phoneTextView: TextView = findViewById(R.id.phoneTextView)
        val profileImageView: ImageView = findViewById(R.id.profileImageView)

        // Bind data to views
        nameTextView.text = employee.name ?: "Unknown"
        roleTextView.text = employee.role ?: "No Role"
        departmentTextView.text = employee.department ?: "No Department"
        emailTextView.text = employee.email ?: "No Email"
        phoneTextView.text = employee.phone ?: "No Phone"

        // Load profile image
        if (!employee.profileImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(employee.profileImage)
                .placeholder(R.drawable.ic_person)
                .into(profileImageView)
        } else {
            profileImageView.setImageResource(R.drawable.ic_person)
        }
    }

    companion object {
        fun createIntent(context: Context, employee: Employee): Intent {
            return Intent(context, EmployeeDetailActivity::class.java).apply {
                putExtra("employee_data", employee)
            }
        }
    }
}