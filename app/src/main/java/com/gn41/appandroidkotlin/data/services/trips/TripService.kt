package com.gn41.appandroidkotlin.data.services.trips

import android.util.Log
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.trips.TripDriverDto
import com.gn41.appandroidkotlin.data.dto.trips.TripReservationDto
import com.gn41.appandroidkotlin.data.dto.trips.TripRideDto
import com.gn41.appandroidkotlin.data.dto.trips.TripRiderDto
import com.gn41.appandroidkotlin.data.dto.trips.TripUserDto
import com.gn41.appandroidkotlin.data.services.SupabaseClient

class TripService {

    private val tripApi = SupabaseClient.tripApi

    suspend fun getUserByAuthId(authId: String, token: String): TripUserDto? {
        return try {
            val response = tripApi.getUserByAuthId(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                authId = "eq.$authId"
            )

            if (response.isSuccessful) {
                response.body()?.firstOrNull()
            } else {
                Log.e("TripService", "getUserByAuthId error=${response.code()} ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("TripService", "getUserByAuthId exception", e)
            null
        }
    }

    suspend fun getRiderByUserId(userId: Int, token: String): TripRiderDto? {
        return try {
            val response = tripApi.getRiderByUserId(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                userId = "eq.$userId"
            )

            if (response.isSuccessful) {
                response.body()?.firstOrNull()
            } else {
                Log.e("TripService", "getRiderByUserId error=${response.code()} ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("TripService", "getRiderByUserId exception", e)
            null
        }
    }

    suspend fun getDriverByUserId(userId: Int, token: String): TripDriverDto? {
        return try {
            val response = tripApi.getDriverByUserId(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                userId = "eq.$userId"
            )

            if (response.isSuccessful) {
                response.body()?.firstOrNull()
            } else {
                Log.e("TripService", "getDriverByUserId error=${response.code()} ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("TripService", "getDriverByUserId exception", e)
            null
        }
    }

    suspend fun getActiveRiderReservation(riderId: Int, token: String): List<TripReservationDto> {
        return try {
            val response = tripApi.getActiveRiderReservation(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                riderId = "eq.$riderId"
            )

            if (response.isSuccessful) {
                response.body() ?: emptyList<TripReservationDto>()
            } else {
                Log.e("TripService", "getActiveRiderReservation error=${response.code()} ${response.errorBody()?.string()}")
                emptyList<TripReservationDto>()
            }
        } catch (e: Exception) {
            Log.e("TripService", "getActiveRiderReservation exception", e)
            emptyList<TripReservationDto>()
        }
    }

    suspend fun getActiveDriverRide(driverId: Int, token: String): TripRideDto? {
        return try {
            val response = tripApi.getActiveDriverRide(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                driverId = "eq.$driverId"
            )

            if (response.isSuccessful) {
                response.body()?.firstOrNull()
            } else {
                Log.e("TripService", "getActiveDriverRide error=${response.code()} ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("TripService", "getActiveDriverRide exception", e)
            null
        }
    }

    suspend fun getReservationsForRide(rideId: Int, token: String): List<TripReservationDto> {
        return try {
            val response = tripApi.getReservationsForRide(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                rideId = "eq.$rideId"
            )

            if (response.isSuccessful) {
                response.body() ?: emptyList<TripReservationDto>()
            } else {
                Log.e("TripService", "getReservationsForRide error=${response.code()} ${response.errorBody()?.string()}")
                emptyList<TripReservationDto>()
            }
        } catch (e: Exception) {
            Log.e("TripService", "getReservationsForRide exception", e)
            emptyList<TripReservationDto>()
        }
    }

    suspend fun updateReservationState(reservationId: Int, newState: String, token: String): Boolean {
        return try {
            val response = tripApi.updateReservationState(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                reservationId = "eq.$reservationId",
                body = mapOf("state" to newState)
            )

            if (!response.isSuccessful) {
                Log.e("TripService", "updateReservationState error=${response.code()} ${response.errorBody()?.string()}")
            }

            response.isSuccessful
        } catch (e: Exception) {
            Log.e("TripService", "updateReservationState exception", e)
            false
        }
    }

    suspend fun updateRideState(rideId: Int, newState: String, token: String): Boolean {
        return try {
            val stateCandidates = when (newState) {
                "CANCELADO" -> listOf("CANCELADO", "CANCELADA")
                "FINALIZADO" -> listOf("FINALIZADO", "FINALIZADA")
                else -> listOf(newState)
            }

            var lastError = ""
            for (state in stateCandidates) {
                val response = tripApi.updateRideState(
                    token = "Bearer $token",
                    apiKey = BuildConfig.SUPABASE_KEY,
                    rideId = "eq.$rideId",
                    body = mapOf("state" to state)
                )

                if (response.isSuccessful) {
                    return true
                }

                lastError = response.errorBody()?.string().orEmpty()
                Log.e("TripService", "updateRideState error state=$state code=${response.code()} $lastError")
            }

            false
        } catch (e: Exception) {
            Log.e("TripService", "updateRideState exception", e)
            false
        }
    }
}


