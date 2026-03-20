package com.gn41.appandroidkotlin.data.services.vehicles

import com.gn41.appandroidkotlin.data.dto.vehicle.VehicleDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.services.SupabaseClient
import com.gn41.appandroidkotlin.data.services.userId.UserIdService

class VehicleService (private val sessionManager: SessionManager,
                      private val userIdService: UserIdService) {
    private val vehicleApi = SupabaseClient.vehicleApi

    suspend fun getUserVehicles(): List<VehicleDto> {
        val token = sessionManager.getToken()

        val authId = sessionManager.getUserId()

        val userId = userIdService.getUserByAuthId(authId).id

        val vehicles = vehicleApi.getUserVehicles("Bearer $token", "eq.$userId")

        return vehicles
    }

    fun getVehicleByLicensePlate(licensePlate: String) : VehicleDto {
        val token = sessionManager.getToken()

        val vehicle = vehicleApi.getVehicleByLicensePlate("Bearer $token","eq.$licensePlate")

        return vehicle.firstOrNull()
            ?: throw Exception("Vehicle not found")
    }
}