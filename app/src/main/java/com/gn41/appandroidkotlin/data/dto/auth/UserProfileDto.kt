package com.gn41.appandroidkotlin.data.dto.auth

data class UserProfileDto(
    val id: Int,
    val first_name: String,
    val last_name: String,

    val zones: ZoneDto?,

    val drivers: List<DriverProfileDto>?,

    val riders: List<RiderProfileDto>?
)

data class ZoneDto(
    val name: String
)

data class DriverProfileDto(
    val rating: Double?,
    val cancellation_odds: Double?
)

data class RiderProfileDto(
    val rating: Double?,
    val cancellation_odds: Double?
)