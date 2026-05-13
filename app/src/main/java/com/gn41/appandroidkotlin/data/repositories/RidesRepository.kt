package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.rides.RideDto
import com.gn41.appandroidkotlin.data.services.rides.RidesService

class RidesRepository(private val ridesService: RidesService) {

    suspend fun getRides(token: String): List<RideDto>? {
        return ridesService.getRides(token)
    }

    suspend fun getRiderDriverRecommendation(
        riderId: Int,
        driverId: Int,
        token: String
    ): Double? {
        return ridesService.getRiderDriverRecommendation(riderId, driverId, token)
    }
}

