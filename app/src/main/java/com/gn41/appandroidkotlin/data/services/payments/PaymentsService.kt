package com.gn41.appandroidkotlin.data.services.payments

import com.gn41.appandroidkotlin.data.dto.payments.RideDriverPaymentDtoRequest
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.payments.PaymentDriverDtoRequest
import com.gn41.appandroidkotlin.data.dto.payments.PaymentDto
import com.gn41.appandroidkotlin.data.dto.payments.PaymentRiderDtoRequest
import com.gn41.appandroidkotlin.data.dto.payments.RidePaymentDto
import com.gn41.appandroidkotlin.data.dto.payments.RideRiderPaymentDtoRequest
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.services.SupabaseClient
import com.gn41.appandroidkotlin.data.services.userId.UserIdService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentsService (
    private val sessionManager: SessionManager,
    private val userIdService: UserIdService
) {
    private val paymentsApi = SupabaseClient.paymentsApi

    suspend fun getRides(selectedRole: String) : List<RidePaymentDto> = withContext(Dispatchers.IO) {
        val token = sessionManager.getToken()

        val userId = userIdService.getUserByAuthId().id

        if (selectedRole == "Conductor") {
            val driverId = userIdService.getDriverIdByUserId(userId)
            return@withContext paymentsApi.get_driver_rides_with_payments(
                apiKey = BuildConfig.SUPABASE_KEY,
                authorization = "Bearer $token",
                request = RideDriverPaymentDtoRequest(driverId)
            )
        }
        else
        {
            val riderId = userIdService.getRiderIdByUserId(userId)
            return@withContext paymentsApi.get_rider_rides_with_payments(
                apiKey = BuildConfig.SUPABASE_KEY,
                authorization = "Bearer $token",
                request = RideRiderPaymentDtoRequest(riderId)
            )
        }
    }

    suspend fun getPayments(selectedRole: String, rideId: Int) : List<PaymentDto> = withContext(Dispatchers.IO) {
        val token = sessionManager.getToken()

        val userId = userIdService.getUserByAuthId().id

        if (selectedRole == "Conductor") {
            val driverId = userIdService.getDriverIdByUserId(userId)
            return@withContext paymentsApi.get_driver_payments(
                apiKey = BuildConfig.SUPABASE_KEY,
                authorization = "Bearer $token",
                request = PaymentDriverDtoRequest(driverId, rideId)
            )
        }
        else{
            val riderId = userIdService.getRiderIdByUserId(userId)
            return@withContext paymentsApi.get_rider_payments(
                apiKey = BuildConfig.SUPABASE_KEY,
                authorization = "Bearer $token",
                request = PaymentRiderDtoRequest(riderId, rideId)
            )
        }
    }
}