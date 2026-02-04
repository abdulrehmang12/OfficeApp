package com.example.hayzelofficeapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hayzelofficeapp.models.Employee
import com.google.firebase.firestore.FirebaseFirestore

class EmployeeListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateLayout: View
    private lateinit var totalEmployeesText: TextView
    private lateinit var searchEditText: EditText
    private lateinit var backButton: ImageView
    private lateinit var employeeAdapter: EmployeeAdapter

    private val db = FirebaseFirestore.getInstance()
    private val employeeList = mutableListOf<Employee>()
    private val filteredList = mutableListOf<Employee>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_list)

        Log.d("EmployeeList", "Activity started")

        // Initialize views
        recyclerView = findViewById(R.id.employeesRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        totalEmployeesText = findViewById(R.id.totalEmployeesText)
        searchEditText = findViewById(R.id.searchEditText)
        backButton = findViewById(R.id.backButton)

        // BACK BUTTON HANDLER - ADDED
        backButton.setOnClickListener {
            finish() // Dashboard wapis jao
        }

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        employeeAdapter = EmployeeAdapter(filteredList) { employee ->
            openEmployeeDetail(employee)
        }
        recyclerView.adapter = employeeAdapter
        Log.d("EmployeeList", "RecyclerView setup completed")

        // Load employees from Firestore
        loadEmployees()

        // Setup search filter
        setupSearch()
    }

    private fun loadEmployees() {
        Log.d("EmployeeList", "Loading employees from Firebase...")
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.GONE

        db.collection("employees")
            .get()
            .addOnSuccessListener { result ->
                Log.d("EmployeeList", "Firebase query successful, ${result.documents.size} documents found")

                employeeList.clear()
                for (document in result) {
                    Log.d("EmployeeList", "Document ID: ${document.id}, Data: ${document.data}")

                    val employee = Employee(
                        id = document.id,
                        name = document.getString("name") ?: "No Name",
                        role = document.getString("role") ?: "No Role",
                        email = document.getString("email") ?: "No Email",
                        phone = document.getString("phone") ?: "No Phone",
                        department = document.getString("department") ?: "No Department",
                        profileImage = document.getString("profileImage")
                    )
                    employeeList.add(employee)
                }

                Log.d("EmployeeList", "Total employees parsed: ${employeeList.size}")

                filteredList.clear()
                filteredList.addAll(employeeList)
                employeeAdapter.updateList(filteredList)

                progressBar.visibility = View.GONE
                updateUI()

                // Log for debugging
                Log.d("EmployeeList", "Adapter item count: ${employeeAdapter.itemCount}")
            }
            .addOnFailureListener { exception ->
                Log.e("EmployeeList", "Firebase error: ${exception.message}", exception)
                progressBar.visibility = View.GONE
                emptyStateLayout.visibility = View.VISIBLE
                totalEmployeesText.text = "Failed to load employees"

                // Show error message
                emptyStateLayout.findViewById<TextView>(R.id.emptyStateText)?.text =
                    "Error: ${exception.localizedMessage}"
            }
    }

    private fun updateUI() {
        if (filteredList.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            Log.d("EmployeeList", "UI: Empty state shown")
        } else {
            emptyStateLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            Log.d("EmployeeList", "UI: RecyclerView shown with ${filteredList.size} items")
        }
        totalEmployeesText.text = "${filteredList.size} employees"
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterEmployees(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterEmployees(query: String) {
        val lowerQuery = query.lowercase()
        filteredList.clear()

        if (query.isEmpty()) {
            filteredList.addAll(employeeList)
        } else {
            filteredList.addAll(employeeList.filter { employee ->
                (employee.name?.lowercase()?.contains(lowerQuery) == true) ||
                        (employee.role?.lowercase()?.contains(lowerQuery) == true) ||
                        (employee.department?.lowercase()?.contains(lowerQuery) == true) ||
                        (employee.email?.lowercase()?.contains(lowerQuery) == true)
            })
        }

        employeeAdapter.updateList(filteredList)
        updateUI()
        Log.d("EmployeeList", "Filtered to ${filteredList.size} employees for query: '$query'")
    }

    private fun openEmployeeDetail(employee: Employee) {
        Log.d("EmployeeList", "Opening detail for: ${employee.name}")
        val intent = Intent(this, EmployeeDetailActivity::class.java)
        intent.putExtra("employee_data", employee)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        Log.d("EmployeeList", "Activity resumed")
        // Optional: Refresh data
        // loadEmployees()
    }
}