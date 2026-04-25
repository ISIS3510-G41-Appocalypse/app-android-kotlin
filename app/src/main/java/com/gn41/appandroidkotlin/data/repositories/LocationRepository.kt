package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.domain.UserSharedLocation

data class LocationResult(
    val locations: List<UserSharedLocation>,
    val isFromCache: Boolean,
    val message: String = ""
)

interface LocationRepository {
    suspend fun saveLocation(location: UserSharedLocation, token: String): Boolean
    suspend fun getLocationsByRide(rideId: Int, token: String): LocationResult
}