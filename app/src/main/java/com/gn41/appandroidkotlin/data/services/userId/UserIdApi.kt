package com.gn41.appandroidkotlin.data.services.userId

import com.gn41.appandroidkotlin.data.dto.user.UserIdDto
import retrofit2.http.GET
import retrofit2.http.Query

interface UserIdApi {
    @GET("rest/v1/users")
    suspend fun getUserByAuthId(
        @Query("select") select: String = "id"
    ) : List<UserIdDto>
}