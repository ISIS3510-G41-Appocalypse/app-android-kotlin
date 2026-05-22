package com.gn41.appandroidkotlin.data.services.payments

import com.gn41.appandroidkotlin.data.dto.payments.PaymentDriverDtoRequest
import com.gn41.appandroidkotlin.data.dto.payments.PaymentDto
import com.gn41.appandroidkotlin.data.dto.payments.PaymentRiderDtoRequest
import com.gn41.appandroidkotlin.data.dto.payments.RideDriverPaymentDtoRequest
import com.gn41.appandroidkotlin.data.dto.payments.RidePaymentDto
import com.gn41.appandroidkotlin.data.dto.payments.RideRiderPaymentDtoRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


interface PaymentsApi {
    @POST("rest/v1/rpc/get_driver_rides_with_payments")
    suspend fun get_driver_rides_with_payments(
        @Header("apiKey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body request: RideDriverPaymentDtoRequest
    ) : List<RidePaymentDto>

    @POST("rest/v1/rpc/get_rider_rides_with_payments")
    suspend fun get_rider_rides_with_payments(
        @Header("apiKey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body request: RideRiderPaymentDtoRequest
    ) : List<RidePaymentDto>

    @POST("rest/v1/rpc/get_driver_payments")
    suspend fun get_driver_payments(
        @Header("apiKey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body request: PaymentDriverDtoRequest
    ) : List<PaymentDto>

    @POST("rest/v1/rpc/get_rider_payments")
    suspend fun get_rider_payments(
        @Header("apiKey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body request: PaymentRiderDtoRequest
    ) : List<PaymentDto>
}