package com.gn41.appandroidkotlin.data.services.ratings

import com.gn41.appandroidkotlin.data.dto.ratings.RateDriverRequestDto
import com.gn41.appandroidkotlin.data.dto.ratings.RateRiderRequestDto
import com.gn41.appandroidkotlin.data.dto.ratings.RatedDriverDto
import com.gn41.appandroidkotlin.data.dto.ratings.RatedRiderDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

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

    @GET("rest/v1/rates_rider")
    suspend fun getRiderRatingsForRide(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("ride_id") rideFilter: String,
        @Query("driver_id") driverFilter: String,
        @Query("select") select: String = "rider_id"
    ): Response<List<RatedRiderDto>>

    @GET("rest/v1/rates_driver")
    suspend fun getDriverRatingsForRide(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("ride_id") rideFilter: String,
        @Query("rider_id") riderFilter: String,
        @Query("select") select: String = "driver_id"
    ): Response<List<RatedDriverDto>>
}

