package com.gn41.appandroidkotlin.presentation.viewmodels

import android.util.Base64
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.gn41.appandroidkotlin.data.repositories.AuthRepository
import com.gn41.appandroidkotlin.data.repositories.TripRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import kotlinx.coroutines.withTimeoutOrNull

class WelcomeViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val tripRepository: TripRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    companion object {
        private const val MAX_EMAIL_LENGTH = 50
        private const val MAX_PASSWORD_LENGTH = 30
    }

    //Para ayudarnnos a decidi si mostramos o on el card de login.
    var showLoginCard by mutableStateOf(value = false)
        private set
    var email by mutableStateOf(value = "")
        private set
    var password by mutableStateOf(value = "")
        private set

    var emailInputError by mutableStateOf(value = "")
        private set
    var passwordInputError by mutableStateOf(value = "")
        private set

    var loginError by mutableStateOf(value = "")
        private set
    var isLoggedIn by mutableStateOf(value = false)
        private set

    var sessionToken by mutableStateOf(value = "")
        private set

    var sessionUserId by mutableStateOf(value = "")
        private set

    var isLoading by mutableStateOf(value = false)
        private set


    init {
        val savedToken = sessionManager.getToken()

        if (savedToken.isNotEmpty()) {
            sessionToken = savedToken
            isLoggedIn = true
        }
    }




    //Metodos

    fun onLoginClicked() {
        showLoginCard = true
    }
    //esta la vamos a implementar despues.... pero se puede reutilizar en el logi card... on en el botón de registro del home.
    fun onRegisterClicked() {
        println("TODO: Register desde WelcomeViewModel")
    }

    fun onEmailInput(newEmail: String) {
        if (newEmail.length > MAX_EMAIL_LENGTH) {
            email = newEmail.take(MAX_EMAIL_LENGTH)
            emailInputError = "Solo puedes escribir $MAX_EMAIL_LENGTH caracteres"
        } else {
            email = newEmail

            if (newEmail.length < MAX_EMAIL_LENGTH) {
                emailInputError = ""
            }
        }

        if (loginError.isNotEmpty()) {
            loginError = ""
        }
    }

    fun onPasswordInput(newPassword: String) {
        if (newPassword.length > MAX_PASSWORD_LENGTH) {
            password = newPassword.take(MAX_PASSWORD_LENGTH)
            passwordInputError = "Solo puedes escribir $MAX_PASSWORD_LENGTH caracteres"
        } else {
            password = newPassword

            if (newPassword.length < MAX_PASSWORD_LENGTH) {
                passwordInputError = ""
            }
        }

        if (loginError.isNotEmpty()) {
            loginError = ""
        }
    }
    fun onLoginSubmit() {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        if (cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
            loginError = "Completa el correo y la contraseña"
            return
        }

        if (!cleanEmail.endsWith("@uniandes.edu.co")) {
            loginError = "Solo se permite correo institucional @uniandes.edu.co"
            return
        }

        if (!networkHelper.isInternetAvailable()) {
            loginError = "Revisa tu conexión a internet y vuelve a intentar"
            return
        }

        viewModelScope.launch {
            isLoading = true
            loginError = ""

            val loginResult = withTimeoutOrNull(15000) {
                authRepository.login(cleanEmail, cleanPassword)
            }

            if (loginResult != null) {
                sessionToken = loginResult.access_token
                sessionManager.saveToken(loginResult.access_token)

                sessionUserId = loginResult.user.id
                sessionManager.saveUserId(loginResult.user.id)

                obtenerDriverId(loginResult.access_token)

                email = cleanEmail
                password = cleanPassword
                isLoggedIn = true
                loginError = ""
            } else {
                sessionToken = ""
                isLoggedIn = false

                loginError = if (networkHelper.isInternetAvailable()) {
                    "Correo o contraseña incorrectos."
                } else {
                    "Revisa tu conexión a internet y vuelve a intentar."
                }
            }

            isLoading = false
            Log.d("WelcomeVM", "Token: $sessionToken")
        }
    }

    private suspend fun obtenerDriverId(token: String) {
        try {
            val authId = extraerAuthIdDelToken(token)
            if (authId.isNullOrEmpty()) {
                Log.e("WelcomeVM", "No se pudo extraer authId del token")
                return
            }

            val usuario = tripRepository.getUserByAuthId(authId, token)
            if (usuario == null) {
                Log.e("WelcomeVM", "Usuario no encontrado para authId: $authId")
                return
            }

            val conductor = tripRepository.getDriverByUserId(usuario.id, token)
            if (conductor != null) {
                sessionManager.saveDriverId(conductor.id)
                Log.d("WelcomeVM", "Driver ID guardado: ${conductor.id}")
            } else {
                Log.w("WelcomeVM", "Usuario no tiene rol de conductor")
            }
        } catch (e: Exception) {
            Log.e("WelcomeVM", "Error obteniendo driver ID", e)
        }
    }

    private fun extraerAuthIdDelToken(token: String): String? {
        return try {
            val partes = token.split('.')
            if (partes.size < 2) return null

            val payloadBytes = Base64.decode(
                partes[1],
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
            val payload = String(payloadBytes)

            val regex = "\"sub\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            regex.find(payload)?.groupValues?.get(1)
        } catch (e: Exception) {
            Log.e("WelcomeVM", "Error extrayendo authId", e)
            null
        }
    }

    fun resetLoginState() {
        isLoggedIn = false
        sessionToken = ""
        sessionUserId = ""
        email = ""
        password = ""
        emailInputError = ""
        passwordInputError = ""
        showLoginCard = false
        isLoading = false
    }
}