package com.gn41.appandroidkotlin.presentation.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.local.RegisterDraftManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterViewModel(
    private val registerDraftManager: RegisterDraftManager,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    companion object {
        private const val MAX_NAME_LENGTH = 50
        private const val MAX_EMAIL_LENGTH = 50
        private const val MAX_PASSWORD_LENGTH = 30
        private const val REGISTRATION_DELAY_MS = 2000L // Simulate network delay
    }

    var name by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    var nameInputError by mutableStateOf("")
        private set
    var emailInputError by mutableStateOf("")
        private set
    var passwordInputError by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set
    var registrationError by mutableStateOf("")
        private set
    var registrationSuccess by mutableStateOf(false)
        private set

    init {
        restoreDraft()
    }

    private fun restoreDraft() {
        viewModelScope.launch(Dispatchers.IO) {
            val (savedName, savedEmail, savedPassword) = registerDraftManager.getDraft()
            withContext(Dispatchers.Main) {
                name = savedName
                email = savedEmail
                password = savedPassword
                Log.d("RegisterViewModel", "Draft restored: Name=$name, Email=$email")
            }
        }
    }

    fun onNameChange(newName: String) {
        if (newName.length > MAX_NAME_LENGTH) {
            nameInputError = "Solo puedes escribir $MAX_NAME_LENGTH caracteres"
        } else {
            name = newName
            nameInputError = ""
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

    private fun saveDraft() {
        viewModelScope.launch(Dispatchers.IO) {
            registerDraftManager.saveDraft(name, email, password)
        }
    }

    private fun clearErrors() {
        if (registrationError.isNotEmpty()) registrationError = ""
    }

    fun onRegisterClick(onRegistrationSuccess: () -> Unit) {
        val currentName = name.trim()
        val currentEmail = email.trim().lowercase()
        val currentPassword = password.trim()

        if (!validateInputs(currentName, currentEmail, currentPassword)) {
            return
        }

        if (!networkHelper.isInternetAvailable()) {
            registrationError = "Revisa tu conexión a internet y vuelve a intentar."
            return
        }

        viewModelScope.launch {
            isLoading = true
            registrationError = ""

            // TODO: Replace with actual backend registration call
            delay(REGISTRATION_DELAY_MS)

            val simulatedSuccess = (currentName.isNotEmpty() && currentEmail.isNotEmpty() && currentPassword.isNotEmpty() && currentEmail.endsWith("@uniandes.edu.co"))

            if (simulatedSuccess) {
                // Simulate successful registration
                registrationSuccess = true
                registerDraftManager.clearDraft() // Clear draft on successful registration
                Log.d("RegisterViewModel", "Registration successful for email: $currentEmail")
                onRegistrationSuccess()
            } else {
                registrationError = "Error en el registro. Verifica tus datos e intenta de nuevo."
                Log.e("RegisterViewModel", "Registration failed for email: $currentEmail")
            }

            isLoading = false
        }
    }

    private fun validateInputs(currentName: String, currentEmail: String, currentPassword: String): Boolean {
        var isValid = true

        if (currentName.isEmpty()) {
            nameInputError = "El nombre no puede estar vacío"
            isValid = false
        } else if (currentName.length > MAX_NAME_LENGTH) {
            nameInputError = "El nombre excede los $MAX_NAME_LENGTH caracteres"
            isValid = false
        }

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

        registrationError = ""
        return isValid
    }
}