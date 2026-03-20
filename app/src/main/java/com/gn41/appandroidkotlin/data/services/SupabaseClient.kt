package com.gn41.appandroidkotlin.data.services

import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.services.auth.AuthApi
import com.gn41.appandroidkotlin.data.services.rides.RideApi
import com.gn41.appandroidkotlin.data.services.userId.UserIdApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {

    private val BASE_URL = BuildConfig.SUPABASE_URL

    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    val rideApi: RideApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RideApi::class.java)
    }

    val userIdApi: UserIdApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserIdApi::class.java)
    }
}