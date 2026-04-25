package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.dto.createRide.CreateRideRequestDto
import com.gn41.appandroidkotlin.data.services.rides.RideService

class RideRepositoryImpl(private val rideService: RideService,
    private val networkHelper: NetworkHelper
) : RideRepository {
    override suspend fun createRide(request: CreateRideRequestDto) : Result<Unit> {
        return rideService.create(request)
    }

    override fun availableConnection() : Boolean {
        return networkHelper.isInternetAvailable()
    }
}