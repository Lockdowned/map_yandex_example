package com.example.composemvvm.domain

import com.example.composemvvm.data.IMapRepository
import javax.inject.Inject

interface IMapUseCase {
    suspend fun getCoordinates(address: String): Pair<Double, Double>
}

class GetCoordinatesUseCase @Inject constructor(private val mapRepository: IMapRepository) :
    IMapUseCase {
    override suspend fun getCoordinates(address: String): Pair<Double, Double> =
        mapRepository.getCoordinates(address)
}