package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.location.UserSharedLocationDto
import com.gn41.appandroidkotlin.data.services.location.LocationService
import com.gn41.appandroidkotlin.domain.UserSharedLocation

class LocationRepositoryImpl(
    private val locationService: LocationService
) : LocationRepository {

    override suspend fun saveLocation(
        location: UserSharedLocation,
        token: String
    ): Boolean {
        val dto = UserSharedLocationDto(
            id = location.id,
            user_id = location.userId,
            ride_id = location.rideId,
            latitude = location.latitude,
            longitude = location.longitude,
            timestamp = location.timestamp,
            is_sharing_enabled = location.isSharingEnabled
        )

        return locationService.insertUserLocation(dto, token)
    }

    override suspend fun getLocationsByRide(
        rideId: Int,
        token: String
    ): List<UserSharedLocation> {
        return locationService.getLocationsByRide(rideId, token).map { dto ->
            UserSharedLocation(
                id = dto.id,
                userId = dto.user_id,
                rideId = dto.ride_id,
                latitude = dto.latitude,
                longitude = dto.longitude,
                timestamp = dto.timestamp,
                isSharingEnabled = dto.is_sharing_enabled
            )
        }
    }
}