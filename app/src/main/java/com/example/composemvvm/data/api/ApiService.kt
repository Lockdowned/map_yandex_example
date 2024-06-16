package com.example.composemvvm.data.api

import com.example.composemvvm.data.api.models.YandexGeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {
    @GET("1.x")
    suspend fun getCoordinates(
        @Query("geocode") geocode: String?,
        @Query("apikey") apiKey: String?,
        @Query("format") format: String?
    ): YandexGeocodingResponse
}