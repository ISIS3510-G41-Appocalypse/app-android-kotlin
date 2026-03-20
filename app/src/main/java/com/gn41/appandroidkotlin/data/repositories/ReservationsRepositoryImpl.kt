package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.services.reservations.RiderSimpleDto
import com.gn41.appandroidkotlin.data.services.reservations.ReservationDto
import com.gn41.appandroidkotlin.data.services.reservations.ReservationsService
import com.gn41.appandroidkotlin.data.services.reservations.UserSimpleDto

class ReservationsRepositoryImpl(
    private val reservationsService: ReservationsService
) : ReservationsRepository {

    override suspend fun getUserByAuthId(authId: String, token: String): UserSimpleDto? {
        return reservationsService.getUserByAuthId(authId, token)
    }

    override suspend fun getRiderByUserId(userId: Int, token: String): RiderSimpleDto? {
        return reservationsService.getRiderByUserId(userId, token)
    }

    override suspend fun getReservations(riderId: Int, token: String): List<ReservationDto>? {
        return reservationsService.getReservations(riderId, token)
    }

    override suspend fun createReservation(
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
