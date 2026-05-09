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
    private val sessionManager: SessionManager,
    private val locationRepository: LocationRepository,
    private val networkHelper: NetworkHelper,
    private val localStorageManager: LocalStorageManager
) : ViewModel() {

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
            val localState = localStorageManager.readTripState(authId)
            if (localState != null) {
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

        viewModelScope.launch {
            try {
                val user = tripRepository.getUserByAuthId(authId, token)
                if (user == null) {
                    TripMemoryCache.clear()
                    localStorageManager.clearTripState()
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "No se encontro el usuario."
                    )
                    return@launch
                }

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
                                    }
                                )
                            }
                        }
                        .sortedBy { stateOrder(it.status) }
                } else {
                    emptyList()
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
                            reservations = reservationItems
                        )
                    } else {
                        null
                    }
                } else {
                    null
                }

                val currentRideId = when {
                    driverTrip != null -> driverTrip.rideId
                    riderTrips.isNotEmpty() -> riderTrips.first().rideId
                    else -> null
                }

                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "",
                    activeRiderTrips = riderTrips,
                    activeDriverTrip = driverTrip,
                    currentUserId = user.id,
                    currentRideId = currentRideId,
                    isOfflineData = false,
                    offlineMessage = ""
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
                uiState.copy(infoMessage = successMessage)
            } else {
                uiState.copy(infoMessage = "No se pudo actualizar el viaje.")
            }
            if (success) loadTrips(showLoading = false)
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