package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reflections")
data class Reflection(
    @PrimaryKey val id: String,
    val storyId: String,
    val authorName: String,
    val authorHandle: String,
    val content: String,
    val timestamp: String,
    val createdAt: Long = System.currentTimeMillis()
)
