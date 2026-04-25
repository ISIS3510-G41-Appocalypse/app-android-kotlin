package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.dto.createRide.CreateRideRequestDto
import com.gn41.appandroidkotlin.data.services.rides.RideService
import com.gn41.appandroidkotlin.localStorage.LocalStorageManager

class RideRepositoryImpl(private val rideService: RideService,
    private val networkHelper: NetworkHelper,
    private val localStorageManager: LocalStorageManager
) : RideRepository {
    override suspend fun createRide(request: CreateRideRequestDto) : Result<Unit> {
        return rideService.create(request)
    }

    override fun availableConnection() : Boolean {
        return networkHelper.isInternetAvailable()
    }

    override suspend fun saveCache() {
        localStorageManager.saveFormState()
    }

    override suspend fun readLocalStorage(): String {
        return localStorageManager.readFormState()
    }

    override suspend fun clearLocalStorage() {
        localStorageManager.clearFormState()
    }
}