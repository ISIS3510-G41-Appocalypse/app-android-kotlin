package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.dto.createRide.CreateRideRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface RideRepository {
    suspend fun createRide(request: CreateRideRequestDto): Result<Unit>

    fun availableConnection(): Boolean

    suspend fun saveCache()

    suspend fun readLocalStorage():String

    suspend fun clearLocalStorage()
}