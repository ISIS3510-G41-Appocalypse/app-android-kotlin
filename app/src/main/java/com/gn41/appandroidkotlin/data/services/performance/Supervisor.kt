package com.gn41.appandroidkotlin.data.services.performance

import com.gn41.appandroidkotlin.data.services.SupabaseClient
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.performance.AddDurationRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object Supervisor {

    val supervisorApi = SupabaseClient.supervisorApi
    fun addDuration(feature: String, duration: Double) {
        val request = AddDurationRequestDto(
            feature = feature,
            duration = duration
        )
        GlobalScope.launch(Dispatchers.IO) {
            supervisorApi.addDuration(
                apiKey = BuildConfig.SUPABASE_KEY,
                request = request
            )
        }
    }
}