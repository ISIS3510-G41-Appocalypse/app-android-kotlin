package com.gn41.appandroidkotlin.presentation.viewmodels

import com.gn41.appandroidkotlin.data.dto.rides.RideDto
import java.util.Locale
import kotlin.math.roundToInt

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
    val totalSlots: Int,
    val availableSlots: Int,
    val zoneName: String,
    val cancellationRiskPercent: Int?
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

    val totalSlots = dto.vehicles?.number_slots ?: 0
    val bookedSlots = dto.reservations.orEmpty().count {
        it.state == "PENDIENTE" || it.state == "ACEPTADA" || it.state == "EN_CURSO"
    }
    val availableSlots = (totalSlots - bookedSlots).coerceAtLeast(0)

    val zoneName = dto.zones?.name ?: ""

    val cancellationRiskPercent = dto.drivers?.cancellation_odds
        ?.times(100)
        ?.roundToInt()
        ?.coerceIn(0, 100)

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
        availableSlots = availableSlots,
        zoneName = zoneName,
        cancellationRiskPercent = cancellationRiskPercent
    )
}