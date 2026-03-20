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
    val rides: List<RideDto> = emptyList(),
    val errorMessage: String = "",
    val selectedTripType: String = "All",
    val selectedDay: String = "All",
    val selectedDepartureTime: String = "All",
    val departureTimeOptions: List<String> = buildDepartureTimeOptions()
)

private fun buildDepartureTimeOptions(): List<String> {
    val slots = (5..21).map { hour -> String.format("%02d:00", hour) }
    return listOf("All") + slots
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

    fun onDayChange(value: String) {
        uiState = uiState.copy(selectedDay = value)
        applyFilters()
    }

    fun onDepartureTimeChange(value: String) {
        uiState = uiState.copy(selectedDepartureTime = value)
        applyFilters()
    }

    fun applyFilters() {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = formatter.format(Date())

        val filtered = allRides.filter { ride ->
            val matchesTripType = when (uiState.selectedTripType) {
                "All" -> true
                "To university" -> ride.type == "TO_UNIVERSITY"
                "From university" -> ride.type == "FROM_UNIVERSITY"
                else -> true
            }

            val matchesDay = when (uiState.selectedDay) {
                "All" -> true
                "Today" -> ride.date == today
                else -> true
            }

            val matchesDepartureTime = when (uiState.selectedDepartureTime) {
                "All" -> true
                else -> matchesHourSlot(
                    rideTime = ride.departure_time,
                    selectedSlot = uiState.selectedDepartureTime
                )
            }

            matchesTripType && matchesDay && matchesDepartureTime
        }

        uiState = uiState.copy(rides = filtered)
    }

    private fun matchesHourSlot(rideTime: String, selectedSlot: String): Boolean {
        val rideHour = rideTime.split(":").firstOrNull()?.toIntOrNull() ?: return false
        val slotHour = selectedSlot.split(":").firstOrNull()?.toIntOrNull() ?: return false

        // Example: selecting 15:00 matches rides from 15:00 to 15:59.
        return rideHour == slotHour
    }

    private fun loadRides() {
        val token = sessionManager.getToken()

        if (token.isEmpty()) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "No active session. Please log in again."
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

                uiState = if (result != null) {
                    Log.d("HomeViewModel", "Rides loaded: ${result.size}")
                    allRides = result

                    uiState.copy(
                        isLoading = false,
                        rides = allRides,
                        errorMessage = ""
                    )
                } else {
                    Log.e("HomeViewModel", "Rides result is null")
                    uiState.copy(
                        isLoading = false,
                        errorMessage = "Could not load rides. Try again."
                    )
                }

                if (result != null) {
                    applyFilters()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Exception loading rides", e)
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Could not load rides. Try again."
                )
            }
        }
    }
}