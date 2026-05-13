package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.dto.createRide.CreateRideRequestDto
import com.gn41.appandroidkotlin.data.services.rides.RideService
import com.gn41.appandroidkotlin.localStorage.LocalStorageManager

class RideRepository(private val rideService: RideService,
    private val networkHelper: NetworkHelper,
    private val localStorageManager: LocalStorageManager
) {
    suspend fun createRide(request: CreateRideRequestDto) : Result<Unit> {
        return rideService.create(request)
    }

/*    override suspend fun cancelRide(id: Int) : Result<Unit> {
        return rideService.cancelRide(id)
    }

    override suspend fun getActiveRide(): ActiveRideDto? {
        return rideService.getActiveRide()
    }

    override suspend fun getRideUsers(id: Int, state: String): List<RideUserDto>? {
        return rideService.getRideUsers(id, state)
    }*/

    fun availableConnection() : Boolean {
        return networkHelper.isInternetAvailable()
    }

    suspend fun saveCache() {
        localStorageManager.saveFormState()
    }

    suspend fun readLocalStorage(): String {
        return localStorageManager.readFormState()
    }

    suspend fun clearLocalStorage() {
        localStorageManager.clearFormState()
    }
}