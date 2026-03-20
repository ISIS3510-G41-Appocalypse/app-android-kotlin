package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.rides.RideDto

interface RidesRepository {
    suspend fun getRides(token: String): List<RideDto>?
}

