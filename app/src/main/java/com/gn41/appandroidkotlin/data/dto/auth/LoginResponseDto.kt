package com.gn41.appandroidkotlin.data.dto.auth

data class LoginResponseDto(
    val access_token: String,
    val user: User
)

data class User(
    val id: String
)