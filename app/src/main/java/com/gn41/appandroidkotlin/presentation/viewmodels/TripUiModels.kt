package com.gn41.appandroidkotlin.presentation.viewmodels

data class TripReservationItemUiModel(
    val id: Int,
    val riderName: String,
    val status: String
)

data class ActiveRiderTripUiModel(
    val reservationId: Int,
    val rideId: Int,
    val source: String,
    val destination: String,
    val status: String,
    val departureTime: String
)

data class ActiveDriverTripUiModel(
    val rideId: Int,
    val source: String,
    val destination: String,
    val status: String,
    val departureTime: String,
    val reservationsCount: Int,
    val reservations: List<TripReservationItemUiModel>
)

data class TripUiState(
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val activeRiderTrip: ActiveRiderTripUiModel? = null,
    val activeDriverTrip: ActiveDriverTripUiModel? = null,
    val infoMessage: String = ""
)

