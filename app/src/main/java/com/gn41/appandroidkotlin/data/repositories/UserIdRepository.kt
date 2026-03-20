package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.user.UserIdDto

interface UserIdRepository {
    suspend fun getUserByAuthId(authId: String): UserIdDto
}