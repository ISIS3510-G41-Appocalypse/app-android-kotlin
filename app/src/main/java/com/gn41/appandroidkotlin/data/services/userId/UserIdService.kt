package com.gn41.appandroidkotlin.data.services.userId

import com.gn41.appandroidkotlin.data.dto.user.UserIdDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.services.SupabaseClient

class UserIdService (private val sessionManager: SessionManager) {
    private val userIdApi = SupabaseClient.userIdApi

    suspend fun getUserByAuthId(authId:String) : UserIdDto {

            val response = userIdApi.getUserByAuthId()

        return response.firstOrNull()
            ?: throw Exception("User not found")
    }
}