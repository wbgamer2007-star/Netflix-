package com.example.data

import kotlinx.coroutines.flow.Flow

class LocalDataRepository(private val dao: UserActivityDao) {
    val allActivities: Flow<List<UserActivity>> = dao.getAllActivities()

    fun getActivity(movieId: String): Flow<UserActivity?> = dao.getActivity(movieId)
    
    suspend fun getActivitySync(movieId: String): UserActivity? = dao.getActivitySync(movieId)

    suspend fun saveProgress(movieId: String, progress: Long, duration: Long) {
        val existing = dao.getActivitySync(movieId)
        if (existing != null) {
            dao.updateProgress(movieId, progress, duration)
        } else {
            dao.insertActivity(UserActivity(movieId = movieId, progress = progress, duration = duration))
        }
    }

    suspend fun toggleLiked(movieId: String, isLiked: Boolean) {
        val existing = dao.getActivitySync(movieId)
        if (existing != null) {
            dao.updateLiked(movieId, isLiked)
        } else {
            dao.insertActivity(UserActivity(movieId = movieId, isLiked = isLiked))
        }
    }
}
