package com.gn41.appandroidkotlin.data.services.rides

import com.gn41.appandroidkotlin.data.dto.recommendations.RiderDriverRecommendationDto
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

    @GET("rest/v1/rider_driver_recommendation")
    suspend fun getRiderDriverRecommendation(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("select") select: String = "rider_id,driver_id,rating",
        @Query("rider_id") riderId: String,
        @Query("driver_id") driverId: String,
        @Query("limit") limit: Int = 1
    ): Response<List<RiderDriverRecommendationDto>>
}
