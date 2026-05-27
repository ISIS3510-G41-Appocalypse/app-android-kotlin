package com.gn41.appandroidkotlin.presentation.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.local.RegisterDraftManager
import com.gn41.appandroidkotlin.data.repositories.AuthRepository
import com.gn41.appandroidkotlin.data.repositories.ZoneRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.gn41.appandroidkotlin.data.dto.auth.CreateCompleteUserRequestDto
import com.gn41.appandroidkotlin.data.dto.auth.VehicleRequestDto

class RegisterViewModel(
    private val registerDraftManager: RegisterDraftManager,
    private val networkHelper: NetworkHelper,
    private val authRepository: AuthRepository,
    private val zoneRepository: ZoneRepository
) : ViewModel() {

    companion object {
        private const val MAX_FIRST_NAME_LENGTH = 50
        private const val MAX_LAST_NAME_LENGTH = 50
        private const val MAX_EMAIL_LENGTH = 50
        private const val MAX_PASSWORD_LENGTH = 30
        private const val MAX_LICENSE_PLATE_LENGTH = 10
        private const val MAX_BRAND_MODEL_COLOR_LENGTH = 30
        private const val REGISTRATION_DELAY_MS = 2000L // Simulate network delay
    }

    // Step 1: Basic Registration
    var firstName by mutableStateOf("")
        private set
    var lastName by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set

    // Step 1 Errors
    var firstNameInputError by mutableStateOf("")
        private set
    var lastNameInputError by mutableStateOf("")
        private set
    var emailInputError by mutableStateOf("")
        private set
    var passwordInputError by mutableStateOf("")
        private set
    var confirmPasswordInputError by mutableStateOf("")
        private set

    // Step 2: Role Selection
    var selectedRole by mutableStateOf("") // "rider", "driver", "both"
        private set
    var roleSelectionError by mutableStateOf("")
        private set

    // Step 3: Preferred Zone Selection
    var selectedZoneName by mutableStateOf("")
        private set
    var selectedZoneId by mutableIntStateOf(-1)
        private set
    var zoneOptions by mutableStateOf<List<Pair<Int, String>>>(emptyList())
        private set
    var zoneSelectionError by mutableStateOf("")
        private set
    var isLoadingZones by mutableStateOf(false)
        private set

    // Step 4: Vehicle Configuration (if Driver or Both)
    var vehicleLicensePlate by mutableStateOf("")
        private set
    var vehicleNumberSlots by mutableStateOf("1") // Default to 1 slot
        private set
    var vehicleBrand by mutableStateOf("")
        private set
    var vehicleModel by mutableStateOf("")
        private set
    var vehicleColor by mutableStateOf("")
        private set

    // Step 4 Errors
    var vehicleLicensePlateError by mutableStateOf("")
        private set
    var vehicleNumberSlotsError by mutableStateOf("")
        private set
    var vehicleBrandError by mutableStateOf("")
        private set
    var vehicleModelError by mutableStateOf("")
        private set
    var vehicleColorError by mutableStateOf("")
        private set

    // General UI State
    var currentStep by mutableIntStateOf(1)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var registrationError by mutableStateOf("")
        private set
    var registrationSuccess by mutableStateOf(false)
        private set

    init {
        restoreDraft()
        loadZones()
    }

    private fun restoreDraft() {
        viewModelScope.launch(Dispatchers.IO) {
            val draft = registerDraftManager.getDraft()
            withContext(Dispatchers.Main) {
                firstName = draft.firstName
                lastName = draft.lastName
                email = draft.email
                //password = draft.password
                // confirmPassword is not saved in draft as it's a runtime check
                selectedRole = draft.role
                selectedZoneId = draft.zoneId ?: -1
                selectedZoneName = zoneOptions.firstOrNull { it.first == draft.zoneId }?.second ?: ""
                vehicleLicensePlate = draft.vehicleLicensePlate
                vehicleNumberSlots = draft.vehicleNumberSlots?.toString() ?: "1"
                vehicleBrand = draft.vehicleBrand
                vehicleModel = draft.vehicleModel
                vehicleColor = draft.vehicleColor
                Log.d("RegisterViewModel", "Draft restored: Email=${draft.email}, Role=${draft.role}")
            }
        }
    }

    private fun saveDraft() {
        viewModelScope.launch(Dispatchers.IO) {
            registerDraftManager.saveDraft(
                firstName,
                lastName,
                email,
                password,
                selectedRole,
                selectedZoneId.takeIf { it != -1 },
                vehicleLicensePlate,
                vehicleNumberSlots.toIntOrNull(),
                vehicleBrand,
                vehicleModel,
                vehicleColor
            )
        }
    }

    private fun
            loadZones() {
        viewModelScope.launch {
            isLoadingZones = true
            if (!networkHelper.isInternetAvailable()) {
                // TODO: Implement zone caching for offline availability if needed
                Log.d("RegisterViewModel", "No internet to load zones. Fallback/caching needed.")
                // For now, if no internet, no zones will be available
                withContext(Dispatchers.Main) {
                    zoneOptions = emptyList()
                    isLoadingZones = false
                }
                return@launch
            }
            try {
                val zones = zoneRepository.getPublicZones()
                Log.d("RegisterViewModel", "Zones response: $zones")
                withContext(Dispatchers.Main) {
                    zoneOptions = zones.map { it.id to it.name }
                    // If a draft zone was restored, set its name
                    if (selectedZoneId != -1 && selectedZoneName.isEmpty()) {
                        selectedZoneName = zoneOptions.firstOrNull { it.first == selectedZoneId }?.second ?: ""
                    } else if (selectedZoneId == -1 && zoneOptions.isNotEmpty()) {
                        // Optionally pre-select first zone if no draft and zones available
                        // selectedZoneId = zoneOptions.first().first
                        // selectedZoneName = zoneOptions.first().second
                    }
                    Log.d("RegisterViewModel", "Zones loaded: ${zoneOptions.size}")
                }
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Error loading zones", e)
                withContext(Dispatchers.Main) {
                    registrationError = "No se pudieron cargar las zonas. Revisa tu conexión."
                    zoneOptions = emptyList()
                }
            } finally {
                isLoadingZones = false
            }
        }
    }

    // Input Handlers
    fun onFirstNameChange(newValue: String) {
        if (newValue.length > MAX_FIRST_NAME_LENGTH) {
            firstNameInputError = "Solo puedes escribir $MAX_FIRST_NAME_LENGTH caracteres"
        } else {
            firstName = newValue
            firstNameInputError = ""
        }
        saveDraft()
        clearErrors()
    }

    fun onLastNameChange(newValue: String) {
        if (newValue.length > MAX_LAST_NAME_LENGTH) {
            lastNameInputError = "Solo puedes escribir $MAX_LAST_NAME_LENGTH caracteres"
        } else {
            lastName = newValue
            lastNameInputError = ""
        }
        saveDraft()
        clearErrors()
    }

    fun onEmailChange(newEmail: String) {
        if (newEmail.length > MAX_EMAIL_LENGTH) {
            emailInputError = "Solo puedes escribir $MAX_EMAIL_LENGTH caracteres"
        } else {
            email = newEmail
            emailInputError = ""
        }
        saveDraft()
        clearErrors()
    }

    fun onPasswordChange(newPassword: String) {
        if (newPassword.length > MAX_PASSWORD_LENGTH) {
            passwordInputError = "Solo puedes escribir $MAX_PASSWORD_LENGTH caracteres"
        } else {
            password = newPassword
            passwordInputError = ""
        }
        saveDraft()
        clearErrors()
    }

    fun onConfirmPasswordChange(newValue: String) {
        if (newValue.length > MAX_PASSWORD_LENGTH) {
            confirmPasswordInputError = "Solo puedes escribir $MAX_PASSWORD_LENGTH caracteres"
        } else {
            confirmPassword = newValue
            confirmPasswordInputError = ""
        }
        saveDraft()
        clearErrors()
    }

    fun onRoleSelected(role: String) {
        selectedRole = role
        roleSelectionError = ""
        saveDraft()
        clearErrors()
    }

    fun onZoneSelected(zoneId: Int, zoneName: String) {
        selectedZoneId = zoneId
        selectedZoneName = zoneName
        zoneSelectionError = ""
        saveDraft()
        clearErrors()
    }

    fun onVehicleLicensePlateChange(newValue: String) {
        if (newValue.length > MAX_LICENSE_PLATE_LENGTH) {
            vehicleLicensePlateError = "Solo puedes escribir $MAX_LICENSE_PLATE_LENGTH caracteres"
        } else {
            vehicleLicensePlate = newValue
            vehicleLicensePlateError = ""
        }
        saveDraft()
        clearErrors()
    }

    fun onVehicleNumberSlotsChange(newValue: String) {
        val num = newValue.toIntOrNull()
        if (newValue.length > 2) {
            vehicleNumberSlotsError = "Max 2 digitos"
        } else if (num == null || num <= 0) {
            vehicleNumberSlotsError = "Debe ser un número > 0"
        } else {
            vehicleNumberSlots = newValue
            vehicleNumberSlotsError = ""
        }
        saveDraft()
        clearErrors()
    }

    fun onVehicleBrandChange(newValue: String) {
        if (newValue.length > MAX_BRAND_MODEL_COLOR_LENGTH) {
            vehicleBrandError = "Solo puedes escribir $MAX_BRAND_MODEL_COLOR_LENGTH caracteres"
        } else {
            vehicleBrand = newValue
            vehicleBrandError = ""
        }
        saveDraft()
        clearErrors()
    }

    fun onVehicleModelChange(newValue: String) {
        if (newValue.length > MAX_BRAND_MODEL_COLOR_LENGTH) {
            vehicleModelError = "Solo puedes escribir $MAX_BRAND_MODEL_COLOR_LENGTH caracteres"
        } else {
            vehicleModel = newValue
            vehicleModelError = ""
        }
        saveDraft()
        clearErrors()
    }

    fun onVehicleColorChange(newValue: String) {
        if (newValue.length > MAX_BRAND_MODEL_COLOR_LENGTH) {
            vehicleColorError = "Solo puedes escribir $MAX_BRAND_MODEL_COLOR_LENGTH caracteres"
        } else {
            vehicleColor = newValue
            vehicleColorError = ""
        }
        saveDraft()
        clearErrors()
    }

    fun nextStep() {
        if (currentStep == 1 && !validateStep1()) return
        if (currentStep == 2 && !validateStep2()) return
        if (currentStep == 3 && !validateStep3()) return

        if (currentStep < 4) {
            currentStep++
        }
        registrationError = ""
    }

    fun previousStep() {
        if (currentStep > 1) {
            currentStep--
        }
        registrationError = ""
    }

    private fun clearErrors() {
        registrationError = ""
        firstNameInputError = ""
        lastNameInputError = ""
        emailInputError = ""
        passwordInputError = ""
        confirmPasswordInputError = ""
        roleSelectionError = ""
        zoneSelectionError = ""
        vehicleLicensePlateError = ""
        vehicleNumberSlotsError = ""
        vehicleBrandError = ""
        vehicleModelError = ""
        vehicleColorError = ""
    }

    private fun validateStep1(): Boolean {
        clearErrors()
        var isValid = true

        if (firstName.trim().isEmpty()) {
            firstNameInputError = "El nombre no puede estar vacío"
            isValid = false
        } else if (firstName.length > MAX_FIRST_NAME_LENGTH) {
            firstNameInputError = "El nombre excede los $MAX_FIRST_NAME_LENGTH caracteres"
            isValid = false
        }

        if (lastName.trim().isEmpty()) {
            lastNameInputError = "El apellido no puede estar vacío"
            isValid = false
        } else if (lastName.length > MAX_LAST_NAME_LENGTH) {
            lastNameInputError = "El apellido excede los $MAX_LAST_NAME_LENGTH caracteres"
            isValid = false
        }

        val currentEmail = email.trim().lowercase()
        if (currentEmail.isEmpty()) {
            emailInputError = "El correo no puede estar vacío"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentEmail).matches()) {
            emailInputError = "Formato de correo inválido"
            isValid = false
        } else if (!currentEmail.endsWith("@uniandes.edu.co")) {
            emailInputError = "Solo se permite correo institucional @uniandes.edu.co"
            isValid = false
        } else if (currentEmail.length > MAX_EMAIL_LENGTH) {
            emailInputError = "El correo excede los $MAX_EMAIL_LENGTH caracteres"
            isValid = false
        }

        val currentPassword = password.trim()
        if (currentPassword.isEmpty()) {
            passwordInputError = "La contraseña no puede estar vacía"
            isValid = false
        } else if (currentPassword.length < 6) {
            passwordInputError = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        } else if (currentPassword.length > MAX_PASSWORD_LENGTH) {
            passwordInputError = "La contraseña excede los $MAX_PASSWORD_LENGTH caracteres"
            isValid = false
        }

        if (confirmPassword.trim().isEmpty()) {
            confirmPasswordInputError = "Confirma tu contraseña"
            isValid = false
        } else if (confirmPassword.trim() != currentPassword) {
            confirmPasswordInputError = "Las contraseñas no coinciden"
            isValid = false
        }
        return isValid
    }

    private fun validateStep2(): Boolean {
        clearErrors()
        var isValid = true
        if (selectedRole.isEmpty()) {
            roleSelectionError = "Debes seleccionar un rol"
            isValid = false
        }
        return isValid
    }

    private fun validateStep3(): Boolean {
        clearErrors()
        var isValid = true
        if (selectedZoneId == -1) {
            zoneSelectionError = "Debes seleccionar una zona preferida"
            isValid = false
        }
        return isValid
    }

    private fun validateVehicleInputs(): Boolean {
        var isValid = true

        if (selectedRole == "driver" || selectedRole == "both") {
            if (vehicleLicensePlate.trim().isEmpty()) {
                vehicleLicensePlateError = "Placa no puede estar vacía"
                isValid = false
            } else if (vehicleLicensePlate.length > MAX_LICENSE_PLATE_LENGTH) {
                vehicleLicensePlateError = "Placa excede los $MAX_LICENSE_PLATE_LENGTH caracteres"
                isValid = false
            }

            val slots = vehicleNumberSlots.toIntOrNull()
            if (vehicleNumberSlots.isEmpty() || slots == null || slots <= 0) {
                vehicleNumberSlotsError = "Cupos deben ser un número mayor a 0"
                isValid = false
            } else if (vehicleNumberSlots.length > 2) {
                vehicleNumberSlotsError = "Cupos: Máx 2 dígitos"
                isValid = false
            }

            if (vehicleBrand.trim().isEmpty()) {
                vehicleBrandError = "Marca no puede estar vacía"
                isValid = false
            } else if (vehicleBrand.length > MAX_BRAND_MODEL_COLOR_LENGTH) {
                vehicleBrandError = "Marca excede los $MAX_BRAND_MODEL_COLOR_LENGTH caracteres"
                isValid = false
            }

            if (vehicleModel.trim().isEmpty()) {
                vehicleModelError = "Modelo no puede estar vacío"
                isValid = false
            } else if (vehicleModel.length > MAX_BRAND_MODEL_COLOR_LENGTH) {
                vehicleModelError = "Modelo excede los $MAX_BRAND_MODEL_COLOR_LENGTH caracteres"
                isValid = false
            }

            if (vehicleColor.trim().isEmpty()) {
                vehicleColorError = "Color no puede estar vacío"
                isValid = false
            } else if (vehicleColor.length > MAX_BRAND_MODEL_COLOR_LENGTH) {
                vehicleColorError = "Color excede los $MAX_BRAND_MODEL_COLOR_LENGTH caracteres"
                isValid = false
            }
        }
        return isValid
    }

    fun onRegisterClick( onRegistrationSuccess: (String, String) -> Unit) {
        if (!validateStep1() || !validateStep2() || !validateStep3() || !validateVehicleInputs()) {
            registrationError = "Completa todos los campos requeridos."
            return
        }

        if (!networkHelper.isInternetAvailable()) {
            registrationError = "Sin conexión a internet. Tu información de registro fue guardada. Intenta de nuevo cuando tengas conexión."
            return
        }

        viewModelScope.launch {
            isLoading = true
            registrationError = ""

            try {

                val vehicleDto =
                    if (selectedRole == "driver" || selectedRole == "both") {

                        VehicleRequestDto(
                            license_plate = vehicleLicensePlate.trim(),
                            number_slots = vehicleNumberSlots.toInt(),
                            brand = vehicleBrand.trim(),
                            model = vehicleModel.trim(),
                            color = vehicleColor.trim()
                        )

                    } else {
                        null
                    }

                val request = CreateCompleteUserRequestDto(
                    first_name = firstName.trim(),
                    last_name = lastName.trim(),
                    email = email.trim().lowercase(),
                    password = password.trim(),
                    role = selectedRole,
                    zone_id = selectedZoneId,
                    vehicle = vehicleDto
                )

                val response = authRepository.createCompleteUser(request)

                if (response.success) {

                    registrationSuccess = true

                    registerDraftManager.clearDraft()

                    Log.d(
                        "RegisterViewModel",
                        "Registration successful for email: $email"
                    )

                    onRegistrationSuccess(
                        email.trim().lowercase(),
                        password.trim()
                    )

                } else {

                    registrationError =
                        response.error
                            ?: "Ocurrió un error desconocido. Repórtalo para poder solucionarlo."

                    Log.e(
                        "RegisterViewModel",
                        "Registration failed: ${response.error_code}"
                    )
                }

            } catch (e: Exception) {

                registrationError =
                    "Ocurrió un error inesperado. Intenta de nuevo."

                Log.e(
                    "RegisterViewModel",
                    "Registration exception",
                    e
                )

            } finally {
                isLoading = false
            }
        }
    }
}