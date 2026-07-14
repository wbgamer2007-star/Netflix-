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

    fun loadProgress(movieId: String) {
        viewModelScope.launch {
            val activity = repository.getActivitySync(movieId)
            _startPosition.value = activity?.progress ?: 0L
        }
    }

    fun saveProgress(movieId: String, progress: Long, duration: Long) {
        viewModelScope.launch {
            repository.saveProgress(movieId, progress, duration)
        }
    }
}
