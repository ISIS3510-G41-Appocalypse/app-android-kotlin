package com.gn41.appandroidkotlin.data.dto.createRide

import com.google.gson.annotations.SerializedName

data class ActiveRideDto(
    val id: Int,

    @SerializedName("driver_id")
    val driverId: Int,

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

data class RideUserDto(
    @SerializedName("rider_id")
    val riderId: Int,

    val name: String,

    @SerializedName("last_name")
    val lastName: String,

    @SerializedName("cancellation_odds")
    val cancellationOdds: Double
)

data class RiderCancellationOddsDto(
    @SerializedName("cancellation_odds")
    val cancellationOdds: Double,

    @SerializedName("user_id")
    val userId: Int
)

data class RiderUserInfoDto(
    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String
)

data class RiderIdDto(
    @SerializedName("rider_id")
    val riderId: Int
)