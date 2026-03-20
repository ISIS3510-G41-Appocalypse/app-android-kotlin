package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.vehicle.VehicleDto

interface VehicleRepository {
    suspend fun getUserVehicles() : List<VehicleDto>

    suspend fun getVehicleByLicensePlate(licensePlate: String) : VehicleDto
}