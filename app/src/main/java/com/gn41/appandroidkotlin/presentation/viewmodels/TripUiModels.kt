package com.gn41.appandroidkotlin.presentation.viewmodels

import com.gn41.appandroidkotlin.domain.UserSharedLocation
import java.util.Locale

data class TripReservationItemUiModel(
    val id: Int,
    val riderName: String,
    val status: String,
    val cancellationOdds: Double?,
    val paymentMethod: String = "Por definir"
)

data class ActiveRiderTripUiModel(
    val reservationId: Int,
    val rideId: Int,
    val source: String,
    val destination: String,
    val status: String,
    val rideStatus: String,
    val departureTime: String,
    val canCancelReservation: Boolean,
    val showCancelButton: Boolean,
    val cancelDisabledReason: String?
)

data class ActiveDriverTripUiModel(
    val rideId: Int,
    val source: String,
    val destination: String,
    val status: String,
    val departureTime: String,
    val reservationsCount: Int,
    val totalSeats: Int,
    val acceptedReservations: Int,
    val availableSeats: Int,
    val reservations: List<TripReservationItemUiModel>
)

data class TripUiState(
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val activeRiderTrips: List<ActiveRiderTripUiModel> = emptyList(),
    val activeDriverTrip: ActiveDriverTripUiModel? = null,
    val infoMessage: String = "",

    val isLocationSharingEnabled: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val locationErrorMessage: String = "",
    val isLocationLoading: Boolean = false,
    val lastLocationTimestamp: Long? = null,
    val locationStatusMessage: String = "",

    val currentUserId: Int? = null,
    val currentRideId: Int? = null,
    val rideLocations: List<UserSharedLocation> = emptyList(),
    val isUsingCachedLocations: Boolean = false,
    val cachedLocationMessage: String = ""
)


data class MapUserMarkerUiState(
    val userId: Int,
    val initials: String,
    val latitude: Double,
    val longitude: Double,
    val isCurrentUser: Boolean,
    val isDriver: Boolean,
    val distanceMeters: Int? = null
)

fun normalizeState(state: String?): String {
    val rawState = state?.trim()?.uppercase(Locale.getDefault()).orEmpty()
    return when (rawState) {
        "PENDIENTE", "PENDING" -> "PENDIENTE"
        "ACEPTADA", "ACCEPTED" -> "ACEPTADA"
        "EN_CURSO", "IN_PROGRESS" -> "EN_CURSO"
        "OFERTADO", "OFFERED", "ACTIVE" -> "OFERTADO"
        "FINALIZADO", "FINALIZADA", "FINISHED", "COMPLETED" -> "FINALIZADO"
        "CANCELADO", "CANCELADA", "CANCELLED" -> "CANCELADO"
        "RECHAZADA", "REJECTED" -> "RECHAZADA"
        else -> rawState
    }
}

fun stateToReadableLabel(state: String?): String {
    val normalizedState = normalizeState(state)
    return when (normalizedState) {
        "OFERTADO" -> "Ofertado"
        "PENDIENTE" -> "Pendiente"
        "ACEPTADA" -> "Aceptada"
        "EN_CURSO" -> "En curso"
        "FINALIZADO" -> "Finalizado"
        "CANCELADO" -> "Cancelado"
        "RECHAZADA" -> "Rechazada"
        else -> normalizedState
            .lowercase(Locale.getDefault())
            .split("_")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                }
            }
            .ifBlank { "Desconocido" }
    }
}


fun buildInitials(fullName: String): String {
    return fullName
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifBlank { "U" }
}


