package com.gn41.appandroidkotlin.data.repositories

import android.util.Log
import com.gn41.appandroidkotlin.data.dto.vehicle.VehicleDto
import com.gn41.appandroidkotlin.data.services.vehicles.VehicleService

class VehicleRepository(private val vehicleService: VehicleService) {
    suspend fun getUserVehicles() : List<VehicleDto> {
        Log.d("CreateRide", "Llamando a getUserVehicles")
        return vehicleService.getUserVehicles()
    }

    suspend fun getVehicleByLicensePlate( licensePlate: String ) : VehicleDto {
        return vehicleService.getVehicleByLicensePlate(licensePlate)
    }
}