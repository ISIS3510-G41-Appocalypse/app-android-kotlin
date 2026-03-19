package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.services.AuthService

class AuthRepositoryImpl(private val authService: AuthService) : AuthRepository {

    override fun login(email: String, password: String): Boolean {
        return authService.login(email, password)
    }
}