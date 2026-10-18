package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_feedbacks")
data class AppFeedback(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rating: Int = 5,                         // 1..5 stars
    val category: String = "General",           // "General", "Bug Report", "Feature Request", "Verse Correction", "Praise"
    val userName: String = "",
    val userEmail: String = "",
    val comments: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Received"              // "Received", "Reviewed", "Resolved"
)
