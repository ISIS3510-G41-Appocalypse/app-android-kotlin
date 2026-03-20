package com.gn41.appandroidkotlin.presentation.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gn41.appandroidkotlin.data.dto.createRide.CreateRideRequestDto
import com.gn41.appandroidkotlin.data.dto.vehicle.VehicleDto
import com.gn41.appandroidkotlin.data.dto.zone.ZoneDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.RideRepository
import com.gn41.appandroidkotlin.data.repositories.VehicleRepository
import com.gn41.appandroidkotlin.data.repositories.ZoneRepository
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

class CreateRideViewModel(private val rideRepository: RideRepository,
    private val vehicleRepository: VehicleRepository,
    private val zoneRepository: ZoneRepository) : ViewModel() {

    sealed class CreateRideUiState {
        object Idle : CreateRideUiState()
        object Loading : CreateRideUiState()
        object Success : CreateRideUiState()
        data class Error(val message: String) : CreateRideUiState()
    }

    var formState by mutableStateOf(CreateRideFormState())
        private set

    var uiState by mutableStateOf<CreateRideUiState>(CreateRideUiState.Idle)
        private set

    var vehicles by mutableStateOf<List<VehicleDto>>(emptyList())
        private set

    var zones by mutableStateOf<List<ZoneDto>>(emptyList())
        private set

    var rideTypes = listOf("TO_UNIVERSITY","FROM_UNIVERSITY")

    var isLoadingData by mutableStateOf(true)
        private set

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            isLoadingData = true

            val vehiclesResult = vehicleRepository.getUserVehicles()
            val zonesResult = zoneRepository.getZones()

            vehicles = vehiclesResult
            zones = zonesResult

            isLoadingData = false
        }
    }

    fun onVehicleSelected(vehicleLicensePlate: String) {
        val vehicleId = vehicleRepository.getVehicleByLicensePlate(vehicleLicensePlate).id
        formState = formState.copy(vehicleId = vehicleId.toString())
    }

    fun onZoneSelected(zoneName: String) {
        val zoneId = zoneRepository.getZoneByName(zoneName).id
        formState = formState.copy(zoneId = zoneId.toString())
    }

    fun onTypeSelected(type: String) {
        formState = formState.copy(type = type)
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

    fun onDateSelected(date: String) {
        formState = formState.copy(date = date)
    }

    fun onDepartureTimeSelected(time: String) {
        formState = formState.copy(departureTime = time)
    }

    fun createRide() {
        viewModelScope.launch {

            val error = validateForm()
            if (error != null) {
                uiState = CreateRideUiState.Error(error)
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
                    departureTime = formState.departureTime,
                    date = formState.date,
                    driverId = 0,
                    state = "",
                    type = formState.type
                )
            )

            uiState = result.fold(
                onSuccess = { CreateRideUiState.Success },
                onFailure = {
                    CreateRideUiState.Error(
                        it.message ?: "Error al crear el viaje"
                    )
                }
            )
        }
    }

    private fun validateForm(): String? {
        return when {
            formState.vehicleId.isBlank() -> "Selecciona un vehículo"
            formState.zoneId.isBlank() -> "Selecciona una zona"
            formState.type.isBlank() -> "Selecciona un tipo"
            formState.source.isBlank() -> "Ingresa punto de salida"
            formState.destination.isBlank() -> "Ingresa destino"
            formState.price.isBlank() -> "Ingresa precio"
            formState.date.isBlank() -> "Selecciona fecha"
            formState.departureTime.isBlank() -> "Selecciona hora"
            else -> null
        }
    }
}