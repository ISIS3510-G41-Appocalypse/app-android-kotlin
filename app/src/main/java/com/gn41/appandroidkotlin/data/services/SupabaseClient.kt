package com.gn41.appandroidkotlin.data.services

import android.util.Log
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.local.SessionEvents
import com.gn41.appandroidkotlin.data.services.auth.AuthApi
import com.gn41.appandroidkotlin.data.services.reservations.ReservationsApi
import com.gn41.appandroidkotlin.data.services.rides.RideApi
import com.gn41.appandroidkotlin.data.services.rides.RidesApi
import com.gn41.appandroidkotlin.data.services.trips.TripApi
import com.gn41.appandroidkotlin.data.services.userId.UserIdApi
import com.gn41.appandroidkotlin.data.services.vehicles.VehicleApi
import com.gn41.appandroidkotlin.data.services.zones.ZoneApi
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {
    private val BASE_URL = BuildConfig.SUPABASE_URL

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code() == 401) {
            Log.e("SupabaseClient", "Authentication failed.")
            runBlocking {
                SessionEvents.emitSessionExpired()
            }
        }
        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
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

    val vehicleApi: VehicleApi by lazy {
        retrofit.create(VehicleApi::class.java)
    }

    val zoneApi: ZoneApi by lazy {
        retrofit.create(ZoneApi::class.java)
    }

    val reservationsApi: ReservationsApi by lazy {
        retrofit.create(ReservationsApi::class.java)
    }

    val tripApi: TripApi by lazy {
        retrofit.create(TripApi::class.java)
    }
}