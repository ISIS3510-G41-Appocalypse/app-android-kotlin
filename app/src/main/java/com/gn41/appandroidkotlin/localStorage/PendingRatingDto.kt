package com.gn41.appandroidkotlin.localStorage

data class PendingRatingDto(
    val authId: String,
    val rideId: Int,
    val ratingType: String,
    val title: String,
    val message: String,
    val buttonText: String,
    val riderId: Int? = null,
    val driverId: Int? = null,
    val driverName: String? = null,
    val ridersToRate: List<PendingRiderToRateDto>? = null,
    val updatedAt: Long
)

data class PendingRiderToRateDto(
    val riderId: Int,
    val name: String,
    val rating: Double? = null
)

