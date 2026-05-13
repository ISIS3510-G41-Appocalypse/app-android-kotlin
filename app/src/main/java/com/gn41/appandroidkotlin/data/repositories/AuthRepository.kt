package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.auth.LoginResponseDto
import com.gn41.appandroidkotlin.data.services.auth.AuthService

class AuthRepository(private val authService: AuthService) {
    //EVITAMOS QUE EL HILO SE QUEDE ESPERANDO TODA LA VIDA UNA RESPUESTA.
    suspend fun login(email: String, password: String): LoginResponseDto? {
        return authService.login(email, password)
    }
}