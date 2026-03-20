package com.gn41.appandroidkotlin.data.dto.vehicle

import com.google.gson.annotations.SerializedName

data class VehicleDto (
    val id: Int,
    val brand: String,
    val model: String,
    val color: String,

    @SerializedName("license_plate")
    val licensePlate: String,

    @SerializedName("number_slots")
    val numberSlots: Int,
)