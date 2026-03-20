package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.auth.LoginResponseDto

interface AuthRepository {
    // CON SUSPEND EVITAMOS QUE EL HILO SE QUEDE ESPERANDO TODA LA VIDA UNA RESPUESTA.
    suspend fun login(email: String, password: String): LoginResponseDto?

}