package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_activity")
data class UserActivity(
    @PrimaryKey val movieId: String,
    val progress: Long = 0L,
    val duration: Long = 0L,
    val isLiked: Boolean = false,
    val lastWatched: Long = System.currentTimeMillis()
)
