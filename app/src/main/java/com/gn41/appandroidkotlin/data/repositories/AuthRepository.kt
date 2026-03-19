package com.gn41.appandroidkotlin.data.repositories

interface AuthRepository {

    fun login(email: String, password: String): Boolean

}