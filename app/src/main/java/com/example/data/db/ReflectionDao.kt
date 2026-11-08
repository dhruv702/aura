package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Reflection
import kotlinx.coroutines.flow.Flow

@Dao
interface ReflectionDao {
    @Query("SELECT * FROM reflections WHERE storyId = :storyId ORDER BY createdAt ASC")
    fun getReflectionsForStory(storyId: String): Flow<List<Reflection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReflection(reflection: Reflection)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReflections(reflections: List<Reflection>)
}
