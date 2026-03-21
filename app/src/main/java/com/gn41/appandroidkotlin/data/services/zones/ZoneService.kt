package com.gn41.appandroidkotlin.data.services.zones

import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.zone.ZoneDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.services.SupabaseClient

class ZoneService(
    private val sessionManager: SessionManager
) {
    private val zoneApi = SupabaseClient.zoneApi

    suspend fun getZones(): List<ZoneDto> {
        val token = sessionManager.getToken()

        if (token.isEmpty()) {
            throw Exception("No auth token")
        }

        return zoneApi.getZones(
            token = "Bearer $token",
            apiKey = BuildConfig.SUPABASE_KEY
        )
    }

    suspend fun getZoneByName(name: String): ZoneDto {
        val token = sessionManager.getToken()

        if (token.isEmpty()) {
            throw Exception("No auth token")
        }

        val zone = zoneApi.getZoneByName(
            token = "Bearer $token",
            apiKey = BuildConfig.SUPABASE_KEY,
            name = "eq.$name"
        )

        return zone.firstOrNull()
            ?: throw Exception("Zone not found")
    }
}