package com.gn41.appandroidkotlin.localStorage

data class RatingDraftDto(
    val authId: String,
    val rideId: Int,
    val ratingType: String,
    val targetUserId: Int,
    val punctuality: Int,
    val behavior: Int,
    val communication: Int,
    val security: Int,
    val paymentPunctuality: Int,
    val updatedAt: Long
)

