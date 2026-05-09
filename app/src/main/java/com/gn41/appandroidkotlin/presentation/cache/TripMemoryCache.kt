package com.gn41.appandroidkotlin.presentation.cache

import com.gn41.appandroidkotlin.presentation.viewmodels.ActiveDriverTripUiModel
import com.gn41.appandroidkotlin.presentation.viewmodels.ActiveRiderTripUiModel

object TripMemoryCache {

    private var cachedRiderTrips: List<ActiveRiderTripUiModel> = emptyList()
    private var cachedDriverTrip: ActiveDriverTripUiModel? = null
    private var cachedCurrentUserId: Int? = null
    private var cachedCurrentRideId: Int? = null
    private var cachedAuthId: String? = null

    fun save(
        activeRiderTrips: List<ActiveRiderTripUiModel>,
        activeDriverTrip: ActiveDriverTripUiModel?,
        currentUserId: Int?,
        currentRideId: Int?,
        authId: String?
    ) {
        cachedRiderTrips = activeRiderTrips
        cachedDriverTrip = activeDriverTrip
        cachedCurrentUserId = currentUserId
        cachedCurrentRideId = currentRideId
        cachedAuthId = authId
    }

    fun getSavedState(authId: String?): CachedTripState? {
        if (!hasData()) {
            return null
        }

        if (authId.isNullOrBlank() || cachedAuthId.isNullOrBlank()) {
            return null
        }

        if (!cachedAuthId.equals(authId, ignoreCase = false)) {
            return null
        }

        return CachedTripState(
            activeRiderTrips = cachedRiderTrips,
            activeDriverTrip = cachedDriverTrip,
            currentUserId = cachedCurrentUserId,
            currentRideId = cachedCurrentRideId,
            authId = cachedAuthId
        )
    }

    fun hasData(): Boolean {
        return cachedRiderTrips.isNotEmpty() || cachedDriverTrip != null
    }

    fun clear() {
        cachedRiderTrips = emptyList()
        cachedDriverTrip = null
        cachedCurrentUserId = null
        cachedCurrentRideId = null
        cachedAuthId = null
    }
}

data class CachedTripState(
    val activeRiderTrips: List<ActiveRiderTripUiModel>,
    val activeDriverTrip: ActiveDriverTripUiModel?,
    val currentUserId: Int?,
    val currentRideId: Int?,
    val authId: String?
)

