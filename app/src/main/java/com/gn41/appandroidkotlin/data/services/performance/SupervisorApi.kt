package com.gn41.appandroidkotlin.data.services.performance

import com.gn41.appandroidkotlin.data.dto.performance.AddDurationRequestDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SupervisorApi {
    @POST("rest/v1/performance_times")
    suspend fun addDuration(@Header("apiKey") apiKey: String,
                       @Body request: AddDurationRequestDto
    )
}