package com.gn41.appandroidkotlin.data.dto.recommendations

import com.google.gson.annotations.SerializedName

data class RiderDriverRecommendationDto(
    @SerializedName("rider_id")
    val riderId: Int? = null,
    @SerializedName("driver_id")
    val driverId: Int? = null,
    val rating: Double? = null
)

