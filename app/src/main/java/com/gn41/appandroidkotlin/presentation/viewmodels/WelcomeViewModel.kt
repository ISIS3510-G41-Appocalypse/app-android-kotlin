package com.gn41.appandroidkotlin.presentation.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.gn41.appandroidkotlin.data.repositories.AuthRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.gn41.appandroidkotlin.data.local.SessionManager

class WelcomeViewModel( private val authRepository: AuthRepository,    private val sessionManager: SessionManager) : ViewModel() {

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

    //ESTA ES LA FUNCIÓN QUE EFECTIVAMENTE INTENTA HACER LOGIN (no abrir la tarjeta)

    fun onLoginSubmit() {
        viewModelScope.launch {
            val loginResult = authRepository.login(email, password)

            if (loginResult != null) {
                sessionToken = loginResult
                sessionManager.saveToken(loginResult)
                isLoggedIn = true
                loginError = ""
            } else {
                sessionToken = ""
                isLoggedIn = false
                loginError = "Correo o contraseña inválidos"
            }

            println("Token: $sessionToken")
        }
    }


}