package com.gn41.appandroidkotlin.data.services

import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.services.auth.AuthApi
import com.gn41.appandroidkotlin.data.services.reservations.ReservationsApi
import com.gn41.appandroidkotlin.data.services.rides.RidesApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {
    private val BASE_URL = BuildConfig.SUPABASE_URL

    // instancia unica de retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val ridesApi: RidesApi by lazy {
        retrofit.create(RidesApi::class.java)
    }

    val reservationsApi: ReservationsApi by lazy {
        retrofit.create(ReservationsApi::class.java)
    }
}