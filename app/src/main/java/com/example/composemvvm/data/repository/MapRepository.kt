package com.example.composemvvm.data.repository

import com.example.composemvvm.BuildConfig
import com.example.composemvvm.data.IMapRepository
import com.example.composemvvm.data.api.ApiService
import com.example.composemvvm.domain.IMapUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapRepository @Inject constructor(val apiService: ApiService) :
    IMapRepository {

    override suspend fun getCoordinates(address: String): Pair<Double, Double> {
        val response = apiService.getCoordinates(
            format = "json",
            geocode = address,
            apiKey = BuildConfig.GEO_API_KEY
        )
        return parseCoordinatesFromLocationString(response.response.GeoObjectCollection.featureMember.first().GeoObject.Point.pos)
    }

    private fun parseCoordinatesFromLocationString(locationString: String): Pair<Double, Double> {
        val parts = locationString.split(" ")
        return Pair(parts[1].toDouble(), parts[0].toDouble())
    }
}