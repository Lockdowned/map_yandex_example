package com.example.composemvvm.data

interface IMapRepository {
    suspend fun getCoordinates(address: String): Pair<Double, Double>
}