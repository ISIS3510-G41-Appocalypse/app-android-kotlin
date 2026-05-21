package com.gn41.appandroidkotlin.presentation.viewmodels

import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gn41.appandroidkotlin.data.dto.ratings.RateDriverRequestDto
import com.gn41.appandroidkotlin.data.dto.ratings.RateRiderRequestDto
import com.gn41.appandroidkotlin.data.repositories.RatingRepository
import com.gn41.appandroidkotlin.data.repositories.TripRepository
import com.gn41.appandroidkotlin.data.local.SessionManager
import kotlinx.coroutines.launch

class RatingViewModel(
    private val tripRepository: TripRepository,
    private val ratingRepository: RatingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var uiState by mutableStateOf(RatingUiState())
        private set

    fun loadRatingData(rideId: Int, ratingType: String) {
        val normalizedRatingType = if (ratingType == "rider") "rider" else "driver"
        val token = sessionManager.getToken()
        if (token.isEmpty()) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "No hay una sesión activa.",
                successMessage = null
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null,
                rideId = rideId,
                ratingType = normalizedRatingType,
                selectedRiderId = null,
                ridersToRate = emptyList(),
                punctuality = 0,
                behavior = 0,
                communication = 0,
                security = 0,
                paymentPunctuality = 0
            )

            try {
                val authId = extractAuthIdFromToken(token)
                if (authId.isNullOrEmpty()) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "No se pudo obtener la información del usuario."
                    )
                    return@launch
                }

                val user = tripRepository.getUserByAuthId(authId, token)
                if (user == null) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "Usuario no encontrado."
                    )
                    return@launch
                }

                val rider = tripRepository.getRiderByUserId(user.id, token)
                val driver = tripRepository.getDriverByUserId(user.id, token)

                if (normalizedRatingType == "rider") {
                    if (driver == null) {
                        uiState = uiState.copy(
                            isLoading = false,
                            errorMessage = "No se pudo obtener la información del conductor."
                        )
                        return@launch
                    }

                    val reservations = tripRepository.getReservationsForRide(rideId, token)
                    val ratedRiderIds = ratingRepository.getRatedRidersForRide(
                        token = token,
                        rideId = rideId,
                        driverId = driver.id
                    )
                    val riders = reservations
                        .mapNotNull { reservation ->
                            val nestedRider = reservation.riders ?: return@mapNotNull null
                            val firstName = nestedRider.users?.first_name.orEmpty()
                            val lastName = nestedRider.users?.last_name.orEmpty()
                            val riderName = listOf(firstName, lastName)
                                .filter { it.isNotBlank() }
                                .joinToString(" ")
                                .ifBlank { "Rider" }

                            RateUserUiModel(
                                riderId = nestedRider.id,
                                name = riderName,
                                rating = nestedRider.rating
                            )
                        }
                        .distinctBy { it.riderId }
                        .filter { riderToRate ->
                            val riderId = riderToRate.riderId
                            riderId != null && riderId !in ratedRiderIds
                        }

                    uiState = uiState.copy(
                        isLoading = false,
                        riderId = rider?.id,
                        driverId = driver.id,
                        ridersToRate = riders,
                        selectedRiderId = riders.firstOrNull()?.riderId,
                        errorMessage = if (riders.isEmpty()) "No hay pasajeros pendientes por calificar." else null
                    )
                } else {
                    if (rider == null) {
                        uiState = uiState.copy(
                            isLoading = false,
                            errorMessage = "No se pudo obtener la información del pasajero."
                        )
                        return@launch
                    }

                    val finishedReservation = tripRepository.getFinishedRiderReservationForRating(
                        riderId = rider.id,
                        token = token
                    )
                    val fallbackReservation = tripRepository.getReservationByRideAndRider(
                        rideId = rideId,
                        riderId = rider.id,
                        token = token
                    )
                    val reservation = when {
                        normalizeState(finishedReservation?.rides?.state) == "FINALIZADO" -> finishedReservation
                        normalizeState(fallbackReservation?.rides?.state) == "FINALIZADO" -> fallbackReservation
                        else -> null
                    }
                    val driverId = reservation?.rides?.drivers?.id
                    val firstName = reservation?.rides?.drivers?.users?.first_name.orEmpty()
                    val lastName = reservation?.rides?.drivers?.users?.last_name.orEmpty()
                    val driverName = listOf(firstName, lastName)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                        .ifBlank { "Driver" }

                    val ratedDriverIds = ratingRepository.getRatedDriversForRide(
                        token = token,
                        rideId = rideId,
                        riderId = rider.id
                    )
                    val driverAlreadyRated = driverId != null && ratedDriverIds.contains(driverId)

                    uiState = uiState.copy(
                        isLoading = false,
                        riderId = rider.id,
                        driverId = if (driverAlreadyRated) null else driverId,
                        ridersToRate = if (driverId == null || driverAlreadyRated) {
                            emptyList()
                        } else {
                            listOf(
                                RateUserUiModel(
                                    driverId = driverId,
                                    name = driverName
                                )
                            )
                        },
                        errorMessage = when {
                            driverId == null -> "No se pudo obtener el conductor de este viaje."
                            driverAlreadyRated -> "Ya calificaste al conductor de este viaje."
                            else -> null
                        }
                    )
                }
            } catch (_: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "No se pudo cargar la información de calificación."
                )
            }
        }
    }

    fun updatePunctuality(value: Int) {
        uiState = uiState.copy(punctuality = value, errorMessage = null, successMessage = null)
    }

    fun updateBehavior(value: Int) {
        uiState = uiState.copy(behavior = value, errorMessage = null, successMessage = null)
    }

    fun updateCommunication(value: Int) {
        uiState = uiState.copy(communication = value, errorMessage = null, successMessage = null)
    }

    fun updateSecurity(value: Int) {
        uiState = uiState.copy(security = value, errorMessage = null, successMessage = null)
    }

    fun updatePaymentPunctuality(value: Int) {
        uiState = uiState.copy(paymentPunctuality = value, errorMessage = null, successMessage = null)
    }

    fun selectRider(riderId: Int) {
        uiState = uiState.copy(
            selectedRiderId = riderId,
            punctuality = 0,
            behavior = 0,
            communication = 0,
            paymentPunctuality = 0,
            errorMessage = null,
            successMessage = null
        )
    }

    fun submitRating() {
        if (uiState.isSubmitting) return

        if (uiState.ratingType == "rider" && uiState.selectedRiderId == null) {
            uiState = uiState.copy(
                errorMessage = "No hay pasajeros pendientes por calificar.",
                successMessage = null
            )
            return
        }

        if (uiState.ratingType == "rider" && uiState.ridersToRate.isEmpty() && uiState.successMessage == "Calificación enviada correctamente.") {
            return
        }

        val token = sessionManager.getToken()
        if (token.isEmpty()) {
            uiState = uiState.copy(errorMessage = "No hay una sesión activa.", successMessage = null)
            return
        }

        if (!isValidScore(uiState.punctuality) ||
            !isValidScore(uiState.behavior) ||
            !isValidScore(uiState.communication)
        ) {
            uiState = uiState.copy(errorMessage = "Califica todos los campos de 1 a 5.", successMessage = null)
            return
        }

        if (uiState.ratingType == "rider" && !isValidScore(uiState.paymentPunctuality)) {
            uiState = uiState.copy(errorMessage = "Califica todos los campos de 1 a 5.", successMessage = null)
            return
        }

        if (uiState.ratingType == "driver" && !isValidScore(uiState.security)) {
            uiState = uiState.copy(errorMessage = "Califica todos los campos de 1 a 5.", successMessage = null)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSubmitting = true, errorMessage = null, successMessage = null)

            val success = if (uiState.ratingType == "rider") {
                val selectedRiderId = uiState.selectedRiderId
                val driverId = uiState.driverId
                val rideId = uiState.rideId

                if (selectedRiderId == null || driverId == null || rideId <= 0) {
                    uiState = uiState.copy(
                        isSubmitting = false,
                        errorMessage = "Selecciona un pasajero antes de enviar.",
                        successMessage = null
                    )
                    return@launch
                }

                val body = RateRiderRequestDto(
                    rider_id = selectedRiderId,
                    driver_id = driverId,
                    punctuality = uiState.punctuality,
                    behavior = uiState.behavior,
                    communication = uiState.communication,
                    payment_punctuality = uiState.paymentPunctuality,
                    ride_id = rideId
                )
                ratingRepository.rateRider(token, body)
            } else {
                val riderId = uiState.riderId
                val driverId = uiState.driverId
                val rideId = uiState.rideId

                if (riderId == null || driverId == null || rideId <= 0) {
                    uiState = uiState.copy(
                        isSubmitting = false,
                        errorMessage = "No se pudieron identificar los usuarios a calificar.",
                        successMessage = null
                    )
                    return@launch
                }

                val body = RateDriverRequestDto(
                    rider_id = riderId,
                    driver_id = driverId,
                    punctuality = uiState.punctuality,
                    behavior = uiState.behavior,
                    communication = uiState.communication,
                    security = uiState.security,
                    ride_id = rideId
                )
                ratingRepository.rateDriver(token, body)
            }

            uiState = if (success) {
                if (uiState.ratingType == "rider") {
                    val ratedRiderId = uiState.selectedRiderId
                    val remainingRiders = uiState.ridersToRate.filterNot { it.riderId == ratedRiderId }
                    val hasMoreRiders = remainingRiders.isNotEmpty()
                    uiState.copy(
                        isSubmitting = false,
                        errorMessage = null,
                        successMessage = if (hasMoreRiders) {
                            "Calificación enviada correctamente. Puedes calificar otro pasajero."
                        } else {
                            "Calificación enviada correctamente."
                        },
                        ridersToRate = remainingRiders,
                        selectedRiderId = remainingRiders.firstOrNull()?.riderId,
                        punctuality = 0,
                        behavior = 0,
                        communication = 0,
                        paymentPunctuality = 0
                    )
                } else {
                    uiState.copy(
                        isSubmitting = false,
                        errorMessage = null,
                        successMessage = "Calificación enviada correctamente."
                    )
                }
            } else {
                uiState.copy(
                    isSubmitting = false,
                    errorMessage = "No se pudo enviar la calificación.",
                    successMessage = null
                )
            }
        }
    }

    fun clearMessages() {
        uiState = uiState.copy(errorMessage = null, successMessage = null)
    }

    private fun isValidScore(value: Int): Boolean {
        return value in 1..5
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
}

