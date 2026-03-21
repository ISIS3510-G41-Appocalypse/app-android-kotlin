package com.gn41.appandroidkotlin.presentation.viewmodels

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
    val infoMessage: String = ""
)

