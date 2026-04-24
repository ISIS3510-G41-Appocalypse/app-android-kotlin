package com.gn41.appandroidkotlin.data.services.rides

import com.gn41.appandroidkotlin.data.dto.createRide.ActiveRideDto
import com.gn41.appandroidkotlin.data.dto.createRide.CreateRideRequestDto
import com.gn41.appandroidkotlin.data.dto.createRide.RideUserDto
import com.gn41.appandroidkotlin.data.dto.createRide.RiderCancellationOddsDto
import com.gn41.appandroidkotlin.data.dto.createRide.RiderIdDto
import com.gn41.appandroidkotlin.data.dto.createRide.RiderUserInfoDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface RideApi {
    @POST("rest/v1/rides")
    suspend fun create(@Header("apiKey") apiKey: String,
                       @Header("Authorization") authorization: String,
                       @Body request: CreateRideRequestDto)

    @PATCH("rest/v1/rides")
    suspend fun cancelRide(@Header("apiKey") apiKey: String,
                           @Header("Authorization") authorization: String,
                           @Query("id") id: String,
                           @Body ridePatch: Map<String, String>
    ) : Response<Unit>

    @GET("rest/v1/rides")
    suspend fun getActiveRide(@Header("apiKey") apiKey: String,
                              @Header("Authorization") authorization: String,
                              @Query("driver_id") driverId: String,
                              @Query("state") state: String = "eq.OFERTADO"
    ) : List<ActiveRideDto>

    @GET("rest/v1/reservations")
    suspend fun getRideUsersId(@Header("apiKey") apiKey: String,
                             @Header("Authorization") authorization: String,
                             @Query("select") select: String = "rider_id",
                             @Query("ride_id") rideId: String
    ) : List<RiderIdDto>

    @GET("rest/v1/riders")
    suspend fun getRideUsersCancellationOdds(@Header("apiKey") apiKey: String,
                                             @Header("Authorization") authorization: String,
                                             @Query("select") select: String = "cancellation_odds,user_id",
                                             @Query("id", encoded = true) ids: String
    ) : List<RiderCancellationOddsDto>

    @GET("rest/v1/users")
    suspend fun getRideUsersInfo(@Header("apiKey") apiKey: String,
                               @Header("Authorization") authorization: String,
                               @Query("select") select: String = "first_name,last_name",
                               @Query("id", encoded = true) ids: String
    ) : List<RiderUserInfoDto>
}