package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ResourceType(val label: String) {
    ALL("All Resources"),
    VIDEO("Video Lectures"),
    FILE_DOCUMENT("Files & Docs"),
    WEB_ARTICLE("Web Articles")
}

@Entity(tableName = "educational_resources")
data class EducationalResource(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val resourceType: String, // "VIDEO", "FILE_DOCUMENT", "WEB_ARTICLE"
    val subject: String,      // "COMPUTER_SCIENCE", "MATHEMATICS", "PHYSICS", "HUMANITIES", "DESIGN"
    val authorName: String,
    val authorHandle: String,
    val authorInstitution: String,
    val mediaUrl: String = "",
    val fileFormat: String = "PDF", // "PDF", "MP4", "ZIP", "HTML", "CODE"
    val fileSizeString: String = "",
    val durationMinutes: Int = 0,
    val content: String,
    val difficulty: String = "Intermediate", // "Beginner", "Intermediate", "Advanced"
    val isBookmarked: Boolean = false,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val downloadCount: Int = 0,
    val coverDrawableName: String = "img_editorial_architecture",
    val createdAt: Long = System.currentTimeMillis()
)
