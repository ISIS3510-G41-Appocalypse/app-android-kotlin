package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.createRide.ActiveRideDto
import com.gn41.appandroidkotlin.data.dto.createRide.CreateRideRequestDto

interface RideRepository {
    suspend fun createRide(request: CreateRideRequestDto): Result<Unit>
    suspend fun cancelRide(id: Int): Result<Unit>

    suspend fun getActiveRide(): ActiveRideDto?
}
