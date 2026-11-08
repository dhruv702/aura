package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EducationalResource
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {
    @Query("SELECT * FROM educational_resources ORDER BY createdAt DESC")
    fun getAllResources(): Flow<List<EducationalResource>>

    @Query("SELECT * FROM educational_resources WHERE resourceType = :type ORDER BY createdAt DESC")
    fun getResourcesByType(type: String): Flow<List<EducationalResource>>

    @Query("SELECT * FROM educational_resources WHERE subject = :subject ORDER BY createdAt DESC")
    fun getResourcesBySubject(subject: String): Flow<List<EducationalResource>>

    @Query("SELECT * FROM educational_resources WHERE isBookmarked = 1 ORDER BY createdAt DESC")
    fun getBookmarkedResources(): Flow<List<EducationalResource>>

    @Query("SELECT * FROM educational_resources WHERE authorHandle = '@you' ORDER BY createdAt DESC")
    fun getMyUploadedResources(): Flow<List<EducationalResource>>

    @Query("SELECT * FROM educational_resources WHERE id = :resourceId LIMIT 1")
    suspend fun getResourceById(resourceId: String): EducationalResource?

    @Query("SELECT * FROM educational_resources WHERE id = :resourceId LIMIT 1")
    fun observeResourceById(resourceId: String): Flow<EducationalResource?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(resources: List<EducationalResource>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: EducationalResource)

    @Update
    suspend fun updateResource(resource: EducationalResource)

    @Query("UPDATE educational_resources SET isBookmarked = :isBookmarked WHERE id = :resourceId")
    suspend fun updateBookmark(resourceId: String, isBookmarked: Boolean)

    @Query("UPDATE educational_resources SET likesCount = likesCount + :delta, isLiked = :isLiked WHERE id = :resourceId")
    suspend fun updateLike(resourceId: String, delta: Int, isLiked: Boolean)

    @Query("UPDATE educational_resources SET downloadCount = downloadCount + 1 WHERE id = :resourceId")
    suspend fun incrementDownload(resourceId: String)

    @Query("SELECT COUNT(*) FROM educational_resources")
    suspend fun getResourceCount(): Int
}
