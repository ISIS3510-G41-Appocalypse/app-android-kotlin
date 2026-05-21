package com.gn41.appandroidkotlin.data.services.auth

import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.auth.CreateCompleteUserRequestDto
import com.gn41.appandroidkotlin.data.dto.auth.CreateCompleteUserResponseDto
import com.gn41.appandroidkotlin.data.dto.auth.LoginRequestDto
import com.gn41.appandroidkotlin.data.dto.auth.LoginResponseDto
import com.gn41.appandroidkotlin.data.services.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthService {

    private val authApi = SupabaseClient.authApi

    suspend fun login(
        email: String,
        password: String
    ): LoginResponseDto? = withContext(Dispatchers.IO) {

        return@withContext try {

            val loginRequest = LoginRequestDto(
                email = email,
                password = password
            )

            val response = authApi.login(
                apiKey = BuildConfig.SUPABASE_KEY,
                request = loginRequest
            )

            if (response.isSuccessful && response.body() != null) {
                response.body()
            } else {
                null
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun createCompleteUser(
        request: CreateCompleteUserRequestDto
    ): CreateCompleteUserResponseDto? = withContext(Dispatchers.IO) {

        return@withContext try {

            val response = authApi.createCompleteUser(
                token = "Bearer ${BuildConfig.SUPABASE_KEY}",
                apiKey = BuildConfig.SUPABASE_KEY,
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                response.body()
            } else {
                println("Create user error: ${response.errorBody()?.string()}")
                null
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}