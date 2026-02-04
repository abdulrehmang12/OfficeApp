package com.example.hayzelofficeapp

import com.google.firebase.Timestamp

data class Announcement(
    val id: String = "",
    val title: String? = null,
    val message: String? = null,
    val createdBy: String? = null,
    val createdAt: Timestamp? = null,
    val expiryAt: Timestamp? = null,
    val priority: String? = "NORMAL",
    val category: String? = "General"
)
