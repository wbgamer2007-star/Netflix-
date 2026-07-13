package com.example.data

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

interface FirebaseApiService {
    @GET("content.json")
    suspend fun getContent(): Map<String, Movie>?
}

object NetworkModule {
    private const val BASE_URL = "https://testing-31c44-default-rtdb.asia-southeast1.firebasedatabase.app/"

    val apiService: FirebaseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(FirebaseApiService::class.java)
    }
}
