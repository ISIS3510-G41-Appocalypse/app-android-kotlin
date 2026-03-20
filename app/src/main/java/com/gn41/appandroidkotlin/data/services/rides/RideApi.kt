package com.gn41.appandroidkotlin.data.services.rides

import com.gn41.appandroidkotlin.data.dto.createRide.CreateRideRequestDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface RideApi {
    @POST("rest/v1/rides")
    suspend fun create(@Header("apiKey") apiKey: String,
                       @Header("Authorization") authorization: String,
                       @Body request: CreateRideRequestDto)
}