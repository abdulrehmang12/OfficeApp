package com.example.hayzelofficeapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HierarchyActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HierarchyAdapter
    private val hierarchyList = mutableListOf<HierarchyNode>()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hierarchy)

        // Setup RecyclerView
        recyclerView = findViewById(R.id.hierarchyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HierarchyAdapter(hierarchyList)
        recyclerView.adapter = adapter

        // Load hierarchy data
        loadHierarchy()
    }

    private fun loadHierarchy() {
        database.reference.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    hierarchyList.clear()

                    // Find CEO (person with no manager or role = CEO)
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        user?.let {
                            if (it.role.uppercase() == "CEO" || it.managerId.isEmpty()) {
                                // Add CEO as level 0
                                hierarchyList.add(HierarchyNode(it, 0))

                                // Find people reporting to CEO
                                loadReportsTo(it.userId, 1)
                            }
                        }
                    }

                    if (hierarchyList.isEmpty()) {
                        Toast.makeText(this@HierarchyActivity, "No hierarchy data found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@HierarchyActivity, "Failed to load hierarchy: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadReportsTo(managerId: String, level: Int) {
        database.reference.child("users")
            .orderByChild("managerId").equalTo(managerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        user?.let {
                            hierarchyList.add(HierarchyNode(it, level))

                            // Find people reporting to this person (recursive)
                            loadReportsTo(it.userId, level + 1)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@HierarchyActivity, "Error loading reports: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}