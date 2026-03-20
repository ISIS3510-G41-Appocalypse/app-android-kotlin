package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.services.reservations.ReservationDto

interface ReservationsRepository {
    suspend fun getReservations(userId: String, token: String): List<ReservationDto>?
    suspend fun createReservation(rideId: Int, userId: String, token: String): Boolean
}

