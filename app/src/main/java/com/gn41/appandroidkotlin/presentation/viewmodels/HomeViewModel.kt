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

data class HomeUiState(
    val isLoading: Boolean = false,
    val rides: List<RideDto> = emptyList(),
    val errorMessage: String = ""
)

class HomeViewModel(
    private val ridesRepository: RidesRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var uiState by mutableStateOf(HomeUiState())
        private set

    init {
        loadRides()
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
                    uiState.copy(
                        isLoading = false,
                        rides = result,
                        errorMessage = ""
                    )
                } else {
                    Log.e("HomeViewModel", "Rides result is null")
                    uiState.copy(
                        isLoading = false,
                        errorMessage = "Could not load rides. Try again."
                    )
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