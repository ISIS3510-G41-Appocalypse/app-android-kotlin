package com.gn41.appandroidkotlin.presentation.viewmodels

import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.LocationRepository
import com.gn41.appandroidkotlin.data.repositories.TripRepository
import com.gn41.appandroidkotlin.domain.UserSharedLocation
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TripViewModel(
    private val tripRepository: TripRepository,
    private val sessionManager: SessionManager,
    private val locationRepository: LocationRepository
) : ViewModel() {

    var uiState by mutableStateOf(TripUiState())
        private set

    init {
        uiState = uiState.copy(
            isLocationSharingEnabled = sessionManager.isLocationSharingEnabled()
        )
        loadTrips()
    }

    fun loadTrips(showLoading: Boolean = true) {
        val token = sessionManager.getToken()
        if (token.isEmpty()) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "No hay una sesion activa."
            )
            return
        }

        val authId = extractAuthIdFromToken(token)
        if (authId.isNullOrEmpty()) {
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
                        .sortedBy { stateOrder(it.state) }
                        .mapNotNull { reservation ->
                            reservation.rides?.let { ride ->
                                ActiveRiderTripUiModel(
                                    reservationId = reservation.id,
                                    rideId = ride.id,
                                    source = ride.source,
                                    destination = ride.destination,
                                    status = reservation.state,
                                    rideStatus = ride.state,
                                    departureTime = ride.departure_time
                                )
                            }
                        }
                } else {
                    emptyList()
                }

                val driverTrip = if (driver != null) {
                    val activeRide = tripRepository.getActiveDriverRide(driver.id, token)
                    if (activeRide != null) {
                        val reservations = tripRepository.getReservationsForRide(activeRide.id, token)
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
                                    status = reservation.state
                                )
                            }

                        val totalSeats = activeRide.vehicles?.number_slots ?: 0
                        val acceptedReservations = reservationItems.count {
                            it.status == "ACEPTADA" || it.status == "EN_CURSO"
                        }
                        val availableSeats = (totalSeats - acceptedReservations).coerceAtLeast(0)

                        ActiveDriverTripUiModel(
                            rideId = activeRide.id,
                            source = activeRide.source,
                            destination = activeRide.destination,
                            status = activeRide.state,
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
                    currentRideId = currentRideId
                )

                if (currentRideId != null) {
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
        changeReservationState(
            reservationId = reservationId,
            newState = "CANCELADA",
            successMessage = "Reserva cancelada correctamente."
        )
    }

    fun onAcceptReservationClicked(reservationId: Int) {
        val currentTrip = uiState.activeDriverTrip
        if (currentTrip != null && currentTrip.availableSeats <= 0) {
            uiState = uiState.copy(infoMessage = "No hay cupos disponibles para aceptar mas reservas.")
            return
        }

        changeReservationState(
            reservationId = reservationId,
            newState = "ACEPTADA",
            successMessage = "Reserva aceptada."
        )
    }

    fun onRejectReservationClicked(reservationId: Int) {
        changeReservationState(
            reservationId = reservationId,
            newState = "RECHAZADA",
            successMessage = "Reserva rechazada."
        )
    }

    fun onCancelTripClicked() {
        val current = uiState.activeDriverTrip ?: return
        changeRideState(
            rideId = current.rideId,
            newState = "CANCELADO",
            successMessage = "Viaje cancelado."
        )
    }

    fun onStartTripClicked() {
        val current = uiState.activeDriverTrip ?: return
        changeRideState(
            rideId = current.rideId,
            newState = "EN_CURSO",
            successMessage = "Viaje iniciado."
        )
    }

    fun onFinishTripClicked() {
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
        uiState = uiState.copy(infoMessage = "Abrir ruta disponible pronto.")
    }

    fun clearInfoMessage() {
        uiState = uiState.copy(
            infoMessage = "",
            locationErrorMessage = ""
        )
    }

    fun onToggleLocationSharing(enabled: Boolean) {
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
        uiState = uiState.copy(
            hasLocationPermission = granted,
            locationErrorMessage = if (granted) "" else "Permiso de ubicación denegado.",
            locationStatusMessage = if (granted) uiState.locationStatusMessage else "No se concedió el permiso de ubicación."
        )
    }

    fun onLocationRequestStarted() {
        uiState = uiState.copy(
            isLocationLoading = true,
            locationErrorMessage = "",
            locationStatusMessage = "Obteniendo ubicación..."
        )
    }

    fun onLocationUpdated(latitude: Double, longitude: Double) {
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
        successMessage: String
    ) {
        val token = sessionManager.getToken()
        if (token.isEmpty()) {
            uiState = uiState.copy(infoMessage = "No hay sesion activa.")
            return
        }

        viewModelScope.launch {
            val success = tripRepository.updateRideState(rideId, newState, token)
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

    private fun stateOrder(state: String): Int = when (state) {
        "PENDIENTE" -> 0
        "ACEPTADA" -> 1
        "EN_CURSO" -> 2
        else -> 3
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
                    rideLocations = locations
                )
            } catch (e: Exception) {
                Log.e("TripViewModel", "loadLocationsForCurrentRide exception", e)
            }
        }
    }
}