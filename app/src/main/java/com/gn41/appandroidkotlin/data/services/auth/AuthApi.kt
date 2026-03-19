package com.gn41.appandroidkotlin.data.services.auth

import com.gn41.appandroidkotlin.data.dto.auth.LoginRequestDto
import com.gn41.appandroidkotlin.data.dto.auth.LoginResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
interface AuthApi {

    @Headers("Content-Type: application/json")
    @POST("auth/v1/token?grant_type=password")
    suspend fun login(
        @Header("apikey") apiKey: String,
        @Body request: LoginRequestDto
    ): Response<LoginResponseDto>
}