package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserActivityDao {
    @Query("SELECT * FROM user_activity WHERE movieId = :id")
    fun getActivity(id: String): Flow<UserActivity?>

    @Query("SELECT * FROM user_activity WHERE movieId = :id")
    suspend fun getActivitySync(id: String): UserActivity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: UserActivity)

    @Query("UPDATE user_activity SET progress = :progress, duration = :duration, lastWatched = :timestamp WHERE movieId = :id")
    suspend fun updateProgress(id: String, progress: Long, duration: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_activity SET isLiked = :isLiked WHERE movieId = :id")
    suspend fun updateLiked(id: String, isLiked: Boolean)
    
    @Query("SELECT * FROM user_activity ORDER BY lastWatched DESC")
    fun getAllActivities(): Flow<List<UserActivity>>
}
