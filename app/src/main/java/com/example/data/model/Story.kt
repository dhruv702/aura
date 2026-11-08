package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class Story(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val authorName: String,
    val authorHandle: String,
    val authorBio: String,
    val coverDrawableName: String = "",
    val coverImageUrl: String = "",
    val content: String,
    val category: String,
    val readTimeMinutes: Int,
    val publishDate: String,
    val applaudCount: Int = 0,
    val isApplauded: Boolean = false,
    val isBookmarked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
