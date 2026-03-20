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
    val rider_id: Int,
    val state: String
)

data class UserSimpleDto(
    val id: Int,
    val auth_id: String?
)

data class RiderSimpleDto(
    val id: Int,
    val user_id: Int
)

data class CreateReservationRequest(
    val ride_id: Int,
    val rider_id: Int,
    val meeting_point: String,
    val destination_point: String,
    val state: String = "PENDIENTE",
    val on_time_rider: Boolean = true,
    val on_time_driver: Boolean = true
)

interface ReservationsApi {
    @GET("rest/v1/users")
    suspend fun getUserByAuthId(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("auth_id") authId: String,
        @Query("select") select: String = "id,auth_id"
    ): Response<List<UserSimpleDto>>

    @GET("rest/v1/riders")
    suspend fun getRiderByUserId(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("user_id") userId: String,
        @Query("select") select: String = "id,user_id"
    ): Response<List<RiderSimpleDto>>

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

