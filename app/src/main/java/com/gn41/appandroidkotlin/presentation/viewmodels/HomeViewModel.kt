package com.gn41.appandroidkotlin.presentation.viewmodels

import android.util.Log
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gn41.appandroidkotlin.data.dto.rides.RideDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.ReservationsRepository
import com.gn41.appandroidkotlin.data.repositories.RidesRepository
import com.gn41.appandroidkotlin.data.repositories.TripRepository
import com.gn41.appandroidkotlin.data.repositories.VehicleRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val isLoading: Boolean = false,
    val rides: List<RideItemUiModel> = emptyList(),
    val errorMessage: String = "",
    val reservationMessage: String = "",
    val selectedZone: String = "Todos",
    val zoneOptions: List<String> = listOf("Todos"),
    val selectedTripType: String = "Todos",
    val selectedDay: String = "Hoy",
    val selectedDepartureTime: String = "Todas",
    val departureTimeOptions: List<String> = buildDepartureTimeOptions(),
    val hasActiveFilters: Boolean = false,
    val activeFilterCount: Int = 0,
    val hasActiveRiderReservation: Boolean = false,
    val hasActiveDriverTrip: Boolean = false,
    val isDriver: Boolean = false
)

private fun buildDepartureTimeOptions(): List<String> {
    val slots = (5..21).map { hour ->
        String.format(Locale.getDefault(), "%02d:00", hour)
    }
    return listOf("Todas") + slots
}

