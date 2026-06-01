package com.gn41.appandroidkotlin.data.services.auth

import com.gn41.appandroidkotlin.data.dto.auth.CreateCompleteUserRequestDto
import com.gn41.appandroidkotlin.data.dto.auth.CreateCompleteUserResponseDto
import com.gn41.appandroidkotlin.data.dto.auth.LoginRequestDto
import com.gn41.appandroidkotlin.data.dto.auth.LoginResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
import com.gn41.appandroidkotlin.data.dto.auth.UserProfileDto

interface AuthApi {

    @Headers("Content-Type: application/json")
    @POST("auth/v1/token?grant_type=password")
    suspend fun login(
        @Header("apikey") apiKey: String,
        @Body request: LoginRequestDto
    ): Response<LoginResponseDto>

    @Headers("Content-Type: application/json")
    @POST("functions/v1/create-complete-user")
    suspend fun createCompleteUser(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Body request: CreateCompleteUserRequestDto
    ): Response<CreateCompleteUserResponseDto>


    @GET("rest/v1/users")
    suspend fun getUserProfile(
        @Header("Authorization") token: String,
        @Header("apikey") apiKey: String,
        @Query("auth_id") authId: String,
        @Query("select")
        select: String =
            "id,first_name,last_name,zones(name),drivers(rating,cancellation_odds),riders(rating,cancellation_odds)"
    ): Response<List<UserProfileDto>>



}