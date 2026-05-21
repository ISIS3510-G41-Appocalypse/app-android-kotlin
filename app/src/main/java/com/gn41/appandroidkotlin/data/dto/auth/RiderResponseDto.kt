package com.gn41.appandroidkotlin.data.dto.auth

data class RiderResponseDto(
    val id: Int,
    val cancellation_odds: Double?,
    val rating: Double?,
    val user_id: Int
)