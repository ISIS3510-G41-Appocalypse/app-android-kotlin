package com.gn41.appandroidkotlin.data.services.location

import com.gn41.appandroidkotlin.data.dto.location.UserSharedLocationDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationApi {

    @POST("rest/v1/user_shared_locations")
    suspend fun insertUserLocation(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Body body: UserSharedLocationDto
    ): Response<Unit>

    @GET("rest/v1/user_shared_locations")
    suspend fun getLocationsByRide(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("ride_id") rideId: String,
        @Query("order") order: String = "timestamp.desc"
    ): Response<List<UserSharedLocationDto>>
}