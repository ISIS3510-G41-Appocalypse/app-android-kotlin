package com.gn41.appandroidkotlin.data.services.rides

import com.gn41.appandroidkotlin.data.dto.createRide.ActiveRideDto
import com.gn41.appandroidkotlin.data.dto.createRide.CreateRideRequestDto
import com.gn41.appandroidkotlin.data.dto.createRide.RidePatchDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RideApi {
    @POST("rest/v1/rides")
    suspend fun create(@Header("apiKey") apiKey: String,
                       @Header("Authorization") authorization: String,
                       @Body request: CreateRideRequestDto)

    @PATCH("rest/v1/rides/{id}")
    suspend fun cancelRide(@Header("apiKey") apiKey: String,
                           @Header("Authorization") authorization: String,
                           @Path("id") id: Int,
                           @Body ridePatch: RidePatchDto
    )

    @GET("rest/v1/rides")
    suspend fun getActiveRide(@Header("apiKey") apiKey: String,
                              @Header("Authorization") authorization: String,
                              @Query("driver_id") driverId: String,
                              @Query("state") state: String = "eq.OFERTADO"
    ) : List<ActiveRideDto>
}