package com.gn41.appandroidkotlin.presentation.viewmodels

import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.TripRepository
import kotlinx.coroutines.launch

class TripViewModel(
    private val tripRepository: TripRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var uiState by mutableStateOf(TripUiState())
        private set



    init {
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

                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "",
                    activeRiderTrips = riderTrips,
                    activeDriverTrip = driverTrip
                )
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
        uiState = uiState.copy(isLocationSharingEnabled = enabled)
    }

    fun onLocationPermissionResult(granted: Boolean) {
        uiState = uiState.copy(hasLocationPermission = granted)
    }

    fun onLocationUpdated(latitude: Double, longitude: Double) {
        uiState = uiState.copy(
            currentLatitude = latitude,
            currentLongitude = longitude
        )
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

    // pendiente primero, luego aceptada, luego en curso
    private fun stateOrder(state: String): Int = when (state) {
        "PENDIENTE" -> 0
        "ACEPTADA" -> 1
        "EN_CURSO" -> 2
        else -> 3
    }
}

