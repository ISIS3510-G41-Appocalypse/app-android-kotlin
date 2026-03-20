package com.gn41.appandroidkotlin.data.services.reservations

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

data class ReservationDto(
    val id: Int,
    val ride_id: Int,
    val rider_id: String,
    val state: String
)

data class CreateReservationRequest(
    val ride_id: Int,
    val rider_id: String,
    val state: String = "PENDIENTE"
)

interface ReservationsApi {
    @GET("rest/v1/reservations")
    suspend fun getReservations(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("rider_id") riderId: String,
        @Query("select") select: String = "id,ride_id,rider_id,state"
    ): Response<List<ReservationDto>>

    @POST("rest/v1/reservations")
    suspend fun createReservation(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body request: CreateReservationRequest
    ): Response<List<ReservationDto>>
}

