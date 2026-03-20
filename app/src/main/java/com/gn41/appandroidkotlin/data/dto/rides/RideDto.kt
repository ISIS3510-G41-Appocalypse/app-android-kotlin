package com.gn41.appandroidkotlin.data.dto.rides

data class RideDto(
    val id: Int,
    val driver_id: Int,
    val vehicle_id: Int,
    val zone_id: Int,
    val source: String,
    val destination: String,
    val date: String,
    val departure_time: String,
    val state: String,
    val type: String
)

