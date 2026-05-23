package com.gn41.appandroidkotlin.presentation.viewmodels

data class RateUserUiModel(
    val riderId: Int? = null,
    val driverId: Int? = null,
    val name: String,
    val rating: Double? = null
)

data class RatingUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val rideId: Int = 0,
    val ratingType: String = "driver",
    val riderId: Int? = null,
    val driverId: Int? = null,
    val selectedRiderId: Int? = null,
    val ridersToRate: List<RateUserUiModel> = emptyList(),
    val punctuality: Int = 0,
    val behavior: Int = 0,
    val communication: Int = 0,
    val security: Int = 0,
    val paymentPunctuality: Int = 0
)

