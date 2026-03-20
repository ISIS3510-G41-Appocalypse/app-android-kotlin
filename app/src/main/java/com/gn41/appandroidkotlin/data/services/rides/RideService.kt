package com.gn41.appandroidkotlin.data.services.rides

import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.createRide.CreateRideRequestDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.services.SupabaseClient
import com.gn41.appandroidkotlin.data.services.userId.UserIdService

class RideService (private val sessionManager: SessionManager,
    private val userIdService: UserIdService) {
    private val rideApi = SupabaseClient.rideApi

    suspend fun create(request: CreateRideRequestDto) : Result<Unit> {
        try {
            val token = sessionManager.getToken()
                ?: return Result.failure(Exception("No auth token"))

            val authId = sessionManager.getUserId()
                ?: return Result.failure(Exception("No user logged in"))

            val userId = userIdService.getUserByAuthId(authId).id

            val finalRequest = request.copy(
                driverId = userId,
                state = "OFERTADO"
            )

            rideApi.create( apiKey = BuildConfig.SUPABASE_KEY,
                authorization = "Bearer $token",
                finalRequest)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }

        return Result.success(Unit)
    }
}