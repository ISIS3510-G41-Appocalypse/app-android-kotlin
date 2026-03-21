package com.gn41.appandroidkotlin.presentation.viewmodels

import android.util.Log
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

class CreateRideViewModel(
    private val rideRepository: RideRepository,
    private val vehicleRepository: VehicleRepository,
    private val zoneRepository: ZoneRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

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

    var loadErrorMessage by mutableStateOf("")
        private set

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                isLoadingData = true
                loadErrorMessage = ""

                Log.d("CreateRide", "Llamando a getUserVehicles")
                val vehiclesResult = vehicleRepository.getUserVehicles()
                val zonesResult = zoneRepository.getZones()

                vehicles = vehiclesResult
                zones = zonesResult
            } catch (e: Exception) {
                Log.e("CreateRide", "Error loading initial data", e)
                loadErrorMessage = "No se pudieron cargar vehiculos o zonas."
                vehicles = emptyList()
                zones = emptyList()
            } finally {
                isLoadingData = false
            }
        }
    }

    fun onVehicleSelected(vehicleLicensePlate: String) {
        formState = formState.copy(vehicleId = vehicleLicensePlate)
    }

    fun onZoneSelected(zoneName: String) {
        formState = formState.copy(zoneId = zoneName)
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
            try {
                val error = validateForm()
                if (error != null) {
                    uiState = CreateRideUiState.Error(error)
                    return@launch
                }

                uiState = CreateRideUiState.Loading

                val driverId = sessionManager.getDriverId()
                if (driverId <= 0) {
                    uiState = CreateRideUiState.Error("No se pudo obtener tu identificación como conductor.")
                    return@launch
                }

                val result = rideRepository.createRide(
                    CreateRideRequestDto(
                        vehicleId = vehicleRepository.getVehicleByLicensePlate(formState.vehicleId).id,
                        zoneId = zoneRepository.getZoneByName(formState.zoneId).id,
                        source = formState.source,
                        destination = formState.destination,
                        price = formState.price.toDouble(),
                        departureTime = formState.departureTime,
                        date = formState.date,
                        driverId = driverId,
                        state = "OFERTADO",
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
            } catch (e: Exception) {
                Log.e("CreateRide", "Error creating ride", e)
                uiState = CreateRideUiState.Error("No se pudo crear el viaje. Revisa tus datos.")
            }
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