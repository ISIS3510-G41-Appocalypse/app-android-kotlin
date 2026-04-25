package com.gn41.appandroidkotlin.data.services.location

import android.util.Log
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.location.UserSharedLocationDto
import com.gn41.appandroidkotlin.data.services.SupabaseClient

class LocationService {

    private val locationApi = SupabaseClient.locationApi

    suspend fun insertUserLocation(
        dto: UserSharedLocationDto,
        token: String
    ): Boolean {
        return try {
            val response = locationApi.insertUserLocation(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                body = dto
            )

            if (!response.isSuccessful) {
                Log.e(
                    "LocationService",
                    "insertUserLocation error=${response.code()} ${response.errorBody()?.string()}"
                )
            }

            response.isSuccessful
        } catch (e: Exception) {
            Log.e("LocationService", "insertUserLocation exception", e)
            false
        }
    }

    suspend fun getLocationsByRide(
        rideId: Int,
        token: String
    ): List<UserSharedLocationDto> {
        val response = locationApi.getLocationsByRide(
            token = "Bearer $token",
            apiKey = BuildConfig.SUPABASE_KEY,
            rideId = "eq.$rideId"
        )

        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        }

        throw Exception("No se pudieron cargar las ubicaciones del ride.")
    }
}