package com.gn41.appandroidkotlin.data.services.auth

import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.auth.LoginRequestDto
import com.gn41.appandroidkotlin.data.dto.auth.LoginResponseDto
import com.gn41.appandroidkotlin.data.services.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthService {

    private val authApi = SupabaseClient.authApi

    suspend fun login(email: String, password: String): LoginResponseDto? = withContext(Dispatchers.IO) {
        val loginRequest = LoginRequestDto(
            email = email,
            password = password
        )

        val response = authApi.login(
            apiKey = BuildConfig.SUPABASE_KEY,
            request = loginRequest
        )

        println("Response body: ${response.body()}")

        return@withContext if (response.isSuccessful && response.body() != null) { response.body() } else {null}
    }
}