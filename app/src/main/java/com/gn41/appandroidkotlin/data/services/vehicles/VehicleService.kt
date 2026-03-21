package com.gn41.appandroidkotlin.data.services.vehicles

import android.util.Log
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.vehicle.VehicleDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.services.SupabaseClient
import com.gn41.appandroidkotlin.data.services.userId.UserIdService

class VehicleService(
    private val sessionManager: SessionManager,
    private val userIdService: UserIdService
) {
    private val vehicleApi = SupabaseClient.vehicleApi

    suspend fun getUserVehicles(): List<VehicleDto> {
        val token = sessionManager.getToken()

        if (token.isEmpty()) {
            return emptyList()
        }

        return try {
            val userId = userIdService.getUserByAuthId().id
            val driver = userIdService.getDriverByUser(userId)
            
            if (driver == null) {
                return emptyList()
            }

            Log.d("CreateRide", "Token: $token")
            Log.d("CreateRide", "driverId: ${driver.id}")

            vehicleApi.getUserVehicles(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                driverId = "eq.${driver.id}"
            )
        } catch (e: Exception) {
            Log.e("VehicleService", "Error getting vehicles", e)
            emptyList()
        }
    }

    suspend fun getVehicleByLicensePlate(licensePlate: String): VehicleDto {
        val token = sessionManager.getToken()

        if (token.isEmpty()) {
            throw Exception("No auth token")
        }

        val vehicle = vehicleApi.getVehicleByLicensePlate(
            token = "Bearer $token",
            apiKey = BuildConfig.SUPABASE_KEY,
            licensePlate = "eq.$licensePlate"
        )

        Log.d("Vehicles", licensePlate)
        Log.d("Vehicles", "$vehicle")

        return vehicle.firstOrNull()
            ?: throw Exception("Vehicle not found")
    }
}