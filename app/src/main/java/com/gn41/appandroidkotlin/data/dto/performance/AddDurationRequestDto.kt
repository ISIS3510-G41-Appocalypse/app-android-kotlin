package com.gn41.appandroidkotlin.data.dto.performance

data class AddDurationRequestDto(
    val feature: String,
    val duration: Double,
    val source: String,
    val platform: String
)