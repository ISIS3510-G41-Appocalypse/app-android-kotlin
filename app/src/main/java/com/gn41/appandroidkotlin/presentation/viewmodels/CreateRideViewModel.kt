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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


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

    var rideTypes = listOf("Hacia la universidad","Desde la universidad")

    var isLoadingData by mutableStateOf(true)
        private set

    var loadErrorMessage by mutableStateOf("")
        private set

    var timeValidationMessage by mutableStateOf("")
        private set

    companion object {
        private const val MAX_SOURCE_LENGTH = 40
        private const val MAX_DESTINATION_LENGTH = 40
        private const val MAX_PRICE_LENGTH = 8
    }

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
        formState = formState.copy(source = value.take(MAX_SOURCE_LENGTH))
    }

    fun onDestinationChanged(value: String) {
        formState = formState.copy(destination = value.take(MAX_DESTINATION_LENGTH))
    }

    fun onPriceChanged(value: String) {
        val filteredValue = value.filter { it.isDigit() || it == '.' }
            .take(MAX_PRICE_LENGTH)

        formState = formState.copy(price = filteredValue)
    }

    fun onDateSelected(date: String) {
        timeValidationMessage = ""
        formState = formState.copy(date = date)
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

                val type: String
                if (formState.type == "Hacia la universidad") {
                    type = "TO_UNIVERSITY"
                }
                else{
                    type = "FROM_UNIVERSITY"
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
                        type = type
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
            formState.source.length > MAX_SOURCE_LENGTH -> "El punto de salida es demasiado largo"
            formState.destination.length > MAX_DESTINATION_LENGTH -> "El destino es demasiado largo"
            formState.price.toDoubleOrNull() == null -> "Ingresa un precio válido"
            formState.price.toDoubleOrNull() != null && formState.price.toDouble() <= 0.0 -> "El precio debe ser mayor a 0"
            isPastDate(formState.date) -> "No puedes seleccionar una fecha pasada"
            isPastDateTime(formState.date, formState.departureTime) -> "No puedes seleccionar una hora pasada para hoy"
            else -> null
        }
    }



    private fun isPastDate(date: String): Boolean {
        return try {
            val parts = date.split("-")
            val y = parts[0].toInt()
            val m = parts[1].toInt()
            val d = parts[2].toInt()

            val today = java.util.Calendar.getInstance()
            val selected = java.util.Calendar.getInstance()
            selected.set(y, m - 1, d, 0, 0, 0)
            selected.set(java.util.Calendar.MILLISECOND, 0)

            today.set(java.util.Calendar.HOUR_OF_DAY, 0)
            today.set(java.util.Calendar.MINUTE, 0)
            today.set(java.util.Calendar.SECOND, 0)
            today.set(java.util.Calendar.MILLISECOND, 0)

            selected.before(today)
        } catch (e: Exception) {
            false
        }
    }

    private fun isPastDateTime(date: String, time: String): Boolean {
        return try {
            val dateParts = date.split("-")
            val timeParts = time.split(":")

            val y = dateParts[0].toInt()
            val m = dateParts[1].toInt()
            val d = dateParts[2].toInt()

            val h = timeParts[0].toInt()
            val min = timeParts[1].toInt()

            val now = java.util.Calendar.getInstance()
            val selected = java.util.Calendar.getInstance()
            selected.set(y, m - 1, d, h, min, 0)
            selected.set(java.util.Calendar.MILLISECOND, 0)

            val todaySameDate =
                now.get(java.util.Calendar.YEAR) == y &&
                        now.get(java.util.Calendar.MONTH) == (m - 1) &&
                        now.get(java.util.Calendar.DAY_OF_MONTH) == d

            todaySameDate && selected.before(now)
        } catch (e: Exception) {
            false
        }
    }

    fun clearTimeValidationMessage() {
        timeValidationMessage = ""
    }

    fun validateAndSetDepartureTime(time: String) {
        if (formState.date.isBlank()) {
            timeValidationMessage = "Selecciona primero una fecha"
            return
        }

        if (isPastDateTime(formState.date, time)) {
            timeValidationMessage = "No puedes seleccionar una hora pasada para hoy"
            return
        }

        timeValidationMessage = ""
        formState = formState.copy(departureTime = time)
    }
}