class HomeViewModel(
    private val ridesRepository: RidesRepository,
    private val sessionManager: SessionManager,
    private val reservationsRepository: ReservationsRepository? = null,
    private val tripRepository: TripRepository? = null,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private var allRides: List<RideDto> = emptyList()
    
    // IDs resueltos confiably desde el token (no desde SessionManager directo)
    private var currentResolvedUserId: Int? = null
    private var currentResolvedDriverId: Int? = null

    var uiState by mutableStateOf(HomeUiState())
        private set

    init {
        loadRides()
    }

    fun onTripTypeChange(value: String) {
        uiState = uiState.copy(selectedTripType = value)
        applyFilters()
    }

    fun onZoneChange(value: String) {
        uiState = uiState.copy(selectedZone = value)
        applyFilters()
    }

    fun onDayChange(value: String) {
        uiState = uiState.copy(selectedDay = value)
        applyFilters()
    }

    fun onDepartureTimeChange(value: String) {
        uiState = uiState.copy(selectedDepartureTime = value)
        applyFilters()
    }

    fun onReserveClicked(rideId: Int) {
        val token = sessionManager.getToken()

        if (token.isEmpty()) {
            uiState = uiState.copy(reservationMessage = "No hay una sesion activa. Inicia sesion nuevamente.")
            return
        }

        val repository = reservationsRepository
        if (repository == null) {
            uiState = uiState.copy(reservationMessage = "Reservas no disponibles por ahora.")
            return
        }

        val authId = extractAuthIdFromToken(token)
        if (authId.isNullOrEmpty()) {
            uiState = uiState.copy(reservationMessage = "No se pudo validar el usuario.")
            return
        }

        val ride = allRides.find { it.id == rideId }
        val meetingPoint = ride?.source ?: ""
        val destinationPoint = ride?.destination ?: ""

        viewModelScope.launch {
            try {
                val user = repository.getUserByAuthId(authId, token)
                if (user == null) {
                    uiState = uiState.copy(reservationMessage = "No se pudo encontrar el usuario.")
                    return@launch
                }

                val rider = repository.getRiderByUserId(user.id, token)
                if (rider == null) {
                    uiState = uiState.copy(reservationMessage = "No se pudo encontrar el rider.")
                    return@launch
                }

                val reservations = repository.getReservations(rider.id, token).orEmpty()
                val hasActive = reservations.any {
                    it.state == "PENDIENTE" ||
                        it.state == "ACEPTADA" ||
                        it.state == "EN_CURSO"
                }

                if (hasActive) {
                    uiState = uiState.copy(
                        reservationMessage = "Ya tienes una reserva activa. Cancela la actual antes de reservar otro viaje."
                    )
                    return@launch
                }

                val created = repository.createReservation(
                    rideId = rideId,
                    riderId = rider.id,
                    meetingPoint = meetingPoint,
                    destinationPoint = destinationPoint,
                    token = token
                )

                uiState = if (created) {
                    uiState.copy(
                        reservationMessage = "Reserva creada correctamente.",
                        hasActiveRiderReservation = true
                    )
                } else {
                    uiState.copy(reservationMessage = "No se pudo crear la reserva. Intenta de nuevo.")
                }

                if (created) {
                    checkBlockingStates()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Exception creating reservation", e)
                uiState = uiState.copy(reservationMessage = "No se pudo crear la reserva. Intenta de nuevo.")
            }
        }
    }

    fun clearReservationMessage() {
        uiState = uiState.copy(reservationMessage = "")
    }

    fun refreshHomeData() {
        loadRides()
    }

    fun clearFilters() {
        uiState = uiState.copy(
            selectedZone = "Todos",
            selectedDay = "Hoy",
            selectedTripType = "Todos",
            selectedDepartureTime = "Todas"
        )
        applyFilters()
    }

    fun applyFilters() {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            isLenient = false
        }
        val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
            isLenient = false
        }
        val now = Date()
        val today = dateFormatter.format(now)

        val filteredRides = allRides.filter { ride ->
            val isUpcomingRide = isRideUpcoming(
                ride = ride,
                now = now,
                dateFormatter = dateFormatter,
                dateTimeFormatter = dateTimeFormatter
            )

            val isNotOwnRide = !isRideCreatedByCurrentUser(
                ride = ride,
                currentUserId = currentResolvedUserId,
                currentDriverId = currentResolvedDriverId
            )

            val matchesZone = when (uiState.selectedZone) {
                "Todos" -> true
                else -> ride.zones?.name == uiState.selectedZone
            }

            val matchesTripType = when (uiState.selectedTripType) {
                "Todos" -> true
                "Hacia la universidad" -> ride.type == "TO_UNIVERSITY"
                "Desde la universidad" -> ride.type == "FROM_UNIVERSITY"
                else -> true
            }

            val matchesDay = when (uiState.selectedDay) {
                "Hoy" -> ride.date == today
                "Próximos viajes" -> {
                    val rideDate = dateFormatter.parse(ride.date)
                    val todayDate = dateFormatter.parse(today)
                    rideDate != null && todayDate != null && rideDate.after(todayDate)
                }
                else -> true
            }

            val matchesDepartureTime = when (uiState.selectedDepartureTime) {
                "Todas" -> true
                else -> matchesHourSlot(
                    rideTime = ride.departure_time,
                    selectedSlot = uiState.selectedDepartureTime
                )
            }

            isUpcomingRide && isNotOwnRide && matchesZone && matchesTripType && matchesDay && matchesDepartureTime
        }

        val activeFilterCount = countActiveFilters(
            zone = uiState.selectedZone,
            day = uiState.selectedDay,
            tripType = uiState.selectedTripType,
            departureTime = uiState.selectedDepartureTime
        )

        uiState = uiState.copy(
            rides = filteredRides
                .sortedByDescending { it.drivers?.rating ?: 0.0 }
                .map { mapToRideUiModel(it) },
            hasActiveFilters = activeFilterCount > 0,
            activeFilterCount = activeFilterCount
        )
    }

    fun logout(onNavigateToLogin: () -> Unit) {
        sessionManager.clearToken()
        sessionManager.clearUserId()
        sessionManager.clearDriverId()
        currentResolvedUserId = null
        currentResolvedDriverId = null
        onNavigateToLogin()
    }

    private fun buildZoneOptions(rides: List<RideDto>): List<String> {
        // obtiene zonas unicas y las ordena
        val zones = rides
            .mapNotNull { it.zones?.name?.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()

        return listOf("Todos") + zones
    }

    private fun countActiveFilters(
        zone: String,
        day: String,
        tripType: String,
        departureTime: String
    ): Int {
        var count = 0
        if (zone != "Todos") count++
        if (day != "Hoy") count++
        if (tripType != "Todos") count++
        if (departureTime != "Todas") count++
        return count
    }

    private fun matchesHourSlot(rideTime: String, selectedSlot: String): Boolean {
        // valida que la hora coincida
        val rideHour = rideTime.split(":").firstOrNull()?.toIntOrNull() ?: return false
        val slotHour = selectedSlot.split(":").firstOrNull()?.toIntOrNull() ?: return false
        return rideHour == slotHour
    }

    private fun isRideUpcoming(
        ride: RideDto,
        now: Date,
        dateFormatter: SimpleDateFormat,
        dateTimeFormatter: SimpleDateFormat
    ): Boolean {
        val rideDate = dateFormatter.parse(ride.date) ?: return false
        val todayDate = dateFormatter.parse(dateFormatter.format(now)) ?: return false

        if (rideDate.before(todayDate)) return false
        if (rideDate.after(todayDate)) return true

        val normalizedTime = normalizeRideTime(ride.departure_time) ?: return false
        val rideDateTime = dateTimeFormatter.parse("${ride.date} $normalizedTime") ?: return false
        return !rideDateTime.before(now)
    }

    private fun isRideCreatedByCurrentUser(
        ride: RideDto,
        currentUserId: Int?,
        currentDriverId: Int?
    ): Boolean {
        if (currentDriverId != null && ride.driver_id == currentDriverId) return true
        if (currentUserId != null && ride.drivers?.user_id == currentUserId) return true
        return false
    }

    private fun normalizeRideTime(rideTime: String): String? {
        val parts = rideTime.split(":")
        if (parts.size < 2) return null

        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null

        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
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

    private suspend fun resolveCurrentUserFromToken(token: String) {
        try {
            val authId = extractAuthIdFromToken(token) ?: return
            val resRepo = reservationsRepository ?: return

            val user = resRepo.getUserByAuthId(authId, token)
            if (user != null) {
                currentResolvedUserId = user.id
                Log.d("HomeViewModel", "[RESOLVE] currentUserId set to: ${user.id}")

                // Resolver driver si existe
                val driver = tripRepository?.getDriverByUserId(user.id, token)
                if (driver != null) {
                    currentResolvedDriverId = driver.id
                    Log.d("HomeViewModel", "[RESOLVE] currentDriverId set to: ${driver.id}")
                } else {
                    currentResolvedDriverId = null
                    Log.d("HomeViewModel", "[RESOLVE] No driver found for this user")
                }
            } else {
                Log.w("HomeViewModel", "[RESOLVE] Could not resolve user from token")
                currentResolvedUserId = null
                currentResolvedDriverId = null
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "[RESOLVE] Exception resolving current user", e)
            currentResolvedUserId = null
            currentResolvedDriverId = null
        }
    }

    private fun loadRides() {
        val token = sessionManager.getToken()

        if (token.isEmpty()) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "No hay una sesion activa. Inicia sesion nuevamente."
            )
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = "")

        viewModelScope.launch {
            try {
                val userVehicles = vehicleRepository.getUserVehicles()
                val isDriver = userVehicles.isNotEmpty()

                uiState = uiState.copy(isDriver = isDriver)

                // Resolver usuario actual confiably desde el token
                resolveCurrentUserFromToken(token)

                val result = ridesRepository.getRides(token)

                if (result != null) {
                    Log.d("HomeViewModel", "Rides loaded: ${result.size}")
                    Log.d("HomeViewModel", "[FILTRO] Resolved userId: $currentResolvedUserId, driverId: $currentResolvedDriverId")
                    allRides = result
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "",
                        zoneOptions = buildZoneOptions(result)
                    )
                    applyFilters()
                    checkBlockingStates()
                } else {
                    Log.e("HomeViewModel", "Rides result is null")
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "No se pudieron cargar los viajes. Intenta de nuevo."
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Exception loading rides", e)
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "No se pudieron cargar los viajes. Intenta de nuevo."
                )
            }
        }
    }

    // verifica si el usuario ya tiene reserva activa o viaje activo como conductor
    private fun checkBlockingStates() {
        val token = sessionManager.getToken()
        if (token.isEmpty()) return

        val authId = extractAuthIdFromToken(token) ?: return

        val resRepo = reservationsRepository ?: return

        viewModelScope.launch {
            try {
                val user = resRepo.getUserByAuthId(authId, token) ?: return@launch

                // se revisa reserva activa como pasajero
                val rider = resRepo.getRiderByUserId(user.id, token)
                val hasActiveRider = if (rider != null) {
                    val reservations = resRepo.getReservations(rider.id, token).orEmpty()
                    reservations.any { it.state == "PENDIENTE" || it.state == "ACEPTADA" || it.state == "EN_CURSO" }
                } else {
                    false
                }

                // se revisa viaje activo como conductor
                val hasActiveDriver = if (tripRepository != null) {
                    val driver = tripRepository.getDriverByUserId(user.id, token)
                    if (driver != null) {
                        val activeRide = tripRepository.getActiveDriverRide(driver.id, token)
                        activeRide != null
                    } else {
                        false
                    }
                } else {
                    false
                }

                uiState = uiState.copy(
                    hasActiveRiderReservation = hasActiveRider,
                    hasActiveDriverTrip = hasActiveDriver
                )
            } catch (e: Exception) {
                Log.e("HomeViewModel", "checkBlockingStates exception", e)
            }
        }
    }
}