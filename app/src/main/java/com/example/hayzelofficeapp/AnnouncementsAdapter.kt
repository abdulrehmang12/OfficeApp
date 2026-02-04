package com.example.hayzelofficeapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class AnnouncementsAdapter(
    private var announcements: MutableList<Announcement>,
    private var isLoading: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM = 0
    private val LOADING = 1

    inner class AnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val authorTextView: TextView = itemView.findViewById(R.id.authorTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val priorityTextView: TextView = itemView.findViewById(R.id.priorityTextView)
    }

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun getItemViewType(position: Int): Int {
        return if (isLoading && position >= announcements.size) LOADING else ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_announcement, parent, false)
            AnnouncementViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_announcement_placeholder, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) announcements.size + 3 else announcements.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AnnouncementViewHolder) {
            val announcement = announcements[position]
            val context = holder.itemView.context

            // Set Text
            holder.titleTextView.text = announcement.title ?: "No Title"
            holder.messageTextView.text = announcement.message ?: "No Message"
            holder.authorTextView.text = "By ${announcement.createdBy ?: "Admin"}"

            // Date
            val timestamp: Timestamp? = announcement.createdAt
            holder.timeTextView.text = timestamp?.let {
                SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault()).format(it.toDate())
            } ?: ""

            // Priority
            val priority = (announcement.priority ?: "NORMAL").uppercase()
            holder.priorityTextView.text = priority
            when (priority) {
                "HIGH", "URGENT" -> {
                    holder.priorityTextView.setTextColor(
                        ContextCompat.getColor(context, android.R.color.holo_red_dark)
                    )
                    holder.priorityTextView.setBackgroundResource(R.drawable.bg_priority_high)
                }
                "LOW" -> {
                    holder.priorityTextView.setTextColor(
                        ContextCompat.getColor(context, android.R.color.holo_green_dark)
                    )
                    holder.priorityTextView.setBackgroundResource(R.drawable.bg_priority_low)
                }
                else -> {
                    holder.priorityTextView.setTextColor(
                        ContextCompat.getColor(context, android.R.color.holo_blue_dark)
                    )
                    holder.priorityTextView.setBackgroundResource(R.drawable.bg_priority_normal)
                }
            }

            // Click
            holder.itemView.setOnClickListener {
                announcement.id?.let { id ->
                    val intent = Intent(context, AnnouncementDetailActivity::class.java)
                    intent.putExtra("id", id)
                    context.startActivity(intent)
                }
            }

            // Slide + Fade Animation
            holder.itemView.alpha = 0f
            holder.itemView.translationY = 100f
            holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(position * 50L)
                .start()

            // Elevation on Touch
            holder.itemView.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> holder.itemView.elevation = 16f
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> holder.itemView.elevation = 8f
                }
                false
            }
        }
    }

    fun updateAnnouncements(newAnnouncements: List<Announcement>) {
        isLoading = false
        announcements.clear()
        announcements.addAll(newAnnouncements)
        notifyDataSetChanged()
    }

    fun setLoading(loading: Boolean) {
        isLoading = loading
        notifyDataSetChanged()
    }
}
