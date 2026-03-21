package com.gn41.appandroidkotlin.data.services.userId

import android.util.Base64
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

        if (token.isEmpty()) {
            throw Exception("No auth token")
        }

        val authId = extractAuthIdFromToken(token)
            ?: throw Exception("Auth id not found")

        val response = userIdApi.getUserByAuthId(
            token = "Bearer $token",
            apiKey = BuildConfig.SUPABASE_KEY,
            authId = "eq.$authId"
        )

        return response.firstOrNull()
            ?: throw Exception("User not found")
    }

    suspend fun getDriverIdByUserId(userId: Int): Int {
        val token = sessionManager.getToken()
        if (token.isEmpty()) {
            throw Exception("No auth token")
        }

        val response = userIdApi.getDriverByUserId(
            token = "Bearer $token",
            apiKey = BuildConfig.SUPABASE_KEY,
            userId = "eq.$userId"
        )

        return response.firstOrNull()?.id
            ?: throw Exception("Driver not found")
    }

    private fun extractAuthIdFromToken(token: String): String? {
        return try {
            val parts = token.split('.')
            if (parts.size < 2) return null

            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val payload = String(payloadBytes)

            val subRegex = "\"sub\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            subRegex.find(payload)?.groupValues?.get(1)
        } catch (_: Exception) {
            null
        }
    }
}