package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.zone.ZoneDto

interface ZoneRepository {
    suspend fun getZones() : List<ZoneDto>

    suspend fun getZoneByName(name: String) : ZoneDto
}