package com.gn41.appandroidkotlin.data.services.auth

import com.gn41.appandroidkotlin.data.dto.auth.LoginRequestDto
import com.gn41.appandroidkotlin.data.services.SupabaseClient

class AuthService {

    private val authApi = SupabaseClient.authApi

    suspend fun login(email: String, password: String): Boolean {
        val loginRequest = LoginRequestDto(email = email, password = password)
        val response = authApi.login(loginRequest)
        return response.isSuccessful && response.body() != null
    }

}