package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.dto.trips.TripDriverDto
import com.gn41.appandroidkotlin.data.dto.trips.TripReservationDto
import com.gn41.appandroidkotlin.data.dto.trips.TripRideDto
import com.gn41.appandroidkotlin.data.dto.trips.TripRiderDto
import com.gn41.appandroidkotlin.data.dto.trips.TripUserDto
import com.gn41.appandroidkotlin.data.services.trips.TripService

class TripRepositoryImpl(
    private val tripService: TripService,
    private val networkHelper: NetworkHelper
) : TripRepository {

    override suspend fun getUserByAuthId(authId: String, token: String): TripUserDto? {
        return tripService.getUserByAuthId(authId, token)
    }

    override suspend fun getRiderByUserId(userId: Int, token: String): TripRiderDto? {
        return tripService.getRiderByUserId(userId, token)
    }

    override suspend fun getDriverByUserId(userId: Int, token: String): TripDriverDto? {
        return tripService.getDriverByUserId(userId, token)
    }

    override suspend fun getActiveRiderReservation(riderId: Int, token: String): List<TripReservationDto> {
        return tripService.getActiveRiderReservation(riderId, token)
    }

    override suspend fun getActiveDriverRide(driverId: Int, token: String): TripRideDto? {
        return tripService.getActiveDriverRide(driverId, token)
    }

    override suspend fun getReservationsForRide(rideId: Int, token: String): List<TripReservationDto> {
        return tripService.getReservationsForRide(rideId, token)
    }

    override suspend fun updateReservationState(reservationId: Int, newState: String, token: String): Boolean {
        return tripService.updateReservationState(reservationId, newState, token)
    }

    override suspend fun updateRideState(rideId: Int, newState: String, token: String): Boolean {
        return tripService.updateRideState(rideId, newState, token)
    }

    override fun availableConnection(): Boolean {
        return networkHelper.isInternetAvailable()
    }
}

