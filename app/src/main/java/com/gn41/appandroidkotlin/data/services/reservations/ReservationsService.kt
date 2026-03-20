package com.gn41.appandroidkotlin.data.services.reservations

import android.util.Log
import com.gn41.appandroidkotlin.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ReservationsService {
    private val reservationsApi: ReservationsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReservationsApi::class.java)
    }

    suspend fun getReservations(userId: String, token: String): List<ReservationDto>? {
        return try {
            val response = reservationsApi.getReservations(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                riderId = "eq.$userId"
            )

            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("ReservationsService", "getReservations failed: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ReservationsService", "Exception in getReservations", e)
            null
        }
    }

    suspend fun createReservation(rideId: Int, userId: String, token: String): Boolean {
        return try {
            val response = reservationsApi.createReservation(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                request = CreateReservationRequest(
                    ride_id = rideId,
                    rider_id = userId,
                    state = "PENDIENTE"
                )
            )

            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ReservationsService", "Exception in createReservation", e)
            false
        }
    }
}


