package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Movie(
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val category: String = "",
    val year: Int = 0,
    val posterUrl: String = "",
    val isHero: Boolean = false,
    val timestamp: Long = 0L
)

@JsonClass(generateAdapter = true)
data class MoviesResponse(
    val movies: Map<String, Movie>? = null
)
