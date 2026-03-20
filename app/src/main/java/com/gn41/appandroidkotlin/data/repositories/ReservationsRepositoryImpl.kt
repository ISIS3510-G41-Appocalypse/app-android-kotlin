package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.services.reservations.ReservationDto
import com.gn41.appandroidkotlin.data.services.reservations.ReservationsService

class ReservationsRepositoryImpl(
    private val reservationsService: ReservationsService
) : ReservationsRepository {
    override suspend fun getReservations(userId: String, token: String): List<ReservationDto>? {
        return reservationsService.getReservations(userId, token)
    }

    override suspend fun createReservation(rideId: Int, userId: String, token: String): Boolean {
        return reservationsService.createReservation(rideId, userId, token)
    }
}

