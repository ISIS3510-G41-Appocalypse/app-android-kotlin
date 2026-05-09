package com.gn41.appandroidkotlin.localStorage

import com.gn41.appandroidkotlin.presentation.viewmodels.ActiveDriverTripUiModel
import com.gn41.appandroidkotlin.presentation.viewmodels.ActiveRiderTripUiModel

data class TripStorageDto(
    val authId: String,
    val currentUserId: Int?,
    val currentRideId: Int?,
    val activeRiderTrips: List<ActiveRiderTripUiModel>,
    val activeDriverTrip: ActiveDriverTripUiModel?,
    val savedAt: Long
)

