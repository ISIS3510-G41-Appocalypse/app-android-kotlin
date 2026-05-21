package com.gn41.appandroidkotlin.data.services.ratings

import com.gn41.appandroidkotlin.data.dto.ratings.RateDriverRequestDto
import com.gn41.appandroidkotlin.data.dto.ratings.RateRiderRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface RatingApi {
    @POST("rest/v1/rates_driver")
    suspend fun rateDriver(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Prefer") prefer: String = "return=minimal",
        @Body body: RateDriverRequestDto
    ): Response<Unit>

    @POST("rest/v1/rates_rider")
    suspend fun rateRider(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Prefer") prefer: String = "return=minimal",
        @Body body: RateRiderRequestDto
    ): Response<Unit>
}

