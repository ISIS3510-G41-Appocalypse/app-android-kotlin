package com.gn41.appandroidkotlin.data.services.rides

import com.gn41.appandroidkotlin.data.dto.rides.RideDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface RidesApi {

    @GET("rest/v1/rides")
    suspend fun getRides(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String
    ): Response<List<RideDto>>
}

