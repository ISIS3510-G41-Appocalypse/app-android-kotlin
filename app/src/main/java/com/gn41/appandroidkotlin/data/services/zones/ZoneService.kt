package com.gn41.appandroidkotlin.data.services.zones

import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.zone.ZoneDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.services.SupabaseClient

class ZoneService (private val sessionManager: SessionManager) {
    private val zoneApi = SupabaseClient.zoneApi

    suspend fun getZones(): List<ZoneDto> {
        val token = sessionManager.getToken()

        val zones = zoneApi.getZones("Bearer $token", BuildConfig.SUPABASE_KEY)

        return zones
    }

    suspend fun getZoneByName(name:String): ZoneDto {
        val token = sessionManager.getToken()

        val zone = zoneApi.getZoneByName("Bearer $token", BuildConfig.SUPABASE_KEY,"eq.$name")

        return zone.firstOrNull()
            ?: throw Exception("Zone not found")
    }
}