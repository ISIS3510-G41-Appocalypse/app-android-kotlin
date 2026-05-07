package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.rides.RideDto
import com.gn41.appandroidkotlin.data.services.rides.RidesService

class RidesRepositoryImpl(private val ridesService: RidesService) : RidesRepository {

    override suspend fun getRides(token: String): List<RideDto>? {
        return ridesService.getRides(token)
    }

    override suspend fun getRiderDriverRecommendation(
        riderId: Int,
        driverId: Int,
        token: String
    ): Double? {
        return ridesService.getRiderDriverRecommendation(riderId, driverId, token)
    }
}

