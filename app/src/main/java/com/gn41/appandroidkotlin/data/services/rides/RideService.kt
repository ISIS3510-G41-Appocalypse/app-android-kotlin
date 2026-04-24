package com.gn41.appandroidkotlin.data.services.rides

import android.util.Log
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.createRide.ActiveRideDto
import com.gn41.appandroidkotlin.data.dto.createRide.CreateRideRequestDto
import com.gn41.appandroidkotlin.data.dto.createRide.RideUserDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.services.SupabaseClient
import com.gn41.appandroidkotlin.data.services.userId.UserIdService
import retrofit2.HttpException

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
            val driverId = userIdService.getDriverIdByUserId(userId)

            val finalRequest = request.copy(
                driverId = driverId,
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

    suspend fun cancelRide(id: Int) : Result<Unit> {
        val token = sessionManager.getToken()

        if (token.isEmpty()) {
            return Result.failure(Exception("No auth token"))
        }

        rideApi.cancelRide(
            authorization = "Bearer $token",
            apiKey = BuildConfig.SUPABASE_KEY,
            id = "eq.$id",
            ridePatch = mapOf("state" to "CANCELADO")
        )

        return Result.success(Unit)
    }

    suspend fun getActiveRide() : ActiveRideDto? {
        val token = sessionManager.getToken()

        if (token.isEmpty()) {
            return null
        }

        val userId = userIdService.getUserByAuthId().id
        val driverId = userIdService.getDriverIdByUserId(userId)

        val ride = rideApi.getActiveRide(
            authorization = "Bearer $token",
            apiKey = BuildConfig.SUPABASE_KEY,
            driverId = "eq.$driverId"
        ).firstOrNull()

        return ride
    }

    suspend fun getRideUsers(id: Int): List<RideUserDto>? {
        try {
            val token = sessionManager.getToken()

            if (token.isEmpty()) {
                return null
            }

            val rideUsersId = rideApi.getRideUsersId(
                authorization = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                rideId = "eq.$id"
            )

            if (rideUsersId.isEmpty()) {
                return null
            }

            var riderIds = ""
            for (i in rideUsersId.indices) {
                if (i==0){
                    riderIds = "${rideUsersId[i].riderId}"
                }
                else {
                    riderIds = "$riderIds,${rideUsersId[i].riderId}"
                }
            }

            Log.d("Id de los riders",riderIds)

            val rideUsersCancellationOddsUsersId = rideApi.getRideUsersCancellationOdds(
                authorization = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                ids = "in.($riderIds)"
            )

            var userIds = ""
            for (i in rideUsersCancellationOddsUsersId.indices) {
                if (i==0){
                    userIds = "${rideUsersCancellationOddsUsersId[i].userId}"
                }
                else {
                    userIds = "$userIds,${rideUsersCancellationOddsUsersId[i].userId}"
                }
            }

            val rideUsersInfo = rideApi.getRideUsersInfo(
                authorization = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                ids = "in.($userIds)"
            )

            val rideUsers = mutableListOf<RideUserDto>()
            for (i in rideUsersId.indices) {
                val rideUser = RideUserDto(
                    riderId = rideUsersId[i].riderId,
                    cancellationOdds = rideUsersCancellationOddsUsersId[i].cancellationOdds,
                    name = rideUsersInfo[i].firstName,
                    lastName = rideUsersInfo[i].lastName
                )
                rideUsers.add(rideUser)
            }

            return rideUsers
        }
        catch(e: HttpException){
            Log.e("API", e.response()?.errorBody()?.string() ?: "null")
            return null
        }
    }

}