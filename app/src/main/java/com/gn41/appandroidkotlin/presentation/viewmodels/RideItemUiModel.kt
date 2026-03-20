package com.gn41.appandroidkotlin.presentation.viewmodels

import com.gn41.appandroidkotlin.data.dto.rides.RideDto
import java.util.Locale

data class RideItemUiModel(
    val id: Int,
    val source: String,
    val destination: String,
    val date: String,
    val departureTime: String,
    val type: String,
    val price: String,
    val driverName: String,
    val driverRating: String?,
    val vehicleInfo: String,
    val totalSlots: String,
    val zoneName: String
)

fun mapToRideUiModel(dto: RideDto): RideItemUiModel {
    // se obtiene nombres del conductor
    val firstName = dto.drivers?.users?.first_name.orEmpty()
    val lastName = dto.drivers?.users?.last_name.orEmpty()

    val driverName = listOf(firstName, lastName)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "Conductor" }

    // se formatea rating con 1 decimal
    val driverRating = dto.drivers?.rating?.let {
        String.format(Locale.getDefault(), "%.1f", it)
    }

    // se combinan marca y modelo del vehiculo
    val vehicleInfo = listOfNotNull(
        dto.vehicles?.brand,
        dto.vehicles?.model
    ).joinToString(" ").ifBlank { "No disponible" }

    val totalSlots = dto.vehicles?.number_slots?.toString() ?: ""

    val zoneName = dto.zones?.name ?: ""

    // se formatea precio con 2 decimales
    val price = dto.price?.let {
        String.format(Locale.getDefault(), "$%.2f", it)
    } ?: "No disponible"

    return RideItemUiModel(
        id = dto.id,
        source = dto.source,
        destination = dto.destination,
        date = dto.date,
        departureTime = dto.departure_time,
        type = dto.type,
        price = price,
        driverName = driverName,
        driverRating = driverRating,
        vehicleInfo = vehicleInfo,
        totalSlots = totalSlots,
        zoneName = zoneName
    )
}