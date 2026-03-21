package com.gn41.appandroidkotlin.data.services.userId

import android.util.Log
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.user.UserIdDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.services.SupabaseClient

class UserIdService(
    private val sessionManager: SessionManager
) {
    private val userIdApi = SupabaseClient.userIdApi

    suspend fun getUserByAuthId(): UserIdDto {
        Log.d("CreateRide", "Llamando a getUserByAuthId")

        val token = sessionManager.getToken()

        val authId = sessionManager.getUserId()

        Log.d("auth",authId)

        if (token.isEmpty()) {
            throw Exception("No auth token")
        }

        val response = userIdApi.getUserByAuthId(
            token = "Bearer $token",
            apiKey = BuildConfig.SUPABASE_KEY,
            authId = "eq.$authId"
        )

        return response.firstOrNull()
            ?: throw Exception("User not found")
    }

    suspend fun getDriverByUser(userId: Int): UserIdDto {
        Log.d("CreateRide", "Llamando a getUserByAuthId")

        val token = sessionManager.getToken()

        if (token.isEmpty()) {
            throw Exception("No auth token")
        }

        val response = userIdApi.getDriverByUser(
            token = "Bearer $token",
            apiKey = BuildConfig.SUPABASE_KEY,
            userId = "eq.$userId"
        )

        return response.firstOrNull()
            ?: throw Exception("User not found")
    }
}