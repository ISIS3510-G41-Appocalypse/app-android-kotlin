package com.gn41.appandroidkotlin.data.services.rides

import android.util.Log
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.rides.RideDto
import com.gn41.appandroidkotlin.data.services.SupabaseClient

class RidesService {
    private val ridesApi = SupabaseClient.ridesApi

    // expande relaciones para obtener conductor, vehiculo y zona
    private val enrichedSelect = "*,drivers(*,users(*)),vehicles(*),zones(*)"

    suspend fun getRides(token: String): List<RideDto>? {
        return try {
            val response = ridesApi.getRides(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                select = enrichedSelect
            )

            if (response.isSuccessful) {
                Log.d("RidesService", "getRides success: ${response.body()}")
                response.body()
            } else {
                Log.e("RidesService", "getRides failed: code=${response.code()}")
                Log.e("RidesService", "error body=${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("RidesService", "Exception in getRides", e)
            null
        }
    }
}