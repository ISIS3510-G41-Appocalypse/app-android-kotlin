package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.trips.TripDriverDto
import com.gn41.appandroidkotlin.data.dto.trips.TripReservationDto
import com.gn41.appandroidkotlin.data.dto.trips.TripRideDto
import com.gn41.appandroidkotlin.data.dto.trips.TripRiderDto
import com.gn41.appandroidkotlin.data.dto.trips.TripUserDto

interface TripRepository {
    suspend fun getUserByAuthId(authId: String, token: String): TripUserDto?
    suspend fun getRiderByUserId(userId: Int, token: String): TripRiderDto?
    suspend fun getDriverByUserId(userId: Int, token: String): TripDriverDto?
    suspend fun getActiveRiderReservation(riderId: Int, token: String): List<TripReservationDto>
    suspend fun getActiveDriverRide(driverId: Int, token: String): TripRideDto?
    suspend fun getReservationsForRide(rideId: Int, token: String): List<TripReservationDto>
    suspend fun updateReservationState(reservationId: Int, newState: String, token: String): Boolean
    suspend fun updateRideState(rideId: Int, newState: String, token: String): Boolean
}

