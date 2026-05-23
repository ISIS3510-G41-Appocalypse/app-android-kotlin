package com.gn41.appandroidkotlin.data.dto.auth

data class CreateCompleteUserRequestDto(
    val first_name: String,
    val last_name: String,
    val email: String,
    val password: String,
    val role: String,
    val zone_id: Int?,
    val vehicle: VehicleRequestDto? = null
)