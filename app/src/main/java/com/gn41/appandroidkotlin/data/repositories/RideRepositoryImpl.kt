package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.createRide.ActiveRideDto
import com.gn41.appandroidkotlin.data.dto.createRide.CreateRideRequestDto
import com.gn41.appandroidkotlin.data.services.rides.RideService

class RideRepositoryImpl(private val rideService: RideService) : RideRepository {
    override suspend fun createRide(request: CreateRideRequestDto) : Result<Unit> {
        return rideService.create(request)
    }

    override suspend fun cancelRide(id: Int) : Result<Unit> {
        return rideService.cancelRide(id)
    }

    override suspend fun getActiveRide(): ActiveRideDto? {
        return rideService.getActiveRide()
    }
}