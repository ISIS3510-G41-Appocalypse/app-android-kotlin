package com.gn41.appandroidkotlin.data.services.trips

import com.gn41.appandroidkotlin.data.dto.trips.TripDriverDto
import com.gn41.appandroidkotlin.data.dto.trips.TripReservationDto
import com.gn41.appandroidkotlin.data.dto.trips.TripRideDto
import com.gn41.appandroidkotlin.data.dto.trips.TripRiderDto
import com.gn41.appandroidkotlin.data.dto.trips.TripUserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Query

interface TripApi {
    @GET("rest/v1/users")
    suspend fun getUserByAuthId(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("auth_id") authId: String,
        @Query("select") select: String = "id,auth_id"
    ): Response<List<TripUserDto>>

    @GET("rest/v1/riders")
    suspend fun getRiderByUserId(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("user_id") userId: String,
        @Query("select") select: String = "id,user_id"
    ): Response<List<TripRiderDto>>

    @GET("rest/v1/drivers")
    suspend fun getDriverByUserId(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("user_id") userId: String,
        @Query("select") select: String = "id,user_id"
    ): Response<List<TripDriverDto>>

    @GET("rest/v1/reservations")
    suspend fun getActiveRiderReservation(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("rider_id") riderId: String,
        @Query("state") state: String = "in.(PENDIENTE,ACEPTADA,EN_CURSO)",
        @Query("select") select: String = "id,ride_id,rider_id,state,rides(id,source,destination,state,departure_time,date)",
        @Query("order") order: String = "id.desc"
    ): Response<List<TripReservationDto>>

    @GET("rest/v1/rides")
    suspend fun getActiveDriverRide(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("driver_id") driverId: String,
        @Query("state") state: String = "in.(OFERTADO,EN_CURSO)",
        @Query("select") select: String = "id,source,destination,state,departure_time,date,vehicles(number_slots)",
        @Query("order") order: String = "id.desc",
        @Query("limit") limit: Int = 1
    ): Response<List<TripRideDto>>

    @GET("rest/v1/reservations")
    suspend fun getReservationsForRide(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("ride_id") rideId: String,
        @Query("state") state: String = "in.(PENDIENTE,ACEPTADA,EN_CURSO)",
        @Query("select") select: String = "id,ride_id,rider_id,state,riders(id,cancellation_odds,users(first_name,last_name))",
        @Query("order") order: String = "id.desc"
    ): Response<List<TripReservationDto>>

    @PATCH("rest/v1/reservations")
    suspend fun updateReservationState(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Header("Prefer") prefer: String = "return=minimal",
        @Query("id") reservationId: String,
        @Body body: Map<String, String>
    ): Response<Unit>

    @PATCH("rest/v1/rides")
    suspend fun updateRideState(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Header("Prefer") prefer: String = "return=minimal",
        @Query("id") rideId: String,
        @Body body: Map<String, String>
    ): Response<Unit>
}

