package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.LocalDataRepository
import com.example.data.UserActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LocalDataRepository(AppDatabase.getDatabase(application).userActivityDao())

    private val _startPosition = MutableStateFlow(0L)
    val startPosition = _startPosition.asStateFlow()

    private val _activeActivity = MutableStateFlow<UserActivity?>(null)
    val activeActivity = _activeActivity.asStateFlow()

    private var collectJob: kotlinx.coroutines.Job? = null

    fun loadProgress(movieId: String) {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            repository.getActivity(movieId).collect { activity ->
                _startPosition.value = activity?.progress ?: 0L
                _activeActivity.value = activity
            }
        }
    }

    fun saveProgress(movieId: String, progress: Long, duration: Long) {
        viewModelScope.launch {
            repository.saveProgress(movieId, progress, duration)
        }
    }

    fun toggleLiked(movieId: String, isLiked: Boolean) {
        viewModelScope.launch {
            repository.toggleLiked(movieId, isLiked)
        }
    }

    fun toggleSaved(movieId: String, isSaved: Boolean) {
        viewModelScope.launch {
            repository.toggleSaved(movieId, isSaved)
        }
    }
}
