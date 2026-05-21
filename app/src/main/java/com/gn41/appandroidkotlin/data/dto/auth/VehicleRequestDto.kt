package com.gn41.appandroidkotlin.data.dto.auth

data class VehicleRequestDto(
    val license_plate: String,
    val number_slots: Int,
    val brand: String,
    val model: String,
    val color: String
)