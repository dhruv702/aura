package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Story
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    fun getAllStories(): Flow<List<Story>>

    @Query("SELECT * FROM stories WHERE category = :category ORDER BY createdAt DESC")
    fun getStoriesByCategory(category: String): Flow<List<Story>>

    @Query("SELECT * FROM stories WHERE isBookmarked = 1 ORDER BY createdAt DESC")
    fun getBookmarkedStories(): Flow<List<Story>>

    @Query("SELECT * FROM stories WHERE authorHandle = '@you' ORDER BY createdAt DESC")
    fun getMyPublishedStories(): Flow<List<Story>>

    @Query("SELECT * FROM stories WHERE id = :storyId LIMIT 1")
    suspend fun getStoryById(storyId: String): Story?

    @Query("SELECT * FROM stories WHERE id = :storyId LIMIT 1")
    fun observeStoryById(storyId: String): Flow<Story?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<Story>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: Story)

    @Update
    suspend fun updateStory(story: Story)

    @Query("UPDATE stories SET applaudCount = applaudCount + :delta, isApplauded = :isApplauded WHERE id = :storyId")
    suspend fun updateApplaud(storyId: String, delta: Int, isApplauded: Boolean)

    @Query("UPDATE stories SET isBookmarked = :isBookmarked WHERE id = :storyId")
    suspend fun updateBookmark(storyId: String, isBookmarked: Boolean)

    @Query("SELECT COUNT(*) FROM stories")
    suspend fun getStoryCount(): Int
}
