package com.gn41.appandroidkotlin.data.services.rides

import android.util.Log
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.rides.RideDto
import com.gn41.appandroidkotlin.data.services.SupabaseClient

class RidesService {
    private val ridesApi = SupabaseClient.ridesApi

    private val enrichedSelect = "*,drivers(*,users(*)),vehicles(*),zones(*),reservations(state)"

    suspend fun getRides(token: String): List<RideDto>? {
        return try {
            Log.d("RidesService", "URL: ${BuildConfig.SUPABASE_URL}")
            Log.d("RidesService", "KEY ok: ${BuildConfig.SUPABASE_KEY.isNotEmpty()}")
            
            val response = ridesApi.getRides(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                select = enrichedSelect,
                order = "drivers(rating).desc.nullslast"
            )

            Log.d("RidesService", "HTTP ${response.code()}")
            
            if (response.isSuccessful) {
                Log.d("RidesService", "OK: ${response.body()?.size} rides")
                response.body()
            } else {
                val err = response.errorBody()?.string()
                Log.e("RidesService", "Error ${response.code()}: $err")
                null
            }
        } catch (e: Exception) {
            Log.e("RidesService", "Exception: ${e.message}", e)
            null
        }
    }
}
