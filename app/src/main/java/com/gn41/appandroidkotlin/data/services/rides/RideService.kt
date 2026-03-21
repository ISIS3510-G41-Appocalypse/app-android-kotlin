package com.gn41.appandroidkotlin.data.services.rides

import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.createRide.CreateRideRequestDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.services.SupabaseClient
import com.gn41.appandroidkotlin.data.services.userId.UserIdService

class RideService(
    private val sessionManager: SessionManager,
    private val userIdService: UserIdService
) {
    private val rideApi = SupabaseClient.rideApi

    suspend fun create(request: CreateRideRequestDto): Result<Unit> {
        return try {
            val token = sessionManager.getToken()

            if (token.isEmpty()) {
                return Result.failure(Exception("No auth token"))
            }

            val userId = userIdService.getUserByAuthId().id

            val finalRequest = request.copy(
                driverId = userId,
                state = "OFERTADO"
            )

            rideApi.create(
                authorization = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                request = finalRequest
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}