package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.zone.ZoneDto
import com.gn41.appandroidkotlin.data.services.zones.ZoneService

class ZoneRepositoryImpl(private val zoneService: ZoneService) : ZoneRepository {
    override suspend fun getZones() : List<ZoneDto> {
        return zoneService.getZones()
    }

    override fun getZoneByName(name:String) : ZoneDto {
        return zoneService.getZoneByName(name)
    }
}