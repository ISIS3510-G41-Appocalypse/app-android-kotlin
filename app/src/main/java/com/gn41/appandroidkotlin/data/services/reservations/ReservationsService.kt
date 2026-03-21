package com.gn41.appandroidkotlin.data.services.reservations

import android.util.Log
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.services.SupabaseClient

class ReservationsService {

    private val reservationsApi = SupabaseClient.reservationsApi

    suspend fun getUserByAuthId(authId: String, token: String): UserSimpleDto? {
        return try {
            Log.d("ReservationsService", "getUserByAuthId authId=$authId")

            val response = reservationsApi.getUserByAuthId(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                authId = "eq.$authId"
            )

            Log.d("ReservationsService", "getUserByAuthId code=${response.code()}")

            if (response.isSuccessful) {
                response.body()?.firstOrNull()
            } else {
                Log.e("ReservationsService", "getUserByAuthId error=${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ReservationsService", "Exception in getUserByAuthId", e)
            null
        }
    }

    suspend fun getRiderByUserId(userId: Int, token: String): RiderSimpleDto? {
        return try {
            Log.d("ReservationsService", "getRiderByUserId userId=$userId")

            val response = reservationsApi.getRiderByUserId(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                userId = "eq.$userId"
            )

            Log.d("ReservationsService", "getRiderByUserId code=${response.code()}")

            if (response.isSuccessful) {
                response.body()?.firstOrNull()
            } else {
                Log.e("ReservationsService", "getRiderByUserId error=${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ReservationsService", "Exception in getRiderByUserId", e)
            null
        }
    }

    suspend fun getReservations(riderId: Int, token: String): List<ReservationDto>? {
        return try {
            Log.d("ReservationsService", "getReservations riderId=$riderId")

            val response = reservationsApi.getReservations(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                riderId = "eq.$riderId"
            )

            Log.d("ReservationsService", "getReservations code=${response.code()}")

            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("ReservationsService", "getReservations error=${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ReservationsService", "Exception in getReservations", e)
            null
        }
    }

    suspend fun createReservation(
        rideId: Int,
        riderId: Int,
        meetingPoint: String,
        destinationPoint: String,
        token: String
    ): Boolean {
        return try {
            Log.d(
                "ReservationsService",
                "createReservation rideId=$rideId riderId=$riderId meetingPoint=$meetingPoint destinationPoint=$destinationPoint"
            )

            val response = reservationsApi.createReservation(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                request = CreateReservationRequest(
                    ride_id = rideId,
                    rider_id = riderId,
                    meeting_point = meetingPoint,
                    destination_point = destinationPoint,
                    state = "PENDIENTE",
                    on_time_rider = true,
                    on_time_driver = true
                )
            )

            Log.d("ReservationsService", "createReservation code=${response.code()}")

            if (!response.isSuccessful) {
                Log.e("ReservationsService", "createReservation error=${response.errorBody()?.string()}")
            }

            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ReservationsService", "Exception in createReservation", e)
            false
        }
    }
}