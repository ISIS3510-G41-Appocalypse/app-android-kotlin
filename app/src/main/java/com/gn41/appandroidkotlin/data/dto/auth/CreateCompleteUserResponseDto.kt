package com.gn41.appandroidkotlin.data.dto.auth

data class CreateCompleteUserResponseDto(
    val success: Boolean,
    val message: String? = null,

    val error_code: String? = null,
    val error: String? = null,

    val auth_id: String? = null,

    val user: UserResponseDto? = null,
    val rider: RiderResponseDto? = null,
    val driver: DriverResponseDto? = null,
    val vehicle: VehicleResponseDto? = null
)