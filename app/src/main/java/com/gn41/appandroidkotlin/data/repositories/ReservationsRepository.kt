package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.services.reservations.RiderSimpleDto
import com.gn41.appandroidkotlin.data.services.reservations.ReservationDto
import com.gn41.appandroidkotlin.data.services.reservations.UserSimpleDto

interface ReservationsRepository {
    suspend fun getUserByAuthId(authId: String, token: String): UserSimpleDto?
    suspend fun getRiderByUserId(userId: Int, token: String): RiderSimpleDto?
    suspend fun getReservations(riderId: Int, token: String): List<ReservationDto>?
    suspend fun createReservation(
        rideId: Int,
        riderId: Int,
        meetingPoint: String,
        destinationPoint: String,
        token: String
    ): Boolean
}

