package com.gn41.appandroidkotlin.presentation.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gn41.appandroidkotlin.data.dto.createRide.CreateRideRequestDto
import com.gn41.appandroidkotlin.data.repositories.RideRepository
import kotlinx.coroutines.launch

data class CreateRideFormState(
    val vehicleId: String = "",
    val zoneId: String = "",
    val source: String = "",
    val destination: String = "",
    val price: String = "",
    val date: String = "",
    val departureTime: String = "",
    val type: String = ""
)

sealed class CreateRideUiState {
    object Idle : CreateRideUiState()
    object Loading : CreateRideUiState()
    object Success : CreateRideUiState()
    data class Error(val message: String) : CreateRideUiState()
}

class CreateRideViewModel(private val rideRepository: RideRepository) : ViewModel() {

    var formState by mutableStateOf(CreateRideFormState())
        private set

    var uiState by mutableStateOf<CreateRideUiState>(CreateRideUiState.Idle)
        private set

    fun onVehicleSelected(vehicleId: String) {
        formState = formState.copy(vehicleId = vehicleId)
    }

    fun onZoneSelected(zoneId: String) {
        formState = formState.copy(zoneId = zoneId)
    }

    fun onSourceChanged(value: String) {
        formState = formState.copy(source = value)
    }

    fun onDestinationChanged(value: String) {
        formState = formState.copy(destination = value)
    }

    fun onPriceChanged(value: String) {
        formState = formState.copy(price = value)
    }

    fun onDateSelected(value: String) {
        formState = formState.copy(date = value)
    }

    fun onDepartureTimeSelected(value: String) {
        formState = formState.copy(departureTime = value)
    }

    fun onTypeSelected(value: String) {
        formState = formState.copy(type = value)
    }

    fun createRide() {
        viewModelScope.launch {
            val validationError = validateForm()

            if (validationError != null) {
                uiState = CreateRideUiState.Error(validationError)
                return@launch
            }

            uiState = CreateRideUiState.Loading

            val result = rideRepository.createRide(
                CreateRideRequestDto(
                    vehicleId = formState.vehicleId.toInt(),
                    zoneId = formState.zoneId.toInt(),
                    source = formState.source,
                    destination = formState.destination,
                    price = formState.price.toDouble(),
                    date = formState.date,
                    departureTime = formState.departureTime,
                    type = formState.type,
                    driverId = 0,
                    state = ""
                )
            )

            uiState = result.fold(
                onSuccess = { CreateRideUiState.Success },
                onFailure = { CreateRideUiState.Error(it.message ?: "Error desconocido") }
            )
        }
    }

    private fun validateForm(): String? {
        return when {
            formState.vehicleId.isBlank() -> "Selecciona un vehículo"
            formState.zoneId.isBlank() -> "Selecciona una zona"
            formState.source.isBlank() -> "Ingresa punto de salida"
            formState.destination.isBlank() -> "Ingresa destino"
            formState.price.isBlank() -> "Ingresa precio"
            formState.date.isBlank() -> "Selecciona fecha"
            formState.departureTime.isBlank() -> "Selecciona una hora"
            formState.type.isBlank() -> "Selecciona un tipo"
            else -> null
        }
    }
}