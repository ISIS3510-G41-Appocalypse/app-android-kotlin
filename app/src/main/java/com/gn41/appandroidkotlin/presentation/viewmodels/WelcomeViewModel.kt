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

class WelcomeViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val tripRepository: TripRepository
) : ViewModel() {

    //Para ayudarnnos a decidi si mostramos o on el card de login.
    var showLoginCard by mutableStateOf(value = false)
        private set
    var email by mutableStateOf(value = "")
        private set
    var password by mutableStateOf(value = "")
        private set

    var loginError by mutableStateOf(value = "")
        private set
    var isLoggedIn by mutableStateOf(value = false)
        private set

    var sessionToken by mutableStateOf(value = "")
        private set

    var sessionUserId by mutableStateOf(value = "")
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
        showLoginCard = true;
    }
    //esta la vamos a implementar despues.... pero se puede reutilizar en el logi card... on en el botón de registro del home.
    fun onRegisterClicked() {
        println("TODO: Register desde WelcomeViewModel")
    }

    fun onEmailInput(newEmail: String) {
        email = newEmail
    }

    fun onPasswordInput(newPassword: String) {
        password = newPassword
    }

    fun onLoginSubmit() {
        viewModelScope.launch {
            val loginResult = authRepository.login(email, password)

            if (loginResult != null) {
                sessionToken = loginResult.access_token
                sessionManager.saveToken(loginResult.access_token)

                sessionUserId = loginResult.user.id
                sessionManager.saveUserId(loginResult.user.id)

                obtenerDriverId(loginResult.access_token)

                isLoggedIn = true
                loginError = ""
            } else {
                sessionToken = ""
                isLoggedIn = false
                loginError = "Correo o contraseña inválidos"
            }

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
        showLoginCard = false
    }
}