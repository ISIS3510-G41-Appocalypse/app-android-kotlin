package com.gn41.appandroidkotlin.presentation.cache

import com.gn41.appandroidkotlin.presentation.viewmodels.ActiveDriverTripUiModel
import com.gn41.appandroidkotlin.presentation.viewmodels.ActiveRiderTripUiModel

object TripMemoryCache {

    private var cachedRiderTrips: List<ActiveRiderTripUiModel> = emptyList()
    private var cachedDriverTrip: ActiveDriverTripUiModel? = null
    private var cachedCurrentUserId: Int? = null
    private var cachedCurrentRideId: Int? = null

    fun save(
        activeRiderTrips: List<ActiveRiderTripUiModel>,
        activeDriverTrip: ActiveDriverTripUiModel?,
        currentUserId: Int?,
        currentRideId: Int?
    ) {
        cachedRiderTrips = activeRiderTrips
        cachedDriverTrip = activeDriverTrip
        cachedCurrentUserId = currentUserId
        cachedCurrentRideId = currentRideId
    }

    fun getSavedState(): CachedTripState? {
        if (!hasData()) {
            return null
        }

        return CachedTripState(
            activeRiderTrips = cachedRiderTrips,
            activeDriverTrip = cachedDriverTrip,
            currentUserId = cachedCurrentUserId,
            currentRideId = cachedCurrentRideId
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
    }
}

data class CachedTripState(
    val activeRiderTrips: List<ActiveRiderTripUiModel>,
    val activeDriverTrip: ActiveDriverTripUiModel?,
    val currentUserId: Int?,
    val currentRideId: Int?
)

