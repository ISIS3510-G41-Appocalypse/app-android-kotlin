package com.gn41.appandroidkotlin.data.services.ratings

import android.util.Log
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.ratings.RateDriverRequestDto
import com.gn41.appandroidkotlin.data.dto.ratings.RateRiderRequestDto
import com.gn41.appandroidkotlin.data.services.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RatingService {

    private val ratingApi = SupabaseClient.ratingApi

    suspend fun rateDriver(token: String, body: RateDriverRequestDto): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = ratingApi.rateDriver(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                body = body
            )

            if (!response.isSuccessful) {
                Log.e("RatingService", "rateDriver error=${response.code()} ${response.errorBody()?.string()}")
            }

            response.isSuccessful
        } catch (e: Exception) {
            Log.e("RatingService", "rateDriver exception", e)
            false
        }
    }

    suspend fun rateRider(token: String, body: RateRiderRequestDto): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = ratingApi.rateRider(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                body = body
            )

            if (!response.isSuccessful) {
                Log.e("RatingService", "rateRider error=${response.code()} ${response.errorBody()?.string()}")
            }

            response.isSuccessful
        } catch (e: Exception) {
            Log.e("RatingService", "rateRider exception", e)
            false
        }
    }
}

