package com.example.data

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json

@JsonClass(generateAdapter = true)
data class Movie(
    @Json(name = "_ignore_id") val _ignore: String? = null,
    val title: String = "",
    val description: String = "",
    val type: String = "movie",
    val videoUrl: String = "",
    val category: String = "",
    val year: Int = 0,
    val posterUrl: String = "",
    val isHero: Boolean = false,
    val language: String = "",
    val timestamp: Long = 0L,
    val seasons: List<Season>? = null
) {
    @Transient var id: String = ""
}

@JsonClass(generateAdapter = true)
data class Season(
    val title: String = "",
    val episodes: List<Episode>? = null
)

@JsonClass(generateAdapter = true)
data class Episode(
    val title: String = "",
    val videoUrl: String = ""
)

object ContentRepository {
    var contentList: List<Movie> = emptyList()
}
