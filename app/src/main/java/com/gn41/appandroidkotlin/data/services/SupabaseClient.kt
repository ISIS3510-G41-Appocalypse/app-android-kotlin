package com.gn41.appandroidkotlin.data.services

import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.services.auth.AuthApi
import com.gn41.appandroidkotlin.data.services.rides.RideApi
import com.gn41.appandroidkotlin.data.services.userId.UserIdApi
import com.gn41.appandroidkotlin.data.services.rides.RidesApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {

    private val BASE_URL = BuildConfig.SUPABASE_URL

    // Single Retrofit instance shared by all APIs
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