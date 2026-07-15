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
        val heroMovies: List<Movie>,
        val categories: Map<String, List<Movie>>
    ) : MoviesUiState()
    data class Error(val message: String) : MoviesUiState()
}

data class SearchResultState(
    val query: String = "",
    val bestMatch: Movie? = null,
    val recommendedMovies: List<Movie> = emptyList(),
    val otherMatches: List<Movie> = emptyList()
)

class MoviesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<MoviesUiState>(MoviesUiState.Loading)
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResult = MutableStateFlow<SearchResultState?>(null)
    val searchResult = _searchResult.asStateFlow()

    private var allMovies: List<Movie> = emptyList()

    init {
        fetchMovies()
    }

    fun fetchMovies() {
        viewModelScope.launch {
            _uiState.value = MoviesUiState.Loading
            try {
                // Speed Boost: If cache is already present, serve instantly!
                if (ContentRepository.contentList.isNotEmpty()) {
                    allMovies = ContentRepository.contentList
                    processMovies(allMovies)
                    return@launch
                }

                val response = NetworkModule.apiService.getContent()
                if (response != null) {
                    val moviesList = response.map { (key, value) -> value.apply { id = key } }.sortedByDescending { it.timestamp }
                    ContentRepository.contentList = moviesList
                    allMovies = moviesList
                    processMovies(moviesList)
                } else {
                    _uiState.value = MoviesUiState.Success(emptyList(), emptyMap())
                }
            } catch (e: Exception) {
                // Offline fallback: If network call fails but cache is available, use cache!
                if (ContentRepository.contentList.isNotEmpty()) {
                    allMovies = ContentRepository.contentList
                    processMovies(allMovies)
                } else {
                    _uiState.value = MoviesUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResult.value = null
            processMovies(allMovies)
        } else {
            val q = query.trim().lowercase()
            
            // 1. Find Title Matches, sorting exact match first, then startsWith, then contains
            val titleMatches = allMovies.filter { it.title.lowercase().contains(q) }
            val sortedTitleMatches = titleMatches.sortedWith(compareBy(
                { !it.title.lowercase().equals(q) },
                { !it.title.lowercase().startsWith(q) }
            ))
            
            val bestMatch = sortedTitleMatches.firstOrNull()
            
            // 2. Fetch 7 to 10 recommended movies of the SAME genre/category as best match
            val recommendedMovies = if (bestMatch != null) {
                val genres = bestMatch.category.split(",").map { it.trim().lowercase() }
                allMovies.filter { movie ->
                    movie.id != bestMatch.id && movie.category.split(",").any { cat -> 
                        genres.contains(cat.trim().lowercase())
                    }
                }.take(10)
            } else {
                emptyList()
            }
            
            // 3. Find Category/Genre matches
            val genreMatches = allMovies.filter { movie ->
                movie.category.split(",").any { cat -> cat.trim().lowercase().contains(q) }
            }
            
            // 4. Combine other matches
            val otherMatches = (sortedTitleMatches.drop(1) + genreMatches)
                .distinctBy { it.id }
                .filter { it.id != bestMatch?.id }
            
            _searchResult.value = SearchResultState(
                query = query,
                bestMatch = bestMatch,
                recommendedMovies = recommendedMovies,
                otherMatches = otherMatches
            )
        }
    }

    private fun processMovies(movies: List<Movie>) {
        val heroMovies = movies.filter { it.isHero }.take(5)
        val finalHeroMovies = if (heroMovies.isNotEmpty()) heroMovies else movies.take(1)
        
        val categories = mutableMapOf<String, MutableList<Movie>>()
        for (movie in movies) {
            val cats = movie.category.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (cats.isEmpty()) {
                val list = categories.getOrPut("Uncategorized") { mutableListOf() }
                if (list.size < 10) {
                    list.add(movie)
                }
            } else {
                for (cat in cats) {
                    val list = categories.getOrPut(cat) { mutableListOf() }
                    if (list.size < 10) {
                        list.add(movie)
                    }
                }
            }
        }
        _uiState.value = MoviesUiState.Success(finalHeroMovies, categories)
    }
}
