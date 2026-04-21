package com.gn41.appandroidkotlin.data.dto.location

data class UserSharedLocationDto(
    val id: Int? = null,
    val user_id: Int,
    val ride_id: Int,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String,
    val is_sharing_enabled: Boolean
)