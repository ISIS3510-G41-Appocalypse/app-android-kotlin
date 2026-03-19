package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.services.auth.AuthService

class AuthRepositoryImpl(private val authService: AuthService) : AuthRepository {
    //EVITAMOS QUE EL HILO SE QUEDE ESPERANDO TODA LA VIDA UNA RESPUESTA.
    override suspend fun login(email: String, password: String): Boolean {
        return authService.login(email, password)
    }
}