package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.zone.ZoneDto
import com.gn41.appandroidkotlin.data.services.zones.ZoneService

class ZoneRepository(private val zoneService: ZoneService) {
    suspend fun getZones() : List<ZoneDto> {
        return zoneService.getZones()
    }

    suspend fun getZoneByName(name:String) : ZoneDto {
        return zoneService.getZoneByName(name)
    }
}