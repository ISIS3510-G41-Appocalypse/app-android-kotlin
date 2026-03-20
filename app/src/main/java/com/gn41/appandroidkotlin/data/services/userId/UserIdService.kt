package com.gn41.appandroidkotlin.data.services.userId

import android.util.Log
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.user.UserIdDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.services.SupabaseClient

class UserIdService (private val sessionManager: SessionManager) {
    private val userIdApi = SupabaseClient.userIdApi

    suspend fun getUserByAuthId() : UserIdDto {
        Log.d("CreateRide", "Llamando a getUserVehicles")

        val token = sessionManager.getToken()
        Log.d("CreateRide", "Llamando a getUserVehicles")
        val response = userIdApi.getUserByAuthId("Bearer $token",BuildConfig.SUPABASE_KEY)
        Log.d("CreateRide", "Llamando a getUserVehicles")
        return response.firstOrNull()
            ?: throw Exception("User not found")
    }
}