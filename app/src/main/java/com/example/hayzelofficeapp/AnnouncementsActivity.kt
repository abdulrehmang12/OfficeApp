package com.example.hayzelofficeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hayzelofficeapp.databinding.ActivityAnnouncementsBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AnnouncementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnnouncementsBinding
    private lateinit var adapter: AnnouncementsAdapter
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnnouncementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.announcementsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AnnouncementsAdapter(mutableListOf())
        binding.announcementsRecyclerView.adapter = adapter

        binding.fabNewAnnouncement.setOnClickListener {
            startActivity(Intent(this, PostAnnouncementActivity::class.java))
        }

        binding.swipeRefreshLayout.setOnRefreshListener { loadAnnouncements() }

        binding.btnCreateAnnouncement.setOnClickListener {
            startActivity(Intent(this, PostAnnouncementActivity::class.java))
        }

        loadAnnouncements()
    }

    private fun loadAnnouncements() {
        val now = Timestamp.now()
        listener?.remove()

        listener = FirebaseFirestore.getInstance()
            .collection("announcements")
            .whereGreaterThan("expiryAt", now)
            .orderBy("expiryAt")
            .addSnapshotListener { snapshot, error ->

                binding.swipeRefreshLayout.isRefreshing = false

                if (error != null) {
                    Log.e("ANNOUNCEMENTS", error.message ?: "")
                    return@addSnapshotListener
                }

                val list = mutableListOf<Announcement>()
                snapshot?.documents?.forEach { doc ->
                    doc.toObject(Announcement::class.java)?.let {
                        list.add(it.copy(id = doc.id))
                    }
                }

                adapter.updateAnnouncements(list)

                if (list.isEmpty()) {
                    binding.emptyStateLayout.visibility = View.VISIBLE
                    binding.announcementsRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyStateLayout.visibility = View.GONE
                    binding.announcementsRecyclerView.visibility = View.VISIBLE
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}
