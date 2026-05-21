package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.ratings.RateDriverRequestDto
import com.gn41.appandroidkotlin.data.dto.ratings.RateRiderRequestDto
import com.gn41.appandroidkotlin.data.services.ratings.RatingService

class RatingRepository(
    private val ratingService: RatingService
) {

    suspend fun rateDriver(token: String, body: RateDriverRequestDto): Boolean {
        return ratingService.rateDriver(token, body)
    }

    suspend fun rateRider(token: String, body: RateRiderRequestDto): Boolean {
        return ratingService.rateRider(token, body)
    }
}

