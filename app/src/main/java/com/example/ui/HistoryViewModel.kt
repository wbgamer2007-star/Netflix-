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

    private val _history = MutableStateFlow<List<Pair<Movie, UserActivity>>>(emptyList())
    val history = _history.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allActivities.collect { activities ->
                val combined = activities.mapNotNull { activity ->
                    ContentRepository.contentList.find { it.id == activity.movieId }?.let { movie ->
                        movie to activity
                    }
                }
                _history.value = combined
            }
        }
    }
}
