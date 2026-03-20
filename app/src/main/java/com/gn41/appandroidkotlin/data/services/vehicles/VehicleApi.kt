package com.gn41.appandroidkotlin.data.services.vehicles

import com.gn41.appandroidkotlin.data.dto.vehicle.VehicleDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface VehicleApi {
    @GET("rest/v1/vehicles")
    suspend fun getUserVehicles(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("driver_id") driverId: String
    ): List<VehicleDto>

    @GET("rest/v1/vehicles")
    suspend fun getVehicleByLicensePlate(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("license_plate") licensePlate: String
    ) : List<VehicleDto>
}