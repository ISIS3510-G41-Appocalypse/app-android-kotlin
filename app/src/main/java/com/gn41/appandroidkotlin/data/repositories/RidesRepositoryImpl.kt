package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.rides.RideDto
import com.gn41.appandroidkotlin.data.services.rides.RidesService

class RidesRepositoryImpl(private val ridesService: RidesService) : RidesRepository {

    override suspend fun getRides(token: String): List<RideDto>? {
        return ridesService.getRides(token)
    }
}

