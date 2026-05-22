package com.gn41.appandroidkotlin.presentation.viewmodels

import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.LocationRepository
import com.gn41.appandroidkotlin.data.repositories.RatingRepository
import com.gn41.appandroidkotlin.data.repositories.TripRepository
import com.gn41.appandroidkotlin.domain.UserSharedLocation
import com.gn41.appandroidkotlin.localStorage.LocalStorageManager
import com.gn41.appandroidkotlin.localStorage.TripStorageDto
import com.gn41.appandroidkotlin.presentation.cache.TripMemoryCache
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TripViewModel(
    private val tripRepository: TripRepository,
    private val ratingRepository: RatingRepository,
    private val sessionManager: SessionManager,
    private val locationRepository: LocationRepository,
    private val networkHelper: NetworkHelper,
    private val localStorageManager: LocalStorageManager
) : ViewModel() {

    companion object {
        private const val TAG = "TripCache"
        private val pendingDriverRatingRideIdsByAuth = mutableMapOf<String, MutableSet<Int>>()
        private val pendingRiderRatingRideIdsByAuth = mutableMapOf<String, MutableSet<Int>>()
        private val skippedDriverRatingRideIdsByAuth = mutableMapOf<String, MutableSet<Int>>()
        private val skippedRiderRatingRideIdsByAuth = mutableMapOf<String, MutableSet<Int>>()
    }

    var uiState by mutableStateOf(TripUiState())
        private set

    var connectivity by mutableStateOf(false)
        private set

    private var lastConnectionState: Boolean? = null

    init {
        uiState = uiState.copy(
            isLocationSharingEnabled = sessionManager.isLocationSharingEnabled()
        )
        observeNetworkChanges()
    }

    private fun observeNetworkChanges() {
        viewModelScope.launch {
            networkHelper.observeNetworkChanges().collect { hasInternet ->
                handleNetworkState(hasInternet)
            }
        }
    }

    private fun handleNetworkState(hasInternet: Boolean) {
        if (!hasInternet) {
            applyOfflineState()
            lastConnectionState = false
            return
        }

        connectivity = true

        if (lastConnectionState != true) {
            lastConnectionState = true
            uiState = uiState.copy(errorMessage = "")
            loadTrips(showLoading = true)
        } else {
            uiState = uiState.copy(errorMessage = "")
        }
    }

    private fun applyOfflineState() {
        connectivity = false

        val token = sessionManager.getToken()
        if (token.isEmpty()) {
            TripMemoryCache.clear()
            localStorageManager.clearTripState()
            Log.d(TAG, "clear caches: no session token")
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "No hay una sesion activa.",
                activeRiderTrips = emptyList(),
                activeDriverTrip = null,
                isOfflineData = false,
                offlineMessage = ""
            )
            return
        }

        val authId = extractAuthIdFromToken(token)
        if (authId.isNullOrEmpty()) {
            TripMemoryCache.clear()
            localStorageManager.clearTripState()
            Log.d(TAG, "clear caches: invalid auth id from token")
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "No se pudo validar el usuario.",
                activeRiderTrips = emptyList(),
                activeDriverTrip = null,
                isOfflineData = false,
                offlineMessage = ""
            )
            return
        }

        val cachedState = TripMemoryCache.getSavedState(authId = authId)

        if (cachedState != null) {
            Log.d(
                TAG,
                "offline restore from RAM authIdPresent=true rider=${cachedState.activeRiderTrips.size} driverRes=${cachedState.activeDriverTrip?.reservations?.size ?: 0}"
            )
            // Restore from cache and mark as offline data
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "",
                activeRiderTrips = cachedState.activeRiderTrips,
                activeDriverTrip = cachedState.activeDriverTrip,
                currentUserId = cachedState.currentUserId,
                currentRideId = cachedState.currentRideId,
                isOfflineData = true,
                offlineMessage = "Estás viendo información guardada. Necesitas conexión para realizar acciones."
            )
        } else {
            Log.d(TAG, "offline RAM miss, reading local storage")
            val localState = localStorageManager.readTripState(authId)
            if (localState != null) {
                Log.d(
                    TAG,
                    "offline restore from FILE authIdPresent=true rider=${localState.activeRiderTrips.size} driverRes=${localState.activeDriverTrip?.reservations?.size ?: 0} savedAt=${localState.savedAt}"
                )
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "",
                    activeRiderTrips = localState.activeRiderTrips,
                    activeDriverTrip = localState.activeDriverTrip,
                    currentUserId = localState.currentUserId,
                    currentRideId = localState.currentRideId,
                    isOfflineData = true,
                    offlineMessage = "Estás viendo información guardada. Necesitas conexión para realizar acciones."
                )
            } else {
                // If offline happened right after a successful render, keep in-memory uiState as last resort.
                val inMemoryRiderTrips = uiState.activeRiderTrips.filter { riderTrip ->
                    val reservationState = normalizeState(riderTrip.status)
                    val rideState = normalizeState(riderTrip.rideStatus)
                    val reservationIsActive = reservationState == "PENDIENTE" ||
                        reservationState == "ACEPTADA" || reservationState == "EN_CURSO"
                    val rideIsActive = rideState != "FINALIZADO" && rideState != "CANCELADO"
                    reservationIsActive && rideIsActive
                }
                val inMemoryDriverTrip = buildDriverTripForStorage(uiState.activeDriverTrip)
                if (inMemoryRiderTrips.isNotEmpty() || inMemoryDriverTrip != null) {
                    Log.d(
                        TAG,
                        "offline restore from CURRENT_UI (timing fallback) authIdPresent=true rider=${inMemoryRiderTrips.size} driverRes=${inMemoryDriverTrip?.reservations?.size ?: 0}"
                    )
                    TripMemoryCache.save(
                        activeRiderTrips = inMemoryRiderTrips,
                        activeDriverTrip = inMemoryDriverTrip,
                        currentUserId = uiState.currentUserId,
                        currentRideId = uiState.currentRideId,
                        authId = authId
                    )
                    localStorageManager.saveTripState(
                        TripStorageDto(
                            authId = authId,
                            currentUserId = uiState.currentUserId,
                            currentRideId = uiState.currentRideId,
                            activeRiderTrips = inMemoryRiderTrips,
                            activeDriverTrip = inMemoryDriverTrip,
                            savedAt = System.currentTimeMillis()
                        )
                    )
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "",
                        activeRiderTrips = inMemoryRiderTrips,
                        activeDriverTrip = inMemoryDriverTrip,
                        isOfflineData = true,
                        offlineMessage = "Estás viendo información guardada. Necesitas conexión para realizar acciones."
                    )
                    return
                }

                Log.d(TAG, "offline no cache found in RAM/FILE")
                // No cache available in memory or local storage
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Sin conexión a internet. No hay datos guardados disponibles.",
                    activeRiderTrips = emptyList(),
                    activeDriverTrip = null,
                    isOfflineData = false,
                    offlineMessage = ""
                )
            }
        }
    }

    fun loadTrips(showLoading: Boolean = true) {
        if (!networkHelper.isInternetAvailable()) {
            applyOfflineState()
            return
        }

        connectivity = true

        val token = sessionManager.getToken()
        if (token.isEmpty()) {
            TripMemoryCache.clear()
            localStorageManager.clearTripState()
            Log.d(TAG, "clear caches: no session token during loadTrips")
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "No hay una sesion activa."
            )
            return
        }

        val authId = extractAuthIdFromToken(token)
        if (authId.isNullOrEmpty()) {
            TripMemoryCache.clear()
            localStorageManager.clearTripState()
            Log.d(TAG, "clear caches: invalid auth id during loadTrips")
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "No se pudo validar el usuario."
            )
            return
        }

        if (showLoading) {
            uiState = uiState.copy(isLoading = true, errorMessage = "")
        } else {
            uiState = uiState.copy(errorMessage = "")
        }

        val pendingDriverIds = pendingDriverSet(authId)
        val pendingRiderIds = pendingRiderSet(authId)
        val skippedDriverIds = skippedDriverSet(authId)
        val skippedRiderIds = skippedRiderSet(authId)

        val persistedSkippedDriverIds = sessionManager.getSkippedDriverRatingRideIds(authId)
        val persistedSkippedRiderIds = sessionManager.getSkippedRiderRatingRideIds(authId)
        skippedDriverIds.addAll(persistedSkippedDriverIds)
        skippedRiderIds.addAll(persistedSkippedRiderIds)

        val persistedPendingDriverIds = sessionManager.getPendingDriverRatingRideIds(authId)
        val persistedPendingRiderIds = sessionManager.getPendingRiderRatingRideIds(authId)
        // Only add pending if not skipped (resolve inconsistencies)
        val validPendingDriverIds = persistedPendingDriverIds.filter { it !in skippedDriverIds }
        val validPendingRiderIds = persistedPendingRiderIds.filter { it !in skippedRiderIds }
        pendingDriverIds.addAll(validPendingDriverIds)
        pendingRiderIds.addAll(validPendingRiderIds)

        Log.d(
            "TripRating",
            "loaded persisted skipped authIdPresent=${authId.isNotBlank()} driver=${persistedSkippedDriverIds.size} rider=${persistedSkippedRiderIds.size}"
        )
        Log.d(
            "TripRating",
            "loaded pending authIdPresent=${authId.isNotBlank()} driver=${validPendingDriverIds.size} rider=${validPendingRiderIds.size}"
        )

        viewModelScope.launch {
            try {
                val user = tripRepository.getUserByAuthId(authId, token)
                if (user == null) {
                    Log.w(TAG, "loadTrips user not found from backend; keeping previous cache")
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "No se encontro el usuario."
                    )
                    return@launch
                }

                // Snapshot of current in-memory pending states before recalculation
                val currentPendingDriverRating = uiState.finishedRideIdForRating
                val currentPendingRiderRating = uiState.finishedRiderRideIdForRating

                val rider = tripRepository.getRiderByUserId(user.id, token)
                val driver = tripRepository.getDriverByUserId(user.id, token)

                val riderTrips = if (rider != null) {
                    val reservations = tripRepository.getActiveRiderReservation(rider.id, token)
                    reservations
                        .mapNotNull { reservation ->
                            reservation.rides?.let { ride ->
                                val reservationState = normalizeState(reservation.state)
                                val rideState = normalizeState(ride.state)

                                if (!shouldShowRiderReservation(reservationState, rideState)) {
                                    return@let null
                                }

                                val canCancelReservation = canCancelRiderReservation(
                                    reservationState = reservationState,
                                    rideState = rideState
                                )

                                ActiveRiderTripUiModel(
                                    reservationId = reservation.id,
                                    rideId = ride.id,
                                    source = ride.source,
                                    destination = ride.destination,
                                    status = reservationState,
                                    rideStatus = rideState,
                                    departureTime = ride.departure_time,
                                    canCancelReservation = canCancelReservation,
                                    showCancelButton = true,
                                    cancelDisabledReason = if (
                                        shouldDisableCancelButton(
                                            reservationState = reservationState,
                                            rideState = rideState
                                        ) && rideState == "EN_CURSO"
                                    ) {
                                        "No puedes cancelar un viaje en curso."
                                    } else {
                                        null
                                    },
                                    driverName = run {
                                        val firstName = ride.drivers?.users?.first_name.orEmpty()
                                        val lastName = ride.drivers?.users?.last_name.orEmpty()
                                        listOf(firstName, lastName)
                                            .filter { it.isNotBlank() }
                                            .joinToString(" ")
                                            .ifBlank { "Conductor" }
                                    },
                                    departureDate = extractDateFromDateTime(ride.date)
                                )
                            }
                        }
                        .sortedBy { stateOrder(it.status) }
                } else {
                    emptyList()
                }

                val finishedRiderReservationForRating = if (rider != null && riderTrips.isEmpty()) {
                    tripRepository.getFinishedRiderReservationForRating(rider.id, token)
                } else {
                    null
                }

                val driverTrip = if (driver != null) {
                    val activeRide = tripRepository.getActiveDriverRide(driver.id, token)
                    if (activeRide != null) {
                        val rideState = normalizeState(activeRide.state)
                        val reservations =
                            tripRepository.getReservationsForRide(activeRide.id, token)
                        val reservationItems = reservations
                            .sortedBy { stateOrder(it.state) }
                            .map { reservation ->
                                val firstName = reservation.riders?.users?.first_name.orEmpty()
                                val lastName = reservation.riders?.users?.last_name.orEmpty()
                                val riderName = listOf(firstName, lastName)
                                    .filter { it.isNotBlank() }
                                    .joinToString(" ")
                                    .ifBlank { "Rider" }

                                TripReservationItemUiModel(
                                    id = reservation.id,
                                    riderName = riderName,
                                    cancellationOdds = reservation.riders?.cancellation_odds,
                                    riderRating = reservation.riders?.rating,
                                    status = normalizeState(reservation.state)
                                )
                            }

                        val totalSeats = activeRide.vehicles?.number_slots ?: 0
                        val acceptedReservations = reservationItems.count { isAcceptedReservation(it.status) }
                        val availableSeats =
                            (totalSeats - acceptedReservations).coerceAtLeast(0)

                        ActiveDriverTripUiModel(
                            rideId = activeRide.id,
                            source = activeRide.source,
                            destination = activeRide.destination,
                            status = rideState,
                            departureTime = activeRide.departure_time,
                            reservationsCount = reservationItems.size,
                            totalSeats = totalSeats,
                            acceptedReservations = acceptedReservations,
                            availableSeats = availableSeats,
                            reservations = reservationItems,
                            departureDate = extractDateFromDateTime(activeRide.date)
                        )
                    } else {
                        null
                    }
                } else {
                    null
                }

                val finishedDriverRideForRating = if (driver != null && driverTrip == null) {
                    tripRepository.getFinishedDriverRideForRating(driver.id, token)
                } else {
                    null
                }
                Log.d("TripRating", "driver finishedRide=${finishedDriverRideForRating?.id}")

                // Detected driver pending from backend for this loadTrips cycle
                val detectedDriverPendingRideId = if (driverTrip == null && finishedDriverRideForRating != null && driver != null) {
                    val reservationsForFinishedRide = tripRepository.getReservationsForRide(
                        rideId = finishedDriverRideForRating.id,
                        token = token
                    )
                    val eligibleReservations = reservationsForFinishedRide
                        .filter { reservation ->
                            val reservationState = normalizeState(reservation.state)
                            reservationState == "ACEPTADA" || reservationState == "EN_CURSO"
                        }
                    val candidateRiderIds = eligibleReservations
                        .map { it.rider_id }
                        .distinct()

                    val ratedRiderIds = ratingRepository.getRatedRidersForRide(
                        token = token,
                        rideId = finishedDriverRideForRating.id,
                        driverId = driver.id
                    )
                    Log.d(
                        "TripRating",
                        "driver reservations=${reservationsForFinishedRide.size} eligible=${eligibleReservations.size} rated=${ratedRiderIds.size}"
                    )

                    // Check if all eligible riders are already rated
                    val hasPendingRidersToRate = candidateRiderIds.any { riderId -> riderId !in ratedRiderIds }
                    if (!hasPendingRidersToRate) {
                        Log.d("TripRating", "remove driver pending because all riders already rated rideId=${finishedDriverRideForRating.id}")
                        pendingDriverIds.remove(finishedDriverRideForRating.id)
                        sessionManager.removePendingDriverRatingRideId(authId, finishedDriverRideForRating.id)
                        null
                    } else {
                        if (hasPendingRidersToRate) finishedDriverRideForRating.id else null
                    }
                } else {
                    null
                }

                if (
                    detectedDriverPendingRideId != null &&
                    detectedDriverPendingRideId !in skippedDriverIds
                ) {
                    pendingDriverIds.add(detectedDriverPendingRideId)
                    sessionManager.addPendingDriverRatingRideId(authId, detectedDriverPendingRideId)
                }

                val memoryDriverPendingRideId = pendingDriverIds
                    .lastOrNull { it !in skippedDriverIds }

                // Preserve in-session pending if not skipped and no active driver trip;
                // avoids losing the pending card due to backend timing after trip finish
                val finalDriverPendingRideId = when {
                    driverTrip != null -> null
                    currentPendingDriverRating != null &&
                        currentPendingDriverRating !in skippedDriverIds -> currentPendingDriverRating
                    memoryDriverPendingRideId != null -> memoryDriverPendingRideId
                    detectedDriverPendingRideId != null &&
                        detectedDriverPendingRideId !in skippedDriverIds -> detectedDriverPendingRideId
                    else -> null
                }

                val currentRideId = when {
                    driverTrip != null -> driverTrip.rideId
                    riderTrips.isNotEmpty() -> riderTrips.first().rideId
                    else -> null
                }

                // Only accept reservations with state ACEPTADA or EN_CURSO for rider pending detection;
                // avoids showing pending card for cancelled, rejected or pending reservations
                val detectedRiderPendingRideId = if (riderTrips.isEmpty()) {
                    finishedRiderReservationForRating
                        ?.takeIf { reservation ->
                            val reservationState = normalizeState(reservation.state)
                            reservationState == "ACEPTADA" || reservationState == "EN_CURSO"
                        }
                        ?.rides
                        ?.takeIf { normalizeState(it.state) == "FINALIZADO" }
                        ?.id
                } else {
                    null
                }

                val riderReservationState = normalizeState(finishedRiderReservationForRating?.state)
                val riderRideState = normalizeState(finishedRiderReservationForRating?.rides?.state)
                Log.d(
                    "TripRating",
                    "rider finishedReservation=${finishedRiderReservationForRating?.id} ride=${finishedRiderReservationForRating?.rides?.id}"
                )
                Log.d(
                    "TripRating",
                    "rider states reservation=$riderReservationState ride=$riderRideState"
                )

                // Validate if rider already rated the driver for this ride
                var validDetectedRiderPendingRideId: Int? = detectedRiderPendingRideId
                if (validDetectedRiderPendingRideId != null && rider != null && finishedRiderReservationForRating != null) {
                    val driverId = finishedRiderReservationForRating.rides?.drivers?.id
                    if (driverId != null) {
                        val ratedDriverIds = ratingRepository.getRatedDriversForRide(
                            token = token,
                            rideId = validDetectedRiderPendingRideId,
                            riderId = rider.id
                        )
                        if (driverId in ratedDriverIds) {
                            Log.d("TripRating", "remove rider pending because driver already rated rideId=$validDetectedRiderPendingRideId")
                            pendingRiderIds.remove(validDetectedRiderPendingRideId)
                            sessionManager.removePendingRiderRatingRideId(authId, validDetectedRiderPendingRideId)
                            validDetectedRiderPendingRideId = null
                        }
                    }
                }

                if (
                    validDetectedRiderPendingRideId != null &&
                    validDetectedRiderPendingRideId !in skippedRiderIds
                ) {
                    pendingRiderIds.add(validDetectedRiderPendingRideId)
                    sessionManager.addPendingRiderRatingRideId(authId, validDetectedRiderPendingRideId)
                }

                val memoryRiderPendingRideId = pendingRiderIds
                    .lastOrNull { it !in skippedRiderIds }

                // Preserve in-session pending rider rating if not skipped and no active rider trips
                val finalRiderPendingRideId = when {
                    riderTrips.isNotEmpty() -> null
                    currentPendingRiderRating != null &&
                        currentPendingRiderRating !in skippedRiderIds -> currentPendingRiderRating
                    memoryRiderPendingRideId != null -> memoryRiderPendingRideId
                    validDetectedRiderPendingRideId != null &&
                        validDetectedRiderPendingRideId !in skippedRiderIds -> validDetectedRiderPendingRideId
                    else -> null
                }

                Log.d(
                    "TripRating",
                    "authIdPresent=${authId.isNotBlank()} driver pending current=$currentPendingDriverRating memory=$memoryDriverPendingRideId detected=$detectedDriverPendingRideId final=$finalDriverPendingRideId skipped=${finalDriverPendingRideId in skippedDriverIds}"
                )
                Log.d(
                    "TripRating",
                    "authIdPresent=${authId.isNotBlank()} rider pending current=$currentPendingRiderRating memory=$memoryRiderPendingRideId detected=$detectedRiderPendingRideId final=$finalRiderPendingRideId skipped=${finalRiderPendingRideId in skippedRiderIds}"
                )

                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "",
                    activeRiderTrips = riderTrips,
                    activeDriverTrip = driverTrip,
                    currentUserId = user.id,
                    currentRideId = currentRideId,
                    isOfflineData = false,
                    offlineMessage = "",
                    finishedRideIdForRating = finalDriverPendingRideId,
                    finishedRiderRideIdForRating = finalRiderPendingRideId
                )

                // Save to memory cache with filtered driver reservations
                val riderTripsForStorage = riderTrips.filter { riderTrip ->
                    val reservationState = normalizeState(riderTrip.status)
                    val rideState = normalizeState(riderTrip.rideStatus)
                    val reservationIsActive = reservationState == "PENDIENTE" ||
                        reservationState == "ACEPTADA" || reservationState == "EN_CURSO"
                    val rideIsActive = rideState != "FINALIZADO" && rideState != "CANCELADO"
                    reservationIsActive && rideIsActive
                }

                val driverTripForStorage = buildDriverTripForStorage(driverTrip)

                TripMemoryCache.save(
                    activeRiderTrips = riderTripsForStorage,
                    activeDriverTrip = driverTripForStorage,
                    currentUserId = user.id,
                    currentRideId = currentRideId,
                    authId = authId
                )
                Log.d(
                    TAG,
                    "saved RAM authIdPresent=true rider=${riderTripsForStorage.size} driverRes=${driverTripForStorage?.reservations?.size ?: 0}"
                )

                localStorageManager.saveTripState(
                    TripStorageDto(
                        authId = authId,
                        currentUserId = user.id,
                        currentRideId = currentRideId,
                        activeRiderTrips = riderTripsForStorage,
                        activeDriverTrip = driverTripForStorage,
                        savedAt = System.currentTimeMillis()
                    )
                )
                Log.d(
                    TAG,
                    "saved FILE authIdPresent=true rider=${riderTripsForStorage.size} driverRes=${driverTripForStorage?.reservations?.size ?: 0}"
                )

                if (currentRideId != null) {
                    sessionManager.saveCurrentRideId(currentRideId)
                    loadLocationsForCurrentRide()
                }
            } catch (e: Exception) {
                Log.e("TripViewModel", "loadTrips exception", e)
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "No se pudo cargar la informacion de viajes."
                )
            }

        }
    }

    fun onCancelReservationClicked(reservationId: Int) {
        if (!canRunOnlineAction("Necesitas conexión a internet para cancelar esta reserva.")) {
            return
        }

        changeReservationState(
            reservationId = reservationId,
            newState = "CANCELADA",
            successMessage = "Reserva cancelada correctamente."
        )
    }

    fun onAcceptReservationClicked(reservationId: Int) {
        if (!canRunOnlineAction("Necesitas conexión a internet para aceptar esta reserva.")) {
            return
        }

        val currentTrip = uiState.activeDriverTrip
        if (currentTrip == null) {
            uiState = uiState.copy(infoMessage = "No tienes un viaje activo como conductor.")
            return
        }

        if (!canManageReservation(currentTrip.status)) {
            uiState = uiState.copy(infoMessage = "Solo puedes gestionar reservas cuando el viaje esta ofertado.")
            return
        }

        val reservation = currentTrip.reservations.firstOrNull { it.id == reservationId }
        if (reservation == null || !isPendingReservation(reservation.status)) {
            uiState = uiState.copy(infoMessage = "La reserva ya no esta pendiente.")
            return
        }

        val acceptedCount = currentTrip.reservations.count { isAcceptedReservation(it.status) }
        if (!canAcceptMoreReservations(acceptedCount, currentTrip.totalSeats)) {
            uiState =
                uiState.copy(infoMessage = "No hay cupos disponibles para aceptar mas reservas.")
            return
        }

        changeReservationState(
            reservationId = reservationId,
            newState = "ACEPTADA",
            successMessage = "Reserva aceptada."
        )
    }

    fun onRejectReservationClicked(reservationId: Int) {
        if (!canRunOnlineAction("Necesitas conexión a internet para rechazar esta reserva.")) {
            return
        }

        val currentTrip = uiState.activeDriverTrip
        if (currentTrip == null) {
            uiState = uiState.copy(infoMessage = "No tienes un viaje activo como conductor.")
            return
        }

        if (!canManageReservation(currentTrip.status)) {
            uiState = uiState.copy(infoMessage = "Solo puedes gestionar reservas cuando el viaje esta ofertado.")
            return
        }

        val reservation = currentTrip.reservations.firstOrNull { it.id == reservationId }
        if (reservation == null || !isPendingReservation(reservation.status)) {
            uiState = uiState.copy(infoMessage = "La reserva ya no esta pendiente.")
            return
        }

        changeReservationState(
            reservationId = reservationId,
            newState = "RECHAZADA",
            successMessage = "Reserva rechazada."
        )
    }

    fun onCancelTripClicked() {
        if (!canRunOnlineAction("Necesitas conexión a internet para cancelar este viaje.")) {
            return
        }

        val current = uiState.activeDriverTrip ?: return
        Log.d("TripCancel", "Driver cancel rideId=${current.rideId}")
        changeRideState(
            rideId = current.rideId,
            newState = "CANCELADO",
            successMessage = "Viaje cancelado.",
            rejectActiveReservations = true
        )
    }

    fun onStartTripClicked() {
        if (!canRunOnlineAction("Necesitas conexión a internet para iniciar este viaje.")) {
            return
        }

        val current = uiState.activeDriverTrip ?: return
        if (!canManageReservation(current.status)) {
            uiState = uiState.copy(infoMessage = "Solo puedes iniciar un viaje ofertado.")
            return
        }

        startTripAndCleanPendingReservations(current)
    }

    fun onFinishTripClicked() {
        if (!canRunOnlineAction("Necesitas conexión a internet para finalizar este viaje.")) {
            return
        }

        val current = uiState.activeDriverTrip ?: return
        changeRideState(
            rideId = current.rideId,
            newState = "FINALIZADO",
            successMessage = "Viaje finalizado."
        )
    }

    fun refreshTrips() {
        loadTrips(showLoading = false)
    }

    fun onOpenRouteClicked() {
        if (!canRunOnlineAction("Necesitas conexión a internet para abrir la ruta.")) {
            return
        }

        uiState = uiState.copy(infoMessage = "Abrir ruta disponible pronto.")
    }

    fun clearInfoMessage() {
        uiState = uiState.copy(
            infoMessage = "",
            locationErrorMessage = ""
        )
    }

    fun clearFinishedRideForRating() {
        val rideId = uiState.finishedRideIdForRating
        val authId = resolveCurrentAuthId()
        if (rideId != null && authId != null) {
            skippedDriverSet(authId).add(rideId)
            pendingDriverSet(authId).remove(rideId)
            sessionManager.addSkippedDriverRatingRideId(authId, rideId)
            sessionManager.removePendingDriverRatingRideId(authId, rideId)
            Log.d("TripRating", "persist skip driver authIdPresent=true rideId=$rideId")
        } else if (rideId != null) {
            Log.d("TripRating", "skip driver not persisted authIdPresent=false rideId=$rideId")
        }
        uiState = uiState.copy(finishedRideIdForRating = null)
    }

    fun clearFinishedRiderRideForRating() {
        val rideId = uiState.finishedRiderRideIdForRating
        val authId = resolveCurrentAuthId()
        if (rideId != null && authId != null) {
            skippedRiderSet(authId).add(rideId)
            pendingRiderSet(authId).remove(rideId)
            sessionManager.addSkippedRiderRatingRideId(authId, rideId)
            sessionManager.removePendingRiderRatingRideId(authId, rideId)
            Log.d("TripRating", "persist skip rider authIdPresent=true rideId=$rideId")
        } else if (rideId != null) {
            Log.d("TripRating", "skip rider not persisted authIdPresent=false rideId=$rideId")
        }
        uiState = uiState.copy(finishedRiderRideIdForRating = null)
    }

    fun completeFinishedRideForRating() {
        // Rating completed: clears pending without marking as skipped;
        // backend anti-duplicate check will prevent re-detection on next loadTrips
        val rideId = uiState.finishedRideIdForRating
        val authId = resolveCurrentAuthId()
        if (rideId != null && authId != null) {
            pendingDriverSet(authId).remove(rideId)
            sessionManager.removePendingDriverRatingRideId(authId, rideId)
            Log.d("TripRating", "complete driver rating rideId=$rideId")
        } else if (rideId != null) {
            Log.d("TripRating", "complete driver rating not persisted rideId=$rideId")
        }
        uiState = uiState.copy(finishedRideIdForRating = null)
    }

    fun completeFinishedRiderRideForRating() {
        // Rating completed: clears pending without marking as skipped;
        // backend anti-duplicate check will prevent re-detection on next loadTrips
        val rideId = uiState.finishedRiderRideIdForRating
        val authId = resolveCurrentAuthId()
        if (rideId != null && authId != null) {
            pendingRiderSet(authId).remove(rideId)
            sessionManager.removePendingRiderRatingRideId(authId, rideId)
            Log.d("TripRating", "complete rider rating rideId=$rideId")
        } else if (rideId != null) {
            Log.d("TripRating", "complete rider rating not persisted rideId=$rideId")
        }
        uiState = uiState.copy(finishedRiderRideIdForRating = null)
    }

    fun clearRatingSessionMemory() {
        pendingDriverRatingRideIdsByAuth.clear()
        pendingRiderRatingRideIdsByAuth.clear()
        skippedDriverRatingRideIdsByAuth.clear()
        skippedRiderRatingRideIdsByAuth.clear()
    }

    fun onToggleLocationSharing(enabled: Boolean) {
        if (!canRunOnlineAction("Necesitas conexión para compartir tu ubicación.")) {
            return
        }

        sessionManager.saveLocationSharingEnabled(enabled)

        uiState = if (enabled) {
            uiState.copy(
                isLocationSharingEnabled = true,
                locationErrorMessage = "",
                locationStatusMessage = "Compartiendo ubicación."
            )
        } else {
            uiState.copy(
                isLocationSharingEnabled = false,
                currentLatitude = null,
                currentLongitude = null,
                isLocationLoading = false,
                lastLocationTimestamp = null,
                locationErrorMessage = "",
                locationStatusMessage = "Ubicación compartida desactivada."
            )
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        if (!canRunOnlineAction()) {
            return
        }

        uiState = uiState.copy(
            hasLocationPermission = granted,
            locationErrorMessage = if (granted) "" else "Permiso de ubicación denegado.",
            locationStatusMessage = if (granted) uiState.locationStatusMessage else "No se concedió el permiso de ubicación."
        )
    }

    fun onLocationRequestStarted() {
        if (!canRunOnlineAction()) {
            return
        }

        uiState = uiState.copy(
            isLocationLoading = true,
            locationErrorMessage = "",
            locationStatusMessage = "Obteniendo ubicación..."
        )
    }

    fun onLocationUpdated(latitude: Double, longitude: Double) {
        if (!canRunOnlineAction()) {
            return
        }

        uiState = uiState.copy(
            currentLatitude = latitude,
            currentLongitude = longitude,
            isLocationLoading = false,
            lastLocationTimestamp = System.currentTimeMillis(),
            locationErrorMessage = "",
            locationStatusMessage = "Ubicación actualizada."
        )

        saveCurrentLocationToBackend(latitude, longitude)
    }

    fun onLocationRequestFailed(message: String) {
        if (!canRunOnlineAction()) {
            return
        }

        uiState = uiState.copy(
            isLocationLoading = false,
            locationErrorMessage = message,
            locationStatusMessage = "No se pudo obtener la ubicación."
        )
    }

    fun onLocationCleared() {
        uiState = uiState.copy(
            currentLatitude = null,
            currentLongitude = null,
            isLocationLoading = false,
            lastLocationTimestamp = null,
            locationErrorMessage = "",
            locationStatusMessage = "Ubicación limpiada."
        )
    }

    private fun saveCurrentLocationToBackend(latitude: Double, longitude: Double) {
        val token = sessionManager.getToken()
        val userId = uiState.currentUserId
        val rideId = uiState.currentRideId

        if (!uiState.isLocationSharingEnabled) return
        if (token.isEmpty()) return
        if (userId == null || rideId == null) return

        viewModelScope.launch {
            val success = locationRepository.saveLocation(
                location = UserSharedLocation(
                    userId = userId,
                    rideId = rideId,
                    latitude = latitude,
                    longitude = longitude,
                    timestamp = buildIsoTimestamp(),
                    isSharingEnabled = true
                ),
                token = token
            )

            if (!success) {
                uiState = uiState.copy(
                    locationErrorMessage = "No se pudo guardar la ubicación en el servidor.",
                    locationStatusMessage = "Ubicación obtenida localmente, pero no se pudo registrar."
                )
            } else {
                loadLocationsForCurrentRide()
            }
        }
    }

    private fun canRunOnlineAction(errorMessage: String = ""): Boolean {
        val isOnline = tripRepository.availableConnection()
        connectivity = isOnline

        if (!isOnline) {
            if (errorMessage.isNotEmpty()) {
                uiState = uiState.copy(infoMessage = errorMessage)
            }
            return false
        }

        return true
    }

    private fun changeReservationState(
        reservationId: Int,
        newState: String,
        successMessage: String
    ) {
        val token = sessionManager.getToken()
        if (token.isEmpty()) {
            uiState = uiState.copy(infoMessage = "No hay sesion activa.")
            return
        }

        viewModelScope.launch {
            val success = tripRepository.updateReservationState(reservationId, newState, token)
            uiState = if (success) {
                uiState.copy(infoMessage = successMessage)
            } else {
                uiState.copy(infoMessage = "No se pudo actualizar la reserva.")
            }
            if (success) loadTrips(showLoading = false)
        }
    }

    private fun changeRideState(
        rideId: Int,
        newState: String,
        successMessage: String,
        rejectActiveReservations: Boolean = false
    ) {
        val token = sessionManager.getToken()
        if (token.isEmpty()) {
            uiState = uiState.copy(infoMessage = "No hay sesion activa.")
            return
        }

        viewModelScope.launch {
            val success = tripRepository.updateRideState(rideId, newState, token)
            if (newState == "CANCELADO") {
                Log.d("TripCancel", "Ride cancelled result=$success for rideId=$rideId")
            }

            if (success && rejectActiveReservations) {
                val rejectResult = tripRepository.rejectActiveReservationsForRide(rideId, token)
                Log.d("TripCancel", "Reject reservations result=$rejectResult for rideId=$rideId")
            }

            uiState = if (success) {
                val newUiState = uiState.copy(infoMessage = successMessage)
                // If finish trip was successful, set finishedRideIdForRating to trigger rating dialog
                if (newState == "FINALIZADO") {
                    val authId = extractAuthIdFromToken(token)
                    if (!authId.isNullOrBlank()) {
                        pendingDriverSet(authId).add(rideId)
                        sessionManager.addPendingDriverRatingRideId(authId, rideId)
                        Log.d("TripRating", "persist pending driver rideId=$rideId")
                    }
                    newUiState.copy(finishedRideIdForRating = rideId)
                } else {
                    newUiState
                }
            } else {
                uiState.copy(infoMessage = "No se pudo actualizar el viaje.")
            }
            if (success) loadTrips(showLoading = false)
        }
    }

    private fun extractDateFromDateTime(dateTime: String): String {
        return try {
            // dateTime is expected to be in format like "2025-05-09 14:30:00" or "2025-05-09"
            dateTime.split(" ").firstOrNull() ?: "Por definir"
        } catch (_: Exception) {
            "Por definir"
        }
    }

    private fun extractAuthIdFromToken(token: String): String? {
        return try {
            val parts = token.split('.')
            if (parts.size < 2) return null

            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val payload = String(payloadBytes)

            val subRegex = "\"sub\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            subRegex.find(payload)?.groupValues?.get(1)
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveCurrentAuthId(): String? {
        val token = sessionManager.getToken()
        if (token.isBlank()) return null
        return extractAuthIdFromToken(token)
    }

    private fun pendingDriverSet(authId: String): MutableSet<Int> {
        return pendingDriverRatingRideIdsByAuth.getOrPut(authId) { mutableSetOf() }
    }

    private fun pendingRiderSet(authId: String): MutableSet<Int> {
        return pendingRiderRatingRideIdsByAuth.getOrPut(authId) { mutableSetOf() }
    }

    private fun skippedDriverSet(authId: String): MutableSet<Int> {
        return skippedDriverRatingRideIdsByAuth.getOrPut(authId) { mutableSetOf() }
    }

    private fun skippedRiderSet(authId: String): MutableSet<Int> {
        return skippedRiderRatingRideIdsByAuth.getOrPut(authId) { mutableSetOf() }
    }

    private fun buildIsoTimestamp(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date())
    }

    private fun stateOrder(state: String): Int = when (normalizeState(state)) {
        "PENDIENTE" -> 0
        "ACEPTADA" -> 1
        "EN_CURSO" -> 2
        else -> 3
    }

    private fun shouldShowRiderReservation(
        reservationState: String?,
        rideState: String?
    ): Boolean {
        val normalizedReservationState = normalizeState(reservationState)
        val normalizedRideState = normalizeState(rideState)

        if (normalizedReservationState in setOf("CANCELADO", "RECHAZADA")) return false
        if (normalizedRideState in setOf("FINALIZADO", "CANCELADO")) return false

        return when (normalizedReservationState) {
            "PENDIENTE", "ACEPTADA" -> true
            "EN_CURSO" -> normalizedRideState == "EN_CURSO"
            else -> false
        }
    }

    private fun canCancelRiderReservation(
        reservationState: String?,
        rideState: String?
    ): Boolean {
        val normalizedReservationState = normalizeState(reservationState)
        val normalizedRideState = normalizeState(rideState)

        val reservationCanBeCancelled =
            normalizedReservationState == "PENDIENTE" || normalizedReservationState == "ACEPTADA"
        val rideAllowsCancellation = normalizedRideState == "OFERTADO"

        return reservationCanBeCancelled && rideAllowsCancellation
    }

    private fun shouldDisableCancelButton(
        reservationState: String?,
        rideState: String?
    ): Boolean {
        val shouldShow = shouldShowRiderReservation(reservationState, rideState)
        return shouldShow && !canCancelRiderReservation(reservationState, rideState)
    }

    private fun isPendingReservation(state: String?): Boolean {
        return normalizeState(state) == "PENDIENTE"
    }

    private fun isAcceptedReservation(state: String?): Boolean {
        val normalizedState = normalizeState(state)
        return normalizedState == "ACEPTADA" || normalizedState == "EN_CURSO"
    }

    private fun canManageReservation(rideState: String?): Boolean {
        return normalizeState(rideState) == "OFERTADO"
    }

    private fun buildDriverTripForStorage(driverTrip: ActiveDriverTripUiModel?): ActiveDriverTripUiModel? {
        if (driverTrip == null) return null

        val rideState = normalizeState(driverTrip.status)
        val isActiveRide = rideState == "OFERTADO" || rideState == "EN_CURSO"
        if (!isActiveRide) return null

        val filteredReservations = driverTrip.reservations.filter { reservation ->
            val reservationState = normalizeState(reservation.status)
            reservationState == "ACEPTADA" || reservationState == "EN_CURSO"
        }

        return driverTrip.copy(
            reservations = filteredReservations,
            reservationsCount = filteredReservations.size,
            acceptedReservations = filteredReservations.size
        )
    }

    private fun canAcceptMoreReservations(acceptedCount: Int, totalSeats: Int): Boolean {
        return acceptedCount < totalSeats
    }

    private fun startTripAndCleanPendingReservations(currentTrip: ActiveDriverTripUiModel) {
        val token = sessionManager.getToken()
        if (token.isEmpty()) {
            uiState = uiState.copy(infoMessage = "No hay sesion activa.")
            return
        }

        viewModelScope.launch {
            val pendingReservations = currentTrip.reservations.filter {
                isPendingReservation(it.status)
            }

            for (reservation in pendingReservations) {
                val rejectSuccess = tripRepository.updateReservationState(
                    reservationId = reservation.id,
                    newState = "RECHAZADA",
                    token = token
                )

                if (!rejectSuccess) {
                    uiState = uiState.copy(
                        infoMessage = "No se pudieron limpiar las reservas pendientes. Intenta de nuevo."
                    )
                    return@launch
                }
            }

            val startSuccess = tripRepository.updateRideState(
                rideId = currentTrip.rideId,
                newState = "EN_CURSO",
                token = token
            )

            uiState = if (startSuccess) {
                uiState.copy(infoMessage = "Viaje iniciado.")
            } else {
                uiState.copy(infoMessage = "No se pudo actualizar el viaje.")
            }

            if (startSuccess) {
                loadTrips(showLoading = false)
            }
        }
    }


    fun loadLocationsForCurrentRide() {
        val token = sessionManager.getToken()
        val rideId = uiState.currentRideId

        if (token.isEmpty() || rideId == null) {
            return
        }

        viewModelScope.launch {
            try {
                val locations = locationRepository.getLocationsByRide(
                    rideId = rideId,
                    token = token
                )

                uiState = uiState.copy(
                    rideLocations = locations.locations,
                    isUsingCachedLocations = locations.isFromCache,
                    cachedLocationMessage = locations.message
                )
            } catch (e: Exception) {
                Log.e("TripViewModel", "loadLocationsForCurrentRide exception", e)
            }
        }
    }




    fun getMapMarkers(): List<MapUserMarkerUiState> {
        val markers = mutableListOf<MapUserMarkerUiState>()
        val currentUserId = uiState.currentUserId ?: return emptyList()

        val currentLat = uiState.currentLatitude
        val currentLng = uiState.currentLongitude

        if (currentLat != null && currentLng != null) {
            markers.add(
                MapUserMarkerUiState(
                    userId = currentUserId,
                    initials = "Tú",
                    latitude = currentLat,
                    longitude = currentLng,
                    isCurrentUser = true,
                    isDriver = uiState.activeDriverTrip != null,
                    distanceMeters = null
                )
            )
        }

        val userNames = mutableMapOf<Int, String>()

        uiState.activeDriverTrip?.reservations?.forEach { reservation ->
            userNames[reservation.id] = reservation.riderName
        }

        val latestLocations = uiState.rideLocations
            .filter { it.isSharingEnabled }
            .groupBy { it.userId }
            .mapNotNull { (_, list) -> list.maxByOrNull { it.timestamp } }

        latestLocations.forEach { location ->
            if (location.userId == currentUserId) return@forEach

            val name = userNames[location.userId] ?: "Usuario"

            val distance = if (currentLat != null && currentLng != null) {
                calculateDistanceMeters(
                    lat1 = currentLat,
                    lon1 = currentLng,
                    lat2 = location.latitude,
                    lon2 = location.longitude
                )
            } else {
                null
            }

            markers.add(
                MapUserMarkerUiState(
                    userId = location.userId,
                    initials = buildInitials(name),
                    latitude = location.latitude,
                    longitude = location.longitude,
                    isCurrentUser = false,
                    isDriver = false,
                    distanceMeters = distance
                )
            )
        }

        return markers
    }



    private fun calculateDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Int {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0].toInt()
    }
}