package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ContentRepository
import com.example.data.Movie
import com.example.data.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MoviesUiState {
    object Loading : MoviesUiState()
    data class Success(
        val heroMovie: Movie?,
        val categories: Map<String, List<Movie>>
    ) : MoviesUiState()
    data class Error(val message: String) : MoviesUiState()
}

class MoviesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<MoviesUiState>(MoviesUiState.Loading)
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private var allMovies: List<Movie> = emptyList()

    init {
        fetchMovies()
    }

    fun fetchMovies() {
        viewModelScope.launch {
            _uiState.value = MoviesUiState.Loading
            try {
                val response = NetworkModule.apiService.getContent()
                if (response != null) {
                    val moviesList = response.map { (key, value) -> value.apply { id = key } }.sortedByDescending { it.timestamp }
                    ContentRepository.contentList = moviesList
                    allMovies = moviesList
                    processMovies(moviesList)
                } else {
                    _uiState.value = MoviesUiState.Success(null, emptyMap())
                }
            } catch (e: Exception) {
                _uiState.value = MoviesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        val filtered = if (query.isBlank()) {
            allMovies
        } else {
            allMovies.filter { it.title.contains(query, ignoreCase = true) }
        }
        processMovies(filtered)
    }

    private fun processMovies(movies: List<Movie>) {
        val heroMovie = movies.firstOrNull { it.isHero } ?: movies.firstOrNull()
        val categories = movies.groupBy { it.category }
        _uiState.value = MoviesUiState.Success(heroMovie, categories)
    }
}
