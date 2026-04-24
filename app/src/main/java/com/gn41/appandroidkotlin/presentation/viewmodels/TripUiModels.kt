package com.gn41.appandroidkotlin.presentation.viewmodels

import com.gn41.appandroidkotlin.domain.UserSharedLocation

data class TripReservationItemUiModel(
    val id: Int,
    val riderName: String,
    val status: String,
    val paymentMethod: String = "Por definir"
)

data class ActiveRiderTripUiModel(
    val reservationId: Int,
    val rideId: Int,
    val source: String,
    val destination: String,
    val status: String,
    val rideStatus: String,
    val departureTime: String
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


