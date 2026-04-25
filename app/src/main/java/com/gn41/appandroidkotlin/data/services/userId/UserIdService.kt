package com.gn41.appandroidkotlin.data.services.userId

import android.util.Base64
import android.util.Log
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.user.UserIdDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.services.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserIdService(
    private val sessionManager: SessionManager
) {
    private val userIdApi = SupabaseClient.userIdApi

    suspend fun getUserByAuthId(): UserIdDto = withContext(Dispatchers.IO) {
        Log.d("CreateRide", "Llamando a getUserByAuthId")

        val token = sessionManager.getToken()

        val authId = sessionManager.getUserId()

        Log.d("auth",authId)

        if (token.isEmpty()) {
            throw Exception("No auth token")
        }
/*
        val authId = extractAuthIdFromToken(token)
            ?: throw Exception("Auth id not found")
*/
        val response = userIdApi.getUserByAuthId(
            token = "Bearer $token",
            apiKey = BuildConfig.SUPABASE_KEY,
            authId = "eq.$authId"
        )

        return@withContext response.firstOrNull()
            ?: throw Exception("User not found")
    }

    suspend fun getDriverByUser(userId: Int): UserIdDto? = withContext(Dispatchers.IO) {
        Log.d("CreateRide", "Llamando a getDriverByUser")

        val token = sessionManager.getToken()

        if (token.isEmpty()) {
            return@withContext null
        }

        return@withContext try {
            val response = userIdApi.getDriverByUser(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                userId = "eq.$userId"
            )
            response.firstOrNull()
        } catch (e: Exception) {
            Log.e("UserIdService", "Error getting driver", e)
            null
        }
    }

    suspend fun getDriverIdByUserId(userId: Int): Int = withContext(Dispatchers.IO) {
        val token = sessionManager.getToken()
        if (token.isEmpty()) {
            throw Exception("No auth token")
        }

        val response = userIdApi.getDriverByUserId(
            token = "Bearer $token",
            apiKey = BuildConfig.SUPABASE_KEY,
            userId = "eq.$userId"
        )

        return@withContext response.firstOrNull()?.id
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