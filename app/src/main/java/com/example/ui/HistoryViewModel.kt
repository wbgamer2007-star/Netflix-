package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.LocalDataRepository
import com.example.data.Movie
import com.example.data.UserActivity
import com.example.data.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LocalDataRepository(AppDatabase.getDatabase(application).userActivityDao())

    private val _watchHistory = MutableStateFlow<List<Pair<Movie, UserActivity>>>(emptyList())
    val watchHistory = _watchHistory.asStateFlow()

    private val _likedMovies = MutableStateFlow<List<Movie>>(emptyList())
    val likedMovies = _likedMovies.asStateFlow()

    private val _savedMovies = MutableStateFlow<List<Movie>>(emptyList())
    val savedMovies = _savedMovies.asStateFlow()

    // For backwards compatibility
    val history = _watchHistory

    init {
        viewModelScope.launch {
            repository.allActivities.collect { activities ->
                val watchList = mutableListOf<Pair<Movie, UserActivity>>()
                val likedList = mutableListOf<Movie>()
                val savedList = mutableListOf<Movie>()

                for (activity in activities) {
                    val movie = ContentRepository.contentList.find { 
                        it.id == activity.movieId || activity.movieId.startsWith(it.id + "_") 
                    }
                    if (movie != null) {
                        if (activity.progress > 0) {
                            if (watchList.none { it.first.id == movie.id }) {
                                watchList.add(movie to activity)
                            }
                        }
                        if (activity.isLiked) {
                            if (likedList.none { it.id == movie.id }) {
                                likedList.add(movie)
                            }
                        }
                        if (activity.isSaved) {
                            if (savedList.none { it.id == movie.id }) {
                                savedList.add(movie)
                            }
                        }
                    }
                }

                _watchHistory.value = watchList
                _likedMovies.value = likedList
                _savedMovies.value = savedList
            }
        }
    }

    fun deleteFromHistory(movieId: String) {
        viewModelScope.launch {
            val activity = repository.getActivitySync(movieId)
            if (activity != null) {
                if (activity.isLiked || activity.isSaved) {
                    // Reset watch progress to clear from watch history, but keep liked/saved status
                    repository.saveProgress(movieId, 0L, 0L)
                } else {
                    repository.deleteActivity(movieId)
                }
            }
        }
    }
}
