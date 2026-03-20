package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.user.UserIdDto

class UserIdRepositoryImpl(private val userIdService: UserIdService) : UserIdRepository {
    override suspend fun getUserByAuthId(authId: String) : UserIdDto {
        return userIdService.getUserByAuthId(authId)
    }
}