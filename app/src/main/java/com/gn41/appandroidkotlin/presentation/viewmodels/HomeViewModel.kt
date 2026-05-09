package com.gn41.appandroidkotlin.presentation.viewmodels

import android.util.Log
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.dto.rides.RideDto
import com.gn41.appandroidkotlin.data.dto.zone.ZoneDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.ReservationsRepository
import com.gn41.appandroidkotlin.data.repositories.RidesRepository
import com.gn41.appandroidkotlin.data.repositories.TripRepository
import com.gn41.appandroidkotlin.data.repositories.VehicleRepository
import com.gn41.appandroidkotlin.data.repositories.ZoneRepository
import com.gn41.appandroidkotlin.data.services.performance.Supervisor
import com.gn41.appandroidkotlin.presentation.cache.TripMemoryCache
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.measureTimedValue

data class HomeUiState(
    val isLoading: Boolean = false,
    val rides: List<RideItemUiModel> = emptyList(),
    val errorMessage: String = "",
    val reservationMessage: String = "",
    val selectedZone: String = "Todos",
    val preferredZoneName: String = "Todos",
    val zoneOptions: List<String> = listOf("Todos"),
    val selectedTripType: String = "Todos",
    val selectedDate: String = todayDateString(),
    val selectedDepartureTime: String = "Todas",
    val hasActiveFilters: Boolean = false,
    val activeFilterCount: Int = 0,
    val hasActiveRiderReservation: Boolean = false,
    val hasActiveDriverTrip: Boolean = false,
    val isDriver: Boolean = false,
    // Estado de conectividad — separado de errorMessage
    val isOffline: Boolean = false
)

private fun todayDateString(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}

