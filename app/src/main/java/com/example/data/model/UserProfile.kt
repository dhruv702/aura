package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String = "current_user",
    val name: String = "Alex Vance",
    val handle: String = "@you",
    val institution: String = "Open Science & Tech Educator",
    val bio: String = "Educator and lifelong learner. Believer in open-access knowledge for everyone.",
    val role: String = "Educator",
    val avatarLetter: String = "A",
    val joinedDate: String = "November 2026",
    val totalUploadsCount: Int = 0,
    val totalDownloadsHelped: Int = 0
)
