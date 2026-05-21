package com.gn41.appandroidkotlin.data.dto.auth

data class VehicleResponseDto(
    val id: Int,
    val brand: String,
    val model: String,
    val color: String,
    val license_plate: String,
    val number_slots: Int,
    val driver_id: Int
)