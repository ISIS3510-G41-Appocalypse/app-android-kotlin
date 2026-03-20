package com.gn41.appandroidkotlin.data.repositories

interface AuthRepository {
    // CON SUSPEND EVITAMOS QUE EL HILO SE QUEDE ESPERANDO TODA LA VIDA UNA RESPUESTA.
    suspend fun login(email: String, password: String): String?

}