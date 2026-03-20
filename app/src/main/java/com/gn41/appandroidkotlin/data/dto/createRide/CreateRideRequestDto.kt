package com.gn41.appandroidkotlin.data.dto.createRide

import com.google.gson.annotations.SerializedName

data class CreateRideRequestDto (
    @SerializedName("driver_id")
    val driverId: String,

    @SerializedName("vehicle_id")
    val vehicleId: Int,

    @SerializedName("zone_id")
    val zoneId: Int,

    val source: String,
    val destination: String,
    val price: Double,
    val date: String,

    @SerializedName("departure_time")
    val departureTime: String,

    val state: String,
    val type: String,
)