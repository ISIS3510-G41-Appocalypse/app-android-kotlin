package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.domain.UserSharedLocation

interface LocationRepository {
    suspend fun saveLocation(location: UserSharedLocation, token: String): Boolean
    suspend fun getLocationsByRide(rideId: Int, token: String): List<UserSharedLocation>
}