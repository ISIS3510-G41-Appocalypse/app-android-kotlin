package com.gn41.appandroidkotlin.data.services

import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.services.auth.AuthApi
import com.gn41.appandroidkotlin.data.services.rides.RideApi
import com.gn41.appandroidkotlin.data.services.userId.UserIdApi
import com.gn41.appandroidkotlin.data.services.rides.RidesApi
import com.gn41.appandroidkotlin.data.services.vehicles.VehicleApi
import com.gn41.appandroidkotlin.data.services.zones.ZoneApi
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

    val rideApi: RideApi by lazy {
        retrofit.create(RideApi::class.java)
    }

    val userIdApi: UserIdApi by lazy {
        retrofit.create(UserIdApi::class.java)
    }

    val zoneApi: ZoneApi by lazy {
        retrofit.create(ZoneApi::class.java)
    }

    val vehicleApi: VehicleApi by lazy {
        retrofit.create(VehicleApi::class.java)
    }
}