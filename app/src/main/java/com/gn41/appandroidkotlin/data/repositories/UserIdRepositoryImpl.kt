package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.user.UserIdDto
import com.gn41.appandroidkotlin.data.services.userId.UserIdService

class UserIdRepositoryImpl(private val userIdService: UserIdService) : UserIdRepository {
    override suspend fun getUserByAuthId(authId: String) : UserIdDto {
        return userIdService.getUserByAuthId(authId)
    }
}