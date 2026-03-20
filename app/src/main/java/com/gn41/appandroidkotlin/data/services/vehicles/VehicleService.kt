package com.gn41.appandroidkotlin.data.services.vehicles

import android.util.Log
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.vehicle.VehicleDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.services.SupabaseClient
import com.gn41.appandroidkotlin.data.services.userId.UserIdService

class VehicleService (private val sessionManager: SessionManager,
                      private val userIdService: UserIdService) {
    private val vehicleApi = SupabaseClient.vehicleApi

    suspend fun getUserVehicles(): List<VehicleDto> {
        Log.d("CreateRide", "Token enviado: ${sessionManager.getToken()}")
        val token = sessionManager.getToken()
        Log.d("CreateRide", "Token enviado: ${sessionManager.getToken()}")

        val userId = userIdService.getUserByAuthId().id

        Log.d("CreateRide", "Token enviado: ${sessionManager.getToken()}")

        val vehicles = vehicleApi.getUserVehicles("Bearer $token", BuildConfig.SUPABASE_KEY,"eq.$userId")

        return vehicles
    }

    suspend fun getVehicleByLicensePlate(licensePlate: String) : VehicleDto {
        val token = sessionManager.getToken()

        val vehicle = vehicleApi.getVehicleByLicensePlate("Bearer $token",BuildConfig.SUPABASE_KEY,"eq.$licensePlate")
        Log.d("Vehicles", licensePlate)
        Log.d("Vehicles", "$vehicle")

        return vehicle.firstOrNull()
            ?: throw Exception("Vehicle not found")
    }
}