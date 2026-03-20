package com.gn41.appandroidkotlin.presentation.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gn41.appandroidkotlin.data.dto.rides.RideDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.RidesRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val isLoading: Boolean = false,
    val rides: List<RideItemUiModel> = emptyList(),
    val errorMessage: String = "",
    val selectedZone: String = "Todos",
    val zoneOptions: List<String> = listOf("Todos"),
    val selectedTripType: String = "Todos",
    val selectedDay: String = "Todos",
    val selectedDepartureTime: String = "Todas",
    val departureTimeOptions: List<String> = buildDepartureTimeOptions(),
    val hasActiveFilters: Boolean = false,
    val activeFilterCount: Int = 0
)

private fun buildDepartureTimeOptions(): List<String> {
    val slots = (5..21).map { hour ->
        String.format(Locale.getDefault(), "%02d:00", hour)
    }
    return listOf("Todas") + slots
}

class HomeViewModel(
    private val ridesRepository: RidesRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private var allRides: List<RideDto> = emptyList()

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

    fun clearFilters() {
        uiState = uiState.copy(
            selectedZone = "Todos",
            selectedDay = "Todos",
            selectedTripType = "Todos",
            selectedDepartureTime = "Todas"
        )
        applyFilters()
    }

    fun applyFilters() {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = formatter.format(Date())

        val filteredRides = allRides.filter { ride ->
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
                "Todos" -> true
                "Hoy" -> ride.date == today
                else -> true
            }

            val matchesDepartureTime = when (uiState.selectedDepartureTime) {
                "Todas" -> true
                else -> matchesHourSlot(
                    rideTime = ride.departure_time,
                    selectedSlot = uiState.selectedDepartureTime
                )
            }

            matchesZone && matchesTripType && matchesDay && matchesDepartureTime
        }

        val activeFilterCount = countActiveFilters(
            zone = uiState.selectedZone,
            day = uiState.selectedDay,
            tripType = uiState.selectedTripType,
            departureTime = uiState.selectedDepartureTime
        )

        uiState = uiState.copy(
            rides = filteredRides.map { mapToRideUiModel(it) },
            hasActiveFilters = activeFilterCount > 0,
            activeFilterCount = activeFilterCount
        )
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
        if (day != "Todos") count++
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

    private fun loadRides() {
        val token = sessionManager.getToken()

        if (token.isEmpty()) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "No hay una sesion activa. Inicia sesion nuevamente."
            )
            return
        }

        uiState = uiState.copy(
            isLoading = true,
            errorMessage = ""
        )

        viewModelScope.launch {
            try {
                val result = ridesRepository.getRides(token)

                if (result != null) {
                    Log.d("HomeViewModel", "Rides loaded: ${result.size}")
                    allRides = result
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "",
                        zoneOptions = buildZoneOptions(result)
                    )
                    applyFilters()
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
}