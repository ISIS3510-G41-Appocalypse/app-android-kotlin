package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.auth.LoginResponseDto
import com.gn41.appandroidkotlin.data.services.auth.AuthService
import com.gn41.appandroidkotlin.data.dto.auth.CreateCompleteUserRequestDto
import com.gn41.appandroidkotlin.data.dto.auth.CreateCompleteUserResponseDto
import com.gn41.appandroidkotlin.data.dto.auth.UserProfileDto

class AuthRepository(private val authService: AuthService) {
    //EVITAMOS QUE EL HILO SE QUEDE ESPERANDO TODA LA VIDA UNA RESPUESTA.
    suspend fun login(email: String, password: String): LoginResponseDto? {
        return authService.login(email, password)
    }

    suspend fun createCompleteUser(
        request: CreateCompleteUserRequestDto
    ): CreateCompleteUserResponseDto {

        return authService.createCompleteUser(request)
    }

    suspend fun getUserProfile(
        authId: String,
        token: String
    ): UserProfileDto? {

        return authService.getUserProfile(
            authId,
            token
        )
    }
}

