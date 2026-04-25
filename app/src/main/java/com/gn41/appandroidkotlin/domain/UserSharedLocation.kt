package com.gn41.appandroidkotlin.domain

data class UserSharedLocation(
    val id: Int? = null,
    val userId: Int,
    val rideId: Int,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String,
    val isSharingEnabled: Boolean
)