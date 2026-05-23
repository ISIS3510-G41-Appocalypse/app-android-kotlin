package com.gn41.appandroidkotlin.data.dto.ratings

data class RateDriverRequestDto(
    val rider_id: Int,
    val driver_id: Int,
    val punctuality: Int,
    val behavior: Int,
    val communication: Int,
    val security: Int,
    val ride_id: Int
)

