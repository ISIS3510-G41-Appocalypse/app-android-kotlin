package com.gn41.appandroidkotlin.data.services.userId

import com.gn41.appandroidkotlin.data.dto.user.UserIdDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface UserIdApi {
    @GET("rest/v1/users")
    suspend fun getUserByAuthId(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("select") select: String = "id"
    ) : List<UserIdDto>
}