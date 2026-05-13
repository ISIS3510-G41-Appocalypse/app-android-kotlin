package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.services.reservations.RiderSimpleDto
import com.gn41.appandroidkotlin.data.services.reservations.ReservationDto
import com.gn41.appandroidkotlin.data.services.reservations.ReservationsService
import com.gn41.appandroidkotlin.data.services.reservations.UserSimpleDto

class ReservationsRepository(
    private val reservationsService: ReservationsService
) {

    suspend fun getUserByAuthId(authId: String, token: String): UserSimpleDto? {
        return reservationsService.getUserByAuthId(authId, token)
    }

    suspend fun getRiderByUserId(userId: Int, token: String): RiderSimpleDto? {
        return reservationsService.getRiderByUserId(userId, token)
    }

    suspend fun getReservations(riderId: Int, token: String): List<ReservationDto>? {
        return reservationsService.getReservations(riderId, token)
    }

    suspend fun createReservation(
        rideId: Int,
        riderId: Int,
        meetingPoint: String,
        destinationPoint: String,
        token: String
    ): Boolean {
        return reservationsService.createReservation(
            rideId = rideId,
            riderId = riderId,
            meetingPoint = meetingPoint,
            destinationPoint = destinationPoint,
            token = token
        )
    }
}
