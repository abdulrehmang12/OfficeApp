package com.example.hayzelofficeapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class AnnouncementDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcement_detail)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val announcementId = intent.getStringExtra("id") ?: return
        loadAnnouncement(announcementId)
    }

    private fun loadAnnouncement(id: String) {
        FirebaseFirestore.getInstance()
            .collection("announcements")
            .document(id)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                val title = doc.getString("title") ?: "No Title"
                val message = doc.getString("message") ?: ""
                val author = doc.getString("createdBy") ?: "Unknown"
                val priority = doc.getString("priority") ?: "NORMAL"
                val category = doc.getString("category") ?: "General"
                val createdAt = doc.getTimestamp("createdAt")

                val txtTitle = findViewById<TextView>(R.id.txtTitle)
                val txtMessage = findViewById<TextView>(R.id.txtMessage)
                val authorView = findViewById<TextView>(R.id.authorTextView)
                val categoryView = findViewById<TextView>(R.id.categoryTextView)
                val dateView = findViewById<TextView>(R.id.dateTextView)
                val priorityView = findViewById<TextView>(R.id.priorityTextView)
                val cardTitleMessage = findViewById<View>(R.id.cardTitleMessage)
                val cardMeta = findViewById<View>(R.id.priorityTextView).parent as View

                // Set texts
                txtTitle.text = title
                txtMessage.text = message
                authorView.text = "By: $author"
                categoryView.text = "Category: $category"
                createdAt?.let {
                    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    dateView.text = "Posted: ${sdf.format(it.toDate())}"
                }

                // Priority styling
                priorityView.text = priority.uppercase()
                val bg = when (priority.uppercase()) {
                    "HIGH", "URGENT" -> R.drawable.bg_priority_high
                    "LOW" -> R.drawable.bg_priority_low
                    else -> R.drawable.bg_priority_normal
                }
                priorityView.setBackgroundResource(bg)

                // Step 2 Animation: fade + slide
                cardTitleMessage.apply {
                    alpha = 0f
                    translationY = 50f
                    animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(450)
                        .start()
                }

                (priorityView.parent.parent as View).apply {
                    alpha = 0f
                    translationY = 50f
                    animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setStartDelay(150)
                        .setDuration(450)
                        .start()
                }
            }
    }
}
