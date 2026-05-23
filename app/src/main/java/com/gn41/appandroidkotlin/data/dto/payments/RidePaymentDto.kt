package com.gn41.appandroidkotlin.data.dto.payments

import com.google.gson.annotations.SerializedName

data class RidePaymentDto (
    val id: Int,
    val source: String,
    val destination: String,
    val date: String,

    @SerializedName("departure_time")
    val departureTime: String
)

data class RideDriverPaymentDtoRequest (
    @SerializedName("p_driver_id")
    val pDriverId: Int
)

data class RideRiderPaymentDtoRequest (
    @SerializedName("p_rider_id")
    val pRiderId: Int
)