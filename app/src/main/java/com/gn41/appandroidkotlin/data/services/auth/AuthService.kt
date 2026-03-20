package com.gn41.appandroidkotlin.data.services.auth

import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.auth.LoginRequestDto
import com.gn41.appandroidkotlin.data.services.SupabaseClient

class AuthService {

    private val authApi = SupabaseClient.authApi

    suspend fun login(email: String, password: String): String? {
        val loginRequest = LoginRequestDto(
            email = email,
            password = password
        )

        val response = authApi.login(
            apiKey = BuildConfig.SUPABASE_KEY,
            request = loginRequest
        )

        println("Response code: ${response.code()}")
        println("Response body: ${response.body()}")
        println("Error body: ${response.errorBody()?.string()}")

        return if (response.isSuccessful && response.body() != null) { response.body()!!.access_token
        } else {null}
    }
}