class HomeViewModel(
    private val ridesRepository: RidesRepository,
    private val sessionManager: SessionManager,
    private val reservationsRepository: ReservationsRepository? = null,
    private val tripRepository: TripRepository? = null,
    private val zoneRepository: ZoneRepository,
    private val vehicleRepository: VehicleRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private var allRides: List<RideDto> = emptyList()
    private var recommendationByRideId: Map<Int, Double> = emptyMap()
    private var lastConnectionState: Boolean? = null

    private var defaultZoneApplied = false
    private var preferredZoneName: String = "Todos"
    private var preferredZoneId: Int? = null

    // IDs resueltos confiablemente desde el token (no desde SessionManager directo)
    private var currentResolvedUserId: Int? = null
    private var currentResolvedAuthId: String? = null
    private var currentResolvedDriverId: Int? = null
    private var currentResolvedZoneId: Int? = null

    var uiState by mutableStateOf(HomeUiState())
        private set

    init {
        observeNetworkChanges()
    }

    fun onTripTypeChange(value: String) {
        uiState = uiState.copy(selectedTripType = value)
        applyFilters()
    }

    fun onZoneChange(value: String) {
        uiState = uiState.copy(selectedZone = value)
        applyFilters()
    }

    fun onDateChange(value: String) {
        uiState = uiState.copy(selectedDate = value)
        applyFilters()
    }

    fun onDepartureTimeChange(value: String) {
        uiState = uiState.copy(selectedDepartureTime = value)
        applyFilters()
    }

    fun onReserveClicked(rideId: Int) {
        val startTime = System.currentTimeMillis()
        // FASE 5: bloquear reserva sin internet
        if (uiState.isOffline) {
            uiState = uiState.copy(reservationMessage = "Necesitas conexión a internet para reservar un viaje.")
            return
        }

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
                val hasActive = hasActivePassengerReservation(
                    riderId = rider.id,
                    token = token,
                    fallbackReservationStates = reservations.map { it.state }
                )

                if (hasActive) {
                    uiState = uiState.copy(
                        reservationMessage = "Ya tienes una reserva activa. Cancela la actual antes de reservar otro viaje."
                    )
                    return@launch
                }

                val (created, time) = measureTimedValue {
                    repository.createReservation(
                        rideId = rideId,
                        riderId = rider.id,
                        meetingPoint = meetingPoint,
                        destinationPoint = destinationPoint,
                        token = token
                    )
                }

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
                Supervisor.addDuration("CreateReservation", time.inWholeMilliseconds.toDouble(), "BACKEND")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Exception creating reservation", e)
                uiState = uiState.copy(reservationMessage = "No se pudo crear la reserva. Intenta de nuevo.")
            }
            val duration = System.currentTimeMillis() - startTime
            Supervisor.addDuration("CreateReservation", duration.toDouble(), "FRONTEND")
        }
    }

    fun clearReservationMessage() {
        uiState = uiState.copy(reservationMessage = "")
    }

    fun refreshHomeData() {
        refreshNetworkState()
    }

    fun refreshNetworkState() {
        handleNetworkState(networkHelper.isInternetAvailable())
    }

    fun clearFilters() {
        uiState = uiState.copy(
            selectedZone = preferredZoneName,
            preferredZoneName = preferredZoneName,
            selectedDate = todayDateString(),
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
        // Current implementation loads offered rides and filters them locally.
        // Backend filtering by date can be added later if the dataset grows.
        val filteredRides = allRides.filter { ride ->
            val isOfferedRide = isRideOffered(ride.state)
            val hasAvailableSeats = isRideWithAvailableSeats(ride)

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
                else -> sameZone(ride.zones?.name.orEmpty(), uiState.selectedZone)
            }

            val matchesTripType = when (uiState.selectedTripType) {
                "Todos" -> true
                "Hacia la universidad" -> ride.type == "TO_UNIVERSITY"
                "Desde la universidad" -> ride.type == "FROM_UNIVERSITY"
                else -> true
            }

            val matchesDate = ride.date == uiState.selectedDate

            val matchesDepartureTime = when (uiState.selectedDepartureTime) {
                "Todas" -> true
                else -> matchesDepartureTimeRange(
                    rideTime = ride.departure_time,
                    selectedTime = uiState.selectedDepartureTime
                )
            }

            isOfferedRide && hasAvailableSeats && isUpcomingRide && isNotOwnRide && matchesZone && matchesTripType && matchesDate && matchesDepartureTime
        }

        val activeFilterCount = countActiveFilters(
            zone = uiState.selectedZone,
            date = uiState.selectedDate,
            tripType = uiState.selectedTripType,
            departureTime = uiState.selectedDepartureTime
        )

        uiState = uiState.copy(
            rides = filteredRides
                .sortedByDescending { it.drivers?.rating ?: 0.0 }
                .map { ride ->
                    mapToRideUiModel(ride).copy(
                        recommendationRating = recommendationByRideId[ride.id]
                    )
                },
            hasActiveFilters = activeFilterCount > 0,
            activeFilterCount = activeFilterCount
        )
    }

    // FASE 4: validar si el conductor puede crear viaje (necesita internet)
    fun onCreateRideRequested(onNavigate: () -> Unit) {
        if (uiState.isOffline) {
            uiState = uiState.copy(reservationMessage = "Necesitas conexión a internet para crear un viaje.")
            return
        }
        onNavigate()
    }

    // FASE 1: observar la red de forma reactiva usando NetworkCallback
    private fun observeNetworkChanges() {
        viewModelScope.launch {
            networkHelper.observeNetworkChanges().collect { hasInternet ->
                Log.d("HomeViewModel", "[NETWORK] isOnline: $hasInternet")
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

        if (uiState.isOffline || lastConnectionState != true) {
            uiState = uiState.copy(
                isOffline = false,
                errorMessage = ""
            )
            lastConnectionState = true
            loadRides()
        } else {
            uiState = uiState.copy(
                isOffline = false,
                errorMessage = ""
            )
        }
    }

    fun logout(onNavigateToLogin: () -> Unit) {
        sessionManager.clearToken()
        sessionManager.clearUserId()
        sessionManager.clearDriverId()
        TripMemoryCache.clear()
        currentResolvedUserId = null
        currentResolvedAuthId = null
        currentResolvedDriverId = null
        currentResolvedZoneId = null
        preferredZoneId = null
        preferredZoneName = "Todos"
        defaultZoneApplied = false
        uiState = uiState.copy(
            selectedZone = "Todos",
            preferredZoneName = "Todos",
            zoneOptions = listOf("Todos")
        )
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
        date: String,
        tripType: String,
        departureTime: String
    ): Int {
        var count = 0
        if (!sameZone(zone, preferredZoneName)) count++
        if (date != todayDateString()) count++
        if (tripType != "Todos") count++
        if (departureTime != "Todas") count++
        return count
    }

    private fun sameZone(a: String, b: String): Boolean {
        return a.trim().equals(b.trim(), ignoreCase = true)
    }

    private fun matchesDepartureTimeRange(rideTime: String?, selectedTime: String): Boolean {
        if (selectedTime == "Todas") return true

        val rideMinutes = parseTimeToMinutes(rideTime) ?: return false
        val selectedMinutes = parseTimeToMinutes(selectedTime) ?: return false
        val endMinutes = selectedMinutes + 60

        return rideMinutes in selectedMinutes..endMinutes
    }

    private fun parseTimeToMinutes(value: String?): Int? {
        val normalized = normalizeRideTime(value ?: return null) ?: return null
        val parts = normalized.split(":")
        if (parts.size < 2) return null

        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null

        return (hour * 60) + minute
    }

    private fun isRideOffered(state: String?): Boolean {
        return state?.trim()?.equals("OFERTADO", ignoreCase = true) == true
    }

    private fun isRideWithAvailableSeats(ride: RideDto): Boolean {
        val totalSeats = ride.vehicles?.number_slots ?: 0
        val activeBookedSeats = ride.reservations.orEmpty().count { reservation ->
            val reservationState = normalizeState(reservation.state)
            reservationState == "ACEPTADA" || reservationState == "EN_CURSO"
        }
        return (totalSeats - activeBookedSeats) > 0
    }

    private fun normalizeState(state: String?): String {
        val rawState = state?.trim()?.uppercase(Locale.getDefault()).orEmpty()
        return when (rawState) {
            "PENDIENTE", "PENDING" -> "PENDIENTE"
            "ACEPTADA", "ACCEPTED" -> "ACEPTADA"
            "EN_CURSO", "IN_PROGRESS" -> "EN_CURSO"
            "OFERTADO", "OFFERED", "ACTIVE" -> "OFERTADO"
            "FINALIZADO", "FINALIZADA", "FINISHED", "COMPLETED" -> "FINALIZADO"
            "CANCELADO", "CANCELADA", "CANCELLED" -> "CANCELADO"
            "RECHAZADA", "REJECTED" -> "RECHAZADA"
            else -> rawState
        }
    }

    private fun isActivePassengerReservation(
        reservationState: String?,
        rideState: String?
    ): Boolean {
        val normalizedReservationState = normalizeState(reservationState)
        val normalizedRideState = normalizeState(rideState)

        val isActiveReservationState = normalizedReservationState in setOf("PENDIENTE", "ACEPTADA", "EN_CURSO")
        val isRideFinishedOrCancelled = normalizedRideState in setOf("FINALIZADO", "CANCELADO")

        return isActiveReservationState && !isRideFinishedOrCancelled
    }

    private suspend fun hasActivePassengerReservation(
        riderId: Int,
        token: String,
        fallbackReservationStates: List<String> = emptyList()
    ): Boolean {
        val tripRepo = tripRepository
        if (tripRepo != null) {
            val reservations = tripRepo.getActiveRiderReservation(riderId, token)
            return reservations.any { reservation ->
                isActivePassengerReservation(
                    reservationState = reservation.state,
                    rideState = reservation.rides?.state
                )
            }
        }

        // Fallback solo por compatibilidad: si no tenemos rideState,
        // evitamos bloquear por estados historicos (ej. ACEPTADA/PENDIENTE vieja).
        // Solo se bloquea cuando el estado es claramente activo y reciente.
        return fallbackReservationStates.any { state ->
            isFallbackActivePassengerReservation(state)
        }
    }

    private fun isFallbackActivePassengerReservation(reservationState: String?): Boolean {
        return normalizeState(reservationState) == "EN_CURSO"
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
            if (currentResolvedAuthId != null && currentResolvedAuthId != authId) {
                // Evita arrastrar zona preferida del usuario anterior.
                defaultZoneApplied = false
                preferredZoneId = null
                preferredZoneName = "Todos"
                currentResolvedUserId = null
                currentResolvedDriverId = null
                currentResolvedZoneId = null
            }
            currentResolvedAuthId = authId

            val resRepo = reservationsRepository ?: return

            val user = resRepo.getUserByAuthId(authId, token)
            if (user != null) {
                currentResolvedUserId = user.id
                currentResolvedZoneId = user.zone_id
                preferredZoneId = user.zone_id
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
                currentResolvedZoneId = null
                preferredZoneId = null
                preferredZoneName = "Todos"
                defaultZoneApplied = false
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "[RESOLVE] Exception resolving current user", e)
            currentResolvedUserId = null
            currentResolvedDriverId = null
            currentResolvedZoneId = null
            preferredZoneId = null
            preferredZoneName = "Todos"
            defaultZoneApplied = false
        }
    }

    private fun loadZoneOptions(
        allZones: List<ZoneDto>,
        offeredRides: List<RideDto>
    ): List<String> {
        val zoneNamesFromTable = allZones
            .mapNotNull { it.name.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()

        if (zoneNamesFromTable.isNotEmpty()) {
            return listOf("Todos") + zoneNamesFromTable
        }

        // Fallback minimo si falla tabla zones.
        return buildZoneOptions(offeredRides)
    }

    private fun resolvePreferredZoneNameFromZones(allZones: List<ZoneDto>, zoneId: Int?): String {
        if (zoneId == null) return "Todos"

        val zoneName = allZones
            .firstOrNull { it.id == zoneId }
            ?.name
            ?.trim()

        return if (zoneName.isNullOrEmpty()) "Todos" else zoneName
    }

    private fun applyOfflineState() {
        allRides = emptyList()
        recommendationByRideId = emptyMap()
        val driverKnownLocally = uiState.isDriver || sessionManager.getDriverId() > 0
        uiState = uiState.copy(
            isOffline = true,
            isLoading = false,
            rides = emptyList(),
            errorMessage = "",
            isDriver = driverKnownLocally
        )
    }

    private fun loadRides() {
        if (!networkHelper.isInternetAvailable()) {
            applyOfflineState()
            return
        }

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

                val allZones = try {
                    zoneRepository.getZones()
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Exception loading zones", e)
                    emptyList()
                }

                val result = ridesRepository.getRides(token)

                if (result != null) {
                    val offeredRides = result.filter { ride -> isRideOffered(ride.state) }
                    Log.d("HomeViewModel", "Rides loaded: ${result.size}")
                    Log.d("HomeViewModel", "[FILTRO] OFERTADO rides: ${offeredRides.size}")
                    Log.d("HomeViewModel", "[FILTRO] Resolved userId: $currentResolvedUserId, driverId: $currentResolvedDriverId")
                    allRides = offeredRides
                    recommendationByRideId = loadRecommendationsForRides(
                        rides = offeredRides,
                        token = token
                    )
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "",
                        zoneOptions = loadZoneOptions(
                            allZones = allZones,
                            offeredRides = offeredRides
                        )
                    )

                    if (!defaultZoneApplied) {
                        preferredZoneName = resolvePreferredZoneNameFromZones(
                            allZones = allZones,
                            zoneId = preferredZoneId
                        )
                        uiState = uiState.copy(
                            selectedZone = preferredZoneName,
                            preferredZoneName = preferredZoneName
                        )
                        defaultZoneApplied = true
                    } else {
                        uiState = uiState.copy(preferredZoneName = preferredZoneName)
                    }

                    applyFilters()
                    checkBlockingStates()
                } else {
                    recommendationByRideId = emptyMap()
                    Log.e("HomeViewModel", "Rides result is null")
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "Para reservar o publicar un viaje necesitas conexión a internet, revisa tu conexión. Si ya tienes una reserva activa, puedes verla en la pestaña Viajes."
                    )
                }
            } catch (e: Exception) {
                recommendationByRideId = emptyMap()
                Log.e("HomeViewModel", "Exception loading rides", e)
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Para reservar o publicar un viaje necesitas conexión a internet, revisa tu conexión. Si ya tienes una reserva activa, puedes verla en la pestaña Viajes."
                )
            }
        }
    }

    private suspend fun loadRecommendationsForRides(
        rides: List<RideDto>,
        token: String
    ): Map<Int, Double> {
        val repository = reservationsRepository ?: return emptyMap()
        val currentUserId = currentResolvedUserId ?: return emptyMap()
        val rider = repository.getRiderByUserId(currentUserId, token) ?: return emptyMap()

        val recommendationMap = mutableMapOf<Int, Double>()

        rides.forEach { ride ->
            val rating = try {
                ridesRepository.getRiderDriverRecommendation(
                    riderId = rider.id,
                    driverId = ride.driver_id,
                    token = token
                )
            } catch (_: Exception) {
                null
            }

            if (rating != null) {
                recommendationMap[ride.id] = rating.coerceIn(0.0, 5.0)
            }
        }

        return recommendationMap
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
                    hasActivePassengerReservation(
                        riderId = rider.id,
                        token = token,
                        fallbackReservationStates = reservations.map { it.state }
                    )
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