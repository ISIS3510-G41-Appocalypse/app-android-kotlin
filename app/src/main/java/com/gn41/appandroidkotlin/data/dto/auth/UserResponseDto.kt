package com.gn41.appandroidkotlin.data.dto.auth

data class UserResponseDto(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val zone_id: Int,
    val auth_id: String
)