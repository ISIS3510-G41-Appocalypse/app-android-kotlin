package com.gn41.appandroidkotlin.data.services.zones

import com.gn41.appandroidkotlin.data.dto.zone.ZoneDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ZoneApi {
    @GET("rest/v1/zones")
    suspend fun getZones(
        @Header("Authorization") token: String
    ): List<ZoneDto>

    @GET("rest/v1/zones")
    fun getZoneByName(
        @Header("Authorization") token: String,
        @Query("name") name: String
    ) : List<ZoneDto>
}