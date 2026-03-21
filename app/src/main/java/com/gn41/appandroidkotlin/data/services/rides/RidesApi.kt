package com.gn41.appandroidkotlin.data.services.rides

import com.gn41.appandroidkotlin.data.dto.rides.RideDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface RidesApi {
    @GET("rest/v1/rides")
    suspend fun getRides(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("select") select: String,
        @Query("order") order: String
    ): Response<List<RideDto>>
}
