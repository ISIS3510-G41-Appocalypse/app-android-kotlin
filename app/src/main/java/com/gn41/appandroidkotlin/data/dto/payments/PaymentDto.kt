package com.gn41.appandroidkotlin.data.dto.payments

import com.google.gson.annotations.SerializedName

data class PaymentDto (
    val amount: Int,
    val state: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String
)

data class PaymentDriverDtoRequest (
    @SerializedName("p_driver_id")
    val pDriverId: Int,
    @SerializedName("r_id")
    val rId: Int
)

data class PaymentRiderDtoRequest (
    @SerializedName("p_rider_id")
    val pRiderId: Int,
    @SerializedName("r_id")
    val rId: Int
)