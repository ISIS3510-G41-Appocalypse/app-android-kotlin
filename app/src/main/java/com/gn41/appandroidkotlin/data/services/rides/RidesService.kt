package com.gn41.appandroidkotlin.data.services.rides

import android.util.Log
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.rides.RideDto
import com.gn41.appandroidkotlin.data.services.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RidesService {
    private val ridesApi = SupabaseClient.ridesApi

    private val enrichedSelect = "*,drivers(*,users(*)),vehicles(*),zones(*),reservations(state)"

    suspend fun getRides(token: String): List<RideDto>? = withContext(Dispatchers.IO){
        return@withContext try {
            if (BuildConfig.DEBUG) {
                Log.d("RidesService", "URL: ${BuildConfig.SUPABASE_URL}")
                Log.d("RidesService", "KEY ok: ${BuildConfig.SUPABASE_KEY.isNotEmpty()}")
            }

            val response = ridesApi.getRides(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                select = enrichedSelect,
                order = "drivers(rating).desc.nullslast"
            )

            if (BuildConfig.DEBUG) {
                Log.d("RidesService", "HTTP ${response.code()}")
            }

            if (response.isSuccessful) {
                if (BuildConfig.DEBUG) {
                    Log.d("RidesService", "OK: ${response.body()?.size} rides")
                }
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

    suspend fun getUpcomingOfferedRides(token: String): List<RideDto>? = withContext(Dispatchers.IO) {
        return@withContext try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val nowTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val dateFilter = "(date.gt.$today,and(date.eq.$today,departure_time.gte.$nowTime))"

            val response = ridesApi.getUpcomingOfferedRides(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                select = enrichedSelect,
                order = "drivers(rating).desc.nullslast",
                state = "eq.OFERTADO",
                dateFilter = dateFilter
            )

            if (response.isSuccessful) {
                if (BuildConfig.DEBUG) {
                    Log.d("RidesService", "Upcoming offered rides loaded: ${response.body()?.size ?: 0}")
                }
                response.body()
            } else {
                val err = response.errorBody()?.string()
                Log.e("RidesService", "Upcoming offered rides error ${response.code()}: $err")
                null
            }
        } catch (e: Exception) {
            Log.e("RidesService", "Upcoming offered rides exception: ${e.message}", e)
            null
        }
    }

    suspend fun getRiderDriverRecommendation(
        riderId: Int,
        driverId: Int,
        token: String
    ): Double? = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = ridesApi.getRiderDriverRecommendation(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                riderId = "eq.$riderId",
                driverId = "eq.$driverId",
                limit = 1
            )

            if (!response.isSuccessful) {
                null
            } else {
                response.body()
                    ?.firstOrNull()
                    ?.rating
                    ?.coerceIn(0.0, 5.0)
            }
        } catch (_: Exception) {
            null
        }
    }
}
