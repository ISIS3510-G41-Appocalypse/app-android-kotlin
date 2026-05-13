package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.dto.trips.TripDriverDto
import com.gn41.appandroidkotlin.data.dto.trips.TripReservationDto
import com.gn41.appandroidkotlin.data.dto.trips.TripRideDto
import com.gn41.appandroidkotlin.data.dto.trips.TripRiderDto
import com.gn41.appandroidkotlin.data.dto.trips.TripUserDto
import com.gn41.appandroidkotlin.data.services.trips.TripService

class TripRepository(
    private val tripService: TripService,
    private val networkHelper: NetworkHelper
) {

    suspend fun getUserByAuthId(authId: String, token: String): TripUserDto? {
        return tripService.getUserByAuthId(authId, token)
    }

    suspend fun getRiderByUserId(userId: Int, token: String): TripRiderDto? {
        return tripService.getRiderByUserId(userId, token)
    }

    suspend fun getDriverByUserId(userId: Int, token: String): TripDriverDto? {
        return tripService.getDriverByUserId(userId, token)
    }

    suspend fun getActiveRiderReservation(riderId: Int, token: String): List<TripReservationDto> {
        return tripService.getActiveRiderReservation(riderId, token)
    }

    suspend fun getActiveDriverRide(driverId: Int, token: String): TripRideDto? {
        return tripService.getActiveDriverRide(driverId, token)
    }

    suspend fun getReservationsForRide(rideId: Int, token: String): List<TripReservationDto> {
        return tripService.getReservationsForRide(rideId, token)
    }

    suspend fun updateReservationState(reservationId: Int, newState: String, token: String): Boolean {
        return tripService.updateReservationState(reservationId, newState, token)
    }

    suspend fun updateRideState(rideId: Int, newState: String, token: String): Boolean {
        return tripService.updateRideState(rideId, newState, token)
    }

    suspend fun rejectActiveReservationsForRide(rideId: Int, token: String): Boolean {
        return tripService.rejectActiveReservationsForRide(rideId, token)
    }

    fun availableConnection(): Boolean {
        return networkHelper.isInternetAvailable()
    }
}

