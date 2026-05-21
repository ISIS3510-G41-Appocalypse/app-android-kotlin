package com.gn41.appandroidkotlin.data.dto.auth

data class CreateCompleteUserResponseDto(
    val success: Boolean,
    val message: String,
    val auth_id: String,
    val user: UserResponseDto,
    val rider: RiderResponseDto?,
    val driver: DriverResponseDto?,
    val vehicle: VehicleResponseDto?
)